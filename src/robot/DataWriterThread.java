package robot;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import lejos.robotics.RegulatedMotor;

/**
 * @author Ilkka Varjokunnas &amp; Jose Uusitalo
 */
public class DataWriterThread extends Thread
{
	/**
	 * Creates an object used to transfer data from the robot.
	 */
	private DataOutputStream out;
	/**
	 * Variable used to store infrared and angle data.
	 */
	private volatile List<String> data;
	/**
	 * Variable used to compare changes in infrared and angle data.
	 */
	private String prevData;
	/**
	 * Creates a regulated motor object to command the turning motor.
	 */
	private RegulatedMotor turningMotor;
	/**
	 * Allows the run-method to function.
	 */
	private boolean run;
	/**
	 * Creates an infrared sensor object.
	 */
	public InfraredSensor infraRed;

	public ColorSensor colorSensor;

	private String prevAngle;
	private double prevIR;
	private double prevColor;

	public DataWriterThread(final DataOutputStream _out, final RegulatedMotor _motor, final InfraredSensor _infraRed)
	{
		out = _out;
		turningMotor = _motor;
		run = true;
		data = new ArrayList<String>();
		infraRed = _infraRed;
		prevData = "!"; // Some unused string.
		prevAngle = "!";
		prevIR = Double.MAX_VALUE;
	}

	public DataWriterThread(final DataOutputStream _out, final RegulatedMotor _motor, final ColorSensor _colorSensor)
	{
		out = _out;
		turningMotor = _motor;
		run = true;
		data = new ArrayList<String>();
		colorSensor = _colorSensor;
		prevData = "!"; // Some unused string.
		prevAngle = "!";
		prevIR = Double.MAX_VALUE;
	}

	/**
	 * Checks if the run-method is looping.
	 */
	public boolean isRunning()
	{
		return run;
	}

	/**
	 * Prevents the run-method being used.
	 */
	public void stopRunning()
	{
		System.out.println("WRITER STOPPED");
		run = false;
	}

	/**
	 * Calls writeAngle continuously.
	 */
	public void run()
	{
		while (run)
		{
			writeAngle(String.valueOf(turningMotor.getTachoCount()));
			// writeIR(infraRed.getDistance());
			writeColor(colorSensor.getColor());

			if (data.size() > 0)
			{
				if (!data.get(0).equals(prevData))
				{
					try
					{
						prevData = data.get(0);
						data.remove(0);

						// if (prevData.charAt(0) != 'i')
						System.out.println("Write '" + prevData + "'");

						out.writeUTF(prevData);
						out.flush();

					}
					catch (SocketException e)
					{
						System.out.println("Error, program closed.");
						stopRunning();
					}
					catch (IOException e)
					{
						System.out.println("Error outputting motor angle data.");
						e.printStackTrace();
					}
				}
				else
				{
					data.remove(0);
				}

				try
				{
					Thread.sleep(50l);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Prints angle when it changes.
	 */
	public void writeAngle(final String _angle)
	{

		// Don't write the same angle many times.
		if (!_angle.equals(prevAngle))
		{
			prevAngle = _angle;
			data.add("a" + prevAngle);
		}
	}

	/**
	 * Sends the infrared data to the computer when it changes.
	 */
	public void writeIR(final double _value)
	{
		// Don't write same IR data continuously.
		if (Double.compare(_value, prevIR) != 0)
		{
			// Also do not write the same detected cannonball more than once.
			if (Double.compare(_value, InfraredSensor.MAX_IR_DISTANCE) >= 0
					|| Double.compare(prevIR, InfraredSensor.MAX_IR_DISTANCE) >= 0)
			{
				prevIR = _value;
				data.add("i" + prevIR);
			}
		}
	}

	public void writeColor(final double _value)
	{
		// Don't write same color data continuously.
		if (Double.compare(_value, prevColor) != 0)
		{
			// Also do not write the same detected cannonball more than once.
			if (!(Double.compare(_value, ColorSensor.MINIMUM_RED_VALUE) >= 0)
					|| !(Double.compare(prevColor, ColorSensor.MINIMUM_RED_VALUE) >= 0))
			{
				prevColor = _value;
				data.add("c" + prevColor);
			}
		}
	}

	/**
	 * Notifies PC when robot has taken a shot.
	 */
	public void finishedShooting()
	{
		data.add("f");
	}

	/**
	 * Notifies PC when robot has finished turning.
	 */
	public void finishedTurning()
	{
		data.add("t");
	}

	public void clearBuffer()
	{
		System.out.println("Buffer cleared");
		data.clear();
		prevData = "!"; // Some unused string.
		prevAngle = "!";
	}
}
