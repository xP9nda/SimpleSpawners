package panda.simplespawners;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import fr.minuskube.inv.InventoryManager;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import panda.simplespawners.data.DataSerialization;
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

    // Primary plugin methods
    @Override
    public void onEnable() {
        // Plugin startup logic

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
}
