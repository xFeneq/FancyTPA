package org.xFeneq.ftpa.commands;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.combat-stop")
                    .replace("{time}", String.valueOf(plugin.getCombatManager().getRemainingCombatTime(player)))));
            return true;
        }

        Location backLoc = plugin.getTpaManager().getLastLocation(player);
        if (backLoc == null) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-back-location")));
            return true;
        }

        int delay = plugin.getConfig().getInt("settings.back-delay", 10);

        if (delay <= 0) {
            performBackTeleport(player, backLoc);
            return true;
        }

        BukkitTask task = new BukkitRunnable() {
            int ticks = delay * 20;
            final Location startPos = player.getLocation().clone();
            double phi = 0;

            @Override
            public void run() {
                if (plugin.getConfig().getBoolean("settings.cancel-on-move", true)) {
                    if (startPos.getWorld() != player.getWorld() || startPos.distance(player.getLocation()) > 0.5) {
                        player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-cancelled")));
                        plugin.getTpaManager().removeActiveTask(player);
                        this.cancel();
                        return;
                    }
                }

                if (ticks % 20 == 0) {
                    plugin.getTpaManager().sendActionBar(player, plugin.getConfig().getString("messages.back-timer")
                            .replace("{time}", String.valueOf(ticks / 20)));
                    player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("effects.sound-countdown", "BLOCK_NOTE_BLOCK_CHIME")), 0.6f, 1.2f);
                }

                if (plugin.getConfig().getBoolean("effects.waiting-spiral", true)) {
                    phi += Math.PI / 10;
                    int pAmount = plugin.getConfig().getInt("effects.particle-amount", 50) / 10;
                    for (double t = 0; t <= 2 * Math.PI; t += Math.PI) {
                        double r = 0.7;
                        double x = r * Math.cos(t + phi);
                        double y = 2.0 - ((double) ticks / (delay * 20.0)) * 2.0;
                        double z = r * Math.sin(t + phi);
                        player.getWorld().spawnParticle(Particle.valueOf(plugin.getConfig().getString("effects.particle-main", "PORTAL")), player.getLocation().add(x, y, z), Math.max(1, pAmount), 0, 0, 0, 0);
                    }
                }

                if (ticks <= 0) {
                    performBackTeleport(player, backLoc);
                    plugin.getTpaManager().removeActiveTask(player);
                    this.cancel();
                    return;
                }
                ticks--;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        plugin.getTpaManager().addActiveTask(player, task);
        return true;
    }

    private void performBackTeleport(Player player, Location loc) {
        spawnBurst(player.getLocation());
        player.teleport(loc);
        spawnBurst(player.getLocation());
        player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("effects.sound-teleport", "ENTITY_ENDERMAN_TELEPORT")), 1.0f, 1.0f);
        player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.back-success")));
    }

    private void spawnBurst(Location loc) {
        if (!plugin.getConfig().getBoolean("effects.teleport-burst", true)) return;
        int amount = plugin.getConfig().getInt("effects.particle-amount", 50);
        loc.getWorld().spawnParticle(Particle.valueOf(plugin.getConfig().getString("effects.particle-burst", "DRAGON_BREATH")), loc.clone().add(0, 1, 0), amount, 0.5, 0.5, 0.5, 0.1);
    }
}