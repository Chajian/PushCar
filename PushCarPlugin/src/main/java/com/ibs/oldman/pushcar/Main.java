package com.ibs.oldman.pushcar;

import com.ibs.oldman.pushcar.api.PushCarApi;
import com.ibs.oldman.pushcar.api.game.GameStatus;
import com.ibs.oldman.pushcar.command.JoinCommands;
import com.ibs.oldman.pushcar.game.TeamColor;
import com.ibs.oldman.pushcar.command.CommandHandler;
import com.ibs.oldman.pushcar.command.TestCommands;
import com.ibs.oldman.pushcar.config.Configurator;
import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GamePlayer;
import com.ibs.oldman.pushcar.utils.ColorChanger;
import lang.I18n;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

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
    }

    @Override
    public void onEnable() {
        Bukkit.getServer().getLogger().info("加载老汉推车插件");
        main = this;
        configurator = new Configurator(main);
        configurator.createFiles();
        CommandHandler commandHandler = new CommandHandler()
                .register("test",new TestCommands())
                .register("join",new JoinCommands());
        Bukkit.getServer().getPluginCommand("pushcar").setExecutor(commandHandler);

        //检测版本号
        String[] bukkitVersion = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        versionNumber = 0;

        for (int i = 0; i < 2; i++) {
            versionNumber += Integer.parseInt(bukkitVersion[i]) * (i == 0 ? 100 : 1);
        }

        isLegacy = versionNumber < 113;

        I18n.load(this, configurator.config.getString("locale"));//加载插件语言配置信息


    }

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

    public HashMap<String, Game> getGames() {
        return games;
    }


    public static Main getMain() {
        return main;
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

    public static boolean isDisabling() {
        return isDisabling;
    }

    public static boolean isSpigot() {
        return isSpigot();
    }

    public static void unregisterGameEntity(Entity entity) {
        main.entitiesInGame.remove(entity);
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

    public static void addGame(Game game) {
        main.games.put(game.getName(), game);
    }


    public ColorChanger getColorChanger() {
        return colorChanger;
    }

    public static int getVersionNumber() {
        return main.versionNumber;
    }
}
