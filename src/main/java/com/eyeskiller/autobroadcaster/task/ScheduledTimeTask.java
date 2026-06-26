package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;

public class ScheduledTimeTask extends BukkitRunnable {

    private final AutoBroadcaster plugin;
    private final AnnouncementManager manager;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public ScheduledTimeTask(AutoBroadcaster plugin, AnnouncementManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            Map<String, Component> messages = manager.getScheduledMessages();
            if (messages.isEmpty()) {
                return;
            }

            String currentTime = LocalTime.now().format(timeFormatter);

            if (messages.containsKey(currentTime)) {
                Component message = messages.get(currentTime);
                Component fullMessage = manager.getPrefix().append(message);
                Bukkit.getServer().sendMessage(fullMessage);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error broadcasting scheduled message", e);
        }
    }
}
