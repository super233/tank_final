package map;

import net.sf.json.JSONObject;

public class Coin extends Point {
	private int point;

	public Coin(JSONObject object) {
		super(object.getInt("x"), object.getInt("y"));
		this.point = object.getInt("point");
		System.out.printf("star x %d, y %d\n point %d\n", getX(), getY(), this.point);
	}
}
