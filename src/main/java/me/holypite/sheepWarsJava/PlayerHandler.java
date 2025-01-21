package me.holypite.sheepWarsJava;

import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerHandler implements Listener {

    private final SheepWarsJava plugin;

    public PlayerHandler(SheepWarsJava plugin) {
        // Enregistrer les événements
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onItemInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Vérifier si l'action est un clic droit
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Vérifier si l'item est de la laine blanche et qu'il a un CustomModelData
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.WHITE_WOOL && item.getItemMeta().hasCustomModelData()) {
                event.setCancelled(true); // Annuler l'événement pour éviter un autre comportement par défaut

                Location spawnLocation = player.getLocation(); // Obtenir la position du joueur pour le spawn

                // Récupérer le nom de l'item
                Component woolName = item.getItemMeta().displayName();

                // Faire apparaître un mouton
                Sheep sheep = (Sheep) player.getWorld().spawnEntity(spawnLocation, EntityType.SHEEP);

                // Définir le nom du mouton et le rendre visible
                sheep.customName(woolName);
                sheep.setCustomNameVisible(true);

                // Définir la couleur du mouton
                sheep.setColor(DyeColor.WHITE);

                // Calculer la direction où le joueur regarde
                Vector direction = player.getLocation().getDirection();
                double speed = 2; // Ajustez la vitesse selon vos besoins
                sheep.setVelocity(direction.multiply(speed));

                // Modifier les attributs de gravité et des dégâts de chute
                sheep.getAttribute(Attribute.GRAVITY).setBaseValue(0.7*0.08); // Réduit la gravité à 50%
                sheep.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER).setBaseValue(0.0); // Annule les dégâts de chute

                // Retirer une unité de laine de l'inventaire
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Vérifier si l'entité est un mouton
        if (event.getEntity() instanceof Sheep sheep) {
            // Vérifier si le mouton a un nom personnalisé
            if (sheep.customName() != null) {
                // Supprimer les drops de l'entité
                event.getDrops().clear();
            }
        }
    }

    @EventHandler
    public void onSheepTouchGround(EntityMoveEvent event) {
        // Vérifier si l'entité est un mouton
        if (event.getEntity() instanceof Sheep sheep) {
            // Vérifier si le mouton a un nom personnalisé et s'il n'a pas encore activé son pouvoir
            if (sheep.customName() != null && !sheep.hasMetadata("Activated")) {
                // Vérifier si le mouton est sur un bloc solide
                List<String> validSheepNames = List.of("Mouton d'abordage", "Mouton Glouton", "Mouton Taupe");
                if (UtilityFoncKit.isEntityOnSolidBlock(sheep) || validSheepNames.contains(UtilityFoncKit.extractPlainText(sheep.customName()))) {
                    // Marquer le mouton comme ayant activé son pouvoir
                    sheep.setMetadata("Activated", new FixedMetadataValue(plugin, true));
                    // Activer le pouvoir du mouton
                    activateSheepPower(sheep);
                }
            }
        }
    }

    private void activateSheepPower(Sheep sheep) {
        plugin.getServer().getScheduler().runTaskLater(plugin, task -> applySheepPower(sheep),20);
    }

    private void applySheepPower(Sheep sheep) {
        // Récupérer le nom du mouton
        String sheepName = UtilityFoncKit.extractPlainText(sheep.customName());
        switch (sheepName) {
            case "Mouton Explosif" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3}; // Compteur mutable pour le compte à rebours

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()){
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        // Afficher le compte à rebours dans le nom
                        // sheep.customName(Component.text(String.valueOf(countdown[0])));
                        countdown[0]--; // Réduire le compteur
                    } else {
                        // Explosion et suppression du mouton
                        sheep.getWorld().createExplosion(sheep.getLocation(), 3.0F, false, true);
                        sheep.remove();
                        task.cancel(); // Arrêter la tâche
                    }
                }, 0L, 20L); // Répéter chaque seconde (20 ticks)
            }

            case "Mouton Trou Noir" -> {
                // Rayon d'attraction et durée (5 secondes)
                int radius = 8;
                int[] durationTicks = {5 * 20}; // Compteur mutable pour la durée (100 ticks)

                // Tâche pour attirer les entités proches
                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        // Si le mouton est mort, arrêter la tâche
                        sheep.remove();
                        task.cancel();
                    } else if (durationTicks[0] > 0) {
                        // Appliquer un effet de lévitation au mouton
                        sheep.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 0));

                        // Ajouter une vélocité aléatoire pour rendre le mouton imprévisible
                        double randomX = ThreadLocalRandom.current().nextDouble(-0.2, 0.2); // Valeur aléatoire entre -0.2 et 0.2
                        double randomZ = ThreadLocalRandom.current().nextDouble(-0.2, 0.2);
                        Vector randomVelocity = new Vector(randomX, 0, randomZ); // Pas d'effet sur Y pour éviter des mouvements incohérents
                        sheep.setVelocity(sheep.getVelocity().add(randomVelocity));

                        // Attirer les entités proches
                        sheep.getWorld().getNearbyEntities(sheep.getLocation(), radius, radius, radius).forEach(entity -> {
                            if (!entity.equals(sheep)) { // Ignorer le mouton lui-même
                                Vector direction = sheep.getLocation().toVector().subtract(entity.getLocation().toVector());
                                if (direction.length() > 0) {
                                    direction = direction.normalize().multiply(0.3); // Ajuster la force d'attraction
                                    entity.setVelocity(direction);
                                }
                            }
                        });

                        // Effet visuel pendant l'attraction
                        sheep.getWorld().spawnParticle(Particle.PORTAL, sheep.getLocation(), 10);

                        // Réduire la durée restante
                        durationTicks[0] -= 2; // Tâche exécutée toutes les 2 ticks
                    } else {
                        // Fin de l'effet après 5 secondes
                        sheep.getWorld().spawnParticle(Particle.EXPLOSION, sheep.getLocation(), 1);
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 2L); // Répéter toutes les 2 ticks
            }

            case "Mouton Glacial" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        // Transformer des blocs proches en pierre infestée et spawn des créatures
                        int radius = 5;
                        double chance = 0.7; // 70% de chance
                        List<Block> blocks = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (UtilityFoncKit.chance(chance) && block.getType().isSolid()) {
                                block.setType(UtilityFoncKit.chance(0.3) ? Material.POWDER_SNOW : Material.SNOW_BLOCK);
                            }
                        }

                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Gluant" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        // Transformer des blocs proches en pierre infestée et spawn des créatures
                        int radius = 5;
                        double chance = 0.7; // 70% de chance
                        List<Block> blocks = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (UtilityFoncKit.chance(chance) && block.getType().isSolid()) {
                                block.setType(Material.SLIME_BLOCK);
                            }
                        }

                        // Faire spawn 3 slimes
                        for (int i = 0; i < 3; i++) {
                            Slime slime = (Slime) sheep.getWorld().spawnEntity(sheep.getLocation(), EntityType.SLIME);
                            slime.setSize(UtilityFoncKit.chance(0.7) ? 1 : 2);
                        }

                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Mielleux" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        // Transformer des blocs proches en pierre infestée et spawn des créatures
                        int radius = 5;
                        double chance = 0.7; // 70% de chance
                        List<Block> blocks = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (UtilityFoncKit.chance(chance) && block.getType().isSolid()) {
                                block.setType(Material.HONEY_BLOCK);
                            }
                        }

                        // Faire spawn 2 abeilles
                        for (int i = 0; i < 2; i++) {
                            sheep.getWorld().spawnEntity(sheep.getLocation(), EntityType.BEE);
                        }

                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Incendiaire" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3};
                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        // Explosion légère avec mise à feu
                        int radius = 5; // Rayon d'effet
                        Location center = sheep.getLocation();

                        // Enflammer les entités proches
                        sheep.getWorld().getNearbyEntities(center, radius, radius, radius).forEach(entity -> {
                            if (entity instanceof LivingEntity) {
                                entity.setFireTicks(100); // Enflammer pendant 5 secondes
                            }
                        });

                        // Enflammer les blocs proches
                        double chance = 0.7; // 70% de chance
                        List<Block> blocks = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (UtilityFoncKit.chance(chance) && block.getType().isSolid()) {
                                Block above = block.getRelative(BlockFace.UP);
                                if (above.getType() == Material.AIR) {
                                    above.setType(Material.FIRE);
                                }
                            }
                        }

                        // Explosion visuelle
                        sheep.getWorld().createExplosion(center, 2.0F, false, true);
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Enfouisseur" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3};
                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        // Transformer les blocs proches en TNT avec un pourcentage de chance
                        int radius = 5;
                        double chance = 0.05; // 5% de chance
                        List<Block> blocks = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (UtilityFoncKit.chance(chance) && block.getType().isSolid()) {
                                block.setType(Material.TNT);
                            }
                        }

                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Parasite" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3};
                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        // Transformer des blocs proches en pierre infestée et spawn des créatures
                        int radius = 5;
                        double chance = 0.2; // 20% de chance
                        List<Block> blocks = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (UtilityFoncKit.chance(chance) && block.getType().isSolid()) {
                                block.setType(Material.INFESTED_STONE);
                            }
                        }

                        // Faire spawn des créatures
                        for (int i = 0; i < 2; i++) {
                            sheep.getWorld().spawnEntity(sheep.getLocation(), EntityType.ENDERMITE);
                            sheep.getWorld().spawnEntity(sheep.getLocation(), EntityType.SILVERFISH);
                        }

                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Clone" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3};
                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        // Rayon de duplication
                        int radius = 5;

                        // Dupliquer les entités proches
                        sheep.getWorld().getNearbyEntities(sheep.getLocation(), radius, radius, radius).forEach(entity -> {
                            // Vérifier que l'entité est un mouton, mais pas un autre Mouton Clone
                            if (entity instanceof Sheep nearbySheep) {
                                String nearbySheepName = UtilityFoncKit.extractPlainText(nearbySheep.customName());
                                if ("Mouton Clone".equals(nearbySheepName)) {
                                    return; // Ne pas dupliquer un autre Mouton Clone
                                }
                            }

                            // Dupliquer uniquement les moutons ou les monstres
                            if (entity instanceof Animals || entity instanceof Enemy) {
                                Entity duplicate = entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());

                                // Si c'est un mouton, configurer la métadonnée
                                if (duplicate instanceof Sheep newClonedSheep) {
                                    newClonedSheep.removeMetadata("Activated", plugin); // Supprimer toute métadonnée liée aux pouvoirs
                                    newClonedSheep.customName(entity.customName());
                                    newClonedSheep.setCustomNameVisible(true);
                                    newClonedSheep.setColor(DyeColor.WHITE); // Couleur par défaut
                                }
                            }
                        });


                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Party!!!" -> {
                // Début du compte à rebours (3 secondes)
                int[] countdown = {3};
                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        // Faire spawn entre 3 et 7 moutons aléatoires
                        int numSheep = ThreadLocalRandom.current().nextInt(3, 8); // Nombre aléatoire entre 3 et 7

                        for (int i = 0; i < numSheep; i++) {
                            // Faire apparaître un mouton aléatoire
                            Sheep randomSheep = (Sheep) sheep.getWorld().spawnEntity(sheep.getLocation(), EntityType.SHEEP);
                            randomSheep.customName(getRandomSheepComponent());
                            randomSheep.setCustomNameVisible(true);
                            randomSheep.setColor(DyeColor.WHITE);
                        }

                        // Transformer les blocs de laine proches en laines de couleur aléatoire
                        int radius = 3;
                        List<Block> blocks = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            DyeColor randomColor = DyeColor.values()[ThreadLocalRandom.current().nextInt(DyeColor.values().length)];
                            block.setType(Material.valueOf(randomColor.name() + "_WOOL"));
                        }

                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Geyser" -> {
                int radius = 8;
                int[] countdown = {3};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        List<Player> playersInRange = UtilityFoncKit.getPlayersInRadius(sheep.getLocation(), radius);
                        for (Player player : playersInRange) {
                            // Projette le joueur violemment dans les airs
                            Vector upward = new Vector(0, 2.5, 0);
                            player.setVelocity(upward);
                            player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 50);
                            // Planifier la projection vers le sol
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                Vector downward = new Vector(0, -2.5, 0);
                                player.setVelocity(player.getVelocity().add(downward));
                            }, 20L); // 1 seconde après
                        }

                        // Supprimer le mouton après activation
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Tempétueux" -> {
                int radius = 10;
                int[] countdown = {3};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else {
                        List<Player> playersInRange = UtilityFoncKit.getPlayersInRadius(sheep.getLocation(), radius);
                        for (Player player : playersInRange) {
                            // Fait tomber un éclair
                            sheep.getWorld().strikeLightningEffect(player.getLocation());

                            // Projette le joueur dans la direction opposée
                            Vector pushDirection = player.getLocation().toVector()
                                    .subtract(sheep.getLocation().toVector()).normalize().multiply(2);
                            player.setVelocity(pushDirection);
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.CLOUD, sheep.getLocation(), 30);

                        // Supprimer le mouton après activation
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Tremblement de Terre" -> {
                int radius = 5;
                int[] durationTicks = {3 * 20}; // 3 secondes
                int[] countdown = {3};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--;
                    } else if (durationTicks[0] > 0) {
                        List<Player> playersInRange = UtilityFoncKit.getPlayersInRadius(sheep.getLocation(), radius);
                        for (Player player : playersInRange) {
                            // Projette le joueur un peu en l'air dans une direction aléatoire
                            Vector randomDirection = new Vector(
                                    ThreadLocalRandom.current().nextDouble(-0.5, 0.5),
                                    0.8,
                                    ThreadLocalRandom.current().nextDouble(-0.5, 0.5)
                            );
                            player.setVelocity(randomDirection);
                        }

                        // Détruit des blocs dans le rayon
                        List<Block> blocks = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (UtilityFoncKit.chance(0.2)) { // 20% de chance
                                block.setType(Material.AIR);
                            }
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.BLOCK, sheep.getLocation(), 100,
                                Material.DIRT.createBlockData());

                        // Réduire la durée
                        durationTicks[0] -= 20; // 1 seconde
                    } else {
                        // Fin de l'effet
                        sheep.getWorld().spawnParticle(Particle.EXPLOSION, sheep.getLocation(), 1);
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L);
            }

            case "Mouton Taupe" -> {
                int duration = 5 * 20; // Durée de vie du mouton (5 secondes)
                int radius = 2; // Rayon pour détruire les blocs
                int[] ticksRemaining = {duration};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (ticksRemaining[0] > 0) {
                        // Détruire les blocs autour du mouton
                        List<Block> blocksInRadius = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocksInRadius) {
                            if (block.getType().isSolid()) {
                                block.setType(Material.AIR); // Détruire le bloc
                                // Effet visuel
                                sheep.getWorld().spawnParticle(Particle.BLOCK, block.getLocation(), 10, block.getBlockData());
                            }
                        }

                        ticksRemaining[0] -= 2; // Réduire la durée restante
                    } else {
                        // Fin de l'effet
                        sheep.getWorld().spawnParticle(Particle.EXPLOSION, sheep.getLocation(), 1);
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 2L); // Répéter toutes les 2 ticks
            }

            case "Mouton Glouton" -> {
                int duration = 7 * 20; // Durée de vie du mouton (7 secondes)
                int radius = 3; // Rayon pour détruire les blocs
                int[] ticksRemaining = {duration};

                // Supprimer la gravité et définir une vitesse constante
                sheep.setGravity(false);
                Vector constantVelocity = sheep.getLocation().getDirection().normalize().multiply(0.5);
                sheep.setVelocity(constantVelocity);

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (ticksRemaining[0] > 0) {
                        // Détruire les blocs autour du mouton
                        List<Block> blocksInRadius = UtilityFoncKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocksInRadius) {
                            if (block.getType().isSolid()) {
                                block.setType(Material.AIR); // Détruire le bloc
                                // Effet visuel
                                sheep.getWorld().spawnParticle(Particle.BLOCK, block.getLocation(), 10, block.getBlockData());
                            }
                        }

                        ticksRemaining[0] -= 2; // Réduire la durée restante
                    } else {
                        // Fin de l'effet
                        sheep.getWorld().spawnParticle(Particle.EXPLOSION, sheep.getLocation(), 1);
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 2L); // Répéter toutes les 2 ticks
            }

            default -> {
                sheep.remove();
            }
        }
    }

    /**
     * Renvoie un composant représentant le nom d'un mouton aléatoire avec son gradient de couleur.
     *
     * @return Un composant contenant le nom d'un mouton aléatoire.
     */
    public static Component getRandomSheepComponent() {
        // Tableau des noms de moutons avec leurs couleurs associées
        Object[][] sheepData = {
                {"Mouton Explosif", "#FF0000", "#8B0000"},
                {"Mouton Trou Noir", "#000000", "#4B0082"},
                {"Mouton Glacial", "#87CEEB", "#00FFFF"},
                {"Mouton Gluant", "#32CD32", "#7FFF00"},
                {"Mouton Mielleux", "#FFD700", "#FFA500"},
                {"Mouton Incendiaire", "#FF4500", "#FF6347"},
                {"Mouton Enfouisseur", "#8B4513", "#A0522D"},
                {"Mouton Parasite", "#4B0082", "#8B008B"}
        };

        // Choisir un mouton aléatoire
        int randomIndex = ThreadLocalRandom.current().nextInt(sheepData.length);
        String name = (String) sheepData[randomIndex][0];
        String startColor = (String) sheepData[randomIndex][1];
        String endColor = (String) sheepData[randomIndex][2];

        // Créer et renvoyer le composant avec le gradient
        TextColor start = TextColor.fromHexString(startColor);
        TextColor end = TextColor.fromHexString(endColor);
        return UtilityFoncKit.createGradientText(name, start, end);
    }

}
