package panda.simplespawners.data;

import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteManager {

    private Connection connection;
    private String databaseDirectory;
    private Plugin simpleSpawnersPlugin;

    public SQLiteManager(Plugin loader, String databaseFile) {
        simpleSpawnersPlugin = loader;
        this.databaseDirectory = databaseFile;
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

    public void connect() {
        // Check that the database directory exists
        checkDirectoryExists(databaseDirectory);

        // Check that the file exists
        File databaseFile = new File(databaseDirectory + "spawners.db");
        if (!databaseFile.exists()) {
            try (FileWriter writer = new FileWriter(databaseDirectory + "spawners.db")) {
                writer.write("");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        // Connect to the file
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:" + databaseDirectory + "spawners.db";
            connection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
