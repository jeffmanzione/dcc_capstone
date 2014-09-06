package world.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.scene.paint.Color;
import agents.action.Action;
import world.Cell;
import world.CellAlreadyHasAgentException;

public class Agent {

	Cell curr;

	protected Agent[] parents;

	double food;

	public final Action produce, consume, relocate, reproduce, commune;

	private final int MAX_SKILL = 8, MAX_MEMORY = 12;
	private final double FOOD_PRODUCTION_CONSTANT = 1.0, MOVE_THRESHOLD = 3.0, REPRODUCE_THRESHOLD = 10.0,
			FOOD_CONSUMPTION_CONSTANT = 0.23, COMMUNICATION_DENOMINATOR = 2.0, SPECIATION_DIFFERENCE_THRESHOLD = 8,
			IDEAL_NUM_NEIGHBORS = 1, FOOD_COST_PER_IDEAL_NEIGHBOR_DIFF = 0.01;

	private Random rand = new Random();

	private double totalWeight, produceMark, consumeMark, relocateMark, reproduceMark;

	public final int generation;

	enum Last {
		PROD, CONS, RELOC, REPROD, COMMUN
	}
	private Last act = Last.RELOC;
	
	private final int birthday;

	public Agent(Cell start, double food, Action produce, Action consume, Action relocate, Action reproduce, Action communicate, int gen) {
		this.curr 		= start;
		this.food 		= food;
		this.produce	= produce;
		this.consume	= consume;
		this.relocate	= relocate;
		this.reproduce 	= reproduce;
		this.commune	= communicate;
		this.generation = gen;

		produceMark 	= totalWeight =	 this.produce.getWeight();
		consumeMark 	= totalWeight += this.consume.getWeight();
		relocateMark 	= totalWeight += this.relocate.getWeight();
		reproduceMark 	= totalWeight += this.reproduce.getWeight();
		totalWeight 				  += this.commune.getWeight();

		acts = Arrays.asList(produce, consume, relocate, reproduce, commune);
		birthday = curr.getDay();

	}

	public Agent(Cell start, double food, Action produce, Action consume, Action relocate, Action reproduce, Action communicate, int gen, Agent... parents) {
		this(start, food, produce, consume, relocate, reproduce, communicate, gen);

		this.parents = parents;

	}

	public void step() throws CellAlreadyHasAgentException {
		// we eat to survive
		//food -= FOOD_CONSUMPTION_CONSTANT + (Math.abs(this.neighbors().size() - IDEAL_NUM_NEIGHBORS) == 0 ? 0 : (FOOD_COST_PER_IDEAL_NEIGHBOR_DIFF * (Math.abs(this.neighbors().size() - IDEAL_NUM_NEIGHBORS))));
		food -= FOOD_CONSUMPTION_CONSTANT;
		// if we starved, that means we died
		if (food <= 0) {
			food = 0;
			die();
		} else {
			// pick number between 0 and 100
			double which = rand.nextDouble();
			// scale it
			double scaled_which = which * totalWeight;

			if (scaled_which <= produceMark) {
				produce();
			} else if (scaled_which <= consumeMark) {
				consume();
			} else if (scaled_which <= relocateMark) {
				relocate();
			} else if (scaled_which <= reproduceMark) {
				reproduce();
			} else {
				communicate();
			}
		}

	}

	private void communicate() {
		act = Last.COMMUN;
		//System.out.println("Communicating: " + this);
		double skill = commune.getSkill() / COMMUNICATION_DENOMINATOR;

		curr.alertCellFoodLoc((int) skill);
	}

	private void produce() {
		act = Last.PROD;
		//System.out.println("Producing: " + this);
		double skill = produce.getSkill();

		double dF = skill * FOOD_PRODUCTION_CONSTANT / MAX_SKILL;
		curr.addFood(dF);

	}

	private void consume() {
		act = Last.CONS;
		//System.out.println("Consuming: " + this);
		double skill = consume.getSkill();

		if (curr.food() > 0) {
			double dF = curr.food() * skill / MAX_SKILL;
			curr.subFood(dF);
			food += dF;
		}
	}

	double moveSum = 0;


	/*
	 * MODIFY THIS TO MOVE IN DIRECTION OF HIGHEST KNOWN FOOD OR IF NONE KNOWN, RANDOM.
	 * 
	 */
	private void relocate() throws CellAlreadyHasAgentException {
		act = Last.RELOC;
		//System.out.println("Relocating: " + this);
		double skill = relocate.getSkill();

		moveSum += skill / MAX_SKILL;

		if (moveSum >= MOVE_THRESHOLD) {

			Cell best = bestAvailable();
			if (best != null) {
				curr.moveAgentTo(best);
				moveSum -= MOVE_THRESHOLD;
				//System.out.println("P: " + memoryPriority + " " + curr);
				//System.out.flush();
				if (memoryPriority.contains(curr)) {
					memoryBank.remove(curr);
					memoryPriority.remove(curr);
				}
			}
		}

	}

