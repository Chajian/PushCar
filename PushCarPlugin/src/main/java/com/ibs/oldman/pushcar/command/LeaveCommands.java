package com.ibs.oldman.pushcar.command;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.game.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 离开游戏指令
 */
public class LeaveCommands implements CommandExecute {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        Game game = Main.getMain().getPlayerGame(player);
        if(args.length == 1){
            game.leaveFromGame(player);
            return true;
        }
        return true;
    }
}
