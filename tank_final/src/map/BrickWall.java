package map;

import net.sf.json.JSONObject;

public class BrickWall {
	private int x;
	private int y;
	
	public BrickWall(JSONObject object) {
		this.x = object.getInt("x");
		this.y = object.getInt("y");
		System.out.printf("brick wall x %d, y %d\n", this.x, this.y);
	}
}
