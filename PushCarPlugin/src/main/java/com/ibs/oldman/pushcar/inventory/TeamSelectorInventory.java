package com.ibs.oldman.pushcar.inventory;

import lang.I;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.ibs.oldman.pushcar.Main;
//import org.screamingsandals.bedwars.api.events.BedwarsOpenTeamSelectionEvent;
//import org.screamingsandals.bedwars.api.events.BedwarsPlayerLeaveEvent;
import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GamePlayer;
import com.ibs.oldman.pushcar.game.Team;
import org.screamingsandals.simpleinventories.SimpleInventories;
import org.screamingsandals.simpleinventories.builder.FormatBuilder;
import org.screamingsandals.simpleinventories.events.PostActionEvent;
import org.screamingsandals.simpleinventories.inventory.GuiHolder;
import org.screamingsandals.simpleinventories.inventory.Options;
import org.screamingsandals.simpleinventories.utils.MapReader;

import java.util.ArrayList;
import java.util.List;

import lang.I18n.*;

//团队选择器监听
public class TeamSelectorInventory implements Listener {
    private Game game;
    private SimpleInventories simpleGuiFormat;
    private Options options;
    private List<Player> openedForPlayers = new ArrayList<>();

    public TeamSelectorInventory(Main plugin, Game game) {
        this.game = game;

        options = new Options(Main.getMain());
        options.setPrefix(I.i18nonly("team_selection_name", "Select team - %arena%").replace("%arena%", game.getName()));
        options.setShowPageNumber(false);
        options.setRender_header_start(54); // Disable header
        options.setRender_offset(0);
        int teamCount = game.getTeams().size();
        if (teamCount <= 9) {
            options.setRender_actual_rows(1);
        } else if (teamCount <= 18) {
            options.setRender_actual_rows(2);
        }

        createData();

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void destroy() {
        openedForPlayers.clear();
        HandlerList.unregisterAll(this);
    }

    public void openForPlayer(Player player) {
//        BedwarsOpenTeamSelectionEvent event = new BedwarsOpenTeamSelectionEvent(this.game, player);
//        Main.getInstance().getServer().getPluginManager().callEvent(event);
//
//        if (event.isCancelled()) {
//            return;
//        }

        createData();
        simpleGuiFormat.openForPlayer(player);
        openedForPlayers.add(player);
    }

    private void createData() {
    	SimpleInventories simpleGuiFormat = new SimpleInventories(options);
        FormatBuilder builder = new FormatBuilder();
        
        ItemStack stack = Main.getConfigurator().readDefinedItem("team-select", Main.isLegacy() ? "WOOL" : "WHITE_WOOL");
        
        for (Team team : game.getTeams().values()) {
            ItemStack teamStack = Main.applyColor(team.teamColor, stack, true);
            ItemMeta teamMeta = teamStack.getItemMeta();

            List<GamePlayer> playersInTeam = game.getPlayersInTeam(team);
            int playersInTeamCount = playersInTeam.size();

            teamMeta.setDisplayName(I.i18nonly("team_select_item")
                    .replace("%teamName%", team.teamColor.chatColor + team.getName())
                    .replace("%inTeam%", String.valueOf(playersInTeamCount))
                    .replace("%maxInTeam%", String.valueOf(team.maxplayers)));
            teamMeta.setLore(formatLore(team, game));
            teamStack.setItemMeta(teamMeta);

            builder.add(teamStack).set("team", team);
        }

        simpleGuiFormat.load(builder);
        simpleGuiFormat.generateData();

        this.simpleGuiFormat = simpleGuiFormat;
    }

    private List<String> formatLore(Team team, Game game) {
        List<String> loreList = new ArrayList<>();
        List<GamePlayer> playersInTeam = game.getPlayersInTeam(team);
        int playersInTeamCount = playersInTeam.size();

        if (playersInTeamCount >= team.maxplayers) {
            loreList.add(team.teamColor.chatColor + I.i18nonly("team_select_item_lore_full"));
        } else {
            loreList.add(team.teamColor.chatColor + I.i18nonly("team_select_item_lore_join"));
        }

        if (!playersInTeam.isEmpty()) {
            loreList.add(I.i18nonly("team_select_item_lore"));

            for (GamePlayer gamePlayer : playersInTeam) {
                loreList.add(team.teamColor.chatColor + gamePlayer.player.getDisplayName());
            }
        }

        return loreList;
    }

    private void repaint() {
        for (Player player : openedForPlayers) {
            GuiHolder guiHolder = simpleGuiFormat.getCurrentGuiHolder(player);
            if (guiHolder == null) {
                return;
            }

            createData();
            guiHolder.setFormat(simpleGuiFormat);
            guiHolder.repaint();
        }
    }

    @EventHandler
    public void onPostAction(PostActionEvent event) {
        if (event.getFormat() != simpleGuiFormat) {
            return;
        }

        Player player = event.getPlayer();
        MapReader reader = event.getItem().getReader();
        if (reader.containsKey("team")) {
            Team team = (Team) reader.get("team");
            game.selectTeam(Main.getPlayerGameProfile(player), team.getName());
            player.closeInventory();

            repaint();
            openedForPlayers.remove(player);
        }
    }

//    @EventHandler
//    public void onPlayerLeave(BedwarsPlayerLeaveEvent event) {
//        if (event.getGame() != game) {
//            return;
//        }
//        repaint();
//    }
}
