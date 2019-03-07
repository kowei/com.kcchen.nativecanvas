package com.kcchen.nativecanvas.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.kcchen.nativecanvas.drawtools.model.NCPaint;
import com.kcchen.nativecanvas.drawtools.model.NCPath;
import com.kcchen.nativecanvas.enums.UNDO_TYPE;
import com.kcchen.nativecanvas.sticker.ImageSticker;
import com.kcchen.nativecanvas.sticker.Sticker;
import com.kcchen.nativecanvas.sticker.TextSticker;
import com.kcchen.nativecanvas.undo.UndoItem;
import com.kcchen.nativecanvas.undo.UndoItemDrawing;
import com.kcchen.nativecanvas.undo.UndoManager;
import com.kcchen.nativecanvas.view.NCStickerView;

import org.json.JSONObject;

import static com.kcchen.nativecanvas.enums.UNDO_TYPE.DRAWING;
import static org.chromium.content.browser.OrientationSensorType.NOT_AVAILABLE;

public class NCLayerDataPaper extends NCLayerData {
    protected static final String TAG = NCLayerDataPaper.class.getSimpleName();

    private int CACHE_DRAWING = 10;
    private Integer backgroundColor;
    private UndoManager undoManager = new UndoManager(0);
    private int flattenStep;
    private boolean isDraw = false;
    private int lastHash;
    private int lastEraser;

    private final UndoManager.OnUndoListener undoListener = new UndoManager.OnUndoListener() {
        @Override
        public void onChanged() {
            if (listeners != null) {
                for (OnLayerListener listener :listeners) {
                    listener.onRefreshView();
                }
            }
        }

        @Override
        public void onImport(UndoManager undoManager) {

        }
    };
    private NCStickerView stickerView;

    public NCLayerDataPaper(SCALE_LIMIT limit) {
        super(limit);
        thread = new HandlerThread(TAG);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        this.handler = new Handler(thread.getLooper());
        undoManager.setOnUndoListener(undoListener);
    }

