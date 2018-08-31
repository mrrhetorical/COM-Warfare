package com.rhetorical.cod;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.rhetorical.cod.files.ProgressionFile;

public class ProgressionManager {

	private HashMap<Player, Integer> prestigeLevel = new HashMap<Player, Integer>();
	// Start from 0

	private HashMap<Player, Integer> level = new HashMap<Player, Integer>();
	// Start from 1

	private HashMap<Player, Double> experience = new HashMap<Player, Double>();
	// Start from 0

	private final int maxLevel;
	private final int maxPrestigeLevel;

	ProgressionManager() {

		for (Player p : Bukkit.getOnlinePlayers()) {
			this.loadData(p);
		}

		int maxLevelFromConfig = Main.getPlugin().getConfig().getInt("maxLevel");
		int maxPrestigeLevelFromConfig = Main.getPlugin().getConfig().getInt("maxPrestigeLevel");

		if (maxLevelFromConfig <= 0) {
			this.maxLevel = 55;
			Main.sendMessage(Main.cs, 
					Main.codPrefix + "\u00A7cMax level set to 55 by default! You must set a maximum level of at least 1!", Main.lang);
		} else {
			this.maxLevel = maxLevelFromConfig;
		}

		if (maxPrestigeLevelFromConfig <= 0) {
			this.maxPrestigeLevel = 10;
			Main.sendMessage(Main.cs, Main.codPrefix
					+ "\u00A7cMax prestige level set to 10 by default! You must set a maximum level of at least 1!", Main.lang);
		} else {
			this.maxPrestigeLevel = maxPrestigeLevelFromConfig;
		}

	}

	private void setLevel(Player p, int level, boolean showMessage) {
		this.level.put(p, level);

		if (showMessage) {
			p.sendMessage(Main.codPrefix + "\u00A77Congratulations! You just ranked up to level \u00A7e"
					+ Integer.toString(getLevel(p)) + "\u00A7r\u00A77!");
		}

	}

	private void addLevel(Player p) {
		if (!this.level.containsKey(p)) {
			this.level.put(p, 1);
		}

		this.level.put(p, this.level.get(p) + 1);
		p.sendMessage(Main.codPrefix + "\u00A77Congratulations! You just ranked up to level \u00A7e"
				+ Integer.toString(getLevel(p)) + "\u00A7r\u00A77!");

		if (this.getLevel(p) == this.maxLevel) {
			p.sendMessage(Main.codPrefix
					+ "\u00A77Congratulations! You've reached the highest rank! Visit the prestige menu to get your reward!");
		}

		Main.shopManager.checkForNewGuns(p);

	}

	public int getLevel(Player p) {
		if (!this.level.containsKey(p)) {
			this.level.put(p, 1);
		}

		return this.level.get(p);
	}

	public void setPrestigeLevel(Player p, int level, boolean showMessage) {
		this.prestigeLevel.put(p, level);

		if (showMessage) {
			p.sendMessage(Main.codPrefix + "\u00A77Congratulations! You just ranked up to prestige level \u00A7e"
					+ Integer.toString(getLevel(p)) + "\u00A7r\u00A77!");
		}

	}

	public void addPrestigeLevel(Player p) {
		if (!this.prestigeLevel.containsKey(p)) {
			this.prestigeLevel.put(p, 0);
		}

		if (getPrestigeLevel(p) >= maxPrestigeLevel)
			return;

		this.prestigeLevel.put(p, this.prestigeLevel.get(p) + 1);
		p.sendMessage(Main.codPrefix + "\u00A77Congratulations! You just ranked up to prestige level \u00A7e"
				+ Integer.toString(getLevel(p)) + "\u00A7r\u00A77!");
		p.sendMessage(Main.codPrefix + "\u00A77Your rank has been reset!");
		setLevel(p, 1, false);
		return;
	}

	int getPrestigeLevel(Player p) {
		if (!this.prestigeLevel.containsKey(p)) {
			this.prestigeLevel.put(p, 0);
		}

		return this.prestigeLevel.get(p);
	}

	private void setExperience(Player p, double experience) {
		this.experience.put(p, experience);
		update(p);
		StatHandler.addExperience(p, experience - StatHandler.getExperience(p.getName()));
	}

	public void addExperience(Player p, double experience) {
		int level = getLevel(p);
		double requiredExperience = getExperienceForLevel(level);

		this.setExperience(p, getExperience(p) + experience);

		if (this.getExperience(p) >= requiredExperience) {
			this.setExperience(p, 0D);
			if (this.getLevel(p) < this.maxLevel) {
				this.addLevel(p);
			}

		}

		StatHandler.addExperience(p, experience);

		update(p);
	}

	private double getExperience(Player p) {
		if (!this.experience.containsKey(p)) {
			this.experience.put(p, 0D);
		}

		return this.experience.get(p);
	}

	private double getExperienceForLevel(int level) {
		double exp = 0D;

		if (level <= 0) {
			return 0D;
		} else if (level == 1) {
			return 400D;
		} else if (level == 2) {
			return 4000D;
		} else if (level <= 10) {
			exp = 4000 + (600 * (level - 2));
		} else if (level >= 10 && level <= 31) {
			exp = 4000 + (800 * level - 2);
		} else if (level >= 31 && level <= 55) {
			exp = 4000 + (1200 * level - 2);
		} else {
			exp = 4000 + (2000 * level - 2);
		}

		return exp;
	}

	public void update(Player p) {

		try {
			p.setExp((float) (getExperience(p) / getExperienceForLevel(getLevel(p))));
		} catch (Exception e) {
			Main.sendMessage(Main.cs, "\u00A7cThere was an error setting the player's experience level", Main.lang);
		}
	}

	void loadData(Player p) {
		int k = 0;
		while (ProgressionFile.getData().contains("Players." + k)) {

			if (p == Bukkit.getPlayer(ProgressionFile.getData().getString("Players." + k + ".name"))) {
				int playerLevel = ProgressionFile.getData().getInt("Players." + k + ".level");
				double playerExperience = ProgressionFile.getData().getDouble("Players." + k + ".experience");
				int playerPrestigeLevel = ProgressionFile.getData().getInt("Players." + k + ".prestigeLevel");

				this.level.put(p, playerLevel);
				this.experience.put(p, playerExperience);
				this.prestigeLevel.put(p, playerPrestigeLevel);

				return;
			}

			k++;
		}

	}

	public void saveData(Player p) {

		int k = 0;
		for (k = 0; ProgressionFile.getData().contains("Players." + k); k++) {
			if (Bukkit.getPlayer(ProgressionFile.getData().getString("Players." + k + ".name")) == p) {
				ProgressionFile.getData().set("Players." + k + ".level", getLevel(p));
				ProgressionFile.getData().set("Players." + k + ".experience", getExperience(p));
				ProgressionFile.getData().set("Players." + k + ".prestigeLevel", getPrestigeLevel(p));
				ProgressionFile.saveData();
				ProgressionFile.reloadData();
				return;
			}
		}

		ProgressionFile.getData().set("Players." + k + ".name", p.getName());
		ProgressionFile.getData().set("Players." + k + ".level", getLevel(p));
		ProgressionFile.getData().set("Players." + k + ".experience", getExperience(p));
		ProgressionFile.getData().set("Players." + k + ".prestigeLevel", getPrestigeLevel(p));
		ProgressionFile.saveData();
		ProgressionFile.reloadData();
	}

	public ArrayList<Player> getPlayerRankings() {

		return new ArrayList<Player>();
	}

}
