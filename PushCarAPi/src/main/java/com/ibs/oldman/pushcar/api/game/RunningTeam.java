package com.ibs.oldman.pushcar.api.game;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * @author Bedwars Team
 * 游戏开始时的团队
 */
public interface RunningTeam extends Team {
    /**
     * @return
     */
    int countConnectedPlayers();

    /**
     * @return
     */
    List<Player> getConnectedPlayers();

    /**
     * @param player
     * @return
     */
    boolean isPlayerInTeam(Player player);

    /**
     * @return
     */
    boolean isDead();

    /**
     * @return
     */
    boolean isAlive();


    /**
     * @return
     */
    org.bukkit.scoreboard.Team getScoreboardTeam();

    /**
     * @param location
     */
    void addTeamChest(Location location);

    /**
     * @param block
     */
    void addTeamChest(Block block);

    /**
     * @param location
     */
    void removeTeamChest(Location location);

    /**
     * @param block
     */
    void removeTeamChest(Block block);

    /**
     * @param location
     * @return
     */
    boolean isTeamChestRegistered(Location location);

    /**
     * @param block
     * @return
     */
    boolean isTeamChestRegistered(Block block);

    /**
     * @return
     */
    Inventory getTeamChestInventory();

    /**
     * @return
     */
    int countTeamChests();
}
