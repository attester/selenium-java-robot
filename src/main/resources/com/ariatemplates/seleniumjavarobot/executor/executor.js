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

var SeleniumJavaRobot = window.SeleniumJavaRobot;
if (!SeleniumJavaRobot) {
    var callIds = 0;
    var calls = [];
    var slice = calls.slice;
    var notifyJava = null;

    var notifyJavaIfNeeded = function () {
        if (notifyJava && calls.length > 0) {
            var fn = notifyJava;
            var arg = calls[0];
            notifyJava = null;
            fn(arg.call);
        }
    };

    SeleniumJavaRobot = window.SeleniumJavaRobot = {
        __getInfo : function (expectsStatus, cb) {
            if (expectsStatus) {
                cb();
            } else {
                notifyJava = cb;
                notifyJavaIfNeeded();
            }
        },
        __callback : function (callId, success, result) {
            var curCall = calls[0];
            if (curCall && calls[0].call.id == callId) {
                calls.shift();
                var curCallback = curCall.callback;
                if (curCallback && typeof curCallback.fn == "function") {
                    curCallback.fn.call(curCallback.scope, {
                        success : success,
                        result : result
                    }, curCallback.args);
                }
            }
        }
    };

    var createFunction = function (name, argsNumber) {
        return SeleniumJavaRobot[name] = function () {
            var curCallId = "c" + callIds;
            callIds++;
            calls.push({
                call : {
                    name : name,
                    id : curCallId,
                    args : slice.call(arguments, 0, argsNumber)
                },
                callback : arguments[argsNumber]
            });
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
}

return SeleniumJavaRobot.__getInfo.apply(SeleniumJavaRobot, arguments);
