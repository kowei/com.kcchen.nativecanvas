//
//  ColorLog.h
//  YuanJian
//
//  Created by KC Chen on 2015/4/14.
//  Copyright (c) 2015年 Datamite Tech. All rights reserved.
//

#ifndef YuanJian_ColorLog_h
#define YuanJian_ColorLog_h

@import Foundation;

#define XCODE_COLORS_ESCAPE @"\033["

#define XCODE_COLORS_RESET_FG  XCODE_COLORS_ESCAPE @"fg;" // Clear any foreground color
#define XCODE_COLORS_RESET_BG  XCODE_COLORS_ESCAPE @"bg;" // Clear any background color
#define XCODE_COLORS_RESET     XCODE_COLORS_ESCAPE @";"   // Clear any foreground or background color

//static func red<T>(object:T) {
//    println("\(ESCAPE)fg255,0,0;\(object)\(RESET)")
//}
//
//static func green<T>(object:T) {
//    println("\(ESCAPE)fg0,255,0;\(object)\(RESET)")
//}
//
//static func blue<T>(object:T) {
//    println("\(ESCAPE)fg0,0,255;\(object)\(RESET)")
//}
//
//static func yellow<T>(object:T) {
//    println("\(ESCAPE)fg255,255,0;\(object)\(RESET)")
//}
//
//static func purple<T>(object:T) {
//    println("\(ESCAPE)fg255,0,255;\(object)\(RESET)")
//}
//
//static func cyan<T>(object:T) {
//    println("\(ESCAPE)fg0,255,255;\(object)\(RESET)")
//}

#define LogBlue(frmt, ...) NSLog((XCODE_COLORS_ESCAPE @"fg0,0,255;" frmt XCODE_COLORS_RESET), ##__VA_ARGS__)
#define LogRed(frmt, ...) NSLog((XCODE_COLORS_ESCAPE @"fg255,0,0;" frmt XCODE_COLORS_RESET), ##__VA_ARGS__)
#define LogPurple(frmt, ...) NSLog((XCODE_COLORS_ESCAPE @"fg255,0,255;" frmt XCODE_COLORS_RESET), ##__VA_ARGS__)
#define LogCyan(frmt, ...) NSLog((XCODE_COLORS_ESCAPE @"fg0,255,255;" frmt XCODE_COLORS_RESET), ##__VA_ARGS__)
#define LogYellow(frmt, ...) NSLog((XCODE_COLORS_ESCAPE @"fg255,255,0;" frmt XCODE_COLORS_RESET), ##__VA_ARGS__)
#define LogGreen(frmt, ...) NSLog((XCODE_COLORS_ESCAPE @"fg0,255,0;" frmt XCODE_COLORS_RESET), ##__VA_ARGS__)




#endif
