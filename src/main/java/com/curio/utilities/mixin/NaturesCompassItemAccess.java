package com.curio.utilities.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface NaturesCompassItemAccess {

    void curioUtilities$stopSearch(ItemStack stack, Player player);
}