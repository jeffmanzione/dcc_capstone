package application;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {


	public void start(final Stage primaryStage) throws Exception {
		new ParamFiller().start(primaryStage);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
