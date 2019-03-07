package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/9.
 */



public enum RESULT_TYPE {
    SUCCESS(0),
    INVALID(1),
    FAILED(2),
    BOOK_NOT_EXISTED(3),
    BOOK_CREATE_FAILED(4),
    PAGE_NOT_EXISTED(5),
    PAGE_CREATE_FAILED(6),
    IMAGE_TYPE_ERROR(7),
    VIEW_INVALID(8),
    ADD_OBJECT_INVALID(9),
    PROPERTY_INVALID(10),
    ;

    private final int index;

    RESULT_TYPE(int index) {
        this.index = index;
    }

    public static RESULT_TYPE get(int index) {
        if (index >= 0) {
            for (RESULT_TYPE b : RESULT_TYPE.values()) {
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
