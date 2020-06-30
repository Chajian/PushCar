package com.ibs.oldman.pushcar.api.game;

/**
 * @author Bedwars Team
 * 竞技场时间
 */
public enum ArenaTime {
    WORLD(-1),
    DAY_BEGINNING(0),
    DAY(1000),
    NOON(6000),
    SUNSET(12000),
    NIGHT(13000),
    MIDNIGHT(18000),
    SUNRISE(23000);

    public final int time;

    private ArenaTime(int time) {
        this.time = time;
    }

    /**
     * @return time in ticks
     */
    public int getTime() {
        return this.time;
    }
}
