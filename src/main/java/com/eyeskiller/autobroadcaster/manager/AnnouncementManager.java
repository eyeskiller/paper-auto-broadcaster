package com.eyeskiller.autobroadcaster.manager;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class AnnouncementManager {

    private final AutoBroadcaster plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    private Component prefix;
    private boolean intervalEnabled;
    private int intervalSeconds;
    private boolean randomOrder;
    private List<Component> intervalMessages;

    private boolean scheduledEnabled;
    private Map<String, Component> scheduledMessages;

    // We store raw messages as well so we can add/remove them from command
    private String rawPrefix;
    private List<String> rawIntervalMessages;

    public AnnouncementManager(AutoBroadcaster plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        rawPrefix = config.getString("prefix", "<gray>[<gradient:#ff0000:#ff7f00>AutoBroadcaster</gradient>] <reset>");
        this.prefix = parseMessage(rawPrefix);

        this.intervalEnabled = config.getBoolean("interval_messages.enabled", true);
        this.intervalSeconds = config.getInt("interval_messages.interval_seconds", 300);
        this.randomOrder = config.getBoolean("interval_messages.random_order", false);
        this.rawIntervalMessages = config.getStringList("interval_messages.messages");

        this.intervalMessages = new ArrayList<>();
        for (String msg : this.rawIntervalMessages) {
            try {
                this.intervalMessages.add(parseMessage(msg));
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse interval message: " + msg, e);
                this.intervalMessages.add(Component.text(msg));
            }
        }

        this.scheduledEnabled = config.getBoolean("scheduled_messages.enabled", true);
        this.scheduledMessages = new HashMap<>();
        if (config.contains("scheduled_messages.messages")) {
            ConfigurationSection section = config.getConfigurationSection("scheduled_messages.messages");
            if (section != null) {
                for (String timeKey : section.getKeys(false)) {
                    String msg = config.getString("scheduled_messages.messages." + timeKey);
                    if (msg != null) {
                        try {
                            this.scheduledMessages.put(timeKey, parseMessage(msg));
                        } catch (Exception e) {
                            plugin.getLogger().log(Level.WARNING, "Failed to parse scheduled message for " + timeKey + ": " + msg, e);
                            this.scheduledMessages.put(timeKey, Component.text(msg));
                        }
                    }
                }
            }
        }
    }

    public Component parseMessage(String text) {
        // Simple heuristic to support both legacy and minimessage
        if (text.contains("&") && !text.contains("<")) {
            return legacySerializer.deserialize(text);
        }
        // MiniMessage is default
        return miniMessage.deserialize(text);
    }

    public void addIntervalMessage(String rawMessage) {
        this.rawIntervalMessages.add(rawMessage);
        this.intervalMessages.add(parseMessage(rawMessage));
        plugin.getConfig().set("interval_messages.messages", this.rawIntervalMessages);
        try {
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config after adding interval message", e);
        }
    }

    public boolean removeIntervalMessage(int index) {
        if (index >= 0 && index < rawIntervalMessages.size()) {
            this.rawIntervalMessages.remove(index);
            this.intervalMessages.remove(index);
            plugin.getConfig().set("interval_messages.messages", this.rawIntervalMessages);
            try {
                plugin.saveConfig();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save config after removing interval message", e);
            }
            return true;
        }
        return false;
    }

    public void addScheduledMessage(String time, String rawMessage) {
        this.scheduledMessages.put(time, parseMessage(rawMessage));
        plugin.getConfig().set("scheduled_messages.messages." + time, rawMessage);
        try {
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save config after adding scheduled message", e);
        }
    }

    public boolean removeScheduledMessage(String time) {
        if (this.scheduledMessages.containsKey(time)) {
            this.scheduledMessages.remove(time);
            plugin.getConfig().set("scheduled_messages.messages." + time, null);
            try {
                plugin.saveConfig();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save config after removing scheduled message", e);
            }
            return true;
        }
        return false;
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
        return intervalMessages;
    }

    public List<String> getRawIntervalMessages() {
        return rawIntervalMessages;
    }

    public boolean isScheduledEnabled() {
        return scheduledEnabled;
    }

    public Map<String, Component> getScheduledMessages() {
        return scheduledMessages;
    }
}
