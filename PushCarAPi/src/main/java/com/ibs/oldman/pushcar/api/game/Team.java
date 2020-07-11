package com.ibs.oldman.pushcar.api.game;

import org.bukkit.Location;

/**
 * 团队接口
 * @author yezi
 */
public interface Team {
    /**
     * @return 游戏对象
     */
//    Game getGame();

    /**
     * @return 返回团队名
     */
    String getName();

    /**
     * @return 返回团队颜色
     */
    TeamColor getColor();

    /**
     * 是否是新的颜色
     * @return isNewColor boolean u
     */
    boolean isNewColor();

    /**
     * @return 团队的出生点
     */
    Location getTeamSpawn();

//    /**
//     * @return 获得矿车出生地位置
//     */
//    Location getTargetBlock();

    /**
     *
     * @return 获得矿车目的地
     */
    Location getTargetBed();

    /**
     * @return 获得最大人数
     */
    int getMaxPlayers();
}
