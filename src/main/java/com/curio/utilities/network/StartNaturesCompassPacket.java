package com.curio.utilities.network;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
import com.curio.utilities.CurioUtilitiesClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record StartNaturesCompassPacket(ResourceLocation biomeId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StartNaturesCompassPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("curioutilities", "start_natures_compass"));

    public static final StreamCodec<FriendlyByteBuf, StartNaturesCompassPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, StartNaturesCompassPacket::biomeId,
            StartNaturesCompassPacket::new
    );

    @Override
    public CustomPacketPayload.Type<StartNaturesCompassPacket> type() {
        return TYPE;
    }

    public static void handle(StartNaturesCompassPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack compass = CurioUtilitiesClient.findCurioNaturesCompass(player);
                if (!compass.isEmpty() && compass.getItem() instanceof NaturesCompassItem compassItem) {
                    BlockPos startPos = player.blockPosition();

                    // Mettre à jour l'identifiant du biome cible dans le Data Component
                    compass.set(NaturesCompass.BIOME_ID, payload.biomeId().toString());

                    // Lancer la recherche sur le serveur
                    compassItem.searchForBiome(player.serverLevel(), player, payload.biomeId(), startPos, compass);
                }
            }
        });
    }
}