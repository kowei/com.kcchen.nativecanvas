package com.kcchen.nativecanvas.multitouch;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;

/**
 * Created by kowei on 2018/3/6.
 */

public class TapGestureDetector extends GestureDetector {
    public TapGestureDetector(Context context, GestureDetector.OnGestureListener listener) {
        super(context, listener);
        init();
    }

    public TapGestureDetector(Context context, GestureDetector.OnGestureListener listener, Handler handler) {
        super(context, listener, handler);
        init();
    }

    private void init() {

    }


}
