package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import com.eyeskiller.autobroadcaster.util.ValidationUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ScheduledTimeTask extends BukkitRunnable {

    private static final int CHECK_INTERVAL_MINUTES = 2;

    private final AutoBroadcaster plugin;
    private final AnnouncementManager manager;
    private final Map<String, LocalDate> lastFiredDates = new HashMap<>();

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

            LocalTime now = LocalTime.now();
            LocalDate today = LocalDate.now();

            for (Map.Entry<String, Component> entry : messages.entrySet()) {
                String timeKey = entry.getKey();
                Component message = entry.getValue();

                if (lastFiredDates.containsKey(timeKey) && lastFiredDates.get(timeKey).equals(today)) {
                    continue;
                }

                try {
                    if (ValidationUtil.isTimeInRange(timeKey, now, CHECK_INTERVAL_MINUTES)) {
                        Component fullMessage = manager.getPrefix().append(message);
                        List<String> worlds = manager.getScheduledWorlds().getOrDefault(timeKey, List.of());
                        String permission = manager.getScheduledPermissions().getOrDefault(timeKey, "");
                        manager.broadcast(fullMessage, worlds, permission);
                        lastFiredDates.put(timeKey, today);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error checking scheduled time: " + timeKey, e);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in scheduled time task", e);
        }
    }
}
