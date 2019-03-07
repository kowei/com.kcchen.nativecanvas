package com.kcchen.nativecanvas.paper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kcchen.nativecanvas.NativeCanvas;
import com.kcchen.nativecanvas.drawtools.EraserTool;
import com.kcchen.nativecanvas.drawtools.PenTool;
import com.kcchen.nativecanvas.drawtools.Tool;
import com.kcchen.nativecanvas.drawtools.ToolReport;
import com.kcchen.nativecanvas.drawtools.attributes.EraserToolAttributes;
import com.kcchen.nativecanvas.enums.PEN_TYPE;
import com.kcchen.nativecanvas.sticker.ImageSticker;
import com.kcchen.nativecanvas.sticker.Sticker;
import com.kcchen.nativecanvas.sticker.TextSticker;
import com.kcchen.nativecanvas.undo.UndoItem;
import com.kcchen.nativecanvas.undo.UndoItemDrawing;
import com.kcchen.nativecanvas.view.NCManager;
import com.kcchen.nativecanvas.view.NCStickerView;

import org.json.JSONObject;

public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {

    protected String TAG = DrawingSurface.class.getSimpleName();

    private ToolReport toolReport;
    private Tool tool;
    private Tool lastTool;
    private SurfaceHolder holder;
    private static final int NULL_POINTER_ID = -1;
    private RectF circleRect = new RectF();
    private Rect surfaceRect = new Rect();
    private PenTool penTool;
    private Paint gridPaint = new Paint();
    private Paint borderPaint = new Paint();
    private Paint bitmapPaint = new Paint();
    private int currentPointerId = NULL_POINTER_ID;
    private int cacheSteps;
    private float dp;
    private EraserTool eraserTool;
    private DrawingPaper paper;
    private DIRTY_LEVEL dirtyLevel = DIRTY_LEVEL.BG;
    private Context context;
    private Canvas reusableCanvas = new Canvas();
    private boolean surfaceCreated;
    private Bitmap paperBitmap;

    public enum DIRTY_LEVEL {
        BG,
        FLATTEN,
        CACHE_PATH,
        TEMP_PATH
    }

    public DrawingSurface(
            @NonNull Context context,
            @NonNull DrawingPaper paper
    ) {
        super(context);
        init(context, paper);
    }

    public void init(Context context, DrawingPaper paper) {

        if(holder == null) {
            setWillNotDraw(false);
            setLayerType(LAYER_TYPE_HARDWARE, null);

            TAG += " " + paper.getIndex();
            this.context = context;
            this.paper = paper;
            setZOrderOnTop(true);    // necessary
            holder = getHolder();
            holder.addCallback(this);
            holder.setFormat(PixelFormat.RGBA_8888);
        }

        initialisePaints();
    }

    private void createPaperBitmap(float width, float height) {
        //Log.d(TAG, "> createPaperBitmap " + paperBitmap);
        if (paperBitmap != null && paperBitmap.isRecycled()) {
            Log.d(TAG, "> createPaperBitmap Recycled");
            paper.setCachedSurfaceBitmap(null);
            paperBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        }
        if (paperBitmap != null && ((paperBitmap.getWidth() != width || paperBitmap.getHeight() != height))) {
            Log.d(TAG, "> createPaperBitmap size change");
            if (!paperBitmap.isRecycled()) {
                paper.setCachedSurfaceBitmap(null);
                final Bitmap oldBitmap = paperBitmap;
                paperBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.wtf(TAG,"> BITMAP_RECYCLED " + oldBitmap);
                        oldBitmap.recycle();
                    }
                },1000);
            }

        }
        if (paperBitmap == null) {
            //Log.d(TAG, "> createPaperBitmap new");
            paper.setCachedSurfaceBitmap(null);
            paperBitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
        }

        paperBitmap.eraseColor(Color.TRANSPARENT);
        redrawPaperBitmap();
        paper.setCachedSurfaceBitmap(paperBitmap);
    }

    public void updatePaperBitmap() {
        //Log.d(TAG, "> updatePaperBitmap " + paperBitmap);
        if (paperBitmap != null && !paperBitmap.isRecycled()) {
            synchronized (paperBitmap) {
                paper.setCachedSurfaceBitmap(paperBitmap);
            }
        }
    }

    public void redrawPaperBitmap(){
        synchronized (paperBitmap) {
            Log.d(TAG, "> redrawPaperBitmap recycled:" + paperBitmap.isRecycled()
                    + "\n reset:" + paper.getLayer().getUndoManager().undoCountFromReset()
                    + "\n resets:" + paper.getLayer().getUndoManager().getResets());

            //int fromStep = paper.getLayer().getUndoManager().undoCountFromReset();
            //int toStep = 0;

            int fromStep = (paper.getLayer().getUndoManager().undoCountFromReset() - 1 >= 0) ? paper.getLayer().getUndoManager().undoCountFromReset() - 1 : 0;
            int toStep = 0;

            if (paper.getLayer().updateHash(fromStep, toStep) && !paperBitmap.isRecycled()) {
                Log.d(TAG, "> redrawPaperBitmap flattenImage paperBitmap");
                paper.getLayer().flattenImage(reusableCanvas, paperBitmap, fromStep, toStep);
            }
        }
    }

    public boolean hasPaperBitmap() {
        //Log.d(TAG,"> hasPaperBitmap " + paper.getLayer().hasDraw() + "  "+paperBitmap);
        return paperBitmap != null && paper.getLayer().hasDraw();
    }

    private void initialisePaints() {
        if(tool == null) {
            penTool = new PenTool("test", null);
            penTool.setColor(Color.BLACK);
            penTool.setAntiAlias(true);
            penTool.setStrokeWidth(1);
            eraserTool = new EraserTool("test", null);
            eraserTool.setAntiAlias(true);
            eraserTool.setStrokeWidth(1);
        }
        borderPaint.setStrokeWidth(1);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.DKGRAY);

        bitmapPaint.setAntiAlias(true);
        bitmapPaint.setDither(true);
        bitmapPaint.setFilterBitmap(true);

        gridPaint.setStrokeWidth(1);
        gridPaint.setAntiAlias(true);
        gridPaint.setColor(Color.GRAY);
    }

    public void setBorderPaint(@NonNull Paint borderPaint) {
        this.borderPaint = borderPaint;
        this.borderPaint.setStyle(Paint.Style.FILL);//設置畫筆類型為填充
    }

    public void setTool(PenTool tool) {
        this.tool = tool;
        toolReport = null;
    }

    public void setStylus(PEN_TYPE type){
        lastTool = null;
        switch (type){
            case BALLPOINT:
                tool = penTool;
                tool.getToolAttributes().getPaint().setStrokeJoin(Paint.Join.ROUND);
                tool.getToolAttributes().getPaint().setStrokeCap(Paint.Cap.ROUND);
                break;
            case ERASER:
                tool = eraserTool;
                tool.getToolAttributes().getPaint().setStrokeJoin(Paint.Join.ROUND);
                tool.getToolAttributes().getPaint().setStrokeCap(Paint.Cap.ROUND);
                break;
            case FOUNTAIN:
                break;
            case UNKNOWN:
                tool = penTool;
                tool.getToolAttributes().getPaint().setStrokeJoin(Paint.Join.ROUND);
                tool.getToolAttributes().getPaint().setStrokeCap(Paint.Cap.ROUND);
                break;
            case HIGHLIGHTER:
                tool = penTool;
                tool.getToolAttributes().getPaint().setStrokeJoin(Paint.Join.ROUND);
                tool.getToolAttributes().getPaint().setStrokeCap(Paint.Cap.SQUARE);
                break;
        }
    }

    public void setPaintColor(int color){
        tool.getToolAttributes().getPaint().setColor(color);
    }

    public void setPaintWidth(float width){
        tool.getToolAttributes().getPaint().setStrokeWidth(width);
    }

    public void setPaintAlpha(int alpha){
        tool.getToolAttributes().getPaint().setAlpha(alpha);
    }

    public boolean isSurfaceCreated() {
        return surfaceCreated;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if(visibility == VISIBLE){
            onResume();
        }else{
            onPause();
        }
    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void release() {
        Log.i(TAG,">  release");
//        if(holder != null){
//            holder.removeCallback(this);
//            holder = null;
//        }
        if(paperBitmap != null && !paperBitmap.isRecycled()){
            final Bitmap oldBitmap = paperBitmap;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.wtf(TAG,"> BITMAP_RECYCLED " + oldBitmap);
                    oldBitmap.recycle();
                }
            },1000);
        }
        paperBitmap = null;
        reusableCanvas = null;
    }


    public void destroy() {
        this.release();
    }

    public void updateUI() {
        invalidate();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "> NCN surfaceCreated" + getWidth() + " x " + getHeight());
        surfaceRect.set(0, 0, getWidth(), getHeight());
        createPaperBitmap(getWidth(), getHeight());

        surfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "> NCN surfaceChanged " + width + " x " + height);
        surfaceRect.set(0, 0, getWidth(), getHeight());
        createPaperBitmap(getWidth(), getHeight());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "> NCN surfaceDestroyed");
        surfaceCreated = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getMetaState() == NativeCanvas.MODIFIED && holder != null) {
            // Log.d(TAG, "> NCN paper onTouch " + Utility.getActionName(event.getAction()) + " =>  " + event.getX() + ", " + event.getY());
            synchronized (holder) {
                int action = event.getActionMasked();
                int index = event.getActionIndex();

                // Choosing to ignore drawing operations that use multiple fingers
                if (currentPointerId == NULL_POINTER_ID) {
                    currentPointerId = event.getPointerId(index);
                }

                // Single-touch event
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (lastTool != null) {
                            tool = lastTool;
                            lastTool = null;
                        }
                        if (!tool.getToolAttributes().isMature()) {
                            tool.getToolAttributes().setMature(true);
                            if (tool instanceof PenTool) {
                                paper.getLayer().addDrawing(toolReport.getPath().clone(), tool.getToolAttributes().getPaint().clone());
                            } else if (tool instanceof EraserTool) {
                                paper.getLayer().addEraser(toolReport.getPath().clone(), tool.getToolAttributes().getPaint().clone());
                            }
                        }
                        toolReport = tool.start(null, event.getX(), event.getY(), event.getEventTime());
                        tool.getToolAttributes().setMature(false);
                        updateUI();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        toolReport = tool.move(null, event.getX(), event.getY(), event.getEventTime());
                        updateUI();
                        break;
                    case MotionEvent.ACTION_UP:
                        currentPointerId = NULL_POINTER_ID;
                        toolReport = tool.end(null, event.getX(), event.getY(), event.getEventTime());
                        tool.getToolAttributes().setMature(true);
                        if (tool instanceof PenTool) {
                            paper.getLayer().addDrawing(toolReport.getPath().clone(), tool.getToolAttributes().getPaint().clone());
                        } else if (tool instanceof EraserTool) {
                            paper.getLayer().addEraser(toolReport.getPath().clone(), tool.getToolAttributes().getPaint().clone());
                        }
                        updateUI();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        // Stops the tool from performing further drawing
                        Log.e(TAG,"> NCN ACTION_CANCEL");
                        currentPointerId = NULL_POINTER_ID;
                        tool.cancel();
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        // Ends drawing operation only if the main pointer left the screen
                        if (event.getPointerId(index) == currentPointerId) {
                            Log.e(TAG,"> NCN ACTION_POINTER_UP");
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (surfaceCreated && canvas != null) {

            boolean isEraser = tool instanceof EraserTool;

            // 暫時換成一般筆，以免殘留橡皮擦外誆
            if (tool != null && tool.getToolAttributes().isMature()) {
                if (isEraser) {
                    lastTool = tool;
                    tool = penTool;
                }
            }

            // 取得緩衝數，並繪製
            cacheSteps = 0;
            if(paperBitmap != null && !paperBitmap.isRecycled()){
                cacheSteps = paper.getLayer().getCacheDrawingCount(reusableCanvas, paperBitmap, isEraser);
            }
            if (cacheSteps > 0) drawPath(canvas, cacheSteps);

            // 繪製目前尚未完成的路徑
            if (tool != null && !tool.getToolAttributes().isMature()) {
                if (toolReport != null) {
                    canvas.drawPath(toolReport.getPath(), tool.getToolAttributes().getPaint());
                    if (tool instanceof EraserTool) {
                        EraserTool eraser = (EraserTool) tool;
                        canvas.drawArc(
                                getCircleRect(eraser.getToolAttributes().getPaint().getStrokeWidth() / 2, eraser.getCurrent().x, eraser.getCurrent().y),
                                0, 360, false, ((EraserToolAttributes)eraser.getToolAttributes()).getPointPaint()
                        );
                    }
                }
            }

            // 繪製格線
            if (NCManager.isDebug) drawBorder(canvas, paper.getSourcePoints());
        }
    }

    private void drawBorder(Canvas canvas, float[] srcPoints) {
        canvas.drawLines(srcPoints, 0, 8, borderPaint);
        canvas.drawLines(srcPoints, 2, 8, borderPaint);
        canvas.drawLines(srcPoints, 10, 16, gridPaint);
        canvas.drawLines(srcPoints, 26, 16, gridPaint);
    }

    private void drawPath(Canvas canvas, int cacheSteps) {

        StringBuilder types = new StringBuilder();
        StringBuilder skips = new StringBuilder();

        for (int i = cacheSteps - 1; i >= 0; i--) {
            UndoItem item = paper.getLayer().getStep(i);

            types.append(item == null ? "NULL" : item.isTransform() ? "" : item.getType().key());
            if(i != 0) types.append(",");
            if(item != null && item.isTransform()) {
                skips.append(item.getType());
                if(i != 0) skips.append(",");
            }

            if(item != null && !item.isTransform()) {
                NCStickerView stickerView = paper.getLayer().getStickerView();
                switch (item.getType()) {
                    case STICKER_IMAGE:
                        JSONObject imageStickerData = item.getData();
                        if (stickerView != null && imageStickerData != null) {
                            ImageSticker imageSticker = (ImageSticker) stickerView.getStickerByUuid(imageStickerData.optString(Sticker.STICKER_UUID));
                            if (imageSticker != null && !imageSticker.isSelected()) {
                                Log.w(TAG,"> draw imageSticker");
                                imageSticker.updateData(imageStickerData);
                                imageSticker.finalizeBitmap();
                                canvas.drawBitmap(imageSticker.getBitmap(), imageSticker.getBitmapPosition().x, imageSticker.getBitmapPosition().y, bitmapPaint);
                            }
                        }
                        break;
                    case STICKER_TEXT:
                        JSONObject textStickerData = item.getData();
                        if (stickerView != null && textStickerData != null) {
                            TextSticker textSticker = (TextSticker) stickerView.getStickerByUuid(textStickerData.optString(Sticker.STICKER_UUID));
                            if (textSticker != null && !textSticker.isSelected()) {
                                Log.w(TAG,"> draw textSticker");
                                textSticker.updateData(textStickerData);
                                textSticker.finalizeBitmap();
                                canvas.drawBitmap(textSticker.getBitmap(), textSticker.getBitmapPosition().x, textSticker.getBitmapPosition().y, bitmapPaint);
                            }
                        }
                        break;
                    case DRAWING:
                        if (item instanceof UndoItemDrawing) {
                            UndoItemDrawing drawing = (UndoItemDrawing) item;
                            canvas.drawPath(drawing.getPath(), drawing.getPaint());
                        }
                        break;
                    case ERASER:
                        if (item instanceof UndoItemDrawing) {
                            UndoItemDrawing drawing = (UndoItemDrawing) item;
                            canvas.drawPath(drawing.getPath(), drawing.getPaint());
                        }
                        break;
                    case TEMPLATE:
                        break;
                }
            }
        }
        Log.w(TAG,"> drawPath from 0 to " + (cacheSteps - 1)
                + "\n draw:" + types.toString()
                + "\n drop:" + skips.toString()
        );
    }

    private RectF getCircleRect(float width, float x, float y) {
        circleRect.set(x - width, y - width, x + width, y + width);
        return circleRect;
    }

    public Bitmap getPaperBitmap(Bitmap bitmap) {
        synchronized (paperBitmap) {
            int fromStep = paper.getLayer().getUndoManager().undoCountFromReset();
            int toStep = 0;
            Log.w(TAG, "> getPaperBitmap "
                    + (bitmap == null ? "" : "\n background:" + bitmap.getWidth() + " x " + bitmap.getHeight())
                    + "\n    drawing:" + paperBitmap.getWidth() + " x " + paperBitmap.getHeight()
            );
            if (paper.getLayer().updateHash(fromStep, toStep)) {
                paper.getLayer().flattenImage(reusableCanvas, paperBitmap, fromStep, toStep);
            }
            if (bitmap != null) {
                reusableCanvas.setBitmap(bitmap);
                reusableCanvas.drawBitmap(paperBitmap, 0, 0, null);
                return bitmap;
            } else {
                return paperBitmap;
            }
        }
    }
}
