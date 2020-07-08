package com.ibs.oldman.pushcar.utils;

import com.ibs.oldman.pushcar.game.Team;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 * 护甲架工具
 */
public class ArmorStandUtils {
    public static void equipArmorStand(ArmorStand stand, Team team) {
        if (stand == null || team == null) {
            return;
        }

        // helmet 头盔
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(team.teamColor.leatherColor);
        helmet.setItemMeta(meta);

        // chestplate 胸甲
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(team.teamColor.leatherColor);
        chestplate.setItemMeta(meta);

        // leggings 绑腿
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        meta = (LeatherArmorMeta) leggings.getItemMeta();
        meta.setColor(team.teamColor.leatherColor);
        leggings.setItemMeta(meta);

        // boots 靴子
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);
        meta = (LeatherArmorMeta) boots.getItemMeta();
        meta.setColor(team.teamColor.leatherColor);
        boots.setItemMeta(meta);

        stand.setHelmet(helmet);
        stand.setChestplate(chestplate);
        stand.setLeggings(leggings);
        stand.setBoots(boots);
    }
}