	private Cell bestAvailable() {

		List<Cell> empties = curr.getEmptyCellsInNeighborhood();

		if (memoryPriority.size() > 0) {

			Cell best = memoryPriority.first();

			while (true) {
				if (best == null) {
					return randomEmptyNeighbor();
				} else if (curr.getNeighborhood().contains(best)) {
					if (best.occupied()) {
						best = memoryPriority.lower(best);
					} else {
						return best;
					}
				} else {
					Cell dir = curr.getCellInDirectionOf(best);
					if (dir.occupied()) {
						return randomEmptyNeighbor();
					} else {
						return dir;
					}
				}
			}

		} else if (empties.size() > 0) {
			return randomEmptyNeighbor();
		} else {
			return null;
		}
	}

	private Cell randomEmptyNeighbor() {
		List<Cell> empties = curr.getEmptyCellsInNeighborhood();
		if (empties.size() > 0) {
			return empties.get(rand.nextInt(empties.size()));
		} else return null;
	}

	double reproduceSum = 0;

	private static final int PRODUCE = 0, CONSUME = 1, RELOCATE = 2, 
			REPRODUCE = 3, COMMUNICATE = 4;

	private void reproduce() throws CellAlreadyHasAgentException {
		act = Last.REPROD;
		//System.out.println("Reproducing: " + this);
		double skill = reproduce.getSkill();

		reproduceSum += skill / MAX_SKILL;

		List<Cell> empties = curr.getEmptyCellsInNeighborhood();

		if (reproduceSum >= REPRODUCE_THRESHOLD && empties.size() > 0) {
			food /= 2;
			reproduceSum -= REPRODUCE_THRESHOLD;

			Cell which = empties.get(rand.nextInt(empties.size()));

			double[] weightsS = new double[5];
			double[] skillsS = new double[5];

			double[] weights = new double[5];
			double[] skills = new double[5];


			List<Agent> potentialPartners = sameSpecies(this.neighbors());

			Agent partner = null;

			if (potentialPartners.size() > 0) /* Sexual reproduction */ {
				partner = potentialPartners.get(rand.nextInt(potentialPartners.size()));
				weightsS[PRODUCE] 		= rand.nextInt(2) > 0 ? produce.getWeight() : partner.produce.getWeight();
				weightsS[CONSUME] 		= rand.nextInt(2) > 0 ? consume.getWeight() : partner.consume.getWeight();
				weightsS[RELOCATE] 		= rand.nextInt(2) > 0 ? relocate.getWeight() : partner.relocate.getWeight();
				weightsS[REPRODUCE] 	= rand.nextInt(2) > 0 ? reproduce.getWeight() : partner.reproduce.getWeight();
				weightsS[COMMUNICATE] 	= rand.nextInt(2) > 0 ? commune.getWeight() : partner.commune.getWeight();

				skillsS[PRODUCE] 		= rand.nextInt(2) > 0 ? produce.getSkill() : partner.produce.getSkill();
				skillsS[CONSUME] 		= rand.nextInt(2) > 0 ? consume.getSkill() : partner.consume.getSkill();
				skillsS[RELOCATE] 		= rand.nextInt(2) > 0 ? relocate.getSkill() : partner.relocate.getSkill();
				skillsS[REPRODUCE] 		= rand.nextInt(2) > 0 ? reproduce.getSkill() : partner.reproduce.getSkill();
				skillsS[COMMUNICATE] 	= rand.nextInt(2) > 0 ? commune.getSkill() : partner.commune.getSkill();

			} else /* Asexual reproduction */ {
				weightsS[PRODUCE] 		= produce.getWeight();
				weightsS[CONSUME] 		= consume.getWeight();
				weightsS[RELOCATE] 		= relocate.getWeight();
				weightsS[REPRODUCE] 	= reproduce.getWeight();
				weightsS[COMMUNICATE] 	= commune.getWeight();

				skillsS[PRODUCE] 		= produce.getSkill();
				skillsS[CONSUME] 		= consume.getSkill();
				skillsS[RELOCATE] 		= relocate.getSkill();
				skillsS[REPRODUCE]		= reproduce.getSkill();
				skillsS[COMMUNICATE] 	= commune.getSkill();
			}

			weights[PRODUCE] 		= weightsS[PRODUCE] 	+ Action.boxMullerGen();
			weights[CONSUME] 		= weightsS[CONSUME] 	+ Action.boxMullerGen();
			weights[RELOCATE] 		= weightsS[RELOCATE] 	+ Action.boxMullerGen();
			weights[REPRODUCE] 		= weightsS[REPRODUCE] 	+ Action.boxMullerGen();
			weights[COMMUNICATE]	= weightsS[COMMUNICATE] + Action.boxMullerGen();

			double min = 100;
			for (int i = 0 ; i < weights.length; i++) {
				if (weights[i] < min) min = weights[i];
			}

			double minDiff = 0 - min;

			double weightSum = 0;


			for (int i = 0 ; i < weights.length; i++) {
				if (minDiff > 0) {
					weights[i] += minDiff;
				}
				weightSum += weights[i];
			}


			for (int i = 0 ; i < weights.length; i++) {
				weights[i] = weights[i] * 100 / weightSum;
			}



			skills[PRODUCE] 	= skillsS[PRODUCE] 		+ Action.boxMullerGen();
			skills[CONSUME] 	= skillsS[CONSUME] 		+ Action.boxMullerGen();
			skills[RELOCATE] 	= skillsS[RELOCATE] 	+ Action.boxMullerGen();
			skills[REPRODUCE] 	= skillsS[REPRODUCE] 	+ Action.boxMullerGen();
			skills[COMMUNICATE]	= skillsS[COMMUNICATE] 	+ Action.boxMullerGen();

			min = 100;
			for (int i = 0 ; i < skills.length; i++) {
				if (skills[i] < min) min = skills[i];
			}

			minDiff = 0 - min;

			double skillSum = 0;


			for (int i = 0 ; i < skills.length; i++) {
				if (minDiff > 0) {
					skills[i] += minDiff;
				}
				skillSum += skills[i];
			}

			for (int i = 0 ; i < skills.length; i++) {
				skills[i] = skills[i] * 35 / skillSum;
			}

			Agent baby;
			if (partner == null) {
				baby = new Agent(which, food, 
						new Action(skills[PRODUCE], 	weights[PRODUCE]), 
						new Action(skills[CONSUME], 	weights[CONSUME]),
						new Action(skills[RELOCATE], 	weights[RELOCATE]), 
						new Action(skills[REPRODUCE], 	weights[REPRODUCE]), 
						new Action(skills[COMMUNICATE], weights[COMMUNICATE]), 
						generation + 1,
						this);
			} else {
				baby = new Agent(which, food, 
						new Action(skills[PRODUCE], 	weights[PRODUCE]), 
						new Action(skills[CONSUME], 	weights[CONSUME]),
						new Action(skills[RELOCATE], 	weights[RELOCATE]), 
						new Action(skills[REPRODUCE], 	weights[REPRODUCE]), 
						new Action(skills[COMMUNICATE], weights[COMMUNICATE]), 
						generation + 1,
						this, partner);
			}

			which.put(baby);
			
			curr.getManager().addToTree(baby, this);
			
			//System.out.println("Reproducing: " + this);
			//System.out.println("BABY: " + baby);
			which.flagged = true;
		}

	}

