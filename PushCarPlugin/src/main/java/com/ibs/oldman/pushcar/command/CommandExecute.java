package com.ibs.oldman.pushcar.command;

import org.bukkit.command.CommandSender;

public interface CommandExecute {
    boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args);

}
