package com.ibs.oldman.pushcar.inventory;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.game.Expert;
import com.ibs.oldman.pushcar.api.game.ExpertType;
import com.ibs.oldman.pushcar.game.Game;
import com.ibs.oldman.pushcar.game.GamePlayer;
import com.ibs.oldman.pushcar.game.Team;
import lang.I;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.screamingsandals.simpleinventories.SimpleInventories;
import org.screamingsandals.simpleinventories.builder.FormatBuilder;
import org.screamingsandals.simpleinventories.events.PostActionEvent;
import org.screamingsandals.simpleinventories.inventory.GuiHolder;
import org.screamingsandals.simpleinventories.inventory.Options;
import org.screamingsandals.simpleinventories.utils.MapReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static lang.I18n.*;
/**
 * 职业选择
 */
public class ExpertSelectorInventory implements Listener {
    private Game game;
    private SimpleInventories simpleGuiFormat;
    private Options options;
    private List<Player> openedForPlayers = new ArrayList<>();


    public ExpertSelectorInventory(Main plugin, Game game) {
        this.game = game;

        options = new Options(Main.getMain());
        options.setPrefix("选择一个职业");//设置gui标题
        options.setShowPageNumber(false);
        options.setRender_header_start(54); // Disable header
        options.setRender_offset(0);
        int teamCount = ExpertType.values().length;
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

        ItemStack stack = null;
//        ItemStack stack = Main.getConfigurator().readDefinedItem("team-select",  "WHITE_WOOL");

        for (ExpertType expertType : ExpertType.values()) {
            stack = new ItemStack(expertType.getShowmaterial());
            ItemMeta teamMeta = stack.getItemMeta();

//            List<GamePlayer> playersInTeam = game.getPlayersInTeam(team);
//            int playersInTeamCount = playersInTeam.size();

            teamMeta.setDisplayName(expertType.getName());
            teamMeta.setLore(Arrays.asList(new String[]{expertType.getDescription()}));
            stack.setItemMeta(teamMeta);

            builder.add(stack).set("expert", expertType);
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
        GamePlayer player = Main.getPlayerGameProfile(event.getPlayer());
        MapReader reader = event.getItem().getReader();
        if (reader.containsKey("expert")) {
            ExpertType expert = (ExpertType) reader.get("expert");

//            game.selectTeam(Main.getPlayerGameProfile(player), expert.getName());
            player.setExpertType(expert);

            player.player.closeInventory();
            player.player.sendMessage(i18n("expter_selected").replace("%expter%",expert.getName()));

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
