//
//  NativeCanvasPenpalAssetManager.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation

/*
 NativeCanvasPenpalAssetManager Object實作
 */
public class NativeCanvasPenpalAssetManager{

    private let ASSET_PROFILE:String = "asset.json";
    public static let ASSET_FOLDER:String = "assets";
    private let ASSET_CLEAN_PERIOD_DEFAULT:CLong = 604800000;
    
    internal var assetPath:String = "";
    internal var assetProfilePath:String = "";
    internal var bookPath:String?
    internal var book:NativeCanvasPenpalBook?
    private var assetProfile:NSURL?
    private var assetFolder:NSURL?
    private var isInit:Bool = false;
    private var assets:[String:NativeCanvasPenpalAsset?]? = [String:NativeCanvasPenpalAsset?]()
    private var assetJson:JSON? = JSON()
    
    private let IMAGEFILE_TYPE:String = "ImagefileType";
    private let IMAGEFILE_FILE:String = "ImagefileFile";
    private var type:ASSET_TYPE?
    private var file:NSURL?
//    private var exportData:JSON = JSON()
//    private var bitmap:UIImage;
//    private Object exportData;
//    private NeuralStyle neuralStyle;
    private let rootＤirectory = NSHomeDirectory()+"/Documents/"
    init(book:NativeCanvasPenpalBook){
        self.book = book;
        if let bookPath = book.bookPath{
            self.assetPath = bookPath + "/" + NativeCanvasPenpalAssetManager.ASSET_FOLDER
            self.assetProfilePath = bookPath + "/" + NativeCanvasPenpalAssetManager.ASSET_FOLDER + "/" + ASSET_PROFILE
        }
        _ = self.initializes();
//        copyFile(isFromTemp: true, filename: "library.json", sourceFile: rootＤirectory, destFile: assetPath)
//        processAsset(assetManager: "")
//        isExportToText()
//        isExportToImage()
    }

    private func initializes()-> Bool{
        ColorLog.cyan(log: "AssetManager initialize")
       if(!isValid()) {
            destroy();
            return false;
        }else{

           return load();
        }
    }
    
    public func NCImageFile(type:ASSET_TYPE, filepath:String) {
    self.type = type;
        InitImagePath(filepath: filepath);
    }
    
    public func NCImageFile(data:JSON) {
//    importData(data: data)
    }
    
    private func InitImagePath(filepath:String) {
//        if(neuralStyle == null && activity != null) neuralStyle = new NeuralStyle(activity);
        
        if(filepath.length > 0) {
            if let fileUri:NSURL = filepath.getFileURL(){
               file = fileUri
            }
        }
        
        getBitmap();
    }
    public func ImageFileisValid()-> Bool{
    // Log.w(TAG, "NCImageFile "
    //         + "\n file:" + file
    //         + "\n exists:" + (file == null ? "" : file.exists())
    //         + "\n canRead:" + (file == null ? "" : file.canRead())
    //         + "\n isFile:" + (file == null ? "" : file.isFile())
    // );
//        return (file != nil && ((file?.filePathURL) != nil) && file.canRead() && file.isFile()) || type.isEmbed();
        return true
    }
    public func isValid()-> Bool{
        if(!isInit){
            if let folderUri:NSURL = assetPath.getFolderURL(){
                assetFolder = folderUri
            }
            if let fileUri:NSURL = assetProfilePath.getFileURL(){
                assetProfile = fileUri
            }
            isInit = true;
        }
        return assetProfile != nil && assetFolder != nil
    }
    
    public func getAssetFile(asset:NativeCanvasPenpalAsset)-> NSURL{
        let assetpath = NSURL(fileURLWithPath: getAssetPath(assetLocation:asset.getKey()))
        return assetpath
    }
    
    public func getAsset(source:String)-> NativeCanvasPenpalAsset{
//        var source:NativeCanvasPenpalAsset
                let assestsource =  assets![source]
        
        return assestsource as! NativeCanvasPenpalAsset
    }
    
    public func getAssetPath(assetLocation:String)-> String{
        var AssetAllPath:String? = assetLocation

        if((assetLocation.length) > 0) {
            return assetPath + "/" + assetLocation;
        }else{
            return bookPath! + "/" + NativeCanvasPenpalAssetManager.ASSET_FOLDER;
        }
       
        return AssetAllPath!;
    }
    
