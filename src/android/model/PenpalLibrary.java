package com.kcchen.nativecanvas.model;

import android.net.Uri;

import com.kcchen.nativecanvas.utils.JsonFormatter;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by kowei on 2017/11/28.
 */

public class PenpalLibrary{

    private static final String TAG = PenpalLibrary.class.getSimpleName();
    private static final String LIBRARY_PROFILE = "library.json";
    private static final HashMap<String, PenpalLibrary> Library = new HashMap<String, PenpalLibrary>();
    private static PenpalLibrary selectedLibrary;

    public static PenpalLibrary get(String libraryPath) {

        if(libraryPath == null) return null;

        if(selectedLibrary == null || !selectedLibrary.getLibraryPath().equals(libraryPath)){
            Uri libraryUri = Utility.isFolder(libraryPath);
            if (libraryUri != null) {
                selectedLibrary = Library.get(libraryUri.getPath());
                if (selectedLibrary == null) {
                    selectedLibrary = new PenpalLibrary(libraryUri.getPath());
                    if (selectedLibrary.isValid()) {
                        Library.put(libraryUri.getPath(), selectedLibrary);
                    }else {
                        selectedLibrary = null;
                    }
                }
            }
        }

        return selectedLibrary;
    }

    public static PenpalLibrary getCurrentLibrary() {
        return selectedLibrary;
    }

    public static PenpalBookshelf getCurrentBookshelf() {
        if(selectedLibrary != null) return selectedLibrary.getSelectedBookshelf();
        return null;
    }

    public static PenpalBook getCurrentBook() {
        PenpalBookshelf bookshelf = getCurrentBookshelf();
        if(bookshelf != null) return bookshelf.getSelectedBook();
        return null;
    }

    public static PenpalPage getCurrentPage() {
        PenpalBook book = getCurrentBook();
        if(book != null) return book.getSelectedPage();
        return null;
    }

    public static void clear(){
        for(PenpalLibrary library: Library.values()){
            library.destroy();
        }
        Library.clear();
    }

    private final String libraryPath;

    private boolean isInit = false;
    private File libraryFile;
    private HashMap<String, PenpalBookshelf> bookshelfs = new HashMap<String, PenpalBookshelf>();
    private JSONObject libraryProfile;
    private PenpalBookshelf selectedBookshelf;
    private PenPalTemplateManager templateManager;

    public PenpalLibrary(String libraryPath) {
        this.libraryPath = libraryPath;
        templateManager = new PenPalTemplateManager(this);
        init();
    }

    private boolean init(){
        if(!isValid()) {
            destroy();
            return false;
        }else{
            load();
            return true;
        }
    }

    public boolean hasBookshelf(String bookshelfID) {
        if(bookshelfID.isEmpty()) return false;
        selectedBookshelf = bookshelfs.get(bookshelfID);
        if(selectedBookshelf == null) {
            selectedBookshelf = new PenpalBookshelf(this, bookshelfID);
            if(selectedBookshelf.isValid()) {
                bookshelfs.put(bookshelfID, selectedBookshelf);
            }else{
                selectedBookshelf.destroy();
                selectedBookshelf = null;
            }
        }
        return selectedBookshelf != null;
    }

    public boolean createBookshelf(String bookshelfID){
        return false;
    }

    public boolean deleteBookshelf(String bookshelfID){
        return false;
    }

    public boolean deleteBookshelf(PenpalBookshelf bookshelf){
        return false;
    }

    public JSONObject getLibraryProfile() {
        return libraryProfile;
    }

    public PenPalTemplateManager getTemplateManager() {
        return templateManager;
    }

    public PenpalBookshelf getSelectedBookshelf(){
        return selectedBookshelf;
    }

    public String getProfilePath(){
        return this.libraryPath + "/" + LIBRARY_PROFILE;
    }

    public String getLibraryPath() {
        return libraryPath;
    }

    public boolean isValid() {
        if(libraryFile == null && !isInit){
            Uri folderUri = Utility.isFolder(libraryPath);
            if(folderUri != null){
                Uri uri = Utility.getUri(getProfilePath());
                if(uri != null) {
                    libraryFile = new File(uri.getPath());
                    if(!libraryFile.exists()){
                        try {
                            if(!libraryFile.createNewFile()){
                                libraryFile = null;
                            }else{
                                libraryFile.delete();
                            }
                        } catch (IOException e) {
                            libraryFile = null;
                            e.printStackTrace();
                        }
                    }
                }
            }
            isInit = true;
        }
        return libraryFile != null;
    }

    public boolean save() {
        boolean result = true;
        if (isValid() && libraryProfile != null) {
            try {
                result &= Utility.overwriteTextFile(libraryFile, JsonFormatter.format(libraryProfile));
                // Log.w(TAG,"> PenpalLibrary save profile "
                //         + "\n saved:" + result
                //         + "\n to:" + libraryFile.getPath()
                //         + "\n profile:" + JsonFormatter.format(libraryProfile)
                // );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean load() {
        if (isValid()) {
            try {
                libraryProfile = createProfile();
                JSONObject readJson = null;
                try{
                    String json = Utility.readJsonFile(libraryFile);
                    if (json != null && !json.isEmpty()) {
                        readJson = new JSONObject(json);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (readJson != null) {
                    libraryProfile = Utility.mergeJson(libraryProfile, readJson);
                    if (Utility.getJsonHash(readJson) != Utility.getJsonHash(libraryProfile)){
                        save();
                    }
                }else {
                    save();
                }
                // Log.d(TAG,"> PenpalLibrary load profile \n" + JsonFormatter.format(libraryProfile));
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
        try {
            profile = new JSONObject();
            profile.put("","");
            libraryFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profile;
    }

    private void cleanBookShelf() {
        for(PenpalBookshelf bookshelf: bookshelfs.values()){
            bookshelf.destroy();
        }
        bookshelfs.clear();
    }

    public void clean() {
        try {
            if(libraryProfile != null){
                while (libraryProfile.keys().hasNext()) {
                    libraryProfile.put(libraryProfile.keys().next(), null);
                }
                libraryProfile = null;
                libraryFile = null;
                cleanBookShelf();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        clean();
    }

    public JSONObject clone() {
        try {
            if(libraryProfile != null){
                return new JSONObject(libraryProfile.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
