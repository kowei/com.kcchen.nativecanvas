//
//  NativeCanvasEnum.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation





public enum ASSET_TYPE: String{

    case PNG,JPEG,GIF,SVG,PDF,UNKNOWN,PNG_64,JPEG_64,GIF_64,SVG_64
    
    private static var _data = [String : String]()
    public var data:String{
        get{
            return ASSET_TYPE._data[self.rawValue] ?? ""
        }
        set{
            ASSET_TYPE._data[self.rawValue] = newValue
        }
    }
    public static func parse(file: String)->ASSET_TYPE{
        let str: String = file.lowercased()
        var type: ASSET_TYPE = ASSET_TYPE.UNKNOWN
        if(str.matches(pattern: ".*\\.png") || str == "png"){
            type = ASSET_TYPE.PNG
        }else if(str.matches(pattern: ".*\\.svg") || str == "svg"){
            type = ASSET_TYPE.SVG
        }else if(str.matches(pattern: ".*\\.gif") || str == "gif"){
            type = ASSET_TYPE.GIF
        }else if(str.matches(pattern: ".*\\.pdf") || str == "pdf"){
            type = ASSET_TYPE.PDF
        }else if(str.matches(pattern: ".*\\.jpg") || str.matches(pattern: ".*\\.jpeg") || str == "jpeg"){
            type = ASSET_TYPE.JPEG
        }else if(str.matches(pattern: "^data:image/jpeg")){
            type = ASSET_TYPE.JPEG_64
            type.data = file
        }else if(str.matches(pattern: "^data:image/png")){
            type = ASSET_TYPE.PNG_64
            type.data = file
        }else if(str.matches(pattern: "^data:image/svg+xml")){
            type = ASSET_TYPE.SVG_64
            type.data = file
        }else if(str.matches(pattern: "^data:image/gif")){
            type = ASSET_TYPE.GIF_64
            type.data = file
        }
        //print("> asset type \(type)")
        return type
    }
    
    public static func isSupported( file:String)->Bool {
        let str:String = file.lowercased()
        var type:ASSET_TYPE = ASSET_TYPE.UNKNOWN
        if (str.matches(pattern: ".*\\.png")) {
            type = PNG;
        } else if (str.matches(pattern: ".*\\.svg")) {
            type = SVG;
        } else if (str.matches(pattern: ".*\\.gif")) {
            type = GIF;
        } else if (str.matches(pattern: ".*\\.pdf")) {
            type = PDF;
        } else if (str.matches(pattern: ".*\\.jpg") || str.matches(pattern: ".*\\.jpeg")) {
            type = JPEG;
        }
        // ColorLog.purple(log: "ASSET_TYPE: \(file) \(str) \(type)");
        return type != ASSET_TYPE.UNKNOWN
    }
    
//    private final let message:String?
//    private let data:String?
    
//    private func ASSET_TYPE(message:String) {
//    self.message = message;
//    }
    
//    public static func get(text:String)-> ASSET_TYPE{
//    if (text != nil) {
////        for (ASSET_TYPE b : ASSET_TYPE.values()) {
////        if (text.equalsIgnoreCase(b.message))
////        return b;
////        }
//    }
//    return ASSET_TYPE.UNKNOWN;
//    }
    
//    public func key()-> String{
//        return message;
//    }
    
    public func getData()-> String{
        return data;
    }
    
//    public func clearData(){
//        data = nil;
//    }
//
    public func isEmbed()-> Bool{
        
        return data != nil;
    }
    
}

public enum RESULT_TYPE: Int{
    case
    SUCCESS             = 0,
    INVALID             = 1,
    FAILED              = 2,
    BOOK_NOT_EXISTED    = 3,
    BOOK_CREATE_FAILED  = 4,
    PAGE_NOT_EXISTED    = 5,
    PAGE_CREATE_FAILED  = 6,
    IMAGE_TYPE_ERROR    = 7,
    VIEW_INVALID        = 8,
    ADD_OBJECT_INVALID  = 9
}

public enum BOOK_TYPE: String{
    case
    UNSET  = "unset",
    NOTE   = "note",
    IMAGE  = "image",
    GIF    = "gif",
    PDF    = "pdf"
}

