package com.kcchen.nativecanvas.view;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.kcchen.nativecanvas.NativeCanvas;
import com.kcchen.nativecanvas.drawtools.Tool;
import com.kcchen.nativecanvas.enums.BOOK_TYPE;
import com.kcchen.nativecanvas.enums.BORDER_TYPE;
import com.kcchen.nativecanvas.enums.CALLBACK_TYPE;
import com.kcchen.nativecanvas.enums.PEN_TYPE;
import com.kcchen.nativecanvas.enums.UNDO_TYPE;
import com.kcchen.nativecanvas.model.Font;
import com.kcchen.nativecanvas.model.NCImageFile;
import com.kcchen.nativecanvas.model.NCLayerData;
import com.kcchen.nativecanvas.model.NCLayerDataText;
import com.kcchen.nativecanvas.model.PenPalTemplate;
import com.kcchen.nativecanvas.model.PenpalLibrary;
import com.kcchen.nativecanvas.paper.DrawingPaper;
import com.kcchen.nativecanvas.sticker.ImageSticker;
import com.kcchen.nativecanvas.sticker.Sticker;
import com.kcchen.nativecanvas.sticker.TextSticker;
import com.kcchen.nativecanvas.undo.UndoItem;
import com.kcchen.nativecanvas.undo.UndoManager;
import com.kcchen.nativecanvas.utils.FontProvider;
import com.kcchen.nativecanvas.utils.JsonFormatter;
import com.kcchen.nativecanvas.utils.Utility;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kowei on 2017/12/18.
 */

public class NCManager {
    private static final String TAG = NCManager.class.getSimpleName();

    public static boolean isDebug = false;
    public final static int NONE = -1;

    private static final int BORDER_COLOR = Color.parseColor("#f5a623");
    public static String selectedLibraryID;
;
    private final int containerViewId;
    private final RelativeLayout.LayoutParams containerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    private final NativeCanvas plugin;
    private final CordovaWebView webview;
    private Activity activity;
    private RelativeLayout containerView;
    private NCBookView bookView;
    private NCStickerView stickerView;
    private NCRect displayRect;
    private DisplayMetrics displayMetrics;
    private boolean isValid;
    private FontProvider fontProvider;
    private boolean isViewMode;
    private JSONObject insert = new JSONObject();
    private PenpalLibrary selectedLibrary;
    private String bookPath;

