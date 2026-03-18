package org.xFeneq.ftpa.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

public class BackCommand implements CommandExecutor {
    private final FancyTPA plugin;
    public BackCommand(FancyTPA plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        Location backLoc = plugin.getTpaManager().getLastLocation(player);
        if (backLoc == null) {
            player.sendMessage(ColorUtil.color("&cNo back location found or it expired (1 min)."));
            return true;
        }

        int waitTime = plugin.getConfig().getInt("settings.back-delay", 10);
        player.sendMessage(ColorUtil.color("&eTeleporting back in " + waitTime + "s..."));

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(backLoc);
                player.sendMessage(ColorUtil.color("&aReturned to previous location!"));
            }
        }.runTaskLater(plugin, waitTime * 20L);
        return true;
    }
}