package com.kcchen.nativecanvas.enums;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kowei on 2018/3/23.
 */


public enum BORDER_TYPE {
    NONE("none",null,null,null),
    CIRCLE("circle",null,null,null),
    RECTANGLE("rectangle",null,null,null),
    IMAGE("image",null,null,null),;

    public static final String BORDER_TYPE_TYPE        = "BorderTypeType";
    public static final String BORDER_TYPE_WIDTH       = "BorderTypeWidth";
    public static final String BORDER_TYPE_HEIGHT      = "BorderTypeHeight";
    public static final String BORDER_TYPE_RESOURCE_ID = "BorderTypeResourceId";
    public static final String BORDER_TYPE_ROTATE      = "BorderTypeRotate";

    private final String key;
    private Integer width;
    private Integer height;
    private Integer resourceId;
    private Float rotate;

    BORDER_TYPE(String key, Integer width, Integer height, Integer resourceId) {
        this.key = key;
        this.width = width;
        this.height = height;
        this.resourceId = resourceId;
    }

    public static BORDER_TYPE get(String text) {
        if (text != null) {
            for (BORDER_TYPE b : BORDER_TYPE.values()) {
                if (text.equalsIgnoreCase(b.key))
                    return b;
            }
        }
        return null;
    }

    public String key() {
        return this.key;
    }

    public Integer getWidth() {
        return width;
    }

    public BORDER_TYPE setWidth(Integer width) {
        //Log.e(TAG,"BORDER_TYPE setWidth " + width);
        this.width = width;
        return this;
    }

    public Float getRotate() {
        return rotate;
    }

    public BORDER_TYPE setRotate(Float rotate) {
        this.rotate = rotate;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public BORDER_TYPE setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public BORDER_TYPE setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public JSONObject exportData() {
        JSONObject data = new JSONObject();
        try {

            data.put(BORDER_TYPE_TYPE, key());
            data.put(BORDER_TYPE_WIDTH, getWidth() == null ? "" : getWidth() + "");
            data.put(BORDER_TYPE_HEIGHT, getHeight() == null ? "" : getHeight() + "");
            data.put(BORDER_TYPE_RESOURCE_ID, getResourceId() == null ? "" : getResourceId() + "");
            data.put(BORDER_TYPE_ROTATE, getRotate() == null ? "" : getRotate() + "");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        String value;
        try {
            value = data.optString(BORDER_TYPE_WIDTH);
            setWidth(value.isEmpty() ? null : Integer.valueOf(value));
            value = data.optString(BORDER_TYPE_HEIGHT);
            setHeight(value.isEmpty() ? null : Integer.valueOf(value));
            value = data.optString(BORDER_TYPE_RESOURCE_ID);
            setResourceId(value.isEmpty() ? null : Integer.valueOf(value));
            value = data.optString(BORDER_TYPE_ROTATE);
            setRotate(value.isEmpty() ? null : Float.parseFloat(value));
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

}
