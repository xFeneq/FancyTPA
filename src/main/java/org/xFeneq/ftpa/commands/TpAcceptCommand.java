package org.xFeneq.ftpa.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.xFeneq.ftpa.FancyTPA;
import org.xFeneq.ftpa.utils.ColorUtil;

import java.util.UUID;

public class TpAcceptCommand implements CommandExecutor {

    private final FancyTPA plugin;

    public TpAcceptCommand(FancyTPA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player target = (Player) sender;

        // Sprawdzenie czy osoba akceptująca nie jest w walce
        if (plugin.getCombatManager().isInCombat(target)) {
            target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.combat-stop")
                    .replace("{time}", String.valueOf(plugin.getCombatManager().getRemainingCombatTime(target)))));
            return true;
        }

        UUID requesterUUID = plugin.getTpaManager().getRequestSender(target);
        if (requesterUUID == null) {
            target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.prefix") + "&cNie masz oczekujących próśb."));
            return true;
        }

        Player requester = Bukkit.getPlayer(requesterUUID);
        if (requester == null) {
            target.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.player-offline")));
            plugin.getTpaManager().removeRequest(requesterUUID);
            return true;
        }

        // Sprawdzenie czy osoba wysyłająca nie weszła w walkę w międzyczasie
        if (plugin.getCombatManager().isInCombat(requester)) {
            target.sendMessage(ColorUtil.color("&cGracz &f" + requester.getName() + " &cjest obecnie w walce!"));
            return true;
        }

        int delay = plugin.getConfig().getInt("settings.teleport-delay", 5);
        boolean isHere = plugin.getTpaManager().isTpaHere(requesterUUID);

        // Określamy kto się rusza, a kto jest celem
        Player movingPlayer = isHere ? target : requester;
        Player destinationPlayer = isHere ? requester : target;

        // Usuwamy prośbę z mapy, bo została zaakceptowana
        plugin.getTpaManager().removeRequest(requesterUUID);

        // Jeśli delay wynosi 0, teleportujemy natychmiast
        if (delay <= 0) {
            performTeleport(movingPlayer, destinationPlayer);
            return true;
        }

        // Start odliczania z efektami
        BukkitTask task = new BukkitRunnable() {
            int ticks = delay * 20;
            final Location startPos = movingPlayer.getLocation().clone();
            double phi = 0;

            @Override
            public void run() {
                // 1. Sprawdzenie czy gracz się ruszył
                if (plugin.getConfig().getBoolean("settings.cancel-on-move", true)) {
                    if (startPos.getWorld() != movingPlayer.getWorld() || startPos.distance(movingPlayer.getLocation()) > 0.5) {
                        movingPlayer.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-cancelled")));
                        plugin.getTpaManager().removeActiveTask(movingPlayer);
                        this.cancel();
                        return;
                    }
                }

                // 2. Co sekundę (20 ticków) wysyłamy Action Bar i dźwięk odliczania
                if (ticks % 20 == 0) {
                    plugin.getTpaManager().sendActionBar(movingPlayer, plugin.getConfig().getString("messages.teleporting-actionbar")
                            .replace("{time}", String.valueOf(ticks / 20)));

                    movingPlayer.playSound(movingPlayer.getLocation(),
                            Sound.valueOf(plugin.getConfig().getString("effects.sound-countdown", "BLOCK_NOTE_BLOCK_CHIME")), 0.6f, 1.2f);
                }

                // 3. Efekt wizualny spirali (wykonywany co tick)
                if (plugin.getConfig().getBoolean("effects.waiting-spiral", true)) {
                    phi += Math.PI / 10;
                    int pAmount = plugin.getConfig().getInt("effects.particle-amount", 50) / 10;
                    if (pAmount < 1) pAmount = 1;

                    for (double t = 0; t <= 2 * Math.PI; t += Math.PI) {
                        double r = 0.7;
                        double x = r * Math.cos(t + phi);
                        // Cząsteczki "wędrują" od dołu do góry w zależności od czasu
                        double y = 2.0 - ((double) ticks / (delay * 20.0)) * 2.0;
                        double z = r * Math.sin(t + phi);

                        Location particleLoc = movingPlayer.getLocation().add(x, y, z);

                        movingPlayer.getWorld().spawnParticle(
                                Particle.valueOf(plugin.getConfig().getString("effects.particle-main", "PORTAL")),
                                particleLoc, pAmount, 0, 0, 0, 0);

                        movingPlayer.getWorld().spawnParticle(
                                Particle.valueOf(plugin.getConfig().getString("effects.particle-secondary", "ENCHANTMENT_TABLE")),
                                particleLoc, pAmount, 0, 0, 0, 0);
                    }
                }

                // 4. Moment teleportacji
                if (ticks <= 0) {
                    performTeleport(movingPlayer, destinationPlayer);
                    plugin.getTpaManager().removeActiveTask(movingPlayer);
                    this.cancel();
                    return;
                }

                ticks--;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Rejestrujemy zadanie, aby system walki mógł je przerwać
        plugin.getTpaManager().addActiveTask(movingPlayer, task);
        return true;
    }

    private void performTeleport(Player movingPlayer, Player destinationPlayer) {
        // Efekt wybuchu przed zniknięciem
        spawnBurst(movingPlayer.getLocation());

        // Zapisujemy lokację do komendy /back
        plugin.getTpaManager().setLastLocation(movingPlayer);

        // Teleportacja
        movingPlayer.teleport(destinationPlayer.getLocation());

        // Efekt wybuchu po pojawieniu się
        spawnBurst(movingPlayer.getLocation());

        // Dźwięk końcowy
        movingPlayer.playSound(movingPlayer.getLocation(),
                Sound.valueOf(plugin.getConfig().getString("effects.sound-teleport", "ENTITY_ENDERMAN_TELEPORT")), 1.0f, 1.0f);

        movingPlayer.sendMessage(ColorUtil.color(plugin.getConfig().getString("messages.teleport-success")));
    }

    private void spawnBurst(Location loc) {
        if (!plugin.getConfig().getBoolean("effects.teleport-burst", true)) return;

        int amount = plugin.getConfig().getInt("effects.particle-amount", 50);
        String burstType = plugin.getConfig().getString("effects.particle-burst", "DRAGON_BREATH");
        String mainType = plugin.getConfig().getString("effects.particle-main", "PORTAL");

        loc.getWorld().spawnParticle(Particle.valueOf(burstType), loc.clone().add(0, 1, 0), amount, 0.5, 0.5, 0.5, 0.1);
        loc.getWorld().spawnParticle(Particle.valueOf(mainType), loc, amount / 2, 0.3, 0.3, 0.3, 0.2);
    }
}