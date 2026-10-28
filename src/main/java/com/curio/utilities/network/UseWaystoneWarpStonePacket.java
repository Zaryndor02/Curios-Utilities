package com.curio.utilities.network;

import com.curio.utilities.waystones.WaystonesIntegration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Demande serveur envoyee lorsque le joueur appuie sur la touche de la Warp Stone.
 */
public record UseWaystoneWarpStonePacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UseWaystoneWarpStonePacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath("curioutilities", "use_waystone_warp_stone")
            );

    public static final StreamCodec<FriendlyByteBuf, UseWaystoneWarpStonePacket> STREAM_CODEC =
            StreamCodec.unit(new UseWaystoneWarpStonePacket());

    @Override
    public CustomPacketPayload.Type<UseWaystoneWarpStonePacket> type() {
        return TYPE;
    }

    public static void handle(UseWaystoneWarpStonePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                WaystonesIntegration.useWarpStoneFromCurio(player);
            }
        });
    }
}
