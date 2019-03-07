#!/usr/bin/env node

var fs = require('fs')
var path = require('path')
const encoding = 'utf-8'
const bridgeHeaderImport = [
    '#import <CommonCrypto/CommonCrypto.h>',
    '#import <CocoaLumberjack/CocoaLumberjack.h>',
    '#include "NativeCanvas/NativeCanvasColorLog.h"',
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

    console.log('######################### REMOVE BRIDGE HEADER #########################')

    if (prepare()) {
        removeSearchPath()
        removeBridgeHeader()
        // removeSwiftVersion()
        fs.writeFileSync(xcconfigPath, xcconfig, encoding);
        fs.writeFileSync(bridgeHeaderPath, bridgeHeader, encoding);
    }

    console.log('------------------------- REMOVE BRIDGE HEADER -------------------------')

    return

    function removeSearchPath() {
        console.log('> remove Search Path...')
        try {
            var matches
            var lines = xcconfig.split('\n')
            for (var lineKey in lines) {
                if (!lines[lineKey]) continue
                matches = lines[lineKey].match(/^(LIBRARY_SEARCH_PATHS[\s|\t]*=[\s|\t]*)(.*)/)
                if (matches) {
                    console.log('> found and modify LIBRARY_SEARCH_PATHS ... ' + matches[1] + matches[2])
                    var libs = remove(matches[2], libPath)
                    if (libs !== '') {
                        lines[lineKey] = matches[1] + libs
                    } else {
                        lines.splice(lineKey, 1)
                    }
                }
                if (!lines[lineKey]) continue
                matches = lines[lineKey].match(/^(HEADER_SEARCH_PATHS[\s|\t]*=[\s|\t]*)(.*)/)
                if (matches) {
                    console.log('> found and modify HEADER_SEARCH_PATHS ... ' + matches[1] + matches[2])
                    var headers = remove(matches[2], headerPath)
                    if (headers !== '') {
                        lines[lineKey] = matches[1] + headers
                    } else {
                        lines.splice(lineKey, 1)
                    }
                }
            }
            xcconfig = lines.join('\n')
        } catch (ex) {
            console.error(ex);
        }
    }

    function remove(list, itemList) {
        var items = list.split(' ')

        for (var item of itemList) {
            for (var key in items) {
                if (items[key] === item) {
                    items.splice(key, 1)
                }
            }
        }
        if (items.length > 0) {
            return items.join(' ')
        }
        return ''
    }

    function removeBridgeHeader() {
        console.log('> remove Bridge Header...')
        try {
            var lines = bridgeHeader.split('\n')
            for (var importEntry of bridgeHeaderImport) {
                for (var lineKey in lines) {
                    if (lines[lineKey] === importEntry) {
                        lines.splice(lineKey, 1)
                    }
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

    function getXcconfigPath(context) {
        var root = path.join(context.opts.projectRoot, 'platforms', 'ios', 'cordova', 'build.xcconfig')
        return root
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