public enum BORDER_TYPE: String{
    case
    NONE      = "none",
    CIRCLE    = "circle",
    RECTANGLE = "rectangle",
    IMAGE     = "image"
    
    private static var _data = [String : String]()
    public var data:String{
        get{
            return BORDER_TYPE._data[self.rawValue] ?? ""
        }
        set{
            BORDER_TYPE._data[self.rawValue] = newValue
        }
    }
    
    public static let BORDER_TYPE_TYPE        = "BorderTypeType";
    public static let BORDER_TYPE_WIDTH       = "BorderTypeWidth";
    public static let BORDER_TYPE_HEIGHT      = "BorderTypeHeight";
    public static let BORDER_TYPE_RESOURCE_ID = "BorderTypeResourceId";
    public static let BORDER_TYPE_ROTATE      = "BorderTypeRotate";
    
    class BORDER_TYPE_VALUE {
        var key:String
        var widthy:Int
        var height:Int
        var resourceId:Int
        var rotate:Float
        
        init(key:String, widthy:Int, height:Int, resourceId:Int, rotate:Float) {
            self.key = key
            self.widthy = widthy
            self.height = height
            self.resourceId = resourceId
            self.rotate = rotate
        }
        
    }
   
    public func key()-> String{
        return self.key();
    }
    public var width: Int {
        get {
            return self.width
        }
        set {
            return width = newValue
        }
       
    }
    public var height: Int {
        get {
            return self.height
        }
        set {
            return height = newValue
        }
    }
    public var resourceId: Int {
        get {
            return self.resourceId
        }
        set {
            return resourceId = newValue
        }
    }
    public var rotate: Float {
        get {
            return self.rotate
        }
        set {
            return rotate = newValue
        }
        
    }

    public func exportData()-> JSON{
        var data:JSON = JSON();
//            data.put(BORDER_TYPE_TYPE, key());
            data[BORDER_TYPE.BORDER_TYPE_TYPE] = JSON(key())
//            data.put(BORDER_TYPE_WIDTH, getWidth() == null ? "" : getWidth() + "");
            data[BORDER_TYPE.BORDER_TYPE_WIDTH] = JSON(width)
//            data.put(BORDER_TYPE_HEIGHT, getHeight() == null ? "" : getHeight() + "");
            data[BORDER_TYPE.BORDER_TYPE_HEIGHT] = JSON(height)
//            data.put(BORDER_TYPE_RESOURCE_ID, getResourceId() == null ? "" : getResourceId() + "");
            data[BORDER_TYPE.BORDER_TYPE_RESOURCE_ID] = JSON(resourceId)
//            data.put(BORDER_TYPE_ROTATE, getRotate() == null ? "" : getRotate() + "");
            data[BORDER_TYPE.BORDER_TYPE_ROTATE] = JSON(rotate)
        ColorLog.purple(log: "BORDER_TYPE exportData:  width \(width)  height: \(height) resourceId: \(resourceId) rotate: \(rotate) ");
        return data;
    }
    
    public mutating func importData(data:JSON)-> Bool{
        var isImported:Bool = false
        var value:String?
            value = data[BORDER_TYPE.BORDER_TYPE_WIDTH].string
            width = Int(value!)!
//            setWidth(value.isEmpty() ? nil : Integer.valueOf(value));
            value = data[BORDER_TYPE.BORDER_TYPE_HEIGHT].string
//            setHeight(value.isEmpty() ? nil : Integer.valueOf(value));
            height = Int(value!)!
            value = data[BORDER_TYPE.BORDER_TYPE_RESOURCE_ID].string
//            setResourceId(value.isEmpty() ? nil : Integer.valueOf(value));
            resourceId = Int(value!)!
            value = data[BORDER_TYPE.BORDER_TYPE_ROTATE].string
//            setRotate(value.isEmpty() ? nil : Float.parseFloat(value));
            rotate = Float(value!)!
//            BORDER_TYPE_VALUE.init(key: key(), widthy: widthy, height: height, resourceId: resourceId, rotate: rotate)
            isImported = true;
        ColorLog.purple(log: "BORDER_TYPE importData:  width \(width)  height: \(height) resourceId: \(resourceId) rotate: \(rotate) ");
        return isImported;
    }
    
}

