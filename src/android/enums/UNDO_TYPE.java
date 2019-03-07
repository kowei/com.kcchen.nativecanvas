package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/12.
 */

public enum UNDO_TYPE {
    DRAWING         ("Drawing"),
    ERASER          ("Eraser"),
    STICKER_IMAGE   ("StickerImage"),
    STICKER_TEXT    ("StickerText"),
    CLEAR           ("Clear"),
    TRANSFORM       ("Transform"),
    TEMPLATE        ("Template"),
    ;

    private final String key;
    private Object object;
    UNDO_TYPE(String key) {this.key = key;}
    public String key() {return this.key;}
    public static UNDO_TYPE get(String text) {
        if (text != null) {
            for (UNDO_TYPE b : UNDO_TYPE.values()) {
                if (text.equalsIgnoreCase(b.key)) {
                    return b;
                }
            }
        }
        return null;
    }
}

