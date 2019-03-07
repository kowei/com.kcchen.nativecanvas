package com.kcchen.nativecanvas.drawtools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.kcchen.nativecanvas.drawtools.attributes.ToolAttributes;
import com.kcchen.nativecanvas.drawtools.model.NCPointF;


/**
 * To make a Tool:
 *  1) Subclass Tool implementing 'onStart()', 'onMove()', 'onEnd()', draw with canvas.setBitmap(bitmap)
 *  2) Give the tool a unique ID in the call to the super constructor
 *  3) If the tool has user configurable attributes, implement a subclass of ToolAttributes and
 *     create a corresponding XML layout. Subclass ToolOptionsView and linkup the UI.
 *  4) In the constructor of the tool, set the 'toolAttributes' variable to an instance of ToolAttributes
 *  5) In ToolboxFragment, create an instance of your tool, options, attributes and ImageButton.
 *  6) In initialiseViews() of ToolboxFragment, call tool.setToolAttributes(attributes). Done!
 */
public abstract class Tool implements Command {
    private static final String TAG = Tool.class.getSimpleName();

    // User interface
    protected final String name;
    protected final Drawable icon;

    // Used to retrieve the tool on config change
    protected final int toolId;

    // Drawing
    protected Canvas canvas = new Canvas();

    protected ToolReport toolReport;
    protected boolean cancelled = false;

    public Tool(String name, Drawable icon, int toolId) {
        this.name = name;
        this.icon = icon;
        this.toolId = toolId;

        toolReport = new ToolReport();
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public int getToolId() {
        return toolId;
    }

    public ToolReport getToolReport() {
        return toolReport;
    }

    @Override
    public final ToolReport start(Bitmap bitmap, float x, float y, long stamp) {
        cancelled = false;
        toolReport.reset();
        toolReport.getPath().moveTo(x, y, stamp);
        onStart(bitmap, toolReport.getPath().getLastPoint());

        return toolReport;
    }

    @Override
    public final ToolReport move(Bitmap bitmap, float x, float y, long stamp) {
        if (!cancelled) {
            toolReport.getPath().lineTo(x, y, stamp);
            onMove(bitmap, toolReport.getPath().getLastPoint());
            return toolReport;
        }
        return toolReport;
    }

    @Override
    public final ToolReport end(Bitmap bitmap, float x, float y, long stamp) {
        if (!cancelled) {
            toolReport.getPath().lineTo(x, y, stamp);
            onEnd(bitmap, toolReport.getPath().getLastPoint());
            return toolReport;
        }
        return toolReport;
    }

    @Override
    public final void cancel() {
        Log.e(TAG,"> NCN cancel");
        cancelled = true;
        toolReport.reset();
    }

    protected abstract void onStart(Bitmap bitmap, NCPointF event);

    protected abstract void onMove(Bitmap bitmap, NCPointF event);

    protected abstract  void onEnd(Bitmap bitmap, NCPointF event);

    protected static boolean isInBounds(Bitmap bitmap, NCPointF point) {
        if (point.x >= 0 && point.x < bitmap.getWidth()) {
            if (point.y >= 0 && point.y < bitmap.getHeight()) {
                return true;
            }
        }
        return false;
    }

    public abstract ToolAttributes getToolAttributes();


}
