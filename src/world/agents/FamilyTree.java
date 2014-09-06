package world.agents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class FamilyTree {
	private Map<Agent, List<Agent>> jumps;

	private List<Agent> roots;

	public FamilyTree(List<Agent> roots) {
		jumps = new HashMap<>();
		this.roots = roots;
		
		roots.forEach(a -> jumps.put(a, new ArrayList<Agent>()));
	}

	public void add(Agent agent, Agent parent) {
		if (!jumps.containsKey(agent)) {
			jumps.put(agent, new ArrayList<Agent>());
		}
		
		jumps.get(parent).add(agent);
	}

	public void toFile(File file) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(file);
		
		Stack<Agent> stack = new Stack<>();
		
		stack.addAll(roots);
		
		while (!stack.isEmpty()) {
			Agent a = stack.pop();
			out.println(a.toString());
			
			stack.addAll(jumps.get(a));
		}
	}

}
