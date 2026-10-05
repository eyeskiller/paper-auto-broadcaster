package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import com.eyeskiller.autobroadcaster.util.ValidationUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ScheduledTimeTask extends BukkitRunnable {

    private static final int CHECK_INTERVAL_MINUTES = 2;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final AutoBroadcaster plugin;
    private final AnnouncementManager manager;
    private final Map<String, LocalDate> lastFiredDates = new HashMap<>();
    private Map<String, LocalTime> cachedTimes;

    public ScheduledTimeTask(AutoBroadcaster plugin, AnnouncementManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private void cacheTimes() {
        cachedTimes = new HashMap<>();
        for (String timeKey : manager.getScheduledMessages().keySet()) {
            try {
                cachedTimes.put(timeKey, LocalTime.parse(timeKey, TIME_FORMATTER));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid time format in scheduled message: " + timeKey);
            }
        }
    }

    @Override
    public void run() {
        try {
            Map<String, Component> messages = manager.getScheduledMessages();
            if (messages.isEmpty()) {
                return;
            }

            if (cachedTimes == null || cachedTimes.size() != messages.size()) {
                cacheTimes();
            }

            LocalTime now = LocalTime.now();
            LocalDate today = LocalDate.now();

            for (String timeKey : messages.keySet()) {
                if (lastFiredDates.containsKey(timeKey) && lastFiredDates.get(timeKey).equals(today)) {
                    continue;
                }

                LocalTime scheduledTime = cachedTimes.get(timeKey);
                if (scheduledTime == null) continue;

                long minutesDiff = java.time.Duration.between(scheduledTime, now).toMinutes();
                if (minutesDiff >= 0 && minutesDiff < CHECK_INTERVAL_MINUTES) {
                    Component fullMessage = manager.getFullScheduledMessage(timeKey);
                    List<String> worlds = manager.getScheduledWorlds().getOrDefault(timeKey, List.of());
                    String permission = manager.getScheduledPermissions().getOrDefault(timeKey, "");
                    manager.broadcast(fullMessage, worlds, permission);
                    lastFiredDates.put(timeKey, today);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error in scheduled time task", e);
        }
    }
}