    private TextEditorDialogFragment.OnTextLayerListener textListener = new TextEditorDialogFragment.OnTextLayerListener() {

        @Override
        public void textChanged(@NonNull String id, @NonNull String text) {
            TextSticker textSticker = currentTextSticker();
            if (textSticker != null) {
                NCLayerDataText textLayer = textSticker.getLayer();
                if (!text.equals(textLayer.getText())) {
                    textLayer.setText(text);
                    textSticker.updateTextSticker();
                    stickerView.invalidate();
                }
            }
        }

        @Override
        public void onDismiss() {
        }
    };
    private NCStickerView.StickerViewListener stickerViewListener = new NCStickerView.StickerViewListener() {
        @Override
        public void onSelected(@Nullable Sticker sticker) {
            //Log.e(TAG, "> NCN onSelected " + sticker.getUuid());
        }

        @Override
        public void onDoubleTap(@NonNull Sticker sticker) {
            //Log.e(TAG, "> NCN onDoubleTap " + sticker.getUuid() + " " + Utility.getVisibilityName(visibility));
            if (sticker instanceof TextSticker) {
                TextSticker textSticker = (TextSticker) sticker;
                if (textSticker != null) {
                    textSticker.showEditText();
                }
            }
        }

        @Override
        public void onDisplay(@Nullable Sticker sticker, int visibility) {
            Log.e(TAG, "> NCN onDisplay " + sticker.getUuid() + " " + Utility.getVisibilityName(visibility));

            try {
                if (visibility == View.VISIBLE) {
                    insert.put("isEditing", true);
                } else {
                    insert.put("isEditing", false);
                }
                insert.put("stickerType", (sticker != null) ? sticker.getClass().getSimpleName() : "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            plugin.sendCallback(insert, true);
        }

        @Override
        public void onPost(Sticker sticker) {
            if(sticker != null) {
                sticker.getLayer().setVisibleScale(stickerView.getScaleX());
                 Log.e(TAG, "> NCN onPost " + sticker.getUuid() + " " +sticker.isAdded());
                if(sticker instanceof TextSticker){
                    TextSticker textSticker = (TextSticker) sticker;
                    textSticker.hideEditText();
                }
                if(sticker.isAdded()){

                    //sticker.finalizeBitmap();
                    bookView.getSelectedPaper().updateSticker(sticker);
                    if(bookView.getSelectedPaper() instanceof DrawingPaper){
                        DrawingPaper paper = (DrawingPaper) bookView.getSelectedPaper();
                        //paper.getSurface().invalidate();
                        paper.redrawPaperBitmap();
                    }

                }else {
                    //sticker.finalizeBitmap();
                    sticker.setAdded(true);
                    bookView.getSelectedPaper().addSticker(sticker);
                }
            }
            stickerView.hide();
        }
    };
    private String templateId;
    private Handler handler;

    public NCManager(NativeCanvas plugin, Activity activity, CordovaWebView webview, int containerViewId) {
        //create or update the layout params for the container view
        this.activity = activity;
        this.plugin = plugin;
        this.webview = webview;
        this.containerViewId = containerViewId;
        try {
            insert.put("type", CALLBACK_TYPE.INSERT_MANAGER.key());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        handler = new Handler(Looper.getMainLooper());
        //testUpdateJson();
        this.init();
    }

    public void init() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!Utility.isValid(containerView)) {
                        containerView = new RelativeLayout(activity);
                        containerView.setId(containerViewId);
                        hide();
                    }

                    if (!Utility.isValid(bookView)) {
                        bookView = new NCBookView(activity);
                    }

                    if (!Utility.isValid(stickerView)) {
                        stickerView = new NCStickerView(activity);
                        stickerView
                                .setBGColor(Color.TRANSPARENT)
                                .setDefaultBorder(2, BORDER_COLOR, true, false)
                                .hide()
                        ;
                        stickerView.setStickerViewListener(stickerViewListener);
                    }

                    if (!hasParent(stickerView)) {
                        //bookView.createPaper();
                        bookView.addView(stickerView);
                        Utility.setRelativeLayout(stickerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, 0, 0, 0);
                    }

                    if (!hasParent(bookView)) {
                        containerView.addView(bookView);
                    }

                    if (!hasParent(containerView)) {
                        try{
                            if(!Utility.addToViewHierarchy((ViewGroup) webview.getView(), containerView, "XWalkContentView")){
                                //Log.w(TAG,"> XWalkContentView not found, add default");
                                ((ViewGroup)webview.getView()).addView(containerView);
                            }
                            Utility.bringViewToFrontHierarchy((ViewGroup) webview.getView(), "XWalkContent$");
                        }catch (Exception e){
                            //Log.w(TAG,"> XWalkContentView Exception, add default");
                            ((ViewGroup)webview.getView()).addView(containerView);
                        }

                        Utility.setFrameLayout(containerView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0, 0, 0, 0);
                    }

                    if (!Utility.isValid(displayMetrics))
                        displayMetrics = activity.getResources().getDisplayMetrics();
                    if (!Utility.isValid(displayRect)) displayRect = new NCRect();
                    onResume();
                }
            });
        }
    }

    public boolean setLayout(final float x, final float y, final float w, final float h) {
        if (this.isValid()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayRect.set(
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, displayMetrics),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, y, displayMetrics),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x + w, displayMetrics),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, y + h, displayMetrics)
                    );

                    //Log.e(TAG, "> NCN MANAGER displayRect:" + displayRect
                    //        + "\n x,y:" + x +", " + y
                    //        + "\n w,h:" + w +" x " + h
                    //);

                    bookView.setBackgroundColor(Color.LTGRAY);
                    containerView.setBackgroundColor(Color.WHITE);

                    if (NCManager.isDebug) {
                        stickerView.setBackgroundColor(0x33990000);
                    }

                    //containerView.setX((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, x, displayMetrics));
                    //containerView.setY((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, y, displayMetrics));
                    //Utility.setFrameLayout(containerView, (int)displayRect.width(), (int)displayRect.height(), 0, 0, 0, 0);

                    NCRect paperRect = new NCRect();
                    paperRect.set(0, 0, displayRect.width(), displayRect.height());
                    // paperRect.set(0, 0, 800, 1200);
                    bookView.setDimention(displayRect, paperRect, false);
                    bookView.setPaperDimention(NONE, paperRect);
                }
            });
            return true;
        }
        return false;
    }

    public boolean setBackgroundColor(final String backgroundColor) {
        if (this.isValid() && backgroundColor != null && !backgroundColor.trim().isEmpty()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Integer bgColor;
                    try {
                        //Log.i(TAG,"> NCN setBackgroundColor " + backgroundColor);
                        bgColor = Color.parseColor(backgroundColor.trim());
                        if(bookView.getSelectedPaper() != null) {
                            bookView.getSelectedPaper().getLayer().setBackgroundColor(bgColor);
                            bookView.getSelectedPaper().setBackgroundColor(bgColor);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        }
        return false;
    }

    public void setTool(Tool tool) {
//        if (selectedTool != tool) {
//            selectedTool = tool;
//            if (surface != null) {
//                surface.setTool(tool);
//            }
//
//            if (actionMode != null) {
//                actionMode.finish();
//            }
//        }
    }

    public void hide() {
       if (this.isValid()) {
           activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   containerView.setVisibility(View.INVISIBLE);
               }
           });
       }
    }

    public void show() {
       if (this.isValid()) {
           activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   containerView.setVisibility(View.VISIBLE);
               }
           });
       }
    }

    public boolean isShoiw() {
        return this.containerView.getVisibility() == View.VISIBLE;
    }

    public void addView(View view) {
        this.removeFromParent(view);
        this.bookView.addView(view);
        // this.bookView.bringChildToFront(view);
    }

    public void addView(View view, RelativeLayout.LayoutParams layoutParams) {
        this.removeFromParent(view);
        this.bookView.addView(view, layoutParams);
        // this.bookView.bringChildToFront(view);
    }


