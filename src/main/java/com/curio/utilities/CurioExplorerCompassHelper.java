package com.curio.utilities;

import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Equivalent Explorer's Compass de CurioCompassHelper.
 *
 * Code COMMUN (client + serveur). Ne l'appeler que si Explorer's Compass est charge.
 */
public final class CurioExplorerCompassHelper {

    private CurioExplorerCompassHelper() {
    }

    /**
     * Renvoie la VRAIE stack du slot Curios (meme reference, pas une copie),
     * ou ItemStack.EMPTY. Verifie dans le bytecode de Curios 9.5.1 :
     * findFirstCurio construit SlotResult avec IDynamicStackHandler.getStackInSlot.
     */
    public static ItemStack find(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.findFirstCurio(
                        stack -> stack.getItem() instanceof ExplorersCompassItem
                ))
                .map(result -> result.stack())
                .orElse(ItemStack.EMPTY);
    }
}
