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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;

import org.apache.commons.exec.OS;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

public abstract class LocalRobotizedWebDriverFactory implements IRobotizedWebDriverFactory {

    private IRobot robot;

    private static void numLockStateWorkaround() {
        try {
            // Shift key is not kept pressed while using keyPress method
            // cf https://forums.oracle.com/thread/2232592
            // The workaround is to use the following line:
            Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
            SeleniumJavaRobot.log("Num lock state was successfully changed.");
        } catch (UnsupportedOperationException e) {
            SeleniumJavaRobot.log("Did not change num lock state: " + e);
        }
    }

    public LocalRobotizedWebDriverFactory() {
        try {
            robot = new LocalRobot(new Robot());
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
        numLockStateWorkaround();
    }

    public abstract RemoteWebDriver createWebDriver();

    public RobotizedWebDriver createRobotizedWebDriver() {
        RemoteWebDriver driver = createWebDriver();
        if (OS.isFamilyMac()) {
            try {
                // put the browser in the foreground:
                Runtime.getRuntime().exec("open -a \"" + driver.getCapabilities().getBrowserName() + "\"");
            } catch (Exception e) {
            }
        }
        driver.manage().window().maximize();
        return new RobotizedWebDriver(robot, driver);
    }

    public static class LocalFirefox extends LocalRobotizedWebDriverFactory {
        private final FirefoxProfile firefoxProfile;

        public LocalFirefox(FirefoxProfile firefoxProfile) {
            this.firefoxProfile = firefoxProfile;
        }

        @Override
        public RemoteWebDriver createWebDriver() {
            return new FirefoxDriver(firefoxProfile);
        }
    }

    public static class LocalBrowser<T extends RemoteWebDriver> extends LocalRobotizedWebDriverFactory {
        private final Constructor<T> webdriverClass;

        public LocalBrowser(Class<T> webdriverClass) {
            try {
                this.webdriverClass = webdriverClass.getConstructor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public RemoteWebDriver createWebDriver() {
            try {
                return webdriverClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static LocalRobotizedWebDriverFactory createRobotizedWebDriverFactory(String browser) {
        if (BrowserType.FIREFOX.equalsIgnoreCase(browser)) {
            FirefoxProfile firefoxProfile = null;
            String firefoxProfileProperty = System.getProperty("webdriver.firefox.profile");
            if (firefoxProfileProperty == null) {
                ProfilesIni allProfiles = new ProfilesIni();
                // Use the default profile to make extensions available,
                // and especially to ease debugging with Firebug
                firefoxProfile = allProfiles.getProfile("default");
            }
            return new LocalFirefox(firefoxProfile);
        } else if (BrowserType.SAFARI.equalsIgnoreCase(browser)) {
            return new LocalBrowser<SafariDriver>(SafariDriver.class);
        } else if (BrowserType.CHROME.equalsIgnoreCase(browser)) {
            return new LocalBrowser<ChromeDriver>(ChromeDriver.class);
        } else if (BrowserType.IE.equalsIgnoreCase(browser)) {
            return new LocalBrowser<InternetExplorerDriver>(InternetExplorerDriver.class);
        } else {
            throw new RuntimeException("Unknown browser value: " + browser);
        }

    }
}
