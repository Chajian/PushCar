package com.ibs.oldman.pushcar.game;

import com.ibs.oldman.pushcar.game.entity.BaseEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 粒子线程
 */
public class EffectThread extends BukkitRunnable {

    public static final List<BaseEffect> effects = new ArrayList<>();

    @Override
    public void run() {
        Iterator iterator = effects.iterator();
        while(iterator.hasNext()){
            BaseEffect baseEffect = (BaseEffect) iterator.next();
            baseEffect.start();
            if(!baseEffect.isLive()){
                iterator.remove();
            }
        }
    }

    public static void put(BaseEffect baseEffect){
        effects.add(baseEffect);
    }

}
