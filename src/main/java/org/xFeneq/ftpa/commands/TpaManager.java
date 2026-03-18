package org.xFeneq.ftpa.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaManager {

    private final FancyTPA plugin;
    private final Map<UUID, UUID> tpaRequests = new HashMap<>();
    private final Map<UUID, UUID> tpaHereRequests = new HashMap<>();

    public TpaManager(FancyTPA plugin) {
        this.plugin = plugin;
    }

    public void sendTpaRequest(Player sender, Player target) {
        tpaRequests.put(sender.getUniqueId(), target.getUniqueId());
        sendInteractiveMessage(target, sender, "wants to teleport to you!");
    }

    public void sendTpaHereRequest(Player sender, Player target) {
        tpaHereRequests.put(sender.getUniqueId(), target.getUniqueId());
        sendInteractiveMessage(target, sender, "wants you to teleport to them!");
    }

    private void sendInteractiveMessage(Player target, Player sender, String text) {
        String header = ColorUtil.color(plugin.getConfig().getString("messages.request-header"));
        String footer = ColorUtil.color(plugin.getConfig().getString("messages.request-footer"));

        target.sendMessage(header);
        target.sendMessage(ColorUtil.color(" &6» &e" + sender.getName() + " &7" + text));
        target.sendMessage("");

        TextComponent accept = new TextComponent(ColorUtil.color(plugin.getConfig().getString("messages.accept-button")));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender.getName()));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorUtil.color(plugin.getConfig().getString("messages.accept-hover")))));

        TextComponent deny = new TextComponent(ColorUtil.color(plugin.getConfig().getString("messages.deny-button")));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + sender.getName()));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ColorUtil.color(plugin.getConfig().getString("messages.deny-hover")))));

        TextComponent line = new TextComponent("          ");
        line.addExtra(accept);
        line.addExtra(new TextComponent("    "));
        line.addExtra(deny);

        target.spigot().sendMessage(line);
        target.sendMessage(footer);
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
}