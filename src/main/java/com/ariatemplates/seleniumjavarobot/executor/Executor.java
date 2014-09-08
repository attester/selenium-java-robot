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

package com.ariatemplates.seleniumjavarobot.executor;

import java.awt.Robot;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

public class Executor {
    private static final String EXECUTOR_SCRIPT;
    static {
        try {
            EXECUTOR_SCRIPT = IOUtils.toString(Executor.class.getResource("executor.js"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static interface Method {
        Object run(List<Object> arguments) throws Exception;
    }

    private static int toInt(Object value) {
        return ((Number) value).intValue();
    }

    public static void startExecutor(final RemoteWebDriver driver, final Robot robot, final Point offset) throws InterruptedException {
        Map<String, Method> methods = new HashMap<String, Method>();

        methods.put("mouseMove", new Method() {
            public Object run(List<Object> arguments) {
                int x = toInt(arguments.get(0));
                int y = toInt(arguments.get(1));
                robot.mouseMove(x, y);
                return null;
            }
        });

        methods.put("smoothMouseMove", new Method() {
            public Object run(List<Object> arguments) throws Exception {
                int fromX = toInt(arguments.get(0));
                int fromY = toInt(arguments.get(1));
                int toX = toInt(arguments.get(2));
                int toY = toInt(arguments.get(3));
                int duration = toInt(arguments.get(4));
                SmoothMouseMove.smoothMouseMove(robot, fromX, fromY, toX, toY, duration);
                return null;
            }
        });

        methods.put("mousePress", new Method() {
            public Object run(List<Object> arguments) {
                int buttons = toInt(arguments.get(0));
                robot.mousePress(buttons);
                return null;
            }
        });

        methods.put("mouseRelease", new Method() {
            public Object run(List<Object> arguments) {
                int buttons = toInt(arguments.get(0));
                robot.mouseRelease(buttons);
                return null;
            }
        });

        methods.put("mouseWheel", new Method() {
            public Object run(List<Object> arguments) {
                int amount = toInt(arguments.get(0));
                robot.mouseWheel(amount);
                return null;
            }
        });

        methods.put("keyPress", new Method() {
            public Object run(List<Object> arguments) {
                int keyCode = toInt(arguments.get(0));
                robot.keyPress(keyCode);
                return null;
            }
        });

        methods.put("keyRelease", new Method() {
            public Object run(List<Object> arguments) {
                int keyCode = toInt(arguments.get(0));
                robot.keyRelease(keyCode);
                return null;
            }
        });

        methods.put("getOffset", new Method() {
            public Object run(List<Object> arguments) {
                Point point = driver.manage().window().getPosition();
                Map<String, Number> map = new HashMap<String, Number>();
                map.put("x", offset.x + point.x);
                map.put("y", offset.y + point.y);
                return map;
            }
        });

        driver.manage().timeouts().setScriptTimeout(1, TimeUnit.HOURS);
        while (true) {
            System.out.println("Loading the robot in the page.");
            try {
                driver.executeScript(EXECUTOR_SCRIPT);
            } catch (UnhandledAlertException e) {
                System.out.println("Alert in the page: " + e.getAlertText());
                continue;
            } catch (UnreachableBrowserException e) {
                System.out.println("The browser exited.");
                return;
            } catch (NoSuchWindowException e) {
                System.out.println("The browser window was closed.");
                return;
            } catch (WebDriverException e) {
                System.err.println(e);
                return;
            }
            while (true) {
                Map<String, Object> curCall = null;
                try {
                    curCall = (Map<String, Object>) driver.executeAsyncScript("return window.SeleniumJavaRobot.__getCall(arguments[0]);");
                } catch (UnhandledAlertException e) {
                    System.out.println("Alert in the page: " + e.getAlertText());
                    continue;
                } catch (TimeoutException e) {
                    continue;
                } catch (WebDriverException e) {
                    // probable navigation to another page
                    break;
                }
                executeCall(curCall, driver, methods);
            }
        }
    }

    private static void executeCall(Map<String, Object> curCall, RemoteWebDriver driver, Map<String, Method> methods) {
        try {
            String curEventName = (String) curCall.get("name");
            String callbackId = (String) curCall.get("cb");
            List<Object> args = (List<Object>) curCall.get("args");
            Method curMethod = methods.get(curEventName);
            System.out.println(String.format("Executing %s (%s)", curEventName, args));
            Object result;
            boolean success = false;
            try {
                result = curMethod.run(args);
                success = true;
            } catch (Exception e) {
                result = e.toString();
            }
            driver.executeScript("window.SeleniumJavaRobot.__callback(arguments[0], arguments[1], arguments[2])", callbackId, success, result);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
