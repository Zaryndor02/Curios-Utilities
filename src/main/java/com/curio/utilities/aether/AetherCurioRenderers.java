package com.curio.utilities.aether;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * Registers Curios renderers for Aether accessories that already have an
 * official Accessories renderer in Aether 1.5.10.
 */
public final class AetherCurioRenderers {

    private static final String[] GLOVES = {
            "leather_gloves",
            "chainmail_gloves",
            "iron_gloves",
            "golden_gloves",
            "diamond_gloves",
            "netherite_gloves",
            "zanite_gloves",
            "gravitite_gloves",
            "neptune_gloves",
            "phoenix_gloves",
            "obsidian_gloves",
            "valkyrie_gloves"
    };

    private static final String[] PENDANTS = {
            "iron_pendant",
            "golden_pendant",
            "zanite_pendant",
            "ice_pendant"
    };

    private AetherCurioRenderers() {
    }

    public static void register(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerItems(GLOVES,
                    "com.aetherteam.aether.client.renderer.accessory.GlovesRenderer");
            registerItems(PENDANTS,
                    "com.aetherteam.aether.client.renderer.accessory.PendantRenderer");
            registerItems(
                    new String[]{"shield_of_repulsion"},
                    "com.aetherteam.aether.client.renderer.accessory.ShieldOfRepulsionRenderer"
            );
        });
    }

    private static void registerItems(String[] itemNames, String rendererClassName) {
        for (String itemName : itemNames) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("aether", itemName);
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(item ->
                    CuriosRendererRegistry.register(
                            item,
                            () -> new AetherCurioRenderer(rendererClassName)
                    )
            );
        }
    }
}
