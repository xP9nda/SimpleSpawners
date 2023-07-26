package panda.simplespawners.handlers;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import panda.simplespawners.SimpleSpawners;

public class ConfigHandler implements Listener {

    // Variables
    private final Plugin simpleSpawnersPlugin;
    private final MiniMessage miniMsg = MiniMessage.miniMessage();
    private SimpleSpawners simpleSpawnersPluginClass;

    // Config values
    private String spawnerStorageMethod;
    private String reloadMessage;
    private String spawnerPlaceMessage;
    private String spawnerAttemptBreakMessage;
    private String spawnerPickupMessage;
    private String spawnerPickupFullInventoryMessage;
    private String spawnerOpenMessage;
    private String spawnerOpenFailedMessage;
    private boolean spawnerDefaultBehaviour;
    private boolean spawnerSneakRequirement;
    private ConfigurationSection primaryMenuConfigurationSection;
    private ConfigurationSection unownedMenuConfigurationSection;
    private ConfigurationSection spawnerItemSection;
    private int unownedMoneyPickupCost;
    private int ownedMoneyPickupCost;
    private String spawnerPickupNotEnoughMoney;

    // Constructor method
    public ConfigHandler(Plugin loader) {
        // Set up plugin reference and reload the config
        simpleSpawnersPlugin = loader;
        reloadConfiguration();
    }

    // Reload config method
    private void reloadConfiguration() {
        // Reload and get the config
        simpleSpawnersPlugin.reloadConfig();
        FileConfiguration pluginConfig = simpleSpawnersPlugin.getConfig();
        simpleSpawnersPluginClass = (SimpleSpawners) Bukkit.getPluginManager().getPlugin("SimpleSpawners");

        // todo: add default values to the pluginConfig.getString() methods
        setReloadMessage(pluginConfig.getString("messages.reload"));
        setSpawnerPlaceMessage(pluginConfig.getString("messages.spawnerPlace"));
        setSpawnerAttemptBreakMessage(pluginConfig.getString("messages.spawnerAttemptBreak"));
        setSpawnerOpenFailedMessage(pluginConfig.getString("messages.spawnerOpenFailed"));
        setSpawnerOpenMessage(pluginConfig.getString("messages.spawnerOpen"));
        setSpawnerPickupMessage(pluginConfig.getString("messages.spawnerPickup"));
        setSpawnerPickupFullInventoryMessage(pluginConfig.getString("messages.spawnerPickupFullInventory"));
        setSpawnerPickupNotEnoughMoney(pluginConfig.getString("messages.spawnerPickupNotEnoughMoney"));
        setSpawnerStorageMethod(pluginConfig.getString("settings.spawnerStorageMethod", "json-flat"));
        setSpawnerDefaultBehaviour(pluginConfig.getBoolean("settings.spawnerDefaultBehaviour"));
        setSpawnerSneakRequirement(pluginConfig.getBoolean("settings.spawnerSneakRequirement"));
        setOwnedMoneyPickupCost(pluginConfig.getInt("settings.ownedMoneyPickupCost"));
        setUnownedMoneyPickupCost(pluginConfig.getInt("settings.unownedMoneyPickupCost"));
        setPrimaryMenuConfigurationSection(pluginConfig.getConfigurationSection("primaryMenu"));
        setUnownedMenuConfigurationSection(pluginConfig.getConfigurationSection("unownedMenu"));
        setSpawnerItemSection(pluginConfig.getConfigurationSection("spawnerItem"));
    }

    // Reload command method
    @CommandMethod("simplespawners|spawners reload")
    @CommandPermission("simplespawners.reload")
    public void onReloadConfigurationCommand(Player commandSender) {
        // Reload the configuration
        reloadConfiguration();

        // Send a message to the command sender alerting them of the reload
        if (!getReloadMessage().isEmpty()) {
            commandSender.sendMessage(miniMsg.deserialize(getReloadMessage()));
        }
    }

    // Getter and setter methods
    public boolean isSpawnerDefaultBehaviour() {
        return spawnerDefaultBehaviour;
    }

    public void setSpawnerDefaultBehaviour(boolean spawnerDefaultBehaviour) {
        this.spawnerDefaultBehaviour = spawnerDefaultBehaviour;
    }

