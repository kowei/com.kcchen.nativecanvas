package com.kcchen.nativecanvas.view;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kcchen.nativecanvas.utils.Utility;
import com.kcchen.penpal.R;

import java.lang.reflect.Field;

/**
 * Created by kowei on 2017/12/21.
 */

public class NCEditText extends AppCompatEditText {
    private static final String TAG = NCEditText.class.getSimpleName();
    private Context context;

    public NCEditText(Context context) {
        super(context);
        setup(context);
    }

    public NCEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context);
    }

    public NCEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context);
    }

    public void setup(Context context){
        this.context = context;
        Utility.setRelativeLayout(this, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,0,0,0,0);
        setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        setBackgroundResource(android.R.color.transparent);
        try {
            // https://github.com/android/platform_frameworks_base/blob/kitkat-release/core/java/android/widget/TextView.java#L562-564
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            f.set(this, R.drawable.nc__cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setFocusableInTouchMode(true);
        setFocusable(true);
        setHint("input");
        setTextColor(Color.BLACK);
    }
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        //Log.i(TAG,"> NCN onSelectionChanged " + selStart + " - " + selEnd);
    }
}
