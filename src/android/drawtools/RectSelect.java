package com.kcchen.nativecanvas.drawtools;


import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

import com.kcchen.nativecanvas.drawtools.attributes.ToolAttributes;
import com.kcchen.nativecanvas.drawtools.model.NCPath;
import com.kcchen.nativecanvas.drawtools.model.NCPointF;

/**
 * Created by ween on 11/10/14.
 */
public class RectSelect extends Selection {

    private static final int TOOL_ID = 4;
    private NCPointF start = new NCPointF();
    private NCPath inversePath = new NCPath();

    public RectSelect(String name, Drawable icon) {
        super(name, icon, TOOL_ID);
    }

    @Override
    protected void onStart(Bitmap bitmap, NCPointF event) {
        roundCoordinates(event);
        clampPoint(bitmap.getWidth(), bitmap.getHeight(), event);

        start.x = event.x;
        start.y = event.y;
        toolReport.getPath().setFillType(Path.FillType.WINDING);
        inversePath.setFillType(Path.FillType.INVERSE_WINDING);
        setPath(toolReport.getPath(), inversePath);

        rectPathRegion(event);
    }

    @Override
    protected void onMove(Bitmap bitmap, NCPointF event) {
        // Aligns the selected region the image pixels and creates a rectangle out of the path
        roundCoordinates(event);
        clampPoint(bitmap.getWidth(), bitmap.getHeight(), event);
        rectPathRegion(event);
    }

    @Override
    protected void onEnd(Bitmap bitmap, NCPointF event) {
        // Aligns the selected region the image pixels and creates a rectangle out of the path
        roundCoordinates(event);
        clampPoint(bitmap.getWidth(), bitmap.getHeight(), event);
        rectPathRegion(event);
        toolReport.getInversePath().set(inversePath);
    }

    @Override
    public ToolAttributes getToolAttributes() {
        return toolAttributes;
    }

    // Creates a rectangular path
    private void rectPathRegion(NCPointF event) {
        pathReset();
        pathMoveTo(start.x, start.y);
        pathLineTo(event.x, start.y);
        pathLineTo(event.x, event.y);
        pathLineTo(start.x, event.y);
        pathClose();
    }
}
