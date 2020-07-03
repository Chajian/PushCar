package com.ibs.oldman.pushcar.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.game.TeamColor;

/**
 *换色器
 * 更换物品和防具的颜色
 */
public class ColorChanger implements com.ibs.oldman.pushcar.api.util.ColorChanger {
    //修改老版的物品颜色
    public static ItemStack changeLegacyStackColor(ItemStack itemStack, TeamColor teamColor) {
        Material material = itemStack.getType();
        String materialName = material.name();

        if (Main.autoColoredMaterials.contains(materialName)) {
            itemStack.setDurability((short) teamColor.woolData);
        } else if (material.toString().contains("GLASS")) {
            itemStack.setType(Material.getMaterial("STAINED_GLASS"));
            itemStack.setDurability((short) teamColor.woolData);
        } else if (material.toString().contains("GLASS_PANE")) {
            itemStack.setType(Material.getMaterial("STAINED_GLASS_PANE"));
            itemStack.setDurability((short) teamColor.woolData);
        }
        return itemStack;
    }

    //改变物品颜色
    public static Material changeMaterialColor(Material material, TeamColor teamColor) {
        String materialName = material.name();

        try {
            materialName = material.toString().substring(material.toString().indexOf("_") + 1);
        } catch (StringIndexOutOfBoundsException ignored) {
        }

        String teamMaterialColor = teamColor.material1_13;

        if (Main.autoColoredMaterials.contains(materialName)) {
            return Material.getMaterial(teamMaterialColor + "_" + materialName);
        } else if (material.toString().contains("GLASS")) {
            return Material.getMaterial(teamMaterialColor + "_STAINED_GLASS");
        } else if (material.toString().contains("GLASS_PANE")) {
            return Material.getMaterial(teamMaterialColor + "_STAINED_GLASS_PANE");
        }
        return material;

    }

    //更改皮革颜色
    public static ItemStack changeLeatherArmorColor(ItemStack itemStack, TeamColor color) {
        Material material = itemStack.getType();

        if (material.toString().contains("LEATHER_") && !material.toString().contains("LEATHER_HORSE_")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();

            meta.setColor(color.leatherColor);
            itemStack.setItemMeta(meta);

            return itemStack;
        }
        return itemStack;
    }


    @Override
    public ItemStack applyColor(com.ibs.oldman.pushcar.api.game.TeamColor apicolor, ItemStack stack) {
        try {
            TeamColor color = TeamColor.fromApiColor(apicolor);
            Material material = stack.getType();
            if (Main.isLegacy()) {
                stack = changeLegacyStackColor(stack, color);
            } else {
                stack.setType(changeMaterialColor(material, color));
            }
            stack = changeLeatherArmorColor(stack, color);
            return stack;
        } catch (NullPointerException e) {
//            Debug.warn("定义的项目不存在。 检查您的配置。");
//            e.printStackTrace();
            return new ItemStack(Material.BLACK_WOOL);
        }
    }
}
