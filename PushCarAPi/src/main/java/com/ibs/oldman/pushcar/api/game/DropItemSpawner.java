package com.ibs.oldman.pushcar.api.game;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * 掉落物接口
 *
 */
public interface DropItemSpawner {

    /*冷却时间*/
    int getCurrentDown();

    /*掉落物类型*/
    Material getDropType();

    String getName();

    /*生成位置*/
    Location getSpawnLocation();

    /*所属团队*/
    Team getTeam();

    void setName(String name);

    void setSpawnLocation(Location location);

    void setTeam(Team team);

    void setCurrentDown(int time);

    void setDropType(Material type);

}
