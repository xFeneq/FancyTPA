package org.xFeneq.ftpa;

import org.bukkit.plugin.java.JavaPlugin;
import org.xFeneq.ftpa.commands.AdminCommands;
import org.xFeneq.ftpa.commands.PlayerCommands;
import org.xFeneq.ftpa.commands.TpAcceptCommand;
import org.xFeneq.ftpa.commands.TpDenyCommand;
import org.xFeneq.ftpa.commands.TpaManager;

public final class FancyTPA extends JavaPlugin {

    private TpaManager tpaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.tpaManager = new TpaManager(this);

        PlayerCommands playerCommands = new PlayerCommands(this);
        getCommand("tpa").setExecutor(playerCommands);
        getCommand("tpahere").setExecutor(playerCommands);

        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this));

        AdminCommands adminCommands = new AdminCommands(this);
        getCommand("tp").setExecutor(adminCommands);
        getCommand("tphere").setExecutor(adminCommands);

        getLogger().info("FancyTPA enabled successfully!");
    }

    public TpaManager getTpaManager() {
        return tpaManager;
    }
}