package com.kcchen.nativecanvas.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

import com.kcchen.nativecanvas.enums.MOVE_MODE;
import com.kcchen.nativecanvas.model.NCLayerData;
import com.kcchen.nativecanvas.multitouch.MoveGestureDetector;
import com.kcchen.nativecanvas.multitouch.RotateGestureDetector;
import com.kcchen.nativecanvas.multitouch.TapGestureDetector;
import com.kcchen.nativecanvas.paper.Paper;
import com.kcchen.nativecanvas.sticker.Sticker;
import com.kcchen.nativecanvas.utils.FontProvider;
import com.kcchen.nativecanvas.utils.Utility;
import com.kcchen.penpal.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kowei on 2017/12/20.
 */

public class NCStickerView extends RelativeLayout {
    private static final String TAG = NCStickerView.class.getSimpleName();
    private Activity activity;
    //rivate PointF center;
    private NCRect stickerViewDimention;
    protected final float[] srcPoint = new float[2];
    protected final float[] destPoint = new float[2];
    private NCRect displayDimention;
    private NCBookView bookView;
    private Matrix matrix = new Matrix();
    private NCLayerData.SCALE_LIMIT paperLimit;
    private boolean isViewMode;
    private Paint cornerPaint = new Paint();
    private float cornerWidth = 40;
    private float cornerHeight = 10;

    public void setViewMode(boolean viewMode) {
        this.isViewMode = viewMode;
        if(selectedSticker!= null && selectedSticker.isSelected()){
            unselectSticker();
        }
    }

    public void clear() {
        for (int i = stickers.size() - 1; i >= 0; i--) {
            if(stickers.get(i) != null) {
                stickers.get(i).destroy();
            }
        }
        stickers.clear();
        Log.d(TAG,"> clear stickers:"+stickers.size());
    }

    public interface Constants {
        float SELECTED_LAYER_ALPHA = 0.15F;
    }

    public interface StickerViewListener {
        void onSelected(@Nullable Sticker sticker);
        void onDoubleTap(@NonNull Sticker sticker);
        void onDisplay(@Nullable Sticker sticker, int visibility);
        void onPost(Sticker sticker);
    }

    // layers
    private final List<Sticker> stickers = new ArrayList<Sticker>();
    private Context context;
    private FontProvider fontProvider;
    private Paint defaultBorderPaint;
    private MOVE_MODE moveMode;

    @Nullable
    private Sticker selectedSticker;
    private Paint selectedLayerPaint;

    // callback
    @Nullable
    private StickerViewListener stickerViewListener;

    // gesture detection
    private ScaleGestureDetector scaleGestureDetector;
    private RotateGestureDetector rotateGestureDetector;
    private MoveGestureDetector moveGestureDetector;
    private TapGestureDetector tapGestureDetector;

    private NCBookView.OnPaperListener paperListener = new NCBookView.OnPaperListener() {

        @Override
        public void onReady(Paper paper) {

        }

        @Override
        public void onDimention(int width, int height) {
            //Log.i(TAG,"> NCN onDimention " + width + "x" + height);
            // TODO: 應該是修改 sticker view 大小及位置，以符合 paper
        }

        @Override
        public void onDimention(NCRect dimention) {
            //Log.i(TAG,"> NCN onDimention " + dimention);
            // TODO: 應該是修改 sticker view 大小及位置，以符合 paper
        }

        @Override
        public void onMove(Paper paper, float deltaX, float deltaY, boolean isMove) {

        }

        @Override
        public void onThumbnailUpdate() {

        }
    };


    // constructors
    public NCStickerView(Activity activity) {
        super(activity);
        init(activity);
    }

    public NCStickerView(Activity activity, AttributeSet attrs) {
        super(activity, attrs);
        init(activity);
    }

    public NCStickerView(Activity activity, AttributeSet attrs, int defStyleAttr) {
        super(activity, attrs, defStyleAttr);
        init(activity);
    }

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NCStickerView(Activity activity, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(activity, attrs, defStyleAttr, defStyleRes);
        init(activity);
    }

