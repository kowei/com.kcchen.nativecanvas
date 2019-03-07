package com.kcchen.nativecanvas.drawtools;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.kcchen.nativecanvas.drawtools.attributes.ToolAttributes;
import com.kcchen.nativecanvas.drawtools.model.NCPointF;


/**
 * Created by ween on 11/15/14.
 */
public class Dropper extends Tool {

    private static final int TOOL_ID = 6;
    private final ToolAttributes toolAttributes;

    public Dropper(String name, Drawable icon) {
        super(name, icon, TOOL_ID);

        toolAttributes = new ToolAttributes();
        toolAttributes.setMature(false);
        toolAttributes.setDropper(true);
    }

    @Override
    protected void onStart(Bitmap bitmap, NCPointF event) {
        pickColour(bitmap, event);
    }

    @Override
    protected void onMove(Bitmap bitmap, NCPointF event) {
        pickColour(bitmap, event);
    }

    @Override
    protected void onEnd(Bitmap bitmap, NCPointF event) {
        pickColour(bitmap, event);
    }

    @Override
    public ToolAttributes getToolAttributes() {
        return toolAttributes;
    }

    // Selects colour under the user's finger
    private void pickColour(Bitmap bitmap, NCPointF point) {
        int pickedColour;
        if (isInBounds(bitmap, point)) {
            // In bounds, picks the colour under the user's finger
            pickedColour = bitmap.getPixel((int) point.x, (int) point.y);
        } else {
            // Out of bounds, picks the current drawing colour
            pickedColour = toolAttributes.getPaint().getColor();
        }
        toolReport.setDropColour(pickedColour);
    }
}
