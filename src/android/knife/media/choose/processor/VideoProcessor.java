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

package com.kcchen.nativecanvas.knife.media.choose.processor;


import com.kcchen.nativecanvas.knife.api.RTMediaFactory;
import com.kcchen.nativecanvas.knife.api.media.RTAudio;
import com.kcchen.nativecanvas.knife.api.media.RTImage;
import com.kcchen.nativecanvas.knife.api.media.RTVideo;

import java.io.IOException;

public class VideoProcessor extends MediaProcessor {

    public interface VideoProcessorListener extends MediaProcessorListener {
        public void onVideoProcessed(RTVideo video);
    }

    public VideoProcessor(String originalFile, RTMediaFactory<RTImage, RTAudio, RTVideo> mediaFactory, VideoProcessorListener listener) {
        super(originalFile, mediaFactory, listener);
    }

    @Override
    protected void processMedia() throws IOException, Exception {
        // TODO
    }

}
