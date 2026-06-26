package com.eyeskiller.autobroadcaster.task;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

    private IntervalTask task;

    @BeforeEach
    void setUp() {
        task = new IntervalTask(plugin, manager);
    }

    @Test
    void run_emptyMessages_doesNotBroadcast() {
        when(manager.getIntervalMessages()).thenReturn(Collections.emptyList());

        task.run();

        verify(manager, never()).broadcastMessage(any());
    }

    @Test
    void run_sequentialOrder_sendsFirstMessage() {
        Component msg1 = Component.text("Message 1");
        Component msg2 = Component.text("Message 2");
        when(manager.getIntervalMessages()).thenReturn(Arrays.asList(msg1, msg2));
        when(manager.isRandomOrder()).thenReturn(false);

        task.run();

        verify(manager).broadcastMessage(msg1);
    }

    @Test
    void run_sequentialOrder_advancesIndex() {
        Component msg1 = Component.text("Message 1");
        Component msg2 = Component.text("Message 2");
        when(manager.getIntervalMessages()).thenReturn(Arrays.asList(msg1, msg2));
        when(manager.isRandomOrder()).thenReturn(false);

        task.run();
        task.run();

        verify(manager).broadcastMessage(msg1);
        verify(manager).broadcastMessage(msg2);
    }

    @Test
    void run_sequentialOrder_wrapsAroundAtEnd() {
        Component msg1 = Component.text("Message 1");
        when(manager.getIntervalMessages()).thenReturn(List.of(msg1));
        when(manager.isRandomOrder()).thenReturn(false);

        task.run();
        task.run();

        verify(manager, times(2)).broadcastMessage(msg1);
    }

    @Test
    void run_randomOrder_broadcastsMessage() {
        Component msg1 = Component.text("Message 1");
        Component msg2 = Component.text("Message 2");
        when(manager.getIntervalMessages()).thenReturn(Arrays.asList(msg1, msg2));
        when(manager.isRandomOrder()).thenReturn(true);

        task.run();

        verify(manager).broadcastMessage(any(Component.class));
    }

    @Test
    void run_prependsPrefix() {
        Component msg = Component.text("Hello");
        when(manager.getIntervalMessages()).thenReturn(List.of(msg));
        when(manager.isRandomOrder()).thenReturn(false);

        task.run();

        verify(manager).broadcastMessage(msg);
    }
}
