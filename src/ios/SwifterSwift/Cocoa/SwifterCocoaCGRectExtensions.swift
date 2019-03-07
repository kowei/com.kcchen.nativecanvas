//
//  SwifterCocoaCGRectExtensions.swift
//  PenPal
//
//  Created by KC Chen on 2018/5/11.
//

#if os(macOS)
import Cocoa
#else
import UIKit
#endif

// MARK: - Methods
public extension CGRect {
    
    /// SwifterSwift: CGRect round String
    ///
    ///     let point = CGPoint(x: 10.123456, y: 10.654321)
    ///     let pointString = point.round(at: 2)
    ///     //pointString = (10.12, 10.65)
    ///
    /// - Parameter at: CGRect to get round at.
    /// - Returns: CGRect rounded String.
    public func round(at position: Int) -> String {
        let x = String(format:"%.\(position)f", self.origin.x)
        let y = String(format:"%.\(position)f", self.origin.y)
        let w = String(format:"%.\(position)f", self.width)
        let h = String(format:"%.\(position)f", self.height)
        return "(\(x), \(y), \(w), \(h))"
    }
    
    public var mid:CGPoint{
        get{
            return CGPoint(x: self.midX, y: self.midY)
        }
    }
    
    var center: CGPoint {
        get {
            return origin + CGVector(dx: width, dy: height) / 2.0
        }
        set {
            origin = center - CGVector(dx: width, dy: height) / 2
        }
    }
}

func +(left: CGSize, right: CGFloat) -> CGSize {
    return CGSize(width: left.width + right, height: left.height + right)
}

func -(left: CGSize, right: CGFloat) -> CGSize {
    return left + (-1.0 * right)
}


// MARK: CGPoint and CGVector math
func -(left: CGPoint, right:CGPoint) -> CGVector {
    return CGVector(dx: left.x - right.x, dy: left.y - right.y)
}

func /(left: CGVector, right:CGFloat) -> CGVector {
    return CGVector(dx: left.dx / right, dy: left.dy / right)
}

func *(left: CGVector, right:CGFloat) -> CGVector {
    return CGVector(dx: left.dx * right, dy: left.dy * right)
}

func +(left: CGPoint, right: CGVector) -> CGPoint {
    return CGPoint(x: left.x + right.dx, y: left.y + right.dy)
}

func +(left: CGVector, right: CGVector) -> CGVector {
    return CGVector(dx: left.dx + right.dx, dy: left.dy + right.dy)
}

func +(left: CGVector?, right: CGVector?) -> CGVector? {
    if let left = left, let right = right {
        return CGVector(dx: left.dx + right.dx, dy: left.dy + right.dy)
    } else {
        return nil
    }
}

func -(left: CGPoint, right: CGVector) -> CGPoint {
    return CGPoint(x: left.x - right.dx, y: left.y - right.dy)
}
