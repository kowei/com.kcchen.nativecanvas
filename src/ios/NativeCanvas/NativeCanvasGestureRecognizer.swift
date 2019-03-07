//
//  NativeCanvasGestureRecognizer.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//
//  Abstract:
//  The custom UIGestureRecognizer subclass to capture strokes.
//

import UIKit
import UIKit.UIGestureRecognizerSubclass


class NativeCanvasGestureRecognizer: UIGestureRecognizer {
    // MARK: Configuration.
    var collectsCoalescedTouches = true
    var usesPredictedSamples = true
    var isForPencil: Bool = false
    
    // MARK: Data.
    var stroke = NativeCanvasStroke()
    var coordinateSpaceView: UIView?
    
    // MARK: State.
    var trackedTouch: UITouch?
    var initialTimestamp: TimeInterval?
    
    var fingerStartTimer: Timer? = nil
    private let cancellationTimeInterval = TimeInterval(0.1)
    
    var ensuredReferenceView: UIView {
        if let view = coordinateSpaceView {
            return view
        } else  {
            return view!
        }
    }
    
    // MARK: Stroke data collection.
    func append(touches: Set<UITouch>, event: UIEvent?) -> Bool {
        ColorLog.cyan(log: "> append")
        if let touchToAppend = trackedTouch {
            
            // Cancel the stroke recognition if we get a second touch during cancellation period.
            for touch in touches {
                if touch !== touchToAppend &&
                    touch.timestamp - initialTimestamp! < cancellationTimeInterval {
                    if state == .possible {
                        state = .failed
                    } else {
                        state = .cancelled
                    }
                    return false
                }
            }
            
            // See if those touches contain our tracked touch. If not, ignore gracefully.
            if touches.contains(touchToAppend) {
                
                let collector = { (stroke: NativeCanvasStroke, touch: UITouch, view: UIView, coalesced: Bool , predicted: Bool ) in
                    

                    // Only collect samples that actually moved in 2D space.
                    let location = touch.location(in: view)
                    
                    if let previousSample = stroke.samples.last {
                        if (previousSample.location - location).quadrance < 0.003 {
                            return
                        }
                    }
                    
                    let sample = StrokeSample(timestamp: touch.timestamp, location: location, coalesced: coalesced)
                    
                    stroke.add(sample: sample)
                }
                
                let view = ensuredReferenceView
                collector(stroke, touchToAppend, view, false, false)
                
                return true
            }
        }
        return false
    }
    
    // MARK: Touch handling methods.
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
        ColorLog.cyan(log: "> touchesBegan")
        if trackedTouch == nil {
            ColorLog.cyan(log: "> touchesBegan trackedTouch")
            trackedTouch = touches.first
            initialTimestamp = trackedTouch?.timestamp
            #if swift(>=4)
            collectForce = trackedTouch!.type == .stylus || view?.traitCollection.forceTouchCapability == .available
            #endif
            if !isForPencil {
                 ColorLog.cyan(log: "> touchesBegan fingerStartTimer")
                fingerStartTimer = Timer.scheduledTimer(timeInterval: cancellationTimeInterval, target: self, selector: #selector(beginIfNeeded(_:)), userInfo: nil, repeats: false)
            }
        }
        if append(touches: touches, event:event) {
            ColorLog.purple(log: "> touchesBegan appended")
            if isForPencil {
                state = .began
            }
        }
    }
    
    // If not for pencil we give other gestures (pan, pinch) a chance by delaying our begin just a little.
    func beginIfNeeded(_ timer: Timer) {
        ColorLog.purple(log: "> beginIfNeeded \(state.name)")
        if state == .possible {
            state = .began
        }
    }
    
    override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
        //ColorLog.cyan(log: "> touchesMoved")
        if append(touches: touches, event:event) {
            ColorLog.purple(log: "> touchesMoved appended")
            if state == .began {
                state = .changed
            }
        }
    }
    
    override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
        //ColorLog.cyan(log: "> touchesEnded")
        if append(touches: touches, event:event) {
            ColorLog.purple(log: "> touchesEnded appended")
            stroke.state = .done
            state = .ended
        }
    }
    
    override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
        if append(touches: touches, event:event) {
            ColorLog.purple(log: "> touchesCancelled appended")
            stroke.state = .cancelled
            state = .failed
        }
    }
    

    
    override func reset() {
        stroke = NativeCanvasStroke()
        trackedTouch = nil
        if let timer = fingerStartTimer {
            timer.invalidate()
            fingerStartTimer = nil
        }
        super.reset()
    }
}
