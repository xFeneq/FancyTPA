package org.xFeneq.ftpa;

import org.bukkit.plugin.java.JavaPlugin;
import org.xFeneq.ftpa.commands.*;

public final class FancyTPA extends JavaPlugin {

    private TpaManager tpaManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.tpaManager = new TpaManager(this);

        PlayerCommands playerCommands = new PlayerCommands(this);
        AdminCommands adminCommands = new AdminCommands(this);
        TpaTabCompleter tabCompleter = new TpaTabCompleter();

        getCommand("tpa").setExecutor(playerCommands);
        getCommand("tpa").setTabCompleter(tabCompleter);

        getCommand("tpahere").setExecutor(playerCommands);
        getCommand("tpahere").setTabCompleter(tabCompleter);

        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this));

        getCommand("tp").setExecutor(adminCommands);
        getCommand("tp").setTabCompleter(tabCompleter);

        getCommand("tphere").setExecutor(adminCommands);
        getCommand("tphere").setTabCompleter(tabCompleter);

        getLogger().info("FancyTPA enabled successfully!");
    }

    public TpaManager getTpaManager() {
        return tpaManager;
    }
}