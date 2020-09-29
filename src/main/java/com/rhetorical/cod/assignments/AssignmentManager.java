package com.rhetorical.cod.assignments;

import com.google.gson.JsonObject;
import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.AssignmentFile;
import com.rhetorical.cod.game.CodMap;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.game.Gamemode;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.sql.SQLDriver;
import org.bukkit.entity.Player;

import java.util.*;

public class AssignmentManager {

	private static AssignmentManager instance;

	private Map<Player, List<Assignment>> playerAssignments = new HashMap<>();

	private AssignmentManager() {
		if (instance != null)
			return;

		instance = this;
		AssignmentType.loadBaseRewards();
	}

	public static AssignmentManager getInstance() {
		return instance != null ? instance : new AssignmentManager();
	}

	public void load(Player p) {
		List<Assignment> assignments = new ArrayList<>();

		if (ComWarfare.MySQL) {

			for (int i = 1; i <= 3; i++) {
				JsonObject jo = SQLDriver.getInstance().getAssignments(p.getUniqueId(), i);
				if (jo.get("assignmentType") != null) {
					AssignmentType type = AssignmentType.valueOf(jo.get("assignmentType").getAsString());
					Gamemode mode = Gamemode.valueOf(jo.get("requiredMode").getAsString());
					int amt = jo.get("amount").getAsInt();
					int progress = jo.get("progress").getAsInt();
					AssignmentRequirement requirement = new AssignmentRequirement(type, amt, mode);
					Assignment assignment = new Assignment(p, requirement, progress, requirement.getAssignmentType().getBaseReward() * requirement.getRequired());
					if (!assignments.contains(assignment)) {
						assignments.add(assignment);
					}
				}
			}

		} else {
			for (int i = 0; AssignmentFile.getData().contains("Players." + p.getName() + ".Assignments." + i); i++) {
				String base = "Players." + p.getName() + ".Assignments." + i + ".";

				AssignmentType type = AssignmentType.valueOf(AssignmentFile.getData().getString(base + "assignmentType"));
				Gamemode mode = Gamemode.valueOf(AssignmentFile.getData().getString(base + "requiredMode"));
				int amt = AssignmentFile.getData().getInt(base + "amount");

				int progress = AssignmentFile.getData().getInt(base + "progress");

				AssignmentRequirement requirement = new AssignmentRequirement(type, amt, mode);

				Assignment assignment = new Assignment(p, requirement, progress, requirement.getAssignmentType().getBaseReward() * requirement.getRequired());
				if (!assignments.contains(assignment)) {
					assignments.add(assignment);
				}
			}
		}

		while (assignments.size() < 3) {
			Assignment assignment = generateAssignment(p);
			assignments.add(assignment);
		}

		playerAssignments.put(p, assignments);
		save(p);

		Assignment[] assignmentArray = new Assignment[assignments.size()];

		for (int i = 0; i < assignments.size(); i++) {
			assignmentArray[i] = assignments.get(i);
		}

		for (int i = 0; i < assignmentArray.length; i++) {
			Assignment a = assignmentArray[i];
			a.checkProgress();
		}
	}

	public void save(Player p) {
		List<Assignment> assignments = playerAssignments.get(p);
		if (ComWarfare.MySQL) {
			for (int i = 1; i <= 3; i++) {
				JsonObject jo = new JsonObject();
				Assignment assignment = assignments.get(i);
				jo.addProperty("assignmentType", assignment.getRequirement().getAssignmentType().toString());
				jo.addProperty("requiredMode", assignment.getRequirement().getReqMode().toString());
				jo.addProperty("amount", assignment.getRequirement().getRequired());
				jo.addProperty("progress", assignment.getProgress());
				SQLDriver.getInstance().setAssignments(p.getUniqueId(), jo, i);
			}
		} else {
			for (int i = 0; i < assignments.size(); i++) {
				String base = "Players." + p.getName() + ".Assignments." + i + ".";

				Assignment assignment = assignments.get(i);

				AssignmentFile.getData().set(base + "assignmentType", assignment.getRequirement().getAssignmentType().toString());
				AssignmentFile.getData().set(base + "requiredMode", assignment.getRequirement().getReqMode().toString());
				AssignmentFile.getData().set(base + "amount", assignment.getRequirement().getRequired());
				AssignmentFile.getData().set(base + "progress", assignment.getProgress());
			}
			AssignmentFile.saveData();
		}
	}

	private Assignment generateAssignment(Player p) {
		int index = (new Random()).nextInt(AssignmentType.values().length);
		AssignmentType type = AssignmentType.values()[index];

		Set<Gamemode> availableModes = new HashSet<>();
		for (CodMap map : GameManager.getAddedMaps())
			availableModes.addAll(map.getAvailableGamemodes());

		Gamemode mode;
		if (type == AssignmentType.PLAY_MODE || type == AssignmentType.WIN_GAME_MODE) {
			mode = !availableModes.isEmpty() ? (Gamemode) availableModes.toArray()[(new Random()).nextInt(availableModes.size())] : Gamemode.ANY;
		} else
			mode = Gamemode.ANY;

		int required;

		if (type != AssignmentType.KILLS)
			required = (new Random()).nextInt(4) + 1;
		else {
			required = (new Random()).nextInt(45) + 5;
			while (required % 5 != 0) {
				required++;
			}
		}
		AssignmentRequirement requirement = new AssignmentRequirement(type, required, mode);
		int reward = type.getBaseReward() * required;

		return new Assignment(p, requirement, reward);
	}

	public void updateAssignments(Player p, int kills, Gamemode gamemode, boolean... gameWon) {
		List<Assignment> assignments = playerAssignments.get(p);

		Assignment[] assignmentArray = new Assignment[assignments.size()];

		for (int i = 0; i < assignments.size(); i++) {
			assignmentArray[i] = assignments.get(i);
		}

		for (Assignment assignment : assignmentArray) {
			if (assignment.getRequirement().getAssignmentType() == AssignmentType.KILLS) {
				assignment.addProgress(kills, gamemode);
			} else if (assignment.getRequirement().getAssignmentType() == AssignmentType.WIN_GAME
					|| assignment.getRequirement().getAssignmentType() == AssignmentType.WIN_GAME_MODE) {
				if (gameWon.length < 1)
					continue;
				assignment.addProgress(gameWon[0] ? 1 : 0, gamemode);
			} else if (assignment.getRequirement().getAssignmentType() == AssignmentType.PLAY_MODE) {
				assignment.addProgress(1, gamemode);
			}
		}

		playerAssignments.put(p, assignments);
	}

	public void completeAssignment(Player p, Assignment assignment) {
		if (!playerAssignments.get(p).contains(assignment))
			return;

		if (!assignment.isCompleted())
			return;

		List<Assignment> assignments = playerAssignments.get(p);
		assignments.remove(assignment);

		assignments.add(generateAssignment(p));

		save(p);

		playerAssignments.put(p, assignments);

		CreditManager.setCredits(p, CreditManager.getCredits(p) + assignment.getReward());


		ComWarfare.sendMessage(p, Lang.ASSIGNMENT_COMPLETED.getMessage().replace("{amount}", assignment.getReward() + ""), ComWarfare.getLang());
	}

	public List<Assignment> getAssignments(Player p) {
		if (!playerAssignments.containsKey(p))
			load(p);

		return playerAssignments.get(p);
	}

}
