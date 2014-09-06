package world;

import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import world.CellManager.Mode;
import world.agents.Agent;

import com.sun.javafx.geom.Point2D;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;

public class World {

	public static final int NEIGHBORHOOD_SIZE = 6;
	public static double MUTATION_FACTOR = 1.0;

	public static Random rand = new Random();

	private int SCALE = 24;

	private Canvas canvas, hover;

	private CellManager cells;

	public final int RADIUS;

	private double mouseR, mouseQ;

	private double centerX, centerY;

	private EventHandler<MouseEvent> moved;
	private EventHandler<ZoomEvent> scrolled;
	private EventHandler<ScrollEvent> scrolled2;
	private final GraphicState shouldDraw;

	public World(final Canvas canvas, final Canvas hover, int radius, boolean showData, double fill) throws CellAlreadyHasAgentException {
		shouldDraw = showData ? GraphicState.DRAW_AND_DATA : GraphicState.DRAW_ONLY;
		RADIUS = radius;
		this.canvas = canvas;
		this.hover = hover;
		cells = new CellManager(radius, fill);

		setup();
	}

	public World(final Canvas canvas, final Canvas hover, int radius, boolean showData, File file, double fill) throws CellAlreadyHasAgentException, IOException {
		shouldDraw = showData ? GraphicState.DRAW_AND_DATA : GraphicState.DRAW_ONLY;
		RADIUS = radius;
		this.canvas = canvas;
		this.hover = hover;
		cells = new CellManager(file, fill);

		setup();
	}

	private void setup() {
		centerX = World.this.canvas.getWidth() / 2;
		centerY = World.this.canvas.getHeight() / 2;

		moved = e -> {

			Point2D coor = getCoors(e);

			mouseQ = coor.x;
			mouseR = coor.y;

			cells.drawHover(World.this.hover, (int) coor.x, (int) coor.y, centerX, centerY, SCALE);

			if (getMode() != Mode.DRAW) {
				if (e.isControlDown()) {
					setMode(Mode.SUBTRACT);
				} else {
					setMode(Mode.ADD);
				}
			}
		};


		scrolled2 = e -> {
			int oldScale = SCALE;
			if (e.getDeltaY() > 0) {
				SCALE *= 2;

				if (SCALE > 128) {
					SCALE = 128;
				}

			} else if (e.getDeltaY() < 0){
				SCALE /= 2;

				if (SCALE < 4) {
					SCALE = 4;
				}
			}

			double oldWidth = Math.cos(Math.PI / 6) * oldScale;
			double width = Math.cos(Math.PI / 6) * SCALE;

			double x = centerX + (mouseQ * 2. * oldWidth) + (mouseR * oldWidth);
			double y = centerY + (mouseR * oldScale * 3. / 2.);

			centerX = x - ((mouseQ * 2 * width) + (mouseR * width));
			centerY = y - (mouseR * SCALE * 3. / 2.);

			e.consume();
			
			draw();

			
			cells.drawHover(World.this.hover, (int) mouseQ, (int) mouseR, centerX, centerY, SCALE);

		};
		
		scrolled = e -> {

			Point2D coor = getCoors(e.getX(), e.getY());

			
			mouseQ = coor.x;
			mouseR = coor.y;

			
			int oldScale = SCALE;
			
			if (e.getZoomFactor() > 1) {
				SCALE *= e.getZoomFactor();

				if (SCALE > 256) {
					SCALE = 256;
				}

			} else if (e.getZoomFactor() < 1){
				SCALE *= e.getZoomFactor();

				if (SCALE < 4) {
					SCALE = 4;
				}
			}

			double oldWidth = Math.cos(Math.PI / 6) * oldScale;
			double width = Math.cos(Math.PI / 6) * SCALE;

			double x = centerX + (mouseQ * 2. * oldWidth) + (mouseR * oldWidth);
			double y = centerY + (mouseR * oldScale * 3. / 2.);

			centerX = x - ((mouseQ * 2 * width) + (mouseR * width));
			centerY = y - (mouseR * SCALE * 3. / 2.);

			e.consume();
			
			draw();

			
			cells.drawHover(World.this.hover, (int) mouseQ, (int) mouseR, centerX, centerY, SCALE);
		};

		hover.addEventHandler(MouseEvent.MOUSE_MOVED, moved);
		//hover.addEventHandler(ZoomEvent.ZOOM, scrolled);
		hover.addEventHandler(ScrollEvent.ANY, scrolled2);
	}

	public void setClicked(EventHandler<MouseEvent> clicked) {
		hover.addEventHandler(MouseEvent.MOUSE_CLICKED, clicked);
	}

