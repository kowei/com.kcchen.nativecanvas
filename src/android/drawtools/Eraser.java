package com.kcchen.nativecanvas.drawtools;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;

import com.kcchen.nativecanvas.drawtools.attributes.EraserToolAttributes;
import com.kcchen.nativecanvas.drawtools.attributes.ToolAttributes;
import com.kcchen.nativecanvas.drawtools.model.NCPointF;


/**
 * Created by ween on 9/28/14.
 */
public class Eraser extends Tool {

    private static final int TOOL_ID = 1;
    private final EraserToolAttributes toolAttributes;
    private NCPointF start = new NCPointF();

    public Eraser(String name, Drawable icon) {
        super(name, icon, TOOL_ID);
        toolAttributes = new EraserToolAttributes();

        // Needed to erase (draws transparent lines)
        toolAttributes.getPaint().setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onStart(Bitmap bitmap, NCPointF event) {
        start.x = event.x;
        start.y = event.y;

        draw(bitmap, event);
    }

    @Override
    protected void onMove(Bitmap bitmap, NCPointF event) {
        draw(bitmap, event);
    }

    @Override
    protected void onEnd(Bitmap bitmap, NCPointF event) {
        draw(bitmap, event);
    }

    @Override
    public ToolAttributes getToolAttributes() {
        return toolAttributes;
    }

    private void draw(Bitmap bitmap, NCPointF event) {
        toolReport.getPath().lineTo(event.x, event.y);
        canvas.setBitmap(bitmap);

        // Work around for drawing individual pixels, which Canvas.drawPath() doesn't do well
        if (((EraserToolAttributes) toolAttributes).getThicknessLevel() == ToolAttributes.MIN_THICKNESS) {
            canvas.drawPoint(start.x, start.y, toolAttributes.getPaint());
            canvas.drawPath(toolReport.getPath(), toolAttributes.getPaint());
        } else if (start.x == event.x && start.y == event.y) {
            canvas.drawPoint(start.x, start.y, toolAttributes.getPaint());
        } else {
            // Regular drawing
            canvas.drawPath(toolReport.getPath(), toolAttributes.getPaint());
        }
    }
}
