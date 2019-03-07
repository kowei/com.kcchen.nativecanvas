//
//  PokeModal.swift
//  PokeModal
//
//  Created by Leonardo Borges Avelino on 8/3/16.
//  Copyright © 2016 Leonardo Borges Avelino. All rights reserved.
//

import Foundation
import UIKit

@objc(NativeCanvasColorPicker) class NativeCanvasColorPicker : UAModalPanel, HSBColorWheelDelegate{

    private var colorWheel:HSBColorWheel?
    private var title:UILabel?
    private var cancelButton:UIButton?
    private var okButton:UIButton?
    private var _selectedColor:UIColor?
    
    public var selectedColor:UIColor?{
        get{
            return _selectedColor
        }
        set{
            _selectedColor = newValue
        }
    }

    override var bounds: CGRect {
        didSet {
            ColorLog.cyan(log: "> bounds changed")
        }
    }

    override init(frame: CGRect) {
        super.init(frame: frame)
        self.initialize()
    }
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        self.initialize()
    }
    
    private func initialize(){
        cancelButton = UIButton(type: UIButtonType.roundedRect)
        okButton = UIButton(type: UIButtonType.roundedRect)
        
        title = UILabel(frame: CGRect.zero)
        title?.text = "Choose color"
        title?.textColor = UIColor.white
        
        colorWheel = HSBColorWheel(frame: CGRect.zero)
        colorWheel?.colorSize = 33
        colorWheel?.wheelDivisions = 10
        colorWheel?.colorSeparation = 2
        colorWheel?.showWheelEdge = true
        colorWheel?.showDivisions = true
        colorWheel?.showColorEdge = true
        colorWheel?.delegate = self
        
        cancelButton?.setTitle("Cancel", for: UIControlState.normal)
        cancelButton?.setTitleColor(UIColor.black, for: UIControlState.normal)
        cancelButton?.setTitleColor(UIColor.gray, for: UIControlState.disabled)
        cancelButton?.on(UIControlEvents.touchUpInside, onCancel)
        cancelButton?.isHighlighted = false
        cancelButton?.showsTouchWhenHighlighted = true
        cancelButton?.backgroundColor = UIColor.white

        okButton?.setTitle("OK", for: UIControlState.normal)
        okButton?.setTitleColor(UIColor.black, for: UIControlState.normal)
        okButton?.setTitleColor(UIColor.gray, for: UIControlState.disabled)
        okButton?.on(UIControlEvents.touchUpInside, onOk)
        okButton?.isHighlighted = false
        okButton?.showsTouchWhenHighlighted = true
        okButton?.backgroundColor = UIColor.white
        enableButton(button: okButton!, isEnable: false)
        
        self.contentView.addSubview(colorWheel!)
        self.contentView.addSubview(title!)
        self.contentView.addSubview(cancelButton!)
        self.contentView.addSubview(okButton!)
        
        self.closeButton.removeFromSuperview()

        setNeedsLayout()
    }
    
    override func setNeedsLayout() {
        super.setNeedsLayout()
        ColorLog.lightGreen(log: "> setNeedsLayout")
        
        let margin = UIScreen.main.bounds.size.width / 12
        let padding = margin / 2
        
        let contentWidth = UIScreen.main.bounds.size.width - (2 * (margin + padding))
        let contentHeight = UIScreen.main.bounds.size.height - (2 * (margin + padding)) - UIApplication.shared.statusBarFrame.height
        
        let buttonPadding = contentWidth / 8
        let buttonWidth = (contentWidth - (buttonPadding * 3)) / 2
        let buttonheight = contentHeight / 9
        let buttonY = contentHeight * 4 / 5

        self.cornerRadius = UIScreen.main.bounds.size.width / 20
        self.borderWidth = UIScreen.main.bounds.size.width / 70
        
        // 以下調整剛好的 margin，在 modal view 外，剛好間隔 margin 距離
        self.margin = UIEdgeInsetsMake(margin, margin, margin , margin);
        self.padding = UIEdgeInsetsMake(padding, padding, padding, padding);
        
        // 以下調整到剛好的顯示區，在statusbar下
        self.frame.origin.y = UIApplication.shared.statusBarFrame.height
        self.frame.size.height = UIScreen.main.bounds.size.height - UIApplication.shared.statusBarFrame.height
        self.bounds = self.frame
        
        // contentContainer 是彈出視窗的容器，彈出視窗會侷限在容器 margin 內
        self.contentContainer.frame = self.frame
        self.contentContainer.bounds = self.frame
        // self.contentView.backgroundColor = UIColor.red

        // Title
        let newTitleSize:CGFloat = (self.title?.font.pointSize)! * NativeCanvasUtil.SCREEN_RATIO
        title?.font = UIFont.boldSystemFont(ofSize: newTitleSize)
        let titleFrame = CGRect(x: padding,
                                y: padding,
                                width: contentWidth,
                                height: buttonheight
        );
        title?.frame = titleFrame
        title?.bounds = titleFrame

        // ColorWheel
        let wheelFrame = CGRect(x: 0,
                                y: contentHeight / 6,
                                width: contentWidth,
                                height: contentWidth
        );
        colorWheel?.frame = wheelFrame
        colorWheel?.bounds = wheelFrame

        let newCancelButtonSize:CGFloat = (self.cancelButton?.titleLabel?.font.pointSize)! * NativeCanvasUtil.SCREEN_RATIO
        cancelButton?.titleLabel?.font = UIFont.systemFont(ofSize: newCancelButtonSize)
        let cancelButtonFrame = CGRect(x: buttonPadding,
                                       y: buttonY,
                                       width: buttonWidth,
                                       height: buttonheight
        );
        cancelButton?.frame = cancelButtonFrame
        cancelButton?.bounds = cancelButtonFrame

        let newOkButtonSize:CGFloat = (self.okButton?.titleLabel?.font.pointSize)! * NativeCanvasUtil.SCREEN_RATIO
        okButton?.titleLabel?.font = UIFont.systemFont(ofSize: newOkButtonSize)
        let okButtonFrame = CGRect(x: buttonPadding + buttonWidth + buttonPadding,
                                   y: buttonY,
                                   width: buttonWidth,
                                   height: buttonheight
        );
        okButton?.frame = okButtonFrame
        okButton?.bounds = okButtonFrame
    }
    
    override func hide() {
        self.isHidden = true
        super.hide()
    }
    
    override func show(from point: CGPoint) {
        self.isHidden = false
        super.show(from: point)
    }
    
    override func show() {
        self.isHidden = false
        super.show()
    }
    
    private func enableButton(button: UIButton, isEnable: Bool){
        if(isEnable){
            button.isEnabled = true
        }else{
            button.isEnabled = false
        }
    }
    
    func onCancel(button: UIButton){
        ColorLog.red(log: "> onCancel")
        self.hide()
    }
    
    func onOk(button: UIButton){
        ColorLog.red(log: "> onOk")
        self.delegate.didClose!(self)
        self.hide()
    }
    
    // HSBColorWheelDelegate
    func colorWheel(_ colorWheel: HSBColorWheel, didSelectColor color: UIColor) {
        ColorLog.purple(log: "> select color: \(color)")
        selectedColor = color
        enableButton(button: okButton!, isEnable: true)
    }
    
    public func getPicker()->HSBColorWheel?{
        return colorWheel
    }
}

