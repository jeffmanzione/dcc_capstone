package world;

import world.agents.Agent;

public class OpenCell extends Cell{

	public OpenCell(int q, int r) {
		super(q, r);
	}


	private Agent agent = null;

	private double food;
	
	public Agent getAgent() {
		return agent;
	}


	public boolean occupied() {
		return agent != null;
	}


	public void step() throws CellAlreadyHasAgentException {
		if (occupied()) {
			//System.out.println("Stepping: " + this);
			agent.step();
		}
	}

	public double food() {
		return food;
	}


	public void addFood(double food) {
		this.food += food;
	}

	public void subFood(double food) {
		this.food -= food;
	}


	public void put(Agent a) throws CellAlreadyHasAgentException {
		if (a == null) {
			throw new NullPointerException();
		} else if (occupied()) {
			throw new CellAlreadyHasAgentException();
		} else {
			agent = a;
			agent.setCurrent(this);
		}
	}
	
	public void moveAgentTo(Cell cell) throws CellAlreadyHasAgentException {
		//System.out.println("Coors: " + this + ", Agent: " + agent + ", To: " + cell);
		if (cell == null){
			throw new NullPointerException();
		} else if (agent == null) {
			throw new NullPointerException();
		} else if (cell.occupied()) {
			throw new CellAlreadyHasAgentException();
		} else {
			cell.flagged = true;
			cell.put(agent);
			this.agent = null;
		}
	}


	public void removeAgent() {
		agent = null;
	}


	public void alertFood(Cell loc) {
		if (occupied()) {
			agent.updateFoodTable(loc);
		}
	}


	public void alertCellFoodLoc(int radius) {
		manager.alertCellsInRadiusOfFood(this, radius);
	}


	@Override
	public boolean isOpen() {
		return true;
	}
	
}
