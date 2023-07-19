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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import panda.simplespawners.SimpleSpawners;
import panda.simplespawners.data.DataSerialization;
import panda.simplespawners.data.SpawnerData;
import panda.simplespawners.menus.providers.OwnedSpawnerProvider;
import panda.simplespawners.menus.providers.UnownedSpawnerProvider;
import panda.simplespawners.utils.SpawnerUtils;

import java.util.HashMap;
import java.util.UUID;

public class SpawnerHandler implements Listener {

    // Variables
    private Plugin simpleSpawnersPlugin;
    private ConfigHandler configHandler;
    private SpawnerUtils spawnerUtils;
    private DataSerialization dataSerialization;
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
        String spawnerMobTypeString = spawnerUtils.capitalizeWords(spawnerUtils.getSpawnerMobType(clickedBlock).name().toLowerCase());
        if (spawnerOwnerUUIDString == null) {
            // Get the appropriate config sections
            menuToOpen = configHandler.getUnownedMenuConfigurationSection();
            ConfigurationSection menuInformationSection = menuToOpen.getConfigurationSection("information");

            // Open the unclaimed spawner menu
            UnownedSpawnerProvider inventoryToOpen = new UnownedSpawnerProvider(simpleSpawnersPlugin);
            inventoryToOpen.setRows(menuInformationSection.getInt("rows"));
            inventoryToOpen.setMenuTitle(menuInformationSection.getString("title"));

            inventoryToOpen.setMobType(spawnerMobTypeString);
            inventoryToOpen.setSpawnerOwner("Unowned");

            inventoryToOpen.buildInventory();
            inventoryToOpen.openBuiltInventory(player);
        } else {
            // Check if the player who interacted with the spawner owns the spawner and send an appropriate message
            if (!spawnerOwnerUUIDString.equals(player.getUniqueId().toString())) {
                player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerOpenFailedMessage()));
                return;
            }
            player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerOpenMessage()));

            // Get the appropriate config sections
            menuToOpen = configHandler.getPrimaryMenuConfigurationSection();
            ConfigurationSection menuInformationSection = menuToOpen.getConfigurationSection("information");

            // Open the spawner menu
            OwnedSpawnerProvider inventoryToOpen = new OwnedSpawnerProvider(simpleSpawnersPlugin);
            inventoryToOpen.setRows(menuInformationSection.getInt("rows"));
            inventoryToOpen.setMenuTitle(menuInformationSection.getString("title"));

            inventoryToOpen.setMobType(spawnerMobTypeString);
            inventoryToOpen.setSpawnerOwner(spawnerUtils.getPlayerNameFromUUID(UUID.fromString(spawnerOwnerUUIDString)));

            inventoryToOpen.buildInventory();
            inventoryToOpen.openBuiltInventory(player);
        }
    }
}
