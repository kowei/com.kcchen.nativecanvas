package com.kcchen.nativecanvas.view;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

import com.kcchen.nativecanvas.NativeCanvas;
import com.kcchen.nativecanvas.enums.MOVE_STATUS;
import com.kcchen.nativecanvas.enums.PEN_TYPE;
import com.kcchen.nativecanvas.model.NCLayerData;
import com.kcchen.nativecanvas.model.NCLayerDataPaper;
import com.kcchen.nativecanvas.model.PenpalBook;
import com.kcchen.nativecanvas.model.PenpalLibrary;
import com.kcchen.nativecanvas.multitouch.MoveGestureDetector;
import com.kcchen.nativecanvas.multitouch.RotateGestureDetector;
import com.kcchen.nativecanvas.multitouch.TapGestureDetector;
import com.kcchen.nativecanvas.paper.DrawingPaper;
import com.kcchen.nativecanvas.paper.Paper;
import com.kcchen.nativecanvas.sticker.Sticker;
import com.kcchen.nativecanvas.undo.UndoManager;
import com.kcchen.nativecanvas.utils.Utility;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by kowei on 2017/12/20.
 */

public class NCBookView extends RelativeLayout {
    private static final String TAG = NCBookView.class.getSimpleName();
    public static final int EVENT_KEEP_ACTION = -1;

    private Activity context;
    private ScaleGestureDetector scaleGestureDetector;
    private RotateGestureDetector rotateGestureDetector;
    private MoveGestureDetector moveGestureDetector;
    private TapGestureDetector tapGestureDetector;
    private List<Paper> papers = new ArrayList<Paper>();
    private int cachePage;
    private Paper selectedPaper;
    private OnPaperListener paperListener;
    private NCRect bookDimention;
    private NCRect paperDimention;
    protected final float[] srcPoint = new float[2];
    protected final float[] destPoint = new float[2];
    private Matrix matrix = new Matrix();
    private MOVE_STATUS moveStatus;
    private boolean isMoving = false;
    private Handler handler = null;
    private HandlerThread thread;
    private NCStickerView stickerView;
    private boolean isViewMode;
    private PEN_TYPE penType;
    private int paintColor = -1;
    private float paintWidth = -1;
    private int paintAlpha = -1;
    private UndoManager.OnUndoListener undoListener;
    private boolean isTouch = false;

    public void setViewMode(boolean viewMode) {
        this.isViewMode = viewMode;
    }


    private Runnable unlockScale = new Runnable() {
        @Override
        public void run() {
            //Log.d(TAG,"> NCN unlock scale");
            isMoving = false;
        }
    };

    private final OnTouchListener onTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //og.i(TAG,"> NCN book view on touch stickerview:" + stickerView.isShown());
            if(!isTouch) return false;

            if(stickerView.isShown()) return false;

