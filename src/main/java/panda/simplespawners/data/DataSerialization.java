package panda.simplespawners.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class DataSerialization {

    // Variables
    private final Plugin simpleSpawnersPlugin;
    private final Gson gson = new GsonBuilder().create();

    // Constructor method
    public DataSerialization(Plugin loader) {
        simpleSpawnersPlugin = loader;
    }

    // Directory creation and checking method
    private void checkDirectoryExists(String filePath) {
        // Create a new file reference and check if it exists
        File file = new File(filePath);
        if (!file.exists()) {
            // If the file does not exist, create the directories it needs
            boolean fileCreationSuccess = file.mkdirs();
            if (fileCreationSuccess) {
                simpleSpawnersPlugin.getSLF4JLogger().info("File path '%s' created successfully.".formatted(filePath));
                return;
            }
            simpleSpawnersPlugin.getSLF4JLogger().info("File path '%s' failed to create.".formatted(filePath));
        }
    }

    // Json file save method
    public void saveSpawnerData(SpawnerData data) {
        String jsonString = gson.toJson(data);
        String filePath = simpleSpawnersPlugin.getDataFolder().getAbsolutePath();

        // Check that the directory exists
        checkDirectoryExists(filePath + "/spawners/");

        filePath += "/spawners/%s.json".formatted(data.getSpawnerUUID());

        // Write the file
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(jsonString);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Load json save as SpawnerData class
    public SpawnerData loadSpawnerData(UUID spawnerUUID) {
        String filePath = simpleSpawnersPlugin.getDataFolder().getAbsolutePath();
        filePath += "/spawners/%s.json".formatted(spawnerUUID.toString());

        // Create a default data class
        SpawnerData data = new SpawnerData();

        // If the file does not exist, then return the default class
        File dataFile = new File(filePath);
        if (!dataFile.exists()) {
            return data;
        }

        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            data = gson.fromJson(json, SpawnerData.class);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return data;
    }

    public void deleteSpawnerDataFile(UUID spawnerUUID) {
        String filePath = simpleSpawnersPlugin.getDataFolder().getAbsolutePath();
        filePath += "/spawners/%s.json".formatted(spawnerUUID.toString());

        File file = new File(filePath);
        if (file.exists()) {
            boolean result = file.delete();
            if (result) {
                simpleSpawnersPlugin.getSLF4JLogger().info("Spawner file '%s' removed due to spawner pickup.".formatted(spawnerUUID.toString()));
            } else {
                simpleSpawnersPlugin.getSLF4JLogger().info("Spawner '%s' was picked up but spawner file could not be deleted.".formatted(spawnerUUID.toString()));
            }
        }
    }
}
