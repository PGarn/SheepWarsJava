package me.holypite.sheepWarsJava;

import me.holypite.sheepWarsJava.Maps.MapManager;
import me.holypite.sheepWarsJava.Players.GameManager;
import me.holypite.sheepWarsJava.Players.LobbyManager;
import me.holypite.sheepWarsJava.Players.PlayerManager;
import me.holypite.sheepWarsJava.Sheeps.GiveAllWoolCommand;
import me.holypite.sheepWarsJava.Sheeps.SheepHandler;
import me.holypite.sheepWarsJava.Sheeps.WoolManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SheepWarsJava extends JavaPlugin {

    @Override
    public void onEnable() {
        // Logique de démarrage du plugin
        WoolManager.init();

        new SheepHandler(this);
        GameManager gameManager = new GameManager(this);
        new PlayerManager(this, gameManager);
        new LobbyManager(this, gameManager);

        getCommand("giveallwool").setExecutor(new GiveAllWoolCommand());

        MapManager.registerMaps();
    }

    public SheepWarsJava getInstance() {
        return this;
    }

    @Override
    public void onDisable() {
        // Logique d'arrêt du plugin
    }
}
