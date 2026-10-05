# AutoBroadcaster

AutoBroadcaster is a lightweight and feature-rich PaperMC plugin that automates server announcements. It allows you to schedule broadcasts at specific intervals or at exact server times, with advanced targeting options.

## Features
- **Interval Messages**: Broadcast messages sequentially or randomly at a set interval (e.g., every 5 minutes).
- **Scheduled Messages**: Broadcast specific messages at exact server times (e.g., daily at 14:00 or 20:30) with a 2-minute window to handle lag gracefully.
- **Per-World Targeting**: Send broadcasts only to players in specific worlds.
- **Per-Permission Targeting**: Restrict broadcasts to players with specific permissions.
- **In-Game Management**: Add, remove, list, and reload messages and configurations directly from the game without needing to edit files manually.
- **Modern Formatting**: Fully supports Paper's [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting out of the box (e.g., `<red>Hello</red>`), while seamlessly falling back to legacy color codes (e.g., `&cHello`) if preferred.
- **Performance Optimized**: Pre-computed messages and zero-allocation filtering ensure minimal CPU/memory usage between broadcasts.
- **Reliable Scheduling**: Scheduled messages use a 2-minute detection window and per-day tracking to prevent duplicates and missed broadcasts.
- **bStats Integration**: Anonymous usage statistics via bStats (service ID: 32227). Server owners can opt-out via `plugins/bStats/config.yml`.

## Commands & Usage
The main command is `/autobroadcaster` (aliases: `/abroadcaster`, `/ab`).
*All commands require the `autobroadcaster.admin` permission.*

| Command | Description | Example |
|---|---|---|
| `/ab reload` | Reloads the configuration from disk and restarts the timers. | `/ab reload` |
| `/ab list` | Displays all currently active interval and scheduled messages along with their IDs/Indices. | `/ab list` |
| `/ab add interval <message>` | Adds a new interval message to the rotation. | `/ab add interval <green>Join our Discord!</green>` |
| `/ab add <HH:mm> <message>` | Adds a new scheduled message at the specified time. | `/ab add 14:30 <gold>Daily event starting!</gold>` |
| `/ab remove interval <index>` | Removes an interval message by its index (use `/ab list` to find the index). | `/ab remove interval 1` |
| `/ab remove time <HH:mm>` | Removes a scheduled message for that specific time. | `/ab remove time 14:30` |

## Permissions
- `autobroadcaster.admin` - Grants full access to all plugin commands. Defaults to server Operators (`op`).

## Configuration (`config.yml`)
You can configure the plugin directly via the `config.yml` file located in `plugins/AutoBroadcaster/config.yml`.

### Basic Configuration

```yaml
# AutoBroadcaster Configuration

# The prefix for all plugin messages
prefix: "<gray>[<gradient:#ff0000:#ff7f00>AutoBroadcaster</gradient>] <reset>"

# Messages that are broadcasted at a specific interval.
# interval_seconds: How often in seconds the messages should be broadcasted (minimum 10).
# random_order: If true, picks a random message. If false, goes in order.
# target_worlds: List of worlds to broadcast to. Leave empty for all worlds.
# required_permission: If set, only players with this permission will see the message. Leave empty for everyone.
interval_messages:
  enabled: true
  interval_seconds: 300
  random_order: false
  target_worlds: []
  required_permission: ""
  messages:
    - "<green>Welcome to our server!</green> Remember to read the rules."
    - "Join our discord at <aqua><click:open_url:'https://discord.gg/example'>discord.gg/example</click></aqua>"

# Messages that are broadcasted at specific server times (HH:MM).
# Make sure the time matches the server's time zone.
# target_worlds and required_permissions are per-time-key maps.
scheduled_messages:
  enabled: true
  messages:
    "12:00": "<gold>It is noon! Don't forget to grab your daily rewards!</gold>"
    "20:00": "<light_purple>Evening event is starting now!</light_purple>"
  target_worlds:
    "12:00": []
    "20:00": []
  required_permissions:
    "12:00": ""
    "20:00": ""
```

### Advanced Targeting Examples

**World-Specific Broadcasts:**
```yaml
interval_messages:
  target_worlds: ["world", "world_nether"]  # Only broadcast to players in these worlds
```

**Permission-Based Broadcasts:**
```yaml
interval_messages:
  required_permission: "server.vip"  # Only players with this permission see the message
```

**Per-Scheduled-Time Targeting:**
```yaml
scheduled_messages:
  messages:
    "12:00": "<gold>Noon event in spawn!</gold>"
    "20:00": "<light_purple>Evening event!</light_purple>"
  target_worlds:
    "12:00": ["world"]  # Noon message only in overworld
    "20:00": []          # Evening message in all worlds
  required_permissions:
    "12:00": ""           # Noon message for everyone
    "20:00": "server.vip" # Evening message for VIPs only
```

## Performance

AutoBroadcaster is designed for minimal resource usage:

- **Zero CPU between broadcasts**: The plugin uses virtually no CPU while waiting for the next broadcast
- **Pre-computed messages**: All message components are built once at config load, not on every broadcast tick
- **Zero-allocation filtering**: World and permission checks use simple loops with no object allocation
- **Cached time parsing**: Scheduled message times are parsed once and cached
- **Minimum interval safeguard**: Intervals below 10 seconds are automatically clamped to prevent performance issues

## Reliability

- **Scheduled message duplicates prevented**: Each scheduled message can only fire once per day, even after reloads
- **Lag-tolerant scheduling**: Uses a 2-minute detection window instead of exact time matching
- **Error handling**: All tasks and broadcasts are wrapped in try-catch blocks to prevent silent failures

## Compilation / Building
This plugin targets PaperMC `26.2.build.22-alpha` and **requires Java 25** to compile and run.
To build it yourself:
1. Clone the repository.
2. Ensure you have a Java 25 JDK installed.
3. Run `./gradlew build`.
4. The `.jar` will be generated in `build/libs/`.