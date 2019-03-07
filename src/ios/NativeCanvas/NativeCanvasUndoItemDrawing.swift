//
//  NativeCanvasUndoItemDrawing.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation

public class NativeCanvasUndoItemDrawing: NativeCanvasUndoItem{
    
    internal let UNDO_DRAWING_PAINT: String = "UndoDrawingPaint"
    internal let UNDO_DRAWING_PATH: String  = "UndoDrawingPath"
    
    private var path: CGPath?
    //private var paint: paint
    
    public init(type: UNDO_TYPE) {
        super.init(type: type, objectName: nil, data: nil)
    }
    
    public override init() {
        super.init()
    }
    
    public override init(type:UNDO_TYPE, objectName:String?, data:JSON?) {
        super.init(type: type, objectName: objectName, data: data)
    }

    public override init(item: JSON) {
        super.init()
        _ = importData(data: item)
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
    }
    
    public override func importData(data: JSON) -> Bool {
        if(super.importData(data: data)){
            
//            setPaint(new NCPaint(data.optJSONObject(UNDO_DRAWING_PAINT)));
//            setPath(new NCPath(data.optJSONObject(UNDO_DRAWING_PATH)));
            
        }
        return true;
    }
    
    public override func exportData() -> JSON {
        var data:JSON = super.exportData()

//        data.put(UNDO_DRAWING_PAINT, paint.exportData());
//        data.put(UNDO_DRAWING_PATH, path.exportData());

        return data;
    }
}
