package com.ibs.oldman.pushcar.listener;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.game.*;
import com.ibs.oldman.pushcar.inventory.TeamSelectorInventory;
import com.ibs.oldman.pushcar.lib.nms.util.MiscUtils;
import com.ibs.oldman.pushcar.utils.ArmorStandUtils;
import com.ibs.oldman.pushcar.utils.Sounds;
import com.ibs.oldman.pushcar.utils.TeamJoinMetaDataValue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

import static lang.I18n.i18n;
import static lang.I18n.i18nonly;
import static com.ibs.oldman.pushcar.command.BaseCommands.ADMIN_PERMISSION;

/**
 * 监听玩家
 */
public class PlayerEvent implements Listener {




    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            Player p = (Player) event.getWhoClicked();
            if (Main.isPlayerInGame(p)) {
                GamePlayer gPlayer = Main.getPlayerGameProfile(p);
                Game game = gPlayer.getGame();
                if (game.getStatus() == GameStatus.WAITING || gPlayer.isSpectator) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (Main.isPlayerInGame(player)) {
            Game game = Main.getPlayerGameProfile(player).getGame();
            if (!(entity instanceof LivingEntity)) {
                return;
            }

            if (game.getStatus() != GameStatus.WAITING) {
                return;
            }
            LivingEntity living = (LivingEntity) entity;
            String displayName = ChatColor.stripColor(living.getCustomName());

            for (Team team : game.getTeams().values()) {
                if (team.name.equals(displayName)) {
                    event.setCancelled(true);
                    game.selectTeam(Main.getPlayerGameProfile(player), displayName);
                    return;
                }
            }

        } else if (player.hasPermission(ADMIN_PERMISSION)) {
            List<MetadataValue> values = player.getMetadata(GameCreator.BEDWARS_TEAM_JOIN_METADATA);
            if (values.size() == 0) {
                return;
            }

            event.setCancelled(true);
            TeamJoinMetaDataValue value = (TeamJoinMetaDataValue) values.get(0);
            if (!((boolean) value.value())) {
                return;
            }

            if (!(entity instanceof LivingEntity)) {
                player.sendMessage(i18n("admin_command_jointeam_entitynotcompatible"));
                return;
            }

            LivingEntity living = (LivingEntity) entity;
            living.setRemoveWhenFarAway(false);
            living.setCanPickupItems(false);
            living.setCustomName(value.getTeam().teamColor.chatColor + value.getTeam().name);
            living.setCustomNameVisible(Main.getConfigurator().config.getBoolean("jointeam-entity-show-name", true));

            if (living instanceof ArmorStand) {
                ArmorStandUtils.equipArmorStand((ArmorStand) living, value.getTeam());
            }

            player.removeMetadata(GameCreator.BEDWARS_TEAM_JOIN_METADATA, Main.getMain());
            player.sendMessage(i18n("admin_command_jointeam_entity_added"));
        }
    }