            boolean isConsume = false;
            if(!isMoving) {
                isConsume |= scaleGestureDetector.onTouchEvent(event);
            }
            //if(!tapGestureDetector.onTouchEvent(event)){
                isConsume |= tapGestureDetector.onTouchEvent(event);
                isConsume |= moveGestureDetector.onTouchEvent(event);
            //}
            return isConsume;
        }
    };

    private OnPaperListener paperChangeListener = new OnPaperListener() {
        @Override
        public void onReady(Paper paper) {

        }

        @Override
        public void onDimention(int width, int height) {

        }

        @Override
        public void onDimention(NCRect dimention) {

        }

        @Override
        public void onMove(Paper paper, float deltaX, float deltaY, boolean isMove) {
            handleMoveTranslate(paper, new PointF(deltaX, deltaY), isMove);
        }

        @Override
        public void onThumbnailUpdate() {

        }
    };

    public NCBookView(Activity context) {
        super(context);
        this.init(context);
        thread = new HandlerThread(TAG);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
        this.handler = new Handler(thread.getLooper());
    }

    private void init(@NonNull Activity context) {
        this.context = context;

        if(this.scaleGestureDetector  == null) this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        if(this.moveGestureDetector   == null) this.moveGestureDetector  = new MoveGestureDetector(context, new MoveListener());
        if(this.tapGestureDetector    == null) {
            this.tapGestureDetector    = new TapGestureDetector(   context, new TapsListener()  );
            this.tapGestureDetector.setIsLongpressEnabled(false);
        }

        setOnTouchListener(onTouchListener);
        updateUI();
    }

    public NCBookView createPaper() {
        if (this.papers == null) {
            this.papers = new ArrayList<Paper>();
            addPaper(0);
            this.selectedPaper = this.papers.get(0);
            showPaper(0);
        }
        return this;
    }

    public NCBookView createPapers(int cachePages) {
        if (this.papers == null) {
            this.cachePage = cachePages;
            this.papers = new ArrayList<Paper>();

            for (int i = 0; i < cachePages + 1; i++) {
                addPaper(i);
            }

            this.selectedPaper = this.papers.get(0);
            showPaper(0);
        }
        return this;
    }

    private boolean withinPapers(int index){
        return 0 <= index && index < papers.size();
    }

    private void addPaper(int index){
        //Log.i(TAG,"> addPaper " + index);
        adjustPapers(index);
        loadPaper(index);
    }

    private void loadPaper(int index) {
        //Log.i(TAG,"> loadPaper " + index);
        if(withinPapers(index)){
            NCLayerDataPaper layer = new NCLayerDataPaper(NCLayerData.SCALE_LIMIT.PAPER_DRAWING);
            layer.setStickerView(stickerView);

            DrawingPaper paper = new DrawingPaper(this.context, layer, index);
            selectedPaper = paper;
            paper.getUndoManager().setOnUndoListener(this.undoListener);
            paper.readProfile();
             attachPaper(index, paper);
        }else{
            Log.e(TAG,"> NCN loadPaper out bound");
        }
    }

    public NCBookView attachPaper(int index, @NonNull Paper paper) {
        paper.setVisibility(View.INVISIBLE);
        paper.setOnPaperListener(paperChangeListener);
        papers.set(index, paper);
        addPaperView(paper);
        if(paperDimention != null) setPaperDimention(index,paperDimention);
        return this;
    }

    private void adjustPapers(int index){
        if (this.papers.size() <= index) {
            for (int i = this.papers.size(); i <= index; i++) {
                this.papers.add(i, null);
            }
        }
    }

    private void addPaperView(int i) {
        Utility.removeFromParent(this.papers.get(i));
        this.addView(this.papers.get(i));
    }

    private void addPaperView(Paper paper) {
        Utility.removeFromParent(paper);
        addView(paper);
    }

    private void removePaperView(Paper paper) {
        Utility.removeFromParent(paper);
    }

    public void showPaper(final int index) {
        PenpalBook penpalBook = PenpalLibrary.getCurrentBook();
        // TODO: 讓 cache 正常
//        if(withinPapers(index)) {
//            Paper paper = papers.get(index);
//            if(paper != null) {
//                paper.destroy();
//                papers.set(index, null);
//                removePaperView(paper);
//            }
//        }
        if(penpalBook != null && penpalBook.hasPage(index) && !withinPapers(index)){
            addPaper(index);
        }
        if (withinPapers(index) && papers.get(index) == null){
            loadPaper(index);
        }

        for (int i = 0; i < this.papers.size(); i++) {
            Paper paper = papers.get(i);
            if(i < index - cachePage || i > cachePage + index ){
                if(paper != null) {
                    paper.destroy();
                    papers.set(i, null);
                    removePaperView(paper);
                }
            }
            if (i != index && paper != null && paper.getVisibility() != View.INVISIBLE)
                paper.setVisibility(View.INVISIBLE);
            if (i == index && paper != null) {
                this.selectedPaper = paper;
                if(paper instanceof DrawingPaper){
                    DrawingPaper drawingPaper = (DrawingPaper) selectedPaper;
                    drawingPaper.readProfile();
                }
                paper.setVisibility(View.VISIBLE);
                setProperty();
            }
        }
        setProperty();

    }

    public void setPenType(PEN_TYPE penType) {
        this.penType = penType;
        setProperty();
    }

    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
        setProperty();
    }

    public void setPaintWidth(float paintWidth) {
        this.paintWidth = paintWidth;
        setProperty();
    }

    public void setPaintAlpha(int paintAlpha) {
        this.paintAlpha = paintAlpha;
        setProperty();
    }

    public void setProperty() {
        if(selectedPaper != null && selectedPaper instanceof DrawingPaper){
            DrawingPaper paper = (DrawingPaper) selectedPaper;
            if(penType != null) paper.setPenType(penType);
            if(paintWidth != -1) paper.setPaintWidth(paintWidth);
            if(paintAlpha != -1) paper.setPaintAlpha(paintAlpha);
            if(paintColor != -1) paper.setPaintColor(paintColor);
        }
    }

    public Paper getSelectedPaper() {
        return selectedPaper;
    }

    public Paper getNextPaper() {
        int i = selectedPaper.getIndex()+1;
        if(this.papers.size() == i){
            NCLayerDataPaper layer = new NCLayerDataPaper(NCLayerData.SCALE_LIMIT.PAPER_DRAWING);
            this.papers.add(i, new DrawingPaper(this.context,layer));
            this.addPaperView(i);
        }
        this.selectedPaper = this.papers.get(i);
        showPaper(i);
        return this.papers.get(i);
    }

    private int getBookViewWidth(){
        return getWidth();
    }

    private int getBookViewHeight(){
        return getHeight();
    }

    public List<Paper> getPapers() {
        return papers;
    }

    public void setDimention(NCRect bookDimention, NCRect paperDimention, boolean isApplyAllPapers){
        //Log.w(TAG,"> NCN setDimention"
        //         + "\n bookDimention: " + bookDimention
        //         + "\n        origin: " + this.getX() + ", " + this.getY()
        //         + "\n      bookview: " + this.getBookViewWidth() + " x " + this.getBookViewHeight()
        //         + "\n    visibility: " + Utility.getVisibilityName(this.getVisibility())
        // );
        this.bookDimention = bookDimention;
        this.paperDimention = paperDimention;
        setX(bookDimention.left());
        setY(bookDimention.top());
        Utility.setRelativeLayout(this,(int)bookDimention.width(),(int)bookDimention.height(),0,0,0,0);
        if(isApplyAllPapers) setPaperDimention(NCManager.NONE, paperDimention);
    }

    public void setPaperDimention(int paperNumber, final NCRect paperDimention) {
        //Log.w(TAG,"> NCN setPaperDimention:"
        //         + "\n       paperDimention: " + paperDimention
        // );
        if(paperNumber < 0){
            for (final Paper paper : getPapers()) {
                if(paper != null) paper.setPaperDimention(bookDimention, paperDimention);
            }
        }else{
            if(getPapers().size() > paperNumber) {
                final Paper paper = getPapers().get(paperNumber);
                if(paper != null) paper.setPaperDimention(bookDimention, paperDimention);
            }
        }
        if(this.paperListener != null) this.paperListener.onDimention(paperDimention);
    }


    @Override
    public void addView(View child) {
        //Log.i(TAG,"> addview "+child);
        super.addView(child);
        if(child instanceof NCStickerView){
            NCStickerView view = (NCStickerView) child;
            view.setOnPaperListener(this, true);
            if(selectedPaper!= null)view.setPaperLimit(selectedPaper.getLayer().getLimit());
            this.stickerView = view;
        }
        if(selectedPaper != null && stickerView != null ){
            stickerView.setPaperLimit(selectedPaper.getLayer().getLimit());
        }
    }

    @Override
    public void removeView(View child) {
        //Log.i(TAG,"> removeView "+child);
        super.removeView(child);
        if(child instanceof NCStickerView){
            NCStickerView view = (NCStickerView) child;
            view.setOnPaperListener(null, false);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if(selectedPaper != null){
            selectedPaper.invalidate();
        }
    }

    public void handleMoveTranslate(final Paper paper, final PointF delta, final boolean isMove) {

        if (paper != null) {
            // limit entity center to screen bounds
            boolean needUpdateUI = false;

            //Log.e(TAG, "> NCN handleMoveTranslate (postTranslate) "
            //         + "\n              delta: " + delta.x + ", " + delta.y
            //         + "\n              Paper: " + paper.getPaperWidth() + " x " + paper.getPaperHeight()
            //         + "\n  Paper real Origin: " + paper.getX() + " x " + paper.getY()
            //         + "\n       Paper Origin: " + paper.getPaperDisplayOrigin().x + " x " + paper.getPaperDisplayOrigin().y
            //         + "\n  Paper DisplayRect: " + paper.getDisplayRect() + " [center]" + paper.getCenterX() + " x " + paper.getCenterY()
            //         + "\n        Paper pivot: " + paper.getPivotX() + ", " + paper.getPivotY()
            //         + "\nPaper ScalefitRatio: " + paper.getScalefitRatio()
            //         + "\n        Paper Layer: " + paper.getLayer().getX() + ", " + paper.getLayer().getY()
            //         + "\n   Paper LayerScale: " + paper.getPaperScale()
            //         + "\n      BookView (pt): " + getWidth() + " x " + getHeight()
            //         + "\n    BookView Origin: " + this.getX() + ", " + this.getY()
            //         + "\n             Screen: " + Utility.getDisplayWidth(context) + " x " + Utility.getDisplayHeight(context)
            // );


            boolean isBeyondLeft = paper.getDisplayRect().left() + delta.x >= paper.getPaperDisplayOrigin().x;
            boolean isBeyondRight = paper.getDisplayRect().right() + delta.x <= paper.getPaperDisplayOrigin().x + paper.getPaperDisplayWidth();
            boolean isBeyondTop = paper.getDisplayRect().top() + delta.y >= paper.getPaperDisplayOrigin().y;
            boolean isBeyondBottom = paper.getDisplayRect().bottom() + delta.y <= paper.getPaperDisplayOrigin().y + paper.getPaperDisplayHeight();
            boolean isMovableX = paper.getDisplayRect().width() >= paper.getPaperDisplayWidth();
            boolean isMovableY = paper.getDisplayRect().height() >= paper.getPaperDisplayHeight();

            //Log.i(TAG,"> NCN handleMoveTranslate"
            //         + "\n          delta: " + delta
            //         + "\n   isBeyondLeft: " + isBeyondLeft
            //         + "\n  isBeyondRight: " + isBeyondRight
            //         + "\n    isBeyondTop: " + isBeyondTop
            //         + "\n isBeyondBottom: " + isBeyondBottom
            //         + "\n         isMove: " + isMove
            //         + "\n     isMovableX: " + isMovableX
            //         + "\n     isMovableY: " + isMovableY
            // );

            float alignY = 0;
            float alignX = 0;
            if (!isBeyondLeft && !isBeyondRight && isMovableX && isMove) {
                paper.getLayer().postTranslate(delta.x / getWidth(), 0.0F);
                needUpdateUI = true;
            } else if (isMovableX) {
                if (isBeyondLeft && delta.x <= 0) {
                    alignX = paper.getPaperDisplayOrigin().x;
                }
                if (isBeyondRight && delta.x >= 0) {
                    alignX = paper.getPaperDisplayOrigin().x + paper.getPaperDisplayWidth() - paper.getDisplayRect().width();
                }
                if (alignX != 0) {
                    paper.getLayer().setX(alignX / paper.getPaperDisplayWidth());
                    needUpdateUI = true;
                }
            }
            if (!isBeyondTop && !isBeyondBottom && isMovableY && isMove) {
                paper.getLayer().postTranslate(0.0F, delta.y / getHeight());
                needUpdateUI = true;
            } else if (isMovableY) {
                if (isBeyondTop && delta.y <= 0) {
                    alignY = paper.getPaperDisplayOrigin().y;
                }
                if (isBeyondBottom && delta.y >= 0) {
                    alignY = paper.getPaperDisplayOrigin().y + paper.getPaperDisplayHeight() - paper.getDisplayRect().height();
                }
                if (alignY != 0) {
                    paper.getLayer().setY(alignY / paper.getPaperDisplayHeight());
                    needUpdateUI = true;
                }
            }
            if (needUpdateUI) {
                updateUI();
            }
        }
    }

    private void updateUI() {
        invalidate();
    }

    public void release(){
        handler.removeCallbacks(unlockScale);
        thread.interrupt();
    }

    public void destroy() {
        this.release();
        clear();
        this.context = null;
        handler = null;
        thread = null;
        this.scaleGestureDetector = null;
        this.rotateGestureDetector = null;
        this.moveGestureDetector = null;
        this.tapGestureDetector = null;
    }

    public NCBookView setOnPaperListener(OnPaperListener listener){
        this.paperListener = listener;
        return this;
    }

    public void onResume() {
        for(Paper paper: getPapers()){
            if(paper != null) paper.onResume();
        }
    }

    public void onPause() {
        for(Paper paper: getPapers()){
            if(paper != null) paper.onPause();
        }
    }

    public void clear() {
        for (int i = papers.size() - 1; i >= 0; i--) {
            if(papers.get(i) != null){
                removePaperView(papers.get(i));
                papers.get(i).destroy();
            }
        }
        papers.clear();
        //Log.i(TAG,"> clear papers:"+papers.size());
    }

    public void setOnUndoListener(UndoManager.OnUndoListener undoListener) {
        this.undoListener = undoListener;
    }

    public void setTouch(boolean touch) {
        this.isTouch = touch;
    }

    public interface OnPaperListener {
        void onReady(Paper paper);
        void onDimention(int width, int height);
        void onDimention(NCRect dimention);
        void onMove(Paper paper, float deltaX, float deltaY, boolean isMove);
        void onThumbnailUpdate();
    }

    private PointF getMappedPoint(MotionEvent event){
        matrix.reset();
        srcPoint[0] = event.getX();
        srcPoint[1] = event.getY();
        float scale = 1 / selectedPaper.getScaleX();
        float offsetX = selectedPaper.getOffsetX() - selectedPaper.getDisplayRect().left();
        float offsetY = selectedPaper.getOffsetY() - selectedPaper.getDisplayRect().top();
        matrix.preTranslate(
                offsetX,
                offsetY
        );
        matrix.postScale(scale, scale);
        matrix.mapPoints(destPoint, srcPoint);
        return new PointF(destPoint[0], destPoint[1]);
    }

    private class TapsListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {
            //Log.e(TAG,"> NCN onDoubleTap");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if(isViewMode) return true;
            if(event != null){
                PointF p = getMappedPoint(event);
                if(p != null){
                    Sticker sticker = stickerView.findStickerAtPoint(p.x, p.y);
                    //Log.e(TAG,"> NCN onSingleTapUp " + stickerView.getStickers().size() + " " + p.x + ", " + p.y + " " + sticker);
                    if(sticker != null && sticker.isAdded()){

                        stickerView.selectSticker(sticker, true);
                        if(selectedPaper instanceof DrawingPaper){
                            DrawingPaper paper = (DrawingPaper) selectedPaper;

                            NCRect paperDimention = paper.getPaperDimention();
                            NCRect dimention = new NCRect();
                            PointF origin = paper.getOrigin();
                            float paperScale = paper.getScaleX();
                            dimention.set(
                                    paperDimention.left()   + origin.x,
                                    paperDimention.top()    + origin.y,
                                    paperDimention.right()  + origin.x,
                                    paperDimention.bottom() + origin.y
                            );
                            stickerView
                                    .setScale(paperScale)
                                    .setDimention(null, null, dimention)
                                    .updateUI()
                            ;

                            if (!stickerView.isShown()) stickerView.show();

                            paper.getSurface().invalidate();
                            paper.redrawPaperBitmap();
                        }
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float scale = 1;
        private PointF viewportFocus = new PointF();

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //Log.w(TAG, "> NCN onScaleBegin focus:" + detector.getFocusX() + ", " + detector.getFocusY() + " - " + detector.getScaleFactor());
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(final ScaleGestureDetector detector) {
            if (getSelectedPaper() != null) {
                float scaleFactorDiff = detector.getScaleFactor();
                getSelectedPaper().getLayer().postScale((scaleFactorDiff - 1.0F) * selectedPaper.getScalefitRatio());
                updateUI();
                return true;
            }
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            //Log.w(TAG, "> NCN onScaleEnd focus:" + detector.getScaleFactor());
            handleMoveTranslate(getSelectedPaper(), new PointF(), false);
            super.onScaleEnd(detector);
        }
    }


    private void sendConvertedEvent(MotionEvent event, int action){
        if(isViewMode) return;
        if((action < 0 && event.getAction() == MotionEvent.ACTION_DOWN)
                || (action >= 0 && action == MotionEvent.ACTION_DOWN)) {
            //Log.w(TAG,"> NCN lock scale");
            isMoving = true;
        }
        if((action < 0 && event.getAction() == MotionEvent.ACTION_UP)
                || (action >= 0 && action == MotionEvent.ACTION_UP)) {
            handler.removeCallbacks(unlockScale);
            handler.postDelayed(unlockScale,1000);
        }
        int applyAction = (action >= 0) ? action : event.getAction();
        PointF p = getMappedPoint(event);

        //Log.d(TAG, ""
        //         + " scale: " + d.format(scale)
        //         + " event: " + d.format(event.getX()) + ", " + d.format(event.getY())
        //         + " preTranslate: " + d.format(offsetX) + ", " + d.format(offsetY)
        //         + " offset: " + d.format(selectedPaper.getOffsetX()) + ", " + d.format(selectedPaper.getOffsetY())
        //         + " pivot: " + d.format(selectedPaper.getPivotX()) + ", " + d.format(selectedPaper.getPivotY())
        //         + " matrix: " + d.format(destPoint[0]) + ", " + d.format(destPoint[1])
        //         + " DisplayRect: " + selectedPaper.getDisplayRect()
        // );
        MotionEvent newEvent = MotionEvent.obtain(
                event.getDownTime(),
                event.getEventTime(),
                applyAction,
                p.x,
                p.y,
                NativeCanvas.MODIFIED
        );
        if(stickerView.getVisibility() != VISIBLE) selectedPaper.dispatchTouchEvent(newEvent);
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {

        @Override
        public boolean onMoveBegin(MoveGestureDetector detector) {

            moveStatus = MOVE_STATUS.INVALID;

            if (detector != null && detector.getCurrentEvent() != null) {
                MotionEvent event = detector.getCurrentEvent();
                if (selectedPaper != null && selectedPaper.getAbsDisplayRect().isInside(event.getX(), event.getY())) {
                    moveStatus = MOVE_STATUS.INSIDE_DOWN;
                }else{
                    moveStatus = MOVE_STATUS.OUTSIDE_DOWN;
                }
                //Log.d(TAG, "> NCN onMoveBegin count:" + detector.getCurrentEvent().getPointerCount() + "  " + event.getX() + ", " + event.getY());
            }

            return super.onMoveBegin(detector);
        }

        /**
        // float lengthX = pivotX + getPaperDisplayOrigin().x - getOrigin().x;
        // float lengthCenterX = getCenter().x - getOrigin().x;
        // float absolutePivotX = paperDimention == null ? 0 : paperDimention.absCenter().x;
        // lengthCenterX == 0 ? 0 : lengthX / lengthCenterX * absolutePivotX;

        // matrix.reset();
        // srcPoint[0] = event.getX();
        // srcPoint[1] = event.getY();
        // float scaleX = selectedPaper.getPaperDimention().absCenter().x
        //         / (selectedPaper.getCenter().x - selectedPaper.getOrigin().x);
        // float scaleY = selectedPaper.getPaperDimention().absCenter().y
        //         / (selectedPaper.getCenter().y - selectedPaper.getOrigin().y);
        // matrix.preTranslate(
        //         selectedPaper.getPaperDisplayOrigin().x - selectedPaper.getOrigin().x,
        //         selectedPaper.getPaperDisplayOrigin().y - selectedPaper.getOrigin().y
        // );
        // matrix.preScale(scaleX, scaleY);
        // matrix.mapPoints(destPoint, srcPoint);
        // Log.e(TAG, "> NCN point in paper?     " + bookDimention
        //         + "\n paperDimention: " + paperDimention
        //         + "\n          point: " + event.getX() + ", " + event.getY()
        //         + "\n          point: " + getX() + ", " + getY()
        //         + "\n          point: " + getSelectedPaper().getNewPivotX(event.getX()) + ", " + getSelectedPaper().getNewPivotY(event.getY())
        //         + "\n          point: " + getTranslationX() + ", " + getTranslationY()
        //         + "\n      map point: " + destPoint[0] + ", " + destPoint[1]
        //         + "\n         offset: " + selectedPaper.getOffsetX() + ", " + selectedPaper.getOffsetY()
        //         + "\n absDisplayRect: " + selectedPaper.getAbsDisplayRect()
        // );
         */
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            //Log.d(TAG, "> NCN onMove:" + moveStatus);
            MotionEvent event = null;
            if (detector != null && detector.getCurrentEvent() != null) {
                event = detector.getCurrentEvent();
                if (detector.getCurrentEvent().getPointerCount() == 1) {
                    MotionEvent begin, end;
                    MOVE_STATUS currentStatus = moveStatus;
                    if (selectedPaper != null && selectedPaper.getAbsDisplayRect().isInside(event.getX(), event.getY())) {
                        // 落在裡面
                        try{
                            switch (moveStatus) {
                                case INSIDE_DOWN:
                                    // 送出 DOWN
                                    begin = detector.getStartEvent();
                                    if (begin != null) {
                                        sendConvertedEvent(begin, EVENT_KEEP_ACTION);
                                    }
                                    sendConvertedEvent(event, EVENT_KEEP_ACTION);
                                    currentStatus = MOVE_STATUS.INSIDE_MOVE;
                                    break;
                                case INSIDE_MOVE:
                                    sendConvertedEvent(event, EVENT_KEEP_ACTION);
                                    break;
                                case OUTSIDE_MOVE:
                                case OUTSIDE_DOWN:
                                    // 外到裡
                                    sendConvertedEvent(event, MotionEvent.ACTION_DOWN);
                                    currentStatus = MOVE_STATUS.INSIDE_MOVE;
                                    break;
                                case INSIDE_UP:
                                case OUTSIDE_UP:
                                case INVALID:
                                default:
                                    // 不合理
                                    currentStatus = MOVE_STATUS.INVALID;
                                    break;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        moveStatus = currentStatus;
                    }else {
                        // 落在外面
                        try{
                            switch (moveStatus) {
                                case INSIDE_MOVE:
                                    // 裡到外
                                    end = detector.getPreviousEvent();
                                    if (end != null) {
                                        sendConvertedEvent(end, MotionEvent.ACTION_UP);
                                    }
                                    currentStatus = MOVE_STATUS.OUTSIDE_MOVE;
                                    break;
                                case OUTSIDE_MOVE:
                                case OUTSIDE_DOWN:
                                    // 外面遊蕩
                                    break;
                                case INSIDE_DOWN:
                                case INSIDE_UP:
                                case OUTSIDE_UP:
                                case INVALID:
                                default:
                                    // 不合理
                                    currentStatus = MOVE_STATUS.INVALID;
                                    break;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        moveStatus = currentStatus;
                    }
                    return true;
                }else if (detector.getCurrentEvent().getPointerCount() == 2) {
                    try{
                        MotionEvent end;
                        switch (moveStatus){
                            case INSIDE_MOVE:
                                end = detector.getPreviousEvent();
                                if (end != null) {
                                    sendConvertedEvent(end, MotionEvent.ACTION_UP);
                                }
                                break;
                            case INSIDE_DOWN:
                            case OUTSIDE_DOWN:
                            case OUTSIDE_MOVE:
                            case INSIDE_UP:
                            case OUTSIDE_UP:
                            case INVALID:
                            default:
                                break;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    PointF delta = detector.getFocusDelta();
                    handleMoveTranslate(getSelectedPaper(), delta, true);
                    moveStatus = MOVE_STATUS.INVALID;
                    return true;
                } else {
                    try{
                        MotionEvent end;
                        switch (moveStatus){
                            case INSIDE_MOVE:
                                end = detector.getPreviousEvent();
                                if (end != null) {
                                    sendConvertedEvent(end, MotionEvent.ACTION_UP);
                                }
                                break;
                            case INSIDE_DOWN:
                            case OUTSIDE_DOWN:
                            case OUTSIDE_MOVE:
                            case INSIDE_UP:
                            case OUTSIDE_UP:
                            case INVALID:
                            default:
                                break;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    moveStatus = MOVE_STATUS.INVALID;
                }
            }
            moveStatus = MOVE_STATUS.INVALID;
            //Log.d(TAG, "> NCN onMove some point releasing.... " + (event == null ? "NULL" : Utility.getActionName(event.getAction()) + "  " + event.getX() + ", " + event.getY()));
            return false;
        }

        @Override
        public void onMoveEnd(MoveGestureDetector detector) {
            if (detector != null && detector.getCurrentEvent() != null) {
                MotionEvent event = detector.getCurrentEvent();
                if (detector.getCurrentEvent().getPointerCount() == 2) {
                    PointF delta = detector.getFocusDelta();
                    handleMoveTranslate(getSelectedPaper(), delta, false);
                    //Log.d(TAG, "> NCN onMoveEnd count " + detector.getCurrentEvent().getPointerCount());
                } else if (detector.getCurrentEvent().getPointerCount() == 1 && moveStatus == MOVE_STATUS.INSIDE_MOVE) {
                    //Log.d(TAG, "> NCN onMoveEnd drawing... ");
                    sendConvertedEvent(event, MotionEvent.ACTION_UP);
                }
            }
            //Log.d(TAG, "> NCN onMoveEnd isMoveBegin:");
            moveStatus = MOVE_STATUS.INVALID;
            super.onMoveEnd(detector);
        }
    }
}
