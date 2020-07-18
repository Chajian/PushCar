package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.api.game.ExpertType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

/**
 * 职业
 */
public class Expert implements com.ibs.oldman.pushcar.api.game.Expert {
    /*职业类型*/
    private ExpertType type;
    /*装备*/
    private List<ItemStack> equipments = new ArrayList<>();
    private Map<String,Integer> cooltime = new HashMap<>();
    private Map<String,Integer> originCooltime = new HashMap<>();

    @Override
    public ItemStack[] getEquipment() {
        return (ItemStack[]) equipments.toArray();
    }

    @Override
    public void upLevel() {

    }

    @Override
    public void setLevel() {

    }


    /**
     * 生成职业装备
     * @param teamForJoin
     */
    public void generateEquipment(Team teamForJoin, GamePlayer player) {
        //皮革套装
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);//胸甲
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);//头盔
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);//靴子
        ItemStack pants = new ItemStack(Material.LEATHER_LEGGINGS);//裤子
        LeatherArmorMeta meta = (LeatherArmorMeta) chestplate.getItemMeta();
        meta.setColor(teamForJoin.teamColor.leatherColor);
        chestplate.setItemMeta(meta);
        helmet.setItemMeta(meta);
        boots.setItemMeta(meta);
        pants.setItemMeta(meta);

        //职业专属装备
        ItemStack arms = null;
        ItemStack other = null;
        ItemMeta meta_arms = null;
        type = player.getExpertType();
        switch (type){
            case SWORD:
                arms = new ItemStack(Material.IRON_SWORD);
                meta_arms = arms.getItemMeta();
                meta_arms.setDisplayName(teamForJoin.teamColor.chatColor+"长虹剑");
                meta_arms.setLore(Arrays.asList(new String[]{"右键触发长虹剑气","冷却:1分钟"}));
                break;

            case ARROW:
                arms = new ItemStack(Material.BOW);
                meta_arms = arms.getItemMeta();
                meta_arms.setDisplayName(teamForJoin.teamColor.chatColor+"爆裂弓");
                meta_arms.setLore(Arrays.asList(new String[]{"每分钟产生一个七杀箭，被击中者必死"}));
                other = new ItemStack(Material.ARROW);
                other.setAmount(64);
                break;

            case ARMOR:
                arms = new ItemStack(Material.SHIELD);
                meta_arms = arms.getItemMeta();
                meta_arms.setDisplayName(teamForJoin.teamColor.chatColor+"蓝盾");
                meta_arms.setLore(Arrays.asList(new String[]{"被击中时有10%的几率免疫伤害"}));
                break;

            case MAGE:
                arms = new ItemStack(Material.BLAZE_ROD);
                meta_arms = arms.getItemMeta();
                meta_arms.setDisplayName(teamForJoin.teamColor.chatColor+"烈火杖");
                meta_arms.setLore(Arrays.asList(new String[]{"右键获得俩次给队友施加buff的机会","冷却时间:1分钟"}));
                break;
        }
        arms.setItemMeta(meta_arms);

        PlayerInventory inventory = player.player.getInventory();
        inventory.setBoots(boots);
        inventory.setChestplate(chestplate);
        inventory.setHelmet(helmet);
        inventory.setLeggings(pants);
        inventory.setItem(1,arms);
        inventory.setItem(2,other);

        //添加冷却时间
        if(!cooltime.containsKey(player.player.getName())){
            cooltime.put(player.player.getName(),type.getCooltime());
            originCooltime.put(player.player.getName(),type.getCooltime());
        }
    }

    /*递减主动技能冷却时间*/
    public void diminishingCoolTime(){
        if(!cooltime.isEmpty())
            for(String name:cooltime.keySet()){
                int old_cooltime = cooltime.get(name);
                if(old_cooltime>1){
                    old_cooltime--;
                }
                else{
//                    old_cooltime = originCooltime.get(name);
                }
                cooltime.put(name,old_cooltime);
            }
    }

    public ExpertType getType() {
        return type;
    }

    /**
     * 重置冷却时间通过玩家名
     * @param name
     */
    public void resetCooltimeByPlayername(String name){
        cooltime.put(name,originCooltime.get(name));
    }
}
