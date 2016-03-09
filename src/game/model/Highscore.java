package game.model;

import java.io.Serializable;

/**
 * A stripped down read-only version of {@link Player} for the purposes of
 * storing highscores and reducing the file size of the highscore database file.
 * 
 * @author Jose Uusitalo
 */
public class Highscore implements Serializable
{
	/**
	 * I have no idea what this is.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Player name.
	 */
	private String name;

	/**
	 * Player score.
	 */
	private double score;

	public Highscore(final String _name, final double _score)
	{
		name = _name;
		score = _score;
	}

	/**
	 * @return the player name of this highscore
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the score of this highscore
	 */
	public double getScore()
	{
		return score;
	}
}
