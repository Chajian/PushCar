package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.game.ArenaTime;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.api.game.GameStore;
import com.ibs.oldman.pushcar.api.game.RunningTeam;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game implements com.ibs.oldman.pushcar.api.game.Game {
    /*团队信息Map*/
    private HashMap<String, Team> teams = new HashMap<>();
    /*玩家列表*/
    private List<GamePlayer> players = new ArrayList<>();
    /*商店*/
    private List<GameStore> stores = new ArrayList<>();
    /*当前团队列表*/
    private List<CurrentTeam> currentTeams = new ArrayList<>();
    /*游戏名称*/
    private String game_name;
    private GameStatus gameStatus;
    /*竞技场对角线位置*/
    private Location point1,point2;
    /*观众的坐标*/
    private Location spectator;
    /*暂停倒计时*/
    private int pauseCountdown;
    private static Main main = null;
    private int gameTime;
    private int minPlayers;
    private int maxplayers;
    private World world;
    private ArenaTime arenaTime;
    private WeatherType weatherType;
    /*大厅提示栏颜色*/
    private BarColor lobby_color;
    /*竞技场提示栏颜色*/
    private BarColor game_color;

    @Override
    public String getName() {
        return game_name;
    }

    @Override
    public GameStatus getStatus() {
        return gameStatus;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void joinToGame(Player player) {
        if(gameStatus==GameStatus.WAITING){
            GamePlayer gamePlayer = main.getPlayerGameProfile(player);
            gamePlayer.changeGame(this);
        }
    }

    @Override
    public void leaveFromGame(Player player) {
        if(gameStatus==GameStatus.DISABLED)
            return;
        GamePlayer gamePlayer = main.getPlayerGameProfile(player);
        gamePlayer.changeGame(null);
        updateScorebroad();
    }

    @Override
    public void selectPlayerTeam(Player player, com.ibs.oldman.pushcar.api.game.Team team) {

    }


    @Override
    public void selectPlayerRandomTeam(Player player) {

    }

    @Override
    public World getGameWorld() {
        return world;
    }

    @Override
    public Location getPoint1() {
        return point1;
    }

    @Override
    public Location getPoint2() {
        return point2;
    }

    @Override
    public Location getSpectatorSpawn() {
        return spectator;
    }

    @Override
    public int getGameTime() {
        return gameTime;
    }

    @Override
    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return maxplayers;
    }

    @Override
    public int countConnectedPlayers() {
        return players.size();
    }

    @Override
    public List<Player> getConnectedPlayers() {
        List<Player> connected_player = new ArrayList<>();
        for(GamePlayer gamePlayer:players){
            Player player = gamePlayer.player;
            connected_player.add(player);
        }
        return connected_player;
    }

    @Override
    public List<GameStore> getGameStores() {
        return stores;
    }

    @Override
    public int countGameStores() {
        return stores.size();
    }

    @Override
    public Team getTeamFromName(String name) {
        for(Team team:teams.values())
            if(team.getName().equals(name))
                return team;
        return null;
    }

    @Override
    public List<com.ibs.oldman.pushcar.api.game.Team> getAvailableTeams() {
        return new ArrayList<>(teams.values());
    }


    @Override
    public int countAvailableTeams() {
        return teams.size();
    }

    @Override
    public List<RunningTeam> getRunningTeams() {
        return new ArrayList<>(currentTeams);
    }

    @Override
    public int countRunningTeams() {
        return currentTeams.size();
    }

    /*获取玩家的团队对象*/
    @Override
    public RunningTeam getTeamOfPlayer(Player player) {
        for(CurrentTeam currentTeam:currentTeams)
            if(currentTeam.isPlayerInTeam(player))
                return currentTeam;
        return null;
    }

    /*判断玩家是否在团队中*/
    @Override
    public boolean isPlayerInAnyTeam(Player player) {
        for(RunningTeam runningTeam:currentTeams)
            if(runningTeam.isPlayerInTeam(player))
                return true;
        return false;
    }

    @Override
    public boolean isPlayerInTeam(Player player, RunningTeam team) {
        return getTeamOfPlayer(player) == team;
    }

    /*坐标是否在竞技场内*/
    @Override
    public boolean isLocationInArena(Location location) {
        return isInArea(location,point1,point2);
    }

    /**
     * 位置是否在竞技场内
     * @param l 目标坐标
     * @param p1 竞技场点1
     * @param p2 竞技场点2
     * @return 如果在竞技场内返回true，否则返回false
     */
    public static boolean isInArea(Location l, Location p1, Location p2) {
        if (!p1.getWorld().equals(l.getWorld())) {
            return false;
        }

        Location min = new Location(p1.getWorld(), Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ()));
        Location max = new Location(p1.getWorld(), Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ()));
        return (min.getX() <= l.getX() && min.getY() <= l.getY() && min.getZ() <= l.getZ() && max.getX() >= l.getX()
                && max.getY() >= l.getY() && max.getZ() >= l.getZ());
    }

    /**
     * 方块是否在竞技场内
     * @param l 目标方块
     * @param p1 竞技场点1
     * @param p2 竞技场点2
     * @return 如果在竞技场内返回true，否则返回false
     */
    public static boolean isChunkInArea(Chunk l, Location p1, Location p2) {
        if (!p1.getWorld().equals(l.getWorld())) {
            return false;
        }

        Chunk min = new Location(p1.getWorld(), Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ())).getChunk();
        Chunk max = new Location(p1.getWorld(), Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ())).getChunk();
        return (min.getX() <= l.getX() && min.getZ() <= l.getZ() && max.getX() >= l.getX() && max.getZ() >= l.getZ());
    }

    /*更新记分板*/
    public void updateScorebroad(){

    }

    public static boolean isBungeeEnabled() {
        return Main.getConfigurator().config.getBoolean("bungee.enabled");
    }

    public void internalJoinPlayer(){

    }


}
