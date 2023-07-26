package panda.simplespawners.handlers;

import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
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
import java.util.List;
import java.util.UUID;

public class SpawnerHandler implements Listener {

    // Variables
    private final Plugin simpleSpawnersPlugin;
    private final ConfigHandler configHandler;
    private final SpawnerUtils spawnerUtils;
    private final DataSerialization dataSerialization;
    private final MiniMessage miniMsg = MiniMessage.miniMessage();

    private final NamespacedKey spawnerUUIDKey;
    private final NamespacedKey spawnerOwnerKey;
    private final NamespacedKey mobTypeKey;

    private HashMap<UUID, SpawnerData> cachedSpawners = new HashMap<>();

    // Constructor method
    public SpawnerHandler(Plugin loader) {
        simpleSpawnersPlugin = loader;

        // Get the plugin class and set up variables appropriately
        SimpleSpawners simpleSpawnersPluginClass = (SimpleSpawners) Bukkit.getPluginManager().getPlugin("SimpleSpawners");
        assert simpleSpawnersPluginClass != null;
        configHandler = simpleSpawnersPluginClass.getConfigHandler();
        spawnerUtils = simpleSpawnersPluginClass.getSpawnerUtils();
        dataSerialization = simpleSpawnersPluginClass.getDataSerialization();

        // Set up namespace item keys
        spawnerUUIDKey = new NamespacedKey(simpleSpawnersPlugin, "uuid");
        spawnerOwnerKey = new NamespacedKey(simpleSpawnersPlugin, "owner");
        mobTypeKey = new NamespacedKey(simpleSpawnersPlugin, "mobtype");

        // todo: load in all saved spawners to the cachedSpawners hashmap
    }

