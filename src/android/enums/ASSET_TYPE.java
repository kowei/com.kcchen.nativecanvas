package com.kcchen.nativecanvas.enums;

/**
 * Created by kowei on 2018/3/9.
 */

public enum ASSET_TYPE {
    PNG("png"),
    JPEG("jpg"),
    GIF("gif"),
    SVG("svg"),
    PDF("pdf"),
    PNG_64("png_64"),
    JPEG_64("jpg_64"),
    GIF_64("gif_64"),
    SVG_64("svg_64"),
    UNKNOWN("unknown"),
    ;

    protected static final String TAG = ASSET_TYPE.class.getSimpleName();

    private final String message;
    private String data;

    ASSET_TYPE(String message) {
        this.message = message;
    }

    public static ASSET_TYPE get(String text) {
        if (text != null) {
            for (ASSET_TYPE b : ASSET_TYPE.values()) {
                if (text.equalsIgnoreCase(b.message))
                    return b;
            }
        }
        return null;
    }

    public String key() {
        return this.message;
    }

    public String getData() {
        return data;
    }

    public void clearData() {
        data = null;
    }

    public boolean isEmbed(){
        return data != null;
    }

    public static ASSET_TYPE parse(String file) {
        String string = file.toLowerCase();
        ASSET_TYPE type;
        if (string.matches(".*\\.png")) {
            type =  PNG;
        } else if (string.matches(".*\\.svg")) {
            type =  SVG;
        } else if (string.matches(".*\\.gif")) {
            type =  GIF;
        } else if (string.matches(".*\\.pdf")) {
            type =  PDF;
        } else if (string.matches(".*\\.jpeg") || string.matches(".*\\.jpg")) {
            type =  JPEG;
        } else if (string.startsWith("data:image/jpeg")) {
            type = JPEG_64;
            String[] data = file.split(",");
            if(data.length == 2) {
                type.data = data[1];
            }
        } else if (string.startsWith("data:image/png")) {
            type =  PNG_64;
            String[] data = file.split(",");
            if(data.length == 2) {
                type.data = data[1];
            }
        } else if (string.startsWith("data:image/svg+xml")) {
            type =  SVG_64;
            String[] data = file.split(",");
            if(data.length == 2) {
                type.data = data[1];
            }
        } else if (string.startsWith("data:image/gif")) {
            type =  GIF_64;
            String[] data = file.split(",");
            if(data.length == 2) {
                type.data = data[1];
            }
        } else {
            type =  UNKNOWN;
        }
        //Log.d(TAG,"ASSET_TYPE:" + type.key());
        return type;
    }

    public static boolean isSupported(String file) {
        String string = file.toLowerCase();
        ASSET_TYPE type = null;
        if (string.matches(".*\\.png")) {
            type = PNG;
        } else if (string.matches(".*\\.svg")) {
            type = SVG;
        } else if (string.matches(".*\\.gif")) {
            type = GIF;
        } else if (string.matches(".*\\.pdf")) {
            type = PDF;
        } else if (string.matches(".*\\.jpeg") || string.matches(".*\\.jpg")) {
            type = JPEG;
        }
        //Log.d(TAG, "ASSET_TYPE:" + (type == null ? "" : type.key()));
        return type != null;
    }

}

