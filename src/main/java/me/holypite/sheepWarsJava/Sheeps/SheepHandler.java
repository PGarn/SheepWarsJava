package me.holypite.sheepWarsJava.Sheeps;

import io.papermc.paper.event.entity.EntityMoveEvent;
import me.holypite.sheepWarsJava.SheepWarsJava;
import me.holypite.sheepWarsJava.Tools.TKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.inventory.meta.PotionMeta;
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
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class SheepHandler implements Listener {

    private final SheepWarsJava plugin;

    public SheepHandler(SheepWarsJava plugin) {
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
                Objects.requireNonNull(sheep.getAttribute(Attribute.GRAVITY)).setBaseValue(0.5*0.08); // Réduit la gravité à 50%
                Objects.requireNonNull(sheep.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER)).setBaseValue(0.0); // Annule les dégâts de chute

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
    public void onSheepMovement(EntityMoveEvent event) {
        // Vérifier si l'entité est un mouton
        if (event.getEntity() instanceof Sheep sheep) {
            // Vérifier si le mouton a un nom personnalisé et s'il n'a pas encore activé son pouvoir
            if (sheep.customName() != null && !sheep.hasMetadata("Activated")) {
                // Vérifier si le mouton est sur un bloc solide
                List<String> validSheepNames = List.of("Mouton Abordage", "Mouton Glouton", "Mouton Taupe","Mouton Mystère");
                if (TKit.isEntityOnSolidBlock(sheep)) {
                    // Marquer le mouton comme ayant activé son pouvoir
                    sheep.setMetadata("Activated", new FixedMetadataValue(plugin, true));
                    // Activer le pouvoir du mouton après 1 seconde
                    plugin.getServer().getScheduler().runTaskLater(plugin, task -> {applySheepPower(sheep);},20);
                } else if (validSheepNames.contains(TKit.extractPlainText(sheep.customName()))) {
                    // Marquer le mouton comme ayant activé son pouvoir
                    sheep.setMetadata("Activated", new FixedMetadataValue(plugin, true));
                    // Activer le pouvoir du mouton après 1 seconde
                    applySheepPower(sheep);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        Entity dismounted = event.getDismounted();
        Entity dismounting = event.getEntity();

        // Vérifier si l'entité démontée est un mouton et qu'il s'agit du mouton abordage
        if (dismounted instanceof Sheep sheep && dismounting instanceof Player) {
            // Vérifier si le mouton est un Mouton Abordage
            if (TKit.extractPlainText(sheep.customName()).equals("Mouton Abordage")) {
                // Supprimer le mouton
                sheep.remove();

                // Logique supplémentaire si nécessaire (par ex. effets visuels)
                sheep.getWorld().spawnParticle(Particle.EXPLOSION, sheep.getLocation(), 10);
            }
        }
    }

    @EventHandler
    public void onPlayerKillPlayer(EntityDeathEvent event) {
        // Vérifier si l'entité morte est un joueur
        if (event.getEntity() instanceof Sheep victim) {
            // Vérifier si le tueur est un joueur
            Entity killer = victim.getKiller();
            if (killer instanceof Player killerPlayer) {
                // Obtenir l'ItemStack correspondant
                ItemStack woolItem = WoolManager.getWoolByName(victim);

                if (woolItem != null) {
                    // Donner la laine au joueur
                    TKit.giveItems(killerPlayer,woolItem);

                    // Message de confirmation
                    killerPlayer.sendMessage("Vous avez obtenu une " + TKit.extractPlainText(woolItem.getItemMeta().customName()) + " !");
                }
            }
        }
    }

    private void applySheepPower(Sheep sheep) {
        // Récupérer le nom du mouton
        String sheepName = TKit.extractPlainText(sheep.customName());
        switch (sheepName) {

            case "Mouton Abordage" -> {
                Player player = TKit.getNearestPlayer(sheep.getLocation(),true); // Le joueur doit chevaucher le mouton
                sheep.addPassenger(player); // Forcer le joueur à monter sur le mouton

                int duration = 10 * 20; // Durée de vie
                int[] countdown = {duration};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        // Maintenir la trajectoire du mouton
                        countdown[0] -= 2; // Réduire le temps restant
                    } else {
                        // Explosion finale
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 2L); // Répéter toutes les 2 ticks
            }

            case "Mouton Mystère" -> {
                // Renommer le mouton avec un nom aléatoire
                sheep.customName(getRandomSheepComponent());
                sheep.setCustomNameVisible(true);
                sheep.removeMetadata("Activated", plugin); // Supprimer toute métadonnée liée aux pouvoirs

                // Ajouter un comportement ou laisser neutre pour qu'il adopte le comportement du mouton mystère
                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        sheep.getWorld().spawnParticle(Particle.ENCHANT, sheep.getLocation(), 50);
                    }
                }, 20L); // Ajout d'un délai visuel d'une seconde
            }

            case "Mouton Araignée" -> {
                int radius = 4; // Rayon pour les blocs et les joueurs
                double chance = 0.2; // Rayon pour les blocs et les joueurs

                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else {
                        // Transformer les blocs autour en toiles d'araignée
                        List<Block> blocksInRadius = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocksInRadius) {
                            if (TKit.chance(chance)) {
                                block.setType(Material.COBWEB); // Transformer en toile d'araignée
                            }
                        }

                        // Appliquer un effet de poison aux joueurs dans le rayon
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius, true);
                        for (Player player : playersInRadius) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 4*20, 0)); // Poison pendant 4 secondes
                        }
                        sheep.remove();
                        task.cancel();
                    }
                }, 3*20L);
            }

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
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (TKit.chance(chance) && block.getType().isSolid()) {
                                block.setType(TKit.chance(0.3) ? Material.POWDER_SNOW : Material.SNOW_BLOCK);
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
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (TKit.chance(chance) && block.getType().isSolid()) {
                                block.setType(Material.SLIME_BLOCK);
                            }
                        }

                        // Faire spawn 3 slimes
                        for (int i = 0; i < 3; i++) {
                            Slime slime = (Slime) sheep.getWorld().spawnEntity(sheep.getLocation(), EntityType.SLIME);
                            slime.setSize(TKit.chance(0.7) ? 1 : 2);
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
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (TKit.chance(chance) && block.getType().isSolid()) {
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
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (block.getType().isSolid() && block.getRelative(BlockFace.UP).getType() == Material.AIR) {
                                if (TKit.chance(chance) ) {
                                    block.getRelative(BlockFace.UP).setType(Material.FIRE);
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
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (TKit.chance(chance) && block.getType().isSolid()) {
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
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (TKit.chance(chance) && block.getType().isSolid()) {
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
                                String nearbySheepName = TKit.extractPlainText(nearbySheep.customName());
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
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            block.setType(Material.valueOf(TKit.getRandomDyeColor().name() + "_WOOL"));
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
                        List<Player> playersInRange = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
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
                        List<Player> playersInRange = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
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
                        List<Player> playersInRange = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
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
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (TKit.chance(0.2)) { // 20% de chance
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
                int duration = 4 * 20; // Durée de vie du mouton (5 secondes)
                double radius = 2.5; // Rayon pour détruire les blocs
                int[] ticksRemaining = {duration};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (ticksRemaining[0] > 0) {
                        // Détruire les blocs autour du mouton
                        List<Block> blocksInRadius = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocksInRadius) {
                            block.setType(Material.AIR); // Détruire le bloc
                            // Effet visuel
                            sheep.getWorld().spawnParticle(Particle.BLOCK, block.getLocation(), 10, block.getBlockData());
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
                int duration = 8 * 20; // Durée de vie du mouton (7 secondes)
                double radius = 2.5; // Rayon pour détruire les blocs
                int[] ticksRemaining = {duration};

                // Supprimer la gravité et définir une vitesse constante
                sheep.setGravity(false);
                Vector constantVelocity = TKit.getNearestPlayer(sheep.getLocation(),false).getLocation().getDirection().normalize().multiply(0.2);
                sheep.setVelocity(constantVelocity);

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (ticksRemaining[0] > 0) {
                        sheep.setVelocity(constantVelocity);
                        // Détruire les blocs autour du mouton
                        List<Block> blocksInRadius = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocksInRadius) {
                            block.setType(Material.AIR); // Détruire le bloc
                            // Effet visuel
                            sheep.getWorld().spawnParticle(Particle.BLOCK, block.getLocation(), 10, block.getBlockData());
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

            case "Mouton Greta" -> {
                int[] countdown = {3}; // Compteur mutable pour le compte à rebours

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--; // Réduire le compteur
                    } else {
                        // Faire pousser un arbre adulte
                        Location location = sheep.getLocation();
                        Block block = location.getBlock();
                        block.getRelative(BlockFace.DOWN).setType(Material.DIRT); // Préparer le sol pour l'arbre
                        block.setType(Material.AIR);
                        sheep.getWorld().generateTree(location, TreeType.MANGROVE);

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 20);

                        // Supprimer le mouton après activation
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L); // Répéter chaque seconde (20 ticks)
            }

            case "Mouton Épineux" -> {
                int radius = 4; // Rayon d'effet
                int[] countdown = {3}; // Compteur mutable pour le compte à rebours

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (countdown[0] > 0) {
                        countdown[0]--; // Réduire le compteur
                    } else {
                        double chance = 0.6;
                        List<Block> blocks = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocks) {
                            if (block.getType().isSolid() && !block.getRelative(BlockFace.UP).isSolid()) {
                                if (TKit.chance(chance)) {
                                    block.getRelative(BlockFace.UP).setType(Material.SWEET_BERRY_BUSH);
                                }
                            }
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.COMPOSTER, sheep.getLocation(), 30);

                        // Supprimer le mouton après activation
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L); // Répéter chaque seconde (20 ticks)
            }

            case "Mouton Chercheur" -> {
                int duration = 5 * 20; // Durée de vie (5 secondes)
                int[] ticksRemaining = {duration};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (ticksRemaining[0] > 0) {
                        // Obtenir le joueur le plus proche
                        Player nearestPlayer = TKit.getNearestPlayer(sheep.getLocation(),false);
                        if (nearestPlayer != null) {
                            // Utiliser le pathfinding pour se diriger vers le joueur
                            sheep.setAI(true);
                            sheep.getPathfinder().moveTo(nearestPlayer, 1.2); // Vitesse ajustée
                        }

                        ticksRemaining[0] -= 20; // Réduire la durée restante (toutes les secondes)
                    } else {
                        // Explosion finale
                        sheep.getWorld().createExplosion(sheep.getLocation(), 2.0F, false, true);
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 20L); // Répéter toutes les secondes
            }

            case "Mouton Instantané" -> {
                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                    } else {
                        // Explosion instantanée au sol
                        sheep.getWorld().createExplosion(sheep.getLocation(), 1.5F, false, true);
                        sheep.remove();
                    }
                },20);
            }

            case "Mouton Fragmentation" -> {
                int radius = 5; // Rayon du cercle pour les petits moutons
                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        // Explosion du mouton principal
                        sheep.getWorld().createExplosion(sheep.getLocation(), 2.5F, false, true);

                        // Faire apparaître 10 moutons "instantanés" en cercle
                        for (int i = 0; i < 10; i++) {
                            double angle = i * (2 * Math.PI / 10); // Diviser le cercle en 10 parties
                            double x = radius * Math.cos(angle);
                            double z = radius * Math.sin(angle);

                            Location spawnLocation = sheep.getLocation().add(x, 1, z);
                            Vector direction = TKit.getDirection(sheep.getLocation(),spawnLocation);
                            Sheep instantSheep = (Sheep) sheep.getWorld().spawnEntity(sheep.getLocation(), EntityType.SHEEP);

                            // Configurer le mouton comme "instantané"
                            instantSheep.setVelocity(direction.normalize().multiply(0.5));
                            instantSheep.setBaby();
                            instantSheep.customName(Component.text("Mouton Instantané"));
                            instantSheep.setCustomNameVisible(true);
                            instantSheep.setColor(TKit.getRandomDyeColor());
                            applySheepPower(instantSheep); // Activer le pouvoir des moutons instantanés
                        }

                        sheep.remove();
                    }
                }, 20L); // Explosion après 1 seconde
            }

            case "Mouton Hérisson" -> {
                int radius = 8; // Rayon d'effet
                int[] waves = {3}; // Nombre de vagues
                int delayBetweenWaves = 20; // Délai entre les vagues (1 seconde)

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (waves[0] > 0) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            // Créer une flèche et la diriger vers le joueur
                            Location arrowSpawn = sheep.getLocation().add(0, 1, 0);
                            Arrow arrow = sheep.getWorld().spawnArrow(arrowSpawn,
                                    TKit.getDirection(arrowSpawn, player.getLocation()),
                                    3.0F, 1);
                            arrow.setShooter(sheep);
                        }

                        waves[0]--;
                    } else {
                        // Supprimer le mouton après avoir tiré toutes les vagues
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, delayBetweenWaves); // Délai entre les vagues
            }

            case "Mouton Mâchoire" -> {
                int radius = 6; // Rayon d'effet

                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            // Lancer le sort de mâchoire vers chaque joueur
                            sheep.getWorld().spawnEntity(player.getLocation(), EntityType.EVOKER_FANGS);
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.LARGE_SMOKE, sheep.getLocation(), 20);

                        // Supprimer le mouton après activation
                        sheep.remove();
                        task.cancel();
                    }
                }, 3 * 20L); // Délai d'activation (3 secondes)
            }

            case "Mouton Déflagration" -> {
                int radius = 10; // Rayon d'effet
                int[] explosions = {3}; // Nombre d'explosions
                int delayBetweenExplosions = 20; // Délai entre les explosions (1 seconde)

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (explosions[0] > 0) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            // Calculer une position proche du joueur pour l'explosion
                            Location explosionLocation = player.getLocation().add(
                                    ThreadLocalRandom.current().nextDouble(-1, 1),
                                    0,
                                    ThreadLocalRandom.current().nextDouble(-1, 1));
                            sheep.getWorld().createExplosion(explosionLocation, 1.0F, false, true);
                        }

                        explosions[0]--;
                    } else {
                        // Supprimer le mouton après les explosions
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, delayBetweenExplosions); // Délai entre les explosions
            }

            case "Mouton Glowing" -> {
                int radius = 10; // Rayon d'effet

                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 10 * 20, 0)); // Glowing pendant 10 secondes
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.END_ROD, sheep.getLocation(), 50);

                        sheep.remove();
                        task.cancel();
                    }
                }, 20L); // Activation après 1 seconde
            }

            case "Mouton Invisible" -> {
                int radius = 10; // Rayon d'effet

                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10 * 20, 0)); // Invisible pendant 10 secondes
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.INSTANT_EFFECT, sheep.getLocation(), 30);

                        sheep.remove();
                        task.cancel();
                    }
                }, 20L); // Activation après 1 seconde
            }

            case "Mouton Soin" -> {
                int radius = 8; // Rayon d'effet
                int[] waves = {10}; // Nombre de vagues
                int delayBetweenWaves = 10; // Délai entre les vagues (0.5 seconde)

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (waves[0] > 0) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0)); // Soin instantané
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.HEART, sheep.getLocation(), 10);

                        waves[0]--;
                    } else {
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, delayBetweenWaves); // Répéter toutes les 0.5 secondes
            }

            case "Mouton Cauchemar" -> {
                int radius = 12; // Rayon d'effet

                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);

                        // Appliquer l'effet d'obscurité
                        for (Player player : playersInRadius) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 10 * 20, 1)); // Obscurité
                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 1)); // Obscurité
                            player.playSound(sheep,Sound.ENTITY_WARDEN_AMBIENT,1,1);
                            player.playSound(sheep,Sound.ENTITY_WARDEN_AGITATED,1,1);
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.SMOKE, sheep.getLocation(), 50);

                        sheep.remove();
                        task.cancel();
                    }
                }, 20L); // Activation après 1 seconde
            }

            case "Mouton Radioactif" -> {
                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        // Créer un nuage d'effet
                        AreaEffectCloud cloud = (AreaEffectCloud) sheep.getWorld().spawnEntity(
                                sheep.getLocation(), EntityType.AREA_EFFECT_CLOUD);

                        // Configurer le nuage d'effet
                        cloud.setDuration(20 * 10); // Dure 10 secondes
                        cloud.setRadius(3); // Rayon de l'effet
                        cloud.setColor(Color.GREEN); // Couleur du nuage
                        cloud.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 5 * 20, 1), true); // Wither niveau 2

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.WITCH, sheep.getLocation(), 50);

                        // Supprimer le mouton
                        sheep.remove();
                        task.cancel();
                    }
                }, 20L); // Activation après 1 seconde
            }

            case "Mouton Enclume" -> {
                int radius = 5; // Rayon d'effet
                double chance = 0.2; // Chance de 20% de faire spawn une enclume

                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        // Parcourir les blocs dans le rayon
                        List<Block> blocksInRadius = TKit.getBlocksInSphere(sheep.getLocation(), radius);
                        for (Block block : blocksInRadius) {
                            if (block.getType().isSolid() && block.getRelative(BlockFace.UP).getType().equals(Material.AIR) && TKit.chance(chance)) {
                                // Faire apparaître une enclume 10 blocs au-dessus
                                Location anvilLocation = block.getLocation().add(0, 10, 0);
                                FallingBlock fallingAnvil = block.getWorld().spawn(anvilLocation, FallingBlock.class);
                                // Configurer les propriétés de l'enclume
                                fallingAnvil.setBlockData(Material.ANVIL.createBlockData());
                                fallingAnvil.setHurtEntities(true); // Permet à l'enclume de blesser les entités
                                fallingAnvil.setDropItem(false); // Évite que l'enclume laisse un item drop
                                fallingAnvil.setGravity(true); // Activer la gravité par défaut

                                // Effet visuel (particule à l'endroit de l'apparition)
                                block.getWorld().spawnParticle(Particle.FALLING_DUST, anvilLocation, 20, Material.ANVIL.createBlockData());
                            }
                        }

                        // Supprimer le mouton
                        sheep.remove();
                        task.cancel();
                    }
                }, 20L); // Activation après 1 seconde
            }

            case "Mouton Alchimiste" -> {
                int radius = 10; // Rayon pour trouver les joueurs
                int[] waves = {5}; // Nombre de vagues
                int delayBetweenWaves = 40; // Délai entre les vagues (0.5 seconde)
                List<PotionEffectType> positiveEffects = TKit.getPotionEffectsByCategory("Positif");

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (waves[0] > 0){
                        // Trouver les joueurs proches
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            // Obtenir un effet positif aléatoire
                            PotionEffectType randomEffect = TKit.getRandomPotionEffect(positiveEffects);

                            // Créer une potion jetable
                            ItemStack potionItem = new ItemStack(Material.SPLASH_POTION);
                            PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
                            if (meta != null) {
                                meta.addCustomEffect(new PotionEffect(randomEffect, 200, 1), true); // 10 secondes, niveau 2
                                potionItem.setItemMeta(meta);
                            }

                            // Lancer la potion
                            ThrownPotion thrownPotion = (ThrownPotion) sheep.getWorld().spawnEntity(
                                    sheep.getLocation().add(0, 1, 0), EntityType.POTION);
                            thrownPotion.setItem(potionItem);
                            thrownPotion.setVelocity(TKit.getDirection(sheep.getLocation(), player.getLocation()).multiply(0.5));
                        }
                        waves[0]--;
                    } else {
                        sheep.remove();
                        task.cancel();
                    }
                }, 20L, delayBetweenWaves); // Lance une potion toutes les 2 secondes
            }

            case "Mouton Sorcier" -> {
                int radius = 10; // Rayon pour trouver les joueurs
                int[] waves = {5}; // Nombre de vagues
                int delayBetweenWaves = 40; // Délai entre les vagues (0.5 seconde)
                List<PotionEffectType> positiveEffects = TKit.getPotionEffectsByCategory("Négatif");

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (waves[0] > 0){
                        // Trouver les joueurs proches
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            // Obtenir un effet positif aléatoire
                            PotionEffectType randomEffect = TKit.getRandomPotionEffect(positiveEffects);

                            // Créer une potion jetable
                            ItemStack potionItem = new ItemStack(Material.SPLASH_POTION);
                            PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
                            if (meta != null) {
                                meta.addCustomEffect(new PotionEffect(randomEffect, 200, 1), true); // 10 secondes, niveau 2
                                potionItem.setItemMeta(meta);
                            }

                            // Lancer la potion
                            ThrownPotion thrownPotion = (ThrownPotion) sheep.getWorld().spawnEntity(
                                    sheep.getLocation().add(0, 1, 0), EntityType.POTION);
                            thrownPotion.setItem(potionItem);
                            thrownPotion.setVelocity(TKit.getDirection(sheep.getLocation(), player.getLocation()).multiply(0.5));
                        }
                        waves[0]--;
                    } else {
                        sheep.remove();
                        task.cancel();
                    }
                }, 20L, delayBetweenWaves); // Lance une potion toutes les 2 secondes
            }

            case "Mouton Piégé" -> {
                int duration = 15 * 20; // Durée de vie (15 secondes)
                double triggerRadius = 2; // Rayon de déclenchement de l'explosion
                int[] ticksRemaining = {duration};

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (ticksRemaining[0] > 0) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), triggerRadius,true);
                        if (!playersInRadius.isEmpty()) {
                            // Explosion si un joueur est proche
                            sheep.getWorld().createExplosion(sheep.getLocation(), 3.0F, false, true);
                            sheep.remove();
                            task.cancel();
                        }

                        ticksRemaining[0] -= 2; // Réduire la durée restante
                    } else {
                        // Fin de l'effet sans explosion
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L, 2L); // Répéter toutes les 2 ticks
            }

            case "Mouton Shuffle" -> {
                int radius = 10; // Rayon d'effet

                plugin.getServer().getScheduler().runTaskLater(plugin, task -> {
                    if (sheep.isValid()) {
                        List<Player> playersInRadius = TKit.getPlayersInRadius(sheep.getLocation(), radius,true);
                        for (Player player : playersInRadius) {
                            // Trouver un bloc solide aléatoire
                            List<Block> solidBlocks = TKit.getOpenAirBlocksAbove(TKit.getBlocksInSphere(sheep.getLocation(), radius));

                            if (!solidBlocks.isEmpty()) {
                                Block randomBlock = solidBlocks.get(ThreadLocalRandom.current().nextInt(solidBlocks.size()));
                                Location teleportLocation = randomBlock.getLocation().add(0,1,0);
                                player.teleport(teleportLocation);

                                // Effet visuel
                                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 50);
                            }
                        }

                        // Supprimer le mouton après l'activation
                        sheep.remove();
                        task.cancel();
                    }
                }, 3*20L); // Activation après 1 seconde
            }

            case "Mouton Bouclier" -> {
                int radius = 4; // Rayon pour trouver les joueurs
                int[] waves = {5*10}; // Nombre de vagues
                int delayBetweenWaves = 2; // Délai entre les vagues (0.5 seconde)

                plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
                    if (!sheep.isValid()) {
                        sheep.remove();
                        task.cancel();
                    } else if (waves[0] > 0){
                        List<Entity> entitiesInRadius = TKit.getEntitiesInRadius(sheep.getLocation(), radius);
                        for (Entity entity : entitiesInRadius) {
                            if ( !entity.equals(sheep) && !(entity instanceof Player) ) {
                                // Expulser les entités non-joueurs
                                Vector pushDirection = TKit.getDirection(sheep.getLocation(),entity.getLocation());
                                entity.setVelocity(pushDirection.multiply(2.0));
                            }
                        }

                        // Effet visuel
                        sheep.getWorld().spawnParticle(Particle.SONIC_BOOM, sheep.getLocation(), 20);

                        waves[0]--;
                    } else {
                        sheep.remove();
                        task.cancel();
                    }
                }, 0L,delayBetweenWaves); // Activation après 1 seconde
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
                {"Mouton Parasite", "#4B0082", "#8B008B"},
                {"Mouton Taupe", "#6B4226", "#A0522D"},
                {"Mouton Greta", "#228B22", "#32CD32"},
                {"Mouton Geyser", "#1E90FF", "#00BFFF"},
                {"Mouton Mystère", "#FFFF00", "#FFD700"},
                {"Mouton Bouclier", "#808080", "#D3D3D3"},
                {"Mouton Invisible", "#FFFFFF", "#E0E0E0"},
                {"Mouton Chercheur", "#8A2BE2", "#9400D3"},
                {"Mouton Soin", "#f3aeff", "#ff00ac"},
                {"Mouton Mâchoire", "#800000", "#FF4500"},
                {"Mouton Abordage", "#4682B4", "#5F9EA0"},
                {"Mouton Shuffle", "#FF69B4", "#FF1493"},
                {"Mouton Araignée", "#556B2F", "#6B8E23"},
                {"Mouton Tempétueux", "#708090", "#00CED1"},
                {"Mouton Enclume", "#696969", "#A9A9A9"},
                {"Mouton Glouton", "#0d3d1b", "#30ff00"},
                {"Mouton Fragmentation", "#FF8C00", "#FFA500"},
                {"Mouton Tremblement de Terre", "#8B0000", "#B22222"},
                {"Mouton Piégé", "#FFC0CB", "#FF69B4"},
                {"Mouton Alchimiste", "#00FF7F", "#32CD32"},
                {"Mouton Sorcier", "#FF6347", "#FF4500"},
                {"Mouton Déflagration", "#FFD700", "#FF8C00"},
                {"Mouton Épineux", "#008000", "#228B22"},
                {"Mouton Hérisson", "#B8860B", "#DAA520"},
                {"Mouton Cauchemar", "#191970", "#4B0082"},
                {"Mouton Radioactif", "#ADFF2F", "#7FFF00"},
                {"Mouton Glowing", "#FFFFE0", "#FFD700"}
        };


        // Choisir un mouton aléatoire
        int randomIndex = ThreadLocalRandom.current().nextInt(sheepData.length);
        String name = (String) sheepData[randomIndex][0];
        String startColor = (String) sheepData[randomIndex][1];
        String endColor = (String) sheepData[randomIndex][2];

        // Créer et renvoyer le composant avec le gradient
        TextColor start = TextColor.fromHexString(startColor);
        TextColor end = TextColor.fromHexString(endColor);
        return TKit.createGradientText(name, start, end);
    }

}
