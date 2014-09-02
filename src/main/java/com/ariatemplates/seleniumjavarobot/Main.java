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

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.exec.OS;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import com.ariatemplates.seleniumjavarobot.calibrator.Calibrator;
import com.ariatemplates.seleniumjavarobot.executor.Executor;

public class Main {
    public final static List<String> BROWSERS_LIST = Arrays.asList(BrowserType.FIREFOX, BrowserType.SAFARI, BrowserType.CHROME, BrowserType.IE);

    public static void main(String[] args) throws Exception {
        String browser = "";
        if (OS.isFamilyMac()) {
            browser = "safari";
        } else {
            browser = "firefox";
        }
        String url = "http://localhost:7777/__attester__/slave.html";
        String usageString = String
                .format("Usage: java -jar selenium-java-robot.jar [options]\nOptions:\n  --url <url> [default: %s]\n  --browser <browser> [default: %s]\n\nAccepted browser values: %s",
                        url, browser, BROWSERS_LIST.toString());
        for (int i = 0, l = args.length; i < l; i++) {
            String curParam = args[i];
            if ("--browser".equalsIgnoreCase(curParam) && i + 1 < l) {
                browser = args[i + 1];
                i++;
            } else if ("--url".equalsIgnoreCase(curParam) && i + 1 < l) {
                url = args[i + 1];
                i++;
            } else if ("--help".equalsIgnoreCase(curParam)) {
                System.out.println(usageString);
                return;
            }
        }
        RemoteWebDriver driver = createWebDriver(browser);
        startDriver(driver, url);
        try {
            driver.quit();
        } catch (Exception e) {
        }
        System.exit(0);
    }

    private static void numLockStateWorkaround() {
        try {
            // Shift key is not kept pressed while using keyPress method
            // cf https://forums.oracle.com/thread/2232592
            // The workaround is to use the following line:
            System.out.println("Setting num lock state...");
            Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
        } catch (UnsupportedOperationException e) {
            System.out.println("Setting num lock state is not supported: " + e);
        }
    }

    public static void startDriver(RemoteWebDriver driver, String url) throws Exception {
        Robot robot = new Robot();
        Point offset = Calibrator.calibrate(driver, robot);
        System.out.println("Computed offset: " + offset);
        driver.get(url);
        numLockStateWorkaround();
        Executor.startExecutor(driver, robot, offset);
    }

    public static RemoteWebDriver createWebDriver(String browser) {
        RemoteWebDriver driver;
        if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
            driver = new FirefoxDriver();
        } else if (BrowserType.SAFARI.equalsIgnoreCase(browser)) {
            driver = new SafariDriver();
        } else if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
            driver = new ChromeDriver();
        } else if (BrowserType.IE.equalsIgnoreCase(browser)) {
            driver = new InternetExplorerDriver();
        } else {
            throw new RuntimeException("Unknown browser value: " + browser);
        }

        if (OS.isFamilyMac()) {
            try {
                // put the browser in the foreground:
                Runtime.getRuntime().exec("open -a " + browser);
            } catch (Exception e) {
            }
        }
        driver.manage().window().maximize();
        return driver;
    }
}
