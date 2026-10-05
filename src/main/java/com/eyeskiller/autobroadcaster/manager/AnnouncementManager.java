package com.eyeskiller.autobroadcaster.manager;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.util.ValidationUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class AnnouncementManager {

    private static final int MIN_INTERVAL_SECONDS = 10;

    private final AutoBroadcaster plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    private Component prefix;
    private boolean intervalEnabled;
    private int intervalSeconds;
    private boolean randomOrder;
    private List<Component> intervalMessages;
    private List<Component> fullIntervalMessages;
    private List<String> rawIntervalMessages;
    private List<String> intervalWorlds;
    private String intervalPermission;

    private boolean scheduledEnabled;
    private Map<String, Component> scheduledMessages;
    private Map<String, Component> fullScheduledMessages;
    private Map<String, List<String>> scheduledWorlds;
    private Map<String, String> scheduledPermissions;

    private String rawPrefix;

    public AnnouncementManager(AutoBroadcaster plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        rawPrefix = config.getString("prefix", "<gray>[<gradient:#ff0000:#ff7f00>AutoBroadcaster</gradient>] <reset>");
        this.prefix = parseMessage(rawPrefix);

        this.intervalEnabled = config.getBoolean("interval_messages.enabled", true);
        this.intervalSeconds = Math.max(config.getInt("interval_messages.interval_seconds", 300), MIN_INTERVAL_SECONDS);
        this.randomOrder = config.getBoolean("interval_messages.random_order", false);
        this.rawIntervalMessages = new ArrayList<>(config.getStringList("interval_messages.messages"));
        this.intervalWorlds = new ArrayList<>(config.getStringList("interval_messages.target_worlds"));
        this.intervalPermission = config.getString("interval_messages.required_permission", "");

        this.intervalMessages = new ArrayList<>();
        this.fullIntervalMessages = new ArrayList<>();
        for (String msg : this.rawIntervalMessages) {
            try {
                Component parsed = parseMessage(msg);
                this.intervalMessages.add(parsed);
                this.fullIntervalMessages.add(this.prefix.append(parsed));
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse interval message: " + msg, e);
                Component fallback = Component.text(msg);
                this.intervalMessages.add(fallback);
                this.fullIntervalMessages.add(this.prefix.append(fallback));
            }
        }

        this.scheduledEnabled = config.getBoolean("scheduled_messages.enabled", true);
        this.scheduledMessages = new HashMap<>();
        this.fullScheduledMessages = new HashMap<>();
        this.scheduledWorlds = new HashMap<>();
        this.scheduledPermissions = new HashMap<>();

        if (config.isConfigurationSection("scheduled_messages.messages")) {
            ConfigurationSection section = config.getConfigurationSection("scheduled_messages.messages");
            if (section != null) {
                for (String timeKey : section.getKeys(false)) {
                    String msg = config.getString("scheduled_messages.messages." + timeKey);
                    if (msg != null) {
                        try {
                            Component parsed = parseMessage(msg);
                            this.scheduledMessages.put(timeKey, parsed);
                            this.fullScheduledMessages.put(timeKey, this.prefix.append(parsed));
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "Failed to parse scheduled message for " + timeKey + ": " + msg, e);
                            Component fallback = Component.text(msg);
                            this.scheduledMessages.put(timeKey, fallback);
                            this.fullScheduledMessages.put(timeKey, this.prefix.append(fallback));
                        }
                    }
                }
            }
        }

        ConfigurationSection worldsSection = config.getConfigurationSection("scheduled_messages.target_worlds");
        if (worldsSection != null) {
            for (String timeKey : worldsSection.getKeys(false)) {
                this.scheduledWorlds.put(timeKey, new ArrayList<>(worldsSection.getStringList(timeKey)));
            }
        }

        ConfigurationSection permsSection = config.getConfigurationSection("scheduled_messages.required_permissions");
        if (permsSection != null) {
            for (String timeKey : permsSection.getKeys(false)) {
                this.scheduledPermissions.put(timeKey, permsSection.getString(timeKey, ""));
            }
        }
    }

    public Component parseMessage(String text) {
        if (ValidationUtil.containsMiniMessageTags(text)) {
            return miniMessage.deserialize(text);
        }
        if (ValidationUtil.containsLegacyColorCodes(text)) {
            return legacySerializer.deserialize(text);
        }
        return miniMessage.deserialize(text);
    }

    public void broadcast(Component message, List<String> targetWorlds, String requiredPermission) {
        boolean hasWorldFilter = !targetWorlds.isEmpty();
        boolean hasPermFilter = requiredPermission != null && !requiredPermission.isEmpty();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hasWorldFilter) {
                String worldName = player.getWorld().getName();
                boolean inTargetWorld = false;
                for (String w : targetWorlds) {
                    if (w.equalsIgnoreCase(worldName)) {
                        inTargetWorld = true;
                        break;
                    }
                }
                if (!inTargetWorld) continue;
            }
            if (hasPermFilter && !player.hasPermission(requiredPermission)) continue;
            player.sendMessage(message);
        }
    }

    public void broadcastIntervalMessage(int index) {
        if (index >= 0 && index < fullIntervalMessages.size()) {
            broadcast(fullIntervalMessages.get(index), intervalWorlds, intervalPermission);
        }
    }

    public Component getFullScheduledMessage(String timeKey) {
        return fullScheduledMessages.get(timeKey);
    }

    public void broadcastMessage(Component message) {
        broadcast(message, intervalWorlds, intervalPermission);
    }

    public void addIntervalMessage(String rawMessage) {
        this.rawIntervalMessages.add(rawMessage);
        Component parsed = parseMessage(rawMessage);
        this.intervalMessages.add(parsed);
        this.fullIntervalMessages.add(this.prefix.append(parsed));
        saveConfigValue("interval_messages.messages", this.rawIntervalMessages);
    }

    public boolean removeIntervalMessage(int index) {
        if (index >= 0 && index < rawIntervalMessages.size()) {
            this.rawIntervalMessages.remove(index);
            this.intervalMessages.remove(index);
            this.fullIntervalMessages.remove(index);
            saveConfigValue("interval_messages.messages", this.rawIntervalMessages);
            return true;
        }
        return false;
    }

    public void addScheduledMessage(String time, String rawMessage) {
        Component parsed = parseMessage(rawMessage);
        this.scheduledMessages.put(time, parsed);
        this.fullScheduledMessages.put(time, this.prefix.append(parsed));
        saveConfigValue("scheduled_messages.messages." + time, rawMessage);
    }

    public boolean removeScheduledMessage(String time) {
        if (this.scheduledMessages.containsKey(time)) {
            this.scheduledMessages.remove(time);
            this.fullScheduledMessages.remove(time);
            this.scheduledWorlds.remove(time);
            this.scheduledPermissions.remove(time);
            saveConfigValue("scheduled_messages.messages." + time, null);
            saveConfigValue("scheduled_messages.target_worlds." + time, null);
            saveConfigValue("scheduled_messages.required_permissions." + time, null);
            return true;
        }
        return false;
    }

    private void saveConfigValue(String path, Object value) {
        plugin.getConfig().set(path, value);
        try {
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config after updating: " + path, e);
        }
    }

    public Component getPrefix() {
        return prefix;
    }

    public boolean isIntervalEnabled() {
        return intervalEnabled;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public boolean isRandomOrder() {
        return randomOrder;
    }

    public List<Component> getIntervalMessages() {
        return Collections.unmodifiableList(intervalMessages);
    }

    public List<String> getRawIntervalMessages() {
        return Collections.unmodifiableList(rawIntervalMessages);
    }

    public List<String> getIntervalWorlds() {
        return Collections.unmodifiableList(intervalWorlds);
    }

    public String getIntervalPermission() {
        return intervalPermission;
    }

    public boolean isScheduledEnabled() {
        return scheduledEnabled;
    }

    public Map<String, Component> getScheduledMessages() {
        return Collections.unmodifiableMap(scheduledMessages);
    }

    public Map<String, List<String>> getScheduledWorlds() {
        return Collections.unmodifiableMap(scheduledWorlds);
    }

    public Map<String, String> getScheduledPermissions() {
        return Collections.unmodifiableMap(scheduledPermissions);
    }
}
