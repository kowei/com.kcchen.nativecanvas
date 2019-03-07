package com.kcchen.nativecanvas.sticker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.kcchen.nativecanvas.model.NCImageFile;
import com.kcchen.nativecanvas.model.NCLayerData;
import com.kcchen.nativecanvas.view.NCRect;

import org.json.JSONObject;

import java.util.List;

public class ImageSticker extends Sticker {
    protected static final String TAG = ImageSticker.class.getSimpleName();

    public static final String STICKER_ASSET = "StickerAsset";
    public static final String STICKER_LAYER = "StickerLayer";
    public static final String STICKER_IMAGE = "StickerImage";
    public static final String STICKER_RECT = "StickerRect";


    @NonNull
    private NCImageFile imageFile;
    private Bitmap finalBitmap;
    private String asset;

    public ImageSticker(@NonNull NCLayerData layer,
                        @NonNull NCImageFile imageFile,
                        @IntRange(from = 1) int canvasWidth,
                        @IntRange(from = 1) int canvasHeight) {
        super(layer, canvasWidth, canvasHeight);

        this.imageFile = imageFile;
        //Log.d(TAG,"> ImageSticker");
        init();
    }

    public ImageSticker(NCLayerData layer, JSONObject json) {
        super(layer);
        importData(json);
    }


    public void setAsset(String asset) {
        this.asset = asset;
    }

    private void setLayer(JSONObject data) {
        layer.importData(data);
    }

    private void setImageFile(JSONObject data) {
        imageFile = new NCImageFile(data);
        if(activity != null)imageFile.setActivity(activity);
    }

    private void setDisplayRect(JSONObject data) {
        displayRect = new NCRect(data);
    }

    @NonNull
    public NCImageFile getImageFile() {
        return imageFile;
    }

