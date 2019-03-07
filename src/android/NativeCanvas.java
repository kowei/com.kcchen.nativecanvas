package com.kcchen.nativecanvas;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kcchen.nativecanvas.enums.ASSET_TYPE;
import com.kcchen.nativecanvas.enums.BOOK_TYPE;
import com.kcchen.nativecanvas.enums.BORDER_TYPE;
import com.kcchen.nativecanvas.enums.CALLBACK_TYPE;
import com.kcchen.nativecanvas.enums.OBJECT_TYPE;
import com.kcchen.nativecanvas.enums.PEN_TYPE;
import com.kcchen.nativecanvas.enums.RESULT_TYPE;
import com.kcchen.nativecanvas.model.MiniMap;
import com.kcchen.nativecanvas.model.NCImageFile;
import com.kcchen.nativecanvas.model.PenpalBook;
import com.kcchen.nativecanvas.model.PenpalLibrary;
import com.kcchen.nativecanvas.model.TextInput;
import com.kcchen.nativecanvas.undo.UndoManager;
import com.kcchen.nativecanvas.utils.Utility;
import com.kcchen.nativecanvas.view.NCManager;
import com.kcchen.penpal.R;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.kcchen.nativecanvas.model.PenpalLibrary.getCurrentBook;
import static com.kcchen.nativecanvas.utils.CordovaUtils.READ_EXTERNAL_REQUEST_CODE;
import static com.kcchen.nativecanvas.utils.CordovaUtils.WRITE_EXTERNAL_REQUEST_CODE;


/**
 * NativeCanvas is a PhoneGap plugin that bridges Android intents and web
 * applications:
 * <p>
 * 1. web apps can spawn intents that call native Android applications. 2.
 * (after setting up correct intent filters for PhoneGap applications), Android
 * intents can be handled by PhoneGap web applications.
 *
 * @author boris@borismus.com
 */
public class NativeCanvas extends CordovaPlugin {
    private static final String TAG = NativeCanvas.class.getSimpleName();
    private static final int containerViewId = R.id.container_view;
    public static final int MODIFIED = 99;
    public static final int INVALID_VALUE = 9999;

    public static final String PROPERTY_LIBRARY            = "library";
    public static final String PROPERTY_BOOKSHELF          = "bookshelf";
    public static final String PROPERTY_BOOK               = "book";
    public static final String PROPERTY_PAGE               = "page";

    private CordovaWebView webview;
    private NCManager ncManager;
    private MiniMap map = new MiniMap();

    private boolean readPermission = false;
    private boolean writePermission = false;
    private CallbackContext registeredCallback;
    private MotionEvent lastEvent;
    private BORDER_TYPE borderType;
    private JSONObject undo = new JSONObject();

