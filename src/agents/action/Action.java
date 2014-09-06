package agents.action;

public class Action {
	private double skill;
	private double weight;

	public Action(double skill, double weight) {
		this.skill = skill;
		this.weight = weight;
	}

	public double getSkill() {
		return skill;
	}

	public double getWeight() {
		return weight;
	}

	/* Box-Muller algorithm for generating uniformly distributed probabs. */
	public static double boxMullerGen() {
		double r, x, y;

		do {
			x = 2.0 * Math.random() - 1.0;
			y = 2.0 * Math.random() - 1.0;
			r = x*x + y*y;
		} while (r > 1 || r == 0);

		double z = x * Math.sqrt(-2.0 * Math.log(r) / r);

		return z / Math.PI;

	}
	
	public String toString() {
		return "[" + skill + ";" + weight + "]";
	}
}
