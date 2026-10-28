package com.curio.utilities.elytra;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

/**
 * Elytra rendering information matching the compatibility textures used by
 * Elytra Slot 9.0.2 where they can be resolved without that mod.
 */
public final class CurioElytraRenderInfo {

    private static final ResourceLocation DEFAULT_TEXTURE =
            fromNamespaceAndPath("minecraft", "textures/entity/elytra.png");

    private static final Map<ResourceLocation, ResourceLocation> TEXTURES = new HashMap<>();

    static {
        TEXTURES.put(
                fromNamespaceAndPath("deeperdarker", "soul_elytra"),
                fromNamespaceAndPath("deeperdarker", "textures/entity/soul_elytra.png")
        );
        TEXTURES.put(
                fromNamespaceAndPath("enderitemod", "enderite_elytra_seperated"),
                DEFAULT_TEXTURE
        );
        TEXTURES.put(
                fromNamespaceAndPath("mythicmetals", "celestium_elytra"),
                fromNamespaceAndPath("mythicmetals", "textures/models/celestium_elytra.png")
        );
        TEXTURES.put(
                fromNamespaceAndPath("mekanism", "hdpe_elytra"),
                fromNamespaceAndPath("mekanism", "textures/entity/hdpe_elytra.png")
        );
        TEXTURES.put(
                fromNamespaceAndPath("alexsmobs", "tarantula_hawk_elytra"),
                DEFAULT_TEXTURE
        );
        TEXTURES.put(
                fromNamespaceAndPath("mna", "spectral_elytra"),
                fromNamespaceAndPath("mna", "textures/entity/elytra.png")
        );
        TEXTURES.put(
                fromNamespaceAndPath("wooden_elytra", "wooden_elytra"),
                fromNamespaceAndPath("wooden_elytra", "textures/entities/wooden_elytra.png")
        );
        TEXTURES.put(
                fromNamespaceAndPath("lolenderite", "enderite_plated_elytra"),
                fromNamespaceAndPath("lolenderite", "textures/entity/enderite_plated_elytra.png")
        );
        TEXTURES.put(
                fromNamespaceAndPath("netherelytra", "netherite_elytra"),
                fromNamespaceAndPath("netherelytra", "textures/entity/netherite_elytra.png")
        );
        TEXTURES.put(
                fromNamespaceAndPath("crystalmod", "sapphire_elytra"),
                fromNamespaceAndPath("crystalmod", "textures/entity/sapphire_elytra.png")
        );
        TEXTURES.put(
                fromNamespaceAndPath("crystalmod", "black_tourmaline_elytra"),
                fromNamespaceAndPath("crystalmod", "textures/entity/black_tourmaline_elytra.png")
        );
    }

    private CurioElytraRenderInfo() {
    }

    public static ResourceLocation getTexture(ItemStack stack) {
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());

        if ("lilwings".equals(id.getNamespace()) && id.getPath().endsWith("_elytra")) {
            String base = id.getPath().substring(0, id.getPath().lastIndexOf('_'));
            return fromNamespaceAndPath("lilwings", "textures/entity/" + base + ".png");
        }

        return TEXTURES.getOrDefault(id, DEFAULT_TEXTURE);
    }

    public static boolean useCapeTexture(ItemStack stack) {
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (id.getNamespace().equals("deeperdarker")
                && id.getPath().equals("soul_elytra")) {
            return false;
        }

        return !("lilwings".equals(id.getNamespace()) && id.getPath().endsWith("_elytra"));
    }
}