//    public void addSticker(int resourceId, Sticker.BORDER_TYPE type) {
//        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), resourceId);
//        if (bitmap != null) addSticker(bitmap, type, 0.7F, 0F, 0F);
//    }
//
//    private void addSticker(final int resourceId) {
//        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), resourceId);
//        if (bitmap != null) addSticker(bitmap, Sticker.BORDER_TYPE.NONE, 0.7F, 0F, 0F);
//    }

    private void addTextSticker(final String text, final BORDER_TYPE type) {
        addTextSticker(text, type, 0.7F, 0F, 0F);
    }

//    public void addSticker(final NCImageFile imageFile, final Sticker.BORDER_TYPE type, final float scale, final float xRatio, final float yRatio) {
//        addSticker(imageFile.getBitmap(), type, scale, xRatio, yRatio);
//    }

    public void addSticker(final NCImageFile imageFile, final BORDER_TYPE type, final float scale, final float xRatio, final float yRatio) {
        if(isViewMode) return;
        activity.runOnUiThread(new Runnable() {
            public View.OnLayoutChangeListener onLayoutChangeListener = new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if(right != 0){
                        stickerView.setVisibleCenter(stickerView.getSelectedSticker(), scale, true);
                        stickerView.removeOnLayoutChangeListener(onLayoutChangeListener);
                    }
                }
            };

            @Override
            public void run() {
                NCLayerData layer = new NCLayerData(NCLayerData.SCALE_LIMIT.STICKER_IMAGE);
                final ImageSticker sticker = new ImageSticker(layer, imageFile, (int)displayRect.width(), (int)displayRect.height());
                sticker
                        .setBorderType(type)
                        .setPaperLimit(bookView.getSelectedPaper().getLayer().getLimit());
                NCRect paperDimention = bookView.getSelectedPaper().getPaperDimention();
                NCRect dimention = new NCRect();
                PointF origin = bookView.getSelectedPaper().getOrigin();
                float paperScale = bookView.getSelectedPaper().getScaleX();
                dimention.set(
                        paperDimention.left()   + origin.x,
                        paperDimention.top()    + origin.y,
                        paperDimention.right()  + origin.x,
                        paperDimention.bottom() + origin.y
                );

                stickerView
                        .addSticker(sticker)
                        .applyDefaultBorder(sticker)
                        .setScale(paperScale)
                        .setDimention(bookView, displayRect, dimention)
                        .updateUI()
                ;
                if (xRatio == 0 && yRatio == 0) {
                    stickerView.addOnLayoutChangeListener(onLayoutChangeListener);
                } else {
                    stickerView.setPosition(sticker, scale, xRatio, yRatio, true);
                }
                if (!stickerView.isShown()) stickerView.show();
            }
        });
    }

    public void setTemplate(final String Id){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PenPalTemplate template = PenpalLibrary.getCurrentLibrary().getTemplateManager().getTemplate(Id);
                if(PenpalLibrary.getCurrentPage() != null) {
                    PenpalLibrary.getCurrentPage().setTemplate(Id);
                    if(bookView != null && bookView.getSelectedPaper() != null) {
                        bookView.getSelectedPaper().reload();
                        Log.w(TAG,"> setTemplate " + template);
                    }else{
                        templateId = Id;
                        Utility.postProcess(handler, resetTemplate(),300);
                    }
                }else {
                    Log.e(TAG,"> setTemplate PenpalLibrary.getCurrentPage is NULL");
                }
            }

            private Runnable resetTemplate() {
                Log.w(TAG,"> RETRY setTemplate");
                setTemplate(templateId);
                return null;
            }
        });
    }

    public void addTextSticker(final String text, final BORDER_TYPE type, final float scale, final float xRatio, final float yRatio) {
        if(isViewMode) return;
        activity.runOnUiThread(new Runnable() {
            public View.OnLayoutChangeListener onLayoutChangeListener = new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if(right != 0){
                        stickerView.setVisibleCenter(stickerView.getSelectedSticker(), scale, true);
                        stickerView.removeOnLayoutChangeListener(onLayoutChangeListener);
                    }
                }
            };

            @Override
            public void run() {
                NCLayerDataText textLayer = createTextLayer();
                textLayer.setSpannableText(new SpannableStringBuilder(text));
                TextSticker sticker = new TextSticker(stickerView, textLayer, (int)displayRect.width(), (int)displayRect.height(), fontProvider);
                sticker
                        .setBorderType(type)
                        .setPaperLimit(bookView.getSelectedPaper().getLayer().getLimit());
                NCRect paperDimention = bookView.getSelectedPaper().getPaperDimention();
                NCRect dimention = new NCRect();
                PointF origin = bookView.getSelectedPaper().getOrigin();
                float paperScale = bookView.getSelectedPaper().getScaleX();
                dimention.set(
                        paperDimention.left()   + origin.x,
                        paperDimention.top()    + origin.y,
                        paperDimention.right()  + origin.x,
                        paperDimention.bottom() + origin.y
                );

                stickerView
                        .addSticker(sticker)
                        .applyDefaultBorder(sticker)
                        .setScale(paperScale)
                        .setDimention(bookView, displayRect, dimention)
                        .updateUI()
                ;
                if (xRatio == 0 && yRatio == 0) {
                    stickerView.addOnLayoutChangeListener(onLayoutChangeListener);
                } else {
                    stickerView.setPosition(sticker, scale, xRatio, yRatio, true);
                }
                if (!stickerView.isShown()) stickerView.show();
            }
        });
    }

    @Nullable
    private TextSticker currentTextSticker() {
        if (stickerView != null && stickerView.getSelectedSticker() instanceof TextSticker) {
            return ((TextSticker) stickerView.getSelectedSticker());
        } else {
            return null;
        }
    }

    private NCLayerDataText createTextLayer() {
        if (this.fontProvider == null)
            this.fontProvider = new FontProvider(activity.getResources());
        NCLayerDataText textLayer = new NCLayerDataText(NCLayerData.SCALE_LIMIT.STICKER_TEXT);
        Font font = new Font();

        font.setColor(textLayer.getLimit().getFontColor());
        font.setSize(textLayer.getLimit().getFontSize());
        font.setTypeface(fontProvider.getDefaultFontName());
        textLayer.setFont(font);

        return textLayer;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.isShoiw()) {
            // Log.i(TAG,"> NCN inside " + NativeCanvas.getActionName(event.getAction()) + " " + event.getX() + ", " + event.getY());
            event.offsetLocation(-this.displayRect.rect().left, -this.displayRect.rect().top);
            return this.bookView.dispatchTouchEvent(event);
        }
        return false;
    }

    public boolean bringToFront() {
        if (this.isValid()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webview.getView().bringToFront();
                    Utility.bringViewToFrontHierarchy((ViewGroup) webview.getView(), "XWalkContent$");
                    // Utility.listViewHierarchy((ViewGroup) webview.getView(), 0);
                }
            });
            return true;
        }
        return false;
    }

    public void setScale(final Float scale) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bookView.getSelectedPaper().getLayer().setScale(scale);
                bookView.getSelectedPaper().invalidate();
            }
        });
    }

    public Float getScale() {
        return bookView.getSelectedPaper().getScaleX();
    }

    public PointF getCenterRatio() {
        return bookView.getSelectedPaper().getDisplayCenterRatio();
    }

    public void setCenter(final float x, final float y) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bookView.getSelectedPaper().setDisplayCenterRatio(x, y);
            }
        });
    }

    public void move(final float x, final float y) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bookView.getSelectedPaper().getLayer().postTranslate(x,y);
                bookView.getSelectedPaper().updateUI();
            }
        });
    }

    public RectF displayRect() {
        return this.displayRect.rect();
    }

    public void makeOffset(MotionEvent event) {
        event.offsetLocation(-this.displayRect.rect().left, -this.displayRect.rect().top);
    }

    public boolean isInside(float x, float y) {
        return this.displayRect.isInside(x, y);
    }

    public boolean isValid() {
        if (!this.isValid) {
            return this.activity != null
                    && this.hasParent(this.containerView)
                    && this.hasParent(this.bookView)
                    && this.hasParent(this.stickerView)
                    && Utility.isValid(this.displayRect)
                    && Utility.isValid(this.displayMetrics)
                    ;
        }
        return this.isValid;
    }

    public void clear(String bookPath) {
//        this.resetBookShelf();
//        this.displayRect = null;
//        this.displayMetrics = null;
//        this.isValid = false;
        boolean isClear = false;
        if(bookPath != null){
            if(this.bookPath != null && !this.bookPath.equals(bookPath)){
                //Log.w(TAG,"> NCN book change, clear data cahched......... " + this.bookPath + " " + bookPath);
                this.bookPath = bookPath;
                isClear = true;
            }
        }else{
            isClear = true;
        }

        if(isClear) {

            bookView.clear();
            stickerView.clear();
        }
    }

    public void destroy() {
        onPause();
        this.clear(null);
        this.removeFromParent(this.bookView);
        this.removeFromParent(this.containerView);
        this.removeFromParent(this.stickerView);

        stickerView.destroy();
        bookView.destroy();

        this.containerView = null;
        this.bookView = null;
        this.stickerView = null;
        this.activity = null;
        this.isValid = false;
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
        this.isValid = false;
        return false;
    }

    public Bitmap getRenderBitmap() {
        return getPaper().getPaperBitmap();
    }

    public void onResume() {
        if(bookView != null){
            bookView.onResume();
        }
        if(stickerView != null){
            stickerView.onResume();
        }
    }

    public void onPause() {
        if(bookView != null){
            bookView.onPause();
        }
        if(stickerView != null){
            stickerView.onPause();
        }
    }

    public DrawingPaper getPaper() {
        return (DrawingPaper) bookView.getSelectedPaper();
    }

    public UndoManager getUndoManager() {
        return bookView.getSelectedPaper().getUndoManager();
    }

    public void setViewMode(boolean isViewMode) {
        this.isViewMode = isViewMode;
        bookView.setViewMode(isViewMode);
        stickerView.setViewMode(isViewMode);
    }

    public void clearPaper() {
        if(bookView.getSelectedPaper() instanceof DrawingPaper){
            DrawingPaper paper = (DrawingPaper) bookView.getSelectedPaper();
            paper.clear();
        }
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bookView.invalidate();
                bookView.getSelectedPaper().invalidate();
                if(bookView.getSelectedPaper() instanceof DrawingPaper){
                    ((DrawingPaper)bookView.getSelectedPaper()).getSurface().invalidate();
                }
                stickerView.updateUI();
                if(stickerView.getSelectedSticker() != null) stickerView.getSelectedSticker().setDebug();
            }
        });
    }