    private void init(@NonNull Activity activity) {
        // I fucking love Android
        this.activity = activity;
        this.moveMode = MOVE_MODE.MOVE;
        setWillNotDraw(false);

        if(this.fontProvider == null) this.fontProvider = new FontProvider(getResources());

        if(this.selectedLayerPaint == null) selectedLayerPaint = new Paint();
        selectedLayerPaint.setAlpha((int) (255 * Constants.SELECTED_LAYER_ALPHA));
        selectedLayerPaint.setAntiAlias(true);
        selectedLayerPaint.setFilterBitmap(true);

        cornerPaint.setAntiAlias(true);
        cornerPaint.setFilterBitmap(true);

        // init listeners
        if(this.scaleGestureDetector  == null) this.scaleGestureDetector  = new ScaleGestureDetector( activity, new ScaleListener() );
        if(this.rotateGestureDetector == null) this.rotateGestureDetector = new RotateGestureDetector(activity, new RotateListener());
        if(this.moveGestureDetector   == null) this.moveGestureDetector   = new MoveGestureDetector(  activity, new MoveListener()  );
        if(this.tapGestureDetector    == null) {
            this.tapGestureDetector    = new TapGestureDetector(   activity, new TapsListener()  );
            this.tapGestureDetector.setIsLongpressEnabled(false);
        }

        setOnTouchListener(onTouchListener);

        updateUI();
    }

    public void onResume() {
        for(Sticker sticker:getStickers()){
            sticker.onResume();
        }
    }

    public void onPause() {
        for(Sticker sticker:getStickers()){
            sticker.onPause();
        }
    }

    public void setPaperLimit(NCLayerData.SCALE_LIMIT paperLimit) {
        this.paperLimit = paperLimit;
    }

    public NCStickerView setOnPaperListener(NCBookView bookView, boolean isSet){
        if(bookView != null){
            if(isSet){
                bookView.setOnPaperListener(paperListener);
            }else{
                bookView.setOnPaperListener(null);
            }
        }
        return this;
    }

    public void setStickerViewListener(@Nullable StickerViewListener listener) {
        this.stickerViewListener = listener;
    }

