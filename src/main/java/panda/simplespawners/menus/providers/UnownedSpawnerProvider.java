package panda.simplespawners.menus.providers;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import panda.simplespawners.SimpleSpawners;
import panda.simplespawners.handlers.ConfigHandler;
import panda.simplespawners.handlers.SpawnerHandler;
import panda.simplespawners.utils.SpawnerUtils;
import java.util.List;
import java.util.Objects;

public class UnownedSpawnerProvider implements InventoryProvider {

    // Variables
    private int rows;
    private String menuTitle;

    private String mobType;
    private String spawnerOwner;
    private Location spawnerLocation;

    private final InventoryManager inventoryManager;
    private final ConfigHandler configHandler;
    private final SpawnerHandler spawnerHandler;
    private final SpawnerUtils spawnerUtils;
    private final Economy economy;
    public static SmartInventory ownedSpawnerInventory;
    private static final MiniMessage miniMsg = MiniMessage.miniMessage();
    private static final CommandSender consoleSender = Bukkit.getServer().getConsoleSender();

    // Constructor method
    public UnownedSpawnerProvider() {
        SimpleSpawners simpleSpawnersPluginClass = (SimpleSpawners) Bukkit.getPluginManager().getPlugin("SimpleSpawners");
        assert simpleSpawnersPluginClass != null;
        inventoryManager = simpleSpawnersPluginClass.getInventoryManager();
        configHandler = simpleSpawnersPluginClass.getConfigHandler();
        spawnerHandler = simpleSpawnersPluginClass.getSpawnerHandler();
        spawnerUtils = simpleSpawnersPluginClass.getSpawnerUtils();
        economy = simpleSpawnersPluginClass.getEconomy();
    }

    // Method to build the inventory once all the properties have been set
    public void buildInventory() {
        ownedSpawnerInventory = SmartInventory.builder()
                .manager(inventoryManager)
                .id("UnownedSpawnerInventory")
                .provider(this)
                .size(this.getRows(), 9)
                .title(this.getMenuTitle())
                .build();
    }

    // Method to open the built inventory to the given player
    public void openBuiltInventory(Player player) {
        ownedSpawnerInventory.open(player);
    }

    public void runInventoryClickEvent(Player player, List<String> commands) {
        for (String commandString : commands) {
            if (commandString.equalsIgnoreCase("[pickup]")) {
                // Check that the player has enough money
                if (economy.getBalance(player) >= configHandler.getUnownedMoneyPickupCost()) {
                    // Check that the player's inventory is not full
                    if (spawnerUtils.hasOpenSlot(player)) {
                        economy.withdrawPlayer(player, configHandler.getUnownedMoneyPickupCost());
                        player.closeInventory();
                        player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerPickupMessage()));
                        spawnerHandler.pickupSpawner(null, player, getSpawnerLocation());
                    } else {
                        player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerPickupFullInventoryMessage()));
                    }
                } else {
                    player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerPickupNotEnoughMoney()));
                }
                continue;
            } else if (commandString.equalsIgnoreCase("[close]")) {
                player.closeInventory();
                continue;
            }

            Bukkit.dispatchCommand(consoleSender, commandString);
        }
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        // Called when an inventory is opened for the player

        // Get the items and add them to the menu
        ConfigurationSection menuToOpen = configHandler.getUnownedMenuConfigurationSection();
        ConfigurationSection menuItemsSection = menuToOpen.getConfigurationSection("items");

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
                        Placeholder.unparsed("mob", getMobType()),
                        Placeholder.unparsed("owner", getSpawnerOwner()),
                        Placeholder.unparsed("money", spawnerUtils.formatNumberWithCommas(configHandler.getUnownedMoneyPickupCost()))
                ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                meta.lore(
                        lore.stream().map(loreMsg -> miniMsg.deserialize(
                                loreMsg,
                                Placeholder.unparsed("mob", this.getMobType()),
                                Placeholder.unparsed("owner", this.getSpawnerOwner()),
                                Placeholder.unparsed("money", spawnerUtils.formatNumberWithCommas(configHandler.getUnownedMoneyPickupCost()))
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
                    runInventoryClickEvent(player, leftClickCommands);
                }

                if (e.isRightClick() && !rightClickCommands.isEmpty()) {
                    runInventoryClickEvent(player, rightClickCommands);
                }
            }));
        }
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

    public String getMobType() {
        return mobType;
    }

    public void setMobType(String mobType) {
        this.mobType = mobType;
    }

    public String getSpawnerOwner() {
        return spawnerOwner;
    }

    public void setSpawnerOwner(String spawnerOwner) {
        this.spawnerOwner = spawnerOwner;
    }

    public Location getSpawnerLocation() {
        return spawnerLocation;
    }

    public void setSpawnerLocation(Location spawnerLocation) {
        this.spawnerLocation = spawnerLocation;
    }
}