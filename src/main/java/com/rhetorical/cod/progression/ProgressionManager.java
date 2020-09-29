package com.rhetorical.cod.progression;

import com.rhetorical.cod.ComVersion;
import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.ProgressionFile;
import com.rhetorical.cod.inventories.ShopManager;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.sounds.events.PlayerLevelUpSoundEvent;
import com.rhetorical.cod.sounds.events.PlayerPrestigeSoundEvent;
import com.rhetorical.cod.sql.SQLDriver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class ProgressionManager {

	private static ProgressionManager instance;

	private HashMap<Player, Integer> prestigeLevel = new HashMap<>();
	// Start from 0

	private HashMap<Player, Integer> level = new HashMap<>();
	// Start from 1

	private HashMap<Player, Double> experience = new HashMap<>();
	// Start from 0

	public final int maxLevel;
	private final int maxPrestigeLevel;

	public ProgressionManager() {

		if (instance != null) {
			maxLevel = -1;
			maxPrestigeLevel = -1;
			return;
		}

		instance = this;

		for (Player p : Bukkit.getOnlinePlayers()) {
			this.loadData(p);
		}

		int maxLevelFromConfig = ComWarfare.getPlugin().getConfig().getInt("maxLevel");
		int maxPrestigeLevelFromConfig = ComVersion.getPurchased() ? ComWarfare.getPlugin().getConfig().getInt("maxPrestigeLevel") : 0;

		if (maxLevelFromConfig <= 0) {
			this.maxLevel = 55;
		} else {
			this.maxLevel = maxLevelFromConfig;
		}

		if (maxPrestigeLevelFromConfig < 0) {
			this.maxPrestigeLevel = 10;
		} else {
			this.maxPrestigeLevel = maxPrestigeLevelFromConfig;
		}

	}

	public static ProgressionManager getInstance() {
		return instance != null ? instance : new ProgressionManager();
	}


	public void setLevel(Player p, int level, boolean showMessage) {
		if (level > getLevel(p)) {
			for (int i = getLevel(p) + 1; i <= level; i++) {
				this.level.put(p, i);
				ShopManager.getInstance().checkForNewGuns(p, showMessage);
			}
		} else {
			this.level.put(p, level);
		}
		if (showMessage) {
			ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.RANK_UP_MESSAGE.getMessage().replace("{level}", getLevel(p) + ""));
		}

	}

	private void addLevel(Player p) {
		this.setExperience(p, 0);
		if (!this.level.containsKey(p)) {
			this.level.put(p, 1);
		}

		this.level.put(p, this.level.get(p) + 1);
		ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.RANK_UP_MESSAGE.getMessage().replace("{level}", getLevel(p) + ""));

		CreditManager.setCredits(p, CreditManager.getCredits(p) + ComWarfare.getRank(p).getLevelCredits());

		Bukkit.getServer().getPluginManager().callEvent(new PlayerLevelUpSoundEvent(p)); //testing event

		if (this.getLevel(p) == this.maxLevel) {
			ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.RANK_UP_READY_TO_PRESTIGE.getMessage());
		}

		ShopManager.getInstance().checkForNewGuns(p, true);
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
			ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.RANK_UP_PRESTIGE_MESSAGE.getMessage().replace("{level}", getPrestigeLevel(p) + ""));
		}

	}

	public boolean addPrestigeLevel(Player p) {
		if (!prestigeLevel.containsKey(p)) {
			prestigeLevel.put(p, 0);
		}

		if (getPrestigeLevel(p) >= maxPrestigeLevel) {
			ComWarfare.sendMessage(p, Lang.ALREADY_HIGHEST_PRESTIGE.getMessage(), ComWarfare.getPrefix());
			return false;
		}

		this.prestigeLevel.put(p, prestigeLevel.get(p) + 1);
		setExperience(p, 0d);
		setLevel(p, 1, false);

		ShopManager.getInstance().prestigePlayer(p);
		LoadoutManager.getInstance().prestigePlayer(p);

		Bukkit.getPluginManager().callEvent(new PlayerPrestigeSoundEvent(p));
		ComWarfare.sendMessage(p,ComWarfare.getPrefix() + Lang.RANK_UP_PRESTIGE_MESSAGE.getMessage().replace("{level}", getPrestigeLevel(p) + ""));
		ComWarfare.sendMessage(p,ComWarfare.getPrefix() + Lang.RANK_RESET_MESSAGE.getMessage());
		return true;
	}

	public int getPrestigeLevel(Player p) {
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

		if (level == maxLevel)
			return;

		double requiredExperience = getExperienceForLevel(level);

		double current = getExperience(p) + experience;

		StatHandler.addExperience(p, experience);

		if (current >= requiredExperience) {
			double difference = current - requiredExperience;
			if (this.getLevel(p) < this.maxLevel) {
				addLevel(p);
			}
			if (difference != 0d)
				addExperience(p, difference);
		} else {
			setExperience(p, current);
		}
		update(p);
	}

	private double getExperience(Player p) {
		if (!this.experience.containsKey(p)) {
			this.experience.put(p, 0D);
		}

		return this.experience.get(p);
	}

	private double getExperienceForLevel(int level) {
		//Original formula: (level * 120d) + 240d
		return (level * 400d) + 800;
	}

	/**
	 * Updates the player's xp hotbar.
	 * */
	public void update(Player p) {

		try {
			p.setExp((float) (getExperience(p) / getExperienceForLevel(getLevel(p))));
		} catch (Exception e) {
			ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.ERROR_SETTING_PLAYER_EXPERIENCE_LEVEL.getMessage(), ComWarfare.getLang());
		}
	}

	public void loadData(Player p) {
		if (ComWarfare.MySQL) {
			int playerLevel = SQLDriver.getInstance().getLevel(p.getUniqueId());
			double playerExperience = SQLDriver.getInstance().getExperience(p.getUniqueId());
			int playerPrestigeLevel = SQLDriver.getInstance().getPrestige(p.getUniqueId());
			level.put(p, playerLevel);
			experience.put(p, playerExperience);
			prestigeLevel.put(p, playerPrestigeLevel);
		} else {
			String playerName = ComWarfare.setName(p);
			int k = 0;
			while (ProgressionFile.getData().contains("Players." + k)) {
				if (playerName.equalsIgnoreCase(ProgressionFile.getData().getString("Players." + k + ".name"))) {
					int playerLevel = ProgressionFile.getData().getInt("Players." + k + ".level");
					double playerExperience = ProgressionFile.getData().getDouble("Players." + k + ".experience");
					int playerPrestigeLevel = ProgressionFile.getData().getInt("Players." + k + ".prestigeLevel");

					level.put(p, playerLevel);
					experience.put(p, playerExperience);
					prestigeLevel.put(p, playerPrestigeLevel);

					return;
				}

				k++;
			}
		}
	}

	public void saveData(Player p) {
		if (ComWarfare.MySQL) {
			SQLDriver.getInstance().setLevel(p.getUniqueId(), getLevel(p));
			SQLDriver.getInstance().setExperience(p.getUniqueId(), getExperience(p));
			SQLDriver.getInstance().setPrestige(p.getUniqueId(), getPrestigeLevel(p));
		} else {
			String playerName = ComWarfare.setName(p);
			for (String key : ProgressionFile.getData().getConfigurationSection("Players").getKeys(false)) {
				if (playerName.equals(key)) {
					ProgressionFile.getData().set("Players." + key + ".Level", getLevel(p));
					ProgressionFile.getData().set("Players." + key + ".Experience", getExperience(p));
					ProgressionFile.getData().set("Players." + key + ".PrestigeLevel", getPrestigeLevel(p));
					ProgressionFile.saveData();
					ProgressionFile.reloadData();
					return;
				}
			}
			ProgressionFile.saveData();
			ProgressionFile.reloadData();
		}
	}

	public ArrayList<Player> getPlayerRankings() {

		return new ArrayList<>();
	}

}