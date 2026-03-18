package org.xFeneq.ftpa.commands;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
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

        UUID requesterUUID = plugin.getTpaManager().getRequestSender(target);
        if (requesterUUID == null) {
            target.sendMessage(ColorUtil.color("&cNo pending requests."));
            return true;
        }

        Player requester = Bukkit.getPlayer(requesterUUID);
        if (requester == null) {
            target.sendMessage(ColorUtil.color("&cPlayer is offline."));
            return true;
        }

        int delay = plugin.getConfig().getInt("settings.teleport-delay", 5);
        target.sendMessage(ColorUtil.color("&aAccepted! Teleporting in " + delay + "s."));
        requester.sendMessage(ColorUtil.color("&a" + target.getName() + " accepted your request!"));

        new BukkitRunnable() {
            int timeLeft = delay * 20;
            double angle = 0;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    plugin.getTpaManager().setLastLocation(plugin.getTpaManager().isTpaHere(requesterUUID) ? target : requester);

                    if (plugin.getTpaManager().isTpaHere(requesterUUID)) {
                        target.teleport(requester.getLocation());
                    } else {
                        requester.teleport(target.getLocation());
                    }

                    plugin.getTpaManager().removeRequest(requesterUUID);
                    this.cancel();
                    return;
                }

                if (plugin.getConfig().getBoolean("effects.circle-enabled", true)) {
                    Player p = plugin.getTpaManager().isTpaHere(requesterUUID) ? target : requester;
                    double x = 0.8 * Math.cos(angle);
                    double z = 0.8 * Math.sin(angle);
                    p.getWorld().spawnParticle(Particle.SPELL_WITCH, p.getLocation().add(x, 0.1, z), 1, 0, 0, 0, 0);
                    angle += 0.3;
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }
}