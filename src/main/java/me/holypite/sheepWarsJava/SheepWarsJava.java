package me.holypite.sheepWarsJava;

import org.bukkit.plugin.java.JavaPlugin;

public class SheepWarsJava extends JavaPlugin {

    @Override
    public void onEnable() {
        // Logique de démarrage du plugin
        new PlayerHandler(this);
        WoolManager.init();
        getCommand("giveallwool").setExecutor(new GiveAllWoolCommand());
    }

    public SheepWarsJava getInstance() {
        return this;
    }

    @Override
    public void onDisable() {
        // Logique d'arrêt du plugin
    }
}
