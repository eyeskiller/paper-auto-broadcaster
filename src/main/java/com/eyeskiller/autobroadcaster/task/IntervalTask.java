package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import org.bukkit.scheduler.BukkitRunnable;

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
            int messageCount = manager.getIntervalMessages().size();
            if (messageCount == 0) {
                return;
            }

            int index;
            if (manager.isRandomOrder()) {
                index = random.nextInt(messageCount);
            } else {
                index = currentIndex;
                if (currentIndex >= messageCount) {
                    currentIndex = 0;
                    index = 0;
                }
                currentIndex++;
            }

            manager.broadcastIntervalMessage(index);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error broadcasting interval message", e);
        }
    }
}
