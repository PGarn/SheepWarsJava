package me.holypite.sheepWarsJava.Maps;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

public record MapData(String mapName, List<Vector> team1Spawns, List<Vector> team2Spawns, Vector pasteLocation) {
}
