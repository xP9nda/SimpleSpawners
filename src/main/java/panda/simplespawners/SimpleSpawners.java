package panda.simplespawners;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.minuskube.inv.InventoryManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import panda.simplespawners.data.DataSerialization;
import panda.simplespawners.handlers.ConfigHandler;
import panda.simplespawners.handlers.SpawnerHandler;
import panda.simplespawners.utils.SpawnerUtils;

public final class SimpleSpawners extends JavaPlugin implements Listener {

    // Variables
    private ConfigHandler configHandler;
    private SpawnerHandler spawnerHandler;
    private SpawnerUtils spawnerUtils;
    private DataSerialization dataSerialization;
    private InventoryManager inventoryManager;
    private Economy economy;
    private boolean vaultLoaded = false;

    // Primary plugin methods
    @Override
    public void onEnable() {
        // Plugin startup logic

        // todo: implement sql database option over separate json file format

        // Config
        saveDefaultConfig();

        // Setup
        var pluginManager = this.getServer().getPluginManager();

        // Handlers and events
        configHandler = new ConfigHandler(this);
        pluginManager.registerEvents(configHandler, this);

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
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("Vault")) {
            if (setupEconomy()) {
                this.getSLF4JLogger().info("Vault and required plugins loaded, enabling SimpleSpawners...");
                vaultLoaded = true;
                // Continue enabling your plugin
                getServer().getPluginManager().registerEvents(this, this);
            } else {
                this.getSLF4JLogger().error("Vault is loaded, but required plugins are not available.");
            }
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
}
