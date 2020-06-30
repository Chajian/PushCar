package com.ibs.oldman.pushcar.command;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TestCommands implements CommandExecute  {

    static List<Location> list = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 3){
            Player player = (Player) sender;
            if(args[1].equals("get")&&args[2].equals("location")){
                list.add(player.getLocation());
                player.sendMessage("发送成功");
            }
            else if(args[1].equals("local") && args[2].equals("Block")){
                for(Location location:list){
                    location.getBlock().setType(Material.BLACK_WOOL);
                    location.getBlock().getChunk().load();
                    player.sendMessage("发送成功");
                }
            }
            else if(args[1].equals("unlocal") && args[2].equals("Block")){
                for(Location location:list){
//                    location.getBlock().setType(Material.BLACK_WOOL);
                    location.getBlock().setType(Material.AIR);
                    location.getBlock().getChunk().unload();
                    player.sendMessage("发送成功");
                }
            }
        }
        return false;
    }
}
