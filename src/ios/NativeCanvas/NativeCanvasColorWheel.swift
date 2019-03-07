//
//  ColorWheel.swift
//  Color picker
//
//  Created by Patricio Aguirre on 4/2/17.
//

import UIKit

public protocol HSBColorWheelDelegate : class {
  func colorWheel(_ colorWheel:HSBColorWheel, didSelectColor color:UIColor)
}

@IBDesignable
public class HSBColorWheel : UIView, ColorViewDelegate {
  
  // MARK: - Properties
  
  private var wheelCenter:CGPoint!
  private var wheelRadius:CGFloat!
  
  public weak var delegate:HSBColorWheelDelegate?
  
  // MARK: - UI properties
  
  internal var selectedColorLayer:CAShapeLayer?
  
  // MARK: - Inspectable properties
  
  @IBInspectable
  public var colorSize:CGFloat = 20 {
    didSet {
      if colorSize <= 0 {
        colorSize = 20
      }
      setNeedsDisplay()
    }
  }
  
  @IBInspectable
  public var wheelDivisions:CGFloat = 4 {
    didSet {
      if wheelDivisions < 1 {
        wheelDivisions = 4
      }
      setNeedsDisplay()
    }
  }
  
  @IBInspectable
  public var colorSeparation:CGFloat = 1 {
    didSet {
      if colorSeparation < 0 {
        colorSeparation = 1
      }
      setNeedsDisplay()
    }
  }
  
  @IBInspectable public var showWheelEdge:Bool = false { didSet { setNeedsDisplay() } }
  @IBInspectable public var wheelEdgeColor:UIColor = UIColor.black { didSet { setNeedsDisplay() } }
  @IBInspectable public var wheelEdgeWidth:CGFloat = 1.0 { didSet { setNeedsDisplay() } }
  
  @IBInspectable public var showDivisions:Bool = false { didSet { setNeedsDisplay() } }
  @IBInspectable public var divisionsColor:UIColor = UIColor.black { didSet { setNeedsDisplay() } }
  @IBInspectable public var divisionsWidth:CGFloat = 1.0 { didSet { setNeedsDisplay() } }
  
  @IBInspectable public var showColorEdge:Bool = true { didSet { setNeedsDisplay() } }
  @IBInspectable public var colorEdgeWidth:CGFloat = 0.5 { didSet { setNeedsDisplay()} }
  @IBInspectable
  public var colorEdgeColor:UIColor = UIColor(colorLiteralRed: 0, green: 0, blue: 0, alpha: 0.5) {
    didSet {
      setNeedsDisplay()
    }
  }
  
  @IBInspectable public var showSelectedColor:Bool = true
  
  // MARK: Methods
  
  public override init(frame: CGRect) {
    super.init(frame: frame)
    initialize()
  }
  
