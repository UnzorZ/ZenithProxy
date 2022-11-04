package com.zenith.cache.data.chunk;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.chunk.TileEntity;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockChangeRecord;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.*;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.packetlib.packet.Packet;
import com.google.common.collect.ImmutableMap;
import com.zenith.cache.CachedData;
import lombok.NonNull;
import net.daporkchop.lib.math.vector.Vec2i;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.zenith.util.Constants.CACHE_LOG;
import static com.zenith.util.Constants.CLIENT_LOG;


public class ChunkCache implements CachedData, BiFunction<Column, Column, Column> {
    private static final Position DEFAULT_SPAWN_POSITION = new Position(8, 64, 8);
    protected final Map<Vec2i, Column> cache = new ConcurrentHashMap<>();
    protected Position spawnPosition = DEFAULT_SPAWN_POSITION;
    public void add(@NonNull Column column) {
        synchronized (this) {
            this.cache.merge(Vec2i.of(column.getX(), column.getZ()), column, this);
            CACHE_LOG.debug("Cached chunk ({}, {})", column.getX(), column.getZ());
        }
    }

    /**
     * @deprecated do not call this directly!
     */
    @Override
    @Deprecated
    public Column apply(@NonNull Column existing, @NonNull Column add) {
        synchronized (this) {
            CACHE_LOG.debug("Chunk ({}, {}) is already cached, merging with existing", add.getX(), add.getZ());
            Chunk[] chunks = existing.getChunks().clone();
            for (int chunkY = 0; chunkY < 16; chunkY++) {
                Chunk addChunk = add.getChunks()[chunkY];
                if (addChunk == null) {
                    continue;
                } else if (add.hasSkylight()) {
                    chunks[chunkY] = addChunk;
                } else {
                    chunks[chunkY] = new Chunk(addChunk.getBlocks(), addChunk.getBlockLight(), chunks[chunkY] == null ? null : chunks[chunkY].getSkyLight());
                }
            }

            return new Column(
                    add.getX(), add.getZ(),
                    chunks,
                    add.hasBiomeData() ? add.getBiomeData() : existing.getBiomeData(),
                    add.getTileEntities());
        }
    }

    public Column get(int x, int z) {
        synchronized (this) {
            return this.cache.get(Vec2i.of(x, z));
        }
    }

    public void remove(int x, int z) {
        synchronized (this) {
            CACHE_LOG.debug("Server telling us to uncache chunk ({}, {})", x, z);
            if (this.cache.remove(Vec2i.of(x, z)) == null) {
                CACHE_LOG.warn("Could not remove column ({}, {})! this is probably a server issue", x, z);
            }
        }
    }

    public boolean updateBlock(final BlockChangeRecord record) {
        synchronized (this) {
            try {
                CLIENT_LOG.debug("Handling block update: pos: [{}, {}, {}], id: {}, data: {}", record.getPosition().getX(), record.getPosition().getY(), record.getPosition().getZ(), record.getBlock().getId(), record.getBlock().getData());
                final Position pos = record.getPosition();
                if (pos.getY() < 0 || pos.getY() >= 256) {
                    CLIENT_LOG.debug("Received out-of-bounds block update: {}", record);
                    return true;
                }
                Column column = get(pos.getX() >> 4, pos.getZ() >> 4);
                if (column != null) {
                    Chunk chunk = column.getChunks()[pos.getY() >> 4];
                    if (chunk == null) {
                        chunk = column.getChunks()[pos.getY() >> 4] = new Chunk(column.hasSkylight());
                    }
                    chunk.getBlocks().set(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF, record.getBlock());
                    handleBlockUpdateTileEntity(record, pos, column);
                }
            } catch (final Exception e) {
                CLIENT_LOG.error("Error applying block update", e);
                return false;
            }
        }
        return true;
    }

