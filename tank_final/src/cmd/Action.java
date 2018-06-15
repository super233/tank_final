package cmd;

import java.util.List;
import java.util.ArrayList;

public class Action {
	private int team;
	private int player_id;
	private int bullet_type;
	private List<String> fire = new ArrayList<String>();
	private List<String> move = new ArrayList<String>();

	public Action(int team, int player, int bullet_type, String move, String fire) {
		this.team = team;
		this.player_id = player;
		this.bullet_type = bullet_type;
		if (move != null && !move.isEmpty()) {
			this.move.add(move);
		}
		this.fire.add(fire);
	}

	public int getTeam() {
		return team;
	}

	public void setTeam(int team) {
		this.team = team;
	}

	public int getPlayer_id() {
		return player_id;
	}

	public void setPlayer_id(int player_id) {
		this.player_id = player_id;
	}

	public int getBullet_type() {
		return bullet_type;
	}

	public void setBullet_type(int bullet_type) {
		this.bullet_type = bullet_type;
	}

	public List<String> getFire() {
		return fire;
	}

	public void setFire(List<String> fire) {
		this.fire = fire;
	}

	public List<String> getMove() {
		return move;
	}

	public void setMove(List<String> move) {
		this.move = move;
	}
}