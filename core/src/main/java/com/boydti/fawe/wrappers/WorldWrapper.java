package com.boydti.fawe.wrappers;

import com.boydti.fawe.FaweCache;
import com.boydti.fawe.object.RunnableVal;
import com.boydti.fawe.object.changeset.FaweChangeSet;
import com.boydti.fawe.util.FaweQueue;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.WorldData;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public class WorldWrapper extends AbstractWorld {

    private final AbstractWorld parent;

    public WorldWrapper(AbstractWorld parent) {
        this.parent = parent;
    }

    @Override
    public boolean useItem(Vector position, BaseItem item, Direction face) {
        return parent.useItem(position, item, face);
    }

    @Override
    public int getMaxY() {
        return parent.getMaxY();
    }

    @Override
    public boolean isValidBlockType(int type) {
        return parent.isValidBlockType(type);
    }

    @Override
    public boolean usesBlockData(int type) {
        return parent.usesBlockData(type);
    }

    @Override
    public Mask createLiquidMask() {
        return parent.createLiquidMask();
    }

    @Override
    public int getBlockType(Vector pt) {
        return parent.getBlockType(pt);
    }

    @Override
    public int getBlockData(Vector pt) {
        return parent.getBlockData(pt);
    }

    @Override
    public void dropItem(Vector pt, BaseItemStack item, int times) {
        parent.dropItem(pt, item, times);
    }

    @Override
    public void simulateBlockMine(Vector pt) {
        parent.simulateBlockMine(pt);
    }

    @Override
    public boolean generateTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return parent.generateTree(editSession, pt);
    }

    @Override
    public boolean generateBigTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return parent.generateBigTree(editSession, pt);
    }

    @Override
    public boolean generateBirchTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return parent.generateBirchTree(editSession, pt);
    }

    @Override
    public boolean generateRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return parent.generateRedwoodTree(editSession, pt);
    }

    @Override
    public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return parent.generateTallRedwoodTree(editSession, pt);
    }

    @Override
    public void checkLoadedChunk(Vector pt) {
        parent.checkLoadedChunk(pt);
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
        parent.fixAfterFastMode(chunks);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2D> chunks) {
        parent.fixLighting(chunks);
    }

    @Override
    public boolean playEffect(Vector position, int type, int data) {
        return parent.playEffect(position, type, data);
    }

    @Override
    public boolean queueBlockBreakEffect(Platform server, Vector position, int blockId, double priority) {
        return parent.queueBlockBreakEffect(server, position, blockId, priority);
    }

    @Override
    public Vector getMinimumPoint() {
        return parent.getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return parent.getMaximumPoint();
    }

    @Override
    @Nullable
    public Operation commit() {
        return parent.commit();
    }

    @Override
    public String getName() {
        return parent.getName();
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        return parent.setBlock(position, block, notifyAndLight);
    }


    @Override
    public int getBlockLightLevel(Vector position) {
        return parent.getBlockLightLevel(position);
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        return parent.clearContainerBlockContents(position);
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        parent.dropItem(position, item);
    }

    @Override
    public boolean regenerate(final Region region, EditSession session) {
        final FaweQueue queue = session.getQueue();
        final FaweChangeSet fcs = (FaweChangeSet) session.getChangeSet();
        session.setChangeSet(fcs);
        final CuboidRegion cb = (CuboidRegion) region;
        final boolean cuboid = region instanceof CuboidRegion;
        Set<Vector2D> chunks = region.getChunks();
        TaskManager.IMP.objectTask(chunks, new RunnableVal<Vector2D>() {
            @Override
            public void run(Vector2D chunk) {
                int cx = chunk.getBlockX();
                int cz = chunk.getBlockZ();
                int bx = cx << 4;
                int bz = cz << 4;
                Vector cmin = new Vector(bx, 0, bz);
                Vector cmax = cmin.add(15, getMaxY(), 15);
                if (cuboid && region.contains(cmin) && region.contains(cmax)) {
                    if (fcs != null) {
                        for (int x = 0; x < 16; x++) {
                            int xx = x + bx;
                            for (int z = 0; z < 16; z++) {
                                int zz = z + bz;
                                for (int y = 0; y < getMaxY() + 1; y++) {
                                    int from = queue.getCombinedId4Data(xx, y, zz);
                                    if (!FaweCache.hasNBT(from >> 4)) {
                                        fcs.add(xx, y, zz, from, 0);
                                    } else {
                                        try {
                                            Vector loc = new Vector(xx, y, zz);
                                            BaseBlock block = getLazyBlock(loc);
                                            fcs.add(loc, block, FaweCache.CACHE_BLOCK[0]);
                                        } catch (Throwable e) {
                                            fcs.add(xx, y, zz, from, 0);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    for (int x = 0; x < 16; x++) {
                        int xx = x + bx;
                        for (int z = 0; z < 16; z++) {
                            int zz = z + bz;
                            for (int y = 0; y < getMaxY() + 1; y++) {
                                final Vector loc = new Vector(xx, y, zz);
                                int from = queue.getCombinedId4Data(xx, y, zz);
                                if (region.contains(loc)) {
                                    if (fcs != null) {
                                        if (!FaweCache.hasNBT(from >> 4)) {
                                            fcs.add(xx, y, zz, from, 0);
                                        } else {
                                            try {

                                                BaseBlock block = getLazyBlock(loc);
                                                fcs.add(loc, block, FaweCache.CACHE_BLOCK[0]);
                                            } catch (Throwable e) {
                                                fcs.add(xx, y, zz, from, 0);
                                            }
                                        }
                                    }
                                } else {
                                    short id = (short) (from >> 4);
                                    byte data = (byte) (from & 0xf);
                                    if (!FaweCache.hasNBT(id)) {
                                        queue.setBlock(xx, y, zz, id, data);
                                    } else {
                                        try {
                                            final BaseBlock block = getLazyBlock(loc);
                                            queue.addTask(cx, cz, new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        setBlock(loc, block, false);
                                                    } catch (WorldEditException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } catch (Throwable e) {
                                            queue.setBlock(xx, y, zz, id, data);
                                        }
                                    }
                                }


                            }
                        }
                    }
                }
                queue.regenerateChunk(cx, cz);
            }
        }, new Runnable() {
            @Override
            public void run() {
                queue.enqueue();
            }
        });
        return false;
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return parent.generateTree(type, editSession, position);
    }

    @Override
    public WorldData getWorldData() {
        return parent.getWorldData();
    }

    @Override
    public boolean equals(Object other) {
        return parent.equals(other);
    }

    @Override
    public int hashCode() {
        return parent.hashCode();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return parent.getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return parent.getEntities();
    }

    @Override
    @Nullable
    public Entity createEntity(Location location, BaseEntity entity) {
        return parent.createEntity(location, entity);
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return parent.getBlock(position);
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return parent.getLazyBlock(position);
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        return parent.getBiome(position);
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        return parent.setBiome(position, biome);
    }
}