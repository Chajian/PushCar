package com.ibs.oldman.pushcar.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BaseCommands implements CommandExecute {
    public static final String ADMIN_PERMISSION = "misat11.bw.admin";
    public static final String OTHER_STATS_PERMISSION = "misat11.bw.otherstats";
    String name;
    String permission;
    boolean allowConsole;

    public BaseCommands(String name, String permission, boolean allowConsole) {
        this.name = name;
        this.permission = permission;
        this.allowConsole = allowConsole;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return false;
    }
}
