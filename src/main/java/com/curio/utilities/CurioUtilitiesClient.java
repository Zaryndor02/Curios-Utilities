package com.curio.utilities;

import com.curio.utilities.aether.AetherCurioRenderers;
import com.curio.utilities.compass.CompassCurioRenderers;
import com.curio.utilities.waystones.WaystonesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

/**
 * Point d'entree client central.
 *
 * Les integrations optionnelles (Nature's Compass, Explorer's Compass et Waystones)
 * enregistrent leurs propres touches dans CurioUtilitiesClientModEvents, puis leurs
 * ticks/HUD sont dispatches ici uniquement si le mod correspondant est present.
 *
 * IMPORTANT : les touches de Nature's Compass ne sont volontairement plus declarees
 * ici. Elles sont definies une seule fois dans NaturesCompassClient afin d'eviter les
 * doublons dans Options > Controles.
 */
@EventBusSubscriber(modid = CurioUtilities.MOD_ID, value = Dist.CLIENT)
public class CurioUtilitiesClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        AetherCurioRenderers.register(event);
        CompassCurioRenderers.register(event);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (CompassIntegrations.naturesCompassLoaded()) {
            NaturesCompassClient.onClientTick(event);
        }

        if (CompassIntegrations.explorersCompassLoaded()) {
            ExplorersCompassClient.onClientTick(event);
        }

        if (WaystonesClient.isAvailable()) {
            WaystonesClient.onClientTick(event);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (CompassIntegrations.naturesCompassLoaded()) {
            NaturesCompassClient.onRenderGui(event);
        }

        if (CompassIntegrations.explorersCompassLoaded()) {
            ExplorersCompassClient.onRenderGui(event);
        }
    }

    /**
     * Conserve ce helper pour les mixins et le code existant.
     * Il renvoie la vraie stack presente dans le slot Curios.
     */
    public static ItemStack findCurioNaturesCompass(Player player) {
        return CurioCompassHelper.find(player);
    }
}
