package com.kcchen.nativecanvas.sticker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MotionEvent;

import com.kcchen.nativecanvas.enums.BORDER_TYPE;
import com.kcchen.nativecanvas.model.NCLayerData;
import com.kcchen.nativecanvas.utils.MathUtils;
import com.kcchen.nativecanvas.utils.Utility;
import com.kcchen.nativecanvas.view.NCManager;
import com.kcchen.nativecanvas.view.NCRect;
import com.kcchen.nativecanvas.view.NCStickerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@SuppressWarnings({"WeakerAccess"})
public abstract class Sticker {
    protected static final String TAG = Sticker.class.getSimpleName();

    public static final String STICKER_WIDTH        = "StickerWidth";
    public static final String STICKER_HEIGHT       = "StickerHeight";
    public static final String STICKER_BORDER_TYPE  = "StickerBorderType";
    public static final String STICKER_SCALE_LIMIT  = "StickerScaleLimit";
    public static final String STICKER_DIMENTION    = "StickerDimention";
    public static final String STICKER_UUID         = "StickerUuid";

    /**
     * data
     */
    @NonNull
    protected final NCLayerData layer;
    /**
     * transformation matrix for the entity
     */
    protected final Matrix matrix = new Matrix();
    protected float scalefitRatio;
    /**
     * width of canvas the entity is drawn in
     */
    @IntRange(from = 0)
    protected int stickerDisplayWidth;
    /**
     * height of canvas the entity is drawn in
     */
    @IntRange(from = 0)
    protected int stickerDisplayHeight;
    /**
     * Initial points of the entity
     * @see #destPoints
     */
    protected final float[] srcPoints = new float[16];  // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0
    protected final float[] srcPoint = new float[2];
    protected int rotateLength;
    protected int width;
    protected int height;
    protected int distance;
    protected double distanceRatio;
    private final PointF pA = new PointF();
    private final PointF pB = new PointF();
    private final PointF pC = new PointF();
    private final PointF pD = new PointF();
    private RectF circleRect;
    @NonNull
    private Paint borderPaint;
    private Paint bitmapPaint;
    private Paint rectPaint;
    private String id = "";
    private BORDER_TYPE borderType;
    /**
     * Destination points of the entity
     * 5 points. Size of array - 10; Starting upper left corner, clockwise
     * last point is the same as first to close the circle
     * NOTE: saved as a field variable in order to avoid creating array in draw()-like methods
     */
    protected final float[] destPoints = new float[16]; // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0
    protected final float[] destPoint = new float[2];
    private boolean isSelected;
    protected NCRect displayRect;
    protected Point origin;
    private NCRect dimention;
    private NCLayerData.SCALE_LIMIT paperLimit;
    private int index;
    private boolean isAdded = false;
    protected NCStickerView stickerView;
    protected Activity activity;
    public String uuid;
    protected ArrayList<Integer> stickerHashs = new ArrayList<Integer>();

    public Sticker(@NonNull NCLayerData layer,
                   @IntRange(from = 1) int stickerDisplayWidth,
                   @IntRange(from = 1) int stickerDisplayHeight) {
        this.layer = layer;
        setStickerDisplayWidth(stickerDisplayWidth);
        setStickerDisplayHeight(stickerDisplayHeight);
        this.circleRect = new RectF();
        this.origin = new Point();
        //Log.i(TAG,"> NCN Sticker stickerDisplayWidth:" + stickerDisplayWidth + " stickerDisplayHeight:" + stickerDisplayHeight);
    }

    public Sticker(@NonNull NCLayerData layer) {
        this.layer = layer;
        this.circleRect = new RectF();
        this.origin = new Point();
        //Log.i(TAG,"> NCN Sticker");
    }

