//
//  NativeCanvasPenpalTemplateManager.swift
//  Paper
//
//  Created by GUAN BEI-FONG on 2018/5/23.
//

import Foundation

public class NativeCanvasPenpalTemplateManager{
    
    private let TEMPLATES_PROFILE:String = "templates.json";
    public static let TEMPLATES_FOLDER:String = "templates";
    private let TEMPLATES_CLEAN_PERIOD_DEFAULT:CLong = 604800000;// 7d*24h*60m*60s*1000ms;
    
//    internal let templateManager:NativeCanvasPenpalTemplateManager?
    internal var library:NativeCanvasPenpalLibrary?
    private var templates:[String:NativeCanvasPenpalTemplate?]? = [String:NativeCanvasPenpalTemplate?]()
    private var templateFolder:String?
    private var templateProfile:NSURL?
    private var isInit:Bool = false
    private var templateJson:JSON? = JSON()
    /**
     * constructor
     * @param library 因為位於 library 之下，傳遞有效的 PenpalLibrary 可以快速對應資料
     */
    init(library:NativeCanvasPenpalLibrary){
        self.library = library;
        self.templateFolder = library.libraryPath! + "/" + NativeCanvasPenpalTemplateManager.TEMPLATES_FOLDER;
        ColorLog.cyan(log: "TemplateManager initialize--- \(templateFolder)")
        self.initialize()
    }
    
    private func initialize()-> Bool{
        ColorLog.cyan(log: "TemplateManager initialize")
        if(!isValid()) {
            destroy();
            return false;
        }else{
            return load();
        }
    }
    
    /**
     * 確認 templateFolder 存在
     * 確認 TEMPLATES_PROFILE 存在或可寫入
     * @return true if conditons valid, false if invalid
     */
    
    public func isValid()->Bool{
        if(!isInit){
            if let folderUri:NSURL = templateFolder?.getFolderURL(){
//                folderUri
                ColorLog.blue(log:">  TemplateManager folderUri \(folderUri))");
            }
            if let fileUri:NSURL = getProfilePath().getFileURL(){
//                assetProfile = fileUri
                ColorLog.blue(log:">  TemplateManager fileUri \(fileUri))");
                templateProfile = fileUri
            }
            isInit = true;
        }
        return templateProfile != nil
    }

    
    public func make()-> NSURL{
        if(isValid()){
            if let folderUri:NSURL = templateFolder?.getFolderURL(){
                do{
                    let nsurl = folderUri
                    let fm = FileManager.default
                    if !fm.isWritableFile(atPath: nsurl.path!) && !fm.fileExists(atPath: nsurl.path!){
                        if fm.createFile(atPath: nsurl.path!, contents: nil, attributes: nil){
                            ColorLog.lightBlue(log: "> TemplateManager Make, create file \(nsurl.path!)...")
                             return nsurl;
                        }
                        
                    }
                    
                }catch (let e as NSError){
                    
                    ColorLog.red(log: "> TemplateManager Make catch save error \(e.localizedDescription)")
                    
                }
               
            }
            
        }
        return (templateFolder?.getFolderURL())!;
    }
    
    public func getTemplateFile(template:NativeCanvasPenpalTemplate)-> NSURL{
        return NSURL(fileURLWithPath: getTemplatePath(templateLocation: template.getKey()))
    }
    
    public func getProfilePath()-> String{
        return templateFolder! + "/" + TEMPLATES_PROFILE;
    }
    
    /**
     * 取得template資料夾或是template完整路徑
     * 例如：
     * @param templateLocation template路徑，為了符合之前資料設計，其包含了template資料夾
     * @return 如果有template路徑，返回template完整路徑，如果沒有，返回template資料夾
     */
    public func getTemplatePath(templateLocation:String)-> String{
        
        let TemplateAllPath:String? = templateLocation

        if((templateLocation.length) > 0) {
        return templateFolder! + "/" + templateLocation;
        }else{
        return templateFolder!;
        }
        
        return TemplateAllPath!;
}
    
    /**
     *
     * @return
     */
    public func getTemplateProfile()-> NSURL{
        return templateProfile!;
    }
    
    /**
     * getter of PenpalLibrary
     * @return PenpalLibrary
     */
    public func getLibrary()-> NativeCanvasPenpalLibrary{
        return library!;
    }
    
