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
import java.awt.image.BufferedImage;

public class RectangleFinder {
    private BufferedImage image;
    private Color colorToFind;
    private int colorTolerance;
    private int width;
    private int height;
    private int expectedWidth;
    private int expectedHeight;

    public static Rectangle findRectangle(Robot robot, Color color, Rectangle initRectangle, int minWidth, int minHeight, int colorTolerance) {
        BufferedImage capture = robot.createScreenCapture(initRectangle);
        RectangleFinder finder = new RectangleFinder(capture, color, minWidth, minHeight, colorTolerance);
        Rectangle result = finder.findRectangle();
        if (result != null) {
            result.x += initRectangle.x;
            result.y += initRectangle.y;
        }
        return result;
    }

    public RectangleFinder(BufferedImage image, Color colorToFind, int expectedWidth, int expectedHeight, int colorTolerance) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.colorToFind = colorToFind;
        this.colorTolerance = colorTolerance;
        this.expectedWidth = expectedWidth;
        this.expectedHeight = expectedHeight;
    }

    public boolean checkRectangle(Rectangle rectangle) {
        int maxX = rectangle.x + rectangle.width;
        int maxY = rectangle.y + rectangle.height;
        for (int x = rectangle.x; x < maxX; x++) {
            for (int y = rectangle.y; y < maxY; y++) {
                if (!isRightColor(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Rectangle findRectangle() {
        for (int x = expectedWidth - 1; x < width; x += expectedWidth) {
            for (int y = expectedHeight - 1; y < height; y += expectedHeight) {
                Rectangle res = findRectangleFromPosition(x, y);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    // this method supposes that the shape is really a rectangle
    private Rectangle findRectangleFromPosition(int x, int y) {
        if (!isRightColor(x, y)) {
            return null;
        }
        Rectangle res = new Rectangle();
        res.x = updateX(x, y, -1);
        res.y = updateY(x, y, -1);
        res.width = updateX(x, y, 1) + 1 - res.x;
        res.height = updateY(x, y, 1) + 1 - res.y;
        if (res.width != expectedWidth || res.height != expectedHeight || !checkRectangle(res)) {
            return null;
        }
        return res;
    }

    private int updateX(int initValue, int y, int increment) {
        int value = initValue + increment;
        while (isCorrectX(value) && isRightColor(value, y)) {
            value += increment;
        }
        return value - increment;
    }

    private int updateY(int x, int initValue, int increment) {
        int value = initValue + increment;
        while (isCorrectY(value) && isRightColor(x, value)) {
            value += increment;
        }
        return value - increment;
    }

    private boolean isRightColor(int x, int y) {
        int rgbColor = image.getRGB(x, y);
        Color color = new Color(rgbColor);
        int distance = Math.abs(color.getRed() - colorToFind.getRed()) + Math.abs(color.getGreen() - colorToFind.getGreen())
                + Math.abs(color.getBlue() - colorToFind.getBlue());
        return distance < colorTolerance;
    }

    private boolean isCorrectX(int x) {
        return x >= 0 && x < width;
    }

    private boolean isCorrectY(int y) {
        return y >= 0 && y < height;
    }
}
