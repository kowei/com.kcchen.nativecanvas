//
//  NativeCanvasPenpalAsset.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation

/*
 NativeCanvasPenpalAsset Object實作
 */
public class NativeCanvasPenpalAsset{

    public static let ASSET_CLEAN_PERIOD:String   = "AssetCleanPeriod";
    public static let ASSET_FILE_NAME:String      = "AssetFileName";
    public static let ASSET_FILE_MD5:String       = "AssetFileMd5";
    public static let ASSET_FROM:String           = "AssetFrom";
    public static let ASSET_LIST:String           = "AssetList";
    public static let ASSET_LOCATION:String       = "AssetLocation";
    public static let ASSET_MD5:String            = "AssetMd5";
    public static let ASSET_NAME:String           = "AssetName";
    
    internal var key:String?
    public var asset:JSON = JSON()
    
    init(key:String, asset:JSON){
        self.key = key;
        self.asset = JSON();
    }
    
    public func getKey()-> String{
        return key!;
    }
    
    public func getAsset()-> JSON{
        return asset;
    }

    public func setName(name:String)-> NativeCanvasPenpalAsset{
        asset[NativeCanvasPenpalAsset.ASSET_NAME] = JSON(name)
//         ColorLog.red(log: ">-  setName by  \(name)")
    
    return self;
    }
    
    public func getName()-> String{
        let name = asset["AssetName"]
        ColorLog.red(log: ">-  getName \(name)")
        return  name.rawString()!;
    }
    
    public func setFileName(fileName:String)-> NativeCanvasPenpalAsset{

        asset[NativeCanvasPenpalAsset.ASSET_FILE_NAME] = JSON(fileName)
//         ColorLog.red(log: ">-  setFileName \(asset[NativeCanvasPenpalAsset.ASSET_FILE_NAME])")
        return self ;
    }
    
    public func getFileName()-> String{
        
        let fileName = asset["AssetFileName"]
//        ColorLog.red(log: ">-  getFileName \(fileName)")
        return fileName.rawString()!;
    }
    
    public func setSourceFrom(sourceFrom:String)-> NativeCanvasPenpalAsset{

        asset[NativeCanvasPenpalAsset.ASSET_FROM] = JSON(sourceFrom)
//        ColorLog.red(log: ">-  setSourceFrom \(asset[NativeCanvasPenpalAsset.ASSET_FROM])")
        return self;
    }
    public func getSourceFrom()-> String{
       
        let file = asset["AssetFrom"]
//        ColorLog.red(log: ">-  getSourceFrom \(file)")
        return file.rawString()!;
    }
    
    public func setFileLocation(foldername:String)-> NativeCanvasPenpalAsset{

        asset[NativeCanvasPenpalAsset.ASSET_LOCATION] = JSON(foldername)
//        ColorLog.red(log: ">-  setFileLocation \(asset[NativeCanvasPenpalAsset.ASSET_LOCATION])")
        return self;
    }
    
    public func  getFileLocation()-> String{

        let fileLocation = asset["AssetLocation"]
//        ColorLog.red(log: ">-  getFileLocation \(fileLocation)")
        return fileLocation.rawString()!;
    }
    
    public func setFileMD5(md5:String)-> NativeCanvasPenpalAsset{
        do {
            asset[NativeCanvasPenpalAsset.ASSET_FILE_MD5] = JSON(md5)
//            ColorLog.red(log: ">-  setFileMD5 \(asset[NativeCanvasPenpalAsset.ASSET_FILE_MD5])")
        } catch {
         ColorLog.red(log: ">- catch error by setFileMD5")
        }
        return self;
    }
    
    public func getFileMD5()-> String{
        
        let filemd5 = asset["AssetFileMd5"]
//        ColorLog.red(log: ">-  getFileMD5 \(filemd5)")
        return filemd5.rawString()!;
    }
    

    public func toString()-> String{
        
        let data = try! JSONSerialization.data(withJSONObject: asset, options: .prettyPrinted)
        let assettostring = NSString(data: data, encoding: String.Encoding.utf8.rawValue)
        
        print(assettostring)
        
        return assettostring! as String;
    }
    
}
