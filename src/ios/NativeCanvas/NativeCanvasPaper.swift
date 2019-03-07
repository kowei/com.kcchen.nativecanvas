//
//  NativeCanvasPaper.swift
//  PenPal
//
//  Created by KC Chen on 2017/11/6.
//
//

import Foundation
import UIKit

public class NativeCanvasPaper : UIView, OnUndoDelegate{

    public var isTouch:Bool = true
    private var isViewMode:Bool?
//    private var currentTool:PEN_TYPE = PEN_TYPE.BALLPOINT
    private var undo: NativeCanvasUndoManager?
    private var backgroundImage:UIImage?
    private var hasBackgroundImage:Bool = true
    private var _dimention:CGRect?
    public var dimention:CGRect?{
        get{
            return _dimention
        }
        set{
            _dimention = newValue
            _dimention = _dimention?.adjustStatus()
            frame = _dimention!
            bounds = dimention!
            ColorLog.yellow(log: "> dimention changed")
            setNeedsLayout()
            setNeedsDisplay()
        }
    }
    var drawColor = UIColor.black
    var lineWidth: CGFloat = 5

    //private var bezierPath: UIBezierPath!
    //private var preRenderImage: UIImage!

    
    /*
     * SpeedSketch
     */
    var displayOptions = StrokeViewDisplayOptions.calligraphy {
        didSet {
            if strokeCollection != nil {
                setNeedsDisplay()
            }
            for view in dirtyRectViews {
                view.isHidden = displayOptions != .debug
            }
        }
    }
    
    var strokeCollection: StrokeCollection? {
        didSet {
            ColorLog.purple(log: "> stokes coming... \(strokeCollection?.strokes.count)")
            if oldValue !== strokeCollection {
                setNeedsDisplay()
            }
            if let lastStroke = strokeCollection?.strokes.last {
                setNeedsDisplay(for: lastStroke)
            }
            strokeToDraw = strokeCollection?.activeStroke
        }
    }
    
    var strokeToDraw: NativeCanvasStroke? {
        didSet {
            if oldValue !== strokeToDraw && oldValue != nil {
                setNeedsDisplay()
            } else {
                if let stroke = strokeToDraw {
                    setNeedsDisplay(for: stroke)
                }
            }
        }
    }
    
    // MARK: Dirty rect calculation and handling.
    var dirtyRectViews: [UIView]!
    var lastEstimatedSample: (Int, StrokeSample)?
    
    
    
    
    // MARK: Inits
    override init(frame: CGRect) {
        ColorLog.cyan(log: "> NativeCanvasPaper init frame:\(frame)");
        super.init(frame: frame);
        initialize()
    }

    required public init?(coder aDecoder: NSCoder) {
        ColorLog.cyan(log: "> NativeCanvasPaper init aDecoder:\(aDecoder)");
        super.init(coder: aDecoder);
        initialize()
    }

    /// Adds the subviews and initializes stack
    private func initialize() {
        self.backgroundColor = UIColor.white
        //self.borderColor = UIColor.black
        //self.borderWidth = 1
        self.isUserInteractionEnabled = true
        self.isMultipleTouchEnabled = true
        undo = NativeCanvasUndoManager()
        //bezierPath = UIBezierPath()
        //bezierPath.lineCapStyle = CGLineCap.round
        //bezierPath.lineJoinStyle = CGLineJoin.round
        setNeedsLayout()
        setNeedsDisplay()
        
        /*
         * SpeedSketch
         */
        layer.drawsAsynchronously = true
        
        let dirtyRectView = { () -> UIView in
            let view = UIView(frame: CGRect(x: -10, y: -10, width: 0, height: 0))
            view.layer.borderColor = UIColor.red.cgColor
            view.layer.borderWidth = 0.5
            view.isUserInteractionEnabled = false
            view.isHidden = true
            self.addSubview(view)
            return view
        }
        dirtyRectViews = [dirtyRectView(), dirtyRectView()]
    }

    /*
     * SpeedSketch
     */
    func setNeedsDisplay(for stroke:NativeCanvasStroke) {
        for dirtyRect in dirtyRects(for: stroke) {
            setNeedsDisplay(dirtyRect)
        }
    }

