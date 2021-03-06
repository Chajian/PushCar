package com.ibs.oldman.pushcar.command;

import com.ibs.oldman.pushcar.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import static lang.I18n.i18nonly;
import static com.ibs.oldman.pushcar.command.BaseCommands.ADMIN_PERMISSION;
import static com.ibs.oldman.pushcar.command.BaseCommands.OTHER_STATS_PERMISSION;

/**
 * 帮助指令
 */
public class HelpCommands implements CommandExecute {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            sendHelp((Player) sender);
        } else if (sender instanceof ConsoleCommandSender) {
            sendConsoleHelp((ConsoleCommandSender) sender);
        }
        return true;
    }

    public void sendHelp(Player player) {
        player.sendMessage(i18nonly("help_title").replace("%version%", Main.getVersion()));
        player.sendMessage(i18nonly("help_bw_join"));
        player.sendMessage(i18nonly("help_bw_leave"));
        player.sendMessage(i18nonly("help_bw_rejoin"));
        player.sendMessage(i18nonly("help_bw_autojoin"));
        player.sendMessage(i18nonly("help_bw_list"));

        if (player.hasPermission(ADMIN_PERMISSION) || player.hasPermission(OTHER_STATS_PERMISSION)) {
            player.sendMessage(i18nonly("help_bw_stats_other"));
        } else {
            player.sendMessage(i18nonly("help_bw_stats"));
        }

        if (player.hasPermission(ADMIN_PERMISSION)) {
            player.sendMessage(i18nonly("help_bw_addholo"));
            player.sendMessage(i18nonly("help_bw_removeholo"));
            player.sendMessage(i18nonly("help_bw_mainlobby"));

            player.sendMessage(i18nonly("help_bw_admin_info"));
            player.sendMessage(i18nonly("help_bw_admin_add"));
            player.sendMessage(i18nonly("help_bw_admin_lobby"));
            player.sendMessage(i18nonly("help_bw_admin_spec"));
            player.sendMessage(i18nonly("help_bw_admin_pos1"));
            player.sendMessage(i18nonly("help_bw_admin_pos2"));
            player.sendMessage(i18nonly("help_bw_admin_pausecountdown"));
            player.sendMessage(i18nonly("help_bw_admin_minplayers"));
            player.sendMessage(i18nonly("help_bw_admin_time"));
            player.sendMessage(i18nonly("help_bw_admin_team_add"));
            player.sendMessage(i18nonly("help_bw_admin_team_color"));
            player.sendMessage(i18nonly("help_bw_admin_team_maxplayers"));
            player.sendMessage(i18nonly("help_bw_admin_team_spawn"));
            player.sendMessage(i18nonly("help_bw_admin_team_bed"));
            player.sendMessage(i18nonly("help_bw_admin_jointeam"));
            player.sendMessage(i18nonly("help_bw_admin_spawner_add"));
            player.sendMessage(i18nonly("help_bw_admin_spawner_reset"));
            player.sendMessage(i18nonly("help_bw_admin_store_add"));
            player.sendMessage(i18nonly("help_bw_admin_store_remove"));
            player.sendMessage(i18nonly("help_bw_admin_config"));
            player.sendMessage(i18nonly("help_bw_admin_arena_time"));
            player.sendMessage(i18nonly("help_bw_admin_arena_weather"));
            player.sendMessage(i18nonly("help_bw_admin_remove"));
            player.sendMessage(i18nonly("help_bw_admin_edit"));
            player.sendMessage(i18nonly("help_bw_admin_save"));
            player.sendMessage(i18nonly("help_bw_reload"));
        }
    }

    public void sendConsoleHelp(ConsoleCommandSender console) {
        console.sendMessage(i18nonly("help_title_console").replace("%version%", Main.getVersion()));
        console.sendMessage(i18nonly("help_bw_list"));
        console.sendMessage(i18nonly("help_bw_stats_other"));
        console.sendMessage(i18nonly("help_bw_reload"));
    }
}
