package com.kcchen.nativecanvas.drawtools;

import android.graphics.Bitmap;

import com.kcchen.nativecanvas.drawtools.model.NCPointF;

/**
 * Created by ween on 10/19/14.
 */
public interface Command {

    /**
     * This method is called when the user begins a drawing with this tool. Begin the drawing
     * operation here.
     * @param bitmap The bitmap to draw upon
     * @param event The coordinates of the drawing event
     */
    ToolReport start(Bitmap bitmap, float x, float y, long stamp);

    /**
     * This method is called when the user moves their finger across the screen. Update the drawing
     * operation if necessary. The bitmap will be clear of previous drawings from during the
     * lifecycle of this operation.
     * @param bitmap The bitmap to draw upon
     * @param x The coordinates of the drawing event
     * @param y The coordinates of the drawing event
     * @param stamp The coordinates of the drawing event
     * @return The Region which this tool has taken over the lifecycle of this drawing operation
     */
    ToolReport move(Bitmap bitmap, float x, float y, long stamp);

    /**
     * This method is called when the drawing operation has been successfully completed. Finish
     * any drawing operations here. The bitmap will be clear of previous drawings from during the
     * lifecycle of this operation.
     * @param bitmap The bitmap to draw upon
     * @param x The coordinates of the drawing event
     * @param y The coordinates of the drawing event
     * @param stamp The coordinates of the drawing event
     * @return The Region which this tool had taken over the lifecycle of this drawing operation
     */
    ToolReport end(Bitmap bitmap, float x, float y, long stamp);

    /**
     * This method is called when a drawing operation has is to be cancelled (e.g. due to a
     * zoom occurring). Any ongoing drawing operation is to be stopped.
     */
    void cancel();

}
