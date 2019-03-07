package com.kcchen.nativecanvas.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonHashTask extends AsyncTask<Object, Void, Integer> {


    private static final String TAG = "JsonHashTask";
    private LogBuilder logger;

    public JsonHashTask(){
        logger      = LogBuilder.with(TAG, Log.DEBUG);
    }

    @Override
    protected Integer doInBackground(Object... params) {
        int hash = 0;
        for (Object object : params) {
            if(object != null){
                if(object instanceof JSONObject){
                    hash += getJsonObjectHash((JSONObject) object);
                }else if (object instanceof JSONArray) {
                    hash += getJsonArrayHash((JSONArray) object);
                }else {
                    //DROP!!!
                }
            }
        }
        return hash;
    }

    private Integer getJsonArrayHash(JSONArray object) {
        int hash = 0;
        if(object != null){
            for (int i = 0; i < object.length(); i++) {
                if(object.isNull(i)){
                    hash += -2;
                }else {
                    try {
                        JSONArray array = object.getJSONArray(i);
                        if(array != null){
                            hash += getJsonArrayHash(array);
                        }else {
                            //DROP!!!
                        }
                    } catch (JSONException e) {
                        try {
                            JSONObject obj = object.getJSONObject(i);
                            if(obj != null){
                                hash += getJsonObjectHash(obj);
                            }else {
                                //DROP!!!
                            }
                        } catch (JSONException e1) {
                            try {
                                String str = object.optString(i);
                                if(Utility.isValid(str)){
                                    hash += str.hashCode();
                                }else {
                                    //DROP!!!
                                }
                            } catch (Exception e2) {
                                logger.error(e2, "getJsonArrayHash");
                            }
                        }
                    }
                }
            }
        }
        return hash;
    }

    private Integer getJsonObjectHash(JSONObject object) {
        int hash = 0;
        if(object != null){
            JSONArray objectNames = object.names();
            if(objectNames != null){
                for (int i = 0; i < objectNames.length(); i++) {
                    String name = objectNames.optString(i);
                    if(object.isNull(name)){
                        hash += -2;
                    }else {
                        try {
                            JSONArray array = object.getJSONArray(name);
                            if(array != null){
                                hash += getJsonArrayHash(array);
                            }else {
                                //DROP!!!
                            }
                        } catch (JSONException e) {
                            try {
                                JSONObject obj = object.getJSONObject(name);
                                if(obj != null){
                                    hash += getJsonObjectHash(obj);
                                }else {
                                    //DROP!!!
                                }
                            } catch (JSONException e1) {
                                try {
                                    String str = object.getString(name);
                                    if(Utility.isValid(str)){
                                        hash += (name + str).hashCode();
                                    }else {
                                        //DROP!!!
                                    }
                                } catch (JSONException e2) {
                                    logger.error(e2, "getJsonObjectHash");
                                }
                            }
                        }
                    }
                }
            }
        }
        return hash;
    }
}
