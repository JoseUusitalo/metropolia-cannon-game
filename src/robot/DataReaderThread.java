package robot;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;

import lejos.hardware.Sound;
import lejos.robotics.RegulatedMotor;

/**
 * @author Ilkka Varjokunnas &amp; Jose Uusitalo
 */
public class DataReaderThread extends Thread
{
	/**
	 * Creates an object to transfer data to the robot.
	 */
	private DataInputStream in;
	/**
	 * Creates a regulated motor object to command the turning motor.
	 */
	private RegulatedMotor turningMotor;
	/**
	 * Creates a regulated motor object to command the shooting motor.
	 */
	private RegulatedMotor shootingMotor;
	/**
	 * Allows the run-method to function.
	 */
	private boolean run;
	/**
	 * Creates an object to transfer data from the robot.
	 */
	private DataWriterThread dataWriter;

	/**
	 * Constructor for the class.
	 *
	 * @param _in
	 * @param _turningMotor
	 * @param _shootingMotor
	 * @param _dataOutput
	 */
	public DataReaderThread(final DataInputStream _in, final RegulatedMotor _turningMotor,
			final RegulatedMotor _shootingMotor, DataWriterThread _dataOutput)
	{
		in = _in;
		turningMotor = _turningMotor;
		shootingMotor = _shootingMotor;
		run = true;
		dataWriter = _dataOutput;
	}

	public void run()
	{
		while (run)
		{
			try
			{
				parseCommand(in.readUTF());

				Thread.sleep(50l);
			}
			catch (SocketException e)
			{
				// Computer program shut down.
			}
			catch (EOFException e)
			{
				// A game is not running on the computer.
			}
			catch (IOException e)
			{
				System.out.println("Error reading data.");
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void parseCommand(String input)
	{
		System.out.println("Read '" + input + "'");
		char commandChar = input.charAt(0);
		switch (commandChar)
		{
			case 's':
				System.out.println("Stop");
				turningMotor.stop();
				break;
			case 'l':
				System.out.println("Left");
				turningMotor.forward();
				break;
			case 'r':
				System.out.println("Right");
				turningMotor.backward();
				break;
			case 'w':
				System.out.println("Fire low!");
				turningMotor.stop(true);
				shootingMotor.rotate(1080);
				dataWriter.finishedShooting();
				turningMotor.stop();
				break;
			case 'h':
				System.out.println("Fire high!");
				turningMotor.stop(true);
				shootingMotor.rotate(-1080);
				dataWriter.finishedShooting();
				turningMotor.stop();
				break;
			case 'a':
				turningMotor.rotateTo(Integer.parseInt(input.substring(1)));
				turningMotor.stop();
				dataWriter.finishedTurning();
				break;
			case '5':
				Sound.playTone(500, 100);
				break;
			case '4':
				Sound.playTone(600, 150);
				break;
			case '3':
				Sound.playTone(700, 200);
				break;
			case '2':
				Sound.playTone(800, 250);
				break;
			case '1':
				Sound.playTone(900, 300);
				break;
			case '0':
				Sound.playTone(1000, 350);
				break;
			case 'i':
				InfraredSensor.MAX_IR_DISTANCE = Integer.parseInt(input.substring(1));
				break;
			case 'c':
				ColorSensor.MINIMUM_RED_VALUE = Double.parseDouble(input.substring(1));
				System.out.println("MIN RED: " + ColorSensor.MINIMUM_RED_VALUE);
				break;
			case ' ':
				// No data
				break;
			case 'z':
				dataWriter.clearBuffer();
				break;
			default:
				System.out.println("Unknown command character...");
		}

	}
}
