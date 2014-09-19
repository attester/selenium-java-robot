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

import com.ariatemplates.seleniumjavarobot.IRobot;

public class SmoothMouseMove {
    public static void smoothMouseMove(IRobot robot, int fromX, int fromY, int toX, int toY, int duration) throws InterruptedException {
        robot.mouseMove(fromX, fromY);
        double currentTime = System.currentTimeMillis();
        double endTime = currentTime + duration;
        while (currentTime < endTime) {
            double howCloseToEnd = (endTime - currentTime) / duration;
            robot.mouseMove((int) (howCloseToEnd * fromX + (1 - howCloseToEnd) * toX), (int) (howCloseToEnd * fromY + (1 - howCloseToEnd) * toY));
            Thread.sleep(50);
            currentTime = System.currentTimeMillis();
        }
        robot.mouseMove(toX, toY);
    }
}
