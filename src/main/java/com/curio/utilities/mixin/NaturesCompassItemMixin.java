package com.curio.utilities.mixin;

import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.chaosthedude.naturescompass.util.BiomeSearchWorker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;

@Mixin(value = NaturesCompassItem.class, remap = false)
public abstract class NaturesCompassItemMixin implements NaturesCompassItemAccess {

    @Shadow
    private BiomeSearchWorker worker;

    @Shadow
    public abstract void setInactive(ItemStack stack, Player player);

    @Shadow
    public abstract void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected);

    // Force la mise a jour de la recherche a chaque tick serveur meme si l'item est dans Curios
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void curioUtilities$tickCurio(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            curioUtilities$syncCurioSlot(player, stack);
        }
    }

    private void curioUtilities$syncCurioSlot(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CuriosApi.getCuriosInventory(serverPlayer).ifPresent(handler -> {
            handler.findFirstCurio(candidate -> candidate == stack).ifPresent(result -> {
                String slotId = result.slotContext().identifier();
                int slotIndex = result.slotContext().index();

                handler.setEquippedCurio(slotId, slotIndex, stack);
                serverPlayer.containerMenu.broadcastChanges();
            });
        });
    }

    @Override
    public void curioUtilities$stopSearch(ItemStack stack, Player player) {
        if (worker != null) {
            worker.stop();
            worker = null;
        }
        setInactive(stack, player);
        curioUtilities$syncCurioSlot(player, stack);
    }
}