    func dirtyRects(for stroke:NativeCanvasStroke) -> [CGRect] {
        var result = [CGRect]()
        for range in stroke.updatedRanges() {
            var lowerBound = range.lowerBound
            if lowerBound > 0 { lowerBound -= 1 }
            
            if let (index, _) = lastEstimatedSample {
                if index < lowerBound {
                    lowerBound = index
                }
            }
            
            let samples = stroke.samples
            var upperBound = range.upperBound
            if upperBound < samples.count { upperBound += 1 }
            let dirtyRect = dirtyRectForSampleStride(stroke.samples[lowerBound..<upperBound])
            result.append(dirtyRect)
        }
        if stroke.predictedSamples.count > 0 {
            let dirtyRect = dirtyRectForSampleStride(stroke.predictedSamples[0..<stroke.predictedSamples.count])
            result.append(dirtyRect)
        }
        if let previousPredictedSamples = stroke.previousPredictedSamples {
            let dirtyRect = dirtyRectForSampleStride(previousPredictedSamples[0..<previousPredictedSamples.count])
            result.append(dirtyRect)
        }
        return result
    }
    
    func dirtyRectForSampleStride(_ sampleStride: ArraySlice<StrokeSample>) -> CGRect {
        var first = true
        var frame = CGRect.zero
        for sample in sampleStride {
            let sampleFrame = CGRect(origin: sample.location, size: .zero)
            if first {
                first = false
                frame = sampleFrame
            } else {
                frame = frame.union(sampleFrame)
            }
        }
        let maxStrokeWidth = CGFloat(20.0)
        return frame.insetBy(dx: -1 * maxStrokeWidth, dy: -1 * maxStrokeWidth)
    }
    