    // Spawner event method
    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        // If the config demands default spawner behaviour to be toggled off, cancel the spawn event
        if (!configHandler.isSpawnerDefaultBehaviour()) {
            event.setCancelled(true);
        }
    }

    // Spawner place method
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();

        // Check if the placed block was a spawner
        if (!(placedBlock.getType() == Material.SPAWNER)) {
            return;
        }

        ItemStack handItem = event.getItemInHand();

        // Create a new spawner data class and set up the data
        SpawnerData spawnerData = new SpawnerData();
        spawnerData.setX(placedBlock.getX());
        spawnerData.setY(placedBlock.getY());
        spawnerData.setZ(placedBlock.getZ());
        spawnerData.setSpawnerUUID(UUID.randomUUID());
        spawnerData.setWorld(placedBlock.getWorld().getName());

        // Save the data about this newly placed spawner and add it to the list of cached spawners
        dataSerialization.saveSpawnerData(spawnerData);
        cachedSpawners.put(spawnerData.getSpawnerUUID(), spawnerData);

        // Set the data in the block itself
        Player player = event.getPlayer();
        CreatureSpawner spawnerBlock = (CreatureSpawner) placedBlock.getState();
        PersistentDataContainer dataContainer = spawnerBlock.getPersistentDataContainer();

        dataContainer.set(spawnerUUIDKey, PersistentDataType.STRING, spawnerData.getSpawnerUUID().toString());
        dataContainer.set(spawnerOwnerKey, PersistentDataType.STRING, player.getUniqueId().toString());

        // Get the held item's data
        PersistentDataContainer heldItemDataContainer = handItem.getItemMeta().getPersistentDataContainer();

        // Set the block's spawn mob based on the saved mob type key of the held item
        String savedMobType = heldItemDataContainer.get(mobTypeKey, PersistentDataType.STRING);
        if (savedMobType != null) {
            spawnerBlock.setSpawnedType(spawnerUtils.getSpawnerMobTypeFromString(savedMobType));
        } else {
            simpleSpawnersPlugin.getSLF4JLogger().warn("Spawner '%s' was placed but had an invalid mob type, defaulted to pig.".formatted(spawnerData.getSpawnerUUID().toString()));
            spawnerBlock.setSpawnedType(EntityType.PIG);
        }

        // Update the spawner's state
        spawnerBlock.update();

        // Alert the player they have placed a private spawner
        if (!configHandler.getSpawnerPlaceMessage().isEmpty()) {
            player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerPlaceMessage()));
        }
    }

    public void destroyBlockAt(int x, int y, int z, String world) {
        // Get the block at the given coordinates and set it to air
        Block spawnerBlock = new Location(Bukkit.getWorld(world), x, y, z).getBlock();
        spawnerBlock.setType(Material.AIR);
    }

    public void givePlayerSpawnerItem(Player player, Block spawnerBlock) {
        ConfigurationSection spawnerItemSection = configHandler.getSpawnerItemSection();

        // Get item properties from the config
        String displayName = spawnerItemSection.getString("displayName");
        List<String> lore = spawnerItemSection.getStringList("lore");
        String spawnerMobTypeString = spawnerUtils.capitalizeWords(spawnerUtils.getSpawnerMobType(spawnerBlock).name().toLowerCase());

        // Create a new itemstack
        ItemStack itemStack = new ItemStack(spawnerBlock.getType());

        itemStack.editMeta(meta -> {
            assert displayName != null;
            meta.displayName(miniMsg.deserialize(
                    displayName,
                    Placeholder.unparsed("mob", spawnerMobTypeString)
            ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

            meta.lore(
                    lore.stream().map(loreMsg -> miniMsg.deserialize(
                            loreMsg,
                            Placeholder.unparsed("mob", spawnerMobTypeString)
                    ).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)).toList()
            );

            // Set up itemstack data
            PersistentDataContainer heldItemDataContainer = meta.getPersistentDataContainer();

            // Set the block's spawn mob based on the saved mob type key of the held item
            heldItemDataContainer.set(mobTypeKey, PersistentDataType.STRING, spawnerUtils.getSpawnerMobType(spawnerBlock).name());
        });

        player.getInventory().addItem(itemStack);
    }

    public void pickupSpawner(UUID spawnerUUID, Player player, Location blockLocation) {
        SpawnerData spawnerData;

        // Check if the spawner UUID exists (is this an owned spawner? yes)
        if (spawnerUUID != null) {
            // Check if the spawner has been cached
            if (cachedSpawners.containsKey(spawnerUUID)) {
                // Load in the spawner data from the cache
                spawnerData = cachedSpawners.get(spawnerUUID);
            } else {
                // Load in the spawner data from the file
                spawnerData = dataSerialization.loadSpawnerData(spawnerUUID);
            }

            // Get the world from the data to check if the file can be deleted
            String world = spawnerData.getWorld();

            // Check that none of the data is null
            if (world == null || world.isEmpty() || world.isBlank()) {
                simpleSpawnersPlugin.getSLF4JLogger().warn("Saved spawner data could not be found during spawner pickup. This may be because the storage method was changed mid-gameplay. Please ensure that the storage method is set before gameplay and is not changed while the server is online.");
            } else {
                // Delete the spawner file
                dataSerialization.deleteSpawnerDataFile(spawnerUUID);
            }
        }

        // Give the player a spawner
        Block savedSpawner = blockLocation.getBlock();
        givePlayerSpawnerItem(player, savedSpawner);

        destroyBlockAt(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ(), blockLocation.getWorld().getName());
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
        if (!configHandler.getSpawnerAttemptBreakMessage().isEmpty()) {
            player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerAttemptBreakMessage()));
        }
    }

    // Spawner interact method
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();

        // Check if the interaction was a right click and was not from the offhand slot
        if (!event.getAction().name().contains("RIGHT_CLICK") || event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        // Check if the block was not a spawner and if the player is not sneaking according to config requirement
        Player player = event.getPlayer();
        if ((clickedBlock == null || !(clickedBlock.getType() == Material.SPAWNER)) || (player.isSneaking() != configHandler.isSpawnerSneakRequirement())) {
            return;
        }

        // Cancel the event
        event.setCancelled(true);

        // Get the block data information
        CreatureSpawner spawner = (CreatureSpawner) clickedBlock.getState();
        PersistentDataContainer dataContainer = spawner.getPersistentDataContainer();
        String spawnerOwnerUUIDString = dataContainer.get(spawnerOwnerKey, PersistentDataType.STRING);
        String spawnerUUIDString = dataContainer.get(spawnerUUIDKey, PersistentDataType.STRING);

        // Check if the spawner has no owner
        // select the correct menu to open and set up variables
        ConfigurationSection menuToOpen;
        String spawnerMobTypeString = spawnerUtils.capitalizeWords(spawnerUtils.getSpawnerMobType(clickedBlock).name().toLowerCase());
        if (spawnerOwnerUUIDString == null) {
            // Get the appropriate config sections
            menuToOpen = configHandler.getUnownedMenuConfigurationSection();
            ConfigurationSection menuInformationSection = menuToOpen.getConfigurationSection("information");

            // Open the unclaimed spawner menu
            UnownedSpawnerProvider unownedInventory = new UnownedSpawnerProvider();
            assert menuInformationSection != null;
            unownedInventory.setRows(menuInformationSection.getInt("rows"));
            unownedInventory.setMenuTitle(menuInformationSection.getString("title"));

            unownedInventory.setMobType(spawnerMobTypeString);
            unownedInventory.setSpawnerOwner("Unowned");
            unownedInventory.setSpawnerLocation(clickedBlock.getLocation());

            unownedInventory.buildInventory();
            unownedInventory.openBuiltInventory(player);
        } else {
            // Check if the player who interacted with the spawner owns the spawner and send an appropriate message
            if (!spawnerOwnerUUIDString.equals(player.getUniqueId().toString())) {
                if (!configHandler.getSpawnerOpenFailedMessage().isEmpty()) {
                    player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerOpenFailedMessage()));
                }
                return;
            }
            if (!configHandler.getSpawnerOpenMessage().isEmpty()) {
                player.sendMessage(miniMsg.deserialize(configHandler.getSpawnerOpenMessage()));
            }

            // Get the appropriate config sections
            menuToOpen = configHandler.getPrimaryMenuConfigurationSection();
            ConfigurationSection menuInformationSection = menuToOpen.getConfigurationSection("information");

            // Open the spawner menu
            OwnedSpawnerProvider ownedInventory = new OwnedSpawnerProvider();
            assert menuInformationSection != null;
            ownedInventory.setRows(menuInformationSection.getInt("rows"));
            ownedInventory.setMenuTitle(menuInformationSection.getString("title"));
            ownedInventory.setSpawnerLocation(clickedBlock.getLocation());

            ownedInventory.setMobType(spawnerMobTypeString);
            ownedInventory.setSpawnerOwner(spawnerUtils.getPlayerNameFromUUID(UUID.fromString(spawnerOwnerUUIDString)));
            assert spawnerUUIDString != null;
            ownedInventory.setSpawnerUUID(UUID.fromString(spawnerUUIDString));

            ownedInventory.buildInventory();
            ownedInventory.openBuiltInventory(player);
        }
    }
}
