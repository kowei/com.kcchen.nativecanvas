package com.kcchen.nativecanvas.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;

/**
 * <b>public class SipResultReceiver extends ResultReceiver</b><p>
 * set up ReceiveResult for intercommunication between classes<p>
 * <b>USAGE:</b><p>
 *
 *new SipResultReceiver(new Handler());<br>
 *setReceiver(this);<br>
 *onReceiveResult(do something);<p>
 *
 * then pass SipResultReceiver to the class you want it to send back<p>
 *
 *Bundle= new Bundle();<br>
 *Bundle.putInt(...);<br>
 *Bundle.putString(...);<br>
 *sipReceiver.send(1, Bundle);<p>
 *
 * @author KC Chen(kc.chen@datamitetek.com)
 *
 */
public class MessageBox extends ResultReceiver implements Parcelable{
    private Receiver receiver;

    public MessageBox(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public void putMessage(int code, Bundle messageBundle) {
        this.send(code, messageBundle);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (this.receiver != null)
            this.receiver.onReceiveResult(resultCode, resultData);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }
}
