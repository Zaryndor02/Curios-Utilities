package com.curio.utilities.mixin;

import com.chaosthedude.naturescompass.gui.NaturesCompassScreen;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.curio.utilities.CurioUtilitiesClient;
import com.curio.utilities.network.StartNaturesCompassPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NaturesCompassScreen.class, remap = false)
public abstract class NaturesCompassScreenMixin {

    @Shadow
    public abstract ResourceLocation getSelectedBiomeLocation();

    @Inject(method = "startSearch", at = @At("HEAD"), cancellable = true)
    private void onStartSearch(Button button, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            ItemStack curioCompass = CurioUtilitiesClient.findCurioNaturesCompass(mc.player);
            boolean hasInHand = mc.player.getMainHandItem().getItem() instanceof NaturesCompassItem
                    || mc.player.getOffhandItem().getItem() instanceof NaturesCompassItem;

            // Si la boussole est dans Curios (et PAS dans les mains)
            if (!hasInHand && !curioCompass.isEmpty()) {
                ResourceLocation selectedBiome = getSelectedBiomeLocation();
                if (selectedBiome != null) {
                    // On envoie notre propre paquet pour appliquer le nouveau biome sur la boussole Curios
                    PacketDistributor.sendToServer(new StartNaturesCompassPacket(selectedBiome));
                    mc.setScreen(null);
                    ci.cancel(); // Annule la recherche par defaut qui cherchait dans la main
                }
            }
        }
    }
}