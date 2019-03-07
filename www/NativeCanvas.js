const PLUGIN_NAME = 'NativeCanvas';

var channel = require('cordova/channel');
// var urlutil = require('cordova/urlutil');
// var channels = {};
var nc;

function NativeCanvas() {
    // for (var c in NativeCanvasChannel) {
    //     console.log('> NC create channel ' + c)
    //     this.channels[c] = channel.create(c);
    // }
    // MINI_MAP("MiniMap"),
    // TEXT_INPUT("TextInput"),
    // EXIT("NativeCanvasExit"),
    // UNDO_MANAGER("UndoManager"),
    // ERROR("Error");
    this.channels = {
        // NativeCanvasChannel.MINI_MAP: channel.create('MiniMap'),
        'MiniMap': channel.create('MiniMap'),
        'TextInput': channel.create('TextInput'),
        'NativeCanvasExit': channel.create('NativeCanvasExit'),
        'UndoManager': channel.create('UndoManager'),
        'Thunbnail': channel.create('Thunbnail'),
        'ProjectManager': channel.create('ProjectManager'),
        'InsertManager': channel.create('InsertManager'),
        'Error': channel.create('Error')
    };
}

NativeCanvas.prototype = {
    _eventHandler: function (event) {
        if (event && (event.type in this.channels)) {
            console.log('> NC fire event ' + event.type)
            this.channels[event.type].fire(event);
        }
    },
    addEventListener: function (eventname, f) {
        if (eventname in this.channels) {
            console.log('> subscrib ' + eventname)
            this.channels[eventname].subscribe(f)
            return Promise.resolve(true);
        }
        return Promise.resolve(false);
    },
    removeEventListener: function (eventname, f) {
        if (eventname in this.channels) {
            console.log('> unsubscrib ' + eventname)
            this.channels[eventname].unsubscribe(f)
            return Promise.resolve(true);
        }
        return Promise.resolve(false);
    }
}

exports.register = function (callbacks) {
    if (!nc) {
        nc = new NativeCanvas();
        if (nc) {
            callbacks = callbacks || {};
            for (var callbackName in callbacks) {
                console.log('> NC register ' + callbackName + ' - ' + callbacks[callbackName])
                nc.addEventListener(callbackName, callbacks[callbackName]);
            }

            var cb = function (eventname) {
                nc._eventHandler(eventname);
            };

            window.cordova.exec(cb, cb, PLUGIN_NAME, 'register')
            return Promise.resolve(nc);
        } else {
            return Promise.reject('NativeCanvas can not init')
        }
    } else {
        return Promise.resolve(nc)
    }
}

exports.openBook = function (library, bookshelf, book, type, page) {
    if (library === void 0 || library === null) {
        library = '';
    }
    if (bookshelf === void 0 || bookshelf === null) {
        bookshelf = '';
    }
    if (book === void 0 || book === null) {
        book = '';
    }
    if (type === void 0 || type === null) {
        type = '';
    }
    if (page === void 0 || page === null) {
        page = 9999;
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'openBook',
            [library, bookshelf, book, type, page]
        );
    });
}

exports.INIT = function (library, bookshelf, book, initPages, bookTemplateId) {
    if (library === void 0 || library === null) {
        return Promise.reject(1)
    }
    if (bookshelf === void 0 || bookshelf === null) {
        return Promise.reject(1)
    }
    if (book === void 0 || book === null) {
        return Promise.reject(1)
    }
    if (initPages === void 0 || initPages === null || initPages === 0) {
        return Promise.reject(1)
    }
    if (bookTemplateId === void 0 || bookTemplateId === null) {
        bookTemplateId = ''
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'INIT',
            [library, bookshelf, book, initPages, bookTemplateId]
        );
    });
}

// library:string, bookshelf?:string, book?:string, page?:number, property?: NativeCanvasProperties
exports.SET_PROFILE = function (property, library, bookshelf, book, page) {
    if (library === void 0 || library === null) {
        return Promise.reject(10)
    }
    if (bookshelf === void 0 || bookshelf === null) {
        bookshelf = '';
    }
    if (book === void 0 || book === null) {
        book = '';
    }
    if (page === void 0 || page === null) {
        page = 9999;
    }
    if (property === void 0 || property === null) {
        return Promise.reject(10)
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'SET_PROFILE',
            [property, library, bookshelf, book, page]
        );
    });
}

exports.setProfile = function (property, isReload, library, bookshelf, book, page) {
    if (library === void 0 || library === null) {
        return Promise.reject(10)
    }
    if (bookshelf === void 0 || bookshelf === null) {
        bookshelf = '';
    }
    if (isReload === void 0 || isReload === null) {
        isReload = true;
    }
    if (book === void 0 || book === null) {
        book = '';
    }
    if (page === void 0 || page === null) {
        page = 9999;
    }
    if (property === void 0 || property === null) {
        return Promise.reject(10)
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'setProfile',
            [property, isReload, library, bookshelf, book, page]
        );
    });
}

exports.openPage = function (page, isInsert) {
    if (page === void 0 || page === null) {
        page = 9999;
        return Promise.reject(5)
    }
    if (isInsert === void 0 || isInsert === null) {
        isInsert = false;
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'openPage',
            [page, isInsert]
        );
    });
}

exports.saveBook = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'saveBook',
            []
        );
    });
}

