package me.holypite.sheepWarsJava.Players;

import me.holypite.sheepWarsJava.Maps.MapData;
import me.holypite.sheepWarsJava.Maps.MapManager;
import me.holypite.sheepWarsJava.Maps.WorldManager;
import me.holypite.sheepWarsJava.SheepWarsJava;
import me.holypite.sheepWarsJava.Tools.World;
import me.holypite.sheepWarsJava.Tools.TKit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager implements Listener {

    private final SheepWarsJava plugin;
    public org.bukkit.World lobbyWorld;
    public org.bukkit.World gameWorld;
    public MapData selectedMap;
    private List<Player> team1 = new ArrayList<>();
    private List<Player> team2 = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private final Location lobbySpawn;
    private static final int minPlayers = 2;
    private boolean gameStarted = false;
    private final MapManager mapManager;


    public static int getMinPlayers() {
        return minPlayers;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public GameManager(SheepWarsJava plugin) {
        this.plugin = plugin;
        this.lobbyWorld = Bukkit.getWorld("lobby_sheepwars-1");
        this.lobbySpawn = new Location(lobbyWorld, 0, 64, 0);
        this.mapManager = new MapManager();
    }

    public void startGame() {
        if (gameWorld.getPlayers().size() >= minPlayers && !gameStarted) {
            gameStarted = true;
            gameWorld = WorldManager.createNewGameWorld();
            selectedMap = selectRandomMap();
            World.loadStructure(gameWorld, TKit.vectorToLocation(gameWorld,selectedMap.pasteLocation()),("sheepwars_"+selectedMap.mapName()));
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

    private void assignTeams() {
        List<Player> players = new ArrayList<>(gameWorld.getPlayers());
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
        List<Vector> team1Spawns = selectedMap.team1Spawns();
        List<Vector> team2Spawns = selectedMap.team2Spawns();

        for (int i = 0; i < team1.size(); i++) {
            team1.get(i).teleport(TKit.vectorsToLocations(gameWorld,team1Spawns).get(i % team1Spawns.size()));
        }
        for (int i = 0; i < team2.size(); i++) {
            team2.get(i).teleport(TKit.vectorsToLocations(gameWorld,team2Spawns).get(i % team2Spawns.size()));
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
        for (Player player : gameWorld.getPlayers()) {
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
