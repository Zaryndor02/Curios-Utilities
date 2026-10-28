package com.curio.utilities.network;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Paquets de l'integration Explorer's Compass.
 * Ne charger cette classe que si Explorer's Compass est present.
 */
public final class ExplorersCompassNetwork {

    private ExplorersCompassNetwork() {
    }

    public static PayloadRegistrar register(PayloadRegistrar registrar) {
        return registrar
                .playToServer(
                        StopExplorersCompassPacket.TYPE,
                        StopExplorersCompassPacket.STREAM_CODEC,
                        StopExplorersCompassPacket::handle
                )
                .playToServer(
                        RequestExplorersCompassSyncPacket.TYPE,
                        RequestExplorersCompassSyncPacket.STREAM_CODEC,
                        RequestExplorersCompassSyncPacket::handle
                );
    }
}
