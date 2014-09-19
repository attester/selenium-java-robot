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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Point;

import com.ariatemplates.seleniumjavarobot.calibrator.Calibrator;
import com.ariatemplates.seleniumjavarobot.executor.Executor;

public class SeleniumJavaRobot {
    // Public options (not supposed to be changed after calling start):
    public String url;
    public boolean autoRestart;
    public IRobotizedWebDriverFactory robotizedWebDriverFactory;

    // Private fields:
    private final Thread mainThread = createMainThread();
    private final ExecutorService quitExecutor = Executors.newSingleThreadExecutor();

    private final Object lock = new Object();
    // The previous lock object is a lock for the following 2 fields:
    private RobotizedWebDriver robotizedWebDriver;
    private boolean stopped = false;

    public void start() {
        mainThread.start();
    }

    public void stop() throws InterruptedException {
        synchronized (lock) {
            if (!stopped && mainThread.isAlive()) {
                log("Closing ...");
            }
            stopped = true;
            if (robotizedWebDriver != null) {
                robotizedWebDriver.stop();
            }
        }
        mainThread.join();
    }

    private Thread createMainThread() {
        Thread result = new Thread(new Runnable() {
            public void run() {
                do {
                    RobotizedWebDriver robotizedWebDriver = null;
                    try {
                        synchronized (lock) {
                            if (stopped) {
                                break;
                            }
                            robotizedWebDriver = robotizedWebDriverFactory.createRobotizedWebDriver();
                            SeleniumJavaRobot.this.robotizedWebDriver = robotizedWebDriver;
                        }
                        startDriver(robotizedWebDriver, url);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        break;
                    } finally {
                        if (robotizedWebDriver != null) {
                            stopBrowserLater(robotizedWebDriver);
                        }
                    }
                } while (autoRestart);
                quitExecutor.shutdown();
                try {
                    quitExecutor.awaitTermination(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                }
                SeleniumJavaRobot.log("End");
            }
        });
        result.setDaemon(false);
        return result;
    }

    private void stopBrowserLater(final RobotizedWebDriver robotizedWebDriver) {
        quitExecutor.execute(new Runnable() {
            public void run() {
                // Makes sure the driver is closed. This is done
                // asynchronously so that we don't loose too
                // much time when --auto-restart is used,
                // because the quit method can take a long time
                // to finish in case the browser crashed or was
                // terminated forcefully.
                robotizedWebDriver.stop();
            }
        });
    }

    public static void startDriver(RobotizedWebDriver robotizedWebDriver, String url) throws InterruptedException {
        Point offset = Calibrator.calibrate(robotizedWebDriver);
        log("Computed offset: " + offset);
        robotizedWebDriver.webDriver.get(url);
        Executor executor = new Executor(robotizedWebDriver, offset);
        executor.run();
    }

    public static void log(String log) {
        System.out.println("[Selenium Java Robot] " + log);
    }
}
