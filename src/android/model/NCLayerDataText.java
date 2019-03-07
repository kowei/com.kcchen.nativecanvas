package com.kcchen.nativecanvas.model;

import android.text.Editable;

import org.json.JSONException;
import org.json.JSONObject;

public class NCLayerDataText extends NCLayerData {

    public static final String LAYER_TEXT = "LayerText";
    public static final String LAYER_FONT = "LayerFont";

    private String text;
    private Font font;
    private Editable spannableText;

    public NCLayerDataText(SCALE_LIMIT limit) {
        super(limit);
        this.font = new Font();
    }

    @Override
    public NCLayerData reset() {
        super.reset();
        this.text = "";
        this.font = new Font();
        return null;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public boolean importData(JSONObject data) {
        boolean isImported = false;
        isImported |= super.importData(data);
        try {
            setText(data.optString(LAYER_TEXT));
            JSONObject fontData = data.optJSONObject(LAYER_FONT);
            if(fontData != null){
                if(font == null ) font = new Font();
                if(!font.importData(fontData)){
                    throw new Exception("font import failed");
                }
            }else{
                throw new Exception("font data broken");
            }
            isImported |= true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isImported;
    }

    public void release() {

    }

    @Override
    public JSONObject exportData() {
        JSONObject data = super.exportData();
        try {
            data.put(LAYER_TEXT, getText());
            data.put(LAYER_FONT, getFont().exportData());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public String toString() {
        return super.toString() + " text:" + text + " " + font;
    }

    public void setSpannableText(Editable spannableText) {
        this.spannableText = spannableText;
    }

    public Editable getSpannableText() {
        return Editable.Factory.getInstance().newEditable(spannableText);
    }

    public String getSpannableTextString() {
        return spannableText.toString();
    }
}
