package cmd;

public class Registration {
	private int team_id = 0;
	private String team_name = "";

	public Registration(int team_id, String name) {
		this.team_id = team_id;
		this.team_name = name;
	}

	public String getTeam_name() {
		return team_name;
	}

	public void setTeam_name(String team_name) {
		this.team_name = team_name;
	}

	public int getTeam_id() {
		return team_id;
	}

	public void setTeam_id(int team_id) {
		this.team_id = team_id;
	}
}
