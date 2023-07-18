package panda.simplespawners.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class SpawnerUtils {

    public EntityType getSpawnerMobType(Block spawnerBlock) {
        if (spawnerBlock.getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner) spawnerBlock.getState();
            return spawner.getSpawnedType();
        }
        return null;
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
}
