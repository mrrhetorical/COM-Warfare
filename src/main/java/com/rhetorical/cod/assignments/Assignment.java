package com.rhetorical.cod.assignments;

import org.bukkit.entity.Player;

public class Assignment {

	private final Player assignee;

	private final AssignmentRequirement assignmentRequirement;
	private final int reward;

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

	public AssignmentRequirement getRequirement() {
		return assignmentRequirement;
	}
}
