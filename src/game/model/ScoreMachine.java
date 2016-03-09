package game.model;

import game.controller.Controller;

/**
 * A class containing various methods and values used for scoring cannon shots.
 * Instatiation of this class is not necessary.
 *
 * @author Ilkka Varjokunnas &amp; Jose Uusitalo
 */
public class ScoreMachine
{
	/**
	 * The maximum infrared distance where a cannonball rolling down the channel
	 * should be detected.
	 */
	public final static int MAX_BALL_IR_DISTANCE = 15;

	public final static double MINIMUM_RED_VALUE = 0.0095;

	/**
	 * The score received when a canonball is detected in a channel. The index
	 * number of the score value is the number of channel - 1.
	 */
	private final static double BALL_SCORE = 10.0;

	/**
	 * Score a player's shot based in the number of seconds left in the timer.
	 *
	 * @param _timeLeft
	 *            Number of seconds left.
	 * @return score to be added to the player
	 */
	public static double scoreShot(final int _timeLeft)
	{
		double score = (double) (_timeLeft) / (double) (Controller.SHOT_TIMER) * BALL_SCORE;
		System.out.println("[ScoreMachine] TIME BONUS: + " + score);
		return score;
	}

	/**
	 * Determine if the given infrared distance value is a ball and score it
	 * accordingly.
	 *
	 * @param _distance
	 *            Infrared distance value received from the robot.
	 * @return number of points (if any) to be added to the player's score
	 */
	public static double scoreDistance(final float _distance)
	{
		if (1 <= _distance && _distance <= MAX_BALL_IR_DISTANCE)
		{
			System.out.println("[ScoreMachine] Ball detected!");
			return BALL_SCORE;
		}

		return 0;
	}

	public static double scoreColor(double _redValue)
	{
		System.out.println("red " + _redValue);
		if (_redValue >= MINIMUM_RED_VALUE)
		{
			System.out.println("[ScoreMachine] Ball detected!");
			return BALL_SCORE;
		}

		return 0;
	}
}
