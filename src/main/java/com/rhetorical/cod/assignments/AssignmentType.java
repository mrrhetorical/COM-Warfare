package com.rhetorical.cod.assignments;

import com.rhetorical.cod.files.AssignmentFile;
import com.rhetorical.cod.lang.Lang;

public enum AssignmentType {

	KILLS(1, Lang.ASSIGNMENT_KILL_PLAYERS.getMessage()),
	PLAY_MODE(20, Lang.ASSIGNMENT_PLAY_MATCH.getMessage()),
	WIN_GAME(50, Lang.ASSIGNMENT_WIN_MATCH.getMessage()),
	WIN_GAME_MODE(75, Lang.ASSIGNMENT_WIN_MODE.getMessage());

	private String representation;

	private int baseReward;

	AssignmentType(int baseReward, String msg) {
		this.baseReward = baseReward;
		representation = msg;
	}

	static void loadBaseRewards() {
		for (AssignmentType obj : AssignmentType.values()) {
			if (AssignmentFile.getData().contains("Assignment.Type." + obj.toString())) {
				obj.baseReward = AssignmentFile.getData().getInt("Assignment.Type." + obj.toString() + ".baseReward");
			} else {
				AssignmentFile.getData().set("Assignment.Type." + obj.toString() + ".baseReward", obj.baseReward);
				AssignmentFile.saveData();
			}
		}
	}

	public int getBaseReward() {
		return baseReward;
	}

	public String getRepresentation() {
		return representation;
	}

}
