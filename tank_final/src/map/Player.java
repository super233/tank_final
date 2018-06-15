package map;

import net.sf.json.JSONObject;

public class Player extends Point {
	private int id;
	private int team;
	private int superBullet;
	private Point goal; // player的目标点

	public Player(JSONObject object) {
		super(object.getInt("x"), object.getInt("y"));
		this.id = object.getInt("id");
		this.team = object.getInt("team");
		this.superBullet = object.getInt("super_bullet");
		System.out.printf("player id %d team %d x %d, y %d, super bullet %d\n", this.id, this.team, getX(), getY(),
				this.superBullet);
	}

	public int getId() {
		return this.id;
	}

	public int getTeam() {
		return this.team;
	}

	public int getSuperBullet() {
		return superBullet;
	}

	public void setGoal(Point goal) {
		this.goal = goal;
	}

	public Point getGoal() {
		return goal;
	}

	@Override
	public String toString() {
		return "player(" + getY() + ", " + getX() + ")";
	}
}
