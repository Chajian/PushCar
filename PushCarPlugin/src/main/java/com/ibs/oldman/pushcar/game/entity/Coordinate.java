package com.ibs.oldman.pushcar.game.entity;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * 粒子实体的行为类
 */
public class Coordinate {
    double angle;
    double x,z,y;

    //获取前面一方格的位置
    public static Location getLine(int distance, Location location1){

        double angle =  Math.toRadians(location1.getYaw());//获得location的角度偏移量
        Location location =location1.clone();
        double pitch = Math.toRadians(location.getPitch());
        double z = location.getZ()+(Math.cos(angle)*distance);
        double x = location.getX()-(Math.sin(angle)*distance);
        double y = location.getY()+1-(Math.sin(pitch)*distance);
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        return location;
    }

    /**
     * 根据三角角度，绘制边
     * @param angle 角度
     * @param length 底部长度
     * @param location 位置
     * @return
     */
//    public static List<Location> getTriangle(Location location,double angle,float length){
//        List<Location> list = new ArrayList<>();
//        double source_x = location.getX();
//        double source_z = location.getZ();
//        double sin_x = Math.sin(angle);
//        double cos_z = Math.cos(angle);
//        for(int i = 0 ; i <= length ; i++){
//            double target_x = source_x+i*sin_x;
//            double target_z = source_z+i*cos_z;
//            Location target = new Location(location.getWorld(),target_x,location.getY(),target_z);
//            list.add(target);
//        }
//        return list;
//    }

    /**
     * 以location为顶点绘制相对玩家坐标方向-n度的等腰三角形
     * @param location 三角形顶点位置
     * @param angle 顶点角度
     * @param length 三角形的高
     * @param hasbottom 是否拥有三角形的底边
     * @return
     */
    public static List<Location> getTriangle(Location player_location, Location location, double angle, float length, boolean hasbottom){
        List<Location> list = new ArrayList<>();
//        double vertex_x = player_location.getX()-location.getX();
//        double vertex_z = player_location.getZ()-location.getZ();
        double vertex_angle_forbtoa = getRelativeAngle(player_location,location);//b相对于a的角度
        double vertex_angle_froatob = getRelativeAngle(location,player_location);//a相对于b的角度
        System.out.println(vertex_angle_forbtoa);
        list.add(location);
        double angle_remove = vertex_angle_forbtoa-angle;
        double angle_add = vertex_angle_forbtoa+angle;
        for(int i = 0 ; i <= length ; i++){

            double sin_add = Math.sin(angle_add);
            double cos_add = Math.cos(angle_add);
            Location location_add = new Location(location.getWorld(),location.getX()+i*sin_add,location.getY(),location.getZ()+i*cos_add);


            double sin_remove = Math.sin(angle_remove);
            double cos_remove = Math.cos(angle_remove);
            Location location_remove = new Location(location.getWorld(),location.getX()+i*sin_remove,location.getY(),location.getZ()+i*cos_remove);

            list.add(location_add);
            list.add(location_remove);
        }
        return list;
    }


    /**
     * 绘制五角星
     * @param location
     * @param length
     * @return
     */
    public static List<Location> getPentagram(Location location, float length){
        List<Location> list = new ArrayList<>();
        Location source_target = location.clone();
        source_target.setX(source_target.getX()+length);
        double angle = Math.PI/6;
        for(int i = 0 ; i < 5 ; i++){
            Location target = getRelativeLocation(location,source_target,i* Math.PI/2.5);
            list.addAll(getTriangle(location,target,angle,length-2,false));
        }
//        +i*(Math.PI/2.5)
//        list.addAll(getTriangle(location,Math.PI/2.4,length));
//        list.addAll(getTriangle(list.get(list.size()-1),-(Math.PI/2.4),length));
//        list.addAll(getTriangle(list.get(list.size()-1),Math.PI/180,length));
//        list.addAll(getTriangle(list.get(list.size()-1),-Math.PI/1.2,length));
//        list.addAll(getTriangle(list.get(list.size()-1),-(Math.PI/3),length));
//        list.addAll(getTriangle(list.get(list.size()-1),(Math.PI/1.2),length));
//        list.addAll(getTriangle(list.get(list.size()-1),-(Math.PI/1.34),length));
//        list.addAll(getTriangle(list.get(list.size()-1),Math.PI/2.4,length));
//        list.addAll(getTriangle(list.get(list.size()-1),Math.PI/1.3,length));
//        list.addAll(getTriangle(list.get(list.size()-1),Math.PI/180,length));
        return list;
    }

