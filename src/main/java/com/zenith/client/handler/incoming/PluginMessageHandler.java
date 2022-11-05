package com.zenith.client.handler.incoming;

import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPluginMessagePacket;
import com.zenith.client.ClientSession;
import com.zenith.util.handler.HandlerRegistry;

public class PluginMessageHandler implements HandlerRegistry.AsyncIncomingHandler<ServerPluginMessagePacket, ClientSession> {
    @Override
    public boolean applyAsync(ServerPluginMessagePacket packet, ClientSession session) {
        return !packet.getChannel().equalsIgnoreCase("MC|Brand");
    }

    @Override
    public Class<ServerPluginMessagePacket> getPacketClass() {
        return ServerPluginMessagePacket.class;
    }
}