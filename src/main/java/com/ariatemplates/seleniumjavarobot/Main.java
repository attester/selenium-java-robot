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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.exec.OS;
import org.openqa.selenium.remote.BrowserType;

public class Main {
    public final static List<String> BROWSERS_LIST = Arrays.asList(BrowserType.FIREFOX, BrowserType.SAFARI, BrowserType.CHROME, BrowserType.IE);

    public static void main(String[] args) throws Exception {
        SeleniumJavaRobot seleniumJavaRobot = new SeleniumJavaRobot();
        String browser;
        seleniumJavaRobot.autoRestart = false;
        if (OS.isFamilyMac()) {
            browser = "safari";
        } else {
            browser = "firefox";
        }
        seleniumJavaRobot.url = "http://localhost:7777/__attester__/slave.html";
        String usageString = String
                .format("Usage: selenium-java-robot [options]\nOptions:\n  --auto-restart\n  --url <url> [default: %s]\n  --browser <browser> [default: %s]\n\nAccepted browser values: %s",
                        seleniumJavaRobot.url, browser, BROWSERS_LIST.toString());
        for (int i = 0, l = args.length; i < l; i++) {
            String curParam = args[i];
            if ("--browser".equalsIgnoreCase(curParam) && i + 1 < l) {
                browser = args[i + 1];
                i++;
            } else if ("--url".equalsIgnoreCase(curParam) && i + 1 < l) {
                seleniumJavaRobot.url = args[i + 1];
                i++;
            } else if ("--auto-restart".equalsIgnoreCase(curParam)) {
                seleniumJavaRobot.autoRestart = true;
            } else if ("--version".equalsIgnoreCase(curParam)) {
                System.out.println(Main.class.getPackage().getImplementationVersion());
                return;
            } else if ("--help".equalsIgnoreCase(curParam)) {
                System.out.println(usageString);
                return;
            } else {
                System.err.println("Unknown command line option: " + curParam);
                System.err.println(usageString);
                return;
            }
        }
        seleniumJavaRobot.robotizedWebDriverFactory = LocalRobotizedWebDriverFactory.createRobotizedWebDriverFactory(browser);
        seleniumJavaRobot.start();
        closeOnStreamEnd(seleniumJavaRobot, System.in);
        closeOnProcessEnd(seleniumJavaRobot);
    }

    private static void closeOnProcessEnd(final SeleniumJavaRobot seleniumJavaRobot) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    seleniumJavaRobot.stop();
                } catch (InterruptedException e) {
                }
            }
        }));
    }

    private static void closeOnStreamEnd(final SeleniumJavaRobot seleniumJavaRobot, final InputStream inputStream) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    while (inputStream.read() > -1) {
                        // do nothing
                    }
                } catch (IOException e) {
                }
                try {
                    seleniumJavaRobot.stop();
                } catch (InterruptedException e) {
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