    /**
     * 根据弧度和半径，为location为中心获取圆的坐标
     * @param radius 半径
     * @param location 玩家location
     * @param degree 弧度偏移量
     * @param angle 圆弧的角度
     * @return
     */
    public static List<Location> getCircle(int radius, Location location, int degree, int angle){
        List<Location> list = new ArrayList<>();
        double count = angle/degree;
        while(count!=0){
            double radians = Math.toRadians(count*degree);//弧度*degree，因为360度每个都有一个粒子会很吃性能
            double x = Math.cos(radians)*radius;
            double z = Math.sin(radians)*radius;
            Location location1 = new Location(location.getWorld(),location.getX()+x,location.getY(),location.getZ()+z);
            list.add(location1);
            count = distanceToZero(count,1);
        }
        return list;
    }

    /**
     * 根据弧度和半径，为location为中心获取圆点的坐标
     * @param radius 半径
     * @param location 玩家location
     * @param degree 弧度偏移量
     * @return
     */
    public static Location getCirclePonit(int radius, Location location, int degree){
        double radians =  Math.toRadians(location.getYaw()+degree);//获得location的角度偏移量
//        System.out.println("位置角度="+radians);
        double x = Math.sin(radians)*radius;
        double z = Math.cos(radians)*radius;
        Location location1 = new Location(location.getWorld(),location.getX()-x,location.getY(),location.getZ()+z,location.getYaw(),location.getPitch());
        return location1;
    }



    /**
     * 主要实现的方法就是将二次函数的图片进行旋转，然后平移
     * //z的正负决定开口方向
     * @param z1 通过z进行二次函数编程翅膀(z就是x)
     * @param location 玩家的位置
     * @param height 二次函数的顶点坐标
     * @return
     */
    public static Location[] wing(double z1, Location location, double height){
        double angle =  Math.toRadians(location.getYaw());
        double size = Math.pow(z1,4);
        double z = Math.sin(angle)*size;
        double x = Math.cos(angle)*size;
        //减1主要的目的是为了让图像位移看起来像翅膀
        double y = z1-1;
        double y2 = z1*-1-1;

        //画左翅膀
        Location left_location1 = new Location(location.getWorld(),location.getX()-x+ Math.cos(angle)*height,location.getY()+y,location.getZ()-z+ Math.sin(angle)*height);//+height主要是进行平移
        Location left_location2 = new Location(location.getWorld(),location.getX()-x+ Math.cos(angle)*height,location.getY()+y2,location.getZ()-z+ Math.sin(angle)*height);//+height主要是进行平移
        Location left_location3 = new Location(location.getWorld(),location.getX()-x+ Math.cos(angle)*height,location.getY()-y,location.getZ()-z+ Math.sin(angle)*height);//+height主要是进行平移
        Location left_location4 = new Location(location.getWorld(),location.getX()-x+ Math.cos(angle)*height,location.getY()-y2,location.getZ()-z+ Math.sin(angle)*height);//+height主要是进行平移

        //画右翅膀
        Location right_location1 = new Location(location.getWorld(),location.getX()+x- Math.cos(angle)*height,location.getY()+y,location.getZ()+z- Math.sin(angle)*height);//+height主要是进行平移
        Location right_location2 = new Location(location.getWorld(),location.getX()+x- Math.cos(angle)*height,location.getY()+y2,location.getZ()+z- Math.sin(angle)*height);//+height主要是进行平移
        Location right_location3 = new Location(location.getWorld(),location.getX()+x- Math.cos(angle)*height,location.getY()-y,location.getZ()+z- Math.sin(angle)*height);//+height主要是进行平移
        Location right_location4 = new Location(location.getWorld(),location.getX()+x- Math.cos(angle)*height,location.getY()-y2,location.getZ()+z- Math.sin(angle)*height);//+height主要是进行平移
        return new Location[]{right_location1,right_location2,right_location3,right_location4,left_location1,left_location2,left_location3,left_location4};
    }

