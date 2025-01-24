package me.holypite.sheepWarsJava.Maps;

import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
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
        registerMap(
                "map1",
                new Vector(0, 64, 0), // pasteLocation
                List.of( // team1Spawns
                        new Vector(100, 64, 100),
                        new Vector(110, 64, 110)
                ),
                List.of( // team2Spawns
                        new Vector(200, 64, 200),
                        new Vector(210, 64, 210)
                )
        );

        // Exemple de map 2
        registerMap(
                "map2",
                new Vector(0, 64, 0), // pasteLocation
                List.of( // team1Spawns
                        new Vector(150, 64, 150),
                        new Vector(160, 64, 160)
                ),
                List.of( // team2Spawns
                        new Vector(250, 64, 250),
                        new Vector(260, 64, 260)
                )
        );
    }

    private static void registerMap(String MapName, Vector pasteLocation, List<Vector> team1Spawns, List<Vector> team2Spawns){
        MapData map = new MapData(MapName, team1Spawns, team2Spawns, pasteLocation);
        addMap(map);
    }
}
