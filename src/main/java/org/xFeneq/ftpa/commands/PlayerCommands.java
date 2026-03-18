package org.xFeneq.ftpa.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

public class PlayerCommands implements CommandExecutor {

    private final FancyTPA plugin;

    public PlayerCommands(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Basic argument check
        if (args.length != 1) {
            player.sendMessage(ColorUtil.color("&cUsage: /" + label + " <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        // Check if target is online
        if (target == null) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline", "&cPlayer is offline.")));
            return true;
        }

        // Prevent self-teleportation
        if (player.equals(target)) {
            player.sendMessage(ColorUtil.color("&cYou cannot teleport to yourself!"));
            return true;
        }

        // Handle /tpa and /tpahere logic
        if (command.getName().equalsIgnoreCase("tpa")) {
            if (!checkPermission(player, "permissions.player-tpa")) return true;

            plugin.getTpaManager().sendTpaRequest(player, target);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-sent").replace("{player}", target.getName())));

        } else if (command.getName().equalsIgnoreCase("tpahere")) {
            if (!checkPermission(player, "permissions.player-tpahere")) return true;

            plugin.getTpaManager().sendTpaHereRequest(player, target);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-sent").replace("{player}", target.getName())));
        }

        return true;
    }

    private boolean checkPermission(Player player, String configPath) {
        String permission = plugin.getConfig().getString(configPath);
        if (player.hasPermission(permission) || player.hasPermission(plugin.getConfig().getString("permissions.player-all"))) {
            return true;
        }
        player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission").replace("{perm}", permission)));
        return false;
    }
}