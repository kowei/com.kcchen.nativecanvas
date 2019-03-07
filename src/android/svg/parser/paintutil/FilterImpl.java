package com.kcchen.nativecanvas.svg.parser.paintutil;

import android.graphics.RectF;

import com.kcchen.nativecanvas.svg.parser.FilterOp;
import com.kcchen.nativecanvas.svg.parser.support.GraphicsSVG;


/**
 * Basic class for filter implementations.
 */
public abstract class FilterImpl {
    public FilterImpl() {
    }

    public abstract void handle(FilterOp fop, GraphicsSVG canvas, boolean stroke, RectF bounds);
}
