package com.kcchen.nativecanvas.model;

import android.net.Uri;

import com.kcchen.nativecanvas.enums.BOOK_TYPE;
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

public class PenpalBookshelf{

    private final static String TAG = PenpalBookshelf.class.getSimpleName();

    private static final String BOOKSHELF_PROFILE = "bookshelf.json";

    private final String id;
    private final PenpalLibrary library;
    private final String bookshelfPath;

    private boolean isInit = false;
    private File bookshelfFile;
    private JSONObject bookshelfProfile;
    private HashMap<String, PenpalBook> books = new HashMap<String, PenpalBook>();
    private PenpalBook selectedBook;


    public PenpalBookshelf(PenpalLibrary library, String bookshelfID) {
        this.id = bookshelfID;
        this.library = library;
        this.bookshelfPath = library.getLibraryPath() + "/" + id;
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

    public boolean hasBook(String bookID) {
        if(bookID.isEmpty()) return false;
        selectedBook = books.get(bookID);
        if(selectedBook == null) {
            selectedBook = new PenpalBook(this, bookID);
            if(selectedBook.isValid()) {
                books.put(bookID, selectedBook);
            }else{
                selectedBook.destroy();
                selectedBook = null;
            }
        }
        return selectedBook != null;
    }

    public boolean hasBook(String book, BOOK_TYPE type) {
        boolean result = hasBook(book);
        if(result){
            getSelectedBook().setType(type);
        }
        return result;
    }

    public boolean createBook(String bookID){
        return false;
    }

    public boolean deleteBook(String bookID){
        return false;
    }

    public boolean deleteBook(PenpalBook book){
        return false;
    }

    public JSONObject getBookshelfProfile() {
        return bookshelfProfile;
    }

    public PenpalBook getSelectedBook(){
        return selectedBook;
    }

    public boolean isValid() {
        if(bookshelfFile == null && !isInit){
            Uri folderUri = Utility.isFolder(bookshelfPath);
            if(folderUri != null){
                Uri uri = Utility.getUri(getProfilePath());
                if(uri != null) {
                    bookshelfFile = new File(uri.getPath());
                    if(!bookshelfFile.exists()){
                        try {
                            if(!bookshelfFile.createNewFile()){
                                bookshelfFile = null;
                            }else{
                                bookshelfFile.delete();
                            }
                        } catch (IOException e) {
                            bookshelfFile = null;
                            e.printStackTrace();
                        }
                    }
                }
            }
            isInit = true;
        }
        return bookshelfFile != null;
    }

    public String getProfilePath(){
        return bookshelfPath + "/" + BOOKSHELF_PROFILE;
    }

    public String getBookshelfPath() {
        return bookshelfPath;
    }

    public PenpalLibrary getLibrary() {
        return library;
    }

    public boolean save() {
        boolean result = true;
        if (isValid() && bookshelfProfile != null) {
            try {
                result &= Utility.overwriteTextFile(bookshelfFile, JsonFormatter.format(bookshelfProfile));
                //Log.w(TAG,"> PenpalBookshelf save profile "
                //         + "\n saved:" + result
                //         + "\n to:" + bookshelfFile.getPath()
                //         + "\n profile:" + JsonFormatter.format(bookshelfProfile)
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
                bookshelfProfile = createProfile();
                JSONObject readJson = null;
                try{
                    String json = Utility.readJsonFile(bookshelfFile);
                    if (json != null && !json.isEmpty()) {
                        readJson = new JSONObject(json);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                if (readJson != null) {
                    bookshelfProfile = Utility.mergeJson(bookshelfProfile, readJson);
                    if (Utility.getJsonHash(readJson) != Utility.getJsonHash(bookshelfProfile)){
                        save();
                    }
                }else {
                    save();
                }
                // Log.d(TAG,"> PenpalBookshelf load profile \n" + JsonFormatter.format(bookshelfProfile));
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
            bookshelfFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profile;
    }

    private void cleanBooks() {
        for(PenpalBook book: books.values()){
            book.destroy();
        }
        books.clear();
    }

    public void clean() {
        try {
            if(bookshelfProfile != null){
                while (bookshelfProfile.keys().hasNext()) {
                    bookshelfProfile.put(bookshelfProfile.keys().next(), null);
                }
                bookshelfProfile = null;
                bookshelfFile = null;
                cleanBooks();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        clean();
    }

}
