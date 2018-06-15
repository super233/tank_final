package map;

import net.sf.json.JSONObject;

public class Bullet {
	private int type;
	private int team;
	private int x;
	private int y;
	private String direction;

	public Bullet(JSONObject object) {
		this.type = object.getInt("type");
		this.team = object.getInt("team");
		this.x = object.getInt("x");
		this.y = object.getInt("y");
		this.direction = object.getString("direction");

		System.out.printf("bullet type %d, team %d, x %d, y %d, direction %s\n", this.type, this.team, this.x, this.y,
				this.direction);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getDirection() {
		return direction;
	}
}
