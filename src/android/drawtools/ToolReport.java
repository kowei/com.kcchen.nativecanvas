package com.kcchen.nativecanvas.drawtools;

import android.util.Log;

import com.kcchen.nativecanvas.drawtools.model.NCPath;

/**
 * Created by ween on 11/14/14.
 */
public class ToolReport {
    private static final String TAG = ToolReport.class.getSimpleName();

    // The path taken over the lifecycle of the this drawing operation
    private NCPath path = new NCPath();

    // The inverse of the path
    private NCPath inversePath = new NCPath();

    // The colour to be returned
    protected int dropColour = 0;

    public void reset() {
        Log.e(TAG,"> NCN reset ");
        path.reset();
        inversePath.reset();
        dropColour = 0;
    }

    public NCPath getPath() {
        return path;
    }

    public NCPath getInversePath() {
        return inversePath;
    }

    public int getDropColour() {
        return dropColour;
    }

    public void setDropColour(int dropColour) {
        this.dropColour = dropColour;
    }
}