    public func save()->Bool {
        var result:Bool = false;
        do {
            if let json:JSON = templateJson{
                let urlString:String = templateProfile!.relativePath!
                let url:String = "file:///" + urlString
                if let nsurl = url.url{
                    do{
                        let fm = FileManager.default
                        if fm.isWritableFile(atPath: nsurl.path) && !fm.fileExists(atPath: nsurl.path){
                            if fm.createFile(atPath: nsurl.path, contents: nil, attributes: nil){
                                ColorLog.lightBlue(log: "> Template save, create file \(nsurl.path)...")
                            }
                        }
                        ColorLog.lightBlue(log: "> Template save to \(nsurl.path)...")
                        try json.rawString()?.write(to: nsurl as URL, atomically: false, encoding: .utf8)
                        ColorLog.lightBlue(log: "> Template save to \(nsurl.path)...")
                        result = true
                    }catch (let e as NSError){
                        ColorLog.red(log: "> Template save, error the path is \(nsurl.path)...")
                        result = false
                        ColorLog.red(log: "> Template catch save error \(e.localizedDescription)")
                    }
                }
            }else{
                ColorLog.lightRed(log: "> Template save to JSON invalid")
            }
        } catch (let e as NSError){
            result = false
            ColorLog.red(log: "> Template catch save error \(e.localizedDescription)")
        }
    
        return result;
    }
    
