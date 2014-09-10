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

var backgroundColor = arguments[0];
var borderWidth = arguments[1];

var applyStyle = function (docStyle) {
    docStyle.overflow = "hidden";
    docStyle.borderWidth = "0px";
    docStyle.margin = "0px";
    docStyle.width = docStyle.height = "100%";
};

applyStyle(document.documentElement.style);
applyStyle(document.body.style);

var iFrame = document.createElement("iframe");
var iFrameStyle = iFrame.style;
iFrame.setAttribute("frameborder", "0");
iFrameStyle.position = "absolute";
iFrameStyle.left = iFrameStyle.top = "0px";
applyStyle(iFrame.style);
document.body.appendChild(iFrame);

// use the iframe to use standard-compliant mode
var iFrameWindow = iFrame.contentWindow;
var iFrameDoc = iFrameWindow.document;
iFrameDoc.open();
iFrameDoc.write("<!doctype html><html><body></body></html>");
iFrameDoc.close();

applyStyle(iFrameDoc.documentElement.style);
applyStyle(iFrameDoc.body.style);

var element = iFrameDoc.createElement("div");
var style = element.style;
style.position = "absolute";
style.width = style.height = "100%";
style.left = style.top = "0px";
style.backgroundColor = backgroundColor;
iFrameDoc.body.appendChild(element);
var width = element.offsetWidth;
var height = element.offsetHeight;

var borderElement = iFrameDoc.createElement("div");
style = borderElement.style;
style.position = "absolute";
style.backgroundColor = backgroundColor;
style.border = borderWidth + "px solid rgb(100,100,100)";
style.left = style.top = "0px";
style.width = (width - 2 * borderWidth) + "px";
style.height = (height - 2 * borderWidth) + "px";
iFrameDoc.body.appendChild(borderElement);
style.zoom = 1;

return {
    width : width,
    height : height
};
