package com.kcchen.nativecanvas.drawtools.attributes;

import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.kcchen.nativecanvas.drawtools.model.NCPaint;

/**
 * Created by ween on 11/2/14.
 */
public class EraserToolAttributes extends ToolAttributes {

    private boolean antiAlias = false;
    private int thicknessLevel = MIN_THICKNESS;
    private int color;
    private int alpha;
    private Paint.Style style;
    private DashPathEffect pathEffect;
    private NCPaint pointPaint = new NCPaint();

    public EraserToolAttributes() {
        super();
    }

    public boolean isAntiAlias() {
        return antiAlias;
    }

    public void setAntiAlias(boolean antiAlias) {
        paint.setAntiAlias(antiAlias);
        this.antiAlias = antiAlias;
    }

    public void setThicknessLevel(int thicknessLevel) {
        paint.setStrokeWidth(thicknessLevel);
        this.thicknessLevel = thicknessLevel;
    }

    public int getThicknessLevel() {
        return thicknessLevel;
    }

    public void setColor(int color) {
        paint.setColor(color);
        this.color = color;
    }

    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        this.alpha = alpha;
    }

    public void setStyle(Paint.Style style) {
        paint.setStyle(style);
        this.style = style;
    }

    public void setPathEffect(DashPathEffect pathEffect) {
        paint.setPathEffect(pathEffect);
        this.pathEffect = pathEffect;
    }

    public NCPaint getPointPaint() {
        return pointPaint;
    }
}
