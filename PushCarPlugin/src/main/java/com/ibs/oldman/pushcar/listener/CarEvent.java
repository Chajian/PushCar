package com.ibs.oldman.pushcar.listener;

import com.ibs.oldman.pushcar.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

/**
 * 矿车监听
 */
public class CarEvent implements Listener {


    /**
     * 当实体尝试攻击实体
     * @param event
     */
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onEntityDamage(EntityDamageByEntityEvent event){
//
//        //处理攻击矿车的行为
//
//        //攻击者
//        Entity damager = event.getDamager();
//        //受害者
//        Entity victimer = event.getEntity();
//        System.out.println("武");
//        //如果是矿车
//        if(victimer instanceof Minecart){
//            System.out.println("武");
//            //如果矿车属于竞技场中的
//            if(Main.isCartInGame(victimer)){
//                System.out.println("胡武");
//                event.setCancelled(true);
//                return;
//            }
//
//        }
//
//    }

    /**
     * 车辆被销毁时触发
     * @param event
     */
    @EventHandler
    public void onDestoryCart(VehicleDestroyEvent event){
        //如果矿车是游戏中的矿车就无法销毁
        Vehicle vehicle = event.getVehicle();
        if(Main.isCartInGame(vehicle)){
            event.setCancelled(true);
            return;
        }
    }

    /**
     * 车辆碰撞事件
     */
    @EventHandler
    public void onCollideCart(VehicleEntityCollisionEvent event){
        Vehicle vehicle = event.getVehicle();
        if(Main.isCartInGame(vehicle)){
            event.setCancelled(true);
            return;
        }
    }

    /**
     * 实体尝试进入车辆
     * @param event
     */
    @EventHandler
    public void onEnterCart(VehicleEntityCollisionEvent event){
        Vehicle vehicle = event.getVehicle();
        if(Main.isCartInGame(vehicle)){
            event.setCancelled(true);
            return;
        }
    }



}
