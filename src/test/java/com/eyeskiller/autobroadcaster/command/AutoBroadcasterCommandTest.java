package com.eyeskiller.autobroadcaster.command;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoBroadcasterCommandTest {

    @Mock
    private AutoBroadcaster plugin;

    @Mock
    private AnnouncementManager manager;

    @Mock
    private CommandSender sender;

    @Mock
    private Command command;

    private AutoBroadcasterCommand cmd;

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getAnnouncementManager()).thenReturn(manager);
        cmd = new AutoBroadcasterCommand(plugin);
    }

    // --- onCommand tests ---

    @Test
    void onCommand_noPermission_sendsError() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(false);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{});

        assertTrue(result);
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void onCommand_noArgs_sendsHelp() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{});

        assertTrue(result);
        verify(sender, atLeast(1)).sendMessage(any(Component.class));
    }

    @Test
    void onCommand_reload_reloadsPlugin() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"reload"});

        assertTrue(result);
        verify(plugin).reloadPlugin();
        verify(sender).sendMessage(any(Component.class));
    }

    @Test
    void onCommand_list_displaysMessages() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);
        when(manager.getRawIntervalMessages()).thenReturn(Arrays.asList("<green>Msg1</green>", "<red>Msg2</red>"));
        when(manager.parseMessage(anyString())).thenReturn(Component.text("parsed"));
        when(manager.getScheduledMessages()).thenReturn(Map.of("12:00", Component.text("Noon")));

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"list"});

        assertTrue(result);
        // header + 2 interval + header + 1 scheduled = 5 messages
        verify(sender, atLeast(4)).sendMessage(any(Component.class));
    }

    @Test
    void onCommand_addInterval_addsMessage() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"add", "interval", "Hello", "World"});

        assertTrue(result);
        verify(manager).addIntervalMessage("Hello World");
        verify(plugin).restartTasks();
    }

    @Test
    void onCommand_addScheduled_validTime_addsMessage() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"add", "14:30", "Event", "starting!"});

        assertTrue(result);
        verify(manager).addScheduledMessage("14:30", "Event starting!");
        verify(plugin).restartTasks();
    }

    @Test
    void onCommand_addScheduled_invalidTime_sendsError() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"add", "25:00", "Bad"});

        assertTrue(result);
        verify(manager, never()).addScheduledMessage(anyString(), anyString());
    }

    @Test
    void onCommand_add_tooFewArgs_sendsUsage() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"add"});

        assertTrue(result);
        verify(manager, never()).addIntervalMessage(anyString());
        verify(manager, never()).addScheduledMessage(anyString(), anyString());
    }

    @Test
    void onCommand_removeInterval_validIndex_removesMessage() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);
        when(manager.removeIntervalMessage(1)).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"remove", "interval", "1"});

        assertTrue(result);
        verify(manager).removeIntervalMessage(1);
        verify(plugin).restartTasks();
    }

    @Test
    void onCommand_removeInterval_invalidIndex_sendsError() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);
        when(manager.removeIntervalMessage(99)).thenReturn(false);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"remove", "interval", "99"});

        assertTrue(result);
        verify(plugin, never()).restartTasks();
    }

    @Test
    void onCommand_removeInterval_notANumber_sendsError() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"remove", "interval", "abc"});

        assertTrue(result);
        verify(manager, never()).removeIntervalMessage(anyInt());
    }

    @Test
    void onCommand_removeTime_existing_removesMessage() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);
        when(manager.removeScheduledMessage("12:00")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"remove", "time", "12:00"});

        assertTrue(result);
        verify(manager).removeScheduledMessage("12:00");
        verify(plugin).restartTasks();
    }

    @Test
    void onCommand_removeTime_nonExisting_sendsError() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);
        when(manager.removeScheduledMessage("99:99")).thenReturn(false);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"remove", "time", "99:99"});

        assertTrue(result);
        verify(plugin, never()).restartTasks();
    }

    @Test
    void onCommand_remove_invalidType_sendsError() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"remove", "badtype", "0"});

        assertTrue(result);
        verify(manager, never()).removeIntervalMessage(anyInt());
        verify(manager, never()).removeScheduledMessage(anyString());
    }

    @Test
    void onCommand_remove_tooFewArgs_sendsUsage() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"remove"});

        assertTrue(result);
        verify(manager, never()).removeIntervalMessage(anyInt());
    }

    @Test
    void onCommand_unknownSubcommand_sendsHelp() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        boolean result = cmd.onCommand(sender, command, "ab", new String[]{"unknown"});

        assertTrue(result);
        verify(sender, atLeast(1)).sendMessage(any(Component.class));
    }

    @Test
    void onCommand_reload_caseInsensitive() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        cmd.onCommand(sender, command, "ab", new String[]{"RELOAD"});

        verify(plugin).reloadPlugin();
    }

    // --- onTabComplete tests ---

    @Test
    void onTabComplete_noPermission_returnsEmpty() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(false);

        List<String> result = cmd.onTabComplete(sender, command, "ab", new String[]{""});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void onTabComplete_firstArg_returnsAllSubcommands() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        List<String> result = cmd.onTabComplete(sender, command, "ab", new String[]{""});

        assertNotNull(result);
        assertTrue(result.contains("reload"));
        assertTrue(result.contains("list"));
        assertTrue(result.contains("add"));
        assertTrue(result.contains("remove"));
    }

    @Test
    void onTabComplete_firstArg_filtersPartialMatch() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        List<String> result = cmd.onTabComplete(sender, command, "ab", new String[]{"re"});

        assertNotNull(result);
        assertTrue(result.contains("reload"));
        assertTrue(result.contains("remove"));
        assertFalse(result.contains("list"));
        assertFalse(result.contains("add"));
    }

    @Test
    void onTabComplete_secondArg_add_returnsTypes() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        List<String> result = cmd.onTabComplete(sender, command, "ab", new String[]{"add", ""});

        assertNotNull(result);
        assertTrue(result.contains("interval"));
        assertTrue(result.contains("time"));
    }

    @Test
    void onTabComplete_secondArg_remove_returnsTypes() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        List<String> result = cmd.onTabComplete(sender, command, "ab", new String[]{"remove", ""});

        assertNotNull(result);
        assertTrue(result.contains("interval"));
        assertTrue(result.contains("time"));
    }

    @Test
    void onTabComplete_secondArg_list_returnsEmpty() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        List<String> result = cmd.onTabComplete(sender, command, "ab", new String[]{"list", ""});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void onTabComplete_thirdArg_returnsEmpty() {
        when(sender.hasPermission("autobroadcaster.admin")).thenReturn(true);

        List<String> result = cmd.onTabComplete(sender, command, "ab", new String[]{"add", "interval", ""});

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
