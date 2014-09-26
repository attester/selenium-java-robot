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
var testTab;
var testPort;
var webdriverTab;
var webdriverPort;
var statusTestPage = chrome.extension.getURL("status.htm");
var slice = [].slice;

// Initialize tabs:
chrome.tabs.update(null, {
    url : chrome.extension.getURL("webdriver.htm"),
    pinned : true
}, function (tab) {
    webdriverTab = tab;
    chrome.tabs.create({
        url : statusTestPage,
        active : true
    }, function (tab) {
        testTab = tab;
    });
});

var injectScript = function (tab) {
    var url = tab.url;
    var needScriptInjection = url.substr(0, statusTestPage.length) != statusTestPage;
    if (needScriptInjection) {
        return chrome.tabs.executeScript(tab.id, {
            file : "content.js",
            runAt : "document_start"
        }, function () {
            var lastError = chrome.runtime.lastError;
            if (lastError) {
                return chrome.tabs.update(tab.id, {
                    url : statusTestPage + "?url=" + encodeURIComponent(url)
                });
            }
        });
    }
};

chrome.tabs.onUpdated.addListener(function (tabId, changeInfo, tab) {
    if (testTab && tabId == testTab.id && changeInfo.status == "loading") {
        injectScript(tab);
    }
});

var curMessageId = null;
var waitingForTestResponse = false;
var sendResponse = function (response) {
    if (curMessageId != null && response.id === curMessageId) {
        console.log("Sending response ", response);
        curMessageId = null;
        waitingForTestResponse = false;
        webdriverPort.postMessage(response);
    }
};

// state management
var defer = Promise.defer();
var setTestPort = function (port) {
    if (port != testPort) {
        if (testPort != null && port != null) {
            console.error("Overriding existing testPort!"); // this should never happen
            return;
        }
        testPort = port;
        var savedDefer = defer;
        defer = Promise.defer();
        savedDefer.resolve();
    }
};
var waitForTestPort = function (callback) {
    if (testPort != null) {
        callback();
    } else {
        defer.promise.then(callback);
    }
};
var waitForTestPortUnload = function (callback) {
    if (testPort == null) {
        callback();
    } else {
        defer.promise.then(callback);
    }
};

var processMessage = function (request) {
    console.log("Processing request ", request);
    if (curMessageId != null) {
        console.warn("New message received when old one did not have any response (it probably timed out).");
    }
    curMessageId = request.id;
    if (request.command == "get") {
        var url = request.args[0];
        chrome.tabs.update(testTab.id, {
            url : url
        }, function () {
            waitForTestPortUnload(function () {
                sendResponse({
                    success : true,
                    id : request.id
                });
            });
        });
    } else {
        waitForTestPort(function () {
            waitingForTestResponse = true;
            testPort.postMessage(request);
        });
    }
};

var initTestPort = function (port) {
    setTestPort(port);
    port.onDisconnect.addListener(function () {
        setTestPort(null);
        if (waitingForTestResponse) {
            sendResponse({
                success : false,
                result : "Detected a page unload event",
                id : curMessageId
            });
        }
    });
    port.onMessage.addListener(sendResponse);
};

var initWebdriverPort = function (port) {
    webdriverPort = port;
    port.onMessage.addListener(processMessage);
};

chrome.runtime.onConnect.addListener(function (port) {
    var sender = port.sender;
    if (!sender || !sender.tab) {
        return;
    }
    if (sender.tab.id == testTab.id) {
        initTestPort(port);
    } else if (sender.tab.id == webdriverTab.id) {
        initWebdriverPort(port);
    } else {
        port.disconnect();
    }
});
