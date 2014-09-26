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

(function () {
    // removes the current script tag (so that we don't disturb the page)
    var currentScript = document.currentScript;
    currentScript.parentNode.removeChild(currentScript);

    var sendResponse = function (response) {
        window.postMessage({
            __selenium_java_robot_response__ : response
        }, "*");
    };

    var createCallback = function (id) {
        var waiting = true;
        return function (response) {
            if (waiting) {
                waiting = false; // don't allow multiple calls of this method
                sendResponse({
                    success : true,
                    result : response,
                    id : id
                });
            }
        };
    };

    var commands = {
        "executeScript" : function (request) {
            var args = request.args, script = args[0], scriptArgs = args[1];
            var fn = new Function(script);
            var res = fn.apply(null, scriptArgs);
            sendResponse({
                success : true,
                result : res,
                id : request.id
            });
        },
        "executeAsyncScript" : function (request) {
            var args = request.args, script = args[0], scriptArgs = args[1];
            var fn = new Function(script);
            scriptArgs.push(createCallback(request.id));
            fn.apply(null, scriptArgs);
        }
    };

    window.addEventListener("message", function (event) {
        // Listens to messages from the content script
        if (event.source != window || !event.data) {
            return;
        }
        var request = event.data.__selenium_java_robot_request__;
        if (request) {
            var command = request.command;
            if (commands.hasOwnProperty(command)) {
                var fn = commands[request.command];
                try {
                    fn(request);
                } catch (e) {
                    sendResponse({
                        success : false,
                        result : e + "",
                        id : request.id
                    });
                }
            } else {
                sendResponse({
                    success : false,
                    result : "Unknown command: " + command,
                    id : request.id
                });
            }
        }
    }, false);
})()
