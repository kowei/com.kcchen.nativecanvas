package com.kcchen.nativecanvas.model;

import android.net.Uri;

import com.kcchen.nativecanvas.enums.ASSET_TYPE;
import com.kcchen.nativecanvas.utils.JsonFormatter;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.kcchen.nativecanvas.model.PenpalAsset.ASSET_CLEAN_PERIOD;
import static com.kcchen.nativecanvas.model.PenpalAsset.ASSET_FILE_MD5;
import static com.kcchen.nativecanvas.model.PenpalAsset.ASSET_LIST;

/**
 * Created by kowei on 2017/11/28.
 */

public class PenpalAssetManager {

    private final static String TAG = PenpalAssetManager.class.getSimpleName();

    private static final String ASSET_PROFILE = "asset.json";
    public static final String ASSET_FOLDER = "assets";
    private static final long ASSET_CLEAN_PERIOD_DEFAULT = 604800000; // 7d*24h*60m*60s*1000ms;


    private final String assetPath;
    private final PenpalBook book;
    private File assetProfileFile;
    private boolean isInit = false;
    private JSONObject assetProfile;
    private HashMap<String,PenpalAsset> assets;
    //private JSONObject assets;

    public PenpalAssetManager(PenpalBook book) {
        this.book = book;
        this.assetPath = book.getBookPath();// + "/" + ASSET_FOLDER;
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

    public boolean isValid() {
        if(assetProfileFile == null && !isInit){
            Uri folderUri = Utility.isFolder(assetPath);
            if(folderUri != null){
                Uri uri = Utility.getUri(getProfilePath());
                if(uri != null) {
                    assetProfileFile = new File(uri.getPath());
                    if(!assetProfileFile.exists()){
                        try {
                            if(!assetProfileFile.createNewFile()){
                                assetProfileFile = null;
                            }else{
                                assetProfileFile.delete();
                            }
                        } catch (IOException e) {
                            assetProfileFile = null;
                            e.printStackTrace();
                        }
                    }
                }
            }
            isInit = true;
        }
        return assetProfileFile != null;
    }

    public File getAssetFile(PenpalAsset asset) {
        return new File(getAssetPath(asset.getKey()));
    }

    public String getProfilePath(){
        return assetPath + "/" + ASSET_FOLDER + "/" + ASSET_PROFILE;
    }

    /**
     * 取得asset資料夾或是asset完整路徑
     * 例如：
     * @param assetLocation asset路徑，為了符合之前資料設計，其包含了asset資料夾
     * @return 如果有asset路徑，返回asset完整路徑，如果沒有，返回asset資料夾
     */
    public String getAssetPath(String assetLocation){
        if(assetLocation != null) {
            return assetPath + "/" + assetLocation;
        }else{
            return assetPath + "/" + ASSET_FOLDER;
        }
    }

    /**
     *
     * @return
     */
    public File getAssetProfileFile() {
        return assetProfileFile;
    }

    public PenpalBook getBook() {
        return book;
    }

    public boolean save() {
        boolean result = true;
        if (isValid() && assetProfile != null) {
            try {
                result &= Utility.overwriteTextFile(assetProfileFile, JsonFormatter.format(assetProfile));
                //Log.w(TAG,"> PenpalAssetManager save profile "
                //        + "\n saved:" + result
                //        + "\n to:" + assetProfileFile.getPath()
                //        + "\n profile:" + JsonFormatter.format(assetProfile)
                //);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 物件建構時，若相關條件都通過，即會呼叫此將profile載入
     * @return 載入成功為true
     */
    public boolean load() {
        if (isValid()) {
            try {
                String json = Utility.readJsonFile(assetProfileFile);
                if (json == null) {
                    // 讀不到profile，建立一個新的預設值，並儲存
                    createProfile();
                }else{
                    try {
                        assetProfile = new JSONObject(json);
                        JSONObject list = assetProfile.optJSONObject(ASSET_LIST);
                        int hash = Utility.getJsonHash(list);
                        if (list == null) {
                            list = new JSONObject();
                        }
                        // 更新asset list，跟檔案系統同步
                        assets = updateAssets();
                        if(Utility.getJsonHash(assetProfile.optJSONObject(ASSET_LIST)) != hash) {
                            assetProfile.put(ASSET_LIST, list);
                            save();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                //Log.i(TAG,"> PenpalAssetManager load profile \n" + JsonFormatter.format(assetProfile));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public PenpalAsset getAsset(String source) {
        return assets.get(source);
    }

    public PenpalAsset getAssetByMD5(String md5){
        if (assets != null){
            for (PenpalAsset asset: assets.values()){
                if(asset.getFileMD5().equals(md5)){
                    return asset;
                }
            }
        }
        return null;
    }

    public boolean isMD5Exist(String md5) {
        return getAssetByMD5(md5) != null;
    }

    public String getAssetMD5(String source) {
        PenpalAsset asset = getAsset(source);
        if (asset != null) {
            return asset.getFileMD5();
        }
        return null;
    }

    public boolean isAssetAvailable(String source) {
        return getAssetMD5(source) != null;
    }

    public boolean isAssetChanged(String source) {
        boolean isChanged = false;

        String fileMd5 = Utility.getMD5(Utility.getFile(getAssetPath(source)));
        String entryMd5 = getAssetMD5(source);

        if (fileMd5 != null && entryMd5 != null) {
            isChanged = !entryMd5.equals(fileMd5);
        }

        if (isChanged) {
            try {
                assets.get(source).setFileMD5(fileMd5);
                assetProfile.put(ASSET_LIST, getAssetsJson());
                save();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return isChanged;
    }

    private void createProfile() {
        // 更新asset list，跟檔案系統同步
        assets = updateAssets();
        try {
            if(assetProfile == null)
                assetProfile = new JSONObject();

            assetProfile.put(ASSET_CLEAN_PERIOD, ASSET_CLEAN_PERIOD_DEFAULT);
            assetProfile.put(ASSET_LIST, getAssetsJson());

            assetProfileFile.createNewFile();
            save();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject getAssetsJson(){
        JSONObject jsonObject = new JSONObject();
        if(assets != null){
            for(String key:assets.keySet()){
                try {
                    jsonObject.put(key, assets.get(key).getAsset());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return jsonObject;
        }
        return new JSONObject();
    }

    private HashMap<String, PenpalAsset> updateAssets() {
        JSONObject list = null;
        if(assetProfile != null)  {
            list = assetProfile.optJSONObject(ASSET_LIST);
        }else{
            assetProfile = new JSONObject();
        }
        if(list == null){
            list = new JSONObject();
        }

        HashMap<String, PenpalAsset> assets = new HashMap<String, PenpalAsset>();
        // remove entry from list if no entity found on file system
        if (list.length() > 0) {
            for (int i = 0; i < list.names().length(); i++) {
                String path = list.names().optString(i);
                if (Utility.getFile(getAssetPath(path)) == null) {
                    list.remove(path);
                }
            }
        }
        // add entry to list if list has no entry
        HashMap<String, String> files = readAssetsFolder(getAssetPath(null));
        //Log.i(TAG, "> PenpalAssetManager asstes " + getAssetPath(null)
        //         + "\n list key: " + TextUtils.join(",", files.keySet())
        //         + "\n values: " + TextUtils.join(",", files.values())
        // );
        try {
            for (String path : files.keySet()) {
                PenpalAsset asset;
                JSONObject entry = list.optJSONObject(path);
                if (entry != null) {
                    // 更新 MD5
                    String md5 = entry.optString(ASSET_FILE_MD5);
                    if (md5 == null || !md5.equalsIgnoreCase(files.get(path))) {
                        entry.put(ASSET_FILE_MD5, files.get(path));
                    }
                    asset = new PenpalAsset(path, entry);
                    if (entry != null) list.put(path, entry);
                    //Log.i(TAG, "> updateAssets update " + list);
                } else {
                    // 建立新的 entry
                    asset = getNewAsset(path, files.get(path));
                    if (asset != null) list.put(asset.getKey(), asset.getAsset());
                    //Log.i(TAG, "> updateAssets add " + list);
                }
                assets.put(path, asset);
            }
            assetProfile.put(ASSET_LIST, list);
            //Log.i(TAG, "> updateAssets\n" + (assetProfile == null || assetProfile.length() == 0 ? "" : JsonFormatter.format(assetProfile)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return assets;
    }

    private HashMap<String, String> readAssetsFolder(String folderString) {
        File folder = Utility.getFolder(folderString);
        HashMap<String, String> files = new HashMap<String, String>();
        if (folder != null) {
            String[] fileStrings = folder.list();
            //Log.i(TAG,"> PenpalAssetManager fileStrings " + TextUtils.join(",",fileStrings));
            for (String fileString : fileStrings) {
                String path = folderString + "/" + fileString;
                File file = Utility.getFile(path);
                if (file != null) {
                    if (ASSET_TYPE.isSupported(fileString)) {
                        files.put(path.replace(assetPath + "/", ""), Utility.getMD5(file));
                    }
                } else {
                    files.putAll(readAssetsFolder(path));
                }
            }
        }
        return files;
    }

    public boolean addAsset(PenpalAsset asset) {
        if (assets != null) {
            try {
                assets.put(asset.getKey(), asset);
                assetProfile.put(ASSET_LIST, getAssetsJson());
                save();
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public PenpalAsset getNewAsset(String path, String md5) {
        try {
            Uri uri = Uri.parse(path);
            File file = Utility.getFile(getAssetPath(path));
            String fileFullname = uri.getLastPathSegment();
            String foldername = path.replace(fileFullname, "");
            String filename = fileFullname;
            if (foldername.length() > 0) foldername = foldername.substring(0, foldername.length() - 1);
            if (filename.length() > 0) filename = filename.split("\\.")[0];

            PenpalAsset entry = new PenpalAsset(path, new JSONObject());
            entry
                    .setName(filename)
                    .setFileName(fileFullname)
                    .setSourceFrom("")
                    .setFileLocation(foldername)
                    .setFileMD5(md5 == null ? Utility.getMD5(file) : md5);

            return entry;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clean() {
        try {
            if(assetProfile != null){
                while (assetProfile.keys().hasNext()) {
                    assetProfile.put(assetProfile.keys().next(), null);
                }
                assetProfile = null;
                assetProfileFile = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        clean();
    }

}
