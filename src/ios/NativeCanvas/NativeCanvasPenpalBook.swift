//
//  NativeCanvasPenpalBook.swift
//  PenPal
//
//  Created by GUAN BEI-FONG on 2018/4/23.
//

import Foundation

public class NativeCanvasPenpalBook{
    private let BOOK_PROFILE: String = "profile.json";
    
    private let BOOK_FILE_FORMAT_VERSION: String   = "fileFormatVersion";
    private let BOOK_IS_GUIDE_DOC: String          = "isGuideDoc";
    private let BOOK_IS_HIDDEN: String             = "isHidden";
    private let BOOK_DISPLAY_NAME: String          = "displayName";
    private let BOOK_TAG: String                   = "tag";
    private let BOOK_PROJECT_TYPE: String          = "projectType";
    private let BOOK_ASSET_CHECK_FLAG: String      = "assetCheckFlag";
    private let BOOK_ASSET_FILES: String           = "assetFiles";
    private let BOOK_CATEGORY: String              = "category";
    private let BOOK_GROUP: String                 = "group";
    private let BOOK_AUTHOR: String                = "author";
    private let BOOK_CREATE_DATE: String           = "createDate";
    private let BOOK_MODIFY_DATE: String           = "modifyDate";
    private let BOOK_COMMENT: String               = "comment";
    private let BOOK_PDFSRCFILENAME: String        = "pdfSrcFileName";


    
    internal var selectedPage:NativeCanvasPenpalPage?
    internal var bookID:String?
    internal var bookPath:String?
    internal var assetManager:NativeCanvasPenpalAssetManager?
    private var bookshelf:NativeCanvasPenpalBookshelf
    private var bookFolder:NSURL?
    private var bookProfile:NSURL?
    private var bookJson:JSON?
    private var Pages:[String:NativeCanvasPenpalPage?]? = [String:NativeCanvasPenpalPage?]()
    private var isInit:Bool = false
    private var bookType:BOOK_TYPE?

    init(bookshelf:NativeCanvasPenpalBookshelf, bookID:String){
        self.bookID = bookID;
        self.bookshelf = bookshelf;
        self.bookPath = bookshelf.bookshelfPath! + "/" + bookID;
        _ = self.initializes()
        // self.test()
    }
    
    private func initializes()-> Bool{
        ColorLog.cyan(log: "Book initialize")
        var assetManager = self.assetManager
        if(!isValid()) {
            destroy();
            return false;
        }else{
            assetManager = NativeCanvasPenpalAssetManager(book: self)
            return load();
        }
    }
    
    public func isValid()->Bool{
        if(!isInit){
            if let folderUri:NSURL = bookPath?.getFolderURL(){
                bookFolder = folderUri
            }
            isInit = true;
        }
        // ColorLog.green(log: "Book isValid \(bookFolder != nil)")
        return bookFolder != nil
    }
    
    public func hasPage( pageNum:String?)->Bool {
        if let id = pageNum, let page = selectedPage{
            if(page.id == id){
                // Do nothing as library = selectedLibrary
                ColorLog.red(log: ">Book selectedPage is current")
            }else{
                ColorLog.red(log: ">Book selectedPage \(createPage(id: pageNum!))")
                selectedPage = createPage(id: pageNum!)
            }
        }else{
            selectedPage = createPage(id: pageNum!)
        }
        return selectedPage != nil;
    }
    
    public func setType(type:BOOK_TYPE){
        self.bookType =  type
    }
    
    private func createPage(id:String)->NativeCanvasPenpalPage?{
        var page:NativeCanvasPenpalPage?
        do{
            if var pages = Pages{
                if let storedPage = pages[id]{
                    // set selectedPage from Library get path
                    page = storedPage
                    ColorLog.red(log: "> selectedPage from Pages " + id)
                }else{
                    let newPage = NativeCanvasPenpalPage(book: self, pageID: id)
                    ColorLog.red(log: "> selectedPage newPage \(newPage)")
                    if(newPage.isValid()){
                        pages[id] = newPage
                        // set selectedPage from new Library path
                        page = newPage
                        ColorLog.red(log: "> selectedPage from NEW " + id)
                    }else{
                        ColorLog.red(log: "> selectedPage NOT FOUND")
                    }
                }
            }else{
                ColorLog.red(log: "> selectedPage  NOT FOUND")
            }
        }catch{
            ColorLog.red(log: "> selectedPage catch")
        }
        return page
    }
    
