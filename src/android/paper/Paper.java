package com.kcchen.nativecanvas.paper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.kcchen.nativecanvas.enums.BORDER_TYPE;
import com.kcchen.nativecanvas.model.NCLayerDataPaper;
import com.kcchen.nativecanvas.sticker.Sticker;
import com.kcchen.nativecanvas.undo.UndoManager;
import com.kcchen.nativecanvas.utils.MathUtils;
import com.kcchen.nativecanvas.utils.Utility;
import com.kcchen.nativecanvas.view.NCBookView;
import com.kcchen.nativecanvas.view.NCManager;
import com.kcchen.nativecanvas.view.NCRect;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;

/**
 * Created by kowei on 2017/12/15.
 */

public abstract class Paper extends RelativeLayout{
    private String TAG = Paper.class.getSimpleName();

    /**
     * data
     */
    @NonNull
    protected NCLayerDataPaper layer;

    /**
     * transformation matrix for the entity
     */
    protected final Matrix matrix = new Matrix();
    /**
     * true - entity is selected and need to draw it's border
     * false - not selected, no need to draw it's border
     */

    /**
     * maximum scale of the initial image, so that
     * the entity still fits within the parent canvas
     */
    protected float scalefitRatio;

    /**
     * Initial points of the entity
     * @see #destPoints
     */
    protected final float[] srcPoints = new float[100];  // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0
    protected final float[] destPoints = new float[100]; // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0
    protected final float[] srcPoint = new float[2];
    protected final float[] destPoint = new float[2];

    protected int width;
    protected int height;
    protected int distance;
    protected double distanceRatio;
    private final PointF pA = new PointF();
    private final PointF pB = new PointF();
    private final PointF pC = new PointF();
    private final PointF pD = new PointF();
    @NonNull
    protected int index;
    private BORDER_TYPE borderType;
    /**
     * Destination points of the entity
     * 5 points. Size of array - 10; Starting upper left corner, clockwise
     * last point is the same as first to close the circle
     * NOTE: saved as a field variable in order to avoid creating array in draw()-like methods
     */
    protected NCRect displayRect;
    protected NCRect absDisplayRect;
    protected Context context;
    protected NCRect paperDimention;
    private NCRect bookDimention;
    private boolean isNewDimention;
    private PointF origin = new PointF();
    protected NCBookView.OnPaperListener paperListener;
    private Paint cornerPaint = new Paint();
    private float cornerWidth = 40;
    private float cornerHeight = 10;



    public enum PAPER_MESSAGE {
        UPDATE_BITMAP("VIDEOINFO","",null),
        UPDATE_VIEW("UpdateView","" ,null ),
        AUDIOINFO("AUDIOINFO","",null),
        FILELIST("FILELIST","",null),
        RECSW("RECSW","",null),
        ;

