//
//  ColorView.swift
//  Color picker
//
//  Created by Patricio Aguirre on 6/2/17.
//
//

import UIKit

protocol ColorViewDelegate : class {
  func colorView(_ colorView:ColorView, didSetlectColor color:UIColor)
}

class ColorView: UIView {
  
  // MARK: - Properties
  
  weak var delegate:ColorViewDelegate?
  
  // MARK: - UI properties
  
  var bgColor = UIColor.blue { didSet { setNeedsDisplay() } }
  
  var showEdge = true { didSet { setNeedsDisplay() } }
  var edgeWidth:CGFloat = 0.5 { didSet { setNeedsDisplay() } }
  var edgeColor = UIColor(colorLiteralRed: 0, green: 0, blue: 0, alpha: 0.5) {
    didSet {
      setNeedsDisplay()
    }
  }
  
  // MARK: - Init
  
  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    configure()
  }
  
  override init(frame: CGRect) {
    super.init(frame: frame)
    configure()
  }
  
  init(frame: CGRect,
       center:CGPoint,
       bgColor:UIColor?,
       showEdge:Bool?,
       edgeWidth:CGFloat?,
       edgeColor:UIColor?,
       delegate:ColorViewDelegate?) {
    super.init(frame: frame)
    
    self.center = center
    
    if let bgColor = bgColor {
      self.bgColor = bgColor
    }
    
    if let showEdge = showEdge {
      self.showEdge = showEdge
    }
    
    if let edgeWidth = edgeWidth {
      self.edgeWidth = edgeWidth
    }
    
    if let edgeColor = edgeColor {
      self.edgeColor = edgeColor
    }
    
    self.delegate = delegate
    
    configure()
  }
  
  private func configure() {
    clipsToBounds = false
    backgroundColor = UIColor.clear
    
    let tap = UITapGestureRecognizer(target: self, action:#selector(handleTapGesture))
    addGestureRecognizer(tap)
  }
  
  // MARK: - Drawing
  
  override func draw(_ rect: CGRect) {
    let shapeCenter = CGPoint(x: bounds.midX, y: bounds.midY)
    let shapeRadius = min(bounds.size.width, bounds.size.height) / 2
    let shape = CAShapeLayer()
    shape.frame = bounds
    shape.path = UIBezierPath(arcCenter: shapeCenter,
                              radius: shapeRadius,
                              startAngle: 0,
                              endAngle: CGFloat(2 * Double.pi),
                              clockwise: true).cgPath
    
    shape.strokeColor = showEdge ? edgeColor.cgColor : UIColor.clear.cgColor
    shape.lineWidth = edgeWidth
    shape.fillColor = bgColor.cgColor
    
    layer.addSublayer(shape)
  }
  
  // MARK: - Helper Methods
  
  private func isAValidTouch(_ touchPoint:CGPoint) -> Bool {
    let dx = touchPoint.x - bounds.midX
    let dy = touchPoint.y - bounds.midY
    let distance = sqrt(pow(dx,2) + pow(dy,2))
    return distance <= min(bounds.size.width, bounds.size.height) / 2
  }
  
  // MARK: - Selectors
  
  func handleTapGesture(_ gesture:UITapGestureRecognizer) {
    let touchPoint = gesture.location(in: self)
    if isAValidTouch(touchPoint) {
      delegate?.colorView(self, didSetlectColor: bgColor)
    }
  }
  
}
