//
//  NativeCanvasPenpalBookshelf.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation

/*
 NativeCanvasPenpalBookshelf Object實作
 */
public class NativeCanvasPenpalBookshelf{
    
    internal var selectedBook:NativeCanvasPenpalBook?
    internal var bookshelfID:String?
    internal var bookshelfPath:String?
    private var library:NativeCanvasPenpalLibrary
    private var bookshelfFolder:NSURL?
    private var bookshelfProfile:NSURL?
    private var bookshelfJson:JSON?
    private var Books:[String:NativeCanvasPenpalBook?]? = [String:NativeCanvasPenpalBook?]()
    private var isInit:Bool = false
    
    init(library:NativeCanvasPenpalLibrary, bookshelfID:String){
        self.bookshelfID = bookshelfID;
        self.library = library;
        self.bookshelfPath = library.libraryPath! + "/" + bookshelfID;
        _ = self.initialize()
    }
    
    private func initialize()-> Bool{
        if(!isValid()) {
            destroy();
            return false;
        }else{
            return load();
        }
    }
    
    public func isValid()->Bool{
        if(!isInit){
            if let folderUri:NSURL = bookshelfPath?.getFolderURL(){
                bookshelfFolder = folderUri
            }
            isInit = true;
        }
        // ColorLog.green(log: "Bookshelf isValid \(bookshelfFolder != nil)")
        return bookshelfFolder != nil
    }

    public func hasBook( bookID:String?)->Bool {
        if let id = bookID, let book = selectedBook{
            if(book.bookID == id){
                // Do nothing as library = selectedLibrary
                ColorLog.red(log: "> selectedBook is current")
            }else{
                selectedBook = createBook(id: bookID!)
            }
        }else{
            selectedBook = createBook(id: bookID!)
        }
        return selectedBook != nil;
    }
    
    public func hasBook(book:String?,type:BOOK_TYPE?)->Bool {
        let result:Bool = hasBook(bookID: book);
        if(result){
            getSelectedBook().setType(type: type!)
        }
        return result;
    
    }
    
    public func getSelectedBook()->NativeCanvasPenpalBook {
        return selectedBook!;
    }
    
    private func createBook(id:String)->NativeCanvasPenpalBook?{
        var book:NativeCanvasPenpalBook?

        if var books = Books{
            if let storedBook = books[id]{
                // set selectedBookshelf from Library get path
                book = storedBook
                ColorLog.red(log: "> selectedBook from Books " + id)
            }else{
                let newBook = NativeCanvasPenpalBook(bookshelf: self, bookID: id)
                if(newBook.isValid()){
                    books[id] = newBook
                    // set selectedBook from new Library path
                    book = newBook
                    ColorLog.red(log: "> selectedBook from NEW " + id)
                }else{
                    ColorLog.red(log: "> selectedBook NOT FOUND")
                }
            }
        }else{
            ColorLog.red(log: "> selectedBook Book NOT FOUND")
        }
        return book
    }
    
    public func save()->Bool {
        var result:Bool = false;
        if (isValid()) {
            if let json = bookshelfJson{
                if let nsurl = bookshelfProfile{
                    do{
                        let fm = FileManager.default
                        if fm.isWritableFile(atPath: nsurl.path!) && !fm.fileExists(atPath: nsurl.path!){
                            if fm.createFile(atPath: nsurl.path!, contents: nil, attributes: nil){
                                ColorLog.lightBlue(log: ">Bookshelf  save, create file \(nsurl.path!)...")
                            }
                        }
                        //                            let url = try URL(fileURLWithPath: "file://" + nsurl.path!)
                        ColorLog.lightBlue(log: "> Bookshelf save to \(nsurl.path!)...")
                        try json.rawString()?.write(to: nsurl as URL, atomically: false, encoding: .utf8)
                        ColorLog.lightBlue(log: "> Bookshelf save to \(nsurl.path!)...")
                        result = true
                    }catch (let e as NSError){
                        result = false
                        ColorLog.red(log: "> Bookshelf catch save error \(e.localizedDescription)")
                    }
                }
            }else{
                ColorLog.lightRed(log: "> Bookshelf save to JSON invalid")
            }
        }
        return result;
    }
    
    public func load()->Bool {
        if (isValid()) {
            do {
                bookshelfJson = createProfile()
                ColorLog.lightYellow(log: "> Bookshelf loaded from default ... \(bookshelfJson)")
                if let url = bookshelfProfile{
                    if let path = url.path{
                        if let data = NSData(contentsOfFile: path){
                            let loadJson = JSON(data: data as Data)
                            ColorLog.lightYellow(log: "> Bookshelf loaded from disk... \(loadJson)")
                            try bookshelfJson?.merge(with: loadJson)
                            ColorLog.lightYellow(log: "> Bookshelf loaded merge... \(bookshelfJson ?? "")")
                        }
                    }
                }
                // TODO: hashcode to verify saving
                _ = save()
                ColorLog.lightYellow(log: "> Bookshelf loaded... \(bookshelfJson ?? "")")
            } catch  {
                ColorLog.red(log: "> Bookshelf catch loaded error")
                bookshelfJson = createProfile()
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

    }
    
    private func cleanBooks() {
//    for(PenpalBook book: books.values()){
//    book.destroy();
//    }
//    books.clear();
    }
    
    
}
