//
//  SwifterCocoaCGVectorExtensions.swift
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
public extension CGVector {
    init(_ point: CGPoint) {
        self.init()
        dx = point.x
        dy = point.y
    }
    
    func apply(transform:CGAffineTransform) -> CGVector {
        return CGVector(CGPoint(self).applying(transform))
    }
    
    func round(toScale scale: CGFloat) -> CGVector {
        return CGVector(dx: CoreGraphics.round(dx * scale) / scale,
                        dy: CoreGraphics.round(dy * scale) / scale)
    }
    
    var quadrance: CGFloat {
        return dx * dx + dy * dy;
    }
    
    var normal: CGVector? {
        if !(dx.isZero && dy.isZero) {
            return CGVector(dx: -dy, dy: dx)
        } else {
            return nil
        }
    }
    
    /// CGVector pointing in the same direction as self, with a length of 1.0 - or nil if the length is zero.
    var normalize: CGVector? {
        let quadrance = self.quadrance
        if quadrance > 0.0 {
            return self / sqrt(quadrance)
        } else {
            return nil
        }
    }
}
