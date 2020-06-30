package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.api.game.RunningTeam;
import com.ibs.oldman.pushcar.api.game.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;


/**
 * 当前团队
 */
public class CurrentTeam implements RunningTeam {
    /*团队信息*/
    public final Team teamInfo;
    /*玩家列表*/
    public final List<GamePlayer> players = new ArrayList<>();
    /*团队记分板*/
    private org.bukkit.scoreboard.Team scoreboardTeam;//团队记分板
//    private Inventory chestInventory = Bukkit.createInventory(null, InventoryType.ENDER_CHEST, I.i18n("team_chest"));

    /*团队放置的方块*/
    private List<Block> chests = new ArrayList<>();
    private Game game;
//    private Hologram holo;
//    private Hologram protectHolo;


    public CurrentTeam(Team team, Game game) {
        this.teamInfo = team;
        this.game = game;
    }

    public boolean isDead() {
        return players.isEmpty();
    }

    public boolean isAlive() {
        return !players.isEmpty();
    }

    public org.bukkit.scoreboard.Team getScoreboardTeam() {
        return scoreboardTeam;
    }

    public void setScoreboardTeam(org.bukkit.scoreboard.Team scoreboardTeam) {
        this.scoreboardTeam = scoreboardTeam;
    }

//    public void setBedHolo(Hologram holo) {
//        this.holo = holo;
//    }
//
//    public Hologram getBedHolo() {
//        return this.holo;
//    }

//    public boolean hasBedHolo() {
//        return this.holo != null;
//    }

//    public void setProtectHolo(Hologram protectHolo) {
//        this.protectHolo = protectHolo;
//    }
//
//    public Hologram getProtectHolo() {
//        return this.protectHolo;
//    }

//    public boolean hasProtectHolo() {
//        return this.protectHolo != null;
//    }

    @Override
    public String getName() {
        return teamInfo.getName();
    }

    @Override
    public TeamColor getColor() {
        return teamInfo.getColor();
    }

    @Override
    public boolean isNewColor() {
        return false;
    }

//    @Override
//    public TeamColor getColor() {
//        return teamInfo.teamColor;
//    }

//    @Override
//    public boolean isNewColor() {
//        return teamInfo.get;
//    }

    @Override
    public Location getTeamSpawn() {
        return teamInfo.getTeamSpawn();
    }

    @Override
    public Location getTargetBlock() {
        return teamInfo.getTargetBlock();
    }

    @Override
    public int getMaxPlayers() {
        return teamInfo.getMaxPlayers();
    }

    @Override
    public int countConnectedPlayers() {
        return players.size();
    }

    @Override
    public List<Player> getConnectedPlayers() {
        List<Player> playerList = new ArrayList<>();
        for (GamePlayer gamePlayer : players) {
            playerList.add(gamePlayer.player);
        }
        return playerList;
    }

    /**
     * @param player  玩家实体
     * @return 如果玩家在此团队中返回ture，否则返回false
     */
    @Override
    public boolean isPlayerInTeam(Player player) {
        for (GamePlayer gamePlayer : players) {
            if (gamePlayer.player.equals(player)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addTeamChest(Location location) {
        addTeamChest(location.getBlock());
    }

    @Override
    public void addTeamChest(Block block) {
        if (!chests.contains(block)) {
            chests.add(block);
        }
    }

    @Override
    public void removeTeamChest(Location location) {
        removeTeamChest(location.getBlock());
    }

    @Override
    public void removeTeamChest(Block block) {
        if (chests.contains(block)) {
            chests.remove(block);
        }
    }

    @Override
    public boolean isTeamChestRegistered(Location location) {
        return isTeamChestRegistered(location.getBlock());
    }

    @Override
    public boolean isTeamChestRegistered(Block block) {
        return chests.contains(block);
    }

    @Override
    public Inventory getTeamChestInventory() {
        return null;
    }

//    @Override
//    public Game getGame() {
//        return game;
//    }



    @Override
    public int countTeamChests() {
        return chests.size();
    }
}