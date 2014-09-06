package world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import world.agents.Agent;



public abstract class Cell {

	public final int q, r;

	protected CellManager manager;

	public boolean flagged = false;
	
	public Cell(int q, int r) {
		this.q = q;
		this.r = r;
	}


	public void setManager(CellManager manager) {
		this.manager = manager;
	}

	public enum Direction {
		LEFT, RIGHT, ULEFT, URIGHT, LRIGHT, LLEFT;
	}

	public Cell get(Direction dir) {
		switch (dir) {
		case LEFT:
			return getLeft();
		case RIGHT:
			return getRight();
		case ULEFT:
			return getUpperLeft();
		case URIGHT:
			return getUpperRight();
		case LLEFT:
			return getLowerLeft();
		case LRIGHT:
		default:
			return getLowerRight();
		}
	}

	public Cell getLeft() {
		return manager.get(q-1, r);
	}

	public Cell getRight() {
		return manager.get(q+1, r);
	}

	public Cell getUpperLeft() {
		return manager.get(q, r-1);
	}

	public Cell getUpperRight() {
		return manager.get(q+1, r-1);
	}

	public Cell getLowerLeft() {
		return manager.get(q-1, r+1);
	}

	public Cell getLowerRight() {
		return manager.get(q, r+1);
	}




	/**
	 * Gets the neighboring agents to this cell
	 * @return	A List:Agent of the neighboring agents
	 */
	public List<Agent> getNeighbors() {
		List<Agent> neighbors = new ArrayList<>();

		for (Direction dir : Direction.values()) {
			Cell cell = this.get(dir);
			if (cell.isOpen() && cell.occupied()) {
				neighbors.add(cell.getAgent());
			}
		}

		return neighbors;
	}

	/**
	 * Gets the neighborhood, which consists of all adjacent tiles.
	 * @return List:Cell of all the adjacent tiles.
	 */
	public List<Cell> getNeighborhood() {
		List<Cell> neighborhood = new ArrayList<>();

		Arrays.asList(Direction.values()).forEach(d -> { Cell c = get(d); if (c.isOpen()) neighborhood.add(c); });

		return neighborhood;
	}

	public abstract boolean isOpen();
	public abstract boolean occupied();
	public abstract Agent getAgent();
	
	public List<Cell> getEmptyCellsInNeighborhood() {
		List<Cell> neighborhood = new ArrayList<>();

		for (Direction dir : Direction.values()) {
			Cell neighbor = get(dir);

			if (!neighbor.occupied() && neighbor.isOpen()) {
				neighborhood.add(get(dir));
			}
		}

		return neighborhood;
	}
	
	public int distanceTo(Cell other) {
		return (Math.abs(this.q - other.q) + Math.abs(this.r - other.r) + Math.abs(this.q + this.r - other.q - other.r)) / 2;
	}
	
	public Cell getCellInDirectionOf(Cell target) {
		double dist = distanceTo(target);
		
		double old_x = q, old_y = -q - r;
		double dx = target.q - old_x, dy = (-target.q - target.r) - old_y;
		
		double new_x = q + (dx / dist), new_y = (-q - r) + (dy / dist);
		
		return manager.get((int) new_x, (int) -(new_y + q));
	}

	public String toString() {
		return "(" + q + "," + r + ")";
	}

	public abstract void step() throws CellAlreadyHasAgentException;

	public abstract double food();


	public abstract void addFood(double food);

	public abstract void subFood(double food);


	public abstract void put(Agent a) throws CellAlreadyHasAgentException;
	
	public abstract void moveAgentTo(Cell cell) throws CellAlreadyHasAgentException;


	public abstract void removeAgent();


	public abstract void alertFood(Cell loc);


	public abstract void alertCellFoodLoc(int radius);


	public int getDay() {
		if (manager == null) return 0;
		else return manager.getDay();
	}
	
	public CellManager getManager() {
		return manager;
	}
	
}
