package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/9.
 */

/**
 * 筆觸類型
 * Unset = 0, 無設定
 * Eraser = 1, 橡皮擦
 * Fountain = 2, 鋼筆
 * HighLight = 3, 螢光筆
 * BallPoint = 4, 原子筆
 */
public enum PEN_TYPE {
    UNKNOWN(0),
    ERASER(1),
    FOUNTAIN(2),
    HIGHLIGHTER(3),
    BALLPOINT(4),
    ;

    private final int index;

    PEN_TYPE(int index) {
        this.index = index;
    }

    public static PEN_TYPE get(int index) {
        if (index >= 0) {
            for (PEN_TYPE b : PEN_TYPE.values()) {
                if (index == b.index)
                    return b;
            }
        }
        return null;
    }

    public int key() {
        return this.index;
    }
}
