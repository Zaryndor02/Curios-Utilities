package com.curio.utilities.mixin;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.curio.utilities.CurioExplorerCompassHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Explorer's Compass (3.4.0) n'a pas de getHeldExplorersCompass : SearchPacket,
 * SearchForNextPacket et TeleportPacket appellent directement
 * ItemUtils.getHeldItem(player, ExplorersCompass.explorersCompass), qui ne regarde
 * que la main principale et la main secondaire.
 * getHeldItem etant generique (n'importe quel Item), on n'agit que pour la boussole.
 *
 * Limite au SERVEUR : cote client le HUD officiel reste inactif et c'est
 * ExplorersCompassClient (RenderGuiLayerEvent) qui dessine, comme pour Nature's Compass.
 *
 * @Pseudo + cible par nom de classe : ignore si Explorer's Compass est absent.
 */
@Pseudo
@Mixin(targets = "com.chaosthedude.explorerscompass.util.ItemUtils", remap = false)
public class ExplorersCompassItemUtilsMixin {

    @Inject(
            method = "getHeldItem",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void curioUtilities$getCurioCompass(
            Player player,
            Item item,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        if (item != ExplorersCompass.explorersCompass) {
            return;
        }

        if (!cir.getReturnValue().isEmpty()) {
            return;
        }

        ItemStack curioCompass = CurioExplorerCompassHelper.find(player);

        if (!curioCompass.isEmpty()) {
            // Meme reference que le slot Curios : les modifications des workers
            // (setSearching, succeed, setNotFound...) se font dans le slot, et Curios
            // les envoie au client tout seul.
            cir.setReturnValue(curioCompass);
        }
    }
}
