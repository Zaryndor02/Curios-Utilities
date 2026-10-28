package com.curio.utilities;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public class CurioUtilitiesServer {

    public static Optional<SlotResult> findCurioNaturesCompass(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof NaturesCompassItem));
    }

    public static void startSearch(ServerPlayer player, ResourceLocation biomeId) {
        if (player.getMainHandItem().getItem() instanceof NaturesCompassItem
                || player.getOffhandItem().getItem() instanceof NaturesCompassItem) {
            return;
        }

        findCurioNaturesCompass(player).ifPresent(slotResult -> {
            ItemStack stack = slotResult.stack();
            if (stack.getItem() instanceof NaturesCompassItem compass) {
                compass.searchForBiome(player.serverLevel(), player, biomeId, player.blockPosition(), stack);
            }
        });
    }

    public static void stopSearch(ServerPlayer player) {
        if (player.getMainHandItem().getItem() instanceof NaturesCompassItem
                || player.getOffhandItem().getItem() instanceof NaturesCompassItem) {
            return;
        }

        findCurioNaturesCompass(player).ifPresent(slotResult -> {
            ItemStack stack = slotResult.stack();
            if (stack.getItem() instanceof NaturesCompassItem) {
                // Réinitialise les données de recherche de la boussole
                stack.remove(NaturesCompass.BIOME_ID);
                stack.remove(NaturesCompass.FOUND_X);
                stack.remove(NaturesCompass.FOUND_Z);
                stack.remove(NaturesCompass.SAMPLES);
            }
        });
    }
}