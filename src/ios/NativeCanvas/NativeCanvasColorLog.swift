//
//  ColorLog.swift
//  YuanJian
//
//  Created by KC Chen on 2015/4/14.
//  Copyright (c) 2015年 Datamite Tech. All rights reserved.
//

import Foundation


struct ColorLog {
    static let ESCAPE = "\u{001b}["
    
    static let RESET_FG = ESCAPE + "fg;" // Clear any foreground color
    static let RESET_BG = ESCAPE + "bg;" // Clear any background color
    static let RESET = ESCAPE + ";"   // Clear any foreground or background color
    //let color:UIColor = UIColor(red: 0.88, green: 0.68, blue: 1.00, alpha: 1.00)
    fileprivate static func colorString<T>(log:T,r:Float,g:Float,b:Float) -> String{
        return "\(start(r: r, g: g, b: b))\(log)\(end())"
    }
    fileprivate static func coloredString(log:String,r:Float,g:Float,b:Float) -> String{
        return "\(start(r: r, g: g, b: b))\(log)\(end())"
    }
    fileprivate static func insertColoredString(log:String,r:Float,g:Float,b:Float) -> String{
        return "\(end())\(start(r: r, g: g, b: b))\(log)"
    }
    private static func start(r:Float,g:Float,b:Float)->String{
        return "\(ESCAPE)fg\(String(Int(255.0 * r))),\(String(Int(255.0 * g))),\(String(Int(255.0 * b)));"
    }
    private static func end()->String{
        return "\(RESET)"
    }

    static func red<T>(log:T)        {print(colorString(log: log, r:1,   g: 0,   b: 0    ))}
    static func lightRed<T>(log:T)   {print(colorString(log: log, r:1,   g: 0.6 ,b: 0.6  ))}
    static func green<T>(log:T)      {print(colorString(log: log, r:0,   g: 1,   b: 0    ))}
    static func lightGreen<T>(log:T) {print(colorString(log: log, r:0.6, g: 1,   b: 0.6  ))}
    static func blue<T>(log:T)       {print(colorString(log: log, r:0,   g: 0,   b: 1    ))}
    static func lightBlue<T>(log:T)  {print(colorString(log: log, r:0.6, g: 0.6, b: 1    ))}
    static func yellow<T>(log:T)     {print(colorString(log: log, r:1,   g: 1,   b: 0    ))}
    static func lightYellow<T>(log:T){print(colorString(log: log, r:1,   g: 1   ,b: 0.6  ))}
    static func purple<T>(log:T)     {print(colorString(log: log, r:1,   g: 0,   b: 1    ))}
    static func lightPurple<T>(log:T){print(colorString(log: log, r:0.88,g: 0.6 ,b: 1    ))}
    static func cyan<T>(log:T)       {print(colorString(log: log, r:0,   g: 1,   b: 1    ))}

    // 打印兩個對象分別藍色和黃色輸出
    static func blueAndYellow<T>(obj1:T,obj2:T) {
        #if DEBUG
        print("\(ESCAPE)fg0,0,255;\(obj1)\(RESET)" + "\(ESCAPE)fg255,255,0;\(obj2)\(RESET)")
        #endif
    }
    // 紅色輸出
    static func redt<T>(object: T) {
        print("\(ESCAPE)fg212,84,0;\(object)\(RESET)")
    }
    
    static func red(log:String)        {print(coloredString(log: log, r:1,   g: 0,   b: 0    ))}
    static func lightRed(log:String)   {print(coloredString(log: log, r:1,   g: 0.6 ,b: 0.6  ))}
    static func green(log:String)      {print(coloredString(log: log, r:0,   g: 1,   b: 0    ))}
    static func lightGreen(log:String) {print(coloredString(log: log, r:0.6, g: 1,   b: 0.6  ))}
    static func blue(log:String)       {print(coloredString(log: log, r:0,   g: 0,   b: 1    ))}
    static func lightBlue(log:String)  {print(coloredString(log: log, r:0.6, g: 0.6, b: 1    ))}
    static func yellow(log:String)     {print(coloredString(log: log, r:1,   g: 1,   b: 0    ))}
    static func lightYellow(log:String){print(coloredString(log: log, r:1,   g: 1   ,b: 0.6  ))}
    static func purple(log:String)     {print(coloredString(log: log, r:1,   g: 0,   b: 1    ))}
    static func lightPurple(log:String){print(coloredString(log: log, r:0.88,g: 0.6 ,b: 1    ))}
    static func cyan(log:String)       {print(coloredString(log: log, r:0,   g: 1,   b: 1    ))}
}

public extension String{
    var purple:String{
        get{
            return ColorLog.insertColoredString(log: self, r:1,   g: 0,   b: 1    )
        }
    }
    var lightRed:String{
        get{
            return ColorLog.insertColoredString(log: self, r:1,   g: 0.6 ,b: 0.6  )
        }
    }
    var lightBlue:String{
        get{
            return ColorLog.insertColoredString(log: self, r:0.6, g: 0.6, b: 1    )
        }
    }
    var lightGreen:String{
        get{
            return ColorLog.insertColoredString(log: self, r:0.6, g: 1,   b: 0.6  )
        }
    }
}
