package org.xFeneq.ftpa.commands;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

public class BackCommand implements CommandExecutor {

    private final FancyTPA plugin;

    public BackCommand(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        Location backLoc = plugin.getTpaManager().getLastLocation(player);
        if (backLoc == null) {
            player.sendMessage(ColorUtil.color("&cNo back location found or it expired (1 min)."));
            return true;
        }

        int delay = plugin.getConfig().getInt("settings.back-delay", 10);
        player.sendMessage(ColorUtil.color("&eTeleporting back in " + delay + "s..."));

        new BukkitRunnable() {
            int timeLeft = delay * 20;
            double angle = 0;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    player.teleport(backLoc);
                    player.sendMessage(ColorUtil.color("&aReturned!"));
                    this.cancel();
                    return;
                }

                if (plugin.getConfig().getBoolean("effects.circle-enabled", true)) {
                    double x = 0.8 * Math.cos(angle);
                    double z = 0.8 * Math.sin(angle);
                    player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(x, 0.1, z), 1, 0, 0, 0, 0);
                    angle += 0.3;
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }
}