package me.holypite.sheepWarsJava.Players;

import me.holypite.sheepWarsJava.SheepWarsJava;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerManager implements Listener {

    private final SheepWarsJava plugin;
    private final GameManager gameManager;

    public PlayerManager(SheepWarsJava plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (gameManager.isGameStarted()) {
            gameManager.makeSpectator(player);
            gameManager.checkGameEnd();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (gameManager.isGameStarted() && player.getLocation().getY() < 0) {
            gameManager.makeSpectator(player);
            gameManager.checkGameEnd();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (gameManager.isGameStarted()) {
            gameManager.makeSpectator(player);
            gameManager.checkGameEnd();
        }
    }
}
