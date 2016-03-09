package robot;

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * @author Jose Uusitalo
 */
public class ColorSensor extends Thread
{
	public static double MINIMUM_RED_VALUE;
	private EV3ColorSensor colorSensor;
	private SampleProvider colorSampleProvider;
	private float[] colorSample;
	private float[] prevColor;

	public ColorSensor(final Port PORT_COLOR_SENSOR)
	{
		colorSensor = new EV3ColorSensor(PORT_COLOR_SENSOR);
		colorSampleProvider = colorSensor.getRedMode();
		colorSample = new float[colorSampleProvider.sampleSize()];
		prevColor = new float[] { Float.MIN_VALUE };
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
			colorSampleProvider.fetchSample(colorSample, 0);

			if (Float.compare(colorSample[0], prevColor[0]) == 0)
			{
				prevColor = colorSample;
			}

		}
	}

	public double getColor()
	{
		// Some precision is lost in the implicit cast to double.
		return colorSample[0];
	}
}
