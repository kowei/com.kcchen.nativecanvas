package com.kcchen.nativecanvas.drawtools.model;

import android.graphics.Point;
import android.graphics.PointF;

import com.kcchen.nativecanvas.utils.Utility;

/**
 * Created by kowei on 2018/3/13.
 */

public class NCPointF extends PointF {
    private static final String TAG = NCPointF.class.getSimpleName();
    private static final String SEP = "#";
    private int stamp;

    public NCPointF() {
        stamp = 0;
    }

    public NCPointF(float x, float y) {
        super(x, y);
        stamp = 0;
    }

    public NCPointF(float x, float y, int stamp) {
        super(x, y);
        this.stamp = stamp;
    }

    public NCPointF(String dataString) {
        super();
        String[] data = dataString.split(SEP);
        if(data.length == 2){
            x = Float.parseFloat(data[0]);
            y = Float.parseFloat(data[1]);
        }else if(data.length == 3){
            x = Float.parseFloat(data[0]);
            y = Float.parseFloat(data[1]);
            stamp = Integer.parseInt(data[2]);
        }
    }

    public NCPointF(Point p) {
        super(p);
        stamp = 0;
    }

    public long getStamp() {
        return stamp;
    }

    @Override
    public String toString() {
        return "\""
                + Float.parseFloat(Utility.formatFixedFloat(x, 4, 2, false)) + SEP
                + Float.parseFloat(Utility.formatFixedFloat(y, 4, 2, false)) + SEP
                + stamp
                + "\""
                ;
    }

}
