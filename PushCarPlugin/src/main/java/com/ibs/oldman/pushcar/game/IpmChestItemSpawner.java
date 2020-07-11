package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.Main;
import com.ibs.oldman.pushcar.api.game.Team;
import com.ibs.oldman.pushcar.config.Configurator;
import lang.I;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.simpleinventories.SimpleInventories;
import org.screamingsandals.simpleinventories.builder.FormatBuilder;
import org.screamingsandals.simpleinventories.inventory.Options;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
    * 天降空投
    *
    * 实现逻辑：
    * 当冷却结束就会发射一个烟花。
    * @see com.ibs.oldman.pushcar.listener.WorldEvent
    * 在世界监听的onChestFirework方法
    * 回调produceChest 和 switchBeacon方法
    * 来生成空投箱子和信标信息
 */
public class IpmChestItemSpawner implements com.ibs.oldman.pushcar.api.game.ChestItemSpawner {

    private int currentdown = -1;
    /*箱子内容*/
    private SimpleInventories inventory;
    private String name;
    private Location spawnLocation;
    private Location chestLocation;
    private List<ItemStack> itemStackList = new ArrayList<ItemStack>();
    private Team team;
    private Chest chest;
    /*烟花*/
    private Firework firework;
    private int presentCurrentdown = -1;
    private int[] nummbers = {1,2,3,4};
    private Options options;
    //飞行角度
    private double angle;

    public IpmChestItemSpawner(int currentdown, String name, Location spawnLocation, double angle,Game game,List<ItemStack> list) {
        this.currentdown = currentdown;
        this.name = name;
        this.spawnLocation = spawnLocation;
        this.angle = angle;
        this.itemStackList = list;
        this.presentCurrentdown = currentdown;
    }

    @Override
    public int getCurrentDown() {
        return currentdown;
    }

    public SimpleInventories getDropInventory() {
        return inventory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Location getSpawnLocation() {
        return spawnLocation;
    }

    @Override
    public Location getChestLocation() {
        return chestLocation;
    }

    @Override
    public Team getTeam() {
        return team;
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /*生成随机物品
    * 随机算法
    *
    * */
    @Override
    public List<ItemStack> getRandomItem() {
        Random random = new Random();
        int random_amount = nummbers[random.nextInt(nummbers.length+1)];
        List<ItemStack> random_items = new ArrayList<>();
        int heen_amount = itemStackList.size();
        Iterator iterator = itemStackList.iterator();
        while(iterator.hasNext()){
            if(heen_amount<=random_amount){
                random_items.add((ItemStack) iterator.next());
            }
            heen_amount--;
        }
        return random_items;
    }

    @Override
    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;
    }

    @Override
    public void setTeam(Team team) {
        this.team = team;
    }

    //从yml文件中读取物品
//    public void readIteam(File file){
//        YamlConfiguration yamlConfiguration = new YamlConfiguration();
//        try {
//            yamlConfiguration.load(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        } catch (InvalidConfigurationException e) {
//            e.printStackTrace();
//            return;
//        }
//        if(yamlConfiguration.isSet("items")){
//            List<Map<String,Object>> list = (List<Map<String, Object>>) yamlConfiguration.getList("items");
//            for(Map<String,Object>item:list){
//               ItemStack itemStack = new ItemStack(Material.valueOf((String) item.get("type")));
//               itemStackList.add(itemStack);
//            }
//        }
//    }

    /**
     * 发射烟花
     * @param world
     */
    public void launchFireWork(World world,Game game){
        if(presentCurrentdown>0){
            presentCurrentdown--;
        }
        else {
            //随机生成烟花的位置
            double y = Math.max(game.getPoint1().getY(),game.getPoint2().getY());
            double max_x = Math.max(game.getPoint1().getX(),game.getPoint2().getX());
            double min_x = Math.min(game.getPoint1().getX(),game.getPoint2().getX());
            double max_z = Math.max(game.getPoint1().getZ(),game.getPoint2().getZ());
            double min_z = Math.min(game.getPoint1().getZ(),game.getPoint2().getZ());
            int distance_x = (int) (max_x - min_x);
            int distance_z = (int) (max_z-min_z);
            Random random = new Random();
            int distance_randomx = random.nextInt(distance_x);
            int distance_randomz = random.nextInt(distance_z);
            double x = min_x+distance_randomx;
            double z = min_z+distance_randomz;
            Location firespawn = new Location(world,x,y,z);

            presentCurrentdown = currentdown;
            Firework firework = (Firework) world.spawnEntity(firespawn, EntityType.FIREWORK);
            FireworkEffect fireworkEffect = FireworkEffect.builder()
                    .flicker(false)
                    .trail(false)
                    .withColor(Color.GREEN)
                    .with(FireworkEffect.Type.BURST)
                    .build();
            firework.getFireworkMeta().addEffect(fireworkEffect);
            firework.setVelocity(new Vector(0,-10,0));
            this.firework = firework;
            Main.registerGameEntity(firework,game);
        }

    }

    @Override
    public void setCurrentDown(int time) {
        this.currentdown = time;
    }

    /**
     * 是否属于游戏的烟花
     * @param firework
     * @return
     */
    public boolean isCheckFirework(Firework firework){
        if(firework.getEntityId()==this.firework.getEntityId())
            return true;
        return false;
    }


    public void setDropInventory(SimpleInventories inventory) {
        this.inventory = inventory;
    }
}
