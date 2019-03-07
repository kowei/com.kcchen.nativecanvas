import Foundation
import UIKit
//import Social
//import MobileCoreServices


@objc(NativeCanvas) class NativeCanvas : CDVPlugin {
    
    private var callbackId:String? = nil
    private var NCManager: NativeCanvasManager? = nil
    private var touchLayer: NativeCanvasTouch? = nil
    private var isViewMode:Bool = false;
    private var borderType:BORDER_TYPE?
    override init() {
        super.init();
    }
    
    override func pluginInitialize() {
        ColorLog.cyan(log: "> NCN pluginInitialize");
        // start as transparent
        self.webView.isOpaque = false;
    }
    
    override func onMemoryWarning() {
        ColorLog.cyan(log: "> NCN MEMORY WARNING!!!")
    }
    
    override func onAppTerminate() {
        ColorLog.cyan(log: "> NCN onAppTerminate");
    }
    
    /*
     getNativeCanvas: () => Promise<NativeCanvasObject>;
     addEventListener: (type: string, callback: (event: NativeCanvasEvent) => void) => Promise<boolean>;
     removeEventListener: (type: string, callback: (event: NativeCanvasEvent) => void) => Promise<number>;
     ＊＊openCanvas: (x?: number, y?: number, width?: number, height?: number, backgroundColor?: string, isBack?: boolean) => Promise<number>;;
     closeCanvas: () => Promise<number>;
     openBook: (library:string, bookshelf:string, book:string, type:string, page?:number) => Promise<number>;
     **INIT: (library: string, bookshelf: string, book: string, initPages: number, bookTemplateId?: string) => Promise<number>;
     openPage: (page:number) => Promise<number>;
     saveBook: () => Promise<number>;
     savePage: () => Promise<number>;
     deleteBook: (id:string) => Promise<number>;
     deletePage: (page:number) => Promise<number>;
     register: (callbacks: {}) => Promise<NativeCanvasObject>;
     isRegistered: () => Promise<boolean>;
     show: () => Promise<void>;
     hide: () => Promise<void>;
     isShow: () => Promise<number>;
     /**
     * resolve: true - 已經開啟canvas
     *          false - Canvas 不存在
     * reject: 系統錯誤
     */
     
     isOpened: () => Promise<boolean>;
     setPosition: (x?: number, y?: number, width?: number, height?: number) => Promise<number>;
     setTouch: (isEnable: boolean) => Promise<number>;
     toggleDebugMode: (isDebugMode: boolean) => Promise<boolean>;
     setProperty: (type?: number, color?: string, width?: string, alpha?: string) => Promise<number>;
     clearCanvas: () => Promise<number>;
     /**
     * type: 0 image
     *       1 text
     *       2
     */
     addObject: (type: number, object?: NativeCanvasObjectInfo) => Promise<number>;
     /**
     * undo: step = -n;
     * redo step = n;
     * query step = 0;
     */
     undoOpen: () => Promise<number>;
     undoStep: (step: number) => Promise<NativeCanvasUndoInfo>;
     undoClose: () => Promise<number>;
     play: () => Promise<number>;
     mapOpen: () => Promise<number>;***
     mapMove: (x: string, y: string) => void;
     mapPosition: (x: string, y: string) => void;
     mapScale: (ratio: string) => void;
     mapClose: () => Promise<number>;
     toggleViewMode: (isViewMode: boolean) => Promise<boolean>;
     setDebug:(boolean isDebug) => Promise<boolean>;
     */
    
