package com.kcchen.nativecanvas.model;

import com.kcchen.nativecanvas.utils.JsonFormatter;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by kowei on 2018/5/18.
 */

public class PenPalTemplate {

    private final static String TAG = PenPalTemplate.class.getSimpleName();

    public static final String TEMPLATE_PROFILE             = "template.json";

    public static final String TEMPLATE_CLEAN_PERIOD        = "TemplateCleanPeriod";
    public static final String TEMPLATE_FILE_NAME           = "TemplateFileName";
    public static final String TEMPLATE_FOLDER_MD5          = "TemplateFolderMd5";
    public static final String TEMPLATE_FROM                = "TemplateFrom";
    public static final String TEMPLATE_LIST                = "TemplateList";
    public static final String TEMPLATE_LOCATION            = "TemplateLocation";
    public static final String TEMPLATE_MD5                 = "TemplateMd5";
    public static final String TEMPLATE_NAME                = "TemplateName";

    public static final String TEMPLATE_FILE_FORMAT_VERSION = "fileFormatVersion";
    public static final String TEMPLATE_SOURCE              = "templateSource";
    public static final String TEMPLATE_CATEGORY_ID         = "categoryId";     //array
    public static final String TEMPLATE_KCCHEN_TEMPLATE_ID  = "KCCHENTemplateId";
    public static final String TEMPLATE_THUMBNAIL           = "thumbnail";
    public static final String TEMPLATE_CONTENT             = "content";        //json
    public static final String TEMPLATE_CONTENT_BG          = "bg";        //json
    public static final String TEMPLATE_DESCRIPTION         = "description";
    public static final String TEMPLATE_CREATE_DATE         = "createDate";
    public static final String TEMPLATE_MODIFY_DATE         = "modifyDate";


    private final String key;
    private final JSONObject templateJson;
    private final String templateProfilePath;
    private boolean isInit = false;
    private File templateProfileFile;
    private JSONObject templateProfile;

    public PenPalTemplate(String path, String key, JSONObject template) {
        this.key = key;

        this.templateJson = template;
        this.templateProfilePath = path + "/" + key + "/" + TEMPLATE_PROFILE;
        init();
        //Log.wtf(TAG,"> PenPalTemplate " + template);
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
        if (templateProfileFile == null && !isInit) {
            templateProfileFile = Utility.getFile(templateProfilePath);
            isInit = true;
        }
        return templateProfileFile != null;
    }

    public boolean save() {
        boolean result = true;
        if (isValid()) {
            try {
                result &= Utility.overwriteTextFile(templateProfileFile, JsonFormatter.format(templateProfile));
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
                String json = Utility.readJsonFile(templateProfileFile);
                if (json != null) {
                    try {
                        templateProfile = new JSONObject(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                if(templateJson != null) {
                    templateProfile = Utility.mergeJson(templateProfile, templateJson);
                    save();
                }
                //Log.wtf(TAG,"> PenPalTemplate load profile \n" + JsonFormatter.format(templateProfile));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public void clean() {
        try {
            if(templateProfile != null){
                while (templateProfile.keys().hasNext()) {
                    templateProfile.put(templateProfile.keys().next(), null);
                }
                templateProfile = null;
                templateProfileFile = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        clean();
    }

    public String getKey() {
        return key;
    }

    public JSONObject getTemplate() {
        return templateProfile;
    }

    public PenPalTemplate setName(String name) {
        try {
            templateProfile.put(TEMPLATE_NAME, name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getSource(){
        return templateProfile.optString(TEMPLATE_SOURCE);
    }

    public JSONArray getCategoryId(){
        return templateProfile.optJSONArray(TEMPLATE_CATEGORY_ID);
    }

    public int getKCCHENTemplateId(){
        return templateProfile.optInt(TEMPLATE_KCCHEN_TEMPLATE_ID);
    }

    public String getThumbnail(){
        return templateProfile.optString(TEMPLATE_THUMBNAIL);
    }

    public String getBackground(){
        JSONObject content = templateProfile.optJSONObject(TEMPLATE_CONTENT);
        if (content != null){
            return content.optString(TEMPLATE_CONTENT_BG);
        }
        return null;
    }

    public String getDescription(){
        return templateProfile.optString(TEMPLATE_DESCRIPTION);
    }

    public String getCreateDate(){
        return templateProfile.optString(TEMPLATE_CREATE_DATE);
    }

    public String getModifyDate(){
        return templateProfile.optString(TEMPLATE_MODIFY_DATE);
    }

    public String getName(){
        return templateProfile.optString(TEMPLATE_NAME);
    }

    public PenPalTemplate setFileName(String fileName) {
        try {
            templateProfile.put(TEMPLATE_FILE_NAME, fileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getFileName(){
        return templateProfile.optString(TEMPLATE_FILE_NAME);
    }

    public PenPalTemplate setSourceFrom(String sourceFrom) {
        try {
            templateProfile.put(TEMPLATE_FROM, sourceFrom);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getSourceFrom(){
        return templateProfile.optString(TEMPLATE_FROM);
    }

    public PenPalTemplate setFileLocation(String foldername) {
        try {
            templateProfile.put(TEMPLATE_LOCATION, foldername);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getFileLocation(){
        return templateProfile.optString(TEMPLATE_LOCATION);
    }

    public PenPalTemplate setFolderMD5(String md5) {
        try {
            templateProfile.put(TEMPLATE_FOLDER_MD5, md5);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getFileMD5(){
        return templateProfile.optString(TEMPLATE_FOLDER_MD5);
    }

    @Override
    public String toString() {
        return templateProfile.toString();
    }
}
