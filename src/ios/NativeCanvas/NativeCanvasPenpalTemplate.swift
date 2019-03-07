//
//  NativeCanvasPenpalTemplate.swift
//  Paper
//
//  Created by GUAN BEI-FONG on 2018/5/23.
//

import Foundation

public class NativeCanvasPenpalTemplate{

    public static let TEMPLATE_PROFILE:String                    = "template.json";
    public static let TEMPLATE_CLEAN_PERIOD:String               = "TemplateCleanPeriod";
    public static let TEMPLATE_LIST:String                       = "TemplateList";
    public static let TEMPLATE_FOLDER_MD5:String                 = "TemplateFolderMd5";
    public static let TEMPLATE_FILE_NAME:String                  = "TemplateFileName";
    public static let TEMPLATE_FROM:String                       = "TemplateFrom";
    public static let TEMPLATE_LOCATION:String                   = "TemplateLocation";
    public static let TEMPLATE_MD5:String                        = "TemplateMd5";
    public static let TEMPLATE_NAME:String                       = "TemplateName";

    public static let TEMPLATE_FILE_FORMAT_VERSION:String        = "fileFormatVersion";
    public static let TEMPLATE_SOURCE:String                     = "templateSource";
    public static let TEMPLATE_CATEGORY_ID:String                = "categoryId";     //array
    public static let TEMPLATE_KCCHEN_TEMPLATE_ID:String         = "KCCHENTemplateId";
    public static let TEMPLATE_THUMBNAIL:String                  = "thumbnail";
    public static let TEMPLATE_CONTENT:String                    = "content";        //json
    public static let TEMPLATE_CONTENT_BG:String                 = "bg";        //json
    public static let TEMPLATE_DESCRIPTION:String                = "description";
    public static let TEMPLATE_CREATE_DATE:String                = "createDate";
    public static let TEMPLATE_MODIFY_DATE:String                = "modifyDate";


    internal let key:String?
    internal let templateJson:JSON
    internal let templateProfilePath:String?
    private var isInit:Bool = false
    private var templateProfileFile:NSURL?
    private var templateProfile:JSON = JSON()
    
    init(path:String, key:String , template:JSON){
        self.key = key;
        self.templateJson = template;
        self.templateProfilePath = path + "/" + key + "/" + NativeCanvasPenpalTemplate.TEMPLATE_PROFILE;
        ColorLog.cyan(log: "Template, template : \(template) ")
        ColorLog.cyan(log: "Template,templateProfilePath:  \(templateProfilePath) ")
        self.initialize()
       
    }
    
    private func initialize()-> Bool{
        ColorLog.cyan(log: "Template, initialize ")
        if(!isValid()) {
            destroy();
            return false;
        }else{
            return load();
        }
    }

    public func isValid()->Bool{
        if(!isInit){
            if let templatefileUri:NSURL = templateProfilePath?.getFileURL(){
                templateProfileFile = templatefileUri
            }
            isInit = true;
        }
        return templateProfileFile != nil
    }
    public func save()->Bool {
        var result:Bool = false;
        let fileManager = FileManager.default
        if (isValid()) {
            do {
                try fileManager.moveItem(at: templateProfileFile as! URL, to: templateProfileFile as! URL)
                
                result = true
                ColorLog.cyan(log: "> overwriteTextFile file complete");
            }
            catch let error as NSError {
                ColorLog.red(log: "> overwriteTextFile file Something went wrong: \(error)");
                result = false
            }
        }
        return result;
    }

    public func load()-> Bool{
        if (isValid()) {
            do {
        
        if let url = templateProfileFile{
            if let path = url.path{
                if let data = NSData(contentsOfFile: path){
                    var loadJson = try JSON(data: data as Data)
                    var list:JSON = JSON(loadJson)
                    templateProfile = JSON(loadJson)
                }
                if (templateJson != JSON.null){
                    try templateProfile.merge(with: templateProfile)
                }
                save();
            }
        }
            }catch{
                return false;
            }
            return true;
        }
        return false;
    }