    public Sticker getSelectedSticker() {
        return selectedSticker;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public Sticker getStickerByUuid(String uuid) {
        if(uuid != null) {
            for (Sticker sticker : stickers) {
                if (sticker.getUuid().equals(uuid)) {
                    return sticker;
                }
            }
        }
        return null;
    }

    public void importSticker(Sticker sticker) {
        if (sticker != null && !hasSticker(sticker)) {
            //Log.i(TAG,"> importSticker " + stickers.size());
            if (stickerViewDimention != null) sticker.setDimention(stickerViewDimention);
            sticker.setIndex(stickers.size());
            sticker.setStickerView(this);
            sticker.setActivity(activity);
            sticker.setAdded(true);
            sticker.setBorderPaint(defaultBorderPaint);
            stickers.add(sticker);
        }
    }

    public NCStickerView addSticker(@Nullable Sticker sticker) {
        if (sticker != null && !hasSticker(sticker)) {
            if (stickerViewDimention != null) sticker.setDimention(stickerViewDimention);
            sticker.setIndex(stickers.size());
            sticker.setStickerView(this);
            sticker.setActivity(activity);
            stickers.add(sticker);
            selectSticker(sticker, false);
        }
        return this;
    }

    public boolean hasSticker(Sticker sticker) {
        boolean isIdentical = false;
        for(Sticker s: stickers){
            //Log.i(TAG, "> sticker compare:" + s.hashCode() + " - " + sticker.hashCode());
            //Log.i(TAG, "> sticker compare:" + s.getUuid() + " - " + sticker.getUuid());
            if(sticker.hashCode() == s.hashCode() || sticker.getUuid().equals(s.getUuid())){
                isIdentical = true;
            }
        }
        return isIdentical;
    }

    public NCStickerView setDimention(NCBookView bookView, NCRect displayDimention, NCRect stickerViewDimention) {
        Rect out = new Rect();
        this.getHitRect(out);
        // Log.w(TAG,"> NCN setDimention"
        //         + "\nstickerViewDimention: " + stickerViewDimention
        //         + "\n              origin: " + this.getX() + "x" + this.getY()
        //         + "\n             HitRect: " + out
        // );
        if(bookView != null) this.bookView = bookView;
        if(displayDimention != null) this.displayDimention = displayDimention;
        if(stickerViewDimention != null) {
            this.stickerViewDimention = stickerViewDimention;
            setX(stickerViewDimention.left());
            setY(stickerViewDimention.top());
            Utility.setRelativeLayout(this,(int)stickerViewDimention.width(),(int)stickerViewDimention.height(),0,0,0,0);
            setStickerDimention(NCManager.NONE, stickerViewDimention);
        }
        return this;
    }

    public void setStickerDimention(int stickerId, NCRect dimention) {
        Log.w(TAG,"> NCN setStickerDimention:" + dimention);
        if(stickerId == NCManager.NONE){
            for(Sticker sticker : getStickers()){
                sticker.setDimention(dimention);
            }
        }else{
            // TODO: 制定 sticker ID
            // if(getStickers().size() > paperNumber) getPapers().get(paperNumber).setDimention(stickerViewDimention);
        }
    }

    public PointF getConvertedOffset() {
        matrix.reset();
        srcPoint[0] = getWidth();
        srcPoint[1] = getHeight();
        matrix.preScale(getScaleX(), getScaleY());
        matrix.preScale(0.5f, 0.5f);
        matrix.mapPoints(destPoint, srcPoint);
        return new PointF(destPoint[0], destPoint[1]);
    }

    public PointF getConvertedOrign(float x, float y){
        PointF offset = getConvertedOffset();
        matrix.reset();
        srcPoint[0] = x;
        srcPoint[1] = y;
        matrix.preTranslate(getPivotX(), getPivotY());
        matrix.preTranslate(-offset.x, -offset.y);
        matrix.mapPoints(destPoint, srcPoint);       // 畫布原點
        return new PointF(destPoint[0], destPoint[1]);
    }

    public PointF getConvertedPoint(PointF origin, float x, float y){
        matrix.reset();
        srcPoint[0] = x;
        srcPoint[1] = y;
        if(origin!= null) matrix.preTranslate(-origin.x, -origin.y);
        matrix.postScale(1/getScaleX(),1/getScaleY());
        matrix.mapPoints(destPoint,srcPoint);
        return new PointF(destPoint[0], destPoint[1]);
    }

    public PointF visibleCenter() {
        PointF origin = getConvertedOrign(getX(),getY());
        PointF center = getConvertedPoint(origin, getPivotX(),getPivotY());

        return center;


        // float w = getWidth();
        // float h = getHeight();
        // float ox = (w * getScaleX() / 2) - getPivotX();                                        // 縮放數值
        // float oy = (h * getScaleY() / 2) - getPivotY();                                       // 縮放數值
        // float alx = getX() - ox;
        // float aty = getY() - oy;

        // PointF rightBottom = getConvertedPoint(origin, getWidth(),getHeight());
        // PointF leftTop = getConvertedPoint(origin, 0,0);

        //(w - (2 * alx))/getScaleX()
        // float vlmx = (0 - (alx/getScaleX()));  float vrmx = vlmx + w/getScaleX();                      // 無縮放
        // float vtmy = (0 - (aty/getScaleX()));  float vbmy = vtmy + h/getScaleX();                     // 無縮放
        // float clx = (vlmx + vrmx) / 2;         float cty = (vtmy + vbmy) / 2;
        // Log.e(TAG, "> NCN visibleCenter " + Utility.formatFixedFloat(getScaleX(), 1, 2, false)
        //         + " |dd "   + Utility.formatFixedFloat(w, 4, 0, false)                  + " x " + Utility.formatFixedFloat(h, 4, 0, false)
        //         + " |ox "   + Utility.formatFixedFloat(origin.x, 3, 0, true)            + ", "  + Utility.formatFixedFloat(origin.y, 3, 0, true)
        //         + " |a0 "   + Utility.formatFixedFloat(alx, 4, 0, true)                 + ", "  + Utility.formatFixedFloat(aty, 4, 0, true)
        //         + " |v0 "   + Utility.formatFixedFloat(vlmx, 4, 0, true)                + ", "  + Utility.formatFixedFloat(vtmy, 4, 0, true)
        //         + " |v0 "   + Utility.formatFixedFloat(leftTop.x, 4, 0, true)           + ", "  + Utility.formatFixedFloat(leftTop.y, 4, 0, true)
        //         + " |v2 "   + Utility.formatFixedFloat(vrmx, 4, 0, true)                + ", "  + Utility.formatFixedFloat(vbmy, 4, 0, true)
        //         + " |v2 "   + Utility.formatFixedFloat(rightBottom.x, 4, 0, true)       + ", "  + Utility.formatFixedFloat(rightBottom.y, 4, 0, true)
        //         + " |cp "   + Utility.formatFixedFloat(clx, 3, 0, true)                 + ", "  + Utility.formatFixedFloat(cty, 3, 0, true)
        //         + " |cp "   + Utility.formatFixedFloat(center.x, 3, 0, true)            + ", "  + Utility.formatFixedFloat(center.y, 3, 0, true)
        // );
    }

    public NCStickerView setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
        return this;
    }

