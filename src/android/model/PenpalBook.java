package com.kcchen.nativecanvas.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.kcchen.nativecanvas.enums.BOOK_TYPE;
import com.kcchen.nativecanvas.utils.JsonFormatter;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by kowei on 2017/11/28.
 */

public class PenpalBook{

    private final static String TAG = PenpalBook.class.getSimpleName();

    private static final String BOOK_PROFILE              = "profile.json";

    public static final String BOOK_ASSET_CHECK_FLAG      = "assetCheckFlag";
    public static final String BOOK_ASSET_FILES           = "assetFiles";
    public static final String BOOK_AUTHOR                = "author";
    public static final String BOOK_TYPE                  = "projectType";
    public static final String BOOK_CATEGORY              = "category";
    public static final String BOOK_CREATE_DATE           = "createDate";
    public static final String BOOK_DISPLAY_NAME          = "displayName";
    public static final String BOOK_FORMAT_VERSION        = "fileFormatVersion";
    public static final String BOOK_GROUP                 = "group";
    public static final String BOOK_IS_GUIDE_DOC          = "isGuideDoc";
    public static final String BOOK_IS_HIDDEN             = "isHidden";
    public static final String BOOK_IS_DEFAULT            = "isDefault";
    public static final String BOOK_MODIFY_DATE           = "modifyDate";
    public static final String BOOK_TAG                   = "tag";
    public static final String BOOK_COMMENT               = "comment";
    public static final String BOOK_PDF_SRC_FILE_NAME     = "pdfSrcFileName";
    public static final String BOOK_COVER                 = "cover";
    public static final String BOOK_COVER_TYPE            = "coverType";
    public static final String BOOK_COVER_ID              = "coverId";
    public static final String BOOK_COVER_URI             = "coverURI";
    public static final String BOOK_COVER_BOOKMARK_COLOR  = "coverBookmarkColor";

    public static final String BOOK_ASSET_CHECK_LASTTIME  = "BookAssetCheckLasttime";
    public static final String BOOK_MODIFY_DATE_1970      = "BookModifyDate1970";
    public static final String BOOK_CREATE_DATE_1970      = "BookCreateDate1970";
    public static final String BOOK_TEMPLATE              = "templateId";

    private final String id;
    private final PenpalBookshelf bookshelf;
    private final String bookPath;

    private BOOK_TYPE bookType;
    private boolean isInit = false;
    private File bookFile;
    private ArrayList<PenpalPage> pages = new ArrayList<PenpalPage>();
    private PenpalAssetManager assetManager;
    private JSONObject bookProfile;
    private PenpalPage selectedPage;
    private File bookFolderFile;

    public PenpalBook(PenpalBookshelf bookshelf, String bookID) {
        this.id = bookID;
        this.bookshelf = bookshelf;
        this.bookPath = bookshelf.getBookshelfPath() + "/" + id;
        init();
    }

    private boolean init(){
        if(!isValid()) {
            destroy();
            return false;
        }else{
            load();
            assetManager = new PenpalAssetManager(this);
            return true;
        }
    }

    /**
     *
     * @param pageIndex
     * @return
     */
    public boolean hasPage(int pageIndex) {

        selectedPage = (pages.size() > pageIndex) ? pages.get(pageIndex) : null;

        if(selectedPage == null) {
            selectedPage = new PenpalPage(this, String.valueOf(pageIndex));
            if(selectedPage.isValid()) {
                if(setPages(pageIndex)) {
                    pages.add(selectedPage);
                }
            }else{
                selectedPage.destroy();
                selectedPage = null;
            }
        }
        return selectedPage != null;
    }

