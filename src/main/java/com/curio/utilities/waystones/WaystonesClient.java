package com.curio.utilities.waystones;

import com.mojang.blaze3d.platform.InputConstants;
import com.curio.utilities.CurioUtilities;
import com.curio.utilities.network.UseWaystoneWarpStonePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Integration client de Waystones pour la Warp Stone placee dans Curios.
 *
 * La touche par defaut est G et reste entierement configurable dans Options > Controles.
 */
public final class WaystonesClient {

    public static final KeyMapping USE_WARP_STONE = new KeyMapping(
            "key.curioutilities.use_warp_stone",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.curioutilities"
    );

    private WaystonesClient() {
    }

    public static boolean isAvailable() {
        return WaystonesIntegration.waystonesLoaded();
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(USE_WARP_STONE);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }

        while (USE_WARP_STONE.consumeClick()) {
            ItemStack warpStone = WaystonesIntegration.findCurioWarpStone(player);
            if (!warpStone.isEmpty()) {
                PacketDistributor.sendToServer(new UseWaystoneWarpStonePacket());
            }
        }
    }
}
