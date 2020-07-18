package com.ibs.oldman.pushcar.command;

import com.ibs.oldman.pushcar.Main;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TestCommands implements CommandExecute {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = (Player) sender;
        if(args[1].equalsIgnoreCase("fire")){
            Block block = player.getLocation().getBlock();
            block.setType(Material.BEACON);
            Beacon beacon = (Beacon) player.getLocation().getBlock().getState();
            beacon.setPrimaryEffect(PotionEffectType.HEAL);
            System.out.println(beacon.getTier());
        }
        return true;
    }
}
