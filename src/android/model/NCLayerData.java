package com.kcchen.nativecanvas.model;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.FloatRange;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NCLayerData {
    protected static final String TAG = NCLayerData.class.getSimpleName();

    public static final String LAYER_X = "LayerX";
    private static final String LAYER_Y = "LayerY";
    private static final String LAYER_LIMIT = "LayerLimit";
    private static final String LAYER_IS_FLIPPED = "LayerIsFlipped";
    private static final String LAYER_ROTATION_IN_DEGREES = "LayerRotationInDegrees";
    private static final String LAYER_SCALE = "LayerScale";

    protected SCALE_LIMIT limit = SCALE_LIMIT.STICKER_IMAGE;
    protected HandlerThread thread = null;
    protected Handler handler = null;


    /**
     * rotation relative to the layer center, in degrees
     */
    @FloatRange(from = 0.0F, to = 360.0F)
    private float rotationInDegrees;

    private float scale;
    /**
     * top left X coordinate, relative to parent canvas
     */
    private float x;
    /**
     * top left Y coordinate, relative to parent canvas
     */
    private float y;
    /**
     * is layer flipped horizontally (by X-coordinate)
     */
    private boolean isFlipped;
    protected ArrayList<OnLayerListener> listeners = new ArrayList<OnLayerListener>();
    private float visibleScale = 1;
    private int pageIndex;

    public void setVisibleScale(float visibleScale) {
        this.visibleScale = visibleScale;
    }

    public float getVisibleScale() {
        return visibleScale;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public enum SCALE_LIMIT {
        STICKER_IMAGE("StickerImage", 4.0F, 0.06F, 0.4F, 0.13F, 0.075F, 0xff000000),
        STICKER_TEXT("TextSticker", 5.0F, 0.2F, 0.8F, 0.13F, 0.075F, 0xff000000),
        PAPER_DRAWING("paper", 5.0F, 1.0F, 1.0F, 0.13F, 0.075F, 0xff000000),
        ;

        public static final String SCALE_LIMIT_TYPE         = "ScaleLimitType";
        public static final String SCALE_LIMIT_MAX          = "ScaleLimitMax";
        public static final String SCALE_LIMIT_MIN          = "ScaleLimitMin";
        public static final String SCALE_LIMIT_INIT         = "ScaleLimitInit";
        public static final String SCALE_LIMIT_MINI_HEIGHT  = "ScaleLimitMiniHeight";
        public static final String SCALE_LIMIT_FONT_SIZE    = "ScaleLimitFontSize";
        public static final String SCALE_LIMIT_FONT_COLOR   = "ScaleLimitFontColor";

        private final String key;
        private Float maxScale;
        private Float minScale;
        private Float initScale;
        private Float miniHeight;
        private Float fontSize;
        private Integer fontColor;

//        float FONT_SIZE_STEP = 0.008F;

        SCALE_LIMIT(String key, Float maxScale, Float minScale, Float initScale, Float miniHeight, Float fontSize, Integer fontColor) {
            this.key = key;
            this.maxScale = maxScale;
            this.minScale = minScale;
            this.initScale = initScale;
            this.miniHeight = miniHeight;
            this.fontSize = fontSize;
            this.fontColor = fontColor;
        }

        public static SCALE_LIMIT get(String text) {
            if (text != null) {
                for (SCALE_LIMIT b : SCALE_LIMIT.values()) {
                    if (text.equalsIgnoreCase(b.key))
                        return b;
                }
            }
            return null;
        }

        public String key() {
            return this.key;
        }

        public Float getMaxScale() {
            return maxScale;
        }

        public SCALE_LIMIT setMaxScale(Float maxScale) {
            this.maxScale = maxScale;
            return this;
        }

        public Float getMinScale() {
            return minScale;
        }

        public SCALE_LIMIT setMinScale(Float minScale) {
            this.minScale = minScale;
            return this;
        }

        public Float getInitScale() {
            return initScale;
        }

        public SCALE_LIMIT setInitScale(Float initScale) {
            this.initScale = initScale;
            return this;
        }

        public Float getMiniHeight() {
            return miniHeight;
        }

        public SCALE_LIMIT setMiniHeight(Float miniHeight) {
            this.miniHeight = miniHeight;
            return this;
        }

        public Float getFontSize() {
            return fontSize;
        }

        public SCALE_LIMIT setFontSize(Float fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public Integer getFontColor() {
            return fontColor;
        }

        public SCALE_LIMIT setFontColor(Integer fontColor) {
            this.fontColor = fontColor;
            return this;
        }

        public JSONObject exportData() {
            JSONObject data = new JSONObject();
            try {
                data.put(SCALE_LIMIT_TYPE, key());
                data.put(SCALE_LIMIT_MAX, getMaxScale() + "");
                data.put(SCALE_LIMIT_MIN, getMinScale() + "");
                data.put(SCALE_LIMIT_INIT, getInitScale() + "");
                data.put(SCALE_LIMIT_MINI_HEIGHT, getMiniHeight() + "");
                data.put(SCALE_LIMIT_FONT_SIZE, getFontSize() + "");
                data.put(SCALE_LIMIT_FONT_COLOR, getFontColor() + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return data;
        }

        public boolean importData(JSONObject data) {
            boolean isImported = false;
            try {
                setMaxScale(Float.parseFloat(data.optString(SCALE_LIMIT_MAX)));
                setMinScale(Float.parseFloat(data.optString(SCALE_LIMIT_MIN)));
                setInitScale(Float.parseFloat(data.optString(SCALE_LIMIT_INIT)));
                setMiniHeight(Float.parseFloat(data.optString(SCALE_LIMIT_MINI_HEIGHT)));
                setFontSize(Float.parseFloat(data.optString(SCALE_LIMIT_FONT_SIZE)));
                setFontColor(Integer.parseInt(data.optString(SCALE_LIMIT_FONT_COLOR)));
                isImported = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isImported;
        }

        public int hashcode() {
            return key.hashCode()
                    + (int) (maxScale * 100)
                    + (int) (minScale * 100)
                    + (int) (initScale * 100)
                    + (int) (miniHeight * 100)
                    + (int) (fontSize * 100)
                    + fontColor
                    ;
        }

        @Override
        public String toString() {
            return "SCALE limit: " + key
                    + " scale(max:" + maxScale + " min:" + minScale + " init:" + initScale + ")"
                    + " text(min height:" + miniHeight + " fontSize:" + fontSize + " fontColor:" + fontColor + ")";
        }

    }

    /**
     * 1 - down scale to fit screen
     * < 1 - smaller than screen
     * > 1 - screen
     * @param limit
     */
    public NCLayerData(SCALE_LIMIT limit) {
        this.limit = limit;
        reset();
    }

    public NCLayerData reset() {
        this.rotationInDegrees = 0.0F;
        this.scale = 1.0F;
        this.isFlipped = false;
        this.x = 0.0F;
        this.y = 0.0F;
        switch (limit){
            case PAPER_DRAWING:
                limit = SCALE_LIMIT.PAPER_DRAWING;
                break;
            case STICKER_IMAGE:
                limit = SCALE_LIMIT.STICKER_IMAGE;
                break;
            case STICKER_TEXT:
                limit = SCALE_LIMIT.STICKER_TEXT;
                break;
            default:
                break;
        }
        return this;
    }

    public void postScale(float scaleDiff) {
        float newVal = scale + scaleDiff;
        if (newVal >= getMinScale() && newVal <= getMaxScale()) {
            scale = newVal;
        }
    }

    public void postRotate(float rotationInDegreesDiff) {
        this.rotationInDegrees += rotationInDegreesDiff;
        this.rotationInDegrees %= 360.0F;
    }

    public void postTranslate(float dx, float dy) {
        this.x += dx;
        this.y += dy;
        //Log.w(TAG,"> NCN postTranslate delta:" + dx + ", " + dy + " -> " + x + ", " + y);
    }

    public NCLayerData flip() {
        this.isFlipped = !isFlipped;
        return this;
    }

    public float getMaxScale() {
        return this.limit.getMaxScale();
    }

    public float getMinScale() {
        return this.limit.getMinScale();
    }

    public float initialScale() {
        return this.limit.getInitScale();
    }

    public SCALE_LIMIT getLimit(){
        return this.limit;
    }

    private NCLayerData setLimit(SCALE_LIMIT limit) {
        this.limit = limit;
        return this;
    }

    public float getRotationInDegrees() {
        return rotationInDegrees;
    }

    public NCLayerData setRotationInDegrees(@FloatRange(from = 0.0, to = 360.0) float rotationInDegrees) {
        this.rotationInDegrees = rotationInDegrees;
        this.rotationInDegrees %= 360.0F;
        return this;
    }

    public float getScale() {
        return scale; // * visibleScale;
    }

    public NCLayerData setScale(float scale) {
        //Log.e(TAG, "> NCN setScale " + scale + " " + getMinScale() + "/" + getMaxScale());
        this.scale = getValidScale(scale);
        return this;
    }

    public boolean isValidScale(float scale){
        if (scale >= getMinScale() && scale <= getMaxScale()) return true;
        return false;
    }

    public float getValidScale(float scale){
        if (scale >= getMinScale() && scale <= getMaxScale()) return scale;
        if (scale < getMinScale()) return getMinScale();
        if (scale > getMaxScale()) return getMaxScale();
        return scale;
    }

    public float getX() {
        return x;
    }

    public NCLayerData setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return y;
    }

    public NCLayerData setY(float y) {
        this.y = y;
        return this;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public NCLayerData setFlipped(boolean flipped) {
        isFlipped = flipped;
        return this;
    }

    public void setOnLayerListener(OnLayerListener listener) {
        if (!listeners.contains(listener)) {
            //Log.d(TAG, "> NCN setOnLayerListener(" + (listeners.size() + 1) + ") " + listener);
            listeners.add(listener);
        }
    }

    public void removeOnLayerListener(OnLayerListener listener) {
        if (listeners.contains(listener)) {
            //Log.d(TAG, "> NCN removeOnLayerListener(" + (listeners.size() - 1) + ") " + listener);
            listeners.remove(listener);
        }
    }

    public interface OnLayerListener{
        void onBitmapUpdate();
        void onRefreshView();
        void onPageReady(int pageIndex);
    }

    public JSONObject exportData(){
        JSONObject data = new JSONObject();
        try {
            data.put(LAYER_X, getX() + "");
            data.put(LAYER_Y, getY() + "");
            data.put(LAYER_LIMIT, getLimit().exportData());
            data.put(LAYER_IS_FLIPPED, isFlipped());
            data.put(LAYER_ROTATION_IN_DEGREES, getRotationInDegrees() + "");
            data.put(LAYER_SCALE, getScale() + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean importData(JSONObject data){
        boolean isImported = false;
        try {
            setX(Float.parseFloat(data.optString(LAYER_X)));
            setY(Float.parseFloat(data.optString(LAYER_Y)));
            JSONObject limitData = data.optJSONObject(LAYER_LIMIT);
            if(limitData != null && limit == null) limit = SCALE_LIMIT.get(limitData.optString(SCALE_LIMIT.SCALE_LIMIT_TYPE));
            if(limit != null){
                if(!limit.importData(limitData)){
                    throw new Exception("limit import failed");
                }
            }else{
                throw new Exception("limit data broken");
            }
            setFlipped(data.optBoolean(LAYER_IS_FLIPPED));
            setRotationInDegrees(Float.parseFloat(data.optString(LAYER_ROTATION_IN_DEGREES)));
            setScale(Float.parseFloat(data.optString(LAYER_SCALE)));
            for(OnLayerListener listener: listeners){
                listener.onPageReady(pageIndex);
            }
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    @Override
    public int hashCode() {
        return (int) (x * 100)
                + (int) (y * 100)
                + limit.hashcode()
                + (isFlipped ? 0 : 13)
                + (int) (rotationInDegrees * 100)
                + (int) (scale * 100)
        ;
    }

    @Override
    public String toString() {
        return "Layer Data"
                + "\n x                : " + x
                + "\n y                : " + y
                + "\n limit            : " + limit.toString()
                + "\n isFlipped        : " + isFlipped
                + "\n rotationInDegrees: " + rotationInDegrees
                + "\n scale            : " + scale
                ;
    }
}
