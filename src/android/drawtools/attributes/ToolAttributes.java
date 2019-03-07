package com.kcchen.nativecanvas.drawtools.attributes;

import android.graphics.Paint;

import com.kcchen.nativecanvas.drawtools.model.NCPaint;

/**
 * Created by ween on 11/2/14.
 */
public class ToolAttributes {

    public static final int MIN_THICKNESS = 1;

    // Paint with which this tool will draw
    protected NCPaint paint;

    // This tool modifies the drawing (for example selection tool will not mutate the drawing)
    protected boolean mature = true;

    // This tool returns a selectable region
    protected boolean selector = false;

    // This tool returns a colour
    protected boolean dropper = false;

    public ToolAttributes() {
        paint = new NCPaint();
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.STROKE);
    }

    public final NCPaint getPaint() {
        return paint;
    }

    public boolean isMature() {
        return mature;
    }

    public void setMature(boolean mature) {
        this.mature = mature;
    }

    public boolean isSelector() {
        return selector;
    }

    public void setSelector(boolean selectable) {
        this.selector = selectable;
    }

    public boolean isDropper() {
        return dropper;
    }

    public void setDropper(boolean dropper) {
        this.dropper = dropper;
    }

}
