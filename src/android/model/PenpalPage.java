package com.kcchen.nativecanvas.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.kcchen.nativecanvas.enums.ASSET_TYPE;
import com.kcchen.nativecanvas.svg.SVGHelper;
import com.kcchen.nativecanvas.utils.JsonFormatter;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by kowei on 2017/11/28.
 */

public class PenpalPage {

    private static String tag = PenpalPage.class.getSimpleName();
    private static String TAG;

    private static final String PAGE_PROFILE                = "drawing.json";
    private static final String PAGE_THUMBNAIL              = "thumbnail.png";

    public static final String PAGE_CONTAINER               = "containerInfo";
    public static final String PAGE_TEMPLATE                = "template";
    public static final String PAGE_BACKGROUND              = "background";
    public static final String PAGE_BACKGROUND_PATH         = "source";
    public static final String PAGE_BACKGROUND_PATH_TYPE    = "sourceType";
    public static final String PAGE_FORMAT_VERSION          = "fileFormatVersion";
    public static final String PAGE_DURATION                = "duration";
    public static final String PAGE_SEGMENT_A               = "segmentA";
    public static final String PAGE_SEGMENT_B               = "segmentB";
    public static final String PAGE_COLLECTED_ACTIONS       = "collectedActions";
    public static final String PAGE_PLATFORM_INFO           = "platformInfo";
    public static final String PAGE_ASSETS_CONFIG           = "assetsConfig";

    private String id;
    private final PenpalBook book;
    private String pagePath;

    private boolean isInit = false;
    private File pageFile;
    private File thumbnailFile;
    private JSONObject pageProfile;
    private int index;
    private String backgroundPath;
    private String templatePath;
    private int hash;
    private Bitmap bitmap;
    private ArrayList<Bitmap> oldBitmaps = new ArrayList<>();
    private Handler handler;

    /**
     *
     * @param book
     * @param pageID index number of page
     */
    public PenpalPage(PenpalBook book, String pageID) {
        this.id = pageID;
        this.book = book;
        this.pagePath = book.getBookPath() + "/" + id;
        this.TAG = this.tag + " " + (Integer.valueOf(id) + 1);
        init();
    }

    private boolean init(){
        //Log.i(TAG,"> PenpalPage init");
        isInit = false;
        if(!isValid()) {
            destroy();
            return false;
        }else{
            handler = new Handler(Looper.getMainLooper());
            load();
            return true;
        }
    }

    public boolean isValid() {
        if(!isInit && pageFile == null){
            Uri folderUri = Utility.isFolder(pagePath);
            if(folderUri != null){
                Uri uri = Utility.getUri(getProfilePath());
                if(uri != null) {
                    pageFile = new File(uri.getPath());
                    if(!pageFile.exists()){
                        try {
                            if(!pageFile.createNewFile()){
                                pageFile = null;
                            }else{
                                pageFile.delete();
                            }
                        } catch (IOException e) {
                            pageFile = null;
                            e.printStackTrace();
                        }
                    }
                }
            }
            isInit = true;
        }
        if(pageFile != null){
            Uri uri = Utility.getUri(getThumbnailPath());
            if(uri != null) {
                thumbnailFile = new File(uri.getPath());
            }
        }else {
            thumbnailFile = null;
        }
        return pageFile != null && thumbnailFile != null;
    }