    public func getAssetProfileFile()-> NSURL{
        return assetProfile!;
    }
   
    public func getProfilePath()-> String{
        return assetPath + "/" + NativeCanvasPenpalAssetManager.ASSET_FOLDER + "/" + ASSET_PROFILE;
    }
    
    public func getBook()-> NativeCanvasPenpalBook{
        
        return self.book!;
    }
    
    public func getBookPath()-> String{

        return bookPath!;
    }
    
    public func getAssetMD5(source:String)-> String{
        let asset:NativeCanvasPenpalAsset = getAsset(source: source)
        if (asset != nil) {
            return asset.getFileMD5();
        }
        return "nil";
    }
    
    public func getAssetByMD5(md5:String)-> String{
        if (assets != nil){
            let asset:NativeCanvasPenpalAsset?
            var ajmd5 = assetJson!["AssetList"].dictionaryValue
            let key = Array(ajmd5.keys)
            let values = Array(ajmd5.values)
            for x in 0...values.count-1{
                for (_, value) in values[x] {
                    
                    if value.string == md5{
//                        print("Item \(index): \(value)")
//                        print("MD5: \(value.string))")
//                        print("ALL: \(Array( values[x].dictionaryValue))")
                       let all = Array(values[x].dictionaryValue)
                       let AssetFileName = all[1].value
                       let AssetLocation = all[3].value
                       let source = AssetLocation.string! + "/" + AssetFileName.string!
                       ColorLog.green(log:"> getAssetByMD5 source : \(source)")
                        return source
                        }
                }
            }
        }
        
    return "nil";
    }
    
    public func getFileName(bookName:String)-> String{
//        let ext:String = type.key().replace("_64","");
        let ext:String = "test"
//        let ext:String = (type?.rawValue.removingRegexMatches(pattern: "_64", replaceWith: ""))!
                let now = Date()
                // 创建一个日期格式器
                let dformatter = DateFormatter()
                dformatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss"
        
                print("当前日期时间：\(dformatter.string(from: now))")
        
    return NativeCanvasPenpalAssetManager.ASSET_FOLDER + "/" + bookName + "_" + dformatter.string(from: now) + "." + ext;
    }
    
    public func isExportToImage()-> Bool{
        if exportData != nil{
           let aa = Array(exportData().dictionaryValue)
//            ColorLog.green(log: "isExportToImage \(aa[0]) \(aa[1])")
            if aa[1] != nil{
                return true;
                }
            }
        return false;
    }
    
    public func isExportToText()-> Bool{
        if exportData != nil{
            let aa = Array(exportData().dictionaryValue)
//            ColorLog.green(log: "isExportToText \(aa[0]) \(aa[1])")
            if aa[0] != nil{
                return true;
            }
        }
        return false;
    }
    
    
    public func isCopyFile()-> Bool{
        if(exportData != nil && exportData().exists() ){ // instanceof File){
            return true;
        }
        return true;
    }
    
    public func save()-> Bool{
        var result:Bool = false;
        if (isValid()) {
            do {
                if let json:JSON = assetJson{
                    let urlString:String = assetProfile!.relativePath!
                    let url:String = "file:///" + urlString
                    if let nsurl = url.url{
                        do{
                            let fm = FileManager.default
                            if fm.isWritableFile(atPath: nsurl.path) && !fm.fileExists(atPath: nsurl.path){
                                if fm.createFile(atPath: nsurl.path, contents: nil, attributes: nil){
                                    ColorLog.lightBlue(log: "> AssetManager save, create file \(nsurl.path)...")
                                }
                            }
                            ColorLog.lightBlue(log: "> AssetManager save to \(nsurl.path)...")
                            try json.rawString()?.write(to: nsurl as URL, atomically: false, encoding: .utf8)
                            ColorLog.lightBlue(log: "> AssetManager save to \(nsurl.path)...")
                            result = true
                        }catch (let e as NSError){
                            ColorLog.red(log: "> AssetManager save, error the path is \(nsurl.path)...")
                            result = false
                            ColorLog.red(log: "> AssetManager catch save error \(e.localizedDescription)")
                        }
                    }
                }else{
                    ColorLog.lightRed(log: "> AssetManager save to JSON invalid")
                }
            } catch (let e as NSError){
                result = false
                ColorLog.red(log: "> AssetManager catch save error \(e.localizedDescription)")
            }
        }
        return result;
    }
    
