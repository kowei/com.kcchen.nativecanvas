package com.kcchen.nativecanvas.utils;

import android.database.Cursor;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class LogBuilder implements Runnable{

    private static final String TAG = "LogBuilder";
    private final static String LEFT_TOP                = "┌";
    private final static String LEFT_TOP_EVENT          = "╒";
    private final static String LEFT_TOP_BOX            = "┏";
    private final static String RIGHT_TOP               = "┐";
    private final static String RIGHT_TOP_EVENT         = "╕";
    private final static String RIGHT_TOP_BOX           = "┓";
    private final static String LEFT_BOTTOM             = "└";
    private final static String LEFT_BOTTOM_EVENT       = "╘";
    private final static String LEFT_BOTTOM_BOX         = "┗";
    private final static String RIGHT_BOTTOM            = "┘";
    private final static String RIGHT_BOTTOM_EVENT      = "╛";
    private final static String RIGHT_BOTTOM_BOX        = "┛";
    private final static String HORIZONTAL_LINE         = "─";
    private final static String HORIZONTAL_LINE_EVENT   = "═";
    private final static String HORIZONTAL_LINE_BOX     = "━";
    private final static String VERTICAL_LINE           = "│";
    private final static String VERTICAL_LINE_BOX       = "┃";
    private final static String VERTICAL_LEFT_MIDDLE    = "├";
    private final static String VERTICAL_RIGHT_MIDDLE   = "┤";
    private final static String VERTICAL_CONTIVUE       = "┆";
    private final static String LINE_FEED               = "\n";
    public static final String START = "START";
    public static final String END = "END";
    private static final String PAUSE = "PAUSE";
    private static int maxLength = 0;
    private static HashMap<String, LogBuilder> logs = new HashMap<String, LogBuilder>();
    private static HashMap<String, HandlerThread> logsThread = new HashMap<String, HandlerThread>();
    private static HashMap<String, Handler> logsHandler = new HashMap<String, Handler>();
    private static int logWidth     = 100;
    private static int logIndent    = 4;
    private static int logMargin    = 1;
    private String name = "";
    private ArrayList<String> item = new ArrayList<String>();
    private ArrayList<String> lines = new ArrayList<String>();
    private int type = Log.DEBUG;
    private int contentWidth;
    private boolean isEvent;
    private boolean isEnable = true;
    private String id;
    private ArrayList<String> box = new ArrayList<String>();
    private int stackInfoLimit = 5;


    /**
     * <pre>
     * with:(這裡用一句話說明這個方法的作用).
     * TODO(這裡說明這個方法適用條件 – 可選).
     * TODO(這裡說明這個方法的執行流程 – 可選).
     * TODO(這裡說明這個方法的使用方法 – 可選).
     * TODO(這裡說明這個方法的注意事項 – 可選).
     * </pre>
     *
     * @author <a href="mailto:kowei.chen@gmail.com">KC Chen</a>
     * @param TAG
     * @param type
     * @param width
     * @return
     * @since JDK 1.6
     */
    public static LogBuilder with(String TAG, int type, int width) {
        try {
            if(logs.get(TAG) == null){
                logWidth  = width;
                logs.put(TAG, new LogBuilder(TAG, type));
                logs.get(TAG).setContext(TAG, type);
                logsThread.put(TAG,new HandlerThread(TAG));
                logsThread.get(TAG).start();
                logsHandler.put(TAG, new Handler(logsThread.get(TAG).getLooper()));
                logsHandler.get(TAG).post(logs.get(TAG) );
            }
        } catch (Exception e) {
        }
        return logs.get(TAG);
    }

    public static LogBuilder with(String TAG, int type) {
        try {
            if(logs.get(TAG) == null){
                logs.put(TAG, new LogBuilder(TAG, type));
                logs.get(TAG).setContext(TAG, type);
                logsThread.put(TAG,new HandlerThread(TAG));
                logsThread.get(TAG).start();
                logsHandler.put(TAG, new Handler(logsThread.get(TAG).getLooper()));
                logsHandler.get(TAG).post(logs.get(TAG) );
            }
        } catch (Exception e) {
        }
        return logs.get(TAG);
    }

    private LogBuilder(String TAG, int type) {
        setId(TAG);
        this.type = type;
        contentWidth    =   logWidth -
                            VERTICAL_LINE.length()*2 -
                            logMargin*2;
    }

    private void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public LogBuilder setLogLength(int maxLength){
        this.maxLength = maxLength;
        return this;
    }

    public LogBuilder setLog( int type) {
        this.type = type;
        return this;
    }

    public LogBuilder setLongMode(int maxLength) {
        this.maxLength  = maxLength;
        return this;
    }

    private void setContext(String TAG, int type) {
        setId(TAG);
        this.type = type;
    }

    public LogBuilder setName(String name) {
        if(isEnable ){
            isEvent = false;

            if(name != null)
                this.name = name;

            synchronized(item) {
                item.clear();
                item.add(getUpHeader(this.name));
            }
            logItem("ANDROID VERSION",Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")");
            logItem("STACK INFO",StackTraceInfo.getInvok(stackInfoLimit));
        }
        return this;
    }

    public LogBuilder setEvent(String event, String method) {
        if(isEnable ){
            isEvent = true;
            this.type = Log.INFO;

            if(name != null)
                this.name = "( " + event + " | " + method + " )";

            synchronized(item) {
                item.clear();
                item.add(getEventUpHeader());
            }
            logItem("ANDROID VERSION",Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")");
            logItem("STACK INFO",StackTraceInfo.getInvok(stackInfoLimit));
        }
        return this;
  }

    public LogBuilder setEvent(String callback) {
        if(isEnable ){
            isEvent = true;
            this.type = Log.VERBOSE;

            if(name != null)
                this.name = "( " + callback + " CALLBACK )";

            synchronized(item) {
                item.clear();
                item.add(getEventUpHeader());
            }
            logItem("ANDROID VERSION",Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")");
            logItem("STACK INFO",StackTraceInfo.getInvok(stackInfoLimit));
        }
        return this;
    }

    public LogBuilder logItem(String name, String value) {
        if(isEnable ){
            if(name != null) {
                synchronized(item) {
                    getLines(item, name, value);
                }
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, String value, int code) {
        if(isEnable ) {
            if(name != null) {
                synchronized(item) {
                    getLines(item, name, value + " (" + code + ")");
                }
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, boolean booleanValue) {
        if(isEnable ){
            synchronized(item) {
            	getLines(item, name, String.valueOf(booleanValue));
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, int intValue) {
        if(isEnable ){
            synchronized(item) {
                getLines(item, name, "" + Integer.valueOf(intValue));
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, long longValue) {
        if(isEnable ){
            synchronized(item) {
                getLines(item, name, "" + Long.valueOf(longValue));
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, float floatValue) {
        if(isEnable ){
            synchronized(item) {
                getLines(item, name, "" + Float.valueOf(floatValue));
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, Exception e) {
        if(isEnable ){
            if(e != null) {
                synchronized(item) {
                    getLines(item, name, e.toString());
                }
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, Uri uri) {
        if(isEnable ){
            synchronized(item) {
                getLines(item, name, uri.toString());
            }
        }
        return this;
    }

    public LogBuilder logItem(String columnName, byte[] blob) {
        if(isEnable ){
            synchronized(item) {
                getLines(item, name, blob.toString());
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, JSONObject jsonObject) {
        if(isEnable ){
            if(name != null)
                if(jsonObject != null){
                    try {
                        synchronized(item) {
                            getLines(item, name, JsonFormatter.formatObject(jsonObject.toString()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG,"JSONObject logItem error:"+e.toString());
                    }
                }
        }
        return this;
    }

    public LogBuilder logItem(String name, JSONArray jsonArray) {
        if(isEnable ){
            if(name != null)
                if(jsonArray != null){
                    try {
                        synchronized(item) {
                            getLines(item, name, JsonFormatter.formatArray(jsonArray.toString()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG,"JSONArray logItem error:"+e.toString());
                    }
                }
        }
        return this;
    }

//    public LogBuilder logItem(String name, DmAccount account) {
//        if(isEnable ){
//            if(name != null)
//                if(account != null){
//                    try {
//                        synchronized(item) {
//                            getLines(item, name, account.toPrintString());
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG,"DmAccount logItem error:"+e.toString());
//                    }
//                }
//        }
//        return this;
//    }
//
//    public LogBuilder logItem(String name, DmAccountManager accounts) {
//        if(isEnable ){
//            if(name != null)
//                if(accounts != null){
//                    try {
//                        synchronized(item) {
//                            getLines(item, name, accounts.toPrintString());
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG,"DmAccountList logItem error:"+e.toString());
//                    }
//                }
//        }
//        return this;
//    }
//
//    public LogBuilder logItem(String name, HashMap<String, DmUpnpDevice> upnpDevices) {
//        if(isEnable ){
//            if(name != null)
//                if(upnpDevices.size() > 0){
//                    try {
//                        synchronized(item) {
//                            for (String upnpDeviceName : upnpDevices.keySet()) {
//                                getLines(item, name, upnpDevices.get(upnpDeviceName).toPrintString());
//                            }
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG,"HashMap<String, DmUpnpDevice> logItem error:"+e.toString());
//                    }
//                }
//        }
//        return this;
//    }
//
//    public LogBuilder logItem(String name, HashMap<String, DmUpnpDevice> upnpDevices, String udn) {
//        if(isEnable ){
//            if(name != null)
//                if(upnpDevices.size() > 0){
//                    try {
//                        synchronized(item) {
//                            getLines(item, name, upnpDevices.get(udn).toPrintString());
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG,"HashMap<String, DmUpnpDevice> logItem error:"+e.toString());
//                    }
//                }
//        }
//        return this;
//    }

//    public LogBuilder logItem(CharSequence name, HashMap<String, DmAriaSystem> ariaSystem) {
//        logItem(name.toString());
//        for (String key : ariaSystem.keySet()) {
//            logItem(key, ariaSystem.get(key));
//        }
//        return null;
//    }

    public LogBuilder logItem(String string) {
        if(isEnable ){
            if(string != null){
                try {
                    synchronized(item) {
                        getLines(item, "", "=====>" + string);
                    }
                } catch (Exception e) {
                    Log.e(TAG,"String logItem error:"+e.toString());
                }
            }
        }
        return this;
    }

    public LogBuilder logItem(final Bundle resultData) {
        if(isEnable ){
            if(Utility.isValid(resultData)){
                try {
                    synchronized(item) {
                        for (String key : resultData.keySet()) {
                            if(Utility.isValid(key) && Utility.isValid(resultData.get(key))){
                                getLines(item, key, resultData.get(key).toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG,"Bundle logItem error:"+e.toString());
                }
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, ArrayList<String> property) {
        if(isEnable ){
            if(property != null){
                logItem(name);
                logSeparator();
                try {
                    for (int i = 0; i < property.size(); i++) {
                        logItem(i + "", property.get(i));
                    }
                } catch (Exception e) {
                    Log.e(TAG,"ArrayList<String> logItem error:"+e.toString());
                }
            }else {
                logItem(""+property);
            }
        }
        return this;
    }

    public LogBuilder logItem(String name, List<String> list) {
        if(isEnable ){
            if(list != null){
                logItem(name);
                logSeparator();
                try {
                    for (int i = 0; i < list.size(); i++) {
                        logItem(list.get(i));
                    }
                } catch (Exception e) {
                    Log.e(TAG,"List<String> logItem error:"+e.toString());
                }
            }else {
                logItem(""+list);
            }
        }
        return this;
    }

    public LogBuilder logItem(HashMap<String, ArrayList<String>> fileList) {
        if(isEnable ){
            if(fileList != null){
                try {
                    for (String path : fileList.keySet()) {
                        logItem(path, fileList.get(path));
                    }
                } catch (Exception e) {
                    Log.e(TAG,"HashMap<String, ArrayList<String>> logItem error:"+e.toString());
                }
            }else {
                logItem(""+fileList);
            }
        }
        return this;
    }

//    public LogBuilder logItem(String name, DmAriaSystem ariaSystem) {
//        if(isEnable ){
//            if(ariaSystem != null){
//                if(ariaSystem.size() > 0){
//                    logItem(name);
//                    logSeparator();
//                    try {
//                        for (String gid : ariaSystem.keySet()) {
//                            logItem(gid, ariaSystem.get(gid));
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG,"DmAriaSystem logItem error:"+e.toString());
//                    }
//                }else {
//                    logItem(name, "empty:" + ariaSystem);
//                }
//            }else {
//                logItem(name, "null");
//            }
//        }
//        return this;
//    }

//    public LogBuilder logItem(String name, FileUpdateQueue queue) {
//        if(isEnable ){
//            if(Utility.isValid(queue)){
//                if(queue.size() > 0){
//                    logItem(name);
//                    logSeparator();
//                    try {
//                        for (int i = 0; i < queue.size(); i++) {
//                            logItem("update "+i, ""
//                                    + " path:"           + queue.get(i).getPath()
//                                    + " hash:"           + Utility.getJsonHash(queue.get(i).getContent())
//                                    + " isAvailable:"    + queue.get(i).isAvailable()
//                                    + " isSorted:"       + queue.get(i).isSorted()
//                                    );
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG,"FileUpdateQueue logItem error:"+e.toString());
//                    }
//                }else {
//                    logItem(name, "empty:" + queue);
//                }
//            }else {
//                logItem(name, "null");
//            }
//        }
//        return this;
//    }
//
//    public LogBuilder logItem(String name, AriaUpdateQueue queue) {
//        if(isEnable ){
//            if(Utility.isValid(queue)){
//                if(queue.size() > 0){
//                    logItem(name);
//                    logSeparator();
//                    try {
//                        for (int i = 0; i < queue.size(); i++) {
//                            logItem("update "+i, ""
//                                    + " path:"           + queue.get(i).getPath()
//                                    + " hash:"           + Utility.getJsonHash(queue.get(i).getContent())
//                                    + " isAvailable:"    + queue.get(i).isAvailable()
//                                    + " isSorted:"       + queue.get(i).isSorted()
//                                    );
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG,"AriaUpdateQueue logItem error:"+e.toString());
//                    }
//                }else {
//                    logItem(name, "empty:" + queue);
//                }
//            }else {
//                logItem(name, "null");
//            }
//        }
//        return this;
//    }

    public void logItem(Cursor cursor) {
        if(cursor != null && cursor.moveToFirst()){
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                for (int j = 0; j < cursor.getColumnCount(); j++) {
                    switch (cursor.getType(j)) {
                    case Cursor.FIELD_TYPE_NULL:
                        logItem("NULL    "+j+"."+cursor.getColumnName(j),"null");
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        logItem("BLOB    "+j+"."+cursor.getColumnName(j),cursor.getBlob(j));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        logItem("FLOAT   "+j+"."+cursor.getColumnName(j),cursor.getFloat(j));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        logItem("INTEGER "+j+"."+cursor.getColumnName(j),cursor.getInt(j));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        logItem("STRING  "+j+"."+cursor.getColumnName(j),cursor.getString(j));
                        break;
                    default:
                        break;
                    }
                }
                logSeparator();
            }
        }else {
            logItem("cursor is not valid!");
        }
    }

    public LogBuilder logSeparator() {
        if(isEnable ){
            synchronized(item) {
                item.add(getSeparator());
            }
        }
        return this;
    }

    public void error(Exception e, String error) {
        if(e != null){
            Log.e(getId(), getErrorUpHeader(e));
            Log.e(getId(), getLines(error, e.toString()));
            Log.e(getId(), getErrorDownHeader(e));
        }else {
            Log.e(getId(), getErrorUpHeader(e));
            Log.e(getId(), getLines("", error));
            Log.e(getId(), getErrorDownHeader(e));
        }
    }

    public void event(String event, String method, String value) {
        if(isEnable ) {
            if(event == null)
                event = "";
            if(method == null)
                method = "";
            if(value == null)
                value = "";
            int width = (event + method + value).length() + 10;
            Log.i(getId(), getBoxUpHeader(width));
            Log.i(getId(), getBoxContent(event, method, value));
            Log.i(getId(), getBoxDownHeader(width));
        }
    }

    public void event(String event) {
        if(isEnable ) {
            if(event == null)
                event = "";
            int width = event.length();
            Log.e(getId(), getBoxUpHeader(width));
            Log.e(getId(), getBoxContent(event));
            Log.e(getId(), getBoxDownHeader(width));
        }
    }

    public LogBuilder direct(int log, int limit,String event) {
        if(Utility.isValid(event)){
            if(event.equals(START)){
                box.clear();
                box.add(START);
                box.add(getUpHeader(""));
                getLines(box, "", StackTraceInfo.getInvok(limit));
                box.add(getSeparator());
                return this;
            }
            if(box.size() > 0 && box.get(0).equals(START)){
                if (event.equals(END)) {
                    box.add(getDownHeader(""));
                    printDirect(log,1);
                    box.clear();
                    return this;
                }else if (event.equals(PAUSE)) {
                    box.add(getSeparator());
                    return this;
                }else {
                    synchronized(box) {
                        getLines(box, "", event);
                    }
                    return this;
                }
            }else {
                box.clear();
                box.add(getUpHeader(""));
                getLines(box, "", StackTraceInfo.getInvok(limit));
                box.add(getSeparator());
                synchronized(box) {
                    for (String line : event.split(System.getProperty("line.separator"))) {
                        getLines(box, "", line);
                    }
                }
                box.add(getDownHeader(""));
                printDirect(log,0);
                return this;
            }
        }else {
            return this;
        }
    }

    public LogBuilder notDirect(int log,int limit,String event) {
        if(isEnable){
            if(Utility.isValid(event)){
                if(event.equals(START)){
                    box.clear();
                    box.add(START);
                    box.add(getUpHeader(""));
                    getLines(box, "", StackTraceInfo.getInvok(limit));
                    box.add(getSeparator());
                    return this;
                }
                if(box.size() > 0 && box.get(0).equals(START)){
                    if (event.equals(END)) {
                        box.add(getDownHeader(""));
                        printDirect(log,1);
                        box.clear();
                        return this;
                    }else if (event.equals(PAUSE)) {
                        box.add(getSeparator());
                        return this;
                    }else {
                        synchronized(box) {
                            getLines(box, "", event);
                        }
                        return this;
                    }
                }else {
                    box.clear();
                    box.add(getUpHeader(""));
                    getLines(box, "", StackTraceInfo.getInvok(limit));
                    box.add(getSeparator());
                    synchronized(box) {
                        for (String line : event.split(System.getProperty("line.separator"))) {
                            getLines(box, "", line);
                        }
                    }
                    box.add(getDownHeader(""));
                    printDirect(log,0);
                    return this;
                }
            }else {
                return this;
            }
        }else {
            return this;
        }
    }

    private void printDirect(int log, int start) {
        synchronized (box) {
            switch (log) {
            case Log.ERROR:
                for (int i = start; i < box.size(); i++) {
                    Log.e(getId(), box.get(i));
                }
                break;
            case Log.INFO:
                for (int i = start; i < box.size(); i++) {
                    Log.i(getId(), box.get(i));
                }
                break;

            case Log.DEBUG:
                for (int i = start; i < box.size(); i++) {
                    Log.d(getId(), box.get(i));
                }
                break;
            case Log.VERBOSE:
                for (int i = start; i < box.size(); i++) {
                    Log.v(getId(), box.get(i));
                }
                break;
            case Log.WARN:
                for (int i = start; i < box.size(); i++) {
                    Log.w(getId(), box.get(i));
                }
                break;
            case Log.ASSERT:
                for (int i = start; i < box.size(); i++) {
                    Log.wtf(getId(), box.get(i));
                }
                break;

            default:
                for (int i = start; i < box.size(); i++) {
                    Log.v(getId(), box.get(i));
                }
                break;
            }
        }
    }

    public LogBuilder limit(int limit) {
        this.stackInfoLimit  = limit;
        return this;
    }

    public LogBuilder e(String event) {
        return direct(Log.ERROR,stackInfoLimit, event);
    }

    public LogBuilder i(String event) {
        return direct(Log.INFO,stackInfoLimit, event);
    }

    public LogBuilder v(String event) {
        return direct(Log.VERBOSE,stackInfoLimit, event);
    }

    public LogBuilder w(String event) {
        return direct(Log.WARN,stackInfoLimit, event);
    }

    public LogBuilder wtf(String event) {
        return direct(Log.ASSERT,stackInfoLimit, event);
    }

    public LogBuilder d(String event) {
        return direct(Log.DEBUG,stackInfoLimit, event);
    }


    public LogBuilder ne(String event) {
        return notDirect(Log.ERROR,stackInfoLimit, event);
    }

    public LogBuilder ni(String event) {
        return notDirect(Log.INFO,stackInfoLimit, event);
    }

    public LogBuilder nv(String event) {
        return notDirect(Log.VERBOSE,stackInfoLimit, event);
    }

    public LogBuilder nw(String event) {
        return notDirect(Log.WARN,stackInfoLimit, event);
    }

    public LogBuilder nwtf(String event) {
        return notDirect(Log.ASSERT,stackInfoLimit, event);
    }

    public LogBuilder nd(String event) {
        return notDirect(Log.DEBUG,stackInfoLimit, event);
    }

    public void d(NetworkInfo info) {
        String s;
        if(Utility.isValid(info)){
            s = info.toString();
        }else {
            s = "NetworkInfo is NULL";
        }
        d(s);
    }

    public void d(WifiInfo info) {
        String s;
        if(Utility.isValid(info)){
            s = info.toString();
        }else {
            s = "WifiInfo is NULL";
        }
        d(s);
    }

    public void d(String name, JSONObject json) {
        ArrayList<String> jsonBox = new ArrayList<String>();
        jsonBox.add(getUpHeader(name));
        if(Utility.isValid(json)){
            try {
                synchronized(jsonBox) {
                    getLines(jsonBox, name, JsonFormatter.formatObject(json.toString()));
                }
            } catch (Exception e) {
                Log.e(TAG,"JSONObject e error:"+e.toString());
            }
        }else {
            synchronized(jsonBox) {
                getLines(jsonBox, name, "JSONObject is NULL");
            }
        }
        jsonBox.add(getDownHeader(name));
        for(String str : jsonBox)
            Log.e(getId(), str);
    }

    public void d(String name, JSONArray json) {
        ArrayList<String> jsonBox = new ArrayList<String>();
        jsonBox.add(getUpHeader(name));
        if(Utility.isValid(json)){
            try {
                synchronized(jsonBox) {
                    getLines(jsonBox, name, JsonFormatter.formatArray(json.toString()));
                }
            } catch (Exception e) {
                Log.e(TAG,"JSONObject e error:"+e.toString());
            }
        }else {
            synchronized(jsonBox) {
                getLines(jsonBox, name, "JSONObject is NULL");
            }
        }
        jsonBox.add(getDownHeader(name));
        for(String str : jsonBox)
            Log.e(getId(), str);
    }

//    public void list(HashMap<Integer, Object> senderList) {
//        int w = RpcCall.getMaxNameLength() + 4;
//        nv(START);
//        nv("Sender List");
//        nv(PAUSE);
//        Object[] ids = senderList.keySet().toArray();
//        for (int i = 0; i < ids.length; i++) {
//            int id = Integer.valueOf(ids[i].toString());
//            RpcCall rpcType = RpcCall.getTypeFromId(id);
//            String idString = (Utility.isValid(rpcType)?rpcType.name():"") + "(" + id + ")";
//            idString += String.format("%0" + (w - idString.length()) + "d", 0).replace("0", " ");
//            nv(idString + senderList.get(id).getClass().getSimpleName());
//        }
//        nv(END);
//    }

    public void e(String name, JSONObject json) {
        if(isEnable ){
            if(Utility.isValid(json)){
                try {
                    ArrayList<String> jsonBox = new ArrayList<String>();
                    jsonBox.add(getUpHeader(name));
                    synchronized(jsonBox) {
                        getLines(jsonBox, name, JsonFormatter.formatObject(json.toString()));
                    }
                    jsonBox.add(getDownHeader(name));
                    for(String str : jsonBox)
                        Log.e(getId(), str);
                } catch (Exception e) {
                    Log.e(TAG,"JSONObject e error:"+e.toString());
                }
            }
        }
    }

    public void e(String name, JSONArray json) {
        if(isEnable ){
            if(json != null){
                try {
                    ArrayList<String> jsonBox = new ArrayList<String>();
                    jsonBox.add(getUpHeader(name));
                    synchronized(jsonBox) {
                        getLines(jsonBox, name, JsonFormatter.formatArray(json.toString()));
                    }
                    jsonBox.add(getDownHeader(name));
                    for(String str : jsonBox)
                        Log.e(getId(), str);
                } catch (Exception e) {
                    Log.e(TAG,"JSONObject e error:"+e.toString());
                }
            }
        }
    }

    public void e(String name, ArrayList<String> array) {
        if(isEnable ){
            if(array != null){
                try {
                    ArrayList<String> jsonBox = new ArrayList<String>();
                    jsonBox.add(getUpHeader(name));
                    synchronized(jsonBox) {
                        for (int i = 0; i < array.size(); i++) {
                            getLines(jsonBox, name, i + ". " + array.get(i));
                        }
                    }
                    jsonBox.add(getDownHeader(name));
                    for(String str : jsonBox)
                        Log.e(getId(), str);
                } catch (Exception e) {
                    Log.e(TAG,"JSONObject e error:"+e.toString());
                }
            }
        }
    }

    public void print() {
        if(isEnable ) {

            synchronized(item) {
                if(isEvent)
                    item.add(getEventDownHeader());
                else
                    item.add(getDownHeader(this.name));

                if(0 < item.size())
                {
                    switch (type) {
                    case Log.ERROR:
                        for(String str : item)
                            Log.e(getId(), str);
                        break;
                    case Log.INFO:
                        for(String str : item)
                            Log.i(getId(), str);
                        break;
                    case Log.DEBUG:
                        for(String str : item)
                            Log.d(getId(), str);
                        break;
                    case Log.VERBOSE:
                        for(String str : item)
                            Log.v(getId(), str);
                        break;
                    case Log.WARN:
                        for(String str : item)
                            Log.w(getId(), str);
                        break;
                    case Log.ASSERT:
                        for(String str : item)
                            Log.wtf(getId(), str);
                        break;
                    default:
                        break;
                    }
                    item.clear();
                }
            }
        }
    }

    public void print(String name, String value) {
        Log.i(getId(), getUpHeader(name));
        Log.i(getId(), getLines(name,value));
        Log.i(getId(), getDownHeader(name));
    }

    private void getLines(ArrayList<String> strList, String name, String value) {
        if(name != null && !name.isEmpty())
            name += ": ";
        else
            name = "";
        if(value == null)
            value = "";

        if(logWidth - name.length() - value.length() - 1 < 0){
            strList.add(getSingleLine(0, contentWidth, name));
            //multiple lines with LINE_FEED
            if(value.indexOf(LINE_FEED) > -1)
                for (String body1 : getStringList(value, LINE_FEED))
                    for (String body2 : getStringList(body1, contentWidth - logIndent))
                        strList.add(getSingleLine(logIndent, contentWidth - logIndent, body2));
            //multiple lines without LINE_FEED
            else
                for (String body : getStringList(value, contentWidth - logIndent))
                    strList.add(getSingleLine(logIndent, contentWidth - logIndent, body));
        //single line
        }else {
            //single line with LINE_FEED
            if(value.indexOf(LINE_FEED) > -1){
                strList.add(getSingleLine(0, contentWidth, name));
                for (String body : getStringList(value, LINE_FEED))
                    strList.add(getSingleLine(logIndent, contentWidth - logIndent, body));
            //single line without LINE_FEED
            }else
                strList.add(getSingleLine(0, contentWidth, name + value));
        }
    }

    private String getLines(String name, String value) {
        if(name != null && !name.isEmpty())
            name += ": ";
        else
            name = "";
        if(value == null)
            value = "";
        lines.clear();
        if(logWidth - name.length() - value.length() - 1 < 0){
            lines.add(getSingleLine(0, contentWidth, name) + LINE_FEED);
            //multiple lines with LINE_FEED
            if(value.indexOf(LINE_FEED) > -1)
                for (String body1 : getStringList(value, LINE_FEED))
                    for (String body2 : getStringList(body1, contentWidth - logIndent))
                        lines.add(getSingleLine(logIndent, contentWidth - logIndent, body2) + LINE_FEED);
            //multiple lines without LINE_FEED
            else
                for (String body : getStringList(value, contentWidth - logIndent))
                    lines.add(getSingleLine(logIndent, contentWidth - logIndent, body) + LINE_FEED);
        //single line
        }else {
            //single line with LINE_FEED
            if(value.indexOf(LINE_FEED) > -1){
                lines.add(getSingleLine(0, contentWidth, name));
                for (String body : getStringList(value, LINE_FEED))
                    lines.add(getSingleLine(logIndent, contentWidth - logIndent, body) + LINE_FEED);
            //single line without LINE_FEED
            }else
                lines.add(getSingleLine(0, contentWidth, name + value) + LINE_FEED);
        }
        return TextUtils.join("", lines);
    }

    private List<String> getStringList(String string, int width) {
        List<String> strings = new ArrayList<String>();
        for (int i = 0; i < Math.ceil((float)string.length()/width); i++) {
            if(width*(i+1) < string.length())
                strings.add(string.substring(width*i, width*(i + 1)));
            else
                strings.add(string.substring(width*i));
        }
        return strings;
    }

    private List<String> getStringList(String string, String separate) {
        List<String> strings = new ArrayList<String>();
        int lastIndex = 0;
        while(lastIndex != -1){
            if(lastIndex != string.length())
                if(string.indexOf(separate,lastIndex) > -1)
                    strings.add(string.substring(lastIndex, string.indexOf(separate,lastIndex)));
                else
                    strings.add(string.substring(lastIndex));
            else
                strings.add("");
            lastIndex = string.indexOf(separate,lastIndex);
            if( lastIndex != -1){
                lastIndex += separate.length();
            }
        }
        return strings;
    }

    private String getSingleLine(int indent, int contentWidth, String content) {
        return  VERTICAL_LINE +
                getSpace(logMargin) +
                getSpace(indent) +
                content + getSpace(contentWidth - content.length()) +
                getSpace(logMargin) +
                VERTICAL_LINE;
    }

    private String getBoxContent(String event, String method, String value) {
        return  VERTICAL_LINE_BOX +
                getSpace(logMargin) +
                "[" + event + "] / [" + method + "] : " + value +
                getSpace(logMargin) +
                VERTICAL_LINE_BOX;
    }

    private String getBoxContent(String event) {
        return  VERTICAL_LINE_BOX +
                getSpace(logMargin) +
                event +
                getSpace(logMargin) +
                VERTICAL_LINE_BOX;
    }

    private String getUpHeader(String name) {
        if(name == null){
            name = "N/A";
        }else if (name.isEmpty()) {

        }
        int length = (int) Math.floor((float)(logWidth - (name.length()>0?name.length() + 4:2)) / 2);
        if(length < 0) length = 1;
        try {
            return  LEFT_TOP +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE) +
                    (name.length() > 0?String.format(" %" + (int)Math.ceil((float)name.length()/2)*2 + "s ", name):"") +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE) +
                    RIGHT_TOP;
        } catch (Exception e2) {
            return  LEFT_TOP +
                    RIGHT_TOP;
        }
    }

    private String getErrorUpHeader(Exception e) {
        String name = "";
        if(e == null){
            name = "( error )";
        }else {
            try {
                name = "( " + e.getClass().getSimpleName() + " | " + e.getCause() + " )";
            } catch (Exception e2) {
                name = "( error )";
            }
        }
        int length = (int) Math.floor((float)(logWidth - name.length() - 4) / 2);
        if(length < 0) length = 1;
        try {
            return  LEFT_TOP +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE) +
                    String.format(" %" + (int)Math.ceil((float)name.length()/2)*2 + "s ", name) +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE) +
                    RIGHT_TOP;
        } catch (Exception e2) {
            return  LEFT_TOP +
                    RIGHT_TOP;
        }
    }

    private String getEventUpHeader() {
        int length = (int) Math.floor((float)(logWidth - this.name.length() - 4) / 2);
        if(length < 0) length = 1;
        try {
            return  LEFT_TOP_EVENT +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE_EVENT) +
                    String.format(" %" + (int)Math.ceil((float)name.length()/2)*2 + "s ", name) +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE_EVENT) +
                    RIGHT_TOP_EVENT;
        } catch (Exception e2) {
            return  LEFT_TOP_EVENT +
                    RIGHT_TOP_EVENT;
        }
    }

    private String getBoxUpHeader(int width) {
        try {
            return  LEFT_TOP_BOX +
                    String.format("%0" + (width + 2) + "d", 0).replace("0", HORIZONTAL_LINE_BOX) +
                    RIGHT_TOP_BOX;
        } catch (Exception e2) {
            return  LEFT_TOP_BOX +
                    RIGHT_TOP_BOX;
        }
    }

    private String getDownHeader(String name) {
        if(name == null){
            name = "N/A";
        }else if (name.isEmpty()) {

        }
        int length = (int) Math.floor((float)(logWidth - (name.length()>0?name.length() + 4:2)) / 2);
        if(length < 0) length = 1;
        try {
            return  LEFT_BOTTOM +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE) +
                    (name.length() > 0?String.format(" %" + (int)Math.ceil((float)name.length()/2)*2 + "s ", name):"") +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE) +
                    RIGHT_BOTTOM;
        } catch (Exception e2) {
            return  LEFT_BOTTOM +
                    RIGHT_BOTTOM;
        }
    }

    private String getErrorDownHeader(Exception e) {
        String name = "";
        if(e == null){
            name = "( error )";
        }else {
            name = "( " + e.getClass().getSimpleName() + " | " + e.getCause() + " )";
        }
        int length = (int) Math.floor((float)(logWidth - name.length() - 4) / 2);
        if(length < 0) length = 1;
        try {
            return  LEFT_BOTTOM +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE) +
                    String.format(" %" + (int)Math.ceil((float)name.length()/2)*2 + "s ", name) +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE) +
                    RIGHT_BOTTOM;
        } catch (Exception e2) {
            return  LEFT_BOTTOM +
                    RIGHT_BOTTOM;
        }
    }

    private String getEventDownHeader() {
        int length = (int) Math.floor((float)(logWidth - this.name.length() - 4) / 2);
        if(length < 0) length = 1;
        try {
            return  LEFT_BOTTOM_EVENT +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE_EVENT) +
                    String.format(" %" + (int)Math.ceil((float)name.length()/2)*2 + "s ", name) +
                    String.format("%0" + length + "d", 0).replace("0", HORIZONTAL_LINE_EVENT) +
                    RIGHT_BOTTOM_EVENT;
        } catch (Exception e2) {
            return  LEFT_BOTTOM_EVENT +
                    RIGHT_BOTTOM_EVENT;
        }
    }

    private String getBoxDownHeader(int width) {
        try {
            return  LEFT_BOTTOM_BOX +
                    String.format("%0" + (width + 2) + "d", 0).replace("0", HORIZONTAL_LINE_BOX) +
                    RIGHT_BOTTOM_BOX;
        } catch (Exception e2) {
            return  LEFT_BOTTOM_BOX +
                    RIGHT_BOTTOM_BOX;
        }
    }

    private String getSeparator() {
        try {
            return  VERTICAL_LEFT_MIDDLE +
                    String.format("%0" + (logWidth - 2) + "d", 0).replace("0", HORIZONTAL_LINE) +
                    VERTICAL_RIGHT_MIDDLE;
        } catch (Exception e2) {
            return  VERTICAL_LEFT_MIDDLE +
                    VERTICAL_RIGHT_MIDDLE;
        }
    }

    //String.format("%0"+n+"d", 0).replace("0", s)
    private String getSpace(int i) {
        if(i > 0)
            return String.format("%0" + i + "d", 0).replace("0", " ");
        else
            return "";
    }

    public int getStackInfoLimit() {
        return stackInfoLimit;
    }

    public void setStackInfoLimit(int stackInfoLimit) {
        this.stackInfoLimit = stackInfoLimit;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public LogBuilder enable(boolean isEnable) {
        if(!isEnable){
            synchronized(item) {
                item.clear();
            }
        }
        this.isEnable = isEnable;
        return this;
    }

    public LogBuilder disableAll() {
        Object[] tags = logs.keySet().toArray();
        synchronized (box) {
            for (int i = 0; i < tags.length; i++) {
                logs.get(tags[i]).enable(false);
            }
        }
        nw("Log disableAll"
                + "\nLogs:\n"
                + StringFormatter.formatTable(true, 2, getLogInfo(0))
                );
//        DmSipJNI.jniLogDisable();
        return logs.get(getId());
    }

    public LogBuilder enableAll() {
        Object[] tags = logs.keySet().toArray();
        synchronized (box) {
            for (int i = 0; i < tags.length; i++) {
                logs.get(tags[i]).enable(true);
            }
        }
        ni("Log enableAll"
                + "\nLogs:\n"
                + StringFormatter.formatTable(true, 2, getLogInfo(0))
                );
//        DmSipJNI.jniLogEnable();
        return logs.get(getId());
    }

    private String[] getLogInfo(int limit) {
        //int i = 1;
        ArrayList<String> strings = new ArrayList<String>();
        String[] strings2 = {};
        String[] tags = {};
        tags = logs.keySet().toArray(tags);
        for (int i = 0; i < tags.length; i++) {
            strings.add(tags[i]);
            strings.add(""+logs.get(tags[i]).isEnable());
        }
        return strings.toArray(strings2);
    }

    public void clear(StringBuilder sb, int clearLength) {
        synchronized (sb) {
            try {
                if(clearLength > 0)
                    sb.delete(0, clearLength);
            } catch (Exception e) {
                Log.e(TAG,"clear error:" + e.toString());
            }
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

}
