package com.ibs.oldman.pushcar.api.event;

import com.ibs.oldman.pushcar.api.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 玩家已经加入游戏事件
 */
public class PushCarPlayerJoinedEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private String cancelMessage = null;
    private Game game;
    private Player player;

    public PushCarPlayerJoinedEvent(Game game,Player player){
        this.game = game;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return PushCarPlayerJoinedEvent.handlers;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {

    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }
}
