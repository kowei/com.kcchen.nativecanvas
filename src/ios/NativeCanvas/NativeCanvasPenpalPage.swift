//
//  NativeCanvasPenpaPage.swift
//  PenPal
//
//  Created by GUAN BEI-FONG on 2018/4/26.
//

import Foundation


public class NativeCanvasPenpalPage{
    private let PAGE_PROFILE:String             = "drawing.json";
    private let PAGE_THUMBNAIL:String           = "thumbnail.png";
    
    public let PAGE_CONTAINER:String            = "containerInfo";
    public let PAGE_TEMPLATE:String             = "template";
    public let PAGE_BACKGROUND:String           = "background";
    public let PAGE_BACKGROUND_PATH:String      = "source";
    public let PAGE_BACKGROUND_PATH_TYPE:String = "sourceType";
    public let PAGE_FORMAT_VERSION:String       = "fileFormatVersion";
    public let PAGE_DURATION:String             = "duration";
    public let PAGE_SEGMENT_A:String            = "segmentA";
    public let PAGE_SEGMENT_B:String            = "segmentB";
    public let PAGE_COLLECTED_ACTIONS:String    = "collectedActions";
    public let PAGE_PLATFORM_INFO:String        = "platformInfo";
    public let PAGE_ASSETS_CONFIG:String        = "assetsConfig";
    
    internal var id:String?
    internal var pagePath:String?
    internal var pageProfilePath:String?
    internal var thumbnailPath:String?
    private var book:NativeCanvasPenpalBook
    private var pageFolder:NSURL?
    private var pageProfile:NSURL?
    private var pageJson:JSON?
    private var isInit:Bool = false
    private var backgroundPath:String?
    private var templatePath:String?
    private var index:Int?
    private var hash:Int?
    private var pageFile:NSURL?
    private var thumbnailFile:NSURL?
    
    let pngView = UIView()
    
    
    init(book:NativeCanvasPenpalBook, pageID:String){
        self.id = pageID;
        self.book = book;
        self.pagePath = book.bookPath! + "/" + id!;
        self.pageProfilePath = pagePath! + "/" + PAGE_PROFILE;
        self.thumbnailPath = pagePath! + "/" + PAGE_THUMBNAIL;
        self.initialize()
        ColorLog.cyan(log: "Page initialize pagePath is \(pagePath)")
        // self.hasBackground()
        // self.getBackground()
    }
    
    private func initialize()-> Bool{
        ColorLog.cyan(log: "Page initialize")
        if(!isValid()) {
            destroy();
            return false;
        }else{
//            make();
            return load();
        }
    }
    public func setIndex(index:Int)-> Int{
        self.index = index
        return self.index!
    }
    
    public func getBook()->NativeCanvasPenpalBook {
        return book;
    }
    public func isValid()->Bool{
        ColorLog.cyan(log: "Page isValid")
//        ColorLog.cyan(log: "Page pageFolder is  \(pagePath) \(pagePath?.getFolderURL())")
//        ColorLog.cyan(log: "page Profile is  \(pageProfilePath) \(pageProfilePath?.getFileURL())")
        if(!isInit){
            
            if let folderUri:NSURL = pagePath?.getFolderURL(){
                pageFolder = folderUri
                
            }else{
                ColorLog.cyan(log: "Page pageFolder is  \(pagePath)")
                let directoryPath = pagePath
                // 用FileManager產生資料夾
                do {
                    try FileManager.default.createDirectory(atPath: directoryPath!, withIntermediateDirectories: true, attributes: nil)
                } catch let error as NSError {
                    print(error.description)
                }
            }
           if let fileUri:NSURL = pageProfilePath?.getFileURL(){
                pageProfile = fileUri
           }else{
            ColorLog.cyan(log: "Page pageProfile is  \(pageProfile)")
            }
            
            isInit = true;
        }
        // ColorLog.green(log: "Page isValid \(pageFolder != nil)")
        return pageFolder != nil && pageProfile != nil
    }
    
