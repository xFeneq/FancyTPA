package org.xFeneq.ftpa.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

public class ReloadCommand implements CommandExecutor {

    private final FancyTPA plugin;

    public ReloadCommand(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ftpa.admin.reload")) {
            String noPerm = plugin.getConfig().getString("messages.no-permission", "&cNo permission! ({perm})");
            sender.sendMessage(ColorUtil.color(noPerm.replace("{perm}", "ftpa.admin.reload")));
            return true;
        }

        plugin.reloadConfig();
        String prefix = plugin.getConfig().getString("messages.prefix", "&b&lFancyTPA &8» ");
        sender.sendMessage(ColorUtil.color(prefix + "&aConfiguration reloaded successfully!"));
        return true;
    }
}