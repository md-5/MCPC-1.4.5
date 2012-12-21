package net.minecraft.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;

// Forge start
public class PlayerInstance {

    public final List b;
    private final ChunkCoordIntPair location;
// Forge end
    private short[] dirtyBlocks;
    private int dirtyCount;
    private int f;
    private boolean loaded = false; // CraftBukkit

    final PlayerManager playerManager;

    public PlayerInstance(PlayerManager playermanager, int i, int j) {
        this.playerManager = playermanager;
        this.b = new ArrayList();
        this.dirtyBlocks = new short[64];
        this.dirtyCount = 0;
        this.location = new ChunkCoordIntPair(i, j);
        // CraftBukkit start
        playermanager.a().chunkProviderServer.getChunkAt(i, j, new Runnable() {
            public void run() {
                PlayerInstance.this.loaded = true;
            }
        });
        // CraftBukkit end
    }

    public void a(final EntityPlayer entityplayer) { // CraftBukkit - added final to argument
        if (this.b.contains(entityplayer)) {
            throw new IllegalStateException("Failed to add player. " + entityplayer + " already is in chunk " + this.location.x + ", " + this.location.z);
        } else {
            this.b.add(entityplayer);

            // CraftBukkit start
            if (this.loaded) {
            entityplayer.chunkCoordIntPairQueue.add(this.location);
            } else {
                // Abuse getChunkAt to add another callback
                this.playerManager.a().chunkProviderServer.getChunkAt(this.location.x, this.location.z, new Runnable() {
                    public void run() {
                        entityplayer.chunkCoordIntPairQueue.add(PlayerInstance.this.location);
                    }
                });
            }
            // CraftBukkit end
        }
    }

    public void b(EntityPlayer entityplayer) {
        if (this.b.contains(entityplayer)) {
            entityplayer.netServerHandler.sendPacket(new Packet51MapChunk(PlayerManager.a(this.playerManager).getChunkAt(this.location.x, this.location.z), true, 0));
            this.b.remove(entityplayer);
            entityplayer.chunkCoordIntPairQueue.remove(this.location);
            // Forge start
            MinecraftForge.EVENT_BUS.post(new ChunkWatchEvent.UnWatch(this.location, entityplayer));
            // Forge end
            if (this.b.isEmpty()) {
                long i = (long) this.location.x + 2147483647L | (long) this.location.z + 2147483647L << 32;

                PlayerManager.b(this.playerManager).remove(i);
                if (this.dirtyCount > 0) {
                    PlayerManager.c(this.playerManager).remove(this);
                }

                this.playerManager.a().chunkProviderServer.queueUnload(this.location.x, this.location.z);
            }
        }
    }

    public void a(int i, int j, int k) {
        if (this.dirtyCount == 0) {
            PlayerManager.c(this.playerManager).add(this);
        }

        this.f |= 1 << (j >> 4);
        if (this.dirtyCount < 64) {
            short short1 = (short) (i << 12 | k << 8 | j);

            for (int l = 0; l < this.dirtyCount; ++l) {
                if (this.dirtyBlocks[l] == short1) {
                    return;
                }
            }

            this.dirtyBlocks[this.dirtyCount++] = short1;
        }
    }

    public void sendAll(Packet packet) {
        for (int i = 0; i < this.b.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) this.b.get(i);

            if (!entityplayer.chunkCoordIntPairQueue.contains(this.location)) {
                entityplayer.netServerHandler.sendPacket(packet);
            }
        }
    }

    public void a() {
        if (this.dirtyCount != 0) {
            int i;
            int j;
            int k;

            if (this.dirtyCount == 1) {
                i = this.location.x * 16 + (this.dirtyBlocks[0] >> 12 & 15);
                j = this.dirtyBlocks[0] & 255;
                k = this.location.z * 16 + (this.dirtyBlocks[0] >> 8 & 15);
                this.sendAll(new Packet53BlockChange(i, j, k, PlayerManager.a(this.playerManager)));
                if (PlayerManager.a(this.playerManager).isTileEntity(i, j, k)) {
                    this.sendTileEntity(PlayerManager.a(this.playerManager).getTileEntity(i, j, k));
                }
            } else {
                int l;

                if (this.dirtyCount == 64) {
                    i = this.location.x * 16;
                    j = this.location.z * 16;
                    this.sendAll(new Packet51MapChunk(PlayerManager.a(this.playerManager).getChunkAt(this.location.x, this.location.z), (this.f == 0xFFFF), this.f)); // CraftBukkit - send everything (including biome) if all sections flagged

                    for (k = 0; k < 16; ++k) {
                        if ((this.f & 1 << k) != 0) {
                            l = k << 4;
                            // Forge start
                            //BugFix: 16 makes it load an extra chunk, which isn't associated with a player, which makes it not unload unless a player walks near it.
                            //ToDo: Find a way to efficiently clean abandoned chunks.
                            //List list = PlayerManager.a(this.playerManager).getTileEntities(i, l, j, i + 16, l + 16, j + 16);
                            List list = PlayerManager.a(this.playerManager).getTileEntities(i, l, j, i + 15, l + 16, j + 15);
                            // Forge end
                            for (int i1 = 0; i1 < list.size(); ++i1) {
                                this.sendTileEntity((TileEntity) list.get(i1));
                            }
                        }
                    }
                } else {
                    this.sendAll(new Packet52MultiBlockChange(this.location.x, this.location.z, this.dirtyBlocks, this.dirtyCount, PlayerManager.a(this.playerManager)));

                    for (i = 0; i < this.dirtyCount; ++i) {
                        j = this.location.x * 16 + (this.dirtyBlocks[i] >> 12 & 15);
                        k = this.dirtyBlocks[i] & 255;
                        l = this.location.z * 16 + (this.dirtyBlocks[i] >> 8 & 15);
                        if (PlayerManager.a(this.playerManager).isTileEntity(j, k, l)) {
                            this.sendTileEntity(PlayerManager.a(this.playerManager).getTileEntity(j, k, l));
                        }
                    }
                }
            }

            this.dirtyCount = 0;
            this.f = 0;
        }
    }

    private void sendTileEntity(TileEntity tileentity) {
        if (tileentity != null) {
            Packet packet = tileentity.getUpdatePacket();

            if (packet != null) {
                this.sendAll(packet);
            }
        }
    }

    static ChunkCoordIntPair a(PlayerInstance playerinstance) {
        return playerinstance.location;
    }

    static List b(PlayerInstance playerinstance) {
        return playerinstance.b;
    }
}
