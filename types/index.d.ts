
declare interface NativeCanvasObject {
    getNativeCanvas: () => Promise<NativeCanvasObject>;
    addEventListener: (type: string, callback: (event: NativeCanvasEvent) => void) => Promise<boolean>;
    removeEventListener: (type: string, callback: (event: NativeCanvasEvent) => void) => Promise<number>;
    openCanvas: (x?: number, y?: number, width?: number, height?: number, backgroundColor?: string, isBack?: boolean) => Promise<number>;
    closeCanvas: () => Promise<number>;
    openBook: (library: string, bookshelf: string, book: string, type: string, page?: number) => Promise<number>;
    INIT: (library: string, bookshelf: string, book: string, initPages: number, bookTemplateId?: string) => Promise<number>;
    SET_PROFILE: (property: NativeCanvasProperties, library: string, bookshelf?: string, book?: string, page?: number) => Promise<number>;
    setProfile: (property: NativeCanvasProperties, isReload:boolean, library: string, bookshelf?: string, book?: string, page?: number) => Promise<number>;
    openPage: (page: number, isInsert?: boolean) => Promise<number>;
    saveBook: () => Promise<number>;
    savePage: () => Promise<number>;
    deleteBook: (id: string) => Promise<number>;
    deletePage: (page: number) => Promise<number>;
    register: (callbacks: {}) => Promise<NativeCanvasObject>;
    isRegistered: () => Promise<boolean>;
    CLEARDATA: () => Promise<number>;
    show: () => Promise<void>;
    hide: () => Promise<void>;
    isShow: () => Promise<boolean>;
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
    mapOpen: () => Promise<number>;
    mapMove: (x: string, y: string) => void;
    mapPosition: (x: string, y: string) => void;
    mapScale: (ratio: string) => void;
    mapClose: () => Promise<number>;
    toggleViewMode: (isViewMode: boolean) => Promise<boolean>;
}

interface Window {
    NativeCanvas: NativeCanvasObject;
}

declare interface NativeCanvasObjectInfo {
    url?: string;
    base64?: string;
    mime?: string;
    text?: string;
}

declare interface NativeCanvasProperties {
    library?: {

    };
    bookshelf?: {

    };
    book?: {
        fileFormatVersion?: string;
        isGuideDoc?: boolean;
        isDefault? : boolean;
        isHidden?: boolean;
        displayName?: string;
        tag?: string;
        projectType?: string;
        assetCheckFlag?: boolean;
        assetFiles?: any[

        ];
        category?: string;
        group?: string;
        author?: string;
        createDate?: string;
        modifyDate?: string;
        comment?: string;
        pdfSrcFileName?: string;
        BookTemplate?: string;
        BookAssetCheckLasttime?: string;
        BookModifyDate1970?: string;
        BookCreateDate1970?: string;
        cover?:
        {
            coverType?: string,
            coverId?: string,
            coverURI?: string,
            coverBookmarkColor?: string
        },
    }

    page?: {
        fileFormatVersion?: string;
        duration?: number;
        segmentA?: number;
        segmentB?: number;
        collectedActions?: {};
        platformInfo?: {};
        containerInfo?:
        {
            background?:
            {
                source?: string;
                sourceType?: string;
            };
            template?: string;
        };
        assetsConfig?: {};
    }
}

declare interface NativeCanvasUndoInfo {
    currentStep?: number;
    canRedo?: number;
    canUndo?: number;
}

declare interface NativeCanvasThumbnailInfo {
    page?: number;
}

declare interface NativeCanvasInsertInfo {
    isEditing?: boolean;
    isReloadBook?: boolean;
    stickerType?: string;
}

declare interface NativeCanvasEvent extends Event {
    /** the eventname, either loadstart, loadstop, loaderror, or exit. */
    type: string;
    /** the URL that was loaded. */
    url: string;
    /** the error code, only in the case of loaderror. */
    code: number;
    /** the error message, only in the case of loaderror. */
    message: string;
    data: string;
    dataType: string;
    dataCompress: string;
    originalRect: string;
    displayRect: string;
    domain: string;
    favicon: string;
    title: string;
    scale: string;
    offsetX: string;
    offsetY: string;
}

// declare enum ObjectType {
//     IMAGE,
//     TEXT
// }



