package org.xFeneq.ftpa.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.xFeneq.ftpa.FancyTPA;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager implements Listener {

    private final FancyTPA plugin;
    private final Map<UUID, Long> combatLog = new HashMap<>();

    public CombatManager(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        tag(victim);
        tag(attacker);
    }

    private void tag(Player player) {
        if (player.hasPermission(plugin.getConfig().getString("permissions.combat-bypass", "fancytpa.bypass.combat"))) return;
        combatLog.put(player.getUniqueId(), System.currentTimeMillis());

        // Przerwij aktywną teleportację
        if (plugin.getTpaManager().hasActiveTask(player)) {
            plugin.getTpaManager().cancelTask(player);
            player.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-cancelled")));
        }
    }

    public boolean isInCombat(Player player) {
        if (player.hasPermission(plugin.getConfig().getString("permissions.combat-bypass", "fancytpa.bypass.combat"))) return false;
        long combatTime = plugin.getConfig().getInt("settings.combat-timer", 15) * 1000L;
        if (!combatLog.containsKey(player.getUniqueId())) return false;
        long lastHit = combatLog.get(player.getUniqueId());
        if (System.currentTimeMillis() - lastHit > combatTime) {
            combatLog.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public long getRemainingCombatTime(Player player) {
        if (!combatLog.containsKey(player.getUniqueId())) return 0;
        long combatTime = plugin.getConfig().getInt("settings.combat-timer", 15) * 1000L;
        long lastHit = combatLog.get(player.getUniqueId());
        return (combatTime - (System.currentTimeMillis() - lastHit)) / 1000L;
    }
}