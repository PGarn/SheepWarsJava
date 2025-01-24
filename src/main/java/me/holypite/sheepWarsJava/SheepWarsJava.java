package me.holypite.sheepWarsJava;

import me.holypite.sheepWarsJava.Maps.MapManager;
import me.holypite.sheepWarsJava.Players.GameManager;
import me.holypite.sheepWarsJava.Players.LobbyManager;
import me.holypite.sheepWarsJava.Players.PlayerManager;
import me.holypite.sheepWarsJava.Sheeps.GiveAllWoolCommand;
import me.holypite.sheepWarsJava.Sheeps.SheepHandler;
import me.holypite.sheepWarsJava.Sheeps.WoolManager;
import me.holypite.sheepWarsJava.Tools.Commands;
import me.holypite.sheepWarsJava.Tools.World;
import org.bukkit.plugin.java.JavaPlugin;

public class SheepWarsJava extends JavaPlugin {
    public GameManager gameManager;

    @Override
    public void onEnable() {
        // Logique de démarrage du plugin
        loadCommands();
        loadWorlds();
        WoolManager.init();

        new SheepHandler(this);
        gameManager = new GameManager(this);
        new PlayerManager(this, gameManager);
        new LobbyManager(this, gameManager);


        MapManager.registerMaps();
    }

    public SheepWarsJava getInstance() {
        return this;
    }

    @Override
    public void onDisable() {
        // Logique d'arrêt du plugin
    }

    private void loadCommands(){
        getCommand("createworld").setExecutor(new Commands());
        getCommand("deleteworld").setExecutor(new Commands());
        getCommand("savestructure").setExecutor(new Commands());
        getCommand("loadstructure").setExecutor(new Commands());
        getCommand("giveallwool").setExecutor(new GiveAllWoolCommand());
        getCommand("createworld").setExecutor(new Commands());
        getCommand("deleteworld").setExecutor(new Commands());
    }

    private void loadWorlds(){
        // Charger le monde existant
        org.bukkit.World lobbyWorld = World.loadExistingWorld("lobby_sheepwars-1");

        if (lobbyWorld != null) {
            System.out.println("Le monde de lobby est prêt.");
        } else {
            System.out.println("Erreur : le monde lobby_sheepwars-1 n'a pas pu être chargé !");
        }

    }
}
