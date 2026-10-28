package com.curio.utilities.network;

import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.curio.utilities.CurioExplorerCompassHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StopExplorersCompassPacket() implements CustomPacketPayload {

    public static final Type<StopExplorersCompassPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("curioutilities", "stop_explorers_compass"));

    public static final StreamCodec<FriendlyByteBuf, StopExplorersCompassPacket> STREAM_CODEC =
            StreamCodec.unit(new StopExplorersCompassPacket());

    @Override
    public Type<StopExplorersCompassPacket> type() {
        return TYPE;
    }

    public static void handle(StopExplorersCompassPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack compass = CurioExplorerCompassHelper.find(player);
                if (!compass.isEmpty() && compass.getItem() instanceof ExplorersCompassItem) {
                    ExplorersCompass.explorersCompass.setInactive(compass, player);
                }
            }
        });
    }
}
