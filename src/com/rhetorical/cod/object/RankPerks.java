package com.rhetorical.cod.object;

public class RankPerks {
	public final String name;
	public int killCredits;
	public double killExperience;
	public int levelCredits;
	
	public RankPerks(String name, int killCredits, double killExperience, int levelCredits) {
		this.name = name;
		this.killCredits = killCredits;
		this.killExperience = killExperience;
		this.levelCredits = levelCredits;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getKillCredits() {
		return this.killCredits;
	}
	
	public double getKillExperience() {
		return this.killExperience;
	}
	
	public int getLevelCredits() {
		return this.levelCredits;
	}
}
