package me.holypite.sheepWarsJava.Tools;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("createworld")) {
            if (args.length == 1) {
                String worldName = args[0];
                if (EmptyWorld.createEmptyWorld(worldName) != null) {
                    sender.sendMessage("Le monde " + worldName + " a été créé avec succès.");
                } else {
                    sender.sendMessage("Échec de la création du monde " + worldName + ".");
                }
            } else {
                sender.sendMessage("Utilisation: /createworld <nom_du_monde>");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("deleteworld")) {
            if (args.length == 1) {
                String worldName = args[0];
                if (EmptyWorld.deleteWorld(worldName)) {
                    sender.sendMessage("Le monde " + worldName + " a été supprimé avec succès.");
                } else {
                    sender.sendMessage("Échec de la suppression du monde " + worldName + ".");
                }
            } else {
                sender.sendMessage("Utilisation: /deleteworld <nom_du_monde>");
            }
            return true;
        }
        return false;
    }
}

