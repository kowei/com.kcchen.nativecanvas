//
//  NativeCanvasPaperContainer.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//
//  Abstract:
//  The content of the scroll view. Adds some margin and a shadow.
//  Setting the documentView places this view, and sizes it to the canvasSize.
//


import UIKit

public class NativeCanvasPaperContainer : UIView {
    
    private var canvasSize: CGSize!
    private var canvasView: UIView!
    public var canvasOrigin: CGPoint!
    public var documentView: UIView? {
        willSet {
            if let previousView = documentView {
                previousView.removeFromSuperview()
            }
        }
        didSet {
            if let newView = documentView {
                if let view = canvasView{
                    newView.frame = view.bounds
                    view.addSubview(newView)
                }
            }
        }
    }
    
    required public init() {
        super.init(frame:CGRect.zero)
    }
    
    required public init(canvasSize: CGSize) {
        super.init(frame:CGRect.zero)
        initialize(canvasSize: canvasSize)
    }
    
    required public init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    public func initialize(canvasSize: CGSize){
        if let _ = self.canvasSize{
            
        }else{
            let screenBounds = UIScreen.main.bounds
            let minDimension = max(screenBounds.width, screenBounds.height)
            self.canvasSize = canvasSize
            let baseInset = CGFloat(44.0)
            var size = canvasSize + (baseInset * 2)
            size.width  = max(minDimension, size.width)
            size.height = max(minDimension, size.height)
            
            ColorLog.green(log: "> canvasSize:\(canvasSize)")
            let frame = CGRect(origin: .zero, size: size)
            self.frame = frame
            self.bounds = frame
            
            canvasOrigin = CGPoint(x: (frame.width - canvasSize.width) / 2.0, y: (frame.height - canvasSize.height) / 2.0)
            let canvasFrame = CGRect(origin: canvasOrigin, size: canvasSize)
            ColorLog.cyan(log: "> initialize canvasOrigin \(canvasOrigin)");
            canvasView = UIView(frame:canvasFrame)
            canvasView.backgroundColor = UIColor.white
            canvasView.layer.shadowOffset = CGSize(width: 0.0, height: 3.0)
            canvasView.layer.shadowRadius = 4.0
            canvasView.layer.shadowColor = UIColor.darkGray.cgColor
            canvasView.layer.shadowOpacity = 1.0
            if let newView = documentView {
                if let view = canvasView{
                    newView.frame = view.bounds
                    view.addSubview(newView)
                }
            }
            self.backgroundColor = UIColor.lightGray
            self.addSubview(canvasView)
        }
    }
}

