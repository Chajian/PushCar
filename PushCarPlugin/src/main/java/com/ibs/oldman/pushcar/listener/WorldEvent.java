package com.ibs.oldman.pushcar.listener;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GameCreator;
import com.ibs.oldman.pushcar.game.IpmChestItemSpawner;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;

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

    //方块被燃烧时
    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (String s : Main.getGameNames()) {
            Game game = Main.getGame(s);
            if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                if (GameCreator.isInArea(event.getBlock().getLocation(), game.getPoint1(), game.getPoint2())) {
                    if (!game.isBlockAddedDuringGame(event.getBlock().getLocation())) {
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }

    //方块消失或者融化时
    @EventHandler
    public void onFade(BlockFadeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (String s : Main.getGameNames()) {
            Game game = Main.getGame(s);
            if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                if (GameCreator.isInArea(event.getBlock().getLocation(), game.getPoint1(), game.getPoint2())) {
                    if (!game.isBlockAddedDuringGame(event.getBlock().getLocation())) {
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }

    //当根据世界情况形成或扩散一个区块时调用。
    @EventHandler
    public void onForm(BlockFormEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getNewState().getType() == Material.SNOW) {
            return;
        }

        for (String s : Main.getGameNames()) {
            Game game = Main.getGame(s);
            if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                if (GameCreator.isInArea(event.getBlock().getLocation(), game.getPoint1(), game.getPoint2())) {
                    if (!game.isBlockAddedDuringGame(event.getBlock().getLocation())) {
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }
    //实体爆炸时
    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final String explosionExceptionTypeName = Main.getConfigurator().config.getString("destroy-placed-blocks-by-explosion-except", null);
        final boolean destroyPlacedBlocksByExplosion = Main.getConfigurator().config.getBoolean("destroy-placed-blocks-by-explosion", true);

        for (String gameName : Main.getGameNames()) {
            Game game = Main.getGame(gameName);
            if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                if (GameCreator.isInArea(event.getLocation(), game.getPoint1(), game.getPoint2())) {
                    if (destroyPlacedBlocksByExplosion) {
                        event.blockList().removeIf(block -> (explosionExceptionTypeName != null && !explosionExceptionTypeName.equals("") && block.getType().name().contains(explosionExceptionTypeName)) || !game.isBlockAddedDuringGame(block.getLocation()));
                    } else {
                        event.blockList().clear();
                    }
                }
            }
        }

    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (String gameName : Main.getGameNames()) {
            Game game = Main.getGame(gameName);
            if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                if (GameCreator.isInArea(event.getLocation(), game.getPoint1(), game.getPoint2())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled() || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        for (String gameName : Main.getGameNames()) {
            Game game = Main.getGame(gameName);
            if (game.getStatus() != GameStatus.DISABLED)
                // prevent creature spawn everytime, not just in game
                if (/*(game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) &&*/ true) {
                    if (GameCreator.isInArea(event.getLocation(), game.getPoint1(), game.getPoint2())) {
                        event.setCancelled(true);
                        return;
                        //}
                    } else /*if (game.getStatus() == GameStatus.WAITING) {*/
                        if (game.getLobbyWorld() == event.getLocation().getWorld()) {
                            if (event.getLocation().distanceSquared(game.getLobbySpawn()) <= Math
                                    .pow(Main.getConfigurator().config.getInt("prevent-lobby-spawn-mobs-in-radius"), 2)) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                }
        }
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (String gameName : Main.getGameNames()) {
            Game game = Main.getGame(gameName);
            if (GameCreator.isInArea(event.getBlock().getLocation(), game.getPoint1(), game.getPoint2())) {
                if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                    Block block = event.getToBlock();
                    if (block.getType() == Material.AIR
                            || game.getRegion().isBlockAddedDuringGame(block.getLocation())) {
                        game.getRegion().addBuiltDuringGame(block.getLocation());
                    } else {
                        event.setCancelled(true);
                    }
                } else if (game.getStatus() != GameStatus.DISABLED) {
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }

        for (String gameName : Main.getGameNames()) {
            Game game = Main.getGame(gameName);
            if (GameCreator.isInArea(event.getBlock().getLocation(), game.getPoint1(), game.getPoint2())) {
                if (game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING) {
                    if (event.getEntityType() == EntityType.FALLING_BLOCK
                            && true) {
                        if (event.getBlock().getType() != event.getTo()) {
                            if (!game.getRegion().isBlockAddedDuringGame(event.getBlock().getLocation())) {
                                if (event.getBlock().getType() != Material.AIR) {
                                    game.getRegion().putOriginalBlock(event.getBlock().getLocation(),
                                            event.getBlock().getState());
                                }
                                game.getRegion().addBuiltDuringGame(event.getBlock().getLocation());
                            }
                        }
                        return; // allow block fall
                    }
                }

                if (game.getStatus() != GameStatus.DISABLED) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedWarsSpawnIsCancelled(CreatureSpawnEvent event) {
        if (!event.isCancelled()) {
            return;
        }
        // Fix for uSkyBlock plugin
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM && Main.getMain().isEntityInGame(event.getEntity())) {
            event.setCancelled(false);
        }
    }

    /**
     * 烟花爆炸时调用
     * @param explodeEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChestFirework(FireworkExplodeEvent explodeEvent){
        Firework firework = explodeEvent.getEntity();
        if(Main.isFireworkInGame(firework)){
            Game game = Main.getGameByFirework(firework);
            IpmChestItemSpawner ipmChestItemSpawner = game.getChestItemSpawnerByFirework(firework);
//            ipmChestItemSpawner.
        }
    }

}
