package org.xFeneq.ftpa.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

public class AdminCommands implements CommandExecutor {

    private final FancyTPA plugin;

    public AdminCommands(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("tp")) {
            if (!player.hasPermission("ftpa.admin.tp")) return true;

            // /tp <player>
            if (args.length == 1) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
                    return true;
                }
                player.teleport(target);
                player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.admin-tp-success").replace("{target}", target.getName())));
                return true;
            }
            // /tp <player> <target>
            else if (args.length == 2) {
                Player p1 = Bukkit.getPlayer(args[0]);
                Player p2 = Bukkit.getPlayer(args[1]);
                if (p1 == null || p2 == null) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
                    return true;
                }
                p1.teleport(p2);
                player.sendMessage(ColorUtil.color("&aTeleported " + p1.getName() + " to " + p2.getName()));
                return true;
            }
            // /tp <x> <y> <z>
            else if (args.length == 3) {
                Location loc = parseLocation(player.getLocation(), args[0], args[1], args[2]);
                if (loc == null) {
                    player.sendMessage(ColorUtil.color("&cInvalid coordinates."));
                    return true;
                }
                player.teleport(loc);
                player.sendMessage(ColorUtil.color("&aTeleported to coordinates."));
                return true;
            }
            // /tp <player> <x> <y> <z>
            else if (args.length == 4) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
                    return true;
                }
                Location loc = parseLocation(target.getLocation(), args[1], args[2], args[3]);
                if (loc == null) {
                    player.sendMessage(ColorUtil.color("&cInvalid coordinates."));
                    return true;
                }
                target.teleport(loc);
                player.sendMessage(ColorUtil.color("&aTeleported " + target.getName() + " to coordinates."));
                return true;
            } else {
                player.sendMessage(ColorUtil.color("&cUsage: /tp <player> or /tp <x> <y> <z> or /tp <player> <x> <y> <z>"));
                return true;
            }
        }
        else if (command.getName().equalsIgnoreCase("tphere")) {
            if (!player.hasPermission("ftpa.admin.tphere")) return true;
            if (args.length != 1) return true;
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) return true;
            target.teleport(player);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.admin-tphere-success").replace("{target}", target.getName())));
        }
        else if (command.getName().equalsIgnoreCase("tpall")) {
            if (!player.hasPermission("ftpa.admin.tpall")) return true;
            Player destination = player;
            World sourceWorld = null;

            // /tpall <nick>
            if (args.length >= 1) {
                destination = Bukkit.getPlayer(args[0]);
                if (destination == null) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
                    return true;
                }
            }
            // /tpall <nick> <dimension>
            if (args.length == 2) {
                sourceWorld = Bukkit.getWorld(args[1]);
                if (sourceWorld == null) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.world-not-found").replace("{world}", args[1])));
                    return true;
                }
            }

            int count = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.equals(destination)) continue;
                if (sourceWorld != null && !p.getWorld().equals(sourceWorld)) continue;
                p.teleport(destination);
                count++;
            }
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.admin-tpall-success").replace("{count}", String.valueOf(count))));
        }

        return true;
    }

    private Location parseLocation(Location base, String xStr, String yStr, String zStr) {
        try {
            double x = xStr.startsWith("~") ? base.getX() + (xStr.length() > 1 ? Double.parseDouble(xStr.substring(1)) : 0) : Double.parseDouble(xStr);
            double y = yStr.startsWith("~") ? base.getY() + (yStr.length() > 1 ? Double.parseDouble(yStr.substring(1)) : 0) : Double.parseDouble(yStr);
            double z = zStr.startsWith("~") ? base.getZ() + (zStr.length() > 1 ? Double.parseDouble(zStr.substring(1)) : 0) : Double.parseDouble(zStr);
            return new Location(base.getWorld(), x, y, z, base.getYaw(), base.getPitch());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}