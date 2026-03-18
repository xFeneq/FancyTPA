package org.xFeneq.ftpa;

import org.bukkit.plugin.java.JavaPlugin;
import org.xFeneq.ftpa.commands.*;
import org.xFeneq.ftpa.utils.CombatManager;

public final class FancyTPA extends JavaPlugin {

    private TpaManager tpaManager;
    private CombatManager combatManager;

    @Override
    public void onEnable() {
        // Inicjalizacja konfiguracji
        saveDefaultConfig();

        // Inicjalizacja managerów
        this.tpaManager = new TpaManager(this);
        this.combatManager = new CombatManager(this);

        // Rejestracja eventów (CombatLog)
        getServer().getPluginManager().registerEvents(combatManager, this);

        // Instancje executorów
        PlayerCommands playerCommands = new PlayerCommands(this);
        AdminCommands adminCommands = new AdminCommands(this);
        TpaTabCompleter tabCompleter = new TpaTabCompleter();

        // Rejestracja komendy głównej (Reload)
        if (getCommand("ftpa") != null) {
            getCommand("ftpa").setExecutor(new ReloadCommand(this));
        }

        // Rejestracja pozostałych komend z ich aliasami/nazwami z plugin.yml
        registerCommand("tpa", playerCommands, tabCompleter);
        registerCommand("tpahere", playerCommands, tabCompleter);
        registerCommand("tpaccept", new TpAcceptCommand(this), null);
        registerCommand("tpdeny", new TpDenyCommand(this), null);
        registerCommand("back", new BackCommand(this), null);
        registerCommand("tp", adminCommands, tabCompleter);
        registerCommand("tphere", adminCommands, tabCompleter);
        registerCommand("tpall", adminCommands, tabCompleter);

        getLogger().info("========================================");
        getLogger().info("FancyTPA v2.2 został pomyślnie włączony!");
        getLogger().info("Autor: xFeneq");
        getLogger().info("========================================");
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor, org.bukkit.command.TabCompleter tab) {
        if (getCommand(name) != null) {
            getCommand(name).setExecutor(executor);
            if (tab != null) {
                getCommand(name).setTabCompleter(tab);
            }
        }
    }

    public TpaManager getTpaManager() { return tpaManager; }
    public CombatManager getCombatManager() { return combatManager; }
}