    /**
     * call super.init() after init
     */
    public void init(){
        if(displayRect == null){
            this.displayRect = new NCRect();
            matrix.mapPoints(destPoints, srcPoints);
            updateDisplayRect(destPoints);
        }
        if(borderPaint == null){
            borderPaint = new Paint();
            borderPaint.setStyle(Paint.Style.FILL);
            borderPaint.setAntiAlias(true);
            borderPaint.setDither(true);
            borderPaint.setFilterBitmap(true);
        }
        if(bitmapPaint == null){
            bitmapPaint = new Paint();
            bitmapPaint.setAntiAlias(true);
            bitmapPaint.setDither(true);
            bitmapPaint.setFilterBitmap(true);
        }
        if(rectPaint == null){
            rectPaint = new Paint();
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setColor(0x99FF00FF);
            rectPaint.setStrokeWidth(5.0f);
            rectPaint.setAntiAlias(true);
            rectPaint.setDither(true);
            rectPaint.setFilterBitmap(true);
        }

        if(false && NCManager.isDebug){
            Random r = new Random();
            float[] testData = new float[1000000];
            for(int i = 0; i < testData.length; i++){
                testData[i] = r.nextFloat();
            }
            long t1, t2;
            t1 = System.currentTimeMillis();
            Utility.minx(testData);
            t2 = System.currentTimeMillis();
            //Log.i(TAG,"> " +t2+ "   "+(t2-t1));
            t1 = System.currentTimeMillis();
            Utility.min(testData);
            t2 = System.currentTimeMillis();
            //Log.w(TAG,"> " +t2+ "   "+(t2-t1));
        }
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public Sticker setDimention(NCRect dimention) {
        //Log.w(TAG,"> NCN setDimention"
        //        + "\n            dimention: " + dimention
        //        + "\n  stickerDisplayWidth: " + stickerDisplayWidth
        //        + "\n stickerDisplayHeight: " + stickerDisplayHeight
        //        + "\n               origin: " + origin.x + "x" + origin.y
        //);
        if(isUpdate()){
            this.dimention = dimention;
            setStickerDisplayWidth((int)dimention.width());
            setStickerDisplayHeight((int)dimention.height());
            this.origin.set((int)dimention.left(),(int)dimention.top());
            this.reInit();
            if(hasRotateControl()) updateRotateControl();
        }
        return this;
    }

    public void setPaperLimit(NCLayerData.SCALE_LIMIT paperLimit) {
        this.paperLimit = paperLimit;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        this.isAdded = added;
    }

    public void setStickerView(NCStickerView stickerView) {
        this.stickerView = stickerView;
        this.reInit();
    }

    public String getID() {
        return id;
    }

    public Sticker setID(String id) {
        this.id = id;
        return this;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public Sticker setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
        return this;
    }

    public Sticker setStickerScale(float scale) {
        this.layer.setScale(scale);
        return this;
    }

    public float getStickerScale() {
        return this.layer.getScale();
    }

    @NonNull
    public NCLayerData getLayer() {
        return layer;
    }

    /**
     * 位移原點是原始計算出的 center，再以 delta 去加減位移
     * @return
     */
    public float getMovedX() {
        return layer.getX() * stickerDisplayWidth;
    }

    public float getMovedY() {
        return layer.getY() * stickerDisplayHeight;
    }

    public float getCenterX() {
        return this.getMovedX() + (getStickerWidth() * scalefitRatio / 2);
    }

    public float getCenterY() {
        return this.getMovedY() + (getStickerHeight() * scalefitRatio / 2);
    }

    public PointF getCenter() {
        return new PointF(this.getCenterX(), this.getCenterY());
    }

    public PointF getOrigin() {
        return new PointF(this.left(), this.top());
    }

    public float left() {
        return this.displayRect == null? 0:this.displayRect.left() + origin.x;
    }

    public float right() {
        return this.displayRect == null? 0:this.displayRect.right() + origin.x;
    }

    public float top() {
        return this.displayRect == null? 0:this.displayRect.top() + origin.y;
    }

    public float bottom() {
        return this.displayRect == null? 0:this.displayRect.bottom() + origin.y;
    }

    public float centerX() {
        return this.displayRect == null? 0:this.displayRect.center().x + origin.x;
    }

    public float centerY() {
        return this.displayRect == null? 0:this.displayRect.center().y + origin.y;
    }

    public Point origin() {
        return origin;
    }

    public NCRect getDisplayRect() {
        return displayRect;
    }

    public Sticker setStickerDisplayWidth(int stickerDisplayWidth) {
        this.stickerDisplayWidth = stickerDisplayWidth;
        return this;
    }

    public Sticker setStickerDisplayHeight(int stickerDisplayHeight) {
        this.stickerDisplayHeight = stickerDisplayHeight;
        return this;
    }

    public int getStickerDisplayWidth() {
        return stickerDisplayWidth;
    }

    public int getStickerDisplayHeight() {
        return stickerDisplayHeight;
    }

    public float getScalefitRatio() {
        return scalefitRatio;
    }

    /**
     * S - scale matrix, R - rotate matrix, T - translate matrix,
     * L - result transformation matrix
     * <p>
     * The correct order of applying transformations is : L = S * R * T
     * <p>
     * See more info: <a href="http://gamedev.stackexchange.com/questions/29260/transform-matrix-multiplication-order">Game Dev: Transform Matrix multiplication order</a>
     * <p>
     * Preconcat works like M` = M * S, so we apply preScale -> preRotate -> preTranslate
     * the result will be the same: L = S * R * T
     * <p>
     * NOTE: postconcat (postScale, etc.) works the other way : M` = S * M, in order to use it
     * we'd need to reverse the order of applying
     * transformations : post holy scale ->  postTranslate -> postRotate -> postScale
     */
    protected void updateMatrix() {
        matrix.reset();

        float topLeftX = getMovedX();
        float topLeftY = getMovedY();

        float centerX = getCenterX();
        float centerY = getCenterY();

        // calculate params
        float rotationInDegree = layer.getRotationInDegrees();
        float scaleX = layer.getScale();
        float scaleY = scaleX;
        if (layer.isFlipped()) {
            // flip (by X-coordinate) if needed
            rotationInDegree *= -1.0F;
            scaleX *= -1.0F;
        }

        // applying transformations : L = S * R * T

        // scale
        matrix.preScale(scaleX, scaleY, centerX, centerY);

        // rotate
        matrix.preRotate(rotationInDegree, centerX, centerY);

        // translate
        matrix.preTranslate(topLeftX, topLeftY);

        // applying holy scale - S`, the result will be : L = S * R * T * S`
        matrix.preScale(scalefitRatio, scalefitRatio);
        //Log.e(TAG, "> NCN updateMatrix " + matrix);
    }

    public Sticker moveToCanvasCenter() {
        moveCenterTo(new PointF(dimention.left() + (stickerDisplayWidth * 0.5F), dimention.top() + (stickerDisplayHeight * 0.5F)));
        return this;
    }

    public Sticker moveCenterTo(PointF moveToCenter) {
        PointF currentCenter = getCenter();
        //Log.e(TAG,"> NCN moveCenterTo "
        //        + "\n from: " + currentCenter
        //        + "\n   to: " + moveToCenter
        //);
        layer.postTranslate(1.0F * (moveToCenter.x - currentCenter.x) / stickerDisplayWidth,
                1.0F * (moveToCenter.y - currentCenter.y) / stickerDisplayHeight);
        return this;
    }

    public Sticker moveOriginTo(float ratioX, float ratioY) {
        PointF currentOrigin = getOrigin();
        //Log.e(TAG,"> NCN moveOriginTo " + currentOrigin);
        layer.postTranslate(1.0F * (ratioX - (currentOrigin.x / stickerDisplayWidth)),
                1.0F * (ratioY - (currentOrigin.y / stickerDisplayHeight)));
        return this;
    }

    public Sticker moveOriginTo(PointF moveToOrigin) {
        PointF currentOrigin = getOrigin();
        layer.postTranslate(1.0F * (moveToOrigin.x - currentOrigin.x) / stickerDisplayWidth,
                1.0F * (moveToOrigin.y - currentOrigin.y) / stickerDisplayHeight);
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

        updateMatrix();
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

    public boolean pointInControlButton(PointF point) {
        updateMatrix();
        // map rect vertices

        if (this.borderType.getWidth() != null) {
            matrix.mapPoints(destPoints, srcPoints);
            float w = translateWidth(this.borderType.getWidth(), 0) * 2;
            //int w = this.borderType.getWidth() * 2;
            return getCircleRect(w, destPoints[0], destPoints[1]).contains(point.x, point.y)
                    || getCircleRect(w, destPoints[2], destPoints[3]).contains(point.x, point.y)
                    || getCircleRect(w, destPoints[4], destPoints[5]).contains(point.x, point.y)
                    || getCircleRect(w, destPoints[6], destPoints[7]).contains(point.x, point.y)
                    ;
        }
        return false;
    }

    public boolean pointInRotateButton(PointF point) {
         updateMatrix();

        if (this.borderType.getRotate() != null) {
            float w = translateWidth(this.borderType.getWidth(), 0) * 2;
            //int w = this.borderType.getWidth() * 2;
            srcPoints[13] = - translateWidth(rotateLength, layer.getScale());
            matrix.mapPoints(destPoints, srcPoints);
            return getCircleRect(w, destPoints[12], destPoints[13]).contains(point.x, point.y);
        }
        return false;
    }

    public boolean setRotationInDegrees(MotionEvent event) {
        PointF center = getCenter();
        PointF touch = getMapPoint(event.getX(),event.getY());
        if(event == null || center == null) return false;

        float degree = (float) (Math.atan2(center.y - touch.y, center.x - touch.x) * 180 / Math.PI) + 270;
        getLayer().setRotationInDegrees(degree);
        return true;
    }

    public boolean setScale(MotionEvent event) {
        PointF center = getCenter();
        PointF touch = getMapPoint(event.getX(),event.getY());
        double distance = Math.sqrt(Math.pow((touch.x - center.x), 2) + Math.pow((touch.y - center.y), 2));
        if(distance != 0){
            float scale = (float) (distance / getDistanceRatio());
            //Log.d(TAG,"> NCN setScale touch:" + event.getX() + ", " + event.getY() + " touch:" + touch + " center:" + center+ " distance:" + distance);
            getLayer().setScale(scale);
            return true;
        }
        return false;
    }

    private PointF getMapPoint(float x, float y) {
        // 更新 matrix
        matrix.reset();
        matrix.preScale(stickerView.getScaleX(), stickerView.getScaleY(), getCenterX(), getCenterY());
        srcPoint[0] = x;
        srcPoint[1] = y;
        matrix.mapPoints(destPoint, srcPoint);
        return new PointF(destPoint[0],destPoint[1]);
    }


    /**
     * http://judepereira.com/blog/calculate-the-real-scale-factor-and-the-angle-of-rotation-from-an-android-matrix/
     *
     * @param canvas       Canvas to draw
     * @param drawingPaint Paint to use during drawing
     */
    public final void draw(@NonNull Canvas canvas, @Nullable Paint drawingPaint) {
        updateMatrix();
        canvas.save();

        if (isSelected()) {
            // get alpha from drawingPaint
            int storedAlpha = borderPaint.getAlpha();
            if (drawingPaint != null) {
                borderPaint.setAlpha(drawingPaint.getAlpha());
            }
            matrix.mapPoints(destPoints, srcPoints);
            updateDisplayRect(destPoints);


            if(NCManager.isDebug) canvas.drawRect(displayRect.rect(), rectPaint);

            drawContent(canvas, drawingPaint);

            drawBorderControl(canvas);

            // restore border alpha
            borderPaint.setAlpha(storedAlpha);
        }


        canvas.restore();
    }

    protected void updateDisplayRect(float[] destPoints) {
        // 0,2,4,6
        // 1,3,5,7
        this.displayRect.set(
                (int) Utility.min(destPoints[0],destPoints[2],destPoints[4],destPoints[6]),
                (int) Utility.min(destPoints[1],destPoints[3],destPoints[5],destPoints[7]),
                (int) Utility.max(destPoints[0],destPoints[2],destPoints[4],destPoints[6]),
                (int) Utility.max(destPoints[1],destPoints[3],destPoints[5],destPoints[7])
        );
    }

    private void drawBorderControl(Canvas canvas) {
        float w = 0;
        if (this.borderType == null) return;
        if (this.borderType.getWidth() != null) {
            w = translateWidth(this.borderType.getWidth(), 0);
        }
        switch (this.borderType) {
            case CIRCLE:
                // Draw outline, Horizontal 0,1 - 2,3  4,5 - 6,7   Vertical 2,3 - 4,5  6,7 - 0,1
                canvas.drawLines(destPoints, 0, 8, borderPaint);
                canvas.drawLines(destPoints, 2, 8, borderPaint);

                // Draw scale circle control point
                if (this.borderType.getWidth() != null) {
                    canvas.drawArc(getCircleRect(w, destPoints[0], destPoints[1]), 0, 360, false, borderPaint);
                    canvas.drawArc(getCircleRect(w, destPoints[2], destPoints[3]), 0, 360, false, borderPaint);
                    canvas.drawArc(getCircleRect(w, destPoints[4], destPoints[5]), 0, 360, false, borderPaint);
                    canvas.drawArc(getCircleRect(w, destPoints[6], destPoints[7]), 0, 360, false, borderPaint);
                }

                // Draw rotate line and control point
                if (this.borderType.getRotate() != null) {
                    srcPoints[13] = - translateWidth(rotateLength, layer.getScale());
                    matrix.mapPoints(destPoints, srcPoints);
                    canvas.drawLines(destPoints, 10, 4, borderPaint);
                    canvas.drawArc(getCircleRect(w, destPoints[12], destPoints[13]), 0, 360, false, borderPaint);
                }
                break;
            case RECTANGLE:
                // Horizontal 0,1 - 2,3  4,5 - 6,7
                canvas.drawLines(destPoints, 0, 8, borderPaint);
                // Vertical 2,3 - 4,5  6,7 - 0,1
                canvas.drawLines(destPoints, 2, 8, borderPaint);
                break;
            case IMAGE:
                // Horizontal 0,1 - 2,3  4,5 - 6,7
                canvas.drawLines(destPoints, 0, 8, borderPaint);
                // Vertical 2,3 - 4,5  6,7 - 0,1
                canvas.drawLines(destPoints, 2, 8, borderPaint);
                break;
            case NONE:
                break;
            default:
                break;
        }
    }

    private RectF getCircleRect(float width, float x, float y) {
        circleRect.set(x - width, y - width, x + width, y + width);
        return circleRect;
    }

    public void setBorderPaint(@NonNull Paint borderPaint) {
        this.borderPaint = borderPaint;
        this.borderPaint.setStyle(Paint.Style.FILL);//設置畫筆類型為填充
        this.borderPaint.setDither(true);
        this.borderPaint.setAntiAlias(true);
        this.borderPaint.setFilterBitmap(true);
    }

    public Sticker setBorderType(BORDER_TYPE borderType) {
        this.borderType = borderType;
        if (this.borderType == null) return null;
        switch (this.borderType) {
            case CIRCLE:
                updateRotateControl();
                break;
            case RECTANGLE:
                break;
            case IMAGE:
                break;
            case NONE:
                break;
            default:
                break;
        }
        return this;
    }

    public boolean hasRotateControl() {
        return borderType != null && borderType == BORDER_TYPE.CIRCLE;
    }

    public void updateRotateControl() {
        if(this.borderType.getRotate() != null){
            this.rotateLength = this.borderType.getRotate().intValue();
            srcPoints[13] = - translateWidth(rotateLength, layer.getScale());
        }
    }

    public float translateWidth(float width, float scale) {
        if(scale != 0) width /= scale;
        float translated = (stickerView != null) ? width / stickerView.getScaleX() : 0;
        //Log.i(TAG, "> translateWidth "
        //        + "\n width:" + width
        //        + "\n scale:" + scale
        //        + "\n translated:" + translated
        //);

        return translated;
    }

    public Bitmap getConvertedBitmap(Bitmap original, float degrees, float scale) {
        //Log.i(TAG, "> getConvertedBitmap "
        //        + "\n original:" + (original == null ? "" : original.getWidth() + " x " + original.getHeight())
        //        + "\n degrees:" + degrees
        //        + "\n scale:" + scale
        //);
        int width = original.getWidth();
        int height = original.getHeight();

        Rect srcR = new Rect(0, 0, width, height);
        RectF dstR = new RectF(0, 0, width, height);
        RectF deviceR = new RectF();

        matrix.reset();
        matrix.preRotate(degrees);
        matrix.preScale(scale * scalefitRatio, scale * scalefitRatio);
        matrix.mapRect(deviceR, dstR);

        int neww = Math.round(deviceR.width());
        int newh = Math.round(deviceR.height());

        Bitmap rotatedBitmap = Bitmap.createBitmap(neww, newh, Bitmap.Config.ARGB_8888);
        rotatedBitmap.eraseColor(Color.TRANSPARENT);

        Canvas canvas = new Canvas(rotatedBitmap);
        canvas.translate(-deviceR.left, -deviceR.top);
        canvas.concat(matrix);
        canvas.drawBitmap(original, srcR, dstR, bitmapPaint);
        canvas.setBitmap(null);

        return rotatedBitmap;
    }

    public void destroy(){
        this.circleRect = null;
        this.borderPaint = null;
        this.id = null;
        this.borderType = null;
    }

    public void release() {
        // free resources here
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

    public JSONObject exportData() {

        JSONObject data = new JSONObject();
        try {
            data.put(STICKER_WIDTH, getStickerDisplayWidth());
            data.put(STICKER_HEIGHT, getStickerDisplayHeight());
            data.put(STICKER_BORDER_TYPE, borderType == null?new JSONObject():borderType.exportData());
            data.put(STICKER_SCALE_LIMIT, paperLimit == null?new JSONObject():paperLimit.exportData());
            data.put(STICKER_DIMENTION, dimention == null?new JSONObject():dimention.exportData());
            data.put(STICKER_UUID, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean updateData(JSONObject data) {
        boolean isImported = false;
        try {
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        try {
            setStickerDisplayWidth(data.optInt(STICKER_WIDTH));
            setStickerDisplayHeight(data.optInt(STICKER_HEIGHT));
            JSONObject borderData = data.optJSONObject(STICKER_BORDER_TYPE);
            if(borderData != null){
                BORDER_TYPE type = BORDER_TYPE.get(borderData.optString(BORDER_TYPE.BORDER_TYPE_TYPE));
                if(type != null){
                    type.importData(borderData);
                    setBorderType(type);
                }
            }
            JSONObject scaleLimitData = data.optJSONObject(STICKER_SCALE_LIMIT);
            if(scaleLimitData != null){
                NCLayerData.SCALE_LIMIT limit = NCLayerData.SCALE_LIMIT.get(scaleLimitData.optString(NCLayerData.SCALE_LIMIT.SCALE_LIMIT_TYPE));
                if(limit != null){
                    limit.importData(scaleLimitData);
                    setPaperLimit(limit);
                }
            }
            JSONObject dimemtionData = data.optJSONObject(STICKER_DIMENTION);
            if(dimemtionData != null && dimemtionData.length() != 0){
                NCRect rect = new NCRect(dimemtionData);
                setDimention(rect);
            }
            setUuid(data.optString(STICKER_UUID));
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    public List<String> getProperties(){
        List<String> properties = new ArrayList<String>();
        properties.add(STICKER_UUID + ":" + getUuid());
        properties.add(STICKER_WIDTH + ":" + getStickerDisplayWidth());
        properties.add(STICKER_HEIGHT + ":" + getStickerDisplayHeight());
        properties.add(STICKER_BORDER_TYPE + ":" + (borderType == null?new JSONObject():borderType.exportData()));
        properties.add(STICKER_SCALE_LIMIT + ":" + (paperLimit == null?new JSONObject():paperLimit.exportData()));
        properties.add(STICKER_DIMENTION + ":" + (dimention == null?new JSONObject():dimention.exportData()));
        return properties;
    }

//    @Override
//    public int hashCode() {
//        return getStickerDisplayWidth() + getStickerDisplayHeight();
//    }

    @Override
    public String toString() {
        return "{"
                + "\n" + TextUtils.join(",\n", getProperties())
                + "\n}"
                ;
    }

    protected abstract void drawContent(@NonNull Canvas canvas, @Nullable Paint drawingPaint);

    public abstract int getStickerWidth();

    public abstract int getStickerHeight();

    public abstract int getDistance();

    public abstract void onResume();

    public abstract void onPause();
    public abstract Bitmap getBitmap();
    public abstract PointF getBitmapPosition();
    public abstract void finalizeBitmap();
    public abstract void reloadBitmap();

    public abstract double getDistanceRatio();

    public abstract void reInit();
    public abstract void setDebug();

    public boolean isUpdate() {
        String last = stickerHashs.size() > 1 ? String.valueOf(stickerHashs.get(stickerHashs.size() - 2)) : "null";
        String current = stickerHashs.size() > 0 ? String.valueOf(stickerHashs.get(stickerHashs.size() - 1)) : "null";
        boolean result = !last.equals(current);

        //Log.d(TAG, "> isUpdate " + getUuid() + " " + result + "  " + last + " - " + current);

        return result;
    }

    public boolean update(JSONObject data) {
        //Log.i(TAG, "> update " + stickerView.isShown());
        stickerHashs.add(Utility.getJsonHash(data));
        boolean result = isUpdate();
        if (result) finalizeBitmap();
        return result;
    }
}
