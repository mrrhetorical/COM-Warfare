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

public class KillstreaksFile {
	private static KillstreaksFile instance = new KillstreaksFile();
	private static final int nameToUuid = 0;
	private static final int uuidToName = 1;
	private static Plugin p;
	private static FileConfiguration killstreaks;
	private static File kfile;

	public static KillstreaksFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		kfile = new File(p.getDataFolder(), "killstreaks.yml");
		if (!kfile.exists()) {
			try {
				kfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create killstreaks.yml!");
			}
		}
		reloadData();

		if (ComWarfare.useUuidForYml)
			replaceNamesAndUUIDs(nameToUuid);
		else
			replaceNamesAndUUIDs(uuidToName);
	}

	public static FileConfiguration getData() {
		return killstreaks;
	}

	public static void saveData() {
		try {
			killstreaks.save(kfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save killstreaks.yml!");
		}
	}

	public static void reloadData() {
		killstreaks = YamlConfiguration.loadConfiguration(kfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}

	public static void replaceNamesAndUUIDs(int type) {
		if (getData().getConfigurationSection("Killstreaks") == null) return;
		for (String key : getData().getConfigurationSection("Killstreaks").getKeys(false)) {
			String replacement;
			if (type == nameToUuid) {
				if (key.length() < 36) {
					try {
						replacement = UUIDFetcher.getUUID(key);
						getData().set("Killstreaks." + replacement, getData().get("Killstreaks." + key));
						getData().set("Killstreaks." + key, null);
						ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player name " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with UUID " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in killstreaks.yml.");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else if (type == uuidToName) {
				if (key.length() > 16) {
					try {
						replacement = NameFetcher.getName(key);
						getData().set("Killstreaks." + replacement, getData().get("Killstreaks." + key));
						getData().set("Killstreaks." + key, null);
						ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player UUID " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with name " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in killstreaks.yml.");
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