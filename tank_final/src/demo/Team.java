package demo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Team {
	private int id;
	private int[] players;

	public Team(JSONObject team) {
		this.id = team.getInt("id");
		System.out.printf("team id %d\n", this.id);
		
		
		JSONArray array = team.getJSONArray("players");
		this.players = new int[array.size()];
		for (int i = 0; i < players.length; i++) {
			players[i] = array.getInt(i);
			System.out.printf("player id %d\n", players[i]);
		}
	}
}
