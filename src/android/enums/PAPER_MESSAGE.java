package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/6/7.
 */

public enum PAPER_MESSAGE {
    UPDATE_BITMAP("VIDEOINFO","",null),
    UPDATE_VIEW("UpdateView","" ,null ),
    AUDIOINFO("AUDIOINFO","",null),
    FILELIST("FILELIST","",null),
    RECSW("RECSW","",null),
    ;

    private final String key;
    private final String value;
    private Object object;
    PAPER_MESSAGE(String key, String value, Object object) {this.key = key;this.value = value;this.object = object;}
    public String key() {return this.key;}
    public String value() {return this.value;}
    public Object object() {return this.object;}
    public void setObject(Object object) {this.object = object;}
    public static PAPER_MESSAGE get(String text) {
        if (text != null) {
            for (PAPER_MESSAGE b : PAPER_MESSAGE.values()) {
                if (text.equalsIgnoreCase(b.key)) {
                    return b;
                }
            }
        }
        return null;
    }
}
