package org.xFeneq.ftpa.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

import java.util.UUID;

public class TpDenyCommand implements CommandExecutor {

    private final FancyTPA plugin;

    public TpDenyCommand(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player target = (Player) sender;

        // Get the requester (if nickname provided in args)
        UUID requesterUUID = (args.length > 0) ? Bukkit.getPlayerUniqueId(args[0]) : plugin.getTpaManager().getRequestSender(target);

        if (requesterUUID == null) {
            target.sendMessage(ColorUtil.color("&cNo pending requests to deny."));
            return true;
        }

        Player requester = Bukkit.getPlayer(requesterUUID);
        if (requester != null) {
            requester.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cYour request was denied by " + target.getName()));
        }

        plugin.getTpaManager().removeRequest(requesterUUID);
        target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&7You denied the request."));
        return true;
    }
}