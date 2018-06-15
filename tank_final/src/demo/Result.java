package demo;

import net.sf.json.JSONObject;

public class Result {
	private int id;
	private int point;

	public Result(JSONObject object) {
		this.id = object.getInt("id");
		this.point = object.getInt("point");
		System.out.printf("team id %d point %d\n", this.id, this.point);
	}
}
