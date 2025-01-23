package me.holypite.sheepWarsJava.Maps;

import org.bukkit.Location;

import java.util.List;

public record MapData(String mapName, List<Location> team1Spawns, List<Location> team2Spawns, Location pasteLocation) {
}
