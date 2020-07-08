package com.ibs.oldman.pushcar.listener;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GameCreator;
import org.bukkit.Chunk;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * 世界监听
 */
public class WorldEvent implements Listener {


    /**
     * 怪物生成事件
     * @param unload
     */
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent unload) {
        if (unload instanceof Cancellable) {
            Chunk chunk = unload.getChunk();

            for (String name : Main.getGameNames()) {
                Game game = Main.getGame(name);
                if (game.getStatus() != GameStatus.DISABLED && game.getStatus() != GameStatus.WAITING
                        && GameCreator.isChunkInArea(chunk, game.getPoint1(), game.getPoint2())) {
                    ((Cancellable) unload).setCancelled(false);
                    return;
                }
            }
        }
    }
}
