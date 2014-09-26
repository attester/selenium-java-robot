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
    public IRobotizedBrowserFactory robotizedBrowserFactory;

    // Private fields:
    private final Thread mainThread = createMainThread();
    private final ExecutorService quitExecutor = Executors.newSingleThreadExecutor();

    private final Object lock = new Object();
    // The previous lock object is a lock for the following 2 fields:
    private RobotizedBrowser robotizedBrowser;
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
            if (robotizedBrowser != null) {
                robotizedBrowser.stop();
            }
        }
        mainThread.join();
    }

    private Thread createMainThread() {
        Thread result = new Thread(new Runnable() {
            public void run() {
                do {
                    RobotizedBrowser robotizedBrowser = null;
                    try {
                        synchronized (lock) {
                            if (stopped) {
                                break;
                            }
                            robotizedBrowser = robotizedBrowserFactory.createRobotizedBrowser();
                            SeleniumJavaRobot.this.robotizedBrowser = robotizedBrowser;
                        }
                        startDriver(robotizedBrowser, url);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        break;
                    } finally {
                        if (robotizedBrowser != null) {
                            stopBrowserLater(robotizedBrowser);
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

    private void stopBrowserLater(final RobotizedBrowser robotizedBrowser) {
        quitExecutor.execute(new Runnable() {
            public void run() {
                // Makes sure the driver is closed. This is done
                // asynchronously so that we don't loose too
                // much time when --auto-restart is used,
                // because the quit method can take a long time
                // to finish in case the browser crashed or was
                // terminated forcefully.
                robotizedBrowser.stop();
            }
        });
    }

    public static void startDriver(RobotizedBrowser robotizedBrowser, String url) throws InterruptedException {
        Point offset = Calibrator.calibrate(robotizedBrowser);
        log("Computed offset: " + offset);
        robotizedBrowser.browser.get(url);
        Executor executor = new Executor(robotizedBrowser, offset);
        executor.run();
    }

    public static void log(String log) {
        System.out.println("[Selenium Java Robot] " + log);
    }
}
