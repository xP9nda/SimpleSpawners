package panda.simplespawners;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.minuskube.inv.InventoryManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import panda.simplespawners.data.DataSerialization;
import panda.simplespawners.data.SQLiteManager;
import panda.simplespawners.handlers.ConfigHandler;
import panda.simplespawners.handlers.SpawnerHandler;
import panda.simplespawners.utils.SpawnerUtils;

public final class SimpleSpawners extends JavaPlugin {

    // Variables
    private ConfigHandler configHandler;
    private SpawnerHandler spawnerHandler;
    private SpawnerUtils spawnerUtils;
    private DataSerialization dataSerialization;
    private InventoryManager inventoryManager;
    private Economy economy;
    private SQLiteManager sqliteManager;

    // Primary plugin methods
    private void enableFunctionality() {
        // Config
        saveDefaultConfig();

        // Loop every second until vault is retrieved
        int iterations = 0;
        Plugin vaultInstance;
        while (true) {
            iterations++;

            // Attempt to find Vault
            vaultInstance = getServer().getPluginManager().getPlugin("Vault");
            this.getSLF4JLogger().info("Attempting to retrieve Vault...");
            if (vaultInstance != null) {
                this.getSLF4JLogger().info("Vault was found, enabling plugin.");
                break;
            }

            // Check that it isn't taking too long to find Vault
            if (iterations >= 7) {
                this.getSLF4JLogger().warn("Took too long to find Vault, please ensure you have Vault downloaded, SimpleSpawners will not work without it. Disabling plugin.");
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Register Vault and set up the economy
        setupEconomy();
        this.getServer().getServicesManager().register(Economy.class, economy, vaultInstance, ServicePriority.Normal);

        // Setup
        var pluginManager = this.getServer().getPluginManager();

        // Handlers and events
        configHandler = new ConfigHandler(this);
        pluginManager.registerEvents(configHandler, this);

        attemptSQLiteConnection();

        spawnerUtils = new SpawnerUtils();
        dataSerialization = new DataSerialization(this);

        spawnerHandler = new SpawnerHandler(this);
        pluginManager.registerEvents(spawnerHandler, this);

        inventoryManager = new InventoryManager(this);
        inventoryManager.init();


        // Commands
        try {
            PaperCommandManager<CommandSender> commandManager = PaperCommandManager.createNative(
                    this, CommandExecutionCoordinator.simpleCoordinator()
            );

            AnnotationParser<CommandSender> annotationParser = new AnnotationParser<>(
                    commandManager, CommandSender.class, params -> SimpleCommandMeta.empty()
            );

            annotationParser.parse(configHandler);
            annotationParser.parse(spawnerHandler);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Timed events
        // todo: set up spawner schedule tasks here
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        enableFunctionality();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        // Disconnect from SQLite manager
        if (sqliteManager != null) {
            sqliteManager.disconnect();
        }
    }

    public void attemptSQLiteConnection() {
        // Check for spawner storage method
        if (configHandler.getSpawnerStorageMethod().equalsIgnoreCase("sqlite-flat") && sqliteManager == null) {
            // Setup SQLite connection
            sqliteManager = new SQLiteManager(this, getDataFolder().getAbsolutePath() + "/spawners/");
            sqliteManager.connect();
        }
    }

    // Vault methods
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    // Getter methods
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public SpawnerUtils getSpawnerUtils() {
        return spawnerUtils;
    }

    public DataSerialization getDataSerialization() {
        return dataSerialization;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public SpawnerHandler getSpawnerHandler() {
        return spawnerHandler;
    }

    public Economy getEconomy() {
        return economy;
    }

    public SQLiteManager getSqliteManager() {
        return sqliteManager;
    }
}
