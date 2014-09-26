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

package com.ariatemplates.seleniumjavarobot;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

public class RemoteWebDriverBrowser implements IBrowser {
    private final RemoteWebDriver webdriver;

    public RemoteWebDriverBrowser(RemoteWebDriver webdriver) {
        if (webdriver == null) {
            throw new NullPointerException();
        }
        this.webdriver = webdriver;
    }

    public Object executeScript(String script, Object... args) {
        return webdriver.executeScript(script, args);
    }

    public Object executeAsyncScript(String script, Object... args) {
        return webdriver.executeAsyncScript(script, args);
    }

    public void quit() {
        webdriver.quit();
    }

    public void get(String url) {
        webdriver.get(url);
    }

    public Point getWindowPosition() {
        return webdriver.manage().window().getPosition();
    }

    public Dimension getWindowSize() {
        return webdriver.manage().window().getSize();
    }

    public void setScriptTimeout(long time, TimeUnit unit) {
        webdriver.manage().timeouts().setScriptTimeout(time, unit);
    }
}
