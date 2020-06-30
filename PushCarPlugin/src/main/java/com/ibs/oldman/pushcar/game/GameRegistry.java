package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.api.game.Registry;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * 游戏中的注册表
 */
public class GameRegistry implements Registry {

    /*
    用于存储被注册过的实体
     */
    HashMap<String,Event> event_registry = new HashMap<>();
    /*
    用于存储被注册过的物品
     */
    TreeMap<String, ItemStack> itemStack_registry = new TreeMap<>();

    @Override
    public boolean registerEvent(String name, Event event) {
        if(!isRegisterEvent(name)){
            event_registry.put(name,event);
            return true;
        }
            return false;
    }

    @Override
    public boolean removeEvent(String name) {
        if(isRegisterEvent(name)){
            event_registry.remove(name);
            return true;
        }
            return false;
    }

    @Override
    public boolean isRegisterEvent(String name) {
        if(event_registry.get(name) != null){
            return true;
        }
        return false;
    }

    @Override
    public boolean registerItem(String name, ItemStack item) {
        if(!isRegisterItem(name)){
            itemStack_registry.put(name,item);
            return true;
        }
            return false;
    }

    @Override
    public boolean removeItem(String name) {
        if (isRegisterItem(name)) {
            itemStack_registry.remove(name);
            return true;
        }
        return false;
    }

    @Override
    public boolean isRegisterItem(String name) {
        if(itemStack_registry.get(name) != null)
            return true;
        return false;
    }
}