    public NCStickerView setCenter(@Nullable Sticker sticker, float scale, boolean isScaleDown){
        if (sticker != null) {
            sticker.moveToCanvasCenter();
            sticker.getLayer().setScale(scale);
            updateUI();
        }
        return this;
    }

    public NCStickerView setVisibleCenter(@Nullable Sticker sticker, PointF position, float scale, boolean isScaleDown){
        if (sticker != null) {
            sticker.moveCenterTo(position);
            sticker.getLayer().setScale(scale/getScaleX());
            updateUI();
        }
        return this;
    }

    public NCStickerView setVisibleCenter(Sticker sticker, float scale, boolean b) {
        if (sticker != null) {
            sticker.getLayer().setScale(scale/getScaleX());
            sticker.moveCenterTo(visibleCenter());
            updateUI();
        }
        return this;
    }


    public NCStickerView setPosition(@Nullable Sticker sticker, float scale, float ratioX, float ratioY, boolean isScaleDown) {
        if (sticker != null) {
            sticker
                    .setStickerScale(scale)
                    .moveOriginTo(ratioX,ratioY);
            updateUI();
        }
        return this;
    }

    public NCStickerView hide() {
        Log.d(TAG,"> NCN hide stickerView");
        if(this.getVisibility() != View.INVISIBLE){
            this.setVisibility(View.INVISIBLE);
            if(this.stickerViewListener != null) this.stickerViewListener.onDisplay(selectedSticker, View.INVISIBLE);
        }
        return this;
    }

    public NCStickerView show() {
        if(this.getVisibility() != View.VISIBLE) {
            this.setVisibility(View.VISIBLE);
            this.bringToFront();
            if(this.stickerViewListener != null) this.stickerViewListener.onDisplay(selectedSticker, View.VISIBLE);
        }
        return this;
    }


    public NCStickerView setBGColor(int color){
        this.setBackgroundColor(color);
        return this;
    }

    public NCStickerView setDefaultBorder(int borderWidth, int borderColor, boolean antiAlias, boolean isApplyAll){
        defaultBorderPaint = new Paint();
        defaultBorderPaint.setStrokeWidth(borderWidth);
        defaultBorderPaint.setAntiAlias(antiAlias);
        defaultBorderPaint.setColor(borderColor);
        defaultBorderPaint.setFilterBitmap(true);

        if(isApplyAll){
            this.applyDefaultBorder(null);
        }
        return this;
    }

    /**
     *
     * @param sticker if null, apply all
     * @return
     */
    public NCStickerView applyDefaultBorder(@Nullable Sticker sticker) {
        if(sticker == null){
            for (Sticker s : getStickers()) {
                s.setBorderPaint(defaultBorderPaint);
            }
        }else{
            sticker.setBorderPaint(defaultBorderPaint);
        }
        return this;
    }

    private void initStickerBorder(@NonNull Sticker sticker ) {
        // init stroke
        this.setDefaultBorder(getResources().getDimensionPixelSize(R.dimen.stroke_size), Color.MAGENTA, true, false);
        sticker.setBorderPaint(this.defaultBorderPaint);
    }

    private void drawSelectedSticker(Canvas canvas) {
        if(selectedSticker != null){
            selectedSticker.draw(canvas, null);
        }
    }

    /**
     * draws all stickers on the canvas
     * @param canvas Canvas where to draw all stickers
     */
    private void drawAllStickers(Canvas canvas) {
        for (int i = 0; i < stickers.size(); i++) {
            stickers.get(i).draw(canvas, null);
        }
    }