    /**
     * 確保 pages 陣列在 pageIndex 位置之前 array size 是存在
     * @param pageIndex
     * @return
     */
    private boolean setPages(int pageIndex){
        try {
            if (this.pages.size() <= pageIndex) {
                for (int i = this.pages.size(); i < pageIndex; i++) {
                    pages.add(null);
                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public void setType(BOOK_TYPE type) {
        this.bookType = type;
    }

    /**
     *
     * @param activity
     * @param pageNumber
     */
    //public void createPages(int pageIndex) {
    //    if (this.pages.size() <= pageIndex) {
    //        for (int i = this.pages.size() + 1; i <= pageIndex; i++) {
    //            createPage(i);
    //        }
    //    }
    //}
    public boolean createPages(Activity activity, int pageNumber) {
        boolean isCreate = false;
        for (int i = 1; i <= pageNumber; i++) {
            int index = i - 1;
            if (pages.size() <= index || pages.get(index) == null) {
                isCreate |= createPage(activity, i);
            }
        }
        return isCreate;
    }

    /**
     *
     * @param pageNumber page number
     * @return
     */
    public boolean createPage(Activity activity, int pageNumber){
        Log.w(TAG, "> " + id + " createPage - " + pageNumber);
        String pageIndex = String.valueOf(pageNumber - 1);
        PenpalPage newPage = new PenpalPage(this, pageIndex);
        if(!newPage.isValid()){
            if(newPage.make()){
                if(setPages(pageNumber - 1)) {
                    pages.add(pageNumber-1, newPage);
                    selectedPage = newPage;

                    Bitmap bitmap = newPage.getBackground();
                    if (bitmap == null) {
                        bitmap = Bitmap.createBitmap(Utility.getDisplayWidth(activity), Utility.getDisplayHeight(activity), Bitmap.Config.ARGB_8888);
                        bitmap.eraseColor(Color.WHITE);
                    }
                    newPage.save(bitmap);
                    bitmap.recycle();
                    System.gc();
                    return true;
                }
            }
        }else{
            if(setPages(pageNumber - 1)) {
                pages.add(pageNumber-1, newPage);
                selectedPage = newPage;

                Bitmap bitmap = newPage.getBackground();
                if (bitmap == null) {
                    bitmap = Bitmap.createBitmap(Utility.getDisplayWidth(activity), Utility.getDisplayHeight(activity), Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.WHITE);
                }
                newPage.save(bitmap);
                bitmap.recycle();
                System.gc();
                return true;
            }
        }

        return false;
    }

    public void loadRestPages(int pageNumber){
        int totalPages = getTotalPages();

    }

    public int getTotalPages() {
        int totalPages = 0;
        if(bookFolderFile.exists()) {
            File[] listFiles = bookFolderFile.listFiles();
            for ( File file : listFiles){
                if (file.getName().matches("^[0-9]+")){
                    totalPages++;
                }
            }
        }
        return totalPages;
    }

    public boolean insertPage(int pageNumber) {
        // 修改資料，保存。必須從後面開始
        Log.w(TAG,"> insertPage number " + pageNumber);

        int totalPages = getTotalPages();
        if(bookFolderFile.exists()) Log.w(TAG,"> insertPage files " + getTotalPages());

        ArrayList<Integer> remove = new ArrayList<>();
        for (int i = totalPages - 1; i >= pageNumber - 1; i--) {
            if (hasPage(i)) {
                if (pages.get(i)
                        .setIndex(i + 1)
                        .save(null)) {
                    // 移動
                    if (pages.get(i)
                            .move(i + 1)) {
                        remove.add(i);
                    }
                }
            }
        }
        // 移除 insert 之後資料
//        for (int i : remove) {
//            pages.get(i).destroy();
//            pages.remove(i);
//        }
        String pageIndex = String.valueOf(pageNumber - 1);
        PenpalPage newPage = new PenpalPage(this, pageIndex);
        if (!newPage.isValid()) {
            if (newPage.make()) {
                if (setPages(pageNumber - 1)) {
                    pages.add(pageNumber - 1, newPage);
                    selectedPage = newPage;
                    newPage.save(newPage.getBackground());
                    return true;
                }
            }
        } else {
            if (setPages(pageNumber - 1)) {
                pages.add(pageNumber - 1, newPage);
                selectedPage = newPage;
                newPage.save(newPage.getBackground());
                return true;
            }
        }

        return false;
    }

    public boolean deletePage(int pageNumber){
        if (pageNumber <= pages.size()) return pages.remove(pageNumber - 1) != null;
        return false;
    }

    public boolean deletePage(PenpalPage page){
        if (pages.contains(page)) return pages.remove(page);
        return false;
    }

    public PenpalPage getSelectedPage() {
        return selectedPage;
    }

    public boolean isValid() {
        if(bookFile == null && !isInit){
            Uri folderUri = Utility.isFolder(bookPath);
            if(folderUri != null){
                bookFolderFile = new File(folderUri.getPath());
                Uri uri = Utility.getUri(getProfilePath());
                if(uri != null) {
                    bookFile = new File(uri.getPath());
                    if(!bookFile.exists()){
                        try {
                            if(!bookFile.createNewFile()){
                                bookFile = null;
                            }else{
                                bookFile.delete();
                            }
                        } catch (IOException e) {
                            bookFile = null;
                            e.printStackTrace();
                        }
                    }
                }
            }
            isInit = true;
        }
        return bookFile != null && bookFolderFile != null;
    }

    public String getProfilePath(){
        return bookPath + "/" + BOOK_PROFILE;
    }

    public String getBookPath() {
        return bookPath;
    }

    public PenpalBookshelf getBookshelf() {
        return bookshelf;
    }

    public JSONObject getBookProfile() {
        return bookProfile;
    }

    public PenpalAssetManager getAssetManager() {
        return assetManager;
    }

    public PenpalBook setTemplate(String id) {
       try {
            if(bookProfile != null) {
                bookProfile.put(BOOK_TEMPLATE, id);
                Log.w(TAG,"> setTemplate " + JsonFormatter.format(bookProfile));
                save();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean save() {
        boolean result = true;
        if (isValid() && bookProfile != null) {
            try {
                result &= Utility.overwriteTextFile(bookFile, JsonFormatter.format(bookProfile));
                Log.w(TAG,"> PenpalBook save profile "
                         + "\n saved:" + result
                         + "\n to:" + bookFile.getPath()
                         + "\n profile:" + JsonFormatter.format(bookProfile)
                 );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean load() {
        if (isValid()) {
            try {
                bookProfile = createProfile();
                JSONObject readJson = null;
                try{
                    String json = Utility.readJsonFile(bookFile);
                    Log.d(TAG,"> PenpalBook load profile " + json);
                    if (json != null && !json.isEmpty()) {
                        readJson = new JSONObject(json);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (readJson != null) {
                    bookProfile = Utility.mergeJson(bookProfile, readJson);
                    Log.d(TAG,"> PenpalBook load merge profile ");
                    if (Utility.getJsonHash(readJson) != Utility.getJsonHash(bookProfile)){
                        save();
                    }
                }else {
                    save();
                }
                 Log.d(TAG,"> PenpalBook load profile \n" + JsonFormatter.format(bookProfile));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    private JSONObject createProfile() {
        JSONObject profile = null;
        JSONObject cover = null;
        JSONArray assets = new JSONArray();
        long current = System.currentTimeMillis();
        /*
         * Symbol   Meaning                 Kind        Example
         * D        day in year             (Number)    189
         * E        day of week             (Text)      E/EE/EEE:Tue, EEEE:Tuesday, EEEEE:T
         * F        day of week in month    (Number)    2 (2nd Wed in July)
         * G        era designator          (Text)      AD
         * H        hour in day (0-23)      (Number)    0
         * K        hour in am/pm (0-11)    (Number)    0
         * L        stand-alone month       (Text)      L:1 LL:01 LLL:Jan LLLL:January LLLLL:J
         * M        month in year           (Text)      M:1 MM:01 MMM:Jan MMMM:January MMMMM:J
         * S        fractional seconds      (Number)    978
         * W        week in month           (Number)    2
         * Z        time zone (RFC 822)     (Time Zone) Z/ZZ/ZZZ:-0800 ZZZZ:GMT-08:00 ZZZZZ:-08:00
         * a        am/pm marker            (Text)      PM
         * c        stand-alone day of week (Text)      c/cc/ccc:Tue, cccc:Tuesday, ccccc:T
         * d        day in month            (Number)    10
         * h        hour in am/pm (1-12)    (Number)    12
         * k        hour in day (1-24)      (Number)    24
         * m        minute in hour          (Number)    30
         * s        second in minute        (Number)    55
         * w        week in year            (Number)    27
         * y        year                    (Number)    yy:10 y/yyy/yyyy:2010
         * z        time zone               (Time Zone) z/zz/zzz:PST zzzz:Pacific Standard Time
         * '        escape for text         (Delimiter) 'Date=':Date=
         * ''       single quote            (Literal)   'o''clock':o'clock
         */
        SimpleDateFormat timeFormater = new SimpleDateFormat("yyyy-mm-dd'T'kk:mm:ss");

        try {
            profile = new JSONObject();
            cover = new JSONObject();

            cover.put(BOOK_COVER_TYPE, "");
            cover.put(BOOK_COVER_ID, "");
            cover.put(BOOK_COVER_URI, "");
            cover.put(BOOK_COVER_BOOKMARK_COLOR, "");

            profile.put(BOOK_FORMAT_VERSION, "v1.0");
            profile.put(BOOK_IS_GUIDE_DOC, false);
            profile.put(BOOK_IS_HIDDEN, false);
            profile.put(BOOK_IS_DEFAULT, false);
            profile.put(BOOK_DISPLAY_NAME, "");
            profile.put(BOOK_TAG, "");
            profile.put(BOOK_TYPE, bookType == null ? com.kcchen.nativecanvas.enums.BOOK_TYPE.UNSET.key() : bookType.key());
            profile.put(BOOK_ASSET_CHECK_FLAG, false);
            profile.put(BOOK_ASSET_CHECK_LASTTIME, current);
            profile.put(BOOK_ASSET_FILES, assets);
            profile.put(BOOK_CATEGORY, "");
            profile.put(BOOK_GROUP, "");
            profile.put(BOOK_AUTHOR, "");
            profile.put(BOOK_CREATE_DATE, timeFormater.format(current));
            profile.put(BOOK_CREATE_DATE_1970, current);
            profile.put(BOOK_MODIFY_DATE, timeFormater.format(current));
            profile.put(BOOK_MODIFY_DATE_1970, current);
            profile.put(BOOK_COMMENT, "");
            profile.put(BOOK_PDF_SRC_FILE_NAME, "");
            profile.put(BOOK_TEMPLATE, "");
            profile.put(BOOK_COVER, cover);

            // Log.i(TAG,"> NCN current time format string - " + timeFormater.format(current));
            bookFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profile;
    }

    private void cleanPages() {
        for(PenpalPage page: pages){
            page.destroy();
        }
        pages.clear();
    }

    public void clean() {
        try {
            if(bookProfile != null){
                while (bookProfile.keys().hasNext()) {
                    bookProfile.put(bookProfile.keys().next(), null);
                }
                bookProfile = null;
                bookFile = null;
                cleanPages();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        clean();
    }

    public String getBookName() {
        String name = "BOOK";
        if (bookProfile != null) {
            name = bookProfile.optString(BOOK_DISPLAY_NAME);
            if(name == null || name.isEmpty()){
                name = "BOOK";
            }
        }
        return name;
    }
}
