package com.rhetorical.cod.progression;

public class RankPerks {
	public final String name;
	private int killCredits;
	private double killExperience;
	private int levelCredits;

	public RankPerks(String name, int killCredits, double killExperience, int levelCredits) {
		this.name = name;
		setKillCredits(killCredits);
		setKillExperience(killExperience);
		setLevelCredits(levelCredits);
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

	public void setKillCredits(int value) {
		killCredits = value;
	}

	public void setKillExperience(double value) {
		killExperience = value;
	}

	public void setLevelCredits(int value) {
		levelCredits = value;
	}
}
