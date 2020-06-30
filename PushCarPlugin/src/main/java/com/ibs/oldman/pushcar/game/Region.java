package com.ibs.oldman.pushcar.game;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理区块的建筑
 * @author Yezi
 */
public class Region implements com.ibs.oldman.pushcar.api.game.Region {

    /*
    玩家放置的物品
     */
    List<Location> chunks = new ArrayList<>();
    /*
    床等一开始生存，后续需要删除，或者破坏的物品
     */
    private HashMap<Location, BlockData> brokenOriginalBlocks = new HashMap<>();


    @Override
    public boolean isBlockAddedDuringGame(Location loc) {
        return chunks.contains(loc);
    }

    @Override
    public void putOriginalBlock(Location loc, BlockState block) {
        brokenOriginalBlocks.put(loc,block.getBlockData());
    }

    @Override
    public void addBuiltDuringGame(Location loc) {
        chunks.add(loc);
    }

    @Override
    public void removeBlockBuiltDuringGame(Location loc) {
        chunks.remove(loc);
    }

    @Override
    public boolean isLiquid(Material material) {
        return material == Material.WATER || material == Material.LAVA;
    }

    @Override
    public boolean isBedBlock(BlockState block) {
        return block.getBlockData() instanceof Bed;
    }

    @Override
    public boolean isBedHead(BlockState block) {
        return isBedBlock(block) && ((Bed) block.getBlockData()).getPart() == Bed.Part.HEAD;
    }

    @Override
    public Block getBedNeighbor(Block head) {
        return getBedNeighbor(head);
    }

    /**
     * 查看块是否被玩家放置过
     * @param chunk
     * @return
     */
    @Override
    public boolean isChunkUsed(Chunk chunk) {
        if (chunk == null) {
            return false;
        }
        for (Location loc : chunks) {
            if (chunk.equals(loc.getChunk())) {
                return true;
            }
        }
        for (Location loc : brokenOriginalBlocks.keySet()) {
            if (chunk.equals(loc.getChunk())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void regen() {
        for (Location block : chunks) {
            Chunk chunk = block.getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            block.getBlock().setType(Material.AIR);
        }
        chunks.clear();
        for (Map.Entry<Location, BlockData> block : brokenOriginalBlocks.entrySet()) {
            Chunk chunk = block.getKey().getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            block.getKey().getBlock().setBlockData(block.getValue());
        }
        brokenOriginalBlocks.clear();
    }
}
