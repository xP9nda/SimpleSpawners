package panda.simplespawners.menus.providers;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import panda.simplespawners.SimpleSpawners;
import panda.simplespawners.handlers.ConfigHandler;
import panda.simplespawners.utils.SpawnerUtils;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public class TrustingProvider implements InventoryProvider {

    // Variables
    private int rows;
    private String menuTitle;
    private int pagedItemAmount;
    private String spawnerOwner;
    private OwnedSpawnerProvider previousMenu;
    private Block spawnerBlock;
    private NamespacedKey spawnerMemberKey;

    private final InventoryManager inventoryManager;
    private final ConfigHandler configHandler;
    private final SpawnerUtils spawnerUtils;
    public static SmartInventory trustingInventory;
    private static final MiniMessage miniMsg = MiniMessage.miniMessage();
    private static final CommandSender consoleSender = Bukkit.getServer().getConsoleSender();

    // Constructor method
    public TrustingProvider() {
        SimpleSpawners simpleSpawnersPluginClass = (SimpleSpawners) Bukkit.getPluginManager().getPlugin("SimpleSpawners");
        assert simpleSpawnersPluginClass != null;
        inventoryManager = simpleSpawnersPluginClass.getInventoryManager();
        configHandler = simpleSpawnersPluginClass.getConfigHandler();
        spawnerUtils = simpleSpawnersPluginClass.getSpawnerUtils();
    }

    // Method to build the inventory once all the properties have been set
    public void buildInventory() {
        trustingInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .id("TrustingProvider")
                .provider(this)
                .size(this.getRows(), 9)
                .title(this.getMenuTitle())
                .build();
    }

    // Method to open the built inventory to the given player
    public void openBuiltInventory(Player player) {
        trustingInventory.open(player);
    }

    public void runInventoryClickEvent(Player player, List<String> commands, Pagination pagination) {
        for (String commandString : commands) {
            if (commandString.equalsIgnoreCase("[close]")) {
                player.closeInventory();
                continue;
            } else if (commandString.equalsIgnoreCase("[return]")) {
                getPreviousMenu().openBuiltInventory(player);
                continue;
            } else if (commandString.equalsIgnoreCase("[pagenext]")) {
                trustingInventory.open(player, pagination.next().getPage());
                continue;
            } else if (commandString.equalsIgnoreCase("[pageprevious]")) {
                trustingInventory.open(player, pagination.previous().getPage());
                continue;
            }

            Bukkit.dispatchCommand(consoleSender, commandString);
        }
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        // Called when an inventory is opened for the player

        // Get the items and add them to the menu
        ConfigurationSection menuToOpen = configHandler.getSpawnerTrustMenuConfigurationSection();
        ConfigurationSection menuItemsSection = menuToOpen.getConfigurationSection("items");
        Pagination pagination = contents.pagination();

        // Set up normal menu items
        assert menuItemsSection != null;
        for (String itemName : menuItemsSection.getKeys(false)) {
            ConfigurationSection itemSection = menuItemsSection.getConfigurationSection(itemName);

            // Get item properties from the config
            assert itemSection != null;
            int slotRow = itemSection.getInt("slotRow");
            int slotCol = itemSection.getInt("slotColumn");
            int quantity = itemSection.getInt("quantity");
            String materialName = itemSection.getString("material");
            String displayName = itemSection.getString("displayName");
            List<String> lore = itemSection.getStringList("lore");

            // Create a new itemstack
            ItemStack itemStack;

            // Set up the item
            assert materialName != null;
            if (materialName.isEmpty() && Material.matchMaterial(materialName) != null) {
                itemStack = new ItemStack(Material.BARRIER);
            } else {
                itemStack = new ItemStack(Objects.requireNonNull(Material.matchMaterial(materialName)));
            }

            itemStack.setAmount(quantity);
            itemStack.editMeta(meta -> {
                assert displayName != null;
                meta.displayName(miniMsg.deserialize(
                        displayName,
                        Placeholder.unparsed("owner", getSpawnerOwner())
                ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                meta.lore(
                        lore.stream().map(loreMsg -> miniMsg.deserialize(
                                loreMsg,
                                Placeholder.unparsed("owner", this.getSpawnerOwner())
                        ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList()
                );
            });

            contents.set(slotRow, slotCol, ClickableItem.of(itemStack, e -> {
                // Cancel the event
                e.setCancelled(true);

                // Find any events that should be run
                List<String> leftClickCommands = itemSection.getStringList("leftClickCommands");
                List<String> rightClickCommands = itemSection.getStringList("rightClickCommands");

                // Run commands
                if (e.isLeftClick() && !leftClickCommands.isEmpty()) {
                    runInventoryClickEvent(player, leftClickCommands, pagination);
                }

                if (e.isRightClick() && !rightClickCommands.isEmpty()) {
                    runInventoryClickEvent(player, rightClickCommands, pagination);
                }
            }));
        }

        // Set up paged items
        ConfigurationSection menuInformationSection = menuToOpen.getConfigurationSection("information");
        assert menuInformationSection != null;
        ConfigurationSection pagedItemTemplate = menuInformationSection.getConfigurationSection("pagedItemTemplate");

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        ClickableItem[] pagedItems = new ClickableItem[onlinePlayers.size() - 1];
        int iterator = 0;

        for ( Player trustablePlayer : onlinePlayers ) {
            // Make sure that the player can not trust themselves to the spawner
            if (trustablePlayer.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            ItemStack trustablePlayerItemStack = new ItemStack(Material.PLAYER_HEAD);

            // Set up skull texture
            SkullMeta skullMeta = (SkullMeta) trustablePlayerItemStack.getItemMeta();
            skullMeta.setOwningPlayer(trustablePlayer);
            trustablePlayerItemStack.setItemMeta(skullMeta);

            // Set up other metadata
            trustablePlayerItemStack.editMeta(meta -> {
                assert pagedItemTemplate != null;
                meta.displayName(miniMsg.deserialize(
                        Objects.requireNonNull(pagedItemTemplate.getString("displayName")),
                        Placeholder.unparsed("player", trustablePlayer.getName())
                ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                meta.lore(
                        pagedItemTemplate.getStringList("lore").stream().map(loreMsg -> miniMsg.deserialize(
                                loreMsg
                        ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList()
                );
            });

            pagedItems[iterator] = (ClickableItem.of(trustablePlayerItemStack, e -> {
                // Cancel the event
                e.setCancelled(true);

                if (e.isLeftClick()) {
                    // Add the selected player to the spawner's trusted list
                    List<String> trustedPlayers = spawnerUtils.getStringListFromBlock(getSpawnerBlock(), getSpawnerMemberKey());

                    // Check that the player is not already trusted
                    if (trustedPlayers.contains(trustablePlayer.getUniqueId().toString())) {
                        if (!configHandler.getSpawnerTrustAlreadyTrusted().isEmpty()) {
                            player.sendMessage(configHandler.getSpawnerTrustAlreadyTrusted());
                        }
                        return;
                    }

                    trustedPlayers.add(trustablePlayer.getUniqueId().toString());
                    spawnerUtils.storeStringListInBlock(getSpawnerBlock(), trustedPlayers, getSpawnerMemberKey());
                }
            }));
            iterator++;
        }

        pagination.setItems(pagedItems);
        pagination.setItemsPerPage(getPagedItemAmount());
        pagination.addToIterator(contents.newIterator(
                SlotIterator.Type.HORIZONTAL,
                menuInformationSection.getInt("pagedItemsStartRow"),
                menuInformationSection.getInt("pagedItemsStartColumn")
        ));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        // Called every tick for the players with the inventory opened
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    public String getSpawnerOwner() {
        return spawnerOwner;
    }

    public void setSpawnerOwner(String spawnerOwner) {
        this.spawnerOwner = spawnerOwner;
    }


    public OwnedSpawnerProvider getPreviousMenu() {
        return previousMenu;
    }

    public void setPreviousMenu(OwnedSpawnerProvider previousMenu) {
        this.previousMenu = previousMenu;
    }

    public int getPagedItemAmount() {
        return pagedItemAmount;
    }

    public void setPagedItemAmount(int pagedItemAmount) {
        this.pagedItemAmount = pagedItemAmount;
    }

    public Block getSpawnerBlock() {
        return spawnerBlock;
    }

    public void setSpawnerBlock(Block spawnerBlock) {
        this.spawnerBlock = spawnerBlock;
    }

    public NamespacedKey getSpawnerMemberKey() {
        return spawnerMemberKey;
    }

    public void setSpawnerMemberKey(NamespacedKey spawnerMemberKey) {
        this.spawnerMemberKey = spawnerMemberKey;
    }
}