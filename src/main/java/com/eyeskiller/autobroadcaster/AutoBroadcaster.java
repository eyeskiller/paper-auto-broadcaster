package com.eyeskiller.autobroadcaster;

import com.eyeskiller.autobroadcaster.command.AutoBroadcasterCommand;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import com.eyeskiller.autobroadcaster.task.IntervalTask;
import com.eyeskiller.autobroadcaster.task.ScheduledTimeTask;
import online.bechatbot.analytics.AnalyticsTracker;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoBroadcaster extends JavaPlugin {

    private AnnouncementManager announcementManager;
    private IntervalTask intervalTask;
    private ScheduledTimeTask scheduledTimeTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.announcementManager = new AnnouncementManager(this);
        this.announcementManager.loadConfig();

        getCommand("autobroadcaster").setExecutor(new AutoBroadcasterCommand(this));
        getCommand("autobroadcaster").setTabCompleter(new AutoBroadcasterCommand(this));

        startTasks();
        
        AnalyticsTracker analytics = new AnalyticsTracker(this, "https://analytics.bechatbot.online/api/track");
        analytics.sendEvent("STARTUP");
        
        getLogger().info("AutoBroadcaster enabled successfully!");
    }

    @Override
    public void onDisable() {
        stopTasks();
        getLogger().info("AutoBroadcaster disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        this.announcementManager.loadConfig();
        restartTasks();
    }

    private void startTasks() {
        if (announcementManager.isIntervalEnabled()) {
            long ticks = announcementManager.getIntervalSeconds() * 20L;
            intervalTask = new IntervalTask(this, announcementManager);
            intervalTask.runTaskTimer(this, ticks, ticks);
        }

        if (announcementManager.isScheduledEnabled()) {
            scheduledTimeTask = new ScheduledTimeTask(this, announcementManager);
            // Run every minute (1200 ticks), wait 20 ticks before starting to offset slightly
            scheduledTimeTask.runTaskTimer(this, 20L, 1200L);
        }
    }

    private void stopTasks() {
        if (intervalTask != null) {
            intervalTask.cancel();
            intervalTask = null;
        }
        if (scheduledTimeTask != null) {
            scheduledTimeTask.cancel();
            scheduledTimeTask = null;
        }
    }

    public void restartTasks() {
        stopTasks();
        startTasks();
    }

    public AnnouncementManager getAnnouncementManager() {
        return announcementManager;
    }
}
