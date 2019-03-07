package com.kcchen.nativecanvas.paper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.kcchen.nativecanvas.enums.PEN_TYPE;
import com.kcchen.nativecanvas.model.NCLayerData;
import com.kcchen.nativecanvas.model.NCLayerDataPaper;
import com.kcchen.nativecanvas.model.PenpalBook;
import com.kcchen.nativecanvas.model.PenpalLibrary;
import com.kcchen.nativecanvas.model.PenpalPage;
import com.kcchen.nativecanvas.sticker.Sticker;
import com.kcchen.nativecanvas.undo.UndoManager;
import com.kcchen.nativecanvas.utils.MessageBox;
import com.kcchen.nativecanvas.utils.Utility;
import com.kcchen.nativecanvas.view.NCRect;

import org.json.JSONObject;

public class DrawingPaper extends Paper implements MessageBox.Receiver {

    private static final String DRAWING_DATA = "DrawingData";

    protected String TAG = DrawingPaper.class.getSimpleName();
    private DrawingSurface surface;
    private ImageView cachedSurface;
    private MessageBox messageBox;
    private PEN_TYPE penType;
    private NCLayerData.OnLayerListener layerListener = new NCLayerData.OnLayerListener() {
        @Override
        public void onBitmapUpdate() {
            send(messageBox, PAPER_MESSAGE.UPDATE_BITMAP, null);
        }

        @Override
        public void onRefreshView() {
            send(messageBox, PAPER_MESSAGE.UPDATE_VIEW, null);
        }

        @Override
        public void onPageReady(int pageIndex) {

        }
    };

    private ImageView background;
    private Bitmap backgroundBitmap;
    private PenpalBook penpalBook;
    private PenpalPage penpalPage;
    private UndoManager.OnUndoListener undoListener = new UndoManager.OnUndoListener() {
        @Override
        public void onChanged() {

        }

        @Override
        public void onImport(UndoManager undoManager) {
            //Log.d(TAG, "> onImport");
        }
    };
    private Bitmap tempBitmap;

    public DrawingPaper(@NonNull Context context,
                        @NonNull NCLayerDataPaper layer) {
        super(context, layer);
        this.init();
    }

    public DrawingPaper(Context context, NCLayerDataPaper layer, int i) {
        super(context, layer, i);
        TAG += " " + i;
        setIndex(i);

        init();
    }


    public void readProfile() {

        penpalBook = PenpalLibrary.getCurrentBook();
        //Log.i(TAG, "> readProfile " + penpalBook);

        if (penpalBook != null && penpalBook.hasPage(getIndex())) {
            try {
                penpalPage = penpalBook.getSelectedPage();
            } catch (Exception e) {
                e.printStackTrace();
            }

            reload();
        }
    }

    private void initReceiver() {
        messageBox = new MessageBox(new Handler());
        messageBox.setReceiver(this);
    }

    private void send(ResultReceiver targetBox, PAPER_MESSAGE type, Bundle bundle) {
        try {
            if (bundle == null)
                ((MessageBox) targetBox).putMessage(type.ordinal(), new Bundle());
            else
                ((MessageBox) targetBox).putMessage(type.ordinal(), bundle);
        } catch (Exception e) {
            Log.e(TAG, "send to " + e.toString());
        }
    }

    public void setCachedSurfaceBitmap(Bitmap bitmap) {
        //Log.d(TAG, "> setCachedSurfaceBitmap");
        if (bitmap != null) {
            cachedSurface.setImageBitmap(bitmap);
        } else {
            cachedSurface.setImageBitmap(tempBitmap);
        }
    }

    public void setBackgroundBitmap(Bitmap bitmap) {
        //Log.d(TAG, "> setBackgroundBitmap");
        if (bitmap != null) {
            background.setImageBitmap(bitmap);
        }
    }

    public void setPenType(PEN_TYPE type) {
        this.penType = type;
        if (surface != null) surface.setStylus(type);
    }

    public void setPaintColor(int color) {
        if (surface != null) surface.setPaintColor(color);
    }

    public void setPaintWidth(float width) {
        if (surface != null) surface.setPaintWidth(width);
    }

    public void setPaintAlpha(int alpha) {
        if (surface != null) surface.setPaintAlpha(alpha);
    }

