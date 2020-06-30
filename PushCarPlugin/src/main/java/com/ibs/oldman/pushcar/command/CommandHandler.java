package com.ibs.oldman.pushcar.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandHandler implements CommandExecutor {

    public static final HashMap<String, CommandExecute> COMMANDS = new HashMap<>();

    public CommandHandler register(String name, CommandExecute cmd){
        COMMANDS.put(name,cmd);
        return this;
    }
    //指令是否存在
    public boolean exists(String name) {
        return COMMANDS.containsKey(name);
    }
    //通过名称后去执行者
    public CommandExecute getExecutor(String name) {
        return COMMANDS.get(name);
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        //If there aren't any arguments, what is the command name going to be?
        if (strings.length == 0) {
            getExecutor("effect").onCommand(commandSender, command, s, strings);
            return true;
        }

        //What if there are arguments in the command? Such as /example args
        // implicit if (args.length > 0)

        //If that argument exists in our registration in the onEnable();
        if (exists(strings[0])) {

            //Get The executor with the name of args[0].
            getExecutor(strings[0]).onCommand(commandSender, command, s, strings);
            return true;
        } else {
            //We want to send a message to the sender if the command doesn't exist.
//            Message.COMMAND_DOES_NOT_EXIST_ERROR.send(sender);
            return true;
        }
    }
}
