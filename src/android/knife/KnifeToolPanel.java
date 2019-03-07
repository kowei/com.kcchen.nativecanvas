package com.kcchen.nativecanvas.knife;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.kcchen.penpal.R;

/**
 * Created by kowei on 2018/2/10.
 */

public class KnifeToolPanel extends RelativeLayout {

    private final ImageButton makeBold;
    private final ImageButton makeItalic;
    private final ImageButton makeUnderline;
    private final ImageButton makeBackground;
    private final ImageButton makeForeground;
    private final ImageButton makeStrikethrough;
    private final ImageButton makeTextUp;
    private final ImageButton makeTextDown;

    public KnifeToolPanel(Context context) {
        super(context);
        View view = inflate(context, R.layout.nc__text_tool_panel, null);
        addView(view);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);

        makeBold = findViewById(R.id.makeBold);
        makeItalic = findViewById(R.id.makeItalic);
        makeUnderline = findViewById(R.id.makeUnderline);
        makeBackground = findViewById(R.id.makeBackground);
        makeForeground = findViewById(R.id.makeForeground);
        makeStrikethrough = findViewById(R.id.makeStrikethrough);
        makeTextUp = findViewById(R.id.makeTextUp);
        makeTextDown = findViewById(R.id.makeTextDown);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener clickListener) {
        makeBold.setOnClickListener(clickListener);
        makeItalic.setOnClickListener(clickListener);
        makeUnderline.setOnClickListener(clickListener);
        makeBackground.setOnClickListener(clickListener);
        makeForeground.setOnClickListener(clickListener);
        makeStrikethrough.setOnClickListener(clickListener);
        makeTextUp.setOnClickListener(clickListener);
        makeTextDown.setOnClickListener(clickListener);
    }

    public void enableButtons(){
        makeBold.setEnabled(true);
        makeItalic.setEnabled(true);
        makeUnderline.setEnabled(true);
        makeBackground.setEnabled(true);
        makeForeground.setEnabled(true);
        makeStrikethrough.setEnabled(true);
        makeTextUp.setEnabled(true);
        makeTextDown.setEnabled(true);
        this.setAlpha(1.0f);
    }

    public void disableButtons(){
        makeBold.setEnabled(false);
        makeItalic.setEnabled(false);
        makeUnderline.setEnabled(false);
        makeBackground.setEnabled(false);
        makeForeground.setEnabled(false);
        makeStrikethrough.setEnabled(false);
        makeTextUp.setEnabled(false);
        makeTextDown.setEnabled(false);
        this.setAlpha(0.5f);
    }

}
