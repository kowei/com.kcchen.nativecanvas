package com.kcchen.nativecanvas.drawtools.model;

import android.graphics.Paint;
import android.text.TextPaint;

/**
 * Created by kowei on 2018/3/13.
 */

public class NCTextPaint extends TextPaint {
    public NCTextPaint() {
    }

    public NCTextPaint(int flags) {
        super(flags);
    }

    public NCTextPaint(Paint p) {
        super(p);
    }
}
