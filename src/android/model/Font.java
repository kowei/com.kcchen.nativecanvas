package com.kcchen.nativecanvas.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Font {

    public static final String FONT_COLOR = "FontColor";
    public static final String FONT_SIZE = "FontSize";
    public static final String FONT_TYPEFACE = "FontTypeface";

    /**
     * color value (ex: 0xFF00FF)
     */
    private int color;
    /**
     * name of the font
     */
    private String typeface;
    /**
     * size of the font, relative to parent
     */
    private float size;

    public Font() {
    }

    public Font(JSONObject data) {
        importData(data);
    }

    public void increaseSize(float diff) {
        size = size + diff;
    }

    public void decreaseSize(float diff) {
        if (size - diff >= Limits.MIN_FONT_SIZE) {
            size = size - diff;
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getTypeface() {
        return typeface;
    }

    public void setTypeface(String typeface) {
        this.typeface = typeface;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public JSONObject exportData() {
        JSONObject data = new JSONObject();
        try {
            data.put(FONT_COLOR, getColor() + "");
            data.put(FONT_SIZE, getSize() + "");
            data.put(FONT_TYPEFACE, getTypeface());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        try {
            setColor(Integer.parseInt(data.optString(FONT_COLOR)));
            setSize(Float.parseFloat(data.optString(FONT_SIZE)));
            setTypeface(data.optString(FONT_TYPEFACE));
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    @Override
    public String toString() {
        return "FONT data font(typeface:" + typeface + " color:" + color + " size:" + size + ")";
    }

    private interface Limits {
        float MIN_FONT_SIZE = 0.01F;
    }
}
