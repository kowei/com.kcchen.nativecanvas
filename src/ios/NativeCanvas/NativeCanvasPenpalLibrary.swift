//
//  NativeCanvasPenpalLibrary.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation


/*
 NativeCanvasPenpalLibrary Object實作
 */
public class NativeCanvasPenpalLibrary{
    /*
     以下是靜態存取介面
     */
    private static var Library:[String:NativeCanvasPenpalLibrary?]? = [String:NativeCanvasPenpalLibrary?]()
    private static var selectedLibrary:NativeCanvasPenpalLibrary?
    private static var selectedLibraryID:String?
    
    public static func hasLibrary(library: NativeCanvasPenpalLibrary?)->Bool {
        if let l = library{
            selectedLibraryID = l.libraryPath
            let result = l.isValid()
//            ColorLog.lightBlue(log: "> PenpalLibrary hasLibrary \(result)")
            return result
        }
        return false
    }
    
    public static func hasBookshelf(library:NativeCanvasPenpalLibrary?, bookshelf:String)->Bool {
        if let l = library{
            if(NativeCanvasPenpalLibrary.hasLibrary(library: l)){
                let result = l.hasBookshelf(bookshelfID: bookshelf)
//                ColorLog.lightBlue(log: "> PenpalLibrary hasBookshelf \(result)")
                return result
            }
        }
        return false
    }
    
    public static func hasBook(library:NativeCanvasPenpalLibrary?, book:String, type:BOOK_TYPE)->Bool {
        if let l = library{
            if(NativeCanvasPenpalLibrary.hasBookshelf(library: l)){
                let result = l.selectedBookshelf?.hasBook(bookID: book) ?? false
//                ColorLog.lightBlue(log: "> PenpalLibrary hasBook \(result)")
                return result
            }
        }
        return false
    }
    
    public static func hasPage(library:NativeCanvasPenpalLibrary?, page:Int)->Bool {
        ColorLog.lightBlue(log: "> PenpalLibrary hasPage** \(page)")
        let pageString:String = "\(page - 1)"
        if let l = library{
            if(NativeCanvasPenpalLibrary.hasBook(library: l)){
                let result = l.selectedBookshelf?.selectedBook?.hasPage(pageNum: pageString) ?? false
                ColorLog.lightBlue(log: "> PenpalLibrary hasPage result: \(result)")
                return result
            }
        }
        return false
    }
    
    public static func hasBookshelf(library:NativeCanvasPenpalLibrary?)->Bool {
        return library != nil
            && library?.selectedBookshelf != nil
            && library?.selectedBookshelf?.isValid() != nil
            && (library?.selectedBookshelf?.isValid())!
    }
    
    public static func hasBook(library:NativeCanvasPenpalLibrary?)->Bool {
        return library != nil
            && library?.selectedBookshelf != nil
            && library?.selectedBookshelf?.selectedBook != nil
            && library?.selectedBookshelf?.selectedBook?.isValid() != nil
            && (library?.selectedBookshelf?.selectedBook?.isValid())!
    }
    
    public static func hasPage(library:NativeCanvasPenpalLibrary?)->Bool {
        return library != nil
            && library?.selectedBookshelf != nil
            && library?.selectedBookshelf?.selectedBook != nil
            && library?.selectedBookshelf?.selectedBook?.selectedPage != nil
            && library?.selectedBookshelf?.selectedBook?.selectedPage?.isValid() != nil
            && (library?.selectedBookshelf?.selectedBook?.selectedPage?.isValid())!
    }

    
    public static func get(libraryPath:String?)->NativeCanvasPenpalLibrary?{
        if let path = libraryPath{
            if let url = path.getFolderURL(){
                let path = url.path
                
                ColorLog.lightGreen(log: "> selectedLibrary path \(path ?? "")")
                
                if let library = selectedLibrary{
                    if(library.libraryPath == path){
                        // Do nothing as library = selectedLibrary
                        ColorLog.red(log: "> selectedLibrary is CURRENT")
                    }else{
                        selectedLibrary = createLibrary(path: path!)
                    }
                }else{
                    selectedLibrary = createLibrary(path: path!)
                }
            }
        }
        return selectedLibrary;
    }
    
