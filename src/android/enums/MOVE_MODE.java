package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/9.
 */

public enum MOVE_MODE {
    MOVE("move"),
    ROTATE("rotate"),
    SCALE("scale"),;

    private final String message;

    MOVE_MODE(String message) {
        this.message = message;
    }

    public static MOVE_MODE get(String text) {
        if (text != null) {
            for (MOVE_MODE b : MOVE_MODE.values()) {
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
