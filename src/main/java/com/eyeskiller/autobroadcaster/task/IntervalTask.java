package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class IntervalTask extends BukkitRunnable {

    private final AutoBroadcaster plugin;
    private final AnnouncementManager manager;
    private final Random random = new Random();
    private int currentIndex = 0;

    public IntervalTask(AutoBroadcaster plugin, AnnouncementManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            List<Component> messages = manager.getIntervalMessages();
            if (messages.isEmpty()) {
                return;
            }

            Component message;
            if (manager.isRandomOrder()) {
                message = messages.get(random.nextInt(messages.size()));
            } else {
                if (currentIndex >= messages.size()) {
                    currentIndex = 0;
                }
                message = messages.get(currentIndex);
                currentIndex++;
            }

            Component fullMessage = manager.getPrefix().append(message);
            Bukkit.getServer().sendMessage(fullMessage);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error broadcasting interval message", e);
        }
    }
}
