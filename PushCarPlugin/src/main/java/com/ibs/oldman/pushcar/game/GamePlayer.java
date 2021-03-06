package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.game.ExpertType;
import com.ibs.oldman.pushcar.lib.nms.entity.PlayerUtils;
import com.ibs.oldman.pushcar.utils.BungeeUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

/**
 * 游戏玩家
 * @author yezi
 */
public class GamePlayer  {

    public final Player player;
    private Game game = null;
    private String latestGame = null;
    private StoredInventory oldInventory = new StoredInventory();
    private ExpertType expertType = ExpertType.SWORD;
    public boolean isSpectator = false;
    public boolean isTeleportingFromGame_justForInventoryPlugins = false;


    public GamePlayer(Player player) {
        this.player = player;
    }

    /*
    修改参与的游戏
     */
    public synchronized void changeGame(Game game) {
        if (this.game != null && game == null) {
            this.game.internalLeavePlayer(this);
            this.game = null;
            this.isSpectator = false;
            this.clean();
            if (Game.isBungeeEnabled()) {
                BungeeUtils.movePlayerToBungeeServer(player, Main.isDisabling());
            } else {
                this.restoreInv();
            }
        } else if (this.game == null && game != null) {
            this.storeInv();
            this.clean();
            this.game = game;
            this.isSpectator = false;
            this.game.internalJoinPlayer(this);
            this.latestGame = this.game.getName();
        } else if (this.game != null) {
            this.game.internalLeavePlayer(this);
            this.game = game;
            this.isSpectator = false;
            this.clean();
            this.game.internalJoinPlayer(this);
            this.latestGame = this.game.getName();
        }
    }

    public Game getGame() {
        return game;
    }

    public String getLatestGameName() {
        return this.latestGame;
    }

    public boolean isInGame() {
        return game != null;
    }

    public boolean canJoinFullGame() {
        return player.hasPermission("bw.vip.forcejoin");
    }

    /*
    存储背包
     */
    public void storeInv() {
        oldInventory.inventory = player.getInventory().getContents();
        oldInventory.armor = player.getInventory().getArmorContents();
        oldInventory.xp = player.getExp();
        oldInventory.effects = player.getActivePotionEffects();
        oldInventory.mode = player.getGameMode();
        oldInventory.leftLocation = player.getLocation();
        oldInventory.level = player.getLevel();
        oldInventory.listName = player.getPlayerListName();
        oldInventory.displayName = player.getDisplayName();
        oldInventory.foodLevel = player.getFoodLevel();
    }

    /*
    恢复背包
     */
    public void restoreInv() {
        isTeleportingFromGame_justForInventoryPlugins = true;
        if (!Main.getConfigurator().config.getBoolean("mainlobby.enabled")) {
            teleport(oldInventory.leftLocation);
        }

        player.getInventory().setContents(oldInventory.inventory);
        player.getInventory().setArmorContents(oldInventory.armor);

        player.addPotionEffects(oldInventory.effects);
        player.setLevel(oldInventory.level);
        player.setExp(oldInventory.xp);
        player.setFoodLevel(oldInventory.foodLevel);

        for (PotionEffect e : player.getActivePotionEffects())
            player.removePotionEffect(e.getType());

        player.addPotionEffects(oldInventory.effects);

        player.setPlayerListName(oldInventory.listName);
        player.setDisplayName(oldInventory.displayName);

        player.setGameMode(oldInventory.mode);

        if (oldInventory.mode == GameMode.CREATIVE)
            player.setAllowFlight(true);
        else
            player.setAllowFlight(false);

        player.updateInventory();
        player.resetPlayerTime();
        player.resetPlayerWeather();
    }

    /*
    清理玩家背包和状态栏
     */
    public void clean() {
        PlayerInventory inv = this.player.getInventory();
        inv.setArmorContents(new ItemStack[4]);
        inv.setContents(new ItemStack[]{});

        this.player.setAllowFlight(false);
        this.player.setFlying(false);
        this.player.setExp(0.0F);
        this.player.setLevel(0);
        this.player.setSneaking(false);
        this.player.setSprinting(false);
        this.player.setFoodLevel(20);
        this.player.setSaturation(10);
        this.player.setExhaustion(0);
        this.player.setMaxHealth(20D);
        this.player.setHealth(this.player.getMaxHealth());
        this.player.setFireTicks(0);
        this.player.setGameMode(GameMode.SURVIVAL);

        if (this.player.isInsideVehicle()) {
            this.player.leaveVehicle();
        }

        for (PotionEffect e : this.player.getActivePotionEffects()) {
            this.player.removePotionEffect(e.getType());
        }

        this.player.updateInventory();
    }

    public ExpertType getExpertType() {
        return expertType;
    }

    public void setExpertType(ExpertType expertType) {
        this.expertType = expertType;
    }



    /*
        传送
         */
    public boolean teleport(Location location) {
        return PlayerUtils.teleportPlayer(player, location);
    }
}
