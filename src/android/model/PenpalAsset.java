package com.kcchen.nativecanvas.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kowei on 2018/3/22.
 */

public class PenpalAsset {
    private final static String TAG = PenpalAsset.class.getSimpleName();

    public static final String ASSET_CLEAN_PERIOD   = "AssetCleanPeriod";
    public static final String ASSET_FILE_NAME      = "AssetFileName";
    public static final String ASSET_FILE_MD5       = "AssetFileMd5";
    public static final String ASSET_FROM           = "AssetFrom";
    public static final String ASSET_LIST           = "AssetList";
    public static final String ASSET_LOCATION       = "AssetLocation";
    public static final String ASSET_MD5            = "AssetMd5";
    public static final String ASSET_NAME           = "AssetName";

    private final String key;
    private final JSONObject asset;

    public PenpalAsset(String key, JSONObject asset) {
        this.key = key;
        this.asset = asset;
    }

    public String getKey() {
        return key;
    }

    public JSONObject getAsset() {
        return asset;
    }

    public PenpalAsset setName(String name) {
        try {
            asset.put(ASSET_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getName(){
        return asset.optString(ASSET_NAME);
    }

    public PenpalAsset setFileName(String fileName) {
        try {
            asset.put(ASSET_FILE_NAME, fileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getFileName(){
        return asset.optString(ASSET_FILE_NAME);
    }

    public PenpalAsset setSourceFrom(String sourceFrom) {
        try {
            asset.put(ASSET_FROM, sourceFrom);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getSourceFrom(){
        return asset.optString(ASSET_FROM);
    }

    public PenpalAsset setFileLocation(String foldername) {
        try {
            asset.put(ASSET_LOCATION, foldername);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getFileLocation(){
        return asset.optString(ASSET_LOCATION);
    }

    public PenpalAsset setFileMD5(String md5) {
        try {
            asset.put(ASSET_FILE_MD5, md5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getFileMD5(){
        return asset.optString(ASSET_FILE_MD5);
    }

    @Override
    public String toString() {
        return asset.toString();
    }
}
