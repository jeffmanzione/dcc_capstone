package world;

import world.agents.Agent;

public class ClosedCell extends Cell {

	public ClosedCell(int q, int r) {
		super(q, r);
	}

	public boolean isOpen() {
		return false;
	}

	public boolean occupied() {
		return true;
	}

	public Agent getAgent() {
		return null;
	}

	public void step() throws CellAlreadyHasAgentException {
		
	}

	public double food() {
		return 0;
	}

	public void addFood(double food) { }

	public void subFood(double food) { }

	public void put(Agent a) throws CellAlreadyHasAgentException { }

	public void moveAgentTo(Cell cell) throws CellAlreadyHasAgentException { }

	public void removeAgent() { }

	public void alertFood(Cell loc) { }

	public void alertCellFoodLoc(int radius) { }

}
