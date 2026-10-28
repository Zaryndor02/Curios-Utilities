package com.curio.utilities.waystones;

import com.curio.utilities.CurioUtilities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Abonnement au bus de MOD (FMLClientSetupEvent) pour l'integration Waystones.
 * Classe a part : aucun fichier existant (CurioUtilitiesClient, renderers Aether/compass)
 * n'a besoin d'etre modifie. N'utilise aucune classe de Waystones.
 */
@EventBusSubscriber(
        modid = CurioUtilities.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public class WaystonesClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        WaystonesCurioRenderers.register(event);
    }
}