    public boolean make() {
        //Log.e(TAG, "> NCN make new page " + pagePath);
        Uri folderUri = Utility.isFolder(pagePath);
        if(folderUri == null){
            Uri uri = Utility.getUri(pagePath);
            if(uri != null) {
                File pageFolder = new File(uri.getPath());
                if(!pageFolder.exists()){
                    try {
                        if(pageFolder.mkdir()){
                            this.isInit = false;
                            return init();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public boolean move(int pageIndex) {
        Log.e(TAG, "> move from " + id + " to " + pageIndex);

        String destPath = book.getBookPath() + "/" + pageIndex;

        Uri destFolderUri = Utility.isFolder(destPath);
        Uri srcFolderUri = Utility.isFolder(pagePath);
        if(destFolderUri == null && srcFolderUri != null){
            Uri uri = Utility.getUri(destPath);
            File srcFolder = new File(srcFolderUri.getPath());
            File destFolder = new File(uri.getPath());
            if(srcFolder.renameTo(destFolder)){

                File bookFolder = new File(book.getBookPath());
                Log.w(TAG,"> move \n" + TextUtils.join(",\n", bookFolder.list()));

                resetInit(pageIndex);
                return init();
            }
        }
        return false;
    }

    private void resetInit(int pageIndex) {
        this.id = pageIndex + "";
        this.pagePath = book.getBookPath() + "/" + id;
        this.TAG = this.tag + " " + (Integer.valueOf(id) + 1);
        pageFile = null;
        thumbnailFile = null;
    }

    public boolean isUpdate() {
        Log.e(TAG, "> NCN isUpdate " + Utility.getJsonHash(this) + " - " + hash);
        return Utility.getJsonHash(pageProfile) != hash;
    }

    public PenpalPage setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getProfilePath(){
        return pagePath + "/" + PAGE_PROFILE;
    }

    public String getThumbnailPath(){
        return pagePath + "/" + PAGE_THUMBNAIL;
    }

    public static String getTAG() {
        return TAG;
    }

    public File getPageFile() {
        return pageFile;
    }

    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public String getPagePath() {
        return pagePath;
    }

    public JSONObject getPageProfile() {
        return pageProfile;
    }

    public PenpalBook getBook() {
        return book;
    }

    public PenpalPage setTemplate(String id) {
        resetTemplate();
        try {
            if(pageProfile != null) {
                JSONObject container = pageProfile.optJSONObject(PAGE_CONTAINER);
                if(container != null) {
                    container.put(PAGE_TEMPLATE, id);
                }
                Log.w(TAG,"> setTemplate " + JsonFormatter.format(pageProfile));
                save(null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    private boolean hasBackground() {
        if(pageProfile != null){
            if(backgroundPath == null){
                JSONObject container = pageProfile.optJSONObject(PAGE_CONTAINER);
                if(container != null){
                    JSONObject background = container.optJSONObject(PAGE_BACKGROUND);
                    if(background != null){
                        backgroundPath = background.optString(PAGE_BACKGROUND_PATH);
                        if(backgroundPath != null && !backgroundPath.isEmpty()){
                            backgroundPath = book.getBookPath() + "/" + backgroundPath;
                        }else{
                            // empty to indicate no background after reading config
                            backgroundPath = "";
                        }
                    }
                }
            }
            if(backgroundPath == null){
                backgroundPath = "";
            }
        }else{
            backgroundPath = "";
        }
        return !backgroundPath.isEmpty();
    }

    public PenpalPage resetTemplate(){
        templatePath = null;
        return this;
    }

    private boolean hasTemplate() {
        String from = "";
        try {
            if (templatePath == null) {
                if (PenpalLibrary.getCurrentLibrary() != null) {
                    //Log.w(TAG, "> hasTemplate1 " + PenpalLibrary.getCurrentLibrary());
                    if (PenpalLibrary.getCurrentLibrary().getTemplateManager() != null) {
                        //Log.w(TAG, "> hasTemplate2 " + PenpalLibrary.getCurrentLibrary().getTemplateManager());
                        if (book != null && book.isValid()) {
                            //Log.w(TAG, "> hasTemplate3 " + book);
                            if (book.getBookProfile() != null) {
                                String templateId = book.getBookProfile().optString(PenpalBook.BOOK_TEMPLATE);
                                //Log.w(TAG, "> hasTemplate4 " + JsonFormatter.format(book.getBookProfile()));
                                //Log.w(TAG, "> hasTemplate4 " + templateId);
                                if (templateId != null && !templateId.isEmpty()) {
                                    PenPalTemplate template = PenpalLibrary.getCurrentLibrary().getTemplateManager().getTemplate(templateId);
                                    //Log.w(TAG, "> hasTemplate5 " + template);
                                    if (template != null && template.isValid()) {
                                        from = "book";
                                        //Log.w(TAG, "> hasTemplate6 ");
                                        templatePath = PenpalLibrary.getCurrentLibrary().getTemplateManager().getTemplatePath(templateId) + "/" + template.getBackground();
                                    }
                                }
                            }
                        }
                        if (pageProfile != null) {
                            JSONObject container = pageProfile.optJSONObject(PAGE_CONTAINER);
                            if (container != null) {
                                String templateId = container.optString(PAGE_TEMPLATE);
                                if (templateId != null && !templateId.isEmpty()) {
                                    PenPalTemplate template = PenpalLibrary.getCurrentLibrary().getTemplateManager().getTemplate(templateId);
                                    if (template != null && template.isValid()) {
                                        from = "page";
                                        templatePath = PenpalLibrary.getCurrentLibrary().getTemplateManager().getTemplatePath(templateId) + "/" + template.getBackground();
                                    }
                                }
                            }
                        }
                    }
                }
                if (templatePath == null) templatePath = "";
            }

            Log.w(TAG, "> hasTemplate " + (from.isEmpty() ? (templatePath == null ? "NOT FOUND " : templatePath) : "from " + from + " " + templatePath));

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (templatePath != null && templatePath.isEmpty()) return false;

        return templatePath != null;
    }

    public Bitmap getBackground() {
        clearBitmap();
        if(pageProfile == null) load();

        if(hasTemplate()){
            ASSET_TYPE type = ASSET_TYPE.parse(templatePath);
            Log.w(TAG, "> NCN getBackground template ......... " + " type:" + type + " path:" + templatePath);
            File file;
            switch (type){
                case PNG:
                case JPEG:
                    file = Utility.getFile(templatePath);
                    if (file != null) {
                        bitmap = Utility.loadImage(file);
                    }
                    break;
                case SVG:
                    try {
                        file = Utility.getFile(templatePath);
                        if (file != null) {
                            bitmap = SVGHelper.noContext().reset().open(file).getBitmap();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case GIF:
                    break;
                case UNKNOWN:
                    break;
                default:
            }
        }else {
            if (hasBackground()) {
                ASSET_TYPE type = ASSET_TYPE.parse(backgroundPath);
               Log.w(TAG, "> NCN getBackground Background......... " + " type:" + type + " path:" + backgroundPath);
                File file;
                switch (type) {
                    case PNG:
                    case JPEG:
                        file = Utility.getFile(backgroundPath);
                        if (file != null) {
                            bitmap = Utility.loadImage(file);
                        }
                        break;
                    case SVG:
                        try {
                            file = Utility.getFile(backgroundPath);
                            if (file != null) {
                                bitmap = SVGHelper.noContext().reset().open(file).getBitmap();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case GIF:
                        break;
                    case UNKNOWN:
                        break;
                    default:
                }
            }
        }
        return bitmap;
    }

    public boolean hasThumbnail() {
        return thumbnailFile != null && thumbnailFile.exists();
    }

    public boolean save(Bitmap bitmap) {
        boolean result = true;
        if (isValid() && pageProfile != null) {
            try {
                result &= Utility.overwriteTextFile(pageFile, JsonFormatter.format(pageProfile));
                Log.w(TAG,"> PenpalPage save profile "
                        + "\n saved:" + result
                        + "\n to:" + pageFile.getPath()
                        + "\n profile:" + JsonFormatter.format(pageProfile)
                );
                if(bitmap != null){
                    result &= Utility.saveThumbnailImage(thumbnailFile, Bitmap.CompressFormat.PNG, 100, bitmap, 1 / 4f);
                    //Log.w(TAG,"> PenpalPage save thumbnail "
                    //        + "\n saved:" + result
                    //        + "\n to:" + thumbnailFile.getPath()
                    //);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean load() {
        if (isValid()) {
            try {
                pageProfile = createProfile();
                JSONObject readJson = null;
                try{
                    String json = Utility.readJsonFile(pageFile);
                    Log.w(TAG,"> PenpalPage load " + json);
                    if (json != null && !json.isEmpty()) {
                        readJson = new JSONObject(json);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (readJson != null) {
                    pageProfile = Utility.mergeJson(pageProfile, readJson);
                    if (Utility.getJsonHash(readJson) != Utility.getJsonHash(pageProfile)){
                        save(null);
                    }
                }else {
                    save(null);
                }
                Log.d(TAG,"> PenpalPage load profile \n" + JsonFormatter.format(pageProfile));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    //{
    //    "fileFormatVersion": "v1.0",
    //    "duration": 0,
    //    "segmentA": 0,
    //    "segmentB": 0,
    //    "collectedActions": {},
    //    "platformInfo": {},
    //    "containerInfo": {
    //        "background": {
    //            "source": "assets/bg/pdf_4.svg",
    //            "sourceType": "external"
    //        }
    //    },
    //    "assetsConfig": {}
    //}

    private JSONObject createProfile() {
        JSONObject profile = null;
        JSONObject container;
        JSONObject background;
        try {
            profile = new JSONObject();
            container = new JSONObject();
            background = new JSONObject();

            background.put(PAGE_BACKGROUND_PATH, "");
            background.put(PAGE_BACKGROUND_PATH_TYPE, "");

            container.put(PAGE_BACKGROUND, background);
            container.put(PAGE_TEMPLATE, "");

            profile.put(PAGE_FORMAT_VERSION, "v1.0");
            profile.put(PAGE_DURATION, 0);
            profile.put(PAGE_SEGMENT_A, 0);
            profile.put(PAGE_SEGMENT_B, 0);
            profile.put(PAGE_COLLECTED_ACTIONS, new JSONObject());
            profile.put(PAGE_PLATFORM_INFO, new JSONObject());
            profile.put(PAGE_CONTAINER, container);
            profile.put(PAGE_ASSETS_CONFIG, new JSONObject());

            pageFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profile;
    }

    public void clean() {
        try {
            if(pageProfile != null){
                while (pageProfile.keys().hasNext()) {
                    pageProfile.put(pageProfile.keys().next(), null);
                }
                hash = 0;
                pageProfile = null;
                pageFile = null;
                thumbnailFile = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearBitmap(){
        if(bitmap != null && !bitmap.isRecycled()){
            Runnable recycleBitmap = new Runnable() {
                @Override
                public void run() {
                    for(Bitmap b:oldBitmaps){
                        if(b != null && !b.isRecycled()){
                            Log.wtf(TAG,"> clearBitmap BITMAP_RECYCLED " + b);
                            b.recycle();
                        }
                    }
                    for(int i = oldBitmaps.size() - 1; i >= 0; i--){
                        if(oldBitmaps.get(i) == null || (oldBitmaps.get(i) != null && oldBitmaps.get(i).isRecycled())){
                            oldBitmaps.remove(i);
                        }
                    }
                    //System.gc();
                    Log.wtf(TAG,"> BITMAP_RECYCLED " + oldBitmaps.size());
                }
            };
            Utility.postProcess(handler,recycleBitmap,200);
            oldBitmaps.add(bitmap);
            bitmap = null;
        }
    }

    public void destroy() {
        isInit = true;
        pageFile = null;
        thumbnailFile = null;
        pageProfile = null;
        index = 0;
        backgroundPath = null;
        hash = -1;
        clean();
        release();
    }

    public void release(){
        clearBitmap();
    }

}
