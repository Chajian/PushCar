package com.ibs.oldman.pushcar.config;

import com.ibs.oldman.pushcar.Main;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 配置器
 */
public class Configurator {
    public File configFile, shopFile, signsFile, recordFile, langFolder,dropitems,chestitems;
    public FileConfiguration config, recordConfig;

    public final File dataFolder;
    public final Main main;

    public Configurator(Main main) {
        this.dataFolder = main.getDataFolder();
        this.main = main;
    }

    public void createFiles() {
        dataFolder.mkdirs();

        configFile = new File(dataFolder, "config.yml");
        signsFile = new File(dataFolder, "sign.yml");
        recordFile = new File(dataFolder, "record.yml");
        langFolder = new File(dataFolder.toString(), "languages");
        dropitems = new File(dataFolder,"dropitems");
        chestitems = new File(chestitems,"chestitems");

        config = new YamlConfiguration();
        recordConfig = new YamlConfiguration();

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!signsFile.exists()) {
            try {
                signsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!recordFile.exists()) {
            try {
                recordFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!langFolder.exists()) {
            langFolder.mkdirs();

            File[] listOfFiles = dataFolder.listFiles();
            if (listOfFiles != null && listOfFiles.length > 0) {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().startsWith("messages_") && file.getName().endsWith(".yml")) {
                    	File dest = new File(langFolder, "language_" + file.getName().substring(9));
                    	file.renameTo(dest);
                    }
                }
            }
        }

        try {
            config.load(configFile);
            recordConfig.load(recordFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        String shopFileName = "shop.yml";
        if (config.getBoolean("turnOnExperimentalGroovyShop", false)) {
            shopFileName = "shop.groovy";
        }
        shopFile = new File(dataFolder, shopFileName);
        if (!shopFile.exists()) {
//            main.saveResource(shopFileName, false);
        }

        AtomicBoolean modify = new AtomicBoolean(true);

        checkOrSetConfig(modify, "locale", "en");
        checkOrSetConfig(modify,"hotbar.selector",4);
        checkOrSetConfig(modify, "scoreboard.title", "§a%game%§r - %time%");
        checkOrSetConfig(modify, "bossbar.game.color", "GREEN");
        checkOrSetConfig(modify, "bossbar.game.style", "SEGMENTED_20");
        checkOrSetConfig(modify, "bossbar.lobby.color", "YELLOW");
        checkOrSetConfig(modify, "bossbar.lobby.style", "SEGMENTED_20");
        checkOrSetConfig(modify, "items.jointeam", "COMPASS");
        checkOrSetConfig(modify,"expter.select","IRON_AXE");
        checkOrSetConfig(modify, "items.leavegame", "SLIME_BALL");
        checkOrSetConfig(modify, "items.startgame", "DIAMOND");
        checkOrSetConfig(modify, "items.shopback", "BARRIER");

        checkOrSetConfig(modify, "sounds.on_bed_destroyed", "ENTITY_ENDER_DRAGON_GROWL");
        checkOrSetConfig(modify, "sounds.on_countdown", "UI_BUTTON_CLICK");
        checkOrSetConfig(modify, "sounds.on_game_start", "ENTITY_PLAYER_LEVELUP");
        checkOrSetConfig(modify, "sounds.on_team_kill", "ENTITY_PLAYER_LEVELUP");
        checkOrSetConfig(modify, "sounds.on_player_kill", "ENTITY_PLAYER_BIG_FALL");
        checkOrSetConfig(modify, "sounds.on_item_buy", "ENTITY_ITEM_PICKUP");
        checkOrSetConfig(modify, "sounds.on_upgrade_buy", "ENTITY_EXPERIENCE_ORB_PICKUP");
        checkOrSetConfig(modify, "sounds.on_respawn_cooldown_wait", "UI_BUTTON_CLICK");
        checkOrSetConfig(modify, "sounds.on_respawn_cooldown_done", "ENTITY_PLAYER_LEVELUP");

        checkOrSetConfig(modify,"chat.send-death-messages-just-in-game",true);


        checkOrSetConfig(modify, "lobby-scoreboard.content", Arrays.asList(" ", "§fMap: §2%arena%",
                "§fPlayers: §2%players%§f/§2%maxplayers%","§fwaiting_time: §2%waitingtime%", " ", "§fWaiting ...", " "));
        checkOrSetConfig(modify,"running-scoreboard.content",Arrays.asList(" ", "§fMap: §2%teamname%",
                "§fTeam_Players: §2%players%","§fwaiting_time: §2%waitingtime%","§fkills: §2%kills%","§fscore: §2%scores%", " ", "§fWaiting ...", " "));

        checkOrSetConfig(modify, "items.shopcosmetic",
                Main.isLegacy() ? new ItemStack(Material.getMaterial("STAINED_GLASS_PANE"), 1, (short) 7)
                        : "GRAY_STAINED_GLASS_PANE");
        checkOrSetConfig(modify, "items.pageback", "ARROW");
        checkOrSetConfig(modify, "items.pageforward", "ARROW");
        checkOrSetConfig(modify, "items.team-select",
                Main.isLegacy() ? new ItemStack(Material.getMaterial("WOOL"), 1, (short) 1)
                        : "WHITE_WOOL");


        if (modify.get()) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //检查或设置配置
    private void checkOrSetConfig(AtomicBoolean modify, String path, Object value) {
        checkOrSet(modify, this.config, path, value);
    }

    //检查或设置
    private static void checkOrSet(AtomicBoolean modify, FileConfiguration config, String path, Object value) {
        if (!config.isSet(path)) {
            if (value instanceof Map) {
                config.createSection(path, (Map<?, ?>) value);
            } else {
                config.set(path, value);
            }
            modify.set(true);
        }
    }

    //读取定义的项目
    public ItemStack readDefinedItem(String item, String def) {
        ItemStack material = new ItemStack(Material.valueOf(def));

        if (config.isSet("items." + item)) {
            Object obj = config.get("items." + item);
            if (obj instanceof ItemStack) {
                material = (ItemStack) obj;
            } else {
                try {
                    material.setType(Material.valueOf((String) obj));
                } catch (IllegalArgumentException e) {
                }
            }
        }

        return material;
    }
}
