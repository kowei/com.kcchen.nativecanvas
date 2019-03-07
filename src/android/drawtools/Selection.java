package com.kcchen.nativecanvas.drawtools;

import android.graphics.drawable.Drawable;

import com.kcchen.nativecanvas.drawtools.attributes.ToolAttributes;
import com.kcchen.nativecanvas.drawtools.model.NCPath;
import com.kcchen.nativecanvas.drawtools.model.NCPointF;


/**
 * Created by ween on 11/14/14.
 */
abstract class Selection extends Tool {

    protected ToolAttributes toolAttributes;
    private NCPath path;
    private NCPath inverse;

    protected Selection(String name, Drawable icon, int toolId) {
        super(name, icon, toolId);

        toolAttributes = new ToolAttributes();
        toolAttributes.setMature(false);
        toolAttributes.setSelector(true);
    }

    protected void setPath(NCPath path, NCPath inverse) {
        this.path = path;
        this.inverse = inverse;
    }

    protected void roundCoordinates(NCPointF point) {
        point.x = Math.round(point.x);
        point.y = Math.round(point.y);
    }

    protected void clampPoint(int width, int height, NCPointF point) {
        if (point.x < 0) {
            point.x = 0;
        } else if (point.x >= width) {
            point.x = width;
        }

        if (point.y < 0) {
            point.y = 0;
        } else if (point.y >= height) {
            point.y = height;
        }
    }

    protected void pathReset() {
        path.reset();
        inverse.reset();
    }

    protected void pathMoveTo(float x, float y) {
        path.moveTo(x, y);
        inverse.moveTo(x, y);
    }

    protected void pathLineTo(float x, float y) {
        path.lineTo(x, y);
        inverse.lineTo(x, y);
    }

    protected void pathClose() {
        path.close();
        inverse.close();
    }
}
