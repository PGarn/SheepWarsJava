package me.holypite.sheepWarsJava.Tools;

import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import org.bukkit.Location;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.random.RandomGenerator;

public class World {
    /**
     * Charge un monde existant.
     *
     * @param worldName Le nom du monde à charger.
     * @return Le monde chargé, ou null s'il n'a pas pu être chargé.
     */
    public static org.bukkit.World loadExistingWorld(String worldName) {
        // Vérifier si le monde est déjà chargé
        org.bukkit.World existingWorld = Bukkit.getWorld(worldName);
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
        org.bukkit.World loadedWorld = Bukkit.createWorld(new WorldCreator(worldName));

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
    public static org.bukkit.World createEmptyWorld(String worldName) {
        // Vérifier si le monde existe déjà
        org.bukkit.World existingWorld = Bukkit.getWorld(worldName);
        if (existingWorld != null) {
            System.out.println("Le monde " + worldName + " existe déjà !");
            return existingWorld;
        }

        // Créer un nouveau monde avec un générateur vide
        WorldCreator creator = new WorldCreator(worldName);
        creator.generator(new EmptyChunkGenerator());

        // Générer et retourner le monde
        org.bukkit.World emptyWorld = creator.createWorld();
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
        org.bukkit.World world = Bukkit.getWorld(worldName);
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

    /**
     * Enregistre une structure basée sur une région spécifiée dans un fichier NBT.
     *
     * @param world Le monde dans lequel la structure est située.
     * @param corner1 Un coin de la région à enregistrer.
     * @param corner2 L'autre coin opposé de la région.
     * @param structureName Le nom du fichier sous lequel enregistrer la structure.
     * @return true si la structure a été enregistrée avec succès, false sinon.
     */
    public static boolean saveStructure(org.bukkit.World world, Location corner1, Location corner2, String structureName) {
        if (world == null || corner1 == null || corner2 == null || structureName == null || structureName.isEmpty()) {
            System.out.println("Erreur : paramètres invalides pour l'enregistrement de la structure.");
            return false;
        }

        // Obtenir le StructureManager
        StructureManager structureManager = Bukkit.getStructureManager();

        // Créer une région basée sur les deux coins
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        Location minCorner = new Location(world, minX, minY, minZ);
        Location maxCorner = new Location(world, maxX, maxY, maxZ);

        // Créer une nouvelle structure
        Structure structure = structureManager.createStructure();
        structure.fill(minCorner, maxCorner, true); // Inclure les entités avec `true`

        // Définir le fichier de sortie
        File structureFile = new File(world.getWorldFolder(), "structures/" + structureName + ".nbt");

        // Sauvegarder la structure
        try {
            structureManager.saveStructure(structureFile, structure);
            System.out.println("Structure enregistrée avec succès sous : " + structureFile.getPath());
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'enregistrement de la structure : " + e.getMessage());
            return false;
        }
    }

    /**
     * Charge et place une structure à partir d'un fichier NBT à un emplacement donné.
     *
     * @param world Le monde dans lequel placer la structure.
     * @param location La position où la structure doit être placée.
     * @param structureName Le nom du fichier de la structure (sans extension .nbt).
     * @return true si la structure a été chargée et placée avec succès, false sinon.
     */
    public static boolean loadStructure(org.bukkit.World world, Location location, String structureName) {
        if (world == null || location == null || structureName == null || structureName.isEmpty()) {
            System.out.println("Erreur : paramètres invalides pour le chargement de la structure.");
            return false;
        }

        // Obtenir le StructureManager
        StructureManager structureManager = Bukkit.getStructureManager();
        if (structureManager == null) {
            System.err.println("Erreur : Impossible de récupérer le StructureManager !");
            return false;
        }

        // Charger la structure
        File structureFile = new File(world.getWorldFolder(), "structures/" + structureName + ".nbt");
        try {
            Structure structure = structureManager.loadStructure(structureFile);
            if (structure != null) {
                // Placer la structure
                structure.place(
                        location,              // Position de placement
                        true,                  // Remplir l'air
                        StructureRotation.NONE, // Pas de rotation
                        Mirror.NONE,           // Pas de miroir
                        0,                     // Décalage d'intégrité
                        1.0F,                  // Intégrité complète
                        Random.from(RandomGenerator.getDefault()) // Graine aléatoire
                );
                System.out.println("Structure " + structureName + " chargée avec succès à " + location);
                return true;
            } else {
                System.out.println("Impossible de charger la structure : " + structureName);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la structure : " + e.getMessage());
        }

        return false;
    }

}
