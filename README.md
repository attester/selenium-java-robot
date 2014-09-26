# Selenium Java Robot

This repository contains a tool which allows JavaScript scripts running in a browser
to generate keyboard and mouse events at the operating system level.

This is especially useful when writing tests for your application, to simulate
actions from the user.

## Installing this tool

Before installing this tool, please make sure [node.js](http://nodejs.org/), [npm](https://www.npmjs.org/doc/README.html)
and [Java](https://java.com/download) are installed on your computer.

Then, this tool can be installed with the following command line:

```
npm install -g selenium-java-robot
```

## Command line usage

Execute the `selenium-java-robot` command:

```
selenium-java-robot
```

Here is the list of accepted options:

**`--browser <browserName>`**

Replace `<browserName>` with the name of one of the following accepted browsers:

* `Firefox` (default value, except on Mac OS)

* `Safari` (default value on Mac OS)

* `Chrome` or `Chrome-debug` (using `Chrome-debug` is much slower than using `Chrome`,
but `Chrome-debug` allows DevTools to be open, where as `Chrome` does not support it,
see [here](https://sites.google.com/a/chromium.org/chromedriver/help/devtools-window-keeps-closing) for a technical explanation)

* `Internet Explorer`

**`--url <initialUrl>`**

Replace `<initialUrl>` with the URL to initially load in the browser.
The default URL is `http://localhost:7777/__attester__/slave.html` which corresponds to the default URL of [attester](https://github.com/attester/attester),
when running it locally.

**`--auto-restart`**

This option makes sure the browser is automatically restarted in case it is closed.
The browser is restarted at the URL specified by the --url parameter.

**`--help`**

If this option is present, the list of accepted options is displayed and the *Selenium Java Robot* exits without starting a browser.

**`--version`**

If this option is present, the version of the *Selenium Java Robot* is displayed and the *Selenium Java Robot* exits without starting a browser.

**`-DpropertyName=value`**

This option allows to set a Java system property before launching the browser. This parameter can be repeated multiple times to set different properties.
Different Selenium drivers use different Java system properties. Please refer to the corresponding documentation:
* [Firefox](https://code.google.com/p/selenium/wiki/FirefoxDriver)
* [Safari](https://code.google.com/p/selenium/wiki/SafariDriver)
* [Internet Explorer](https://code.google.com/p/selenium/wiki/InternetExplorerDriver)

## Calibration

Once the *Selenium Java Robot* starts, it first tries to detect the position of the viewport inside the browser window by displaying
a calibration page and taking a screenshot to detect the coordinates of the page. This is done automatically.
This process can sometimes fail if the browser window is hidden by another window.

Once this is done, the browser automatically navigates to the URL specified in the command line.

## JavaScript API

Inside the page loaded by the *Selenium Java Robot*, an object called `SeleniumJavaRobot` is automatically made available.
It contains some methods which can be called to simulate keyboard and mouse events.

### Callback

Each method on the `SeleniumJavaRobot` object accepts a callback as its last parameter, to be notified when
the corresponding operation is done. When the callback is provided (which is optional), it is expected to
be either a simple function, or an object with the following structure:

```js
{
   fn: function (response, args) { /* ... */ }, // function to be called when the operation is done.
   scope: window, // object to be available as this in the callback function
   args: { /* something */ } // second argument passed to the callback function
}
```

Here is the structure of the `response` object passed in the callback as the first argument:

```js
{
   success: true, // true if there was no problem during the execution of the method, false otherwise
   result: null // if success is true, this is the result of the method (currently only relevant for getOffset)
   // if success is false, result contains a string with the error message
}
```

### List of methods

You can find in this section the description of the methods available on the `SeleniumJavaRobot` object.
Note that most of those methods are simply a bridge to the corresponding method in the
[Java Robot](http://docs.oracle.com/javase/6/docs/api/java/awt/Robot.html).

* `getOffset (callback: Callback)`

Returns the coordinates of the top left corner of the viewport in the screen, as detected during the calibration phase.

```js
SeleniumJavaRobot.getOffset({
   fn: function (response) {
      if (response.success) {
         var coordinates = response.result;
         alert("The coordinates of the viewport in the screen are: " + coordinates.x + "," + coordinates.y);
      }
   }
})
```

* `mouseMove (x: Number, y: Number, callback: Callback)`

Instantly moves the mouse to the specified `x`, `y` screen coordinates.

* `smoothMouseMove (fromX: Number, fromY: Number, toX: Number, toY: Number, duration: Number, callback: Callback)`

Instantly moves the mouse to the specified `fromX`, `fromY` screen coordinates, then smoothly moves the mouse
from there to the `toX`, `toY` screen coordinates. The duration of the move must be expressed in milliseconds.

* `mousePress (buttons: Number, callback: Callback)`

Presses one or more mouse buttons. The mouse buttons should be released using the mouseRelease method.
The `buttons` parameter can be a combination (with the logical OR operator `a | b`) of one or more of the following flags:

```js
var BUTTON1_MASK = 16;
var BUTTON2_MASK = 8;
var BUTTON3_MASK = 4;
```

For example, to press both the button 1 and button 2 of the mouse at the same time, call:

```js
SeleniumJavaRobot.mousePress(16 | 8);
```

* `mouseRelease (buttons: Number, callback: Callback)`

Releases one or more mouse buttons.

* `mouseWheel (amount: Number, callback: Callback)`

Rotates the scroll wheel on wheel-equipped mice.

The `amount` parameter is the number of "notches" to move the mouse wheel Negative values indicate movement up/away from the user,
positive values indicate movement down/towards the user.

* `keyPress (keyCode: Number, callback: Callback)`

Presses a given key. The key should be released using the keyRelease method.
Valid key codes are the constants starting with `VK_` as listed in
[this Java documentation](http://docs.oracle.com/javase/6/docs/api/constant-values.html#java.awt.event.KeyEvent.VK_0).

* `keyRelease (keyCode: Number, callback: Callback)`

Releases a given key.

## How to recompile this tool

Before compiling this tool, you need [a Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
and [Maven](http://maven.apache.org/) to be installed on your computer.

Then you can simply execute the following command from the root directory of your clone
of this repository:

```
npm install
```

This will install dependencies and compile the *Selenium Java Robot* tool.

## How it is implemented

[Selenium](http://www.seleniumhq.org/) is used to start the web browser and to communicate with it.

The [Java Robot class](http://docs.oracle.com/javase/6/docs/api/java/awt/Robot.html) is used to send keyboard and
mouse events to the operating system, and to perform the screen capture for the calibration.

For more information about the implementation, do not hesitate to have a look at the source code in this repository.

## License

[Apache License 2.0](LICENSE)
