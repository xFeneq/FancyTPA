package org.xFeneq.ftpa.commands;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

        String perm = plugin.getConfig().getString("permissions.player-tpaccept");
        if (!target.hasPermission(perm) && !target.hasPermission(plugin.getConfig().getString("permissions.player-all"))) {
            target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission").replace("{perm}", perm)));
            return true;
        }

        UUID requesterUUID = (args.length > 0) ? Bukkit.getPlayerUniqueId(args[0]) : plugin.getTpaManager().getRequestSender(target);

        if (requesterUUID == null) {
            target.sendMessage(ColorUtil.color("&cYou have no pending requests."));
            return true;
        }

        Player requester = Bukkit.getPlayer(requesterUUID);
        if (requester == null) {
            target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
            return true;
        }

        startTeleportTimer(requester, target);
        return true;
    }

    private void startTeleportTimer(Player requester, Player target) {
        int delay = plugin.getConfig().getInt("settings.teleport-delay");

        new BukkitRunnable() {
            int timeLeft = delay;
            double angle = 0;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    executeFinalTeleport(requester, target);
                    this.cancel();
                    return;
                }

                // Circle Effect logic
                if (plugin.getConfig().getBoolean("effects.circle-enabled", true)) {
                    Location loc = requester.getLocation();
                    for (int i = 0; i < 20; i++) {
                        double x = 0.8 * Math.cos(i * Math.PI / 10);
                        double z = 0.8 * Math.sin(i * Math.PI / 10);
                        loc.add(x, 0.1, z);
                        loc.getWorld().spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0, 0);
                        loc.subtract(x, 0.1, z);
                    }
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void executeFinalTeleport(Player requester, Player target) {
        plugin.getTpaManager().setLastLocation(requester);
        Location startPos = requester.getLocation().clone();
        Location endPos = target.getLocation().clone();

        if (plugin.getTpaManager().isTpaHere(requester.getUniqueId())) {
            target.teleport(requester.getLocation());
        } else {
            requester.teleport(target.getLocation());
        }

        if (plugin.getConfig().getBoolean("effects.enabled")) {
            playAdvancedEffects(startPos, endPos);
        }

        requester.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-success")));
        plugin.getTpaManager().removeRequest(requester.getUniqueId());
    }

    private void playAdvancedEffects(Location start, Location end) {
        Particle p = Particle.valueOf(plugin.getConfig().getString("effects.particle-type", "PORTAL"));
        Sound s = Sound.valueOf(plugin.getConfig().getString("effects.sound", "ENTITY_ENDERMAN_TELEPORT"));

        for (int i = 0; i < 50; i++) {
            double x = (Math.random() - 0.5) * 2;
            double y = Math.random() * 2;
            double z = (Math.random() - 0.5) * 2;
            start.getWorld().spawnParticle(p, start.clone().add(x, y, z), 0, -x, -y, -z, 0.1);
        }
        start.getWorld().playSound(start, s, 1.0f, 1.0f);

        for (int i = 0; i < 50; i++) {
            double dx = (Math.random() - 0.5);
            double dy = (Math.random() - 0.5);
            double dz = (Math.random() - 0.5);
            end.getWorld().spawnParticle(p, end.clone().add(0, 1, 0), 0, dx, dy, dz, 0.2);
        }
        end.getWorld().playSound(end, s, 1.0f, 1.0f);
    }
}