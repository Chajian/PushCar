package com.ibs.oldman.pushcar.game.entity.wave;

import com.ibs.oldman.pushcar.game.entity.BaseEffect;
import com.ibs.oldman.pushcar.game.entity.Coordinate;
import org.bukkit.Location;
import org.bukkit.Particle;

import java.util.Iterator;

/**
 * 空投光柱
 */
public class BeamLight extends BaseEffect {
    private Location location;
    private int height = 20;

    public BeamLight(Location location,int height) {
        this.location = location;
        particle = Particle.values()[17];
        live = true;
        this.height = height;
        world = location.getWorld();
    }

    @Override
    public void model() {
        for(int i = 0 ; i <= height ; i++) {
            Location location1 = location.clone();
            location1.setY(location1.getY()+i);
            locations.addAll(Coordinate.getCircle(1, location1, 6, 360));
        }
    }

    public void setLive(boolean live1){
        live = live1;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
