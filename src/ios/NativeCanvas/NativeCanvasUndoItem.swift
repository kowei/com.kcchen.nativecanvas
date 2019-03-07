//
//  NativeCanvasUndoItem.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation

public class NativeCanvasUndoItem: NSData{
    
    public static let UNDO_ITEM_TYPE: String       = "UndoItemType"
    public static let UNDO_OBJECT_NAME: String     = "UndoObjectName"
    public static let UNDO_DATA: String            = "UndoData"
    public static let UNDO_INDEX: String           = "UndoIndex"
    public static let UNDO_STAMP: String           = "UndoStamp"
    public static let UNDO_IS_TRANSFORM: String    = "UndoIsTransform"
    
    public override var description: String{
        get{
            return exportData().rawString() ?? ""
        }
    }
    private var _objectName:String?
    internal var objectName:String?{
        get{
            return _objectName
        }
        set{
            _objectName = newValue
        }
    }
    private var _data:JSON?
    internal var data:JSON?{
        get{
            return _data
        }
        set{
            _data = newValue
        }
    }
    private var _type:UNDO_TYPE?
    internal var type:UNDO_TYPE?{
        get{
            return _type
        }
        set{
            _type = newValue
        }
    }
    private var _timestamp:Double?
    internal var stamp:Double?{
        get{
            return _timestamp
        }
        set{
            _timestamp = newValue
        }
    }
    private var _isTransform:Bool = false
    internal var isTransform:Bool{
        get{
            return _isTransform
        }
        set{
            _isTransform = newValue
        }
    }
    private var _index:Int?
    internal var index:Int?{
        get{
            return _index
        }
        set{
            _index = newValue
        }
    }
    
    public init(type:UNDO_TYPE, objectName:String?, data:JSON?) {
        super.init()
        ColorLog.cyan(log: "> NativeCanvasUndoItem init")
        self.type = type
        self.objectName = objectName
        self.data = data
        self.stamp = NSDate().timeIntervalSince1970
    }
    
    public init(item:JSON){
        super.init()
        ColorLog.cyan(log: "> NativeCanvasUndoItem init")
        _ = importData(data: item);
    }
    
    public override init(){
        super.init()
        ColorLog.cyan(log: "> NativeCanvasUndoItem init")
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        ColorLog.cyan(log: "> NativeCanvasUndoItem init decoder")
    }
    
    public func destroy() {
        data = nil;
    }
    
    public func exportData()->JSON {
        var data:JSON = JSON()
        
        data[NativeCanvasUndoItem.UNDO_ITEM_TYPE].string    = type?.rawValue
        data[NativeCanvasUndoItem.UNDO_OBJECT_NAME].string  = (objectName == nil || objectName! == "") ? "\"\"" : objectName
        data[NativeCanvasUndoItem.UNDO_DATA].object         = self.data ?? JSON()
        data[NativeCanvasUndoItem.UNDO_INDEX].int           = index
        data[NativeCanvasUndoItem.UNDO_STAMP].double        = stamp
        data[NativeCanvasUndoItem.UNDO_IS_TRANSFORM].bool   = isTransform
        
        return data
    }
    
    public func importData(data:JSON)->Bool {
        
        index       = data[NativeCanvasUndoItem.UNDO_INDEX].int
        type        = UNDO_TYPE(rawValue: data[NativeCanvasUndoItem.UNDO_ITEM_TYPE].string!)
        objectName  = data[NativeCanvasUndoItem.UNDO_OBJECT_NAME].string
        stamp       = data[NativeCanvasUndoItem.UNDO_STAMP].double
        self.data   = data[NativeCanvasUndoItem.UNDO_DATA].object as? JSON
        isTransform = data[NativeCanvasUndoItem.UNDO_IS_TRANSFORM].bool!
        
        return true
    }
    
    public func hashCode()->String{
        if let d = data{
            return (d.rawString()?.MD5())!
        }
        return ""
    }
}
