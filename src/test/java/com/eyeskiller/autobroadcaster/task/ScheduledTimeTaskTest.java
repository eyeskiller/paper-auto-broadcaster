package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTimeTaskTest {

    @Mock
    private AutoBroadcaster plugin;

    @Mock
    private AnnouncementManager manager;

    @Mock
    private Server server;

    private ScheduledTimeTask task;

    @BeforeEach
    void setUp() {
        task = new ScheduledTimeTask(plugin, manager);
    }

    @Test
    void run_emptyMessages_doesNotBroadcast() {
        when(manager.getScheduledMessages()).thenReturn(Collections.emptyMap());

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            task.run();
            bukkit.verify(() -> org.bukkit.Bukkit.getServer(), never());
        }
    }

    @Test
    void run_matchingTime_broadcastsMessage() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Component msg = Component.text("Scheduled message");
        Component prefix = Component.text("PREFIX: ");
        Map<String, Component> messages = new HashMap<>();
        messages.put(currentTime, msg);

        when(manager.getScheduledMessages()).thenReturn(messages);
        when(manager.getPrefix()).thenReturn(prefix);

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            bukkit.when(() -> org.bukkit.Bukkit.getServer()).thenReturn(server);

            task.run();

            verify(server).sendMessage(prefix.append(msg));
        }
    }

    @Test
    void run_noMatchingTime_doesNotBroadcast() {
        Map<String, Component> messages = new HashMap<>();
        messages.put("99:99", Component.text("Never matches"));

        when(manager.getScheduledMessages()).thenReturn(messages);

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            task.run();
            bukkit.verify(() -> org.bukkit.Bukkit.getServer(), never());
        }
    }

    @Test
    void run_multipleSchedules_onlyMatchingTimeBroadcasts() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Component matchMsg = Component.text("Match");
        Component noMatchMsg = Component.text("No match");
        Component prefix = Component.text("");
        Map<String, Component> messages = new HashMap<>();
        messages.put(currentTime, matchMsg);
        messages.put("99:99", noMatchMsg);

        when(manager.getScheduledMessages()).thenReturn(messages);
        when(manager.getPrefix()).thenReturn(prefix);

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            bukkit.when(() -> org.bukkit.Bukkit.getServer()).thenReturn(server);

            task.run();

            verify(server, times(1)).sendMessage(any(Component.class));
        }
    }
}
