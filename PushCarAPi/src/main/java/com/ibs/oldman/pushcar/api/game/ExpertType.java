package com.ibs.oldman.pushcar.api.game;

import org.bukkit.Material;

/**
 * 职业类型
 */
public enum ExpertType {
    SWORD("剑客","", Material.IRON_SWORD,60),
    ARROW("弓箭手","",Material.ARROW,60),
    ARMOR("装甲兵","",Material.ARMOR_STAND,60),
    MAGE("法师","",Material.BLAZE_ROD,60);

    private String name;
    private String description;
    private Material showmaterial;
    private int level;
    private int cooltime;

     ExpertType(String name, String description,Material material,int cooltime) {
        this.name = name;
        this.description = description;
        this.showmaterial = material;
        this.level = 1;
        this.cooltime = cooltime;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Material getShowmaterial() {
        return showmaterial;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCooltime() {
        return cooltime;
    }
}
