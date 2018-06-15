package map;

import net.sf.json.JSONObject;

public class Star {
	private int x;
	private int y;
	
	public Star(JSONObject object) {
		this.x = object.getInt("x");
		this.y = object.getInt("y");
		System.out.printf("star x %d, y %d\n", this.x, this.y);
	}
}