exports.savePage = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'savePage',
            []
        );
    });
}

exports.deleteBook = function (id) {
    if (id === void 0 || id === null) {
        id = '';
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'deleteBook',
            [id]
        );
    });
}

exports.deletePage = function (page) {
    if (page === void 0 || page === null) {
        page = 0;
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'deletePage',
            [page]
        );
    });
}

exports.isRegistered = function () {
    if (nc) {
        return new Promise(function (resolve, reject) {
            window.cordova.exec(
                function (args) {
                    if (args === 0) {
                        resolve(false);
                    } else {
                        resolve(true);
                    }
                },
                function (args) {
                    reject(args);
                },
                PLUGIN_NAME,
                'isRegistered',
                []
            );
        });
    } else {
        return Promise.resolve(false)
    }
}

exports.getNativeCanvas = function () {
    if (nc) {
        return Promise.resolve(nc);
    } else {
        return Promise.reject('NativeCanvas can not init')
    }
}

exports.openCanvas = function (x, y, width, height, backgroundColor, isBack) {
    if (x === void 0 || x === null) {
        x = 0;
    }
    if (y === void 0 || y === null) {
        y = 0;
    }
    if (width === void 0 || width === null) {
        width = 0;
    }
    if (height === void 0 || height === null) {
        height = 0;
    }
    if (backgroundColor === void 0 || backgroundColor === null) {
        backgroundColor = '#FFFFFF';
    }
    if (isBack === void 0 || isBack === null) {
        isBack = true;
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'openCanvas',
            [x, y, width, height, backgroundColor, isBack]
        );
    });
}
exports.show = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'show',
            []
        );
    });
}
exports.hide = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'hide',
            []
        );
    });
}
exports.isShow = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'isShow',
            []
        );
    });
}
exports.closeCanvas = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'closeCanvas',
            []
        );
    });
}
exports.isOpened = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'isOpened',
            []
        );
    });
}
exports.setPosition = function (x, y, width, height) {
    if (x === void 0 || x === null) {
        x = 0;
    }
    if (y === void 0 || y === null) {
        y = 0;
    }
    if (width === void 0 || width === null) {
        width = document.width;
    }
    if (height === void 0 || height === null) {
        height = document.height;
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'setPosition',
            [x, y, width, height]
        );
    });
}
exports.setTouch = function (isEnable) {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'setTouch',
            [isEnable]
        );
    });
}
exports.toggleDebugMode = function (isDebugMode) {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'toggleDebugMode',
            [isDebugMode]
        );
    });
}
exports.setProperty = function (type, color, width, alpha) {
    if (type === void 0 || type === null) {
        type = 9999;
    }
    if (color === void 0 || color === null) {
        color = '';
    }
    if (width === void 0 || width === null) {
        width = 9999;
    }
    if (alpha === void 0 || alpha === null) {
        alpha = 9999;
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'setProperty',
            [type, color, width, alpha]
        );
    });
}
exports.clearCanvas = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'clearCanvas',
            []
        );
    });
}
exports.CLEARDATA = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'CLEARDATA',
            []
        );
    });
}
exports.addObject = function (type, object) {
    if (type === void 0 || type === null) {
        type = 9999;
    }
    if (object === void 0 || object === null) {
        object = {};
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'addObject',
            [type, object]
        );
    });
}
exports.undoOpen = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'undoOpen',
            []
        );
    });
}
/**
 * undo: step = -n;
 * redo step = n;
 * query step = 0;
 * reject need to handle
 */
exports.undoStep = function (step) {
    if (step === void 0 || step === null || step === undefined) {
        return Promise.reject('Invalid setp');
    }
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'undoStep',
            [step]
        );
    });
}
exports.undoClose = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'undoClose',
            []
        );
    });
}
exports.play = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'play',
            []
        );
    });
}
exports.mapOpen = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'mapOpen',
            []
        );
    });
}
exports.mapMove = function (x, y) {
    if (x === void 0 || x === null || x === undefined) {
        return;
    }
    if (y === void 0 || y === null || y === undefined) {
        return;
    }
    window.cordova.exec(
        function () {
        },
        function () {
        },
        PLUGIN_NAME,
        'mapMove',
        [x, y]
    );
}
exports.mapPosition = function (x, y) {
    if (x === void 0 || x === null || x === undefined) {
        return;
    }
    if (y === void 0 || y === null || y === undefined) {
        return;
    }
    window.cordova.exec(
        function () {
        },
        function () {
        },
        PLUGIN_NAME,
        'mapPosition',
        [x, y]
    );
}
exports.mapScale = function (scale) {
    if (scale === void 0 || scale === null || scale === undefined) {
        return;
    }
    window.cordova.exec(
        function () {
        },
        function () {
        },
        PLUGIN_NAME,
        'mapScale',
        [scale]
    );
}
exports.mapClose = function () {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                // resolve(JSON.parse(decodeURIComponent(args)));
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'mapClose',
            []
        );
    });
}
exports.toggleViewMode = function (isViewMode) {
    return new Promise(function (resolve, reject) {
        window.cordova.exec(
            function (args) {
                resolve(args);
            },
            function (args) {
                reject(args);
            },
            PLUGIN_NAME,
            'toggleViewMode',
            [isViewMode]
        );
    });
}

