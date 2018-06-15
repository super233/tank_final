package map;

import net.sf.json.JSONObject;

public class Score {
	private int id;
	private int point;
	private int life;
	
	public Score(JSONObject object) {
		this.id = object.getInt("id");
		this.point = object.getInt("point");
		this.life = object.getInt("remain_life");
		System.out.printf("team id %d point %d life %d\n",
				this.id, this.point, this.life);
	}
}