        private final String key;
        private final String value;
        private Object object;
        PAPER_MESSAGE(String key, String value, Object object) {this.key = key;this.value = value;this.object = object;}
        public String key() {return this.key;}
        public String value() {return this.value;}
        public Object object() {return this.object;}
        public void setObject(Object object) {this.object = object;}
        public static PAPER_MESSAGE get(String text) {
            if (text != null) {
                for (PAPER_MESSAGE b : PAPER_MESSAGE.values()) {
                    if (text.equalsIgnoreCase(b.key)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }
    public Paper(Context context, @NonNull NCLayerDataPaper layer) {
        super(context);
        this.setup(context, layer);
    }

    public Paper(Context context, AttributeSet attrs, @NonNull NCLayerDataPaper layer) {
        super(context, attrs);
        this.setup(context, layer);
    }

    public Paper(Context context, NCLayerDataPaper layer, int i) {
        super(context);
        TAG += " " + i;
        setIndex(i);
        this.setup(context, layer);
    }

    public Paper(Context context, AttributeSet attrs, int defStyleAttr, @NonNull NCLayerDataPaper layer) {
        super(context, attrs, defStyleAttr);
        this.setup(context, layer);

    }

    public void init(){
        if(displayRect == null){
            this.displayRect = new NCRect();
            matrix.mapPoints(destPoints, srcPoints);
            updateDisplayRect(displayRect, destPoints);
        }
        if(absDisplayRect == null){
            this.absDisplayRect = new NCRect();
            matrix.mapPoints(destPoints, srcPoints);
            updateDisplayRect(absDisplayRect, destPoints);
        }
    }

    private void setup(Context context, NCLayerDataPaper layer) {
        this.layer = layer;
        this.context  = context;

        cornerPaint.setStrokeWidth(1);
        cornerPaint.setStyle(Paint.Style.FILL);
        cornerPaint.setAntiAlias(true);
        cornerPaint.setColor(Color.RED);

    }

    public Paper setPaperDimention(NCRect bookDimention, NCRect paperDimention) {
        //Log.w(TAG, "> NCN setDimention"
        //         + "\n    bookDimention: " + bookDimention
        //         + "\n   paperDimention: " + paperDimention
        //         + "\n            pivot: " + getPivotX() + ", " + getPivotY()
        //         + "\n       visibility: " + Utility.getVisibilityName(this.getVisibility())
        // );
        layer.reset();
        this.bookDimention = bookDimention;
        this.paperDimention = paperDimention;
        Utility.setRelativeLayout(this, (int) paperDimention.width(), (int) paperDimention.height(), 0, 0, 0, 0);
        this.reInit();
        getLayer().postScale((getScalefitRatio() - 1.0F));
        // layer.setScale(scalefitRatio);
        isNewDimention = true;
        updateUI();
        return this;
    }

    public Paper setOnPaperListener(NCBookView.OnPaperListener listener){
        this.paperListener = listener;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public UndoManager getUndoManager() {
        return layer.getUndoManager();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getCenterX() {
        return this.displayRect == null? 0:displayRect.center().x;
    }

    public float getCenterY() {
        return this.displayRect == null? 0:displayRect.center().y;
    }

    public PointF getCenter() {
        return this.displayRect == null? new PointF():displayRect.center();
    }

    public float left() {
        return this.displayRect == null? 0:this.displayRect.left();
    }

    public float right() {
        return this.displayRect == null? 0:this.displayRect.right();
    }

    public float top() {
        return this.displayRect == null? 0:this.displayRect.top();
    }

    public float bottom() {
        return this.displayRect == null? 0:this.displayRect.bottom();
    }

    public PointF getOrigin() {
        return this.displayRect == null? new PointF():new PointF(left(),top());
    }

    public Paper moveCenterTo(PointF moveToCenter) {
        PointF currentCenter = getCenter();
        float ratioX = (1.0F * (moveToCenter.x - currentCenter.x +origin.x)/ getPaperDisplayWidth());
        float ratioY = (1.0F * (moveToCenter.y - currentCenter.y +origin.y) / getPaperDisplayHeight());
        float ox = getX();
        float oy = getY();
        float rx = layer.getX();
        float ry = layer.getY();
        // layer.postTranslate(ratioX, ratioY);
        if (paperListener != null)
            paperListener.onMove(this, moveToCenter.x - currentCenter.x + origin.x, moveToCenter.y - currentCenter.y + origin.y, true);
        updateUI();

        //Log.e(TAG, "> NCN moveCenterTo (postTranslate) "
        //         + "\n            pivot: " + getPivotX() + ", " + getPivotY()
        //         + "\n  origin left-top: " + left() + ", " + top()
        //         + "\n      origin from: " + ox + ", " + oy
        //         + "\n           offset: " + getOffsetX() + ", " + getOffsetY()
        //         + "\n       point from: " + currentCenter.x + ", " + currentCenter.y
        //         + "\n         point to: " + moveToCenter.x + ", " + moveToCenter.y
        //         + "\n       layer from: " + rx + ", " + ry
        //         + "\n         layer to: " + layer.getX() + ", " + layer.getY()
        //         + "\n            ratio: " + ratioX + ", " + ratioY
        //         + "\nPaperDisplay (pt): " + getPaperDisplayWidth() + " x " + getPaperDisplayHeight()
        //         + "\n      displayRect: " + displayRect
        // );
        return this;
    }

    /**
     * For more info:
     * <a href="http://math.stackexchange.com/questions/190111/how-to-check-if-a-point-is-inside-a-rectangle">StackOverflow: How to check point is in rectangle</a>
     * <p>NOTE: it's easier to apply the same transformation matrix (calculated before) to the original source points, rather than
     * calculate the result points ourselves
     * @param point point
     * @return true if point (x, y) is inside the triangle
     */
    public boolean pointInLayerRect(PointF point) {

        // updateMatrix();
        // map rect vertices
        matrix.mapPoints(destPoints, srcPoints);

        pA.x = destPoints[0];
        pA.y = destPoints[1];
        pB.x = destPoints[2];
        pB.y = destPoints[3];
        pC.x = destPoints[4];
        pC.y = destPoints[5];
        pD.x = destPoints[6];
        pD.y = destPoints[7];

        return MathUtils.pointInTriangle(point, pA, pB, pC) || MathUtils.pointInTriangle(point, pA, pD, pC);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float alignX;
        float alignY;
        // 同步 scale
        //Log.d(TAG,"> NCN onDraw scale change " + Math.abs(getScaleX() - layer.getScale()));
        if (getScaleX() != layer.getScale()){  // && Math.abs(getScaleX() - layer.getScale()) > 0.01) {
            setScaleX(layer.getScale());
            setScaleY(layer.getScale());
            PointF delta = adjustOffset();
            //if (paperListener != null)
                //paperListener.onMove(this, delta.x, delta.y, false);
        }

        /**
         * {
         *  Scale X      , Skew X       , Transform X
         *  Skew Y       , Scale Y      , Transform Y
         *  Perspective 0, Perspective 1, Perspective 2
         *  }
         */
        // 更新 matrix
        matrix.reset();
        matrix.preTranslate(getMovedX(), getMovedY());
        matrix.preScale(getScaleX(), getScaleX(), getPivotX(), getPivotY());
        matrix.mapPoints(destPoints, srcPoints);
        updateDisplayRect(absDisplayRect, destPoints);

        matrix.reset();
        matrix.preTranslate(origin.x + getMovedX(), origin.y + getMovedY());
        matrix.preScale(getScaleX(), getScaleX(), getPivotX(), getPivotY());

        // matrix.preTranslate(origin.x, origin.y);
        matrix.mapPoints(destPoints, srcPoints);
        updateDisplayRect(displayRect, destPoints);

        // 同步 origin

        alignX = (getDisplayRect().width() <= getPaperDisplayWidth()) ? (getPaperDisplayWidth() - getDisplayRect().width()) / 2 + origin.x : destPoints[0];
        alignY = (getDisplayRect().height() <= getPaperDisplayHeight()) ? (getPaperDisplayHeight() - getDisplayRect().height()) / 2 + origin.y : destPoints[1];

        if (alignX != getX() || alignY != getY()) {
            // setX(alignX);
            // setY(alignY);
            //Log.e(TAG, "> NCN layer x " + layer.getX() + " " + (alignX) + " " + (alignX) / getPaperDisplayWidth());
            //Log.e(TAG, "> NCN layer y " + layer.getY() + " " + (alignY) + " " + (alignY) / getPaperDisplayHeight());
            layer.setX(alignX / getPaperDisplayWidth());
            layer.setY(alignY / getPaperDisplayHeight());
            AdditiveAnimator
                    .animate(this)
                    .setDuration(300)
                    .x(alignX)
                    .y(alignY)
                    .start();
        }

        if (isNewDimention) {
            //if (isShown())
                moveCenterTo(getPaperDisplayCenter());
            isNewDimention = false;
        }

        // 繪製角落
        if (NCManager.isDebug) drawCorner(canvas, srcPoints);
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

    private PointF getReversedXY(PointF origin) {
        PointF offset = getConvertedOffset();
        matrix.reset();
        srcPoint[0] = origin.x;
        srcPoint[1] = origin.y;
        matrix.preTranslate(-getPivotX(), -getPivotY());
        matrix.preTranslate(offset.x, offset.y);
        matrix.mapPoints(destPoint, srcPoint);
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

    private PointF getReversedOrigin(PointF center, float x, float y) {
        matrix.reset();
        srcPoint[0] = center.x;
        srcPoint[1] = center.y;
        matrix.preScale(-getScaleX(),-getScaleY());
        matrix.postTranslate(x, y);
        matrix.mapPoints(destPoint,srcPoint);
        return new PointF(destPoint[0], destPoint[1]);
    }

    public PointF getReversedPoint(PointF origin, float x, float y){
        matrix.reset();
        srcPoint[0] = x;
        srcPoint[1] = y;
        matrix.preScale(getScaleX(),getScaleY());
        if(origin!= null) matrix.postTranslate(origin.x, origin.y);
        matrix.mapPoints(destPoint,srcPoint);
        return new PointF(destPoint[0], destPoint[1]);
    }

    private void drawCorner(Canvas canvas, float[] srcPoints) {
        PointF origin = getConvertedOrign(getX(),getY());
        PointF center = getConvertedPoint(origin, getPivotX(),getPivotY());
        PointF rightBottom = getConvertedPoint(origin, getWidth(),getHeight());
        PointF leftTop = getConvertedPoint(origin, 0,0);

        float cornerReveseWidth = cornerWidth/getScaleX();
        float cornerReveseHeight = cornerHeight/getScaleX();
        float vlmx = leftTop.x;     //(0 - (alx/getScaleX()));
        float vrmx = rightBottom.x; //vlmx + getWidth()/getScaleX();                      // 無縮放
        float vtmy = leftTop.y;     //(0 - (aty/getScaleX()));
        float vbmy = rightBottom.y; //vtmy + getHeight()/getScaleX();                     // 無縮放
        float vlx = vlmx + cornerReveseWidth;       float vrx = vrmx - cornerReveseWidth;                // 無縮放
        float vty = vtmy + cornerReveseWidth;       float vby = vbmy - cornerReveseWidth;                // 無縮放
        float clx = center.x; //(vlx + vrx) / 2;
        float cty = center.y; //(vty + vby) / 2;

        //Log.e(TAG, "> NCN " + Utility.formatFixedFloat(layer.getScale(), 1, 2, false)
        //         + " |dd "   + Utility.formatFixedFloat(getVisibleWidth(), 4, 0, false) + " x " + Utility.formatFixedFloat(getVisibleHeight(), 4, 0, false)
        //         + " |ox "   + Utility.formatFixedFloat(ox, 3, 0, true)          + ", "  + Utility.formatFixedFloat(oy, 3, 0, true)
        //         + " |a0 "   + Utility.formatFixedFloat(alx, 4, 0, true)         + ", "  + Utility.formatFixedFloat(aty, 4, 0, true)
        //         + " |a2 "   + Utility.formatFixedFloat(arx, 4, 0, true)         + ", "  + Utility.formatFixedFloat(aby, 4, 0, true)
        //         + " |v0 "   + Utility.formatFixedFloat(vlx, 4, 0, true)         + ", "  + Utility.formatFixedFloat(vty, 4, 0, true)
        //         + " |v2 "   + Utility.formatFixedFloat(vrx, 4, 0, true)         + ", "  + Utility.formatFixedFloat(vby, 4, 0, true)
        //         + " |m0 "   + Utility.formatFixedFloat(vlmx, 4, 0, true)        + ", "  + Utility.formatFixedFloat(vtmy, 4, 0, true)
        //         + " |m2 "   + Utility.formatFixedFloat(vrmx, 4, 0, true)        + ", "  + Utility.formatFixedFloat(vbmy, 4, 0, true)
        // );

        cornerPaint.setColor(Color.BLUE);
        // left-top
        canvas.drawRect(
                vlx, vty,
                vlx + cornerReveseWidth, vty + cornerReveseHeight,
                cornerPaint
        );
        canvas.drawRect(
                vlx, vty,
                vlx + cornerReveseHeight, vty + cornerReveseWidth,
                cornerPaint
        );
        // right-top
        canvas.drawRect(
                vrx - cornerReveseWidth, vty,
                vrx, vty + cornerReveseHeight,
                cornerPaint
        );
        canvas.drawRect(
                vrx - cornerReveseHeight, vty,
                vrx, vty + cornerReveseWidth,
                cornerPaint
        );
        // center
        canvas.drawRect(
                clx - cornerReveseWidth, cty - (cornerReveseHeight/2),
                clx + cornerReveseWidth, cty + (cornerReveseHeight/2),
                cornerPaint
        );
        canvas.drawRect(
                clx - (cornerReveseHeight/2), cty - cornerReveseWidth,
                clx + (cornerReveseHeight/2), cty + cornerReveseWidth,
                cornerPaint
        );
        // right-bottom
        canvas.drawRect(
                vrx - cornerReveseWidth, vby - cornerReveseHeight,
                vrx, vby,
                cornerPaint
        );
        canvas.drawRect(
                vrx - cornerReveseHeight, vby - cornerReveseWidth,
                vrx, vby,
                cornerPaint
        );
        // left-bottom
        canvas.drawRect(
                vlx, vby - cornerReveseHeight,
                vlx + cornerReveseWidth, vby,
                cornerPaint
        );
        canvas.drawRect(
                vlx, vby - cornerReveseWidth,
                vlx + cornerReveseHeight, vby,
                cornerPaint
        );
    }

    public float getMovedX() {
        return layer.getX() * getPaperDisplayWidth();
    }

    public float getMovedY() {
        return layer.getY() * getPaperDisplayHeight();
    }

    public PointF getDisplayCenterRatio() {
        PointF origin = getConvertedOrign(getX(), getY());
        PointF center = getConvertedPoint(origin, getPivotX(), getPivotY());
        //Log.e(TAG,"> NCN center from " + )
        return new PointF(center.x / getWidth(), center.y / getHeight());
    }

    public void setDisplayCenterRatio(float ratioX, float ratioY) {
        PointF origin00 = new PointF(getX(), getY());
        PointF origin01 = getConvertedOrign(origin00.x, origin00.y);
        PointF center21 = new PointF(ratioX * getWidth(), ratioY * getHeight());
        PointF center22 = getReversedPoint(origin01, center21.x, center21.y);
        PointF center23 = getReversedOrigin(center21, getPivotX(), getPivotY());
        PointF center24 = getReversedXY(center23);
        layer.setX(center24.x / getPaperDisplayWidth());
        layer.setY(center24.y / getPaperDisplayHeight());
        updateUI();
    }

    public float getMovedFromOriginX() {
        return layer.getX() * getPaperDisplayWidth() + origin.x;
    }

    public float getMovedFromOriginY() {
        return layer.getY() * getPaperDisplayHeight() + origin.y;
    }

    public float getCenterVectorX() {
        return this.getMovedX() + (getPaperDisplayWidth() * scalefitRatio / 2);
    }

    public float getCenterVectorY() {
        return this.getMovedY() + (getPaperDisplayHeight() * scalefitRatio / 2);
    }

    public float getCX() {
        float cx = this.getCenterVectorX() + origin.x;
        return cx;
    }

    public float getCY() {
        float cy = this.getCenterVectorY() + origin.y;
        return cy;
    }

    public PointF getC() {
        return new PointF(this.getCX(), this.getCY());
    }

    public float getOffsetX() {
        return getPivotX() * (getScaleX() - 1);
    }

    public float getOffsetY() {
        return getPivotY() * (getScaleX() - 1);
    }

    private PointF adjustOffset() {
        PointF old = new PointF(origin.x, origin.y);
        origin.set(getOffsetX(), getOffsetY());
        //Log.e(TAG,"> NCN adjustOffset from " + old  + " to " + origin);
        old.set(origin.x - old.x, origin.y - old.y);
        return old;
    }

    protected PointF getScaledPoint(PointF point, float pivotX, float pivotY, float scale){
        PointF scalePoint = new PointF();
        if (scale >= 1){
            scalePoint.x = point.x + (point.x - pivotX)*(scale - 1);
            scalePoint.y = point.y + (point.y - pivotY)*(scale - 1);
        }else{
            scalePoint.x = point.x - (point.x - pivotX)*(1 - scale);
            scalePoint.y = point.y - (point.y - pivotY)*(1 - scale);
        }
        return scalePoint;
    }

    protected void updateDisplayRect(NCRect rect, float[] destPoints) {
        rect.set(
                // x: 0 6 - 2 4   y: 1 3 - 5 7
                (int) Utility.min(destPoints[0],destPoints[2],destPoints[4],destPoints[6]),
                (int) Utility.min(destPoints[1],destPoints[3],destPoints[5],destPoints[7]),
                (int) Utility.max(destPoints[0],destPoints[2],destPoints[4],destPoints[6]),
                (int) Utility.max(destPoints[1],destPoints[3],destPoints[5],destPoints[7])
        );
    }

    public NCLayerDataPaper getLayer() {
        return layer;
    }

    public NCRect getDisplayRect() {
        return displayRect;
    }

    public NCRect getAbsDisplayRect() {
        return absDisplayRect;
    }

    protected abstract void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint);

    public void destroy(){
        //Log.i(TAG, "> destroy");
        release();
    }

    public void release() {
        onPause();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            release();
        } finally {
            //noinspection ThrowFromFinallyBlock
            super.finalize();
        }
    }

    public boolean hasDimention() {
        return getPaperWidth() != 0 && getPaperHeight() != 0;
    }


    public void updateUI() {
        invalidate();
    }

    public float getPaperWidth() {
        return (this.paperDimention != null) ? this.paperDimention.width() : 0;
    }

    public float getPaperHeight() {
        return (this.paperDimention != null) ? this.paperDimention.height() : 0;
    }

    public float getPaperDisplayWidth() {
        return (this.bookDimention != null) ? this.bookDimention.width() : 0;
    }

    public float getPaperDisplayHeight() {
        return (this.bookDimention != null) ? this.bookDimention.height() : 0;
    }

    /**
     * Paper 不會跟螢幕尺寸一樣，因此有一個初始比例，將 paper 等比例擴展到可視畫面
     *
     * @return 返回初始比例
     */
    public float getScalefitRatio() {
        return scalefitRatio;
    }

    /**
     * Paper 不會跟螢幕尺寸一樣，因此有一個初始比例，將 paper 等比例擴展到可視畫面
     *
     * @return 返回可視畫面寬度
     */
    public float getVisibleWidth() {
        return getWidth() * getScalefitRatio();
    }

    /**
     * Paper 不會跟螢幕尺寸一樣，因此有一個初始比例，將 paper 等比例擴展到可視畫面
     *
     * @return 返回可視畫面高度
     */

    public float getVisibleHeight() {
        return getHeight() * getScalefitRatio();
    }

    private PointF getPaperDisplayCenter() {
        return (this.bookDimention != null) ? this.bookDimention.absCenter() : new PointF();
    }

    public PointF getPaperDisplayOrigin() {
        return origin;
    }

    public abstract int getDistance();

    public abstract double getDistanceRatio();

    public abstract void reInit();

    public abstract void onResume();
    public abstract void onPause();
    public abstract void reload();
    public abstract void save();


    public NCRect getPaperDimention() {
        return paperDimention;
    }

    public abstract void addSticker(Sticker sticker);
    public abstract void updateSticker(Sticker sticker);

}
