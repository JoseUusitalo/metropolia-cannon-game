package game.model;

/**
 * A dummy model for displaying the highscores in the user interface highscore
 * list.
 *
 * @author Jose Uusitalo
 */
public class HighscoreListRow
{
	/**
	 * The integer signifying the position of the highscore in the list.
	 */
	private int position;

	/**
	 * Name of the player who made the {@link Highscore}.
	 */
	private String name;

	/**
	 * The number of points the player scored.
	 */
	private double score;

	public HighscoreListRow(final int _position, final String _name, double _score)
	{
		position = _position;
		name = _name;
		score = _score;
	}

	/**
	 * @return the position of this highscore in the list in the range [1,
	 *         {@link model.HighscoreList#HIGHSCORE_LIST_MAX_SIZE}]
	 */
	public int getPosition()
	{
		return position;
	}

	/**
	 * @return the player name who made this highscore
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
