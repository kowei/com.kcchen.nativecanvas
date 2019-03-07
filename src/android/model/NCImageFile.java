package com.kcchen.nativecanvas.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.kcchen.drawingpdf.EscapeUtils;
import com.kcchen.nativecanvas.enums.ASSET_TYPE;
import com.kcchen.nativecanvas.filters.NeuralStyle;
import com.kcchen.nativecanvas.utils.Utility;

import org.json.JSONObject;

import java.io.File;

import static com.kcchen.nativecanvas.svg.SVGHelper.noContext;

/**
 * Created by kowei on 2017/12/26.
 */

public class NCImageFile{
    protected static final String TAG = NCImageFile.class.getSimpleName();
    private static final String IMAGEFILE_TYPE = "ImagefileType";
    private static final String IMAGEFILE_FILE = "ImagefileFile";
    private Activity activity;

    private ASSET_TYPE type;
    private File file;
    private Bitmap bitmap;
    private Object exportData;
    private NeuralStyle neuralStyle;

    public NCImageFile(Activity activity, ASSET_TYPE type, String filepath) {
        this.type = type;
        this.activity = activity;
        init(filepath);
    }

    public NCImageFile(JSONObject data) {
        importData(data);
    }

    private void init(String filepath) {
        if(neuralStyle == null && activity != null) neuralStyle = new NeuralStyle(activity);

        if(filepath != null) {
            Uri uri = Uri.parse(filepath);
            if (uri == null) {
                uri = Uri.parse(EscapeUtils.encodeURIComponent(filepath));
            }

            if (uri != null) {
                String path = uri.getPath();
                if(path != null) {
                    file = new File(path);
                }
            }
        }

        getBitmap();
    }

    public boolean isValid() {
        // Log.w(TAG, "NCImageFile "
        //         + "\n file:" + file
        //         + "\n exists:" + (file == null ? "" : file.exists())
        //         + "\n canRead:" + (file == null ? "" : file.canRead())
        //         + "\n isFile:" + (file == null ? "" : file.isFile())
        // );
        return (file != null && file.exists() && file.canRead() && file.isFile()) || type.isEmbed();
    }

    public Object getExportData() {
        return exportData;
    }

    public boolean isCopyFile(){
        if(exportData != null && exportData instanceof File){
            return true;
        }
        return false;
    }

    public boolean isExportToImage(){
        if(exportData != null && exportData instanceof Bitmap){
            return true;
        }
        return false;
    }

    public boolean isExportToText(){
        if(exportData != null && exportData instanceof String){
            return true;
        }
        return false;
    }

    public String getFileName(String bookName){
        String ext = type.key().replace("_64","");
        return PenpalAssetManager.ASSET_FOLDER + "/" + bookName + "_" + System.currentTimeMillis() + "." + ext;
    }

    public String processAsset(PenpalAssetManager assetManager) {
        String filename = null;
        boolean result = true;
        boolean isFromTemp = false;
        try{
            if (isCopyFile()) {
                File sourceFile = (File) exportData;
                if (sourceFile != null) {
                   isFromTemp = sourceFile.getAbsolutePath().matches(".*/cache/.*")
                            || sourceFile.getAbsolutePath().matches(".*/temp/.*")
                            || sourceFile.getAbsolutePath().matches(".*/tmp/.*")
                    ;
                    String sourceMD5 = Utility.getMD5(sourceFile);
                    PenpalAsset asset = assetManager.getAssetByMD5(sourceMD5);

                    if (asset == null) {
                        // asset manager沒有此資源
                        Uri uri = Uri.parse(sourceFile.getAbsolutePath());
                        filename = PenpalAssetManager.ASSET_FOLDER + "/" + uri.getLastPathSegment();
                        File destFile = new File(assetManager.getAssetPath(filename));
                        if (destFile.exists()) {
                            // 寫入之處有同名檔案，更改寫入檔名
                            filename = getFileName(assetManager.getBook().getBookName());
                            destFile = new File(assetManager.getAssetPath(filename));
                        }
                        result &= copyFile(assetManager, isFromTemp, filename, sourceFile, destFile);
                    } else {
                        // asset manager已經有此資源。不複製，不覆蓋，取得資源名稱
                        Log.e(TAG, "> copy file existed");
                        filename = asset.getKey();
                        if(isFromTemp) sourceFile.delete();
                        file = assetManager.getAssetFile(asset);
                    }
                }
            }
            if (isExportToImage()) {
                filename = getFileName(assetManager.getBook().getBookName());
                Bitmap bitmap = (Bitmap) exportData;
                if (bitmap != null || !bitmap.isRecycled()) {
                    File destFile = new File(assetManager.getAssetPath(filename));
                    result &= Utility.saveImage(destFile, Bitmap.CompressFormat.PNG, 100, bitmap);
                    if(result) {
                        file = destFile;
                        assetManager.addAsset(assetManager.getNewAsset(filename,null));
                        //Log.e(TAG, "> save bitmap to " + destFile);
                    }
                } else {
                    result &= false;
                    //Log.e(TAG, "> save bitmap invalid");
                }
            }
            if(isExportToText()){
                filename = getFileName(assetManager.getBook().getBookName());
                String data = (String) exportData;
                if(data != null){
                    File destFile = new File(assetManager.getAssetPath(filename));
                    result &= Utility.saveTextFile(destFile, data);
                    if(result) {
                        file = destFile;
                        assetManager.addAsset(assetManager.getNewAsset(filename,null));
                        //Log.e(TAG, "> save text to " + destFile);
                    }
                }else{
                    result &= false;
                    //Log.e(TAG, "> save text invalid");
                }
            }
            //Log.e(TAG, "> processAsset: " + assetManager.getAssetPath("")
            //        + "\n isCopyFile:" + isCopyFile()
            //        + "\n isExportToImage:" + isExportToImage()
            //        + "\n isExportToText:" + isExportToText()
            //        + "\n return filename:" + filename
            //        + "\n return isFromTemp:" + isFromTemp
            //);
        }catch (Exception e){
            result &= false;
            e.printStackTrace();
        }

        if(result) {
            if(type.isEmbed() && file != null) {
                type.clearData();
                type = ASSET_TYPE.get(type.key().replace("_64",""));
            }
            return filename;
        }else {
            return null;
        }
    }

