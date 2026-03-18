package org.xFeneq.ftpa.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

public class BackCommand implements CommandExecutor {

    private final FancyTPA plugin;

    public BackCommand(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Sprawdzanie walki
        if (isPlayerInCombat(player)) {
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.combat-stop", "&cYou cannot do this while in combat!")));
            return true;
        }

        Location backLoc = plugin.getTpaManager().getLastLocation(player);

        if (backLoc == null) {
            player.sendMessage(ColorUtil.color("&cNo previous location found or it has expired (1 minute)."));
            return true;
        }

        int delay = plugin.getConfig().getInt("settings.back-delay", 10);
        player.sendMessage(ColorUtil.color("&eTeleporting back in " + delay + " seconds. Don't move!"));

        new BukkitRunnable() {
            int timeLeft = delay * 20; // Przeliczamy na ticki dla płynniejszych cząsteczek
            final Location startPos = player.getLocation();
            double angle = 0;

            @Override
            public void run() {
                // Anulowanie jeśli gracz się ruszy
                if (startPos.distance(player.getLocation()) > 0.5) {
                    player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-cancelled", "&cCancelled! You moved.")));
                    this.cancel();
                    return;
                }

                // Efekt kręgu (wyświetlany co tick dla płynności)
                if (plugin.getConfig().getBoolean("effects.circle-enabled", true)) {
                    double x = 0.8 * Math.cos(angle);
                    double z = 0.8 * Math.sin(angle);
                    player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().add(x, 0.1, z), 1, 0, 0, 0, 0);
                    angle += 0.3;
                }

                if (timeLeft <= 0) {
                    player.teleport(backLoc);
                    player.sendMessage(ColorUtil.color("&aReturned to previous location!"));
                    this.cancel();
                    return;
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Uruchamiamy co 1 tick dla płynnego kręgu

        return true;
    }

    private boolean isPlayerInCombat(Player player) {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("PvPManager")) {
                if (me.NoEul.PvPManager.PvPManager.getInstance().getPlayerHandler().get(player).isInCombat()) return true;
            }
            if (Bukkit.getPluginManager().isPluginEnabled("CombatLogX")) {
                com.github.sirblobman.combatlogx.api.ICombatLogX api = (com.github.sirblobman.combatlogx.api.ICombatLogX) Bukkit.getPluginManager().getPlugin("CombatLogX");
                if (api.getCombatManager().isInCombat(player)) return true;
            }
            if (Bukkit.getPluginManager().isPluginEnabled("DeluxeCombat")) {
                nl.marido.deluxecombat.api.DeluxeCombatAPI api = new nl.marido.deluxecombat.api.DeluxeCombatAPI();
                if (api.isInCombat(player)) return true;
            }
        } catch (NoClassDefFoundError | Exception ignored) {}
        return false;
    }
}