    /**
     * as a side effect - the method deselects Entity (if any selected)
     * @return bitmap with all the Entities at their current positions
     */
    public Bitmap getAllThumbnailImage() {
        selectSticker(null, false);

        Bitmap bmp = Bitmap.createBitmap(getStickerViewWidth(), getStickerViewHeight(), Bitmap.Config.ARGB_8888);
        // IMPORTANT: always create white background, cos if the image is saved in JPEG format,
        // which doesn't have transparent pixels, the background will be black
        bmp.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bmp);
        drawAllStickers(canvas);

        return bmp;
    }

    public Bitmap getSelectedThumbnailImage() {
        // TODO: size to fit sticker
        Bitmap bmp = Bitmap.createBitmap(getStickerViewWidth(), getStickerViewHeight(), Bitmap.Config.ARGB_8888);
        // IMPORTANT: always create white background, cos if the image is saved in JPEG format,
        // which doesn't have transparent pixels, the background will be black
        bmp.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bmp);
        drawSelectedSticker(canvas);

        return bmp;
    }

    private void handleMoveTranslate(PointF delta) {
        if (selectedSticker != null) {
            boolean needUpdateUI = false;

            // Log.e(TAG,"> NCN handleMoveTranslate "
            //         + "\n                delta: " + delta.x + ", " + delta.y
            //         + "\n              Sticker: " + selectedSticker.getStickerWidth() + " x " + selectedSticker.getStickerHeight()
            //         + "\n       StickerDisplay: " + selectedSticker.getStickerDisplayWidth() + " x " + selectedSticker.getStickerDisplayHeight()
            //         + "\n          StickerView: " + getStickerViewWidth() + " x " + getStickerViewHeight()
            //         + "\n   StickerView Origin: " + this.getX() + ", " + this.getY()
            //         + "\n               Screen: " + Utility.getDisplayWidth(context) + " x " + Utility.getDisplayHeight(context)
            //         + "\n          DisplayRect: " + selectedSticker.getDisplayRect() + " [center]" + selectedSticker.getCenterX() + " x " + selectedSticker.getCenterY() + " [center]" + selectedSticker.getCenterX() + " x " + selectedSticker.getCenterY() + " [center]" + selectedSticker.centerX() + " x " + selectedSticker.centerY()
            //         + "\n        ScalefitRatio: " + selectedSticker.getScalefitRatio()
            //         + "\n                Layer: " + selectedSticker.getLayer().getX() + ", " + selectedSticker.getLayer().getY()
            //         + "\n           LayerScale: " + selectedSticker.getStickerScale()
            // );

            if (selectedSticker.centerX() + delta.x >= this.getX() && selectedSticker.centerX() + delta.x <= this.getX() + getStickerViewWidth()) {
                selectedSticker.getLayer().postTranslate(delta.x / getStickerViewWidth(), 0.0F);
                needUpdateUI = true;
            }
            if (selectedSticker.centerY() + delta.y >= this.getY() && selectedSticker.centerY() + delta.y <= this.getY() + getStickerViewHeight()) {
                selectedSticker.getLayer().postTranslate(0.0F, delta.y / getStickerViewHeight());
                needUpdateUI = true;
            }
            if (needUpdateUI) {
                updateUI();
            }
        }
    }

    private void initialTranslateAndScale(@NonNull Sticker sticker) {
        sticker.moveToCanvasCenter();
        sticker.getLayer().setScale(sticker.getLayer().initialScale());
    }

    public void selectSticker(@Nullable Sticker sticker, boolean isNotify) {

        if (selectedSticker != null) selectedSticker.setIsSelected(false);

        if (sticker != null) {
            sticker.setIsSelected(true);
        }else{
            if (stickerViewListener != null && selectedSticker != null) {
                stickerViewListener.onPost(stickers.get(selectedSticker.getIndex()));
            }
        }
        selectedSticker = sticker;
        if (isNotify && stickerViewListener != null && selectedSticker != null) {
            stickerViewListener.onSelected(selectedSticker);
        }
        invalidate();
    }

    public void unselectSticker() {
        if (selectedSticker != null) {
             selectSticker(null, true);
        }
    }

    @Nullable
    public Sticker findStickerAtPoint(float x, float y) {
        Sticker selected = null;
        PointF p = new PointF(x, y);
        for (int i = stickers.size() - 1; i >= 0; i--) {
            Log.d(TAG,"> findStickerAtPoint " + i);
            if (stickers.get(i).pointInLayerRect(p)) {
                selected = stickers.get(i);
                break;
            }
        }
        return selected;
    }

    private void updateSelectionOnTap(MotionEvent event) {
        Sticker sticker = findStickerAtPoint(event.getX(), event.getY());
        selectSticker(sticker, true);
    }

    private void updateOnLongPress(MotionEvent event) {
        // if layer is currently selected and point inside layer - move it to front
        if (selectedSticker != null) {
            PointF p = new PointF(event.getX(), event.getY());
            if (selectedSticker.pointInLayerRect(p)) {
                bringStickerToFront(selectedSticker);
            }
        }
    }

    public void bringStickerToFront(@NonNull Sticker sticker) {
        // removing and adding brings layer to front
        if (stickers.remove(sticker)) {
            sticker.setIndex(stickers.size());
            stickers.add(sticker);
            invalidate();
        }
    }

    public void bringStickerToBack(@Nullable Sticker sticker) {
        if (sticker == null) {
            return;
        }
        if (stickers.remove(sticker)) {
            sticker.setIndex(0);
            stickers.add(0, sticker);
            invalidate();
        }
    }

    public void flipSelectedSticker() {
        if (selectedSticker == null) {
            return;
        }
        selectedSticker.getLayer().flip();
        invalidate();
    }

    public void deletedSelectedSticker() {
        if (selectedSticker == null) {
            return;
        }
        if (stickers.remove(selectedSticker)) {
            selectedSticker.release();
            selectedSticker = null;
            invalidate();
        }
    }

    private int getStickerViewWidth(){
        return getWidth();
    }

    private int getStickerViewHeight(){
        return getHeight();
    }

    private boolean getControl(MotionEvent event) {
        boolean isControl = false;
        if (selectedSticker != null && event != null) {
            PointF p = new PointF(event.getX(), event.getY());
            if (selectedSticker.pointInControlButton(p)) {
                // Log.e(TAG,"> NCN zoom");
                moveMode = MOVE_MODE.SCALE;
                isControl = true;
            }else if(selectedSticker.pointInRotateButton(p)) {
                // Log.e(TAG, "> NCN rotate");
                moveMode = MOVE_MODE.ROTATE;
                isControl = true;
            }else{
                moveMode = MOVE_MODE.MOVE;
                // Log.e(TAG, "> NCN move");
            }
        }
        return isControl;
    }

    public void updateUI() {
        invalidate();
    }

    public void destroy() {
        this.release();
        clear();
        this.context = null;
        this.fontProvider = null;
        this.defaultBorderPaint = null;
        this.moveMode = null;
        if(this.selectedSticker != null) {
            this.selectedSticker.destroy();
            this.selectedSticker = null;
        }
        this.selectedLayerPaint = null;
        this.stickerViewListener = null;
        this.scaleGestureDetector = null;
        this.rotateGestureDetector = null;
        this.moveGestureDetector = null;
        this.tapGestureDetector = null;

    }

    // memory
    public void release() {
        for (Sticker sticker : stickers) {
            sticker.release();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // dispatch draw is called after child views is drawn.
        // the idea that is we draw background stickers, than child views (if any), and than selected item
        // to draw on top of child views - do it in dispatchDraw(Canvas)
        // to draw below that - do it in onDraw(Canvas)
        if (selectedSticker != null) {
            selectedSticker.draw(canvas, selectedLayerPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // drawAllStickers(canvas);
        drawSelectedSticker(canvas);
        if (NCManager.isDebug) drawCorner(canvas);
        super.onDraw(canvas);
    }

    /**
     * float cornerReveseWidth = cornerWidth/getScaleX();
     * float cornerReveseHeight = cornerHeight/getScaleX();
     * float cornerExtraWidth = 20/getScaleX();
     * float cornerExtraHeight = 20/getScaleX();
     * float ox = (getWidth()*getScaleX()/2) - getPivotX();                                                                       // 縮放數值
     * float oy = (getHeight()*getScaleX()/2) - getPivotY();                                                                      // 縮放數值
     * float alx = getX() - ox;                    float arx = alx + (getWidth()*getScaleX());                                    // 縮放數值
     * float aty = getY() - oy;                    float aby = aty + (getHeight()*getScaleX());                                   // 縮放數值
     * float vlmx = (0 - (alx/getScaleX()));  float vrmx = vlmx + getWidth()/getScaleX();                                         // 無縮放
     * float vtmy = (0 - (aty/getScaleX()));  float vbmy = vtmy + getHeight()/getScaleX();                                        // 無縮放
     * float vlx = vlmx + cornerReveseWidth + cornerExtraWidth;       float vrx = vrmx - cornerReveseWidth - cornerExtraWidth;    // 無縮放
     * float vty = vtmy + cornerReveseWidth + cornerExtraWidth;       float vby = vbmy - cornerReveseWidth - cornerExtraWidth;    // 無縮放
     * float clx = (vlx + vrx) / 2;                float cty = (vty + vby) / 2;
     * Log.e(TAG, "> NCN drawCorner    " + Utility.formatFixedFloat(getScaleX(), 1, 2, false)
     *          + " |ox "   + Utility.formatFixedFloat(ox, 3, 0, true)          + ", "  + Utility.formatFixedFloat(oy, 3, 0, true)
     *          + " |a0 "   + Utility.formatFixedFloat(alx, 4, 0, true)         + ", "  + Utility.formatFixedFloat(aty, 4, 0, true)
     *          + " |a2 "   + Utility.formatFixedFloat(arx, 4, 0, true)         + ", "  + Utility.formatFixedFloat(aby, 4, 0, true)
     *          + " |v0 "   + Utility.formatFixedFloat(vlx, 4, 0, true)         + ", "  + Utility.formatFixedFloat(vty, 4, 0, true)
     *          + " |v2 "   + Utility.formatFixedFloat(vrx, 4, 0, true)         + ", "  + Utility.formatFixedFloat(vby, 4, 0, true)
     *          + " |m0 "   + Utility.formatFixedFloat(vlmx, 4, 0, true)        + ", "  + Utility.formatFixedFloat(vtmy, 4, 0, true)
     *          + " |m2 "   + Utility.formatFixedFloat(vrmx, 4, 0, true)        + ", "  + Utility.formatFixedFloat(vbmy, 4, 0, true)
     *          + " |cc "   + Utility.formatFixedFloat(clx, 3, 0, true)         + ", "  + Utility.formatFixedFloat(cty, 3, 0, true)
     *          + " |ox "   + Utility.formatFixedFloat(origin.x, 3, 0, true)    + ", "  + Utility.formatFixedFloat(origin.y, 3, 0, true)
     *          + " |ox "   + Utility.formatFixedFloat(center.x, 3, 0, true)    + ", "  + Utility.formatFixedFloat(center.y, 3, 0, true)
     * );
     */
    private void drawCorner(Canvas canvas) {

        PointF origin = getConvertedOrign(getX(),getY());
        PointF center = getConvertedPoint(origin, getPivotX(),getPivotY());
        PointF rightBottom = getConvertedPoint(origin, getWidth(),getHeight());
        PointF leftTop = getConvertedPoint(origin, 0,0);

        float cornerReveseWidth = cornerWidth/getScaleX();
        float cornerReveseHeight = cornerHeight/getScaleX();
        float cornerExtraWidth = cornerReveseHeight;
        float w = cornerReveseWidth - cornerReveseHeight;
        float vlmx = leftTop.x;     //(0 - (alx/getScaleX()));
        float vrmx = rightBottom.x; //vlmx + getWidth()/getScaleX();                      // 無縮放
        float vtmy = leftTop.y;     //(0 - (aty/getScaleX()));
        float vbmy = rightBottom.y; //vtmy + getHeight()/getScaleX();                     // 無縮放
        float vlx = vlmx + cornerReveseWidth + cornerExtraWidth;       float vrx = vrmx - cornerReveseWidth - cornerExtraWidth;                // 無縮放
        float vty = vtmy + cornerReveseWidth + cornerExtraWidth;       float vby = vbmy - cornerReveseWidth - cornerExtraWidth;                // 無縮放
        float clx = center.x; //(vlx + vrx) / 2;
        float cty = center.y; //(vty + vby) / 2;

        cornerPaint.setColor(Color.CYAN);
        // left-top
        canvas.drawRect(
                vlx, vty,
                vlx + w, vty + w,
                cornerPaint
        );
        // right-top
        canvas.drawRect(
                vrx - w, vty,
                vrx, vty + w,
                cornerPaint
        );
        // center
        canvas.drawRect(
                clx - (cornerReveseHeight), cty - (cornerReveseHeight),
                clx + (cornerReveseHeight), cty + (cornerReveseHeight),
                cornerPaint
        );
        // right-bottom
        canvas.drawRect(
                vrx - w, vby - w,
                vrx, vby,
                cornerPaint
        );
        // left-bottom
        canvas.drawRect(
                vlx, vby - w,
                vlx + w, vby,
                cornerPaint
        );
    }


    // gesture detectors
    private final OnTouchListener onTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Log.i(TAG,"> NCN sticker view on touch");

            if(isViewMode) return false;

            boolean isConsume = false;
            //if(!tapGestureDetector.onTouchEvent(event)){
                isConsume |= tapGestureDetector.onTouchEvent(event);
                isConsume |= scaleGestureDetector.onTouchEvent(event);
                isConsume |= rotateGestureDetector.onTouchEvent(event);
                isConsume |= moveGestureDetector.onTouchEvent(event);
            //}

            return isConsume;
        }
    };

    private class TapsListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {
            if (stickerViewListener != null && selectedSticker != null) {
                stickerViewListener.onDoubleTap(selectedSticker);
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            //updateOnLongPress(event);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            // updateSelectionOnTap(event);
            Sticker sticker = findStickerAtPoint(event.getX(), event.getY());
            if (sticker == null || sticker != selectedSticker) {
                unselectSticker();
                return true;
            }
            return false;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (selectedSticker != null) {
                float scaleFactorDiff = detector.getScaleFactor();
                selectedSticker.getLayer().postScale(scaleFactorDiff - 1.0F);
                updateUI();
            }
            return true;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotateBegin(RotateGestureDetector detector) {
            // Log.i(TAG,"> NCN onRotateBegin");
            return super.onRotateBegin(detector);
        }

        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if (selectedSticker != null) {
                // Log.i(TAG,"> NCN onRotate");
                selectedSticker.getLayer().postRotate(-detector.getRotationDegreesDelta());
                updateUI();
            }
            return true;
        }

        @Override
        public void onRotateEnd(RotateGestureDetector detector) {
            // Log.i(TAG,"> NCN onRotateEnd");
            super.onRotateEnd(detector);
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMoveBegin(MoveGestureDetector detector) {
            // Log.e(TAG, "> NCN onMoveBegin");
            getControl(detector.getStartEvent());
            return super.onMoveBegin(detector);
        }

        @Override
        public boolean onMove(MoveGestureDetector detector) {
            switch (moveMode) {
                case MOVE:
                    handleMoveTranslate(detector.getFocusDelta());
                    break;
                case ROTATE:
                    if (selectedSticker != null) {
                        // Log.e(TAG, "> NCN ROTATE " + selectedSticker.getLayer().getRotationInDegrees() + "  " + detector.getCurrentEvent().getX() + ", " + detector.getCurrentEvent().getY());
                        if(selectedSticker.setRotationInDegrees(detector.getCurrentEvent())){
                            updateUI();
                        }
                    }
                    break;
                case SCALE:
                    if (selectedSticker != null) {
                        // Log.e(TAG, "> NCN SCALE " + selectedSticker.getLayer().getScale() + "  " + detector.getCurrentEvent().getX() + ", " + detector.getCurrentEvent().getY());
                        if(selectedSticker.setScale(detector.getCurrentEvent())){
                            updateUI();
                        }
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onMoveEnd(MoveGestureDetector detector) {
            switch (moveMode){
                case MOVE:
                    break;
                case SCALE:
                    moveMode = MOVE_MODE.MOVE;
                    if(selectedSticker.hasRotateControl()){
                        selectedSticker.updateRotateControl();
                    }
                    break;
                case ROTATE:
                    moveMode = MOVE_MODE.MOVE;
                    break;
                default:
                    moveMode = MOVE_MODE.MOVE;
                    break;
            }
            super.onMoveEnd(detector);
        }
    }

}
