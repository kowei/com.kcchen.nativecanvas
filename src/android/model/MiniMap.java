package com.kcchen.nativecanvas.model;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;

import com.kcchen.nativecanvas.enums.CALLBACK_TYPE;
import com.kcchen.nativecanvas.utils.LogBuilder;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by kowei on 2017/11/28.
 */

public class MiniMap extends JSONObject {

    private final static String TAG = MiniMap.class.getSimpleName();

    private HandlerThread thread = null;
    private Handler handler = null;
    private CALLBACK_TYPE type = CALLBACK_TYPE.MINI_MAP;
    private Bitmap originImage;
    private Bitmap image;
    private Rect originalRect;
    private Rect displayRect;
    private String centerX;
    private String centerY;
    private String scale;
    private OnReadyListener listener;
    private String imageBase64;
    private MiniMap minimap;
    private int minimapHash;
    private Runnable processImage = new Runnable() {
        @Override
        public void run() {
            try {
                image = Bitmap.createScaledBitmap(originImage, originImage.getWidth() / 4, originImage.getHeight() / 4, true);

                if (image != null) {
                    Log.e(TAG, "> NCN image change size "
                            + originImage.getWidth() + "x" + originImage.getHeight()
                            + " -> "
                            + image.getWidth() + "x" + image.getHeight()
                    );
                    getBase64();
                    minimap.put("data", imageBase64);
                    minimap.put("dataType", "png");
                    minimap.put("dataCompress", "base64");
                } else {
                    minimap.put("data", "");
                    minimap.put("dataType", "");
                    minimap.put("dataCompress", "");
                }
                int hash = Utility.getJsonHash(minimap);
                Log.w(TAG, "> NCN processImage " + hash + " / " + minimapHash + '\n' + minimap.toString());
                if (minimapHash != hash) {
                    minimapHash = hash;
                    if (listener != null) {
                        Log.w(TAG, "> NCN fire minimap");
                        listener.onReady();
                    }
                } else {
                    Log.w(TAG, "> NCN not fireing same minimap");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public MiniMap() {
        try {
            thread = new HandlerThread(TAG);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
            this.handler = new Handler(thread.getLooper());
            this.put("type", this.type.key());
            this.put("data", "");
            this.put("dataType", "");
            this.put("dataCompress", "");
            this.put("originalRect", "");
            this.put("displayRect", "");
            minimap = this;
            Log.w(TAG, "> NCN minimap init");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clean() {
        try {
            while (this.keys().hasNext()) {
                this.put(this.keys().next(), null);
            }
            if (image != null && !image.isRecycled()) {
                Log.wtf(TAG,"> BITMAP_RECYCLED " + image);
                image.recycle();
            }
            image = null;
            if (originImage != null && !originImage.isRecycled()) {
                Log.wtf(TAG,"> BITMAP_RECYCLED " + originImage);
                originImage.recycle();
            }
            originImage = null;
            listener = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        Log.w(TAG, "> NCN destroy minimap");
        clean();
        handler.removeCallbacks(processImage);
        handler = null;
        thread.interrupt();
        thread = null;
        minimap = null;
        listener = null;
        imageBase64 = null;
        if (image != null && !image.isRecycled()) {
            Log.wtf(TAG,"> BITMAP_RECYCLED " + image);
            image.recycle();
        }
        image = null;
        if (originImage != null && !originImage.isRecycled()) {
            Log.wtf(TAG,"> BITMAP_RECYCLED " + originImage);
            originImage.recycle();
        }
        originImage = null;
        listener = null;
        originalRect = null;
        displayRect = null;
    }

    public CALLBACK_TYPE getType() {
        return this.type;
    }

//    public void setType(NativeCanvas.CALLBACK_TYPE type) {
//        this.type = type;
//        try {
//            this.put("type", this.type.key());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    public Bitmap getImage() {
        return this.image;
    }

    public void setImage(Bitmap image) {
        if(image == null) return;
        //Log.w(TAG, "> NCN setImage "
        //        + (this.originImage != null && image != null ? this.originImage.sameAs(image) + " " : " one is null ")
        //        + (this.originImage != null ? this.originImage.getByteCount() : " null ")
        //        + " / "
        //        + (image != null ? image.getByteCount() : " null ")
        //);
        if (this.originImage != null && this.originImage.sameAs(image)) {
            //Log.w(TAG, "> NCN return cached image");
            try {
                put("data", imageBase64);
                put("dataType", "png");
                put("dataCompress", "base64");
                int thishash = Utility.getJsonHash(this);
                //Log.w(TAG, "> NCN setImage minimapHash " + thishash + " / " + minimapHash);
                if (minimapHash != thishash) {
                    minimapHash = thishash;
                    if (listener != null) {
                        Log.w(TAG, "> NCN fire minimap");
                        listener.onReady();
                    }
                } else {
                    Log.w(TAG, "> NCN not fireing same minimap");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            this.originImage = image.copy(image.getConfig(), true);
            //Log.w(TAG, "> NCN update cached image");
            handler.post(this.processImage);
        }
    }

    private void getBase64() {
        try {
            ByteArrayOutputStream data = new ByteArrayOutputStream();

            if (this.image != null && !this.image.isRecycled() && this.image.compress(Bitmap.CompressFormat.PNG, 100, data)) {
                byte[] dataBytes = data.toByteArray();
                byte[] output = Base64.encode(dataBytes, Base64.NO_WRAP);
                this.imageBase64 = new String(output);
                if (this.image != null) {
                    Log.wtf(TAG,"> BITMAP_RECYCLED " + image);
                    this.image.recycle();
                    this.image = null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Rect getOriginalRect() {
        return originalRect;
    }

    public void setOriginalRect(Rect originalRect) {
        this.originalRect = originalRect;
        try {
            if (originalRect != null) {
                this.put("originalRect", originalRect.flattenToString());
            } else {
                this.put("originalRect", "");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Rect getDisplayRect() {
        return displayRect;
    }

    public void setDisplayRect(Rect displayRect) {
        this.displayRect = displayRect;
        try {
            if (displayRect != null) {
                this.put("displayRect", displayRect.flattenToString());
            } else {
                this.put("displayRect", "");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCenterX() {
        return centerX;
    }

    public void setCenterX(String centerX) {
        this.centerX = centerX;
        try {
            if (centerX != null) {
                this.put("offsetX", centerX);
            } else {
                this.put("offsetX", "");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCenterY() {
        return centerY;
    }

    public void setCenterY(String centerY) {
        this.centerY = centerY;
        try {
            if (centerY != null) {
                this.put("offsetY", centerY);
            } else {
                this.put("offsetY", "");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
        try {
            if (scale != null) {
                this.put("scale", scale);
            } else {
                this.put("scale", "");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public JSONObject clone() {
        try {
            return new JSONObject(this.toString());
        } catch (JSONException e1) {
            e1.printStackTrace();
            try {
                return this.clone();
            } catch (Exception e2) {
                e2.printStackTrace();
                return this;
            }
        }
    }

    public void print() {
        LogBuilder.with(TAG, Log.DEBUG)
                .d("MiniMap", this);
    }

    public void setOnReadyListener(OnReadyListener listener) {
        this.listener = listener;
    }

    public interface OnReadyListener {
        void onReady();
        void onBitmapChange();
        void onPropertyChange();
        void onAllChange();
    }
}
