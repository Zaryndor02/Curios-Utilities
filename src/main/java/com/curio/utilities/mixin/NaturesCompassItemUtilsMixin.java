package com.curio.utilities.mixin;

import com.curio.utilities.CurioCompassHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nature's Compass retrouve la boussole cote serveur avec
 * ItemUtils.getHeldNatureCompass(player), qui ne regarde que la main principale
 * et la main secondaire. SearchPacket, SearchForNextPacket et TeleportPacket
 * passent tous par cette methode : sans ce mixin ils ne font rien quand la
 * boussole est dans un slot Curios.
 *
 * Le mixin est volontairement limite au SERVEUR. Cote client, le HUD officiel
 * de Nature's Compass reste donc inactif et c'est NaturesCompassClient
 * (RenderGuiLayerEvent) qui dessine, sans double affichage.
 *
 * Seul changement par rapport a la version precedente : @Pseudo + cible par nom de
 * classe (String) au lieu de ItemUtils.class, pour que le mixin soit simplement
 * ignore si Nature's Compass est absent. La logique est strictement identique.
 */
@Pseudo
@Mixin(targets = "com.chaosthedude.naturescompass.util.ItemUtils", remap = false)
public class NaturesCompassItemUtilsMixin {

    @Inject(
            method = "getHeldNatureCompass",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void curioUtilities$getCurioCompass(
            Player player,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        if (!cir.getReturnValue().isEmpty()) {
            return;
        }

        ItemStack curioCompass = CurioCompassHelper.find(player);

        if (!curioCompass.isEmpty()) {
            // Meme reference que le slot Curios : les modifications du worker
            // (setSearching, succeed, setNotFound...) se font dans le slot, et
            // Curios les envoie au client tout seul.
            cir.setReturnValue(curioCompass);
        }
    }
}