    public func load()-> Bool{
        if (isValid()) {
            do {
                createProfile()
                ColorLog.lightYellow(log: "> AssetManager loaded from default ... \(String(describing: assetJson))")
                if let url = assetProfile{
                    if let path = url.path{
                        if let data = NSData(contentsOfFile: path){
                            var loadJson = try JSON(data: data as Data)
                            var list:JSON = loadJson[NativeCanvasPenpalAsset.ASSET_LIST]
                            
                            ColorLog.lightYellow(log: "> AssetManager list... \(list )")
                            ColorLog.lightYellow(log: "> AssetManager loaded from disk... \(loadJson )")
                            try assetJson?.merge(with: loadJson)
                            ColorLog.lightYellow(log: "> AssetManager loaded merge... \(assetJson)")

//                            int hash = Utility.getJsonHash(list);
                            let hash = (list.rawString()?.MD5())!

                            if (list == nil) {
                                list = JSON()
                            }
                            // 更新asset list，跟檔案系統同步
                            assets = updateAssets();///
                            ColorLog.lightYellow(log: "> AssetManager loaded hash... \(hash)")
                            ColorLog.lightYellow(log: "> AssetManager loaded hash 2... \(assetJson![NativeCanvasPenpalAsset.ASSET_LIST].rawString()?.MD5())")
                            if assetJson![NativeCanvasPenpalAsset.ASSET_LIST].rawString()?.MD5() != hash {
                                assetJson![NativeCanvasPenpalAsset.ASSET_LIST] = list
                                save();
                            }

                        }
                    }
                }
            } catch  {

            }
            return true;
        }
        return false;
    }
    
    private func createProfile(){
        assets = updateAssets();
        ColorLog.blue(log: "> createProfile-assetJson is  -\(assetJson!["AssetList"]))")
//      getAssetsJson()
        if assetJson == JSON.null{
            assetJson = JSON()
            var assetList:JSON = JSON()
            assetJson![NativeCanvasPenpalAsset.ASSET_CLEAN_PERIOD] = JSON(ASSET_CLEAN_PERIOD_DEFAULT)
            assetJson![NativeCanvasPenpalAsset.ASSET_LIST] = assetList
            assetList[NativeCanvasPenpalAsset.ASSET_LIST] = getAssetsJson()
//            save()
        }
        save()
        
    }
    
    private func getAssetsJson()-> JSON{
        var jsonObject:JSON = JSON()
        if(assets != nil){
            for (key, _) in assetJson!["AssetList"] {
                jsonObject[key] = assetJson!["AssetList"][key]
            }
        }
            return jsonObject
    }

    private func updateAssets()-> [String:NativeCanvasPenpalAsset?]?{
        
        var list:JSON = JSON()
        
        if var profile = assetJson{
            list = profile[NativeCanvasPenpalAsset.ASSET_LIST]
        }
        ColorLog.yellow(log: "> updateAssets profile = assetJson \(list)")

        var assets:[String:NativeCanvasPenpalAsset?] = [String:NativeCanvasPenpalAsset?]()
        
        // remove entry from list if no entity found on file system
        if (list != JSON.null && list.count > 0) {
            for (path, _) in list{
                if let _ = path.getFileURL(){
                }else{
                    _ = list.removeByKey(key: path)
                }
            }
        }

        // add entry to list if list has no entry
        let files = readAssetsFolder(underFolder: NativeCanvasPenpalAssetManager.ASSET_FOLDER, folderString: assetPath)
        for item in files {
            var asset:NativeCanvasPenpalAsset!
            ColorLog.green(log: "> readAssetsFolder  path \(item.key) \(item.value)")
            var entry = list[item.key]
            if entry != JSON.null{
                let md5String = entry[NativeCanvasPenpalAsset.ASSET_FILE_MD5].string
                if let md5 = md5String{
                    if md5 != item.value{
                        // 更新 MD5
                        entry[NativeCanvasPenpalAsset.ASSET_FILE_MD5].string = item.value
                    }
                }
                asset = NativeCanvasPenpalAsset(key: item.key, asset: entry)
                list[item.key] = entry // not sure if entry updated and list updated too, asign again to set new value
            }else{
                // 建立新的 entry
                asset = getNewAsset(path: item.key, md5: item.value)
                list[asset.getKey()] = asset.getAsset()

            }
            assets[item.key] = asset
        }
        assetJson![NativeCanvasPenpalAsset.ASSET_LIST] = list
        ColorLog.yellow(log: "> updateAssets assets \(assets)")
//        getAssetByMD5(md5: "D0924B7627E88247529A0B1F60B325FC")
        return assets
    }

