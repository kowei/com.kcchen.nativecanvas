package com.kcchen.nativecanvas.knife.spans;


import android.os.Parcel;
import android.text.style.AbsoluteSizeSpan;

/**
 * Created by kowei on 2018/2/9.
 */

public class KnifeAbsoluteSizeSpan extends AbsoluteSizeSpan {
    public static final Creator CREATOR = new Creator();

    public KnifeAbsoluteSizeSpan(int size, boolean dip) {
        super(size, dip);
        // TODO Auto-generated constructor stub
    }


    public KnifeAbsoluteSizeSpan(int size) {
        super(size);
        // TODO Auto-generated constructor stub
    }


    public KnifeAbsoluteSizeSpan(Parcel src) {
        super(src);
        // TODO Auto-generated constructor stub
    }

    public static class Creator implements android.os.Parcelable.Creator {
        @Override
        public Object createFromParcel(Parcel source) {
            return null;
        }

        @Override
        public Object[] newArray(int size) {
            return new Object[0];
        }
    }
}
