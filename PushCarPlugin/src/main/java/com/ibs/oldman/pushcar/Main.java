package com.ibs.oldman.pushcar;

import com.ibs.oldman.pushcar.api.PushCarApi;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.command.*;
import com.ibs.oldman.pushcar.game.EffectThread;
import com.ibs.oldman.pushcar.game.TeamColor;
import com.ibs.oldman.pushcar.config.Configurator;
import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GamePlayer;
import com.ibs.oldman.pushcar.lib.nms.util.ClassStorage;
import com.ibs.oldman.pushcar.listener.CarEvent;
import com.ibs.oldman.pushcar.listener.PlayerEvent;
import com.ibs.oldman.pushcar.listener.WorldEvent;
import com.ibs.oldman.pushcar.utils.ColorChanger;
import lang.I18n;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.screamingsandals.simpleinventories.listeners.InventoryListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends JavaPlugin implements PushCarApi {
    private static Main main;
    private Configurator configurator;

    /*禁用插件*/
    private static boolean isDisabling;
    private HashMap<Player, GamePlayer> playersInGame = new HashMap<>();//在游戏中的玩家
    private HashMap<Entity, Game> entitiesInGame = new HashMap<>();//在游戏中的实体
    HashMap<String, Game> games = new HashMap<>();
    private boolean isLegacy = false;
    private ColorChanger colorChanger;//换色器
    private static boolean isSpigot = false;
    int versionNumber = 0;
    String version;
    EffectThread effectThread = null;
    public static List<String> autoColoredMaterials = new ArrayList<>();//自动着色颜料

    static {
        // ColorChanger list of materials   换色器的用料
        autoColoredMaterials.add("WOOL");
        autoColoredMaterials.add("CARPET");
        autoColoredMaterials.add("CONCRETE");
        autoColoredMaterials.add("CONCRETE_POWDER");
        autoColoredMaterials.add("STAINED_CLAY"); // LEGACY ONLY
        autoColoredMaterials.add("TERRACOTTA"); // FLATTENING ONLY
        autoColoredMaterials.add("STAINED_GLASS");
        autoColoredMaterials.add("STAINED_GLASS_PANE");
    }

    @Override
    public void onDisable() {
        Bukkit.getServer().getLogger().info("卸载老汉推车插件");
        isDisabling = true;
//        if (signManager != null) {
//            signManager.save();
//        }
        for (Game game : games.values()) {
            game.stop();
        }
        this.getServer().getServicesManager().unregisterAll(this);
//        if (isHologramsEnabled() && hologramInteraction != null) {
//            hologramInteraction.unloadHolograms();
//        }
//
//        metrics = null;
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getLogger().info("加载老汉推车插件");
        main = this;
        configurator = new Configurator(main);
        colorChanger = new ColorChanger();
        configurator.createFiles();
        //粒子
        effectThread = new EffectThread();
        effectThread.runTaskTimer(this,500L,10L);

        I18n.load(this, configurator.config.getString("locale"));//加载插件语言配置信息
        CommandHandler commandHandler = new CommandHandler()
                .register("admin",new AdminCommands())
                .register("test",new TestCommands())
                .register("leave",new LeaveCommands())
                .register("help",new HelpCommands())
                .register("join",new JoinCommands());
        Bukkit.getServer().getPluginCommand("pushcar").setExecutor(commandHandler);

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerEvent(),this);
        Bukkit.getServer().getPluginManager().registerEvents(new CarEvent(),this);
        Bukkit.getServer().getPluginManager().registerEvents(new WorldEvent(),this);
        InventoryListener.init(this);

        //检测版本号
        String[] bukkitVersion = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        versionNumber = 0;
        version = this.getDescription().getVersion();

        for (int i = 0; i < 2; i++) {
            versionNumber += Integer.parseInt(bukkitVersion[i]) * (i == 0 ? 100 : 1);
        }
        isLegacy = versionNumber < 113;
        isSpigot = ClassStorage.IS_SPIGOT_SERVER;

        System.out.println("版本号"+versionNumber+": "+isLegacy);
        //初始化竞技场
        final File arenasFolder = new File(getDataFolder(), "arenas");//竞技场文件
        if (arenasFolder.exists()) {
            try (Stream<Path> stream = Files.walk(Paths.get(arenasFolder.getAbsolutePath()))) {
                final List<String> results = stream.filter(Files::isRegularFile)
                        .map(Path::toString)
                        .collect(Collectors.toList());

                if (results.isEmpty()) {
//                    Debug.info("No arenas have been found!", true);
                } else {
                    for (String result : results) {
                        File file = new File(result);
                        if (file.exists() && file.isFile()) {
                            Game.loadGame(file);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(); // maybe remove after testing
            }
        }

    }

    /**
     * 最高人数的比赛
     * @return
     */
    public com.ibs.oldman.pushcar.api.game.Game getGameWithHighestPlayers() {
        TreeMap<Integer, com.ibs.oldman.pushcar.api.game.Game> gameList = new TreeMap<>();
        for (com.ibs.oldman.pushcar.api.game.Game game : getGames().values()) {
            if (game.getStatus() != GameStatus.WAITING) {
                continue;
            }
            if (game.getConnectedPlayers().size() >= game.getMaxPlayers()) {
                continue;
            }
            gameList.put(game.countConnectedPlayers(), game);
        }

        Map.Entry<Integer, com.ibs.oldman.pushcar.api.game.Game> lastEntry = gameList.lastEntry();
        return lastEntry.getValue();
    }

    public static boolean isFarmBlock(Material mat) {
        if (main.configurator.config.getBoolean("farmBlocks.enable",false)) {
            List<String> list = (List<String>) main.configurator.config.getList("farmBlocks.blocks");
            return list.contains(mat.name());
        }
        return false;
    }

    public static boolean isBreakableBlock(Material mat) {
        if (main.configurator.config.getBoolean("breakable.enabled",false)) {
            List<String> list = (List<String>) main.configurator.config.getList("breakable.blocks");
            boolean asblacklist = main.configurator.config.getBoolean("breakable.asblacklist", false);
            return list.contains(mat.name()) ? !asblacklist : asblacklist;
        }
        return false;
    }

    public HashMap<String, Game> getGames() {
        return games;
    }


    public static Main getMain() {
        return main;
    }

    public boolean isEntityInGame(Entity entity) {
        return entitiesInGame.containsKey(entity);
    }

    public static Game getGame(String string) {
        return main.games.get(string);
    }

    public static boolean isGameExists(String string) {
        return main.games.containsKey(string);
    }


    public static boolean isLegacy() {
        return main.isLegacy;
    }

    public static Configurator getConfigurator() {
        return main.configurator;
    }

    /*将玩家转换成Gameplayer*/
    public static GamePlayer getPlayerGameProfile(Player player) {
        if (main.playersInGame.containsKey(player))
            return main.playersInGame.get(player);
        GamePlayer gPlayer = new GamePlayer(player);
        main.playersInGame.put(player, gPlayer);
        return gPlayer;
    }

    public static Game getInGameEntity(Entity entity) {
        return main.entitiesInGame.getOrDefault(entity, null);
    }

    /**
     * 获取玩家所在的游戏对象
     * @param player 玩家
     * @return 如果玩家在任何游戏竞技场内就返回所在游戏对象。否则返回null
     */
    public Game getPlayerGame(Player player){
        if(main.isPlayerInGame(player)){
            for(Game game:games.values()){
                if(game.isPlayerInAnyTeam(player))
                    return game;
            }
        }
        return null;
    }


    public static boolean isDisabling() {
        return isDisabling;
    }

    public static boolean isSpigot() {
        return isSpigot;
    }

    public static void unregisterGameEntity(Entity entity) {
        main.entitiesInGame.remove(entity);
    }

    public static void registerGameEntity(Entity entity, Game game) {
        main.entitiesInGame.put(entity, game);
    }

    public HashMap<Player, GamePlayer> getPlayersInGame() {
        return playersInGame;
    }

    public static List<Entity> getGameEntities(Game game) {
        List<Entity> entityList = new ArrayList<>();
        for (Map.Entry<Entity, Game> entry : main.entitiesInGame.entrySet()) {
            if (entry.getValue() == game) {
                entityList.add(entry.getKey());
            }
        }
        return entityList;
    }
    public static List<String> getGameNames() {
        List<String> list = new ArrayList<>();
        for (Game game : main.games.values()) {
            list.add(game.getName());
        }
        return list;
    }

    public static ItemStack applyColor(TeamColor color, ItemStack itemStack) {
        return applyColor(color, itemStack, false);
    }

    public static ItemStack applyColor(TeamColor color, ItemStack itemStack, boolean clone) {
        com.ibs.oldman.pushcar.api.game.TeamColor teamColor = color.toApiColor();
        if (clone) {
            itemStack = itemStack.clone();
        }
        return main.getColorChanger().applyColor(teamColor, itemStack);
    }

    public static ItemStack applyColor(com.ibs.oldman.pushcar.api.game.TeamColor teamColor, ItemStack itemStack) {
        return main.getColorChanger().applyColor(teamColor, itemStack);
    }

    public static boolean isPlayerInGame(Player player) {
        if (main.playersInGame.containsKey(player))
            return main.playersInGame.get(player).isInGame();
        return false;
    }

    //判断矿车是否在游戏中
    public static boolean isCartInGame(Entity entity){
        for(Game game : main.games.values())
            if(game.isCartInTeam(entity))
                return true;
        return false;
    }

    //通过烟花获取game对象
    public static Game getGameByFirework(Firework firework){
        for(Game game:main.games.values()){
            if(game.isFireworkInGame(firework))
                return game;
        }
        return null;
    }

    public static boolean isFireworkInGame(Firework firework){
        for(Game game:main.games.values()){
            if(game.isFireworkInGame(firework))
                return true;
        }
        return false;
    }

    public static void addGame(Game game) {
        main.games.put(game.getName(), game);
    }


    public ColorChanger getColorChanger() {
        return colorChanger;
    }

    public static int getVersionNumber() {
        return main.versionNumber;
    }

    public static String getVersion() {
        return main.version;
    }
}
