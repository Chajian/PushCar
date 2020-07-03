package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.boss.BossBar;
import com.ibs.oldman.pushcar.api.boss.BossBar19;
import com.ibs.oldman.pushcar.api.boss.StatusBar;
import com.ibs.oldman.pushcar.api.game.ArenaTime;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.api.game.GameStore;
import com.ibs.oldman.pushcar.api.game.RunningTeam;
import static lang.I.*;

import com.ibs.oldman.pushcar.api.spawner.ItemSpawner;
import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.ibs.oldman.pushcar.inventory.TeamSelectorInventory;
import com.ibs.oldman.pushcar.lib.nms.util.MiscUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game implements com.ibs.oldman.pushcar.api.game.Game {
    /*团队信息Map*/
    private HashMap<String, Team> teams = new HashMap<>();
    /*玩家列表*/
    private List<GamePlayer> players = new ArrayList<>();
    /*商店*/
    private List<GameStore> stores = new ArrayList<>();
    /*当前团队列表*/
    private List<CurrentTeam> currentTeams = new ArrayList<>();
    /*游戏名称*/
    private String game_name;
    private GameStatus gameStatus,afterRebuild,previousStatus=GameStatus.DISABLED;
//    private GameStatus previousStatus = GameStatus.DISABLED;//之前状态
    /*竞技场对角线位置*/
    private Location point1,point2;
    /*观众的坐标*/
    private Location spectator;
    /*大厅位置*/
    private Location lobbySpawn;
    /*暂停倒计时*/
    private int pauseCountdown = -1;
    /*倒计时*/
    private int countdown = -1;
    /*倒计时之前*/
    private int previousCountdown = -1;
    /*团队选择窗口*/
    private TeamSelectorInventory teamSelectorInventory;
    private static Main main = null;
    private int gameTime;
    private int minPlayers;
    private int maxplayers;
    private Region region = new Region();
    private World world;
    private ArenaTime arenaTime;
    private StatusBar statusbar;//状态栏
    private WeatherType weatherType;
    /*大厅提示栏颜色*/
    private BarColor lobby_color;
    /*竞技场提示栏颜色*/
    private BarColor game_color;

    /*处理run（）的任务*/
    private BukkitTask task;

    @Override
    public String getName() {
        return game_name;
    }

    @Override
    public GameStatus getStatus() {
        return gameStatus;
    }

    @Override
    public void start() {
        if (gameStatus == GameStatus.DISABLED) {
            gameStatus = GameStatus.WAITING;
            countdown = -1;
            maxplayers = 0;
            for (Team team : teams.values()) {
                maxplayers += team.maxplayers;
            }
//            new BukkitRunnable() {
//                public void run() {
////                    updateSigns();
//                }
//            }.runTask(Main.getMain());

//            if (Main.getConfigurator().config.getBoolean("bossbar.use-xp-bar", false)) {
//                statusbar = new XPB();
//            } else {
//                statusbar = BossBarSelector.getBossBar();
//            }
        }
    }

    @Override
    public void stop() {
        if (gameStatus == GameStatus.DISABLED) {
            return; // Game is already stopped
        }
        List<GamePlayer> clonedPlayers = (List<GamePlayer>) ((ArrayList<GamePlayer>) players).clone();
        for (GamePlayer p : clonedPlayers)
            p.changeGame(null);
        if (gameStatus != GameStatus.REBUILDING) {
            gameStatus = GameStatus.DISABLED;
//            updateSigns();
        } else {
            afterRebuild = GameStatus.DISABLED;
        }
    }

    /*玩家请求加入游戏*/
    @Override
    public void joinToGame(Player player) {
        if(gameStatus==GameStatus.WAITING){
            GamePlayer gamePlayer = main.getPlayerGameProfile(player);
            gamePlayer.changeGame(this);
        }
    }

    @Override
    public void leaveFromGame(Player player) {
        if(gameStatus==GameStatus.DISABLED)
            return;
        GamePlayer gamePlayer = main.getPlayerGameProfile(player);
        gamePlayer.changeGame(null);
        updateScorebroad();
    }

    @Override
    public void selectPlayerTeam(Player player, com.ibs.oldman.pushcar.api.game.Team team) {
        if (!Main.isPlayerInGame(player)) {
            return;
        }
        GamePlayer profile = Main.getPlayerGameProfile(player);
        if (profile.getGame() != this) {
            return;
        }

        selectTeam(profile, team.getName());
    }


    @Override
    public void selectPlayerRandomTeam(Player player) {
        joinRandomTeam(Main.getPlayerGameProfile(player));
    }

    @Override
    public World getGameWorld() {
        return world;
    }

    @Override
    public Location getPoint1() {
        return point1;
    }

    @Override
    public Location getPoint2() {
        return point2;
    }

    @Override
    public Location getSpectatorSpawn() {
        return spectator;
    }

    @Override
    public int getGameTime() {
        return gameTime;
    }

    @Override
    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public int getMaxPlayers() {
        return maxplayers;
    }

    @Override
    public int countConnectedPlayers() {
        return players.size();
    }

    @Override
    public List<Player> getConnectedPlayers() {
        List<Player> connected_player = new ArrayList<>();
        for(GamePlayer gamePlayer:players){
            Player player = gamePlayer.player;
            connected_player.add(player);
        }
        return connected_player;
    }

    @Override
    public List<GameStore> getGameStores() {
        return stores;
    }

    @Override
    public int countGameStores() {
        return stores.size();
    }

    @Override
    public Team getTeamFromName(String name) {
        for(Team team:teams.values())
            if(team.getName().equals(name))
                return team;
        return null;
    }

    @Override
    public List<com.ibs.oldman.pushcar.api.game.Team> getAvailableTeams() {
        return new ArrayList<>(teams.values());
    }


    @Override
    public int countAvailableTeams() {
        return teams.size();
    }

    @Override
    public List<RunningTeam> getRunningTeams() {
        return new ArrayList<>(currentTeams);
    }

    @Override
    public int countRunningTeams() {
        return currentTeams.size();
    }

    /*获取玩家的团队对象*/
    @Override
    public RunningTeam getTeamOfPlayer(Player player) {
        for(CurrentTeam currentTeam:currentTeams)
            if(currentTeam.isPlayerInTeam(player))
                return currentTeam;
        return null;
    }

    /*判断玩家是否在团队中*/
    @Override
    public boolean isPlayerInAnyTeam(Player player) {
        for(RunningTeam runningTeam:currentTeams)
            if(runningTeam.isPlayerInTeam(player))
                return true;
        return false;
    }

    public CurrentTeam getPlayerTeam(GamePlayer player) {
        for (CurrentTeam team : currentTeams) {
            if (team.players.contains(player)) {
                return team;
            }
        }
        return null;
    }

    @Override
    public boolean isPlayerInTeam(Player player, RunningTeam team) {
        return getTeamOfPlayer(player) == team;
    }

    /*坐标是否在竞技场内*/
    @Override
    public boolean isLocationInArena(Location location) {
        return isInArea(location,point1,point2);
    }

    /**
     * 位置是否在竞技场内
     * @param l 目标坐标
     * @param p1 竞技场点1
     * @param p2 竞技场点2
     * @return 如果在竞技场内返回true，否则返回false
     */
    public static boolean isInArea(Location l, Location p1, Location p2) {
        if (!p1.getWorld().equals(l.getWorld())) {
            return false;
        }

        Location min = new Location(p1.getWorld(), Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ()));
        Location max = new Location(p1.getWorld(), Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ()));
        return (min.getX() <= l.getX() && min.getY() <= l.getY() && min.getZ() <= l.getZ() && max.getX() >= l.getX()
                && max.getY() >= l.getY() && max.getZ() >= l.getZ());
    }

    /**
     * 方块是否在竞技场内
     * @param l 目标方块
     * @param p1 竞技场点1
     * @param p2 竞技场点2
     * @return 如果在竞技场内返回true，否则返回false
     */
    public static boolean isChunkInArea(Chunk l, Location p1, Location p2) {
        if (!p1.getWorld().equals(l.getWorld())) {
            return false;
        }

        Chunk min = new Location(p1.getWorld(), Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ())).getChunk();
        Chunk max = new Location(p1.getWorld(), Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ())).getChunk();
        return (min.getX() <= l.getX() && min.getZ() <= l.getZ() && max.getX() >= l.getX() && max.getZ() >= l.getZ());
    }

    /*更新记分板*/
    public void updateScorebroad(){

    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public void setArenaTime(ArenaTime arenaTime) {
        this.arenaTime = arenaTime;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxplayers() {
        return maxplayers;
    }

    public World getWorld() {
        return world;
    }

    public String getGame_name() {
        return game_name;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public void setPoint1(Location point1) {
        this.point1 = point1;
    }

    public void setPoint2(Location point2) {
        this.point2 = point2;
    }

    public Location getSpectator() {
        return spectator;
    }

    public void setSpectator(Location spectator) {
        this.spectator = spectator;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public void setMaxplayers(int maxplayers) {
        this.maxplayers = maxplayers;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public ArenaTime getArenaTime() {
        return arenaTime;
    }

    /**
     * 初始化竞技场
     * @param file 竞技场文件
     * @param firstAttempt 第一次尝试
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Game loadGame(File file, boolean firstAttempt) {
        if (!file.exists()) {
            return null;
        }
        FileConfiguration configMap = new YamlConfiguration();
        try {
            configMap.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }

        Game game = new Game();
        game.game_name = configMap.getString("name");
        game.pauseCountdown = configMap.getInt("pauseCountdown");
        game.gameTime = configMap.getInt("gameTime");
        String worldName = configMap.getString("world");
        game.world = Bukkit.getWorld(worldName);
        //加载世界
        if(game.world!=null){
            if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
                Bukkit.getConsoleSender().sendMessage("§c[B§fW] §cWorld " + worldName
                        + " was not found, but we found Multiverse-Core, so we will try to load this world.");

                Core multiverse = (Core) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
                if (multiverse != null) {
                    MVWorldManager manager = multiverse.getMVWorldManager();
                    if (manager.loadWorld(worldName)) {
                        Bukkit.getConsoleSender().sendMessage("§c[B§fW] §aWorld " + worldName
                                + " was succesfully loaded with Multiverse-Core, continue in arena loading.");

                        game.world = Bukkit.getWorld(worldName);
                    }
                } else {
                    Bukkit.getConsoleSender().sendMessage("§c[B§fW] §cArena " + game.game_name
                            + " can't be loaded, because world " + worldName + " is missing!");
                    return null;
                }
            } else if (firstAttempt) {
                Bukkit.getConsoleSender().sendMessage(
                        "§c[B§fW] §eArena " + game.game_name + " can't be loaded, because world " + worldName + " is missing! We will try it again after all plugins will be loaded!");
                Bukkit.getScheduler().runTaskLater(Main.getMain(), () -> loadGame(file, false), 10L);
                return null;
            } else {
                Bukkit.getConsoleSender().sendMessage(
                        "§c[B§fW] §cArena " + game.game_name + " can't be loaded, because world " + worldName + " is missing!");
                return null;
            }
        }
        //设置世界规则
        if (Main.getVersionNumber() >= 115) {
            game.world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        }
        game.point1 = MiscUtils.readLocationFromString(game.world, configMap.getString("pos1"));
        game.point2 = MiscUtils.readLocationFromString(game.world, configMap.getString("pos2"));
        game.spectator = MiscUtils.readLocationFromString(game.world, configMap.getString("specSpawn"));
        String spawnWorld = configMap.getString("lobbySpawnWorld");
        World lobbySpawnWorld = Bukkit.getWorld(spawnWorld);
        //加载大厅世界
        if (lobbySpawnWorld == null) {
            if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
                Bukkit.getConsoleSender().sendMessage("§c[B§fW] §cWorld " + spawnWorld
                        + " was not found, but we found Multiverse-Core, so we will try to load this world.");

                Core multiverse = (Core) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
                MVWorldManager manager = multiverse.getMVWorldManager();
                if (manager.loadWorld(spawnWorld)) {
                    Bukkit.getConsoleSender().sendMessage("§c[B§fW] §aWorld " + spawnWorld
                            + " was succesfully loaded with Multiverse-Core, continue in arena loading.");

                    lobbySpawnWorld = Bukkit.getWorld(spawnWorld);
                } else {
                    Bukkit.getConsoleSender().sendMessage("§c[B§fW] §cArena " + game.game_name
                            + " can't be loaded, because world " + spawnWorld + " is missing!");
                    return null;
                }
            } else if (firstAttempt) {
                Bukkit.getConsoleSender().sendMessage(
                        "§c[B§fW] §eArena " + game.game_name + " can't be loaded, because world " + worldName + " is missing! We will try it again after all plugins will be loaded!");
                Bukkit.getScheduler().runTaskLater(Main.getMain(), () -> loadGame(file, false), 10L);
                return null;
            } else {
                Bukkit.getConsoleSender().sendMessage(
                        "§c[B§fW] §cArena " + game.game_name + " can't be loaded, because world " + spawnWorld + " is missing!");
                return null;
            }
        }
        game.lobbySpawn = MiscUtils.readLocationFromString(lobbySpawnWorld, configMap.getString("lobbySpawn"));
        game.minPlayers = configMap.getInt("minPlayers", 2);

        //读取队伍信息
        if (configMap.isSet("teams")) {
            for (String teamN : configMap.getConfigurationSection("teams").getKeys(false)) {//读取队伍信息
                ConfigurationSection team = configMap.getConfigurationSection("teams").getConfigurationSection(teamN);
                Team t = new Team();
//                t.newColor = team.getBoolean("isNewColor", false);
                t.teamColor = TeamColor.valueOf(MiscUtils.convertColorToNewFormat(team.getString("color"), t));
                t.name = teamN;
                t.bed = MiscUtils.readLocationFromString(game.world, team.getString("bed"));
                t.maxplayers = team.getInt("maxPlayers");
                t.spawn = MiscUtils.readLocationFromString(game.world, team.getString("spawn"));
                t.game = game;

//                t.newColor = true;
                game.teams.put(t.name,t);
            }
        }
        //读取物品生成器信息
//        if (configMap.isSet("spawners")) {
//            List<Map<String, Object>> spawners = (List<Map<String, Object>>) configMap.getList("spawners");
//            for (Map<String, Object> spawner : spawners) {
//                ItemSpawner sa = new ItemSpawner(
//                        MiscUtils.readLocationFromString(game.world, (String) spawner.get("location")),
//                        Main.getSpawnerType(((String) spawner.get("type")).toLowerCase()),
//                        (String) spawner.get("customName"), ((Boolean) spawner.getOrDefault("hologramEnabled", true)),
//                        ((Number) spawner.getOrDefault("startLevel", 1)).doubleValue(),//每次生成的数量
//                        game.getTeamFromName((String) spawner.get("team")),//队伍专属
//                        (int) spawner.getOrDefault("maxSpawnedResources", -1));//遗留未捡的资源最多有多少
//                game.spawners.add(sa);
//            }
//        }
        //读取商店
        if (configMap.isSet("stores")) {
            List<Object> stores = (List<Object>) configMap.getList("stores");
            for (Object store : stores) {
                if (store instanceof Map) {
                    Map<String, String> map = (Map<String, String>) store;
                    game.stores.add(new GameStore(MiscUtils.readLocationFromString(game.world, map.get("loc")),
                            map.get("shop"), "true".equals(map.getOrDefault("parent", "true")),
                            EntityType.valueOf(map.getOrDefault("type", "VILLAGER").toUpperCase()),
                            map.getOrDefault("name", ""), map.containsKey("name")));
                } else if (store instanceof String) {
                    game.stores.add(new GameStore(MiscUtils.readLocationFromString(game.world, (String) store), null,
                            true, EntityType.VILLAGER, "", false));
                }
            }
        }
        game.arenaTime = ArenaTime.valueOf(configMap.getString("arenaTime", ArenaTime.WORLD.name()).toUpperCase());
//        try {
//            game.lobbyBossBarColor = loadBossBarColor(
//                    configMap.getString("lobbyBossBarColor", "default").toUpperCase());
//            game.gameBossBarColor = loadBossBarColor(configMap.getString("gameBossBarColor", "default").toUpperCase());
//        } catch (Throwable t) {
//            // We're using 1.8
//        }
        Main.addGame(game);
        game.start();
        Bukkit.getConsoleSender().sendMessage("§c[B§fW] §aArena §f" + game.game_name + "§a loaded!");
        return game;
    }

    public static boolean isBungeeEnabled() {
        return Main.getConfigurator().config.getBoolean("bungee.enabled");
    }

    /*玩家离开游戏*/
    public void internalLeavePlayer(GamePlayer gamePlayer) {
        if (gameStatus == GameStatus.DISABLED) {
            return;
        }

        players.remove(gamePlayer);

        statusbar.removePlayer(gamePlayer.player);

        //传送到主大厅
        gamePlayer.player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        if (Main.getConfigurator().config.getBoolean("mainlobby.enabled")
                && !Main.getConfigurator().config.getBoolean("bungee.enabled")) {
            Location mainLobbyLocation = MiscUtils.readLocationFromString(
                    Bukkit.getWorld(Main.getConfigurator().config.getString("mainlobby.world")),
                    Main.getConfigurator().config.getString("mainlobby.location"));
            gamePlayer.teleport(mainLobbyLocation);
        }

        //将玩家从游戏队伍中退出
        if (gameStatus == GameStatus.RUNNING || gameStatus == GameStatus.WAITING) {
            CurrentTeam team = getPlayerTeam(gamePlayer);
            if (team != null) {
                team.players.remove(gamePlayer);
                if (gameStatus == GameStatus.WAITING) {
                    team.getScoreboardTeam().removeEntry(gamePlayer.player.getName());
                    if (team.players.isEmpty()) {
                        currentTeams.remove(team);
                        team.getScoreboardTeam().unregister();
                    }
                } else {
                    updateScorebroad();
                }
            }
        }

        //预留一个开启玩家数据统计的方法


        if (players.isEmpty()) {
//            if (!preServerRestart) {
//                BedWarsPlayerLastLeaveEvent playerLastLeaveEvent = new BedWarsPlayerLastLeaveEvent(this, gamePlayer.player,
//                        getPlayerTeam(gamePlayer));
//                Main.getInstance().getServer().getPluginManager().callEvent(playerLastLeaveEvent);
//            }

            if (gameStatus != GameStatus.WAITING) {
                afterRebuild = GameStatus.WAITING;
//                updateSigns();
                rebuild();
            } else {
                gameStatus = GameStatus.WAITING;
                cancelTask();
            }
//            countdown = -1;
//            if (gameScoreboard.getObjective("display") != null) {
//                gameScoreboard.getObjective("display").unregister();
//            }
//            if (gameScoreboard.getObjective("lobby") != null) {
//                gameScoreboard.getObjective("lobby").unregister();
//            }
//            gameScoreboard.clearSlot(DisplaySlot.SIDEBAR);
//            for (CurrentTeam team : teamsInGame) {
//                team.getScoreboardTeam().unregister();
//            }
//            teamsInGame.clear();
//            for (GameStore store : gameStore) {
//                LivingEntity villager = store.kill();
//                if (villager != null) {
//                    Main.unregisterGameEntity(villager);
//                }
//            }
        }

    }

    /*重构*/
    public void rebuild() {
        currentTeams.clear();
//        activeSpecialItems.clear();
//        activeDelays.clear();

//        BedwarsPreRebuildingEvent preRebuildingEvent = new BedwarsPreRebuildingEvent(this);
//        Main.getInstance().getServer().getPluginManager().callEvent(preRebuildingEvent);

        //清理生成的资源
//        for (ItemSpawner spawner : spawners) {
//            spawner.currentLevel = spawner.startLevel;
//            spawner.spawnedItems.clear();
//        }
        //杀死商人，并且注销
        for (GameStore store : stores) {
            LivingEntity villager = store.kill();
            if (villager != null) {
                Main.unregisterGameEntity(villager);
            }
        }
        //恢复游戏开始之前的建筑
        region.regen();
        // Remove items
        //删除方块
        for (Entity e : this.world.getEntities()) {
            if (isInArea(e.getLocation(),point1,point2)) {
                if (e instanceof Item) {
                    Chunk chunk = e.getLocation().getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }
                    e.remove();
                }
            }
        }

        // Chest clearing
        //清理箱子的物品
//        for (Map.Entry<Location, ItemStack[]> entry : usedChests.entrySet()) {
//            Location location = entry.getKey();
//            Chunk chunk = location.getChunk();
//            if (!chunk.isLoaded()) {
//                chunk.load();
//            }
//            Block block = location.getBlock();
//            ItemStack[] contents = entry.getValue();
//            if (block.getState() instanceof InventoryHolder) {
//                InventoryHolder chest = (InventoryHolder) block.getState();
//                chest.getInventory().setContents(contents);
//            }
//        }
//        usedChests.clear();

        // Clear fake ender chests
        //清理末地箱子
//        for (Inventory inv : fakeEnderChests.values()) {
//            inv.clear();
//        }
//        fakeEnderChests.clear();

        // Remove remaining entities registered by other plugins
        //清理其他插件注册的实体
        for (Entity entity : Main.getGameEntities(this)) {
            Chunk chunk = entity.getLocation().getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
            }
            entity.remove();
            Main.unregisterGameEntity(entity);
        }

        // Holograms destroy
        //清理全息
//        for (Hologram holo : createdHolograms) {
//            holo.destroy();
//        }
//        createdHolograms.clear();
//        countdownHolograms.clear();

//        UpgradeRegistry.clearAll(this);

//        BedwarsPostRebuildingEvent postRebuildingEvent = new BedwarsPostRebuildingEvent(this);
//        Main.getInstance().getServer().getPluginManager().callEvent(postRebuildingEvent);

        this.gameStatus = this.afterRebuild;
        this.countdown = -1;
//        updateSigns();
        cancelTask();

    }

    public List<GamePlayer> getPlayersInTeam(Team team) {
        CurrentTeam currentTeam = null;
        for (CurrentTeam cTeam : currentTeams) {
            if (cTeam.teamInfo == team) {
                currentTeam = cTeam;
            }
        }

        if (currentTeam != null) {
            return currentTeam.players;
        } else {
            return new ArrayList<>();
        }
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    public void setPauseCountdown(int pauseCountdown) {
        this.pauseCountdown = pauseCountdown;
    }

    public static Game createGame(String name) {
        Game game = new Game();
        game.game_name = name;
        game.pauseCountdown = 60;
        game.gameTime = 3600;
        game.minPlayers = 2;

        return game;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public void setPreviousCountdown(int previousCountdown) {
        this.previousCountdown = previousCountdown;
    }

    public void setPreviousStatus(GameStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    /*玩家加入游戏*/
    public void internalJoinPlayer(GamePlayer gamePlayer){
        if(!gamePlayer.player.isEmpty()){
            if(!players.contains(gamePlayer)){
                players.add(gamePlayer);
            }

            //预留一个统计玩家的方法
            if (arenaTime.time >= 0) {
                gamePlayer.player.setPlayerTime(arenaTime.time, false);
            }

            //设置玩家快捷键按钮
            final BukkitRunnable joinTask = new BukkitRunnable() {
                @Override
                public void run() {

                    if (true) {
                        int compassPosition = Main.getConfigurator().config.getInt("hotbar.selector", 0);
                        if (compassPosition >= 0 && compassPosition <= 8) {
                            ItemStack compass = Main.getConfigurator().readDefinedItem("jointeam", "COMPASS");
                            ItemMeta metaCompass = compass.getItemMeta();
                            metaCompass.setDisplayName(i18n("compass_selector_team", false));
                            compass.setItemMeta(metaCompass);
                            gamePlayer.player.getInventory().setItem(compassPosition, compass);
                        }
                    }

                    //离开物品
                    int leavePosition = Main.getConfigurator().config.getInt("hotbar.leave", 8);
                    if (leavePosition >= 0 && leavePosition <= 8) {
                        ItemStack leave = Main.getConfigurator().readDefinedItem("leavegame", "SLIME_BALL");
                        ItemMeta leaveMeta = leave.getItemMeta();
                        leaveMeta.setDisplayName(i18n("leave_from_game_item", false));
                        leave.setItemMeta(leaveMeta);
                        gamePlayer.player.getInventory().setItem(leavePosition, leave);
                    }

                    //vip通道
                    if (gamePlayer.player.hasPermission("bw.vip.startitem")
                            || gamePlayer.player.hasPermission("misat11.bw.vip.startitem")) {
                        int vipPosition = Main.getConfigurator().config.getInt("hotbar.start", 1);
                        if (vipPosition >= 0 && vipPosition <= 8) {
                            ItemStack startGame = Main.getConfigurator().readDefinedItem("startgame", "DIAMOND");
                            ItemMeta startGameMeta = startGame.getItemMeta();
                            startGameMeta.setDisplayName(i18n("start_game_item", false));
                            startGame.setItemMeta(startGameMeta);

                            gamePlayer.player.getInventory().setItem(vipPosition, startGame);
                        }
                    }

                }
            };

            //将玩家传送到大厅，并且基于快捷键
            if (gamePlayer.teleport(lobbySpawn)) {
                joinTask.runTaskLater(Main.getMain(), 1L);
            } else {
                joinTask.runTaskLater(Main.getMain(), 10L);
            }

            if (gamePlayer.player.isEmpty()) {
                runTask();
            } else {
                statusbar.addPlayer(gamePlayer.player);
            }

        }
    }

    /*随机加入团队*/
    public void joinRandomTeam(){

    }

    public void runTask() {
        if (task != null) {
            if (Bukkit.getScheduler().isQueued(task.getTaskId())) {
                task.cancel();
            }
            task = null;
        }
        task = (new BukkitRunnable() {

            public void run() {
                Game.this.run();
            }

        }.runTaskTimer(Main.getMain(), 0, 20));
    }

    /*取消任务*/
    private void cancelTask() {
        if (task != null) {
            if (Bukkit.getScheduler().isQueued(task.getTaskId())) {
                task.cancel();
            }
            task = null;
        }
    }

    public void run(){
//步骤1:检查这个游戏是否运行
        if (gameStatus == GameStatus.DISABLED) { // Game is not running, why cycle is still running? 游戏不运行，为什么循环仍然运行
            cancelTask();
            return;
        }

        //步骤2:如果这是第一次，准备等候大厅
        if (countdown == -1 && gameStatus == GameStatus.WAITING) {
            previousCountdown = countdown = pauseCountdown;
            previousStatus = GameStatus.WAITING;
            String title = i18nonly("bossbar_waiting");//设置标题为等待
            statusbar.setProgress(0);
            statusbar.setVisible(true);
            for (GamePlayer p : players) {
                statusbar.addPlayer(p.player);
            }
//            if (statusbar instanceof BossBar) {
//                BossBar bossbar = (BossBar) statusbar;
//                bossbar.setMessage(title);
//                if (bossbar instanceof BossBar19) {
//                    BossBar19 bossbar19 = (BossBar19) bossbar;
//                    bossbar19.setColor(lobbyBossBarColor != null ? lobbyBossBarColor
//                            : BarColor.valueOf(Main.getConfigurator().config.getString("bossbar.lobby.color")));//设置颜色
//                    bossbar19
//                            .setStyle(BarStyle.valueOf(Main.getConfigurator().config.getString("bossbar.lobby.style")));//设置标题样式
//                }
//            }
            if (teamSelectorInventory == null) {
                teamSelectorInventory = new TeamSelectorInventory(Main.getMain(), this);
            }
//            updateSigns();
        }
    }

    public void selectTeam(GamePlayer playerGameProfile, String displayName) {
        if (gameStatus == GameStatus.WAITING) {
            displayName = ChatColor.stripColor(displayName);
            playerGameProfile.player.closeInventory();
            for (Team team : teams.values()) {
                if (displayName.equals(team.name)) {
                    internalTeamJoin(playerGameProfile, team);
                    break;
                }
            }
        }
    }

    /*随机加入队伍*/
    public void joinRandomTeam(GamePlayer player) {
        Team teamForJoin;
        if (currentTeams.size() < 2) {
            teamForJoin = getFirstTeamThatIsntInGame();
        } else {
            CurrentTeam current = getTeamWithLowestPlayers();
            if (current.players.size() >= current.getMaxPlayers()) {
                teamForJoin = getFirstTeamThatIsntInGame();
            } else {
                teamForJoin = current.teamInfo;
            }
        }

        if (teamForJoin == null) {
            return;
        }

        internalTeamJoin(player, teamForJoin);
    }

    public CurrentTeam getCurrentTeamByTeam(Team team) {
        for (CurrentTeam current : currentTeams) {
            if (current.teamInfo == team) {
                return current;
            }
        }
        return null;
    }


    public Team getFirstTeamThatIsntInGame() {
        for (Team team : teams.values()) {
            if (getCurrentTeamByTeam(team) == null) {
                return team;
            }
        }
        return null;
    }

    public CurrentTeam getTeamWithLowestPlayers() {
        CurrentTeam lowest = null;

        for (CurrentTeam team : currentTeams) {
            if (lowest == null) {
                lowest = team;
            }

            if (lowest.players.size() > team.players.size()) {
                lowest = team;
            }
        }

        return lowest;
    }


    /*处理玩家加入团队*/
    private void internalTeamJoin(GamePlayer player, Team teamForJoin) {
        CurrentTeam current = null;
        for (CurrentTeam t : currentTeams) {
            if (t.teamInfo == teamForJoin) {
                current = t;
                break;
            }
        }

        CurrentTeam cur = getPlayerTeam(player);
//        BedwarsPlayerJoinTeamEvent event = new BedwarsPlayerJoinTeamEvent(current, player.player, this, cur);
//        Main.getInstance().getServer().getPluginManager().callEvent(event);

//        if (event.isCancelled()) {
//            return;
//        }

        //设置团队记分板
//        if (current == null) {
//            current = new CurrentTeam(teamForJoin, this);
//            org.bukkit.scoreboard.Team scoreboardTeam = gameScoreboard.getTeam(teamForJoin.name);
//            if (scoreboardTeam == null) {
//                scoreboardTeam = gameScoreboard.registerNewTeam(teamForJoin.name);
//            }
//            if (!Main.isLegacy()) {
//                scoreboardTeam.setColor(teamForJoin.color.chatColor);
//            } else {
//                scoreboardTeam.setPrefix(teamForJoin.color.chatColor.toString());
//            }
//            scoreboardTeam.setAllowFriendlyFire(getOriginalOrInheritedFriendlyfire());
//
//            current.setScoreboardTeam(scoreboardTeam);
//        }
        //处理消息
        if (cur == current) {
            player.player.sendMessage(
                    i18n("team_already_selected").replace("%team%", teamForJoin.teamColor.chatColor + teamForJoin.name)
                            .replace("%players%", Integer.toString(current.players.size()))
                            .replaceAll("%maxplayers%", Integer.toString(current.teamInfo.maxplayers)));
            return;
        }
        if (current.players.size() >= current.teamInfo.maxplayers) {
            if (cur != null) {
                player.player.sendMessage(i18n("team_is_full_you_are_staying")
                        .replace("%team%", teamForJoin.teamColor.chatColor + teamForJoin.name)
                        .replace("%oldteam%", cur.teamInfo.teamColor.chatColor + cur.teamInfo.name));
            } else {
                player.player.sendMessage(
                        i18n("team_is_full").replace("%team%", teamForJoin.teamColor.chatColor + teamForJoin.name));
            }
            return;
        }

        //移出玩家
        if (cur != null) {
            cur.players.remove(player);
            cur.getScoreboardTeam().removeEntry(player.player.getName());
            if (cur.players.isEmpty()) {
                currentTeams.remove(cur);
                cur.getScoreboardTeam().unregister();
            }
        }

        //加入玩家
        current.players.add(player);
        current.getScoreboardTeam().addEntry(player.player.getName());
        player.player
                .sendMessage(i18n("team_selected").replace("%team%", teamForJoin.teamColor.chatColor + teamForJoin.name)
                        .replace("%players%", Integer.toString(current.players.size()))
                        .replaceAll("%maxplayers%", Integer.toString(current.teamInfo.maxplayers)));

        //默认羊毛
        if (true) {
            int colorPosition = Main.getConfigurator().config.getInt("hotbar.color", 1);
            if (colorPosition >= 0 && colorPosition <= 8) {
                ItemStack stack = teamForJoin.teamColor.getWool();
                ItemMeta stackMeta = stack.getItemMeta();
                stackMeta.setDisplayName(teamForJoin.teamColor.chatColor + teamForJoin.name);
                stack.setItemMeta(stackMeta);
                player.player.getInventory().setItem(colorPosition, stack);
            }
        }

        //皮革套装
        if (true) {
            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
            meta.setColor(teamForJoin.teamColor.leatherColor);
            chestplate.setItemMeta(meta);
            player.player.getInventory().setChestplate(chestplate);
        }

        //将玩家加入列表
        if (!currentTeams.contains(current)) {
            currentTeams.add(current);
        }
    }


    public HashMap<String, Team> getTeams() {
        return teams;
    }
}
