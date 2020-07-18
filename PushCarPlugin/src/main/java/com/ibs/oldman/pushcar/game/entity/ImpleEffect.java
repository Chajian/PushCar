package com.ibs.oldman.pushcar.game.entity;

/**
 * 粒子接口
 */
public interface ImpleEffect {

    //粒子行为
    public void activity();
    //初始化粒子模型
    public void model();
    //绘图
    public void draw();
    //重绘
    public void redraw();
    //结束
    public void destory();

}
