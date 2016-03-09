package game.model;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import game.controller.Controller;

/**
 * A List of finite size of highscores sorted by their scores.
 *
 * @see Highscore
 * @author Jose Uusitalo
 */
public class HighscoreList
{
	/**
	 * The List of highscores containing the name of the player and their score.
	 */
	private List<Highscore> list;

	/**
	 * The maximum number of rows the highscore list in the user interface is
	 * capable of showing.
	 *
	 * @see HighscoreListRow
	 * @see view.RightPane
	 */
	public static final int HIGHSCORE_LIST_MAX_SIZE = 20;

	/**
	 * The name of the highscore file.
	 */
	private final String FILENAME = "highscores.data";

	public HighscoreList()
	{
		list = new ArrayList<Highscore>();
	}

	/**
	 * @return the List of highscores
	 * @see Highscore
	 */
	public List<Highscore> getList()
	{
		return list;
	}

	/**
	 * Add a player to the list of highscores if there is space on the list and
	 * their score is greater or equal to the current lowest score. If the score
	 * was added to the list, the list is written to a file for storage.
	 *
	 * @param _player
	 *            The Player whose score is to be added to the highscore list.
	 * @return <code>true</code> if the player's score was added to the list,
	 *         <code>false</code> otherwise
	 * @see HighscoreList#writeToFile
	 */
	public boolean addScore(final Player _player)
	{
		/*
		 * The size should not be greater than max size but it never hurts to be
		 * prepared to handle all situations.
		 */
		if (list.size() >= HIGHSCORE_LIST_MAX_SIZE)
		{
			// List full.
			double score = _player.getScore();

			if (score >= getLowestScore())
			{
				int id = firstScoreIndex(score);
				if (id >= 0)
				{
					list.add(id, new Highscore(_player.getName(), score));
				}
				else
				{
					/*
					 * Score amount does not exist yet, which means the score is
					 * greater than the lowest score. Add new highscore to the
					 * top of the list. The list is later trimmed to maximum
					 * size and ordered by score in that order, which means the
					 * last lowest score will be removed.
					 */
					list.add(0, new Highscore(_player.getName(), score));
				}

				System.out.println("[HighscoreList] Trimming list maximum size.");
				if (list.size() > HIGHSCORE_LIST_MAX_SIZE)
					list = list.subList(0, HIGHSCORE_LIST_MAX_SIZE);
			}
			else
			{
				System.out.println("[HighscoreList] Not enought points for a high score.");
				return false;
			}
		}
		else
		{
			System.out.println("[HighscoreList] List not yet full, adding score.");
			list.add(new Highscore(_player.getName(), _player.getScore()));
		}

		writeToFile();
		return true;
	}

	/**
	 * Does a linear search of the highscore list looking for the specified
	 * score.
	 *
	 * @param _score
	 *            Score amount to search for.
	 * @return the first index of the score in the list if found,
	 *         <code>-1</code> if score does not exist in the list
	 */
	private int firstScoreIndex(final double _score)
	{
		double listScore;
		for (int i = 0; i < list.size() - 1; i++)
		{
			listScore = list.get(i).getScore();
			// Found what we're looking for.
			if (listScore == _score)
			{
				return i;
			}
			else if (listScore < _score)
			{
				/*
				 * Because the list is in an descending order we already went
				 * past what we were looking for, thus the score is not in the
				 * list.
				 */
				return -1;
			}
		}
		return -1;
	}

	/**
	 * @return the current lowest highscore
	 */
	private double getLowestScore()
	{
		return list.get(list.size() - 1).getScore();
	}

	/**
	 * Reads the list of highscores from the binary file "highscores" in the
	 * same folder as this program and assigns it into a List for later use.
	 */
	public void readFromFile()
	{
		FileInputStream fileInput = null;
		ObjectInputStream objectInput = null;
		Object o;
		List<Highscore> old = list;

		try
		{
			fileInput = new FileInputStream(FILENAME);
			objectInput = new ObjectInputStream(fileInput);
			list.clear();

			while ((o = objectInput.readObject()) != null)
			{
				if (o instanceof Highscore)
					list.add((Highscore) o);
			}
		}
		catch (EOFException e)
		{
			System.out.println("[HighscoreList] Highscores read from file.");
		}
		catch (FileNotFoundException e)
		{
			System.out.println("[HighscoreList] No highscore file found.");
		}
		catch (Exception e)
		{
			// Rolling back just in case.
			list = old;
			System.out.println(e);
		}
		finally
		{
			if (objectInput != null)
			{
				try
				{
					objectInput.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Writes the current list of highscores into a binary file for storing the
	 * data across program launches.
	 */
	private void writeToFile()
	{
		FileOutputStream fileOutput = null;
		ObjectOutputStream objectOutput = null;

		System.out.println("[HighscoreList] Sorting list.");
		Collections.sort(list, new Comparator<Highscore>()
		{
			@Override
			public int compare(final Highscore first, final Highscore second)
			{
				/*
				 * Sort the highscore list into descending order by
				 * score.
				 */
				return Double.compare(second.getScore(), first.getScore());
			}
		});

		System.out.println("[HighscoreList] Done sorting.");

		try
		{
			fileOutput = new FileOutputStream(FILENAME);
			objectOutput = new ObjectOutputStream(fileOutput);
			for (Highscore h : list)
			{
				objectOutput.writeObject(h);
			}
		}
		catch (Exception e)
		{
			System.err.println(e);
		}
		finally
		{
			if (objectOutput != null)
			{
				try
				{
					objectOutput.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		System.out.println("[HighscoreList] Highscores written to the file.");
	}

	/**
	 * <p>
	 * <b>FOR DEBUG USE ONLY.</b>
	 * </p>
	 * <p>
	 * Delete the highscore file and remove all entries from the current
	 * highscore list. Will not do anything if debug mode is not enabled.
	 * </p>
	 */
	public void debugClearHighscores()
	{
		if (Controller.DEBUG)
		{
			list.clear();
			File file = new File(FILENAME);

			if (!file.exists())
				throw new IllegalArgumentException("Can't find the highscore file!");

			if (!file.canWrite())
				throw new IllegalArgumentException("Can't write to the highscore file!");

			if (!file.delete())
				throw new IllegalArgumentException("Deleting highscore file failed!");
		}
	}

	public void debugRemoveHighscoreByIndex(final int _index)
	{
		readFromFile();
		list.remove(_index);
		writeToFile();
	}

	public void debugPrintHighscores()
	{
		readFromFile();
		for (int i = 0; i < list.size(); i++)
		{
			System.out.println(i + ": " + list.get(i).getName() + " " + list.get(i).getScore());
		}
	}
}
