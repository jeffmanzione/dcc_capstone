package application;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.sun.javafx.geom.Point2D;

import world.Cell;
import world.CellAlreadyHasAgentException;
import world.World;
import world.agents.Agent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Sim extends Application {

	public int X_DIM = 800;
	public int Y_DIM = 800;

	private Canvas bg, fg;

	private World world;

	private boolean cont = false;

	private Button next, step, hist, tree, restart;
	private ToggleButton explain;
	private TextField ratio;
	private Button reset;

	private Label details;

	private ComboBox<String> speed;

	private Stage stage;

	private int SLEEP_LENGTH = 250;

	private int size;

	private boolean shouldData;

	private Agent selected = null;

	private double fill;

	public Sim(boolean shouldData, int size, double fill) {
		this.shouldData = shouldData;
		this.size = size;
		this.fill = fill;
	}

	public Sim(int size, double fill) {
		this.shouldData = true;
		this.size = size;
		this.fill = fill;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.stage = primaryStage;

		stage.setTitle("Jeff's Evolutionary MAS");

		final Group root = new Group();

		bg = new Canvas(X_DIM, Y_DIM);
		fg = new Canvas(X_DIM, Y_DIM);

		next = new Button("Start");
		next.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				toggleTime();
			}
		});

		step = new Button("Step");

		step.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				try {
					world.step();
				} catch (CellAlreadyHasAgentException e1) {
					e1.printStackTrace();
				}
			}
		});

		speed = new ComboBox<String>();
		speed.getItems().addAll("Very Slow", "Slow", "Normal", "Fast", "Very Fast");
		speed.getSelectionModel().select("Normal");

		speed.valueProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> val, String was, String changedTo) {                
				switch (changedTo) {
				case "Very Slow":
					SLEEP_LENGTH = 400;
					break;
				case "Slow":
					SLEEP_LENGTH = 250;
					break;
				case "Fast":
					SLEEP_LENGTH = 75;
					break;
				case "Very Fast":
					SLEEP_LENGTH = 1;
					break;
				case "Normal":
				default:
					SLEEP_LENGTH = 100;
				}
			}    
		});

		hist = new Button("History");
		hist.setOnAction(e -> {
			if (selected != null) System.out.println(selected.getHistory());
		});

		tree = new Button("Tree");
		tree.setOnAction(e -> world.getTree());

		ratio = new TextField();
		ratio.setText("32");

		ratio.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				//reset();
				fg.requestFocus();
			}
		});

		reset = new Button("Capture");
		reset.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				// Get a DOMImplementation.
				DOMImplementation domImpl =
						GenericDOMImplementation.getDOMImplementation();

				// Create an instance of org.w3c.dom.Document.
				String svgNS = "http://www.w3.org/2000/svg";
				Document document = domImpl.createDocument(svgNS, "svg", null);
				// Create an instance of the SVG Generator.
				SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

				// Ask the test to render into the SVG Graphics2D implementation.
				world.print(svgGenerator, Double.valueOf(ratio.getText()));

				// Finally, stream out SVG to the standard output using
				// UTF-8 encoding.
				boolean useCSS = true; // we want to use CSS style attributes
				Writer out;
				try {
					File file = new File("test.svg");
					out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
					svgGenerator.stream(out, useCSS);
					out.close();

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//System.out.println("Done.");
			}
		});

		details = new Label();
		details.setVisible(false);
		details.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);-fx-text-fill: white;-fx-font: 16px \"Courier New\";-fx-padding: 4;");

		world = new World(bg, fg, size, shouldData, fill);
		//world = new World(bg, fg, size, shouldData, new File("map.png"), fill);

		root.getChildren().add(bg);
		root.getChildren().add(fg);


		explain = new ToggleButton("Help");

		restart = new Button("Restart");
		
		restart.setOnAction(e -> {
			try {
				new ParamFiller().start(stage);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		
		List<Control> butts = Arrays.asList(explain, step, next, speed, restart /*, hist, tree,  reset*/);
		butts.forEach(b -> {
			root.getChildren().add(b);
			b.setMinWidth(140);
			b.setMaxWidth(140);
			b.setMinHeight(b == reset ? 96 : 140);
			b.setLayoutX(20);
			b.setLayoutY(i);
			i += 160;
			b.getStyleClass().add("buttons");
			b.setVisible(false);
		} );
		
		ImageView imgView = new ImageView(new Image(Main.class.getResourceAsStream("/help.png")));
		StackPane spImgV = new StackPane(imgView);
		spImgV.setLayoutX(20 + 140);
		spImgV.setLayoutY(20);
		spImgV.getStyleClass().add("imgview");
		root.getChildren().add(spImgV);
		
		spImgV.setVisible(false);
		
		hist.setDisable(true);
		tree.setDisable(true);
		reset.setDisable(true);
		ratio.setDisable(true);

		
		explain.selectedProperty().addListener(e -> {
			System.out.println(true);
			if (spImgV.isVisible()) {
				spImgV.setVisible(false);
			} else {
				spImgV.setVisible(true);
			}
		});

		/*root.getChildren().add(ratio);
		ratio.setMaxWidth(120);
		ratio.setMinWidth(120);
		ratio.setMaxHeight(24);
		ratio.setMinHeight(24);
		ratio.setLayoutX(20);
		ratio.setLayoutY(i-44);
		 */

		root.getChildren().add(details);
		details.setMinWidth(232);
		details.setMaxWidth(232);
		details.setMinHeight(170);
		details.setMaxHeight(170);
		details.setLayoutX(20);
		details.setLayoutY(Y_DIM - 190);

		next.toFront();

		final Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.show();


		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent t) {
				Platform.exit();
				System.exit(0);
			}

		});

		scene.setOnKeyTyped(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {

				//System.out.println(">" + e.getCharacter() + "< " + e.isAltDown());

				char ch = e.getCharacter().charAt(0);

				if (ch == 'w') {
					world.incYOff();
				} else if (ch == 's') {
					world.decYOff();
				} else if (ch == 'a') {
					world.incXOff();
				} else if (ch == 'd') {
					world.decXOff();
				} else if (ch == 'e') {
					primaryStage.setFullScreen(!primaryStage.isFullScreen());
				}

			}
		});

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {

				//System.out.println(">" + e.getCharacter() + "<");

				char ch = e.getCharacter().charAt(0);

				if (ch == ' ') {
					toggleTime();
				}
			}
		});


		Task<Void> task = new Task<Void>() {
			public Void call() throws Exception {

				while (true) {

					try {

						lock.lock();

						while (!cont)
							cond.await();

						Thread thread = new Thread() {
							public void run() {
								try {
									lock.lock();

									if (cont)
										world.step();

									lock.unlock();

								} catch (CellAlreadyHasAgentException e) {
									e.printStackTrace();
								}
							}
						};

						thread.join();

						Platform.runLater(thread);

						lock.unlock();

						Thread.sleep(SLEEP_LENGTH);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		};

		Thread thread = new Thread(task);
		thread.start();


		repaint();

		ChangeListener<Number> resizeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> num, Number old, Number newNum) {
				//System.out.println(scene.getWidth() + " " + scene.getHeight());

				root.getChildren().remove(bg);
				root.getChildren().remove(fg);

				bg = new Canvas(scene.getWidth(), scene.getHeight());
				fg = new Canvas(scene.getWidth(), scene.getHeight());


				root.getChildren().add(bg);
				root.getChildren().add(fg);
				fg.toBack();
				bg.toBack();

				world.setCanvases(bg, fg);


				//ratio.setLayoutX(scene.getWidth() - 156);
				//ratio.setLayoutY(2);

				//reset.setLayoutX(scene.getWidth() - 104);

				details.setLayoutY(scene.getHeight() - 190);

			}
		};

		scene.widthProperty().addListener(resizeListener);
		scene.heightProperty().addListener(resizeListener);

		root.setOnMouseMoved(e -> {
			if (e.getX() < 160) {
				butts.forEach(b -> b.setVisible(true));
				if (explain.isSelected()) {
					spImgV.setVisible(true);
				}
			} else {
				butts.forEach(b -> b.setVisible(false));
				spImgV.setVisible(false);
			}
		});
		
		EventHandler<MouseEvent> clicked = e -> {
			Point2D coor = world.getCoors(e);

			//System.out.println(coor);

			if (world.valid((int) coor.x, (int) coor.y)) {
				Cell cell = world.get((int) coor.x, (int) coor.y);
				if (cell.isOpen() && cell.occupied()) {

					Agent agent = cell.getAgent();
					details.setText(
							String.format("Gen:  %d\nFood: %.4f\nAct:  Weight\tSkill\nProd: %.4f\t%.4f\nCons: %.4f\t%.4f\nRel:  %.4f\t%.4f\nRep:  %.4f\t%.4f\nComm: %.4f\t%.4f", 
									agent.generation, 
									agent.stomach(),
									agent.produce.getWeight(), agent.produce.getSkill(), 
									agent.consume.getWeight(), agent.consume.getSkill(), 
									agent.relocate.getWeight(), agent.relocate.getSkill(), 
									agent.reproduce.getWeight(), agent.reproduce.getSkill(), 
									agent.commune.getWeight(), agent.commune.getSkill()));
					details.setVisible(true);

					selected = agent;
					
				} else {
					details.setVisible(false);
					selected = null;
				}
			} else {
				details.setVisible(false);
				selected = null;
			}
			

			world.setSelectedAgent(selected);
			world.draw();

		};

		root.setOnMouseClicked(clicked);

		Point prevMouse = new Point(0,0);

		root.setOnMousePressed(e -> {
			prevMouse.setLocation(e.getX(), e.getY());
		});

		root.setOnMouseDragged(e -> {
			double dx = prevMouse.getX() - e.getX();
			double dy = prevMouse.getY() - e.getY();

			world.move(-dx, -dy);

			prevMouse.setLocation(e.getX(), e.getY());

			e.consume();

		});

		/*root.setOnTouchPressed(e -> {
			prevMouse.setLocation(e.getTouchPoint().getX(), e.getTouchPoint().getY());
		});

		root.setOnTouchMoved(e -> {

			if (e.getTouchCount() == 1) {
				double dx = prevMouse.getX() - e.getTouchPoint().getX();
				double dy = prevMouse.getY() - e.getTouchPoint().getY();

				world.move(-dx, -dy);


				prevMouse.setLocation(e.getTouchPoint().getX(), e.getTouchPoint().getY());

				//System.out.println(prevMouse.x  + " " + prevMouse.y);

				e.consume();
			}


		});*/

		stage.setFullScreen(true);


	}

	int i = 20;

	private void pause() {

		if (cont) {
			lock.lock();
			next.setText("Start");
			cont = false;
			lock.unlock();

			step.setDisable(false);
		}

	}


	private Lock lock = new ReentrantLock();
	private Condition cond = lock.newCondition();

	private void go() {
		if (!cont) {
			lock.lock();

			next.setText("Stop");
			cont = true;

			cond.signalAll();

			lock.unlock();

			step.setDisable(true);
		}
	}

	private void toggleTime() {
		if (cont) {
			pause();
		} else {
			go();
		}

	}

	private void repaint() {
		world.initialDraw();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
