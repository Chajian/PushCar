package com.ibs.oldman.pushcar.command;

import com.ibs.oldman.pushcar.Main;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TestCommands implements CommandExecute {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if(args[1].equalsIgnoreCase("fire")){

//            firework.getFireworkMeta().setPower(1);
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    firework.detonate();
//                    System.out.println(firework.getLocation().toString());
//                }
//            }.runTaskLater(Main.getMain(),20L);

//            firework.setVelocity(new Vector(x,y,z));
        }
        return true;
    }
}
