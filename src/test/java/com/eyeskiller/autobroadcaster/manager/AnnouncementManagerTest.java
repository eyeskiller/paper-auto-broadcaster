package com.eyeskiller.autobroadcaster.manager;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementManagerTest {

    @Mock
    private AutoBroadcaster plugin;

    @Mock
    private FileConfiguration config;

    private AnnouncementManager manager;

    @BeforeEach
    void setUp() {
        lenient().when(plugin.getConfig()).thenReturn(config);
        manager = new AnnouncementManager(plugin);
    }

    private void setupDefaultConfig() {
        when(config.getString("prefix", "<gray>[<gradient:#ff0000:#ff7f00>AutoBroadcaster</gradient>] <reset>"))
                .thenReturn("<gold>Prefix: </gold>");
        when(config.getBoolean("interval_messages.enabled", true)).thenReturn(true);
        when(config.getInt("interval_messages.interval_seconds", 300)).thenReturn(300);
        when(config.getBoolean("interval_messages.random_order", false)).thenReturn(false);
        when(config.getStringList("interval_messages.messages"))
                .thenReturn(new ArrayList<>(Arrays.asList("<green>Hello</green>", "<red>World</red>")));
        when(config.getBoolean("scheduled_messages.enabled", true)).thenReturn(true);
        when(config.contains("scheduled_messages.messages")).thenReturn(false);
    }

    @Test
    void loadConfig_setsIntervalEnabled() {
        setupDefaultConfig();
        manager.loadConfig();
        assertTrue(manager.isIntervalEnabled());
    }

    @Test
    void loadConfig_setsIntervalSeconds() {
        setupDefaultConfig();
        manager.loadConfig();
        assertEquals(300, manager.getIntervalSeconds());
    }

    @Test
    void loadConfig_setsRandomOrderFalse() {
        setupDefaultConfig();
        manager.loadConfig();
        assertFalse(manager.isRandomOrder());
    }

    @Test
    void loadConfig_parsesIntervalMessages() {
        setupDefaultConfig();
        manager.loadConfig();
        List<Component> messages = manager.getIntervalMessages();
        assertEquals(2, messages.size());
    }

    @Test
    void loadConfig_storesRawIntervalMessages() {
        setupDefaultConfig();
        manager.loadConfig();
        List<String> raw = manager.getRawIntervalMessages();
        assertEquals(2, raw.size());
        assertEquals("<green>Hello</green>", raw.get(0));
        assertEquals("<red>World</red>", raw.get(1));
    }

    @Test
    void loadConfig_setsScheduledEnabled() {
        setupDefaultConfig();
        manager.loadConfig();
        assertTrue(manager.isScheduledEnabled());
    }

    @Test
    void loadConfig_loadsScheduledMessages() {
        setupDefaultConfig();
        when(config.contains("scheduled_messages.messages")).thenReturn(true);
        ConfigurationSection section = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("scheduled_messages.messages")).thenReturn(section);
        when(section.getKeys(false)).thenReturn(new HashSet<>(Arrays.asList("12:00", "20:00")));
        when(config.getString("scheduled_messages.messages.12:00")).thenReturn("<gold>Noon!</gold>");
        when(config.getString("scheduled_messages.messages.20:00")).thenReturn("<red>Evening!</red>");

        manager.loadConfig();

        assertEquals(2, manager.getScheduledMessages().size());
        assertTrue(manager.getScheduledMessages().containsKey("12:00"));
        assertTrue(manager.getScheduledMessages().containsKey("20:00"));
    }

    @Test
    void loadConfig_noScheduledSection_emptyMap() {
        setupDefaultConfig();
        manager.loadConfig();
        assertTrue(manager.getScheduledMessages().isEmpty());
    }

    @Test
    void loadConfig_setsPrefix() {
        setupDefaultConfig();
        manager.loadConfig();
        assertNotNull(manager.getPrefix());
    }

    @Test
    void parseMessage_miniMessageFormat() {
        Component result = manager.parseMessage("<green>Hello</green>");
        assertNotNull(result);
    }

    @Test
    void parseMessage_legacyAmpersandFormat() {
        Component result = manager.parseMessage("&cHello &bWorld");
        assertNotNull(result);
    }

    @Test
    void parseMessage_mixedFormat_usesMiniMessage() {
        Component result = manager.parseMessage("&c<green>Hello</green>");
        assertNotNull(result);
    }

    @Test
    void parseMessage_plainText_usesMiniMessage() {
        Component result = manager.parseMessage("Hello World");
        assertNotNull(result);
    }

    @Test
    void addIntervalMessage_addsToLists() {
        setupDefaultConfig();
        manager.loadConfig();

        int sizeBefore = manager.getIntervalMessages().size();
        manager.addIntervalMessage("<blue>New message</blue>");

        assertEquals(sizeBefore + 1, manager.getIntervalMessages().size());
        assertEquals(sizeBefore + 1, manager.getRawIntervalMessages().size());
        assertEquals("<blue>New message</blue>", manager.getRawIntervalMessages().get(sizeBefore));
    }

    @Test
    void addIntervalMessage_savesToConfig() {
        setupDefaultConfig();
        manager.loadConfig();

        manager.addIntervalMessage("<blue>New</blue>");

        verify(config).set(eq("interval_messages.messages"), any(List.class));
        verify(plugin).saveConfig();
    }

    @Test
    void removeIntervalMessage_validIndex_removesAndReturnsTrue() {
        setupDefaultConfig();
        manager.loadConfig();

        assertTrue(manager.removeIntervalMessage(0));
        assertEquals(1, manager.getIntervalMessages().size());
        assertEquals(1, manager.getRawIntervalMessages().size());
    }

    @Test
    void removeIntervalMessage_validIndex_savesToConfig() {
        setupDefaultConfig();
        manager.loadConfig();

        manager.removeIntervalMessage(0);

        verify(config).set(eq("interval_messages.messages"), any(List.class));
        verify(plugin).saveConfig();
    }

    @Test
    void removeIntervalMessage_negativeIndex_returnsFalse() {
        setupDefaultConfig();
        manager.loadConfig();

        assertFalse(manager.removeIntervalMessage(-1));
    }

    @Test
    void removeIntervalMessage_outOfBoundsIndex_returnsFalse() {
        setupDefaultConfig();
        manager.loadConfig();

        assertFalse(manager.removeIntervalMessage(100));
    }

    @Test
    void addScheduledMessage_addsToMap() {
        setupDefaultConfig();
        manager.loadConfig();

        manager.addScheduledMessage("15:30", "<gold>Afternoon!</gold>");

        assertTrue(manager.getScheduledMessages().containsKey("15:30"));
    }

    @Test
    void addScheduledMessage_savesToConfig() {
        setupDefaultConfig();
        manager.loadConfig();

        manager.addScheduledMessage("15:30", "<gold>Afternoon!</gold>");

        verify(config).set("scheduled_messages.messages.15:30", "<gold>Afternoon!</gold>");
        verify(plugin).saveConfig();
    }

    @Test
    void removeScheduledMessage_existingKey_removesAndReturnsTrue() {
        setupDefaultConfig();
        when(config.contains("scheduled_messages.messages")).thenReturn(true);
        ConfigurationSection section = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("scheduled_messages.messages")).thenReturn(section);
        when(section.getKeys(false)).thenReturn(new HashSet<>(List.of("12:00")));
        when(config.getString("scheduled_messages.messages.12:00")).thenReturn("<gold>Noon!</gold>");

        manager.loadConfig();

        assertTrue(manager.removeScheduledMessage("12:00"));
        assertFalse(manager.getScheduledMessages().containsKey("12:00"));
    }

    @Test
    void removeScheduledMessage_existingKey_savesToConfig() {
        setupDefaultConfig();
        when(config.contains("scheduled_messages.messages")).thenReturn(true);
        ConfigurationSection section = mock(ConfigurationSection.class);
        when(config.getConfigurationSection("scheduled_messages.messages")).thenReturn(section);
        when(section.getKeys(false)).thenReturn(new HashSet<>(List.of("12:00")));
        when(config.getString("scheduled_messages.messages.12:00")).thenReturn("<gold>Noon!</gold>");

        manager.loadConfig();
        manager.removeScheduledMessage("12:00");

        verify(config).set("scheduled_messages.messages.12:00", null);
        verify(plugin).saveConfig();
    }

    @Test
    void removeScheduledMessage_nonExistingKey_returnsFalse() {
        setupDefaultConfig();
        manager.loadConfig();

        assertFalse(manager.removeScheduledMessage("99:99"));
    }

    @Test
    void loadConfig_intervalDisabled() {
        when(config.getString("prefix", "<gray>[<gradient:#ff0000:#ff7f00>AutoBroadcaster</gradient>] <reset>"))
                .thenReturn("<gold>Prefix</gold>");
        when(config.getBoolean("interval_messages.enabled", true)).thenReturn(false);
        when(config.getInt("interval_messages.interval_seconds", 300)).thenReturn(60);
        when(config.getBoolean("interval_messages.random_order", false)).thenReturn(true);
        when(config.getStringList("interval_messages.messages")).thenReturn(new ArrayList<>());
        when(config.getBoolean("scheduled_messages.enabled", true)).thenReturn(false);
        when(config.contains("scheduled_messages.messages")).thenReturn(false);

        manager.loadConfig();

        assertFalse(manager.isIntervalEnabled());
        assertEquals(60, manager.getIntervalSeconds());
        assertTrue(manager.isRandomOrder());
        assertFalse(manager.isScheduledEnabled());
    }

    @Test
    void loadConfig_emptyIntervalMessages() {
        setupDefaultConfig();
        when(config.getStringList("interval_messages.messages")).thenReturn(new ArrayList<>());

        manager.loadConfig();

        assertTrue(manager.getIntervalMessages().isEmpty());
        assertTrue(manager.getRawIntervalMessages().isEmpty());
    }
}
