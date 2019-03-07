package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/9.
 */

public enum MOVE_STATUS {
    INSIDE_DOWN("InsideDown"),
    OUTSIDE_DOWN("OutsideDown"),
    INSIDE_MOVE("InsideMove"),
    OUTSIDE_MOVE("OutsideMove"),
    INSIDE_UP("InsideUp"),
    OUTSIDE_UP("OutsideUp"),
    INVALID("Invalid"),
    ;

    private final String message;

    MOVE_STATUS(String message) {
        this.message = message;
    }

    public static MOVE_STATUS get(String text) {
        if (text != null) {
            for (MOVE_STATUS b : MOVE_STATUS.values()) {
                if (text.equalsIgnoreCase(b.message))
                    return b;
            }
        }
        return null;
    }

    public String key() {
        return this.message;
    }
}
