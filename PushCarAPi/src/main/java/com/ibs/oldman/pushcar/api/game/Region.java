package com.ibs.oldman.pushcar.api.game;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/**
 * @author Bedwars Team
 * 区域
 */
public interface Region {
    /**
     * @param loc
     * @return
     * 在游戏期间被添加的方块
     */
    boolean isBlockAddedDuringGame(Location loc);

    /**
     * @param loc
     * @param block
     * 放置原始方块
     */
    void putOriginalBlock(Location loc, BlockState block);

    /**
     * @param loc
     *
     * 在游戏中被添加的建筑物
     */
    void addBuiltDuringGame(Location loc);

    /**
     * @param loc
     * 移除游戏中内置的方块
     */
    void removeBlockBuiltDuringGame(Location loc);

    /**
     * @param material
     * @return
     * 液体
     */
    boolean isLiquid(Material material);

    /**
     * @param block
     * @return
     * 床
     */
    boolean isBedBlock(BlockState block);

    /**
     * @param block
     * @return
     *床头
     */
    boolean isBedHead(BlockState block);

    /**
     * @param head
     * @return
     * 床的四周
     */
    Block getBedNeighbor(Block head);

    /**
     * @param chunk
     * @return
     * chunk被使用
     */
    boolean isChunkUsed(Chunk chunk);

    /**
     * Don't use from API
     * 再生
     */
    void regen();
}