    /**
     * 物件建構時，若相關條件都通過，即會呼叫此將profile載入
     * @return 載入成功為true
     */
    public func load()-> Bool{
    if (isValid()) {
        ColorLog.green(log: "TemplateManager load \(templateProfile)")
        do {
            
            createProfile()
            ColorLog.lightYellow(log: "> TemplateManager loaded from default ... \(String(describing: templateJson))")
            if let url = templateProfile{
                if let path = url.path{
                    if let data = NSData(contentsOfFile: path){
                        var loadJson = try JSON(data: data as Data)
                        var list:JSON = loadJson[NativeCanvasPenpalTemplate.TEMPLATE_LIST]
                        
                        ColorLog.lightYellow(log: "> TemplateManager list... \(list )")
                        ColorLog.lightYellow(log: "> TemplateManager loaded from disk... \(loadJson )")
                        try templateJson!.merge(with: loadJson)
                        ColorLog.lightYellow(log: "> TemplateManager loaded merge... \(templateJson)")
                        
                        //                            int hash = Utility.getJsonHash(list);
                        let hash = (list.rawString()?.MD5())!
                        
                        if (list == nil) {
                            list = JSON()
                        }
                        // 更新Template list，跟檔案系統同步
                        templates = updateTemplates();///
                        ColorLog.lightYellow(log: "> TemplateManager loaded hash... \(hash)")
                        ColorLog.lightYellow(log: "> TemplateManager loaded hash 2... \(templateJson![NativeCanvasPenpalTemplate.TEMPLATE_LIST].rawString()?.MD5())")
                        if templateJson![NativeCanvasPenpalTemplate.TEMPLATE_LIST].rawString()?.MD5() != hash {
                            templateJson![NativeCanvasPenpalTemplate.TEMPLATE_LIST] = list
                            ColorLog.lightYellow(log: "> TemplateManager save new .TEMPLATE_LIST.. \(hash)")
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
    
    public func getTemplate(source:String)-> NativeCanvasPenpalTemplate?{
        ColorLog.lightYellow(log: "> TemplateManager getTemplate source :  \(source)")
        ColorLog.yellow(log: "> TemplateManager getTemplate templates :  \(templates)")
        ColorLog.lightYellow(log: "> TemplateManager getTemplate templates[] :  \(templates?[source])")
        if templates != nil{
            templates = updateTemplates();
//            ColorLog.lightYellow(log: "> TemplateManager getTemplate templates is't nil :  \(templates)")
            templateJson![NativeCanvasPenpalTemplate.TEMPLATE_LIST] = JSON(getTemplatesJson())
//            ColorLog.yellow(log: "> TemplateManager getTemplate.source: \(source) .templates[source]  \(templates![source])")
        }
        return (templates?[source])!
    }
    
    public func getTemplateByMD5(md5:String)-> String{
        if (templates != nil){
            let template:NativeCanvasPenpalTemplate?
            var ajmd5 = templateJson!["TemplateList"].dictionaryValue
            let key = Array(ajmd5.keys)
            let values = Array(ajmd5.values)
            for x in 0...values.count-1{
                for (_, value) in values[x] {
                    
                    if value.string == md5{
                        print("Item \(index): \(value)")
                        print("MD5: \(value.string))")
                        print("ALL: \(Array( values[x].dictionaryValue))")
                        let all = Array(values[x].dictionaryValue)
                        let TemplateFileName = all[1].value
                        let TemplateLocation = all[3].value
                        let source = TemplateLocation.string! + "/" + TemplateFileName.string!
                        ColorLog.green(log:"> getTemplateByMD5 source : \(source)")
                        return source
                    }
                }
            }
        }
        return "nil";
    }
    
    public func isMD5Exist(md5:String)-> Bool{
        return getTemplateByMD5(md5: md5) != nil;
    }
    
    /**
     *
     * @param folderName
     * @return
     */
    public func getTemplateMD5(folderName:String)-> String{
        let template:NativeCanvasPenpalTemplate = getTemplate(source: folderName)!
        if (template != nil) {
            return template.getFileMD5();
        }
            return "nil";
    }
    
    /**
     * 查找資料庫是否有此template
     * @param folderName
     * @return true id available, false is invalid
     */
    public func isTemplateAvailable(folderName:String)-> Bool{
        
        return getTemplateMD5(folderName: folderName) != nil;
    }
    
    /**
     * 根據資料夾名稱，比對已經儲存的MD5，檢視是否有變動
     * @param folderName
     * @return true if changed, false is same
     */
    public func isTemplateChanged(folderName:String)-> Bool{
        var isChanged:Bool = false;
        let TemplatePath = getTemplatePath(templateLocation: folderName)
        let folderMD5:String = TemplatePath.MD5()
        
        //getFolderMD5(getFolder(TemplatePath), false, NativeCanvasPenpalTemplate.TEMPLATE_PROFILE);
        let entryMd5:String = getTemplateMD5(folderName: folderName);
        if (folderMD5 != nil && entryMd5 != nil) {
            isChanged = !entryMd5.matches(pattern: folderMD5);
        }
    
        if (isChanged) {
        //            templates![folderName,default: _] = folderMD5
            templateJson![NativeCanvasPenpalTemplate.TEMPLATE_LIST] = JSON(getTemplatesJson())
            save();
            }
        return isChanged;
    }
    
    /**
     * 建立預設的 json 資料
     */
    private func createProfile()-> JSON{
    // 更新template list，跟檔案系統同步
        templates = updateTemplates();///
        if(templateJson == nil){
            templateJson = JSON();
            var templateList:JSON = JSON()
            templateJson![NativeCanvasPenpalTemplate.TEMPLATE_CLEAN_PERIOD] = JSON(TEMPLATES_CLEAN_PERIOD_DEFAULT)
            templateList = getTemplatesJson()
            templateList[NativeCanvasPenpalTemplate.TEMPLATE_LIST] = JSON(templateList)
            save();
        }
//        save();
        return templateJson!;
    }
    
    /**
     * 將 templates 陣列，轉換成 json 資料
     * @return json 包含所有 template 資料
     */
    private func getTemplatesJson()->JSON{
        var jsonObject:JSON = JSON()
            if(templates != nil){
                for (key, _) in templateJson!["TemplateList"] {
                    jsonObject[key] = templateJson!["TemplateList"][key]
                }
            }
        ColorLog.green(log: "> TemplateManager jsonObject \(jsonObject)")
        return jsonObject
    }
    
    /**
     *
     * @return
     */
    private func updateTemplates()-> [String:NativeCanvasPenpalTemplate?]?{
        
        var list:JSON = JSON()
        
        if var profile = templateJson{
            list = profile[NativeCanvasPenpalTemplate.TEMPLATE_LIST]
        }
        ColorLog.yellow(log: "> updateTemplate profile = templateJson \(list)")
        
        var templates:[String:NativeCanvasPenpalTemplate?] = [String:NativeCanvasPenpalTemplate?]()
        
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
        let files = readTemplatesFolder(underFolder: NativeCanvasPenpalTemplateManager.TEMPLATES_FOLDER, folderString: templateFolder!)
        for item in files {
            var template:NativeCanvasPenpalTemplate!
            ColorLog.green(log: "> readTemplatesFolder -path \(item.key) \(item.value)")
            var entry = list[item.key]
            if entry != JSON.null{
                let md5String = entry[NativeCanvasPenpalTemplate.TEMPLATE_FOLDER_MD5].string
                if let md5 = md5String{
                    if md5 != item.value{
                        // 更新 MD5
                        entry[NativeCanvasPenpalTemplate.TEMPLATE_FOLDER_MD5].string = item.value
                    }
                }
                
                template = NativeCanvasPenpalTemplate(path: NativeCanvasPenpalTemplateManager.TEMPLATES_FOLDER, key: item.key , template: entry)
                list[item.key] = entry // not sure if entry updated and list updated too, asign again to set new value
            }else{
                // 建立新的 entry
                template = getNewTemplate(path: item.key, md5: item.value)
                list[template.getKey()] = template.getTemplate()//
                
            }
            templates[item.key] = template
        }
        templateJson![NativeCanvasPenpalTemplate.TEMPLATE_LIST] = list
        ColorLog.yellow(log: "> updateTemplates templates \(templates)")
//                getTemplateByMD5(md5: "31A211F141CDA9CA6502130D926DAABA")
        return templates
    }
    
    private func readTemplatesFolder(underFolder:String, folderString:String)->[String:String] {
        var files:[String:String] = [String:String]()
        if let url = folderString.getFolderURL(){
            ColorLog.yellow(log: ">readTemplatesFolder url \(url)")
            let fm = FileManager.default
            let fileStrings = fm.subpaths(atPath: (url.absoluteString?.removingRegexMatches(pattern: "^file://"))!)
            for fileString in fileStrings!{
                let subfolderPath = folderString + "/" + fileString;
                if let subfolder = subfolderPath.getFolderURL(){
                    let subfolderJsonPath = folderString + "/" + fileString + "/" + NativeCanvasPenpalTemplate.TEMPLATE_PROFILE;
                    if let jsonfile = subfolderJsonPath.getFileURL(){
                        let jsonkey = subfolderPath.removingRegexMatches(pattern: templateFolder! + "/", replaceWith: "")
                        let jsonvalue = fileString.MD5()
                            files[jsonkey] = jsonvalue
                    }
                }
            }
        }
        ColorLog.purple(log: "> readTemplatesFolder \(files)")
        return files;
    }
    
    public func addTemplate(template:NativeCanvasPenpalTemplate)-> Bool{
        if (templates != nil) {
            templates![template.getKey()] = template
//                templates.put(template.getKey(), template);
            templateJson![NativeCanvasPenpalTemplate.TEMPLATE_LIST] =  getTemplatesJson()
                save();
                return true;
               
        }
        return false;
    }
    
    public func getNewTemplate(path:String, md5:String)-> NativeCanvasPenpalTemplate{
       
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
                ColorLog.yellow(log: "> getNewTemplate: uri ' '  \(path)")
                ColorLog.yellow(log: "> getNewTemplate: fileFullname TemplateFileName  \(fileFullname)")
                ColorLog.yellow(log: "> getNewTemplate: filename  TemplateName \(filename)")
                ColorLog.yellow(log: "> getNewTemplate: foldername :TemplateLocation  \(foldername)")
                ColorLog.yellow(log: "> getNewTemplate: md5  \(md5)")
        
        var newjson:JSON!
        let entry:NativeCanvasPenpalTemplate!
        newjson = JSON(path)
        _ = entry = NativeCanvasPenpalTemplate.init(path: templateFolder!, key: path, template: newjson)
        _ = entry.setName(name:filename)
        _ = entry.setFileName(fileName:fileFullname)
        _ = entry.setSourceFrom(sourceFrom:"")
        _ = entry.setFileLocation(foldername:foldername)
        _ = entry.setFolderMD5(md5:md5 )
        
        
        ColorLog.cyan(log: "> getNewTemplate: entry return  \(entry.getName())")
        ColorLog.cyan(log: "> getNewTemplate: entry return \( entry.getFileName())")
        ColorLog.cyan(log: "> getNewTemplate: entry return \(String(describing: entry?.getSourceFrom()))")
        ColorLog.cyan(log: "> getNewTemplate: entry return \(String(describing: entry?.getFileLocation()))")
        ColorLog.cyan(log: "> getNewTemplate: entry return \(String(describing: entry?.getFileMD5()))")
        ColorLog.cyan(log: "> getNewTemplate: entry return ALL \(String(describing: entry?.getTemplate()))")
//        ColorLog.cyan(log: "> getNewTemplate: entry getDescription \(String(describing: entry?.getDescription()))")
//        ColorLog.cyan(log: "> getNewTemplate: entry getKCCHENTemplateId \(String(describing: entry?.getKCCHENTemplateId()))")

        
        return entry!
    }
    
    public func clean() {
        if(templateJson != nil){
            while ((templateJson?.dictionary?.keys.count) != nil) {
                templateJson![(templateJson?.dictionary?.keys.count)!] = JSON()
            }
                templateJson = nil;
                templateProfile = NSURL(string: "")!
        }
    }
    
        
    
    
    public func destroy() {
    ColorLog.lightBlue(log:"> templateManager destroy");
    clean();
    }
    
    
    
    
}
