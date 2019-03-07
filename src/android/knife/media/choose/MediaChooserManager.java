/*
 * Copyright (C) 2015-2018 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kcchen.nativecanvas.knife.media.choose;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.kcchen.nativecanvas.knife.api.RTMediaFactory;
import com.kcchen.nativecanvas.knife.api.media.RTAudio;
import com.kcchen.nativecanvas.knife.api.media.RTImage;
import com.kcchen.nativecanvas.knife.api.media.RTVideo;
import com.kcchen.nativecanvas.knife.media.MediaUtils;
import com.kcchen.nativecanvas.knife.media.MonitoredActivity;
import com.kcchen.nativecanvas.knife.media.choose.processor.MediaProcessor;
import com.kcchen.nativecanvas.knife.media.choose.processor.MediaProcessor.MediaProcessorListener;
import com.kcchen.nativecanvas.knife.utils.Constants;
import com.kcchen.penpal.R;


abstract class MediaChooserManager implements MediaProcessorListener {

    public interface MediaChooserListener {
        /**
         * Handle any error condition if at all, when you receive this callback
         */
        public void onError(String reason);
    }

    transient protected MonitoredActivity mActivity;
    transient protected RTMediaFactory<RTImage, RTAudio, RTVideo> mMediaFactory;

    // the type of chooser (see MediaChooserActivity.REQUEST_PICK_PICTURE etc.)
    transient protected Constants.MediaAction mMediaAction;

    transient private MediaChooserListener mListener;

    // the file path and name of the original file
    // the MediaChooserManager sets this once the user picked a file
    private String mOriginalFile;

    MediaChooserManager(MonitoredActivity activity, Constants.MediaAction mediaAction,
                        RTMediaFactory<RTImage, RTAudio, RTVideo> mediaFactory,
                        MediaChooserListener listener, Bundle savedInstanceState) {
        mActivity = activity;
        mMediaFactory = mediaFactory;
        mMediaAction = mediaAction;
        mListener = listener;

        if (savedInstanceState != null) {
            mOriginalFile = savedInstanceState.getString("mOriginalFile");
        }
    }

    void onSaveInstanceState(Bundle outState) {
        outState.putString("mOriginalFile", mOriginalFile);
    }

    /**
     * Call this method, to start the chooser, i.e, The camera app or the gallery depending upon the type
     *
     * @return False if it's not possible to choose a media (e.g. when no sdcard is available to take a picture)
     * @throws IllegalArgumentException
     */
    abstract boolean chooseMedia() throws IllegalArgumentException;

    /**
     * Call this method to process the result from within your onActivityResult
     * method. You don't need to do any processing at all. Just pass in the
     * request code and the data, and everything else will be taken care of.
     *
     * @param mediaAction
     * @param data
     */
    abstract void processMedia(Constants.MediaAction mediaAction, Intent data);

    @Override
    /* MediaChooserListener */
    public void onError(String reason) {
        if (mListener != null) {
            mListener.onError(reason);
        }
    }

    @SuppressLint("NewApi")
    protected void startActivity(Intent intent) {
        if (mActivity != null) {
            mActivity.startActivityForResult(intent, mMediaAction.requestCode());
        }
    }

    protected void startBackgroundJob(MediaProcessor processor) {
        mActivity.startBackgroundJob(R.string.rte_processing_image, processor);
    }

    protected String getOriginalFile() {
        return mOriginalFile;
    }

    protected void setOriginalFile(String originalFile) {
        mOriginalFile = originalFile;
    }

    protected String determineOriginalFile(Intent data) {
        mOriginalFile = null;
        if (data != null && data.getDataString() != null) {
            try {
                mOriginalFile = MediaUtils.determineOriginalFile(mActivity.getApplicationContext(), data.getData());
            } catch (IllegalArgumentException e) {
                onError(e.getMessage());
            }
        }
        return mOriginalFile;
    }
}
