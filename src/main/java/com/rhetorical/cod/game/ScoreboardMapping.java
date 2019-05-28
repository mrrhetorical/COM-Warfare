package com.rhetorical.cod.game;

import com.rhetorical.cod.lang.Lang;
import org.bukkit.entity.Player;

class ScoreboardMapping {

	private final Player owner;

	private String kills, deaths, streak, credits, level, prestige, time, score;

	ScoreboardMapping(Player p) {
		owner = p;
		updateKills(0);
		updateDeaths(0);
		updateStreak(0);
		updateCredits(0);
		updateLevel(0);
		updatePrestige(0);
		updateTime("0:00");
		updateScore(0);
	}

	Player getOwner() {
		return owner;
	}

	String getKills() {
		return kills;
	}

	public void updateKills(int amount) {
		setKills(String.format(Lang.SCOREBOARD_KILLS.getMessage(), amount));
	}

	private void setKills(String kills) {
		this.kills = kills;
	}

	String getDeaths() {
		return deaths;
	}

	void updateDeaths(int amount) {
		setDeaths(String.format(Lang.SCOREBOARD_DEATHS.getMessage(), amount));
	}

	private void setDeaths(String deaths) {
		this.deaths = deaths;
	}

	String getStreak() {
		return streak;
	}

	void updateStreak(int amount) {
		setStreak(String.format(Lang.SCOREBOARD_STREAK.getMessage(), amount));
	}

	private void setStreak(String streak) {
		this.streak = streak;
	}

	String getCredits() {
		return credits;
	}

	void updateCredits(int amount) {
		setCredits(String.format(Lang.SCOREBOARD_CREDITS.getMessage(), amount));
	}

	private void setCredits(String credits) {
		this.credits = credits;
	}

	String getLevel() {
		return level;
	}

	void updateLevel(int amount) {
		setLevel(String.format(Lang.SCOREBOARD_LEVEL.getMessage(), amount));
	}

	private void setLevel(String level) {
		this.level = level;
	}

	String getPrestige() {
		return prestige;
	}

	void updatePrestige(int amount) {
		setPrestige(String.format(Lang.SCOREBOARD_PRESTIGE.getMessage(), amount));
	}

	private void setPrestige(String prestige) {
		this.prestige = prestige;
	}

	String getTime() {
		return time;
	}

	void updateTime(String time) {
		setTime(String.format(Lang.SCOREBOARD_TIME.getMessage(), time));
	}

	private void setTime(String time) {
		this.time = time;
	}

	String getScore() {
		return score;
	}

	void updateScore(int amount) {
		setScore(String.format(Lang.SCOREBOARD_SCORE.getMessage(), amount));
	}

	private void setScore(String score) {
		this.score = score;
	}
}