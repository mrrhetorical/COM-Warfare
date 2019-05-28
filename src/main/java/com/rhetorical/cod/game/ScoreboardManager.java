package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.progression.StatHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ScoreboardManager {

	private final GameInstance game;

	private Map<Player, Scoreboard> scoreboards = new HashMap<>();
	private Map<Player, Objective> gameObjective = new HashMap<>(),
			lobbyObjective = new HashMap<>();
	private Map<Player, ScoreboardMapping> playerMappings = new HashMap<>();


	ScoreboardManager(GameInstance game) {
		this.game = game;
	}

	void setupLobbyBoard(Player p, String time) {
		Objective objective = getScoreboard(p).registerNewObjective("lobby", "dummy");
		lobbyObjective.put(p, objective);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		objective.setDisplayName(Lang.LOBBY_SCOREBOARD.getMessage());

		ScoreboardMapping mapping = getMapping(p);

		mapping.updateKills(StatHandler.getKills(p.getName()));
		mapping.updateDeaths(StatHandler.getDeaths(p.getName()));
		mapping.updateLevel(Main.progressionManager.getLevel(p));
		mapping.updatePrestige(Main.progressionManager.getPrestigeLevel(p));
		mapping.updateTime(time);

		final List<Team> registered = new ArrayList<>(scoreboards.get(p).getTeams());

		for (Team t : registered) {
			t.unregister();
		}

		Team kills = scoreboards.get(p).registerNewTeam("kills"),
				deaths = scoreboards.get(p).registerNewTeam("deaths"),
				credits = scoreboards.get(p).registerNewTeam("credits"),
				level = scoreboards.get(p).registerNewTeam("level"),
				prestige = scoreboards.get(p).registerNewTeam("prestige"),
				tScore = scoreboards.get(p).registerNewTeam("time");

		kills.addEntry(ChatColor.RED.toString());
		deaths.addEntry(ChatColor.BLUE.toString());
		credits.addEntry(ChatColor.GREEN.toString());
		level.addEntry(ChatColor.BLACK.toString());
		prestige.addEntry(ChatColor.WHITE.toString());
		tScore.addEntry(ChatColor.GRAY.toString());

		Score space1 = objective.getScore("                    "),
				space2 = objective.getScore("  "),
				space3 = objective.getScore("   ");

		objective.getScore(ChatColor.GRAY.toString()).setScore(8);
		space1.setScore(7);
		objective.getScore(ChatColor.BLACK.toString()).setScore(6);
		objective.getScore(ChatColor.WHITE.toString()).setScore(5);
		space2.setScore(4);
		objective.getScore(ChatColor.RED.toString()).setScore(3);
		objective.getScore(ChatColor.BLUE.toString()).setScore(2);
		space3.setScore(1);
		objective.getScore(ChatColor.GREEN.toString()).setScore(0);


		updateLobbyTeams(p, mapping);


		p.setScoreboard(getScoreboard(p));
	}

	private void updateLobbyTeams(Player p, ScoreboardMapping mapping) {
		getScoreboard(p).getTeam("time").setPrefix(mapping.getTime());
		getScoreboard(p).getTeam("kills").setPrefix(mapping.getKills());
		getScoreboard(p).getTeam("deaths").setPrefix(mapping.getDeaths());
		getScoreboard(p).getTeam("credits").setPrefix(mapping.getCredits());
		getScoreboard(p).getTeam("level").setPrefix(mapping.getLevel());
		getScoreboard(p).getTeam("prestige").setPrefix(mapping.getPrestige());
	}

	void updateLobbyBoard(Player p, String time) {

		ScoreboardMapping mapping = getMapping(p);

		mapping.updateCredits(CreditManager.getCredits(p));
		mapping.updateTime(time);
		mapping.updateLevel(Main.progressionManager.getLevel(p));
		mapping.updatePrestige(Main.progressionManager.getPrestigeLevel(p));

		updateLobbyTeams(p, mapping);
	}

	void setupGameBoard(Player p, String time) {

		Objective objective = getScoreboard(p).registerNewObjective("game", "dummy");
		gameObjective.put(p, objective);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		CodScore score = getGame().getScore(p);

		objective.setDisplayName(Lang.GAME_SCOREBOARD.getMessage());

		ScoreboardMapping mapping = getMapping(p);

		mapping.updateKills(score.getKills());
		mapping.updateDeaths(score.getDeaths());
		mapping.updateStreak(score.getKillstreak());
		mapping.updateTime(time);
		mapping.updateCredits(CreditManager.getCredits(p));

		final List<Team> registered = new ArrayList<>(scoreboards.get(p).getTeams());

		for (Team t : registered) {
			t.unregister();
		}


		Team kills = scoreboards.get(p).registerNewTeam("kills"),
				deaths = scoreboards.get(p).registerNewTeam("deaths"),
				credits = scoreboards.get(p).registerNewTeam("credits"),
				streak = scoreboards.get(p).registerNewTeam("streak"),
				gScore = scoreboards.get(p).registerNewTeam("score"),
				tScore = scoreboards.get(p).registerNewTeam("time");

		Score space1 = objective.getScore("                    "),
				space2 = objective.getScore("  "),
				space3 = objective.getScore("   ");


		kills.addEntry(ChatColor.RED.toString());
		deaths.addEntry(ChatColor.BLUE.toString());
		credits.addEntry(ChatColor.GREEN.toString());
		streak.addEntry(ChatColor.BLACK.toString());
		gScore.addEntry(ChatColor.WHITE.toString());
		tScore.addEntry(ChatColor.GRAY.toString());

		objective.getScore(ChatColor.GRAY.toString()).setScore(8);
		space1.setScore(7);
		objective.getScore(ChatColor.RED.toString()).setScore(6);
		objective.getScore(ChatColor.BLUE.toString()).setScore(5);
		space2.setScore(4);
		objective.getScore(ChatColor.BLACK.toString()).setScore(3);
		objective.getScore(ChatColor.WHITE.toString()).setScore(2);
		space3.setScore(1);
		objective.getScore(ChatColor.GREEN.toString()).setScore(0);

		updateGameTeams(p, mapping);

		p.setScoreboard(getScoreboard(p));
	}

	private void updateGameTeams(Player p, ScoreboardMapping mapping) {
		getScoreboard(p).getTeam("time").setPrefix(mapping.getTime());
		getScoreboard(p).getTeam("kills").setPrefix(mapping.getKills());
		getScoreboard(p).getTeam("deaths").setPrefix(mapping.getDeaths());
		getScoreboard(p).getTeam("credits").setPrefix(mapping.getCredits());
		getScoreboard(p).getTeam("streak").setPrefix(mapping.getStreak());
		getScoreboard(p).getTeam("score").setPrefix(mapping.getScore());
	}

	void updateGameScoreBoard(Player p, String time) {

		CodScore score = getGame().getScore(p);

		ScoreboardMapping mapping = getMapping(p);

		getScoreboard(p).resetScores(mapping.getTime());
		getScoreboard(p).resetScores(mapping.getKills());
		getScoreboard(p).resetScores(mapping.getDeaths());
		getScoreboard(p).resetScores(mapping.getStreak());
		getScoreboard(p).resetScores(mapping.getCredits());
		getScoreboard(p).resetScores(mapping.getScore());

		mapping.updateTime(time);
		mapping.updateKills(score.getKills());
		mapping.updateStreak(score.getKillstreak());
		mapping.updateDeaths(score.getDeaths());
		mapping.updateScore((int) score.getScore());
		mapping.updateCredits(CreditManager.getCredits(p));

		updateGameTeams(p, mapping);
	}

	void clearScoreboards(Player p) {
		scoreboards.remove(p);
		gameObjective.remove(p);
		lobbyObjective.remove(p);
		playerMappings.remove(p);
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	private Objective getLobbyBoard(Player p) {
		return lobbyObjective.get(p);
	}

	private Objective getGameBoard(Player p) {
		return gameObjective.get(p);
	}

	private GameInstance getGame() {
		return game;
	}

	private Scoreboard getScoreboard(Player p) {
		return scoreboards.computeIfAbsent(p, k -> Bukkit.getScoreboardManager().getNewScoreboard());
	}

	private ScoreboardMapping getMapping(Player p) {
		return playerMappings.computeIfAbsent(p, k -> new ScoreboardMapping(p));
	}
}
