//
//  NativeCanvasUtils.swift
//  PenPal
//
//  Created by KC Chen on 2018/4/18.
//

import Foundation
import UIKit
import QuartzCore
import OpenGLES
import GLKit
import CoreMedia
import AVFoundation

public class NativeCanvasUtil{
    public static let SCREEN_RATIO: CGFloat = UIScreen.main.bounds.width / 320
    public static let STATUSBAR_HEIGHT: CGFloat = 20
}

public extension UIGestureRecognizerState{
    public var name:String{
        switch self {
        case .began:
            return "began"
        case .cancelled:
            return "cancelled"
        case .changed:
            return "changed"
        case .ended:
            return "ended"
        case .failed:
            return "failed"
        case .possible:
            return "possible"
        }
    }
}

public extension Sequence where Iterator.Element: CustomStringConvertible {
    func joined(seperator: String) -> String {
        return self.map({ (val) -> String in
            "\(val)"
        }).joined(separator: seperator)
    }
}

public extension CGRect{
    mutating func adjustStatus()->CGRect{
        let offset = UIApplication.shared.statusBarFrame.height - NativeCanvasUtil.STATUSBAR_HEIGHT
        self.origin.y += offset
        return self
    }
}
public extension FileManager{
    func isFolder(filePath: String)->URL?{
        var  result:Bool = false;
        var file:String
        var folderUri = NSURL(string: filePath)!
        file = (filePath == nil) ? "" : filePath
        
        if(folderUri == nil){
            folderUri = NSURL(string: file.pathEncode())!
            print("> NCN open Path folderUri pathEncode :\(folderUri)")
        }
        //        if let path = folderUri.pathWithoutFileScheme(){
        //            print("open Path file path:\(path)")
        //        }
        if(folderUri != nil){
            let fileManager = FileManager.default
            let exist = fileManager.fileExists(atPath: file.removingRegexMatches(pattern: "file://"))
            print("> NCN open Path folderUri exist :  \(exist) ")
            result = exist
        }
        if(!result) {
            
        };return folderUri as URL;
        return nil
    }
}
public extension String {
    //SwiftRegx
    
    public func index(offset: Int) -> String.Index {
        if offset == NSNotFound {
            return endIndex
        }
        else if offset < 0 {
            return index(endIndex, offsetBy: offset)
        }
        else {
            return index(startIndex, offsetBy: offset)
        }
    }
    
    public func substring(from: Int, to:Int) -> String {
        return substring(with: index(offset:from)..<index(offset:to))
    }
    
    public subscript(from: Int) -> String {
        let from = index(offset: from)
        return substring(with: from..<index( from, offsetBy: 1 ))
    }
    
    public subscript(from: Int, to: Int) -> String {
        return substring(from: from, to: to)
    }
    
    public subscript(range: Range<Int>) -> String {
        return substring(from: range.lowerBound, to: range.upperBound)
    }
    
    public subscript(pattern: String) -> SwiftRegex {
        return self[pattern, SwiftRegex.defaultOptions]
    }
    
    public subscript(pattern: String, options: NSRegularExpression.Options) -> SwiftRegex {
        return SwiftRegex(target: self.mutable, pattern: pattern, options: options)
    }
    
    public var mutable: NSMutableString {
        return NSMutableString(string: self)
    }

    
    
    func addScheme()->String{
        return "file:///\(self)"
    }
    
    func fileExist()->Bool{
        let fm = FileManager.default
        return fm.fileExists(atPath: self.removingRegexMatches(pattern: "^file://"))
    }
    
    func getFileURL()->NSURL?{
        if let fileUrl = NSURL(string: self.removingRegexMatches(pattern: "^file:/+").pathEncode()){
            let fileManager = FileManager.default
            if !fileManager.fileExists(atPath: fileUrl.path!){
                if fileManager.createFile(atPath: fileUrl.path!, contents: nil, attributes: nil){
                    do{
                        try fileManager.removeItem(atPath: fileUrl.path!)
                    }catch{
                        ColorLog.lightBlue(log: "> getFileURL remove file catch error")
                    }
                    return fileUrl
                    ColorLog.lightBlue(log: "> getFileURL create file \(fileUrl.path!)...")
                }else{
                    return nil
                }
            }else{
                let canWrite = fileManager.isWritableFile(atPath: fileUrl.path!)
                ColorLog.lightRed(log: "> getFileURL writable :  \(fileUrl.path!) \(canWrite)")
                
                if canWrite{
                    return fileUrl
                }
                return nil
            }
        }else{
            ColorLog.red(log: "> getFileURL error :\(self)")
        }
        return nil
    }
    
    func getFolderURL()->NSURL?{
        if let folderUrl = NSURL(string: self.removingRegexMatches(pattern: "^file:/+").pathEncode()){
            let fileManager = FileManager.default
            var isDirectory:ObjCBool = false
            let exist = fileManager.fileExists(atPath: folderUrl.path!, isDirectory: &isDirectory)
            ColorLog.lightRed(log: "> getFolderURL exist :  \(folderUrl.path!) \(isDirectory.boolValue)")

            if exist{
                if isDirectory.boolValue{
                    return folderUrl
                }
            }
            return nil
        }else{
            ColorLog.red(log: "> getFolderURL error :\(self)")
        }
        return nil
    }
    
//    func matches(pattern: String)-> Bool {
//        do {
//            let regex = try NSRegularExpression(pattern: pattern, options: NSRegularExpression.Options.caseInsensitive)
//            let range = NSMakeRange(0, self.characters.count)
//            let matches = regex.matches(in: self, options: [], range: range)
//            return matches.count > 0
//        } catch {
//            return false
//        }
//    }
    func MD5() -> String {
        
        let context = UnsafeMutablePointer<CC_MD5_CTX>.allocate(capacity: 1)
        var digest = Array<UInt8>(repeating:0, count:Int(CC_MD5_DIGEST_LENGTH))
        CC_MD5_Init(context)
        CC_MD5_Update(context, self, CC_LONG(self.lengthOfBytes(using: String.Encoding.utf8)))
        CC_MD5_Final(&digest, context)
        context.deallocate(capacity: 1)
        var hexString = ""
        for byte in digest {
            hexString += String(format:"%02x", byte)
        }
        
        return hexString.uppercased()
    }
}

extension NSMutableString {
    
    public subscript(pattern: String) -> MutableRegex {
        return self[pattern, SwiftRegex.defaultOptions]
    }
    
    public subscript(pattern: String, options: NSRegularExpression.Options) -> MutableRegex {
        return MutableRegex(target: self, pattern: pattern, options: options)
    }
    
}

extension UIImage {
    func DegreesToRadians(degrees:CGFloat) -> CGFloat {
        return degrees * CGFloat(M_PI) / CGFloat(180.0)
    }
    
    func RadiansToDegrees(radians:CGFloat) -> CGFloat {
        return radians * CGFloat(180.0) / CGFloat(M_PI)
    }
}

extension UIView {
    func loadFromXib(nib:String, bundle: Bundle! = nil) -> UIView!{
        return UINib(
            nibName: nib,
            bundle: bundle
            ).instantiate(withOwner: nil, options: nil)[0] as? UIView
    }
    
    func loadFromNib(nibNamed: String, bundle : Bundle? = nil) -> UIView? {
        return UINib(
            nibName: nibNamed,
            bundle: bundle
            ).instantiate(withOwner: nil, options: nil)[0] as? UIView
    }
}

extension UnsafePointer {
    

}

extension UnsafeMutablePointer {


}
