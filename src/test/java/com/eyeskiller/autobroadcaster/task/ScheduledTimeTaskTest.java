package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.PluginLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTimeTaskTest {

    @Mock
    private AutoBroadcaster plugin;

    @Mock
    private AnnouncementManager manager;

    @Mock
    private Logger logger;

    private ScheduledTimeTask task;

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getLogger()).thenReturn(logger);
        task = new ScheduledTimeTask(plugin, manager);
    }

    @Test
    void run_emptyMessages_doesNotBroadcast() {
        when(manager.getScheduledMessages()).thenReturn(Collections.emptyMap());

        task.run();

        verify(manager, never()).broadcast(any(), any(), any());
    }

    @Test
    void run_matchingTime_broadcastsMessage() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
        Component msg = Component.text("Scheduled message");
        Component fullMsg = Component.text("[Prefix] Scheduled message");
        Map<String, Component> messages = new HashMap<>();
        messages.put(currentTime, msg);

        when(manager.getScheduledMessages()).thenReturn(messages);
        when(manager.getFullScheduledMessage(currentTime)).thenReturn(fullMsg);
        when(manager.getScheduledWorlds()).thenReturn(Collections.emptyMap());
        when(manager.getScheduledPermissions()).thenReturn(Collections.emptyMap());

        task.run();

        verify(manager).broadcast(eq(fullMsg), eq(List.of()), eq(""));
    }

    @Test
    void run_noMatchingTime_doesNotBroadcast() {
        Map<String, Component> messages = new HashMap<>();
        messages.put("3:00", Component.text("Never matches"));

        when(manager.getScheduledMessages()).thenReturn(messages);
        lenient().when(manager.getScheduledWorlds()).thenReturn(Collections.emptyMap());
        lenient().when(manager.getScheduledPermissions()).thenReturn(Collections.emptyMap());

        task.run();

        verify(manager, never()).broadcast(any(), any(), any());
    }

    @Test
    void run_multipleSchedules_onlyMatchingTimeBroadcasts() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm"));
        Component matchMsg = Component.text("Match");
        Component fullMatchMsg = Component.text("[Prefix] Match");
        Component noMatchMsg = Component.text("No match");
        Map<String, Component> messages = new HashMap<>();
        messages.put(currentTime, matchMsg);
        messages.put("3:00", noMatchMsg);

        when(manager.getScheduledMessages()).thenReturn(messages);
        when(manager.getFullScheduledMessage(currentTime)).thenReturn(fullMatchMsg);
        when(manager.getScheduledWorlds()).thenReturn(Collections.emptyMap());
        when(manager.getScheduledPermissions()).thenReturn(Collections.emptyMap());

        task.run();

        verify(manager, times(1)).broadcast(eq(fullMatchMsg), eq(List.of()), eq(""));
    }
}
