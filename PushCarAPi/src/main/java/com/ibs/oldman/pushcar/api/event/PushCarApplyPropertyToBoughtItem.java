package com.ibs.oldman.pushcar.api.event;

import com.ibs.oldman.pushcar.api.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * @author Bedwars Team
 * Bedwars将财产应用于购买物品
 */
public class PushCarApplyPropertyToBoughtItem extends PushCarApplyPropertyToItem {

    /**
     * @param game
     * @param player
     * @param stack
     * @param properties
     */
    public PushCarApplyPropertyToBoughtItem(Game game, Player player, ItemStack stack, Map<String, Object> properties) {
        super(game, player, stack, properties);
    }
}
