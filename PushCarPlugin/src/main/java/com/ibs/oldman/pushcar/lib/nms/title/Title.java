package com.ibs.oldman.pushcar.lib.nms.title;

import org.bukkit.entity.Player;

import static com.ibs.oldman.pushcar.lib.nms.util.ClassStorage.*;
import static com.ibs.oldman.pushcar.lib.nms.util.ClassStorage.NMS.*;

public class Title {
	public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		try {
			player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
		} catch (Throwable t) {
			try {
				Object titleComponent = getMethod(ChatSerializer, "a,field_150700_a", String.class)
					.invokeStatic("{\"text\": \"" + title + "\"}");
				Object subtitleComponent = getMethod(ChatSerializer, "a,field_150700_a", String.class)
					.invokeStatic("{\"text\": \"" + subtitle + "\"}");
				
				Object titlePacket = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent)
					.newInstance(findEnumConstant(EnumTitleAction, "TITLE"), titleComponent);
				Object subtitlePacket = PacketPlayOutTitle.getConstructor(EnumTitleAction, IChatBaseComponent)
					.newInstance(findEnumConstant(EnumTitleAction, "SUBTITLE"), subtitleComponent);
				Object timesPacket = PacketPlayOutTitle
					.getConstructor(EnumTitleAction, IChatBaseComponent, int.class, int.class, int.class)
					.newInstance(findEnumConstant(EnumTitleAction, "TIMES"), null, fadeIn, stay, fadeOut);
				
				sendPacket(player, titlePacket);
				sendPacket(player, subtitlePacket);
				sendPacket(player, timesPacket);
			} catch (Throwable ignored) {
			}
		}
	}
}
