package game.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import game.controller.Controller;

/**
 * A thread dedicated to receiving data from the robot.
 *
 * @author Jose Uusitalo
 */
public class DataReaderThread extends Thread
{
	/**
	 * <p>
	 * An array of data type IDs that can be received from the robot.
	 * </p>
	 * <p>
	 * a = Angle data from the main motor tha rotates the cannon.<br>
	 * i = Infrared sensor reading from the infrared sensor.<br>
	 * c = Color data from the color sensor.<br>
	 * f = Finished firing the cannon.<br>
	 * t = Finished turning the cannon.<br>
	 * z = Debug.
	 * </p>
	 */
	public static final List<Character> dataTypes = Collections
			.unmodifiableList(Arrays.asList('a', 'i', 'c', 'f', 't', 'z', ' ', '-'));

	/**
	 * The input stream from where the angle is read from.
	 */
	private DataInputStream in;

	/**
	 * Whether or not to read data from the input stream.
	 */
	private boolean read;

	/**
	 * <p>
	 * The input data read from the robot in a string format.
	 * </p>
	 * <p>
	 * The format of the data is as follows: {@literal <ID char><Data>}
	 * </p>
	 * <p>
	 * Example input containing a motor angle value: <code>a12</code><br>
	 * Example input containing an infrared reading: <code>i20.09</code><br>
	 * Example input containing an RGB array: <code>c255,198,5</code><br>
	 * </p>
	 *
	 * @see DataReaderThread#dataTypes
	 */
	private String input;

	/**
	 * The previously read input data. It is not necessary to process the same
	 * input over and over again in this project.
	 */
	private String previousInput;

	/**
	 * The data type which is the first <code>char</code> of the input data.
	 *
	 * @see DataReaderThread#input
	 */
	private char id;

	/**
	 * MVC-model controller
	 *
	 * @see Controller
	 */
	private Controller controller;

	/**
	 * The robot from where the data comes from.
	 */
	private EV3Robot robot;

	/**
	 * Whether or not any data has been received from the robot.
	 */
	private boolean dataReceived;

	/**
	 * The player whose score is to be modified.
	 */
	private Player player;

	public DataReaderThread(final DataInputStream _in, final Controller _controller, final EV3Robot _robot,
			final Player _player)
	{
		in = _in;
		robot = _robot;
		player = _player;
		controller = _controller;
		input = " "; // No data initially.
		previousInput = "-"; // No data symbol 2.
	}

	/**
	 * Starts the thread.
	 */
	@Override
	public void run()
	{
		while (read && controller.isConnected())
		{
			try
			{
				if (!Controller.DEBUG)
					input = in.readUTF();

				if (!input.equals(previousInput))
				{
					previousInput = input;
					id = input.charAt(0);

					if (id != 'c' && id != 'a')
						System.out.println("[DataReaderThread] Read '" + input + "'");

					if (dataTypes.contains(id))
					{
						switch (id)
						{
							case 'a':
								robot.setCurrentAngle(Integer.parseInt(input.substring(1)));
								break;
							case 'i':
								player.scoreIR(Float.parseFloat(input.substring(1)));
								break;
							case 'c':
								player.scoreColor(input.substring(1));
								break;
							case 'f':
								robot.setShootingInProgress(false);
								break;
							case 't':
								robot.setAngleTurnInProgress(false);
								break;
							case '-':
							case ' ':
								// Pass.
								break;
							default:
								System.err.println("[DataReaderThread] Unknown command character!");
								break;
						}
					}
					else
					{
						System.err.println("[DataReaderThread] Unknown data type ID.");
					}
				}

				if (input != null && !dataReceived)
				{
					dataReceived = true;
					controller.dataReceived();
				}
			}
			catch (SocketException e)
			{
				/*
				 * This error should not happen because the connection status is
				 * checked before the code that throws this exception is run,
				 * and if the check does not pass the code does not run.
				 */
				System.err.println("[DataReaderThread] SocketException. This was not supposed to happen.");
				e.printStackTrace();
			}
			catch (IOException e)
			{
				System.err.println("[DataReaderThread] Error reading data. Robot program shut down.");
				controller.endGame();
				controller.disconnect();
			}

			try
			{
				Thread.sleep(Controller.SLEEP_TIME);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Begin the value reading loop and receive data.
	 */
	public void startReading()
	{
		System.out.println("[DataReaderThread] STARTING");
		read = true;
	}

	/**
	 * Stop the value reading loop and stop receiving data.
	 */
	public void stopReading()
	{
		System.out.println("[DataReaderThread] STOPPING");
		read = false;
	}

	/**
	 * <p>
	 * <b>FOR DEBUG USE ONLY.</b>
	 * </p>
	 * <p>
	 * Fakes data read by this thread to read the given angle value. Will not do
	 * anything if debug mode is not enabled.
	 * </p>
	 *
	 * @param _angle
	 *            The integer angle this thread reads.
	 */
	public void debugModAngle(final int _angle)
	{
		System.out.println("[DataReaderThread] Faking rotation angle to " + _angle);
		if (Controller.DEBUG)
			input = "a" + _angle;
	}

	/**
	 * <p>
	 * <b>FOR DEBUG USE ONLY.</b>
	 * </p>
	 * <p>
	 * Fakes data received from the robot.
	 * Will not do anything if debug mode is not enabled.
	 * </p>
	 *
	 * @param _angle
	 *            The data this thread reads.
	 */
	public void debugWrite(final String _data)
	{
		if (Controller.DEBUG)
		{
			try
			{
				Thread.sleep(Controller.SLEEP_TIME * 2);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
			System.out.println("[DataReaderThread] Faking data: " + _data);
			input = _data;
		}
	}

	/**
	 * Clear all read data.
	 */
	public void clearBufferData()
	{
		System.out.println("[DataReaderThread] Clearing data");
		previousInput = "-";
		input = " ";
	}
}