    // MARK: Drawing methods.
    
    
    /**
     Note: this is not a particularily efficient way to draw a great stroke path
     with CoreGraphics. It is just a way to produce an interesting looking result.
     For a real world example you would reuse and cache CGPaths and draw longer
     paths instead of an aweful lot of tiny ones, etc. You would also respect the
     draw rect to cull your draw requests. And you would use bezier paths to
     interpolate between the points to get a smooother curve.
     */
    func draw(stroke: NativeCanvasStroke, in rect:CGRect, isActive active: Bool) {
        let displayOptions = self.displayOptions
        
        let updateRanges = stroke.updatedRanges()
        if displayOptions == .debug {
            for (index, dirtyRectView) in dirtyRectViews.enumerated() {
                if index < updateRanges.count {
                    dirtyRectView.alpha = 1.0
                    dirtyRectView.frame = dirtyRectForSampleStride(stroke.samples[updateRanges[index]])
                } else {
                    dirtyRectView.alpha = 0.0
                }
            }
        }
        
        lastEstimatedSample = nil
        stroke.clearUpdateInfo()
        let sampleCount = stroke.samples.count
        guard sampleCount > 0 else {
            ColorLog.purple(log: "> no samples")
           return
        }
        guard let context = UIGraphicsGetCurrentContext() else {
            ColorLog.purple(log: "> no context")
            return
            
        }
        let strokeColor = UIColor.black
        
        let lineSettings: (()->())
        let forceEstimatedLineSettings: (()->())

        lineSettings = {
            context.setLineWidth(0.25)
            context.setStrokeColor(strokeColor.cgColor)
        }
        forceEstimatedLineSettings = lineSettings

        var forceOffset = CGFloat(0.1)
        
        let fillColorRegular = UIColor.black.cgColor
        let fillColorCoalesced = UIColor.lightGray.cgColor
        let fillColorPredicted = UIColor.red.cgColor
        
        var lockedAzimuthUnitVector: CGVector?
        let azimuthLockAltitudeThreshold = CGFloat.pi / 2.0 * 0.80 // locking azimuth at 80% altitude
        
        lineSettings()

        
        var heldFromSample: StrokeSample?
        var heldFromSampleUnitVector: CGVector?
        
        func draw(segment: StrokeSegment) {
            // ColorLog.purple(log: "> draw segment")
            if let toSample = segment.toSample {
                let fromSample: StrokeSample = heldFromSample ?? segment.fromSample
                
                // Skip line segments that are too short.
                if (fromSample.location - toSample.location).quadrance < 0.003 {
                    if heldFromSample == nil {
                        heldFromSample = fromSample
                        heldFromSampleUnitVector = segment.fromSampleUnitNormal
                    }
                    return
                }
                
                if toSample.predicted {
                    if displayOptions == .debug {
                        context.setFillColor(fillColorPredicted)
                    }
                } else {
                    if displayOptions == .debug && fromSample.coalesced {
                        context.setFillColor(fillColorCoalesced)
                    } else {
                        context.setFillColor(fillColorRegular)
                    }
                }
                
                if displayOptions == .calligraphy {
                    
                    #if swift(>=4)
                    var fromAzimuthUnitVector = Stroke.calligraphyFallbackAzimuthUnitVector
                    var toAzimuthUnitVector   = Stroke.calligraphyFallbackAzimuthUnitVector
                    
                    if fromSample.azimuth != nil {
                        
                        if lockedAzimuthUnitVector == nil {
                            lockedAzimuthUnitVector = fromSample.azimuthUnitVector
                        }
                        fromAzimuthUnitVector = fromSample.azimuthUnitVector
                        toAzimuthUnitVector = toSample.azimuthUnitVector
                        if fromSample.altitude! > azimuthLockAltitudeThreshold {
                            fromAzimuthUnitVector = lockedAzimuthUnitVector!
                        }
                        if toSample.altitude! > azimuthLockAltitudeThreshold {
                            toAzimuthUnitVector = lockedAzimuthUnitVector!
                        } else {
                            lockedAzimuthUnitVector = toAzimuthUnitVector
                        }
                        
                    }
                    // Rotate 90 degrees
                    let calligraphyTransform = CGAffineTransform(rotationAngle: CGFloat.pi / 2.0)
                    fromAzimuthUnitVector = fromAzimuthUnitVector.apply(transform: calligraphyTransform)
                    toAzimuthUnitVector = toAzimuthUnitVector.apply(transform: calligraphyTransform)
                    
                    let fromUnitVector = fromAzimuthUnitVector * forceAccessBlock(fromSample)
                    let toUnitVector = toAzimuthUnitVector * forceAccessBlock(toSample)
                    #endif
                    
                    context.beginPath()
                    #if swift(>=4)
                    context.move(to: fromSample.location + fromUnitVector)
                    context.addLine(to: toSample.location + toUnitVector)
                    context.addLine(to: toSample.location - toUnitVector)
                    context.addLine(to: fromSample.location - fromUnitVector)
                    #else
                    context.move(to: fromSample.location)
                    context.addLine(to: toSample.location)
                    context.addLine(to: toSample.location)
                    context.addLine(to: fromSample.location)
                    #endif
                    context.closePath()

                    context.drawPath(using: .fillStroke)
                    
                } else {
                    
                    #if swift(>=4)
                    let fromUnitVector = (heldFromSampleUnitVector != nil ? heldFromSampleUnitVector! : segment.fromSampleUnitNormal) * forceAccessBlock(fromSample)
                    let toUnitVector = segment.toSampleUnitNormal * forceAccessBlock(toSample)
                    
                    let isForceEstimated = fromSample.estimatedProperties.contains(.force) || toSample.estimatedProperties.contains(.force)
                    if isForceEstimated {
                        if lastEstimatedSample == nil {
                            lastEstimatedSample = (segment.fromSampleIndex+1,toSample)
                        }
                        forceEstimatedLineSettings()
                    } else {
                        lineSettings()
                    }
                    #endif
                    
                    context.beginPath()
                    #if swift(>=4)
                    context.move(to: fromSample.location + fromUnitVector)
                    context.addLine(to: toSample.location + toUnitVector)
                    context.addLine(to: toSample.location - toUnitVector)
                    context.addLine(to: fromSample.location - fromUnitVector)
                    #else
                    context.move(to: fromSample.location)
                    context.addLine(to: toSample.location)
                    context.addLine(to: toSample.location)
                    context.addLine(to: fromSample.location)
                    #endif
                    
                    context.closePath()
                    context.drawPath(using: .fillStroke)
                }
                
                #if swift(>=4)
                let isEstimated = fromSample.estimatedProperties.contains(.azimuth)
                if fromSample.azimuth != nil && (!fromSample.coalesced || isEstimated) && !fromSample.predicted && displayOptions == .debug {
                    
                    let length = CGFloat(20.0)
                    let azimuthUnitVector = fromSample.azimuthUnitVector
                    let azimuthTarget = fromSample.location + azimuthUnitVector * length
                    let altitudeStart = azimuthTarget + (azimuthUnitVector * (length / -2.0))
                    let altitudeTarget = altitudeStart + (azimuthUnitVector * (length / 2.0)).apply(transform: CGAffineTransform(rotationAngle: fromSample.altitude!))
                    
                    // Draw altitude as black line coming from the center of the azimuth.
                    altitudeSettings()
                    context.beginPath()
                    context.move(to: altitudeStart)
                    context.addLine(to: altitudeTarget)
                    context.strokePath()
                    
                    // Draw azimuth as orange (or blue if estimated) line.
                    azimuthSettings()
                    if isEstimated {
                        context.setStrokeColor(UIColor.blue.cgColor)
                    }
                    context.beginPath()
                    context.move(to: fromSample.location)
                    context.addLine(to: azimuthTarget)
                    context.strokePath()
                    
                }
                #endif
                
                if heldFromSample != nil {
                    heldFromSample = nil
                    heldFromSampleUnitVector = nil
                }
            }
        }
        
        if stroke.samples.count == 1 {
            // Construct a face segment to draw for a stroke that is only one point.
            let sample = stroke.samples.first!
            

            let tempSampleFrom = StrokeSample(timestamp: sample.timestamp, location: sample.location + CGVector(dx: -0.5, dy: 0.0), coalesced: false)
            let tempSampleTo = StrokeSample(timestamp: sample.timestamp, location: sample.location, coalesced: false)

            let segment = StrokeSegment(sample: tempSampleFrom)
            segment.advanceWithSample(incomingSample: tempSampleTo)
            segment.advanceWithSample(incomingSample: nil)
            
            draw(segment: segment)
        } else {
            for segment in stroke {
                ColorLog.purple(log: "> draw segment \(segment)")
                draw(segment:segment)
            }
        }
        
    }
    
