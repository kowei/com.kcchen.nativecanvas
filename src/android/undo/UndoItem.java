package com.kcchen.nativecanvas.undo;

import android.text.TextUtils;

import com.kcchen.nativecanvas.enums.UNDO_TYPE;
import com.kcchen.nativecanvas.utils.JsonFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ween on 11/29/14.
 */
public class UndoItem{
    private static final String TAG = UndoItem.class.getSimpleName();
    public static final String UNDO_ITEM_TYPE = "UndoItemType";
    public static final String UNDO_OBJECT_NAME = "UndoObjectName";
    public static final String UNDO_DATA = "UndoData";
    public static final String UNDO_INDEX = "UndoIndex";
    public static final String UNDO_STAMP = "UndoStamp";
    public static final String UNDO_IS_TRANSFORM = "UndoIsTransform";

    private long stamp;

    private UNDO_TYPE type;
    private int index;
    private JSONObject data;
    private String objectName;
    private boolean isTransform = false;

    public UndoItem(UNDO_TYPE type, String objectName, JSONObject data) {
        super();
        this.type = type;
        this.objectName = objectName;
        this.data = data;
        this.stamp = System.currentTimeMillis();
    }

    public UndoItem() {
        super();
    }

    public UndoItem(JSONObject item) {
        super();
        importData(item);
    }

    public boolean isTransform() {
        return isTransform;
    }

    public void setTransform(boolean transform) {
        isTransform = transform;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public UNDO_TYPE getType() {
        return type;
    }

    public void setType(UNDO_TYPE type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void destroy() {
        data = null;
    }

    public JSONObject exportData() {

        JSONObject data = new JSONObject();
        try {

            data.put(UNDO_ITEM_TYPE, type.key());
            data.put(UNDO_OBJECT_NAME, (objectName == null || objectName.isEmpty()) ? "\"\"" : objectName);
            data.put(UNDO_DATA, this.data == null ? new JSONObject() : this.data);
            data.put(UNDO_INDEX, index);
            data.put(UNDO_STAMP, stamp);
            data.put(UNDO_IS_TRANSFORM, isTransform());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        try {

            setIndex(data.optInt(UNDO_INDEX));
            setType(UNDO_TYPE.get(data.optString(UNDO_ITEM_TYPE)));
            setObjectName(data.optString(UNDO_OBJECT_NAME));
            setStamp(data.optLong(UNDO_STAMP));
            setData(data.optJSONObject(UNDO_DATA));
            setTransform(data.optBoolean(UNDO_IS_TRANSFORM));

            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    public List<String> getProperties() {
        List<String> properties = new ArrayList<String>();
        try {

            properties.add(UNDO_ITEM_TYPE + ":" + type.key());
            properties.add(UNDO_INDEX + ":" + index);
            properties.add(UNDO_STAMP + ":" + stamp);
            properties.add(UNDO_IS_TRANSFORM + ":" + isTransform());
            properties.add(UNDO_DATA + ":" + JsonFormatter.format(this.data == null ? new JSONObject() : this.data));
            properties.add(UNDO_OBJECT_NAME + ":" + ((objectName == null || objectName.isEmpty()) ? "\"\"" : objectName));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return properties;
    }

    @Override
    public int hashCode() {
        return type.hashCode()
                + ((data != null) ? data.hashCode() : -1)
                ;
    }

    @Override
    public String toString() {
        return "{"
                + "\n" + TextUtils.join(",\n", getProperties())
                + "\n}"
                ;
    }

}
