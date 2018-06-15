package cmd;

import java.util.List;

import cmd.Action;

public class RoundAction {
	private int round_id;
	private List<Action> actions;

	public RoundAction(int round_id, List<Action> actions) {
		this.round_id = round_id;
		this.actions = actions;
	}

	public int getRound_id() {
		return round_id;
	}

	public void setRound_id(int round_id) {
		this.round_id = round_id;
	}

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}
}
