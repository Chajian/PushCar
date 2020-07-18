package com.ibs.oldman.pushcar.api.game;

import org.bukkit.inventory.ItemStack;

/**
 * 职业
 */
public interface Expert {

    /*获得装备*/
    ItemStack[] getEquipment();

    /*升级*/
    void upLevel();

    void setLevel();


}
