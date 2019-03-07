package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/9.
 */

public enum OBJECT_TYPE {
    IMAGE(0),
    TEXT(1),
    TEMPLATE(2),
    ;

    private final int index;

    OBJECT_TYPE(int index) {
        this.index = index;
    }

    public static OBJECT_TYPE get(int index) {
        if (index >= 0) {
            for (OBJECT_TYPE b : OBJECT_TYPE.values()) {
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
