package game.model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import game.controller.Controller;

/**
 * A thread dedicated to sending commands to the robot.
 *
 * @author Jose Uusitalo
 */
public class DataWriterThread extends Thread
{
	/**
	 * The output stream where the data is written to.
	 */
	private DataOutputStream out;

	/**
	 * Whether or not to write data to the output stream.
	 */
	private boolean write;

	/**
	 * The command that is sent to the robot.
	 */
	private volatile List<String> commandBuffer;

	/**
	 * The previous command sent to the robot.
	 */
	private String prevCommandString;

	/**
	 * The {@link Controller}.
	 */
	private Controller controller;

	public DataWriterThread(final DataOutputStream _out, final Controller _controller)
	{
		out = _out;
		commandBuffer = new ArrayList<String>(); // No data.
		prevCommandString = "!"; // An unused command char.
		controller = _controller;
	}

	/**
	 * Starts the thread.
	 */
	@Override
	public void run()
	{
		while (write)
		{
			// Don't send anything if there's nothing to send.
			if (commandBuffer.size() > 0)
			{
				// The first element is sometimes null here for some reason.
				if (commandBuffer.get(0) != null)
				{
					if (!commandBuffer.get(0).equals(prevCommandString))
					{
						try
						{
							System.out.println("[DataWriterThread] Write: '" + commandBuffer.get(0) + "'");
							prevCommandString = commandBuffer.get(0);
							commandBuffer.remove(0);

							/*
							 * This is the first piece of code that throws an
							 * error (SocketException) when the program is shut
							 * down in the robot.
							 */
							out.writeUTF(prevCommandString);
							out.flush();

							try
							{
								Thread.sleep(Controller.SLEEP_TIME);
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
						catch (SocketException e)
						{
							System.err.println("[DataWriterThread] Robot program closed, disconnecting.");
							controller.terminateConnection();
						}
						catch (IOException e)
						{
							System.err.println("[DataWriterThread] Error writing data.");
							e.printStackTrace();
						}
					}
					else
					{
						commandBuffer.remove(0);
					}
				}
			}
		}
	}

	/**
	 * Begin the data sending loop.
	 */
	public void startWriting()
	{
		write = true;
		System.out.println("[DataWriterThread] STARTING");
	}

	/**
	 * Stop the data sending loop.
	 */
	public void stopWriting()
	{
		write = false;
		System.out.println("[DataWriterThread] STOPPING");
	}

	/**
	 * Send a command to start turning the cannon left. The cannon will continue
	 * to turn until stopped.
	 */
	public void turnLeft()
	{
		commandBuffer.add("l");
	}

	/**
	 * Send a command to start turning the cannon right. The cannon will
	 * continue to turn until stopped.
	 */
	public void turnRight()
	{
		commandBuffer.add("r");
	}

	/**
	 * Send a command to stop turning the cannon.
	 */
	public void stopTurning()
	{
		commandBuffer.add("s");
	}

	/**
	 * Send a command to fire a high shot with the cannon and stops the cannon
	 * rotation.
	 */
	public void shootCannonHigh()
	{
		commandBuffer.add("h");
	}

	/**
	 * Send a command to fire a low shot with the cannon and stops the cannon
	 * rotation.
	 */
	public void shootCannonLow()
	{
		commandBuffer.add("w");
	}

	/**
	 * Send the number of seconds left to the robot for sound effects.
	 *
	 * @param _timeLeft
	 *            Number of seconds left.
	 */
	public void timeLeft(final int _timeLeft)
	{
		commandBuffer.add(String.valueOf(_timeLeft));
	}

	/**
	 * Commands the robot to rotate to the specified angle.
	 *
	 * @param _angle
	 *            Angle to turn to.
	 */
	public void turnToAngle(final int _angle)
	{
		commandBuffer.add("a" + _angle);
	}

	/**
	 * Clear data writing buffer.
	 */
	public void clearBuffer()
	{
		commandBuffer.clear();
		prevCommandString = "!";
	}

	/**
	 * Send the maximum infrared distance value at which a cannonball may be
	 * detected.
	 *
	 * @param _distance
	 *            Infrared distance value.
	 */
	public void sendMaxIRDistance(final int _distance)
	{
		commandBuffer.add("i" + _distance);
	}

	public void sendMaxIRDistance(final double _redValue)
	{
		commandBuffer.add("c" + _redValue);
	}

	public void sendClearBufferCommand()
	{
		commandBuffer.add("z");
	}
}
