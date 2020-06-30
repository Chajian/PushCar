package com.ibs.oldman.pushcar.api.event;

import com.ibs.oldman.pushcar.api.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * @author Bedwars Team
 */
public class PushCarApplyPropertyToDisplayedItem extends PushCarApplyPropertyToItem {

    /**
     * @param game
     * @param player
     * @param stack
     * @param properties
     */
    public PushCarApplyPropertyToDisplayedItem(Game game, Player player, ItemStack stack, Map<String, Object> properties) {
        super(game, player, stack, properties);
    }
}
