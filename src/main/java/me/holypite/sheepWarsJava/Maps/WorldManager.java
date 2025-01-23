package me.holypite.sheepWarsJava.Maps;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WorldManager {

    public static World createNewGameWorld() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String worldName = "sheepwars_" + timeStamp;
        WorldCreator worldCreator = new WorldCreator(worldName);
        return Bukkit.createWorld(worldCreator);
    }

    public static void deleteWorld(World world) {
        Bukkit.unloadWorld(world, false);
        // Supprimer le dossier du monde ici si nécessaire
    }
}
