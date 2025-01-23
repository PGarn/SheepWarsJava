package me.holypite.sheepWarsJava.Sheeps;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiveAllWoolCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cette commande doit être exécutée par un joueur !");
            return true;
        }

        Player player = (Player) sender;

        // Donne toutes les laines au joueur
        WoolManager.giveAllWool(player);

        player.sendMessage("Vous avez reçu toutes les laines personnalisées !");
        return true;
    }
}
