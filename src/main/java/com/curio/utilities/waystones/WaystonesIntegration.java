package com.curio.utilities.waystones;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Integration OPTIONNELLE de Waystones.
 *
 * Aucun type Waystones n'est utilise directement dans la signature des methodes :
 * cela permet a Curio Utilities de rester chargeable lorsque Waystones est absent.
 */
public final class WaystonesIntegration {

    public static final String WAYSTONES_ID = "waystones";
    public static final ResourceLocation WARP_STONE_ID = ResourceLocation.parse("waystones:warp_stone");

    private WaystonesIntegration() {
    }

    public static boolean waystonesLoaded() {
        return ModList.get().isLoaded(WAYSTONES_ID);
    }

    public static Optional<Item> warpStoneItem() {
        if (!waystonesLoaded()) {
            return Optional.empty();
        }
        return BuiltInRegistries.ITEM.getOptional(WARP_STONE_ID);
    }

    /**
     * Renvoie la vraie stack du slot Curios, ou EMPTY.
     */
    public static ItemStack findCurioWarpStone(net.minecraft.world.entity.player.Player player) {
        if (player == null || !waystonesLoaded()) {
            return ItemStack.EMPTY;
        }

        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(stack -> {
                    Item warpStone = BuiltInRegistries.ITEM.getOptional(WARP_STONE_ID).orElse(null);
                    return warpStone != null && stack.is(warpStone);
                }))
                .map(result -> result.stack())
                .orElse(ItemStack.EMPTY);
    }

    /**
     * Demande a Waystones d'ouvrir son menu de selection pour la Warp Stone.
     *
     * Warp StoneItem.finishUsingItem() est la routine vanilla de Waystones qui ouvre
     * le menu de destinations. Elle est appelee ici cote serveur avec la vraie stack
     * du slot Curios. Reflection evite de charger les classes Waystones/Balm lorsque
     * l'integration est absente.
     */
    public static void useWarpStoneFromCurio(ServerPlayer player) {
        if (player == null || !waystonesLoaded()) {
            return;
        }

        ItemStack warpStone = findCurioWarpStone(player);
        if (warpStone.isEmpty()) {
            return;
        }

        Item item = warpStone.getItem();
        try {
            Method finishUsingItem = item.getClass().getMethod(
                    "finishUsingItem",
                    ItemStack.class,
                    Level.class,
                    LivingEntity.class
            );

            finishUsingItem.invoke(item, warpStone, player.level(), player);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            // Waystones n'a pas la signature attendue : aucune action plutot que de
            // faire planter le client/serveur.
        }
    }
}
