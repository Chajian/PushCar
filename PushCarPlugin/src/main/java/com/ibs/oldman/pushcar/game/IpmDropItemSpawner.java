package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.api.game.Team;
import com.ibs.oldman.pushcar.game.entity.wave.BeamLight;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 掉落物生成器
 *
 *
 *
 */
public class IpmDropItemSpawner implements com.ibs.oldman.pushcar.api.game.DropItemSpawner {
    private int currentdown = -1;
    private int presentCurrentdown = -1;
    private Location spawnlocation;
    private Material type;
    private Team team;
    private String name;
    private List<ItemStack> producedRes = new ArrayList<>();

    /**
     *
     * @param currentdown 生成倒计时
     * @param spawnlocation 生成位置
     * @param type 物品种类
     * @param team 是否属于专属团队，暂时没有开发
     * @param name 生成器名称
     */
    public IpmDropItemSpawner(int currentdown, Location spawnlocation, Material type, Team team, String name) {
        this.currentdown = currentdown;
        this.spawnlocation = spawnlocation;
        this.type = type;
        this.team = team;
        this.name = name;
        this.presentCurrentdown = currentdown;
    }

    @Override
    public int getCurrentDown() {
        return currentdown;
    }

    @Override
    public Material getDropType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getSpawnLocation() {
        return spawnlocation;
    }

    @Override
    public Team getTeam() {
        return team;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setSpawnLocation(Location location) {
        this.spawnlocation = location;
    }

    @Override
    public void setTeam(Team team) {
        this. team = team;
    }

    @Override
    public void setCurrentDown(int time) {
        this.currentdown = time;
    }

    @Override
    public void setDropType(Material type) {
        this.type = type;
    }

    /*
    掉落物运行机制
     */
    public void runTask(World world){
        if(presentCurrentdown>0){
            presentCurrentdown--;
        }
        else {
            presentCurrentdown = currentdown;
            ItemStack itemStack = new ItemStack(type);
            world.dropItem(spawnlocation,itemStack);
            putRes(itemStack);
        }
    }




    public void clearAllItems(){
        producedRes.clear();
    }


    /**
     * 添加游戏资源到列表中
     * @param itemStack
     */
    public void putRes(ItemStack itemStack){
        producedRes.add(itemStack);
    }
}
