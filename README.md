A two-part Java project for remote controlling a Lego Mindstorms EV3 robot with a wireless TCP/IP connection over Bluetooth developed by two 2nd year students (Ilkka Varjokunnas & me) of Metropolia University of Applied Sciences during 2015-10-14—2015-11-12.
The robot package contains the code to be uploaded into the EV3 brick, the graphical user interface (built with JavaFX) and game logic are in the game package.
A limited debug mode can be activated by editing the Controller.java which allows using the user interface without access to a Lego Mindstorms robot but it does not simulate the game logic in full.

## Required Lego Mindstorms EV3 Robot Parts ##
See the *instructions* folder for full building instructions (generated with [Lego Digital Designer](http://ldd.lego.com/fi-fi/)) for building the robot.
If the debug mode is not used, the code requires that at least the following parts are connected to the specified ports on the EV3 Brick. Part number in parentheses.
-  **Turning mechanism:** [Port C] Ms 2013 engine (6057952)
-  **Firing mechanism:** [Port D] small motor (6008577)
-  **Scoring sensor:** [Port #4] Ms, EV3, sensor, colour (6008919)

## Wireless Connection using an EV3 Brick ##
1. Enable Bluetooth in the EV3 Brick.
2. Pair the computer with the Brick.
3. Connect to the Brick using an access point (terminology may vary depending on the operating system) to turn the Brick into a Bluetooth Personal Area Network (PAN) server.
4. Upload the robot code to the Brick using the EV3 Control Center provided in the [leJOS EV3](http://www.lejos.org/ev3.php) toolkit.
5. Start the program in the robot.
6. Run View.java on the computer.
7. Connect to the robot using the default settings.

## Robot Operation & Game Goal ##
The robot is controlled using the arrow keys:
left and right arrows rotate the cannon, up and down arrow keys fire the cannon with a high or a low arc respectively.

The goal of the game is to fire plastic cannonballs into a suitable playing area (for example built from cardboard) with some sort of a target area.
When a cannonball leaves the target area via a channel or a groove, a color sensor embedded in said channel or groove registers the color of the passing ball and scores the player points.
The player who gets the highest score (as seen in the highscore list) with six shots, wins.