    public func createPage(page:Int)->Bool{
        let pageID = "\(page - 1)"
        let newPage = NativeCanvasPenpalPage(book: self, pageID: pageID)
        _ = newPage.make()
        if var pages = Pages{
            if newPage.isValid(){
                pages[pageID] = newPage
                selectedPage = newPage;
                return true;
            }
        }
        return false;
    }
    
    public func getBookPath()->String {
    return bookPath!;
    }
    
    public func getBookshelf()-> NativeCanvasPenpalBookshelf{
    return bookshelf;
    }
    public func getAssetManager()-> NativeCanvasPenpalAssetManager{
    return assetManager!;
    }
    
    public func save()->Bool {
        var result:Bool = false;
        if (isValid()) {
            do {
                if let json = bookJson{
                    if let nsurl = bookProfile{
                        do{
                            let fm = FileManager.default
                            if fm.isWritableFile(atPath: nsurl.path!) && !fm.fileExists(atPath: nsurl.path!){
                                if fm.createFile(atPath: nsurl.path!, contents: nil, attributes: nil){
                                    ColorLog.lightBlue(log: ">Book  save, create file \(nsurl.path!)...")
                                }
                            }
                            //                            let url = try URL(fileURLWithPath: "file://" + nsurl.path!)
                            ColorLog.lightBlue(log: "> Book save to \(nsurl.path!)...")
                            try json.rawString()?.write(to: nsurl as URL, atomically: false, encoding: .utf8)
                            ColorLog.lightBlue(log: "> Book save to \(nsurl.path!)...")
                            result = true
                        }catch (let e as NSError){
                            result = false
                            ColorLog.red(log: "> Book catch save error \(e.localizedDescription)")
                        }
                    }
                }else{
                    ColorLog.lightRed(log: "> Book save to JSON invalid")
                }
            } catch (let e as NSError){
                result = false
                ColorLog.red(log: "> Book catch save error \(e.localizedDescription)")
            }
        }
        return result;
    }
    
    public func load()->Bool {
        if (isValid()) {
            do {
                bookJson = createProfile()
                ColorLog.lightYellow(log: "> Book loaded from default ... \(bookJson)")
                ColorLog.lightYellow(log: "> Book book name: ...'' \(getBookName())''")
                if let url = bookProfile{
                    if let path = url.path{
                        if let data = NSData(contentsOfFile: path){
                            let loadJson = JSON(data: data as Data)
                            ColorLog.lightYellow(log: "> Book loaded from disk... \(loadJson)")
                            try bookJson?.merge(with: loadJson)
                            ColorLog.lightYellow(log: "> Book loaded merge... \(bookJson ?? "")")
                        }
                    }
                }
                // TODO: hashcode to verify saving
                _ = save()
                ColorLog.lightYellow(log: "> Book loaded... \(bookJson ?? "")")
            } catch  {
                ColorLog.red(log: "> Book catch loaded error")
                bookJson = createProfile()
                _ = save()
                return false;
            }
            return true;
        }
        return false;
    }
    
    private func createProfile()->JSON {
        
        var bookprofile:JSON = JSON()
        let bookAssetFiles:JSON = JSON("{}")
        let currentTime = Int64(Date().timeIntervalSince1970 * 1000)//時間戳
        bookprofile[BOOK_FILE_FORMAT_VERSION] = "v1.0"
        bookprofile[BOOK_IS_GUIDE_DOC] = false
        bookprofile[BOOK_IS_HIDDEN] = false
        bookprofile[BOOK_DISPLAY_NAME] = ""
        bookprofile[BOOK_TAG] = ""
        bookprofile[BOOK_PROJECT_TYPE] = ""
        bookprofile[BOOK_ASSET_CHECK_FLAG] = false
        bookprofile[BOOK_ASSET_FILES] = bookAssetFiles
        bookprofile[BOOK_CATEGORY] = ""
        bookprofile[BOOK_GROUP] = ""
        bookprofile[BOOK_AUTHOR] = ""
        bookprofile[BOOK_CREATE_DATE] = ""//JSON(currentTime)
        bookprofile[BOOK_MODIFY_DATE] = ""
        bookprofile[BOOK_PDFSRCFILENAME] = ""
        
        
//        let now = Date()
//        // 创建一个日期格式器
//        let dformatter = DateFormatter()
//        dformatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
//        print("当前日期时间：\(dformatter.string(from: now))")

        return bookprofile;
    }
    