    // update any tile entities implicitly affected by this block update
    // server doesn't always send us tile entity update packets and relies on logic in client
    private void handleBlockUpdateTileEntity(BlockChangeRecord record, Position pos, Column column) {
        if (record.getBlock().getId() == 0 && record.getBlock().getData() == 0) {
            final List<TileEntity> tileEntitiesList = column.getTileEntities();
            final Optional<TileEntity> foundTileEntity = tileEntitiesList.stream()
                    .filter(tileEntity -> tileEntity.getPosition().equals(pos))
                    .findFirst();
            foundTileEntity.ifPresent(tileEntitiesList::remove);
        } else {
            // if we don't create a tile entity for certain blocks they render with no texture for some reason
            if (record.getBlock().getId() == 54) {
                writeTileEntity(column, "minecraft:chest", record.getPosition());
            } else if (record.getBlock().getId() == 146) {
                writeTileEntity(column, "minecraft:trapped_chest", record.getPosition());
            } else if (record.getBlock().getId() == 130) {
                writeTileEntity(column, "minecraft:ender_chest", record.getPosition());
            } else if (record.getBlock().getId() == 116) {
                writeTileEntity(column, "minecraft:enchanting_table", record.getPosition());
            }
        }
    }

    public boolean multiBlockUpdate(final ServerMultiBlockChangePacket packet) {
        synchronized (this) {
            for (BlockChangeRecord record : packet.getRecords()) {
                updateBlock(record);
            }
            return true;
        }
    }

    public boolean updateBlock(ServerBlockChangePacket packet) {
        synchronized (this) {
            return updateBlock(packet.getRecord());
        }
    }

    private void writeTileEntity(final Column column, final String tileEntityId, final Position position) {
        final CompoundTag tileEntityTag = new CompoundTag(tileEntityId, ImmutableMap.of(
                // there's probably more properties some tile entities need but this seems to work well enough
                "id", new StringTag("id", tileEntityId),
                "x", new IntTag("x", position.getX()),
                "y", new IntTag("y", position.getY()),
                "z", new IntTag("z", position.getZ())
        ));
        final Optional<TileEntity> foundTileEntity = column.getTileEntities()
                .stream()
                .filter(tileEntity -> tileEntity.getPosition().equals(position))
                .findFirst();
        if (foundTileEntity.isPresent()) {
            foundTileEntity.get().setCompoundTag(tileEntityTag);
        } else {
            column.getTileEntities().add(new TileEntity(position, tileEntityTag));
        }
    }

    public void updateTileEntity(final ServerUpdateTileEntityPacket packet) {
        synchronized (this) {
            final Column column = get(packet.getPosition().getX() >> 4, packet.getPosition().getZ() >> 4);
            final List<TileEntity> tileEntities = column.getTileEntities();
            final Optional<TileEntity> existingTileEntity = tileEntities.stream()
                    .filter(tileEntity -> tileEntity.getPosition().equals(packet.getPosition()))
                    .findFirst();
            final CompoundTag packetNbt = packet.getNBT();
            if (packetNbt != null && !packetNbt.isEmpty()) {
                // ensure position is encoded in NBT
                // not sure if this is totally needed or not
                packetNbt.put(new IntTag("x", packet.getPosition().getX()));
                packetNbt.put(new IntTag("y", packet.getPosition().getY()));
                packetNbt.put(new IntTag("z", packet.getPosition().getZ()));
                if (existingTileEntity.isPresent()) {
                    existingTileEntity.get().setCompoundTag(packetNbt);
                } else {
                    tileEntities.add(new TileEntity(packet.getPosition(), packetNbt));
                }
            } else {
                existingTileEntity.ifPresent(tileEntities::remove);
            }
        }
    }

    public void setSpawnPosition(final Position position) {
        this.spawnPosition = position;
    }

    @Override
    public void getPackets(@NonNull Consumer<Packet> consumer) {
        synchronized (this) {
            this.cache.values().parallelStream()
                    .map(ServerChunkDataPacket::new)
                    .forEach(consumer);
            consumer.accept(new ServerSpawnPositionPacket(spawnPosition));
        }
    }

    @Override
    public void reset(boolean full) {
        synchronized (this) {
            this.cache.clear();
            this.spawnPosition = DEFAULT_SPAWN_POSITION;
        }
    }

    @Override
    public String getSendingMessage() {
        synchronized (this) {
            return String.format("Sending %d chunks, world spawn position [%d, %d, %d]",
                    this.cache.size(),
                    this.spawnPosition.getX(),
                    this.spawnPosition.getY(),
                    this.spawnPosition.getZ());
        }
    }
}