    public func getNewAsset(path:String, md5:String)->NativeCanvasPenpalAsset {
    
        let fileFullname:String =  path.lastPathComponent
        var foldername:String = path.removingRegexMatches(pattern: fileFullname, replaceWith: "")
        var filename:String = fileFullname;
        if (foldername.length > 0){
            foldername = foldername.substring(from: 0, to: foldername.length - 1);
        }
        if (filename.length > 0)
        {
            filename = String(filename.splitted(by: ".")[0])

        }
//        ColorLog.yellow(log: "> getNewAsset: uri ' '  \(path)")
//        ColorLog.yellow(log: "> getNewAsset: fileFullname AssetFileName  \(fileFullname)")
//        ColorLog.yellow(log: "> getNewAsset: filename  AssetName \(filename)")
//        ColorLog.yellow(log: "> getNewAsset: foldername :AssetLocation  \(foldername)")
//        ColorLog.yellow(log: "> getNewAsset: md5  \(md5)")

        var newjson:JSON!
        let entry:NativeCanvasPenpalAsset!
        newjson = JSON(path)
        _ = entry = NativeCanvasPenpalAsset.init(key: path, asset: newjson!)

        _ = entry.setName(name:filename)
        _ = entry.setFileName(fileName:fileFullname)
        _ = entry.setSourceFrom(sourceFrom:"")
        _ = entry.setFileLocation(foldername:foldername)
        _ = entry.setFileMD5(md5:md5 )


//        ColorLog.cyan(log: "> getNewAsset: entry return  \(entry.getName())")
//        ColorLog.cyan(log: "> getNewAsset: entry return \( entry.getFileName())")
//        ColorLog.cyan(log: "> getNewAsset: entry return \(String(describing: entry?.getSourceFrom()))")
//        ColorLog.cyan(log: "> getNewAsset: entry return \(String(describing: entry?.getFileLocation()))")
//        ColorLog.cyan(log: "> getNewAsset: entry return \(String(describing: entry?.getFileMD5()))")
//        ColorLog.cyan(log: "> getNewAsset: entry return ALL \(String(describing: entry?.asset))")
        return entry!
    }
    
    
    private func readAssetsFolder(underFolder:String, folderString:String)->[String:String] {
        var files:[String:String] = [String:String]()
        if let url = folderString.getFolderURL(){
            let fm = FileManager.default
            let fileStrings = fm.subpaths(atPath: (url.absoluteString?.removingRegexMatches(pattern: "^file://"))!)
            for file in fileStrings!{
//                ColorLog.yellow(log: "> readAssetsFolder \(underFolder + "/" + file)")
                if (ASSET_TYPE.isSupported(file: file)) {
                    files[underFolder + "/" + file] = file.MD5()
                }
            }
        }
        ColorLog.purple(log: "> readAssetsFolder \(files)")
        return files;
    }
    
    public func destroy() {
        clean();
    }
    public func clean() {
        if(assetJson != nil){
        assetJson = JSON.null;
        }
    }
    
    public func getBitmap()->UIImage?{
        var image:UIImage?
        if (ImageFileisValid()){// && bitmap == nil) {
            let type:ASSET_TYPE = ASSET_TYPE.parse(file: self.type!.rawValue)
//            ColorLog.lightBlue(log: "> NCN bitmap .........type: \(type) filename:  \(backgroundPath)  pageFolder: \(pageFolder) ")
            
//            switch type {
//
//            case .PNG:
//                <#code#>
//            case .JPEG:
//                <#code#>
//            case .GIF:
//                <#code#>
//            case .SVG:
//                <#code#>
//            case .PDF:
//                <#code#>
//            case .UNKNOWN:
//                <#code#>
//            case .PNG_64:
//                <#code#>
//            case .JPEG_64:
//                <#code#>
//            case .GIF_64:
//                <#code#>
//            case .SVG_64:
//                <#code#>
//            }
        }
        return image
    }
    
