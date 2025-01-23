package me.holypite.sheepWarsJava.Tools;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import java.io.File;
import java.util.Random;

public class EmptyWorld {
    /**
     * Charge un monde existant.
     *
     * @param worldName Le nom du monde à charger.
     * @return Le monde chargé, ou null s'il n'a pas pu être chargé.
     */
    public static World loadExistingWorld(String worldName) {
        // Vérifier si le monde est déjà chargé
        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            System.out.println("Le monde " + worldName + " est déjà chargé !");
            return existingWorld;
        }

        // Vérifier si le dossier du monde existe
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            System.out.println("Le dossier du monde " + worldName + " n'existe pas !");
            return null;
        }

        // Charger le monde
        System.out.println("Chargement du monde " + worldName + "...");
        World loadedWorld = Bukkit.createWorld(new WorldCreator(worldName));

        if (loadedWorld != null) {
            System.out.println("Le monde " + worldName + " a été chargé avec succès !");
        } else {
            System.out.println("Erreur : Impossible de charger le monde " + worldName + " !");
        }

        return loadedWorld;
    }

    /**
     * Crée un monde vide avec le nom spécifié.
     *
     * @param worldName Le nom du monde à créer.
     * @return true si le monde a été créé avec succès, false sinon.
     */
    public static World createEmptyWorld(String worldName) {
        // Vérifier si le monde existe déjà
        World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            System.out.println("Le monde " + worldName + " existe déjà !");
            return existingWorld;
        }

        // Créer un nouveau monde avec un générateur vide
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new EmptyChunkGenerator());

        // Générer et retourner le monde
        World emptyWorld = creator.createWorld();
        emptyWorld.save();

        if (emptyWorld != null) {
            System.out.println("Le monde vide " + worldName + " a été créé avec succès !");
        } else {
            System.out.println("Erreur : Impossible de créer le monde " + worldName + " !");
        }

        return emptyWorld;
    }

    public static class EmptyChunkGenerator extends ChunkGenerator {
        public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
            // Aucun bloc à générer
        }

        @Override
        public boolean shouldGenerateNoise() {
            return false;
        }

        @Override
        public boolean shouldGenerateCaves() {
            return false;
        }

        @Override
        public boolean shouldGenerateDecorations() {
            return false;
        }

        @Override
        public boolean shouldGenerateMobs() {
            return false;
        }

        @Override
        public boolean shouldGenerateStructures() {
            return false;
        }
    }

    /**
     * Supprime un monde avec le nom spécifié.
     *
     * @param worldName Le nom du monde à supprimer.
     * @return true si le monde a été supprimé avec succès, false sinon.
     */
    public static boolean deleteWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            Bukkit.unloadWorld(world, false);
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            return deleteDirectory(worldFolder);
        }
        return false;
    }

    /**
     * Supprime un répertoire et son contenu de manière récursive.
     *
     * @param directory Le répertoire à supprimer.
     * @return true si le répertoire a été supprimé avec succès, false sinon.
     */
    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            return directory.delete();
        }
        return false;
    }
}
