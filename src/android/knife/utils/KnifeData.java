package com.kcchen.nativecanvas.knife.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.kcchen.nativecanvas.knife.KnifeText;
import com.kcchen.nativecanvas.knife.spans.KnifeAbsoluteSizeSpan;
import com.kcchen.nativecanvas.knife.spans.KnifeBulletSpan;
import com.kcchen.nativecanvas.knife.spans.KnifeQuoteSpan;
import com.kcchen.nativecanvas.knife.spans.KnifeURLSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kowei on 2018/3/29.
 */

public class KnifeData {
    public static final String SPAN_SEP = "=";
    public static final String SPAN_DATA_SEP = ",";
    public static final String SPAN_INDEX = "SpanIndex";
    public static final String SPAN_VALUE = "SpanValue";
    public static final String SPAN_DATA = "SpanData";
    public static final String SPAN_TEXT = "SpanText";

    public static JSONObject exportData(Spanned source) {
        try {
            JSONObject data = new JSONObject();
            data.put(SPAN_TEXT, source.toString());

            JSONArray spannedArray = new JSONArray();
            Object[] spanned = source.getSpans(0, source.length(), Object.class);
            for (int i = 0; i < spanned.length; i++) {
                try {
                    JSONObject spannedObject = new JSONObject();
                    int format = 0;
                    String formatData = "";

                    if (spanned[i] instanceof StyleSpan) {
                        StyleSpan styleSpan = (StyleSpan) spanned[i];
                        formatData += styleSpan.getStyle();
                        format = KnifeText.FORMAT_STYLE;
                    } else if (spanned[i] instanceof UnderlineSpan) {
                        format = KnifeText.FORMAT_UNDERLINED;
                    } else if (spanned[i] instanceof ForegroundColorSpan) {
                        ForegroundColorSpan foregroundColorSpan = (ForegroundColorSpan) spanned[i];
                        formatData += foregroundColorSpan.getForegroundColor();
                        format = KnifeText.FORMAT_FORGROUND;
                    } else if (spanned[i] instanceof BackgroundColorSpan) {
                        BackgroundColorSpan backgroundColorSpan = (BackgroundColorSpan) spanned[i];
                        formatData += backgroundColorSpan.getBackgroundColor();
                        format = KnifeText.FORMAT_BACKGROUND;
                    } else if (spanned[i] instanceof KnifeAbsoluteSizeSpan) {
                        KnifeAbsoluteSizeSpan absoluteSizeSpan = (KnifeAbsoluteSizeSpan) spanned[i];
                        formatData += absoluteSizeSpan.getSize();
                        format = KnifeText.FORMAT_TEXTSIZE;
                    } else if (spanned[i] instanceof StrikethroughSpan) {
                        format = KnifeText.FORMAT_STRIKETHROUGH;
                    } else if (spanned[i] instanceof KnifeBulletSpan) {
                        //int bulletColor, int bulletRadius, int bulletGapWidth
                        KnifeBulletSpan bulletSpan = (KnifeBulletSpan) spanned[i];
                        formatData += bulletSpan.getBulletColor();
                        formatData += SPAN_DATA_SEP;
                        formatData += bulletSpan.getBulletRadius();
                        formatData += SPAN_DATA_SEP;
                        formatData += bulletSpan.getBulletGapWidth();
                        format = KnifeText.FORMAT_BULLET;
                    } else if (spanned[i] instanceof KnifeQuoteSpan) {
                        //int quoteColor, int quoteStripeWidth, int quoteGapWidth
                        KnifeQuoteSpan quoteSpan = (KnifeQuoteSpan) spanned[i];
                        formatData += quoteSpan.getQuoteColor();
                        formatData += SPAN_DATA_SEP;
                        formatData += quoteSpan.getQuoteStripeWidth();
                        formatData += SPAN_DATA_SEP;
                        formatData += quoteSpan.getQuoteGapWidth();
                        format = KnifeText.FORMAT_QUOTE;
                    } else if (spanned[i] instanceof KnifeURLSpan) {
                        //String url, int linkColor, boolean linkUnderline
                        KnifeURLSpan urlSpan = (KnifeURLSpan) spanned[i];
                        formatData += urlSpan.getURL();
                        formatData += SPAN_DATA_SEP;
                        formatData += urlSpan.getLinkColor();
                        formatData += SPAN_DATA_SEP;
                        formatData += urlSpan.isLinkUnderline();
                        format = KnifeText.FORMAT_LINK;
                    }
                    if (format != 0) {
                        spannedObject.put(SPAN_INDEX, i);
                        spannedObject.put(SPAN_VALUE, format + SPAN_SEP + formatData + SPAN_SEP + source.getSpanStart(spanned[i]) + SPAN_SEP + source.getSpanEnd(spanned[i]) + SPAN_SEP + source.getSpanFlags(spanned[i]));
                        spannedArray.put(spannedObject);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            data.put(SPAN_DATA, spannedArray);
            return data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static SpannableString importData(JSONObject source) {
        try {
            if (source != null) {
                String text = source.optString(SPAN_TEXT);
                if (text != null) {
                    SpannableString spannableString = new SpannableString(text);
                    JSONArray data = source.optJSONArray(SPAN_DATA);
                    if (data != null) {
                        for (int i = 0; i < data.length(); i++) {
                            try {

                                JSONObject spannedData = data.optJSONObject(i);
                                int index = spannedData.optInt(SPAN_INDEX);
                                String value = spannedData.optString(SPAN_VALUE);
                                String[] values = value.split(SPAN_SEP);
                                int spanType = Integer.valueOf(values[0]);
                                String spanParam = String.valueOf(values[1]);
                                String[] spanParams = spanParam.split(SPAN_DATA_SEP);
                                int start = Integer.valueOf(values[2]);
                                int end = Integer.valueOf(values[3]);
                                int flags = Integer.valueOf(values[4]);

                                Object span = null;
                                switch (spanType) {
                                    case KnifeText.FORMAT_STYLE:
                                        span = new StyleSpan(Integer.valueOf(spanParams[0]));
                                        break;
                                    case KnifeText.FORMAT_UNDERLINED:
                                        span = new UnderlineSpan();
                                        break;
                                    case KnifeText.FORMAT_FORGROUND:
                                        span = new ForegroundColorSpan(Integer.valueOf(spanParams[0]));
                                        break;
                                    case KnifeText.FORMAT_BACKGROUND:
                                        span = new BackgroundColorSpan(Integer.valueOf(spanParams[0]));
                                        break;
                                    case KnifeText.FORMAT_TEXTSIZE:
                                        span = new KnifeAbsoluteSizeSpan(Integer.valueOf(spanParams[0]));
                                        break;
                                    case KnifeText.FORMAT_STRIKETHROUGH:
                                        span = new StrikethroughSpan();
                                        break;
                                    case KnifeText.FORMAT_BULLET:
                                        span = new KnifeBulletSpan(Integer.valueOf(spanParams[0]), Integer.valueOf(spanParams[1]), Integer.valueOf(spanParams[1]));
                                        break;
                                    case KnifeText.FORMAT_QUOTE:
                                        span = new KnifeQuoteSpan(Integer.valueOf(spanParams[0]), Integer.valueOf(spanParams[1]), Integer.valueOf(spanParams[1]));
                                        break;
                                    case KnifeText.FORMAT_LINK:
                                        span = new KnifeURLSpan(String.valueOf(spanParams[0]), Integer.valueOf(spanParams[1]), Boolean.valueOf(spanParams[1]));
                                        break;
                                    default:
                                        break;
                                }

                                if (span != null) spannableString.setSpan(span, start, end, flags);

                            } catch (Exception e) {
                                e.printStackTrace();
                                continue;
                            }
                        }
                    }
                    return spannableString;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SpannableString("");
    }
}
