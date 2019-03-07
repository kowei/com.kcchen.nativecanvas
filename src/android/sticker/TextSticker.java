package com.kcchen.nativecanvas.sticker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.kcchen.nativecanvas.knife.KnifeText;
import com.kcchen.nativecanvas.knife.spans.KnifeAbsoluteSizeSpan;
import com.kcchen.nativecanvas.model.NCLayerDataText;
import com.kcchen.nativecanvas.utils.FontProvider;
import com.kcchen.nativecanvas.utils.Utility;
import com.kcchen.nativecanvas.view.NCManager;
import com.kcchen.nativecanvas.view.NCRect;
import com.kcchen.nativecanvas.view.NCStickerView;
import com.kcchen.nativecanvas.view.RotateLayout;

import org.json.JSONObject;

import java.util.List;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;

import static com.kcchen.nativecanvas.sticker.ImageSticker.STICKER_LAYER;
import static com.kcchen.nativecanvas.sticker.ImageSticker.STICKER_RECT;


public class TextSticker extends Sticker {
    protected static final String TAG = TextSticker.class.getSimpleName();
    private static final String STICKER_KNIFE = "StickerKnife";

    private TextPaint textPaint;
    private FontProvider fontProvider;
    private NCStickerView parentView;
    private float textSizeRatio;
    private float textScaleRatio;
    private KnifeText knife;
    private RotateLayout editLayout;
    @Nullable
    private Bitmap bitmap;
    private Bitmap finalBitmap;

    public TextSticker(@NonNull NCStickerView stickerView,
                       @NonNull NCLayerDataText textLayer,
                       @IntRange(from = 1) int canvasWidth,
                       @IntRange(from = 1) int canvasHeight,
                       @NonNull FontProvider fontProvider) {
        super(textLayer, canvasWidth, canvasHeight);
        this.parentView = stickerView;
        this.fontProvider = fontProvider;
        init();
    }

    public TextSticker(Activity activity, NCStickerView stickerView, FontProvider fontProvider, NCLayerDataText layer, JSONObject json) {
        super(layer);
        this.parentView = stickerView;
        this.fontProvider = fontProvider;
        init();
        importData(json);
        setActivity(activity);
    }

    public void initEditText() {
        if (parentView != null && activity != null) {
            textScaleRatio = getLayer().getFont().getSize() * stickerDisplayWidth / knife.getTextSize();
            textSizeRatio = getLayer().getFont().getSize() * stickerDisplayWidth / textPaint.getTextSize();
            hideEditText();
            setDebug();
        }
    }

    private void setKnife(JSONObject knifeData) {
        if (knife != null && isUpdate()) knife.importData(knifeData);
    }

    private void setLayer(JSONObject data) {
        if (layer != null && isUpdate()) layer.importData(data);
    }

    private void setDisplayRect(JSONObject data) {
        if (displayRect == null || isUpdate()) displayRect = new NCRect(data);
    }

    @Override
    public void setActivity(Activity activity) {
        //Log.d(TAG,"> setActivity");
        super.setActivity(activity);
        initEditText();
        knife.setText(getLayer().getSpannableText());
        knife.setTypeface(fontProvider.getTypeface(getLayer().getFont().getTypeface()));
        knife.setInputType(knife.getInputType()
                | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | EditorInfo.TYPE_TEXT_VARIATION_FILTER
        );
        knife.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (knife.getToolPanel().getParent() == null) {
            parentView.addView(knife.getToolPanel());
        }
    }

    public void focus() {
        knife.post(new Runnable() {
            @Override
            public void run() {
                // force show the keyboard
                knife.requestFocus();
                Selection.setSelection(knife.getText(), knife.length());
                showKeyboard(knife);
            }
        });
    }

