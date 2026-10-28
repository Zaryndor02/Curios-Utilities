package com.curio.utilities.network;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Paquets de l'integration Nature's Compass (inchanges, juste deplaces ici).
 * Ne charger cette classe que si Nature's Compass est present.
 */
public final class NaturesCompassNetwork {

    private NaturesCompassNetwork() {
    }

    public static PayloadRegistrar register(PayloadRegistrar registrar) {
        return registrar
                .playToServer(
                        StopNaturesCompassPacket.TYPE,
                        StopNaturesCompassPacket.STREAM_CODEC,
                        StopNaturesCompassPacket::handle
                )
                .playToServer(
                        RequestNaturesCompassSyncPacket.TYPE,
                        RequestNaturesCompassSyncPacket.STREAM_CODEC,
                        RequestNaturesCompassSyncPacket::handle
                );
    }
}
