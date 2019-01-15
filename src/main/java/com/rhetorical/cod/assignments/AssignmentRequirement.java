package com.rhetorical.cod.assignments;

import com.rhetorical.cod.game.Gamemode;

public class AssignmentRequirement {

	private AssignmentType assignmentType;
	private int reqAmt;
	private Gamemode reqMode;

	public AssignmentRequirement(AssignmentType type, int required, Gamemode requiredMode) {
		assignmentType = type;
		reqAmt = required;
		reqMode = requiredMode;
	}

	public AssignmentType getAssignmentType() {
		return assignmentType;
	}

	public int getRequired() {
		return reqAmt;
	}

	public Gamemode getReqMode() {
		return reqMode;
	}
}
