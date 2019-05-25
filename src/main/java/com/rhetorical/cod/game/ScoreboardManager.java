package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.progression.StatHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
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

		objective.setDisplayName(Main.codPrefix);

		ScoreboardMapping mapping = getMapping(p);

		mapping.updateKills(StatHandler.getKills(p.getName()));
		mapping.updateDeaths(StatHandler.getDeaths(p.getName()));
		mapping.updateLevel(Main.progressionManager.getLevel(p));
		mapping.updatePrestige(Main.progressionManager.getPrestigeLevel(p));
		mapping.updateTime(time);

		Score kills = objective.getScore(mapping.getKills()),
				deaths = objective.getScore(mapping.getDeaths()),
				credits = objective.getScore(mapping.getCredits()),
				level = objective.getScore(mapping.getLevel()),
				prestige = objective.getScore(mapping.getPrestige()),
				tScore = objective.getScore(mapping.getTime()),
				space1 = objective.getScore(" "),
				space2 = objective.getScore("  "),
				space3 = objective.getScore("   ");

		tScore.setScore(8);
		space1.setScore(7);
		level.setScore(6);
		prestige.setScore(5);
		space2.setScore(4);
		kills.setScore(3);
		deaths.setScore(2);
		space3.setScore(1);
		credits.setScore(0);

		p.setScoreboard(getScoreboard(p));
	}

	void updateLobbyBoard(Player p, String time) {

		Objective objective = getLobbyBoard(p);

		ScoreboardMapping mapping = getMapping(p);

		getScoreboard(p).resetScores(mapping.getCredits());
		getScoreboard(p).resetScores(mapping.getTime());
		getScoreboard(p).resetScores(mapping.getLevel());
		getScoreboard(p).resetScores(mapping.getPrestige());

		mapping.updateCredits(CreditManager.getCredits(p));
		mapping.updateTime(time);
		mapping.updateLevel(Main.progressionManager.getLevel(p));
		mapping.updatePrestige(Main.progressionManager.getPrestigeLevel(p));

		Score credits = objective.getScore(mapping.getCredits()),
				sTime = objective.getScore(mapping.getTime()),
				level = objective.getScore(mapping.getLevel()),
				prestige = objective.getScore(mapping.getPrestige());

		credits.setScore(0);
		sTime.setScore(8);
		level.setScore(6);
		prestige.setScore(5);
	}

	void setupGameBoard(Player p, String time) {

		Objective objective = getScoreboard(p).registerNewObjective("game", "dummy");
		gameObjective.put(p, objective);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		CodScore score = getGame().getScore(p);

		objective.setDisplayName(Main.codPrefix);

		ScoreboardMapping mapping = getMapping(p);

		mapping.updateKills(score.getKills());
		mapping.updateDeaths(score.getDeaths());
		mapping.updateStreak(score.getKillstreak());
		mapping.updateTime(time);
		mapping.updateCredits(CreditManager.getCredits(p));

		Score kills = objective.getScore(mapping.getKills()),
				deaths = objective.getScore(mapping.getDeaths()),
				streak = objective.getScore(mapping.getStreak()),
				credits = objective.getScore(mapping.getCredits()),
				tScore = objective.getScore(mapping.getTime()),
				gScore = objective.getScore(mapping.getScore()),
				space1 = objective.getScore(" "),
				space2 = objective.getScore("  "),
				space3 = objective.getScore("   ");

		tScore.setScore(8);
		space1.setScore(7);
		kills.setScore(6);
		deaths.setScore(5);
		space2.setScore(4);
		gScore.setScore(3);
		streak.setScore(2);
		space3.setScore(1);
		credits.setScore(0);

		p.setScoreboard(getScoreboard(p));
	}

	void updateGameScoreBoard(Player p, String time) {

		CodScore score = getGame().getScore(p);

		Objective objective = getGameBoard(p);

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

		Score tScore = objective.getScore(mapping.getTime()),
				kills = objective.getScore(mapping.getKills()),
				deaths = objective.getScore(mapping.getDeaths()),
				gScore = objective.getScore(mapping.getScore()),
				streak = objective.getScore(mapping.getStreak()),
				credits = objective.getScore(mapping.getCredits());

		tScore.setScore(8);
		kills.setScore(6);
		deaths.setScore(5);
		gScore.setScore(3);
		streak.setScore(2);
		credits.setScore(0);

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
