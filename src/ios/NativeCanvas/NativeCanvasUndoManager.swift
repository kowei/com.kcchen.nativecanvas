//
//  NativeCanvasUndoManager.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation

public protocol OnUndoDelegate: class {
    func onChanged()
    func onImport(manager: NativeCanvasUndoManager)
}

public class NativeCanvasUndoManager: NSData{
    
    internal let UM_DATA: String = "UmData"
    
    public override var description: String{
        get{
            return toString()
        }
    }
    public var _undoItems:[NativeCanvasUndoItem?] = [NativeCanvasUndoItem?]()
    public var undoItems:[NativeCanvasUndoItem?]{
        get{
            return _undoItems
        }
    }
    private var redoItems:[NativeCanvasUndoItem?] = [NativeCanvasUndoItem?]()
    private var maxUndos: Int = 0
    private var _resets:[Int] = [Int]()
    private var dataHash:Int?
    public var resets:[Int]{
        get{
            return _resets
        }
    }
    private var isChanged:Bool = false
    private var isMove:Bool = false
    private var _delegates:[OnUndoDelegate] = [OnUndoDelegate]()
    public var delegate:OnUndoDelegate?{
        get{
            if _delegates.count > 0 {
                return _delegates.item(at: _delegates.count - 1)!
            }
            return nil
        }
        set{
            if let d = newValue{
                _delegates.append(d)
            }
        }
    }
    public var canUndo:Bool{
        get{ return undoItems.count > 0 }
    }
    public var canRedo:Bool{
        get{ return redoItems.count > 0 }
    }
    public var undoCount:Int{
        get{ return undoItems.count }
    }
    public var redoCount:Int{
        get{ return redoItems.count }
    }
    public var undoCountFromReset:Int{
        get{ return undoItems.count - getResetPoint() }
    }
    public var getCurrent:Int {
        get{ return (undoCount - 1 >= 0) ? undoCount - 1 : 0 }
    }


    public override init(){
        super.init()
        dataHash = toString().hashValue
    }

    public init(maxUndos: Int){
        super.init()
        self.maxUndos = maxUndos
        dataHash = toString().hashValue
    }
    
