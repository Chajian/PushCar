package com.ibs.oldman.pushcar.boss;

import com.ibs.oldman.pushcar.api.boss.BossBar;

public class BossBarSelector {
    public static BossBar getBossBar() {
        try {
            return new BossBar19();
        } catch (Throwable t) {
            return new BossBar18();
        }
    }
}