	private List<Agent> sameSpecies(List<Agent> neighbors) {
		List<Agent> inSpecies = new ArrayList<Agent>();
		for (Agent a : neighbors) {
			if (Math.abs(a.consume.getWeight() - this.consume.getWeight()) + 
					Math.abs(a.consume.getSkill() - this.consume.getSkill()) + 
					Math.abs(a.produce.getWeight() - this.produce.getWeight()) + 
					Math.abs(a.produce.getSkill() - this.produce.getSkill()) +
					Math.abs(a.relocate.getWeight() - this.relocate.getWeight()) +
					Math.abs(a.relocate.getSkill() - this.relocate.getSkill()) +
					Math.abs(a.reproduce.getWeight() - this.reproduce.getWeight()) +
					Math.abs(a.reproduce.getSkill() - this.reproduce.getSkill()) +
					Math.abs(a.commune.getWeight() - this.commune.getWeight()) +
					Math.abs(a.commune.getSkill() - this.commune.getSkill()) < SPECIATION_DIFFERENCE_THRESHOLD) {
				inSpecies.add(a);
			}
		}
		return inSpecies;
	}

	private void die() {
		//System.out.println("Dying: " + this);
		curr.addFood(food);
		curr.removeAgent();
	}

	public List<Agent> neighbors() {
		return curr.getNeighbors();
	}

	public void setCurrent(Cell cell) {
		curr = cell;
	}

