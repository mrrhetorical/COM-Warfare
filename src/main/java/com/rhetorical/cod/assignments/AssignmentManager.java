package com.rhetorical.cod.assignments;

import com.rhetorical.cod.files.AssignmentFile;
import com.rhetorical.cod.game.Gamemode;
import org.bukkit.entity.Player;

import java.util.*;

public class AssignmentManager {

	private Map<Player, List<Assignment>> playerAssignments = new HashMap<>();

	public AssignmentManager() {
		AssignmentType.loadBaseRewards();
	}

	public void load(Player p) {
		List<Assignment> assignments = new ArrayList<>();
		for (int i = 0; AssignmentFile.getData().contains("Players." + p.getName() + ".Assignments." + i); i++) {
			String base = "Players." + p.getName() + ".Assignments." + i + ".";

			AssignmentType type = AssignmentType.valueOf(AssignmentFile.getData().getString(base + "assignmentType"));
			Gamemode mode = Gamemode.valueOf(AssignmentFile.getData().getString(base + "requiredMode"));
			int amt = AssignmentFile.getData().getInt(base + "amount");

			AssignmentRequirement requirement = new AssignmentRequirement(type, amt, mode);

			Assignment assignment = new Assignment(p, requirement, requirement.getAssignmentType().getBaseReward() * requirement.getRequired());
			if (!assignments.contains(assignment)) {
				assignments.add(assignment);
			}
		}

		while (assignments.size() < 3) {
				int index = (new Random()).nextInt(AssignmentType.values().length);
				int mIndex = (new Random()).nextInt(Gamemode.values().length);
				AssignmentType type = AssignmentType.values()[index];
				Gamemode mode;
				if (type == AssignmentType.PLAY_MODE || type == AssignmentType.WIN_GAME_MODE)
					mode = Gamemode.values()[mIndex];
				else
					mode = Gamemode.ANY;

				int required;

				if (type != AssignmentType.KILLS)
					required = (new Random()).nextInt(4);
				else
					required = (new Random()).nextInt(50) + 25;
				AssignmentRequirement requirement = new AssignmentRequirement(type, required, mode);
				int reward = type.getBaseReward() * required;
				Assignment assignment = new Assignment(p, requirement, reward);
				assignments.add(assignment);
		}

		playerAssignments.put(p, assignments);
		save(p);
	}

	private void save(Player p) {
		List<Assignment> assignments = playerAssignments.get(p);
		for (int i = 0; i < assignments.size(); i++) {
			String base = "Players." + p.getName() + ".Assignments." + i + ".";

			Assignment assignment = assignments.get(i);

			AssignmentFile.getData().set(base + "assignmentType", assignment.getRequirement().getAssignmentType().toString());
			AssignmentFile.getData().set(base + "requiredMode", assignment.getRequirement().getReqMode().toString());
			AssignmentFile.getData().set(base + "amount", assignment.getRequirement().getRequired());
		}

		AssignmentFile.saveData();
	}

}
