package com.kcchen.nativecanvas.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kcchen.drawingpdf.EscapeUtils;
import com.kcchen.inappbrowser.util.DocumentsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 類別: Utility
 * <br>功能: 常用工具。
 * <br>說明:
 * <p>
 * 常用工具。
 * </p>
 * <p>日期: 2014/11/11 下午2:37:46
 * </p><p></p>
 *
 * @author KC Chen <kowei.chen@gmail.com>
 * @print public
 * @since JDK 1.6, Smart Router SDK 1.0
 */
public class Utility {
    public static final String TAG = "Utility";
    private static final String STORAGE_IMAGE_ICON = "DmSip";
    private static final String FILE_EXTENSION_JPEG = "jpeg";
    public static final String REGISTER_MESSAGE_BOX = "RegisterMessageBox";
    public static final String MESSAGE_BOX = "MessageBox";
    public static final String CLASS_ID = "ClassID";
    private static Pattern fileExtensionPattern = Pattern.compile("([0-9]+)(\\.)([a-zA-Z]+)");
    private static Pattern filePattern = Pattern.compile("(.+)(\\.)([a-zA-Z]+)");
    private static Pattern numberPattern = Pattern.compile("[0-9]*\\.[0-9]+");
    private static Pattern digitPattern = Pattern.compile("[0-9]{0,19}");
    public static final DecimalFormat sizeFormater = new DecimalFormat("#,###,###");//("#,###,###.0");
    public static final DecimalFormat twoDigitFormater = new DecimalFormat("00");//("#,###,###.0");
    public static final DecimalFormat f = new DecimalFormat("0000.00");

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
    private static SimpleDateFormat timeFormater = new SimpleDateFormat("kk:mm");


    private static LogBuilder logger = LogBuilder.with(TAG, Log.ERROR);


    public Utility() {
    }

    public static String encodeToBase64(Bitmap image, CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static boolean isNumber(String str) {
        return numberPattern.matcher(str).matches();
    }

    public static boolean isDigit(String str) {
        synchronized (str) {
            Matcher digitMatcher = digitPattern.matcher(str);
            return digitMatcher.matches();
        }
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        if (bmpOriginal != null) {
            int width, height;
            height = bmpOriginal.getHeight();
            width = bmpOriginal.getWidth();

            Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Config.RGB_565);
            Canvas c = new Canvas(bmpGrayscale);
            Paint paint = new Paint();
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
            paint.setColorFilter(f);
            c.drawBitmap(bmpOriginal, 0, 0, paint);
            return bmpGrayscale;
        } else {
            return bmpOriginal;
        }
    }

