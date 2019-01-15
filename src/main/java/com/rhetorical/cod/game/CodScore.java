package com.rhetorical.cod.game;

import org.bukkit.entity.Player;

import com.rhetorical.cod.progression.StatHandler;

public class CodScore {

	final Player owner;

	private int deaths;
	private int kills;
	private int killStreak;
	private double score;

	public CodScore(Player p) {
		this.owner = p;
		this.deaths = 0;
		this.kills = 0;
		this.killStreak = 0;
		this.score = 0D;
	}

	public Player getOwner() {
		return this.owner;
	}
	
	public int getDeaths() {
		return this.deaths;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public int getKills() {
		return this.kills;
	}

	public void addKill() {
		this.kills++;
		StatHandler.addKill(owner);
	}
	
	public void setKills(int kills) {
		this.kills = kills;
	}
	
	public int getKillstreak() {
		return this.killStreak;
	}

	public void resetKillstreak() {
		this.killStreak = 0;
	}

	public void addKillstreak() {
		this.killStreak++;
	}
	
	public double getScore() {
		return this.score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public void addScore(double toAdd) {
		this.score += toAdd;
	}
	
	public void removeScore(double toRemove) {
		this.score -= toRemove;
	}
}