    func openCanvas(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN openCanvas \(command.callbackId)");
        DispatchQueue.global(qos: .default).async {
            
            // Background Thread
            var x:CGFloat = (command.argument(at: 0, withDefault: 0) as! CGFloat);
            var y:CGFloat = (command.argument(at: 1, withDefault: 0) as! CGFloat);
            let width:CGFloat = command.argument(at: 2, withDefault: 200) as! CGFloat;
            let height:CGFloat = command.argument(at: 3, withDefault: 200) as! CGFloat;
            let bgColorString:String = command.argument(at: 4, withDefault: "#000000") as! String;
            let bgColor:UIColor = UIColor(hexaDecimalString: "#000000")!;//bgColorString)!
            ColorLog.cyan(log: "> NCN openCanvas x:\(x) y:\(y) w:\(width) h:\(height) bgStr:\(bgColorString) bg:\(bgColor.toWebRGB())");
            
            DispatchQueue.main.async {
                var pluginResult:CDVPluginResult? = nil
                // Run UI Updates
                x += self.webView.frame.origin.x;
                y += self.webView.frame.origin.y;
                if let view = self.NCManager{
                    view.destroy();
                    view.removeFromParentViewController();
                    self.NCManager = nil;
                }
                
                if self.NCManager == nil{
                    ColorLog.lightGreen(log: "> NCN create NCManager and add to +webview");
                    self.NCManager = NativeCanvasManager();
                    self.NCManager?.view.frame = CGRect(
                        x: self.webView.frame.origin.x,
                        y: self.webView.frame.origin.y,
                        width: self.webView.frame.width,
                        height: self.webView.frame.height
                    )
                    self.NCManager?.view.bounds = (self.NCManager?.view.frame)!
                    self.NCManager?.view.isHidden = true;
                    
                    self.webView.isOpaque = false;
                    self.webView.superview?.addSubview((self.NCManager?.view)!);
                    self.webView.superview?.bringSubview(toFront: self.webView);
                    
                    if self.touchLayer == nil{
                        ColorLog.lightGreen(log: "> NCN create touchLayer and add to ++webview");
                        self.touchLayer = NativeCanvasTouch(webview: self.webView as! UIWebView, ncmanager: self.NCManager!)
                        self.webView.superview?.superview?.addSubview(self.touchLayer!);
                        self.webView.superview?.superview?.bringSubview(toFront: self.touchLayer!);
                    }
                    
                    
                    
                    // self.callbackId = command.callbackId
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs:  RESULT_TYPE.SUCCESS.rawValue
                    );
                    self.commandDelegate!.send(
                        pluginResult,
                        callbackId: command.callbackId
                    );
                }
            }
        }
    }
    
    func closeCanvas(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> closeCanvas");
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            
            DispatchQueue.main.async {
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                
                if let view = self.NCManager{
                    view.destroy();
                    view.removeFromParentViewController();
                    self.NCManager = nil;
                    
                    if let touch = self.touchLayer{
                        touch.destroy();
                        touch.removeFromSuperview()
                        self.touchLayer = nil;
                    }
                    
                    
                    self.callbackId = command.callbackId
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: RESULT_TYPE.SUCCESS.rawValue
                    );
                    self.commandDelegate!.send(
                        pluginResult,
                        callbackId: command.callbackId
                    );
                }
            }
        }
    }
    func openBook(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN openBook");
        
        let library:String = command.argument(at: 0, withDefault: " ") as! String;
        let bookshelf:String = command.argument(at: 1, withDefault: " ") as! String;
        let book:String = command.argument(at: 2, withDefault: " ") as! String;
        let type:String = command.argument(at: 3, withDefault: " ") as! String;
        let page:Int = command.argument(at: 4, withDefault:0 ) as! Int;
        print("> NCN openBook library page : \(page) is OK");
        self.commandDelegate.run(inBackground: {
            var pluginResult:CDVPluginResult? = nil
            DispatchQueue.main.async {
                
                if let booktype:BOOK_TYPE = BOOK_TYPE(rawValue: type){
                    print(">N CN openBook BOOK_TYPE is \(booktype)");
                    
                    if let manager = self.NCManager{
                        if let _ = library.getFolderURL(), let penpalLibrary = NativeCanvasPenpalLibrary.get(libraryPath: library){
                            print("> NCN openBook library \(library) is OK");
                            print("> NCN openBook penpalLibrary \(penpalLibrary) is OK");
                            
                            
                            
                            if NativeCanvasPenpalLibrary.hasLibrary(library: penpalLibrary){
                                if NativeCanvasPenpalLibrary.hasBookshelf(library: penpalLibrary, bookshelf: bookshelf){
                                    if NativeCanvasPenpalLibrary.hasBook(library: penpalLibrary, book: book, type: booktype){
                                        //                                        self.openPage(library: penpalLibrary, page: page)
                                        if NativeCanvasPenpalLibrary.hasPage(library: penpalLibrary, page: page){
                                            
                                            print("> openBook penpalLibrary1 \(NativeCanvasPenpalLibrary.hasLibrary(library: penpalLibrary)) is OK");
                                            print("> openBook penpalLibrary2 \(NativeCanvasPenpalLibrary.hasBookshelf(library: penpalLibrary, bookshelf: bookshelf)) is OK");
                                            print("> openBook penpalLibrary3 \(NativeCanvasPenpalLibrary.hasBook(library: penpalLibrary, book: book, type: booktype)) is OK");
                                            print("> openBook penpalLibrary4 \(NativeCanvasPenpalLibrary.hasPage(library: penpalLibrary, page: page)) is OK");
                                            //                            if NativeCanvasPenpalLibrary.hasLibrary(library: penpalLibrary)
                                            //                                && NativeCanvasPenpalLibrary.hasBookshelf(library: penpalLibrary, bookshelf: bookshelf)
                                            //                                && NativeCanvasPenpalLibrary.hasBook(library: penpalLibrary, book: book, type: booktype)
                                            //                                && NativeCanvasPenpalLibrary.hasPage(library: penpalLibrary, page: page)
                                            //                            {
                                            print("> openBook check is OK");
                                            
                                            manager.openBook(library: library, bookshelf: bookshelf, book: book, type: type ,page: page);
                                            pluginResult = CDVPluginResult(
                                                status: CDVCommandStatus_OK,
                                                messageAs: 0
                                            );
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        pluginResult = CDVPluginResult(
                            status: CDVCommandStatus_OK,
                            messageAs: RESULT_TYPE.FAILED.rawValue
                        );
                    }
                    
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: RESULT_TYPE.FAILED.rawValue
                    );
                }
                
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                )
            }
        })
    }
    
    func INIT(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN INIT");
        
        let library:String = command.argument(at: 0, withDefault: " ") as! String;
        let bookshelf:String = command.argument(at: 1, withDefault: " ") as! String;
        let book:String = command.argument(at: 2, withDefault: " ") as! String;
        let initPages:NSNumber = command.argument(at: 3, withDefault: " ") as! NSNumber;
        let bookTemplateId:String = command.argument(at: 4, withDefault:0 ) as! String;
        print(">NCN INIT  library : \(library) is OK");
        print(">NCN INIT  bookshelf : \(bookshelf) is OK");
        print(">NCN INIT  book : \(book) is OK");
        print(">NCN INIT  page : \(initPages) is OK");
        print(">NCN INIT  bookTemplateId : \(bookTemplateId) is OK");
        var wasOpen:Bool = false;
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            if let manager = self.NCManager{
                
//                manager.openPage(library: Int(library)!);
                self.callbackId = command.callbackId
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: RESULT_TYPE.SUCCESS.rawValue
                );
            }else{
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: RESULT_TYPE.FAILED.rawValue
                );
            }
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            
        })
    }
    func openPage(_ command: CDVInvokedUrlCommand) {
        DispatchQueue.global(qos: .default).async {
            
            let library:Int = command.argument(at: 0, withDefault: " ") as! Int;
            ColorLog.cyan(log: "> NCN openPage ---  library:\((library))");
            ColorLog.cyan(log: "> NCN openPage command ");
            
            self.commandDelegate.run(inBackground: {
                
                var pluginResult:CDVPluginResult? = nil
                if let manager = self.NCManager{
                    
                    manager.openPage(library: library);
                    self.callbackId = command.callbackId
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: RESULT_TYPE.SUCCESS.rawValue
                    );
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: RESULT_TYPE.FAILED.rawValue
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
                
            })
        }
    }
    func saveBook(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: RESULT_TYPE.SUCCESS.rawValue
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN saveBook");
        })
    }
    func savePage(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: RESULT_TYPE.SUCCESS.rawValue
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN savePage");
        })
    }
    func deleteBook(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: RESULT_TYPE.SUCCESS.rawValue
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN deleteBook");
        })
    }
    func deletePage(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: RESULT_TYPE.SUCCESS.rawValue
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN deletePage");
        })
    }
    func show(_ command: CDVInvokedUrlCommand) {
        ColorLog.lightRed(log: "> NCN show");
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            
            DispatchQueue.main.async {
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                
                if let view = self.NCManager{
                    
                    view.view.isHidden = false;
                    
                    self.callbackId = command.callbackId
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: true
                    );
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: false
                    );
                }
                
                if(pluginResult == nil){
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: false
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
            }
        }
    }
    func hide(_ command: CDVInvokedUrlCommand) {
        ColorLog.lightRed(log: "> NCN hide");
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            
            DispatchQueue.main.async {
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                
                if let view = self.NCManager{
                    
                    view.view.isHidden = true;
                    
                    self.callbackId = command.callbackId
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: true
                    );
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: false
                    );
                }
                
                if(pluginResult == nil){
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: false
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
            }
        }
    }
    func isShow(_ command: CDVInvokedUrlCommand) {
        ColorLog.lightRed(log: "> NCN isShow");
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            
            DispatchQueue.main.async {
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                
                if let view = self.NCManager{
                    
                    if(view.view.isHidden == false){
                        pluginResult = CDVPluginResult(
                            status: CDVCommandStatus_OK,
                            messageAs: true
                        );
                    }else{
                        pluginResult = CDVPluginResult(
                            status: CDVCommandStatus_OK,
                            messageAs: false
                        );
                    }
                    
                    self.callbackId = command.callbackId
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: true
                    );
                }
                
                if(pluginResult == nil){
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: false
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
            }
        }
    }
    func isOpened(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN isOpened");
//        let Result:Bool = (command.argument(at: 0, withDefault: 0) as! Bool);
//        ColorLog.cyan(log: "> NCN isOpened is  \( Result)");
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            
            DispatchQueue.main.async {
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                self.callbackId = command.callbackId
                
                if self.NCManager != nil{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: true
                    );
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: false
                    );
                }
                
                if(pluginResult == nil){
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: false
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
            }
        }
    }
    
    public func register(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN register");
        DispatchQueue.global(qos: .default).async {
//            let Result:Bool = (command.argument(at: 0, withDefault: 0) as! Bool);
//            ColorLog.cyan(log: "> NCN register is \( Result)");
            self.callbackId = command.callbackId
            let pluginResult:CDVPluginResult? = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
        }
    }
    
    public func isRegistered(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN isRegistered");
        DispatchQueue.global(qos: .default).async {
            let Result:Bool = (command.argument(at: 0, withDefault: 0) as! Bool);
            ColorLog.cyan(log: "> NCN isRegistered is \( Result)");
            var pluginResult:CDVPluginResult? = nil
            
            if self.callbackId != nil{
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: true
                );
            }else{
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: false
                );
            }
            
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
        }
        
    }
    
    private func sendCallback(message: String, pluginResult: CDVPluginResult){
        self.commandDelegate.send(
            pluginResult,
            callbackId: self.callbackId
        );
    }
    
    func setPosition(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN setPosition");
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            let x:CGFloat = (command.argument(at: 0, withDefault: 0) as! CGFloat);
            let y:CGFloat = (command.argument(at: 1, withDefault: 0) as! CGFloat);
            let width:CGFloat = command.argument(at: 2, withDefault: 200) as! CGFloat;
            let height:CGFloat = command.argument(at: 3, withDefault: 200) as! CGFloat;
            
            
            DispatchQueue.main.async {
                
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                self.callbackId = command.callbackId
                
                if let manager = self.NCManager{
                    manager.setPosition(x: x,y: y,width: width,height: height);
                    ColorLog.red(log: "> NCN setPosition x:\(x) y:\(y) w:\(width) h:\(height) \(self.webView.frame) \(self.webView.bounds) \(manager.getPaper().frame) \(manager.getPaper().bounds)");
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: true
                    );
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: false
                    );
                }
                
                if(pluginResult == nil){
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: false
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
            }
        }
    }
    func setTouch(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN setTouch");
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            let isTouch:Bool = (command.argument(at: 0, withDefault: false) as! Bool);
            
            ColorLog.cyan(log: "> NCN setTouch:\(isTouch)");
            
            DispatchQueue.main.async {
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                self.callbackId = command.callbackId
                
                if let view = self.touchLayer{
                    view.setTouch(isTouch: isTouch);
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: true
                    );
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: false
                    );
                }
                
                if(pluginResult == nil){
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: false
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
            }
        }
    }
    func toggleDebugMode(_ command: CDVInvokedUrlCommand) {
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            let isDebug:Bool = (command.argument(at: 0, withDefault: true) as! Bool);
            
            ColorLog.cyan(log: "> NCN toggleDebugMode:\(isDebug)");
            
            DispatchQueue.main.async {
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                self.callbackId = command.callbackId
                
                if let view = self.NCManager{
                    view.setDebug(isDebug: isDebug);
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: true
                    );
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: false
                    );
                }
                
                if(pluginResult == nil){
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: false
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
            }
        }
    }
    func setProperty(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN setProperty");
        let typeOption:CGFloat = command.argument(at: 0, withDefault: 0) as! CGFloat;
        let type:CGFloat = (typeOption == 9999) ? -1 : typeOption
        let color:String = command.argument(at: 1, withDefault: "#000000") as! String;
        let widthOption:String = command.argument(at: 2, withDefault: "200") as! String;
        let width:String = (widthOption == "9999") ? "-1" : widthOption
        let alphaOption:String  = command.argument(at: 3, withDefault: "1") as! String;
        let alpha:String = (alphaOption == "9999") ? "-1" : alphaOption
        
        ColorLog.cyan(log: "> NCN setProperty type:\(type) color:\(color) w:\(width) alpha:\(alpha))");
        
        DispatchQueue.main.async {
            // Run UI Updates
            var pluginResult:CDVPluginResult? = nil
            self.callbackId = command.callbackId
            
            if let view = self.NCManager{
                view.setProperty(type: type,color: color,width: width,alpha: alpha);
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: true
                );
            }else{
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: false
                );
            }
            
            if(pluginResult == nil){
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_ERROR,
                    messageAs: false
                );
            }
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
        }
    }
    func clearCanvas(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.red(log: "> NCN clearCanvas");
        })
    }
    func addObject(_ command: CDVInvokedUrlCommand) {
        ColorLog.cyan(log: "> NCN addObject");
        let typeOption:Int = command.argument(at: 0, withDefault: 0) as! Int;
        let type:Int = (typeOption == 9999) ? -1 : typeOption;
        let object:JSON = JSON(command.argument(at: 1, withDefault: 1))
        //        print("> NCN addObject type:\(type) object:\(object) ");
        DispatchQueue.main.async {
            var pluginResult:CDVPluginResult? = nil
            self.callbackId = command.callbackId
            let objecttype:OBJECT_TYPE = OBJECT_TYPE(rawValue: type)!
            ColorLog.cyan(log:"> addObject OBJECT_TYPE is \(objecttype)");
            if let view = self.NCManager{
                view.addObject(type:type , object:object);
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: true
                );
            }else{
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: false
                );
            }
            if(pluginResult == nil){
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_ERROR,
                    messageAs: false
                );
            }
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
        }
    }
    func undoOpen(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN undoOpen");
        })
    }
    func undoStep(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN undoStep");
        })
    }
    func undoClose(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN undoClose");
        })
    }
    func play(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN play");
        })
    }
    func mapOpen(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: RESULT_TYPE.SUCCESS.rawValue
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN mapOpen");
        })
    }
    func mapMove(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN mapMove");
        })
    }
    func mapPosition(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN mapPosition");
        })
    }
    func mapScale(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: true
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            
            ColorLog.cyan(log: "> NCN mapScale");
            
        })
    }
    func mapClose(_ command: CDVInvokedUrlCommand) {
        self.commandDelegate.run(inBackground: {
            
            var pluginResult:CDVPluginResult? = nil
            
            self.callbackId = command.callbackId
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: RESULT_TYPE.SUCCESS.rawValue
            );
            self.commandDelegate!.send(
                pluginResult,
                callbackId: command.callbackId
            );
            ColorLog.cyan(log: "> NCN mapClose");
        })
    }
    func toggleViewMode(_ command: CDVInvokedUrlCommand) {
        DispatchQueue.global(qos: .default).async {
            // Background Thread
            let isViewMode:Bool = (command.argument(at: 0, withDefault: true) as! Bool);
            self.isViewMode = isViewMode
            DispatchQueue.main.async {
                // Run UI Updates
                var pluginResult:CDVPluginResult? = nil
                self.callbackId = command.callbackId
                if isViewMode == false{
                    ColorLog.cyan(log: "> NCN toggleViewMode:\(self.isViewMode)");
                    self.NCManager?.setViewMode(isViewMode: !self.isViewMode);
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: true
                    );
                }else {
                    self.NCManager?.setViewMode(isViewMode: self.isViewMode);
                    ColorLog.cyan(log: "> NCN toggleViewMode: else  \(self.isViewMode)");
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: false
                    );
                }
                
                if(pluginResult == nil){
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_ERROR,
                        messageAs: false
                    );
                }
                self.commandDelegate!.send(
                    pluginResult,
                    callbackId: command.callbackId
                );
            }
        }
    }
    func hasView(_ command: CDVInvokedUrlCommand) {
        let ishasView:Bool = (command.argument(at: 0, withDefault: true) as! Bool);
        ColorLog.cyan(log: "> NCN hasView:\(ishasView)");
        
    }
    private func openPage(library:NativeCanvasPenpalLibrary, page:Int)->CDVPluginResult? {
        var pluginResult:CDVPluginResult? = nil
        ColorLog.cyan(log: "> NCN **openPage:\(page)");
        if(NativeCanvasPenpalLibrary.hasPage(library: library, page: page)){
            //            NCManager.readPage(library, page)
            pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK,
                messageAs: RESULT_TYPE.SUCCESS.rawValue
            )
        }else{
            if let book = NativeCanvasPenpalLibrary.getCurrentBook(){
                ColorLog.cyan(log: "> NCN **openPage book :\(book)");
                if book.createPage(page: page){
                    //ncManager.readPage(library, page);
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: RESULT_TYPE.SUCCESS.rawValue
                    )
                }else{
                    pluginResult = CDVPluginResult(
                        status: CDVCommandStatus_OK,
                        messageAs: RESULT_TYPE.PAGE_CREATE_FAILED.rawValue
                    )
                }
            }else{
                pluginResult = CDVPluginResult(
                    status: CDVCommandStatus_OK,
                    messageAs: RESULT_TYPE.PAGE_NOT_EXISTED.rawValue
                )
            }
        }
        return pluginResult;
    }
}