    public func make()-> Bool{
//        if(isValid()){
            if let folderUri:NSURL = pagePath?.getFolderURL(){
                do{
                    let nsurl = folderUri
                    let fm = FileManager.default
                    if !fm.isWritableFile(atPath: nsurl.path!) && !fm.fileExists(atPath: nsurl.path!){
                        if fm.createFile(atPath: nsurl.path!, contents: nil, attributes: nil){
                            ColorLog.lightBlue(log: "> Page Make, create file \(nsurl.path!)...")
                        }
                        
                    }
                    
                }catch (let e as NSError){
                    
                    ColorLog.red(log: "> Page Make catch save error \(e.localizedDescription)")
                    
                }
                
            }
//        }
        return initialize();
    }
    
    public func isUpdate()-> Bool{
        //        do {
        //            if let json = pageJson{
        //                let json1:JSON = []
        //                json1["a"]=1
        //
        //                let hash = json.rawString()?.hashValue
        //            }
        //        }catch {
        //
        //        }
        return (self.hash != nil);
    }
    
    public func getPageProfile()-> NSURL{
        
        return pageProfile!
        
    }
    public func setTemplate(id:String) {
        ColorLog.lightBlue(log: "Page setTemplate:  id \(id)")
        if(pageJson != nil) {
            var container:JSON = [];
            pageJson![PAGE_CONTAINER] = container["containerInfo"][PAGE_TEMPLATE]
            container["containerInfo"][PAGE_TEMPLATE] = JSON(id)
        if(container["containerInfo"][PAGE_TEMPLATE] == nil) {
            container["containerInfo"][PAGE_TEMPLATE] = JSON(id)
            }
        }
    }
    
    private func hasBackground()-> Bool{
        if(pageProfile != nil){
//                        ColorLog.green(log: "Page pageProfile")
            if(backgroundPath == nil){
//                        ColorLog.green(log: "Page backgroundPath")
                let container:JSON = [];
                if(container != nil){
//                        ColorLog.green(log: "Page container")
                    let background:JSON = [];
                    if(background != nil){
//                        ColorLog.green(log: "Page background")
                        do {
                            let data: Data = try NSData(contentsOfFile: pageProfilePath!) as Data
                            var obj1:JSON = try JSON(data: data)
                            backgroundPath = obj1["containerInfo"]["background"]["source"].rawString()
                        }catch{
                            
                        }
                    }
                }
            }
            
            if(backgroundPath == nil){
                backgroundPath = "";
            }
        }else{
            backgroundPath = "";
        }
        
        return backgroundPath != nil
        
    }
    
    private func hasTemplate()-> Bool{
        if let library = NativeCanvasPenpalLibrary.getCurrentLibrary() {
            if library.getTemplateManager() != nil{
                if pageJson != nil{
                    if templatePath != nil{
                        var container:JSON = JSON()
                        
                        do {
                            let data: Data = try NSData(contentsOfFile: templatePath!) as Data
                            var obj1:JSON = try JSON(data: data)
                            templatePath = obj1["containerInfo"]["template"].rawString()                        }catch{
                            
                        }
                         if container != nil {
                            let templateId:String = container[PAGE_TEMPLATE].string!
                            if(templateId != nil){
                                let template:NativeCanvasPenpalTemplate = NativeCanvasPenpalLibrary.getCurrentLibrary()!.getTemplateManager()!.getTemplate(source: templateId)!;
                                if(template != nil && template.isValid()){
                                    templatePath = (NativeCanvasPenpalLibrary.getCurrentLibrary()?.getTemplateManager()?.getTemplatePath(templateLocation: templateId))! + "/" + template.getBackground();
                                }
                            }
                        }
                                if(templatePath == nil){
                                    templatePath = "";
                                }else{
                                templatePath = "";
                            }
                        }
                    }
                }
                return templatePath != nil;
            }
        return false;
    }
    
