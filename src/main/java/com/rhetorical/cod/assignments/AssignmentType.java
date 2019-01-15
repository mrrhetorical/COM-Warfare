package com.rhetorical.cod.assignments;

import com.rhetorical.cod.files.AssignmentFile;

public enum AssignmentType {

	KILLS(1), PLAY_MODE(20), WIN_GAME(50), WIN_GAME_MODE(75);

	private int baseReward;

	AssignmentType(int baseReward) {
		this.baseReward = baseReward;
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

}