    internal static func createLibrary(path:String)->NativeCanvasPenpalLibrary?{
        var library:NativeCanvasPenpalLibrary?
        if let librarys = Library{
            if let storedLibrary = librarys[path]{
                // set selectedLibrary from Library get path
                library = storedLibrary
                ColorLog.red(log: "> selectedLibrary from OLD Library " + path)
            }else{
                let newLibrary = NativeCanvasPenpalLibrary(libraryPath: path)
                if(newLibrary.isValid()){
                    Library![path] = newLibrary
                    // set selectedLibrary from new Library path
                    library = newLibrary
                    ColorLog.red(log: "> selectedLibrary from NEW " + path)
                }else{
                    ColorLog.red(log: "> selectedLibrary NOT FOUND")
                }
            }
        }else{
            ColorLog.red(log: "> selectedLibrary Library NOT FOUND")
        }
        return library
    }
    
    public static func getCurrentLibrary()->NativeCanvasPenpalLibrary? {
        return selectedLibrary;
    }
    
    public func getTemplateManager()->NativeCanvasPenpalTemplateManager? {
        return templateManager
    }
    
    public static func getCurrentBookshelf()->NativeCanvasPenpalBookshelf? {
        return getCurrentLibrary()?.selectedBookshelf
    }
    
    public static func getCurrentBook()->NativeCanvasPenpalBook? {
        return getCurrentBookshelf()?.selectedBook
    }
    
    public static func getCurrentPage()->NativeCanvasPenpalPage? {
        return getCurrentBook()?.selectedPage
    }
    
    public static func close(){
        for item in Library! {
            if let library:NativeCanvasPenpalLibrary = item.value{
                library.destroy()
            }
            Library![item.key] = nil
        }
    }
    // end static API ---------------------------------------------------------
    
    private let LIBRARY_PROFILE:String = "library.json";

    internal var libraryPath:String?
    internal var libraryProfilePath:String?
    internal var selectedBookshelf:NativeCanvasPenpalBookshelf?
    internal var templateManager:NativeCanvasPenpalTemplateManager?
    private var libraryFolder:NSURL?
    private var libraryProfile:NSURL?
    private var libraryJson:JSON?
    private var Bookshelfs:[String:NativeCanvasPenpalBookshelf?]? = [String:NativeCanvasPenpalBookshelf?]()
    private var isInit:Bool = false

    init(libraryPath:String){
        self.libraryPath = libraryPath;
        self.libraryProfilePath = libraryPath + "/" + LIBRARY_PROFILE;
        self.initialize()
    }
    
    private func initialize()-> Bool{
        ColorLog.cyan(log: "Library initialize")
//        var templateManager = self.templateManager
        if(!isValid()) {
            destroy();
            return false;
        }else{
            templateManager = NativeCanvasPenpalTemplateManager(library: self)
            return load();
        }
    }
    
    public func isValid()->Bool{
        if(!isInit){
            if let folderUri:NSURL = libraryPath?.getFolderURL(), let fileUri:NSURL = libraryProfilePath?.getFileURL(){
                libraryFolder = folderUri
                libraryProfile = fileUri
            }
            isInit = true;
        }
        // ColorLog.green(log: "Library isValid \(libraryFolder != nil)")
        return libraryFolder != nil && libraryProfile != nil
    }

    public func hasBookshelf( bookshelfID:String?)->Bool {
        if let path = bookshelfID, let bookshelf = selectedBookshelf{
            if(bookshelf.bookshelfID == path){
                // Do nothing as library = selectedLibrary
                ColorLog.red(log: "> selectedBookshelf is current")
            }else{
                selectedBookshelf = createBookshelf(id: bookshelfID!)
            }
        }else{
            selectedBookshelf = createBookshelf(id: bookshelfID!)
        }
        return selectedBookshelf != nil;
    }
    
