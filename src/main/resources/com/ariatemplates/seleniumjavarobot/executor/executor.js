/*
 * Copyright 2014 Amadeus s.a.s.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var callbackCounter = 0;
var callbacks = {};
var calls = [];
var slice = calls.slice;
var notifyJava = null;

var notifyJavaIfNeeded = function () {
    if (notifyJava && calls.length > 0) {
        var fn = notifyJava;
        var arg = calls.shift();
        notifyJava = null;
        fn(arg);
    }
};

var SeleniumJavaRobot = window.SeleniumJavaRobot = {
    __getCall : function (cb) {
        notifyJava = cb;
        notifyJavaIfNeeded();
    },
    __callback : function (callbackId, success, result) {
        var curCallback = callbacks[callbackId];
        delete callbacks[callbackId];
        if (curCallback && curCallback.fn) {
            curCallback.fn.call(curCallback.scope, {
                success : success,
                result : result
            }, curCallback.args);
        }
    }
};

var createFunction = function (name, argsNumber) {
    return SeleniumJavaRobot[name] = function () {
        var callbackId = "c" + callbackCounter;
        callbackCounter++;
        calls.push({
            name : name,
            cb : callbackId,
            args : slice.call(arguments, 0, argsNumber)
        });
        callbacks[callbackId] = arguments[argsNumber];
        notifyJavaIfNeeded();
    };
};

createFunction("mouseMove", 2);
createFunction("smoothMouseMove", 5);
createFunction("mousePress", 1);
createFunction("mouseRelease", 1);
createFunction("mouseWheel", 1);
createFunction("keyPress", 1);
createFunction("keyRelease", 1);
createFunction("getOffset", 0);
