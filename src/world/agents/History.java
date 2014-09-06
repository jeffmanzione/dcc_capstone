package world.agents;

public abstract class History {
	protected Agent a = null;
	
	private static History singleton = new AsexualHistory();
	
	protected History() { }
	
	public static History createFromStart(Agent start) {
		return singleton.create(start);
	}
	
	public History create(Agent start) {
		if (start.parents == null) {
			OriginHistory h = new OriginHistory();
			h.a = start;
			return h;
		}
		
		switch(start.parents.length) {
		case 0:
			AsexualHistory h = new AsexualHistory();
			h.a = start;
			
			return h;
		case 1:
			AsexualHistory h1 = new AsexualHistory();
			h1.a = start;
			h1.p = create(start.parents[0]);
			
			return h1;
		case 2:
			SexualHistory h2 = new SexualHistory();
			h2.a = start;
			h2.p1 = create(start.parents[0]);
			h2.p2 = create(start.parents[1]);
			
			return h2;
		default:
			return null;
		}
		
	}
	
	public abstract String toString();
}

class OriginHistory extends History {
	public String toString() {
		return "" + a.birthday();
	}
	
}

class AsexualHistory extends History {
	protected History p;

	public String toString() {
		return "(A " + a.birthday() + ": " + p + ")";
	}
}

class SexualHistory extends History {
	protected History p1, p2;

	@Override
	public String toString() {
		return "(S " + a.birthday() + ": " + p1 + ", " + p2 + ")";
	}
}