extension UIColor {
    
    
    //Convert RGBA String to UIColor object
    //"rgbaString" must be separated by space "0.5 0.6 0.7 1.0" 50% of Red 60% of Green 70% of Blue Alpha 100%
    public convenience init?(rgbaString : String){
        self.init(ciColor: CIColor(string: rgbaString))
    }
    
    //Convert UIColor to RGBA String
    func toRGBAString()-> String {
        
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var a: CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        return "\(r) \(g) \(b) \(a)"
        
    }
    //return UIColor from Hexadecimal Color string
    public convenience init?(hexaDecimalString: String) {
        
        let r, g, b, a: CGFloat
        
        if hexaDecimalString.hasPrefix("#") {
            let start = hexaDecimalString.index(hexaDecimalString.startIndex, offsetBy: 1)
            let hexColor = hexaDecimalString.substring(from: start)
            ColorLog.cyan(log: "> NCN hexColor:\(hexColor)");
            #if swift(>=4)
            let count = hexColor.count;
            #else
            let count = hexColor.characters.count;
            #endif
            ColorLog.cyan(log: "> NCN count:\(count)");
            if count == 8 {
                let scanner = Scanner(string: hexColor)
                var hexNumber: UInt64 = 0
                
                if scanner.scanHexInt64(&hexNumber) {
                    r = CGFloat((hexNumber & 0xff000000) >> 24) / 255
                    g = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
                    b = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
                    a = CGFloat(hexNumber & 0x000000ff) / 255
                    self.init(red: r, green: g, blue: b, alpha: a)
                    return
                }
            }else if count == 6 {
                let scanner = Scanner(string: hexColor)
                var hexNumber: UInt64 = 0
                
                if scanner.scanHexInt64(&hexNumber) {
                    r = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
                    g = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
                    b = CGFloat(hexNumber & 0x000000ff) / 255
                    self.init(red: r, green: g, blue: b, alpha: 1)
                    return
                }
            }
        }
        
        return nil
    }
    // Convert UIColor to Hexadecimal String
    func toHexString() -> String {
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var a: CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        return String(
            format: "%02X%02X%02X",
            Int(r * 0xff),
            Int(g * 0xff),
            Int(b * 0xff)
        )
    }
    