    public NCLayerDataPaper setBackgroundColor(Integer backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public Integer getBackgroundColor() {
        return backgroundColor;
    }

    // 用 thread 跑，會更新 bitmap 時閃爍
    // handler.post(new Runnable() {
    //     @Override
    //     public void run() {
    //         flattenImage(canvas, bitmap);
    //         if (listener != null) listener.onBitmapUpdate();
    //     }
    // });
    public int getCacheDrawingCount(final Canvas canvas, Bitmap bitmap, boolean isEraser) {

        // 得到從重設點的步驟數
        int steps = undoManager.undoCountFromReset();
        // 假如是橡皮擦，放棄緩衝
        int fromStep = isEraser ? 0 : steps;
        flattenStep = (int) (Math.floor(fromStep / CACHE_DRAWING) * CACHE_DRAWING);
        int toStep = isEraser ? 0 : fromStep - flattenStep;

        // 檢查是否有橡皮擦在緩衝區，如果有，平面化擴展至最後一個橡皮擦
        int bufferStep = steps - (fromStep - toStep);
        if(bufferStep != 0 && getLastEraser(bufferStep) && lastEraser < toStep){
            toStep = lastEraser;
            bufferStep = steps - (fromStep - toStep);
        }

        // Log.i(TAG, "> NCN getCacheDrawingCount"
        //         + " Steps: " + flattenStep + "/"+(steps - (fromStep - toStep))+ "/" + steps
        //         + " from-to: " + fromStep +"->"+ toStep
        //         + " resets: " + undoManager.getResets()
        // );

        // 檢查是否需要平面化
        if(updateHash(fromStep,toStep)){
            Log.d(TAG,"> getCacheDrawingCount flattenImage " + fromStep + " - " + toStep);
            flattenImage(canvas, bitmap, fromStep, toStep);
        }

        // 返回尚未平面化步驟（緩衝區）
        return bufferStep;
    }

    public boolean updateHash(int from, int to) {
        // Log.e(TAG, "> NCN updateHash from: " + from + " to: " + to + " Count: " + undoManager.undoCountFromReset() + " / " + undoManager.undoCount());
        int hash = (from != to) ? getFlattenHash(from, to) : NOT_AVAILABLE;
        // Log.e(TAG, "> updateHash Hash:" + (lastHash != hash) + "  " + hash + " / " + lastHash);
        if (lastHash == hash) return false;
        lastHash = hash;
        return true;
    }

    public int getFlattenHash(int fromStep, int toStep) {
        int hash = 0;
        for (int i = fromStep -1 ; i >= toStep; i--) {
            if(undoManager.get(i) != null){
                hash += undoManager.get(i).hashCode();
            }
        }
        return hash;
    }

    public boolean getLastEraser(int bufferStep) {
        lastEraser = -1;
        for (int i = bufferStep - 1; i >= 0; i--) {
            if(undoManager.get(i) != null && undoManager.get(i).getType() == UNDO_TYPE.ERASER) lastEraser = i;
        }
        return !(lastEraser == -1);
    }

    public boolean hasDraw() {
        return isDraw;
    }

    public void flattenImage(Canvas canvas, Bitmap bitmap, int fromStep, int toStep) {

        synchronized (bitmap){
            StringBuilder types = new StringBuilder();
            StringBuilder skips = new StringBuilder();

            bitmap.eraseColor(Color.TRANSPARENT);
            canvas.setBitmap(bitmap);

            for (int i = fromStep - 1; i >= toStep; i--) {
                UndoItem item = undoManager.get(i);

                types.append(item == null ? "NULL" : item.isTransform() ? "" : item.getType().key());
                if(i != 0) types.append(",");
                if(item != null && item.isTransform()) {
                    skips.append(item.getType());
                    if(i != 0) skips.append(",");
                }

                if(item != null && !item.isTransform()){
                    NCStickerView imageStickerView = getStickerView();
                    switch (item.getType()) {
                        case STICKER_IMAGE:
                            JSONObject imageStickerData = item.getData();
                            if (imageStickerView != null && imageStickerData != null) {
                                ImageSticker imageSticker = (ImageSticker) imageStickerView.getStickerByUuid(imageStickerData.optString(Sticker.STICKER_UUID));
                                if (imageSticker != null && !imageSticker.isSelected()) {
                                    imageSticker.updateData(imageStickerData);
                                    imageSticker.finalizeBitmap();
                                    canvas.drawBitmap(imageSticker.getBitmap(), imageSticker.getBitmapPosition().x, imageSticker.getBitmapPosition().y, null);
                                }
                            }
                            break;
                        case STICKER_TEXT:
                            JSONObject textStickerData = item.getData();
                            if (imageStickerView != null && textStickerData != null) {
                                TextSticker textSticker = (TextSticker) imageStickerView.getStickerByUuid(textStickerData.optString(Sticker.STICKER_UUID));
                                if (textSticker != null && !textSticker.isSelected()) {
                                    textSticker.updateData(textStickerData);
                                    textSticker.finalizeBitmap();
                                    canvas.drawBitmap(textSticker.getBitmap(), textSticker.getBitmapPosition().x, textSticker.getBitmapPosition().y, null);
                                }
                            }
                            break;
                        case DRAWING:
                            if (item instanceof UndoItemDrawing) {
                                UndoItemDrawing drawing = (UndoItemDrawing) item;
                                isDraw |= true;
                                canvas.drawPath(drawing.getPath(), drawing.getPaint());
                            }
                            break;
                        case ERASER:
                            if (item instanceof UndoItemDrawing) {
                                UndoItemDrawing drawing = (UndoItemDrawing) item;
                                isDraw |= true;
                                canvas.drawPath(drawing.getPath(), drawing.getPaint());
                            }
                            break;
                        case CLEAR:
                            bitmap.eraseColor(Color.TRANSPARENT);
                            canvas.setBitmap(bitmap);
                            break;
                        case TEMPLATE:
                            break;
                    }
                }
            }
            Log.w(TAG, "> flattenImage from " + (toStep) + " to " + (fromStep - 1)
                    + "\n draw:" + types.toString()
                    + "\n drop:" + skips.toString()
            );
            // canvas.setBitmap(null);
        }
    }

    public UndoItem getStep(int index) {
        return undoManager.get(index);
    }

    public int getFlattenStep() {
        return flattenStep;
    }

    public int getCacheDrawing() {
        return CACHE_DRAWING;
    }

    public void setCacheDrawing(int cacheDrawing) {
        this.CACHE_DRAWING = cacheDrawing;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public UndoItem undo(){
        return undoManager.undo();
    }

    public UndoItem redo(){
        return undoManager.redo();
    }

    public void updateSticker(Sticker sticker) {
        if(stickerView.isShown() && stickerView.getStickerByUuid(sticker.getUuid()) != null){
            Log.i(TAG,"> updateSticker");
            if(sticker.exportData() != null && sticker.isUpdate()){
                addSticker(sticker);
            }
        }
    }

    public void addSticker(Sticker sticker) {
        UndoItem item = null;
        if(sticker instanceof ImageSticker){
            ImageSticker imageSticker = (ImageSticker) sticker;
            NCImageFile imageFile = imageSticker.getImageFile();
            if(imageFile.isValid()) {
                String asset = imageFile.processAsset(PenpalLibrary.getCurrentBook().getAssetManager());
                imageSticker.setAsset(asset);
            }
            item = new UndoItem(UNDO_TYPE.STICKER_IMAGE, ImageSticker.class.getSimpleName(), imageSticker.exportData());
        }else if(sticker instanceof TextSticker){
            item = new UndoItem(UNDO_TYPE.STICKER_TEXT, TextSticker.class.getSimpleName(), sticker.exportData());
        }
        if(item != null) {
            try {
                Log.w(TAG, "> NCN undoManager item " + item);
                undoManager.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addDrawing(NCPath path, NCPaint paint) {
        if (path != null && paint != null) {
            UndoItemDrawing item;
            item = new UndoItemDrawing(DRAWING);


            item.setDrawing(path,paint);
            if(item != null) {
                try {
                    undoManager.add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void addEraser(NCPath path, NCPaint paint) {
        if (path != null && paint != null) {
            UndoItemDrawing item;
            item = new UndoItemDrawing(UNDO_TYPE.ERASER);
            item.setDrawing(path,paint);
            if(item != null) {
                try {
                    undoManager.add(item);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void addClear() {
        UndoItem item;
        item = new UndoItem(UNDO_TYPE.CLEAR, null, null);
        if (item != null) {
            undoManager.add(item);
        }
    }

    @Override
    public NCLayerData reset() {
        super.reset();
        return null;
    }

    @Override
    public boolean importData(JSONObject data) {
        boolean isImported = false;
        isImported |= super.importData(data);
        try {
            isImported |= true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    public void release() {
        if(undoManager != null) {
            undoManager.setOnUndoListener(null);
            undoManager.release();
            undoManager = null;
        }
    }

    @Override
    public JSONObject exportData() {
        JSONObject data = super.exportData();
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public void setStickerView(NCStickerView stickerView) {
        this.stickerView = stickerView;
    }

    public NCStickerView getStickerView() {
        return stickerView;
    }
}
