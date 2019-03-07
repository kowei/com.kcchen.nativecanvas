package com.kcchen.nativecanvas.drawtools.model;

import android.graphics.Path;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kowei on 2018/3/13.
 */

public class NCPath extends Path {
    private static final String TAG = NCPath.class.getSimpleName();
    private static final String PATH_STAMP = "PathStamp";
    private static final String PATH_POINTS = "PathPoints";
    private static final String PATH_FILL_TYPE = "PathFillType";

    private long stamp;
    private ArrayList<NCPointF> points = new ArrayList<NCPointF>();

    public NCPath() {
    }

    public NCPath(NCPath src) {
        super(src);
        points = (ArrayList<NCPointF>) src.getPoints().clone();
        stamp = src.getStamp();
        setFillType(src.getFillType());
    }

    public NCPath(JSONObject jsonObject) {
        super();
        if(jsonObject != null) importData(jsonObject);
    }

    @Override
    public void reset() {
        super.reset();
        this.points.clear();
        this.stamp = 0;
    }

    public void moveTo(float x, float y, long stamp) {
        moveTo(x, y);
        this.stamp = stamp;
        addPoint(new NCPointF(x, y, (int) (stamp - this.stamp)));
    }

    public void lineTo(float x, float y, long stamp) {
        lineTo(x, y);
        addPoint(new NCPointF(x, y, (int) (stamp - this.stamp)));
    }

    public void addPoint(NCPointF point){
        points.add(point);
    }

    public NCPointF getLastPoint() {
        if (points.size() > 0) return points.get(points.size() - 1);
        return null;
    }

    public ArrayList<NCPointF> getPoints() {
        return points;
    }

    public void setPoints(JSONArray array){
        if(array != null){
            this.points.clear();
            reset();
            for(int i=0; i<array.length(); i++){
                String data = array.optString(i);
                if(data != null) {
                    NCPointF point = new NCPointF(data);
                    if(i == 0){
                        moveTo(point.x, point.y);
                    }else{
                        lineTo(point.x, point.y);
                    }
                    points.add(i, point);
                }else{
                    points.add(i, new NCPointF());
                }
            }
        }
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public JSONObject exportData() {
        JSONObject data = new JSONObject();
        try {
            data.put(PATH_STAMP, stamp);
            data.put(PATH_FILL_TYPE, getFillType().name());
            data.put(PATH_POINTS, new JSONArray(points.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean importData(JSONObject data) {
        boolean isImported = false;
        try {
            setPoints(data.optJSONArray(PATH_POINTS));
            setStamp(data.optLong(PATH_STAMP));
            FillType fillType = FillType.valueOf(data.optString(PATH_FILL_TYPE));
            if(fillType != null) setFillType(fillType);
            isImported = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    public NCPath clone(){
        return new NCPath(this);
    }

    public void destroy(){
        this.stamp = 0;
        this.points.clear();
    }

    @Override
    public String toString() {
        return "{"
                + "\n" + PATH_STAMP + ":" + stamp + ","
                + "\n" + PATH_FILL_TYPE + ":" + getFillType().name() + ","
                + "\n" + PATH_POINTS + ":" + points.toString()
                + "\n}"
                ;
    }

}
