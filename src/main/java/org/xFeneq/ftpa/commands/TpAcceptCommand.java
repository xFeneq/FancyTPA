package org.xfeneq.ftpa.commands;

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
import org.xfeneq.ftpa.FancyTPA;
import org.xfeneq.ftpa.utils.ColorUtil;

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

        // Check permission from config
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
        boolean cancelOnMove = plugin.getConfig().getBoolean("settings.cancel-on-move");

        new BukkitRunnable() {
            int timeLeft = delay;
            final Location startLoc = requester.getLocation();

            @Override
            public void run() {
                // Check if player moved
                if (cancelOnMove && (startLoc.getX() != requester.getLocation().getX() || startLoc.getZ() != requester.getLocation().getZ())) {
                    requester.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-cancelled")));
                    this.cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    executeFinalTeleport(requester, target);
                    this.cancel();
                    return;
                }

                // Show time on Action Bar
                String msg = plugin.getConfig().getString("messages.teleporting-timer").replace("{time}", String.valueOf(timeLeft));
                requester.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ColorUtil.color(msg)));

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void executeFinalTeleport(Player requester, Player target) {
        Location start = requester.getLocation().add(0, 1, 0);
        Location end = target.getLocation().add(0, 1, 0);

        // Teleport based on request type (TPA or TPAHERE)
        if (plugin.getTpaManager().isTpaHere(requester.getUniqueId())) {
            target.teleport(requester.getLocation());
        } else {
            requester.teleport(target.getLocation());
        }

        // Play Visual Effects
        if (plugin.getConfig().getBoolean("effects.enabled")) {
            playEffects(start, end);
        }

        requester.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-success")));
        plugin.getTpaManager().removeRequest(requester.getUniqueId());
    }

    private void playEffects(Location start, Location end) {
        Particle p = Particle.valueOf(plugin.getConfig().getString("effects.particle-type"));
        Sound s = Sound.valueOf(plugin.getConfig().getString("effects.sound"));

        // Implosion at start
        start.getWorld().spawnParticle(p, start, 50, 0.5, 0.5, 0.5, 0.1);
        start.getWorld().playSound(start, s, 1.0f, 1.0f);

        // Explosion at destination
        end.getWorld().spawnParticle(p, end, 50, 0.5, 0.5, 0.5, 0.1);
        end.getWorld().playSound(end, s, 1.0f, 1.0f);
    }
}