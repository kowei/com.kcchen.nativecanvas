//
//  DrawingPadController.swift
//  PenPal
//
//  Created by KC Chen on 2017/11/6.
//
//

import Foundation

@objc(NativeCanvasManager) class NativeCanvasManager : UIViewController, UAModalPanelDelegate, UIGestureRecognizerDelegate{
    
    fileprivate var paper: NativeCanvasPaper!
    fileprivate var paperContainer: NativeCanvasPaperContainer!
    private var scrollView: UIScrollView!
    private var fingerStrokeRecognizer: NativeCanvasGestureRecognizer!
    private var strokeCollection = StrokeCollection()
    private var notificationObservers = [NSObjectProtocol]()
    private var colorPicker: NativeCanvasColorPicker? = nil
    private var isViewMode:Bool = false
    private var panGesture: UIPanGestureRecognizer?
    private var pinchGesture: UIPinchGestureRecognizer?
    private var tapGesture: UITapGestureRecognizer?
    public var FullPath: String? = nil
    public var Book: String? = nil
    private let rootＤirectory = NSHomeDirectory()+"/Documents/"
    private var lastState:UIGestureRecognizerState?
    
    
    // MARK: Pencil Recognition and UI Adjustments
    /*
     Since usage of the Apple Pencil can be very temporary, the best way to
     actually check for it being in use is to remember the last interaction.
     Also make sure to provide an escape hatch if you modify your UI for
     times when the pencil is in use vs. not.
     */

    
    convenience init(){
        self.init(nibName: nil, bundle: nil)
        initialize()
    }
    
    override init(nibName nibNameOrNil: String?, bundle nibBundleOrNil: Bundle?) {
        super.init(nibName: nibNameOrNil, bundle: nibBundleOrNil);
        initialize()
    }
    
    required init?(coder aDecoder: NSCoder) {
        ColorLog.cyan(log: "> manager init aDecoder:\(aDecoder)");
        super.init(coder: aDecoder);
        initialize()
    }
    
