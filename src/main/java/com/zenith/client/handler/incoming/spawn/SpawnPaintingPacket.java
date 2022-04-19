/*
 * Adapted from The MIT License (MIT)
 *
 * Copyright (c) 2016-2020 DaPorkchop_
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * Any persons and/or organizations using this software must include the above copyright notice and this permission notice,
 * provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.zenith.client.handler.incoming.spawn;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.spawn.ServerSpawnPaintingPacket;
import lombok.NonNull;
import com.zenith.client.PorkClientSession;
import com.zenith.util.cache.data.entity.EntityPainting;
import com.zenith.util.handler.HandlerRegistry;

import static com.zenith.util.Constants.*;

/**
 * @author DaPorkchop_
 */
public class SpawnPaintingPacket implements HandlerRegistry.AsyncIncomingHandler<ServerSpawnPaintingPacket, PorkClientSession> {
    @Override
    public boolean applyAsync(@NonNull ServerSpawnPaintingPacket packet, @NonNull PorkClientSession session) {
        CACHE.getEntityCache().add(new EntityPainting()
                .setDirection(packet.getDirection())
                .setPaintingType(packet.getPaintingType())
                .setEntityId(packet.getEntityId())
                .setUuid(packet.getUUID())
                .setX(packet.getPosition().getX())
                .setY(packet.getPosition().getY())
                .setZ(packet.getPosition().getZ())
        );
        return true;
    }

    @Override
    public Class<ServerSpawnPaintingPacket> getPacketClass() {
        return ServerSpawnPaintingPacket.class;
    }
}
