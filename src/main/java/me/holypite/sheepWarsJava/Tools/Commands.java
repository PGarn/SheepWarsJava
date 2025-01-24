package me.holypite.sheepWarsJava.Tools;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("createworld")) {
            if (args.length == 1) {
                String worldName = args[0];
                if (World.createEmptyWorld(worldName) != null) {
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
                if (World.deleteWorld(worldName)) {
                    sender.sendMessage("Le monde " + worldName + " a été supprimé avec succès.");
                } else {
                    sender.sendMessage("Échec de la suppression du monde " + worldName + ".");
                }
            } else {
                sender.sendMessage("Utilisation: /deleteworld <nom_du_monde>");
            }
            return true;

        } else if (command.getName().equalsIgnoreCase("savestructure")) {
            if (args.length == 7) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Seul un joueur peut utiliser cette commande !");
                    return true;
                }

                String structureName = args[0];
                org.bukkit.World world = player.getWorld();

                try {
                    // Obtenir les coordonnées des deux coins
                    int x1 = Integer.parseInt(args[1]);
                    int y1 = Integer.parseInt(args[2]);
                    int z1 = Integer.parseInt(args[3]);
                    int x2 = Integer.parseInt(args[4]);
                    int y2 = Integer.parseInt(args[5]);
                    int z2 = Integer.parseInt(args[6]);

                    // Créer les deux coins
                    Location corner1 = new Location(world, x1, y1, z1);
                    Location corner2 = new Location(world, x2, y2, z2);

                    // Enregistrer la structure
                    if (World.saveStructure(world, corner1, corner2, structureName)) {
                        player.sendMessage("Structure enregistrée avec succès sous le nom : " + structureName);
                    } else {
                        player.sendMessage("Erreur : Impossible d'enregistrer la structure.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Erreur : Les coordonnées doivent être des nombres.");
                }
            } else {
                sender.sendMessage("Utilisation: /savestructure <nom> <x1> <y1> <z1> <x2> <y2> <z2>");
            }
            return true;

        } else if (command.getName().equalsIgnoreCase("loadstructure")) {
            if (args.length == 4) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Seul un joueur peut utiliser cette commande !");
                    return true;
                }

                String structureName = args[0];
                org.bukkit.World world = player.getWorld();

                try {
                    // Obtenir les coordonnées de placement
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int z = Integer.parseInt(args[3]);

                    // Créer la position de placement
                    Location location = new Location(world, x, y, z);

                    // Charger et placer la structure
                    if (World.loadStructure(world, location, structureName)) {
                        player.sendMessage("Structure " + structureName + " chargée avec succès à " + location);
                    } else {
                        player.sendMessage("Erreur : Impossible de charger la structure.");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("Erreur : Les coordonnées doivent être des nombres.");
                }
            } else {
                sender.sendMessage("Utilisation: /loadstructure <nom> <x> <y> <z>");
            }
            return true;
        }

        return false;
    }
}