public enum CALLBACK_TYPE: String{
    case
    MINI_MAP       = "MiniMap",
    TEXT_INPUT     = "TextInput",
    EXIT           = "NativeCanvasExit",
    UNDO_MANAGER   = "UndoManager",
    INSERT_MANAGER = "InsertManager",
    ERROR          = "Error"
}

public enum MOVE_MODE: String{
    case
    MOVE   = "move",
    ROTATE = "rotate",
    SCALE  = "scale"
}


public enum MOVE_STATUS: String{
    case
    INSIDE_DOWN    = "InsideDown",
    OUTSIDE_DOWN   = "OutsideDown",
    INSIDE_MOVE    = "InsideMove",
    OUTSIDE_MOVE   = "OutsideMove",
    INSIDE_UP      = "InsideUp",
    OUTSIDE_UP     = "OutsideUp",
    INVALID        = "Invalid"
}

public enum NEURAL_STYLE: String{
    
    case PNG,JPEG,GIF,SVG,PDF,PNG_64,JPEG_64,GIF_64,SVG_64,UNKNOWN
    
    private static var _data = [String : String]()
    public var data:String{
        get{
            return NEURAL_STYLE._data[self.rawValue] ?? ""
        }
        set{
            NEURAL_STYLE._data[self.rawValue] = newValue
        }
    }
    public static func parse(file: String)->NEURAL_STYLE{
        let str: String = file.lowercased()
        var type: NEURAL_STYLE? = nil
        if(str.matches(pattern: ".*\\.png") || str == "png"){
            type = NEURAL_STYLE.PNG
        }else if(str.matches(pattern: ".*\\.svg") || str == "svg"){
            type = NEURAL_STYLE.SVG
        }else if(str.matches(pattern: ".*\\.gif") || str == "gif"){
            type = NEURAL_STYLE.GIF
        }else if(str.matches(pattern: ".*\\.pdf") || str == "pdf"){
            type = NEURAL_STYLE.PDF
        }else if(str.matches(pattern: ".*\\.jpg") || str.matches(pattern: ".*\\.jpeg") || str == "jpeg"){
            type = NEURAL_STYLE.JPEG
        }else if(str.matches(pattern: "^data:image/jpeg")){
            type = NEURAL_STYLE.JPEG_64
            type?.data = file
        }else if(str.matches(pattern: "^data:image/png")){
            type = NEURAL_STYLE.PNG_64
            type?.data = file
        }else if(str.matches(pattern: "^data:image/svg+xml")){
            type = NEURAL_STYLE.SVG_64
            type?.data = file
        }else if(str.matches(pattern: "^data:image/gif")){
            type = NEURAL_STYLE.GIF_64
            type?.data = file
        }
        //print("> asset type \(type)")
        return type!
    }
}

public enum OBJECT_TYPE: Int {
    case
    IMAGE     = 0,
    TEXT      = 1,
    TEMPLATE  = 2
}


/**
 * 筆觸類型
 * Unset = 0, 無設定
 * Eraser = 1, 橡皮擦
 * Fountain = 2, 鋼筆
 * HighLight = 3, 螢光筆
 * BallPoint = 4, 原子筆
 */
public enum PEN_TYPE: Int {
    case
    UNKNOWN     = 0,
    ERASER      = 1,
    FOUNTAIN    = 2,
    HIGHLIGHTER = 3,
    BALLPOINT   = 4
}

public enum UNDO_TYPE: String{
    case
    DRAWING         = "Drawing",
    ERASER          = "Eraser",
    STICKER_IMAGE   = "StickerImage",
    STICKER_TEXT    = "StickerText",
    CLEAR           = "Clear",
    TRANSFORM       = "Transform",
    TEMPLATE        = "Template"
}

public enum StrokeViewDisplayOptions {
    case debug
    case calligraphy
    case ink
}

public enum StrokePhase {
    case began
    case changed
    case ended
    case cancelled
}


enum StrokeState {
    case active
    case done
    case cancelled
}
