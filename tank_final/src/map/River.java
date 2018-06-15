package map;

import net.sf.json.JSONObject;

public class River {
	private int x;
	private int y;
	
	public River(JSONObject object) {
		this.x = object.getInt("x");
		this.y = object.getInt("y");
		System.out.printf("river x %d, y %d\n", this.x, this.y);
	}
}
