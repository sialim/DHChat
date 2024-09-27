package me.sialim.dhchat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DHChat extends JavaPlugin implements Listener, CommandExecutor {

    @Override
    public void onEnable() {
        getCommand("dhchatreload").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("dhchat.reload")) {
            reloadConfig();
            sender.sendMessage("DHChat config reloaded!");
            return true;
        } else {
            sender.sendMessage("Missing permission 'dhchat.reload'");
        }
        return false;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        ConfigurationSection config = getConfig();

        TextComponent finalMessage = new TextComponent();
        boolean formatted = false;

        Set<String> keys = config.getKeys(false);
        for (String key : keys) {
            if (p.hasPermission(key)) {
                List<Map<?, ?>> parts = config.getMapList(key + ".parts");
                for (Map<?, ?> part : parts) {
                    String lineFormat = (String) part.get("line");
                    String hoverCommand = (String) part.get("command");
                    List<String> hoverLines = (List<String>) part.get("hover");

                    String formattedLine = PlaceholderAPI.setPlaceholders(p, lineFormat);
                    TextComponent messagePart = new TextComponent(ChatColor.translateAlternateColorCodes('&', formattedLine));

                    if (hoverLines != null && !hoverLines.isEmpty()) {
                        String hoverText = ChatColor.translateAlternateColorCodes('&', String.join("\n", hoverLines));
                        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(hoverText)});
                        messagePart.setHoverEvent(hoverEvent);
                    }

                    if (hoverCommand != null) {
                        String parsedCommand = PlaceholderAPI.setPlaceholders(p, hoverCommand);
                        messagePart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, parsedCommand));
                    }

                    finalMessage.addExtra(messagePart);
                }
                formatted = true;
                break;
            }
        }

        if (!formatted) {
            finalMessage = new TextComponent(p.getName() + ": " + e.getMessage());
        }

        e.setCancelled(true);
        for (Player recipient : e.getRecipients()) {
            recipient.spigot().sendMessage(finalMessage);
        }
    }
}