    public void redrawPaperBitmap() {
        //Log.d(TAG, "> redrawPaperBitmap " + surface.hasPaperBitmap() + "  " + surface);
        if (surface != null && surface.hasPaperBitmap()) {
            surface.redrawPaperBitmap();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            onResume();
        } else {
            onPause();
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void reload() {
        Log.i(TAG, "> NCN reload " + penpalPage.isValid());
        if (penpalPage != null && penpalPage.isValid()) {
            Bitmap bitmap = penpalPage.getBackground();
            if(bitmap != null && !bitmap.isRecycled()){
                setBackgroundBitmap(bitmap.copy(bitmap.getConfig(), true));
            }else{
                setBackgroundBitmap(tempBitmap);
            }

            if (getUndoManager() != null && getUndoManager().isEmpty()) {
                JSONObject data = penpalPage.getPageProfile().optJSONObject(DRAWING_DATA);
                if (data != null)
                    getUndoManager().importData(penpalPage.getPageProfile().optJSONObject(DRAWING_DATA));
            }
        }
    }

    @Override
    public void save() {
        if (penpalPage != null && penpalPage.isValid()) {
            try {
                if (getUndoManager().isUpdate()) {
                    //Log.d(TAG, "> NCN save profile");
                    penpalPage.getPageProfile().put(DRAWING_DATA, getUndoManager().exportData());
                    if (penpalPage.save(getPaperBitmap())) {
                        paperListener.onThumbnailUpdate();
                    }
                } else if (!penpalPage.hasThumbnail()) {
                    Log.d(TAG, "> NCN save thumbnail");
                    if (penpalPage.save(getPaperBitmap())) {
                        paperListener.onThumbnailUpdate();
                    }
                } else {
                    Log.wtf(TAG, "> NCN save profile ESCAPE");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "> NCN save profile FAILED");
        }
    }

    @Override
    public void addSticker(Sticker sticker) {
        layer.addSticker(sticker);
        if (surface != null) surface.updateUI();
    }

    @Override
    public void updateSticker(Sticker sticker) {
        layer.updateSticker(sticker);
    }

    @Override
    public int getDistance() {
        return this.distance;
    }

    @Override
    public double getDistanceRatio() {
        return this.distanceRatio;
    }

    @Override
    public void release() {
        super.release();
        //Log.wtf(TAG, "> NCN release Paper " + this.hashCode());
        if (penpalPage != null) {
            penpalPage.clearBitmap();
        }
        if (surface != null) {
            surface.destroy();
            surface = null;
        }
        if (background != null) {
            background.setImageBitmap(null);
            background = null;
        }
        if (tempBitmap != null) {
            Log.wtf(TAG, "> BITMAP_RECYCLED " + tempBitmap);
            tempBitmap.recycle();
            tempBitmap = null;
        }
    }

    @Override
    public void setIndex(int index) {
        super.setIndex(index);
    }

    @Override
    public Paper setPaperDimention(NCRect bookDimention, NCRect paperDimention) {
        super.setPaperDimention(bookDimention, paperDimention);
        // Glide test
        // Glide.with(context).load("http://goo.gl/gEgYUd").into(background);
        // HTML2Bitmap test
        //new AsyncTask<Void, Void, Bitmap>() {
        //    @Override
        //    protected Bitmap doInBackground(Void... voids) {
        //        String html = "<html><body><p>Hello world!</p><br/>Html bitmap</body><html>";
        //        // return Html2Bitmap.getBitmap(context, html, (int) paperDimention.width(), (int) paperDimention.height());
        //        return Html2Bitmap.getBitmap(context, "http://tw.kcchen.com/", (int) paperDimention.width(), (int) paperDimention.height());
        //    }
        //
        //    @Override
        //    protected void onPostExecute(Bitmap bitmap) {
        //        if (bitmap != null) {
        //            background.setImageBitmap(bitmap);
        //        }
        //    }
        //}.execute();
        return this;
    }

    @Override
    public void init() {
        //Log.wtf(TAG, "> NEW paper!!! " + this.hashCode());
        layer.setPageIndex(index);
        layer.setOnLayerListener(layerListener);

        tempBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        initReceiver();
        if (surface == null) {
            background = new ImageView(context);
            this.addView(background);
            Utility.setRelativeLayout(background, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, 0, 0);
            cachedSurface = new ImageView(context);
            this.addView(cachedSurface);
            Utility.setRelativeLayout(cachedSurface, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, 0, 0);
            surface = new DrawingSurface(context, this);
            this.addView(surface);
            Utility.setRelativeLayout(surface, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0, 0, 0);
        }
        // setBackgroundColor(Color.GREEN);
        if (layer != null && layer.getBackgroundColor() != null) {
            setBackgroundColor(layer.getBackgroundColor());
        } else {
            setBackgroundColor(Color.WHITE);
        }
        setCachedSurfaceBitmap(null);
        float widthAspect = 1.0F * this.getPaperDisplayWidth() / getPaperWidth();
        float heightAspect = 1.0F * this.getPaperDisplayHeight() / getPaperHeight();
        // fit the smallest size
        scalefitRatio = Math.min(widthAspect, heightAspect);
        layer.getLimit().setMinScale(scalefitRatio);
        this.distance = (int) (Math.sqrt(Math.pow(getPaperWidth(), 2) + Math.pow(getPaperHeight(), 2)) / 2);
        this.distanceRatio = (int) (this.distance * this.scalefitRatio);

        //Log.e(TAG, "> NCN init Paper: " + this
        //         + " " + getPaperWidth() + "x" + getPaperHeight()
        //         + " scalefitRatio:" + scalefitRatio
        // );

        // rectangle of paper
        srcPoints[0] = 0;                               // left.x
        srcPoints[1] = 0;                               // left.y

        srcPoints[2] = getPaperWidth();                 // right.x
        srcPoints[3] = 0;                               // right.y
        srcPoints[4] = getPaperWidth();
        srcPoints[5] = getPaperHeight();
        srcPoints[6] = 0;
        srcPoints[7] = getPaperHeight();

        srcPoints[8] = 0;
        srcPoints[9] = 0;
        // grid Horizontal
        srcPoints[10] = 0;
        srcPoints[11] = getPaperHeight() * 1 / 5;
        srcPoints[12] = getPaperWidth();
        srcPoints[13] = getPaperHeight() * 1 / 5;
        srcPoints[14] = 0;
        srcPoints[15] = getPaperHeight() * 2 / 5;
        srcPoints[16] = getPaperWidth();
        srcPoints[17] = getPaperHeight() * 2 / 5;
        srcPoints[18] = 0;
        srcPoints[19] = getPaperHeight() * 3 / 5;
        srcPoints[20] = getPaperWidth();
        srcPoints[21] = getPaperHeight() * 3 / 5;
        srcPoints[22] = 0;
        srcPoints[23] = getPaperHeight() * 4 / 5;
        srcPoints[24] = getPaperWidth();
        srcPoints[25] = getPaperHeight() * 4 / 5;
        // grid Vertical
        srcPoints[26] = getPaperWidth() * 1 / 5;
        srcPoints[27] = 0;
        srcPoints[28] = getPaperWidth() * 1 / 5;
        srcPoints[29] = getPaperHeight();
        srcPoints[30] = getPaperWidth() * 2 / 5;
        srcPoints[31] = 0;
        srcPoints[32] = getPaperWidth() * 2 / 5;
        srcPoints[33] = getPaperHeight();
        srcPoints[34] = getPaperWidth() * 3 / 5;
        srcPoints[35] = 0;
        srcPoints[36] = getPaperWidth() * 3 / 5;
        srcPoints[37] = getPaperHeight();
        srcPoints[38] = getPaperWidth() * 4 / 5;
        srcPoints[39] = 0;
        srcPoints[40] = getPaperWidth() * 4 / 5;
        srcPoints[41] = getPaperHeight();

        this.getUndoManager().setOnUndoListener(undoListener);
        if(paperListener != null){
            paperListener.onReady(this);
        }
        super.init();
    }

    @Override
    public void reInit() {
        this.init();
    }


    @Override
    protected void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {

    }

    @Override
    public void destroy() {
        this.release();
        super.destroy();
    }

    public float[] getSourcePoints() {
        return srcPoints;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        PAPER_MESSAGE type = PAPER_MESSAGE.values()[resultCode];
        //Log.e(TAG, "> NCN onReceiveResult type:" + type.key());

        switch (type) {
            case UPDATE_BITMAP:
                if (surface != null) surface.updatePaperBitmap();
                break;
            case UPDATE_VIEW:
                if (surface != null) surface.updateUI();
                break;
        }
    }

    public DrawingSurface getSurface() {
        return surface;
    }

    public void clear() {
        layer.addClear();
        if (surface != null) surface.updateUI();
    }

    public Bitmap getPaperBitmap() {
        //Log.w(TAG, "> getPaperBitmap " + this.hashCode());
        Bitmap bitmap = null;
        Drawable drawable = background.getDrawable();
        if (backgroundBitmap != null) {
            if (!backgroundBitmap.isRecycled()) {
                Log.wtf(TAG, "> BITMAP_RECYCLED " + backgroundBitmap);
                backgroundBitmap.recycle();
            }
            backgroundBitmap = null;
        }
        if (drawable != null && backgroundBitmap == null) {
            background.setDrawingCacheEnabled(true);
            backgroundBitmap = Bitmap.createBitmap(background.getDrawingCache());
            background.setDrawingCacheEnabled(false);
        }
        if (surface != null) bitmap = surface.getPaperBitmap(backgroundBitmap);
        return bitmap;
    }

}