    /*玩家右键交互事件*/
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled() && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        if (Main.isPlayerInGame(player)) {
            GamePlayer gPlayer = Main.getPlayerGameProfile(player);
            Game game = gPlayer.getGame();
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (game.getStatus() == GameStatus.WAITING || gPlayer.isSpectator) {
                    event.setCancelled(true);
                    if (event.getMaterial() == Material
                            .valueOf(Main.getConfigurator().config.getString("items.jointeam", "COMPASS"))) {
                        if (game.getStatus() == GameStatus.WAITING) {
                            TeamSelectorInventory inv = game.getTeamSelectorInventory();
                            if (inv == null) {
                                return;
                            }
                            inv.openForPlayer(player);
                        } else if (gPlayer.isSpectator) {
                            // TODO
                        }
                    } else if (event.getMaterial() == Material
                            .valueOf(Main.getConfigurator().config.getString("items.startgame", "DIAMOND"))) {
                        if (game.getStatus() == GameStatus.WAITING && (player.hasPermission("bw.vip.startitem")
                                || player.hasPermission("misat11.bw.vip.startitem"))) {
                            if (game.checkMinPlayers()) {
                                game.gameStartItem = true;
                            } else {
                                player.sendMessage(i18n("vip_not_enough_players"));
                            }
                        }
                    } else if (event.getMaterial() == Material
                            .valueOf(Main.getConfigurator().config.getString("items.leavegame", "SLIME_BALL"))) {
                        game.leaveFromGame(player);
                    }
                }

                if (game.getStatus() == GameStatus.RUNNING) {
                    if (event.getClickedBlock() != null) {
                        if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                            Block chest = event.getClickedBlock();
                            CurrentTeam team = game.getTeamOfChest(chest);
                            event.setCancelled(true);

                            if (team == null) {
                                player.openInventory(game.getFakeEnderChest(gPlayer));
                                return;
                            }

                            if (!team.players.contains(gPlayer)) {
                                player.sendMessage(i18n("team_chest_is_not_your"));
                                return;
                            }

//                            BedwarsTeamChestOpenEvent teamChestOpenEvent = new BedwarsTeamChestOpenEvent(game, player,
//                                    team);
//                            Main.getInstance().getServer().getPluginManager().callEvent(teamChestOpenEvent);
//
//                            if (teamChestOpenEvent.isCancelled()) {
//                                return;
//                            }

                            player.openInventory(team.getTeamChestInventory());
                            //触发箱子
                        } else if (event.getClickedBlock().getState() instanceof InventoryHolder) {
                            InventoryHolder holder = (InventoryHolder) event.getClickedBlock().getState();
//                            game.addChestForFutureClear(event.getClickedBlock().getLocation(), holder.getInventory());
                        } else if (event.getClickedBlock().getType().name().contains("CAKE") && Main.getConfigurator().config.getBoolean("disableCakeEating", true)) {
                            event.setCancelled(true);
                        }
                    }
                }

                if (event.getClickedBlock() != null) {
                    if (game.getRegion().isBedBlock(event.getClickedBlock().getState())) {
                        // prevent Essentials to set home in arena
                        event.setCancelled(true);

                        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            ItemStack stack = event.getItem();
                            if (stack != null && stack.getAmount() > 0 && stack.getType().isBlock()) {
                                BlockFace face = event.getBlockFace();
                                Block block = event.getClickedBlock().getLocation().clone().add(MiscUtils.getDirection(face))
                                        .getBlock();
                                if (block.getType() == Material.AIR) {
                                    BlockState originalState = block.getState();
                                    block.setType(stack.getType());
                                    try {
                                        // The method is no longer in API, but in legacy versions exists
                                        Block.class.getMethod("setData", byte.class).invoke(block,
                                                (byte) stack.getDurability());
                                    } catch (Exception e) {
                                    }
                                    BlockPlaceEvent bevent = new BlockPlaceEvent(block, originalState,
                                            event.getClickedBlock(), stack, player, true);
                                    Bukkit.getPluginManager().callEvent(bevent);

                                    if (bevent.isCancelled()) {
                                        originalState.update(true, false);
                                    } else {
                                        stack.setAmount(stack.getAmount() - 1);
                                        // TODO get right block place sound
                                        Sounds.BLOCK_STONE_PLACE.playSound(player, block.getLocation(), 1, 1);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK &&
                    game.getStatus() == GameStatus.RUNNING && !gPlayer.isSpectator
                    && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DRAGON_EGG
                    && Main.getConfigurator().config.getBoolean("disableDragonEggTeleport", true)) {
                event.setCancelled(true);
                BlockBreakEvent blockBreakEvent = new BlockBreakEvent(event.getClickedBlock(), player);
                Bukkit.getPluginManager().callEvent(blockBreakEvent);
                if (blockBreakEvent.isCancelled()) {
                    return;
                }
                if (blockBreakEvent.isDropItems()) {
                    event.getClickedBlock().breakNaturally();
                } else {
                    event.getClickedBlock().setType(Material.AIR);
                }
            }
        }
    }
}
