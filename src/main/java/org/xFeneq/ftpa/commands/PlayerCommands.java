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
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (plugin.getCombatManager().isInCombat(player)) {
            String time = String.valueOf(plugin.getCombatManager().getRemainingCombatTime(player));
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.combat-stop").replace("{time}", time)));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ColorUtil.color("&cUsage: /" + label + " <player> [or] /tpa ignore <player>"));
            return true;
        }

        if (args[0].equalsIgnoreCase("ignore") && args.length == 2) {
            Player targetIgnore = Bukkit.getPlayer(args[1]);
            if (targetIgnore == null) {
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
                return true;
            }
            boolean ignored = plugin.getTpaManager().toggleIgnore(player, targetIgnore);
            if (ignored) {
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.ignore-added").replace("{player}", targetIgnore.getName())));
            } else {
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.ignore-removed").replace("{player}", targetIgnore.getName())));
            }
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
            return true;
        }

        if (player.equals(target)) {
            player.sendMessage(ColorUtil.color("&cYou cannot teleport to yourself!"));
            return true;
        }

        if (plugin.getTpaManager().isIgnoring(target, player)) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.ignored-by-target")));
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpa")) {
            plugin.getTpaManager().sendTpaRequest(player, target);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-sent").replace("{player}", target.getName())));
        } else if (command.getName().equalsIgnoreCase("tpahere")) {
            plugin.getTpaManager().sendTpaHereRequest(player, target);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-sent").replace("{player}", target.getName())));
        }

        return true;
    }
}