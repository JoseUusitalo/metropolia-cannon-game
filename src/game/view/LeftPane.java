package game.view;

import game.controller.Controller;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * The left pane holds the interface elements for creating (and disconnecting) a
 * remote connection to the robot as well as starting a new game and ending the
 * current game.
 *
 * @author Jose Uusitalo
 * @see model.EV3Robot
 */
public class LeftPane
{
	/**
	 * Textfield where the user enters their name.
	 */
	static TextField fieldPlayerName;

	/**
	 * Textfield for entering the IP of the robot to connect to.
	 */
	static TextField fieldRobotIP;

	/**
	 * Textfield for entering the port of the robot to connect to.
	 */
	static TextField fieldRobotPort;

	/**
	 * Button used to create a connection to the specified robot.
	 */
	private Button btnConnectToRobot;

	/**
	 * Button used to close the connection to the currently connected robot.
	 */
	static Button btnDisconnectFromRobot;

	/**
	 * Button for starting the game.
	 */
	static Button btnStartGame;

	/**
	 * Button for stopping the current game.
	 */
	static Button btnEndGame;

	/**
	 * Button for starting the game in demo mode.
	 */
	static Button btnDemoMode;

	/**
	 * @see Controller
	 */
	private Controller controller;

	/**
	 * The root pane for this section of the user interface.
	 */
	private VBox vbox;

	/**
	 * The preferred width of all interface elements.
	 */
	private final double prefWidth = 140.0;

	public LeftPane(final Controller _controller)
	{
		controller = _controller;
		vbox = null;
	}

