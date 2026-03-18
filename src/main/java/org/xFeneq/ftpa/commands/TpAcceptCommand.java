package org.xFeneq.ftpa.commands;

import org.bukkit.Bukkit;
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
import java.util.UUID;

public class TpAcceptCommand implements CommandExecutor {

    private final FancyTPA plugin;

    public TpAcceptCommand(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player target = (Player) sender;

        if (plugin.getCombatManager().isInCombat(target)) {
            target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.combat-stop").replace("{time}", String.valueOf(plugin.getCombatManager().getRemainingCombatTime(target)))));
            return true;
        }

        UUID requesterUUID = plugin.getTpaManager().getRequestSender(target);
        if (requesterUUID == null) return true;

        Player requester = Bukkit.getPlayer(requesterUUID);
        if (requester == null) return true;

        if (plugin.getCombatManager().isInCombat(requester)) {
            target.sendMessage(ColorUtil.color("&cThat player is currently in combat."));
            return true;
        }

        int delay = plugin.getConfig().getInt("settings.teleport-delay", 5);
        boolean isHere = plugin.getTpaManager().isTpaHere(requesterUUID);
        Player movingPlayer = isHere ? target : requester;
        Player destinationPlayer = isHere ? requester : target;

        plugin.getTpaManager().removeRequest(requesterUUID);

        BukkitTask task = new BukkitRunnable() {
            int timeLeft = delay * 20;
            final Location startPos = movingPlayer.getLocation();
            double angle = 0;

            @Override
            public void run() {
                if (startPos.distance(movingPlayer.getLocation()) > 0.5 && plugin.getConfig().getBoolean("settings.cancel-on-move", true)) {
                    movingPlayer.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-cancelled")));
                    plugin.getTpaManager().removeActiveTask(movingPlayer);
                    this.cancel();
                    return;
                }

                if (timeLeft % 20 == 0) {
                    plugin.getTpaManager().sendActionBar(movingPlayer, plugin.getConfig().getString("messages.teleporting-actionbar").replace("{time}", String.valueOf(timeLeft / 20)));
                }

                if (timeLeft <= 0) {
                    plugin.getTpaManager().setLastLocation(movingPlayer);
                    movingPlayer.teleport(destinationPlayer.getLocation());
                    movingPlayer.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-success")));
                    plugin.getTpaManager().removeActiveTask(movingPlayer);
                    this.cancel();
                    return;
                }

                if (plugin.getConfig().getBoolean("effects.circle-enabled", true)) {
                    double x = 0.8 * Math.cos(angle);
                    double z = 0.8 * Math.sin(angle);
                    movingPlayer.getWorld().spawnParticle(Particle.SPELL_WITCH, movingPlayer.getLocation().add(x, 0.1, z), 1, 0, 0, 0, 0);
                    angle += 0.3;
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        plugin.getTpaManager().addActiveTask(movingPlayer, task);
        return true;
    }
}