    /**
     * 通过俩点确定一条直线的原理画直线
     * @param a 点
     * @param b 点
     * @return
     */
    public static List<Location> drawLine(Location a, Location b){
        List<Location> list = new ArrayList<>();
        double x;
        double z;
        double y;
        //获取x，z俩点的距离
        x = b.getX()-a.getX();
        z = b.getZ()-a.getZ();
        y = b.getY()-a.getY();
        double beveledge = Math.sqrt(x*x+z*z);//斜边
        double sinx = x/beveledge;
        double cosz = z/beveledge;
        double tany = y/beveledge;

        while(beveledge!=0){
            Location location = new Location(a.getWorld(),a.getX()+beveledge*sinx,a.getY()+beveledge*tany,a.getZ()+beveledge*cosz);
            list.add(location);
            beveledge = distanceToZero(beveledge,1);
        }
        System.out.println(list.size()+" sinx="+sinx+"  cosz="+cosz+"  beveledge="+beveledge+" x="+x+" z="+z);
        return list;
    }

    /**
     * 向零不断递减或递增
     * @param target 目标数
     * @param decreaunit 递减单位
     */
    public static double distanceToZero(double target,float decreaunit){
        if(target>-decreaunit&&target<decreaunit){
            target=0;
        }
        else if(target>0){
            target-=decreaunit;
        }
        else if(target<0){
            target+=decreaunit;
        }
        return target;
    }

    /**
     * 以location为中心旋转x
     * @param location 中心点坐标
     * @param length 线条长度 x,或者z的长度
     * @param angle 旋转角度
     * @return 返回x
     */
    public static double spinPointx(Location location, double length, int angle){
        double x = location.getX();
        double sinx = Math.sin(angle)*length;
        double target_x = x - sinx;
        return target_x;
    }


    /**
     * 以location为中心旋转z
     * @param location 中心点坐标
     * @param length 线条长度 x,或者z的长度
     * @param angle 旋转角度
     * @return 返回z
     */
    public static double spinPointz(Location location, double length, int angle){
        double z = location.getZ();
        double sinz = Math.cos(angle)*length;
        double target_z = z + sinz;
        return target_z;
    }

    /**
     * 获取点b相对于点a的角度
     * @param a 点
     * @param b 点
     * @return
     */

    public static double getRelativeAngle(Location a, Location b){
        double vertex_x = a.getX()-b.getX();
        double vertex_z = a.getZ()-b.getZ();
        double hypotenuse = Math.sqrt(Math.pow(vertex_x,2)+ Math.pow(vertex_z,2));//斜边
        double sin = vertex_x/hypotenuse;
        double angle = Math.asin(sin);
//        System.out.println(vertex_x+":"+hypotenuse);
        return angle;
    }

    /**
     * 获取点b相对于点a的旋转angle角度之后的位置
     * @param a 点
     * @param b 点
     * @param angle 角度
     * @return
     */
    public static Location getRelativeLocation(Location a, Location b, double angle){
        double x = a.getX() - b.getX();
        double z = a.getZ() - b.getZ();
        double hypotenuse = Math.sqrt(Math.pow(x,2)+ Math.pow(z,2));//斜边
        double source_angle = getRelativeAngle(a,b);//b相对于a的角度
        source_angle+=angle;//选择转后的角度
        double target_x = Math.sin(source_angle)*hypotenuse;
        double target_z = Math.cos(source_angle)*hypotenuse;
        System.out.println("斜边:"+hypotenuse+":角度"+source_angle);
        return new Location(b.getWorld(),b.getX()+target_x,b.getY(),b.getZ()+target_z);
    }
}
