package org.xFeneq.ftpa.commands;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

import java.util.*;

public class TpaManager {

    private final FancyTPA plugin;
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();
    private final Map<UUID, UUID> tpaHereRequests = new HashMap<>();
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> locationTimestamp = new HashMap<>();
    private final Map<UUID, Set<UUID>> ignoreLists = new HashMap<>();
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public TpaManager(FancyTPA plugin) {
        this.plugin = plugin;
    }

    public void sendTpaRequest(Player sender, Player target) {
        UUID sId = sender.getUniqueId();
        UUID tId = target.getUniqueId();

        tpaRequests.put(sId, tId);
        sendInteractiveMessage(target, sender, "wants to teleport to you!");

        int expireTime = plugin.getConfig().getInt("settings.request-expire", 30);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (tpaRequests.containsKey(sId) && tpaRequests.get(sId).equals(tId)) {
                tpaRequests.remove(sId);
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-expired-sender").replace("{player}", target.getName())));
                target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-expired-target").replace("{player}", sender.getName())));
            }
        }, expireTime * 20L);
    }

    public void sendTpaHereRequest(Player sender, Player target) {
        UUID sId = sender.getUniqueId();
        UUID tId = target.getUniqueId();

        tpaHereRequests.put(sId, tId);
        sendInteractiveMessage(target, sender, "wants you to teleport to them!");

        int expireTime = plugin.getConfig().getInt("settings.request-expire", 30);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (tpaHereRequests.containsKey(sId) && tpaHereRequests.get(sId).equals(tId)) {
                tpaHereRequests.remove(sId);
                sender.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-expired-sender").replace("{player}", target.getName())));
                target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-expired-target").replace("{player}", sender.getName())));
            }
        }, expireTime * 20L);
    }

    private void sendInteractiveMessage(Player target, Player sender, String text) {
        String header = ColorUtil.color(plugin.getConfig().getString("messages.request-header"));
        String footer = ColorUtil.color(plugin.getConfig().getString("messages.request-footer"));

        target.sendMessage(header);
        target.sendMessage(ColorUtil.color(" &6» &e" + sender.getName() + " &7" + text));

        TextComponent accept = new TextComponent(ColorUtil.color(plugin.getConfig().getString("messages.accept-button")));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorUtil.color(plugin.getConfig().getString("messages.accept-hover")))));

        TextComponent deny = new TextComponent(ColorUtil.color(plugin.getConfig().getString("messages.deny-button")));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + sender.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorUtil.color(plugin.getConfig().getString("messages.deny-hover")))));

        TextComponent line = new TextComponent("  ");
        line.addExtra(accept);
        line.addExtra(new TextComponent("    "));
        line.addExtra(deny);

        target.spigot().sendMessage(line);
        target.sendMessage(footer);
    }

    public boolean toggleIgnore(Player owner, Player target) {
        ignoreLists.putIfAbsent(owner.getUniqueId(), new HashSet<>());
        Set<UUID> ignored = ignoreLists.get(owner.getUniqueId());
        if (ignored.contains(target.getUniqueId())) {
            ignored.remove(target.getUniqueId());
            return false;
        } else {
            ignored.add(target.getUniqueId());
            return true;
        }
    }

    public boolean isIgnoring(Player target, Player sender) {
        return ignoreLists.containsKey(target.getUniqueId()) && ignoreLists.get(target.getUniqueId()).contains(sender.getUniqueId());
    }

    public void addActiveTask(Player player, BukkitTask task) {
        activeTasks.put(player.getUniqueId(), task);
    }

    public void cancelTask(Player player) {
        if (activeTasks.containsKey(player.getUniqueId())) {
            activeTasks.get(player.getUniqueId()).cancel();
            activeTasks.remove(player.getUniqueId());
        }
    }

    public void removeActiveTask(Player player) {
        activeTasks.remove(player.getUniqueId());
    }

    public boolean hasActiveTask(Player player) {
        return activeTasks.containsKey(player.getUniqueId());
    }

    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.color(message)));
    }

    public UUID getRequestSender(Player target) {
        for (Map.Entry<UUID, UUID> entry : tpaRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) return entry.getKey();
        }
        for (Map.Entry<UUID, UUID> entry : tpaHereRequests.entrySet()) {
            if (entry.getValue().equals(target.getUniqueId())) return entry.getKey();
        }
        return null;
    }

    public boolean isTpaHere(UUID senderId) {
        return tpaHereRequests.containsKey(senderId);
    }

    public void removeRequest(UUID senderId) {
        tpaRequests.remove(senderId);
        tpaHereRequests.remove(senderId);
    }

    public void setLastLocation(Player player) {
        lastLocations.put(player.getUniqueId(), player.getLocation());
        locationTimestamp.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public Location getLastLocation(Player player) {
        Long timestamp = locationTimestamp.get(player.getUniqueId());
        if (timestamp == null || (System.currentTimeMillis() - timestamp) > 60000) {
            lastLocations.remove(player.getUniqueId());
            return null;
        }
        return lastLocations.get(player.getUniqueId());
    }
}