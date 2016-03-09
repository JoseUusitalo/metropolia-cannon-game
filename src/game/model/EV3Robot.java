package game.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import game.controller.Controller;

/**
 * A class representing a physical robot and the actions it can do.
 *
 * @author Jose Uusitalo
 */
public class EV3Robot
{
	/**
	 * The IP address of the robot.
	 */
	private String ip;

	/**
	 * The port used for the socket.
	 */
	private int port;

	/**
	 * The socket for a remote connection to the robot.
	 */
	private Socket socket;

	/**
	 * An input stream through which this program receives the angle of the
	 * robot's main motor.
	 */
	private DataInputStream in;

	/**
	 * An output stream through which this program sends data to the physical
	 * robot.
	 */
	private DataOutputStream out;

	/**
	 * The thread that reads the data sent from the robot.
	 */
	private DataReaderThread dataReader;

	/**
	 * The thread that writes data to the robot.
	 */
	private DataWriterThread dataWriter;

	/**
	 * The {@link Controller}.
	 */
	private Controller controller;

	/**
	 * The current angle of the main motor of the cannon.
	 */
	private int currentAngle;

	/**
	 * The minimum angle the robot's main motor can rotate to. This is limited
	 * by the length of the cables. 0 degrees is assumed to be straight forward.
	 */
	public static final int LIMIT_ANGLE_MIN = -85;

	/**
	 * The maximum angle the robot's main motor can rotate to. This is limited
	 * by the length of the cables. 0 degrees is assumed to be straight forward.
	 */
	public static final int LIMIT_ANGLE_MAX = 85;

	/**
	 * The number of balls the cannon can shoot every round. (We currently have
	 * access to six balls although the cannon can hold ten.)
	 */
	public static final int MAX_BALLS = 6;

	/**
	 * Number of balls left in the cannon.
	 */
	private int ballsLeft;

	/**
	 * Can the player currently remotely control the robot through the user
	 * interface?
	 */
	private boolean robotControlEnabled;

	/**
	 * Timer for counting seconds.
	 */
	private Timer timer;

	/**
	 * Is the cannon currently in the process of shooting a cannonball?
	 */
	private boolean shootingInProgress;

	/**
	 * Is the cannon currently in the process of rotating to a specific angle?
	 */
	private boolean angleTurnInProgress;

	/**
	 * The default port for the Lego Mindstorms EV3 robot.
	 */
	private final static int defaultPort = 1111;

	/**
	 * The default IP-address for the Lego Mindstorms EV3 robot.
	 */
	private final static String defaultIP = "10.0.1.1";

	public EV3Robot(final String _ip, final int _port, final Controller _controller)
	{
		ip = _ip;
		port = _port;
		controller = _controller;
		timer = null;
		currentAngle = 0;
		robotControlEnabled = false;
		ballsLeft = MAX_BALLS;
	}

