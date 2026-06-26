package com.eyeskiller.autobroadcaster;

import com.eyeskiller.autobroadcaster.command.AutoBroadcasterCommand;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import com.eyeskiller.autobroadcaster.task.IntervalTask;
import com.eyeskiller.autobroadcaster.task.ScheduledTimeTask;
import online.bechatbot.analytics.AnalyticsTracker;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class AutoBroadcaster extends JavaPlugin {

    private AnnouncementManager announcementManager;
    private IntervalTask intervalTask;
    private ScheduledTimeTask scheduledTimeTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.announcementManager = new AnnouncementManager(this);
        this.announcementManager.loadConfig();

        PluginCommand cmd = getCommand("autobroadcaster");
        if (cmd != null) {
            AutoBroadcasterCommand commandHandler = new AutoBroadcasterCommand(this);
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
        } else {
            getLogger().severe("Command 'autobroadcaster' not found in plugin.yml — commands will not work");
        }

        startTasks();

        try {
            AnalyticsTracker analytics = new AnalyticsTracker(this, "https://analytics.bechatbot.online/api/track");
            analytics.sendEvent("STARTUP");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Failed to send analytics event", e);
        }
        
        getLogger().info("AutoBroadcaster enabled successfully!");
    }

    @Override
    public void onDisable() {
        stopTasks();
        getLogger().info("AutoBroadcaster disabled.");
    }

    public void reloadPlugin() {
        reloadConfig();
        try {
            this.announcementManager.loadConfig();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload configuration", e);
        }
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