    private boolean copyFile(PenpalAssetManager assetManager, boolean isFromTemp, String filename, File sourceFile, File destFile) {
        boolean result = true;
        if(isFromTemp){
            result &= sourceFile.renameTo(destFile);
            //Log.e(TAG, "> move file");
        }else{
            result &= Utility.copyFile(sourceFile, destFile);
            //Log.e(TAG, "> copy file");
        }
        if(result) {
            PenpalAsset asset = assetManager.getNewAsset(filename, null);
            if(!isFromTemp) asset.setSourceFrom(sourceFile.getAbsolutePath());
            file = destFile;
            assetManager.addAsset(asset);
            //Log.e(TAG, "> copy file success and update to profile");
        }
        return result;
    }

    public Bitmap getBitmap() {
        if (isValid() && bitmap == null) {
            switch (type) {
                case PNG:
                case JPEG:
                    if(file != null) bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    //Log.e(TAG, "> getBitmap PNG JPEG image: " + file
                    //        + "\n bitmap:" + bitmap
                    //        + "\n Width:" + (bitmap == null ? "" : bitmap.getWidth())
                    //        + "\n Height:" + (bitmap == null ? "" : bitmap.getHeight())
                    //);
                    if (bitmap != null) {
                        exportData = file;
                    }
                    break;
                case GIF:
                    break;
                case SVG:
                    try {
                        //Log.e(TAG, "> getBitmap SVG image: " + file);
                        if(file != null) bitmap = noContext().open(file).getBitmap();
                        if (bitmap != null) {
                            exportData = file;
                        }
                        //Log.e(TAG, "> getBitmap SVG image: " + bitmap
                        //        + "\n Width:" + (bitmap == null ? "" : bitmap.getWidth())
                        //        + "\n Height:" + (bitmap == null ? "" : bitmap.getHeight())
                        //);
                    } catch (Exception e) {
                        //Log.e(TAG, "> getBitmap SVG image: " + bitmap);

                        e.printStackTrace();
                    }
                    break;
                case PNG_64:
                case JPEG_64:
                    // Log.e(TAG,"> NCN Raster image base64: " + type.getData());
                    byte[] decodedString = Base64.decode(type.getData(), Base64.DEFAULT);
                    if (decodedString != null && decodedString.length > 0) {
                        bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        exportData = bitmap;
                    }
                    break;
                case GIF_64:
                    break;
                case SVG_64:
                    try {
                        // Log.e(TAG, "> NCN SVG image base64: " + type.getData());
                        String svgString = EscapeUtils.decodeURIComponent(type.getData());
                        if (svgString != null && svgString.length() > 0) {
                            bitmap = noContext().open(svgString).getBitmap();
                            exportData = svgString;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case UNKNOWN:
                    break;
            }
//            if(bitmap != null) {
//                Bitmap image = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4, true);
//                if(image!= null && !bitmap.isRecycled()){
//                    bitmap.recycle();
//                    bitmap = image;
//                }
//                bitmap = neuralStyle.stylize(bitmap);
//            }
        }

        // Log.e(TAG, "> NCN image: "
        //         + "\n bitmap:" + bitmap
        //         + "\n Width:" + (bitmap == null ? "" : bitmap.getWidth())
        //         + "\n Height:" + (bitmap == null ? "" : bitmap.getHeight()));
        return bitmap;
    }

    public void release(){
        //Log.w(TAG,"> release ");
        if(bitmap != null && !bitmap.isRecycled()){
            Log.wtf(TAG,"> BITMAP_RECYCLED " + bitmap);
            bitmap.recycle();
        }
        bitmap = null;
        if(exportData != null && exportData instanceof Bitmap){
            Bitmap bitmap = (Bitmap) exportData;
            if(!bitmap.isRecycled()){
                Log.wtf(TAG,"> BITMAP_RECYCLED " + bitmap);
                bitmap.recycle();
            }
            exportData = null;
        }
    }

    public void destroy(){
        //Log.w(TAG,"> destroy ");
        release();
        this.file = null;
    }

    public JSONObject exportData() {
        JSONObject data = new JSONObject();
        try {
            data.put(IMAGEFILE_TYPE, type.key());
            data.put(IMAGEFILE_FILE, file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        try {
            setType(data.optString(IMAGEFILE_TYPE));
            init(data.optString(IMAGEFILE_FILE));
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    @Override
    public int hashCode() {
        return type.key().hashCode() + (file == null?0:file.getAbsolutePath().hashCode());
    }

    public void setType(String type) {
        if(type != null) this.type = ASSET_TYPE.get(type);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