	public World(int radius, double fill) throws CellAlreadyHasAgentException {
		shouldDraw = GraphicState.DATA_ONLY;
		RADIUS = radius;

		cells = new CellManager(radius, fill);
	}

	public void setCanvases(Canvas bg, Canvas hover) {
		hover.removeEventHandler(MouseEvent.MOUSE_MOVED, moved);
		//hover.removeEventHandler(MouseEvent.MOUSE_CLICKED, clicked);
		//hover.removeEventHandler(ZoomEvent.ZOOM, scrolled);
		hover.removeEventHandler(ScrollEvent.ANY, scrolled2);

		this.canvas = bg;
		this.hover = hover;

		hover.addEventHandler(MouseEvent.MOUSE_MOVED, moved);
		//hover.addEventHandler(MouseEvent.MOUSE_CLICKED, clicked);
		//hover.addEventHandler(ZoomEvent.ZOOM, scrolled);
		hover.addEventHandler(ScrollEvent.ANY, scrolled2);

		this.initialDraw();
	}

	public Point2D getCoors(MouseEvent e) {
		double x = e.getX() - centerX;
		double y = e.getY() - centerY;

		double d_q = ((1. / 3. * Math.sqrt(3) * x - 1. / 3. * y) / SCALE);
		double d_r = (2. / 3. * y / SCALE);

		int q, r;

		if (d_q % 1 > 0) {
			q = ((int) d_q) + ((d_q % 1 > 0.5) ? 1 : 0);
		} else {
			q = ((int) d_q) + ((Math.abs(d_q % 1) > 0.5) ? -1 : 0);
		}

		if (d_r % 1 > 0) {
			r = ((int) d_r) + ((d_r % 1 > 0.5) ? 1 : 0);
		} else {
			r = ((int) d_r) + ((Math.abs(d_r % 1) > 0.5) ? -1 : 0);
		}

		return new Point2D(q, r);
	}

	public Point2D getCoors(double x, double y) {
		double d_q = ((1. / 3. * Math.sqrt(3) * x - 1. / 3. * y) / SCALE);
		double d_r = (2. / 3. * y / SCALE);

		int q, r;

		if (d_q % 1 > 0) {
			q = ((int) d_q) + ((d_q % 1 > 0.5) ? 1 : 0);
		} else {
			q = ((int) d_q) + ((Math.abs(d_q % 1) > 0.5) ? -1 : 0);
		}

		if (d_r % 1 > 0) {
			r = ((int) d_r) + ((d_r % 1 > 0.5) ? 1 : 0);
		} else {
			r = ((int) d_r) + ((Math.abs(d_r % 1) > 0.5) ? -1 : 0);
		}

		return new Point2D(q, r);
	}

	public void draw(int q, int r) {
		cells.drawLocation(q, r, canvas, centerX, centerY, SCALE);
	}

	public void draw() {
		cells.draw(canvas, centerX, centerY, SCALE);
	}

	public void draw(double mouseX, double mouseY) {
		cells.draw(canvas, centerX, centerY, SCALE);
		cells.drawHover(hover, (int) mouseQ, (int) mouseR, centerX, centerY, SCALE);
	}

	public void initialDraw() {
		cells.draw(canvas, centerX, centerY, SCALE);
		cells.drawHover(hover, (int) mouseQ, (int) mouseR, centerX, centerY, SCALE);
	}

	public void step() throws CellAlreadyHasAgentException {
		cells.step(shouldDraw, canvas, centerX, centerY, SCALE);
	}

	public void incYOff() {
		centerY += (150 - SCALE);
		initialDraw();
	}

	public void move(double x, double y) {
		centerX += x;
		centerY += y;
		initialDraw();
	}

	public void incXOff() {
		centerX += (150 - SCALE);
		initialDraw();
	}

	public void decYOff() {
		centerY -= (150 - SCALE);
		initialDraw();
	}

	public void decXOff() {
		centerX -= (150 - SCALE);
		initialDraw();
	}

	public void setHover(CellGroup newValue) {
		cells.setHover(newValue);
		cells.drawHover(hover, (int) mouseQ, (int) mouseR, centerX, centerY, SCALE);
	}

	public void setMode(Mode mode) {
		cells.setMode(mode);
	}

	public Mode getMode() {
		return cells.getMode();
	}

	public void clearSelection() {
		cells.clearSelection();
	}

	public void print(Graphics2D g, double rad) {
		cells.print(g, rad);
	}

	public boolean valid(int x, int y) {
		return cells.valid(x, y);
	}

	public Cell get(int x, int y) {
		return cells.get(x, y);
	}

	public void getTree() {
		cells.getTree();
	}

	public void setSelectedAgent(Agent selected) {
		cells.setSelectedAgent(selected);
		
	}
}
