package application;


import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public class Practice extends Application {

	private double BRUSH_SIZE = 5;
	private Point2D loc = new Point2D(0, 0);
	private MouseEvent curr;
	private Rectangle2D prev = new Rectangle2D(0 - BRUSH_SIZE / 2, 0 - BRUSH_SIZE / 2, BRUSH_SIZE, BRUSH_SIZE);

	GraphicsContext gc;
	GraphicsContext foreground;

	private static final int DIM = 800;
	
	@Override
	public void start(Stage primaryStage) {

		Group root = new Group();

		Canvas bg_layer = new Canvas(DIM, DIM);

		Canvas fg_layer = new Canvas(DIM, DIM);


		gc = bg_layer.getGraphicsContext2D();
		foreground = fg_layer.getGraphicsContext2D();



		EventHandler<MouseEvent> dragged = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				curr = e;
				handleRepaint();
			}

		};

		EventHandler<ScrollEvent> scrolled = new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent e) {
				double diff = e.getDeltaY() / 10;
				BRUSH_SIZE += diff;

				if (BRUSH_SIZE < 1) {
					BRUSH_SIZE =  1;
				}

				handleRepaint();
			}

		};


		fg_layer.addEventHandler(MouseEvent.MOUSE_PRESSED, dragged);
		fg_layer.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragged);
		fg_layer.addEventHandler(MouseEvent.MOUSE_MOVED, dragged);
		fg_layer.addEventHandler(ScrollEvent.ANY, scrolled);

		root.getChildren().add(bg_layer);
		root.getChildren().add(fg_layer);
		fg_layer.toFront();
		bg_layer.toBack();

		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.show();

	}


	private void handleRepaint() {
		foreground.clearRect(prev.getMinX() - 1, prev.getMinY() - 1, prev.getWidth() + 2, prev.getHeight() + 2);

		if (curr != null) {
			prev = new Rectangle2D(curr.getX() - BRUSH_SIZE / 2, curr.getY() - BRUSH_SIZE / 2, BRUSH_SIZE, BRUSH_SIZE);



			
			if (curr.getEventType() == MouseEvent.MOUSE_DRAGGED) {
				
				double dX = curr.getX() - loc.getX();
				double dY = curr.getY() - loc.getY();
				
				double theta = Math.atan(dY / dX);
				
				if (dX == 0) {
					theta = (dY > 0 ? Math.PI / 2 : -Math.PI / 2);
				} else if (dY == 0) {
					theta = (dX > 0 ? 0 : Math.PI);
				} if (dX < 0 && dY > 0) {
					theta += Math.PI;
				} else if (dX < 0 && dY < 0) {
					theta += Math.PI;
				}
				
				theta = (theta + Math.PI / 2) % (2 * Math.PI);
				
				Point2D o1 = new Point2D(loc.getX() + Math.cos(theta) * BRUSH_SIZE / 2, loc.getY() + Math.sin(theta) * BRUSH_SIZE / 2);
				Point2D o2 = new Point2D(loc.getX() - Math.cos(theta) * BRUSH_SIZE / 2, loc.getY() - Math.sin(theta) * BRUSH_SIZE / 2);
				
				Point2D n1 = new Point2D(curr.getX() + Math.cos(theta) * BRUSH_SIZE / 2, curr.getY() + Math.sin(theta) * BRUSH_SIZE / 2);
				Point2D n2 = new Point2D(curr.getX() - Math.cos(theta) * BRUSH_SIZE / 2, curr.getY() - Math.sin(theta) * BRUSH_SIZE / 2);
				
				gc.fillOval(curr.getX() - BRUSH_SIZE / 2, curr.getY() - BRUSH_SIZE / 2, BRUSH_SIZE, BRUSH_SIZE);

				gc.fillPolygon(new double[] {o1.getX(), o2.getX(), n2.getX(), n1.getX()}, new double[] {o1.getY(), o2.getY(), n2.getY(), n1.getY()}, 4);

				gc.setFill(Color.BLACK);
				
			} else if (curr.getEventType() == MouseEvent.MOUSE_PRESSED) {
				gc.fillOval(curr.getX() - BRUSH_SIZE / 2, curr.getY() - BRUSH_SIZE / 2, BRUSH_SIZE, BRUSH_SIZE);
			}
			
			
			foreground.strokeOval(prev.getMinX(), prev.getMinY(), prev.getWidth(), prev.getHeight());
			
			
			loc = new Point2D(curr.getX(), curr.getY());

		}
	}
	public static void main(String[] args) {
		launch(args);
	}
}