    public String getReloadMessage() {
        return reloadMessage;
    }

    public void setReloadMessage(String reloadMessage) {
        this.reloadMessage = reloadMessage;
    }

    public String getSpawnerPlaceMessage() {
        return spawnerPlaceMessage;
    }

    public void setSpawnerPlaceMessage(String spawnerPlaceMessage) {
        this.spawnerPlaceMessage = spawnerPlaceMessage;
    }

    public String getSpawnerAttemptBreakMessage() {
        return spawnerAttemptBreakMessage;
    }

    public void setSpawnerAttemptBreakMessage(String spawnerAttemptBreakMessage) {
        this.spawnerAttemptBreakMessage = spawnerAttemptBreakMessage;
    }

    public String getSpawnerPickupMessage() {
        return spawnerPickupMessage;
    }

    public void setSpawnerPickupMessage(String spawnerPickupMessage) {
        this.spawnerPickupMessage = spawnerPickupMessage;
    }

    public ConfigurationSection getPrimaryMenuConfigurationSection() {
        return primaryMenuConfigurationSection;
    }

    public void setPrimaryMenuConfigurationSection(ConfigurationSection primaryMenuConfigurationSection) {
        this.primaryMenuConfigurationSection = primaryMenuConfigurationSection;
    }

    public String getSpawnerOpenMessage() {
        return spawnerOpenMessage;
    }

    public void setSpawnerOpenMessage(String spawnerOpenMessage) {
        this.spawnerOpenMessage = spawnerOpenMessage;
    }

    public String getSpawnerOpenFailedMessage() {
        return spawnerOpenFailedMessage;
    }

    public void setSpawnerOpenFailedMessage(String spawnerOpenFailedMessage) {
        this.spawnerOpenFailedMessage = spawnerOpenFailedMessage;
    }

    public ConfigurationSection getUnownedMenuConfigurationSection() {
        return unownedMenuConfigurationSection;
    }

    public void setUnownedMenuConfigurationSection(ConfigurationSection unownedMenuConfigurationSection) {
        this.unownedMenuConfigurationSection = unownedMenuConfigurationSection;
    }

    public boolean isSpawnerSneakRequirement() {
        return spawnerSneakRequirement;
    }

    public void setSpawnerSneakRequirement(boolean spawnerSneakRequirement) {
        this.spawnerSneakRequirement = spawnerSneakRequirement;
    }

    public ConfigurationSection getSpawnerItemSection() {
        return spawnerItemSection;
    }

    public void setSpawnerItemSection(ConfigurationSection spawnerItemSection) {
        this.spawnerItemSection = spawnerItemSection;
    }

    public String getSpawnerPickupFullInventoryMessage() {
        return spawnerPickupFullInventoryMessage;
    }

    public void setSpawnerPickupFullInventoryMessage(String spawnerPickupFullInventoryMessage) {
        this.spawnerPickupFullInventoryMessage = spawnerPickupFullInventoryMessage;
    }

    public int getUnownedMoneyPickupCost() {
        return unownedMoneyPickupCost;
    }

    public void setUnownedMoneyPickupCost(int unownedMoneyPickupCost) {
        this.unownedMoneyPickupCost = unownedMoneyPickupCost;
    }

    public int getOwnedMoneyPickupCost() {
        return ownedMoneyPickupCost;
    }

    public void setOwnedMoneyPickupCost(int ownedMoneyPickupCost) {
        this.ownedMoneyPickupCost = ownedMoneyPickupCost;
    }

    public String getSpawnerPickupNotEnoughMoney() {
        return spawnerPickupNotEnoughMoney;
    }

    public void setSpawnerPickupNotEnoughMoney(String spawnerPickupNotEnoughMoney) {
        this.spawnerPickupNotEnoughMoney = spawnerPickupNotEnoughMoney;
    }

    public String getSpawnerStorageMethod() {
        return spawnerStorageMethod;
    }

    public void setSpawnerStorageMethod(String spawnerStorageMethod) {
        if (spawnerStorageMethod.equalsIgnoreCase("sqlite-flat")) {
            simpleSpawnersPluginClass.attemptSQLiteConnection();
        }
        this.spawnerStorageMethod = spawnerStorageMethod;
    }
}
