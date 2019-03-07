package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/9.
 */



public enum BOOK_TYPE {
    UNSET("unset"),
    NOTE("note"),
    IMAGE("image"),
    GIF("gif"),
    PDF("pdf"),
    ;

    private final String index;

    BOOK_TYPE(String index) {
        this.index = index;
    }

    public static BOOK_TYPE get(String index) {

        for (BOOK_TYPE b : BOOK_TYPE.values()) {
            if (index.equalsIgnoreCase(b.index))
                return b;
        }

        return null;
    }

    public String key() {
        return this.index;
    }
}
