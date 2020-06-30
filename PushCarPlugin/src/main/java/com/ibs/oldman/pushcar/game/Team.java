package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.api.game.TeamColor;
import org.bukkit.Location;

/**
 * 团队信息
 */
public class Team implements com.ibs.oldman.pushcar.api.game.Team {
    protected com.ibs.oldman.pushcar.game.TeamColor teamColor;
    private String name;
    private Location bed;
    private Location spawn;
    private int maxplayer;
    private Game game;


    public Team clone() {
        Team t = new Team();
        t.teamColor = this.teamColor;
        t.name = this.name;
        t.bed = this.bed;
        t.spawn = this.spawn;
        t.maxplayer = this.maxplayer;
        t.game = this.game;
        return t;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public TeamColor getColor() {
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
        return maxplayer;
    }
}