    public func destroy() {
        clean();
    }
    
    private func cleanPages() {
//    for(PenpalPage page: pages.values()){
//    page.destroy();
//    }
//    pages.clear();
    }
    
    public func clean() {
//        try {
//        if(bookProfile != null){
//        while (bookProfile.keys().hasNext()) {
//        bookProfile.put(bookProfile.keys().next(), null);
//        }
//        bookProfile = null;
//        bookFile = null;
//        cleanPages();
//        }
//        } catch (JSONException e) {
//        e.printStackTrace();
//        }
        do {
            
        } catch {
            
        }
    }
    
    public func getBookName()->String {
        var name:String = "BOOK";
        if(bookProfile != nil) {
            let path = bookProfile?.path
            ColorLog.lightYellow(log: "> Book bookprofile .....path ... \(path)")
//            name = bookProfile?. //.optString(BOOK_DISPLAY_NAME);
//                if(name == nil || name.isEmpty){
//                    name = "BOOK";
//                    }
            }
        return name;
    }
    
    public func test(){
        do{
            var jsonObj:JSON = [
                "1": 1,
                "2":
                    [
                        "2-1":2.1,
                        "2-2":2.2
                ],
                "3":3
            ]
            ColorLog.lightRed(log: "jsonObj(): \(jsonObj)")
            
            
            let fm = FileManager.default
            ColorLog.lightRed(log: "NSHomeDirectory(): \(NSHomeDirectory())")
            
            let root = URL(fileURLWithPath: NSHomeDirectory())
            ColorLog.lightRed(log: "absoluteString: \(root.absoluteString)")
            ColorLog.lightRed(log: "fragment: \(root.fragment)")
            ColorLog.lightRed(log: "path: \(root.path)")
            
            let rootContent = try fm.contentsOfDirectory(atPath: root.path)
            ColorLog.lightRed(log: "rootContent: \(rootContent)")
            let path = "file://" + NSHomeDirectory() + "/Documents/projects/04eaf489-221b-43d1-94e3-324ac3bd5c87/profile.json"
            let fixpath = ("file://" + NSHomeDirectory() + "/Documents/projects/04eaf489-221b-43d1-94e3-324ac3bd5c87/profile.json").removingRegexMatches(pattern: "^file:/+")
            ColorLog.cyan(log: "path: \(path)")
            ColorLog.cyan(log: "fixpath: \(fixpath)")
            let nsurl = NSURL(string: path)!
            let fixnsurl = NSURL(string: fixpath)!

            ColorLog.cyan(log: "absoluteString: \(nsurl.path ?? "")")
            ColorLog.cyan(log: "absoluteString: \(nsurl.absoluteString ?? "")")
            ColorLog.yellow(log: "absoluteString: \(fixnsurl.path ?? "")")
            ColorLog.yellow(log: "absoluteString: \(fixnsurl.absoluteString ?? "")")
            
            let test = fm.fileExists(atPath: nsurl.path!)
            
            ColorLog.lightRed(log: "fileExists: \(test)")
            
            let data: Data = try NSData(contentsOfFile: nsurl.path!) as Data
            ColorLog.lightBlue(log: "data: \(data)")
            var obj1:JSON = try JSON(data: data)
                
            ColorLog.lightBlue(log: "\(obj1)")
            
            ColorLog.yellow(log: "GET STRING: \(obj1["fileFormatVersion"])")
            obj1["add"] = "testAdd"
            try obj1.rawString()?.write(to: nsurl as URL, atomically: false, encoding: .utf8)
            
            
            let data2: Data = try NSData(contentsOfFile: nsurl.path!) as Data
            ColorLog.lightYellow(log: "data: \(data2)")
            var obj2:JSON = try JSON(data: data2)
            
            ColorLog.lightYellow(log: "\(obj2)")
            
            obj2["add"] = JSON.null
            
            ColorLog.lightYellow(log: "\(obj2)")
            
            obj2.dictionaryObject?.removeValue(forKey: "add")
            ColorLog.lightYellow(log: "\(obj2)")


        }catch{
            ColorLog.red(log: "> catch error")
        }
        
    }
}
