package me.holypite.sheepWarsJava.Players;

import me.holypite.sheepWarsJava.Maps.MapData;
import me.holypite.sheepWarsJava.Maps.MapManager;
import me.holypite.sheepWarsJava.Maps.WorldManager;
import me.holypite.sheepWarsJava.SheepWarsJava;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager implements Listener {

    private final SheepWarsJava plugin;
    private World gameWorld;
    private List<Player> team1 = new ArrayList<>();
    private List<Player> team2 = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private final Location lobbySpawn;
    private static final int minPlayers = 4;
    private boolean gameStarted = false;
    private final MapManager mapManager;

    public GameManager(SheepWarsJava plugin) {
        this.plugin = plugin;
        this.lobbySpawn = new Location(Bukkit.getWorld("lobby_sheepwars-1"), 0, 64, 0);
        this.mapManager = new MapManager();
    }

    public void startGame() {
        if (Bukkit.getOnlinePlayers().size() >= minPlayers && !gameStarted) {
            gameStarted = true;
            gameWorld = WorldManager.createNewGameWorld();
            assignTeams();
            teleportPlayers();
            new BukkitRunnable() {
                @Override
                public void run() {
                    checkGameEnd();
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    public static int getMinPlayers() {
        return minPlayers;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    private void assignTeams() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Random random = new Random();
        while (!players.isEmpty()) {
            Player player = players.remove(random.nextInt(players.size()));
            if (team1.size() <= team2.size()) {
                team1.add(player);
            } else {
                team2.add(player);
            }
        }
    }

    private void teleportPlayers() {
        MapData selectedMap = selectRandomMap();
        List<Location> team1Spawns = selectedMap.team1Spawns();
        List<Location> team2Spawns = selectedMap.team2Spawns();

        for (int i = 0; i < team1.size(); i++) {
            team1.get(i).teleport(team1Spawns.get(i % team1Spawns.size()));
        }
        for (int i = 0; i < team2.size(); i++) {
            team2.get(i).teleport(team2Spawns.get(i % team2Spawns.size()));
        }
    }

    private MapData selectRandomMap() {
        List<MapData> maps = new ArrayList<>(mapManager.getAllMaps().values());
        Random random = new Random();
        return maps.get(random.nextInt(maps.size()));
    }

    void checkGameEnd() {
        if (team1.isEmpty() || team2.isEmpty()) {
            endGame();
        }
    }

    private void endGame() {
        gameStarted = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(lobbySpawn);
        }
        team1.clear();
        team2.clear();
        spectators.clear();
        WorldManager.deleteWorld(gameWorld);
    }

    public void makeSpectator(Player player) {
        if (team1.contains(player)) {
            team1.remove(player);
        } else if (team2.contains(player)) {
            team2.remove(player);
        }
        spectators.add(player);
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
    }
}
