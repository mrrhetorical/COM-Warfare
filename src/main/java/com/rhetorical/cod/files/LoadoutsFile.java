package com.rhetorical.cod.files;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.util.NameFetcher;
import com.rhetorical.cod.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;

public class LoadoutsFile {
	private static LoadoutsFile instance = new LoadoutsFile();
	private static final int nameToUuid = 0;
	private static final int uuidToName = 1;
	private static Plugin p;
	private static FileConfiguration loadouts;
	private static File lfile;

	public static LoadoutsFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		lfile = new File(p.getDataFolder(), "loadouts.yml");
		if (!lfile.exists()) {
			try {
				lfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create loadouts.yml!");
			}
		}
		reloadData();

		if (ComWarfare.useUuidForYml)
			replaceNamesAndUUIDs(nameToUuid);
		else
			replaceNamesAndUUIDs(uuidToName);
	}

	public static FileConfiguration getData() {
		return loadouts;
	}

	public static void saveData() {
		try {
			loadouts.save(lfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save loadouts.yml!");
		}
	}

	public static void reloadData() {
		loadouts = YamlConfiguration.loadConfiguration(lfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}

	public static void replaceNamesAndUUIDs(int type) {
		if (getData().getConfigurationSection("Loadouts") == null) return;
		for (String key : getData().getConfigurationSection("Loadouts").getKeys(false)) {
			String replacement;
			if (type == nameToUuid) {
				if (key.length() < 36) {
					try {
						replacement = UUIDFetcher.getUUID(key);
						getData().set("Loadouts." + replacement, getData().get("Loadouts." + key));
						getData().set("Loadouts." + key, null);
						ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player name " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with UUID " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in loadouts.yml.");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (type == uuidToName) {
				if (key.length() > 16) {
					try {
						replacement = NameFetcher.getName(key);
						getData().set("Loadouts." + replacement, getData().get("Loadouts." + key));
						getData().set("Loadouts." + key, null);
						ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player UUID " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with name " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in loadouts.yml.");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		saveData();
		reloadData();
	}
}