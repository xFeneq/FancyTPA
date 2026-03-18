package org.xFeneq.ftpa.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;
import java.util.UUID;

public class TpDenyCommand implements CommandExecutor {
    private final FancyTPA plugin;
    public TpDenyCommand(FancyTPA plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player target = (Player) sender;
        UUID requesterUUID = plugin.getTpaManager().getRequestSender(target);

        if (requesterUUID == null) {
            target.sendMessage(ColorUtil.color("&cNo pending requests."));
            return true;
        }

        plugin.getTpaManager().removeRequest(requesterUUID);
        target.sendMessage(ColorUtil.color("&cRequest denied."));
        return true;
    }
}