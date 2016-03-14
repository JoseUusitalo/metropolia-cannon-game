package game.view;

import java.util.Arrays;
import java.util.List;

import game.controller.Controller;
import game.model.EV3Robot;
import game.model.HighscoreListRow;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * The user interface in the MVC model.
 *
 * @author Jose Uusitalo
 */
public class View extends Application
{
	/**
	 * A magic number representing the "Shoot Up" button in the user interface.
	 */
	public static final int BUTTON_SHOOT_UP = 0;

	/**
	 * A magic number representing the "Shoot Down" button in the user
	 * interface.
	 */
	public static final int BUTTON_SHOOT_DOWN = 1;

	/**
	 * A magic number representing the "Turn Left" button in the user interface.
	 */
	public static final int BUTTON_TURN_LEFT = 2;

	/**
	 * A magic number representing the "Turn Right" button in the user
	 * interface.
	 */
	public static final int BUTTON_TURN_RIGHT = 3;

	/**
	 * A list of button ID strings used in the interface. The order of these
	 * strings is hardcoded and must not be changed.
	 */
	public static final List<String> BUTTONS = Arrays.asList("shootUp", "shootDown", "turnLeft", "turnRight");

	/**
	 * @see Controller
	 */
	private Controller controller;

	/**
	 * The root panel of the window, which contains all the nodes.
	 */
	private BorderPane rootBorderPane;

	/**
	 * The main control panel which contains the user interface elements the
	 * user interacts with.
	 */
	private BorderPane controlPane;

	/**
	 * Whether or not the control pane controls are disabled. (Grayed out.)
	 */
	private boolean controlsEnabled;

	/**
	 * Initializes the controller.
	 */
	public View()
	{
		controller = new Controller(this);
	}

	/**
	 * Launches the application.
	 *
	 * @param args
	 *            An array of string containing various system provided
	 *            arguments.
	 */
	public static void main(String[] args)
	{
		launch(args);
	}

