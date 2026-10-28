package com.curio.utilities.elytra;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashSet;
import java.util.Set;

/**
 * Native Elytra support for Curio Utilities (detection, equip rule, lookup in the "elytra" slot).
 *
 * L'enregistrement de la capability Curios est dans CurioElytraModEvents (bus de MOD) ;
 * le pont vers Caelus est dans CurioElytraCaelusEvents.
 * The existing Curios "back" slot remains available for backpacks and other back-slot integrations.
 */
public final class CurioElytraCommon {

    private static final Set<ResourceLocation> KNOWN_ELYTRA_IDS = new HashSet<>(Set.of(
            ResourceLocation.fromNamespaceAndPath("minecraft", "elytra"),
            ResourceLocation.fromNamespaceAndPath("silentgear", "elytra"),
            ResourceLocation.fromNamespaceAndPath("mekanism", "hdpe_elytra"),
            ResourceLocation.fromNamespaceAndPath("alexsmobs", "tarantula_hawk_elytra"),
            ResourceLocation.fromNamespaceAndPath("wooden_elytra", "wooden_elytra"),
            ResourceLocation.fromNamespaceAndPath("mna", "spectral_elytra"),
            ResourceLocation.fromNamespaceAndPath("deeperdarker", "soul_elytra"),
            ResourceLocation.fromNamespaceAndPath("enderitemod", "enderite_elytra_seperated"),
            ResourceLocation.fromNamespaceAndPath("netherelytra", "netherite_elytra"),
            ResourceLocation.fromNamespaceAndPath("lolenderite", "enderite_plated_elytra"),
            ResourceLocation.fromNamespaceAndPath("mythicmetals", "celestium_elytra"),
            ResourceLocation.fromNamespaceAndPath("crystalmod", "black_tourmaline_elytra"),
            ResourceLocation.fromNamespaceAndPath("crystalmod", "sapphire_elytra")
    ));

    private CurioElytraCommon() {
    }

    /**
     * Matches the vanilla Elytra and the compatibility Elytras supported by
     * Elytra Slot 9.0.2. Lil Wings Elytras are detected dynamically.
     */
    public static boolean isElytra(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (KNOWN_ELYTRA_IDS.contains(id)) {
            return true;
        }

        return "lilwings".equals(id.getNamespace())
                && id.getPath().endsWith("_elytra");
    }

    /**
     * An Elytra in the dedicated Curios slot can coexist with chest armor,
     * but not with another Elytra already occupying the vanilla chest slot.
     */
    public static boolean canEquip(LivingEntity entity) {
        return !isElytra(entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST));
    }

    /** Elytra reconnue ET utilisable (ItemStack.canElytraFly : non cassee), comme pour le plastron vanilla. */
    public static boolean canFly(ItemStack stack, LivingEntity entity) {
        return !stack.isEmpty() && isElytra(stack) && stack.canElytraFly(entity);
    }

    /** Stack REEL du slot Curios "elytra" (meme reference, jamais une copie). */
    public static ItemStack getEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.findFirstCurio(CurioElytraCommon::isElytra, "elytra"))
                .map(result -> result.stack())
                .orElse(ItemStack.EMPTY);
    }

    public static boolean isEquipped(LivingEntity entity) {
        return !getEquipped(entity).isEmpty();
    }
}
