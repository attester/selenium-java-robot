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

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DebuggableChrome implements IBrowser {

    private final RemoteWebDriver webdriver;

    public DebuggableChrome() {
        ChromeOptions options = new ChromeOptions();
        String debugExtension = System.getProperty("seleniumjavarobot.chrome.debugextension");
        if (debugExtension == null || !new File(debugExtension).isDirectory()) {
            throw new RuntimeException("Please set the seleniumjavarobot.chrome.debugextension system property to point to the path of the chrome extension.");
        }
        options.addArguments("load-extension=" + debugExtension);
        options.addArguments("start-maximized");
        webdriver = new ChromeDriver(options);
        // waits for the extension page to be loaded:
        (new WebDriverWait(webdriver, 10)).until(ExpectedConditions.presenceOfElementLocated(By.id("selenium-java-robot")));
        webdriver.manage().timeouts().setScriptTimeout(1, TimeUnit.DAYS);
    }

    public Object executeScript(String script, Object... args) {
        return sendCommand("executeScript", script, args);
    }

    public Object executeAsyncScript(String script, Object... args) {
        return sendCommand("executeAsyncScript", script, args);
    }

    public void get(String url) {
        sendCommand("get", url);
    }

    private int curId = 0;

    private Object sendCommand(String commandName, Object... args) {
        curId++;
        @SuppressWarnings("unchecked")
        Map<String, Object> res = (Map<String, Object>) webdriver.executeAsyncScript(
                "executeWebdriverCommand(arguments[0], arguments[1], arguments[2], arguments[3]);", commandName, args, curId);
        if (((Boolean) res.get("success")).booleanValue()) {
            return res.get("result");
        } else {
            throw new WebDriverException((String) res.get("result"));
        }
    }

    public void quit() {
        this.webdriver.quit();
    }

    public Point getWindowPosition() {
        return this.webdriver.manage().window().getPosition();
    }

    public Dimension getWindowSize() {
        return this.webdriver.manage().window().getSize();
    }

    public void setScriptTimeout(long time, TimeUnit unit) {
        this.webdriver.manage().timeouts().setScriptTimeout(time, unit);
    }

}
