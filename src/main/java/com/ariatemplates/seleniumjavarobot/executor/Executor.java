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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import com.ariatemplates.seleniumjavarobot.IRobot;
import com.ariatemplates.seleniumjavarobot.RobotizedWebDriver;
import com.ariatemplates.seleniumjavarobot.SeleniumJavaRobot;

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
        Object run(Executor executor, List<Object> arguments) throws InterruptedException;
    }

    private static int toInt(Object value) {
        return ((Number) value).intValue();
    }

    private static final Map<String, Method> methods;
    static {
        methods = new HashMap<String, Executor.Method>();
        methods.put("mouseMove", new Method() {
            public Object run(Executor executor, List<Object> arguments) {
                int x = toInt(arguments.get(0));
                int y = toInt(arguments.get(1));
                executor.robot.mouseMove(x, y);
                return null;
            }
        });

        methods.put("smoothMouseMove", new Method() {
            public Object run(Executor executor, List<Object> arguments) throws InterruptedException {
                int fromX = toInt(arguments.get(0));
                int fromY = toInt(arguments.get(1));
                int toX = toInt(arguments.get(2));
                int toY = toInt(arguments.get(3));
                int duration = toInt(arguments.get(4));
                SmoothMouseMove.smoothMouseMove(executor.robot, fromX, fromY, toX, toY, duration);
                return null;
            }
        });

        methods.put("mousePress", new Method() {
            public Object run(Executor executor, List<Object> arguments) {
                int buttons = toInt(arguments.get(0));
                executor.robot.mousePress(buttons);
                return null;
            }
        });

        methods.put("mouseRelease", new Method() {
            public Object run(Executor executor, List<Object> arguments) {
                int buttons = toInt(arguments.get(0));
                executor.robot.mouseRelease(buttons);
                return null;
            }
        });

        methods.put("mouseWheel", new Method() {
            public Object run(Executor executor, List<Object> arguments) {
                int amount = toInt(arguments.get(0));
                executor.robot.mouseWheel(amount);
                return null;
            }
        });

        methods.put("keyPress", new Method() {
            public Object run(Executor executor, List<Object> arguments) {
                int keyCode = toInt(arguments.get(0));
                executor.robot.keyPress(keyCode);
                return null;
            }
        });

        methods.put("keyRelease", new Method() {
            public Object run(Executor executor, List<Object> arguments) {
                int keyCode = toInt(arguments.get(0));
                executor.robot.keyRelease(keyCode);
                return null;
            }
        });

        methods.put("getOffset", new Method() {
            public Object run(Executor executor, List<Object> arguments) {
                Point point = executor.driver.manage().window().getPosition();
                Map<String, Number> map = new HashMap<String, Number>();
                map.put("x", executor.offset.x + point.x);
                map.put("y", executor.offset.y + point.y);
                return map;
            }
        });

    }

    private final RobotizedWebDriver robotizedWebDriver;
    private final IRobot robot;
    private final RemoteWebDriver driver;
    private Point offset;

    public Executor(RobotizedWebDriver robotizedWebDriver, Point offset) {
        this.robotizedWebDriver = robotizedWebDriver;
        this.robot = robotizedWebDriver.robot;
        this.driver = robotizedWebDriver.webDriver;
        this.offset = offset;
    }

    public void run() throws InterruptedException {
        driver.manage().timeouts().setScriptTimeout(1, TimeUnit.SECONDS);
        while (true) {
            boolean scriptExecuted = false;
            try {
                synchronized (robotizedWebDriver) {
                    if (robotizedWebDriver.isStopped()) {
                        return;
                    }
                    SeleniumJavaRobot.log("Loading the robot in the page.");
                    driver.executeScript(EXECUTOR_SCRIPT);
                }
                scriptExecuted = true;
                while (true) {
                    if (robotizedWebDriver.isStopped()) {
                        return;
                    }
                    Map<String, Object> curCall = null;
                    try {
                        // don't use synchronized(robotizedWebDriver) here,
                        // as this call can block for a long time
                        curCall = (Map<String, Object>) driver.executeAsyncScript("return window.SeleniumJavaRobot.__getCall(arguments[0]);");
                    } catch (TimeoutException e) {
                        continue;
                    } catch (UnhandledAlertException e) {
                        SeleniumJavaRobot.log("Alert in the page: " + e.getAlertText());
                        continue;
                    }
                    executeCall(curCall);
                }
            } catch (UnhandledAlertException e) {
                SeleniumJavaRobot.log("Alert in the page: " + e.getAlertText());
                continue;
            } catch (UnreachableBrowserException e) {
                SeleniumJavaRobot.log("The browser exited.");
                return;
            } catch (NoSuchWindowException e) {
                SeleniumJavaRobot.log("The browser window was closed.");
                return;
            } catch (RuntimeException e) {
                if (scriptExecuted) {
                    // this may be a page change
                    // try to re-execute the script
                    continue;
                }
                System.err.println(e);
                return;
            }
        }
    }

    private void executeCall(Map<String, Object> curCall) throws InterruptedException {
        try {
            String curEventName = (String) curCall.get("name");
            String callbackId = (String) curCall.get("cb");
            List<Object> args = (List<Object>) curCall.get("args");
            Method curMethod = methods.get(curEventName);
            SeleniumJavaRobot.log(String.format("Executing %s (%s)", curEventName, args));
            Object result;
            boolean success = false;
            try {
                result = curMethod.run(this, args);
                success = true;
            } catch (RuntimeException e) {
                result = e.toString();
            }
            synchronized (robotizedWebDriver) {
                driver.executeScript("window.SeleniumJavaRobot.__callback(arguments[0], arguments[1], arguments[2])", callbackId, success, result);
            }
        } catch (RuntimeException e) {
            System.err.println(e);
        }
    }
}
