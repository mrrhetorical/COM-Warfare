package com.rhetorical.cod.lang;

import com.rhetorical.cod.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class LevelNames {

	private static LevelNames singleton;

	private Map<Integer, String> levelNames = new HashMap<>();

	private LevelNames() {
		if (singleton != null)
			return;

		singleton = this;

		setup();
	}

	private void setup() {
		FileConfiguration config = Main.getPlugin().getConfig();
		for (int i = 0; i < Main.progressionManager.maxLevel; i++) {
			if (config.contains("LevelNames." + i)) {
				levelNames.put(i, config.getString("LevelNames." + i));
			}
		}
	}

	public static LevelNames getInstance() {
		return singleton != null ? singleton : new LevelNames();
	}

	public String getLevelName(int level) {
		return levelNames.getOrDefault(level, "");
	}
}