    // Convert UIColor to Web String
    func toWebRGB() -> String {
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var a: CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        ColorLog.cyan(log: "r:\(Int(r * 0xff)) g:\(Int(g * 0xff)) b:\(Int(b * 0xff)) a:\(Int(a * 0xff))");
        return String(
            format: "#%02X%02X%02X",
            Int(r * 0xff),
            Int(g * 0xff),
            Int(b * 0xff)
        )
    }
    
    // Convert UIColor to Web String
    func toWebRGBA() -> String {
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var a: CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        ColorLog.cyan(log: "r:\(Int(r * 0xff)) g:\(Int(g * 0xff)) b:\(Int(b * 0xff)) a:\(Int(a * 0xff))");
        return String(
            format: "#%02X%02X%02X%02X",
            Int(r * 0xff),
            Int(g * 0xff),
            Int(b * 0xff),
            Int(a * 0xff)
        )
    }
    
    func toRGBAString(uppercased: Bool = true) -> String {
        var r: CGFloat = 0
        var g: CGFloat = 0
        var b: CGFloat = 0
        var a: CGFloat = 0
        self.getRed(&r, green: &g, blue: &b, alpha: &a)
        let rgba = [r, g, b, a].map { $0 * 255 }.reduce("", { $0 + String(format: "%02x", Int($1)) })
        return uppercased ? rgba.uppercased() : rgba
    }
}

