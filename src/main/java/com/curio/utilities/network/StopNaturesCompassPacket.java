package com.curio.utilities.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.curio.utilities.CurioNatureCompassHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StopNaturesCompassPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StopNaturesCompassPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("curioutilities", "stop_natures_compass"));

    public static final StreamCodec<FriendlyByteBuf, StopNaturesCompassPacket> STREAM_CODEC =
            StreamCodec.unit(new StopNaturesCompassPacket());

    @Override
    public CustomPacketPayload.Type<StopNaturesCompassPacket> type() {
        return TYPE;
    }

    public static void handle(StopNaturesCompassPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // CurioCompassHelper est du code commun. L'ancienne version appelait
                // CurioUtilitiesClient (classe client) depuis le serveur : ca marche
                // en solo mais plante sur un serveur dedie (NoClassDefFoundError).
                ItemStack compass = CurioNatureCompassHelper.find(player);
                if (!compass.isEmpty() && compass.getItem() instanceof NaturesCompassItem) {
                    compass.remove(NaturesCompass.BIOME_ID);
                    compass.remove(NaturesCompass.FOUND_X);
                    compass.remove(NaturesCompass.FOUND_Z);
                    compass.remove(NaturesCompass.SAMPLES);
                    compass.remove(NaturesCompass.SEARCH_RADIUS);
                    compass.remove(NaturesCompass.DISPLAY_COORDS);
                }
            }
        });
    }
}
