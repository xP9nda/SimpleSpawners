package panda.simplespawners.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import panda.simplespawners.SimpleSpawners;
import panda.simplespawners.handlers.ConfigHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DataSerialization {

    // Variables
    private final Plugin simpleSpawnersPlugin;
    private final Gson gson = new GsonBuilder().create();

    private SimpleSpawners simpleSpawnersPluginClass;
    private final ConfigHandler configHandler;

    // Constructor method
    public DataSerialization(Plugin loader) {
        simpleSpawnersPlugin = loader;
        simpleSpawnersPluginClass = (SimpleSpawners) Bukkit.getPluginManager().getPlugin("SimpleSpawners");
        assert simpleSpawnersPluginClass != null;
        configHandler = simpleSpawnersPluginClass.getConfigHandler();
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

    // File save method
    public void saveSpawnerData(SpawnerData data) {
        String saveMethod = configHandler.getSpawnerStorageMethod();

        String filePath = simpleSpawnersPlugin.getDataFolder().getAbsolutePath();

        // Check that the directory exists
        checkDirectoryExists(filePath + "/spawners/");

        // Check for json flat file storage method
        if (saveMethod.equalsIgnoreCase("json-flat")) {
            String jsonString = gson.toJson(data);
            filePath += "/spawners/%s.json".formatted(data.getSpawnerUUID());
    
            // Write the file
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(jsonString);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        // Check for SQLite flat file storage method
        } else if (saveMethod.equalsIgnoreCase("sqlite-flat")) {
            try {
                // Create a new statement with the connection
                if (simpleSpawnersPluginClass.getSqliteManager() == null) {
                    simpleSpawnersPluginClass.attemptSQLiteConnection();
                }

                Connection sqliteManagerConnection = simpleSpawnersPluginClass.getSqliteManager().getConnection();
                if (sqliteManagerConnection == null) {
                    simpleSpawnersPlugin.getSLF4JLogger().warn("Attempted to use SQLite storage method, but connection was not established. Please set spawnerStorageMethod option BEFORE starting the server.");
                    return;
                }
                Statement statement = sqliteManagerConnection.createStatement();

                // Attempt to create the table if it does not exist
                statement.execute("CREATE TABLE IF NOT EXISTS spawners (uuid TEXT PRIMARY KEY, x INT, y INT, z INT, world TEXT)");

                // Add an entry to the table
                statement.execute("INSERT INTO spawners (uuid, x, y, z, world) VALUES ('%s', %s, %s, %s, '%s')".formatted(
                        data.getSpawnerUUID(),
                        data.getX(),
                        data.getY(),
                        data.getZ(),
                        data.getWorld()
                ));

            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        String saveMethod = configHandler.getSpawnerStorageMethod();

        // Check for json flat file storage method
        if (saveMethod.equalsIgnoreCase("json-flat")) {
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
        } else if (saveMethod.equalsIgnoreCase("sqlite-flat")) {
            try {
                // Create a new statement with the connection
                if (simpleSpawnersPluginClass.getSqliteManager() == null) {
                    simpleSpawnersPluginClass.attemptSQLiteConnection();
                }

                Connection sqliteManagerConnection = simpleSpawnersPluginClass.getSqliteManager().getConnection();
                if (sqliteManagerConnection == null) {
                    simpleSpawnersPlugin.getSLF4JLogger().warn("Attempted to use SQLite storage method, but connection was not established. Please set spawnerStorageMethod option BEFORE starting the server.");
                    return;
                }
                Statement statement = sqliteManagerConnection.createStatement();

                // Delete the entry with the appropriate primary key in the database
                statement.execute("DELETE FROM spawners WHERE uuid = '%s'".formatted(spawnerUUID.toString()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
