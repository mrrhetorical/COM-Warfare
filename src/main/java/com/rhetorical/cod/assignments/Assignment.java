package com.rhetorical.cod.assignments;

import com.rhetorical.cod.game.Gamemode;
import org.bukkit.entity.Player;

public class Assignment {

	private final Player assignee;

	private final AssignmentRequirement assignmentRequirement;
	private final int reward;
	private int progress = 0;
	private boolean completed = false;

	public Assignment(Player assignee, AssignmentRequirement assignmentRequirement, int progress, int reward) {
		this.assignee = assignee;
		this.assignmentRequirement = assignmentRequirement;
		addProgress(progress, Gamemode.ANY);
		this.reward = reward;
	}

	public Assignment (Player assignee, AssignmentRequirement assignmentRequirement, int reward) {
		this.assignee = assignee;
		this.assignmentRequirement = assignmentRequirement;
		this.reward = reward;
	}

	public Player getAssignee() {
		return assignee;
	}

	public int getReward() {
		return reward;
	}

	public int getProgress() {
		return progress;
	}

	public void addProgress(int amount, Gamemode currentMode) {
		if (getRequirement().getReqMode() == currentMode
				|| getRequirement().getReqMode() == Gamemode.ANY) {
			progress += amount;
		}

		if (progress > getRequirement().getRequired()) {
			completed = true;
		}
	}

	public boolean isCompleted() {
		return completed;
	}

	public AssignmentRequirement getRequirement() {
		return assignmentRequirement;
	}
}
