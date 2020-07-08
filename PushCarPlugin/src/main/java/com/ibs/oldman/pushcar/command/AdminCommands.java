package com.ibs.oldman.pushcar.command;

import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GameCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static lang.I18n.i18n;
import static lang.I18n.i18nonly;

public class AdminCommands implements CommandExecute {

    static List<GameCreator> list = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if(args.length == 3 && args[2].equals("add")){
            GameCreator gameCreator = new GameCreator(Game.createGame(args[1]));
            list.add(gameCreator);
            System.out.println(i18n("arena_added"));
        }
        else if(args.length >=3){
            GameCreator gameCreator = getGameCreatorByName(args[1]);
            if(gameCreator!=null){
                String[] argss = new String[0];
                if(args.length >3)
                    argss = Arrays.copyOfRange(args,3,args.length);
                gameCreator.cmd((Player) sender,args[2],argss);
            }
        }
        else if(args.length >= 1 && args.length <=2){
            player.sendMessage(i18nonly("help_bw_admin_info"));
            player.sendMessage(i18nonly("help_bw_admin_add"));
            player.sendMessage(i18nonly("help_bw_admin_lobby"));
            player.sendMessage(i18nonly("help_bw_admin_spec"));
            player.sendMessage(i18nonly("help_bw_admin_pos1"));
            player.sendMessage(i18nonly("help_bw_admin_pos2"));
            player.sendMessage(i18nonly("help_bw_admin_pausecountdown"));
            player.sendMessage(i18nonly("help_bw_admin_minplayers"));
            player.sendMessage(i18nonly("help_bw_admin_time"));
            player.sendMessage(i18nonly("help_bw_admin_team_add"));
            player.sendMessage(i18nonly("help_bw_admin_team_color"));
            player.sendMessage(i18nonly("help_bw_admin_team_maxplayers"));
            player.sendMessage(i18nonly("help_bw_admin_team_spawn"));
            player.sendMessage(i18nonly("help_bw_admin_team_bed"));
            player.sendMessage(i18nonly("help_bw_admin_jointeam"));
            player.sendMessage(i18nonly("help_bw_admin_spawner_add"));
            player.sendMessage(i18nonly("help_bw_admin_spawner_reset"));
            player.sendMessage(i18nonly("help_bw_admin_store_add"));
            player.sendMessage(i18nonly("help_bw_admin_store_remove"));
            player.sendMessage(i18nonly("help_bw_admin_config"));
            player.sendMessage(i18nonly("help_bw_admin_arena_time"));
            player.sendMessage(i18nonly("help_bw_admin_arena_weather"));
            player.sendMessage(i18nonly("help_bw_admin_remove"));
            player.sendMessage(i18nonly("help_bw_admin_edit"));
            player.sendMessage(i18nonly("help_bw_admin_save"));
        }

        return false;
    }

    public GameCreator getGameCreatorByName(String name){
        for(GameCreator gameCreator:list){
            if(gameCreator.getGame().getGame_name().equals(name))
                return gameCreator;
        }
        return null;
    }
}