    public override func draw(_ rect: CGRect) {
        UIColor.white.set()
        UIRectFill(rect)
        //ColorLog.purple(log: "> draw")
        // Optimization opportunity: Draw the existing collection in a different view,
        // and only draw each time we add a stroke.
        if let strokeCollection = strokeCollection {
            for stroke in strokeCollection.strokes {
                //ColorLog.purple(log: "> draw real")
                draw(stroke: stroke, in: rect, isActive: false)
            }
        }
        
        if let stroke = strokeToDraw {
            //ColorLog.purple(log: "> draw real active")
            draw(stroke: stroke, in: rect, isActive: true)
        }
        // self.drawGrid(lines: 10)
    }

    public override func setNeedsLayout() {
        ColorLog.lightBlue(log: "> Paper setNeedsLayout")
        DispatchQueue.main.async {
            if self.hasBackgroundImage{
                ColorLog.lightBlue(log: "> Paper hasBackgroundImage")
                if (self.backgroundImage != nil){
                    ColorLog.lightBlue(log: "> Paper backgroundImage is nil")
                }else{
                    if let d = self.dimention{
                        ColorLog.blue(log: "> Paper *setNeedsLayout: \(d)")
                        ColorLog.blue(log: "> Paper *setNeedsLayout:www \(NativeCanvasPenpalLibrary.getCurrentPage().debugDescription)")
//                        ColorLog.blue(log: "> Paper *setNeedsLayout:zzz \(NativeCanvasPenpalLibrary.getCurrentPage()?.getPageFile())")
                        if let page = NativeCanvasPenpalLibrary.getCurrentPage(){
                            
                            if let image = page.getBackground(size: d.size){
                                self.backgroundImage = image
                                ColorLog.blue(log: ">Paper *setNeedsLayout getBackground: \(image)")
                                let imageView:UIImageView = UIImageView(image: image)
                                imageView.contentMode = UIViewContentMode.scaleAspectFit//

                                imageView.frame = d
                                imageView.bounds = d
                                self.addSubview(imageView)
                                self.bringSubview(toFront: imageView)
                            }else{
                                self.hasBackgroundImage = false
                            }
                        }
                    }
                }
                ColorLog.lightBlue(log: "> Paper backgroundImage is't nil")
            }
            super.setNeedsLayout()
        }
    }

