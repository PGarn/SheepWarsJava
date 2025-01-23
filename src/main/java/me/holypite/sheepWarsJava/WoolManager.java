package me.holypite.sheepWarsJava;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WoolManager {
    public static ItemStack taupeWool;
    public static ItemStack gretaWool;
    public static ItemStack explosiveWool;
    public static ItemStack blackHoleWool;
    public static ItemStack geyserWool;
    public static ItemStack randomWool;
    public static ItemStack shieldWool;
    public static ItemStack invisibleWool;
    public static ItemStack honeyWool;
    public static ItemStack seekerWool;
    public static ItemStack healWool;
    public static ItemStack jawWool;
    public static ItemStack boardingWool;
    public static ItemStack incendiaryWool;
    public static ItemStack shuffleWool;
    public static ItemStack icyWool;
    public static ItemStack spiderWool;
    public static ItemStack stormWool;
    public static ItemStack anvilWool;
    public static ItemStack eaterWool;
    public static ItemStack stickyWool;
    public static ItemStack fragmentationWool;
    public static ItemStack parasiteWool;
    public static ItemStack earthquakeWool;
    public static ItemStack trappedWool;
    public static ItemStack positivePotionWool;
    public static ItemStack negativePotionWool;
    public static ItemStack chainReactionWool;
    public static ItemStack thornyWool;
    public static ItemStack cloneWool;
    public static ItemStack partyWool;
    public static ItemStack hedgehogWool;
    public static ItemStack nightmareWool;
    public static ItemStack radioactiveWool;
    public static ItemStack burrowerWool;
    public static ItemStack glowingWool;

    private static ItemStack createCustomWool(String name, String startColorHex, String endColorHex, String description, int customModelData) {
        ItemStack item = new ItemStack(Material.WHITE_WOOL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Nom de l'objet avec gradient
            TextColor startColor = TextColor.fromHexString(startColorHex);
            TextColor endColor = TextColor.fromHexString(endColorHex);
            Component displayName = UtilityFoncKit.createGradientText(name, startColor, endColor);
            meta.displayName(displayName);

            // Description (lore)
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(description).color(TextColor.fromHexString("#D2691E")));
            meta.lore(lore);

            // CustomModelData pour différencier les objets
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static void init() {
        taupeWool = createCustomWool("Mouton Taupe", "#6B4226", "#A0522D", "Creuse sous terre pour surprendre ses adversaires", 2);
        gretaWool = createCustomWool("Mouton Greta", "#228B22", "#32CD32", "Écologique et dangereux", 3);
        explosiveWool = createCustomWool("Mouton Explosif", "#FF0000", "#8B0000", "Boom ! Une explosion garantie", 4);
        blackHoleWool = createCustomWool("Mouton Trou Noir", "#000000", "#4B0082", "Aspire tout dans un rayon", 5);
        geyserWool = createCustomWool("Mouton Geyser", "#1E90FF", "#00BFFF", "Projette ses ennemis en l'air", 6);
        randomWool = createCustomWool("Mouton Mystère", "#FFFF00", "#FFD700", "Effet imprévisible", 7);
        shieldWool = createCustomWool("Mouton Bouclier", "#808080", "#D3D3D3", "Protège son équipe", 8);
        invisibleWool = createCustomWool("Mouton Invisible", "#FFFFFF", "#E0E0E0", "Se déplace sans être vu", 9);
        honeyWool = createCustomWool("Mouton Mielleux", "#FFD700", "#FFA500", "Ralentit les ennemis", 10);
        seekerWool = createCustomWool("Mouton Chercheur", "#8A2BE2", "#9400D3", "Pourchasse ses cibles", 11);
        healWool = createCustomWool("Mouton Soin", "#f3aeff", "#ff00ac", "Régénère la vie des alliés", 12);
        jawWool = createCustomWool("Mouton Mâchoire", "#800000", "#FF4500", "Invoque des entités agressives", 13);
        boardingWool = createCustomWool("Mouton Abordage", "#4682B4", "#5F9EA0", "Charge les ennemis", 14);
        incendiaryWool = createCustomWool("Mouton Incendiaire", "#FF4500", "#FF6347", "Met le feu partout", 15);
        shuffleWool = createCustomWool("Mouton Shuffle", "#FF69B4", "#FF1493", "Réorganise le terrain", 16);
        icyWool = createCustomWool("Mouton Glacial", "#87CEEB", "#00FFFF", "Gèle tout sur son passage", 17);
        spiderWool = createCustomWool("Mouton Araignée", "#556B2F", "#6B8E23", "Tisse des toiles empoisonnées", 18);
        stormWool = createCustomWool("Mouton Tempétueux", "#708090", "#00CED1", "Déclenche des orages", 20);
        anvilWool = createCustomWool("Mouton Enclume", "#696969", "#A9A9A9", "Fait pleuvoir des enclumes", 21);
        eaterWool = createCustomWool("Mouton Glouton", "#0d3d1b", "#30ff00", "Il a faim!", 22);
        stickyWool = createCustomWool("Mouton Gluant", "#32CD32", "#7FFF00", "Rend les ennemis collants", 23);
        fragmentationWool = createCustomWool("Mouton Fragmentation", "#FF8C00", "#FFA500", "Explose en plusieurs fragments", 24);
        parasiteWool = createCustomWool("Mouton Parasite", "#4B0082", "#8B008B", "Libère des créatures infectées", 25);
        earthquakeWool = createCustomWool("Mouton Tremblement de Terre", "#8B0000", "#B22222", "Secoue violemment le sol", 26);
        trappedWool = createCustomWool("Mouton Piégé", "#FFC0CB", "#FF69B4", "Ressemble à un soin, mais explose", 27);
        positivePotionWool = createCustomWool("Mouton Alchimiste", "#00FF7F", "#32CD32", "Applique un effet bénéfique", 28);
        negativePotionWool = createCustomWool("Mouton Sorcier", "#FF6347", "#FF4500", "Applique un effet néfaste", 29);
        chainReactionWool = createCustomWool("Mouton Déflagration", "#FFD700", "#FF8C00", "Déclenche des explosions en série", 30);
        thornyWool = createCustomWool("Mouton Épineux", "#008000", "#228B22", "Place des cactus et des buissons", 31);
        cloneWool = createCustomWool("Mouton Clone", "#40E0D0", "#48D1CC", "Duplique les moutons proches", 32);
        partyWool = createCustomWool("Mouton Party!!!", "#FF00FF", "#800080", "Se divise en plusieurs moutons", 33);
        hedgehogWool = createCustomWool("Mouton Hérisson", "#B8860B", "#DAA520", "Lance des flèches dans toutes les directions", 34);
        nightmareWool = createCustomWool("Mouton Cauchemar", "#191970", "#4B0082", "Plonge dans l'obscurité", 35);
        radioactiveWool = createCustomWool("Mouton Radioactif", "#ADFF2F", "#7FFF00", "Laisse une zone de poison", 36);
        burrowerWool = createCustomWool("Mouton Enfouisseur", "#8B4513", "#A0522D", "Remplace des blocs par des TNT", 37);
        glowingWool = createCustomWool("Mouton Glowing", "#FFFFE0", "#FFD700", "Illumine son environnement", 38);
    }

    /**
     * Retourne la laine correspondant au mouton basé sur son nom personnalisé.
     *
     * @param entity L'entité dont on veut récupérer la laine.
     * @return L'ItemStack représentant la laine, ou null si aucun correspondance n'est trouvée.
     */
    public static ItemStack getWoolByName(Entity entity) {
        if (entity.customName() == null) {
            return null;
        }

        String name = UtilityFoncKit.extractPlainText(entity.customName());
        return switch (name) {
            case "Mouton Taupe" -> taupeWool;
            case "Mouton Greta" -> gretaWool;
            case "Mouton Explosif" -> explosiveWool;
            case "Mouton Trou Noir" -> blackHoleWool;
            case "Mouton Geyser" -> geyserWool;
            case "Mouton Mystère" -> randomWool;
            case "Mouton Bouclier" -> shieldWool;
            case "Mouton Invisible" -> invisibleWool;
            case "Mouton Mielleux" -> honeyWool;
            case "Mouton Chercheur" -> seekerWool;
            case "Mouton Soin" -> healWool;
            case "Mouton Mâchoire" -> jawWool;
            case "Mouton Abordage" -> boardingWool;
            case "Mouton Incendiaire" -> incendiaryWool;
            case "Mouton Shuffle" -> shuffleWool;
            case "Mouton Glacial" -> icyWool;
            case "Mouton Araignée" -> spiderWool;
            case "Mouton Tempétueux" -> stormWool;
            case "Mouton Enclume" -> anvilWool;
            case "Mouton Glouton" -> eaterWool;
            case "Mouton Gluant" -> stickyWool;
            case "Mouton Fragmentation" -> fragmentationWool;
            case "Mouton Parasite" -> parasiteWool;
            case "Mouton Tremblement de Terre" -> earthquakeWool;
            case "Mouton Piégé" -> trappedWool;
            case "Mouton Alchimiste" -> positivePotionWool;
            case "Mouton Sorcier" -> negativePotionWool;
            case "Mouton Déflagration" -> chainReactionWool;
            case "Mouton Épineux" -> thornyWool;
            case "Mouton Clone" -> cloneWool;
            case "Mouton Party!!!" -> partyWool;
            case "Mouton Hérisson" -> hedgehogWool;
            case "Mouton Cauchemar" -> nightmareWool;
            case "Mouton Radioactif" -> radioactiveWool;
            case "Mouton Enfouisseur" -> burrowerWool;
            case "Mouton Glowing" -> glowingWool;
            default -> null;
        };
    }

    public static void giveAllWool(Player player) {
        UtilityFoncKit.giveItems(
            player,
            taupeWool,
            gretaWool,
            explosiveWool,
            blackHoleWool,
            geyserWool,
            randomWool,
            shieldWool,
            invisibleWool,
            honeyWool,
            seekerWool,
            healWool,
            jawWool,
            boardingWool,
            incendiaryWool,
            shuffleWool,
            icyWool,
            spiderWool,
            stormWool,
            anvilWool,
            eaterWool,
            stickyWool,
            fragmentationWool,
            parasiteWool,
            earthquakeWool,
            trappedWool,
            positivePotionWool,
            negativePotionWool,
            chainReactionWool,
            thornyWool,
            cloneWool,
            partyWool,
            hedgehogWool,
            nightmareWool,
            radioactiveWool,
            burrowerWool,
            glowingWool
        );
    }
}

