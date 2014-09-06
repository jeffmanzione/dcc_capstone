package world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class CellGroup {
	
	public final String name;
	public final int[][] coors;
	
	
	public static CellGroup NONE;
	
	
	public static CellGroup createCellGroup(String name, int[][] coors) {
		CellGroup group = new CellGroup(name, coors);

		if (CELL_GROUPS == null) {
			CELL_GROUPS = new HashMap<>();
		}
		
		CELL_GROUPS.put(group.name, group);
	
		return group;
	}
	
	private CellGroup(String name, int[][] coors) {
		this.name = name;
		this.coors = coors;
	}
	
	
	public static Map<String, CellGroup> CELL_GROUPS;
	

	
	public static CellGroup getCellGroup(String name) {
		return CELL_GROUPS.get(name);
	}
	
	public CellGroup rotateLeft() {
		int[][] newCoors = new int[coors.length][];
		for (int index = 0; index < coors.length; index++) {
			int oldQ = coors[index][0];
			int oldR = coors[index][1];
			
			int oldX = oldQ;
			int oldZ = oldR;
			int oldY = -oldX - oldZ;
			
			int x = -oldY;
			//int y = -oldZ;
			int z = -oldX;
			
			int q = x;
			int r = z;
			
			newCoors[index] = new int[]{q, r};
			
		}
		return new CellGroup(this.name, newCoors);
	}
	
	public CellGroup rotateRight() {
		int[][] newCoors = new int[coors.length][];
		for (int index = 0; index < coors.length; index++) {
			int oldQ = coors[index][0];
			int oldR = coors[index][1];
			
			int oldX = oldQ;
			int oldZ = oldR;
			int oldY = -oldX - oldZ;
			
			int x = -oldZ;
			//int y = -oldX;
			int z = -oldY;
			
			int q = x;
			int r = z;
			
			newCoors[index] = new int[]{q, r};
			
		}
		
		return new CellGroup(this.name, newCoors);
	}
	
	public String toString() {
		return this.name;
	}
	
	public static Collection<CellGroup> getAll() {
		return CELL_GROUPS.values();
	}
	
	public static void init() {
		CELL_GROUPS = new HashMap<>();
		
		
		NONE = createCellGroup("None", new int[][] {});

	}
}
