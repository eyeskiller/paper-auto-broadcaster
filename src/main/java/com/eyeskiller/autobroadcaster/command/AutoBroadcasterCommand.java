package com.eyeskiller.autobroadcaster.command;

import com.eyeskiller.autobroadcaster.AutoBroadcaster;
import com.eyeskiller.autobroadcaster.manager.AnnouncementManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AutoBroadcasterCommand implements CommandExecutor, TabCompleter {

    private final AutoBroadcaster plugin;

    public AutoBroadcasterCommand(AutoBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("autobroadcaster.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        AnnouncementManager manager = plugin.getAnnouncementManager();

        switch (subCommand) {
            case "reload":
                plugin.reloadPlugin();
                sendSuccess(sender, "AutoBroadcaster configuration reloaded!");
                break;
            case "list":
                sender.sendMessage(Component.text("--- Interval Messages ---", NamedTextColor.GOLD));
                List<String> intervals = manager.getRawIntervalMessages();
                for (int i = 0; i < intervals.size(); i++) {
                    sender.sendMessage(Component.text(i + ": ").color(NamedTextColor.YELLOW)
                            .append(manager.parseMessage(intervals.get(i))));
                }
                
                sender.sendMessage(Component.text("--- Scheduled Messages ---", NamedTextColor.GOLD));
                for (Map.Entry<String, Component> entry : manager.getScheduledMessages().entrySet()) {
                    sender.sendMessage(Component.text(entry.getKey() + ": ").color(NamedTextColor.YELLOW)
                            .append(entry.getValue()));
                }
                break;
            case "add":
                if (args.length < 3) {
                    sendError(sender, "Usage: /ab add <interval|time(HH:mm)> <message...>");
                    return true;
                }
                String type = args[1];
                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                
                if (type.equalsIgnoreCase("interval")) {
                    manager.addIntervalMessage(message);
                    restartAndConfirm(sender, "Added interval message.");
                } else if (type.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    manager.addScheduledMessage(type, message);
                    restartAndConfirm(sender, "Added scheduled message for " + type + ".");
                } else {
                    sendError(sender, "Invalid type! Use 'interval' or a valid time like '14:30'.");
                }
                break;
            case "remove":
                if (args.length < 3) {
                    sendError(sender, "Usage: /ab remove <interval|time> <index|HH:mm>");
                    return true;
                }
                String removeType = args[1];
                String id = args[2];
                
                if (removeType.equalsIgnoreCase("interval")) {
                    try {
                        int index = Integer.parseInt(id);
                        if (manager.removeIntervalMessage(index)) {
                            restartAndConfirm(sender, "Removed interval message at index " + index + ".");
                        } else {
                            sendError(sender, "Invalid index.");
                        }
                    } catch (NumberFormatException e) {
                        sendError(sender, "Index must be a number.");
                    }
                } else if (removeType.equalsIgnoreCase("time")) {
                    if (!id.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                        sendError(sender, "Invalid time format! Use HH:mm (e.g. 14:30).");
                    } else if (manager.removeScheduledMessage(id)) {
                        restartAndConfirm(sender, "Removed scheduled message for " + id + ".");
                    } else {
                        sendError(sender, "No scheduled message found for " + id + ".");
                    }
                } else {
                    sendError(sender, "Invalid type! Use 'interval' or 'time'.");
                }
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message, NamedTextColor.GREEN));
    }

    private void sendError(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message, NamedTextColor.RED));
    }

    private void restartAndConfirm(CommandSender sender, String message) {
        plugin.restartTasks();
        sendSuccess(sender, message);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("AutoBroadcaster Commands:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/ab reload - Reload the config", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/ab list - List all messages", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/ab add interval <message> - Add an interval message", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/ab add <HH:mm> <message> - Add a scheduled message", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/ab remove interval <index> - Remove an interval message", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/ab remove time <HH:mm> - Remove a scheduled message", NamedTextColor.YELLOW));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("autobroadcaster.admin")) return new ArrayList<>();
        
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "list", "add", "remove"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                completions.addAll(Arrays.asList("interval", "time"));
            }
        }
        
        // Filter by what they typed
        String current = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(current));
        
        return completions;
    }
}
