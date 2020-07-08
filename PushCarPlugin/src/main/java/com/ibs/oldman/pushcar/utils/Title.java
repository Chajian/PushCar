package com.ibs.oldman.pushcar.utils;

import com.ibs.oldman.pushcar.Main;
import org.bukkit.entity.Player;

/**
 * 标题
 */
public class Title {
    public static void send(Player player, String title, String subtitle) {
        int fadeIn = Main.getConfigurator().config.getInt("title.fadeIn");
        int stay = Main.getConfigurator().config.getInt("title.stay");
        int fadeOut = Main.getConfigurator().config.getInt("title.fadeOut");

        com.ibs.oldman.pushcar.lib.nms.title.Title.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }
}