    public void hideKeyboard(View view) {
        // Check if no view has focus:
        //Log.d(TAG,"> NCN hideKeyboard parentView:"+ parentView+ " context:"+ activity);
        if (parentView != null && activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showKeyboard(View view) {
        //Log.d(TAG,"> NCN showKeyboard parentView:"+ parentView+ " context:"+ activity);
        if (parentView != null && activity != null) {
            InputMethodManager ims = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            ims.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void showEditText() {
        //Log.d(TAG,"> NCN showEditText parentView:"+ parentView+ " context:"+ context);
        initEditText();
        if (editLayout != null) {
            editLayout.setVisibility(View.VISIBLE);
        }
        knife.getToolPanel().setVisibility(View.VISIBLE);
        updateTextSticker();
        parentView.invalidate();
        focus();
    }

    public void hideEditText() {
        //Log.d(TAG,"> NCN hideEditText parentView:"+ parentView+ " context:"+ context);
        hideKeyboard(knife);
        if (editLayout != null) {
            editLayout.setVisibility(View.INVISIBLE);
            updateTextSticker();
        }
        knife.getToolPanel().setVisibility(View.INVISIBLE);
    }

    private void updateTextSticker(boolean moveToPreviousCenter) {
        //if (activity != null && isUpdate()) {
        if (activity != null) {
            // save previous center
            PointF oldCenter = getCenter();

            Bitmap newBmp = createBitmap(getLayer(), bitmap);
            // recycle previous bitmap (if not reused) as soon as possible
            if (bitmap != null && bitmap != newBmp && !bitmap.isRecycled()) {
                Log.wtf(TAG, "> BITMAP_RECYCLED " + bitmap);
                bitmap.recycle();
            }

            this.bitmap = newBmp;

            this.width = bitmap.getWidth();
            this.height = bitmap.getHeight();
            //Log.d(TAG,"> NCN updateTextSticker " + width + " x " + height);
            this.distance = (int) (Math.sqrt(Math.pow(getStickerWidth(), 2) + Math.pow(getStickerHeight(), 2)) / 2);
            float widthAspect = 1.0F * stickerDisplayWidth / this.getStickerWidth();
            float heightAspect = 1.0F * stickerDisplayHeight / this.getStickerHeight();
            //fit the smallest size
            scalefitRatio = Math.min(widthAspect, heightAspect);
            // this.scalefitRatio = widthAspect;
            if (stickerView != null)
                this.distanceRatio = this.distance * this.scalefitRatio * stickerView.getScaleX();
            //Log.w(TAG,"> NCN init " + distanceRatio);

            // initial position of the entity
            srcPoints[0] = 0;                                  // origin.x + 0;
            srcPoints[1] = 0;                                  // origin.y + 0;
            srcPoints[2] = this.getStickerWidth();             // origin.x + this.getStickerWidth();
            srcPoints[3] = 0;                                  // origin.y + 0;
            srcPoints[4] = this.getStickerWidth();             // origin.x + this.getStickerWidth();
            srcPoints[5] = this.getStickerHeight();            // origin.y + this.getStickerHeight();
            srcPoints[6] = 0;                                  // origin.x + 0;
            srcPoints[7] = this.getStickerHeight();            // origin.y + this.getStickerHeight();
            srcPoints[8] = 0;                                  // origin.x + 0;
            srcPoints[9] = 0;                                  // origin.y + 0;
            srcPoints[10] = (this.getStickerWidth() / 2);       // origin.x + (this.getStickerWidth() / 2);
            srcPoints[11] = 0;                                  // origin.y + 0;
            srcPoints[12] = (this.getStickerWidth() / 2);       // origin.x + (this.getStickerWidth() / 2);
            srcPoints[13] = -this.rotateLength;                  // origin.y + this.rotateLength;

            if (moveToPreviousCenter) {
                // move to previous center
                moveCenterTo(oldCenter);
            }
            //Log.d(TAG,"> NCN updateTextSticker " + srcPoint.hashCode());
            //呼叫以更新最後資料hash
            exportData();
        }
    }

    /**
     * If reuseBmp is not null, and size of the new bitmap matches the size of the reuseBmp,
     * new bitmap won't be created, reuseBmp it will be reused instead
     *
     * @param textLayer text to draw
     * @param reuseBmp  the bitmap that will be reused
     * @return bitmap with the text
     */
    @NonNull
    private Bitmap createBitmap(@NonNull NCLayerDataText textLayer, @Nullable Bitmap reuseBmp) {
        Log.d(TAG, "> createBitmap ");
        int boundsWidth = stickerDisplayWidth;
        StaticLayout layout = getStaticLayout(boundsWidth);

        // calculate height for the entity, min - Limits.MIN_BITMAP_HEIGHT
        int boundsHeight = (layout == null) ? 0 : layout.getHeight();

        // create bitmap not smaller than TextLayer.Limits.MIN_BITMAP_HEIGHT
        int bmpHeight = (int) (stickerDisplayHeight * Math.max(this.getLayer().getLimit().getMiniHeight(),
                1.0F * boundsHeight / stickerDisplayHeight));

        // create bitmap where text will be drawn
        Bitmap bmp;
        if (reuseBmp != null && reuseBmp.getWidth() == boundsWidth
                && reuseBmp.getHeight() == bmpHeight) {
            // if previous bitmap exists, and it's width/height is the same - reuse it
            bmp = reuseBmp;
            bmp.eraseColor(Color.TRANSPARENT); // erase color when reusing
        } else {
            bmp = Bitmap.createBitmap(boundsWidth, bmpHeight, Bitmap.Config.ARGB_8888);
        }

        // draws static layout on canvas
        if (editLayout.getVisibility() == View.INVISIBLE && layout != null) {
            Canvas canvas = new Canvas(bmp);
            canvas.save();

            // move text to center if bitmap is bigger that text
            if (boundsHeight < bmpHeight) {
                //calculate Y coordinate - In this case we want to draw the text in the
                //center of the canvas so we move Y coordinate to center.
                float textYCoordinate = (bmpHeight - boundsHeight) / 2;
                canvas.translate(0, textYCoordinate);
            }

            layout.draw(canvas);
            canvas.restore();
        }

        return bmp;
    }

    private StaticLayout getStaticLayout(int width) {
        //Log.i(TAG,"> NCN getStaticLayout " + textPaint.getTextSize() + "  " + getLayer().getFont().getSize() * stickerDisplayWidth + " " + getLayer().getSpannableText());

        if (getLayer().getSpannableText() == null) return null;

        int start = 0;
        int end = knife.length();
        int baseSize = (int) (textSizeRatio * textPaint.getTextSize());
        Editable text = getLayer().getSpannableText();

        KnifeAbsoluteSizeSpan[] sizeSpans = text.getSpans(start, end, KnifeAbsoluteSizeSpan.class);
        //Log.e(TAG,"> NCN size span:" + sizeSpans.length);
        text.setSpan(new KnifeAbsoluteSizeSpan(baseSize), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //Log.e(TAG,"> NCN size span all " + start + " - " + end + " @ " + textPaint.getTextSize() + " -> " + baseSize);
        for (KnifeAbsoluteSizeSpan span : sizeSpans) {
            int size = span.getSize();
            int newSize = (int) (size * textScaleRatio);
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);
            //Log.e(TAG,"> NCN size span " + spanStart + " - " + spanEnd + " @ " + size + " -> " + newSize);
            text.removeSpan(span);
            text.setSpan(new KnifeAbsoluteSizeSpan(newSize), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 假如要印 spannable，text paint 裡面就不能包含 text size, color資訊
        StaticLayout layout = new StaticLayout(
                text,                           // - text which will be drawn
                textPaint,
                width,                          // - width of the layout
                Layout.Alignment.ALIGN_CENTER,  // - layout alignment
                1,                              // 1 - text spacing multiply
                1,                              // 1 - text spacing add
                true);                          // true - include padding

        if (NCManager.isDebug) analysisTextSpan(text, start, end);

        return layout;
    }

    private void analysisTextSpan(Editable text, int start, int end) {
        int index = 1;

        Object[] spans = text.getSpans(start, end, Object.class);
        for (Object span : spans) {
            boolean isClassified = false;
            if (span instanceof StyleSpan) {
                StyleSpan s = (StyleSpan) span;
                //Log.i(TAG,"> NCN " + index + " " +Utility.getStyleName(s.getStyle()));
                isClassified = true;
            }
            if (span instanceof UnderlineSpan) {
                UnderlineSpan s = (UnderlineSpan) span;
                //Log.i(TAG,"> NCN " + index + " UnderlineSpan" + s.describeContents());
                isClassified = true;
            }
            if (span instanceof StrikethroughSpan) {
                StrikethroughSpan s = (StrikethroughSpan) span;
                //Log.i(TAG,"> NCN " + index + " StrikethroughSpan" + s.describeContents());
                isClassified = true;
            }
            if (span instanceof ForegroundColorSpan) {
                ForegroundColorSpan s = (ForegroundColorSpan) span;
                //Log.i(TAG,"> NCN " + index + " ForegroundColorSpan" + s.describeContents());
                isClassified = true;
            }
            if (span instanceof BackgroundColorSpan) {
                BackgroundColorSpan s = (BackgroundColorSpan) span;
                //Log.i(TAG,"> NCN " + index + " BackgroundColorSpan" + s.describeContents());
                isClassified = true;
            }
            if (span instanceof AbsoluteSizeSpan) {
                AbsoluteSizeSpan s = (AbsoluteSizeSpan) span;
                //Log.i(TAG,"> NCN " + index + " AbsoluteSizeSpan" + s.describeContents());
                isClassified = true;
            }
            if (span instanceof RelativeSizeSpan) {
                RelativeSizeSpan s = (RelativeSizeSpan) span;
                //Log.i(TAG,"> NCN " + index + " RelativeSizeSpan" + s.describeContents());
                isClassified = true;
            }
            if (span instanceof TypefaceSpan) {
                TypefaceSpan s = (TypefaceSpan) span;
                //Log.i(TAG,"> NCN " + index + " TypefaceSpan" + s.describeContents());
                isClassified = true;
            }
            if (span instanceof ImageSpan) {
                ImageSpan s = (ImageSpan) span;
                //Log.i(TAG,"> NCN " + index + " ImageSpan" + s.getSource());
                isClassified = true;
            }
            if (span instanceof SuperscriptSpan) {
                SuperscriptSpan s = (SuperscriptSpan) span;
                //Log.i(TAG,"> NCN " + index + " SuperscriptSpan" + s.describeContents());
                isClassified = true;
            }
            if (span instanceof SubscriptSpan) {
                SubscriptSpan s = (SubscriptSpan) span;
                //Log.i(TAG,"> NCN " + index + " SubscriptSpan" + s.describeContents());
                isClassified = true;
            }

            if (!isClassified) {
                //Log.i(TAG,"> NCN " + index + " unknown " + span);
            }
            index++;
        }
    }

    private void removeFromParent(View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    private boolean hasParent(View view) {
        if (view != null && view.getParent() != null) {
            return true;
        }
        return false;
    }

    @Override
    @NonNull
    public NCLayerDataText getLayer() {
        return (NCLayerDataText) layer;
    }

    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        View panel = knife.getToolPanel();
        int start = knife.getSelectionStart();
        int end = knife.getSelectionEnd();
        float size = (float) (getLayer().getFont().getSize() *
                Math.sqrt(
                        Math.pow(Math.abs(destPoints[0] - destPoints[2]), 2) + Math.pow(Math.abs(destPoints[1] - destPoints[3]), 2)
                ));
        editLayout.setDimention(displayRect, 360 - (int) getLayer().getRotationInDegrees());
        if (knife.getTextSize() != size) knife.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);

        float x = displayRect.center().x - (panel.getMeasuredWidth() / 2);
        float y = Utility.max(destPoints[1], destPoints[3], destPoints[5], destPoints[7], destPoints[13]) + (panel.getMeasuredHeight() * 3 / 2);
        //Log.d(TAG,"> NCN drawContent " + getLayer().getScale());
        if (panel.getX() != x || panel.getY() != y) {
            AdditiveAnimator
                    .animate(panel)
                    .setDuration(200)
                    .x(x)
                    .y(y)
                    .start();
        }

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, matrix, drawingPaint);
        }

        knife.setSelection(start, end);
    }

    @Override
    public int getStickerWidth() {
        return bitmap != null ? this.width : 0;
    }

    @Override
    public int getStickerHeight() {
        return bitmap != null ? this.height : 0;
    }

    @Override
    public int getDistance() {
        return bitmap != null ? this.distance : 0;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public Bitmap getBitmap() {
        if (finalBitmap == null) finalizeBitmap();
        return finalBitmap;
    }

    @Override
    public PointF getBitmapPosition() {
        return new PointF(displayRect.left(), displayRect.top());
    }

    @Override
    public void finalizeBitmap() {
        Bitmap temp = null;
        if (isUpdate() && bitmap != null) {
            temp = getConvertedBitmap(bitmap, layer.getRotationInDegrees(), layer.getScale());
        }
        if (temp != null && finalBitmap != null) {
            if (!finalBitmap.isRecycled()) {
                Log.wtf(TAG, "> BITMAP_RECYCLED " + finalBitmap);
                finalBitmap.recycle();
            }
            finalBitmap = null;
        }
        if (temp != null) {
            //Log.d(TAG,"> finalizeBitmap");
            finalBitmap = temp;
        }
    }

    @Override
    public void reloadBitmap() {

    }

    @Override
    public double getDistanceRatio() {
        return bitmap != null ? this.distanceRatio : 0;
    }

    @Override
    public void reInit() {
        init();
    }

    public void updateTextSticker() {
        updateTextSticker(true);
    }

    @Override
    public void release() {
        if (bitmap != null && !bitmap.isRecycled()) {
            Log.wtf(TAG, "> BITMAP_RECYCLED " + bitmap);
            bitmap.recycle();
        }
    }

    @Override
    public void destroy() {
        this.release();
        super.destroy();
    }

    @Override
    public void init() {
        //Log.d(TAG,"> init");
        if (uuid == null) uuid = this.getClass().getSimpleName() + "-" + System.currentTimeMillis();
        if (textPaint == null) {
            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setFilterBitmap(true);
            textPaint.setAntiAlias(true);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTypeface(fontProvider.getTypeface(getLayer().getFont().getTypeface()));
        }

        if (editLayout == null) {
            editLayout = new RotateLayout(parentView.getContext());
            editLayout.setBackgroundColor(Color.CYAN);
            parentView.addView(editLayout);
        }

        if (knife == null) {
            knife = new KnifeText(parentView.getContext());

            knife.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //Log.d(TAG,"> NCN beforeTextChanged ");
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    getLayer().setSpannableText(knife.getText());
                    //Log.d(TAG,"> NCN onTextChanged " + getLayer().getSpannableTextString());
                    updateTextSticker();
                    parentView.invalidate();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    //Log.d(TAG,"> NCN afterTextChanged ");
                }
            });
            knife.setSelectAllOnFocus(true);

            knife.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    //Log.d(TAG,"> NCN onEditorAction ");
                    if (actionId == EditorInfo.IME_ACTION_SEARCH
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        hideKeyboard(parentView);
                        return true;
                    }
                    return false;
                }
            });
            editLayout.addView(knife);

        }

        updateTextSticker(false);
        super.init();
    }

    @Override
    public void setDebug() {
        if (NCManager.isDebug) {
            editLayout.setBackgroundColor(0x30FF0000);
            knife.setBackgroundColor(0x300000FF);
        } else {
            editLayout.setBackgroundColor(Color.TRANSPARENT);
            knife.setBackgroundColor(Color.TRANSPARENT);
        }
        updateTextSticker();
    }

    @Override
    public JSONObject exportData() {
        JSONObject data = super.exportData();
        try {

            data.put(STICKER_KNIFE, knife == null ? new JSONObject() : knife.exportData());
            data.put(STICKER_LAYER, layer == null ? new JSONObject() : layer.exportData());
            data.put(STICKER_RECT, displayRect == null ? new JSONObject() : displayRect.exportData());

            update(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean updateData(JSONObject data) {
        update(data);
        if (isUpdate()) {
            //Log.d(TAG,"> updateData");
            boolean isImported = false;
            if (super.updateData(data)) {
                try {

                    setKnife(data.optJSONObject(STICKER_KNIFE));
                    setLayer(data.optJSONObject(STICKER_LAYER));
                    setDisplayRect(data.optJSONObject(STICKER_RECT));

                    isImported = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return isImported;
        }
        return false;
    }


    @Override
    public boolean importData(JSONObject data) {

        update(data);

        boolean isImported = false;
        if (super.importData(data)) {
            try {

                setKnife(data.optJSONObject(STICKER_KNIFE));
                setLayer(data.optJSONObject(STICKER_LAYER));
                setDisplayRect(data.optJSONObject(STICKER_RECT));

                isImported = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isImported;
    }

    @Override
    public String toString() {
        List<String> property = getProperties();
        property.add(STICKER_KNIFE + ":" + (knife == null ? new JSONObject() : knife.exportData()));
        property.add(STICKER_LAYER + ":" + (layer == null ? new JSONObject() : layer.exportData()));
        property.add(STICKER_RECT + ":" + (displayRect == null ? new JSONObject() : displayRect.exportData()));
        return "{"
                + "\n" + TextUtils.join(",\n", property)
                + "\n}"
                ;
    }
}
