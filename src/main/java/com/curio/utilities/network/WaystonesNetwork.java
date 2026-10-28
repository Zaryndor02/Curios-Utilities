package com.curio.utilities.network;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Enregistrement des paquets de l'integration Waystones. */
public final class WaystonesNetwork {

    private WaystonesNetwork() {
    }

    public static PayloadRegistrar register(PayloadRegistrar registrar) {
        return registrar.playToServer(
                UseWaystoneWarpStonePacket.TYPE,
                UseWaystoneWarpStonePacket.STREAM_CODEC,
                UseWaystoneWarpStonePacket::handle
        );
    }
}
