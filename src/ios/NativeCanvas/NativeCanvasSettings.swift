//
//  NativeCanvasSettings.swift
//  TouchDraw
//
//  Created by Christian Paul Dehli on 9/4/16.
//

/// Properties to describe a stroke (color, width)
open class NativeCanvasSettings: NSObject {

    /// Color of the brush
    private static let defaultColor = CIColor(color: UIColor.black)
    internal var color: CIColor?

    /// Width of the brush
    private static let defaultWidth = CGFloat(10.0)
    internal var width: CGFloat

    /// Default initializer
    override public init() {
        color = NativeCanvasSettings.defaultColor
        width = NativeCanvasSettings.defaultWidth
        super.init()
    }

    /// Initializes a NativeCanvasSettings with another NativeCanvasSettings object
    public convenience init(_ settings: NativeCanvasSettings) {
        self.init()
        self.color = settings.color
        self.width = settings.width
    }

    /// Initializes a NativeCanvasSettings with a color and width
    public convenience init(color: CIColor?, width: CGFloat) {
        self.init()
        self.color = color
        self.width = width
    }

    /// Used to decode a NativeCanvasSettings with a decoder
    required public convenience init?(coder aDecoder: NSCoder) {
        let color = aDecoder.decodeObject(forKey: NativeCanvasSettings.colorKey) as? CIColor
        var width = aDecoder.decodeObject(forKey: NativeCanvasSettings.widthKey) as? CGFloat
        if width == nil {
            width = NativeCanvasSettings.defaultWidth
        }

        self.init(color: color, width: width!)
    }
}

// MARK: - NSCoding

extension NativeCanvasSettings: NSCoding {
    internal static let colorKey = "color"
    internal static let widthKey = "width"

    /// Used to encode a NativeCanvasSettings with a coder
    open func encode(with aCoder: NSCoder) {
        aCoder.encode(self.color, forKey: NativeCanvasSettings.colorKey)
        aCoder.encode(self.width, forKey: NativeCanvasSettings.widthKey)
    }
}
