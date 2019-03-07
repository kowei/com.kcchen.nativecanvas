package com.kcchen.nativecanvas.model;

import android.net.Uri;
import android.util.Log;

import com.kcchen.nativecanvas.utils.JsonFormatter;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static com.kcchen.nativecanvas.model.PenPalTemplate.TEMPLATE_CLEAN_PERIOD;
import static com.kcchen.nativecanvas.model.PenPalTemplate.TEMPLATE_FOLDER_MD5;
import static com.kcchen.nativecanvas.model.PenPalTemplate.TEMPLATE_LIST;


/**
 * Created by kowei on 2018/5/18.
 */

public class PenPalTemplateManager {

    private final static String TAG = PenPalTemplateManager.class.getSimpleName();

    private static final String TEMPLATES_PROFILE = "templates.json";
    public static final String TEMPLATES_FOLDER = "templates";
    private static final long TEMPLATES_CLEAN_PERIOD_DEFAULT = 604800000; // 7d*24h*60m*60s*1000ms;


    private final String templateFolder;
    private final PenpalLibrary library;
    private File templateProfile;
    private boolean isInit = false;
    private JSONObject templateJson;
    private HashMap<String,PenPalTemplate> templates;

    /**
     * constructor
     * @param library 因為位於 loibrary 之下，傳遞有效的 PenpalLibrary 可以快速對應資料
     */
    public PenPalTemplateManager(PenpalLibrary library) {
        this.library = library;
        this.templateFolder = library.getLibraryPath() + "/" + TEMPLATES_FOLDER;
        init();
    }

    /**
     * initialize after cobstructor
     * 檢查這個物件是否 valid，如果 valid ，就載入資料，否則結束並清除資料
     * @return
     */
    private boolean init(){
        if(!isValid()) {
            destroy();
            return false;
        }else{
            load();
            return true;
        }
    }

    /**
     * 確認 templateFolder 存在
     * 確認 TEMPLATES_PROFILE 存在或可寫入
     * @return true if conditons valid, false if invalid
     */
    public boolean isValid() {
        if(templateProfile == null && !isInit){
            Uri folderUri = Utility.isFolder(templateFolder);
            if (folderUri == null) folderUri = make();
            if(folderUri != null){
                Uri fileUri = Utility.getUri(getProfilePath());
                if(fileUri != null) {
                    //Log.wtf(TAG,"> folder:" + folderUri + " file:" + fileUri);
                    templateProfile = new File(fileUri.getPath());
                    if(!templateProfile.exists()){
                        try {
                            if(!templateProfile.createNewFile()){
                                templateProfile = null;
                            }else{
                                templateProfile.delete();
                            }
                        } catch (IOException e) {
                            templateProfile = null;
                            e.printStackTrace();
                        }
                    }
                }
            }
            isInit = true;
        }
        return templateProfile != null;
    }

