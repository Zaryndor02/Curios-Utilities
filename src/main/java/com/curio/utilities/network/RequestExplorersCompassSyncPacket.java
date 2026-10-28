package com.curio.utilities.network;

import com.chaosthedude.explorerscompass.config.ConfigHandler;
import com.chaosthedude.explorerscompass.network.SyncPacket;
import com.chaosthedude.explorerscompass.util.PlayerUtils;
import com.chaosthedude.explorerscompass.util.StructureUtils;
import com.curio.utilities.CurioExplorerCompassHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestExplorersCompassSyncPacket() implements CustomPacketPayload {

    public static final Type<RequestExplorersCompassSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("curioutilities", "request_explorers_compass_sync"));

    public static final StreamCodec<FriendlyByteBuf, RequestExplorersCompassSyncPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestExplorersCompassSyncPacket());

    @Override
    public Type<RequestExplorersCompassSyncPacket> type() {
        return TYPE;
    }

    public static void handle(RequestExplorersCompassSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (CurioExplorerCompassHelper.find(player).isEmpty()) {
                return;
            }

            ServerLevel level = player.serverLevel();
            boolean canTeleport = ConfigHandler.GENERAL.allowTeleport.get()
                    && PlayerUtils.canTeleport(player.getServer(), player);
            int maxNextSearches = ConfigHandler.GENERAL.maxNextSearches.get();
            boolean infiniteXp = player.hasInfiniteMaterials();

            var allowedStructureKeys = StructureUtils.getAllowedStructureKeys(level);
            var xpLevelsForAllowedStructureKeys = StructureUtils.getXpLevelsForAllowedStructures(level, allowedStructureKeys);
            var dimensionKeysForAllowedStructureKeys = StructureUtils.getGeneratingDimensionsForAllowedStructures(level, allowedStructureKeys);
            var structureKeysToTypeKeys = StructureUtils.getStructureKeysToTypeKeys(level);
            var typeKeysToStructureKeys = StructureUtils.getTypeKeysToStructureKeys(level);

            PacketDistributor.sendToPlayer(player, new SyncPacket(
                    canTeleport,
                    maxNextSearches,
                    infiniteXp,
                    allowedStructureKeys,
                    xpLevelsForAllowedStructureKeys,
                    dimensionKeysForAllowedStructureKeys,
                    structureKeysToTypeKeys,
                    typeKeysToStructureKeys
            ));
        });
    }
}