//    public boolean setProjectFolder(String projectFolder) {
//
//        if(projectFolder.equals(this.projectFolder) && isProjectValid()) return true;
//        projectFolderUri = isFolder(projectFolder);
//        if(projectFolderUri != null) this.projectFolder = projectFolder;
//        return projectFolderUri != null;
//    }
//
//    public boolean setProject(String project) {
//        if(project.equals(this.project) && isProjectValid()) return true;
//        projectUri = isFolder(projectFolder + "/" + project);
//        if(projectUri != null) this.project = project;
//        return projectUri != null;
//    }

    public void setProjectType(BOOK_TYPE projectType) {
//        this.projectType = projectType;
    }

    public boolean isCurrentPageValid() {
        return bookView.getSelectedPaper() != null;
    }

    public void readPage(PenpalLibrary library, int page) {
        selectedLibrary = library;
        int index = page - 1;
        bookView.showPaper(index);
    }

    public void savePage() {
        if(bookView.getSelectedPaper() != null) bookView.getSelectedPaper().save();
    }

    public void setPenType(PEN_TYPE penType) {
        bookView.setPenType(penType);
    }

    public void setPaintColor(int paintColor) {
        bookView.setPaintColor(paintColor);
    }

    public void setPaintWidth(float paintWidth) {
        bookView.setPaintWidth(paintWidth);
    }

    public void setPaintAlpha(int paintAlpha) {
        bookView.setPaintAlpha(paintAlpha);
    }

    public static boolean hasLibrary(PenpalLibrary library) {
        if(library != null){
            selectedLibraryID = library.getLibraryPath();
            //Log.i(TAG,"> NCN hasLibrary " + library.isValid());
            return library.isValid();
        }
        return false;
    }

    public static boolean hasBookshelf(PenpalLibrary library, String bookshelf) {
        if(hasLibrary(library)){
            //Log.i(TAG,"> NCN hasBookshelf " + library.hasBookshelf(bookshelf));
            return library.hasBookshelf(bookshelf);
        }
        return false;
    }

    public static boolean hasBook(PenpalLibrary library, String book, BOOK_TYPE type) {
        if(hasBookshelf(library)){
            //Log.i(TAG,"> NCN hasBook " + library.getSelectedBookshelf().hasBook(book, type));
            return library.getSelectedBookshelf().hasBook(book, type);
        }
        return false;
    }

    public static boolean hasBook(PenpalLibrary library, String book) {
        if(hasBookshelf(library)){
            //Log.i(TAG,"> NCN hasBook " + library.getSelectedBookshelf().hasBook(book, type));
            return library.getSelectedBookshelf().hasBook(book);
        }
        return false;
    }

    public static boolean hasPage(PenpalLibrary library, int pageNumber) {
        if(hasBook(library)){
            //Log.i(TAG,"> NCN hasPage " + library.getSelectedBookshelf().getSelectedBook().hasPage(pageString));
            return library.getSelectedBookshelf().getSelectedBook().hasPage(pageNumber - 1);
        }
        return false;
    }

    public static boolean hasBookshelf(PenpalLibrary library) {
        return library != null
                && library.getSelectedBookshelf() != null
                && library.getSelectedBookshelf().isValid();
    }

    public static boolean hasBook(PenpalLibrary library) {
        return library != null
                && library.getSelectedBookshelf() != null
                && library.getSelectedBookshelf().getSelectedBook() != null
                && library.getSelectedBookshelf().getSelectedBook().isValid();
    }

    public static boolean hasPage(PenpalLibrary library) {
        return library != null
                && library.getSelectedBookshelf() != null
                && library.getSelectedBookshelf().getSelectedBook() != null
                && library.getSelectedBookshelf().getSelectedBook().getSelectedPage() != null
                && library.getSelectedBookshelf().getSelectedBook().getSelectedPage().isValid();
    }

    public void updateSticker(UndoManager undoManager) {
        for(UndoItem item: undoManager.getUndoItems()){
            if(item.getType() == UNDO_TYPE.STICKER_IMAGE || item.getType() == UNDO_TYPE.STICKER_TEXT){
                JSONObject data = item.getData();
                //Log.i(TAG, "> updateSticker " + data);
                if(data != null){
                    String uuid = data.optString(Sticker.STICKER_UUID);
                    //Log.i(TAG, "> updateSticker stickerView " + stickerView);
                    if(stickerView.getStickerByUuid(uuid) == null){

                        Sticker sticker = null;
                        if(item.getType() == UNDO_TYPE.STICKER_IMAGE) {
                            NCLayerData imageStickerLayer = new NCLayerData(NCLayerData.SCALE_LIMIT.STICKER_IMAGE);
                            sticker = new ImageSticker(imageStickerLayer, data);
                        }else if(item.getType() == UNDO_TYPE.STICKER_TEXT) {
                            NCLayerDataText textStickerLayer = createTextLayer();
                            textStickerLayer.setSpannableText(new SpannableStringBuilder("testssss"));
                            sticker = new TextSticker(activity, stickerView, fontProvider, textStickerLayer, data);
                        }

                        if(sticker != null) {
                            //Log.i(TAG, "> updateSticker " + sticker.getUuid());
                            sticker.setActivity(activity);
                            sticker.finalizeBitmap();
                            stickerView.importSticker(sticker);
                        }
                    }
                }
            }
        }
    }

    public void setOnUndoListener(UndoManager.OnUndoListener undoListener) {
        bookView.setOnUndoListener(undoListener);
    }

    public void setTouch(boolean touch) {
        bookView.setTouch(touch);
    }


    private void testUpdateJson(){
        try {
            JSONObject test1 = new JSONObject();
            JSONObject test1c = new JSONObject();
            JSONArray test1a = new JSONArray();
            JSONObject test2 = new JSONObject();
            JSONObject test2c= new JSONObject();
            JSONArray test2a = new JSONArray();

            test1c.put("a","1");
            test1c.put("b","2");
            test1c.put("c","3");

            // TODO: array 必須從0開始建立，是否改成自動生成？
            test1a.put(0,"0");
            test1a.put(1,"1");
            test1a.put(2,"2");
            test1a.put(3,"3");

            test1.put("abc",test1c);
            test1.put("d",test1a);
            test1.put("e","5");
            test1.put("f","6");
            test1.put("g","7");
            test1.put("h","8");
            test1.put("i","9");

            Log.w(TAG,"> test1 \n" + test1);
            Log.w(TAG,"> test1 \n" + JsonFormatter.format(test1));

            test2c.put("a","4");
            test2c.put("s","5");
            test2c.put("c","6");

            test2a.put(0,"0");
            test2a.put(1,"1");
            test2a.put(2,"b");
            test2a.put(3,"3");
            test2a.put(4,"4");

            test2.put("abc",test2c);
            test2.put("def",test2c);
            test2.put("d",test2a);
            test2.put("e","5");
            test2.put("f","6");
            test2.put("g","71");
            test2.put("h","8");
            test2.put("i","9");

            Log.w(TAG,"> test2 \n" + JsonFormatter.format(test2));

            // TODO: merge 時 array 不是擴展，需修正
            JSONObject result = Utility.mergeJson(test1, test2);
            Log.w(TAG,"> test1 merge with test2\n" + JsonFormatter.format(result));

            Utility.updateJson(test1, test2);
            Log.w(TAG,"> test1 update with test2\n" + JsonFormatter.format(test1));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
