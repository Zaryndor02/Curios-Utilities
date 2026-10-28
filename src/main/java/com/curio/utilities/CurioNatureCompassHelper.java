package com.curio.utilities;

import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Code COMMUN (client + serveur) : ne doit importer aucune classe client.
 * CurioUtilitiesClient, lui, est une classe client et ne doit jamais etre
 * appelee depuis du code serveur.
 */
public final class CurioNatureCompassHelper {

    private CurioNatureCompassHelper() {
    }

    /**
     * Renvoie la VRAIE stack du slot Curios (meme reference, pas une copie),
     * ou ItemStack.EMPTY.
     */
    public static ItemStack find(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(
                        stack -> stack.getItem() instanceof NaturesCompassItem
                ))
                .map(result -> result.stack())
                .orElse(ItemStack.EMPTY);
    }
}
