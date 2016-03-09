package game.model;

import game.controller.Controller;

/**
 * This represents a player who is playing the game. This class is also used to
 * store data about players who have played the game in the past.
 *
 * @author Jose Uusitalo &amp; Ilkka Varjokunnas
 */
public class Player
{
	/**
	 * The name of the player.
	 */
	private String name;

	/**
	 * The default name of the player.
	 */
	private final static String defaultName = "Pelaaja";

	/**
	 * This player's score value.
	 */
	private double score;

	/**
	 * MVC controller.
	 */
	private Controller controller;

	public Player(final Controller _controller)
	{
		name = defaultName;
		score = 0;
		controller = _controller;
	}

	public Player(final String _name, final Controller _controller)
	{
		name = _name;
		score = 0;
		controller = _controller;
	}

	/**
	 * @return the name of this player
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return this player's current score
	 */
	public double getScore()
	{
		return score;
	}

	/**
	 * Modify this player's score. Use negative values to subtract from the
	 * score. Updates the score text in the user interface.
	 *
	 * @param _score
	 *            Number of points to add or remove from this player's score.
	 */
	public void modScore(double _score)
	{
		score += _score;
		controller.updateScore(score);
	}

	/**
	 * @return the default player name
	 */
	public static String getDefaultName()
	{
		return defaultName;
	}

	/**
	 * Assign the player name.
	 *
	 * @param _name
	 *            New player name String.
	 */
	public void setName(String _name)
	{
		name = _name;
	}

	/**
	 * Attempt to score the infrared distance value received from the robot.
	 *
	 * @param _distance
	 *            Distance value received.
	 */
	public void scoreIR(final float _distance)
	{
		System.out.println("ir " + _distance);
		modScore(ScoreMachine.scoreDistance(_distance));
	}

	public void scoreColor(final String _redValue)
	{
		if (!controller.isGameOver())
			modScore(ScoreMachine.scoreColor(Double.parseDouble(_redValue)));
	}
}
