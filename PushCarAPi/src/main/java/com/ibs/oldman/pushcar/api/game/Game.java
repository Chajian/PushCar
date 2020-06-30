package com.ibs.oldman.pushcar.api.game;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public interface Game  {

    String getName();

    GameStatus getStatus();

    void start();

    void stop();

    default boolean inActivited(){
        return getStatus() != GameStatus.DISABLED;
    }

    void joinToGame(Player player);

    void leaveFromGame(Player player);

    void selectPlayerTeam(Player player,Team team);

    void selectPlayerRandomTeam(Player player);

    World getGameWorld();

    Location getPoint1();

    Location getPoint2();

    Location getSpectatorSpawn();

    int getGameTime();

    int getMinPlayers();

    int getMaxPlayers();

    int countConnectedPlayers();

    List<Player> getConnectedPlayers();

    List<GameStore> getGameStores();

    int countGameStores();

    Team getTeamFromName(String name);

    List<Team> getAvailableTeams();

    int countAvailableTeams();

    List<RunningTeam> getRunningTeams();

    int countRunningTeams();

    RunningTeam getTeamOfPlayer(Player player);

    boolean isPlayerInAnyTeam(Player player);

    boolean isPlayerInTeam(Player player,RunningTeam team);

    boolean isLocationInArena(Location location);



}