	/**
	 * Creates the pane if it does not exist.
	 *
	 * @return GridPane with the remote connection and game start/end controls
	 */
	public VBox getPane()
	{
		if (vbox == null)
		{
			vbox = new VBox(10.0);
			vbox.setPadding(new Insets(0, 40, 0, 0));

			vbox.getChildren().addAll(getConnectionPane(), getGamePane());

			// Validator code adapted from:
			// https://stackoverflow.com/questions/23579438/form-validator-message

			final ContextMenu nameValidator = new ContextMenu();
			nameValidator.getStyleClass().add("validator-contextmenu");
			nameValidator.setAutoHide(false);

			final ContextMenu ipValidator = new ContextMenu();
			ipValidator.getStyleClass().add("validator-contextmenu");
			ipValidator.setAutoHide(false);

			final ContextMenu portValidator = new ContextMenu();
			portValidator.getStyleClass().add("validator-contextmenu");
			portValidator.setAutoHide(false);

			btnConnectToRobot.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent event)
				{
					nameValidator.hide();
					ipValidator.hide();
					portValidator.hide();

					if (validateConnectionFields())
					{
						if (Controller.DEBUG)
							controller.debugConnect();
						else
							controller.connect();
					}
				}

				private boolean validateConnectionFields()
				{
					boolean ok = true;

					MenuItem ipMissing = new MenuItem("Syötä IP-osoite.");
					MenuItem portMissing = new MenuItem("Syötä portti.");
					MenuItem portNegative = new MenuItem("Syötä positiivinen porttinumero.");
					MenuItem portNotInt = new MenuItem("Syötä kokonaisluku.");

					ipMissing.getStyleClass().add("validator-menuitem");
					portMissing.getStyleClass().add("validator-menuitem");
					portNegative.getStyleClass().add("validator-menuitem");
					portNotInt.getStyleClass().add("validator-menuitem");

					// IP empty?
					if (fieldRobotIP.getText().isEmpty() || fieldRobotIP.getText().equals(" "))
					{
						ipValidator.getItems().clear();
						ipValidator.getItems().add(ipMissing);
						ipValidator.show(fieldRobotIP, Side.RIGHT, 5, 0);
						ok = false;
					}

					// Port empty?
					if (fieldRobotPort.getText().isEmpty() || fieldRobotPort.getText().equals(" "))
					{
						portValidator.getItems().clear();
						portValidator.getItems().add(portMissing);
						portValidator.show(fieldRobotPort, Side.RIGHT, 5, 0);
						ok = false;
					}
					else
					{
						int _port = 0;

						try
						{
							_port = Integer.parseInt(fieldRobotPort.getText());

							if (_port < 0)
							{
								portValidator.getItems().clear();
								portValidator.getItems().add(portNegative);
								portValidator.show(fieldRobotPort, Side.RIGHT, 5, 0);
								ok = false;
							}
						}
						catch (NumberFormatException e)
						{
							portValidator.getItems().clear();
							portValidator.getItems().add(portNotInt);
							portValidator.show(fieldRobotPort, Side.RIGHT, 5, 0);
							ok = false;
						}
					}

					return ok;
				}
			});

			btnDisconnectFromRobot.setOnAction(new EventHandler<ActionEvent>()
			{

				@Override
				public void handle(ActionEvent event)
				{
					controller.disconnect();
				}
			});

			// Hide the error message when the field is in focus.
			fieldRobotIP.focusedProperty().addListener(new ChangeListener<Boolean>()
			{
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue,
						Boolean newPropertyValue)
				{
					if (newPropertyValue)
					{
						ipValidator.hide();
					}
				}
			});

			// Hide the error message when the field is in focus.
			fieldRobotPort.focusedProperty().addListener(new ChangeListener<Boolean>()
			{
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue,
						Boolean newPropertyValue)
				{
					if (newPropertyValue)
					{
						portValidator.hide();
					}
				}
			});
		}

		return vbox;
	}

	/**
	 * Creates the pane if it does not exist.
	 *
	 * @return VBox with the interface controls for starting and stopping a game
	 */
	private VBox getGamePane()
	{
		VBox _vbox = new VBox(10.0);

		Label name = new Label("Pelaaja");
		fieldPlayerName = new TextField();
		fieldPlayerName.setMaxWidth(prefWidth);

		// Limiting the length of the string in the text field.
		fieldPlayerName.lengthProperty().addListener(new ChangeListener<Number>()
		{

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				if (newValue.intValue() > oldValue.intValue())
				{
					if (fieldPlayerName.getText().length() >= Controller.PLAYER_NAME_MAX_LENGTH)
					{

						fieldPlayerName
								.setText(fieldPlayerName.getText().substring(0, Controller.PLAYER_NAME_MAX_LENGTH));
					}
				}
			}
		});

		btnStartGame = new Button("Aloita");
		btnStartGame.setPrefWidth(prefWidth);

		btnEndGame = new Button("Lopeta")
		{
			@Override
			public void requestFocus()
			{
				// Disable focus traversal.
			}
		};
		btnEndGame.setPrefWidth(prefWidth);

		btnDemoMode = new Button("Demo");
		btnDemoMode.setPrefWidth(prefWidth);

		// Validator code adapted from:
		// https://stackoverflow.com/questions/23579438/form-validator-message

		final ContextMenu nameValidator = new ContextMenu();
		nameValidator.getStyleClass().add("validator-contextmenu");
		nameValidator.setAutoHide(false);

		btnDemoMode.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				controller.startDemoMode();
			}
		});

		btnEndGame.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				controller.endGame();
			}
		});

		btnStartGame.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				nameValidator.hide();

				if (validateName())
				{
					controller.startGame();
				}
			}

			private boolean validateName()
			{
				boolean ok = true;

				MenuItem nameMissing = new MenuItem("Syötä pelaajan nimi.");

				nameMissing.getStyleClass().add("validator-menuitem");

				// Name is empty?
				if (fieldPlayerName.getText().isEmpty() || fieldPlayerName.getText().equals(" "))
				{
					nameValidator.getItems().clear();
					nameValidator.getItems().add(nameMissing);
					nameValidator.show(fieldPlayerName, Side.RIGHT, 5, 0);
					ok = false;
				}

				return ok;
			}
		});

		// Hide the error message when the field is in focus.
		fieldPlayerName.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue,
					Boolean newPropertyValue)
			{
				if (newPropertyValue)
				{
					nameValidator.hide();
				}
			}
		});

		_vbox.getChildren().addAll(btnDemoMode, name, fieldPlayerName, btnStartGame, btnEndGame);
		return _vbox;
	}

	/**
	 * Creates the pane if it does not exist.
	 *
	 * @return VBox with the remote connection controls
	 */
	private VBox getConnectionPane()
	{
		VBox _vbox = new VBox(10.0);

		Label ip = new Label("IP");
		fieldRobotIP = new TextField();
		fieldRobotIP.setMaxWidth(prefWidth);

		/*
		 * Limiting the length of the string in the text field. Code from:
		 * http://stackoverflow.com/a/22720553
		 */
		fieldRobotIP.lengthProperty().addListener(new ChangeListener<Number>()
		{
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
			{
				if (newValue.intValue() > oldValue.intValue())
				{
					if (fieldPlayerName.getText().length() >= 15)
					{

						fieldPlayerName.setText(fieldPlayerName.getText().substring(0, 15));
					}
				}
			}
		});

		Label port = new Label("Portti");
		fieldRobotPort = new TextField();
		fieldRobotPort.setMaxWidth(prefWidth);

		Button btnDefaults = new Button("Lataa oletukset");
		btnDefaults.setPrefWidth(prefWidth);

		btnDefaults.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent event)
			{
				controller.setConnectionDefaults();
			}
		});

		btnConnectToRobot = new Button("Yhdistä");
		btnConnectToRobot.setPrefWidth(prefWidth);

		btnDisconnectFromRobot = new Button("Katkaise yhteys");
		btnDisconnectFromRobot.setPrefWidth(prefWidth);

		_vbox.getChildren().addAll(ip, fieldRobotIP, port, fieldRobotPort, btnDefaults, btnConnectToRobot,
				btnDisconnectFromRobot);
		return _vbox;
	}
}
