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

        // Rejestracja eventu walki
        getServer().getPluginManager().registerEvents(combatManager, this);

        PlayerCommands playerCommands = new PlayerCommands(this);
        getCommand("tpa").setExecutor(playerCommands);
        getCommand("tpahere").setExecutor(playerCommands);
        getCommand("tpaccept").setExecutor(new TpAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TpDenyCommand(this));
        getCommand("back").setExecutor(new BackCommand(this));

        // TabCompleter
        TpaTabCompleter tabCompleter = new TpaTabCompleter();
        getCommand("tpa").setTabCompleter(tabCompleter);
        getCommand("tpahere").setTabCompleter(tabCompleter);

        getLogger().info("FancyTPA enabled with internal Combat Tag!");
    }

    public TpaManager getTpaManager() { return tpaManager; }
    public CombatManager getCombatManager() { return combatManager; }
}