  public required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
    initialize()
  }
  
  private func initialize() {
    clipsToBounds = true
    backgroundColor = UIColor.clear
  }
  
  // MARK: - Draw
  
  public override func draw(_ rect: CGRect) {
    super.draw(rect)

    let frameRadius = min(bounds.size.width, bounds.size.height) / 2
    let colorRadius = colorSize / 2
    
    wheelRadius = frameRadius - colorRadius - colorEdgeWidth
    wheelCenter = CGPoint(x: bounds.midX, y: bounds.midY)
    
    if showWheelEdge { drawWheel() }
    drawColors()
  }
  
  private func drawWheel() {
    let wheelLayer = CAShapeLayer()
    wheelLayer.frame = bounds
    wheelLayer.path = UIBezierPath(arcCenter: wheelCenter,
                                   radius: wheelRadius,
                                   startAngle: 0,
                                   endAngle: CGFloat(2 * Double.pi),
                                   clockwise: true).cgPath
    wheelLayer.lineWidth = wheelEdgeWidth
    wheelLayer.strokeColor = wheelEdgeColor.cgColor
    wheelLayer.fillColor = UIColor.clear.cgColor
    layer.addSublayer(wheelLayer)
  }
  
  private func drawColors() {
    let pi_rad = CGFloat(Double.pi)
    let offsetAngle = pi_rad / wheelDivisions
    let end = Int(wheelDivisions * 2)
    
    for i in 0 ... end {
      let currentAngle = CGFloat(i) * offsetAngle
      let endX = wheelCenter.x + wheelRadius * cos(currentAngle)
      let endY = wheelCenter.y + wheelRadius * sin(currentAngle)
      
      var point = CGPoint(x: endX, y: endY)
      while isAValidPointForColor(point) {
        if showDivisions { drawLine(wheelCenter, endPoint: point) }
        drawColorAtPoint(point)
        
        let ds = getSeparationForPoint(point)
        let dx = point.x - ds * cos(currentAngle)
        let dy = point.y - ds * sin(currentAngle)
        point = CGPoint(x: dx, y: dy)
      }
    }
    
    drawCenterColor()
  }
  
  private func drawLine(_ startPoint:CGPoint, endPoint:CGPoint) {
    let line = UIBezierPath()
    line.move(to: startPoint)
    line.addLine(to: endPoint)
    line.close()
    line.lineWidth = divisionsWidth
    divisionsColor.setStroke()
    line.stroke()
  }
  
  private func drawCenterColor() {
    let frame = CGRect(x: wheelCenter.x, y: wheelCenter.y, width: colorSize, height: colorSize)
    let bgColor = UIColor(colorLiteralRed: 1, green: 1, blue: 1, alpha: 1)
    let color = ColorView(frame: frame,
                          center: wheelCenter,
                          bgColor: bgColor,
                          showEdge: showColorEdge,
                          edgeWidth: colorEdgeWidth,
                          edgeColor: colorEdgeColor,
                          delegate: self)
    
    addSubview(color)
  }
  
  private func drawColorAtPoint(_ point:CGPoint) {
    let size = getSizeForColorAtPoint(point)
    let frame = CGRect(x: 0, y: 0, width: size, height: size)
    let bgColor = getHSBColorForPoint(point)
    let color = ColorView(frame: frame,
                          center: point,
                          bgColor: bgColor,
                          showEdge: showColorEdge,
                          edgeWidth: colorEdgeWidth,
                          edgeColor: colorEdgeColor,
                          delegate: self)
    
    addSubview(color)
  }
  
  internal func showSelectedColorShapeAtPoint(_ point:CGPoint) {
    hidSelectedColorShape()
    
    let radius = point == wheelCenter ? colorSize / 2 : getSizeForColorAtPoint(point) / 2
    selectedColorLayer = CAShapeLayer()
    selectedColorLayer!.fillColor = UIColor.clear.cgColor
    selectedColorLayer!.strokeColor = UIColor.cyan.cgColor
    selectedColorLayer!.lineWidth = 2.0
    selectedColorLayer!.path = UIBezierPath(arcCenter: point,
                                            radius: radius,
                                            startAngle: 0,
                                            endAngle: CGFloat(2 * Double.pi),
                                            clockwise: true).cgPath
    
    layer.addSublayer(selectedColorLayer!)
  }
  
  internal func hidSelectedColorShape() {
    selectedColorLayer?.removeFromSuperlayer()
    selectedColorLayer = nil
  }
  
  // MARK: - Helper Methods
  
  private func isAValidPointForColor(_ point:CGPoint) -> Bool {
    let distance = getDistanceFromWheelCenterToPoint(point)
    return distance >= colorSize
  }
  
  private func getDistanceFromWheelCenterToPoint(_ point:CGPoint) -> CGFloat {
    let dx = point.x - wheelCenter.x
    let dy = point.y - wheelCenter.y
    return sqrt(pow(dx,2) + pow(dy,2))
  }
  
  private func getAngleBetweenWheelCenterAndPoint(_ point:CGPoint) -> CGFloat {
    let dx = point.x - wheelCenter.x
    let dy = point.y - wheelCenter.y
    return atan2(dy, dx)
  }
  
  private func getDegAngleBetweenWheelCenterAndPoint(_ point:CGPoint) -> CGFloat {
    let pi_rad = CGFloat(Double.pi)
    let dx = point.x - wheelCenter.x
    let dy = point.y - wheelCenter.y
    let angle = atan2(dy, dx) * 180 / pi_rad
    return angle >= 0 ? angle : angle + 360
  }
  
  private func getHSBColorForPoint(_ point:CGPoint) -> UIColor {
    let distance = getDistanceFromWheelCenterToPoint(point) / wheelRadius
    let angle = getDegAngleBetweenWheelCenterAndPoint(point)
    let hue:CGFloat = angle / 360 // transform from [0, 360] to [0, 1]
    return UIColor(hue: hue, saturation: distance, brightness: 1.0, alpha: 1.0)
  }

  private func getSizeForColorAtPoint(_ point:CGPoint) -> CGFloat {
    let distance = getDistanceFromWheelCenterToPoint(point)
    let scale = (distance / wheelRadius)
    return scale * colorSize
  }
  
  private func getSeparationForPoint(_ point:CGPoint) -> CGFloat {
    let colorSize = getSizeForColorAtPoint(point)
    let distance = getDistanceFromWheelCenterToPoint(point)
    let factor = colorSeparation * wheelRadius / distance
    return colorSize  + factor / 2
  }
  
  // MARK: - Color Button Delegate
  
  func colorView(_ colorView: ColorView, didSetlectColor color: UIColor) {
    if showSelectedColor {
      showSelectedColorShapeAtPoint(colorView.center)
    }
    
    delegate?.colorWheel(self, didSelectColor: color)
  }
  
}
