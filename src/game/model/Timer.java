package game.model;

import java.util.Random;
import game.controller.Controller;

/**
 * The shot timer class counts the number of seconds left to aim and fire the
 * cannon.
 *
 * @see Controller#SHOT_TIMER
 * @author Ilkka Varjokunnas &amp; Jose Uusitalo
 */
public class Timer extends Thread
{
	/**
	 * Whether or not to run the timer.
	 */
	private boolean run;

	/**
	 * Time left in seconds.
	 */
	private int seconds;

	/**
	 * A random value generator.
	 */
	private Random rand;

	/**
	 * Robot being controlled.
	 */
	private EV3Robot robot;

	/**
	 * MVC controller.
	 */
	private Controller controller;

	/**
	 * @return the number of seconds left on this timer
	 */
	public int getSeconds()
	{
		return seconds;
	}

	public Timer(final EV3Robot _robot, final Controller _controller)
	{
		seconds = Controller.SHOT_TIMER;
		rand = new Random();
		robot = _robot;
		controller = _controller;

	}

	/**
	 * Starts the thread.
	 */
	@Override
	public void run()
	{
		double prosenttiarvo;
		while (run)
		{
			try
			{
				System.out.println("[Timer] Second: " + seconds);

				prosenttiarvo = (double) seconds / (double) Controller.SHOT_TIMER;
				controller.setTimeLeft(prosenttiarvo);
				System.out.println("[Timer] Percent: " + prosenttiarvo);
				Thread.sleep(1000l);
				seconds = seconds - 1;
				if (seconds == 0)
				{
					System.out.println(seconds);
					if (rand.nextBoolean())
					{
						System.out.println("[Timer] Fire high!");
						robot.shootCannonHigh();
					}
					else
					{
						System.out.println("[Timer] Fire low!");
						robot.shootCannonLow();
					}
				}
				else if (seconds < 0)
				{
					// Wait for shooting to complete before resetting timer.
					while (robot.getShootingInProgress())
					{
						Thread.sleep(10l);
					}
					seconds = Controller.SHOT_TIMER;
				}
				System.out.println("[Timer] Reloop.");
			}
			catch (InterruptedException e)
			{

				e.printStackTrace();
			}
		}

	}

	/**
	 * Begin the timer counting loop.
	 */
	public void startTimer()
	{
		run = true;
		System.out.println("[Timer] TIMER STARTING");
	}

	/**
	 * Stop the timer counting loop.
	 */
	public void stopTimer()
	{
		run = false;
		System.out.println("[Timer] TIMER STOPPING");
	}

	/**
	 * Resets the timer.
	 */
	public void resetTimer()
	{
		seconds = Controller.SHOT_TIMER;
		System.out.println("[Timer] TIMER RESET");
	}

}
