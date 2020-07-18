package com.ibs.oldman.pushcar.game.entity;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *粒子实体基类
 */
public class BaseEffect implements ImpleEffect{

    protected List<Location> locations = new ArrayList<>();
    protected Particle particle;
    protected Coordinate coordinate;
    protected World world;
    protected boolean redraw = false;
    protected boolean live = false;
    protected boolean cleanmodel = false;
    protected int density = 1;
    {
        coordinate = new Coordinate();
    }

    @Override
    public void activity() {

    }

    @Override
    public void model() {
    }

    @Override
    public void draw() {
        Iterator iterator = locations.iterator();
        while(iterator.hasNext()){
            Location location = (Location) iterator.next();
            world.spawnParticle(particle,location.getX(),location.getY(),location.getZ(),density);
        }
        locations.clear();
    }

    @Override
    public void redraw() {

    }

    @Override
    public void destory() {

    }

    public boolean isLive() {
        return live;
    }

    public void start() {
        try {
            if (cleanmodel)
                locations.clear();
            model();
            draw();
            activity();
            if (redraw)
                draw();
            if (!live)
                destory();
        }
        catch (Exception e){
            e.printStackTrace();
            live = false;
        }
    }
}
