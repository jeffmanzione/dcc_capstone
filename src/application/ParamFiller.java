package application;

import world.World;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ParamFiller extends Application{

	private ToggleGroup toggle;

	private RadioButton graphic, text, both;

	private Button go;

	private Label err;

	public void start(final Stage primaryStage) throws Exception {
		GridPane pane = new GridPane();
		pane.setAlignment(Pos.TOP_LEFT);
		pane.setHgap(40);
		pane.setVgap(20);
		pane.setPadding(new Insets(20, 20, 20, 20));

		toggle = new ToggleGroup();
		graphic = new RadioButton("Graphical");
		text = new RadioButton("Text");
		both = new RadioButton("Both");
		graphic.setToggleGroup(toggle);
		text.setToggleGroup(toggle);
		both.setToggleGroup(toggle);
		graphic.setSelected(true);
		Label topLabel = new Label("Run Type:");
		topLabel.getStyleClass().add("labels");
		pane.add(topLabel, 0, 0);
		pane.add(graphic, 1, 0);
		pane.add(text, 2, 0);
		pane.add(both, 3, 0);

		Label size = new Label("Size:");
		size.getStyleClass().add("labels");
		TextField sizeField = new TextField("300");
		sizeField.setMaxWidth(80);
		pane.add(size, 0, 1);
		pane.add(sizeField, 1, 1);

		Label fill = new Label("Fill:");
		fill.getStyleClass().add("labels");
		TextField fillField = new TextField("0.05");
		fillField.setMaxWidth(80);
		pane.add(fill, 0, 2);
		pane.add(fillField, 1, 2);
		
		go = new Button("Create");
		err = new Label("");
		err.getStyleClass().add("error");
		pane.add(go, 0, 3);
		pane.add(err, 1, 3);

		final Scene scene = new Scene(pane, 440, 200);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setResizable(false);
		primaryStage.setTitle("DCC Capstone");

		go.setOnAction(e -> {
			try {
				int siz = Integer.parseInt(sizeField.getText());
				try {
					double fil = Double.parseDouble(fillField.getText());
					primaryStage.setResizable(true);
					if (graphic.isSelected()) {
						Sim sim = new Sim(false, siz, fil);
						try {
							sim.start(primaryStage);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else if (text.isSelected()) {
						World world;
						try {
							world = new World(siz, fil);
							for (int i = 0; i <= 6000; i++) {
								world.step();
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} else {
						Sim sim = new Sim(true, siz, fil);
						try {
							sim.start(primaryStage);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				} catch (NumberFormatException ers) {
					err.setText("Fill is not a float!");
				}
			} catch (NumberFormatException er) {
				err.setText("Size is not an int!");
			}
		});
	}

}
