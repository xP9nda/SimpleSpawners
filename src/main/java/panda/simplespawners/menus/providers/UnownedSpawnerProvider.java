package panda.simplespawners.menus.providers;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import panda.simplespawners.SimpleSpawners;
import panda.simplespawners.handlers.ConfigHandler;

import java.util.List;

public class UnownedSpawnerProvider implements InventoryProvider {

    // Variables
    private int rows;
    private String menuTitle;

    private String mobType;
    private String spawnerOwner;

    private Plugin simpleSpawnersPlugin;
    private InventoryManager inventoryManager;
    private ConfigHandler configHandler;
    public static SmartInventory ownedSpawnerInventory;
    private static final MiniMessage miniMsg = MiniMessage.miniMessage();

    // Constructor method
    public UnownedSpawnerProvider(Plugin loader) {
        simpleSpawnersPlugin = loader;
        SimpleSpawners simpleSpawnersPluginClass = (SimpleSpawners) Bukkit.getPluginManager().getPlugin("SimpleSpawners");
        inventoryManager = simpleSpawnersPluginClass.getInventoryManager();
        configHandler = simpleSpawnersPluginClass.getConfigHandler();
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

    @Override
    public void init(Player player, InventoryContents contents) {
        // Called when an inventory is opened for the player

        // Get the items and add them to the menu
        ConfigurationSection menuToOpen = configHandler.getUnownedMenuConfigurationSection();
        ConfigurationSection menuItemsSection = menuToOpen.getConfigurationSection("items");

        for (String itemName : menuItemsSection.getKeys(false)) {
            ConfigurationSection itemSection = menuItemsSection.getConfigurationSection(itemName);

            // Get item properties from the config
            int slotRow = itemSection.getInt("slotRow");
            int slotCol = itemSection.getInt("slotColumn");
            int quantity = itemSection.getInt("quantity");
            String materialName = itemSection.getString("material");
            String displayName = itemSection.getString("displayName");
            List<String> lore = itemSection.getStringList("lore");

            // Create a new itemstack
            ItemStack itemStack;

            // Set up the item
            if ((materialName.isEmpty() || materialName == null) && Material.matchMaterial(materialName) != null) {
                itemStack = new ItemStack(Material.BARRIER);
            } else {
                itemStack = new ItemStack(Material.matchMaterial(materialName));
            }

            itemStack.setAmount(quantity);
            itemStack.editMeta(meta -> {
                meta.displayName(miniMsg.deserialize(
                    displayName,
                    Placeholder.unparsed("mob", getMobType()),
                    Placeholder.unparsed("owner", getSpawnerOwner())
                ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                if (lore != null || lore.isEmpty()) {
                    meta.lore(
                            lore.stream().map(loreMsg -> miniMsg.deserialize(
                                    loreMsg,
                                    Placeholder.unparsed("mob", this.getMobType()),
                                    Placeholder.unparsed("owner", this.getSpawnerOwner())
                            ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList()
                    );
                }
            });

            contents.set(slotRow, slotCol, ClickableItem.of(itemStack, event -> {
                if (event.isLeftClick()) {
                    player.sendMessage(miniMsg.deserialize("left clicked item"));
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
}