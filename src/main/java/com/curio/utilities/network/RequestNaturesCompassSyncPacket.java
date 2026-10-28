package com.curio.utilities.network;

import com.chaosthedude.naturescompass.config.ConfigHandler;
import com.chaosthedude.naturescompass.network.SyncPacket;
import com.chaosthedude.naturescompass.util.BiomeUtils;
import com.chaosthedude.naturescompass.util.PlayerUtils;
import com.curio.utilities.CurioNatureCompassHelper;
import com.curio.utilities.CurioUtilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;

/**
 * Client -> serveur : "envoie-moi les donnees de Nature's Compass".
 *
 * Dans Nature's Compass 3.4.0, SyncPacket n'est envoye que par
 * NaturesCompassItem.use() (clic droit avec la boussole en main). Ce paquet
 * reproduit exactement le meme calcul pour une boussole portee dans Curios.
 */
public record RequestNaturesCompassSyncPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<RequestNaturesCompassSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            CurioUtilities.MOD_ID,
                            "request_natures_compass_sync"
                    )
            );

    public static final StreamCodec<FriendlyByteBuf, RequestNaturesCompassSyncPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestNaturesCompassSyncPacket());

    @Override
    public CustomPacketPayload.Type<RequestNaturesCompassSyncPacket> type() {
        return TYPE;
    }

    public static void handle(
            RequestNaturesCompassSyncPacket payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            // On ne repond que si le joueur porte vraiment la boussole dans Curios.
            if (CurioNatureCompassHelper.find(player).isEmpty()) {
                return;
            }

            ServerLevel level = player.serverLevel();

            // Meme calcul que NaturesCompassItem.use() cote serveur.
            boolean canTeleport =
                    ConfigHandler.GENERAL.allowTeleport.get()
                            && PlayerUtils.canTeleport(player.getServer(), player);

            int maxNextSearches = ConfigHandler.GENERAL.maxNextSearches.get();

            boolean infiniteXp = player.hasInfiniteMaterials();

            List<ResourceLocation> allowedBiomes =
                    BiomeUtils.getAllowedBiomeKeys(level);

            Map<ResourceLocation, Integer> xpLevelsForAllowedBiomes =
                    BiomeUtils.getXpLevelsForAllowedBiomes(allowedBiomes);

            PacketDistributor.sendToPlayer(
                    player,
                    new SyncPacket(
                            canTeleport,
                            maxNextSearches,
                            infiniteXp,
                            allowedBiomes,
                            xpLevelsForAllowedBiomes,
                            BiomeUtils.getGeneratingDimensionsForAllowedBiomes(level)
                    )
            );
        });
    }
}
