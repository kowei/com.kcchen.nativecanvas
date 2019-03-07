package com.kcchen.nativecanvas.drawtools.model;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import org.json.JSONException;
import org.json.JSONObject;

import static com.kcchen.nativecanvas.svg.parser.support.GraphicsSVG.COLOR;

/**
 * Created by kowei on 2018/3/13.
 */

public class NCPaint extends Paint {
    private static final String TAG = NCPaint.class.getSimpleName();

    private static final String PAINT_ANTI_ALIAS = "PaintAntiAlias";
    private static final String PAINT_COLOR = "PaintColor";
    private static final String PAINT_FILTER_BITMAP = "PaintFilterBitmap";
    private static final String PAINT_STROKE_CAP = "PaintStrokeCap";
    private static final String PAINT_STROKE_JOIN = "PaintStrokeJoin";
    private static final String PAINT_STROKE_MITER = "PaintStrokeMiter";
    private static final String PAINT_STROKE_WIDTH = "PaintStrokeWidth";
    private static final String PAINT_STYLE = "PaintStyle";
    private static final String PAINT_XFER_MODE = "PaintXfermode";
    private PorterDuff.Mode mode;


    public NCPaint() {
    }

    public NCPaint(int flags) {
        super(flags);
    }

    public NCPaint(NCPaint paint) {
        super(paint);
        importData(paint.exportData());
    }

    public NCPaint(JSONObject jsonObject) {
        super();
        if(jsonObject != null) importData(jsonObject);
    }

    @Override
    public void reset() {
        super.reset();
    }

    public void setXfermode(PorterDuff.Mode mode) {
        this.mode = mode;
        setXfermode(new PorterDuffXfermode(mode));
    }

    public JSONObject exportData() {
        JSONObject data = new JSONObject();
        try {
            data.put(PAINT_ANTI_ALIAS, isAntiAlias());
            data.put(PAINT_COLOR, getColor());
            data.put(PAINT_FILTER_BITMAP, isFilterBitmap());
            data.put(PAINT_STROKE_CAP, getStrokeCap().name());
            data.put(PAINT_STROKE_JOIN, getStrokeJoin().name());
            data.put(PAINT_STROKE_MITER, getStrokeMiter());
            data.put(PAINT_STROKE_WIDTH, getStrokeWidth());
            data.put(PAINT_STYLE, getStyle().name());
            if(mode != null){
                data.put(PAINT_XFER_MODE, mode.name());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        String key;
        try {
            setAntiAlias(data.optBoolean(PAINT_ANTI_ALIAS));
            setColor(data.optInt(PAINT_COLOR));
            setFilterBitmap(data.optBoolean(PAINT_FILTER_BITMAP));
            key = data.optString(PAINT_STROKE_CAP);
            if(!key.isEmpty()){
                Cap cap = Cap.valueOf(key);
                if(cap != null) setStrokeCap(cap);
            }

            key = data.optString(PAINT_STROKE_JOIN);
            if(!key.isEmpty()){
                Join join = Join.valueOf(key);
                if(join != null) setStrokeJoin(join);
            }

            setStrokeMiter((float) data.optDouble(PAINT_STROKE_MITER));
            setStrokeWidth((float) data.optDouble(PAINT_STROKE_WIDTH));

            key = data.optString(PAINT_STYLE);
            if(!key.isEmpty()){
                Style style = Style.valueOf(key);
                if(style != null) setStyle(style);
            }

            key = data.optString(PAINT_XFER_MODE);
            if(!key.isEmpty()){
                PorterDuff.Mode portMode = PorterDuff.Mode.valueOf(key);
                if(portMode != null) setXfermode(portMode);
            }

            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    public NCPaint clone(){
        return new NCPaint(this);
    }

    public void destroy(){

    }

    @Override
    public String toString() {
        return "{"
                + "\n" + PAINT_ANTI_ALIAS + ":" + isAntiAlias() + ","
                + "\n" + PAINT_COLOR + ":" + getColor() + ","
                + "\n" + PAINT_FILTER_BITMAP + ":" + isFilterBitmap() + ","
                + "\n" + PAINT_STROKE_CAP + ":" + getStrokeCap().name() + ","
                + "\n" + PAINT_STROKE_JOIN + ":" + getStrokeJoin().name() + ","
                + "\n" + PAINT_STROKE_MITER + ":" + getStrokeMiter() + ","
                + "\n" + PAINT_STROKE_WIDTH + ":" + getStrokeWidth() + ","
                + "\n" + PAINT_STYLE + ":" + getStyle().name() + ","
                + "\n" + PAINT_XFER_MODE + ":" + mode
                + "\n}"
                ;
    }

}