    deinit {
        let defaultCenter = NotificationCenter.default
        for closure in notificationObservers {
            defaultCenter.removeObserver(closure)
        }
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
         ColorLog.cyan(log: "> manager viewWillAppear")
        let notifier = NotificationCenter.default
        notifier.addObserver(self, selector: #selector(onStatusbarChanged), name: NSNotification.Name.UIApplicationDidChangeStatusBarFrame, object: nil)
        notifier.addObserver(self, selector: #selector(onStatusbarWillChanged), name: NSNotification.Name.UIApplicationWillChangeStatusBarFrame, object: nil)
        // notifier.post(name: NSNotification.Name.UIApplicationDidChangeStatusBarFrame, object: nil)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        ColorLog.cyan(log: "> manager viewDidLoad")
    }

    override func viewWillDisappear(_ animated: Bool) {
        ColorLog.cyan(log: "> manager viewWillDisappear");
        self.destroy();
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        super.viewDidDisappear(animated)
        ColorLog.cyan(log: "> manager viewDidDisappear");
        let notifier = NotificationCenter.default
        notifier.removeObserver(self, name: NSNotification.Name.UIApplicationDidChangeStatusBarFrame, object: nil)
        notifier.removeObserver(self, name: NSNotification.Name.UIApplicationWillChangeStatusBarFrame, object: nil)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        ColorLog.cyan(log: "> manager viewDidAppear");
        scrollView.flashScrollIndicators()
    }
    
    func hitTest(point: CGPoint, event: UIEvent?) -> UIView? {
        if let view = colorPicker{
            if !view.isHidden{
                view.getPicker()?.showSelectedColorShapeAtPoint(point)
                //ColorLog.cyan(log: "> manager hitTest colorPicker \(point)")
                return view.hitTest(point, with: event)
            }
        }
        //ColorLog.cyan(log: "> manager hitTest Paper \(point))")

        // 因為 paper 在最上層，要先丟給 paper
        return self.paper.hitTest(point, with: event)
    }
    
    private func initialize(){
        DispatchQueue.main.async {
            ColorLog.cyan(log: "> manager initialize")
            self.view.backgroundColor = UIColor.gray
            
            if self.paper == nil{
                self.paper = NativeCanvasPaper()
                //self.paper.addShadow(ofColor: UIColor.white, radius: 3, offset: CGSize(width: 5, height: 5), opacity: 0.5)
                self.paper.autoresizingMask = [.flexibleWidth, .flexibleHeight]
                //self.view.addSubview(self.paper!)
                
                //add pan gesture
                //self.panGesture = UIPanGestureRecognizer(target: self, action: #selector(self.handlePan(pan:)))
                //self.panGesture?.delegate = self
                
                //add tap gesture
                //self.tapGesture = UITapGestureRecognizer(target: self, action: #selector(self.handleTap(tap:)))
                //self.tapGesture?.delegate = self
                
                //add pinch gesture
                //self.pinchGesture = UIPinchGestureRecognizer(target: self, action:#selector(self.handlePinch(pinch:)))
                //self.pinchGesture?.delegate = self
                
                //self.paper.addGestureRecognizer(self.panGesture!)
                //self.paper.addGestureRecognizer(self.pinchGesture!)
                //self.paper.addGestureRecognizer(self.tapGesture!)
                
            }
            
            if self.paperContainer == nil{
                self.paperContainer = NativeCanvasPaperContainer()
                self.paperContainer.documentView = self.paper
            }
            
            if self.scrollView == nil{
                self.scrollView = UIScrollView(frame: CGRect.zero)
                self.scrollView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
                self.scrollView.addSubview(self.paperContainer)
                self.scrollView.delegate = self
                self.scrollView.panGestureRecognizer.minimumNumberOfTouches = 2
                // We put our UI elements on top of the scroll view, so we don't want any of the
                // delay or cancel machinery in place.
                self.scrollView.delaysContentTouches = false
                
                self.view.addSubview(self.scrollView)
            }

            if self.fingerStrokeRecognizer == nil{
                self.fingerStrokeRecognizer = NativeCanvasGestureRecognizer(target: self, action: #selector(self.strokeUpdated(_:)))
                self.fingerStrokeRecognizer.delegate = self
                self.fingerStrokeRecognizer.cancelsTouchesInView = false
                self.scrollView.addGestureRecognizer(self.fingerStrokeRecognizer)
                self.fingerStrokeRecognizer.coordinateSpaceView = self.paper
                self.fingerStrokeRecognizer.isForPencil = false
            }
            
            if(self.colorPicker == nil){
                self.colorPicker = NativeCanvasColorPicker(frame: self.view.frame)
                self.view.superview?.addSubview(self.colorPicker!)
                self.colorPicker?.delegate = self
                self.colorPicker?.isHidden = true
            }
        }
    }
    
    internal func strokeUpdated(_ strokeGesture: NativeCanvasGestureRecognizer) {
        
        var stroke: NativeCanvasStroke?
        if strokeGesture.state != .cancelled {
            stroke = strokeGesture.stroke
            if strokeGesture.state == .began ||
                (strokeGesture.state == .ended && strokeCollection.activeStroke == nil) {
                strokeCollection.activeStroke = stroke
            }
        } else {
            strokeCollection.activeStroke = nil
        }
        
        if let stroke = stroke {
            if strokeGesture.state == .ended {
                strokeCollection.takeActiveStroke()
            }
        }
        
        paper.strokeCollection = strokeCollection
    }
    
    func receivedAllUpdatesForStroke(_ stroke: NativeCanvasStroke) {
        paper.setNeedsDisplay(for: stroke)
        stroke.clearUpdateInfo()
    }
    
    func clearButtonAction(_ sender: AnyObject) {
        self.strokeCollection = StrokeCollection()
        paper.strokeCollection = self.strokeCollection
    }
    
    // addObserver NSNotification.Name.UIApplicationDidChangeStatusBarFrame
    internal func onStatusbarChanged(){
        ColorLog.cyan(log: "> onStatusbarChanged \(UIApplication.shared.statusBarFrame.height)")
        if isModal(){
            colorPicker?.setNeedsLayout()
        }
    }
    
    // addObserver NSNotification.Name.UIApplicationWillChangeStatusBarFrame
    internal func onStatusbarWillChanged(){
        ColorLog.cyan(log: "> onStatusbarWillChanged \(UIApplication.shared.statusBarFrame.height)")
        if isModal(){
            colorPicker?.setNeedsLayout()
        }
    }
    
//    public func handlePan(pan: UIPanGestureRecognizer) {
//        if pan.state == .began || pan.state == .changed {
//            if let view = pan.view {
//
//                let translation = pan.translation(in: self.view)
//                //let scale = view.transform.d
//                var dx:CGFloat = translation.x
//                var dy:CGFloat = translation.y
//
//                dx = adjustXboundary(view: view, xoffset: dx)
//                dy = adjustYboundary(view: view, yoffset: dy)
//
//                var frame = view.frame
//                frame.origin.x += dx
//                frame.origin.y += dy
//                view.animateTo(frame: frame, withDuration: 0.2)
//
//                ColorLog.lightRed(log: "> pan \(translation.round(at: 2)) \(view.frame.round(at: 2)) \(self.paper.dimention?.round(at: 2))")
//
//                pan.setTranslation(CGPoint.zero, in: self.view)
//            }
//        }
//    }
    
    private func adjustXboundary(view:UIView, xoffset:CGFloat)->CGFloat{
        var dx:CGFloat = xoffset
        let currentLeft:CGFloat = view.frame.origin.x
        let currentRight:CGFloat = currentLeft + view.frame.width
        if let boundaryLeft = self.paper.dimention?.origin.x{
            if currentLeft <= boundaryLeft{
                if dx >= 0{
                    // 向右移
                    if currentLeft + dx <= boundaryLeft{
                        
                    }else{
                        // 修正
                        dx = boundaryLeft - currentLeft
                    }
                }else{
                    // 向左移
                }
            }else{
                dx = boundaryLeft - currentLeft
            }
            if let boundaryWidth = self.paper.dimention?.width{
                let boundaryRight = boundaryLeft + boundaryWidth
                if currentRight >= boundaryRight{
                    if dx >= 0{
                        // 向右移
                    }else{
                        // 向左移
                        if currentRight + dx >= boundaryRight{
                            
                        }else{
                            // 修正
                            dx = boundaryRight - currentRight
                        }
                    }
                }else{
                    dx = boundaryRight - currentRight
                }
            }
        }
        return dx
    }
    
    private func adjustYboundary(view:UIView, yoffset:CGFloat)->CGFloat{
        var dy:CGFloat = yoffset
        let currentTop:CGFloat = view.frame.origin.y
        let currentBottom:CGFloat = currentTop + view.frame.height
        if let boundaryTop = self.paper.dimention?.origin.y{
            if currentTop <= boundaryTop{
                if dy >= 0{
                    // 向下移
                    if currentTop + dy <= boundaryTop{
                        
                    }else{
                        // 修正
                        dy = boundaryTop - currentTop
                    }
                }else{
                    // 向上移
                }
            }else{
                dy = boundaryTop - currentTop
            }
            if let boundaryHeight = self.paper.dimention?.height{
                let boundaryBottom = boundaryTop + boundaryHeight
                if currentBottom >= boundaryBottom{
                    if dy >= 0{
                        // 向下移
                    }else{
                        // 向上移
                        if currentBottom + dy >= boundaryBottom{
                            
                        }else{
                            // 修正
                            dy = boundaryBottom - currentBottom
                        }
                    }
                }else{
                    dy = boundaryBottom - currentBottom
                }
            }
        }
        return dy
    }

//    public func handleTap(tap: UITapGestureRecognizer) {
//        ColorLog.lightBlue(log: "> handleTap \(tap.numberOfTapsRequired) \(tap.numberOfTouchesRequired) \(tap.numberOfTouches)")
//        if tap.state == UIGestureRecognizerState.ended
//            || tap.state == UIGestureRecognizerState.cancelled{
//            paper.startDrawing()
//        }
//    }
    
    public func handlePinch(pinch: UIPinchGestureRecognizer) {
        if pinch.state == UIGestureRecognizerState.ended
            || pinch.state == UIGestureRecognizerState.cancelled{
             //paper.startDrawing()
        }
        let currentState = pinch.state
        let previousState = self.lastState

        ColorLog.red(log: "> \(currentState.name)")
        
        switch pinch.state {
        case .began, .changed, .ended:
            if let view = pinch.view {
                
                let scale = (view.transform.d * 100).rounded()
                
                if scale >= 100{
                    if scale <= 500{
                        //let originalTransform = view.transform
                        let bounds = view.bounds
                        let pinchCenter = pinch.location(in: view)
                        let translation = CGPoint(x:pinchCenter.x - bounds.midX, y:pinchCenter.y - bounds.midY)
                        var dx:CGFloat = translation.x
                        var dy:CGFloat = translation.y
                        
                        dx = adjustXboundary(view: view, xoffset: dx)
                        dy = adjustYboundary(view: view, yoffset: dy)
                        //let translateTransform = originalTransform.translatedBy(x: dx, y: dy)
                        //let scaledAnTranslateTransform = translateTransform.scaledBy(x: pinch.scale, y: pinch.scale)
                        
                        var frame = view.frame
                        frame.origin.x += dx
                        frame.origin.y += dy
                        view.animateTo(scale: pinch.scale, origin: frame.origin, withDuration: 0.7)
                        { (isComplete) in
                            ColorLog.lightGreen(log: "> 1~5 animator complete:\(isComplete) last:\(previousState?.name) current:\(currentState.name)")
                            if let state = previousState{
                                if state == UIGestureRecognizerState.changed && currentState == UIGestureRecognizerState.ended{
                                    self.paper.setNeedsDisplay()
                                }
                                if let last = self.lastState{
                                    if last != UIGestureRecognizerState.ended{
                                        self.paper.setNeedsDisplay()
                                    }
                                }
                            }
                        }
                        
                        ColorLog.lightPurple(log: "> 1~5 \(pinch.state.name) \(("scale:"+scale.round(at: 2)).lightBlue) \(("pinch:" + pinchCenter.round(at: 2)).purple) \(("mid:" + frame.mid.round(at: 2)).lightRed) \(("offset:" + translation.round(at: 2)).lightGreen)")
                        
                        //UIView.animate(withDuration: 0.7, animations: {
                        //view.transform = scaledAnTranslateTransform
                        //})
                    }else{
                        //ColorLog.lightRed(log: "> scale back to 5 \(scale.round(at: 0))%")
                        let ds = 5 / view.transform.d
                        let originalTransform = view.transform
                        let scaledTransform = originalTransform.scaledBy(x: ds, y: ds)
                        
                        ColorLog.lightPurple(log: ">  ~5 \(pinch.state.name)")
                        
                        UIView.animate(withDuration: 0.7, animations: {
                            view.transform = scaledTransform
                        })
                        { (isComplete) in
                            ColorLog.lightGreen(log: ">  ~5 animator complete:\(isComplete) last:\(previousState?.name) current:\(currentState.name)")
                            if let state = previousState{
                                if state == UIGestureRecognizerState.changed && currentState == UIGestureRecognizerState.ended{
                                    self.paper.setNeedsDisplay()
                                }
                                if let last = self.lastState{
                                    if last != UIGestureRecognizerState.ended{
                                        self.paper.setNeedsDisplay()
                                    }
                                }
                            }
                        }
                    }
                    
                }else{
                    //ColorLog.lightRed(log: "> scale back to 1 \(scale.round(at: 0))%")
                    ColorLog.lightPurple(log: "> 1~  \(pinch.state.name)")
                    if let frame = self.paper.dimention{
                        view.animateTo(frame: frame, withDuration: 0.7)
                        { (isComplete) in
                            ColorLog.lightGreen(log: "> 1~  animator complete:\(isComplete) last:\(previousState?.name) current:\(currentState.name)")
                            if let state = previousState{
                                if state == UIGestureRecognizerState.changed && currentState == UIGestureRecognizerState.ended{
                                    self.paper.setNeedsDisplay()
                                }
                                if let last = self.lastState{
                                    if last != UIGestureRecognizerState.ended{
                                        self.paper.setNeedsDisplay()
                                    }
                                }
                            }
                        }
                    }
                }
                pinch.scale = 1
            }else{
                ColorLog.red(log: "> no view \(pinch.state.name)")
            }
        default:
            ColorLog.red(log: "> default")
            self.paper.setNeedsDisplay()
        }

//self.paper.setNeedsDisplay()
        lastState = pinch.state
    }
    
    public func handleRotate(recognizer : UIRotationGestureRecognizer) {
        if let view = recognizer.view {
            view.transform = view.transform.rotated(by: recognizer.rotation)
            recognizer.rotation = 0
        }
    }
    
    //MARK:- UIGestureRecognizerDelegate Methods for multitouch
    // We want the pencil to recognize simultaniously with all others.
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer:UIGestureRecognizer) -> Bool {
        
        return true
    }
    
    // Since our gesture recognizer is beginning immediately, we do the hit test ambiguation here
    // instead of adding failure requirements to the gesture for minimizing the delay
    // to the first action sent and therefore the first lines drawn.
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldReceive touch: UITouch) -> Bool {
        
        return true
    }
    

    
//    //MARK:- UIGestureRecognizerDelegate Methods for gesture begin
//    func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
//        //printGesture(state: gestureRecognizer.state, name: "ShouldBegin", touches: gestureRecognizer.numberOfTouches, otherTouches: 0)
//        paper.startDrawing()
//        if gestureRecognizer.numberOfTouches == 2{
//            ColorLog.lightBlue(log: "> Gesture begin")
//            paper.stopDrawing()
//            return true
//        }else{
//            if gestureRecognizer is UITapGestureRecognizer  && gestureRecognizer.numberOfTouches == 1{
////                if(paper.hasDrawing())!{
////                    return false
////                }
//                ColorLog.lightBlue(log: "> tap")
//                paper.stopDrawing()
//                return true
//            }
//            return false
//        }
//    }
    
    public func isModal()->Bool{
        let isHidden = colorPicker?.isHidden ?? false
        return !isHidden
    }
    
    public func destroy(){
        ColorLog.cyan(log: "> NCN DrawingPadController destroy");
        NotificationCenter.default.removeObserver(self);
        self.paper.removeFromSuperview();
        self.paper = nil;
    }
    
    public func setPosition(x:CGFloat,y:CGFloat,width:CGFloat,height:CGFloat){
        DispatchQueue.main.async {
            let frame = CGRect(x: x, y: y, width: width, height: height)
            ColorLog.cyan(log: "> setPosition \(frame)");

            self.paperContainer.initialize(canvasSize: frame.size)
            self.paperContainer.setNeedsDisplay()
            
            self.scrollView.frame = frame
            self.scrollView.contentSize = frame.size
            self.scrollView.contentOffset = self.paperContainer.canvasOrigin
            ColorLog.cyan(log: "> setPosition contentOffset \(self.scrollView.contentOffset)")
            self.scrollView.backgroundColor = self.paperContainer.backgroundColor
            self.scrollView.maximumZoomScale = 5.0
            self.scrollView.minimumZoomScale = 0.8
            self.scrollView.setNeedsDisplay()
            
            self.paper.dimention = CGRect(origin: CGPoint.zero, size: frame.size)
            self.paper.backgroundColor = UIColor.green
            self.paper.setNeedsDisplay()

            #if swift(>=4)
            self.scrollView.panGestureRecognizer.allowedTouchTypes = [UITouchType.direct.rawValue as NSNumber]
            self.scrollView.pinchGestureRecognizer?.allowedTouchTypes = [UITouchType.direct.rawValue as NSNumber]
            #endif
            

            //self.testSVG(x: x, y: y, width: width, height: height)
        }

        // 測試 UAModalPanel +
        //colorPicker?.show(from: self.view.center)
    }

    //inherit from UAModalPanelDelegate
    func willShow(_ modalPanel: UAModalPanel!) {
        ColorLog.lightYellow(log: "> willShowModalPanel")
    }
    
    //inherit from UAModalPanelDelegate
    func didShow(_ modalPanel: UAModalPanel!) {
        ColorLog.lightYellow(log: "> didShowModalPanel")
    }
    
    //inherit from UAModalPanelDelegate
    func willClose(_ modalPanel: UAModalPanel!) {
        ColorLog.lightYellow(log: "> willCloseModalPanel")
    }
    
    //inherit from UAModalPanelDelegate
    func didClose(_ modalPanel: UAModalPanel!) {
        ColorLog.lightYellow(log: "> didCloseModalPanel \((modalPanel as! NativeCanvasColorPicker).selectedColor ?? UIColor.clear)")
    }

    public func getPaper()->NativeCanvasPaper{
        return paper
    }
    
    public func getScrollView()->UIScrollView{
        return scrollView
    }
    
    public func touchAction(_ sender:UITapGestureRecognizer){
        ColorLog.cyan(log: "> touched");
    }
    
    public func setTouch(isTouch:Bool){
        if let view = paper{
            view.setTouch(isTouch: isTouch)
            ColorLog.cyan(log: "> NCN setTouch is \(isTouch) ");
        }
    }
    public func getNativeCanvas(){
        ColorLog.cyan(log: "> NCN getNativeCanvas is  ");
    }
    public func addEventListener(){
        ColorLog.cyan(log: "> NCN addEventListener is  ");
    }
    public func removeEventListener(){
        ColorLog.cyan(log: "> NCN removeEventListener is  ");
        
    }
    //    public func openCanvas(){
    //        ColorLog.cyan(log: "> NCN openCanvas is  ");
    //    }
    public func closeCanvas(){
        ColorLog.cyan(log: "> NCN closeCanvas is  ");
    }
    
    public func openBook(library: String, bookshelf: String, book: String, type: String , page : Int){
       let newlibrary =  library.replacingOccurrences(of: "file://", with: "")
        ColorLog.cyan(log: "> NCN openBook is library:\(library)  newlibrary:\(newlibrary) bookshelf:\(bookshelf) book:\(book) type:\(type) page:\(page)");
        FullPath = newlibrary + bookshelf + "/" + book
         print("> NCN openBook FullPath \(FullPath)");
        if let p = paper{
            p.setNeedsLayout()
        }
    }
    
    public func openPage(library:Int){
        ColorLog.cyan(log: "> NCN openPage is PAGE \(library) ");
        ColorLog.cyan(log: "> NCN Full Path -- openPage : \(String(describing: FullPath))");
     
//        ParseDrawing(fullpath: FullPath!, book :Book! ,page: library)
    }
    public func saveBook(){
        ColorLog.cyan(log: "> NCN saveBook is  ");
    }
    public func savePage(){
        ColorLog.cyan(log: "> NCN savePage is  ");
    }
    public func deleteBook(){
        ColorLog.cyan(log: "> NCN deleteBook is  ");
    }
    public func deletePage(){
        ColorLog.cyan(log: "> NCN deletePage is  ");
    }
    
    public func register(){
        ColorLog.cyan(log: "> NCN register is  ");
    }
    public func isRegistered(){
        ColorLog.cyan(log: "> NCN isRegistered is  ");
    }
    public func show(){
        ColorLog.cyan(log: "> NCN show is  ");
    }
    public func hide(){
        ColorLog.cyan(log: "> NCN hide is  ");
    }
    public func isShow(){
        ColorLog.cyan(log: "> NCN isShow is  ");
    }
    public func isOpened(){
        ColorLog.cyan(log: "> NCN isOpened is  ");
    }
    
    public func toggleDebugMode(isDebug: Bool){
        ColorLog.cyan(log: "> NCN toggleDebugMode is  isDebug:\(isDebug)");
    }
    public func setProperty(type: CGFloat,color: String,width: String,alpha: String){
        ColorLog.cyan(log: "> NCN setProperty is type:\(type) color:\(color) w:\(width) alpha:\(alpha)");
        
        
    }
    public func clearCanvas(){
        ColorLog.cyan(log: "> NCN clearCanvas is  ");
    }
    
    public func addObject(type: Int,object:JSON){
        ColorLog.lightYellow(log: "> NCN addObject Type Number  is: \(type) object:\(object) ");
        
        let objecttype:OBJECT_TYPE = OBJECT_TYPE(rawValue: type)!
        
        switch objecttype {

        case .IMAGE:
            ColorLog.lightYellow(log:"> NCN addObject OBJECT_TYPE:IMAGE == \(objecttype)");
            let mime:String = object["mime"].description
            let url:String = object["url"].description
            ColorLog.lightYellow(log:"> NCN  mime coming...> " + mime);
            ColorLog.lightYellow(log:"> NCN  url coming...> " + url);
            break
        case .TEXT:
            ColorLog.lightYellow(log:"> NCN addObject OBJECT_TYPE:TEXT == \(objecttype)");
            let text:String = object["test"].description
            ColorLog.lightYellow(log:"> NCN  text coming...> " + text);
            
            break
        case .TEMPLATE:
            ColorLog.lightYellow(log:"> NCN addObject OBJECT_TYPE:TEMPLATE == \(objecttype)");
            let template:String = object["template"].description
            ColorLog.lightYellow(log:"> NCN  template coming...> " + template);
            if(template != nil) {
                setTemplate(Id: template);
                
            }
            break
        }
        
    }
    public func undoOpen(){
        ColorLog.cyan(log: "> NCN addEventundoOpenListener is  ");
    }
    public func undoStep(){
        ColorLog.cyan(log: "> NCN undoStep is  ");
    }
    public func undoClose(){
        ColorLog.cyan(log: "> NCN undoClose is  ");
    }
    public func play(){
        ColorLog.cyan(log: "> NCN play is  ");
    }
    public func mapOpen(){
        ColorLog.cyan(log: "> NCN mapOpen is  ");
    }
    public func mapMove(){
        ColorLog.cyan(log: "> NCN mapMove is  ");
    }
    public func mapPosition(){
        ColorLog.cyan(log: "> NCN mapPosition is  ");
    }
    public func mapScale(){
        ColorLog.cyan(log: "> NCN mapScale is  ");
    }
    public func mapClose(){
        ColorLog.cyan(log: "> NCN mapClose is  ");
    }
    public func toggleViewMode(){
        ColorLog.cyan(log: "> NCN toggleViewMode is  ");
    }
    public func setDebug(isDebug: Bool){
        ColorLog.cyan(log: "> NCN setDebug is  setDebug:\(isDebug)");
    }
    public func setViewMode(isViewMode: Bool){
        paper.setViewMode(isViewMode: isViewMode)
        ColorLog.cyan(log: "> NCN setViewMode is  :\(isViewMode)");
    }
    public func hasLibrary(PenpalLibrary: Bool) -> Bool {
//    if(PenpalLibrary != nil){
//        let  selectedLibraryID: String = NativeCanvasPenpalLibrary.getLibraryPath();
        ColorLog.cyan(log: "> NCN hasLibrary +");// \(selectedLibraryID)");
//    return true
//    }
    return false;
    }
    
    public func setTemplate(Id:String){
        
        if (NativeCanvasPenpalLibrary.getCurrentLibrary()?.getTemplateManager()?.getTemplate(source: Id)) != nil{
             ColorLog.lightBlue(log: "> NCN  setTemplate : \(NativeCanvasPenpalLibrary.getCurrentLibrary()?.getTemplateManager()?.getTemplate(source: Id))")
            let template = NativeCanvasPenpalLibrary.getCurrentLibrary()?.getTemplateManager()?.getTemplate(source: Id)
//            template?.getBackground()
//            if let manager = library.getTemplateManager(){
//                ColorLog.cyan(log: "> NCN setTemplate manager \(manager)");
//                let template = manager.getTemplate(source: Id)
//                 ColorLog.cyan(log: "> NCN setTemplate template \(template)");
//            }
            //barney
             ColorLog.lightBlue(log: "> NCN  setTemplate template: \(template) ")
            if let p = paper{
                p.setNeedsLayout()
            }
            NativeCanvasPenpalLibrary.getCurrentPage()?.setTemplate(id: Id);
            
        }
    }
    
    public func addTextSticker(text:String, type:BORDER_TYPE, scale:Float, xRatio:Float, yRatio:Float) {
        if(isViewMode){
            return;
            }
//    activity.runOnUiThread(new Runnable() {
//    public View.OnLayoutChangeListener onLayoutChangeListener = new View.OnLayoutChangeListener() {
//    @Override
//    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//    if(right != 0){
//    stickerView.setVisibleCenter(stickerView.getSelectedSticker(), scale, true);
//    stickerView.removeOnLayoutChangeListener(onLayoutChangeListener);
//    }
//    }
//    };
//
////    @Override
////    public void run() {
//    NCLayerDataText textLayer = createTextLayer();
//    textLayer.setSpannableText(new SpannableStringBuilder(text));
//    TextSticker sticker = new TextSticker(stickerView, textLayer, (int)displayRect.width(), (int)displayRect.height(), fontProvider);
//    sticker
//    .setBorderType(type)
//    .setPaperLimit(bookView.getSelectedPaper().getLayer().getLimit());
//    NCRect paperDimention = bookView.getSelectedPaper().getPaperDimention();
//    NCRect dimention = new NCRect();
//    PointF origin = bookView.getSelectedPaper().getOrigin();
//    float paperScale = bookView.getSelectedPaper().getScaleX();
//    dimention.set(
//    paperDimention.left()   + origin.x,
//    paperDimention.top()    + origin.y,
//    paperDimention.right()  + origin.x,
//    paperDimention.bottom() + origin.y
//    );
//
//    stickerView
//    .addSticker(sticker)
//    .applyDefaultBorder(sticker)
//    .setScale(paperScale)
//    .setDimention(bookView, displayRect, dimention)
//    .updateUI()
//    ;
//    if (xRatio == 0 && yRatio == 0) {
//    stickerView.addOnLayoutChangeListener(onLayoutChangeListener);
//    } else {
//    stickerView.setPosition(sticker, scale, xRatio, yRatio, true);
//    }
//    if (!stickerView.isShown()) stickerView.show();
//    }
//    });
    }
    
    private func printGesture(state: UIGestureRecognizerState, name: String, touches: Int, otherTouches: Int){
        switch state{
        case .began:
            ColorLog.lightBlue(log: "> \(name) began \(touches) \(otherTouches)")
            break
        case .cancelled:
            ColorLog.lightBlue(log: "> \(name) cancelled \(touches) \(otherTouches)")
            break
        case .changed:
            ColorLog.lightBlue(log: "> \(name) changed \(touches) \(otherTouches)")
            break
        case .ended:
            ColorLog.lightBlue(log: "> \(name) ended \(touches) \(otherTouches)")
            break
        case .failed:
            ColorLog.lightBlue(log: "> \(name) failed \(touches) \(otherTouches)")
            break
        case .possible:
            ColorLog.lightBlue(log: "> \(name) possible \(touches) \(otherTouches)")
            break
        }
    }
    
    private func testSwiftyJSON(){
        ColorLog.lightBlue(log: "--------------------SwiftyJSON->>>>>>>>>>>>--------")
        var test1:JSON = JSON()
        test1[1] = "a"
        ColorLog.lightBlue(log: "> SwiftyJSON \(test1)")
        test1[9] = "b"
        ColorLog.lightBlue(log: "> SwiftyJSON \(test1)")
        test1[3] = JSON(parseJSON: "{\"a\":\"b\"}") //????????????
        test1[4] = JSON([1,2,3,4]) //????????????
        test1[5] = JSON(["a":1,"b":2,"c":3,"d":4]) //????????????
        ColorLog.lightBlue(log: "> SwiftyJSON \(test1)")
        
        
        
        let d = test1.rawString() ?? ""
        ColorLog.red(log: "> SwiftyJSON rawString \(d)")
        let test3:JSON = JSON(parseJSON: d)
        ColorLog.red(log: "> SwiftyJSON parseJSON \(test3)")
        let test4:JSON = JSON(string: d)
        ColorLog.red(log: "> SwiftyJSON string \(test4)")
        let test5:JSON = JSON.parse(d)
        ColorLog.red(log: "> SwiftyJSON parse \(test5)")
        
        
        var test2:JSON = JSON()
        test2["sss"] = "a"
        ColorLog.yellow(log: "> SwiftyJSON \(test2)")
        test2["xxx"][1] = "b"
        ColorLog.yellow(log: "> SwiftyJSON \(test2)")
        test2["xxx"][3] = JSON(parseJSON: "{\"a\":\"b\"}")
        ColorLog.yellow(log: "> SwiftyJSON \(test2)")
        test2["xxxzz"] = JSON(parseJSON: "{\"aa\":\"ba\"}")
        ColorLog.yellow(log: "> SwiftyJSON \(test2)")
        
        
        var test6:JSON = JSON()
        test6[11] = "test6"
        var test7:JSON = JSON()
        test7["sss"] = "test7"
        test7["xxx"][2] = "test7"
        do{
            ColorLog.green(log: "> SwiftyJSON arrar merge 1 \(test6)")
            ColorLog.green(log: "> SwiftyJSON arrar merge 2 \(test1)")
            try test6.merge(with: test1)
            ColorLog.green(log: "> SwiftyJSON arrar merge \(test6)")
            
            ColorLog.green(log: "> SwiftyJSON json merge 1 \(test7)")
            ColorLog.green(log: "> SwiftyJSON json merge 2 \(test2)")
            try test7.merge(with: test2)
            ColorLog.green(log: "> SwiftyJSON json merge \(test7)")
        }catch{
            ColorLog.red(log: "> SwiftyJSON catch merge error")
        }
        
        if let _ = test1.array{
            test1.removeByIndex(index: 1)
        }
        if let _ = test7.dictionary{
            test7.removeByKey(key: "xxx")
        }
        ColorLog.green(log: "> SwiftyJSON test index \(test1.arrayValue)")
        ColorLog.green(log: "> SwiftyJSON test index \(test1.array)")
        ColorLog.green(log: "> SwiftyJSON test index \(test1.arrayObject)")
        ColorLog.green(log: "> SwiftyJSON test index \(test1.removeByIndex(index: 1))")
        ColorLog.green(log: "> SwiftyJSON test index \(test1)")
        ColorLog.green(log: "> SwiftyJSON test index \(test7.dictionary)")
        ColorLog.green(log: "> SwiftyJSON test index \(test7.dictionaryObject)")
        ColorLog.green(log: "> SwiftyJSON test index \(test7.dictionaryValue)")
        ColorLog.green(log: "> SwiftyJSON test index \(test7.removeByKey(key: "xxx"))")
        ColorLog.green(log: "> SwiftyJSON test index \(test7)")
        ColorLog.lightBlue(log: "--------------------SwiftyJSON----<<<<<<<<<<---------")
    }
    
    private func testSVG(x:CGFloat, y:CGFloat, width: CGFloat, height: CGFloat){
        // 測試 SVG
        if let url = Bundle.main.url(forResource: "sun", withExtension: "svg") {

            ColorLog.cyan(log: "> svg test 1 \(url)")
            let svg:SVGKImage = SVGKImage(contentsOf: url)

            ColorLog.cyan(log: "> svg test 2 \(svg.size)")

            if let svgView = SVGKFastImageView(svgkImage: svg){
                ColorLog.cyan(log: "> svg test 3 \(svg)")
                svgView.frame = CGRect(x: x + 15, y: y + 15, width: width - 30, height: height - 30);
                svgView.bounds = CGRect(x: x + 15, y: y + 15, width: width - 30, height: height - 30);
                svgView.backgroundColor = UIColor.white
                self.paper.addSubview(svgView)
            }
        }else{
            ColorLog.cyan(log: "> svg test can not find file")
        }
    }
    
    private func testAnimation(){
        if let view = self.paper{
            view.backgroundColor = UIColor.brown
            
            let originalTransform = view.transform
            let scaledTransform = originalTransform.scaledBy(x: 0.2, y: 0.2)
            let scaledAndTranslatedTransform = scaledTransform.translatedBy(x: 0.0, y: -250.0)
            UIView.animate(withDuration: 0.5, delay: 0.0, options: UIViewAnimationOptions.curveEaseIn, animations: {
                //Frame Option 1:
                view.frame = CGRect(x: view.frame.origin.x, y: 20, width: view.frame.width, height: view.frame.height)
                
                //Frame Option 2:
                //view.center = CGPoint(x: self.view.frame.width / 2, y: self.view.frame.height / 4)
                view.backgroundColor = .blue
                
            },completion: { finish in
                
                UIView.animate(withDuration: 1, delay: 0.25,options: UIViewAnimationOptions.curveEaseOut,animations: {
                    view.backgroundColor = .orange
                    view.transform = CGAffineTransform(scaleX: 0.25, y: 0.25)
                    
                    //self.animationButton.isEnabled = false // If you want to restrict the button not to repeat animation..You can enable by setting into true
                    
                },completion: nil)})
        }
    }
}

extension NativeCanvasManager: UIScrollViewDelegate {
    
    func viewForZooming(in scrollView: UIScrollView) -> UIView? {
        ColorLog.green(log: "> viewForZooming")
        return self.paperContainer
    }
    
    func scrollViewDidEndZooming(_ scrollView: UIScrollView, with view: UIView?, atScale scale: CGFloat) {
        ColorLog.green(log: "> scrollViewDidEndZooming")
        var desiredScale = self.traitCollection.displayScale
        let existingScale = paper.contentScaleFactor
        
        if scale >= 2.0 {
            desiredScale *= 2.0
        }
        
        if abs(desiredScale - existingScale) > 0.00001 {
            paper.contentScaleFactor = desiredScale
            paper.setNeedsDisplay()
        }
    }
}

