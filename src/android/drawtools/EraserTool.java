package com.kcchen.nativecanvas.drawtools;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.kcchen.nativecanvas.drawtools.attributes.EraserToolAttributes;
import com.kcchen.nativecanvas.drawtools.attributes.ToolAttributes;
import com.kcchen.nativecanvas.drawtools.model.NCPointF;


/**
 * Created by ween on 9/28/14.
 */
public class EraserTool extends Tool {

    private static final int TOOL_ID = 1;
    private final EraserToolAttributes toolAttributes;
    private NCPointF start = new NCPointF();
    private NCPointF current = new NCPointF();

    public EraserTool(String name, Drawable icon) {
        super(name, icon, TOOL_ID);
        toolAttributes = new EraserToolAttributes();

        // Needed to erase (draws transparent lines)
        toolAttributes.getPaint().setXfermode(PorterDuff.Mode.CLEAR);

        float[] intervals = new float[]{7.0f, 3.0f};
        float phase = 0;
        DashPathEffect dashPathEffect = new DashPathEffect(intervals, phase);
        toolAttributes.getPointPaint().setAntiAlias(true);
        toolAttributes.getPointPaint().setColor(Color.DKGRAY);
        toolAttributes.getPointPaint().setStyle(Paint.Style.STROKE);
        toolAttributes.getPointPaint().setPathEffect(dashPathEffect);
    }

    public NCPointF getCurrent() {
        return current;
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
        current.x = event.x;
        current.y = event.y;
        //toolReport.getPath().lineTo(event.x, event.y);
        if(bitmap != null){
            canvas.setBitmap(bitmap);
            canvas.drawPath(toolReport.getPath(), toolAttributes.getPaint());
        }
    }

    public void setColor(int color){
        toolAttributes.getPaint().setColor(color);
    }

    public void setAntiAlias(boolean isAntiAlias){
        toolAttributes.getPaint().setAntiAlias(isAntiAlias);
    }

    public void setStrokeWidth(int width){
        toolAttributes.getPaint().setStrokeWidth(width);
    }

}
