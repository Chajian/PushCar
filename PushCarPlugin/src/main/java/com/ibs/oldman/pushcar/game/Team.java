package com.ibs.oldman.pushcar.game;

import org.bukkit.Location;

/**
 * 团队信息
 */
public class Team implements com.ibs.oldman.pushcar.api.game.Team {
    public TeamColor teamColor;
    public String name;
    public Location bed;//矿车出生位置
    public Location targetbed;//矿车目的地
    public Location spawn;
    public int maxplayers;
    public Game game;


    public Team clone() {
        Team t = new Team();
        t.teamColor = this.teamColor;
        t.name = this.name;
        t.bed = this.bed;
        t.spawn = this.spawn;
        t.maxplayers = this.maxplayers;
        t.game = this.game;
        return t;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public com.ibs.oldman.pushcar.api.game.TeamColor getColor() {
        return teamColor.toApiColor();
    }

    @Override
    public boolean isNewColor() {
        return false;
    }

    @Override
    public Location getTeamSpawn() {
        return spawn;
    }

    @Override
    public Location getTargetBlock() {
        return bed;
    }

    @Override
    public int getMaxPlayers() {
        return maxplayers;
    }

    @Override
    public Location getTargetBed() {
        return targetbed;
    }
}
