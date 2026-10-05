package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import net.kyori.adventure.text.Component;
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

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ScheduledTimeTaskTest {

    @Mock
    private AutoBroadcaster plugin;

    @Mock
    private AnnouncementManager manager;

    private ScheduledTimeTask task;

    @BeforeEach
    void setUp() {
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
        Map<String, Component> messages = new HashMap<>();
        messages.put(currentTime, msg);

        when(manager.getScheduledMessages()).thenReturn(messages);
        when(manager.getPrefix()).thenReturn(Component.text("[Prefix] "));
        when(manager.getScheduledWorlds()).thenReturn(Collections.emptyMap());
        when(manager.getScheduledPermissions()).thenReturn(Collections.emptyMap());

        task.run();

        verify(manager).broadcast(any(Component.class), eq(List.of()), eq(""));
    }

    @Test
    void run_noMatchingTime_doesNotBroadcast() {
        Map<String, Component> messages = new HashMap<>();
        messages.put("99:99", Component.text("Never matches"));

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
        Component noMatchMsg = Component.text("No match");
        Map<String, Component> messages = new HashMap<>();
        messages.put(currentTime, matchMsg);
        messages.put("99:99", noMatchMsg);

        when(manager.getScheduledMessages()).thenReturn(messages);
        when(manager.getPrefix()).thenReturn(Component.text("[Prefix] "));
        when(manager.getScheduledWorlds()).thenReturn(Collections.emptyMap());
        when(manager.getScheduledPermissions()).thenReturn(Collections.emptyMap());

        task.run();

        verify(manager, times(1)).broadcast(any(Component.class), eq(List.of()), eq(""));
    }
}
