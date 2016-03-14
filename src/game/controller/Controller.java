package game.controller;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import game.model.EV3Robot;
import game.model.Highscore;
import game.model.HighscoreList;
import game.model.HighscoreListRow;
import game.model.Player;
import game.view.View;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * The controller in the MVC-model.
 *
 * @author Jose Uusitalo &amp; Ilkka Varjokunnas
 */
public class Controller
{
	/**
	 * Whether or not to run this program in debug mode which creates a valid
	 * but "null" remote connection. Used for testing the user interface and
	 * business logic without access to the robot itself.
	 */
	public static final boolean DEBUG = true;

	/**
	 * Whether or not to print additional debugging info about GUI and input
	 * events.
	 */
	public static final boolean DEBUG_INFO = true;

	/**
	 * Whether or not to use the shot timer feature.
	 * 
	 * Note: This does not work at the moment.
	 */
	public static final boolean USE_TIMER = false;

	/**
	 * The maximum number of characters the player's name can have.
	 */
	public static final int PLAYER_NAME_MAX_LENGTH = 14;

	/**
	 * Time in seconds until the robot shoots automatically.
	 */
	public static final int SHOT_TIMER = 5;

	/**
	 * Constant used in many sleep commands.
	 */
	public static final long SLEEP_TIME = 50;

	/**
	 * MVC-model view.
	 */
	private View view;

	/**
	 * An object representing the physical robot.
	 */
	private EV3Robot robot;

	/**
	 * An object representing the player.
	 */
	private Player player;

	/**
	 * A list of highscores of all players.
	 */
	private HighscoreList highscores;

	/**
	 * Whether or not the wireless connection between the PC and the robot has
	 * been terminated.
	 */
	private boolean connected;

	/**
	 * Used for running the robot in demonstration mode.
	 */
	private DemoMode demo;

	/**
	 * Whether or not data has been received from the data reader thread.
	 */
	private boolean dataReceived;

	private boolean gameOver;

	public Controller(final View _view)
	{
		view = _view;
		highscores = new HighscoreList();
	}

	/**
	 * Establishes a remote connection between the PC and the robot and enables
	 * the robot controls in the interface.
	 */
	public void connect()
	{
		connected = false;
		robot = new EV3Robot(view.getIP(), view.getPort(), this);

		try
		{
			robot.openConnection();
			connected = true;
		}
		catch (UnknownHostException e)
		{
			view.errorMessage("Unknown host: Incorrect IP or port.");
			e.printStackTrace();
		}
		catch (ConnectException e)
		{
			view.errorMessage(
					"Connection failed. Please check the connection details and confirm that the Bluetooth connection is on.");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			view.errorMessage("Unknown error.");
			e.printStackTrace();
		}

		view.disableConnectionControls(connected);
		view.disableGameControls(!connected, true);
	}

	/**
	 * Creates a valid but fake and non-functional remote connection. Used for
	 * trying out the user interface without the robot itself.
	 */
	public void debugConnect()
	{
		connected = false;
		robot = new EV3Robot(view.getIP(), view.getPort(), this);

		robot.openFakeTestingConnection();
		connected = true;

		view.disableConnectionControls(connected);
		view.disableGameControls(!connected, true);
	}

	/**
	 * Ends the current round and destroys the remote connection between the PC
	 * and the robot.
	 */
	public void disconnect()
	{
		if (connected)
		{
			if (robot != null)
				robot.setRobotControlEnabled(false);

			try
			{
				if (robot != null)
					robot.closeConnection();
				connected = false;
			}
			catch (IOException e)
			{
				view.errorMessage("Unable to disconnect.");
				e.printStackTrace();
			}
			catch (NullPointerException e)
			{
				view.errorMessage("Failed to disconnect, some data streams have not been initialized.");
				e.printStackTrace();
			}

			view.disableConnectionControls(connected);
			view.disableGameControls(!connected);
			view.disableRobotControls(!connected);
		}
	}

	/**
	 * Connections details are in a String array in the following order: <br>
	 * 0: default robot IP-address<br>
	 * 1: default robot remote port<br>
	 *
	 * @return the default remote connection details
	 */
	public static String[] getConnectionDefaults()
	{
		return new String[] { EV3Robot.getDefaultIP(), String.valueOf(EV3Robot.getDefaultPort()) };
	}

	/**
	 * Places the default remote connection details into the user interface text
	 * fields.
	 */
	public void setConnectionDefaults()
	{
		if (DEBUG)
			view.setConnectionDefaults(new String[] {"DEBUG", "0"});
		else
			view.setConnectionDefaults(getConnectionDefaults());
	}

	/**
	 * Converts the given angle to a percentage of the range between the given
	 * minimum and maximum angles for use in the user interface progress bar
	 * which displays the current angle of the cannon.
	 *
	 * @param _angle
	 *            Angle as a double.
	 * @param _angleMin
	 *            The minimum angle as a double.
	 * @param _angleMax
	 *            The maximum angle as a double.
	 * @return a double in the range [0.0, 1.0]
	 * @see view.BottomPane
	 */
	public static double toPercentage(final double _angle, final double _angleMin, final double _angleMax)
	{
		return 1.0 - (_angle - _angleMin) / (_angleMax - _angleMin);
	}

