package robot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.BrickFinder;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.remote.ev3.RemoteRequestMenu;
import lejos.robotics.RegulatedMotor;

public class Cannonbot
{
	/**
	 * IP address of the robot.
	 */
	public static final String EV3_IP = "10.0.1.1";
	
	/**
	 * The port used to communicate with the robot.
	 */
	public static final int EV3_PORT = 1111;

	/**
	 * Creates the object used to control the turning motor.
	 */
	final static RegulatedMotor turningMotor = new EV3LargeRegulatedMotor(BrickFinder.getDefault().getPort("C"));
	
	/**
	 * Creates the object used to control the shooting motor.
	 */
	final static RegulatedMotor shootingMotor = new EV3MediumRegulatedMotor(BrickFinder.getDefault().getPort("D"));

	/**
	 * Assingns a port to the infrared sensor.
	 */
	private static Port PORT_INFRARED_SENSOR = SensorPort.S4;

	/**
	 * Creates a serversocket in the robot.
	 */
	private static ServerSocket server;
	
	/**
	 * Creates a socket in the robot.
	 */
	private static Socket socket;
	
	/**
	 * Creates an object used to transfer data from the robot.
	 */
	private static DataOutputStream out;
	
	/**
	 * Creates an object used to transfer data to the robot.
	 */
	private static DataInputStream in;
	
	/**
	 * Creates an object used to remotely access the EV3 brick's commands.
	 */
	private static RemoteRequestMenu remoteRequestMenu;

	/**
	 * Sets motors to default positions and stops all data transfer.
	 */
	public void stopProgram()
	{
		try
		{
			turningMotor.rotateTo(0);
			shootingMotor.rotateTo(0);
			in.close();
			out.close();
			server.close();
		}
		catch (IOException e)
		{
			System.out.println("Error closing server and output stream.");
			e.printStackTrace();
		}

		Sound.playTone(1000, 250);
		remoteRequestMenu.stopProgram();
	}

	public static void main(String[] args)
	{
		System.out.println("Opening socket.");
		try
		{
			server = new ServerSocket(1111);
			System.out.println("Waiting for socket...");
			socket = server.accept();
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			remoteRequestMenu = new RemoteRequestMenu(EV3_IP);
		}
		catch (IOException e)
		{
			System.out.println("Error establishing remote connection!");
			e.printStackTrace();
		}
		System.out.println("Data streams open.");
		shootingMotor.setSpeed(500);
		turningMotor.setSpeed(60);

		//InfraredSensor infraredSensor = new InfraredSensor(PORT_INFRARED_SENSOR);
		ColorSensor colorSensor = new ColorSensor(PORT_INFRARED_SENSOR);

		//DataWriterThread dataOutput = new DataWriterThread(out, turningMotor, infraredSensor);
		DataWriterThread dataOutput = new DataWriterThread(out, turningMotor, colorSensor);
		DataReaderThread dataInput = new DataReaderThread(in, turningMotor, shootingMotor, dataOutput);

		//infraredSensor.setDaemon(true);
		colorSensor.setDaemon(true);
		dataOutput.setDaemon(true);

		//infraredSensor.start();
		colorSensor.start();

		dataOutput.start();
		dataInput.start();
		System.out.println("Sending and receiving data.");
	}
}