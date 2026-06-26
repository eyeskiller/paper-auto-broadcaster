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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntervalTaskTest {

    @Mock
    private AutoBroadcaster plugin;

    @Mock
    private AnnouncementManager manager;

    @Mock
    private Server server;

    private IntervalTask task;

    @BeforeEach
    void setUp() {
        task = new IntervalTask(plugin, manager);
    }

    @Test
    void run_emptyMessages_doesNotBroadcast() {
        when(manager.getIntervalMessages()).thenReturn(Collections.emptyList());

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            task.run();
            bukkit.verify(() -> org.bukkit.Bukkit.getServer(), never());
        }
    }

    @Test
    void run_sequentialOrder_sendsFirstMessage() {
        Component msg1 = Component.text("Message 1");
        Component msg2 = Component.text("Message 2");
        Component prefix = Component.text("Prefix: ");
        when(manager.getIntervalMessages()).thenReturn(Arrays.asList(msg1, msg2));
        when(manager.isRandomOrder()).thenReturn(false);
        when(manager.getPrefix()).thenReturn(prefix);

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            bukkit.when(() -> org.bukkit.Bukkit.getServer()).thenReturn(server);

            task.run();

            verify(server).sendMessage(any(Component.class));
        }
    }

    @Test
    void run_sequentialOrder_advancesIndex() {
        Component msg1 = Component.text("Message 1");
        Component msg2 = Component.text("Message 2");
        Component prefix = Component.text("");
        when(manager.getIntervalMessages()).thenReturn(Arrays.asList(msg1, msg2));
        when(manager.isRandomOrder()).thenReturn(false);
        when(manager.getPrefix()).thenReturn(prefix);

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            bukkit.when(() -> org.bukkit.Bukkit.getServer()).thenReturn(server);

            task.run();
            task.run();

            verify(server, times(2)).sendMessage(any(Component.class));
        }
    }

    @Test
    void run_sequentialOrder_wrapsAroundAtEnd() {
        Component msg1 = Component.text("Message 1");
        Component prefix = Component.text("");
        when(manager.getIntervalMessages()).thenReturn(List.of(msg1));
        when(manager.isRandomOrder()).thenReturn(false);
        when(manager.getPrefix()).thenReturn(prefix);

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            bukkit.when(() -> org.bukkit.Bukkit.getServer()).thenReturn(server);

            task.run();
            task.run();

            verify(server, times(2)).sendMessage(any(Component.class));
        }
    }

    @Test
    void run_randomOrder_broadcastsMessage() {
        Component msg1 = Component.text("Message 1");
        Component msg2 = Component.text("Message 2");
        Component prefix = Component.text("");
        when(manager.getIntervalMessages()).thenReturn(Arrays.asList(msg1, msg2));
        when(manager.isRandomOrder()).thenReturn(true);
        when(manager.getPrefix()).thenReturn(prefix);

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            bukkit.when(() -> org.bukkit.Bukkit.getServer()).thenReturn(server);

            task.run();

            verify(server).sendMessage(any(Component.class));
        }
    }

    @Test
    void run_prependsPrefix() {
        Component msg = Component.text("Hello");
        Component prefix = Component.text("PREFIX: ");
        when(manager.getIntervalMessages()).thenReturn(List.of(msg));
        when(manager.isRandomOrder()).thenReturn(false);
        when(manager.getPrefix()).thenReturn(prefix);

        try (MockedStatic<org.bukkit.Bukkit> bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            bukkit.when(() -> org.bukkit.Bukkit.getServer()).thenReturn(server);

            task.run();

            verify(server).sendMessage(prefix.append(msg));
        }
    }
}
