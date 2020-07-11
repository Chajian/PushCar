package com.ibs.oldman.pushcar.api.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 天降宝箱
 */
public interface ChestItemSpawner {

    /*冷却时间*/
    int getCurrentDown();


    String getName();

    /*生成位置*/
    Location getSpawnLocation();

    /*获取宝箱的位置*/
    Location getChestLocation();

    /*所属团队*/
    Team getTeam();

    /*设置烟花飞行的角度*/
    double getAngle();

    void setName(String name);

    void setAngle(double angle);

    /*随机生成物品*/
    List<ItemStack> getRandomItem();

    void setSpawnLocation(Location location);

    void setTeam(Team team);

    void setCurrentDown(int time);





}