	private Map<Cell, Double> memoryBank;
	private TreeSet<Cell> memoryPriority;

	{
		memoryBank = new HashMap<>();
		memoryPriority = new TreeSet<Cell>(new Comparator<Cell>() {
			public int compare(Cell c1, Cell c2) {
				return Double.compare(memoryBank.containsKey(c2) ? memoryBank.get(c2) : 0, memoryBank.containsKey(c1) ? memoryBank.get(c1) : 0);
			}
		}) {
			private static final long serialVersionUID = -9132160684073633389L;

			public boolean contains(Object cell) {
				try {
					return super.contains(cell);
				} catch (NullPointerException e) {
					return false;
				}
			}
		};
	}

	public void updateFoodTable(Cell loc) {
		//System.out.println("Inserting: " + loc + ": " + loc.food());

		if (memoryBank.containsKey(loc)) {
			memoryBank.remove(loc);
		}

		if (memoryPriority.contains(loc)) {
			memoryPriority.remove(loc);
		}

		memoryBank.put(loc, loc.food());
		memoryPriority.add(loc);

		if (memoryBank.size() > MAX_MEMORY) {
			Cell last = memoryPriority.last();
			if (memoryPriority.contains(last)) {
				memoryPriority.remove(last);
				memoryBank.remove(last);
			}
		}
	}

	public String toString() {
		String result = "<" + this.hashCode() + "," + generation + "," + birthday + ",{";
		
		if (parents != null && parents.length > 0) {
			result += parents[0].hashCode();
			
			for (int i = 1; i < parents.length; i++) {
				result += "," + parents[i].hashCode();
			}
		}
		
		result += "}: " + produce.toString() + "," + consume.toString() + "," + relocate.toString() + "," + reproduce.toString() + "," + commune.toString() + ">";
	
		return result;
	}
	
	private static final Color 
			P = Color.web("0099cc"),
			CS = Color.web("33cc33"),
			RL = Color.web("ffff00"),
			RP = Color.web("ff9966"),
			CM = Color.web("ff6666");
	

	public Color getColor() {
		switch (act) {
		case PROD:
			return P;
		case CONS:
			return CS;
		case RELOC:
			return RL;
		case REPROD:
			return RP;
		default:
			return CM;
		}

		//		List<Action> acts = Arrays.asList(produce, consume, relocate, reproduce, commune);
		//		int tot_num = acts.size();
		//		int interval = 360 / tot_num;
		//		i = 0;
		//		
		//		IntSupplier getI = () -> i += interval;
		//		
		//		double max = acts.stream().map(a -> a.getSkill()).max(Double::compare).get();
		//		Stream<Color> scaled = acts.stream().map(a -> Color.web(String.format("hsl(%d,%d%%,%d%%)", getI.getAsInt(), (int) (a.getSkill() * 100.0 / max), 100)));
		//		
		//		Double[] vals = scaled.map(col -> new Double[]{col.getRed(), col.getGreen(), col.getBlue()}).reduce((sum, col) -> new Double[]{(sum[0] + col[0]), (sum[1] + col[1]), (sum[2] + col[2])}).get();
		//		
		//		
		//		Color col = Color.web(String.format("rgb(%d,%d,%d)", (int) (vals[0] * 255 / tot_num), (int) (vals[1] * 255 / tot_num), (int) (vals[2] * 255 / tot_num)));
		//		System.out.println(col);
		//		
		//		return col;
	}

	public int i = 0;

	private List<Action> acts ;

	public List<Double> vals() {

		List<Double> skills = acts.stream().map(a -> a.getSkill()).collect(Collectors.toList());
		double sum = skills.stream().reduce((x, y) -> x + y).get();
		return skills.parallelStream().map(x -> x * 360.0 / sum).collect(Collectors.toList());

	}
	
	public List<Double> weights() {
		return acts.stream().map(a -> a.getWeight()).collect(Collectors.toList());
	}

	public Color getColorFromVal(int index) {
		switch (index) {
		case 0:
			return P;
		case 1:
			return CS;
		case 2:
			return RL;
		case 3:
			return RP;
		default:
			return CM;
		}
	}

	public java.awt.Color getColorAWT() {
		switch (act) {
		case PROD:
			return java.awt.Color.BLUE;
		case CONS:
			return java.awt.Color.RED;
		case RELOC:
			return java.awt.Color.BLACK;
		case REPROD:
			return new java.awt.Color(0x00800080);
		default:
			return java.awt.Color.ORANGE;
		}
	}

	public double stomach() {
		return food;
	}

	
	
	public History getHistory() {
		return History.createFromStart(this);
	}
	
	public int birthday() {
		return birthday;
	}

}
