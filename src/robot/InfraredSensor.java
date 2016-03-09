package robot;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;

/**
 * @author Jose Uusitalo
 */
public class InfraredSensor extends Thread
{
	/**
	 * The maximum distance at which a cannonball may be detected and distance
	 * data written to the computer.
	 */
	public static int MAX_IR_DISTANCE;

	/**
	 * Creates an infrared sensor object.
	 */
	private EV3IRSensor infraredSensor;

	/**
	 * Creates a sample provider object.
	 */
	private SampleProvider infraredDistanceProvider;

	/**
	 * Variable used to store distance received from SampleProvider.
	 */
	private float[] distanceSample;
	/**
	 * Distance used in the run method.
	 */
	private double distance;

	/**
	 * The previous distance is stored so the same distance is not sent more
	 * than once.
	 */
	private double prevDistance;

	/**
	 *
	 * @return distance to detected object.
	 */
	public double getDistance()
	{
		return distance;
	}

	/**
	 * Constructor for the class.
	 *
	 * @param PORT_INFRARED_SENSOR
	 *            port where infrared sensor is connected.
	 */
	public InfraredSensor(final Port PORT_INFRARED_SENSOR)
	{
		infraredSensor = new EV3IRSensor(PORT_INFRARED_SENSOR);
		infraredDistanceProvider = infraredSensor.getDistanceMode();
		distanceSample = new float[infraredDistanceProvider.sampleSize()];

		distance = 0.0;
		prevDistance = -1.0;
	}

	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(10l);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			infraredDistanceProvider.fetchSample(distanceSample, 0);
			distance = distanceSample[0];
			if (distance != prevDistance)
			{
				prevDistance = distance;
			}

		}
	}
}
