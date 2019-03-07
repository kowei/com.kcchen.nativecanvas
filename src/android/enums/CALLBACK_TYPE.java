package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/9.
 */

public enum CALLBACK_TYPE {
    MINI_MAP("MiniMap"),
    TEXT_INPUT("TextInput"),
    EXIT("NativeCanvasExit"),
    UNDO_MANAGER("UndoManager"),
    INSERT_MANAGER("InsertManager"),
    ERROR("Error"),
    ;

    private final String message;

    CALLBACK_TYPE(String message) {
        this.message = message;
    }

    public static CALLBACK_TYPE get(String text) {
        if (text != null) {
            for (CALLBACK_TYPE b : CALLBACK_TYPE.values()) {
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
