package com.curio.utilities.network;

import com.curio.utilities.CompassIntegrations;
import com.curio.utilities.CurioUtilities;
import com.curio.utilities.waystones.WaystonesIntegration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Enregistrement des paquets.
 *
 * Cette classe ne reference AUCUN paquet de compass directement : chaque integration
 * enregistre les siens dans sa propre classe (NaturesCompassNetwork,
 * ExplorersCompassNetwork), chargee seulement si le mod correspondant est present.
 * Sans cela, les classes des paquets (qui utilisent les classes des compass)
 * seraient chargees meme avec le mod absent.
 */
@EventBusSubscriber(
        modid = CurioUtilities.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD
)
public class CurioUtilitiesNetwork {

    @SubscribeEvent
    public static void registerPayloads(
            RegisterPayloadHandlersEvent event
    ) {
        // optional() : un client ou un serveur qui n'a pas l'une des integrations
        // (mod compass absent d'un cote) peut quand meme se connecter.
        PayloadRegistrar registrar = event.registrar(CurioUtilities.MOD_ID)
                .versioned("1")
                .optional();

        if (CompassIntegrations.naturesCompassLoaded()) {
            registrar = NaturesCompassNetwork.register(registrar);
        }

        if (CompassIntegrations.explorersCompassLoaded()) {
            registrar = ExplorersCompassNetwork.register(registrar);
        }

        if (WaystonesIntegration.waystonesLoaded()) {
            registrar = WaystonesNetwork.register(registrar);
        }
    }
}
