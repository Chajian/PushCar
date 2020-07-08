package com.ibs.oldman.pushcar.command;

import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GameCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestCommands implements CommandExecute  {

    static List<GameCreator> list = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 4 && args[1].equals("add") && args[2].equals("arena")){
            GameCreator gameCreator = new GameCreator(Game.createGame(args[3]));
            list.add(gameCreator);
            System.out.println("添加成功");
        }
        else{
            GameCreator gameCreator = getGameCreatorByName(args[2]);
            if(gameCreator!=null){
                String[] argss = Arrays.copyOfRange(args,3,args.length);
                gameCreator.cmd((Player) sender,args[1],argss);
                System.out.println("执行成功!");
            }
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
