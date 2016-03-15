package game.view;

import game.controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * The center pane of the user interface with the interface elements used to
 * control the robot remotely and display player information.
 *
 * @author Jose Uusitalo
 */
public class CenterPane implements EventHandler<ActionEvent>
{
	/**
	 * The root pane for this section of the user interface.
	 */
	private GridPane grid;

	/**
	 * Pane for showing player information like their name.
	 */
	private VBox playerInfo;

	/**
	 * Pane for holding robot control buttons.
	 */
	private GridPane controlPanel;

	/**
	 * @see Controller
	 */
	@SuppressWarnings("unused")
	private Controller controller;

	/**
	 * Button for shooting a high shot with the cannon.
	 *
	 * @see model.EV3Robot
	 */
	static Button btnShootUp;

	/**
	 * Button for shooting a low shot with the cannon.
	 *
	 * @see model.EV3Robot
	 */
	static Button btnShootDown;

	/**
	 * Button for turning left.
	 *
	 * @see model.EV3Robot
	 */
	static Button btnTurnLeft;

	/**
	 * Button for turning right.
	 *
	 * @see model.EV3Robot
	 */
	static Button btnTurnRight;

	/**
	 * Displays the player's score.
	 *
	 * @see model.Player
	 */
	static Label lblPlayerScoreText;

	/**
	 * Displays the player's name.
	 *
	 * @see model.Player
	 */
	static Label lblPlayerName;

	/**
	 * Displays the number of cannonballs left in the cannon.
	 *
	 * @see model.Player
	 */
	static Label lblCannonballsLeft;

	/**
	 * Displays the time remaining for this shot.
	 */
	static ProgressBar progbarTimeLeft;

	public CenterPane(final Controller _controller)
	{
		controller = _controller;
		grid = null;
	}

	/**
	 * Creates the pane if it does not exist.
	 *
	 * @return the center pane
	 */
	public GridPane getPane()
	{
		if (grid == null)
		{
			grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setGridLinesVisible(false);

			double gridGap = 40.0;

			grid.setHgap(gridGap);
			grid.setVgap(gridGap);

			grid.add(getPlayerInfoPane(), 0, 0);
			grid.add(getControlPane(), 0, 1);
		}

		return grid;
	}

	/**
	 * Creates the pane if it does not exist.
	 *
	 * @return the player information pane
	 */
	private VBox getPlayerInfoPane()
	{
		if (playerInfo == null)
		{
			playerInfo = new VBox();

			lblPlayerName = new Label("");
			lblPlayerName.getStyleClass().add("player-name");

			lblPlayerScoreText = new Label("");
			lblPlayerScoreText.getStyleClass().add("player-score");

			HBox ballCounter = new HBox();
			Label ballLabel = new Label("Cannonballs: ");
			ballLabel.getStyleClass().add("player-name");
			lblCannonballsLeft = new Label("");
			lblCannonballsLeft.getStyleClass().add("player-name");
			ballCounter.getChildren().addAll(ballLabel, lblCannonballsLeft);
			ballCounter.setAlignment(Pos.BASELINE_CENTER);

			Label timeLeftLabel = new Label("Aiming time left");
			timeLeftLabel.getStyleClass().add("timelefttext");

			// This is dumb if you ask me.
			lblPlayerName.setMaxWidth(Double.MAX_VALUE);
			lblPlayerScoreText.setMaxWidth(Double.MAX_VALUE);
			ballCounter.setMaxWidth(Double.MAX_VALUE);
			timeLeftLabel.setMaxWidth(Double.MAX_VALUE);

			progbarTimeLeft = new ProgressBar(1.0);
			progbarTimeLeft.setMaxWidth(Double.MAX_VALUE);
			progbarTimeLeft.setMinHeight(50);

			// playerInfo.getChildren().addAll(lblPlayerName,
			// lblPlayerScoreText, timeLeftLabel, progbarTimeLeft);
			playerInfo.getChildren().addAll(lblPlayerName, lblPlayerScoreText, ballCounter);
		}
		return playerInfo;
	}

	/**
	 * Creates the pane if it does not exist.
	 *
	 * @return GridPane with the necessary nodes for controlling the robot
	 */
	private GridPane getControlPane()
	{
		if (controlPanel == null)
		{
			double btnSize = 140;

			btnShootUp = new Button("FIRE\nHIGH");
			btnShootUp.setPrefHeight(btnSize);
			btnShootUp.setPrefWidth(btnSize);
			btnShootUp.setId(View.BUTTONS.get(0));

			btnShootDown = new Button("FIRE\nLOW");
			btnShootDown.setPrefHeight(btnSize);
			btnShootDown.setPrefWidth(btnSize);
			btnShootDown.setId(View.BUTTONS.get(1));

			btnTurnLeft = new Button("TURN\nLEFT");
			btnTurnLeft.setPrefHeight(btnSize);
			btnTurnLeft.setPrefWidth(btnSize);
			btnTurnLeft.setId(View.BUTTONS.get(2));

			btnTurnRight = new Button("TURN\nRIGHT");
			btnTurnRight.setPrefHeight(btnSize);
			btnTurnRight.setPrefWidth(btnSize);
			btnTurnRight.setId(View.BUTTONS.get(3));

			btnShootUp.getStyleClass().add("bevelGray");
			btnShootDown.getStyleClass().add("bevelGray");
			btnTurnLeft.getStyleClass().add("bevelGray");
			btnTurnRight.getStyleClass().add("bevelGray");

			btnShootUp.setOnAction(this);
			btnShootDown.setOnAction(this);
			btnTurnLeft.setOnAction(this);
			btnTurnRight.setOnAction(this);

			controlPanel = new GridPane();
			controlPanel.setHgap(20);
			controlPanel.setVgap(20);
			controlPanel.setGridLinesVisible(false);

			controlPanel.add(btnShootUp, 1, 0);
			controlPanel.add(btnTurnLeft, 0, 1);
			controlPanel.add(btnShootDown, 1, 1);
			controlPanel.add(btnTurnRight, 2, 1);
		}
		return controlPanel;
	}

	/**
	 * Handles the <b>mouse click</b> on the buttons used for robot remote
	 * control. Keyboard events are handled in {@link View}.
	 *
	 * @param event
	 *            Mouse click event.
	 */
	@Override
	public void handle(ActionEvent event)
	{
		controller.mousePress(event);
	}
}
