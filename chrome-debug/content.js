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

var head = document.getElementsByTagName("head")[0] || document.body;
var pageScript = document.createElement("script");
pageScript.setAttribute("type", "text/javascript");
pageScript.setAttribute("src", chrome.extension.getURL("page.js"));
head.appendChild(pageScript);
var currentCallback = null;
var port = chrome.runtime.connect();

window.addEventListener("message", function (event) {
    if (event.source != window || !event.data) {
        return;
    }
    // Listens to responses only:
    var response = event.data.__selenium_java_robot_response__;
    if (response) {
        port.postMessage(response);
    }
}, false);

port.onMessage.addListener(function (request) {
    window.postMessage({
        "__selenium_java_robot_request__" : request
    }, "*");
});