	/**
	 * Open a remote connection to the robot.
	 *
	 * @throws UnknownHostException
	 *             Unknwon host: wrong IP or port.
	 * @throws ConnectException
	 *             Unable to connect due to missing Bluetooth connection.
	 * @throws IOException
	 *             Some other connection error.
	 */
	public void openConnection() throws UnknownHostException, ConnectException, IOException
	{
		System.out.println("[EV3Robot] Opening socket...");

		socket = new Socket(ip, port);

		System.out.println("[EV3Robot] Creating input stream.");
		in = new DataInputStream(socket.getInputStream());

		System.out.println("[EV3Robot] Creating output stream.");
		out = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * Creates a fake remote connection for testing and debug purposes.
	 */
	public void openFakeTestingConnection()
	{
		System.out.println("[EV3Robot] Opening fake socket...");
		socket = new Socket();

		System.out.println("[EV3Robot] Creating fake input stream.");
		in = new DataInputStream(new InputStream()
		{

			@Override
			public int read() throws IOException
			{
				return 45;
			}
		});

		System.out.println("[EV3Robot] Creating fake output stream.");
		out = new DataOutputStream(new DataOutputStream(new OutputStream()
		{

			@Override
			public void write(int b) throws IOException
			{
				// No need to write anything.
			}
		}));
	}

	/**
	 * Creates the threads for reading and writing data to the robot.
	 *
	 * @param _player
	 *            The current player.
	 */
	public void createDataTransferThreads(final Player _player)
	{
		System.out.println("[EV3Robot] Creating input/output threads.");

		dataReader = new DataReaderThread(in, controller, this, _player);
		dataWriter = new DataWriterThread(out, controller);

		dataWriter.startWriting();
		dataReader.startReading();

		dataReader.start();
		dataWriter.start();
	}

	/**
	 * Stop and delete threads that transfer data to and from the robot.
	 */
	public void deleteDataTransferThreads()
	{
		System.out.println("[EV3Robot] Deleting input/output threads.");
		if (dataWriter != null)
		{
			dataWriter.stopWriting();
			dataWriter = null;
		}

		if (dataReader != null)
		{
			dataReader.stopReading();
			dataReader = null;
		}
	}

	/**
	 * Closes the remote connection to the robot. <b>Do not call manually.</b>
	 * Use {@link Controller#disconnect()} instead.
	 *
	 * @throws IOException
	 *             when connection failed to close
	 * @throws NullPointerException
	 *             when the input or output stream was not opened
	 */
	public void closeConnection() throws IOException, NullPointerException
	{
		System.out.println("[EV3Robot] Closing connection.");
		deleteDataTransferThreads();
		out.close();
		in.close();
		socket.close();
	}

	/**
	 * @return the IP-address of the robot
	 */
	public String getIp()
	{
		return ip;
	}

	/**
	 * @return the port where to connect to on the robot
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @return current angle of the cannon's main motor
	 */
	public int getCurrentAngle()
	{
		return currentAngle;
	}

	/**
	 * @return <code>true</code> if the current angle of the cannon is less than
	 *         its maximum possible angle, <code>false</code> if it equal or
	 *         greater than that
	 */
	public boolean isNotAtMaxAngle()
	{
		return (currentAngle < LIMIT_ANGLE_MAX);
	}

	/**
	 * @return <code>true</code> if the current angle of the cannon is greater
	 *         than its minimum possible angle, <code>false</code> if it equal
	 *         or less than that
	 */
	public boolean isNotAtMinAngle()
	{
		return (currentAngle > LIMIT_ANGLE_MIN);
	}

	/**
	 * Resets the robot state for reuse so you do not need to recreate the
	 * remote connection.
	 *
	 * @param _player
	 *            The current player.
	 */
	public void reset(final Player _player)
	{
		System.out.println("[EV3Robot] Robot reset.");
		currentAngle = 0;
		ballsLeft = MAX_BALLS;
		shootingInProgress = false;
		angleTurnInProgress = false;
		robotControlEnabled = false;
		// createNewTimer();

		// deleteDataTransferThreads();
		createDataTransferThreads(_player);
	}

	/**
	 * Enables/Disables user control over robot movement.
	 *
	 * @param _value
	 *            A boolean value determining whether or not the user can
	 *            control the robot remotely.
	 */
	public void setRobotControlEnabled(final boolean _value)
	{
		robotControlEnabled = _value;
	}

	/**
	 * @return the default port number for creating a remote connection to the
	 *         robot
	 */
	public static int getDefaultPort()
	{
		return defaultPort;
	}

	/**
	 * @return the default IP-address for creating a remote connection to the
	 *         robot
	 */
	public static String getDefaultIP()
	{
		return defaultIP;
	}

	/**
	 * Updates the cannon's angle value for display in the user interface.
	 *
	 * @param _newAngle
	 *            New angle integer.
	 */
	public void setCurrentAngle(final int _newAngle)
	{
		// System.out.println(currentAngle + " to " + _newAngle);
		// Robot is at max angle.
		if (!isNotAtMaxAngle())
		{
			System.out.println("WOAH MAX");
			// Trying to turn even more, stop.
			if (_newAngle > currentAngle)
			{
				System.out.println("STOP MAX");
				stopTurning();
			}
		}
		else if (!isNotAtMinAngle())
		{
			System.out.println("WOAH MIN");
			// Robot is at min angle, trying to turn even more, stop.
			if (_newAngle < currentAngle)
			{
				System.out.println("STOP MIN");
				stopTurning();
			}
		}

		// No need to set the same value multiple times.
		if (_newAngle != currentAngle)
		{
			currentAngle = _newAngle;

			if (Controller.DEBUG_INFO)
				// System.out.println("[EV3Robot] setCurrentAngle indicator
				// update: " + _newAngle);

				controller.updateCannonAngleIndicator();

		}
	}

	/**
	 * Turns the cannon left until stopped, or the limit angle is reached. Does
	 * not perform a readiness check!
	 *
	 * @return <code>true</code> if robot is not at max angle,
	 *         <code>false</code> if robot is already at max angle
	 * @see EV3Robot#LIMIT_ANGLE_MAX
	 */
	public boolean forceTurnLeft()
	{
		if (isNotAtMaxAngle())
		{
			System.out.println("[EV3Robot] Force Turning left.");
			dataWriter.turnLeft();

			if (Controller.DEBUG)
			{
				dataReader.debugModAngle(currentAngle + 5);
				System.out.println("[EV3Robot] DEBUG: turnLeft indicator update");
				controller.updateCannonAngleIndicator();
			}
			/*
			 * Indicator update (in non-debug mode) handled as follows: turn
			 * robot, robot turns, robot reports to PC about angle change, data
			 * reader thread gets the new angle and calls setCurrentAngle here.
			 */
			return true;
		}

		System.out.println("[EV3Robot] Unable to force turn left: robot at maximum angle.");
		stopTurning();
		return false;
	}

	/**
	 * Turns the cannon left until stopped, or the limit angle is reached.
	 *
	 * @return <code>true</code> if robot is ready and is not at max angle,
	 *         <code>false</code> if robot is not ready or is already at max
	 *         angle
	 * @see EV3Robot#LIMIT_ANGLE_MAX
	 */
	public boolean turnLeft()
	{
		if (isReady())
		{
			if (isNotAtMaxAngle())
			{
				System.out.println("[EV3Robot] Turning left.");
				dataWriter.turnLeft();

				if (Controller.DEBUG)
				{
					dataReader.debugModAngle(currentAngle + 5);
					System.out.println("[EV3Robot] DEBUG: turnLeft indicator update");
					controller.updateCannonAngleIndicator();
				}
				/*
				 * Indicator update (in non-debug mode) handled as follows: turn
				 * robot, robot turns, robot reports to PC about angle change,
				 * data reader thread gets the new angle and calls
				 * setCurrentAngle here.
				 */
				return true;
			}

			System.out.println("[EV3Robot] Unable to turn left: robot at maximum angle.");
			stopTurning();
			return false;
		}

		System.out.println("[EV3Robot] Unable to turn left: robot is busy.");
		return false;
	}

	/**
	 * Turn the cannon left until stopped, or the limit angle is reached.
	 *
	 * @return <code>true</code> if robot is ready and is not at min angle,
	 *         <code>false</code> if robot is not ready or is already at min
	 *         angle
	 * @see EV3Robot#LIMIT_ANGLE_MIN
	 */
	public boolean turnRight()
	{
		if (isReady())
		{
			if (isNotAtMinAngle())
			{
				System.out.println("[EV3Robot] Turning right.");
				dataWriter.turnRight();

				if (Controller.DEBUG)
				{
					dataReader.debugModAngle(currentAngle + 5);
					System.out.println("[EV3Robot] DEBUG: turnRight indicator update");
					controller.updateCannonAngleIndicator();
				}
				/*
				 * Indicator update (in non-debug mode) handled as follows: turn
				 * robot, robot turns, robot reports to PC about angle change,
				 * data reader thread gets the new angle and calls
				 * setCurrentAngle here.
				 */
				return true;
			}

			System.out.println("[EV3Robot] Unable to turn right: robot at minimum angle.");
			stopTurning();
			return false;
		}

		System.out.println("[EV3Robot] Unable to turn right: robot is busy.");
		return false;
	}

	/**
	 * Turn the cannon left until stopped, or the limit angle is reached. Does
	 * not perform a readiness check!
	 *
	 * @return <code>true</code> if robot is not at min angle,
	 *         <code>false</code> if robot is already at min angle
	 * @see EV3Robot#LIMIT_ANGLE_MIN
	 */
	public boolean forceTurnRight()
	{
		if (isNotAtMinAngle())
		{
			System.out.println("[EV3Robot] Force turning right.");
			dataWriter.turnRight();

			if (Controller.DEBUG)
			{
				dataReader.debugModAngle(currentAngle + 5);
				System.out.println("[EV3Robot] DEBUG: turnRight indicator update");
				controller.updateCannonAngleIndicator();
			}
			/*
			 * Indicator update (in non-debug mode) handled as follows: turn
			 * robot, robot turns, robot reports to PC about angle change, data
			 * reader thread gets the new angle and calls setCurrentAngle here.
			 */
			return true;
		}

		System.out.println("[EV3Robot] Unable to force turn left: robot at minimum angle.");
		stopTurning();
		return false;
	}

	/**
	 * Stop turning the cannon.
	 */
	public void stopTurning()
	{
		// You can always stop the robot.
		System.out.println("[EV3Robot] Stopping");
		dataWriter.stopTurning();
	}

	/**
	 * Fires a high shot with the cannon and stops the cannon movement.
	 */
	public void shootCannonHigh()
	{
		System.out.println("[EV3Robot] Trying to shoot high, cannonballs left: " + ballsLeft);
		if (isReady() && ballsLeft > 0)
		{
			System.out.println("[EV3Robot] Shooting high.");

			if (timer != null)
				ScoreMachine.scoreShot(timer.getSeconds());

			dataWriter.shootCannonHigh();
			modBallsLeft(-1);
			System.out.println("[EV3Robot] Waiting for a high shot to complete.");
			setShootingInProgress(true);
		}
		else
		{
			System.out.println("[EV3Robot] Cannot shoot high, robot busy.");
		}
	}

	/**
	 * Modify the number of cannonballs remaining in the cannon. Updates the
	 * user interface on function call.
	 *
	 * @param n
	 *            Use positive integers to add balls and negative integers to
	 *            remove balls.
	 */
	private void modBallsLeft(final int n)
	{
		ballsLeft += n;
		controller.setCannonballsLeft(ballsLeft);
	}

	/**
	 * Fires a low shot with the cannon.
	 */
	public void shootCannonLow()
	{
		System.out.println("[EV3Robot] Trying to shoot low, cannonballs left: " + ballsLeft);
		if (isReady() && ballsLeft > 0)
		{
			System.out.println("[EV3Robot] Shooting low.");

			if (timer != null)
				ScoreMachine.scoreShot(timer.getSeconds());

			dataWriter.shootCannonLow();
			modBallsLeft(-1);

			System.out.println("[EV3Robot] Waiting for a low shot to complete.");
			setShootingInProgress(true);
		}
	}

	/**
	 * Checks if there are cannon balls left to fire. If not, ends the game for
	 * the current player.
	 */
	private void checkBalls()
	{
		System.out.println("[EV3Robot] Cannonballs left: " + ballsLeft);
		// It should never be less than 0 but it doesn't hurt to be sure.
		if (ballsLeft < 1 && (!shootingInProgress && !angleTurnInProgress))
		{
			controller.endGame();
		}
	}

	/**
	 * Rotates the cannon to the specified angle. Player control of the robot is
	 * disabled during the execution of this method.
	 *
	 * @param _angle
	 *            Angle to turn to.
	 */
	public void turnToAngle(final int _angle)
	{
		if (!angleTurnInProgress)
		{
			setAngleTurnInProgress(true);
			System.out.println("[EV3Robot] Turning to angle: " + _angle);
			dataWriter.turnToAngle(_angle);
		}
		else
		{
			System.out.println("[EV3Robot] Already turning.");
		}
	}

	public void forceTurnToAngle(final int _angle)
	{
		setAngleTurnInProgress(true);
		System.out.println("[EV3Robot] Force turning to angle: " + _angle);
		dataWriter.turnToAngle(_angle);
	}

	/**
	 * Rotates the cannon to a random angle between the limits. Player control
	 * of the robot is disabled during the execution of this method.
	 */
	public void turnToAngle()
	{
		setAngleTurnInProgress(true);
		int rand = ((int) Math.round((LIMIT_ANGLE_MAX + Math.abs(LIMIT_ANGLE_MIN)) * Math.random()))
				- Math.abs(LIMIT_ANGLE_MIN);

		System.out.println("[EV3Robot] Turning to random angle: " + rand);

		dataWriter.turnToAngle(rand);
	}

	/**
	 * Set a boolean value that shows whether or not the robot is in the process
	 * of shooting. Robot control is automatically disabled during shooting.
	 *
	 * @param _value
	 *            <code>true</code> if the robot is currently firing its cannon,
	 *            <code>false</code> otherwise.
	 */
	public void setShootingInProgress(final boolean _value)
	{
		System.out.println("[EV3Robot] Shooting in progress: " + _value);

		shootingInProgress = _value;

		if (_value)
		{
			turnToAngle();
			if (Controller.USE_TIMER)
				timer.stopTimer();
		}
		else
		{
			checkForControl();
		}
	}

	/**
	 * @return <code>true</code> if the robot is currently firing its cannon,
	 *         <code>false</code> otherwise.
	 */
	public boolean getShootingInProgress()
	{
		return shootingInProgress;
	}

	/**
	 * Change the value of {@link EV3Robot#angleTurnInProgress}.
	 *
	 * @param _value
	 *            Boolean value signifying whether or not the robot is in the
	 *            process of turning to a specified angle.
	 */
	public void setAngleTurnInProgress(final boolean _value)
	{
		System.out.println("[EV3Robot] Angle turn in progress: " + _value);

		angleTurnInProgress = _value;

		checkForControl();
	}

	/**
	 * We do not know if random turning or shooting finishes first. If neither
	 * are in progress: allow robot control, reset timer (if used), and check
	 * for cannonballs and for game end. If something is in progress disallow
	 * robot control.
	 *
	 * @see EV3Robot#checkBalls()
	 */
	private void checkForControl()
	{
		if (!shootingInProgress && !angleTurnInProgress)
		{
			// Nothing in progress, allow controls and check for balls.
			robotControlEnabled = true;
			if (Controller.USE_TIMER)
			{
				System.out.println("[EV3Robot] Resetting timer.");
				timer.resetTimer();
				timer.startTimer();
			}
			checkBalls();
		}
		else
		{
			robotControlEnabled = false;
		}
	}

	/**
	 * @return <code>true</code> if the cannon is ready to be controlled by the
	 *         player, <code>false</code> if the cannon is busy doing something
	 *         else
	 */
	public boolean isReady()
	{
		System.out.println(
				"[EV3Robot] Ready? " + shootingInProgress + " " + angleTurnInProgress + " " + robotControlEnabled);
		return !shootingInProgress && !angleTurnInProgress && robotControlEnabled;
	}

	/**
	 * <b>FOR DEBUG USE ONLY.</b> In hindsight we should have implemented an
	 * actual state machine...
	 */
	public void printState()
	{
		System.out.println("\tAngle turn in progress: " + angleTurnInProgress);
		System.out.println("\tShooting in progress: " + shootingInProgress);
		System.out.println("\tRobot control enabled: " + robotControlEnabled);
	}

	/**
	 * Stops the existing timer and creates a new timer but does not start it.
	 */
	public void createNewTimer()
	{
		if (timer != null)
			timer.stopTimer();

		timer = new Timer(this, controller);
	}

	/**
	 * Send an infrared distance value to the robot which is the maximum
	 * distance at which a ball detection may occur. If the IR sensor reads a
	 * value less or equal to this value, the detection is paused for a little
	 * while to prevent multiple detections from a single ball.
	 */
	public void sendMaxIRDistance()
	{
		dataWriter.sendMaxIRDistance(ScoreMachine.MAX_BALL_IR_DISTANCE);
	}

	public void sendMinRedValue()
	{
		dataWriter.sendMaxIRDistance(ScoreMachine.MINIMUM_RED_VALUE);
	}

	/**
	 * @return
	 */
	public boolean isRobotControlEnabled()
	{
		return robotControlEnabled;
	}

	public void clearWriteBuffer()
	{
		dataWriter.sendClearBufferCommand();
	}

	public void clearPCBuffer()
	{
		dataReader.clearBufferData();
	}
}