    private func createBookshelf(id:String)->NativeCanvasPenpalBookshelf?{
        var bookshelf:NativeCanvasPenpalBookshelf?

        if var bookshelfs = Bookshelfs{
            if let storedBookshelf = bookshelfs[id]{
                // set selectedBookshelf from Library get path
                bookshelf = storedBookshelf
                ColorLog.red(log: "> selectedBookshelf from Library " + id)
            }else{
                let newBookshelf = NativeCanvasPenpalBookshelf(library: self, bookshelfID: id)
                if(newBookshelf.isValid()){
                    bookshelfs[id] = newBookshelf
                    // set selectedLibrary from new Library path
                    bookshelf = newBookshelf
                    ColorLog.red(log: "> selectedBookshelf from NEW " + id)
                }else{
                    ColorLog.red(log: "> selectedBookshelf NOT FOUND")
                }
            }
        }else{
            ColorLog.red(log: "> selectedLibrary Bookshelf NOT FOUND")
        }
        return bookshelf
    }
    
    public func save()->Bool {
        var result:Bool = false;
        if (isValid()) {

            if let json = libraryJson{
                let urlString:String = libraryProfile!.relativePath!
                let url:String = "file:///" + urlString
                if let nsurl = url.url{
//                    if let nsurl = libraryProfile{
                    do{
                        let fm = FileManager.default
                        if fm.isWritableFile(atPath: nsurl.path) && !fm.fileExists(atPath: nsurl.path){
                            if fm.createFile(atPath: nsurl.path, contents: nil, attributes: nil){
                                ColorLog.lightBlue(log: "> save, create file \(nsurl.path)...")
                            }
                        }
                        //                            let url = try URL(fileURLWithPath: "file://" + nsurl.path!)
                        ColorLog.lightBlue(log: "> library save to \(nsurl.path)...")
                        try json.rawString()?.write(to: nsurl as URL, atomically: false, encoding: .utf8)
                        ColorLog.lightBlue(log: "> library save to \(nsurl.path)...")
                        result = true
                    }catch (let e as NSError){
                        ColorLog.red(log: "> save, error the path is \(nsurl.path)...")
                        result = false
                        ColorLog.red(log: "> library catch save error \(e.localizedDescription)")
                    }
                }
            }else{
                ColorLog.lightRed(log: "> library save to JSON invalid")
            }
        }
        return result;
    }
    
    public func load()->Bool {
        if (isValid()) {
            do {
                libraryJson = createProfile()
                ColorLog.lightYellow(log: "> library loaded from default ... \(libraryJson)")
                if let url = libraryProfile{
                    if let path = url.path{
                        if let data = NSData(contentsOfFile: path){
                            let loadJson = JSON(data: data as Data)
                            ColorLog.lightYellow(log: "> library loaded from disk... \(loadJson)")
                            try libraryJson?.merge(with: loadJson)
                            ColorLog.lightYellow(log: "> library loaded merge... \(libraryJson ?? "")")
                        }
                    }
                }
                // TODO: hashcode to verify saving
                _ = save()
                ColorLog.lightYellow(log: "> library loaded... \(libraryJson ?? "")")
            } catch  {
                ColorLog.red(log: "> library catch loaded error")
                libraryJson = createProfile()
                _ = save()
                return false;
            }
            return true;
        }
        return false;
    }
    
    private func createProfile()->JSON {
        let profile:JSON = ["" :""];
        
        // profile[LIBRARY_PROFILE] = "v1.0"
        return profile;
    }
    
    public func destroy() {
        clean();
    }
    
    public func clean() {

            if(libraryJson != nil){
//                while (libraryJson.keys().hasNext()) {
//                    libraryJson.put(libraryJson.keys().next(), null);
//            Bookshelfs.removeAllCachedResourceValues()
                }
//                libraryJson = null;
//                libraryFile = null;
//                cleanBookShelf();
//            }

    }
}
