package com.ibs.oldman.pushcar.api.game;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

/**
 * 注册表
 */
public interface Registry {

    /**
     * 注册实体
     * @param name
     * @param event
     * @return
     */
    public boolean registerEvent(String name,Event event);

    /**
     * 删除实体
     * @param name
     * @return
     */
    public boolean removeEvent(String name);

    /**
     * 判断实体是否注册过
     * @param name
     * @return
     */
    public boolean isRegisterEvent(String name);

    /**
     * 注册物品
     * @param name
     * @param item
     * @return
     */
    public boolean registerItem(String name, ItemStack item);

    /**
     * 删除物品
     * @param name
     * @return
     */
    public boolean removeItem(String name);


    /**
     * 判断物品是否注册
     * @param name
     * @return
     */
    public boolean isRegisterItem(String name);


}
