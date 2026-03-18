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

        // --- NASZ WŁASNY ANTY-LOGOUT ---
        if (plugin.getCombatManager().isInCombat(player)) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.combat-stop", "&cYou are in combat! Wait before using commands.")));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ColorUtil.color("&cUsage: /" + label + " <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline", "&cOffline!")));
            return true;
        }

        if (command.getName().equalsIgnoreCase("tpa")) {
            plugin.getTpaManager().sendTpaRequest(player, target);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-sent", "&aSent!").replace("{player}", target.getName())));
        } else if (command.getName().equalsIgnoreCase("tpahere")) {
            plugin.getTpaManager().sendTpaHereRequest(player, target);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.request-sent", "&aSent!").replace("{player}", target.getName())));
        }

        return true;
    }
}