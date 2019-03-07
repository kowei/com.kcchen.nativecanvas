package com.kcchen.nativecanvas.undo;

import android.text.TextUtils;

import com.kcchen.nativecanvas.drawtools.model.NCPaint;
import com.kcchen.nativecanvas.drawtools.model.NCPath;
import com.kcchen.nativecanvas.enums.UNDO_TYPE;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by ween on 11/29/14.
 */
public class UndoItemDrawing extends UndoItem{
    private static final String TAG = UndoItemDrawing.class.getSimpleName();
    public static final String UNDO_DRAWING_PAINT = "UndoDrawingPaint";
    public static final String UNDO_DRAWING_PATH = "UndoDrawingPath";

    private NCPath path;
    private NCPaint paint;

    public UndoItemDrawing(UNDO_TYPE type) {
        super(type, null, null);
    }

    public UndoItemDrawing() {
        super();
    }

    public UndoItemDrawing(JSONObject item) {
        super();
        importData(item);
    }

    public void setDrawing(NCPath path, NCPaint paint){
        this.path = path;
        this.paint = paint;
        if(path != null && path.getStamp() != 0) setStamp(path.getStamp());
    }

    public NCPath getPath() {
        return path;
    }

    public void setPath(NCPath path) {
        this.path = path;
        if(path != null && path.getStamp() != 0) setStamp(path.getStamp());
    }

    public NCPaint getPaint() {
        return paint;
    }

    public void setPaint(NCPaint paint) {
        this.paint = paint;
    }

    @Override
    public JSONObject exportData() {
        JSONObject data = super.exportData();
        try {
            data.put(UNDO_DRAWING_PAINT, paint.exportData());
            data.put(UNDO_DRAWING_PATH, path.exportData());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public boolean importData(JSONObject data) {
        boolean isImported = false;
        if(super.importData(data)){
            try {
                setPaint(new NCPaint(data.optJSONObject(UNDO_DRAWING_PAINT)));
                setPath(new NCPath(data.optJSONObject(UNDO_DRAWING_PATH)));
                isImported = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isImported;
    }

    @Override
    public int hashCode() {
        return super.hashCode()
                + ((paint != null) ? paint.hashCode() : -2)
                + ((path != null) ? path.hashCode() : -3)
                ;
    }

    @Override
    public String toString() {
        List<String> property = getProperties();
        property.add(UNDO_DRAWING_PATH + ":" + path);
        property.add(UNDO_DRAWING_PAINT + ":" + paint);
        return "{"
                + "\n" + TextUtils.join(",\n", property)
                + "\n}"
                ;
    }
}
