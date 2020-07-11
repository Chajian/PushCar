package com.ibs.oldman.pushcar.listener;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.game.*;
import com.ibs.oldman.pushcar.inventory.TeamSelectorInventory;
import com.ibs.oldman.pushcar.lib.nms.entity.PlayerUtils;
import com.ibs.oldman.pushcar.lib.nms.util.MiscUtils;
import com.ibs.oldman.pushcar.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.screamingsandals.simpleinventories.utils.StackParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static lang.I18n.i18n;
import static lang.I18n.i18nonly;
import static com.ibs.oldman.pushcar.command.BaseCommands.ADMIN_PERMISSION;

/**
 * 监听玩家
 */
public class PlayerEvent implements Listener {


    /**
     * 玩家破坏时调用
     * @param event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (Main.isPlayerInGame(event.getPlayer())) {
            final Player player = event.getPlayer();
            final GamePlayer gamePlayer = Main.getPlayerGameProfile(player);
            final Game game = gamePlayer.getGame();
            final Block block = event.getBlock();

            if (game.getStatus() == GameStatus.WAITING) {
                event.setCancelled(true);
                return;
            }

            if (!game.blockBreak(Main.getPlayerGameProfile(event.getPlayer()), event.getBlock(), event)) {
                event.setCancelled(true);
            }

            //Fix for obsidian dropping
            if (game.getStatus() == GameStatus.RUNNING && gamePlayer.isInGame()) {
                if (block.getType() == Material.ENDER_CHEST) {
                    event.setDropItems(false);
                }
            }
        } else if (Main.getConfigurator().config.getBoolean("preventArenaFromGriefing",false)) {
            for (String gameN : Main.getGameNames()) {
                Game game = Main.getGame(gameN);
                if (game.getStatus() != GameStatus.DISABLED && GameCreator.isInArea(event.getBlock().getLocation(), game.getPoint1(), game.getPoint2())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player) || event.isCancelled()) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (Main.isPlayerInGame(player)) {
            GamePlayer gPlayer = Main.getPlayerGameProfile(player);
            Game game = gPlayer.getGame();
            if (game.getStatus() == GameStatus.WAITING || gPlayer.isSpectator) {
                event.setCancelled(true);
            }

            if (game.getStatus() == GameStatus.RUNNING && Main.getConfigurator().config.getBoolean("disable-hunger",true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            if (event instanceof EntityDamageByEntityEvent) {
                Game game = Main.getInGameEntity(event.getEntity());
                if (game != null) {
//                    if (game.isEntityShop(event.getEntity()) && game.getOriginalOrInheritedPreventKillingVillagers()) {
//                        event.setCancelled(true);
//                    }
                }

                if (event.getEntity() instanceof ArmorStand) {
                    Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                    if (damager instanceof Player) {
                        Player player = (Player) damager;
                        if (Main.isPlayerInGame(player)) {
                            GamePlayer gPlayer = Main.getPlayerGameProfile(player);
                            if (gPlayer.getGame().getStatus() == GameStatus.WAITING || gPlayer.isSpectator) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }

            return;
        }

        Player player = (Player) event.getEntity();
        if (Main.isPlayerInGame(player)) {
            GamePlayer gPlayer = Main.getPlayerGameProfile(player);
            Game game = gPlayer.getGame();
            if (gPlayer.isSpectator) {
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    gPlayer.teleport(game.getSpectatorSpawn());
                }
                event.setCancelled(true);
            } else if (game.getStatus() == GameStatus.WAITING) {
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    gPlayer.teleport(game.getLobbySpawn());
                }
                event.setCancelled(true);
            } else if (game.getStatus() == GameStatus.RUNNING) {
                if (gPlayer.isSpectator) {
                    event.setCancelled(true);
                }
                if (game.isProtectionActive(player) && event.getCause() != EntityDamageEvent.DamageCause.VOID) {
                    event.setCancelled(true);
                    return;
                }

                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    player.setHealth(0.5);
                }

            } else if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) event;
                if (edbee.getDamager() instanceof Player) {
                    Player damager = (Player) edbee.getDamager();
                    if (Main.isPlayerInGame(damager)) {
                        if (Main.getPlayerGameProfile(damager).isSpectator) {
                            event.setCancelled(true);
                        }
                    }
                } else if (edbee.getDamager() instanceof Firework) {
                    if (Main.isPlayerInGame(player)) {
                        event.setCancelled(true);
                    }
                } else if (edbee.getDamager() instanceof TNTPrimed) {
                    if (edbee.getDamager().hasMetadata(player.getUniqueId().toString())) {
                        edbee.setCancelled(Main.getConfigurator().config.getBoolean("tnt.dont-damage-placer", false));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        if (Main.isPlayerInGame(event.getPlayer())) {
            Game game = Main.getPlayerGameProfile(event.getPlayer()).getGame();
            if (game.getStatus() == GameStatus.WAITING) {
                event.setCancelled(true);
                return;
            }
            if (!game.blockPlace(Main.getPlayerGameProfile(event.getPlayer()), event.getBlock(),
                    event.getBlockReplacedState(), event.getItemInHand())) {
                event.setCancelled(true);
            }

            if (game.getStatus() == GameStatus.RUNNING
                    && GameCreator.isInArea(event.getBlock().getLocation(), game.getPoint1(), game.getPoint2())) {
                Block block = event.getBlock();
                int explosionTime = Main.getConfigurator().config.getInt("tnt.explosion-time", 8) * 20;

                if (block.getType() == Material.TNT
                        && Main.getConfigurator().config.getBoolean("tnt.auto-ignite", false)) {
                    block.setType(Material.AIR);
                    Location location = block.getLocation().add(0.5, 0.5, 0.5);
                    ;

                    TNTPrimed tnt = (TNTPrimed) location.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(explosionTime);

                    tnt.setMetadata(event.getPlayer().getUniqueId().toString(), new FixedMetadataValue(Main.getMain(), null));

                    Main.registerGameEntity(tnt, game);

                    new BukkitRunnable() {
                        public void run() {
                            Main.unregisterGameEntity(tnt);
                        }
                    }.runTaskLater(Main.getMain(), explosionTime + 10);
                }
            }
        } else if (Main.getConfigurator().config.getBoolean("preventArenaFromGriefing")) {
            for (String gameN : Main.getGameNames()) {
                Game game = Main.getGame(gameN);
                if (game.getStatus() != GameStatus.DISABLED && GameCreator.isInArea(event.getBlock().getLocation(), game.getPoint1(), game.getPoint2())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (Main.isPlayerInGame(event.getPlayer())) {
            GamePlayer gPlayer = Main.getPlayerGameProfile(event.getPlayer());
            Game game = gPlayer.getGame();
            CurrentTeam team = game.getPlayerTeam(gPlayer);

            if (game.getStatus() == GameStatus.WAITING) {
                event.setRespawnLocation(gPlayer.getGame().getLobbySpawn());
                return;
            }
            if (gPlayer.isSpectator) {
                if (team == null) {
                    event.setRespawnLocation(gPlayer.getGame().makeSpectator(gPlayer, true));
                } else {
                    event.setRespawnLocation(gPlayer.getGame().makeSpectator(gPlayer, false));
                }
            } else {
                event.setRespawnLocation(gPlayer.getGame().getPlayerTeam(gPlayer).teamInfo.spawn);

                if (Main.getConfigurator().config.getBoolean("respawn.protection-enabled", true)) {
                    RespawnProtection respawnProtection = game.addProtectedPlayer(gPlayer.player);
                    respawnProtection.runProtection();
                }

                SpawnEffects.spawnEffect(gPlayer.getGame(), gPlayer.player, "game-effects.respawn");
//                if (gPlayer.getGame().getOriginalOrInheritedPlayerRespawnItems()) {
//                    List<ItemStack> givedGameStartItems = StackParser.parseAll((Collection<Object>) Main.getConfigurator().config
//                            .getList("gived-player-respawn-items"));
//                    if (givedGameStartItems != null) {
//                        MiscUtils.giveItemsToPlayer(givedGameStartItems, gPlayer.player, team.getColor());
//                    } else {
//                        Debug.warn("You have wrongly configured gived-player-respawn-items!", true);
//                    }
//                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player victim = event.getEntity();

        if (Main.isPlayerInGame(victim)) {
            GamePlayer gVictim = Main.getPlayerGameProfile(victim);
            Game game = gVictim.getGame();
            CurrentTeam victimTeam = game.getPlayerTeam(gVictim);
            ChatColor victimColor = victimTeam.teamInfo.teamColor.chatColor;
            List<ItemStack> drops = new ArrayList<>(event.getDrops());
            int respawnTime = Main.getConfigurator().config.getInt("respawn-cooldown.time", 5);

            event.setKeepInventory(false);
            event.setDroppedExp(victim.getLevel()/2);


            if (game.getStatus() == GameStatus.RUNNING) {
//                if (!game.getOriginalOrInheritedPlayerDrops()) {
//                    event.getDrops().clear();
//                }
                //处理游戏消息
                if (Main.getConfigurator().config.getBoolean("chat.send-death-messages-just-in-game")) {
                    String deathMessage = event.getDeathMessage();
                    if (Main.getConfigurator().config.getBoolean("chat.send-custom-death-messages")) {
                        if (event.getEntity().getKiller() != null) {
                            Player killer = event.getEntity().getKiller();
                            GamePlayer gKiller = Main.getPlayerGameProfile(killer);
                            CurrentTeam killerTeam = game.getPlayerTeam(gKiller);
                            ChatColor killerColor = killerTeam.teamInfo.teamColor.chatColor;

                            deathMessage = i18n("player_killed")
                                    .replace("%victim%", victimColor + victim.getDisplayName())
                                    .replace("%killer%", killerColor + killer.getDisplayName())
                                    .replace("%victimTeam%", victimColor + victimTeam.getName())
                                    .replace("%killerTeam%", killerColor + killerTeam.getName());
                        } else {
                            deathMessage = i18n("player_self_killed")
                                    .replace("%victim%", victimColor + victim.getDisplayName())
                                    .replace("%victimTeam%", victimColor + victimTeam.getName());
                        }

                    }
                    if (deathMessage != null) {
                        event.setDeathMessage(null);
                        for (Player player : game.getConnectedPlayers()) {
                            player.sendMessage(deathMessage);
                        }
                    }
                }

                CurrentTeam team = game.getPlayerTeam(gVictim);
                SpawnEffects.spawnEffect(game, victim, "game-effects.kill");
//                if (!team.isBed) {
//                    gVictim.isSpectator = true;
//                    team.players.remove(gVictim);
//                    team.getScoreboardTeam().removeEntry(victim.getName());
//                    if (Main.isPlayerStatisticsEnabled()) {
//                        PlayerStatistic statistic = Main.getPlayerStatisticsManager().getStatistic(victim);
//                        statistic.setCurrentLoses(statistic.getCurrentLoses() + 1);
//                        statistic.setCurrentScore(statistic.getCurrentScore()
//                                + Main.getConfigurator().config.getInt("statistics.scores.lose", 0));
//
//                    }
//                    game.updateScoreboard();
//                }

                boolean onlyOnBedDestroy = Main.getConfigurator().config.getBoolean("statistics.bed-destroyed-kills",
                        false);

                Player killer = victim.getKiller();
                if (Main.isPlayerInGame(killer)) {
                    GamePlayer gKiller = Main.getPlayerGameProfile(killer);
                    if (gKiller.getGame() == game) {
//                        if (!onlyOnBedDestroy ) {
//                            game.dispatchRewardCommands("player-kill", killer,
//                                    Main.getConfigurator().config.getInt("statistics.scores.kill", 10));
//                        }
//                        if (team.isDead()) {
//                            SpawnEffects.spawnEffect(game, victim, "game-effects.teamkill");
//                            Sounds.playSound(killer, killer.getLocation(),
//                                    Main.getConfigurator().config.getString("sounds.on_team_kill"),
//                                    Sounds.ENTITY_PLAYER_LEVELUP, 1, 1);
//                        } else {
                            Sounds.playSound(killer, killer.getLocation(),
                                    Main.getConfigurator().config.getString("sounds.on_player_kill"),
                                    Sounds.ENTITY_PLAYER_BIG_FALL, 1, 1);
//                            Main.depositPlayer(killer, Main.getVaultKillReward());
//                        }

                    }
                }
                //击杀者获取死亡者一半的等级
                if(killer!=null)
                    killer.setLevel(killer.getLevel()+victim.getLevel()/2);

//                BedwarsPlayerKilledEvent killedEvent = new BedwarsPlayerKilledEvent(game, victim,
//                        Main.isPlayerInGame(killer) ? killer : null, drops);
//                Main.getInstance().getServer().getPluginManager().callEvent(killedEvent);

//                if (Main.isPlayerStatisticsEnabled()) {//开启玩家统计
//                    PlayerStatistic diePlayer = Main.getPlayerStatisticsManager().getStatistic(victim);
//                    PlayerStatistic killerPlayer;
//
//                    boolean teamIsDead = !team.isBed;
//
//                    if (!onlyOnBedDestroy || teamIsDead) {
//                        diePlayer.setCurrentDeaths(diePlayer.getCurrentDeaths() + 1);
//                        diePlayer.setCurrentScore(diePlayer.getCurrentScore()
//                                + Main.getConfigurator().config.getInt("statistics.scores.die", 0));
//                    }
//
//                    if (killer != null) {
//                        if (!onlyOnBedDestroy || teamIsDead) {
//                            killerPlayer = Main.getPlayerStatisticsManager().getStatistic(killer);
//                            if (killerPlayer != null) {
//                                killerPlayer.setCurrentKills(killerPlayer.getCurrentKills() + 1);
//                                killerPlayer.setCurrentScore(killerPlayer.getCurrentScore()
//                                        + Main.getConfigurator().config.getInt("statistics.scores.kill", 10));
//                            }
//                        }
//                    }
//                }
            }
            if (Main.getVersionNumber() < 115) {
                PlayerUtils.respawn(Main.getMain(), victim, 3L);
            }

            //重生冷却
            if (Main.getConfigurator().config.getBoolean("respawn-cooldown.enabled")
                    && victimTeam.isAlive()
                    && !gVictim.isSpectator) {
                game.makeSpectator(gVictim, false);

                new BukkitRunnable() {
                    int livingTime = respawnTime;
                    GamePlayer gamePlayer = gVictim;
                    Player player = gamePlayer.player;

                    @Override
                    public void run() {
                        if (livingTime > 0) {
                            Title.send(player,
                                    i18nonly("respawn_cooldown_title").replace("%time%", String.valueOf(livingTime)), "");
                            Sounds.playSound(player, player.getLocation(),
                                    Main.getConfigurator().config.getString("sounds.on_respawn_cooldown_wait"),
                                    Sounds.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
                        }

                        livingTime--;
                        if (livingTime == 0) {
                            game.makePlayerFromSpectator(gamePlayer);
                            Sounds.playSound(player, player.getLocation(),
                                    Main.getConfigurator().config.getString("sounds.on_respawn_cooldown_done"),
                                    Sounds.UI_BUTTON_CLICK, 1, 1);

                            this.cancel();
                        }
                    }
                }.runTaskTimer(Main.getMain(), 20L, 20L);
            }
        }
    }

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
