package com.rhetorical.cod.object;

public class RankPerks {
	public final String name;
	private int killCredits;
	private double killExperience;
	private int levelCredits;

	public RankPerks(String name, int killCredits, double killExperience, int levelCredits) {
		this.name = name;
		this.killCredits = killCredits;
		this.killExperience = killExperience;
		this.levelCredits = levelCredits;
	}

	public String getName() {
		return this.name;
	}

	int getKillCredits() {
		return this.killCredits;
	}

	double getKillExperience() {
		return this.killExperience;
	}

	public int getLevelCredits() {
		return this.levelCredits;
	}
}