    public func setTouch(isTouch:Bool){
        self.isTouch = isTouch;
        if self.isTouch {
            //            self.touch
        }
    }
//
//    public override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?) {
//        if isTouch && isDrawing{
//            guard touches.count != 0 else { return }
//
//            let touchPoint = touches.first!.location(in: self)
//            self.lastTouchPoint = touchPoint
//            //bezierPath.removeAllPoints()
//
//            ColorLog.lightBlue(log: "> Paper touchesBegan \(touches.count) \(touchPoint.round(at: 2))")
//
//            if self.currentTool == .BALLPOINT || self.currentTool == .ERASER || self.currentTool == .HIGHLIGHTER {
//                //self.pointsBuffer.append(touchPoint)
//                //self.updateTopLayer()
//                self.setNeedsDisplay()
//            }
//        }
//    }
//
//    public override func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?) {
//        if isTouch && isDrawing{
//            guard touches.count != 0 else { return }
//
//            let touchPoint = touches.first!.location(in: self)
//
//            if let point = lastTouchPoint{
//                bezierPath.move(to: point)
//                bezierPath.addLine(to: touchPoint)
//            }
//            self.lastTouchPoint = touchPoint
//
//
//
//            //
//            //if !self.frame.contains(rawPoint){
//            //    touchesEnded(touches, with: event)
//            //    return
//            //}
//
//            ColorLog.lightBlue(log: "> Paper touchesMoved \(touches.count) \(touchPoint.round(at: 2))")
//
//
//
//            if self.currentTool == .BALLPOINT || self.currentTool == .ERASER || self.currentTool == .HIGHLIGHTER {
//                //self.pointsBuffer.append(touchPoint)
//                //self.updateTopLayer()
//                self.setNeedsDisplay()
//            }
//        }
//    }
//
//    public override func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?) {
//        if isTouch && isDrawing{
//            let touchPoint = touches.first!.location(in: self)
//            ColorLog.lightBlue(log: "> Paper touchesCancelled \(touches.count) \(touchPoint.round(at: 2))")
//            bezierPath.removeAllPoints()
//            setNeedsDisplay()
//        }
//    }
//
//    public override func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?) {
//        if isTouch && isDrawing{
//            let touchPoint = touches.first!.location(in: self)
//            ColorLog.lightBlue(log: "> Paper touchesEnded \(touches.count) \(touchPoint.round(at: 2))")
//            renderToImage()
//            setNeedsDisplay()
//            bezierPath.removeAllPoints()
//        }
//    }
//
//    // MARK: - Pre render
//
//    func renderToImage() {
//        if(bezierPath.isEmpty) { return }
//
//        if let bounds = dimention{
//            ColorLog.red(log: "> \(bounds)")
//            let frame = CGRect(x: 0, y: 0, width: bounds.width + x, height: bounds.height + y)
//            UIGraphicsBeginImageContextWithOptions(frame.size, false, 0.0)
//            if preRenderImage != nil{
//                preRenderImage.draw(in: frame)
//            }
//
//            bezierPath.lineWidth = lineWidth
//            drawColor.setFill()
//            drawColor.setStroke()
//            bezierPath.stroke()
//
//            preRenderImage = UIGraphicsGetImageFromCurrentImageContext()
//
//            UIGraphicsEndImageContext()
//        }
//
//    }
//
//    override public func draw(_ rect: CGRect) {
//        super.draw(rect)
//        ColorLog.lightBlue(log: "> Paper draw")
//        if let bounds = dimention{
//            let frame = CGRect(x: 0, y: 0, width: bounds.width + x, height: bounds.height + y)
//            if preRenderImage != nil {
//                preRenderImage.draw(in: frame)
//            }
//
//            bezierPath.lineWidth = lineWidth
//            drawColor.setFill()
//            drawColor.setStroke()
//            bezierPath.stroke()
//            self.drawGrid(lines: 10)
//        }
//    }
//
//
    private func drawGrid(lines:Int){

        let aPath = UIBezierPath()
        for i in 1 ... (lines - 1){
            aPath.move(to: CGPoint(     x:self.frame.origin.x + (self.frame.width * CGFloat(i) / CGFloat(lines)),     y:self.frame.origin.y))
            aPath.addLine(to: CGPoint(  x:self.frame.origin.x + (self.frame.width * CGFloat(i) / CGFloat(lines)),     y:self.frame.origin.y + (self.frame.height)))
            aPath.move(to: CGPoint(     x:self.frame.origin.x,                              y:self.frame.origin.y+(self.frame.height * CGFloat(i) / CGFloat(lines))))
            aPath.addLine(to: CGPoint(  x:self.frame.origin.x + (self.frame.width),         y:self.frame.origin.y+(self.frame.height * CGFloat(i) / CGFloat(lines))))
        }

        //Keep using the method addLineToPoint until you get to the one where about to close the path
        aPath.close()

        //If you want to stroke it with a red color
        UIColor.red.setStroke()
        aPath.stroke()
    }

