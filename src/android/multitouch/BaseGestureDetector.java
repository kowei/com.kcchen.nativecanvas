package com.kcchen.nativecanvas.multitouch;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

/**
 * @author Almer Thie (code.almeros.com)
 *         Copyright (c) 2013, Almer Thie (code.almeros.com)
 *         <p>
 *         All rights reserved.
 *         <p>
 *         Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *         <p>
 *         Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *         Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 *         in the documentation and/or other materials provided with the distribution.
 *         <p>
 *         THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *         INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *         IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *         OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *         OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *         OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *         OF SUCH DAMAGE.
 */
public abstract class BaseGestureDetector{
    protected static final String TAG = BaseGestureDetector.class.getSimpleName();

    /**
     * This value is the threshold ratio between the previous combined pressure
     * and the current combined pressure. When pressure decreases rapidly
     * between events the position values can often be imprecise, as it usually
     * indicates that the user is in the process of lifting a pointer off of the
     * device. This value was tuned experimentally.
     */
    protected static final float PRESSURE_THRESHOLD = 0.67f;
    protected final Context context;
    protected boolean isGestureInProgress;
    protected MotionEvent previousEvent;
    protected MotionEvent currentEvent;
    protected float currentPressure;
    protected float previousPressure;
    protected long timeDelta;


    public BaseGestureDetector(Context context) {
        this.context = context;
    }

    /**
     * All gesture detectors need to be called through this method to be able to
     * detect gestures. This method delegates work to handler methods
     * (handleStartProgressEvent, handleInProgressEvent) implemented in
     * extending classes.
     *
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        final int actionCode = event.getAction() & MotionEvent.ACTION_MASK;
        if (!isGestureInProgress) {
            handleStartProgressEvent(actionCode, event);
        } else {
            handleInProgressEvent(actionCode, event);
        }
        return true;
    }

    /**
     * Called when the current event occurred when NO gesture is in progress
     * yet. The handling in this implementation may set the gesture in progress
     * (via isGestureInProgress) or out of progress
     *
     * @param actionCode
     * @param event
     */
    protected abstract void handleStartProgressEvent(int actionCode, MotionEvent event);

    /**
     * Called when the current event occurred when a gesture IS in progress. The
     * handling in this implementation may set the gesture out of progress (via
     * isGestureInProgress).
     *
     * @param actionCode
     * @param event
     */
    protected abstract void handleInProgressEvent(int actionCode, MotionEvent event);

    public MotionEvent getStartEvent() {
        if(previousEvent.getAction() == MotionEvent.ACTION_DOWN){
            return previousEvent;
        }
        return null;
    }

    public MotionEvent getPreviousEvent() {
        return previousEvent;
    }

    public MotionEvent getCurrentEvent() {
        return currentEvent;
    }

    protected void updateStateByEvent(MotionEvent curr) {
        final MotionEvent prev = previousEvent;
        if(prev == null) {
            Log.e(TAG, "> NCN Previous event LOSS!!!");
            return;
        }

        // Reset currentEvent
        if (currentEvent != null) {
            currentEvent.recycle();
            currentEvent = null;
        }
        currentEvent = MotionEvent.obtain(curr);


        // Delta time
        timeDelta = curr.getEventTime() - prev.getEventTime();

        // Pressure
        currentPressure = curr.getPressure(curr.getActionIndex());
        previousPressure = prev.getPressure(prev.getActionIndex());
    }

    protected void resetState() {
        if (previousEvent != null) {
            previousEvent.recycle();
            previousEvent = null;
        }
        if (currentEvent != null) {
            currentEvent.recycle();
            currentEvent = null;
        }
        isGestureInProgress = false;
    }


    /**
     * Returns {@code true} if a gesture is currently in progress.
     *
     * @return {@code true} if a gesture is currently in progress, {@code false} otherwise.
     */
    public boolean isInProgress() {
        return isGestureInProgress;
    }

    /**
     * Return the time difference in milliseconds between the previous accepted
     * GestureDetector event and the current GestureDetector event.
     *
     * @return Time difference since the last move event in milliseconds.
     */
    public long getTimeDelta() {
        return timeDelta;
    }

    /**
     * Return the event time of the current GestureDetector event being
     * processed.
     *
     * @return Current GestureDetector event time in milliseconds.
     */
    public long getEventTime() {
        return currentEvent.getEventTime();
    }

}
