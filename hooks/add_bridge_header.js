#!/usr/bin/env node

var fs = require('fs')
var path = require('path')
const encoding = 'utf-8'
const bridgeHeaderImport = [
    '#import <CommonCrypto/CommonCrypto.h>',
    '#import <CocoaLumberjack/CocoaLumberjack.h>',
    '#include "NativeCanvas/NativeCanvasColorLog.h"',
    '#include "UAModalPanel/UAModalPanel.h"',
    '#include "SVGKit/SVGKit.h"',
    '#include "SVGKit/SVGKImage.h"'
]
const headerIds = [
    'com.kcchen.nativecanvas'
    // "com.kcchen.drawingpdf/wrapper"
]
const libIds = [
    'com.kcchen.nativecanvas/lib'
]
const pluginRoot = '$(PROJECT_DIR)/$(PROJECT_NAME)/Plugins'
var bridgeHeader
var bridgeHeaderPath
var xcodeCordovaProj
var headerPath
var libPath
var xcconfigPath
var xcconfig

module.exports = function (context) {
    // console.log(context)

    console.log('> check ios... ' + (context.opts.cordova.platforms.indexOf('ios') !== -1))
    if (context.opts.cordova.platforms.indexOf('ios') === -1) return;

    console.log('######################### ADD BRIDGE HEADER #########################')

    if (prepare()) {
        addSearchPath()
        addBridgeHeader()
        fs.writeFileSync(xcconfigPath, xcconfig, encoding);
        fs.writeFileSync(bridgeHeaderPath, bridgeHeader, encoding);
    }

    console.log('------------------------- ADD BRIDGE HEADER  -------------------------')

    return

    function getXcconfigPath(context) {
        var root = path.join(context.opts.projectRoot, 'platforms', 'ios', 'cordova', 'build.xcconfig')
        return root
    }

    function openXcConfig() {
        if (!xcconfigPath) xcconfigPath = getXcconfigPath(context)
        try {
            xcconfig = fs.readFileSync(xcconfigPath, encoding);
            console.log('> check xcconfigPath [' + xcconfigPath + ']... ' + !(!xcconfig))
        } catch (e) {
            console.error('> check xcconfigPath [' + xcconfigPath + ']... NOT EXISTED.')
            console.error(e)
            return
        }
        return !(!xcconfig);
    }

    function merge(list, itemList) {
        var items = list.split(' ')
        var isExisted = false

        for (var item of itemList) {
            isExisted = false
            for (var entry of items) {
                if (entry === item) {
                    isExisted = true
                }
            }
            if (!isExisted) {
                items.push(item)
            }
        }

        return items.join(' ')
    }

    function addSearchPath() {
        console.log('> add Search Path...')
        try {
            var matches
            var lines = xcconfig.split('\n')
            var isLibExisted = false
            var isHeaderExisted = false
            for (var lineKey in lines) {
                matches = lines[lineKey].match(/^(LIBRARY_SEARCH_PATHS[\s|\t]*=[\s|\t]*)(.*)/)
                if (matches) {
                    console.log('> found and modify LIBRARY_SEARCH_PATHS ... ' + matches[1] + matches[2])
                    lines[lineKey] = matches[1] + merge(matches[2], libPath)
                    isLibExisted = true
                }
                matches = lines[lineKey].match(/^(HEADER_SEARCH_PATHS[\s|\t]*=[\s|\t]*)(.*)/)
                if (matches) {
                    console.log('> found and modify HEADER_SEARCH_PATHS ... ' + matches[1] + matches[2])
                    lines[lineKey] = matches[1] + merge(matches[2], headerPath)
                    isHeaderExisted = true
                }
            }
            if (!isLibExisted) {
                lines.push('LIBRARY_SEARCH_PATHS = ' + libPath)
            }
            if (!isHeaderExisted) {
                lines.push('HEADER_SEARCH_PATHS = ' + headerPath)
            }
            xcconfig = lines.join('\n')
        } catch (ex) {
            console.error(ex);
        }
    }

    function addBridgeHeader() {
        console.log('> add Bridge Header...')
        try {
            var lines = bridgeHeader.split('\n')
            for (var importEntry of bridgeHeaderImport) {
                var isExisted = false
                for (var lineKey in lines) {
                    if (lines[lineKey] === importEntry) {
                        isExisted = true
                    }
                }
                if (!isExisted) {
                    lines.push(importEntry)
                }
            }
            bridgeHeader = lines.join('\n')
            // console.log(lines)
        } catch (ex) {
            console.error('\nThere was an error fetching your ../../build.json file.');
        }
    }

    function openBridgeHeader() {
        if (!bridgeHeaderPath) bridgeHeaderPath = getBridgeHeaderPath(context)
        try {
            bridgeHeader = fs.readFileSync(bridgeHeaderPath, encoding);
            console.log('> check bridgeHeaderPath [' + bridgeHeaderPath + ']... ' + !(!bridgeHeader))
        } catch (e) {
            console.error('> check bridgeHeaderPath [' + bridgeHeaderPath + ']... NOT EXISTED.')
            console.error(e)
            return
        }
        return !(!bridgeHeader);
    }

    function wrapPath(pathes) {
        var finalPathes = []
        for (var p of pathes) {
            finalPathes.push('"' + path.join(pluginRoot, p) + '"')
        }
        return finalPathes
    }

    function prepare() {
        if (!openXcConfig()) return false;
        if (!openBridgeHeader()) return false;

        headerPath = wrapPath(headerIds)
        libPath = wrapPath(libIds)

        console.log('> prepared!')
        return true
    }

    function getBridgeHeaderPath(context) {
        var root = path.join(context.opts.projectRoot, 'platforms', 'ios')

        var xcodeProjDir;
        xcodeCordovaProj;

        try {
            xcodeProjDir = fs.readdirSync(root).filter(function (e) {
                return e.match(/\.xcodeproj$/i);
            })[0];
            if (!xcodeProjDir) {
                throw new Error('The provided path "' + root + '" is not a Cordova iOS project.');
            }

            var cordovaProjName = xcodeProjDir.substring(xcodeProjDir.lastIndexOf(path.sep) + 1, xcodeProjDir.indexOf('.xcodeproj'));
            xcodeCordovaProj = path.join(root, cordovaProjName);
        } catch (e) {
            throw new Error('The provided path "' + root + '" is not a Cordova iOS project.');
        }

        return path.join(xcodeCordovaProj, 'Bridging-Header.h')
    }
};
