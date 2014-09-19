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

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class LocalRobot implements IRobot {

    private Robot robot;

    public LocalRobot(Robot robot) {
        this.robot = robot;
    }

    public BufferedImage createScreenCapture(Rectangle screenRect) {
        return robot.createScreenCapture(screenRect);
    }

    public void keyPress(int keycode) {
        robot.keyPress(keycode);
    }

    public void keyRelease(int keycode) {
        robot.keyRelease(keycode);
    }

    public void mouseMove(int x, int y) {
        robot.mouseMove(x, y);
    }

    public void mousePress(int buttons) {
        robot.mousePress(buttons);
    }

    public void mouseRelease(int buttons) {
        robot.mouseRelease(buttons);
    }

    public void mouseWheel(int wheelAmt) {
        robot.mouseWheel(wheelAmt);
    }

}
