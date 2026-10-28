package com.curio.utilities.compass;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public final class CompassCurioRenderers {

    private CompassCurioRenderers() {
    }

    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerItem("naturescompass", "naturescompass");
            registerItem("explorerscompass", "explorerscompass");
        });
    }

    private static void registerItem(String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        BuiltInRegistries.ITEM.getOptional(id).ifPresent(item ->
                CuriosRendererRegistry.register(item, CompassCurioRenderer::new)
        );
    }
}
