package com.ibs.oldman.pushcar;

import com.ibs.oldman.pushcar.api.PushCarApi;
import com.ibs.oldman.pushcar.command.CommandHandler;
import com.ibs.oldman.pushcar.command.TestCommands;
import com.ibs.oldman.pushcar.config.Configurator;
import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class Main extends JavaPlugin implements PushCarApi {
    private static Main main;
    private Configurator configurator;

    /*禁用插件*/
    private static boolean isDisabling;
    private HashMap<Player, GamePlayer> playersInGame = new HashMap<>();//在游戏中的玩家
    private HashMap<Entity, Game> entitiesInGame = new HashMap<>();//在游戏中的实体
    HashMap<String, Game> games = new HashMap<>();
    private boolean isLegacy = false;
    int versionNumber = 0;

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

        CommandHandler commandHandler = new CommandHandler()
                .register("test",new TestCommands());
        Bukkit.getServer().getPluginCommand("pushcar").setExecutor(commandHandler);

        //检测版本号
        String[] bukkitVersion = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        versionNumber = 0;

        for (int i = 0; i < 2; i++) {
            versionNumber += Integer.parseInt(bukkitVersion[i]) * (i == 0 ? 100 : 1);
        }

        isLegacy = versionNumber < 113;

    }

    public HashMap<String, Game> getGames() {
        return games;
    }


    public static Main getMain() {
        return main;
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
}
