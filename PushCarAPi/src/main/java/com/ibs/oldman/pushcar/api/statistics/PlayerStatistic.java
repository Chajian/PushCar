package com.ibs.oldman.pushcar.api.statistics;

import java.util.UUID;

/**
 * 玩家统计
 * @author yezi
 *
 */
public interface PlayerStatistic {
    /**
     * @return 返回死亡次数
     */
    int getCurrentDeaths();

    /**
     *
     * @param currentDeaths 设置当前死亡
     */
    void setCurrentDeaths(int currentDeaths);



    /**
     * @return 杀敌数
     */
    int getCurrentKills();

    /**
     * @param currentKills 设置杀敌数
     */
    void setCurrentKills(int currentKills);

    /**
     * @return 获得当前失败次数
     */
    int getCurrentLoses();

    /**
     * @param currentLoses 设置失败次数
     */
    void setCurrentLoses(int currentLoses);

    /**
     * @return 获得当前积分
     */
    int getCurrentScore();

    /**
     * @param currentScore 设置当前积分
     */
    void setCurrentScore(int currentScore);

    /**
     * @return 获得总胜利次数
     */
    int getCurrentWins();

    /**
     * @param currentWins 设置总胜利次数
     */
    void setCurrentWins(int currentWins);

    /**
     * @return
     */
    double getCurrentKD();

    /**
     * @return 获得总游戏次数
     */
    int getCurrentGames();

    /**
     * @return 获得死亡次数
     */
    int getDeaths();


    /**
     * @return 获得杀敌数
     */
    int getKills();

    /**
     * @return 获得失败次数
     */
    int getLoses();

    /**
     * @return 获得玩家名
     */
    String getName();

    /**
     * @return 获得玩家积分
     */
    int getScore();

    /**
     * @return 获得uuid
     */
    UUID getUuid();

    /**
     * @return 获得总胜利次数
     */
    int getWins();

    /**
     * @return
     */
    double getKD();

    /**
     * @return 获得游戏次数
     */
    int getGames();
}
