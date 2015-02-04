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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

import com.ariatemplates.seleniumjavarobot.RobotizedBrowser;
import com.ariatemplates.seleniumjavarobot.SeleniumJavaRobot;

public class Calibrator {

    private static final String CALIBRATOR_SCRIPT;
    private static final String CALIBRATOR_HTML;
    static {
        try {
            CALIBRATOR_SCRIPT = IOUtils.toString(Calibrator.class.getResource("calibrator.js"));
            CALIBRATOR_HTML = IOUtils.toString(Calibrator.class.getResource("calibrator.html"));
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }
    private static final int DEFAULT_COLOR_TOLERANCE = 50;
    private static final Color CALIBRATION_COLOR = new Color(255, 0, 0);
    private static final int BORDER = 10;

    static class CalibrationHandler extends AbstractHandler {
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            SeleniumJavaRobot.log("Serving " + target);
            if ("/".equals(target)) {
                response.setStatus(200);
                response.setContentType("text/html");
                response.setContentLength(CALIBRATOR_HTML.length());
                PrintWriter writer = response.getWriter();
                writer.write(CALIBRATOR_HTML);
                writer.close();
            } else {
                response.sendError(404);
            }
        }
    }

    public static Point calibrate(RobotizedBrowser robotizedBrowser) throws InterruptedException {
        return calibrate(robotizedBrowser, DEFAULT_COLOR_TOLERANCE);
    }

    public static Point calibrate(RobotizedBrowser robotizedBrowser, int colorTolerance) throws InterruptedException {
        Server server = new Server(0);
        try {
            server.setHandler(new CalibrationHandler());
            server.start();
            robotizedBrowser.browser.get("http://127.0.0.1:" + server.getURI().getPort() + "/");
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // call the calibration script:
        @SuppressWarnings("unchecked")
        Map<String, Long> jsInfos = (Map<String, Long>) robotizedBrowser.browser.executeScript(CALIBRATOR_SCRIPT);
        int width = jsInfos.get("width").intValue();
        int height = jsInfos.get("height").intValue();
        SeleniumJavaRobot.log(String.format("Viewport size: %d x %d", width, height));
        Point windowPosition = robotizedBrowser.browser.getWindowPosition();
        Dimension windowSize = robotizedBrowser.browser.getWindowSize();
        Rectangle windowRectangle = new Rectangle(windowPosition.x, windowPosition.y, windowSize.width, windowSize.height);
        SeleniumJavaRobot.log("Browser window rectangle: " + windowRectangle);
        // Give some time to the browser to display the expected color:
        Thread.sleep(500);
        // look for the rectangle full of the expected color:
        Rectangle rect = RectangleFinder.findRectangle(robotizedBrowser.robot, CALIBRATION_COLOR, windowRectangle, width - 2 * BORDER, height - 2 * BORDER,
                colorTolerance);
        if (rect == null) {
            throw new RuntimeException("Calibration failed.");
        }
        return new Point(rect.x - BORDER - windowPosition.x, rect.y - BORDER - windowPosition.y);
    }
}