    public Uri make() {
        //Log.e(TAG, "> NCN make new template folder " + templateFolder);
        this.isInit = false;
        Uri folderUri = Utility.isFolder(templateFolder);
        if(folderUri == null){
            Uri uri = Utility.getUri(templateFolder);
            if(uri != null) {
                File folderFile = new File(uri.getPath());
                if(!folderFile.exists()){
                    try {
                        folderFile.mkdir();
                        return uri;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public File getTemplateFile(PenPalTemplate template) {
        return new File(getTemplatePath(template.getKey()));
    }

    public String getProfilePath(){
        return templateFolder + "/" + TEMPLATES_PROFILE;
    }

    /**
     * 取得template資料夾或是template完整路徑
     * 例如：
     * @param templateLocation template路徑，為了符合之前資料設計，其包含了template資料夾
     * @return 如果有template路徑，返回template完整路徑，如果沒有，返回template資料夾
     */
    public String getTemplatePath(String templateLocation){
        if(templateLocation != null) {
            return templateFolder + "/" + templateLocation;
        }else{
            return templateFolder;
        }
    }

    /**
     *
     * @return
     */
    public File getTemplateProfile() {
        return templateProfile;
    }

    /**
     * getter of PenpalLibrary
     * @return PenpalLibrary
     */
    public PenpalLibrary getLibrary() {
        return library;
    }

    public boolean save() {
        boolean result = true;
        if (isValid() && templateJson != null) {
            try {
                result &= Utility.overwriteTextFile(templateProfile, JsonFormatter.format(templateJson));
                //Log.w(TAG,"> PenpalTemplateManager save profile "
                //                        + "\n saved:" + result
                //                        + "\n to:" + templateProfile.getPath()
                //                        + "\n profile:" + JsonFormatter.format(templateJson)
                //                );
            } catch (Exception e) {
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
                String json = Utility.readJsonFile(templateProfile);
                if (json == null || json.isEmpty()) {
                    // 讀不到profile，建立一個新的預設值，並儲存
                    createProfile();
                }else{
                    try {
                        templateJson = new JSONObject(json);
                        JSONObject list = templateJson.optJSONObject(TEMPLATE_LIST);
                        int hash = Utility.getJsonHash(list);
                        if (list == null) {
                            list = new JSONObject();
                        }
                        // 更新template list，跟檔案系統同步
                        templates = updateTemplates();
                        if(Utility.getJsonHash(templateJson.optJSONObject(TEMPLATE_LIST)) != hash) {
                            templateJson.put(TEMPLATE_LIST, list);
                            save();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                Log.i(TAG,"> PenpalTemplateManager load profile \n" + JsonFormatter.format(templateJson));
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    public PenPalTemplate getTemplate(String source) {
        try {
            if(templates.get(source) == null) {
                templates = updateTemplates();
                templateJson.put(TEMPLATE_LIST, getTemplatesJson());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return templates.get(source);
    }

    public PenPalTemplate getTemplateByMD5(String md5){
        if (templates != null){
            for (PenPalTemplate template: templates.values()){
                if(template.getFileMD5().equals(md5)){
                    return template;
                }
            }
        }
        return null;
    }

    public boolean isMD5Exist(String md5) {
        return getTemplateByMD5(md5) != null;
    }

    /**
     *
     * @param folderName
     * @return
     */
    public String getTemplateMD5(String folderName) {
        PenPalTemplate template = getTemplate(folderName);
        if (template != null) {
            return template.getFileMD5();
        }
        return null;
    }

    /**
     * 查找資料庫是否有此template
     * @param folderName
     * @return true id available, false is invalid
     */
    public boolean isTemplateAvailable(String folderName) {
        return getTemplateMD5(folderName) != null;
    }

    /**
     * 根據資料夾名稱，比對已經儲存的MD5，檢視是否有變動
     * @param folderName
     * @return true if changed, false is same
     */
    public boolean isTemplateChanged(String folderName) {
        boolean isChanged = false;

        String folderMD5 = Utility.getFolderMD5(Utility.getFolder(getTemplatePath(folderName)), false, PenPalTemplate.TEMPLATE_PROFILE);
        String entryMd5 = getTemplateMD5(folderName);

        if (folderMD5 != null && entryMd5 != null) {
            isChanged = !entryMd5.equals(folderMD5);
        }

        if (isChanged) {
            try {
                templates.get(folderName).setFolderMD5(folderMD5);
                templateJson.put(TEMPLATE_LIST, getTemplatesJson());
                save();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return isChanged;
    }

    /**
     * 建立預設的 json 資料
     */
    private JSONObject createProfile() {
        // 更新template list，跟檔案系統同步
        templates = updateTemplates();
        try {
            if(templateJson == null)
                templateJson = new JSONObject();

            templateJson.put(TEMPLATE_CLEAN_PERIOD, TEMPLATES_CLEAN_PERIOD_DEFAULT);
            templateJson.put(TEMPLATE_LIST, getTemplatesJson());

            templateProfile.createNewFile();
            save();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return templateJson;
    }

    /**
     * 將 templates 陣列，轉換成 json 資料
     * @return json 包含所有 template 資料
     */
    private JSONObject getTemplatesJson(){
        JSONObject jsonObject = new JSONObject();
        if(templates != null){
            for(String key:templates.keySet()){
                try {
                    jsonObject.put(key, templates.get(key).getTemplate());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonObject;
    }

    /**
     *
     * @return
     */
    private HashMap<String, PenPalTemplate> updateTemplates() {
        JSONObject list = null;
        if(templateJson != null)  {
            list = templateJson.optJSONObject(TEMPLATE_LIST);
        }else{
            templateJson = new JSONObject();
        }
        if(list == null){
            list = new JSONObject();
        }

        HashMap<String, PenPalTemplate> templates = new HashMap<String, PenPalTemplate>();
        // remove entry from list if no entity found on file system
        if (list.length() > 0) {
            for (int i = 0; i < list.names().length(); i++) {
                String path = list.names().optString(i);
                if (Utility.getFolder(getTemplatePath(path)) == null) {
                    list.remove(path);
                }
            }
        }
        // add entry to list if list has no entry
        HashMap<String, String> files = readTemplatesFolder(getTemplatePath(null));
        //Log.i(TAG, "> PenpalTemplateManager template " + getTemplatePath(null)
        //         + "\n list key: " + TextUtils.join(",", files.keySet())
        //         + "\n values: " + TextUtils.join(",", files.values())
        // );
        try {
            for (String path : files.keySet()) {
                PenPalTemplate template;
                JSONObject entry = list.optJSONObject(path);
                if (entry != null) {
                    // 更新 MD5
                    String md5 = entry.optString(TEMPLATE_FOLDER_MD5);
                    if (md5 == null || !md5.equalsIgnoreCase(files.get(path))) {
                        entry.put(TEMPLATE_FOLDER_MD5, files.get(path));
                    }
                    template = new PenPalTemplate(templateFolder, path, entry);
                    if (entry != null) list.put(path, entry);
                    //Log.i(TAG, "> updateTemplates update " + list);
                } else {
                    // 建立新的 entry
                    template = getNewTemplate(path, files.get(path));
                    if (template != null) list.put(template.getKey(), template.getTemplate());
                    //Log.i(TAG, "> updateTemplates add " + list);
                }
                templates.put(path, template);
            }
            templateJson.put(TEMPLATE_LIST, list);
            //Log.i(TAG, "> updateTemplates\n" + (templateJson == null || templateJson.length() == 0 ? "" : JsonFormatter.format(templateJson)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return templates;
    }

    private HashMap<String, String> readTemplatesFolder(String folderString) {
        File folder = Utility.getFolder(folderString);
        HashMap<String, String> files = new HashMap<String, String>();
        if (folder != null) {
            String[] fileStrings = folder.list();
            for (String fileString : fileStrings) {
                String subfolderPath = folderString + "/" + fileString;
                File subfolder = Utility.getFolder(subfolderPath);
                if (subfolder != null) {
                    String subfolderJsonPath = folderString + "/" + fileString + "/" + PenPalTemplate.TEMPLATE_PROFILE;
                    File jsonfile = Utility.getFile(subfolderJsonPath);
                    if (jsonfile != null) {
                        files.put(subfolderPath.replace(templateFolder + "/", ""), Utility.getFolderMD5(subfolder, false, PenPalTemplate.TEMPLATE_PROFILE));
                    }
                }
            }
            //Log.i(TAG,"> PenpalTemplateManager files " + files);
        }
        return files;
    }

    public boolean addTemplate(PenPalTemplate template) {
        if (templates != null) {
            try {
                templates.put(template.getKey(), template);
                templateJson.put(TEMPLATE_LIST, getTemplatesJson());
                save();
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public PenPalTemplate getNewTemplate(String path, String md5) {
        try {
            Uri uri = Uri.parse(path);
            File folder = Utility.getFolder(getTemplatePath(path));
            String fileFullname = uri.getLastPathSegment();
            String foldername = path.replace(fileFullname, "");
            String filename = fileFullname;
            if (foldername.length() > 0) foldername = foldername.substring(0, foldername.length() - 1);
            if (filename.length() > 0) filename = filename.split("\\.")[0];

            PenPalTemplate entry = new PenPalTemplate(templateFolder, path, new JSONObject());
            entry
                    .setName(filename)
                    .setFileName(fileFullname)
                    .setSourceFrom("")
                    .setFileLocation(foldername)
                    .setFolderMD5(md5 == null ? Utility.getMD5(folder) : md5);

            return entry;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clean() {
        try {
            if(templateJson != null){
                while (templateJson.keys().hasNext()) {
                    templateJson.put(templateJson.keys().next(), null);
                }
                templateJson = null;
                templateProfile = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        //Log.w(TAG,"> templateManager destroy");
        clean();
    }

}
