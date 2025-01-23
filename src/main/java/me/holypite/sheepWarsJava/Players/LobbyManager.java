package me.holypite.sheepWarsJava.Players;

import me.holypite.sheepWarsJava.SheepWarsJava;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class LobbyManager implements Listener {

    private final SheepWarsJava plugin;
    private final GameManager gameManager;

    public LobbyManager(SheepWarsJava plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        startLobbyCheck();
    }

    private void startLobbyCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameManager == null){
                    System.out.println("gameManager est null.");
                } else if (gameManager.lobbyWorld.getPlayers().size() >= GameManager.getMinPlayers()) {
                    gameManager.startGame();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
