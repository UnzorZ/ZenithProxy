package com.zenith.network.server.handler.player.incoming;

import com.github.steveice10.mc.protocol.packet.ingame.serverbound.ServerboundClientInformationPacket;
import com.zenith.network.registry.AsyncIncomingHandler;
import com.zenith.network.server.ServerConnection;

import static com.zenith.Shared.CACHE;

public class ClientInformationHandler implements AsyncIncomingHandler<ServerboundClientInformationPacket, ServerConnection> {
    @Override
    public boolean applyAsync(ServerboundClientInformationPacket packet, ServerConnection session) {
        CACHE.getChunkCache().setRenderDistance(packet.getRenderDistance());
        return true;
    }
}
