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

public class RobotizedBrowser {
    private boolean stopped;
    public final IRobot robot;
    public final IBrowser browser;

    public RobotizedBrowser(IRobot robot, IBrowser browser) {
        if (robot == null || browser == null) {
            throw new NullPointerException();
        }
        this.robot = robot;
        this.browser = browser;
    }

    public synchronized boolean isStopped() {
        return stopped;
    }

    public synchronized void stop() {
        if (!stopped) {
            stopped = true;
            try {
                browser.quit();
            } catch (RuntimeException e) {
            }
        }
    }
}
