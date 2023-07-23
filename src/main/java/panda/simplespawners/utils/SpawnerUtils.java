package panda.simplespawners.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
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
        if (offlinePlayer != null) {
            return offlinePlayer.getName();
        }
        return null;
    }

    public OfflinePlayer getOfflinePlayerFromUUID(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer != null) {
            return offlinePlayer;
        }
        return null;
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
        ItemStack[] inventoryContents = player.getInventory().getContents();

        for (ItemStack item : inventoryContents) {
            if (item == null || item.getType().isAir()) {
                return true; // Found an open slot
            }
        }
        return false; // No open slots found
    }

    public static String formatNumberWithCommas(int number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number);
    }
}