    private MiniMap.OnReadyListener mapListener = new MiniMap.OnReadyListener(){
        @Override
        public void onReady() {
            sendCallback(map, true);
        }

        @Override
        public void onBitmapChange() {

        }

        @Override
        public void onPropertyChange() {

        }

        @Override
        public void onAllChange() {

        }
    };
    private UndoManager.OnUndoListener undoListener = new UndoManager.OnUndoListener() {
        @Override
        public void onChanged() {
            try {
                undo.put("currentStep", ncManager.getUndoManager().getCurrent());
                undo.put("canRedo", ncManager.getUndoManager().redoCount());
                undo.put("canUndo", ncManager.getUndoManager().undoCount());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendCallback(undo, true);
        }

        @Override
        public void onImport(UndoManager undoManager) {
            try {
                //Log.i(TAG,"> onImport");
                ncManager.updateSticker(undoManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Called after plugin construction and fields have been initialized.
     * Prefer to use pluginInitialize instead since there is no value in
     * having parameters on the initialize() function.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        this.webview = webView;
    }

    @Override
    public void onResume(boolean multitasking) {
        if(ncManager != null){
            ncManager.onResume();
        }
        super.onResume(multitasking);
    }

    @Override
    public void onPause(boolean multitasking) {
        if(ncManager != null){
            ncManager.onPause();
        }
        super.onPause(multitasking);
    }

    /**
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The registeredCallback context used when calling back into JavaScript.
     * @return true: comsume  false: reject if using Promise
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        try {
            Log.e(TAG, "execute " + "action:" + action + " args:" + args + " callbackContext:" + callbackContext);

            if (action.equals("INIT")) {
                return INIT(args.getString(0), args.getString(1), args.getString(2), args.getInt(3), args.getString(4), callbackContext);
            } else if (action.equals("openCanvas")) {
                return openNativeCanvas(args.getInt(0), args.getInt(1), args.getInt(2), args.getInt(3), args.getString(4), args.getBoolean(5), callbackContext);
            } else if (action.equals("closeCanvas")) {
                return closeNativeCanvas(callbackContext);
            } else if (action.equals("openBook")) {
                return openBook(args.getString(0), args.getString(1), args.getString(2), args.getString(3), args.getInt(4), callbackContext);
            } else if (action.equals("SET_PROFILE")) {
                return SET_PROFILE(args.getJSONObject(0), args.getString(1), args.getString(2), args.getString(3), args.getInt(4), callbackContext);
            } else if (action.equals("setProfile")) {
                return setProfile(args.getJSONObject(0), args.getBoolean(1), args.getString(2), args.getString(3), args.getString(4), args.getInt(5), callbackContext);
            } else if (action.equals("openPage")) {
                return openPage(args.getInt(0), args.getBoolean(1), callbackContext);
            } else if (action.equals("saveBook")) {
                return saveBook(callbackContext);
            } else if (action.equals("savePage")) {
                return savePage(callbackContext);
            } else if (action.equals("deleteBook")) {
                return deleteBook(callbackContext);
            } else if (action.equals("deletePage")) {
                return deletePage(callbackContext);
            } else if (action.equals("register")) {
                return register(callbackContext);
            } else if (action.equals("isRegistered")) {
                return isRegistered(callbackContext);
            } else if (action.equals("show")) {
                return show(callbackContext);
            } else if (action.equals("hide")) {
                return hide(callbackContext);
            } else if (action.equals("isShow")) {
                return isShow(callbackContext);
            } else if (action.equals("isOpened")) {
                return isOpened(callbackContext);
            } else if (action.equals("setPosition")) {
                return setPosition(args.getInt(0), args.getInt(1), args.getInt(2), args.getInt(3), callbackContext);
            } else if (action.equals("setTouch")) {
                return setTouch(args.getBoolean(0), callbackContext);
            } else if (action.equals("toggleDebugMode")) {
                return toggleDebugMode(args.getBoolean(0), callbackContext);
            } else if (action.equals("setProperty")) {
                return setProperty(PEN_TYPE.get(args.getInt(0)), args.getString(1), Float.valueOf(args.getString(2)), Float.valueOf(args.getString(3)), callbackContext);
            } else if (action.equals("clearCanvas")) {
                return clearCanvas(callbackContext);
            } else if (action.equals("CLEARDATA")) {
                return CLEARDATA(callbackContext);
            } else if (action.equals("addObject")) {
                return addObject(OBJECT_TYPE.get(args.getInt(0)), args.getJSONObject(1), callbackContext);
            } else if (action.equals("undoOpen")) {
                return undoOpen(callbackContext);
            } else if (action.equals("undoStep")) {
                return undoStep(args.getInt(0), callbackContext);
            } else if (action.equals("undoClose")) {
                return undoClose(callbackContext);
            } else if (action.equals("play")) {
                return true;
            } else if (action.equals("mapOpen")) {
                return mapOpen(callbackContext);
            } else if (action.equals("mapMove")) {
                return mapMove(Float.valueOf(args.getString(0)), Float.valueOf(args.getString(1)), callbackContext);
            } else if (action.equals("mapPosition")) {
                return mapPosition(Float.valueOf(args.getString(0)), Float.valueOf(args.getString(1)), callbackContext);
            } else if (action.equals("mapScale")) {
                return mapScale(Float.valueOf(args.getString(0)), callbackContext);
            } else if (action.equals("mapClose")) {
                return mapClose(callbackContext);
            } else if (action.equals("toggleViewMode")) {
                return toggleViewMode(args.getBoolean(0), callbackContext);
            }

            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.INVALID_ACTION));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            final String errorMessage = e.getMessage();
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, errorMessage));
            return true;
        }
    }

    private boolean INIT(String libraryPath, String bookshelfPath, String bookPath, int initPages, String bookTemplateId, CallbackContext callbackContext) {
        boolean wasOpen = false;
        PluginResult pluginResult = null;
        if (!isOpened(callbackContext)) {
            openNativeCanvas(0, 0, 0, 0, "", false, callbackContext);
        } else {
            wasOpen = true;
        }

        String parsedlibrary = Utility.parsePath(libraryPath);

        if (parsedlibrary != null) {
            PenpalLibrary penpalLibrary = PenpalLibrary.get(parsedlibrary);

            if (NCManager.hasLibrary(penpalLibrary)
                    && NCManager.hasBookshelf(penpalLibrary, bookshelfPath)
                    && NCManager.hasBook(penpalLibrary, bookPath)
                    ) {
                PenpalBook book = PenpalLibrary.getCurrentBook();
                if (book != null) {
                    if (!bookTemplateId.isEmpty()) {
                        book.setTemplate(bookTemplateId);
                    }
                    book.createPages(cordova.getActivity(), initPages);
                    book.save();
                    Log.d(TAG, "> INIT create " + book.getTotalPages() + " pages.");
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                    sendCallbackUpdate(true);
                }
            }
        } else {
            pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
        }

        if (!wasOpen && isOpened(callbackContext)) closeNativeCanvas(callbackContext);

        if (pluginResult == null)
            pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());

        pluginResult.setKeepCallback(false);
        Log.d(TAG, "> INIT finish");
        callbackContext.sendPluginResult(pluginResult);
        return true;
    }

    private boolean SET_PROFILE(JSONObject property, String libraryPath, String bookshelfPath, String bookPath, int pageNumber, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                boolean wasOpen = false;
                PluginResult pluginResult = null;
                if (!isOpened(callbackContext)) {
                    openNativeCanvas(0, 0, 0, 0, "", false, callbackContext);
                } else {
                    wasOpen = true;
                }

                String parsedlibrary = Utility.parsePath(libraryPath);

                if (hasView() && parsedlibrary != null && property != null && property.length() > 0) {

                    PenpalLibrary library = PenpalLibrary.get(parsedlibrary);

                    if (NCManager.hasLibrary(library)){
                        if (NCManager.hasBookshelf(library, bookshelfPath)){
                            if (NCManager.hasBook(library, bookPath)){
                                if (NCManager.hasPage(library, pageNumber)){
                                    pluginResult = updateProfile(PenpalLibrary.getCurrentPage().getPageProfile(), property.optJSONObject(PROPERTY_PAGE));
                                    if (pluginResult.getMessage().equalsIgnoreCase(RESULT_TYPE.SUCCESS.key() + "")){
                                        PenpalLibrary.getCurrentPage().save(null);
                                    }
                                }else{
                                    pluginResult = updateProfile(PenpalLibrary.getCurrentBook().getBookProfile(), property.optJSONObject(PROPERTY_BOOK));
                                    if (pluginResult.getMessage().equalsIgnoreCase(RESULT_TYPE.SUCCESS.key() + "")){
                                        PenpalLibrary.getCurrentBook().save();
                                    }
                                }
                            }else{
                                pluginResult = updateProfile(PenpalLibrary.getCurrentBookshelf().getBookshelfProfile(), property.optJSONObject(PROPERTY_BOOKSHELF));
                                if (pluginResult.getMessage().equalsIgnoreCase(RESULT_TYPE.SUCCESS.key() + "")){
                                    PenpalLibrary.getCurrentBookshelf().save();
                                }
                            }
                        }else{
                            pluginResult = updateProfile(PenpalLibrary.getCurrentLibrary().getLibraryProfile(), property.optJSONObject(PROPERTY_LIBRARY));
                            if (pluginResult.getMessage().equalsIgnoreCase(RESULT_TYPE.SUCCESS.key() + "")){
                                PenpalLibrary.getCurrentLibrary().save();
                            }
                        }
                    }else{
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                    }

                }else{
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (!wasOpen && isOpened(callbackContext)) closeNativeCanvas(callbackContext);

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    private boolean setProfile(JSONObject property, boolean isReload, String libraryPath, String bookshelfPath, String bookPath, int pageNumber, CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult pluginResult = null;
                String parsedlibrary = Utility.parsePath(libraryPath);

                if (hasView() && parsedlibrary != null && property != null && property.length() > 0) {

                    PenpalLibrary library = PenpalLibrary.get(parsedlibrary);

                    if (NCManager.hasLibrary(library)){
                        if (NCManager.hasBookshelf(library, bookshelfPath)){
                            if (NCManager.hasBook(library, bookPath)){
                                if (NCManager.hasPage(library, pageNumber)){
                                    pluginResult = updateProfile(PenpalLibrary.getCurrentPage().getPageProfile(), property.optJSONObject(PROPERTY_PAGE));
                                    if (pluginResult.getMessage().equalsIgnoreCase(RESULT_TYPE.SUCCESS.key() + "")){
                                        PenpalLibrary.getCurrentPage().save(null);
                                        if(isReload) ncManager.getPaper().reload();
                                    }
                                }else{
                                    pluginResult = updateProfile(PenpalLibrary.getCurrentBook().getBookProfile(), property.optJSONObject(PROPERTY_BOOK));
                                    if (pluginResult.getMessage().equalsIgnoreCase(RESULT_TYPE.SUCCESS.key() + "")){
                                        PenpalLibrary.getCurrentBook().save();
                                        if(isReload) ncManager.getPaper().reload();
                                    }
                                }
                            }else{
                                pluginResult = updateProfile(PenpalLibrary.getCurrentBookshelf().getBookshelfProfile(), property.optJSONObject(PROPERTY_BOOKSHELF));
                                if (pluginResult.getMessage().equalsIgnoreCase(RESULT_TYPE.SUCCESS.key() + "")){
                                    PenpalLibrary.getCurrentBookshelf().save();
                                    if(isReload) ncManager.getPaper().reload();
                                }
                            }
                        }else{
                            pluginResult = updateProfile(PenpalLibrary.getCurrentLibrary().getLibraryProfile(), property.optJSONObject(PROPERTY_LIBRARY));
                            if (pluginResult.getMessage().equalsIgnoreCase(RESULT_TYPE.SUCCESS.key() + "")){
                                PenpalLibrary.getCurrentLibrary().save();
                                if(isReload) ncManager.getPaper().reload();
                            }
                        }
                    }else{
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                    }

                }else{
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    private PluginResult updateProfile(JSONObject profile, JSONObject property) {
        PluginResult pluginResult = null;
        if(profile != null && property != null) {
            try {
                if (Utility.updateJson(profile, property)) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (pluginResult == null)
            pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
        return pluginResult;
    }

    private boolean openBook(final String library, final String bookshelf, final String book, final String type, final int pageNumber, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult pluginResult;
                if (hasView()) {
                    BOOK_TYPE bookType = BOOK_TYPE.get(type);
                    String parsedlibrary = Utility.parsePath(library);

                    if(parsedlibrary != null){
                        PenpalLibrary penpalLibrary = PenpalLibrary.get(parsedlibrary);
                        if(NCManager.hasLibrary(penpalLibrary)
                                && NCManager.hasBookshelf(penpalLibrary, bookshelf)
                                && NCManager.hasBook(penpalLibrary, book, bookType)
                                ){
                            if(pageNumber != INVALID_VALUE) {
                                ncManager.clear(getCurrentBook().getBookPath());
                                pluginResult = openPage(penpalLibrary, pageNumber, false);
                                //pluginResult = openPage(penpalLibrary, 3, true);

                            }else{
                                pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                            }
                        }else{
                            pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                        }
                    }else{
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.BOOK_NOT_EXISTED.key());
                    }


                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "openNativeCanvas error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean openPage(final int page, boolean isInsert, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult pluginResult;
                if (hasView()) {
                    PenpalLibrary library = PenpalLibrary.getCurrentLibrary();
                    if(library != null){
                        pluginResult = openPage(library, page, isInsert);
                    }else{
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.BOOK_NOT_EXISTED.key());
                    }
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "openNativeCanvas error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private PluginResult openPage(PenpalLibrary library, int page, boolean isInsert) {
        PluginResult pluginResult;
        if(NCManager.hasPage(library, page) && !isInsert){
            ncManager.readPage(library, page);
            pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
        }else{
            PenpalBook book = PenpalLibrary.getCurrentBook();
            if(book != null){
                if(isInsert){
                    if (book.insertPage(page)){
                        sendCallbackUpdate(true);
                        ncManager.readPage(library, page);
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                    }else{
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.PAGE_CREATE_FAILED.key());
                    }
                }else if(!NCManager.hasPage(library, page)){
                    if (book.createPage(cordova.getActivity(), page)){
                        sendCallbackUpdate(true);
                        ncManager.readPage(library, page);
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                    }else{
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.PAGE_CREATE_FAILED.key());
                    }
                }else{
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.PAGE_NOT_EXISTED.key());
                }
            }else{
                pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.PAGE_NOT_EXISTED.key());
            }
        }
        return pluginResult;
    }

    private PluginResult savePage() {
        PluginResult pluginResult;
        if(ncManager.isCurrentPageValid()){
            Log.d(TAG,"> NCN savePage ");
            ncManager.savePage();
            pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
        }else{
            pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.PAGE_NOT_EXISTED.key());
        }
        return pluginResult;
    }

    private boolean saveBook(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult pluginResult;
               if (hasView()) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "openNativeCanvas error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean savePage(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult pluginResult;
                if (hasView()) {
//                    if(ncManager.isProjectValid()){
                        pluginResult = savePage();
//                    }else{
//                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.BOOK_NOT_EXISTED.key());
//                    }
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "openNativeCanvas error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean deleteBook(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult pluginResult;
                if (hasView()) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "openNativeCanvas error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean deletePage(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult pluginResult;
                if (hasView()) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "openNativeCanvas error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }


    private boolean openNativeCanvas(final int x, final int y, final int width, final int height, final String backgroundColor, final Boolean isBack, final CallbackContext callbackContext) {

        final NativeCanvas plugin = this;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {


                PluginResult pluginResult;
                if(ncManager == null) ncManager = new NCManager(plugin, cordova.getActivity(), webview, containerViewId);

                if (hasView()) {
                    if(!backgroundColor.isEmpty()) ncManager.setBackgroundColor(backgroundColor);
                    ncManager.setOnUndoListener(undoListener);
                    //setBack(isBack);
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean closeNativeCanvas(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PluginResult pluginResult = null;

                if(ncManager == null) return;
                try{
                    synchronized (ncManager){
                        if (hasView()) {
                            savePage();
                            //webView.getView().setOnTouchListener(originTouchListener);
                        }
                        RelativeLayout containerView = cordova.getActivity().findViewById(containerViewId);
                        if (containerView != null) {
                            containerView.removeAllViews();
                            ((ViewGroup) containerView.getParent()).removeView(containerView);
                        }
                        webView.getView().setBackgroundColor(0xFFFFFF);
                        webView.getView().bringToFront();
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                    }
                }catch (Exception e){
                    if (pluginResult == null) {
                        pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                    }
                    e.printStackTrace();
                }finally {
                    if (pluginResult == null) {
                        pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                    }
                    ncManager.destroy();
                    ncManager = null;
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);

            }
        });
        return true;
    }

    private boolean register(CallbackContext callbackContext) {
        registeredCallback = callbackContext;
        try {
            undo.put("type", CALLBACK_TYPE.UNDO_MANAGER.key());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PluginResult pluginResult = null;
        if (!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionHelper.requestPermission(this, READ_EXTERNAL_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!PermissionHelper.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionHelper.requestPermission(this, WRITE_EXTERNAL_REQUEST_CODE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return true;
    }

    private boolean isRegistered(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                if (registeredCallback == null) {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,false));
                } else {
                    callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK,true));
                }
            }
        });
        return true;
    }

    private boolean setProperty(final PEN_TYPE type, final String color, final float width, final float alpha, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult;

                if (hasView()) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                try {

                    if (type != null && hasView()) {
                        Integer penColor = null;
                        int penAlpha = -1;
                        float penWidth = -1;
                        if (color != null && color.trim().isEmpty() == false) {
                            try {
                                penColor = Color.parseColor(color.trim());
                                //Log.e(TAG, "> NCN parseColor" + penColor);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (width != INVALID_VALUE) {
                            DisplayMetrics metrics = cordova.getActivity().getResources().getDisplayMetrics();
                            penWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, metrics);
                        }
                        if (alpha != INVALID_VALUE) {
                            penAlpha = (int) ((float) 255 * alpha);
                        }

                        ncManager.setPenType(type);

                        switch (type) {
                            case UNKNOWN:
                                break;
                            case BALLPOINT:
                                if (penColor != null) {
                                    ncManager.setPaintColor(penColor);
                                    //Log.e(TAG,"> NCN setPaintColor" + penColor);
                                }
                                if (penWidth > 0)
                                    ncManager.setPaintWidth(penWidth);
                                if (penAlpha > 0) ncManager.setPaintAlpha(penAlpha);
                                break;
                            case HIGHLIGHTER:
                                if (penColor != -1) ncManager.setPaintColor(penColor);
                                if (penWidth > 0)
                                    ncManager.setPaintWidth(penWidth);
                                if (penAlpha > 0) ncManager.setPaintAlpha(penAlpha);
                                break;
                            case ERASER:
                                if (penWidth > 0)
                                    ncManager.setPaintWidth(penWidth);

                                break;
                            case FOUNTAIN:
                                break;
                        }
                    }
                } catch (final Exception e) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.FAILED.key());

                    showToast(cordova.getActivity(), e.getLocalizedMessage());

                    if (color != null && color.isEmpty() == false)
                        Log.e(TAG, "color= " + color);

                    e.printStackTrace();
                }


                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                }

                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean CLEARDATA(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                PluginResult pluginResult;
                PenpalLibrary.clear();
                pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean clearCanvas(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult;


                try {

                    if (hasView()) {
                        ncManager.clearPaper();
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                    } else {
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                    }

                } catch (final Exception e) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.FAILED.key());
                    e.printStackTrace();
                }


                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                }

                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    /**
     * type: 0 image
     * 1 text
     * 2
     */
    private boolean addObject(final OBJECT_TYPE type, final JSONObject object, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;
                if(borderType == null){
                    borderType = BORDER_TYPE.CIRCLE;
                    borderType
                            .setWidth((int) Utility.getSizeFromWidthRatio(cordova.getActivity(), 2.0f))
                            .setRotate(Utility.getSizeFromWidthRatio(cordova.getActivity(), 10.0f))
                    ;
                }

                try {

                    if (type != null && hasView()) {

                        switch (type) {
                            case IMAGE:
                                if (hasView()) {
                                    final String url = object.optString("url");
                                    Bitmap bitmap = null;

                                    ASSET_TYPE type = ASSET_TYPE.parse(url);
                                    NCImageFile imageFile = new NCImageFile(cordova.getActivity(), type, url);

                                    if(imageFile.getBitmap() != null){
                                        ncManager.addSticker(imageFile, borderType, 0.5F, 0F, 0F);
                                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                                    }else{
                                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.IMAGE_TYPE_ERROR.key());
                                    }

                                    //pluginResult = new PluginResult(PluginResult.Status.OK, true);
                                } else {
                                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.VIEW_INVALID.key());
                                }
                                break;
                            case TEXT:
                                if (hasView()) {
                                    ncManager.addTextSticker("test", borderType, 0.5F, 0F, 0F);
                                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                                } else {
                                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.VIEW_INVALID.key());
                                }
                                break;
                            case TEMPLATE:
                                if (hasView()) {
                                    final String template = object.optString("template");
                                    Log.d(TAG,"> template coming..." + template);
                                    if(template != null) ncManager.setTemplate(template);
                                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                                } else {
                                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.VIEW_INVALID.key());
                                }
                                break;
                            default:
                                pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.ADD_OBJECT_INVALID.key());
                                break;
                        }
                    }
                } catch (final Exception e) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.FAILED.key());
                    e.printStackTrace();
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                }

                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    private boolean undoOpen(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;

                try {
                    ncManager.getUndoManager().setOnUndoListener(undoListener);
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());

                } catch (Exception e) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                    e.printStackTrace();
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                }

                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    private boolean undoStep(final int step, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;

                if (hasView()) {
                    if(ncManager.getUndoManager().canUndo() || ncManager.getUndoManager().canRedo()){
                        ncManager.getUndoManager().move(step);
                        try {
                            undo.put("currentStep", ncManager.getUndoManager().getCurrent());
                            undo.put("canRedo", ncManager.getUndoManager().redoCount());
                            undo.put("canUndo", ncManager.getUndoManager().undoCount());
                            pluginResult = new PluginResult(PluginResult.Status.OK, undo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        pluginResult = new PluginResult(PluginResult.Status.OK);
                    }
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK);
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "undoStep error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean undoClose(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;

                try {
                    ncManager.getUndoManager().removeOnUndoListener(undoListener);
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());

                } catch (Exception e) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                    e.printStackTrace();
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                }

                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    private boolean setTouch(final boolean isEnable, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult;

                if (hasView()) {
                    ncManager.setTouch(isEnable);
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean toggleDebugMode(final boolean isDebugMode, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult;

                if (hasView()) {
                    ncManager.setDebug(!isDebugMode);
                    pluginResult = new PluginResult(PluginResult.Status.OK, !isDebugMode);
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, isDebugMode);
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "toggleDebugMode error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean setPosition(final int x, final int y, final int width, final int height, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;

                if (hasView()) {
                    if (ncManager.setLayout(x, y, width, height)) {
                        //webView.getView().setOnTouchListener(filterTouchListener);
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.SUCCESS.key());
                    } else {
                        pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                    }
                } else {
                    //webView.getView().setOnTouchListener(originTouchListener);
                    pluginResult = new PluginResult(PluginResult.Status.OK, RESULT_TYPE.INVALID.key());
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, RESULT_TYPE.FAILED.key());
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean isOpened(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;

                if (hasView() && !ncManager.displayRect().isEmpty()) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, true);
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK, false);
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, false);
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    private boolean show(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;

                if (hasView()) {
                    Log.e(TAG,"> NCN show hasview");
                    ncManager.setTouch(true);
                    ncManager.show();
                    punchWebview();
                    pluginResult = new PluginResult(PluginResult.Status.OK);
                } else {
                    Log.e(TAG,"> NCN show NOT hasview");
                    ncManager.setTouch(false);
                    restoreWebview();
                    pluginResult = new PluginResult(PluginResult.Status.ERROR);
                }

                try {

                    TextInput text = new TextInput();

                    text.put("type", CALLBACK_TYPE.TEXT_INPUT.key());
                    text.put("message", "TEXT_INPUT");
                    sendCallback(text, true);

                    JSONObject obj = new JSONObject();
                    obj.put("type", CALLBACK_TYPE.EXIT.key());
                    obj.put("message", "EXIT");
                    sendCallback(obj, true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "show error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }

    private boolean hide(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;

                if (hasView()) {
                    ncManager.setTouch(false);
                    ncManager.hide();
                    pluginResult = new PluginResult(PluginResult.Status.OK);
                } else {
                    ncManager.setTouch(false);
                    pluginResult = new PluginResult(PluginResult.Status.ERROR);
                }
                restoreWebview();

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "hide error!!!");
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }


        });

        return true;
    }

    private boolean isShow(final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult;

                if (hasView() && ncManager.isShoiw()) {
                    pluginResult = new PluginResult(PluginResult.Status.OK,true);
                } else {
                    pluginResult = new PluginResult(PluginResult.Status.OK,false);
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, false);
                }

                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }
        });

        return true;
    }


    private boolean mapOpen(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult;

                try {

                    if (hasView()) {
                        if (map == null) map = new MiniMap();
                        PointF centerRatio = ncManager.getCenterRatio();
                        map.setOnReadyListener(mapListener);
                        map.setScale(ncManager.getScale() + "");
                        map.setCenterX(centerRatio.x + "");
                        map.setCenterY(centerRatio.y + "");
                        map.setImage(ncManager.getRenderBitmap());
                    }

                    pluginResult = new PluginResult(PluginResult.Status.OK, true);

                } catch (Exception e) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, false);
                    e.printStackTrace();
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "mapOpen error!!!");
                }

                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    /**
     * @param x               percentage from origin x
     * @param y               percentage from origin y
     * @param callbackContext
     * @return
     */
    private boolean mapMove(final Float x, final Float y, CallbackContext callbackContext) {
        if(x != null && y != null) ncManager.setCenter(x, y);
        // if(x != null && y != null) ncManager.move(x, y);
        return true;
    }

    private boolean mapPosition(final Float x, final Float y, CallbackContext callbackContext) {
        if(x != null && y != null) ncManager.setCenter(x, y);
        return true;
    }

    private boolean mapScale(final Float scale, CallbackContext callbackContext) {
        if(scale != null) ncManager.setScale(scale);
        return true;
    }

    /**
     * not implement yet
     *
     * @param callbackContext
     * @return
     */
    private boolean mapClose(final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult = null;

                try {
                    map.setOnReadyListener(null);
                    pluginResult = new PluginResult(PluginResult.Status.OK, true);

                } catch (Exception e) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, false);
                    e.printStackTrace();
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "mapClose error!!!");
                }

                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    private boolean toggleViewMode(final boolean isViewMode, final CallbackContext callbackContext) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                PluginResult pluginResult;

                try {
                    ncManager.setViewMode(!isViewMode);
                    pluginResult = new PluginResult(PluginResult.Status.OK, !isViewMode);

                } catch (Exception e) {
                    pluginResult = new PluginResult(PluginResult.Status.OK, isViewMode);
                    e.printStackTrace();
                }

                if (pluginResult == null) {
                    pluginResult = new PluginResult(PluginResult.Status.ERROR, "mapClose error!!!");
                }

                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
        return true;
    }

    private boolean hasView() {
        // Log.e(TAG, "> NCN hasView " + (ncManager != null ? ncManager.isValid() : "NULL"));

        if (!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PermissionHelper.requestPermission(this, READ_EXTERNAL_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (ncManager == null || !ncManager.isValid()) {
            return false;
        } else {
            return true;
        }
    }

    private void restoreWebview() {
        webView.getView().setBackgroundColor(0xFFFF00);
        webView.getView().bringToFront();
    }

    private void punchWebview() {
        webView.getView().setBackgroundColor(Color.TRANSPARENT);
        ncManager.bringToFront();
    }

    private static void showToast(final Activity activity, final String message) {
        if (activity == null || message == null || message.isEmpty())
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {

                JSONObject obj = new JSONObject();
                obj.put("type", CALLBACK_TYPE.ERROR.key());
                obj.put("message", "Permission Denied");
                obj.put("requestCode", requestCode);
                sendCallback(obj, true);

                return;
            }
        }

        switch (requestCode) {
            case READ_EXTERNAL_REQUEST_CODE:
                this.readPermission = true;
                Log.e(TAG,"> NCN READ permission - " + permissions);
                break;
            case WRITE_EXTERNAL_REQUEST_CODE:
                this.writePermission = true;
                Log.e(TAG,"> NCN WRITE permission - " + permissions );
                break;
        }
    }

    public void sendCallbackUpdate(boolean keepCallback){
        JSONObject insert = null;
        try {
            insert = new JSONObject();
            insert.put("type", CALLBACK_TYPE.INSERT_MANAGER.key());
            insert.put("isReloadBook", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendCallback(insert, keepCallback);
    }

    /**
     * Create a new plugin success result and send it back to JavaScript
     *
     * @param obj a JSONObject contain event payload information
     */
    public void sendCallback(JSONObject obj, boolean keepCallback) {
        sendCallback(obj, keepCallback, PluginResult.Status.OK);
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param obj    a JSONObject contain event payload information
     * @param status the status code to return to the JavaScript environment
     */
    public void sendCallback(JSONObject obj, boolean keepCallback, PluginResult.Status status) {
        //Log.e(TAG, "> sendCallback ########### " + registeredCallback + " ############");
        if (registeredCallback != null) {
            try {
                if(obj instanceof MiniMap){
                    ((MiniMap)obj).print();
                }else{
                    //Log.i(TAG, "> send registeredCallback " + keepCallback + "\n" + JsonFormatter.format(obj));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            PluginResult result = new PluginResult(status, obj);
            result.setKeepCallback(keepCallback);
            registeredCallback.sendPluginResult(result);
            if (!keepCallback) {
                registeredCallback = null;
            }
        }
    }

    private void setBack(Boolean isBack) {
        if (isBack) {
            webView.getView().bringToFront();
        } else {
            ncManager.bringToFront();
        }
    }
}
