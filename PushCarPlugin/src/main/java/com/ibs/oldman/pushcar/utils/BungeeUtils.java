package com.ibs.oldman.pushcar.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.ibs.oldman.pushcar.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 蹦极工具
 */
public class BungeeUtils {
    public static void movePlayerToBungeeServer(Player player, boolean serverRestart) {
        if (serverRestart) {
            internalMove(player);
            return;
        }

        new BukkitRunnable() {
            public void run() {
               internalMove(player);
            }
        }.runTask(Main.getMain());
    }

    public static void sendPlayerBungeeMessage(Player player, String string) {
        new BukkitRunnable() {
            public void run() {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();

                out.writeUTF("Message");
                out.writeUTF(player.getName());
                out.writeUTF(string);

                Bukkit.getServer().sendPluginMessage(Main.getMain(), "BungeeCord", out.toByteArray());
            }
        }.runTaskLater(Main.getMain(), 30L);
    }

    private static void internalMove(Player player) {
        String server = Main.getConfigurator().config.getString("bungee.server");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(Main.getMain(), "BungeeCord", out.toByteArray());
    }
}