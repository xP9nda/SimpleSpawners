package panda.simplespawners.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SpawnerUtils {

    public EntityType getSpawnerMobType(Block spawnerBlock) {
        if (spawnerBlock.getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner) spawnerBlock.getState();
            return spawner.getSpawnedType();
        }
        return null;
    }

    public EntityType getSpawnerMobTypeFromString(String entityTypeName) {
        try {
            return EntityType.valueOf(entityTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Invalid EntityType name provided
            return null;
        }
    }

    public String getPlayerNameFromUUID(UUID uuid) {
        if (uuid == null) {
            return "";
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer.getName();
    }

    public OfflinePlayer getOfflinePlayerFromUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer;
    }

    public List<String> getStringListFromBlock(Block block, NamespacedKey key) {
        if (block == null || key == null) {
            return null;
        }

        Bukkit.getLogger().info("get string listfrom block");
        Bukkit.getLogger().info(String.valueOf(block));
        Bukkit.getLogger().info(String.valueOf(key));

        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();
        PersistentDataContainer dataContainer = spawnerBlock.getPersistentDataContainer();
        String savedList = dataContainer.get(key, PersistentDataType.STRING);
        assert savedList != null;

        Bukkit.getLogger().info(savedList);
        if (savedList.isEmpty()) {
            return new ArrayList<>();
        }

        Bukkit.getLogger().info("pass empty check");

        // Split the string into a list
        return Arrays.asList(savedList.split(";"));
    }

    public void storeStringListInBlock(Block block, List<String> stringList, NamespacedKey key) {
        if (block == null || stringList == null || key == null) {
            return;
        }

        Bukkit.getLogger().info("  store string:");
        Bukkit.getLogger().info(stringList.toString());

        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();
        PersistentDataContainer dataContainer = spawnerBlock.getPersistentDataContainer();

        // Convert the list to a string
        String savedString = String.join(";", stringList);
        Bukkit.getLogger().info(savedString);

        dataContainer.set(key, PersistentDataType.STRING, savedString);
        spawnerBlock.update();
    }

    public String capitalizeWords(String input) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            } else if (c == '_') {
                c = ' ';
                capitalizeNext = true;
            } else if (capitalizeNext) {
                c = Character.toTitleCase(c);
                capitalizeNext = false;
            }

            result.append(c);
        }

        return result.toString();
    }

    public boolean hasOpenSlot(Player player) {
        return player.getInventory().firstEmpty() != -1;
    }

    public static String formatNumberWithCommas(int number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number);
    }
}
