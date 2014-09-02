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

package com.ariatemplates.seleniumjavarobot.calibrator;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Calibrator {

    private static final String CALIBRATOR_SCRIPT;
    static {
        try {
            CALIBRATOR_SCRIPT = IOUtils.toString(Calibrator.class.getResource("calibrator.js"));
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
    private static final Color DEFAULT_CALIBRATION_COLOR = new Color(237, 22, 31);
    private static final int DEFAULT_COLOR_TOLERANCE = 50;
    private static final int BORDER = 10;

    public static Point calibrate(RemoteWebDriver driver, Robot robot) throws InterruptedException {
        return calibrate(driver, robot, DEFAULT_CALIBRATION_COLOR, DEFAULT_COLOR_TOLERANCE);
    }

    public static Point calibrate(RemoteWebDriver driver, Robot robot, Color calibrationColor, int colorTolerance) throws InterruptedException {
        // call the calibration script:
        Map<String, Long> jsInfos = (Map<String, Long>) driver.executeScript(CALIBRATOR_SCRIPT,
                String.format("rgb(%d,%d,%d)", calibrationColor.getRed(), calibrationColor.getGreen(), calibrationColor.getBlue()), BORDER);
        int width = jsInfos.get("width").intValue();
        int height = jsInfos.get("height").intValue();
        System.out.println(String.format("Viewport size: %d x %d", width, height));
        Point windowPosition = driver.manage().window().getPosition();
        Dimension windowSize = driver.manage().window().getSize();
        Rectangle windowRectangle = new Rectangle(windowPosition.x, windowPosition.y, windowSize.width, windowSize.height);
        System.out.println("Browser window rectangle: " + windowRectangle);
        // Give some time to the browser to display the expected color:
        Thread.currentThread().sleep(200);
        // look for the rectangle full of the expected color:
        Rectangle rect = RectangleFinder.findRectangle(robot, calibrationColor, windowRectangle, width - 2 * BORDER, height - 2 * BORDER, colorTolerance);
        if (rect == null) {
            throw new RuntimeException("Calibration failed.");
        }
        return new Point(rect.x - BORDER - windowPosition.x, rect.y - BORDER - windowPosition.y);
    }
}
