package com.curio.utilities.waystones;

import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * Enregistrement du renderer Curios du Warp Stone (meme principe que CompassCurioRenderers).
 * Ne fait rien si Waystones n'est pas charge.
 */
public final class WaystonesCurioRenderers {

    private WaystonesCurioRenderers() {
    }

    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Item warpStone = WaystonesIntegration.warpStoneItem().orElse(null);
            if (warpStone != null) {
                CuriosRendererRegistry.register(warpStone, WaystonesCurioRenderer::new);
            }
        });
    }
}
