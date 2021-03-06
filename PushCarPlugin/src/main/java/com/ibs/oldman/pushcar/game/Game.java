package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.boss.BossBar;
import com.ibs.oldman.pushcar.api.boss.BossBar19;
import com.ibs.oldman.pushcar.api.boss.StatusBar;
import com.ibs.oldman.pushcar.api.event.PushCartTickEvent;
import com.ibs.oldman.pushcar.api.game.ArenaTime;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.api.game.GameStore;
import com.ibs.oldman.pushcar.api.game.RunningTeam;
import static lang.I.*;

import com.ibs.oldman.pushcar.api.spawner.ItemSpawner;
import com.ibs.oldman.pushcar.boss.BossBarSelector;
import com.ibs.oldman.pushcar.boss.XPBar;
import com.ibs.oldman.pushcar.inventory.ExpertSelectorInventory;
import com.ibs.oldman.pushcar.utils.Sounds;
import com.ibs.oldman.pushcar.utils.Title;
import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.ibs.oldman.pushcar.inventory.TeamSelectorInventory;
import com.ibs.oldman.pushcar.lib.nms.util.MiscUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.screamingsandals.simpleinventories.utils.StackParser;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    private GameStatus gameStatus = GameStatus.DISABLED,afterRebuild = GameStatus.WAITING,previousStatus=GameStatus.DISABLED,nextStatus;
//    private GameStatus previousStatus = GameStatus.DISABLED;//之前状态
    /*竞技场对角线位置*/
    private Location point1,point2;
    /*观众的坐标*/
    private Location spectator;
    /*大厅位置*/
    private Location lobbySpawn;
    /*矿车出生点*/
    private Location cartSapwn;
    /*暂停倒计时*/
    private int pauseCountdown = -1;
    /*倒计时*/
    private int countdown = -1;
    /*下一个倒计时*/
    private int nextCountdown = -1;
    /*倒计时之前*/
    private int previousCountdown = -1;
    /*团队选择窗口*/
    private TeamSelectorInventory teamSelectorInventory;
    private ExpertSelectorInventory expertSelectorInventory;
    /*生成职业装备*/
    Expert expert = new Expert();
    /*矿车*/
    private Vehicle vehicle = null;
    private static Main main = null;
    private int gameTime;
    private int minPlayers;
    private Scoreboard gameScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private int maxplayers;
    private Region region = new Region();
    private World world;
    private ArenaTime arenaTime = ArenaTime.WORLD;
    private StatusBar statusbar;//状态栏
    private WeatherType weatherType;
    private List<IpmDropItemSpawner> dropItemSpawners = new ArrayList<>();
    private List<IpmChestItemSpawner> chestItemSpawners = new ArrayList<>();
    /*重生保护*/
    private Map<Player, RespawnProtection> respawnProtectionMap = new HashMap<>();
    //boss提示框栏
    private BarColor lobbyBossBarColor = null;
    private BarColor gameBossBarColor = null;
    /*大厅提示栏颜色*/
    private BarColor lobby_color;
    /*竞技场提示栏颜色*/
    private BarColor game_color;
    /*末地箱子*/
    private Map<GamePlayer, Inventory> fakeEnderChests = new HashMap<>();
    /*处理run（）的任务*/
    private BukkitTask task;
    /*通知游戏等待*/
    public static final int POST_GAME_WAITING = 3;
    public boolean gameStartItem;
    /*刚刚开始,用于判断是否初始化玩家位置和矿车*/
    public boolean just_start = true;

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

            if (Main.getConfigurator().config.getBoolean("bossbar.use-xp-bar", false)) {
                statusbar = new XPBar();
            } else {
                statusbar = BossBarSelector.getBossBar();
            }
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

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    /*玩家请求加入游戏*/
    @Override
    public void joinToGame(Player player) {
        if(gameStatus==GameStatus.WAITING){
            GamePlayer gamePlayer = main.getPlayerGameProfile(player);
            gamePlayer.changeGame(this);
        }
    }

    public World getLobbyWorld(){
        return lobbySpawn.getWorld();
    }

    @Override
    public void leaveFromGame(Player player) {
        if(gameStatus==GameStatus.DISABLED)
            return;
        GamePlayer gamePlayer = main.getPlayerGameProfile(player);
        gamePlayer.changeGame(null);
        updateScorebroad();
    }

    public CurrentTeam getTeamOfChest(Block block) {
        for (CurrentTeam team : currentTeams) {
            if (team.isTeamChestRegistered(block)) {
                return team;
            }
        }
        return null;
    }

    public Inventory getFakeEnderChest(GamePlayer player) {
        if (!fakeEnderChests.containsKey(player)) {
            fakeEnderChests.put(player, Bukkit.createInventory(player.player, InventoryType.ENDER_CHEST));
        }
        return fakeEnderChests.get(player);
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

    /**
     * 更新运行状态的记分板
     * @param gamePlayer
     */
    public void updateRunningScoreboard(GamePlayer gamePlayer){
        if(gameStatus != GameStatus.RUNNING)
            return;
        gameScoreboard.clearSlot(DisplaySlot.SIDEBAR);

        Objective obj = gameScoreboard.getObjective("running");
        if(obj != null){
            obj.unregister();
        }
        obj = gameScoreboard.registerNewObjective("running","dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(Main.getConfigurator().config.getString("running-scoreboard.title","§ePushCarting"));
        List<String> rows = Main.getConfigurator().config.getStringList("running-scoreboard.content");
        int rowMax = rows.size();
        if(rows == null || rows.isEmpty()){
            return;
        }

        for (String row : rows) {
            if (row.trim().equals("")) {
                for (int i = 0; i <= rowMax; i++) {
                    row = row + " ";
                }
            }

            Score score = obj.getScore(this.formatRunningScoreboardString(row, gamePlayer));
            score.setScore(rowMax);
            rowMax--;
        }
        gamePlayer.player.setScoreboard(gameScoreboard);
//        System.out.println("更新啦");
    }

    /*更新大厅记分板*/
    public void updateLobbyScoreboard(){
        if (gameStatus != GameStatus.WAITING) {
            return;
        }
        gameScoreboard.clearSlot(DisplaySlot.SIDEBAR);

        Objective obj = gameScoreboard.getObjective("lobby");
        if (obj != null) {
            obj.unregister();
        }

        obj = gameScoreboard.registerNewObjective("lobby", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(this.formatLobbyScoreboardString(
                Main.getConfigurator().config.getString("lobby-scoreboard.title", "§ePushCart")));

        List<String> rows = Main.getConfigurator().config.getStringList("lobby-scoreboard.content");
        int rowMax = rows.size();
        if (rows == null || rows.isEmpty()) {
            return;
        }

        for (String row : rows) {
            if (row.trim().equals("")) {
                for (int i = 0; i <= rowMax; i++) {
                    row = row + " ";
                }
            }

            Score score = obj.getScore(this.formatLobbyScoreboardString(row));
            score.setScore(rowMax);
            rowMax--;
        }

        for (GamePlayer player : players) {
            player.player.setScoreboard(gameScoreboard);
        }
    }


    /**
     * 将信息转换成大厅格式
     * @param str
     * @return
     */
    private String formatLobbyScoreboardString(String str) {
        String finalStr = str;

        finalStr = finalStr.replace("%arena%", game_name);
        finalStr = finalStr.replace("%players%", String.valueOf(players.size()));
        finalStr = finalStr.replace("%maxplayers%", String.valueOf(maxplayers));
        finalStr = finalStr.replace("%waitingtime%",String.valueOf(countdown));


        return finalStr;
    }

    private String formatRunningScoreboardString(String str,GamePlayer gamePlayer){
        String finalStr = str;

        finalStr = finalStr.replace("%teamname%", getPlayerTeam(gamePlayer).getName());
        finalStr = finalStr.replace("%players%", String.valueOf(getPlayerTeam(gamePlayer).countConnectedPlayers()));
        finalStr = finalStr.replace("%waitingtime%",String.valueOf(countdown));
//        finalStr = finalStr.replace("%kills%", String.valueOf(maxplayers));//杀敌后面统计出了再记录
//        finalStr = finalStr.replace("%score%",String.valueOf(countdown));//积分也是一样

        return finalStr;
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

    @Override
    public Location getCartSpawn() {
        return cartSapwn;
    }

    public List<IpmDropItemSpawner> getDropItemSpawners() {
        return dropItemSpawners;
    }

    public void setDropItemSpawners(List<IpmDropItemSpawner> dropItemSpawners) {
        this.dropItemSpawners = dropItemSpawners;
    }

    public List<IpmChestItemSpawner> getChestItemSpawners() {
        return chestItemSpawners;
    }

    public void setChestItemSpawners(List<IpmChestItemSpawner> chestItemSpawners) {
        this.chestItemSpawners = chestItemSpawners;
    }

    @Override
    public void setCartSpawn(Location location) {
        this.cartSapwn = location;
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

    @Override
    public Vehicle getCart() {
        return vehicle;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public ArenaTime getArenaTime() {
        return arenaTime;
    }

    public static Game loadGame(File file) {
        return loadGame(file, true);
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
        if(game.world==null){
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
        game.cartSapwn = MiscUtils.readLocationFromString(game.world,configMap.getString("cartSapwn"));
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
//                t.bed = MiscUtils.readLocationFromString(game.world, team.getString("bed"));
//                team.getDouble("cartSpeed")==-1?"":"";
                t.cart_speed = team.getDouble("cartSpeed",0.01);
                t.cart_range = team.getDouble("cartRange",2);
                t.targetbed = MiscUtils.readLocationFromString(game.world,team.getString("targetbed"));
                t.maxplayers = team.getInt("maxPlayers");
                t.spawn = MiscUtils.readLocationFromString(game.world, team.getString("spawn"));
                t.game = game;

//                t.newColor = true;
                game.teams.put(t.name,t);
            }
        }


        //读取定点道具生成器
        if(configMap.isSet("dropitems")){
            List<Map<String,Object>> dropitems = (List<Map<String, Object>>) configMap.getList("dropitems");
            for(Map<String,Object> dropitem:dropitems){
                IpmDropItemSpawner dropItemSpawner = new IpmDropItemSpawner(
                        (int)dropitem.get("currentdown"),
                        MiscUtils.readLocationFromString(game.world,(String)dropitem.get("spawnlocation")),
                        Material.valueOf((String) dropitem.get("type")),
                        null,
                        (String)dropitem.get("name"));
                game.dropItemSpawners.add(dropItemSpawner);
            }
        }

        //读取空投信息
        if(configMap.isSet("chestitems.property")){
            List<Map<String,Object>> chestitems = (List<Map<String, Object>>) configMap.getList("chestitems.property");
            for(Map<String,Object> chestitem:chestitems){
                IpmChestItemSpawner chestItemSpawner = new IpmChestItemSpawner(
                        (int)chestitem.get("currentdown"),
                        (String) chestitem.get("name"),
                        MiscUtils.readLocationFromString(game.world, (String) chestitem.get("spawnlocation")),
                        0,
                        game,
                        game.readchestItems(configMap));
                game.chestItemSpawners.add(chestItemSpawner);
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
        try {
            game.lobbyBossBarColor = loadBossBarColor(
                    configMap.getString("lobbyBossBarColor", "default").toUpperCase());
            game.gameBossBarColor = loadBossBarColor(configMap.getString("gameBossBarColor", "default").toUpperCase());
        } catch (Throwable t) {
            // We're using 1.8
        }
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

        //删除玩家,次方法为了防止java.util.ConcurrentModificationException异常
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
            countdown = -1;
            if (gameScoreboard.getObjective("display") != null) {
                gameScoreboard.getObjective("display").unregister();
            }
            if (gameScoreboard.getObjective("lobby") != null) {
                gameScoreboard.getObjective("lobby").unregister();
            }
            gameScoreboard.clearSlot(DisplaySlot.SIDEBAR);
            for (CurrentTeam team : currentTeams) {
                team.getScoreboardTeam().unregister();
            }
            currentTeams.clear();
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

//        vehicle.remove();
//        Main.unregisterGameEntity(vehicle);

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
        for(IpmChestItemSpawner ipmChestItemSpawner:chestItemSpawners){
            ipmChestItemSpawner.clear();
        }
        for(IpmDropItemSpawner ipmDropItemSpawner:dropItemSpawners){
            ipmDropItemSpawner.clearAllItems();
        }

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

    public ExpertSelectorInventory getExpertSelectorInventory() {
        return expertSelectorInventory;
    }

    /*玩家加入游戏*/
    public void internalJoinPlayer(GamePlayer gamePlayer){
            if(!players.contains(gamePlayer)){
                players.add(gamePlayer);
            }

            //预留一个统计玩家的方法
            if (arenaTime.time >= 0) {
                gamePlayer.player.setPlayerTime(arenaTime.time, false);
            }

            if (gameStatus == GameStatus.WAITING) {
                    //设置玩家快捷键按钮
                    final BukkitRunnable joinTask = new BukkitRunnable() {
                        @Override
                        public void run() {

                        if (true) {
                            int compassPosition = Main.getConfigurator().config.getInt("hotbar.selector", 4);
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

                        //开启职业
                        if (true) {
                            int compassPosition = Main.getConfigurator().config.getInt("expter.selector",2 );
                            if (compassPosition >= 0 && compassPosition <= 8) {
                                ItemStack compass = new ItemStack(Material.IRON_AXE);
                                ItemMeta metaCompass = compass.getItemMeta();
                                metaCompass.setDisplayName("选择职业");
                                compass.setItemMeta(metaCompass);
                                gamePlayer.player.getInventory().setItem(compassPosition, compass);
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

    public boolean checkMinPlayers() {
        return players.size() >= getMinPlayers();
    }

    public TeamSelectorInventory getTeamSelectorInventory() {
        return teamSelectorInventory;
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

    /*游戏运行逻辑*/
    public void run(){
//步骤1:检查这个游戏是否运行
        if (gameStatus == GameStatus.DISABLED) { // Game is not running, why cycle is still running? 游戏不运行，为什么循环仍然运行
            cancelTask();
            return;
        }

        //步骤2:如果这是第一次，准备等候大厅
        if (countdown == -1 && gameStatus == GameStatus.WAITING) {
            previousStatus = GameStatus.WAITING;
            statusbar.setProgress(0);
            statusbar.setVisible(true);
            previousCountdown = countdown = pauseCountdown;
            String title = i18nonly("bossbar_waiting");//设置标题为等待
            for (GamePlayer p : players) {
                statusbar.addPlayer(p.player);
            }
            if (statusbar instanceof BossBar) {
                BossBar bossbar = (BossBar) statusbar;
                bossbar.setMessage(title);
                if (bossbar instanceof BossBar19) {
                    BossBar19 bossbar19 = (BossBar19) bossbar;
                    bossbar19.setColor(lobbyBossBarColor != null ? lobbyBossBarColor
                            : BarColor.valueOf(Main.getConfigurator().config.getString("bossbar.lobby.color")));//设置颜色
                    bossbar19
                            .setStyle(BarStyle.valueOf(Main.getConfigurator().config.getString("bossbar.lobby.style")));//设置标题样式
                }
            }
            if (teamSelectorInventory == null) {
                teamSelectorInventory = new TeamSelectorInventory(Main.getMain(), this);
            }
            if(expertSelectorInventory == null)
                expertSelectorInventory = new ExpertSelectorInventory(Main.getMain(),this);
        }
        nextCountdown = countdown;
        nextStatus = gameStatus;

        //如果游戏在等阶段
        if(gameStatus == GameStatus.WAITING) {
            //是否有游戏开始物品
            if (gameStartItem) {
                if (players.size() >= getMinPlayers()) {//玩家数量大于最低玩家数量
                    //剩余的玩家随机加入团队
                    for (GamePlayer gamePlayer : players)
                        if (getPlayerTeam(gamePlayer) == null)
                            joinRandomTeam(gamePlayer);
                }
                //玩家数量大于1,设置倒计时为0，并且游戏开始物品取消
                if(players.size() >1){
                    countdown = 0;
                    gameStartItem = false;
                }
            }
            //玩家数量大于最低玩家数量并且团队数量大于1
            if(players.size() >= getMinPlayers() && teams.values().size()>1){
                //倒计时为0设置下一个倒计时为游戏时间，下一个游戏状态为运行状态
                if (countdown == 0) {
                    nextCountdown = gameTime;
                    nextStatus = GameStatus.RUNNING;



                    //生成矿车
                    vehicle = (Vehicle) world.spawnEntity(cartSapwn,EntityType.MINECART_TNT);
                    //将所有玩家传送到出生点
                    for (CurrentTeam currentTeam : currentTeams) {
                        currentTeam.teleportPlayersToSpawn();
                        currentTeam.setMinecart(vehicle);
                    }
                    Main.registerGameEntity(vehicle,this);
                }
                else {//否则下一个倒计时递减，
                    nextCountdown--;
                    //倒计时在1-10之间，并且倒计时不等于前倒计时,递归玩家发送字幕并且播放声音
                    if (countdown <= 10 && countdown >= 1 && countdown != previousCountdown) {
                        for (GamePlayer player : players) {
                            Title.send(player.player, ChatColor.YELLOW + Integer.toString(countdown), "");
                            Sounds.playSound(player.player, player.player.getLocation(),
                                    Main.getConfigurator().config.getString("sounds.on_countdown"), Sounds.UI_BUTTON_CLICK,
                                    1, 1);
                        }
                    }
                }
            }
            else{//否则下一个倒计时等于倒计时等于暂停时间
                nextCountdown = countdown = pauseCountdown;
            }
            setBossbarProgress(countdown, pauseCountdown);
            updateLobbyScoreboard();
        }
        else if(gameStatus == GameStatus.RUNNING){//游戏运行中

            if(countdown== 0){//如果游戏时间还没到
                nextCountdown = POST_GAME_WAITING;
                nextStatus = GameStatus.GAME_END_CELEBRATING;

                //通过积分进行比赛获胜判决
            }
            else{
                nextCountdown--;
                //递减主动技能冷却时间
                expert.diminishingCoolTime();
            }
            for(GamePlayer gamePlayer:players){
                updateRunningScoreboard(gamePlayer);
            }
            setBossbarProgress(countdown, pauseCountdown);
        }
        else if(gameStatus == GameStatus.GAME_END_CELEBRATING){
            if(countdown == 0){
                nextStatus = GameStatus.REBUILDING;
                nextCountdown = 0;

                for(GamePlayer gamePlayer:(List<GamePlayer>)((ArrayList<GamePlayer>)players).clone()){
                    gamePlayer.changeGame(null);
                }
            }
            else{
                nextCountdown--;
            }
        }
        //加载滴答事件
        PushCartTickEvent tickEvent = new PushCartTickEvent(this,previousCountdown,previousStatus,countdown,gameStatus,nextCountdown,nextStatus);
        Bukkit.getPluginManager().callEvent(tickEvent);
        // Phase 5: Update Previous information
        // 步骤 5: 更新之前的信息
        previousCountdown = countdown;
        previousStatus = gameStatus;

        //处理滴答事件
        if(gameStatus != tickEvent.getNextStatus()){

            //如果一个状态是运行
            if(tickEvent.getNextStatus() == GameStatus.RUNNING){

                //如果开启了大厅之后随机加入队伍
                if (true) {
                    for (GamePlayer player : players) {//将全部没有加入队伍的玩家随机加入一个队伍
                        if (getPlayerTeam(player) == null) {
                            joinRandomTeam(player);
                        }
                    }
                }

                //设置boss标题进度为0
                statusbar.setProgress(0);
                //如果开启了游戏大厅boss标题就显示
                statusbar.setVisible(true);
                //设置boss标题信息
                if (statusbar instanceof BossBar) {
                    BossBar bossbar = (BossBar) statusbar;
                    bossbar.setMessage(i18n("bossbar_running", false));
                    if (bossbar instanceof BossBar19) {
                        BossBar19 bossbar19 = (BossBar19) bossbar;
                        bossbar19.setColor(gameBossBarColor != null ? gameBossBarColor
                                : BarColor.valueOf(Main.getConfigurator().config.getString("bossbar.game.color")));
                        bossbar19.setStyle(
                                BarStyle.valueOf(Main.getConfigurator().config.getString("bossbar.game.style")));
                    }
                }//清除选择队伍窗口
//
                if(teamSelectorInventory != null)
                    teamSelectorInventory.destroy();
                teamSelectorInventory = null;

                if (gameScoreboard.getObjective("lobby") != null) {//注销游戏计分版信息
                    gameScoreboard.getObjective("lobby").unregister();
                }
                gameScoreboard.clearSlot(DisplaySlot.SIDEBAR);
                for(GamePlayer player:this.players){
                    CurrentTeam team = getPlayerTeam(player);
                    player.player.getInventory().clear();
                    // Player still had armor on legacy versions
                    player.player.getInventory().setHelmet(null);
                    player.player.getInventory().setChestplate(null);
                    player.player.getInventory().setLeggings(null);
                    player.player.getInventory().setBoots(null);

                    Sounds.playSound(player.player, player.player.getLocation(),
                            Main.getConfigurator().config.getString("sounds.on_game_start"),
                            Sounds.ENTITY_PLAYER_LEVELUP, 1, 1);
                }


                for(GamePlayer gamePlayer:this.players){
                    expert.generateEquipment(getPlayerTeam(gamePlayer).teamInfo,gamePlayer);
                }
//
            }


        }

        //如果游戏处于运行状态，判断是否有队伍获胜
        if(gameStatus == GameStatus.RUNNING && tickEvent.getNextStatus() == GameStatus.RUNNING){
            CurrentTeam winner = null;
            for(CurrentTeam currentTeam:currentTeams){

                //是否有队伍获胜
                if(currentTeam.isArrived()){
                    winner = currentTeam;
                }
                else{//队伍施加推力
                    currentTeam.launchCart();
                }
            }
            //生成定点掉落物品
            for(IpmDropItemSpawner ipmDropItemSpawner:dropItemSpawners){
                ipmDropItemSpawner.runTask(getWorld());
            }

            //发射空投
            for(IpmChestItemSpawner chestItemSpawner:chestItemSpawners){
                chestItemSpawner.launchFireWork(getWorld(),this);
            }

            //游戏结束的奖励惩罚措施
            if(winner != null){
                //庆祝
                for(GamePlayer gamePlayer:winner.players){
                    Title.send(gamePlayer.player,"你赢啦","聪明鬼你赢啦!");
                    gamePlayer.player.sendMessage("你赢啦!");
                }
                //失败者退出游戏
                Iterator iterator = players.iterator();
                while(iterator.hasNext()){
                    GamePlayer gamePlayer = (GamePlayer) iterator.next();
                    if(!isPlayerInTeam(gamePlayer.player,winner)) {
                        gamePlayer.changeGame(null);
                        break;
                    }
                }
                tickEvent.setNextStatus(GameStatus.GAME_END_CELEBRATING);
                tickEvent.setNextCountdown(POST_GAME_WAITING);
            }
        }
        countdown = tickEvent.getNextCountdown();
        gameStatus = tickEvent.getNextStatus();
        System.out.println("游戏状态"+gameStatus.toString()+"之后的状态"+nextStatus.toString());
        if(gameStatus == GameStatus.REBUILDING)
            rebuild();

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

    public RespawnProtection addProtectedPlayer(Player player) {
        int time = Main.getConfigurator().config.getInt("respawn.protection-time", 10);

        RespawnProtection respawnProtection = new RespawnProtection(this, player, time);
        respawnProtectionMap.put(player, respawnProtection);

        return respawnProtection;
    }

    @SuppressWarnings("unchecked")
    public void makePlayerFromSpectator(GamePlayer gamePlayer) {
        Player player = gamePlayer.player;
        CurrentTeam currentTeam = getPlayerTeam(gamePlayer);

        if (gamePlayer.getGame() == this && currentTeam != null) {
            gamePlayer.isSpectator = false;
            new BukkitRunnable() {
                @Override
                public void run() {
                    gamePlayer.teleport(currentTeam.getTeamSpawn());
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.setGameMode(GameMode.SURVIVAL);

                    if (Main.getConfigurator().config.getBoolean("respawn.protection-enabled", true)) {
                        RespawnProtection respawnProtection = addProtectedPlayer(player);
                        respawnProtection.runProtection();
                    }

//                    if (gamePlayer.getGame().getOriginalOrInheritedPlayerRespawnItems()) {
//                        List<ItemStack> givedGameStartItems = StackParser.parseAll((Collection<Object>) Main.getConfigurator().config
//                                .getList("gived-player-respawn-items"));
//                        if (givedGameStartItems != null) {
//                            MiscUtils.giveItemsToPlayer(givedGameStartItems, player, currentTeam.getColor());
//                        } else {
//                            Debug.warn("You have wrongly configured gived-player-respawn-items!", true);
//                        }
//                    }
                }
            }.runTask(Main.getMain());
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

    public Location makeSpectator(GamePlayer gamePlayer, boolean leaveItem) {
        Player player = gamePlayer.player;
        gamePlayer.isSpectator = true;
        BukkitRunnable spectatorCreationTask = new BukkitRunnable() {
            @Override
            public void run() {
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setGameMode(GameMode.SPECTATOR);

                if (leaveItem) {
                    int leavePosition = Main.getConfigurator().config.getInt("hotbar.leave", 8);
                    if (leavePosition >= 0 && leavePosition <= 8) {
                        ItemStack leave = Main.getConfigurator().readDefinedItem("leavegame", "SLIME_BALL");
                        ItemMeta leaveMeta = leave.getItemMeta();
                        leaveMeta.setDisplayName(i18n("leave_from_game_item", false));
                        leave.setItemMeta(leaveMeta);
                        gamePlayer.player.getInventory().setItem(leavePosition, leave);
                    }
                }
            }
        };

        if (gamePlayer.teleport(spectator)) {
            spectatorCreationTask.runTaskLater(Main.getMain(), 1L);
        } else {
            spectatorCreationTask.runTaskLater(Main.getMain(), 10L);
        }

        return spectator;
    }


    /*设置boss进度条*/
    public void setBossbarProgress(int count, int max) {
        double progress = (double) count / (double) max;
        statusbar.setProgress(progress);
        if (statusbar instanceof XPBar) {
            XPBar xpbar = (XPBar) statusbar;
            xpbar.setSeconds(count);
        }
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


    /*处理玩家加入团队
    * 初始装备，记分板，boss提示框
    *
    * */
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
        if (current == null) {
            current = new CurrentTeam(teamForJoin, this);
            org.bukkit.scoreboard.Team scoreboardTeam = gameScoreboard.getTeam(teamForJoin.name);
            if (scoreboardTeam == null) {
                scoreboardTeam = gameScoreboard.registerNewTeam(teamForJoin.name);
            }
            if (!Main.isLegacy()) {
                scoreboardTeam.setColor(teamForJoin.teamColor.chatColor);
            } else {
                scoreboardTeam.setPrefix(teamForJoin.teamColor.chatColor.toString());
            }
            scoreboardTeam.setAllowFriendlyFire(true);

            current.setScoreboardTeam(scoreboardTeam);
        }
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

        //职业套装
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

    /**
     * 判断矿车是否属于竞技场
     * @param entity 实体
     * @return
     */
    public boolean isCartInTeam(Entity entity){
        for(CurrentTeam currentTeam:currentTeams)
            if(currentTeam.isCartInTeam(entity))
                return true;
        return false;
    }

    public void removeProtectedPlayer(Player player) {
        RespawnProtection respawnProtection = respawnProtectionMap.get(player);
        if (respawnProtection == null) {
            return;
        }

        try {
            respawnProtection.cancel();
        } catch (Exception ignored) {
        }

        respawnProtectionMap.remove(player);
    }

    public boolean isBlockAddedDuringGame(Location loc) {
        return gameStatus == GameStatus.RUNNING && region.isBlockAddedDuringGame(loc);
    }

    public void saveToConfig() {
        File dir = new File(Main.getMain().getDataFolder(), "arenas");
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, game_name + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration configMap = new YamlConfiguration();
        configMap.set("name", game_name);
        configMap.set("pauseCountdown", pauseCountdown);
        configMap.set("gameTime", gameTime);
        configMap.set("world", world.getName());
        configMap.set("pos1", MiscUtils.setLocationToString(point1));
        configMap.set("pos2", MiscUtils.setLocationToString(point2));
        configMap.set("specSpawn", MiscUtils.setLocationToString(spectator));
        configMap.set("lobbySpawn", MiscUtils.setLocationToString(lobbySpawn));
        configMap.set("cartSapwn",MiscUtils.setLocationToString(cartSapwn));
        configMap.set("lobbySpawnWorld", lobbySpawn.getWorld().getName());
        configMap.set("minPlayers", minPlayers);
        if (!teams.isEmpty()) {
            for (Team t : teams.values()) {
                configMap.set("teams." + t.name + ".isNewColor", t.isNewColor());
                configMap.set("teams." + t.name + ".color", t.teamColor.name());
                configMap.set("teams." + t.name + ".maxPlayers", t.maxplayers);
                configMap.set("teams." + t.name + ".spawn", MiscUtils.setLocationToString(t.spawn));
                configMap.set("teams."+t.name+".targetbed",MiscUtils.setLocationToString(t.targetbed));
            }
        }

        List<Map<String, Object>> nS = new ArrayList<>();
        for(IpmDropItemSpawner dropItemSpawner:dropItemSpawners){
            Map<String,Object> dropitemMap = new HashMap<>();
            dropitemMap.put("currentdown",dropItemSpawner.getCurrentDown());
            dropitemMap.put("spawnlocation",MiscUtils.setLocationToString(dropItemSpawner.getSpawnLocation()));
            dropitemMap.put("type",dropItemSpawner.getDropType().toString());
            dropitemMap.put("name",dropItemSpawner.getName());
            nS.add(dropitemMap);
        }
        configMap.set("dropitems", nS);

        List<Map<String, Object>> tS = new ArrayList<>();
        for(IpmChestItemSpawner chestItemSpawner:chestItemSpawners){
            Map<String,Object> chestitemMap = new HashMap<>();
            chestitemMap.put("currentdown",chestItemSpawner.getCurrentDown());
            chestitemMap.put("name",chestItemSpawner.getName());
            chestitemMap.put("spawnlocation",MiscUtils.setLocationToString(chestItemSpawner.getSpawnLocation()));
//            chestitemMap.put("chestlocation",MiscUtils.setLocationToString(chestItemSpawner.getChestLocation()));
//            chestitemMap.put("angle",chestItemSpawner.getAngle());
            tS.add(chestitemMap);
        }
        configMap.set("chestitems",tS);

//        for (ItemSpawner spawner : spawners) {
//            Map<String, Object> spawnerMap = new HashMap<>();
//            spawnerMap.put("location", MiscUtils.setLocationToString(spawner.loc));
//            spawnerMap.put("type", spawner.type.getConfigKey());
//            spawnerMap.put("customName", spawner.customName);
//            spawnerMap.put("startLevel", spawner.startLevel);
//            spawnerMap.put("hologramEnabled", spawner.hologramEnabled);
//            if (spawner.getTeam() != null) {
//                spawnerMap.put("team", spawner.getTeam().getName());
//            } else {
//                spawnerMap.put("team", null);
//            }
//            spawnerMap.put("maxSpawnedResources", spawner.maxSpawnedResources);
//            nS.add(spawnerMap);
//        }
//        configMap.set("spawners", nS);
//        if (!gameStore.isEmpty()) {
//            List<Map<String, String>> nL = new ArrayList<>();
//            for (GameStore store : gameStore) {
//                Map<String, String> map = new HashMap<>();
//                map.put("loc", MiscUtils.setLocationToString(store.getStoreLocation()));
//                map.put("shop", store.getShopFile());
//                map.put("parent", store.getUseParent() ? "true" : "false");
//                map.put("type", store.getEntityType().name());
//                if (store.isShopCustomName()) {
//                    map.put("name", store.getShopCustomName());
//                }
//                nL.add(map);
//            }
//            configMap.set("stores", nL);
//        }


//        configMap.set("constant." + COMPASS_ENABLED, writeBooleanConstant(compassEnabled));
//        configMap.set("constant." + ADD_WOOL_TO_INVENTORY_ON_JOIN, writeBooleanConstant(addWoolToInventoryOnJoin));
//        configMap.set("constant." + COLORED_LEATHER_BY_TEAM_IN_LOBBY,
//                writeBooleanConstant(coloredLeatherByTeamInLobby));
//        configMap.set("constant." + CRAFTING, writeBooleanConstant(crafting));
//        configMap.set("constant." + JOIN_RANDOM_TEAM_AFTER_LOBBY, writeBooleanConstant(joinRandomTeamAfterLobby));
//        configMap.set("constant." + JOIN_RANDOM_TEAM_ON_JOIN, writeBooleanConstant(joinRandomTeamOnJoin));
//        configMap.set("constant." + KEEP_INVENTORY, writeBooleanConstant(keepInventory));
//        configMap.set("constant." + PREVENT_KILLING_VILLAGERS, writeBooleanConstant(preventKillingVillagers));
//        configMap.set("constant." + PLAYER_DROPS, writeBooleanConstant(playerDrops));
//        configMap.set("constant." + FRIENDLY_FIRE, writeBooleanConstant(friendlyfire));
//        configMap.set("constant." + LOBBY_BOSSBAR, writeBooleanConstant(lobbybossbar));
//        configMap.set("constant." + GAME_BOSSBAR, writeBooleanConstant(gamebossbar));
//        configMap.set("constant." + LOBBY_SCOREBOARD, writeBooleanConstant(lobbyscoreboard));
//        configMap.set("constant." + SCOREBOARD, writeBooleanConstant(ascoreboard));
//        configMap.set("constant." + PREVENT_SPAWNING_MOBS, writeBooleanConstant(preventSpawningMobs));
//        configMap.set("constant." + SPAWNER_HOLOGRAMS, writeBooleanConstant(spawnerHolograms));
//        configMap.set("constant." + SPAWNER_DISABLE_MERGE, writeBooleanConstant(spawnerDisableMerge));
//        configMap.set("constant." + GAME_START_ITEMS, writeBooleanConstant(gameStartItems));
//        configMap.set("constant." + PLAYER_RESPAWN_ITEMS, writeBooleanConstant(playerRespawnItems));
//        configMap.set("constant." + SPAWNER_HOLOGRAMS_COUNTDOWN, writeBooleanConstant(spawnerHologramsCountdown));
//        configMap.set("constant." + DAMAGE_WHEN_PLAYER_IS_NOT_IN_ARENA,
//                writeBooleanConstant(damageWhenPlayerIsNotInArena));
//        configMap.set("constant." + REMOVE_UNUSED_TARGET_BLOCKS, writeBooleanConstant(removeUnusedTargetBlocks));
//        configMap.set("constant." + ALLOW_BLOCK_FALLING, writeBooleanConstant(allowBlockFalling));
//        configMap.set("constant." + HOLO_ABOVE_BED, writeBooleanConstant(holoAboveBed));
//        configMap.set("constant." + SPECTATOR_JOIN, writeBooleanConstant(spectatorJoin));

        configMap.set("arenaTime", arenaTime.name());
//        configMap.set("arenaWeather", arenaWeather == null ? "default" : arenaWeather.name());
//
//        try {
//            configMap.set("lobbyBossBarColor", lobbyBossBarColor == null ? "default" : lobbyBossBarColor.name());
//            configMap.set("gameBossBarColor", gameBossBarColor == null ? "default" : gameBossBarColor.name());
//        } catch (Throwable t) {
//            // We're using 1.8
//        }

        try {
            configMap.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BarColor loadBossBarColor(String color) {
        try {
            return BarColor.valueOf(color);
        } catch (Exception e) {
            return null;
        }
    }

    public HashMap<String, Team> getTeams() {
        return teams;
    }

    public boolean isProtectionActive(Player player) {
        return (respawnProtectionMap.containsKey(player));
    }

    public boolean blockPlace(GamePlayer player, Block block, BlockState replaced, ItemStack itemInHand) {
        if (gameStatus != GameStatus.RUNNING) {
            return false; // ?
        }
        if (player.isSpectator) {
            return false;
        }
//        if (Main.isFarmBlock(block.getType())) {
//            return true;
//        }
        if (!GameCreator.isInArea(block.getLocation(), point1, point2)) {
            return false;
        }

//        BedwarsPlayerBuildBlock event = new BedwarsPlayerBuildBlock(this, player.player, getPlayerTeam(player), block,
//                itemInHand, replaced);
//        Main.getInstance().getServer().getPluginManager().callEvent(event);

//        if (event.isCancelled()) {
//            return false;
//        }

        if (replaced.getType() != Material.AIR) {
            if (Main.isBreakableBlock(replaced.getType())) {
                region.putOriginalBlock(block.getLocation(), replaced);
            } else if (region.isLiquid(replaced.getType())) {
                region.putOriginalBlock(block.getLocation(), replaced);
            } else {
                return false;
            }
        }
        region.addBuiltDuringGame(block.getLocation());

        return true;
    }

    public boolean blockBreak(GamePlayer player, Block block, BlockBreakEvent event) {
        if (gameStatus != GameStatus.RUNNING) {
            return false; // ?
        }
        if (player.isSpectator) {
            return false;
        }
//        if (Main.isFarmBlock(block.getType())) {
//            return true;
//        }
        if (!GameCreator.isInArea(block.getLocation(), getPoint1(), getPoint2())) {
            return false;
        }

//        BedwarsPlayerBreakBlock breakEvent = new BedwarsPlayerBreakBlock(this, player.player, getPlayerTeam(player),
//                block);
//        Main.getInstance().getServer().getPluginManager().callEvent(breakEvent);

//        if (breakEvent.isCancelled()) {
//            return false;
//        }

        if (region.isBlockAddedDuringGame(block.getLocation())) {
            region.removeBlockBuiltDuringGame(block.getLocation());

            if (block.getType() == Material.ENDER_CHEST) {
                CurrentTeam team = getTeamOfChest(block);
                if (team != null) {
                    team.removeTeamChest(block);
                    String message = i18n("team_chest_broken");
                    for (GamePlayer gp : team.players) {
                        gp.player.sendMessage(message);
                    }

//                    if (breakEvent.isDrops()) {
//                        event.setDropItems(false);
//                        player.player.getInventory().addItem(new ItemStack(Material.ENDER_CHEST));
//                    }
                }
            }

//            if (!breakEvent.isDrops()) {
//                try {
//                    event.setDropItems(false);
//                } catch (Throwable tr) {
//                    block.setType(Material.AIR);
//                }
//            }
            return true;
        }

//        Location loc = block.getLocation();
//        if (region.isBedBlock(block.getState())) {
//            if (!region.isBedHead(block.getState())) {
//                loc = region.getBedNeighbor(block).getLocation();
//            }
//        }
//        if (isTargetBlock(loc)) {
//            if (region.isBedBlock(block.getState())) {
//                if (getPlayerTeam(player).teamInfo.bed.equals(loc)) {
//                    return false;
//                }
//                bedDestroyed(loc, player.player, true);
//                region.putOriginalBlock(block.getLocation(), block.getState());
//                if (block.getLocation().equals(loc)) {
//                    Block neighbor = region.getBedNeighbor(block);
//                    region.putOriginalBlock(neighbor.getLocation(), neighbor.getState());
//                } else {
//                    region.putOriginalBlock(loc, region.getBedNeighbor(block).getState());
//                }
//                try {
//                    event.setDropItems(false);
//                } catch (Throwable tr) {
//                    if (region.isBedHead(block.getState())) {
//                        region.getBedNeighbor(block).setType(Material.AIR);
//                    } else {
//                        block.setType(Material.AIR);
//                    }
//                }
//                return true;
//            } else {
//                if (getPlayerTeam(player).teamInfo.bed.equals(loc)) {
//                    return false;
//                }
//                bedDestroyed(loc, player.player, false);
//                region.putOriginalBlock(loc, block.getState());
//                try {
//                    event.setDropItems(false);
//                } catch (Throwable tr) {
//                    block.setType(Material.AIR);
//                }
//                return true;
//            }
//        }
//        if (Main.isBreakableBlock(block.getType())) {
//            region.putOriginalBlock(block.getLocation(), block.getState());
//            return true;
//        }
        return false;
    }

    /*读取宝箱物品列表*/
    public List<ItemStack> readChestItems(List<Map<String,Object>> maps){

        List<ItemStack> list = new ArrayList<>();
        if(maps!=null && !maps.isEmpty())
            for(Map<String,Object>item:maps){
                ItemStack itemStack = new ItemStack(Material.valueOf((String) item.get("type")));
                list.add(itemStack);
            }
        return list;
    }

    /**
     * 读取物品信息
     * @param configMap
     * @return
     */
    public List<ItemStack> readchestItems(FileConfiguration configMap){
        //读取队伍信息
        List<ItemStack> list = new ArrayList<>();
        if (configMap.isSet("chestitems.items")) {
            System.out.println("asd");
            for (String items : configMap.getConfigurationSection("chestitems.items").getKeys(false)) {//读取队伍信息
                System.out.println("收到甲方可"+items+configMap.getConfigurationSection("chestitems.items").toString());
                ConfigurationSection item = configMap.getConfigurationSection("chestitems.items").getConfigurationSection(items);
                String type = item.getString("type");
                System.out.println(type);
                ItemStack itemStack = new ItemStack(Material.valueOf(type));
                itemStack.getItemMeta().setDisplayName(item.getString("name"));
                list.add(itemStack);
            }
        }
        return list;
    }

    /*判断烟花是否属于游戏的空投*/
    public boolean isFireworkInGame(Firework firework){
        for(IpmChestItemSpawner ipmChestItemSpawner:chestItemSpawners){
            if(ipmChestItemSpawner.isCheckFirework(firework));
                return true;
        }
        return false;
    }

    public IpmChestItemSpawner getChestItemSpawnerByFirework(Firework firework){
        for(IpmChestItemSpawner ipmChestItemSpawner:chestItemSpawners){
            if(ipmChestItemSpawner.isCheckFirework(firework));
                return ipmChestItemSpawner;
        }
        return null;
    }

    public Expert getExpert() {
        return expert;
    }
}