	/**
	 * Sets the value in the progress bar in the user interface based on the
	 * current cannon angle.
	 *
	 * @see view.BottomPane
	 */
	public void updateCannonAngleIndicator()
	{
		double oldProgress = view.getCannonAngleBarProgress();
		int angle = robot.getCurrentAngle();
		double progress = toPercentage(angle, EV3Robot.LIMIT_ANGLE_MIN, EV3Robot.LIMIT_ANGLE_MAX);
		boolean lessThanZero = Double.compare(progress, 0.0) <= 0;
		boolean moreThanOne = Double.compare(progress, 1.0) >= 0;
		
		// To prevent the bar from showing the "ambiguous progress" animation.
		if (lessThanZero || moreThanOne)
		{
			System.out.println("[Controller] At limit, flashing.");
			view.flashRobotAngleBar();

			if (lessThanZero)
				progress = 0.0;
			else
				progress = 1.0;
		}
		
		view.setCannonAngleIndicator(progress);

		if (DEBUG_INFO)
			System.out.println("[Controller] Updating angle to: " + angle + "(" + progress + ") old " + oldProgress + " " + progress);
	}

	/**
	 * Updates the highscore table in the user interface.
	 */
	public void updateHighscores()
	{
		highscores.readFromFile();
		ObservableList<HighscoreListRow> scores = FXCollections.observableArrayList();

		int i = 1;
		for (Highscore h : highscores.getList())
		{
			scores.add(new HighscoreListRow(i++, h.getName(), h.getScore()));
		}

		view.setHighscores(scores);
	}

