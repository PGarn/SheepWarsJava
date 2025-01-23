package me.holypite.sheepWarsJava.Maps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MapManager {
    private static Map<String, MapData> maps = new HashMap<>();

    public static void addMap(MapData mapData) {
        maps.put(mapData.mapName(), mapData);
    }

    public MapData getMap(String mapName) {
        return maps.get(mapName);
    }

    public Map<String, MapData> getAllMaps() {
        return maps;
    }

    public static void registerMaps() {
        // Exemple de map 1
        World world1 = Bukkit.getWorld("sheepwars_map1");
        Location pasteLocation1 = new Location(world1, 0, 64, 0);
        Location[] team1Spawns1 = {
                new Location(world1, 100, 64, 100),
                new Location(world1, 110, 64, 110)
        };
        Location[] team2Spawns1 = {
                new Location(world1, 200, 64, 200),
                new Location(world1, 210, 64, 210)
        };
        MapData map1 = new MapData("map1", Arrays.asList(team1Spawns1), Arrays.asList(team2Spawns1), pasteLocation1);
        addMap(map1);

        // Exemple de map 2
        World world2 = Bukkit.getWorld("sheepwars_map2");
        Location pasteLocation2 = new Location(world2, 0, 64, 0);
        Location[] team1Spawns2 = {
                new Location(world2, 150, 64, 150),
                new Location(world2, 160, 64, 160)
        };
        Location[] team2Spawns2 = {
                new Location(world2, 250, 64, 250),
                new Location(world2, 260, 64, 260)
        };
        MapData map2 = new MapData("map2", Arrays.asList(team1Spawns2), Arrays.asList(team2Spawns2), pasteLocation2);
        addMap(map2);

        // Ajouter d'autres maps si nécessaire
    }
}