    public func save()->Bool {
        var result:Bool = false;
        if (isValid()) {
            do {
                if let json = pageJson{
                    let urlString:String = pageProfile!.relativePath!
                    let url:String = "file:///" + urlString
//                    ColorLog.lightBlue(log: ">Page  save, path is  \(url)...")
                    if let nsurl = url.url{
//                    if let nsurl = pageProfile{
                        do{
                            let fm = FileManager.default
                            if fm.isWritableFile(atPath: nsurl.path) && !fm.fileExists(atPath: nsurl.path){
                                if fm.createFile(atPath: nsurl.path, contents: nil, attributes: nil){
                                    ColorLog.lightBlue(log: ">Page  save, create file \(nsurl.path)...")
                                }
                            }
                            //                            let url = try URL(fileURLWithPath: "file://" + nsurl.path!)
                            ColorLog.lightBlue(log: "> Page save to \(nsurl.path)...")
                            try json.rawString()?.write(to: nsurl as URL, atomically: false, encoding: .utf8)
                            ColorLog.lightBlue(log: "> Page save to \(nsurl.path)...")
                            result = true
                        }catch (let e as NSError){
                            result = false
                            ColorLog.red(log: "> Page catch save error \(e.localizedDescription)")
                        }
                    }
                }else{
                    ColorLog.lightRed(log: "> Page save to JSON invalid")
                }
            } catch (let e as NSError){
                result = false
                ColorLog.red(log: "> Page catch save error \(e.localizedDescription)")
            }
        }
        return result;
    }
    
    public func load()->Bool {
        if (isValid()) {
            do {
                pageJson = createProfile()
                ColorLog.lightYellow(log: "> Page loaded from default ... \(pageJson)")
                if let url = pageProfile{
                    if let path = url.path{
                        let data = try NSString(contentsOfFile: path, encoding: String.Encoding.utf8.rawValue)
                        let loadJson = JSON(string: data as String)
                        ColorLog.lightYellow(log: "> Page loaded from disk... \(loadJson)")
                        try pageJson?.merge(with: loadJson)
                        ColorLog.lightYellow(log: "> Page loaded merge... \(pageJson ?? "")")
                    }
                }
                // TODO: hashcode to verify saving
                save()
                ColorLog.lightYellow(log: "> Page loaded... \(pageJson ?? "")")
            } catch  {
                ColorLog.red(log: "> Page catch loaded error")
                pageJson = createProfile()
                save()
                return false;
            }
            return true;
        }
        return false;
    }
    
