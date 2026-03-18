package org.xFeneq.ftpa.commands;

import org.bukkit.Bukkit;
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
        Player admin = (Player) sender;

        // Handling /tp <player>
        if (command.getName().equalsIgnoreCase("tp")) {
            if (!checkAdminPerm(admin, "permissions.admin-tp")) return true;
            if (args.length < 1) return false;

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                admin.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
                return true;
            }

            admin.teleport(target.getLocation());
            admin.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.admin-tp-success").replace("{target}", target.getName())));
            return true;
        }

        // Handling /tphere <player/all> [dimension]
        if (command.getName().equalsIgnoreCase("tphere")) {
            if (!checkAdminPerm(admin, "permissions.admin-tphere")) return true;
            if (args.length < 1) return false;

            // Summon all players
            if (args[0].equalsIgnoreCase("all")) {
                World world = (args.length == 2) ? Bukkit.getWorld(args[1]) : null;
                int count = 0;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.equals(admin)) continue;
                    if (world != null && !p.getWorld().equals(world)) continue;

                    p.teleport(admin.getLocation());
                    count++;
                }

                String msg = (world == null) ? "messages.admin-tphere-all" : "messages.admin-tphere-dimension";
                admin.sendMessage(ColorUtil.color(plugin.getConfig().getString(msg)
                        .replace("{count}", String.valueOf(count))
                        .replace("{world}", world != null ? world.getName() : "")));
                return true;
            }

            // Summon single player
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                admin.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
                return true;
            }

            target.teleport(admin.getLocation());
            admin.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.admin-tphere-success").replace("{target}", target.getName())));
        }

        return true;
    }

    private boolean checkAdminPerm(Player player, String configPath) {
        String perm = plugin.getConfig().getString(configPath);
        if (player.hasPermission(perm) || player.hasPermission(plugin.getConfig().getString("permissions.admin-all"))) {
            return true;
        }
        player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.no-permission").replace("{perm}", perm)));
        return false;
    }
}