    public func setViewMode(isViewMode: Bool){
        self.isViewMode = isViewMode
        if(self.isViewMode)!{
            //            self.touch
        }
        ColorLog.lightRed(log: "> Paper setViewMode is :\(isViewMode)");
    }

//    public func stopDrawing(){
////        isDrawing = false
////        bezierPath.removeAllPoints()
////        setNeedsDisplay()
//    }
//
//    public func startDrawing(){
////        isDrawing = true
////        bezierPath.removeAllPoints()
////        setNeedsDisplay()
//    }
//
//    public func hasDrawing()->Bool{
//        if let path = bezierPath{
//            return !path.isEmpty
//        }
//        return false
//    }
//    // MARK: Actions
//
//    func clear() {
//        preRenderImage = nil
//        bezierPath.removeAllPoints()
//        setNeedsDisplay()
//    }
    
    // inheritate from OnUndoDelegate
    public func onChanged() {
        ColorLog.cyan(log: "> onChanged")
    }
    
    // inheritate from OnUndoDelegate
    public func onImport(manager: NativeCanvasUndoManager) {
        ColorLog.cyan(log: "> onImport")
    }
    
    private func testUndoManager(){
        // test undo manager
        undo?.delegate = self
        undo?.removeDelegatess(delegate: self)
        undo?.delegate = self
        _ = undo?.isUpdate()
        undo?.add(undoItem: NativeCanvasUndoItemDrawing(type: UNDO_TYPE.DRAWING, objectName: "test1", data: nil))
        _ = undo?.isUpdate()
        undo?.add(undoItem: NativeCanvasUndoItem(type: UNDO_TYPE.ERASER, objectName: "test2", data: nil))
        _ = undo?.isUpdate()
        undo?._undoItems.append(nil)
        undo?.add(undoItem: NativeCanvasUndoItem(type: UNDO_TYPE.CLEAR, objectName: "test3", data: nil))
        _ = undo?.isUpdate()
        _ = undo?.undo()
        _ = undo?.isUpdate()
        _ = undo?.redo()
        _ = undo?.isUpdate()
        let exportData = undo?.exportData()
        ColorLog.lightYellow(log: "> undo manager exportData \(exportData)")
        ColorLog.lightGreen(log: "> undo manager \(undo)")
        undo?.importData(data: exportData!)
        _ = undo?.isUpdate()
        ColorLog.lightRed(log: "> undo manager \(undo)")
    }

}