    public func processAsset(assetManager:String)-> String{
        let assetManager:NativeCanvasPenpalAssetManager?
        var filename:String? = nil;//NativeCanvasPenpalAssetManager
        var result:Bool = true;
        var isFromTemp:Bool = false;
        do{
        if (isCopyFile()) {
            var sourceFile = exportData().rawString()
            if (sourceFile != nil) {
            isFromTemp = (sourceFile!.matches(pattern: "cache")) ||
                         (sourceFile!.matches(pattern: "temp")) ||
                         (sourceFile!.matches(pattern: "tmp"));
                    ColorLog.purple(log: "> processAsset isFromTemp \(isFromTemp)")
                let sourceMD5 = String((sourceFile?.MD5())!)
                    ColorLog.purple(log: "> processAsset sourceMD5 \(_ = sourceMD5)")
                let asset = getAssetByMD5(md5: sourceMD5!)
                    ColorLog.purple(log: "> processAsset asset \(asset)")
                if(asset == nil) {
                    // asset manager沒有此資源
                    if let url = sourceFile?.getFolderURL(){
                        filename = NativeCanvasPenpalAssetManager.ASSET_FOLDER + "/" + url.lastPathComponent!
                        var destFile:NSURL = NSURL(string: getAssetPath(assetLocation: filename!))!
                        ColorLog.purple(log: "> processAsset destFile \(destFile) ")
                        let fm = FileManager.default
                        if fm.isWritableFile(atPath: destFile.path!) && !fm.fileExists(atPath: destFile.path!){
                        // 寫入之處有同名檔案，更改寫入檔名
                            filename = getFileName(bookName: getBook().getBookName())
                            destFile = NSURL(fileURLWithPath: getAssetPath(assetLocation: filename!))
                            }
                            result = result && copyFile(isFromTemp: isFromTemp, filename: filename!, sourceFile: sourceFile!, destFile: destFile.absoluteString!);
                        }
                    
                }else {
                    // asset manager已經有此資源。不複製，不覆蓋，取得資源名稱
                    ColorLog.yellow(log:"> copy file existed");
//                    filename = asset.getKey();
                    filename = asset
                    ColorLog.purple(log: "> processAsset filename \(filename) ")
                    if(isFromTemp){
                      sourceFile?.removeAll()
                    }
//                    file = getAssetFile(asset: asset.getFileURL());
                }
                    }
                }
            }catch {
                    result = result && false;
                
                }
        if (isExportToImage()) {
            filename = getFileName(bookName: getBook().getBookName())
//            var bitmap:UIImageView =  exportData;
//            if (bitmap != nil || !bitmap.isRecycled()) {
                var destFile:NSURL = NSURL(fileURLWithPath: getAssetPath(assetLocation: filename!))
//                result &= Utility.saveImage(destFile, Bitmap.CompressFormat.PNG, 100, bitmap);
                if(result) {
                    file = destFile;
//                    addAsset(asset: assetManager!.getNewAsset(path: filename!,md5: ""));
//                    ColorLog.yellow(log: "> save bitmap to " + destFile);
                }
//            } else {
                result = result && false;
                ColorLog.yellow(log:"> save bitmap invalid");
//            }
        }
        if(isExportToText()){
            filename = getFileName(bookName: getBook().getBookName())
            let data:String = exportData().description
            if(data != nil){
                var destFile:NSURL = NSURL(fileURLWithPath: getAssetPath(assetLocation: filename!))
//                result = result && Utility.saveTextFile(destFile, data);
                if(result) {
                    file = destFile;
                    addAsset(asset: getNewAsset(path: filename!,md5: ""));
//                    ColorLog.yellow(log: "> save text to " + destFile);
                }
            }else{
                result = result && false;
                ColorLog.red(log: "> save text invalid");
            }
        }
//        let booktype:BOOK_TYPE = BOOK_TYPE(rawValue: type!.rawValue)!
//        type?.data
//                if(result) {
//                    if(type.isEmbed() && file != nil) {
//                        type.clearData();
//                        type = ASSET_TYPE.get(type.key().replace("_64",""));
//                    }
//                    return filename;
//                }else {
//                    return "nil";
                    //                }
                    
        
        return "nil";
    }
    
