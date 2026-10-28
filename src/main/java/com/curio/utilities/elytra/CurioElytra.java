package com.curio.utilities.elytra;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

/**
 * Curio implementation for Elytra items stored in Curios' dedicated "elytra" slot.
 *
 * Vol : Caelus decide si le joueur peut planer (attribut fall_flying, voir CurioElytraCaelusEvents).
 * Caelus ne fait PAS le tick de l'objet : c'est ce curioTick qui appelle
 * ItemStack.elytraFlightTick() (durabilite + evenement de vol), une fois par tick de vol.
 */
public final class CurioElytra implements ICurio {

    private final ItemStack stack;

    public CurioElytra(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        LivingEntity entity = slotContext.entity();
        int fallFlyingTicks = entity.getFallFlyingTicks();

        if (fallFlyingTicks <= 0 || !entity.isFallFlying()) {
            return;
        }

        // Si le plastron est lui-meme une Elytra utilisable, vanilla (updateFallFlying) appelle deja
        // elytraFlightTick pour elle : on ne tick pas en plus l'Elytra Curios (un seul appel par tick).
        if (entity.getItemBySlot(EquipmentSlot.CHEST).canElytraFly(entity)) {
            return;
        }

        // 'stack' est le stack reel du slot Curios : la durabilite y est retiree.
        stack.elytraFlightTick(entity, fallFlyingTicks);
    }

    @Override
    public boolean canEquip(SlotContext slotContext) {
        return CurioElytraCommon.canEquip(slotContext.entity());
    }

    @Override
    public SoundInfo getEquipSound(SlotContext slotContext) {
        return new SoundInfo(SoundEvents.ARMOR_EQUIP_ELYTRA.value(), 1.0F, 1.0F);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext) {
        return true;
    }
}
