package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.lib.nms.util.MiscUtils;
import lang.I;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.ibs.oldman.pushcar.Main;

import static lang.I18n.i18nonly;

/**
 *重生保护
 */
public class RespawnProtection extends BukkitRunnable {
    private Game game;
    private Player player;
    private int length;
    private boolean running = true;

    public RespawnProtection(Game game, Player player, int seconds) {
        this.game = game;
        this.player = player;
        this.length = seconds;
    }

    @Override
    public void run() {
    	if (!running) return;
        if (length > 0) {
            MiscUtils.sendActionBarMessage(player, I.i18nonly("respawn_protection_remaining").replace("%time%", String.valueOf(this.length)));

        }
        if (length <= 0) {
            MiscUtils.sendActionBarMessage(player, I.i18nonly("respawn_protection_end"));
            game.removeProtectedPlayer(player);
            running = false;
        }
        length--;
    }

    public void runProtection() {
        runTaskTimerAsynchronously(Main.getMain(), 5L, 20L);
    }


}