    public func getBackground(size:CGSize?)->UIImage?{
        ColorLog.lightBlue(log:"> Page getBackground Start")
        var image:UIImage?
        if pageProfile == nil {
            _ = load();
        }
        if hasTemplate() {
            let type:ASSET_TYPE = ASSET_TYPE.parse(file: templatePath!)
            ColorLog.lightBlue(log: "> Page getBackground ....hasTemplate.....type: \(type) filename:  \(templatePath)  pageFolder: \(pageFolder) ")
            
            switch type {
                
            case .PNG:
                ColorLog.lightBlue(log: "> Page getBackground ....hasTemplate..PNG...>>>>.type: \(type))")
                break
            case .PDF:
                
                break
            case .JPEG:
                
                break
            case .GIF:
                
                break
            case .SVG:
                let SVGPath = book.bookPath! + "/" +  backgroundPath!
                if let nsurl = SVGPath.getFileURL(){
                    let urlString:String = nsurl.relativePath!
                    
                    if let path = urlString.addScheme().url{
                        ColorLog.lightPurple(log: ">Page svg path \(path) \(urlString.fileExist())")
                        // file:///var/mobile/Containers/Data/Application/67A213A2-0931-4B3D-B54E-045B5D77412F/Documents/projects/2229a77c-96d9-43b9-9024-24ad2f4ddaad/assets/bg/pdf_0.svg
                        //                        if let svg:SVGKImage = SVGKImage(contentsOf: path){
                        //                            if let scaleSize = size{
                        //                                ColorLog.lightPurple(log: "> svg change size \(scaleSize)")
                        //                                svg.scaleToFit(inside: scaleSize)
                        //                            }
                        //                            image = svg.uiImage
                        //                            ColorLog.lightPurple(log: "> svg get image \(image) \(image?.bytesSize)")
                        //                        }
                    }
                }else{
                    ColorLog.lightPurple(log: ">Page svg can not find file")
                }
                break
            case .UNKNOWN:
                
                break
            case .PNG_64:
                
                break
            case .JPEG_64:
                
                break
            case .GIF_64:
                
                break
            case .SVG_64:
                
                break
            }
    }else if hasBackground() {
            let type:ASSET_TYPE = ASSET_TYPE.parse(file: backgroundPath!)
            ColorLog.lightBlue(log: "> Page getBackground .hasBackground........type: \(type) filename:  \(backgroundPath)  pageFolder: \(pageFolder) ")
            
            switch type {
                
            case .PNG:
                
                break
            case .PDF:
                
                break
            case .JPEG:
                
                break
            case .GIF:
                
                break
            case .SVG:
                let SVGPath = book.bookPath! + "/" +  backgroundPath!
                if let nsurl = SVGPath.getFileURL(){
                    let urlString:String = nsurl.relativePath!

                    if let path = urlString.addScheme().url{
                        ColorLog.lightPurple(log: ">Page svg path \(path) \(urlString.fileExist())")
// file:///var/mobile/Containers/Data/Application/67A213A2-0931-4B3D-B54E-045B5D77412F/Documents/projects/2229a77c-96d9-43b9-9024-24ad2f4ddaad/assets/bg/pdf_0.svg
//                        if let svg:SVGKImage = SVGKImage(contentsOf: path){
//                            if let scaleSize = size{
//                                ColorLog.lightPurple(log: "> svg change size \(scaleSize)")
//                                svg.scaleToFit(inside: scaleSize)
//                            }
//                            image = svg.uiImage
//                            ColorLog.lightPurple(log: "> svg get image \(image) \(image?.bytesSize)")
//                        }
                    }
                }else{
                    ColorLog.lightPurple(log: ">Page svg can not find file")
                }
                break
            case .UNKNOWN:
                
                break
            case .PNG_64:
                
                break
            case .JPEG_64:
                
                break
            case .GIF_64:
                
                break
            case .SVG_64:
                
                break
            }
        }
        return image
    }
    
    func getImageFromWeb(_ urlString: String, closure: @escaping (UIImage?) -> ()) {
        guard let url = URL(string: urlString) else {
            return closure(nil)
        }
        let task = URLSession(configuration: .default).dataTask(with: url) { (data, response, error) in
            guard error == nil else {
                print("error: \(String(describing: error))")
                return closure(nil)
            }
            guard response != nil else {
                print("no response")
                return closure(nil)
            }
            guard data != nil else {
                print("no data")
                return closure(nil)
            }
            DispatchQueue.main.async {
                closure(UIImage(data: data!))
            }
        }; task.resume()
    }

    func getDataFromUrl(url: URL, completion: @escaping (Data?, URLResponse?, Error?) -> ()) {
        URLSession.shared.dataTask(with: url) { data, response, error in
            completion(data, response, error)
            }.resume()
    }
    
