package com.rhetorical.cod.assignments;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.AssignmentFile;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.sql.SQLDriver;

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
			if (ComWarfare.MySQL) {
				obj.baseReward = SQLDriver.getInstance().getAssignmentTypes("KILLS");
				obj.baseReward = SQLDriver.getInstance().getAssignmentTypes("PLAY_MODE");
				obj.baseReward = SQLDriver.getInstance().getAssignmentTypes("WIN_GAME");
				obj.baseReward = SQLDriver.getInstance().getAssignmentTypes("WIN_GAME_MODE");
			} else {
				if (AssignmentFile.getData().contains("Assignment.Type." + obj)) {
					obj.baseReward = AssignmentFile.getData().getInt("Assignment.Type." + obj + ".baseReward");
				} else {
					AssignmentFile.getData().set("Assignment.Type." + obj + ".baseReward", obj.baseReward);
					AssignmentFile.saveData();
				}
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

