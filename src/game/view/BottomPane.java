package game.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A pane at the bottom of the user interface with a progress bar which
 * graphically displays the current cannon angle.
 *
 * @author Jose Uusitalo
 */
public class BottomPane
{
	/**
	 * The root pane for this section of the user interface.
	 */
	private VBox vbox;

	/**
	 * A progress bar that visually displays the angle of the robot's cannon.
	 */
	static ProgressBar progbarCannonAngle;

	public BottomPane()
	{
		vbox = null;
	}

	/**
	 * Creates the pane if it does not exist.
	 *
	 * @return the bottom pane
	 */
	public VBox getPane()
	{
		if (vbox == null)
		{
			vbox = new VBox();
			vbox.setPadding(new Insets(20, 0, 0, 0));

			Label angleLabel = new Label("Tykkitornin kulma");
			angleLabel.setMaxWidth(Double.MAX_VALUE);
			angleLabel.setAlignment(Pos.BASELINE_CENTER);

			// TODO: Stackpane overlay warning text on angle bar.
			progbarCannonAngle = new ProgressBar(0.5);
			progbarCannonAngle.setMaxWidth(Double.MAX_VALUE);
			progbarCannonAngle.getStyleClass().add("progbar-purple");
			progbarCannonAngle.setMinHeight(50);

			Label warning = new Label("Tykkitorni ei voi kääntyä enempää");
			warning.setMaxWidth(Double.MAX_VALUE);
			warning.setAlignment(Pos.BASELINE_CENTER);

			GridPane grid = new GridPane();
			Label angle90neg = new Label("-90°");
			angle90neg.setMaxWidth(Double.MAX_VALUE);
			angle90neg.setAlignment(Pos.BASELINE_RIGHT);

			Label angle0 = new Label("0°");
			angle0.setMaxWidth(Double.MAX_VALUE);
			angle0.setAlignment(Pos.BASELINE_CENTER);

			Label angle90 = new Label("90°");
			angle90.setMaxWidth(Double.MAX_VALUE);

			grid.add(angle90, 0, 0);
			grid.add(angle0, 1, 0);
			grid.add(angle90neg, 2, 0);

			ColumnConstraints middle = new ColumnConstraints(100, 100, Double.MAX_VALUE);
			middle.setHgrow(Priority.ALWAYS);
			ColumnConstraints sides = new ColumnConstraints(100);
			grid.getColumnConstraints().addAll(sides, middle, sides);

			vbox.getChildren().addAll(angleLabel, progbarCannonAngle, grid);
		}
		return vbox;
	}
}