	/**
	 * Updates the player score text in the user interface to the specified
	 * value.
	 *
	 * @param _score
	 *            Score double to show in the interface.
	 */
	public void updateScore(final double _score)
	{
		// Some runnable magic Jose found online.
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				view.setPlayerScore(String.valueOf(_score) + " p");
			}
		});
	}

	/**
	 * Set the player name in the user interface.
	 *
	 * @param _name
	 *            Player name String.
	 */
	public void setPlayerName(final String _name)
	{
		view.setPlayerName(_name);
	}

	/**
	 * Set the remaining cannonballs in the user interface.
	 *
	 * @param _cannonballsLeft
	 *            Number of cannonballsleft as a String.
	 */
	public void setCannonballsLeft(final int _cannonballsLeft)
	{
		view.setCannonballsLeft(String.valueOf(_cannonballsLeft));
	}

	/**
	 * Used when the connection was unexpectedly terminated. For example when
	 * the program was shut down on the robot and there is no hope of automatic
	 * recovery.
	 */
	public void terminateConnection()
	{
		System.err.println("[Controller] TERMINATING CONNECTION");
		connected = false;
		endGame();
		disconnect();
	}

	/**
	 * @return <code>true</code> if there is a remote connection between the PC
	 *         and the robot, <code>false</code> otherwise
	 */
	public boolean isConnected()
	{
		return connected;
	}

	/**
	 * Processes user interface button presses with the mouse.
	 *
	 * @param guiButtonEvent
	 *            Button press event.
	 */
	public void mousePress(ActionEvent guiButtonEvent)
	{
		int id = View.BUTTONS.indexOf(((Button) guiButtonEvent.getSource()).getId());

		try
		{
			if (id > -1)
			{
				switch (id)
				{
					case View.BUTTON_SHOOT_UP:
						if (DEBUG_INFO)
							//System.out.println("[Controller] MOUSE PRESS UP");
						robot.shootCannonHigh();
						break;
					case View.BUTTON_SHOOT_DOWN:
						if (DEBUG_INFO)
							//System.out.println("[Controller] MOUSE PRESS DOWN");
						robot.shootCannonLow();
						break;
					case View.BUTTON_TURN_LEFT:
						if (DEBUG_INFO)
							//System.out.println("[Controller] MOUSE PRESS LEFT");
						robot.turnLeft();
						break;
					case View.BUTTON_TURN_RIGHT:
						if (DEBUG_INFO)
							//System.out.println("[Controller] MOUSE PRESS RIGHT");
						robot.turnRight();
						break;
					default:
						System.err.println("[Controller] Unknown key!");
						break;
				}
			}
			else
			{
				throw new Exception("Invalid button ID.");
			}
			Thread.sleep(100l);
			robot.stopTurning();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Processes user interface keyboard key press events.
	 *
	 * @param _keyboardEvent
	 *            Key press event.
	 */
	public void keyPress(KeyEvent _keyboardEvent)
	{
		KeyCode code = _keyboardEvent.getCode();
		if (code.isArrowKey())
		{
			switch (code)
			{
				case UP:
					if (DEBUG_INFO)
						//System.out.println("[Controller] KEY PRESS: shoot high");
					robot.shootCannonHigh();
					break;
				case DOWN:
					if (DEBUG_INFO)
						//System.out.println("[Controller] KEY PRESS: shoot low");
					robot.shootCannonLow();
					break;
				case LEFT:
					if (DEBUG_INFO)
						//System.out.println("[Controller] KEY PRESS: turn left");
					robot.turnLeft();
					break;
				case RIGHT:
					if (DEBUG_INFO)
						//System.out.println("[Controller] KEY PRESS: turn right");
					robot.turnRight();
					break;
				default:
					System.err.println("[Controller] Unknown key!");
					break;
			}
			view.visualPressControlButton(code);
		}
	}

	/**
	 * Updates the time left indicator in the view and sends a command to the
	 * robot to make beep sound.
	 *
	 * @param _percentOfTotal
	 *            Progress value for the progress bar.
	 */
	public void setTimeLeft(final double _percentOfTotal)
	{
		view.setTimeLeft(_percentOfTotal);

	}

	/**
	 * Processes user interface keyboard key release events.
	 *
	 * @param _keyEvent
	 *            Key release event.
	 */
	public void keyRelease(KeyEvent _keyEvent)
	{
		KeyCode code = _keyEvent.getCode();
		if (code.isArrowKey())
		{
			if (DEBUG_INFO)
			{
				switch (code)
				{
					case UP:
						//System.out.println("[Controller] KEY RELEASE: shoot up");
						break;
					case DOWN:
						//System.out.println("[Controller] KEY RELEASE: shoot down");
						break;
					case LEFT:
						//System.out.println("[Controller] KEY RELEASE: turn left");
						break;
					case RIGHT:
						//System.out.println("[Controller] KEY RELEASE: turn right");
						break;
					default:
						System.err.println("[Controller] Unknown key!");
						break;
				}
			}
			robot.stopTurning();
			view.visualReleaseControlButton(code);
		}
	}

	/**
	 * The method for starting a game with a new player.
	 */
	public void startGame()
	{
		System.out.println("[Controller] Connection? " + connected);

		if (connected)
		{
			System.out.println("[Controller] START GAME");

			view.setDisconnectButtonDisabled(true);

			player = new Player(this);

			player.setName(view.getName());
			view.setPlayerName(player.getName());
			view.setCannonballsLeft(String.valueOf(EV3Robot.MAX_BALLS));
			updateScore(player.getScore());

			robot.reset(player);

			view.disableGameControls(true, true);
			view.disableRobotControls(false);

			System.out.println("[Controller] Waiting for robot to initialize data streams...");
			while (!dataReceived)
			{
				try
				{
					Thread.sleep(Controller.SLEEP_TIME / 2l);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			System.out.println("[Controller] Robot data streams initialized.");

			gameOver = false;
			robot.setRobotControlEnabled(true);

			// robot.sendMaxIRDistance();
			robot.sendMinRedValue();
			// robot.setRobotControlEnabled(true);

			System.out.println("[Controller] Waiting for robot to be ready.");
			//robot.turnToAngle(0);
			while (!robot.isReady())
			{
				try
				{
					Thread.sleep(Controller.SLEEP_TIME / 2l);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			robot.printState();
			robot.clearPCBuffer();
			robot.clearWriteBuffer();
			System.out.println("[Controller] Ready to play!");
		}
	}

	/**
	 * Called when the game is over for the current player.
	 */
	public void endGame()
	{
		System.out.println("[Controller] END GAME");
		gameOver = true;
		robot.forceTurnToAngle(0);
		view.disableGameControls(true);

		try
		{
			Thread.sleep(5000l);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		view.setDisconnectButtonDisabled(false);

		if (robot != null)
			robot.deleteDataTransferThreads();

		if (demo != null)
			demo.stopRun();

		System.out.println("[Controller] Checking for highscore.");
		if (highscores.addScore(player))
			System.out.println("[Controller] " + player.getName() + " got a new highscore: " + player.getScore());
		else
			System.out.println("[Controller] No new highscore.");

		updateHighscores();

		view.setPlayerName("");
		view.setPlayerScore("");
		view.disableGameControls(false, true);
		view.disableRobotControls(true);
	}

	/**
	 * Set the given name into the player name textfield in the user interface.
	 *
	 * @param _name
	 *            The name to set in the field.
	 */
	public void setPlayerNameTextField(String _name)
	{
		view.setPlayerNameTextField(_name);
	}

	/**
	 * Begins the demo mode which controls the robot autonomously. Currently not
	 * functional.
	 */
	public void startDemoMode()
	{
		demo = new DemoMode(this, robot);
		demo.startDemo();
		demo.start();
	}

	/**
	 * Set the {@link dataReceived} boolean value to true to signify that the
	 * robot has sent some kind of data to the PC, which means that the data
	 * streams are working.
	 */
	public void dataReceived()
	{
		dataReceived = true;
	}

	public boolean isGameOver()
	{
		return gameOver;
	}
}
