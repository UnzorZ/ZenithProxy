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

package com.zenith.client.handler.incoming.entity;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityEquipmentPacket;
import lombok.NonNull;
import com.zenith.client.PorkClientSession;
import com.zenith.util.cache.data.entity.EntityEquipment;
import com.zenith.util.handler.HandlerRegistry;

import static com.zenith.util.Constants.*;

/**
 * @author DaPorkchop_
 */
public class EntityEquipmentHandler implements HandlerRegistry.AsyncIncomingHandler<ServerEntityEquipmentPacket, PorkClientSession> {
    @Override
    public boolean applyAsync(@NonNull ServerEntityEquipmentPacket packet, @NonNull PorkClientSession session) {
        try {
            EntityEquipment entity = CACHE.getEntityCache().get(packet.getEntityId());
            if (entity != null) {
                entity.getEquipment().put(packet.getSlot(), packet.getItem());
            } else {
                CLIENT_LOG.warn("Received ServerEntityEquipmentPacket for invalid entity (id=%d)", packet.getEntityId());
            }
            return true;
        } catch (ClassCastException e)  {
            CLIENT_LOG.warn("Received ServerEntityEquipmentPacket for non-equipment entity (id=%d)", e, packet.getEntityId());
            return false;
        }
    }

    @Override
    public Class<ServerEntityEquipmentPacket> getPacketClass() {
        return ServerEntityEquipmentPacket.class;
    }
}
