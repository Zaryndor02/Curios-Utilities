package com.curio.utilities;

import net.neoforged.fml.ModList;

/**
 * Savoir quels mods optionnels sont presents.
 *
 * Code COMMUN (client + serveur) qui ne reference AUCUNE classe de Nature's Compass
 * ni d'Explorer's Compass : il peut toujours etre charge, meme si ces mods sont absents.
 *
 * Regle d'or du projet : une classe qui reference une classe d'un mod optionnel
 * ne doit etre appelee qu'apres un test ici.
 */
public final class CompassIntegrations {

    public static final String NATURES_COMPASS_ID = "naturescompass";
    public static final String EXPLORERS_COMPASS_ID = "explorerscompass";

    private CompassIntegrations() {
    }

    public static boolean naturesCompassLoaded() {
        return ModList.get().isLoaded(NATURES_COMPASS_ID);
    }

    public static boolean explorersCompassLoaded() {
        return ModList.get().isLoaded(EXPLORERS_COMPASS_ID);
    }
}
