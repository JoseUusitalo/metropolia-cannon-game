package game.view;

import game.model.HighscoreListRow;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.ResizeFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

/**
 * The right panel of the user interface. Contains the highscore list.
 *
 * @author Jose Uusitalo
 */
public class RightPane
{
	/**
	 * The container for the highscore list.
	 */
	private BorderPane bpane;

	/**
	 * A TableView displaying the highscores.
	 */
	static TableView<HighscoreListRow> highscores;

	public RightPane()
	{
		bpane = null;
	}

	/**
	 * Creates the pane if it does not exist.
	 *
	 * @return the border pane with the highscore list
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BorderPane getPane()
	{
		if (bpane == null)
		{
			int pxHeight = 612;
			bpane = new BorderPane();
			bpane.setPadding(new Insets(0, 0, 0, 40));

			Label highscoresTitle = new Label("Highscores");
			highscoresTitle.getStyleClass().add("highscores-title");
			highscoresTitle.setMaxWidth(Double.MAX_VALUE);

			highscores = new TableView<HighscoreListRow>()
			{
				@Override
				public void requestFocus()
				{
					// Disable focus traversal.
				}
			};

			highscores.setEditable(false);
			highscores.setMinHeight(pxHeight);
			highscores.setMaxHeight(pxHeight);

			// Disable column resizing.
			highscores.setColumnResizePolicy(new Callback<TableView.ResizeFeatures, Boolean>()
			{
				@Override
				public Boolean call(ResizeFeatures p)
				{
					return true;
				}
			});

			TableColumn<HighscoreListRow, Integer> colPosition = new TableColumn<HighscoreListRow, Integer>("Pos.");
			colPosition.setCellValueFactory(new PropertyValueFactory<HighscoreListRow, Integer>("position"));
			colPosition.prefWidthProperty().bind(highscores.widthProperty().multiply(0.15));

			TableColumn<HighscoreListRow, String> colName = new TableColumn<HighscoreListRow, String>("Name");
			colName.setCellValueFactory(new PropertyValueFactory<HighscoreListRow, String>("name"));
			colName.prefWidthProperty().bind(highscores.widthProperty().multiply(0.55));

			TableColumn<HighscoreListRow, Double> colScore = new TableColumn<HighscoreListRow, Double>("Score");
			colScore.setCellValueFactory(new PropertyValueFactory<HighscoreListRow, Double>("score"));

			// If the total is 1.0 you get a useless scroll bar.
			colScore.prefWidthProperty().bind(highscores.widthProperty().multiply(0.29));

			highscores.getColumns().addAll(colPosition, colName, colScore);

			bpane.setTop(highscoresTitle);
			bpane.setRight(highscores);
		}
		return bpane;
	}

}
