package map;

import net.sf.json.JSONObject;

public class IronWall {
	private int x;
	private int y;
	
	public IronWall(JSONObject object) {
		this.x = object.getInt("x");
		this.y = object.getInt("y");
		System.out.printf("iron wall x %d, y %d\n", this.x, this.y);
	}
}