    public static Bitmap toGrayscale(Drawable drawerOriginal) {
        return toGrayscale(drawableToBitmap(drawerOriginal));
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    public static String hostnameToIp(String hostname) {
        InetAddress x;
        String ip = null;
        try {
            x = InetAddress.getByName(hostname);
            ip = x.getHostAddress();//得到字符串形式的ip地址
        } catch (UnknownHostException e) {
            return hostname;
        } catch (Exception e) {
            return hostname;
        }
        return ip;
    }

    public static String ipToHostname(String ip) {
        InetAddress x;
        String hostname = null;
        try {
            x = InetAddress.getByName(hostname);
            ip = x.getHostAddress();//得到字符串形式的ip地址
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static String printList(ArrayList<String> list, String message) {
        if (list != null) {
            StringBuilder listString = new StringBuilder();
            int i = 1;
            for (String string : list) {
                listString.append(i + ". " + string + "\n");
                i++;
            }
            return message + ":(" + list.size() + ")\n" + listString.toString();
        } else {
            return "The list is empty";
        }
    }

    public static boolean saveImage(String name, CompressFormat format, int quality, Bitmap img) {
        if(img == null || name == null) {
            //Log.w(TAG,"> NCN saveImage file:" + name + " Image:" + img);
            return false;
        }
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
            img.compress(format, quality, byteOutStream);
        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        }


        if (isExternalStorageWritable()) {
            try {
                File fileFolder = getImageStorageDir(null);
                String filePath = fileFolder.toString() + "/" + name + "." + FILE_EXTENSION_JPEG;
                Log.e(TAG, "saveBitmapToJPEG:" + filePath);
                FileOutputStream output = new FileOutputStream(filePath);
                output.write(byteOutStream.toByteArray());
                output.flush();
                output.close();
                return true;
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean saveThumbnailImage(File file, CompressFormat format, int quality, Bitmap originImage, float shrink) {
        if(originImage == null || file == null) {
            //Log.w(TAG,"> NCN saveThumbnailImage file:" + file + " originImage:" + originImage);
            return false;
        }
        Bitmap image;
        Log.w(TAG,"> saveThumbnailImage " + shrink + " " + ((int) (originImage.getWidth() * shrink))
                + "\n originImage:" + originImage.getWidth() + " x " + originImage.getHeight()
        );
        if(originImage.getWidth() < 1/shrink || originImage.getHeight() < 1/shrink){
            image = originImage.copy(originImage.getConfig(),true);
        }else{
            image = Bitmap.createScaledBitmap(originImage, (int) (originImage.getWidth() * shrink), (int) (originImage.getHeight() * shrink), true);
        }
        if(image == null) return false;
        Log.w(TAG,"> saveThumbnailImage " + shrink + " " + ((int) (originImage.getWidth() * shrink))
                + "\n originImage:" + originImage.getWidth() + " x " + originImage.getHeight()
                + "\n shrinkImage:" + image.getWidth() + " x " + image.getHeight()
        );

        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
            image.compress(format, quality, byteOutStream);
        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        }

        if (isExternalStorageWritable()) {
            try {
                Log.e(TAG, "saveBitmapTo:" + format.name() + " " + file.getAbsoluteFile());
                FileOutputStream output = new FileOutputStream(file.getAbsoluteFile());
                output.write(byteOutStream.toByteArray());
                output.flush();
                output.close();
                Log.wtf(TAG,"> BITMAP_RECYCLED " + image);
                image.recycle();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                Log.wtf(TAG,"> BITMAP_RECYCLED " + image);
                image.recycle();
                return false;
            }
        } else {
            Log.wtf(TAG,"> BITMAP_RECYCLED " + image);
            image.recycle();
            return false;
        }
    }

    public static boolean saveImage(File file, CompressFormat format, int quality, Bitmap bitmap) {
        if(bitmap == null || file == null) {
            Log.w(TAG, "> NCN saveImage file:" + file + " Image:" + bitmap
                    + "\n image:" + (bitmap != null ? bitmap.getWidth() + " x " + bitmap.getHeight() : "")
            );
            return false;
        }

        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
            bitmap.compress(format, quality, byteOutStream);
        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        }


        if (isExternalStorageWritable()) {
            try {
                Log.e(TAG, "saveBitmapTo:" + format.name() + " " + file.getAbsoluteFile());
                FileOutputStream output = new FileOutputStream(file.getAbsoluteFile());
                output.write(byteOutStream.toByteArray());
                output.flush();
                output.close();
                return true;
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean saveImage(String name, Context context, CompressFormat format, int quality, int resourceId) {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        try {
            Drawable drawable = context.getResources().getDrawable(resourceId);
            Bitmap img = drawableToBitmap(drawable);
            img.compress(format, quality, byteOutStream);
        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        }


        if (isExternalStorageWritable()) {
            try {
                File fileFolder = getImageStorageDir(null);
                String filePath = fileFolder.toString() + "/" + name + "." + format.name().toLowerCase();
                Log.e(TAG, "saveImage:" + filePath);
                FileOutputStream output = new FileOutputStream(filePath);
                output.write(byteOutStream.toByteArray());
                output.flush();
                output.close();
                return true;
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static Bitmap loadJPEG(String name, Context context) {
        Bitmap bitmap = null;
        if (isExternalStorageReadable()) {

            try {
                File fileFolder = getImageStorageDir(null);
                String filePath = fileFolder.toString() + "/" + name + "." + FILE_EXTENSION_JPEG;
                File file = new File(filePath);
                if (file.isFile()) {
                    bitmap = BitmapFactory.decodeFile(filePath);
                } else {
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "loadJPEG error:" + e.toString());
                return null;
            }
        }
        return bitmap;
    }

    public static Bitmap getBitmapFromAsset(final Context context, final String filePath) {
        final AssetManager assetManager = context.getAssets();

        Bitmap bitmap = null;
        try {
            final InputStream inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static File getFile(String path) {
        File result = null;
        Uri fileUri = Uri.parse(path);
        if (fileUri != null) {
            File file = new File(fileUri.getPath());
            if(file != null && file.exists() && file.canRead() && file.isFile()){
                result = file;
            }
        }

        return result;
    }

    public static File getFolder(String path) {
        File result = null;
        Uri fileUri = Uri.parse(path);
        if (fileUri != null) {
            File file = new File(fileUri.getPath());
            if(file != null && file.exists() && file.canRead() && file.isDirectory()){
                result = file;
            }
        }

        return result;
    }

    public static long gteBitmapHash(Bitmap bmp){
        long hash = 31; //or a higher prime at your choice
        for(int x = 0; x < bmp.getWidth(); x++){
            for (int y = 0; y < bmp.getHeight(); y++){
                hash *= (bmp.getPixel(x,y) + 31);
            }
        }
        return hash;
    }

    public static boolean checkMD5(String md5, File updateFile) {
        if (TextUtils.isEmpty(md5) || updateFile == null) {
            Log.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = getMD5(updateFile);
        if (calculatedDigest == null) {
            Log.e(TAG, "calculatedDigest null");
            return false;
        }

        Log.v(TAG, "Calculated digest: " + calculatedDigest);
        Log.v(TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String getMD5(byte[] bytes) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        try {
            return getMD5String(digest.digest(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        }
    }

    public static String getFolderMD5(File dirToHash, boolean includeHiddenFiles, String... excepts) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        assert (dirToHash.isDirectory());
        Vector<FileInputStream> fileStreams = new Vector<>();

        //System.out.println("Found files for hashing:");
        collectInputStreams(dirToHash, fileStreams, includeHiddenFiles, excepts == null ? new ArrayList<>() : Arrays.asList(excepts));

        SequenceInputStream seqStream =
                new SequenceInputStream(fileStreams.elements());

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = seqStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return getMD5String(digest.digest());
        } catch (IOException e) {
            throw new RuntimeException("Error reading files to hash in "
                    + dirToHash.getAbsolutePath(), e);
        } finally {
            try {
                seqStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    private static void collectInputStreams(File dir,
                                            List<FileInputStream> foundStreams,
                                            boolean includeHiddenFiles,
                                            List<String> excepts) {

        File[] fileList = dir.listFiles();
        Arrays.sort(fileList,               // Need in reproducible order
                new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return f1.getName().compareTo(f2.getName());
                    }
                });

        for (File f : fileList) {
            if (!includeHiddenFiles && f.getName().startsWith(".") || excepts.contains(f.getName())) {
                // Skip it
                //Log.wtf(TAG,"> skip " + f.getName());
            }
            else if (f.isDirectory()) {
                collectInputStreams(f, foundStreams, includeHiddenFiles, excepts);
            }
            else {
                try {
                    //System.out.println("\t" + f.getAbsolutePath());
                    foundStreams.add(new FileInputStream(f));
                }
                catch (FileNotFoundException e) {
                    throw new AssertionError(e.getMessage()
                            + ": file should never not be found!");
                }
            }
        }

    }

    public static String getMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            return getMD5String(digest.digest());
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }

    private static String getMD5String(byte[] md5Bytes) {
        String returnVal = "";
        for (int i=0; i < md5Bytes.length; i++)
        {
            returnVal += Integer.toString( ( md5Bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        if(returnVal.isEmpty()){
            return null;
        }else {
            return returnVal.toUpperCase();
        }
    }

    public static String readJsonFile(File file) {
        String jsonString = null;
        InputStream inputStream = null;
        BufferedReader jsonReader = null;
        if (file != null && file.exists() && file.isFile()) {
            try {
                //Load File
                inputStream = new FileInputStream(file);
                jsonReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder jsonBuilder = new StringBuilder();
                for (String line = null; (line = jsonReader.readLine()) != null; ) {
                    jsonBuilder.append(line).append("\n");
                }

                jsonString = jsonBuilder.toString();
            } catch (Exception e) {
                Log.e(TAG, "file not found");
            } finally {
                try {
                    inputStream.close();
                    jsonReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonString;
    }

    public static String readTextFile(File file){
        String string = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        if (file != null && file.exists() && file.isFile()) {
            try {
                //Load File
                inputStream = new FileInputStream(file);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder stringBuilder = new StringBuilder();
                for (String line = null; (line = bufferedReader.readLine()) != null; ) {
                    stringBuilder.append(line).append("\n");
                }

                string = stringBuilder.toString();
            } catch (Exception e) {
                Log.e(TAG, "file not found");
            } finally {
                try {
                    inputStream.close();
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return string;
    }

    public static boolean copyFile(File srcFile, File destFile) {
        String string = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        if ((srcFile != null && srcFile.exists() && srcFile.isFile())
                && (destFile.getParentFile().exists() && destFile.getParentFile().canWrite())) {
            try {
                inputStream = new FileInputStream(srcFile);
                outputStream = new FileOutputStream(destFile);

                byte[] buf = new byte[102400];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }

            } catch (Exception e) {
                Log.e(TAG, "file not found");
            } finally {
                try {
                    inputStream.close();
                    outputStream.close();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[102400];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean overwriteTextFile(File file, String text){
        boolean result = false;
        OutputStream outputStream = null;
        if(file.exists() && file.isFile()){
            try {
                file.delete();
                file.createNewFile();
                outputStream = new FileOutputStream(file);
                outputStream.write(text.getBytes());
                //Log.e(TAG, "overwriteTextFile " + json.length() + " \n" + json);
            } catch (Exception e) {
                Log.e(TAG, "file not found");
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                        result = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    public static boolean saveTextFile(File file, String text){
        boolean result = false;
        OutputStream outputStream = null;
        if(file.getParentFile().exists() && file.getParentFile().canWrite()){
            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(text.getBytes());
                //Log.e(TAG, "overwriteTextFile " + json.length() + " \n" + json);
            } catch (Exception e) {
                Log.e(TAG, "file not found");
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                        result = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    public static String parsePath(String path){
        Uri folderUri = Uri.parse(path);
        if(folderUri == null){
            folderUri = Uri.parse(EscapeUtils.encodeURIComponent(path));
        }

        if(folderUri != null){
            return folderUri.getPath();
        }
        return null;
    }

    public static Uri isFolder(String path){
        boolean result = false;

        Uri folderUri = Uri.parse(path);
        if(folderUri == null){
            folderUri = Uri.parse(EscapeUtils.encodeURIComponent(path));
        }

        if(folderUri != null){
            File folder = new File(folderUri.getPath());
            result = folder.exists() && folder.isDirectory();
        }

        if(result) return folderUri;
        return null;
    }

    public static Uri isFile(String path){
        boolean result = false;

        Uri fileUri = Uri.parse(path);
        if(fileUri == null){
            fileUri = Uri.parse(EscapeUtils.encodeURIComponent(path));
        }

        if(fileUri != null){
            File file = new File(fileUri.getPath());
            result = file.exists() && file.isFile();
        }

        if(result) return fileUri;
        return null;
    }

    public static Uri getUri(String path){
        boolean result = false;

        Uri fileUri = Uri.parse(path);
        if(fileUri == null){
            fileUri = Uri.parse(EscapeUtils.encodeURIComponent(path));
        }

        if(fileUri != null){
            return fileUri;
        }
        return null;
    }

    /**
     * 合併兩個json串，只增不減
     * @param object1
     * @param object2
     * @return
     */
    public static JSONObject mergeJson(Object object1,Object object2){
        if (object1 == null &&object2 == null){
            return null;
        }
        try {
            if (object1 == null){
                return new JSONObject(object2.toString());
            }
            if (object2 == null){
                return new JSONObject(object1.toString());
            }
            //Log.i(TAG,"> merging...");
            JSONObject jsonObject1 = new JSONObject(object1.toString());
            JSONObject jsonObject2= new JSONObject(object2.toString());
            Iterator iterator = jsonObject2.keys();
            while (iterator.hasNext()){
                String key = (String) iterator.next();
                Object value2 = jsonObject2.get(key);
                if (jsonObject1.has(key)){
                    Object value1 = jsonObject1.get(key);
                    if (!(value1 instanceof JSONObject)){
                        jsonObject1.put(key,value2);
                    }else {
                        jsonObject1.put(key,mergeJson(value1,value2));
                    }
                }else {
                    jsonObject1.put(key,value2);
                }
            }
            return jsonObject1;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateJson(JSONObject baseData, JSONObject updateData){
        if (baseData == null || updateData == null){
            return false;
        }
        boolean isUpdate = false;
        try {

            Iterator iterator = updateData.keys();
            while (iterator.hasNext()){
                String key = (String) iterator.next();
                Object updateValue = updateData.get(key);
                if (baseData.has(key)){
                    Object baseValue = baseData.get(key);
                    if ( baseValue instanceof JSONObject && updateValue instanceof JSONObject){
                        isUpdate |= updateJson((JSONObject)baseValue,(JSONObject)updateValue);
                    }else {
                        isUpdate = true;
                        baseData.put(key,updateValue);
                    }
                }
            }
            return isUpdate;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static File getAccountIconFile(String accountId) {
        try {
            File fileFolder = getImageStorageDir(null);
            File[] files;
            files = fileFolder.listFiles();
            Matcher fileMatcher;
            int index = -1;
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
//                    Log.e(TAG,"==> file:"+files[i].getName());
                    fileMatcher = filePattern.matcher(files[i].getName());
                    if (fileMatcher.matches()) {
//                        Log.e(TAG,"files match:"+accountId+"/"+fileMatcher.group(0) + "|" + fileMatcher.groupCount() + "|" + fileMatcher.group(1) + "|" + fileMatcher.group(2) + "|" + fileMatcher.group(3));
                        if (accountId.equals(fileMatcher.group(1))) {
                            index = i;
                            break;
                        }
                    }
                }
                if (files.length > 0 && index >= 0) {
                    Log.e(TAG, "matched file:" + files[index].getName());
                    return files[index];
                }
                return null;
            } else {
                return null;
            }

        } catch (Exception e) {
            Log.e(TAG, "getAccountIconFile error:" + e.toString());
            return null;
        }
    }

    public static Bitmap loadImage(File lastFile) {
        Bitmap bitmap = null;
        if (isExternalStorageReadable()) {
            try {
                if (lastFile != null && lastFile.isFile()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inSampleSize = 4;
                    bitmap = BitmapFactory.decodeFile(lastFile.getAbsolutePath(), options);
                } else {
                    return null;
                }
            } catch (Exception e1) {
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeFile(lastFile.getAbsolutePath());
                } catch (Exception e2) {
                    Log.e(TAG, "loadImage error:" + e2.toString());
                }
            }
        }
        if (bitmap != null) return bitmap;
        return null;
    }

    public static File getLastFile(File fileFolder) {
        File[] files;
        files = fileFolder.listFiles();
        Matcher fileMatcher;

        Long recentFile = 0L;
        int index = 0;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
//                Log.e(TAG,"files:"+files[i].getName());
                fileMatcher = fileExtensionPattern.matcher(files[i].getName());
                if (fileMatcher.matches()) {
                    if (recentFile < Long.valueOf(fileMatcher.group(1))) {
                        index = i;
                        recentFile = Long.valueOf(fileMatcher.group(1));
                    }
//                    Log.e(TAG,"files match:"+fileMatcher.group(0) + "|" + fileMatcher.groupCount() + "|" + fileMatcher.group(1) + "|" + fileMatcher.group(2) + "|" + fileMatcher.group(3));
                }
            }
            if (files.length > 0) {
                Log.e(TAG, "recent file:" + files[index].getName());
                return files[index];
            }
            return null;
        } else {
            return null;
        }

    }

    public static File getImageStorageDir(String subFolder) {
        // Get the directory for the app's private pictures directory.
        File file = null;
        if (subFolder != null) {
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + STORAGE_IMAGE_ICON + "/" + subFolder, "");
        } else {
            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + STORAGE_IMAGE_ICON, "");
        }
        if (file != null) {
            if (file.mkdirs()) {
                Log.d(TAG, "getImageStorageDir Directory created:" + file.getPath());
            } else {
                Log.d(TAG, "getImageStorageDir Directory not created or existed:" + file.getPath());
            }
        }
        return file;
    }

    public static File getImageStorageFile(String subFolder, String fileName, CompressFormat format) {
        // Get the directory for the app's private pictures directory.
        return new File(getImageStorageDir(subFolder).toString() + "/" + fileName + "." + format.name());
    }

    public static File getAccountStorageDir(Context context, String accountId) {
        // Get the directory for the app's private pictures directory.
        File fileFolder = null;
        if (context.getExternalFilesDir(STORAGE_IMAGE_ICON) != null)
            fileFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + STORAGE_IMAGE_ICON + "/" + accountId, "");
        if (!fileFolder.mkdirs()) {
            Log.d(TAG, "getAccountStorageDir Directory not created:" + fileFolder.getPath());
        } else {
            Log.e(TAG, "getAccountStorageDir Directory is existed:" + fileFolder.getPath());
        }
        return fileFolder;
    }

    public static File getAccountStorageFile(Context context, String accountId, String fileName, String extension) {
        // Get the directory for the app's private pictures directory.
        if (extension != null || !extension.isEmpty()) fileName = fileName + "." + extension;
        File file = null, fileFolder = null;
        if (context.getExternalFilesDir(STORAGE_IMAGE_ICON) != null)
            fileFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + STORAGE_IMAGE_ICON + "/" + accountId, "");
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + STORAGE_IMAGE_ICON + "/" + accountId, fileName);
        if (!fileFolder.mkdirs()) {
            Log.d(TAG, "getAccountStorageFile Directory not created:" + fileFolder.getPath());
        } else {
            Log.e(TAG, "getAccountStorageFile Directory is existed:" + fileFolder.getPath());
        }
        return file;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getDeviceID(Context context) {

        /*
         * String Return_DeviceID =
         * USERNAME_and_PASSWORD.getString(DeviceID_key,"Guest"); return
         * Return_DeviceID;
         */

        /*
         * String Return_DeviceID =
         * USERNAME_and_PASSWORD.getString(DeviceID_key,"Guest"); return
         * Return_DeviceID;
         */

        TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        String m_szImei = TelephonyMgr.getDeviceId(); // Requires
        // 1 READ_PHONE_STATE

        // 2 compute DEVICE ID
        String m_szDevIDShort = "35"
                + // we make this look like a valid IMEI
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10
                + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
                + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
                + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
                + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
                + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
                + Build.USER.length() % 10; // 13 digits
        // 3 android ID - unreliable
        String m_szAndroidID = "";// Secure.getString(this.getContentResolver(),
        // Secure.ANDROID_ID);
        // 4 wifi manager, read MAC address - requires
        // android.permission.ACCESS_WIFI_STATE or comes as null
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
        // 5 Bluetooth MAC address android.permission.BLUETOOTH required
        // BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
        // m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // String m_szBTMAC = "";
        // if (m_BluetoothAdapter != null)
        //  m_szBTMAC = m_BluetoothAdapter.getAddress();
        // System.out.println("m_szBTMAC " + m_szBTMAC);

        // 6 SUM THE IDs
        String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID
                + m_szWLANMAC; // + m_szBTMAC;
        System.out.println("m_szLongID " + m_szLongID);
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        byte p_md5Data[] = m.digest();

        String m_szUniqueID = new String();
        for (int i = 0; i < p_md5Data.length; i++) {
            int b = (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper
            // padding)
            if (b <= 0xF)
                m_szUniqueID += "0";
            // add number to string
            m_szUniqueID += Integer.toHexString(b);
        }
        m_szUniqueID = m_szUniqueID.toUpperCase();

        Log.d("--DeviceI--", m_szUniqueID);
        Log.d("DeviceIdCheck", "DeviceId that generated MPreferenceActivity:"
                + m_szUniqueID);

        return m_szUniqueID;

    }

    public static String parseString(String... strings) {
        String mainString = strings[0];
        for (int i = 1; i < strings.length; i++) {
            mainString.replaceFirst("[s]", strings[i]);
        }
        return null;
    }

    /**
     * 以最省記憶體的方式讀取本地資源的圖片
     *
     * @param context
     * @return
     */
    public static Bitmap readBitMap(Context context, int resourceId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //獲取資源圖片
        try {
            InputStream is = context.getResources().openRawResource(resourceId);
            return BitmapFactory.decodeStream(is, null, opt);
        } catch (Exception e) {
            Log.e(TAG, "readBitMap error:" + e.toString());
        }
        return null;
    }

    /* Try to walk heaps, triggering crash on corruption hopefully. */
    public static void memoryProbe() {

        System.gc();
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        Double allocated = new Double(Debug.getNativeHeapAllocatedSize()) / 1048576.0;
        Double available = new Double(Debug.getNativeHeapSize()) / 1048576.0;
        Double free = new Double(Debug.getNativeHeapFreeSize()) / 1048576.0;
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
    }

    public static Bitmap convertViewToBitmap(View view, int width, int height) {
        //view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        //view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.layout(0, 0, width, height);
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    /**
     * getStatusBarHeight:get status bar height <br/>
     * android screen consist of
     * 1. status bar
     * 2. content
     * 3. navigation bar
     * <p>
     * but sometime status bar is inside navigation bar,
     * and sometime navigation bar is gone (real button)
     *
     * @param context
     * @return
     * @author kc
     * @since JDK 1.6
     */
    public static int getStatusBarHeight(Context context) {
        if (!isOnScreenSystemBar(context)) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
            return 0;
        } else {
            return 0;
        }
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * getDisplayHeight:get display height. <br/>
     * always exclude navigation bar, but not sure status bar is locate
     * in display area or in navigation bar
     *
     * @param context
     * @return
     * @author kc
     * @since JDK 1.6
     */
    public static int getDisplayHeight(Context context) {
        Rect rect = new Rect();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRectSize(rect);
        return rect.bottom - rect.top - getStatusBarHeight(context);
    }

    public static int getDisplayWidth(Context context) {
        Rect rect = new Rect();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRectSize(rect);
        return rect.right - rect.left;
    }

    public static Rect getDisplayRect(Context context) {
        Rect rect = new Rect();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRectSize(rect);
        if (!isOnScreenSystemBar(context)) {
            rect.top = getStatusBarHeight(context);
        }
        return rect;
    }

    public static int getOutsideHeight(Context context) {
        return getStatusBarHeight(context) + getNavigationBarHeight(context);
    }

    private static boolean isOnScreenSystemBar(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rawDisplayHeight = 0;
        try {
            Method getRawHeight = Display.class.getMethod("getRawHeight");
            rawDisplayHeight = (Integer) getRawHeight.invoke(display);
        } catch (Exception ex) {
        }

        int UIRequestedHeight = display.getHeight();

        return rawDisplayHeight - UIRequestedHeight > 0;
    }

    public static String convertStreamToString(InputStream is)
            throws IOException {
        //
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        //
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    /**
     * getContrastYIQ: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param hexcolor
     * @return
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static int getContrastYIQ(int hexcolor) {
        Integer r, g, b;
        r = Color.red(hexcolor);
        g = Color.green(hexcolor);
        b = Color.blue(hexcolor);
        int yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return (yiq >= 128) ? Color.BLACK : Color.WHITE;
    }

    /**
     * getContrastYIQ: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param hexcolor
     * @param light
     * @param dark
     * @return
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static int getContrastYIQ(int hexcolor, int light, int dark) {
        Integer r, g, b;
        r = Color.red(hexcolor);
        g = Color.green(hexcolor);
        b = Color.blue(hexcolor);
        int yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return (yiq >= 128) ? dark : light;
    }

    public static void printLongResult(String TAG, String string, int lineChar) {
        for (int i = 0; i < Math.ceil((float) string.length() / lineChar); i++) {
            if (lineChar * (i + 1) < string.length())
                Log.d(TAG, string.substring(lineChar * i, lineChar * (i + 1)));
            else
                Log.d(TAG, string.substring(lineChar * i));
        }
    }

//    public static String getFormatedSize(Long size, Unit unit , String pattern) {
//        if(pattern != null) sizeFormater.applyPattern(pattern);
//        String sizeUnit = "";
//        float sizeGb = (float)size/(1<<30);
//        float sizeMb = (float)size/(1<<20);
//        float sizeKb = (float)size/(1<<10);//(float)dr / (1<<30)
//        if( sizeGb >= 1){
//            size = (long) sizeGb;
//            sizeUnit = (unit == Unit.BYTE) ? "GB":"Gbit";
//        }else if(sizeMb >= 1){
//            size = (long) sizeMb;
//            sizeUnit = (unit == Unit.BYTE) ? "MB":"Mbit";
//        }else if(sizeKb >= 1){
//            size = (long) sizeKb;
//            sizeUnit = (unit == Unit.BYTE) ? "KB":"Kbit";
//        }else{
//            sizeUnit = (unit == Unit.BYTE) ? "B":"bit";
//        }
//        return sizeFormater.format(size) + " " + sizeUnit;
//    }

//    public static String getFormatedTime(Context context, long time, Unit unit, String pattern) {
//        StringBuilder timeBuilder = new StringBuilder();
//        if(pattern != null) timeFormater.applyPattern(pattern);
//        if(time > 0){
//            if(unit == Unit.TIME_MILS)
//                formatDuration(time, timeBuilder);
//            else if(unit == Unit.TIME_S)
//                formatDuration(time * 1000, timeBuilder);
//            Log.e(TAG,"time string:"+timeBuilder.toString());
//            String timeString = timeBuilder.toString().
//                    replaceAll("(\\+|\\+I|\\d+ms$)", "").
//                    replaceAll("[d|m|h|s]", ":").
//                    replaceAll(":$", "");
//            String[] timeArray = timeString.split(":");
//            for (int i = 0; i < timeArray.length; i++) {
//                timeArray[i] = twoDigitFormater.format(Integer.valueOf(timeArray[i]));
//            }
//            return TextUtils.join(":", timeArray);
//        }else
//            return context.getResources().getString(R.string.time_unknown);
//  }

//    public static String getFormatedTimeByUnit(Context context, long time, Unit unit, String[] units) {
//        StringBuilder timeBuilder = new StringBuilder();
//        if(time > 0){
//            if(unit == Unit.TIME_MILS)
//                TimeUtils.formatDuration(time, timeBuilder);
//            else if(unit == Unit.TIME_S)
//                TimeUtils.formatDuration(time * 1000, timeBuilder);
//            String timeString = timeBuilder.toString().
//                    replaceAll("(\\+|\\+I|\\d+ms$)", "").
//                    replaceAll("[d|m|h|s]", ":").
//                    replaceAll(":$", "");
//            String[] timeArray = timeString.split(":");
//            timeString = "";
//            for (int i = 0; i < timeArray.length; i++) {
//                timeString += units.length > i?timeArray[i]+units[units.length - timeArray.length + i]:timeArray[i]+":";
//                //timeArray[i] = twoDigitFormater.format(Integer.valueOf(timeArray[i]));
//            }
//            return timeString;
//        }else
//            return context.getResources().getString(R.string.time_unknown);
//    }

//    public static String getFormatedDate(Context context, long time, Unit unit, String pattern) {
//        if(pattern != null) sizeFormater.applyPattern(pattern);
//        float time2 = 0;
//        String timeParsedUnit = "";
//        if((float)time/60/60/24/30 > 1){
//            time2  = (float)time/60/60/24/30;
//            timeParsedUnit = context.getResources().getString(R.string.time_month);
//        }else if((float)time/60/60/24 > 1){
//            time2 = (float)time/60/60/24;
//            timeParsedUnit = context.getResources().getString(R.string.time_day);
//        }else{
//            time2 = (float)time/60/60;
//            timeParsedUnit = context.getResources().getString(R.string.time_hour);
//        }
//      return sizeFormater.format(time2) + " " + timeParsedUnit;
//  }

    public static FrameLayout.LayoutParams setFrameLayout(RelativeLayout view, int w, int h, int l, int t, int r, int b) {
        FrameLayout.LayoutParams fparams = null;
        if (view != null) {
            fparams = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (fparams == null) {
                fparams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                view.setLayoutParams(fparams);
            }
            fparams.width = w;
            fparams.height = h;
            fparams.setMargins(l, t, r, b);
        }
        return fparams;
    }

    public static FrameLayout.LayoutParams setFrameLayout(FrameLayout view, int w, int h, int l, int t, int r, int b) {
        FrameLayout.LayoutParams fparams = null;
        if (view != null) {
            fparams = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (fparams == null) {
                fparams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                view.setLayoutParams(fparams);
            }
            fparams.width = w;
            fparams.height = h;
            fparams.setMargins(l, t, r, b);
        }
        return fparams;
    }

    public static FrameLayout.LayoutParams setFrameLayout(View view, int w, int h, int l, int t, int r, int b) {
        FrameLayout.LayoutParams fparams = null;
        if (view != null) {
            fparams = (FrameLayout.LayoutParams) view.getLayoutParams();
            if (fparams == null) {
                fparams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                view.setLayoutParams(fparams);
            }
            fparams.width = w;
            fparams.height = h;
            fparams.setMargins(l, t, r, b);
        }
        return fparams;
    }

    public static LayoutParams setRelativeLayout(TextView text, int w, int h, int l, int t, int r, int b) {
        LayoutParams rparams = null;
        if (text != null) {
            rparams = (LayoutParams) text.getLayoutParams();
            if (rparams == null) {
                rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                text.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static ViewGroup.LayoutParams setViewLayout(View view, int w, int h) {
        ViewGroup.LayoutParams rparams = null;
        if (view != null) {
            rparams = view.getLayoutParams();
            if (rparams == null) {
                rparams = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                view.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
        }
        return rparams;
    }


    public static LayoutParams setRelativeLayout(RelativeLayout layout, int w, int h, int l, int t, int r, int b) {
        LayoutParams rparams = null;
        if (layout != null) {
            rparams = (LayoutParams) layout.getLayoutParams();
            if (rparams == null) {
                rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                layout.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static void setRelativeLayout(int w, int h, int l, int t, int r, int b, ImageView... view) {
        for (int i = 0; i < view.length; i++) {
            LayoutParams rparams = null;
            if (view[i] != null) {
                rparams = (LayoutParams) view[i].getLayoutParams();
                if (rparams == null) {
                    rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                    view[i].setLayoutParams(rparams);
                }
                rparams.width = w;
                rparams.height = h;
                rparams.setMargins(l, t, r, b);
            }
        }
    }

    public static void setRelativeLayout(int w, int h, int l, int t, int r, int b, RelativeLayout... layout) {
        for (int i = 0; i < layout.length; i++) {
            LayoutParams rparams = null;
            if (layout[i] != null) {
                rparams = (LayoutParams) layout[i].getLayoutParams();
                if (rparams == null) {
                    rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                    layout[i].setLayoutParams(rparams);
                }
                rparams.width = w;
                rparams.height = h;
                rparams.setMargins(l, t, r, b);
            }
        }
    }

    public static LayoutParams setRelativeLayout(LinearLayout layout, int w, int h, int l, int t, int r, int b) {
        LayoutParams rparams = null;
        if (layout != null) {
            rparams = (LayoutParams) layout.getLayoutParams();
            if (rparams == null) {
                rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                layout.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static LayoutParams setRelativeLayout(ImageView view, int w, int h, int l, int t, int r, int b) {
        LayoutParams rparams = null;
        if (view != null) {
            rparams = (LayoutParams) view.getLayoutParams();
            if (rparams == null) {
                rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                view.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static LayoutParams setRelativeLayout(ToggleButton switcher, int w, int h, int l, int t, int r, int b) {
        LayoutParams rparams = null;
        if (switcher != null) {
            rparams = (LayoutParams) switcher.getLayoutParams();
            if (rparams == null) {
                rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                switcher.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static LayoutParams setRelativeLayout(Button button, int w, int h, int l, int t, int r, int b) {
        LayoutParams rparams = null;
        if (button != null) {
            rparams = (LayoutParams) button.getLayoutParams();
            if (rparams == null) {
                rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                button.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static TableLayout.LayoutParams setTableLayout(ImageView image, int w, int h, int l, int t, int r, int b) {
        TableLayout.LayoutParams tparams = null;
        if (image != null) {
            tparams = (TableLayout.LayoutParams) image.getLayoutParams();
            if (tparams == null) {
                tparams = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                image.setLayoutParams(tparams);
            }
            tparams.width = w;
            tparams.height = h;
            tparams.setMargins(l, t, r, b);
        }
        return tparams;
    }

    public static TableLayout.LayoutParams setTableLayout(TableRow row, int w, int h, int l, int t, int r, int b) {
        TableLayout.LayoutParams tparams = null;
        if (row != null) {
            tparams = (TableLayout.LayoutParams) row.getLayoutParams();
            if (tparams == null) {
                tparams = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                row.setLayoutParams(tparams);
            }
            tparams.width = w;
            tparams.height = h;
            tparams.setMargins(l, t, r, b);
        }
        return tparams;
    }

    public static TableRow.LayoutParams setTableRowLayout(ImageView image, int w, int h, int l, int t, int r, int b) {
        TableRow.LayoutParams trparams = null;
        if (image != null) {
            trparams = (TableRow.LayoutParams) image.getLayoutParams();
            if (trparams == null) {
                trparams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                image.setLayoutParams(trparams);
            }
            trparams.width = w;
            trparams.height = h;
            trparams.setMargins(l, t, r, b);
        }
        return trparams;
    }

    public static TableRow.LayoutParams setTableRowLayout(TableLayout table, int w, int h, int l, int t, int r, int b) {
        TableRow.LayoutParams trparams = null;
        if (table != null) {
            trparams = (TableRow.LayoutParams) table.getLayoutParams();
            if (trparams == null) {
                trparams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                table.setLayoutParams(trparams);
            }
            trparams.width = w;
            trparams.height = h;
            trparams.setMargins(l, t, r, b);
        }
        return trparams;
    }

    public static TableRow.LayoutParams setTableRowLayout(TextView text, int w, int h, int l, int t, int r, int b) {
        TableRow.LayoutParams trparams = null;
        if (text != null) {
            trparams = (TableRow.LayoutParams) text.getLayoutParams();
            if (trparams == null) {
                trparams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                text.setLayoutParams(trparams);
            }
            trparams.width = w;
            trparams.height = h;
            trparams.setMargins(l, t, r, b);
        }
        return trparams;
    }

    public static LinearLayout.LayoutParams setLinearLayout(Button button, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams rparams = null;
        if (button != null) {
            rparams = (LinearLayout.LayoutParams) button.getLayoutParams();
            if (rparams == null) {
                rparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                button.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static LinearLayout.LayoutParams setLinearLayout(ImageView view, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams rparams = null;
        if (view != null) {
            rparams = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (rparams == null) {
                rparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                view.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static LinearLayout.LayoutParams setLinearLayout(Spinner spinner, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams rparams = null;
        if (spinner != null) {
            rparams = (LinearLayout.LayoutParams) spinner.getLayoutParams();
            if (rparams == null) {
                rparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                spinner.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    public static LinearLayout.LayoutParams setLinearLayout(View view, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams rparams = null;
        if (view != null) {
            rparams = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (rparams == null) {
                rparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                view.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    /**
     * setLinearLayout: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param text
     * @param w
     * @param h
     * @param l
     * @param t
     * @param r
     * @param b
     * @return
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static LinearLayout.LayoutParams setLinearLayout(TextView text, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams rparams = null;
        if (text != null) {
            rparams = (LinearLayout.LayoutParams) text.getLayoutParams();
            if (rparams == null) {
                rparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                text.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    /**
     * setRelativeLayout: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param list
     * @param w
     * @param h
     * @param l
     * @param t
     * @param r
     * @param b
     * @return
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static LayoutParams setRelativeLayout(ListView list, int w, int h, int l, int t, int r, int b) {
        LayoutParams rparams = null;
        if (list != null) {
            rparams = (LayoutParams) list.getLayoutParams();
            if (rparams == null) {
                rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                list.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }


    /**
     * setLinearLayout: (一句話說明這個方法)
     * <p>說明:
     *		<p>
     *		</p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @param tag
     * @param w
     * @param h
     * @param l
     * @param t
     * @param r
     * @param b
     * @return
     * @since JDK 1.6, Smart Router SDK 1.0
     * @print public
     */
//    public static LinearLayout.LayoutParams setLinearLayout(DmChartTag tag, int w, int h, int l, int t, int r, int b) {
//        LinearLayout.LayoutParams rparams = null;
//        if(tag != null){
//            rparams = (LinearLayout.LayoutParams) tag.getLayoutParams();
//            if(rparams == null) {
//                rparams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
//                tag.setLayoutParams(rparams);
//            }
//            rparams.width = w;
//            rparams.height = h;
//            rparams.setMargins(l, t, r, b);
//        }
//        return rparams;
//    }

    /**
     * setRelativeLayout: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param view
     * @param w
     * @param h
     * @param l
     * @param t
     * @param r
     * @param b
     * @return
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static LayoutParams setRelativeLayout(View view, int w, int h, int l, int t, int r, int b) {
        LayoutParams rparams = null;
        if (view != null) {
            rparams = (LayoutParams) view.getLayoutParams();
            if (rparams == null) {
                rparams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
                view.setLayoutParams(rparams);
            }
            rparams.width = w;
            rparams.height = h;
            rparams.setMargins(l, t, r, b);
        }
        return rparams;
    }

    /**
     * formatMac: 格式化MAC地址。
     * <p>說明:
     * <p>
     * 格式化MAC地址。轉成大寫，且有；間隔。例如: 1A:24:3C:33:6D:22
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param mac 未格式化MAC地址。
     * @return 格式化MAC地址。
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static CharSequence formatMac(String mac) {
        String displayMac = "";
        for (int i = 0; i < mac.length(); i++) {
            displayMac += mac.substring(i, i + 1);
            if (i != 0 && i != mac.length() - 1 && i % 2 == 1) {
                displayMac += ":";
            }
        }
        return displayMac;
    }

    public static String convertVersionNumber(int number, int radix) {
        String version = "";
        int left;
        left = number % radix;
        number = (int) Math.floor(number / radix);
        version = left + version;
        while (true) {
            left = number % radix;
            number = (int) Math.floor(number / radix);
            version = left + version;
            if (number == 0)
                break;
        }
        return version;
    }

//    public static String convertVersion(String versionCode) {
//        String yrStr = versionCode.substring(0, 4).substring(2, 4);
//        String monthStr = versionCode.substring(4, 6);
//        String dayStr = versionCode.substring(6, 8);
//        String hrmmStr = versionCode.substring(8, 12);
//        int yr = Integer.valueOf(yrStr);
//        yr -= 11;
//        int month = Integer.valueOf(monthStr);
//        int hourMin = Integer.valueOf(hrmmStr);
//
//        String hourMinStr = convertVersionNumber(hourMin, 6);
//
//        String monthC = "A";
//        monthC += (month - 1);
//
//        return "V" + IRouterBase.VERSION + "." + yr + "." + monthC + "" + convertVersionNumber(Integer.valueOf(dayStr), 6) + "." + hourMinStr;
//    }

    /**
     * getMacAddress: 取得系統的WiFi MAC地址。
     * <p>說明:
     * <p>
     * 取得系統的WiFi MAC地址。
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param context
     * @return MAC地址
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static String getWifiMacAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        return wifiInf.getMacAddress();
    }

    public static String getIPAddress(Context context) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        long ip = wifiInf.getIpAddress();
        if (ip != 0)
            return String.format("%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
        }
        return "0.0.0.0";
    }

    /**
     * isValid: 是否有效。
     * <p>說明:
     * <p>
     * 是否有效。
     * <pre>
     *      物件：null->無效
     *      數字：0->無效
     *      字串：null->無效
     *           空值->無效
     *      其他皆有效。
     *      </pre>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 物件：null->無效，數字：0->無效，字串：null->無效，空值->無效，其他皆有效。
     * <p></p>
     *
     * @param object 檢測的物件
     * @return true:有效 false:無效
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static boolean isValid(final Object object) {
        boolean result = (object == null ? false : true);
        //logger.event("isValid", object.getClass().getSimpleName(), "result:"+result);
        if (!result) {
            return result;
        } else {
            synchronized (object) {
                result = testLong(object);
            }
        }

        return result;
    }

//    public static String getConvertedNumber(int count, Unit unit) {
//
//        return null;
//    }

    private static boolean testLong(Object object) {
        return object.getClass() == Long.class ?
                testLong0(object)
                :
                testFloat(object);
    }

    private static boolean testLong0(Object object) {
        return (Long) object == 0 ?
                false
                :
                true
                ;
    }

    private static boolean testFloat(Object object) {
        return object.getClass() == Float.class ?
                testFloat0(object)
                :
                testInteger(object)
                ;
    }

    private static boolean testFloat0(Object object) {
        return (Float) object == 0 ?
                false
                :
                true
                ;
    }

    private static boolean testInteger(Object object) {
        return object.getClass() == Integer.class ?
                testInteger0(object)
                :
                testString(object)
                ;
    }

    private static boolean testString(Object object) {
        return object.getClass() == String.class ?
                testStringEmpty(object)
                :
                testJsonArray(object)
                ;
    }

    private static boolean testInteger0(Object object) {
        return (Integer) object == 0 ?
                false
                :
                true
                ;
    }

    private static boolean testJsonArray(Object object) {
        return object.getClass() == JSONArray.class ?
                testJsonArrayNull(object)
                :
                true
                ;
    }

    private static boolean testJsonArrayNull(Object object) {
        boolean isNull = true;
        JSONArray test = ((JSONArray) object);
        for (int i = 0; i < test.length(); i++) {
            if (!test.isNull(i)) {
                isNull = false;
            }
        }
        boolean isValid = !isNull;
        return isValid;
    }

    private static boolean testStringEmpty(Object object) {
        return ((String) object).isEmpty() ?
                false
                :
                testStringDigit(object)
                ;
    }

    private static boolean testStringDigit(Object object) {
        return Utility.isDigit((String) object) ?
                testStringLength(object)
                :
                true
                ;
    }

    private static boolean testStringLength(Object object) {
        return ((String) object).length() > 1 ?
                true
                :
                testString0(object)
                ;
    }

    private static boolean testString0(Object object) {
        return Long.valueOf(((String) object)) == 0 ?
                false
                :
                true
                ;
    }

    /**
     * getLanguageCode: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param locale
     * @return
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static String getLanguageCode(Locale locale) {
        String language = locale.getLanguage().toLowerCase() + "_" + locale.getCountry().toLowerCase();
        return language;
    }

    public static String getNonePrimaryPath(Context context, Uri uri) {
        String path = "";
        String askId = "";
        int askSize = 0;
        String askName = "";
        String askMime = "";
        Cursor cursor;

//        logger
//        .enable(true)
//        .setEvent("getPath")
//        .logItem("URI",             uri.toString())
//        .logItem("URI Last Path",   uri.getLastPathSegment())
//        .logSeparator();

        cursor = context.getContentResolver().query(uri, null, null, null, null);

//        logger
//        .logItem(cursor);

        if (cursor != null && cursor.moveToFirst()) {
            askId = cursor.getString(cursor.getColumnIndex("document_id"));
            askSize = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
            askName = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME));
            askMime = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.MIME_TYPE));
            if (askName.lastIndexOf(".") > -1)
                askName = askName.substring(0, askName.lastIndexOf("."));
            cursor.close();
        }

//        logger
//        .logItem("Ask ID",      askId)
//        .logItem("Ask Name",    askName)
//        .logItem("Ask MIME",    askMime)
//        .logItem("Ask Size",    askSize)
//        .logSeparator();

        if (Utility.isValid(askId)) {

//            logger
//            .logItem("id is valid, go for new FS");

            if (isValid(askMime)) {
                cursor = context.getContentResolver().query(
                        MediaStore.Files.getContentUri("external"),
                        null,
                        MediaStore.Files.FileColumns.TITLE + "='" + askName + "'"
                                + " AND " +
                                "storage_id" + "!='65537'"
                                + " AND " +
                                MediaStore.Files.FileColumns.SIZE + "='" + askSize + "'"
                        ,
                        null,
                        MediaStore.Files.FileColumns.DISPLAY_NAME + " LIMIT 10");

                if (cursor != null && cursor.moveToFirst()) {

//                    logger
//                    .logSeparator()
//                    .logItem("external")
//                    .logItem(cursor);

                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    cursor.close();
                } else {

//                    logger
//                    .logItem("can't not match file in external DB on new FS");

                }
            }

        } else {

            path = uri.getPath();

//            logger
//            .logItem("id is not valid, go for old FS");

        }

//        logger
//        .logItem("Path",path)
//        .print();

        return path;
    }

    /**
     * getPath: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param context
     * @param uri
     * @return
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    return getNonePrimaryPath(context, uri);
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static float getConvertedSize(Context context, float size) {
        return (float) getDisplayWidth(context) / size / (float) 16;
    }

    public static float getSizeFromWidthRatio(Context context, float ratio) {
        return (float) getDisplayWidth(context) / 100 * ratio;
    }

    public static float max(float... numbers) {
        float max = numbers[0];
        for (float val : numbers) {
            max = Math.max(max, val);
        }
        return max;
    }

    public static float min(float... numbers) {
        float min = numbers[0];
        for (float val : numbers) {
            min = Math.min(min, val);
        }
        return min;
    }

    public static float maxx(float... numbers) {
        int size = (int) Math.ceil((double) numbers.length / 2);
        float[] result = new float[size];
        for (int i = 0; i < numbers.length; i = i + 2) {
            result[i / 2] = (i + 1 >= numbers.length) ? numbers[i] : Math.max(numbers[i], numbers[i + 1]);
        }
        if (result.length == 1) {
            return result[0];
        } else {
            return maxx(result);
        }
    }

    public static float minx(float... numbers) {
        int size = (int) Math.ceil((double) numbers.length / 2);
        float[] result = new float[size];
        for (int i = 0; i < numbers.length; i = i + 2) {
            result[i / 2] = (i + 1 >= numbers.length) ? numbers[i] : Math.min(numbers[i], numbers[i + 1]);
        }
        if (result.length == 1) {
            return result[0];
        } else {
            return minx(result);
        }
    }

    public static String getActionName(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:            //0
                return "DOWN   ";
            case MotionEvent.ACTION_UP:              //1
                return "UP     ";
            case MotionEvent.ACTION_MOVE:            //2
                return "MOVE   ";
            case MotionEvent.ACTION_CANCEL:          //3
                return "CANCEL ";
            case MotionEvent.ACTION_OUTSIDE:         //4
                return "OUTSIDE";
            case MotionEvent.ACTION_POINTER_DOWN:    //5
                return "POINTER_DOWN";
            case MotionEvent.ACTION_POINTER_UP:      //6
                return "POINTER_UP";
            case MotionEvent.ACTION_MASK:            //255
                return "MASK";
            default:
                return "UNKOWN " + action + " ";

        }
    }

    public static JSONArray removeJsonArrayElement(JSONArray array, String obj) {
        for (int i = 0, len = array.length(); i < len; i++) {
            String item = null;
            if (!array.isNull(i)) {
                try {
                    item = array.getString(i);
                    if (item.equalsIgnoreCase(obj)) {
                        array.put(i, null);
                    }
                } catch (JSONException e) {
//                    logger.error(e, "removeJsonArrayElement");;
                }
            }
        }
        return array;
    }

    public static int getJsonHash(Object object) {
        try {
            if(object != null){
                int hash = new JsonHashTask().execute(object).get();
                return hash;
            }else {
                return -1;
            }
        } catch (Exception e) {
            logger.error(e, "getJsonHash");
        }
        return -1;
    }

    /**
     * startProcess: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param handler
     * @param runnable
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static void startProcess(Handler handler, Runnable runnable) {
        if (Utility.isValid(handler)
                && Utility.isValid(runnable)) {
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        }
    }

    /**
     * getLogger: 取得LogBuilder物件。
     * <p>說明:
     *		<p>
     *      取得LogBuilder物件。
     *		</p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @param TAG 標籤，一個標籤會有獨立一個LogBuilder。
     * @param isLog 是否開啟Log功能。
     * @return LogBuilder物件。
     * @since JDK 1.6, Smart Router SDK 1.0
     * @print public
     */
//    public static LogBuilder getLogger(String TAG, boolean isLog) {
//        if(isLog) {
//            return LogBuilder.with(TAG, Log.DEBUG).enableAll();
//        }else {
//            return LogBuilder.with(TAG, Log.DEBUG).disableAll();
//        }
//    }

    /**
     * postProcess: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param handler
     * @param runnable
     * @param millis
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static void postProcess(Handler handler, Runnable runnable, long millis) {
        if (Utility.isValid(handler)
                && Utility.isValid(runnable)) {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, millis);
        }
    }

    /**
     * stopProcess: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param handler
     * @param runnable
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static void stopProcess(Handler handler, Runnable runnable) {
        if (Utility.isValid(handler)) {
            handler.removeCallbacks(runnable);
        }
    }

    public static String getFormatedPath(String path) {
        path = path.trim();
        int last = path.lastIndexOf("/");
        while (last > -1 && last + 1 == path.length()) {
            path = path.substring(0, last);
            last = path.lastIndexOf("/");
        }
        return path + "/";
    }

//    public static RequestParams getDeviceParams(Context context, String pairKey, String mac, int id) {
//        String language = Utility.getLanguageCode(Locale.getDefault());
//        RequestParams params = new RequestParams();
//
//        params.add("uid",               Uri.encode(Utility.getDeviceID(context)));  // must
//        params.add("os",                IRouterBase.OS);                                 // must
//        params.add("os_version",        Uri.encode(Build.VERSION.RELEASE));         // must
//        params.add("brand",             Uri.encode(Build.BRAND));                   // must
//        params.add("language",          Uri.encode(language));                      // must
//        params.add("model_no",          Uri.encode(Build.MODEL));                   // must
//        params.add("seq_id",            String.valueOf(id));                        // must
//        params.add("serial_no",         Uri.encode(Build.SERIAL));
//        params.add("owner_name",        "");                                        //getUserName());
//        params.add("phone_no",          "");                                        //phone.getLine1Number());
//        params.add("profile_version",   "");
//        params.add("op",                "");
//        if(pairKey != null){
//            params.add("pair_key",      Uri.encode(pairKey));
//        }else{
//            if(mac != null)
//                params.add("dev_mac",       Uri.encode(mac));
//            else
//                return null;
//        }
//
//        return params;
//    }

    public static String getSpace(int i) {
        if (i > 0)
            return String.format("%0" + i + "d", 0).replace("0", " ");
        else
            return "";
    }

    /**
     * <pre>
     * getMaxDivider:取得最大公因數
     * </pre>
     *
     * @param x
     * @param y
     * @return
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @since JDK 1.6
     */
    public static int getMaxDivider(int x, int y) {
        int a = 0, b = 0, c = 0;
        if (x > y) {
            a = x;
            b = y;
        } else {
            a = y;
            b = x;
        }

        do {
            c = a % b;
            if (c != 0) {
                a = b;
                b = c;
            } //if
        } while (c != 0);
        return b;
    } //runMe

    /**
     * convertSize: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param size
     * @param fontFactor
     * @param text
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static void convertSize(int size, float fontFactor, TextView... text) {
        if (fontFactor != 0f) {
            size = (int) (fontFactor * size);
        }
        for (int i = 0; i < text.length; i++) {
            text[i].setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        }
    }

    /**
     * convertColor: (一句話說明這個方法)
     * <p>說明:
     * <p>
     * </p>
     * </p>適用條件: 無
     * <br>執行流程: 無
     * <br>使用方法: 無
     * <br>注意事項: 無
     * <p></p>
     *
     * @param context
     * @param backgroundColorResourceId
     * @param hiColor
     * @param lowColor
     * @param text
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @print public
     * @since JDK 1.6, Smart Router SDK 1.0
     */
    public static void convertColor(Context context, int backgroundColorResourceId, int hiColor, int lowColor, TextView... text) {
        int color = getColor(context, backgroundColorResourceId, hiColor, lowColor);
        for (int i = 0; i < text.length; i++) {
            text[i].setTextColor(color);
        }
    }

    private static int getColor(Context context, int backgroundColorResourceId, int hiColor, int lowColor) {
        return Utility.getContrastYIQ(
                context.getResources().getColor(backgroundColorResourceId),
                context.getResources().getColor(hiColor),
                context.getResources().getColor(lowColor)
        );
    }

    public List<String> getUniqueIds(Context context) {
        List<String> ids = new ArrayList<String>();

        TelephonyManager tm = (TelephonyManager) ((ContextWrapper) context).getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String DeviceId, SerialNum, androidId;
        ids.add(tm.getDeviceId());
        ids.add(tm.getSimSerialNumber());
        /**
         * Secure.ANDROID_ID:
         * It's known to be null sometimes, it's documented as "can change upon factory reset".
         * Use at your own risk, and it can be easily changed on a rooted phone.
         */
        ids.add(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        ids.add(new UUID(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).hashCode(), ((long) tm
                .getDeviceId().hashCode() << 32) | tm.getSimSerialNumber().hashCode()).toString());
        Log.d(TAG, "=> Android DeviceId is: " + ids.get(0));
        Log.d(TAG, "=> Android SerialNum is: " + ids.get(1));
        Log.d(TAG, "=> Android androidId is: " + ids.get(2));
        Log.d(TAG, "=> Android UUID is: " + ids.get(3));
        return ids;
    }

    public static String getVisibilityName(int visibility) {
        switch (visibility){
            case View.INVISIBLE:
                return "INVISIBLE";
            case View.VISIBLE:
                return "VISIBLE";
            case View.GONE:
                return "GONE";
            default:
                return "UNKNOWN";
        }
    }

    public static String getStyleName(int style) {
        switch (style){
            case Typeface.BOLD:
                return "BOLD";
            case Typeface.BOLD_ITALIC:
                return "BOLD_ITALIC";
            case Typeface.ITALIC:
                return "ITALIC";
            case Typeface.NORMAL:
                return "NORMAL";
            default:
                return "UNKNOWN";
        }
    }

    public static void removeFromParent(View view) {
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    public static boolean hasParent(View view) {
        if (view != null && view.getParent() != null) {
            return true;
        }
        return false;
    }

    public static boolean addToView(ViewGroup rootView, View view, String viewName) {
        // Log.wtf(TAG, "> " + viewName + " " + rootView.toString().matches(".*" + viewName + ".*") + " " + getObjectName(rootView.toString()) + " - " + getObjectName(view.toString()));
        if (rootView != null && rootView.toString().matches(".*\\Q" + viewName + "\\E.*")) {
            rootView.addView(view);
            return true;
        }
        return false;
    }

    public static boolean addToViewHierarchy(ViewGroup rootView, View view,  String viewName) {
        if(addToView(rootView, view, viewName)) return true;

        int children = rootView.getChildCount();
        ArrayList<ViewGroup> subFolders = new ArrayList<>();
        for (int i = 0; i < children; i++) {
            View child = rootView.getChildAt(i);
            try {
                if (((ViewGroup) child).getChildCount() > 0) {
                    subFolders.add((ViewGroup) child);
                }
                if(addToView((ViewGroup) child, view, viewName)) return true;

            } catch (Exception e) {

            }
        }
        if (subFolders.size() > 0) {
            for (ViewGroup viewGroup : subFolders) {
                if(addToViewHierarchy(viewGroup, view, viewName)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean bringViewToFront(ViewGroup rootView, String viewName) {
        // Log.wtf(TAG, "> " + viewName + " " + rootView.toString().matches(".*" + viewName + ".*") + " " + getObjectName(rootView.toString()));
        if (rootView != null && rootView.toString().matches(".*\\Q" + viewName + "\\E.*")) {
            rootView.bringToFront();
            return true;
        }
        return false;
    }

    public static boolean bringViewToFrontHierarchy(ViewGroup rootView, String viewName) {
        if(bringViewToFront(rootView, viewName)) return true;

        int children = rootView.getChildCount();
        ArrayList<ViewGroup> subFolders = new ArrayList<>();
        for (int i = 0; i < children; i++) {
            View child = rootView.getChildAt(i);
            try {
                if (((ViewGroup) child).getChildCount() > 0) {
                    subFolders.add((ViewGroup) child);
                }
                if(bringViewToFront((ViewGroup) child, viewName)) return true;

            } catch (Exception e) {

            }
        }
        if (subFolders.size() > 0) {
            for (ViewGroup viewGroup : subFolders) {
                if(bringViewToFrontHierarchy(viewGroup, viewName)){
                    return true;
                }
            }
        }
        return false;
    }

    public static void listViewHierarchy(ViewGroup view, int index) {
        int children = view.getChildCount();
        String indent = getSpace(index * 4);
        String nextIndent = getSpace((index + 1) * 4);
        String parent = index == 0?"":getObjectName(view.getParent().toString()) + " child " + ((ViewGroup)view.getParent()).indexOfChild(view) + " - ";
        //Log.wtf(TAG, "+ " + indent + parent + getObjectName(view.toString()) + " has " + children + " children");
        Log.wtf(TAG, "+ " + indent + getObjectName(view.toString()));
        ArrayList<ViewGroup> subFolders = new ArrayList<>();
        for (int i = 0; i < children; i++) {
            View child = view.getChildAt(i);
            try {
                if (((ViewGroup) child).getChildCount() > 0) {
                    subFolders.add((ViewGroup) child);
                } else {
                    //Log.d(TAG, "| " + nextIndent + getObjectName(view.toString()) + " child " + (i  + 1) + " - " + getObjectName(child.toString()));
                    Log.d(TAG, "| " + nextIndent + getObjectName(child.toString()));
                }
            } catch (Exception e) {
                //Log.d(TAG, "| " + nextIndent + getObjectName(view.toString()) + " child " + (i + 1) + " - " + getObjectName(child.toString()));
                Log.d(TAG, "| " + nextIndent + getObjectName(child.toString()));
                //e.printStackTrace();
            }
        }
        if (subFolders.size() > 0) {
            for (ViewGroup viewGroup : subFolders) {
                listViewHierarchy(viewGroup, index + 1);
            }
        }
    }

    public static String getObjectName(String obj) {

        if (obj != null) {
            String fullId = obj.substring(0, obj.indexOf("{"));
            String[] segs = fullId.split("\\.");
            String id = null;
            if (segs.length > 0) id = segs[segs.length - 1];
            String fullProperty = obj.replace(fullId, "");
            String[] properties = fullProperty.split(" ");
            String property = null;
            if (properties.length > 0) property = properties[0].substring(1);
//            Log.i(TAG, "> obj "
//                    + "\n fullId: " + fullId
//                    + "\n id :" + id
//                    + "\n fullProperty: " + fullProperty
//                    + "\n property: " + property
//            );
            return id + "(" + property + ")";
        }
        return "";
    }

    public static String formatFixedFloat(float x, int maxInteger, int maxFraction, boolean hasNegtive) {
        String pattern = "";
        pattern += fill(maxInteger,"0");
        if(maxFraction > 0){
            pattern += ".";
            pattern += fill(maxFraction,"0");
        }
        f.applyPattern(pattern);
        if(hasNegtive) f.setPositivePrefix("+");
        return f.format(x);
    }

    private static String fill(int maxInteger, String fill) {
        String string = "";
        for(int i = maxInteger; i > 0; i --){
            string += fill;
        }
        return string;
    }




//    public static String getUserName(Context context) {
//        AccountManager manager = AccountManager.get(context);
//        Account[] phoneAccounts = manager.getAccountsByType("com.google");
//        List<String> possibleEmails = new LinkedList<String>();
//
//        for (Account account : phoneAccounts) {
//            // TODO: Check possibleEmail against an email regex or treat
//            // account.name as an email address only for certain account.type
//            // values.
//            possibleEmails.add(account.name);
//        }
//
//        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
//            String email = possibleEmails.get(0);
//            String[] parts = email.split("@");
//            if (parts.length > 0 && parts[0] != null)
//                return parts[0];
//            else
//                return null;
//        } else
//            return null;
//    }

}
