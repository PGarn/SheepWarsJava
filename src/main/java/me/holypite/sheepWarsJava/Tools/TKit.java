package me.holypite.sheepWarsJava.Tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TKit {
    /**
     * Extrait uniquement le texte brut d'un composant Adventure sans les styles, les couleurs ou les enfants.
     *
     * @param component Le composant Adventure.
     * @return Le texte brut contenu dans le composant.
     */
    public static String extractPlainText(Component component) {
        // Initialiser une variable pour contenir le texte brut
        StringBuilder plainText = new StringBuilder();

        // Vérifier si le composant est un TextComponent (contenant un texte brut)
        if (component instanceof net.kyori.adventure.text.TextComponent textComponent) {
            plainText.append(textComponent.content()); // Ajouter uniquement le contenu textuel
        }

        // Ajouter récursivement les textes des enfants
        for (Component child : component.children()) {
            plainText.append(extractPlainText(child)); // Appeler récursivement pour traiter les enfants
        }

        return plainText.toString();
    }

    /**
     * Crée un texte avec un dégradé de couleur entre deux couleurs données.
     *
     * @param text       Le texte à styliser.
     * @param startColor La couleur de départ du dégradé.
     * @param endColor   La couleur de fin du dégradé.
     * @return Un composant Adventure représentant le texte avec le dégradé appliqué.
     */
    public static Component createGradientText(String text, TextColor startColor, TextColor endColor) {
        int length = text.length();
        Component gradientText = Component.empty();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            int red = interpolate(startColor.red(), endColor.red(), ratio);
            int green = interpolate(startColor.green(), endColor.green(), ratio);
            int blue = interpolate(startColor.blue(), endColor.blue(), ratio);

            TextColor color = TextColor.color(red, green, blue);

            gradientText = gradientText.append(
                    Component.text(String.valueOf(text.charAt(i))).color(color)
            );
        }

        return gradientText;
    }

    /**
     * Interpole entre deux valeurs entières selon un ratio donné.
     *
     * @param start La valeur de départ.
     * @param end   La valeur de fin.
     * @param ratio Le ratio entre les deux valeurs (compris entre 0 et 1).
     * @return La valeur interpolée.
     */
    private static int interpolate(int start, int end, float ratio) {
        return (int) (start + (end - start) * ratio);
    }

    /**
     * Donne des objets au joueur. Si l'inventaire est plein, les objets restants sont déposés au sol.
     *
     * @param player Le joueur à qui donner les objets.
     * @param items  Les objets à donner.
     */
    public static void giveItems(Player player, ItemStack... items) {
        PlayerInventory inventory = player.getInventory();
        Location dropLocation = player.getLocation();

        HashMap<Integer, ItemStack> leftoverItems = inventory.addItem(items);

        for (ItemStack leftover : leftoverItems.values()) {
            if (leftover != null) {
                player.getWorld().dropItemNaturally(dropLocation, leftover);
            }
        }
    }

    /**
     * Téléporte un joueur à une position sécurisée en cherchant un espace vide à proximité.
     *
     * @param player   Le joueur à téléporter.
     * @param location La position cible.
     * @return true si la téléportation a réussi, false sinon.
     */
    public static boolean safeTeleport(Player player, Location location) {
        Location safeLocation = location.clone();
        for (int y = -5; y <= 5; y++) {
            safeLocation.setY(location.getY() + y);
            if (safeLocation.getBlock().isPassable()) {
                player.teleport(safeLocation);
                return true;
            }
        }
        return false;
    }

    /**
     * Envoie un message stylisé à un joueur.
     *
     * @param player  Le joueur à qui envoyer le message.
     * @param message Le contenu du message.
     * @param color   La couleur du texte.
     */
    public static void sendStyledMessage(Player player, String message, TextColor color) {
        player.sendMessage(Component.text(message).color(color));
    }

    /**
     * Vérifie si une position est sûre pour un joueur.
     *
     * @param location La position à vérifier.
     * @return true si la position est sûre, false sinon.
     */
    public static boolean isSafeLocation(Location location) {
        return location.getBlock().isPassable() && location.clone().add(0, 1, 0).getBlock().isPassable();
    }

    /**
     * Affiche un cercle de particules autour d'un joueur.
     *
     * @param player   Le joueur autour duquel afficher les particules.
     * @param particle Le type de particule.
     * @param radius   Le rayon du cercle.
     */
    public static void displayParticleCircle(Player player, Particle particle, double radius) {
        Location center = player.getLocation();
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            center.getWorld().spawnParticle(particle, x, center.getY(), z, 1);
        }
    }

    /**
     * Dépose des items en cercle autour d'un joueur.
     *
     * @param player Le joueur autour duquel les items seront déposés.
     * @param items  Les items à déposer.
     * @param radius Le rayon du cercle.
     */
    public static void dropItemsInCircle(Player player, ItemStack[] items, double radius) {
        Location center = player.getLocation();
        int angleStep = 360 / items.length;

        for (int i = 0; i < items.length; i++) {
            double angle = Math.toRadians(i * angleStep);
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location dropLocation = new Location(center.getWorld(), x, center.getY(), z);
            center.getWorld().dropItemNaturally(dropLocation, items[i]);
        }
    }

    /**
     * Convertit une durée en secondes en format lisible.
     *
     * @param seconds La durée en secondes.
     * @return Une chaîne formatée en "MM:SS".
     */
    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Vérifie si deux joueurs sont dans une certaine distance.
     *
     * @param player1     Le premier joueur.
     * @param player2     Le second joueur.
     * @param maxDistance La distance maximale.
     * @return true si les joueurs sont proches, false sinon.
     */
    public static boolean arePlayersClose(Player player1, Player player2, double maxDistance) {
        return player1.getLocation().distance(player2.getLocation()) <= maxDistance;
    }

    /**
     * Trouve toutes les entités dans un rayon donné.
     *
     * @param location La position à partir de laquelle chercher.
     * @param radius Le rayon de recherche.
     * @return La liste des entités dans le rayon.
     */
    public static List<Entity> getEntitiesInRadius(Location location, double radius) {
        return location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
                .toList();
    }

    /**
     * Calcule la direction entre deux locations.
     *
     * @param from La position de départ.
     * @param to La position de destination.
     * @return Un vecteur normalisé représentant la direction de "from" vers "to".
     */
    public static Vector getDirection(Location from, Location to) {
        if (from.equals(to)) {
            return new Vector(0, 0, 0); // Retourne un vecteur nul si les positions sont identiques
        }
        return to.toVector().subtract(from.toVector()).normalize();
    }


    /**
     * Trouve les joueurs dans un rayon donné.
     *
     * @param location La position à partir de laquelle chercher.
     * @param radius Le rayon de recherche.
     * @param ignoreSpectators Indique si les joueurs en mode spectateur doivent être ignorés.
     * @return La liste des joueurs dans le rayon.
     */
    public static List<Player> getPlayersInRadius(Location location, double radius, boolean ignoreSpectators) {
        return location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(player -> !ignoreSpectators || player.getGameMode() != GameMode.SPECTATOR) // Filtrer selon le mode spectateur
                .toList();
    }

    /**
     * Trouve le joueur le plus proche d'une position donnée.
     *
     * @param location La position à partir de laquelle chercher.
     * @param ignoreSpectators Indique si les joueurs en mode spectateur doivent être ignorés.
     * @return Le joueur le plus proche ou null s'il n'y a aucun joueur.
     */
    public static Player getNearestPlayer(Location location, boolean ignoreSpectators) {
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : location.getWorld().getPlayers()) {
            if (ignoreSpectators && player.getGameMode() == GameMode.SPECTATOR) {
                continue; // Ignorer les joueurs en mode spectateur si nécessaire
            }

            double distance = player.getLocation().distance(location);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestPlayer = player;
            }
        }

        return nearestPlayer;
    }

    /**
     * Envoie un message au joueur le plus proche d'une position donnée.
     *
     * @param location La position à partir de laquelle chercher.
     * @param message  Le message à envoyer.
     */
    public static void messageNearestPlayer(Location location, String message) {
        // Trouver le joueur le plus proche
        Player nearestPlayer = getNearestPlayer(location,false);

        // Vérifier si un joueur a été trouvé
        if (nearestPlayer != null) {
            // Envoyer le message au joueur
            nearestPlayer.sendMessage(message);
        }
    }

    /**
     * Vérifie si une entité est sur un bloc solide.
     *
     * @param entity L'entité a vérifié
     * @return True si l'entité est sur un bloc solide, sinon false.
     */
    public static boolean isEntityOnSolidBlock(Entity entity) {
        // Vérifie si le bloc sous l'entité est solide
        return !(Math.abs(entity.getVelocity().getY()) > 0.05 || areAllBlocksAir(getBlocksUnder(entity.getLocation())));
    }

    /**
     * Renvoie true avec un certain pourcentage de chance.
     *
     * @param chance Un pourcentage entre 0 et 1 (par exemple, 0.7 pour 70%).
     * @return true si le pourcentage aléatoire est respecté, sinon false.
     */
    public static boolean chance(double chance) {
        if (chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("La chance doit être entre 0 et 1.");
        }
        return ThreadLocalRandom.current().nextDouble() < chance;
    }

    /**
     * Retourne une liste des blocs dans un rayon donné autour d'une localisation.
     *
     * @param center La localisation centrale.
     * @param radius Le rayon de recherche.
     * @return Une liste des blocs dans le rayon.
     */
    public static List<Block> getBlocksInSquare(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    Block block = blockLoc.getBlock();
                    if (block.getType().isSolid()) { // Ajouter uniquement les blocs solides
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }

    /**
     * Retourne une liste des blocs dans une sphère de rayon donné autour d'une localisation.
     *
     * @param center La localisation centrale.
     * @param radius Le rayon de recherche.
     * @return Une liste des blocs dans le rayon.
     */
    public static List<Block> getBlocksInSphere(Location center, double radius) {
        List<Block> blocks = new ArrayList<>();
        double radiusSquared = radius * radius; // Distance au carré pour éviter les racines carrées inutiles

        for (double x = -radius; x <= radius; x++) {
            for (double y = -radius; y <= radius; y++) {
                for (double z = -radius; z <= radius; z++) {
                    Location blockLoc = center.clone().add(x, y, z);
                    // Vérifier si la distance au centre est dans le rayon
                    if (blockLoc.distanceSquared(center) <= radiusSquared) {
                        Block block = blockLoc.getBlock();
                        if (block.getType().isSolid()) { // Ajouter uniquement les blocs solides
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        return blocks;
    }

    /**
     * Retourne les 9 blocs sous une localisation, incluant le bloc directement en dessous
     * et les 8 blocs adjacents dans le plan X-Z.
     *
     * @param center La localisation centrale (par exemple, la position d'une entité).
     * @return Une liste des 9 blocs sous la localisation.
     */
    public static List<Block> getBlocksUnder(Location center) {
        List<Block> blocks = new ArrayList<>();
        Location baseLocation = center.clone().subtract(0, 1, 0); // Décalage vers le bas (sous l'entité)

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // Obtenir le bloc aux coordonnées (x, y-1, z) par rapport à la localisation centrale
                Location blockLocation = baseLocation.clone().add(x, 0, z);
                Block block = blockLocation.getBlock();
                blocks.add(block);
            }
        }
        return blocks;
    }

    /**
     * Renvoie une liste des blocs d'air libre situés au-dessus des blocs solides dans la liste donnée.
     *
     * @param blocks La liste des blocs à analyser.
     * @return Une liste des blocs d'air libre situés au-dessus des blocs solides.
     */
    public static List<Block> getOpenAirBlocksAbove(List<Block> blocks) {
        List<Block> openAirBlocks = new ArrayList<>();
        for (Block block : blocks) {
            if (block.getType().isSolid() && !block.getRelative(BlockFace.UP).getType().isSolid()) {
                // Ajouter le bloc d'air au-dessus à la liste
                openAirBlocks.add(block.getRelative(BlockFace.UP));
            }
        }
        return openAirBlocks;
    }

    /**
     * Génère une couleur de teinture aléatoire.
     *
     * @return Une valeur aléatoire de DyeColor.
     */
    public static DyeColor getRandomDyeColor() {
        DyeColor[] colors = DyeColor.values();
        return colors[ThreadLocalRandom.current().nextInt(colors.length)];
    }

    public static boolean areAllBlocksAir(List<Block> blocks) {
        for (Block block : blocks) {
            // Si un bloc n'est pas de l'air, retourne false
            if (block.getType() != Material.AIR && block.getType() != Material.VOID_AIR && block.getType() != Material.CAVE_AIR) {
                return false;
            }
        }
        // Tous les blocs sont de l'air
        return true;
    }

    /**
     * Retourne une liste des effets de potion correspondant à la catégorie donnée.
     *
     * @param category "Positif", "Négatif", ou "Neutre".
     * @return Une liste des effets de potion dans la catégorie spécifiée.
     */
    public static List<PotionEffectType> getPotionEffectsByCategory(String category) {
        List<PotionEffectType> positiveEffects = List.of(
                PotionEffectType.ABSORPTION,
                PotionEffectType.CONDUIT_POWER,
                PotionEffectType.DOLPHINS_GRACE,
                PotionEffectType.FIRE_RESISTANCE,
                PotionEffectType.HEALTH_BOOST,
                PotionEffectType.HERO_OF_THE_VILLAGE,
                PotionEffectType.INSTANT_HEALTH,
                PotionEffectType.INVISIBILITY,
                PotionEffectType.JUMP_BOOST,
                PotionEffectType.LUCK,
                PotionEffectType.NIGHT_VISION,
                PotionEffectType.REGENERATION,
                PotionEffectType.RESISTANCE,
                PotionEffectType.SATURATION,
                PotionEffectType.SLOW_FALLING,
                PotionEffectType.SPEED,
                PotionEffectType.STRENGTH,
                PotionEffectType.WATER_BREATHING
        );

        List<PotionEffectType> negativeEffects = List.of(
                PotionEffectType.BAD_OMEN,
                PotionEffectType.BLINDNESS,
                PotionEffectType.DARKNESS,
                PotionEffectType.HUNGER,
                PotionEffectType.INSTANT_DAMAGE,
                PotionEffectType.LEVITATION,
                PotionEffectType.MINING_FATIGUE,
                PotionEffectType.NAUSEA,
                PotionEffectType.POISON,
                PotionEffectType.SLOWNESS,
                PotionEffectType.UNLUCK,
                PotionEffectType.WEAKNESS,
                PotionEffectType.WITHER
        );

        List<PotionEffectType> neutralEffects = List.of(
                PotionEffectType.GLOWING,
                PotionEffectType.OOZING,
                PotionEffectType.RAID_OMEN,
                PotionEffectType.TRIAL_OMEN,
                PotionEffectType.WEAVING,
                PotionEffectType.WIND_CHARGED
        );

        return switch (category.toLowerCase()) {
            case "positif" -> positiveEffects;
            case "négatif" -> negativeEffects;
            case "neutre" -> neutralEffects;
            default -> throw new IllegalArgumentException("Catégorie invalide : " + category);
        };
    }

    /**
     * Sélectionne un effet de potion aléatoire dans la liste donnée.
     *
     * @param effects La liste des effets de potion.
     * @return Un effet de potion aléatoire.
     */
    public static PotionEffectType getRandomPotionEffect(List<PotionEffectType> effects) {
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("La liste des effets de potion est vide.");
        }
        return effects.get(ThreadLocalRandom.current().nextInt(effects.size()));
    }

    /**
     * Convertit un vecteur en une location dans un monde donné.
     *
     * @param world Le monde dans lequel la location doit être créée.
     * @param vector Le vecteur à convertir.
     * @return Une location correspondant au vecteur dans le monde spécifié.
     */
    public static Location vectorToLocation(org.bukkit.World world, Vector vector) {
        // Vérifier que le monde et le vecteur ne sont pas nulls
        if (world == null || vector == null) {
            throw new IllegalArgumentException("Le monde ou le vecteur ne peut pas être null !");
        }

        // Convertir le vecteur en Location
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Convertit une liste de vecteurs en une liste de locations dans un monde donné.
     *
     * @param world Le monde dans lequel les locations doivent être créées.
     * @param vectors La liste des vecteurs à convertir.
     * @return Une liste de locations correspondant aux vecteurs dans le monde spécifié.
     */
    public static List<Location> vectorsToLocations(org.bukkit.World world, List<Vector> vectors) {
        // Liste pour stocker les locations résultantes
        List<Location> locations = new ArrayList<>();

        // Vérifier que le monde et les vecteurs ne sont pas nulls
        if (world == null || vectors == null) {
            throw new IllegalArgumentException("Le monde ou la liste des vecteurs ne peut pas être null !");
        }

        // Parcourir chaque vecteur et le convertir en Location
        for (Vector vector : vectors) {
            if (vector != null) {
                // Créer une location à partir du vecteur dans le monde
                locations.add(vectorToLocation(world, vector));
            }
        }

        return locations;
    }

}
