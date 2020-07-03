package com.ibs.oldman.pushcar.api.util;

import com.ibs.oldman.pushcar.api.game.TeamColor;
import org.bukkit.inventory.ItemStack;


/**
 * @author Bedwars Team
 * 换色器
 */
public interface ColorChanger {

    /**
     * Apply color of team to ItemStack
     *将团队的颜色应用到物品身上
     *
     * @param color Color of team
     * @param stack ItemStack that should be colored
     * @return colored ItemStack or normal ItemStack if ItemStack can't be colored
     */
    ItemStack applyColor(TeamColor color, ItemStack stack);
}
