package com.kcchen.nativecanvas.drawtools;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.kcchen.nativecanvas.drawtools.attributes.PenToolAttributes;
import com.kcchen.nativecanvas.drawtools.attributes.ToolAttributes;
import com.kcchen.nativecanvas.drawtools.model.NCPointF;


/**
 * Created by ween on 9/28/14.
 */
// This is a pen
public class PenTool extends Tool {
    private static final String TAG = PenTool.class.getSimpleName();
    private static final int TOOL_ID = 0;
    private final PenToolAttributes toolAttributes;
    private NCPointF start = new NCPointF();

    public PenTool(String name, Drawable icon) {
        super(name, icon, TOOL_ID);
        toolAttributes = new PenToolAttributes();
        toolAttributes.getPaint().setXfermode(PorterDuff.Mode.SRC_OVER);
        toolAttributes.getPaint().setStrokeJoin(Paint.Join.ROUND);
        toolAttributes.getPaint().setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onStart(Bitmap bitmap, NCPointF event) {
        start.x = (int) event.x;
        start.y = (int) event.y;

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
