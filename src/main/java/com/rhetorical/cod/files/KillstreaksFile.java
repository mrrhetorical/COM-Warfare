package com.rhetorical.cod.files;

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
		killstreaks = YamlConfiguration.loadConfiguration(kfile);
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
}