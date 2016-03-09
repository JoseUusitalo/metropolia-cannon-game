package game.controller;

import game.model.EV3Robot;

/**
 * Autonomously controls the robot and fires the cannon perfectly at the at
 * target, in theory scoring maximum possible points.
 *
 * @author Jose Uusitalo &amp; Ilkka Varjokunnas
 */
public class DemoMode extends Thread
{
	/**
	 * The angle the cannon should turn to in order to hit the target.
	 */
	private final int targetAngle;
	private final int targetMinAngle;
	private final int targetMaxAngle;

	/**
	 * MVC controller.
	 */
	private Controller controller;

	/**
	 * The robot being controlled.
	 */
	private EV3Robot robot;

	/**
	 * Whether or not to continue running the demo mode.
	 */
	private boolean run;

	public DemoMode(Controller _controller, EV3Robot _robot)
	{
		controller = _controller;
		robot = _robot;
		targetAngle = 8;
		targetMaxAngle = targetAngle + 1;
		targetMinAngle = targetAngle - 1;
	}

	/**
	 * The demo mode logic.
	 */
	public void startDemo()
	{
		controller.setPlayerNameTextField("DEMO");
		controller.startGame();
		startRun();
	}

	@Override
	public void run()
	{
		while (run)
		{
			if (controller.isGameOver())
			{
				System.out.println("STOP!");
				stopRun();
			}
			else
			{
				System.out.println("TURNING");
				robot.turnToAngle(targetAngle);

				if (robot.isRobotControlEnabled())
				{
					if (robot.getCurrentAngle() >= targetMinAngle && robot.getCurrentAngle() <= targetMaxAngle)
					{
						System.out.println("FIRE");
						robot.shootCannonHigh();
					}
					else
					{
						System.out.println("ALMOST");
						robot.turnToAngle(targetAngle);
					}
				}

				try
				{
					Thread.sleep(100l);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Begin running the demo mode.
	 */
	private void startRun()
	{
		run = true;
	}

	/**
	 * Stop running the demo mode.
	 */
	public void stopRun()
	{
		run = false;
	}
}
