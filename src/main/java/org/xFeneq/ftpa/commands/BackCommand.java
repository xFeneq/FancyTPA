package org.xFeneq.ftpa.commands;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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

        if (plugin.getCombatManager().isInCombat(player)) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.combat-stop").replace("{time}", String.valueOf(plugin.getCombatManager().getRemainingCombatTime(player)))));
            return true;
        }

        Location backLoc = plugin.getTpaManager().getLastLocation(player);
        if (backLoc == null) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-back-location")));
            return true;
        }

        int delay = plugin.getConfig().getInt("settings.back-delay", 10);

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = delay * 20;
            final Location startPos = player.getLocation();
            double angle = 0;

            @Override
            public void run() {
                if (startPos.distance(player.getLocation()) > 0.5 && plugin.getConfig().getBoolean("settings.cancel-on-move", true)) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-cancelled")));
                    plugin.getTpaManager().removeActiveTask(player);
                    this.cancel();
                    return;
                }

                if (timeLeft % 20 == 0) {
                    plugin.getTpaManager().sendActionBar(player, plugin.getConfig().getString("messages.back-actionbar").replace("{time}", String.valueOf(timeLeft / 20)));
                }

                if (timeLeft <= 0) {
                    player.teleport(backLoc);
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.back-success")));
                    plugin.getTpaManager().removeActiveTask(player);
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

        plugin.getTpaManager().addActiveTask(player, task);
        return true;
    }
}