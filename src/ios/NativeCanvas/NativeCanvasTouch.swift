//
//  NativeCanvasTouch.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/13.
//

import Foundation

@objc(NativeCanvasTouch) class NativeCanvasTouch : UIView{
    private var webView: UIWebView? = nil
    private var manager: NativeCanvasManager? = nil
    private var isTouch:Bool = false

    init(webview: UIWebView, ncmanager: NativeCanvasManager){
        ColorLog.cyan(log: "> NCNT init frame:\(UIScreen.main.bounds)");
        super.init(frame: UIScreen.main.bounds);
        webView = webview
        manager = ncmanager
        initialize(UIScreen.main.bounds)
    }
    
    required init?(coder aDecoder: NSCoder) {
        ColorLog.cyan(log: "> NCNT init aDecoder:\(aDecoder)");
        super.init(coder: aDecoder);
        initialize(UIScreen.main.bounds)
    }

    /// Adds the subviews and initializes stack
    private func initialize(_ frame: CGRect) {
        self.frame = frame
        self.bounds = frame
        self.alpha = 0;
    }
    
    public func destroy(){
        ColorLog.cyan(log: "> NCNT destroy");
        NotificationCenter.default.removeObserver(self);
    }
    
    public func setTouch(isTouch:Bool){
        self.isTouch = isTouch
        ColorLog.cyan(log: "> NCNT setTouch \(isTouch) ");
    }
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        if(manager?.isModal())!{
            ColorLog.cyan(log: "> hitTest drop to modal \(point))")
            return nil
        }else{
            if (manager?.getScrollView().frame.contains(point))!{
                if(isTouch){
                    ColorLog.cyan(log: "> hitTest manager \(point) \(manager?.getScrollView().frame)")
                    return manager?.hitTest(point: point, event:event)
                }else{
                    ColorLog.cyan(log: "> hitTest drop to webView \(point))")
                    return nil
                }
            }else{
                ColorLog.cyan(log: "> hitTest webView \(point) \(manager?.getScrollView().frame))")
                return nil
            }
        }
    }
}