    public func clean() {
        do {
        if(templateProfile != nil){
            while ((templateProfile.dictionary?.keys.count) != nil) {
                templateProfile[(templateProfile.dictionary?.keys.count)!] = JSON()
            }
                templateProfile = nil;
                templateProfileFile = NSURL(string: "")!
                }
            }catch{
        
        }
    }

    public func destroy() {
        clean();
    }

    public func getKey()-> String{
        return key!;
    }

    public func getTemplate()-> JSON{
        return templateProfile;
    }

    public func setName(name:String)-> NativeCanvasPenpalTemplate {
        templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_NAME] = JSON(name)
    return self;
    }

    public func getSource()-> String{
        return templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_SOURCE].string!
    }

    public func getCategoryId()-> JSON{
        return templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_CATEGORY_ID]
    }

    public func getKCCHENTemplateId()-> Int{
        var KCCHENTemplateId:JSON = templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_KCCHEN_TEMPLATE_ID]
        if (KCCHENTemplateId.null != nil){
            return KCCHENTemplateId.int!
        }
        return -1
    }

    public func getThumbnail()-> String{
        return templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_THUMBNAIL].description
    }

    public func getBackground()-> String{
        var content:JSON = templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_CONTENT]
        ColorLog.lightBlue(log: "getBackground \(content)")
        if (content != nil){
            return content[NativeCanvasPenpalTemplate.TEMPLATE_CONTENT_BG].string!
        }
        return "nil";
    }

    public func getDescription()-> String{
        var Description:JSON = templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_DESCRIPTION]
        if (Description.null != nil){
            return Description[NativeCanvasPenpalTemplate.TEMPLATE_DESCRIPTION].string!
        }
        return "nil";
    }

    public func getCreateDate()-> String{
        var CreateDate:JSON = templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_CREATE_DATE]
        if (CreateDate.null != nil){
            return CreateDate[NativeCanvasPenpalTemplate.TEMPLATE_MODIFY_DATE].string!
        }
        return "nil";
    }

    public func getModifyDate()-> String{
        var ModifyDate:JSON = templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_MODIFY_DATE]
        if (ModifyDate.null != nil){
            return ModifyDate[NativeCanvasPenpalTemplate.TEMPLATE_MODIFY_DATE].string!
        }
        return "nil";
    }

    public func getName()-> String{
        let name = templateProfile["TemplateName"]
        ColorLog.red(log: ">- Template getName \(name)")
        return  name.rawString()!;
    }

    public func setFileName(fileName:String)-> NativeCanvasPenpalTemplate{
    
        templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_FILE_NAME] = JSON(fileName);
    
        return self;
    }

    public func getFileName()-> String{
        let fileName = templateProfile["TemplateFileName"]
                ColorLog.red(log: ">- template getFileName \(fileName)")
        return fileName.rawString()!;
    }

    public func setSourceFrom(sourceFrom:String)-> NativeCanvasPenpalTemplate{
        templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_FROM] = JSON(sourceFrom);
        return self;
    }

    public func getSourceFrom()-> String{
        let file = templateProfile["TemplateFrom"]
                ColorLog.red(log: ">- templateProfile  getSourceFrom \(file)")
        return file.rawString()!;
    }

    public func setFileLocation(foldername:String)-> NativeCanvasPenpalTemplate{
        templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_LOCATION] = JSON(foldername);
        return self;
    }

    public func getFileLocation()-> String{
        let fileLocation = templateProfile["TemplateLocation"]
                ColorLog.red(log: ">- templateProfile  getFileLocation \(fileLocation)")
        return fileLocation.rawString()!;
    }

    public func setFolderMD5(md5:String)-> NativeCanvasPenpalTemplate{
        templateProfile[NativeCanvasPenpalTemplate.TEMPLATE_FOLDER_MD5] = JSON(md5);
        return self;
    }

    public func getFileMD5()-> String{
        let filemd5 = templateProfile["TemplateFileMd5"]
                ColorLog.red(log: ">- templateProfile  getFileMD5 \(filemd5)")
        return filemd5.rawString()!;
    
    }

    public func toString()-> String{
        return templateProfile.string!
    }
}