    @Override
    public void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        // Log.e(TAG, "> NCN drawContent " + (bitmap == null ? "NULL" : bitmap.getByteCount()) + " matrix:" + matrix);
        if (imageFile.getBitmap() != null) {
            canvas.drawBitmap(imageFile.getBitmap(), matrix, drawingPaint);
        }
    }

    @Override
    public int getStickerWidth() {
        return this.width;
    }

    @Override
    public int getStickerHeight() {
        return this.height;
    }

    @Override
    public int getDistance() {
        return this.distance;
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public Bitmap getBitmap() {
        if(finalBitmap == null) finalizeBitmap();
        return finalBitmap;
    }

    @Override
    public PointF getBitmapPosition() {
        return new PointF(displayRect.left(),displayRect.top());
    }

    @Override
    public void finalizeBitmap() {
        Bitmap temp = null, bitmap = null;
        if(isUpdate() && imageFile != null) {
            bitmap = imageFile.getBitmap();
            if(bitmap != null) temp = getConvertedBitmap(bitmap, layer.getRotationInDegrees(), layer.getScale());
        }
        if(temp != null && finalBitmap != null){
            if(!finalBitmap.isRecycled()){
                Log.wtf(TAG,"> BITMAP_RECYCLED " + finalBitmap);
                finalBitmap.recycle();
            }
            finalBitmap = null;
        }
        if(temp != null) {
            //Log.d(TAG,"> finalizeBitmap");
            finalBitmap = temp;
        }
    }

    @Override
    public void reloadBitmap() {

    }

    @Override
    public double getDistanceRatio() {
        return this.distanceRatio;
    }

    @Override
    public void reInit() {
        //Log.d(TAG,"> reInit");
        init();
    }

    @Override
    public void setDebug() {

    }

    @Override
    public void release() {
        if (finalBitmap != null && !finalBitmap.isRecycled()) {
            Log.wtf(TAG,"> BITMAP_RECYCLED " + finalBitmap);
            finalBitmap.recycle();
        }
        finalBitmap = null;
        if(imageFile != null){
            imageFile.release();
        }
    }

    @Override
    public void destroy() {
        //Log.w(TAG,"> destroy ");
        imageFile.destroy();
        release();
        super.destroy();
    }

    @Override
    public void init() {
        if(uuid == null) uuid = this.getClass().getSimpleName() + "-" + System.currentTimeMillis();

        if (imageFile != null) {
            this.width = imageFile.getBitmap() == null ? 0 : imageFile.getBitmap().getWidth();
            this.height = imageFile.getBitmap() == null ? 0 : imageFile.getBitmap().getHeight();
        }

        float widthAspect = 1.0F * stickerDisplayWidth / this.getStickerWidth();
        float heightAspect = 1.0F * stickerDisplayHeight / this.getStickerHeight();
        // fit the smallest size
        scalefitRatio = Math.min(widthAspect, heightAspect);
        this.distance = (int) (Math.sqrt(Math.pow(getStickerWidth(), 2) + Math.pow(getStickerHeight(), 2)) / 2);
        if(stickerView != null) this.distanceRatio = this.distance * this.scalefitRatio * stickerView.getScaleX();
        //Log.w(TAG, "> init " + uuid
        //        + "\n distanceRatio:" + distanceRatio
        //        + "\n display:" + stickerDisplayWidth + "x" + stickerDisplayHeight
        //        + "\n scalefitRatio:" + scalefitRatio
        //);

        // initial position of the entity
        srcPoints[0]  = 0;                                   // origin.x + 0;
        srcPoints[1]  = 0;                                   // origin.y + 0;
        srcPoints[2]  = this.getStickerWidth();              // origin.x + this.getStickerWidth();
        srcPoints[3]  = 0;                                   // origin.y + 0;
        srcPoints[4]  = this.getStickerWidth();              // origin.x + this.getStickerWidth();
        srcPoints[5]  = this.getStickerHeight();             // origin.y + this.getStickerHeight();
        srcPoints[6]  = 0;                                   // origin.x + 0;
        srcPoints[7]  = this.getStickerHeight();             // origin.y + this.getStickerHeight();
        srcPoints[8]  = 0;                                   // origin.x + 0;
        srcPoints[9]  = 0;                                   // origin.y + 0;
        srcPoints[10] = (this.getStickerWidth() / 2);        // origin.x + (this.getStickerWidth() / 2);
        srcPoints[11] = 0;                                   // origin.y + 0;
        srcPoints[12] = (this.getStickerWidth() / 2);        // origin.x + (this.getStickerWidth() / 2);
        srcPoints[13] = - this.rotateLength;                   // origin.y + this.rotateLength;
        super.init();

        exportData();
    }


    @Override
    public JSONObject exportData() {
        JSONObject data = super.exportData();
        try {

            data.put(STICKER_ASSET, asset);
            data.put(STICKER_LAYER, layer == null ? new JSONObject() : layer.exportData());
            data.put(STICKER_IMAGE, imageFile == null ? new JSONObject() : imageFile.exportData());
            data.put(STICKER_RECT, displayRect == null ? new JSONObject() : displayRect.exportData());

            update(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public boolean updateData(JSONObject data) {
        update(data);

        if(isUpdate()){
            Log.d(TAG,"> updateData");
            boolean isImported = false;
            if(super.updateData(data)){
                try {

                    setAsset(data.optString(STICKER_ASSET));
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
        //Log.d(TAG,"> importData");
        update(data);

        boolean isImported = false;
        if(super.importData(data)){
            try {

                setAsset(data.optString(STICKER_ASSET));
                setLayer(data.optJSONObject(STICKER_LAYER));
                setImageFile(data.optJSONObject(STICKER_IMAGE));
                setDisplayRect(data.optJSONObject(STICKER_RECT));

                init();
                isImported = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isImported;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash += asset == null? -1:asset.hashCode();
        hash += layer == null? -1:layer.hashCode();
        hash += imageFile == null? -1:imageFile.hashCode();
        hash += displayRect == null? -1:displayRect.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        List<String> property = getProperties();
        property.add(STICKER_ASSET + ":" + asset);
        property.add(STICKER_LAYER + ":" + (layer == null ? new JSONObject() : layer.exportData()));
        property.add(STICKER_IMAGE + ":" + (imageFile == null ? new JSONObject() : imageFile.exportData()));
        property.add(STICKER_RECT  + ":" + (displayRect == null ? new JSONObject() : displayRect.exportData()));
        return "{"
                + "\n" + TextUtils.join(",\n", property)
                + "\n}"
                ;
    }
}