    func downloadImage(url: URL) {
        print("Download Started")
        getDataFromUrl(url: url) { data, response, error in
            guard let data = data, error == nil else { return }
            print(response?.suggestedFilename ?? url.lastPathComponent)
            print("Download Finished")
            DispatchQueue.main.async() {
//                self.imageView.image = UIImage(data: data)
            }
        }
    }
 /*
    
    extension UIImageView {
        func downloadedFrom(url: URL, contentMode mode: UIViewContentMode = .scaleAspectFit) {
            contentMode = mode
            URLSession.shared.dataTask(with: url) { data, response, error in
                guard
                    let httpURLResponse = response as? HTTPURLResponse, httpURLResponse.statusCode == 200,
                    let mimeType = response?.mimeType, mimeType.hasPrefix("image"),
                    let data = data, error == nil,
                    let image = UIImage(data: data)
                    else { return }
                DispatchQueue.main.async() {
                    self.image = image
                }
                }.resume()
        }
        func downloadedFrom(link: String, contentMode mode: UIViewContentMode = .scaleAspectFit) {
            guard let url = URL(string: link) else { return }
            downloadedFrom(url: url, contentMode: mode)
        }
    }*/
    private func createProfile()->JSON {
        
//        var container:JSON = JSON("{}")
//        var background:JSON = JSON("{}")
//        let collectedActions:JSON = JSON("{}")
//        let platformInfo:JSON = JSON("{}")
//        let assetsConfig:JSON = JSON("{}")
        var profile:JSON = JSON()
        
        profile[PAGE_FORMAT_VERSION] = "v1.0"
        profile[PAGE_DURATION] = 0
        profile[PAGE_SEGMENT_A] = 0
        profile[PAGE_SEGMENT_B] = 0
        profile[PAGE_PLATFORM_INFO] = JSON("{}")
        profile[PAGE_CONTAINER][PAGE_TEMPLATE] = JSON("")
        profile[PAGE_CONTAINER][PAGE_BACKGROUND][PAGE_BACKGROUND_PATH] = JSON("")
        profile[PAGE_CONTAINER][PAGE_BACKGROUND][PAGE_BACKGROUND_PATH_TYPE] = JSON("")
        profile[PAGE_COLLECTED_ACTIONS] = JSON("{}")
        profile[PAGE_ASSETS_CONFIG] = JSON("{}")
        /*
         pageJson():Optional({
         "segmentB" : 0,
         "fileFormatVersion" : "v1.0",
         "assetsConfig" : "{}",
         "platformInfo" : "{}",
         "containerInfo" : {
         "template" : "",
         "background" : {
         "sourceType" : "",
         "source" : ""
         }
         },
         "collectedActions" : "{}",
         "duration" : 0,
         "segmentA" : 0
         })
         */
        ColorLog.yellow(log: "page profile: \(profile)")
        return profile
    }
    
    public func clearBitmap(){
        //    if(bitmap != null && !bitmap.isRecycled()){
        //    bitmap.recycle();
        //    }
        //    bitmap = null;
    }
    
    public func destroy() {
        isInit = true;
        pageProfile = nil;
        pagePath = nil;
        index = 0;
        hash = -1;
        thumbnailPath = nil;
        backgroundPath = nil;
        clean();
        release();
    }
    public func release(){
        clearBitmap();
    }
    
    public func clean() {
        do {
            if(pageProfile != nil){
                pageProfile?.removeAllCachedResourceValues()
                //            while (pageProfile.keys().hasNext()) {
                //            pageProfile.put(pageProfile.keys().next(), null);
                //            }
                hash = 0;
                pageProfile = nil;
                pageFolder = nil;
                thumbnailPath = nil;
            }
            
        } catch {
            
        }
    }
    
    public func hasThumbnail()-> Bool{
        return thumbnailFile != nil && (thumbnailFile?.isFileURL)!;
    }
    public func getProfilePath()-> String{
        return pagePath! + "/" + PAGE_PROFILE;
    }
    
    public func getThumbnailPath()-> String{
        return pagePath! + "/" + PAGE_THUMBNAIL;
    }
    
    public func getPageFile()-> NSURL{
        return pageFile!;
    }
    
    public func getThumbnailFile()-> NSURL{
        return thumbnailFile!;
    }
    
    public func getPagePath()-> String{
        return pagePath!;
    }
    
}
