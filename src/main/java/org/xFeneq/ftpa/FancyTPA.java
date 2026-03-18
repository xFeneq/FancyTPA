package org.xFeneq.ftpa;

import org.bukkit.plugin.java.JavaPlugin;
import org.xFeneq.ftpa.commands.*;
import org.xFeneq.ftpa.utils.CombatManager;

public final class FancyTPA extends JavaPlugin {

    private TpaManager tpaManager;
    private CombatManager combatManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.tpaManager = new TpaManager(this);
        this.combatManager = new CombatManager(this);

        getServer().getPluginManager().registerEvents(combatManager, this);

        TpaTabCompleter tabCompleter = new TpaTabCompleter();

        PlayerCommands playerCommands = new PlayerCommands(this);
        getCommand("tpa").setExecutor(playerCommands);
        getCommand("tpa").setTabCompleter(tabCompleter);
        getCommand("tpahere").setExecutor(playerCommands);
        getCommand("tpahere").setTabCompleter(tabCompleter);
        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this));
        getCommand("back").setExecutor(new BackCommand(this));

        AdminCommands adminCommands = new AdminCommands(this);
        getCommand("tp").setExecutor(adminCommands);
        getCommand("tp").setTabCompleter(tabCompleter);
        getCommand("tphere").setExecutor(adminCommands);
        getCommand("tphere").setTabCompleter(tabCompleter);
        getCommand("tpall").setExecutor(adminCommands);
        getCommand("tpall").setTabCompleter(tabCompleter);

        getLogger().info("FancyTPA v2.0 enabled!");
    }

    public TpaManager getTpaManager() { return tpaManager; }
    public CombatManager getCombatManager() { return combatManager; }
}