	/**
	 * Opens the user interface.
	 *
	 * @param _primaryStage
	 *            A Stage object provided by the system.
	 */
	@Override
	public void start(final Stage _primaryStage)
	{
		try
		{
			setUserAgentStylesheet(STYLESHEET_MODENA);
			rootBorderPane = createRoot();
			rootBorderPane.setPadding(new Insets(10, 10, 10, 10));

			Scene scene = new Scene(rootBorderPane, rootBorderPane.getPrefWidth(), rootBorderPane.getPrefHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			scene.setOnKeyPressed(new EventHandler<KeyEvent>()
			{
				@Override
				public void handle(KeyEvent _keyEvent)
				{
					if (controlsEnabled)
						controller.keyPress(_keyEvent);
				}
			});

			scene.setOnKeyReleased(new EventHandler<KeyEvent>()
			{
				@Override
				public void handle(KeyEvent _keyEvent)
				{
					if (controlsEnabled)
						controller.keyRelease(_keyEvent);
				}
			});

			_primaryStage.setScene(scene);
			_primaryStage.setTitle("ILJO-3 TVT 14 Cannon Game");

			_primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
			{

				@Override
				public void handle(WindowEvent event)
				{
					System.out.println("[View] Shutting down.");
					controller.disconnect();
				}
			});

			disableConnectionControls(false);
			disableGameControls(true);
			disableRobotControls(true);
			System.out.println("[View] Updating highscores.");
			controller.updateHighscores();

			_primaryStage.show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Creates the root pane of the interface by connecting other layout panes
	 * together. The root pane is the topmost pane in the hierarchy containing
	 * all other user interface elements.
	 *
	 * @return the root pane
	 */
	private BorderPane createRoot()
	{
		rootBorderPane = new BorderPane();
		controlPane = new BorderPane();

		LeftPane leftPane = new LeftPane(controller);
		CenterPane centerPane = new CenterPane(controller);
		BottomPane bottomPane = new BottomPane();
		RightPane rightPane = new RightPane();

		controlPane.setCenter(centerPane.getPane());
		controlPane.setBottom(bottomPane.getPane());

		rootBorderPane.setLeft(leftPane.getPane());
		rootBorderPane.setCenter(controlPane);
		rootBorderPane.setRight(rightPane.getPane());

		return rootBorderPane;
	}

	/**
	 * Gets the string the player input into the
	 * {@link LeftPane#fieldPlayerName} text field in the connection controls of
	 * {@link LeftPane}.
	 *
	 * @return the player name
	 */
	public String getName()
	{
		return LeftPane.fieldPlayerName.getText();
	}

	/**
	 * Gets the string the player input into the
	 * {@link LeftPane#fieldPlayerName} text field in the connection controls of
	 * {@link LeftPane}.
	 *
	 * @return the robot IP address as a String
	 */
	public String getIP()
	{
		return LeftPane.fieldRobotIP.getText();
	}

	/**
	 * Gets the number the player input into the {@link LeftPane#fieldRobotIP}
	 * text field in the connection controls of {@link LeftPane}.
	 *
	 * @return the robot port as an int
	 */
	public int getPort()
	{
		return Integer.parseInt(LeftPane.fieldRobotPort.getText());
	}

	/**
	 * Show an error message dialog to the user.
	 *
	 * @param _message
	 *            the string to be shown to the user
	 */
	public void errorMessage(final String _message)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setContentText(_message);
		alert.showAndWait();
	}

	/**
	 * Disables the remote connection controls in the interface and toggles the
	 * state of the disconnect button. The disconnect button is disabled when
	 * there is no connection to the robot and enabled when a connection has
	 * been established.
	 *
	 * @param _disabled
	 *            The boolean value defining whether to disable the interface
	 *            elements. <code>true</code> to disable all controls, but
	 *            enable the disconnect button. <code>false</code> to enable all
	 *            controls, but disable the disconnect button.
	 * @see LeftPane
	 */
	public void disableConnectionControls(final boolean _disabled)
	{
		ObservableList<Node> connectionControls = ((VBox) (((VBox) rootBorderPane.getLeft()).getChildren()).get(0))
				.getChildren();

		for (Node n : connectionControls)
		{
			n.setDisable(_disabled);
		}

		setDisconnectButtonDisabled(!_disabled);
	}

	/**
	 * Disables the game round controls in the user interface.
	 *
	 * @param _disabled
	 *            <code>true</code> to disable the interface controls,
	 *            <code>false</code> to enable them.
	 */
	public void disableGameControls(final boolean _disabled)
	{
		ObservableList<Node> gameControls = ((VBox) (((VBox) rootBorderPane.getLeft()).getChildren()).get(1))
				.getChildren();

		for (Node n : gameControls)
		{
			n.setDisable(_disabled);
		}
	}

	/**
	 * Disables the game round controls in the user interface and toggles the
	 * state of the end game button. The button will be enabled when the rest of
	 * game controls are disabled or disabled when the rest of the game controls
	 * are enabled.
	 *
	 * @param _disabled
	 *            <code>true</code> to disable the interface controls,
	 *            <code>false</code> to enable them.
	 * @param _toggleEndGame
	 *            <code>true</code> to toggle the state of the end game button,
	 *            <code>false</code> to enable/disable it with the rest of the
	 *            game controls.
	 */
	public void disableGameControls(final boolean _disabled, final boolean _toggleEndGame)
	{
		if (!_toggleEndGame)
		{
			disableGameControls(_disabled);
		}
		else
		{
			ObservableList<Node> gameControls = ((VBox) (((VBox) rootBorderPane.getLeft()).getChildren()).get(1))
					.getChildren();

			for (Node n : gameControls)
			{
				n.setDisable(_disabled);
			}

			LeftPane.btnEndGame.setDisable(!_disabled);
		}
	}

	/**
	 * Disables the main robot controls at the center of the window.
	 *
	 * @param _disabled
	 *            a boolean representing the state of the controls.
	 *            <code>true</code> to disable all controls, <code>false</code>
	 *            to enable all controls.
	 * @see CenterPane
	 */
	public void disableRobotControls(final boolean _disabled)
	{
		controlsEnabled = !(_disabled);
		ObservableList<Node> centerNodes = ((GridPane) controlPane.getCenter()).getChildren();
		ObservableList<Node> bottomNodes = ((VBox) controlPane.getBottom()).getChildren();
		for (Node n : centerNodes)
		{
			n.setDisable(_disabled);
		}
		for (Node n : bottomNodes)
		{
			n.setDisable(_disabled);
		}
	}

	/**
	 * Sets the robot remote connection TextFields to their default values.
	 *
	 * @param _connectionDefaults
	 *            A String array containing the default values for the text
	 *            fields.
	 * @see controller.Controller#getConnectionDefaults()
	 */
	public void setConnectionDefaults(final String[] _connectionDefaults)
	{
		LeftPane.fieldRobotIP.setText(_connectionDefaults[0]);
		LeftPane.fieldRobotPort.setText(_connectionDefaults[1]);
	}

	/**
	 * Changes the visual style of the {@link CenterPane} robot control buttons
	 * to their pushed down state. The change is pure cosmetic.
	 *
	 * @param _code
	 *            A KeyCode representing the keyboard key that was pressed.
	 * @see Controller#keyPress(KeyEvent)
	 */
	public void visualPressControlButton(final KeyCode _code)
	{
		if (Controller.DEBUG_INFO)
			System.out.println("[View] VIS PRESS: " + _code);
		switch (_code)
		{
			case UP:
				CenterPane.btnShootUp.getStyleClass().remove(1);
				CenterPane.btnShootUp.getStyleClass().add("bevelGrayFakePress");
				break;
			case DOWN:
				CenterPane.btnShootDown.getStyleClass().remove(1);
				CenterPane.btnShootDown.getStyleClass().add("bevelGrayFakePress");
				break;
			case RIGHT:
				CenterPane.btnTurnRight.getStyleClass().remove(1);
				CenterPane.btnTurnRight.getStyleClass().add("bevelGrayFakePress");
				break;
			case LEFT:
				CenterPane.btnTurnLeft.getStyleClass().remove(1);
				CenterPane.btnTurnLeft.getStyleClass().add("bevelGrayFakePress");
				break;
			default:
				break;
		}
	}

	/**
	 * Changes the visual style of the {@link CenterPane} robot control buttons
	 * to their default idle state. The change is pure cosmetic.
	 *
	 * @param _code
	 *            A KeyCode representing the keyboard key that was released.
	 * @see Controller#keyRelease(KeyEvent)
	 */
	public void visualReleaseControlButton(final KeyCode _code)
	{
		if (Controller.DEBUG_INFO)
			System.out.println("[View] VIS RELEASE: " + _code);
		switch (_code)
		{
			case UP:
				CenterPane.btnShootUp.getStyleClass().remove(1);
				CenterPane.btnShootUp.getStyleClass().add("bevelGray");
				break;
			case DOWN:
				CenterPane.btnShootDown.getStyleClass().remove(1);
				CenterPane.btnShootDown.getStyleClass().add("bevelGray");
				break;
			case RIGHT:
				CenterPane.btnTurnRight.getStyleClass().remove(1);
				CenterPane.btnTurnRight.getStyleClass().add("bevelGray");
				break;
			case LEFT:
				CenterPane.btnTurnLeft.getStyleClass().remove(1);
				CenterPane.btnTurnLeft.getStyleClass().add("bevelGray");
				break;
			default:
				System.err.println("[View] Unknown key code!");
				break;
		}
	}

	/**
	 * Visually simulates pressing down the robot remote control buttons without
	 * firing any actions.
	 *
	 * @param _code
	 *            ID of the button to press.
	 * @see View#BUTTON_SHOOT_UP
	 * @see View#BUTTON_SHOOT_DOWN
	 * @see View#BUTTON_TURN_LEFT
	 * @see View#BUTTON_TURN_RIGHT
	 */
	public void visualPressControlButton(final int _code)
	{
		if (Controller.DEBUG_INFO)
			//System.out.println("[View] VIS PRESS: " + _code);

		switch (_code)
		{
			case BUTTON_SHOOT_UP:
				CenterPane.btnShootUp.getStyleClass().remove(1);
				CenterPane.btnShootUp.getStyleClass().add("bevelGrayFakePress");
				break;
			case BUTTON_SHOOT_DOWN:
				CenterPane.btnShootDown.getStyleClass().remove(1);
				CenterPane.btnShootDown.getStyleClass().add("bevelGrayFakePress");
				break;
			case BUTTON_TURN_RIGHT:
				CenterPane.btnTurnRight.getStyleClass().remove(1);
				CenterPane.btnTurnRight.getStyleClass().add("bevelGrayFakePress");
				break;
			case BUTTON_TURN_LEFT:
				CenterPane.btnTurnLeft.getStyleClass().remove(1);
				CenterPane.btnTurnLeft.getStyleClass().add("bevelGrayFakePress");
				break;
			default:
				System.err.println("[View] Unknown button ID.");
				break;
		}
	}

	/**
	 * Visually simulates releasing the robot remote control buttons without
	 * firing any actions.
	 *
	 * @param _code
	 *            ID of the button to press.
	 * @see View#BUTTON_SHOOT_UP
	 * @see View#BUTTON_SHOOT_DOWN
	 * @see View#BUTTON_TURN_LEFT
	 * @see View#BUTTON_TURN_RIGHT
	 */
	public void visualReleaseControlButton(final int _code)
	{
		if (Controller.DEBUG_INFO)
			//System.out.println("[View] VIS RELEASE: " + _code);

		switch (_code)
		{
			case BUTTON_SHOOT_UP:
				CenterPane.btnShootUp.getStyleClass().remove(1);
				CenterPane.btnShootUp.getStyleClass().add("bevelGray");
				break;
			case BUTTON_SHOOT_DOWN:
				CenterPane.btnShootDown.getStyleClass().remove(1);
				CenterPane.btnShootDown.getStyleClass().add("bevelGray");
				break;
			case BUTTON_TURN_RIGHT:
				CenterPane.btnTurnRight.getStyleClass().remove(1);
				CenterPane.btnTurnRight.getStyleClass().add("bevelGray");
				break;
			case BUTTON_TURN_LEFT:
				CenterPane.btnTurnLeft.getStyleClass().remove(1);
				CenterPane.btnTurnLeft.getStyleClass().add("bevelGray");
				break;
			default:
				System.err.println("[View] Unknown button ID.");
				break;
		}
	}

	/**
	 * Sets a new value into the time left indicator progressbar.
	 *
	 * @param _percentOfTotal
	 *            A double in the range [0.0, 1.0].
	 * @see CenterPane#progbarTimeLeft
	 */
	public void setTimeLeft(final double _percentOfTotal)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				CenterPane.progbarTimeLeft.setProgress(_percentOfTotal);
			}
		});
	}

	/**
	 * Set the robot angle indicator progressbar on the user interface to the
	 * specified value.
	 *
	 * @param _value
	 *            A linearly scaling double representing the angle value where:
	 *            <br>
	 *            0.0 = 90 deg<br>
	 *            0.5 = 0 deg<br>
	 *            1.0 = -90 deg<br>
	 * @see BottomPane#progbarCannonAngle
	 * @see Controller#toPercentage(double, double, double)
	 */
	public void setCannonAngleIndicator(final double _value)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				BottomPane.progbarCannonAngle.setProgress(_value);
			}
		});
	}

	/**
	 * Flashes the robot angle indicator at the bottom of the interface and
	 * briefly shows a warning text to notify the user they attempted to rotate
	 * the robot over its {@link EV3Robot#LIMIT_ANGLE_MIN}.
	 *
	 * @see BottomPane#progbarCannonAngle
	 */
	public void flashRobotAngleBar()
	{
		final Animation flashAngleBar = new Transition()
		{
			{
				setCycleCount(1);
				setCycleDuration(new Duration(100.0));
			}

			@Override
			protected void interpolate(double frac)
			{
				// Not an ideal solution. There are probably some sort of
				// keyframes but I do not know how to use them.
				if (getCurrentTime().lessThanOrEqualTo(new Duration(95.0)))
				{
					BottomPane.progbarCannonAngle.getStyleClass().remove(1);
					BottomPane.progbarCannonAngle.getStyleClass().add("progbar-red");
				}
				else if (getCurrentTime().compareTo(new Duration(100.0)) == 0)
				{
					BottomPane.progbarCannonAngle.getStyleClass().remove(1);
					BottomPane.progbarCannonAngle.getStyleClass().add("progbar-purple");
				}
			}
		};

		flashAngleBar.playFromStart();
	}

	/**
	 * Set new data into the highscore list.
	 *
	 * @param _scores
	 *            An observable list containing {@link HighscoreListRow} data of
	 *            the new highscores.
	 * @see RightPane#highscores
	 */
	public void setHighscores(final ObservableList<HighscoreListRow> _scores)
	{
		@SuppressWarnings("unchecked")
		TableView<HighscoreListRow> tableView = (TableView<HighscoreListRow>) ((BorderPane) rootBorderPane.getRight())
				.getRight();
		tableView.setItems(_scores);
	}

	/**
	 * Sets the text in the interface which displays the player's current score
	 *
	 * @param score
	 *            New score value as text.
	 * @author Ilkka Varjokunnas
	 */
	public void setPlayerScore(final String score)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				CenterPane.lblPlayerScoreText.setText(score);
			}
		});
	}

	/**
	 * Set the name to show on the interface.
	 *
	 * @param _name
	 *            String of the player name.
	 */
	public void setPlayerName(final String _name)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				CenterPane.lblPlayerName.setText(_name);
			}
		});
	}

	/**
	 * Set the amount of remaining cannonballs to show on the interface.
	 *
	 * @param _cannonballsLeft
	 *            String of the amount of remaining cannonballs.
	 */
	public void setCannonballsLeft(final String _cannonballsLeft)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				CenterPane.lblCannonballsLeft.setText(_cannonballsLeft);
			}
		});
	}

	/**
	 * Sets the player name in the text field in game start/end controls.
	 *
	 * @param _name
	 *            New player name String.
	 */
	public void setPlayerNameTextField(final String _name)
	{
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				LeftPane.fieldPlayerName.setText(_name);
			}
		});
	}

	/**
	 * @return the progress value of the cannon angle indicator progress bar
	 */
	public double getCannonAngleBarProgress()
	{
		return BottomPane.progbarCannonAngle.getProgress();
	}

	/**
	 * Enable/disable the disconnect button.
	 *
	 * @param _value
	 *            <code>true</code> to enable, <code>false</code> to disable.
	 */
	public void setDisconnectButtonDisabled(boolean _value)
	{
		LeftPane.btnDisconnectFromRobot.setDisable(_value);
	}
}
