package world;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;












import agents.action.Action;
import world.agents.Agent;
import world.agents.FamilyTree;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class CellManager {

	private Cell[][] cells;

	private static final Random rand = new Random();
	//public static final double INITIAL_PROB = 0.05;

	//private static final int HISTORY_LEN = 50;

	private int offset;

	//private int CURRENT = 0, PREV = -1;
	//private int OLD_SCALE = 24;

	private FamilyTree tree;

	public CellManager(int radius, double fill) throws CellAlreadyHasAgentException {
		this.offset = radius;

		cells = new Cell[offset * 2 - 1][offset * 2 - 1];

		List<Agent> agents = new ArrayList<>();

		for (int i = 0; i < offset * 2 - 1 ; i++) {
			for (int j = 0; j < offset * 2 - 1; j++) {
				cells[i][j] = new OpenCell(i - offset + 1, j - offset + 1);

				cells[i][j].setManager(this);

				if (rand.nextDouble() < fill) {
					Agent agent = new Agent(cells[i][j], 10, new Action(7, 20), new Action(7, 20), new Action(7, 20), new Action(7, 20), new Action(7, 20), 0);
					cells[i][j].put(agent);
					agents.add(agent);
				}
				double val = rand.nextInt(51) / 15;
				cells[i][j].addFood(val * val);
			}

		}

		tree = new FamilyTree(agents);

	}

	public CellManager(File file, double fill) throws CellAlreadyHasAgentException, IOException {
		//System.out.println(file.getName());
		Image img = new Image("file:" + file.getName());

		this.offset = (int) ((img.getWidth() + 1) / 2);

		cells = new Cell[offset * 2][offset * 2];

		List<Agent> agents = new ArrayList<>();

		for (int i = 0; i < img.getWidth(); i++) {
			for (int j = 0; j < img.getHeight(); j++) {
				if (img.getPixelReader().getColor(i, j).equals(Color.WHITE)) {
					cells[i][j] = new OpenCell(i - offset + 1, j - offset + 1);
					if (rand.nextDouble() < fill) {
						Agent agent = new Agent(cells[i][j], 10, new Action(7, 20), new Action(7, 20), new Action(7, 20), new Action(7, 20), new Action(7, 20), 0);
						agents.add(agent);
						cells[i][j].put(agent);
					}
					double val = rand.nextInt(51) / 15;
					cells[i][j].addFood(val * val);
				} else {
					cells[i][j] = new ClosedCell(i - offset + 1, j - offset + 1);
				}
				cells[i][j].setManager(this);
			}
		}

		tree = new FamilyTree(agents);

	}

	public boolean valid(int q, int r) {
		return Math.abs(q) < offset && Math.abs(r) < offset;
	}

	public Cell get(int q, int r) {

		int q_t = (q + offset - 1);
		if (q_t < 0) q_t += (offset * 2 - 1);
		else if (q > (offset - 1)) q_t -= (offset * 2 - 1);

		int r_t = (r + offset - 1);
		if (r_t < 0) r_t += (offset * 2 - 1);
		else if (r > (offset - 1)) r_t -= (offset * 2 - 1);

		return cells[q_t][r_t];
	}

	public void drawLocation(int q, int r, Canvas canvas, double centerX, double centerY, int radius) {

		GraphicsContext g = canvas.getGraphicsContext2D();

		double width = Math.cos(Math.PI / 6) * radius;

		Cell toDraw = this.get(q, r);

		double x = centerX + q * 2 * width + r * width;
		double y = centerY + r * radius * 3 / 2;

		if (toDraw.occupied()) {

			if (radius < 2) {
				g.fillRect(x - width, y - width, 1, 1);
			} else {
				g.fillOval(x - width, y - width, width * 2, width * 2);
			}

		} else {
			/*g.setFill(Color.WHITE);

			g.fillOval(x - width, y - width, width * 2, width * 2);

			g.setFill(Color.BLACK);

			if (radius > 2) {
				g.strokeOval(x - width, y - width, width * 2, width * 2);
			}*/
		}

	}

	public void draw(Canvas canvas, double centerX, double centerY, int radius) {

		GraphicsContext g = canvas.getGraphicsContext2D();

		double width = Math.cos(Math.PI / 6) * radius;


		g.setFill(Color.GREY);

		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		//repaintAll = true;
		//OLD_SCALE = radius;

		g.setFill(Color.BLACK);
		//}

		if (radius <= 2) {

			g.setLineWidth(1);


			double x1 = centerX + (1 - offset) * 2 * width + (1 - offset) * width;
			double y1 = centerY + (1 - offset) * radius * 3 / 2;
			double x2 = centerX + (offset - 1) * 2 * width + (1 - offset) * width;
			double y2 = centerY + (1 - offset) * radius * 3 / 2;

			g.strokeLine(x1, y1, x2, y2);

			x1 = centerX + (1 - offset) * 2 * width + (offset - 1) * width;
			y1 = centerY + (offset - 1) * radius * 3 / 2;
			x2 = centerX + (offset - 1) * 2 * width + (offset - 1) * width;
			y2 = centerY + (offset - 1) * radius * 3 / 2;

			g.strokeLine(x1, y1, x2, y2);

			x1 = centerX + (1 - offset) * 2 * width + (1 - offset) * width;
			y1 = centerY + (1 - offset) * radius * 3 / 2;
			x2 = centerX + (1 - offset) * 2 * width + (offset - 1) * width;
			y2 = centerY + (offset - 1) * radius * 3 / 2;

			g.strokeLine(x1, y1, x2, y2);

			x1 = centerX + (offset - 1) * 2 * width + (1 - offset) * width;
			y1 = centerY + (1 - offset) * radius * 3 / 2;
			x2 = centerX + (offset - 1) * 2 * width + (offset - 1) * width;
			y2 = centerY + (offset - 1) * radius * 3 / 2;

			g.strokeLine(x1, y1, x2, y2);
		}

		for (int q = 1 - offset; q < offset; q++) {
			for (int r = 1 - offset; r < offset; r++) {

				double x = centerX + q * 2 * width + r * width;
				double y = centerY + r * radius * 3 / 2;

				if (x >= -radius && x <= canvas.getWidth() + radius && y >= -radius && y <= canvas.getHeight() + radius) {

					Cell toDraw = this.get(q, r);

					if (toDraw.isOpen()) {
						double off = (toDraw.food() >= 50) ? 1 : (toDraw.food() / 50);
						if (off < 0) off = 0;
						g.setFill(new Color(1 - off, 1, 1 - off, 1));

					}

					if (toDraw.isOpen()) {
						if (radius < 2) {
							g.fillRect(x - width, y - width, 1, 1);
						} else {
							g.fillOval(x - width, y - width, width * 2, width * 2);
						}
						g.setFill(Color.BLACK);
					}

					if (toDraw.isOpen() && toDraw.occupied()) {
						Agent a = toDraw.getAgent();

						if (a == selectedAgent) {
							g.setFill(new Color(1,0,0,0.3));
							g.fillOval(x - width, y - width, width * 2, width * 2);
						}

						if (radius >= 128) {

							List<Double> vals = a.vals();
							List<Double> weights = a.weights();
							double pos = 90;
							for (int i = 0; i < vals.size(); i++) {
								double val = vals.get(i);
								double weight = weights.get(i);
								g.setFill(a.getColorFromVal(i));

								double actual =  (width * weight / 20 - width) * 2 + width;

								g.fillArc(x - actual/2, y - actual/2, actual, actual, pos, val, ArcType.ROUND);
								g.setFill(a.getColor());
								g.fillOval(x - width / 8, y - width / 8, width / 4, width / 4);
								//g.setLineWidth(width / 16);
								g.strokeArc(x - actual/2, y - actual/2, actual, actual, pos, val, ArcType.OPEN);
								pos += val;
							}
						} else {

							g.setFill(toDraw.getAgent().getColor());

							if (radius < 2) {
								g.fillRect(x - width, y - width, 1, 1);
							} else {
								g.fillOval(x - width/2, y - width/2, width/2 * 2, width/2 * 2);
							}
						}
					}
				}
			}
		}


	}

	//private Color blocked = new Color(0.5, 0.5, 0.5, 1.0);

	int count = 0;

	private int day = 0;

	public void step(GraphicState state, Canvas canvas, double centerX, double centerY, int radius) throws CellAlreadyHasAgentException {
		day++;
		if (state == GraphicState.DRAW_ONLY) {
			for (int q = 1 - offset; q < offset; q++) {
				for (int r = 1 - offset; r < offset; r++) {

					Cell cell = get(q, r);
					if (cell.flagged) {
						cell.flagged = false;
					} else {
						if (cell.occupied()) {
							cell.step();
						}
					}
				}
			}

			this.draw(canvas, centerX, centerY, radius);

		} else if (state == GraphicState.DATA_ONLY) {
			double prodW = 0, consW = 0, relW = 0, repW = 0, commW = 0, prodS = 0, consS = 0, relS = 0, repS = 0, commS = 0;
			int num = 0;
			double gen = 0;

			for (int q = 1 - offset; q < offset; q++) {
				for (int r = 1 - offset; r < offset; r++) {

					Cell cell = get(q, r);
					if (!cell.flagged) {
						if (cell.occupied()) {
							num++;
							if (count % 20 == 0) {
								Agent a = cell.getAgent();
								prodW += a.produce.getWeight();
								prodS += a.produce.getSkill();
								consW += a.consume.getWeight();
								consS += a.consume.getSkill();
								relW += a.relocate.getWeight();
								relS += a.relocate.getSkill();
								repW += a.reproduce.getWeight();
								repS += a.reproduce.getSkill();
								commW += a.commune.getWeight();
								commS += a.commune.getSkill();
								gen += a.generation;
							}
						}
					}

				}
			}

			prodW /= num;
			consW /= num;
			relW /= num;
			repW /= num;
			commW /= num;
			prodS /= num;
			consS /= num;
			relS /= num;
			repS /= num;
			commS /= num;
			gen /= num;

			double stdProdW = 0, stdConsW = 0, stdRelW = 0, stdRepW = 0, stdCommW = 0,
					stdProdS = 0, stdConsS = 0, stdRelS = 0, stdRepS = 0, stdCommS = 0;

			for (int q = 1 - offset; q < offset; q++) {
				for (int r = 1 - offset; r < offset; r++) {

					Cell cell = get(q, r);
					if (cell.flagged) {
						cell.flagged = false;
					} else {
						if (cell.occupied()) {
							if (count % 20 == 0) {
								Agent a = cell.getAgent();
								stdProdW += Math.pow(a.produce.getWeight() - prodW, 2);
								stdProdS += Math.pow(a.produce.getSkill() - prodS, 2);
								stdConsW += Math.pow(a.consume.getWeight() - consW, 2);
								stdConsS += Math.pow(a.consume.getSkill() - consS, 2);
								stdRelW += Math.pow(a.relocate.getWeight() - relW, 2);
								stdRelS += Math.pow(a.relocate.getSkill() - relS, 2);
								stdRepW += Math.pow(a.reproduce.getWeight() - repW, 2);
								stdRepS += Math.pow(a.reproduce.getSkill() - repS, 2);
								stdCommW += Math.pow(a.commune.getWeight() - commW, 2);
								stdCommS += Math.pow(a.commune.getSkill() - commS, 2);
							}
							cell.step();
						}
					}

				}
			}

			stdProdW = Math.sqrt(stdProdW / num);
			stdConsW = Math.sqrt(stdConsW / num);
			stdRelW = Math.sqrt(stdRelW / num);
			stdRepW = Math.sqrt(stdRepW / num);
			stdCommW = Math.sqrt(stdCommW / num);
			stdProdS = Math.sqrt(stdProdS / num);
			stdConsS = Math.sqrt(stdConsS / num);
			stdRelS = Math.sqrt(stdRelS / num);
			stdRepS = Math.sqrt(stdRepS / num);
			stdCommS = Math.sqrt(stdCommS / num);

			if (count % 20 == 0) {
				System.out.println(count + "\t\t" + num + "\t\t" + prodW + "\t" + consW + "\t" + relW + "\t" + repW + "\t" + commW + "\t\t" + prodS + "\t" + consS + "\t" + relS + "\t" + repS + "\t" + commS + "\t\t" + gen + "\t\t" + stdProdW + "\t" + stdConsW + "\t" + stdRelW + "\t" + stdRepW + "\t" + stdCommW + "\t\t" + stdProdS + "\t" + stdConsS + "\t" + stdRelS + "\t" + stdRepS + "\t" + stdCommS);
			}

			//CURRENT = (CURRENT + 1) % HISTORY_LEN;
			//PREV = (PREV + 1) % HISTORY_LEN;


			count++;
		} else {
			double prodW = 0, consW = 0, relW = 0, repW = 0, commW = 0, prodS = 0, consS = 0, relS = 0, repS = 0, commS = 0;
			int num = 0;
			double gen = 0;

			for (int q = 1 - offset; q < offset; q++) {
				for (int r = 1 - offset; r < offset; r++) {

					Cell cell = get(q, r);
					if (!cell.flagged) {
						if (cell.occupied() && cell.isOpen()) {
							num++;
							if (count % 20 == 0) {
								Agent a = cell.getAgent();
								prodW += a.produce.getWeight();
								prodS += a.produce.getSkill();
								consW += a.consume.getWeight();
								consS += a.consume.getSkill();
								relW += a.relocate.getWeight();
								relS += a.relocate.getSkill();
								repW += a.reproduce.getWeight();
								repS += a.reproduce.getSkill();
								commW += a.commune.getWeight();
								commS += a.commune.getSkill();
								gen += a.generation;
							}
						}
					}

				}
			}

			prodW /= num;
			consW /= num;
			relW /= num;
			repW /= num;
			commW /= num;
			prodS /= num;
			consS /= num;
			relS /= num;
			repS /= num;
			commS /= num;
			gen /= num;

			double stdProdW = 0, stdConsW = 0, stdRelW = 0, stdRepW = 0, stdCommW = 0,
					stdProdS = 0, stdConsS = 0, stdRelS = 0, stdRepS = 0, stdCommS = 0;

			for (int q = 1 - offset; q < offset; q++) {
				for (int r = 1 - offset; r < offset; r++) {

					Cell cell = get(q, r);
					if (cell.flagged) {
						cell.flagged = false;
					} else {
						if (cell.occupied() && cell.isOpen()) {
							if (count % 20 == 0) {
								Agent a = cell.getAgent();
								stdProdW += Math.pow(a.produce.getWeight() - prodW, 2);
								stdProdS += Math.pow(a.produce.getSkill() - prodS, 2);
								stdConsW += Math.pow(a.consume.getWeight() - consW, 2);
								stdConsS += Math.pow(a.consume.getSkill() - consS, 2);
								stdRelW += Math.pow(a.relocate.getWeight() - relW, 2);
								stdRelS += Math.pow(a.relocate.getSkill() - relS, 2);
								stdRepW += Math.pow(a.reproduce.getWeight() - repW, 2);
								stdRepS += Math.pow(a.reproduce.getSkill() - repS, 2);
								stdCommW += Math.pow(a.commune.getWeight() - commW, 2);
								stdCommS += Math.pow(a.commune.getSkill() - commS, 2);
							}
							cell.step();
						}
					}

				}
			}

			stdProdW = Math.sqrt(stdProdW / num);
			stdConsW = Math.sqrt(stdConsW / num);
			stdRelW = Math.sqrt(stdRelW / num);
			stdRepW = Math.sqrt(stdRepW / num);
			stdCommW = Math.sqrt(stdCommW / num);
			stdProdS = Math.sqrt(stdProdS / num);
			stdConsS = Math.sqrt(stdConsS / num);
			stdRelS = Math.sqrt(stdRelS / num);
			stdRepS = Math.sqrt(stdRepS / num);
			stdCommS = Math.sqrt(stdCommS / num);

			if (count % 20 == 0) {
				System.out.println(count + "\t\t" + num + "\t\t" + prodW + "\t" + consW + "\t" + relW + "\t" + repW + "\t" + commW + "\t\t" + prodS + "\t" + consS + "\t" + relS + "\t" + repS + "\t" + commS + "\t\t" + gen + "\t\t" + stdProdW + "\t" + stdConsW + "\t" + stdRelW + "\t" + stdRepW + "\t" + stdCommW + "\t\t" + stdProdS + "\t" + stdConsS + "\t" + stdRelS + "\t" + stdRepS + "\t" + stdCommS);
			}

			//CURRENT = (CURRENT + 1) % HISTORY_LEN;
			//PREV = (PREV + 1) % HISTORY_LEN;

			count++;

			this.draw(canvas, centerX, centerY, radius);
		}
	}

	private CellGroup hover = CellGroup.NONE;

	private Set<Cell> selected = new HashSet<>();

	public enum Mode {
		DRAW, ADD, SUBTRACT;
	}

	private Mode mode = Mode.DRAW;

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Mode getMode() {
		return this.mode;
	}

	public CellGroup getHover() {
		return this.hover;
	}


	//private Lock lock = new ReentrantLock();

	public void clearSelection() {
		selected.clear();
	}

	public void movedTo(int q, int r) {
		switch (mode) {
		case ADD:
			selected.add(this.get(q, r));
			break;
		case SUBTRACT:
			selected.remove(this.get(q, r));
			break;
		default:

		}
	}

	public void setHover(CellGroup newValue) {
		this.hover = newValue;
	}

	private int old_hover_q = Integer.MAX_VALUE/*, old_hover_r = Integer.MAX_VALUE*/;

	public void drawHover(Canvas canvas, int q, int r, double centerX, double centerY, int radius) {

		GraphicsContext g = canvas.getGraphicsContext2D();

		double width = Math.cos(Math.PI / 6) * radius;
		double x = centerX + q * 2 * width + r * width;
		double y = centerY + r * radius * 3 / 2;

		if (old_hover_q != Integer.MAX_VALUE) {
			g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		}

		if (Math.abs(q) < offset && Math.abs(r) < offset) {

			g.setStroke(Color.RED);

			g.strokeOval(x - width + 1, y - width + 1, width * 2 - 2, width * 2 - 2);

			g.setStroke(Color.BLACK);

		}

		old_hover_q = q;
		//old_hover_r = r;

		if (mode == Mode.DRAW && hover != CellGroup.NONE) {

			for (int[] loc : hover.coors) {

				x = centerX + (q + loc[0]) * 2 * width + (r + loc[1]) * width;
				y = centerY + (r + loc[1]) * radius * 3 / 2;

				g.setFill(Color.GREEN);

				g.fillOval(x - width, y - width, width * 2, width * 2);

				g.setFill(Color.BLACK);
			}
		} else {

			for (Cell cell : selected) {

				x = centerX + cell.q * 2 * width + cell.r * width;
				y = centerY + cell.r * radius * 3 / 2;

				g.setFill(new Color(0, 1, 0, .5));

				g.fillOval(x - width, y - width, width * 2, width * 2);
				g.setFill(Color.BLACK);

			}
		}

	}


	public void alertCellsInRadiusOfFood(Cell center, int radius) {
		// q = x
		// r = z

		Cell cell;
		for (int x = -radius + center.q; 
				x <= radius + center.q; 
				x++) {
			for (int y = Math.max(-radius + -(center.q + center.r), -x - radius + -(center.q + center.r)); 
					y <= Math.min(radius + -(center.q + center.r), -x + radius + -(center.q + center.r)); 
					y++) {

				cell = get(x, -(x + y));

				cell.alertFood(center);
			}
		}
	}

	public void print(Graphics2D g, double rad) {

		double radius = rad;

		double width = Math.cos(Math.PI / 6) * radius;


		double centerX = radius + (offset - 1) * 2 * width + (offset - 1) * width;
		double centerY = radius + (offset - 1) * radius * 3 / 2;

		//System.out.println("Width: " + (int) (centerX*2) + ", Height: " + (int) (centerY*2));

		g.setPaint(java.awt.Color.BLACK);

		if (radius <= 2) {

			double x1 = centerX + (1 - offset) * 2 * width + (1 - offset) * width;
			double y1 = centerY + (1 - offset) * radius * 3 / 2;
			double x2 = centerX + (offset - 1) * 2 * width + (1 - offset) * width;
			double y2 = centerY + (1 - offset) * radius * 3 / 2;

			g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

			x1 = centerX + (1 - offset) * 2 * width + (offset - 1) * width;
			y1 = centerY + (offset - 1) * radius * 3 / 2;
			x2 = centerX + (offset - 1) * 2 * width + (offset - 1) * width;
			y2 = centerY + (offset - 1) * radius * 3 / 2;

			g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

			x1 = centerX + (1 - offset) * 2 * width + (1 - offset) * width;
			y1 = centerY + (1 - offset) * radius * 3 / 2;
			x2 = centerX + (1 - offset) * 2 * width + (offset - 1) * width;
			y2 = centerY + (offset - 1) * radius * 3 / 2;

			g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

			x1 = centerX + (offset - 1) * 2 * width + (1 - offset) * width;
			y1 = centerY + (1 - offset) * radius * 3 / 2;
			x2 = centerX + (offset - 1) * 2 * width + (offset - 1) * width;
			y2 = centerY + (offset - 1) * radius * 3 / 2;

			g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
		}

		for (int q = 1 - offset; q < offset; q++) {
			for (int r = 1 - offset; r < offset; r++) {


				double x = centerX + q * 2 * width + r * width;
				double y = centerY + r * radius * 3 / 2;


				Cell toDraw = this.get(q, r);


				if (toDraw.food() > 0) {

					g.setPaint(new java.awt.Color(0, 255, 0, (int) ((toDraw.food() > 50 ? 1 : toDraw.food() / 50) * 255)));

					if (radius < 2) {
						g.fillRect((int) (x - width), (int) (y - width), 1, 1);
					} else {
						g.fillOval((int) (x - width), (int) (y - width), (int) (width * 2), (int) (width * 2));
					}

					g.setPaint(java.awt.Color.BLACK);
				}

				if (toDraw.occupied()) {

					g.setPaint(toDraw.getAgent().getColorAWT());

					if (radius < 2) {
						g.fillRect((int) (x - width), (int) (y - width), 1, 1);
					} else {
						g.fillOval((int) (x - width/2), (int) (y - width/2), (int) (width), (int) (width));
					}
				}

			}
		}
	}

	public int getDay() {
		return day;
	}

	public void getTree() {
		try {
			tree.toFile(new File("tree.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addToTree(Agent baby, Agent parent) {
		tree.add(baby, parent);
	}

	private Agent selectedAgent = null;

	public void setSelectedAgent(Agent selected2) {
		selectedAgent = selected2;

	}

}
