package com.ibs.oldman.pushcar.command;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GamePlayer;
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
        GamePlayer playergame = Main.getPlayerGameProfile(player);
        if(args.length == 1 && Main.isPlayerInGame(player)){
            playergame.changeGame(null);
            return true;
        }
        return true;
    }
}