    private func copyFile(isFromTemp:Bool, filename:String ,sourceFile:String ,destFile:String)-> Bool{
        var result:Bool = true;
        let fileManager = FileManager.default
//         let homeDirectory = NSHomeDirectory()
        let tmpfiile = "tmp.file"
        let srcUrl =  sourceFile + filename
        let toUrl = destFile + "/" + ASSET_PROFILE
        let lastpathname:String =  srcUrl.lastPathComponent     // get path last Component name ex: xxx.json
        let tmpUrl = destFile + "/" + lastpathname
//        ColorLog.cyan(log: "> copy file lastpathname  \(lastpathname)");
//        ColorLog.cyan(log: "> copy file toUrl \(filename) :::\(toUrl)");
//        ColorLog.cyan(log: "> copy file tmpUrl \(filename) ::: \(tmpUrl)");
            if(isFromTemp){
                do {
//                    try fileManager.removeItem(atPath: tmpUrl)
                    try fileManager.moveItem(atPath: toUrl, toPath: tmpUrl)
                    result = true
                    ColorLog.yellow(log: "> move file complete");
                }
                catch let error as NSError {
                    ColorLog.red(log: "> rename file Something went wrong: \(error)");
                    result = false
                }
                
            }else{
//                result &= copyFile(sourceFile ,destFile);
                do {
                    try fileManager.copyItem(atPath: sourceFile, toPath: destFile)
                    result = true
                    ColorLog.cyan(log: "> copy file complete");
                }
                catch let error as NSError {
                    ColorLog.red(log: "> copy file Something went wrong: \(error)");
                    result = false
                }
                
            }
            if(result) {
                let asset = getNewAsset(path: filename, md5: "");
//                ColorLog.red(log: "> copy fileasset: \(asset)");
               _ =  asset.setSourceFrom(sourceFrom: sourceFile)
//                ColorLog.red(log: "> copy fileasset: \(asset.getSourceFrom())");
                if(!isFromTemp){
//                    asset.setSourceFrom(sourceFrom: sourceFile.getFileURL());
                    file = destFile.url as NSURL?;
//                    ColorLog.red(log: "> copy fileasset: file  \(file)");
                  _ =  addAsset(asset: asset);
                }
                ColorLog.cyan(log: "> copy file success and update to profile");
            }
//        ColorLog.yellow(log: "> copy file success and update to profile ::: \(assetJson)");
            return result;
    }
    
    public func addAsset(asset:NativeCanvasPenpalAsset)-> Bool{
        if (assets != nil) {
            assets![asset.getKey()] = asset
            assetJson![NativeCanvasPenpalAsset.ASSET_LIST] = getAssetsJson()
           _ = save();
            return true;
            }
        return false;
    }
//    public void release(){
//        Log.w(TAG,"> release ");
//        if(bitmap != null && !bitmap.isRecycled()){
//        bitmap.recycle();
//        }
//        bitmap = null;
//        if(exportData != null && exportData instanceof Bitmap){
//        Bitmap bitmap = (Bitmap) exportData;
//        if(!bitmap.isRecycled()){
//        bitmap.recycle();
//        }
//        exportData = null;
//        }
//    }
    public func setType(type:String) {
        if(type != nil){
        self.type = ASSET_TYPE.parse(file: self.type!.rawValue)
        }
    }
    public func exportData()-> JSON{
        var data:JSON = JSON()
        data[IMAGEFILE_TYPE] = JSON("png")
        data[IMAGEFILE_FILE] = JSON("/tmp/")
//        data[IMAGEFILE_TYPE] = JSON(type!)
//        data[IMAGEFILE_FILE] = JSON(file?.absoluteURL!)
//        data.put(IMAGEFILE_FILE, file.getAbsolutePath());
        return data
    }
//    public int hashCode() {
//    return type.key().hashCode() + (file == null?0:file.getAbsolutePath().hashCode());
//    }
    public func importData(data:JSON)-> Bool{
        var isImported:Bool = false;
        setType(type: data[IMAGEFILE_TYPE].string!);
        InitImagePath(filepath: data[IMAGEFILE_FILE].string!);
        isImported = true;
        return isImported;
    }
}
