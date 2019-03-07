package com.kcchen.nativecanvas.view;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by kowei on 2017/12/18.
 */

public class NCRect implements Cloneable{
    private static final String TAG = NCRect.class.getSimpleName();
    public static final String RECT_TOP     = "RectTop";
    public static final String RECT_RIGHT   = "RectRight";
    public static final String RECT_BOTTOM  = "RectBottom";
    public static final String RECT_LEFT    = "RectLeft";

    private RectF rect;
    private PointF center;

    public NCRect(RectF rect) {
        this.rect = rect;
        this.center = new PointF();
    }

    public NCRect() {
        this.rect = new RectF();
        this.center = new PointF();
    }

    public NCRect(JSONObject data) {
        this.rect = new RectF();
        this.center = new PointF();
        importData(data);
    }

    public NCRect setLeft(float left){
        this.rect.left = left;
        return this;
    }

    public NCRect moveLeft(int x) {
        this.rect.set(x, top(), x + width(), bottom());
        return this;
    }

    public NCRect moveTop(int y) {
        this.rect.set(left(), y, right(), y + height());
        return this;
    }

    public NCRect setRight(float right){
        this.rect.right = right;
        return this;
    }

    public NCRect setTop(float top){
        this.rect.top = top;
        return this;
    }

    public NCRect setBottom(float bottom){
        this.rect.bottom = bottom;
        return this;
    }

    public NCRect set(int left, int top, int right, int bottom){
        this.rect.set(left, top, right, bottom);
        return this;
    }

    public NCRect set(float left, float top, float right, float bottom) {
        this.rect.set((int)left, (int)top, (int)right, (int)bottom);
        return this;
    }

    public RectF rect(){
        return this.rect;
    }

    public float width() {
        return this.rect.width();
    }

    public float height() {
        return this.rect.height();
    }

    public float left() {
        return this.rect.left;
    }

    public float right() {
        return this.rect.right;
    }

    public float top() {
        return this.rect.top;
    }

    public float bottom() {
        return this.rect.bottom;
    }

    public PointF center() {
        this.center.set(rect.centerX(), rect.centerY());
        return this.center;
    }

    public PointF absCenter() {
        this.center.set(rect.centerX() - left(), rect.centerY() - top());
        return this.center;
    }

    public NCRect cloneRect() {
        try {
            NCRect newRect = (NCRect) this.clone();
            //Log.i(TAG,"> NCN clone " + newRect);
            return newRect;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isInside(float x, float y){
        return this.rect.contains((int)x, (int) y);
    }

    public boolean isInside(PointF origin, float x, float y) {
        return this.rect.contains((int) (x - origin.x), (int) (y - origin.y));
    }

    public boolean isValid(){
        return this.rect != null && !this.rect.isEmpty();
    }

    public JSONObject exportData() {
        JSONObject data = new JSONObject();
        try {
            data.put(RECT_TOP, rect.top );
            data.put(RECT_RIGHT, rect.right );
            data.put(RECT_BOTTOM, rect.bottom );
            data.put(RECT_LEFT, rect.left );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        try {
            setLeft((float) data.optDouble(RECT_LEFT));
            setTop((float) data.optDouble(RECT_TOP));
            setRight((float) data.optDouble(RECT_RIGHT));
            setBottom((float) data.optDouble(RECT_BOTTOM));
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    @Override
    public int hashCode() {
        if (this.rect != null) {
            return 0
                    + ("left:" + this.rect.left).hashCode()
                    + ("right:" + this.rect.right).hashCode()
                    + ("top:" + this.rect.top).hashCode()
                    + ("bottom:" + this.rect.bottom).hashCode()
                    ;
        }
        return -1;
    }

    @Override
    public String toString() {
        return width() + " x " + height() + " l:" + rect.left + " r:" + rect.right + " t:" + rect.top + " b:" + rect.bottom + " [center]" + rect.centerX() + " x " + rect.centerY();
    }

}
