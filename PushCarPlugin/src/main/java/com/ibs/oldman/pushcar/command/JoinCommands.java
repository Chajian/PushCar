package com.ibs.oldman.pushcar.command;

import com.ibs.oldman.pushcar.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static lang.I18n.i18n;

public class JoinCommands implements CommandExecute {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if (Main.isPlayerInGame(player)) {
            player.sendMessage(("you_are_already_in_some_game"));
            return true;
        }

        if (args.length >= 1) {
            String arenaN = args[0];
            if (Main.isGameExists(arenaN)) {
                Main.getGame(arenaN).joinToGame(player);
            } else {
                player.sendMessage(i18n("no_arena_found"));
            }
        } else {
            Main.getMain().getGameWithHighestPlayers().joinToGame(player);
            return true;
        }
        return true;
    }
}
