package com.ibs.oldman.pushcar.lib.nms.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static  com.ibs.oldman.pushcar.lib.nms.util.ClassStorage.*;
import static  com.ibs.oldman.pushcar.lib.nms.util.ClassStorage.NMS.*;

public class PlayerUtils {
	public static void respawn(Plugin instance, Player player, long delay) {
		new BukkitRunnable() {

			@Override
			public void run() {
				try {
					player.spigot().respawn();
				} catch (Throwable t) {
					try {
						Object selectedObj = findEnumConstant(EnumClientCommand, "PERFORM_RESPAWN");
						Object packet = PacketPlayInClientCommand.getDeclaredConstructor(EnumClientCommand)
							.newInstance(selectedObj);
						Object connection = getPlayerConnection(player);
						getMethod(connection, "a,func_147342_a", PacketPlayInClientCommand).invoke(packet);
					} catch (Throwable ignored) {
						t.printStackTrace();
					}
				}
			}
		}.runTaskLater(instance, delay);
	}

	public static void fakeExp(Player player, float percentage, int levels) {
		try {
			Object packet = PacketPlayOutExperience.getConstructor(float.class, int.class, int.class)
				.newInstance(percentage, player.getTotalExperience(), levels);
			sendPacket(player, packet);
		} catch (Throwable t) {
		}
	}

	public static boolean teleportPlayer(Player player, Location location) {
		try {
			return player.teleportAsync(location).isDone();
		} catch (Throwable t) {
			player.teleport(location);
			return true;
		}
	}
}
