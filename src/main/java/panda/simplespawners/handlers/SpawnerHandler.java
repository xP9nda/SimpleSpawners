package panda.simplespawners.handlers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.incendo.interfaces.core.util.Vector2;
import org.incendo.interfaces.paper.type.ChestInterface;
import panda.simplespawners.SimpleSpawners;
import panda.simplespawners.data.DataSerialization;
import panda.simplespawners.data.SpawnerData;
import panda.simplespawners.menus.MenuHandler;
import panda.simplespawners.menus.MenuInterface;
import panda.simplespawners.menus.MenuItem;
import panda.simplespawners.utils.SpawnerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SpawnerHandler implements Listener {

    // Variables
    private Plugin simpleSpawnersPlugin;
    private ConfigHandler configHandler;
    private SpawnerUtils spawnerUtils;
    private DataSerialization dataSerialization;
    private MenuHandler menuHandler;
    private MiniMessage miniMsg = MiniMessage.miniMessage();

    private final NamespacedKey spawnerItemKey;
    private final NamespacedKey spawnerOwnerKey;

    private HashMap<UUID, SpawnerData> cachedSpawners = new HashMap<>();

    // Constructor method
    public SpawnerHandler(Plugin loader) {
        simpleSpawnersPlugin = loader;

        // Get the plugin class and set up variables appropriately
        SimpleSpawners simpleSpawnersPluginClass = (SimpleSpawners) Bukkit.getPluginManager().getPlugin("SimpleSpawners");
        configHandler = simpleSpawnersPluginClass.getConfigHandler();
        spawnerUtils = simpleSpawnersPluginClass.getSpawnerUtils();
        dataSerialization = simpleSpawnersPluginClass.getDataSerialization();
        menuHandler = simpleSpawnersPluginClass.getMenuHandler();

        // Set up namespace item keys
        spawnerItemKey = new NamespacedKey(simpleSpawnersPlugin, "item");
        spawnerOwnerKey = new NamespacedKey(simpleSpawnersPlugin, "owner");
    }

    // Spawner event method
    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        // If the config demands default spawner behaviour to be toggled off, cancel the spawn event
        if (!configHandler.isSpawnerDefaultBehaviour()) {
            event.setCancelled(true);
//            simpleSpawnersPlugin.getSLF4JLogger().info("Spawner event cancelled.");
        }
    }

    // Spawner place method
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        ItemStack handItem = event.getItemInHand();

        // Check if the placed block was a spawner
        if (!(placedBlock.getType() == Material.SPAWNER)) {
            return;
        }

        Player player = event.getPlayer();
        CreatureSpawner spawnerBlock = (CreatureSpawner) placedBlock.getState();
        PersistentDataContainer dataContainer = spawnerBlock.getPersistentDataContainer();

        // Set the block's NBT data
        dataContainer.set(spawnerOwnerKey, PersistentDataType.STRING, player.getUniqueId().toString());

        // Update the spawner's state
        spawnerBlock.update();

        // Alert the player they have placed a private spawner
        player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerPlaceMessage()));
    }

    // Spawner break method
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block placedBlock = event.getBlock();

        // Check if the placed block was a spawner
        if (!(placedBlock.getType() == Material.SPAWNER)) {
            return;
        }

        // Cancel the event
        event.setCancelled(true);

        // Alert the player that spawners can not be broken
        Player player = event.getPlayer();
        player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerAttemptBreakMessage()));
    }

    // Spawner interact method
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();

        // Check if the interaction was a right click and was not from the offhand slot
        if (!event.getAction().name().contains("RIGHT_CLICK") || event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        // Check if the block was a spawner and if the player is sneaking
        Player player = event.getPlayer();
        if ((clickedBlock == null || !(clickedBlock.getType() == Material.SPAWNER)) || !player.isSneaking()) {
            return;
        }

        // Cancel the event
        event.setCancelled(true);

        // Get the block data information
        CreatureSpawner spawner = (CreatureSpawner) clickedBlock.getState();
        PersistentDataContainer dataContainer = spawner.getPersistentDataContainer();
        String spawnerOwnerUUIDString = dataContainer.get(spawnerOwnerKey, PersistentDataType.STRING);

        // Check if the spawner has no owner
        // select the correct menu to open and set up variables
        ConfigurationSection menuToOpen;
        UUID spawnerOwnerUUID;
        if (spawnerOwnerUUIDString == null) {
            menuToOpen = configHandler.getUnownedMenuConfigurationSection();
            spawnerOwnerUUID = null;
            spawnerOwnerUUIDString = "";
        } else {
            menuToOpen = configHandler.getPrimaryMenuConfigurationSection();
            spawnerOwnerUUID = UUID.fromString(spawnerOwnerUUIDString);

            // Check if the player who interacted with the spawner owns the spawner and send an appropriate message
            if (!spawnerOwnerUUIDString.equals(player.getUniqueId().toString())) {
                player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerOpenFailedMessage()));
                return;
            }
            player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerOpenMessage()));
        }

        final String finalSpawnerOwnerUUIDString = spawnerOwnerUUIDString;

        // Construct and open the spawner menu
        ConfigurationSection menuInformationSection = menuToOpen.getConfigurationSection("information");
        int rows = menuInformationSection.getInt("rows");
        String title = menuInformationSection.getString("title");
        Material backgroundMaterial = Material.matchMaterial(menuInformationSection.getString("backgroundMaterial"));

        // Create a new menu interface and set its properties
        MenuInterface spawnerMenu = new MenuInterface();
        spawnerMenu.setRows(rows);
        spawnerMenu.setMenuTitle(title);
        spawnerMenu.setBackgroundMaterial(backgroundMaterial);

        // Get the menu items and set up the menu interface
        List<MenuItem> menuItems = new ArrayList<>();
        ConfigurationSection menuItemsSection = menuToOpen.getConfigurationSection("items");

        for (String itemName : menuItemsSection.getKeys(false)) {
            ConfigurationSection itemSection = menuItemsSection.getConfigurationSection(itemName);

            // Item properties
            int slotX = itemSection.getInt("slotX");
            int slotY = itemSection.getInt("slotY");
            int quantity = itemSection.getInt("quantity");
            Material material = Material.matchMaterial(itemSection.getString("material"));
            String displayName = itemSection.getString("displayName");
            List<String> lore = itemSection.getStringList("lore");

            // Set up new menu item object
            MenuItem menuItem = new MenuItem();
            menuItem.setItemName(displayName);
            menuItem.setLore(lore);
            menuItem.setMaterial(material);
            menuItem.setSlot(Vector2.at(slotX, slotY));
            menuItem.setStackQuantity(quantity);

            menuItems.add(menuItem);
        }
        spawnerMenu.setMenuItems(menuItems);

        // Request a menu creation
        ChestInterface interfaceMenu = menuHandler.createMenuInterface(spawnerMenu);

        // Show the menu to the player
        menuHandler.showMenuInterfaceToPlayer(interfaceMenu, player);
    }

    // Inventory click method
//    @EventHandler
//    public void onInventoryClick(InventoryClickEvent event) {
//        ItemStack clickedItem = event.getCurrentItem();
//
//        // Check if the item exists and has the spawner inventory item key, if it does then cancel the event
//        if (clickedItem != null) {
//            if (!clickedItem.hasItemMeta()) {
//                return;
//            }
//
//            String itemKey = clickedItem.getItemMeta().getPersistentDataContainer().get(spawnerItemKey, PersistentDataType.STRING);
//            if (itemKey == null) {
//                return;
//            }
//
//            if (itemKey.equals("spawnerInventoryItem")) {
//                // Cancel the event
//                event.setCancelled(true);
//            }
//        }
//    }
}
