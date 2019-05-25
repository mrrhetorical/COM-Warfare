package com.rhetorical.cod.game;

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
		setKills(String.format("Kills: %s", amount));
	}

	private void setKills(String kills) {
		this.kills = kills;
	}

	String getDeaths() {
		return deaths;
	}

	void updateDeaths(int amount) {
		setDeaths(String.format("Deaths: %s", amount));
	}

	private void setDeaths(String deaths) {
		this.deaths = deaths;
	}

	String getStreak() {
		return streak;
	}

	void updateStreak(int amount) {
		setStreak(String.format("Streak: %s", amount));
	}

	private void setStreak(String streak) {
		this.streak = streak;
	}

	String getCredits() {
		return credits;
	}

	void updateCredits(int amount) {
		setCredits(String.format("Credits: %s", amount));
	}

	private void setCredits(String credits) {
		this.credits = credits;
	}

	String getLevel() {
		return level;
	}

	void updateLevel(int amount) {
		setLevel(String.format("Level: %s", amount));
	}

	private void setLevel(String level) {
		this.level = level;
	}

	String getPrestige() {
		return prestige;
	}

	void updatePrestige(int amount) {
		setPrestige(String.format("Prestige: %s", amount));
	}

	private void setPrestige(String prestige) {
		this.prestige = prestige;
	}

	String getTime() {
		return time;
	}

	void updateTime(String time) {
		setTime(String.format("%s", time));
	}

	private void setTime(String time) {
		this.time = time;
	}

	String getScore() {
		return score;
	}

	void updateScore(int amount) {
		setScore(String.format("Score: %s", amount));
	}

	private void setScore(String score) {
		this.score = score;
	}
}