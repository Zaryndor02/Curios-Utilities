package com.curio.utilities;

import com.curio.utilities.waystones.WaystonesClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Enregistrement unique des touches sur le bus MOD.
 */
@EventBusSubscriber(
        modid = CurioUtilities.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public class CurioUtilitiesClientModEvents {

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        if (CompassIntegrations.naturesCompassLoaded()) {
            NaturesCompassClient.registerKeyMappings(event);
        }

        if (CompassIntegrations.explorersCompassLoaded()) {
            ExplorersCompassClient.registerKeyMappings(event);
        }

        if (WaystonesClient.isAvailable()) {
            WaystonesClient.registerKeyMappings(event);
        }
    }
}