    public required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        dataHash = toString().hashValue
    }
    
    public func get(index:Int)->NativeCanvasUndoItem?{
        if 0 <= index && index <= undoItems.endIndex{
            return undoItems.item(at: index)!
        }
        return nil
    }
    
    public func add(undoItem: NativeCanvasUndoItem) {
        // set index to undo item
        undoItem.index = undoItems.count

        // clear redo if any
        clearRedos()
        setResets(undoItem: undoItem)
        tagTransform(undoItem: undoItem, isKeepLast: false)

        // Maintains the maximum number of undos (and hence the maximum number of redos)
        _undoItems.append(undoItem)
        isChanged = true;
        if (maxUndos > 0 && undoItems.count > maxUndos) {
            _undoItems.remove(at: 0)
            isChanged = false;
        }
        sendChanged();
        ColorLog.cyan(log: "> NCN undo \(undoItems.count)")
    }
    
    private func clearRedos(){
        if (redoItems.count > 0) {
            redoItems.removeAll()
        }
    }
    
    private func setResets(undoItem: NativeCanvasUndoItem?){
        var removes:[Int] = [Int]()
        for reset in resets {
            if (reset > undoItems.count) {
                removes.append(reset)
            }
        }
        for remove in removes {
            _resets.remove(at: remove);
        }
        if let item = undoItem{
            if (item.type == UNDO_TYPE.CLEAR) {
                if !resets.contains(undoItems.count) {
                    _resets.append(undoItems.count)
                }
            }
        }else{
            for undoItem in undoItems{
                if let item = undoItem{
                    if (item.type == UNDO_TYPE.CLEAR) {
                        if !resets.contains(undoItems.count) {
                            _resets.append(undoItems.count)
                        }
                    }
                }
            }
        }
    }
    
    private func sendChanged() {
        if (isChanged && !isMove) {
            for delegate in _delegates {
                ColorLog.cyan(log: "> NCN sendChanged(\(_delegates.count)) \(delegate)");
                delegate.onChanged();
            }
        }
    }

    private func tagTransform(undoItem: NativeCanvasUndoItem, isKeepLast:Bool) {
        if(undoItem.type == UNDO_TYPE.STICKER_IMAGE || undoItem.type == UNDO_TYPE.STICKER_TEXT){
            if let uuid = undoItem.data![NativeCanvasSticker.STICKER_UUID].string{
                var isUpdateLast:Bool = false
                for i in 0 ... undoItems.count - 1{
                    if let item = undoItems.item(at: i){
                        if ((item?.type == UNDO_TYPE.STICKER_IMAGE || item?.type == UNDO_TYPE.STICKER_TEXT) && item?.data![NativeCanvasSticker.STICKER_UUID].string == uuid) {
                            ColorLog.cyan(log: "> tagTransform \(uuid) - \(item?.data![NativeCanvasSticker.STICKER_UUID].string ?? "null")");
                            if (isKeepLast && !isUpdateLast) {
                                item?.isTransform = false
                                isUpdateLast = true;
                            } else {
                                item?.isTransform = true
                            }
                        }
                    }
                }
            }
        }
    }
    
    public func getResetPoint()->Int{
        var shortestDistance:Int = 0
        for reset in resets{
            let distance = undoItems.count - reset;
            if(shortestDistance == 0 && distance >= 0){
                shortestDistance = distance;
            }
            if(distance > 0) {
                shortestDistance = min(shortestDistance, distance)
            }
        }
        if(shortestDistance != 0){
            return undoItems.count - shortestDistance;
        }else{
            return 0;
        }
    }
    
    public func undo()->NativeCanvasUndoItem? {
        isChanged = false
        if let undoItem = _undoItems.popLast() {
            // Pushes item onto redo stack
            isChanged = true
            redoItems.append(undoItem)
            tagTransform(undoItem: undoItem!, isKeepLast: true)

            sendChanged()
            return undoItem
        }
        return nil
    }
    
    public func redo()->NativeCanvasUndoItem? {
        isChanged = false
        if let redoItem = redoItems.popLast() {
            // Pushes item onto undo stack
            isChanged = true;
            _undoItems.append(redoItem)
            if(isChanged){
                tagTransform(undoItem: redoItem!, isKeepLast: true)
            }
            sendChanged()
            return redoItem
        }
        return nil
    }
    
    public func move(step:Int)->Bool {
        if(step < 0){
            isMove = true;
            for _ in 1 ... -step {
                _ = undo();
            }
            isMove = false;
            sendChanged();
            return true;
        }else if(step > 0){
            isMove = true;
            for _ in 1 ... step {
                _ = redo();
            }
            isMove = false;
            sendChanged();
            return true;
        }
        return false;
    }
    
    public func setData(data:JSON) {
        if data.count == 0 { return }
        for i in 0 ... (data.count - 1) {
            if let item = data.array?.item(at: i){
                if let type: UNDO_TYPE = UNDO_TYPE(rawValue: item[NativeCanvasUndoItem.UNDO_ITEM_TYPE].string!){
                    adjustUndoItems(index: i);
                    switch type{
                    case .DRAWING, .ERASER:
                        _undoItems.insert(NativeCanvasUndoItemDrawing(item: item), at: i)
                        break
                    case .STICKER_IMAGE:
                        _undoItems.insert(NativeCanvasUndoItem(item: item), at: i)
                        break
                    case .STICKER_TEXT:
                        _undoItems.insert(NativeCanvasUndoItem(item: item), at: i)
                        break
                    case .CLEAR:
                        _undoItems.insert(NativeCanvasUndoItem(item: item), at: i)
                        break
                    case .TRANSFORM:
                        break
                    case .TEMPLATE:
                        break
                    }
                    isChanged = true;
                    sendChanged();
                    ColorLog.lightBlue(log: "> NCN setData \(undoCount)");
                }
            }
        }
    }
    
    private func adjustUndoItems(index:Int){
        if undoItems.endIndex < index {
            for i in undoItems.endIndex ... index{
                _undoItems.insert(nil, at: i)
            }
        }
    }
    
    public func isEmpty()->Bool {
        return undoItems.count == 0 && redoItems.count == 0;
    }
    
    private func removeNull() {
        ColorLog.lightBlue(log: "> removeNull \(_undoItems.count)")
        var isRemoved: Bool = false
        while let index = _undoItems.index(where: {$0 == nil}) {
            _undoItems.remove(at: index)
            isRemoved = true
        }
        if isRemoved{
            for i in 0 ... (undoItems.count - 1) {
                ColorLog.lightBlue(log: "-------> removeNull new item \(i) \(undoItems.item(at: i))")
                let item = undoItems.item(at: i) as! NativeCanvasUndoItem
                item.index = i
            }
            ColorLog.lightBlue(log: "-------> removeNull \(_undoItems.count)")
        }
    }
    
    private func removeNull( jsonArray:JSON)->JSON {
        //ColorLog.lightBlue(log: "> removeNull JSON \(jsonArray.endIndex) \(jsonArray.sp)")
        for (index,subJson):(String, JSON) in jsonArray {
            ColorLog.lightBlue(log: "> removeNull JSON item \(index) \(subJson)")
        }

        var cleanJsonArray = JSON();
        if let arrayLength = jsonArray.array?.count{
            for i in 0 ... arrayLength {
                if let item = jsonArray.array?.item(at: i){
                    cleanJsonArray[i] = item
                }
            }
        }
        ColorLog.lightBlue(log: "> removeNull JSON \(cleanJsonArray) \(cleanJsonArray.count)")
        return cleanJsonArray;
    }
    
    public func isUpdate()->Bool {
        let currentHash = toString().hashValue
        ColorLog.lightBlue(log: "> NCN isUpdate \(dataHash) - \(currentHash)");
        return dataHash != currentHash
    }
    
    public func exportData()->JSON {
        var exportData:JSON = JSON()

        removeNull();
        exportData[UM_DATA].string = toString()

        return exportData;
    }
    
    public func importData( data:JSON)->Bool {
        
        clear()
        
        let jsonArray = JSON(parseJSON: data[UM_DATA].string ?? "")
        setData(data: removeNull(jsonArray: jsonArray))

        removeNull()

        setResets(undoItem: nil)
        dataHash = toString().hashValue
        ColorLog.lightBlue(log: "> importData \(dataHash)")
        
        for delegate in _delegates {
            delegate.onImport(manager: self)
        }
        return true
    }
    
    public func clear() {
        _undoItems.removeAll()
        redoItems.removeAll()
        _resets.removeAll()
        isChanged = true
        isMove = false
        sendChanged()
        isChanged = false
        isMove = false
    }
    
    public func removeDelegatess(delegate: OnUndoDelegate){
        ColorLog.lightBlue(log: "> removeDelegatess \(_delegates.count) \(_delegates)")
        if let index = _delegates.index(where: {$0 === delegate}) {
            _delegates.remove(at: index)
        }
        ColorLog.lightBlue(log: "> removeDelegatess \(_delegates.count) \(_delegates)")
    }
    
    private func toString()->String{
        var undoItemsJson:JSON = JSON()
        if _undoItems.count > 0{
            for i in 0 ... (_undoItems.count - 1) {
                if let item = _undoItems.item(at: i){
                    if let data = item{
                        undoItemsJson[i] = data.exportData()
                    }
                }
            }
        }
        return undoItemsJson.rawString() ?? ""
    }
}
