package com.rhetorical.cod.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class StatsFile {
	private static StatsFile instance = new StatsFile();
	private static Plugin p;
	private static FileConfiguration stats;
	private static File sfile;

	public static StatsFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		sfile = new File(p.getDataFolder(), "stats.yml");
		if (!sfile.exists()) {
			try {
				sfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create stats.yml!");
			}
		}
		stats = YamlConfiguration.loadConfiguration(sfile);
	}

	public static FileConfiguration getData() {
		return stats;
	}

	public static void saveData() {
		try {
			stats.save(sfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save stats.yml!");
		}
	}

	public static void reloadData() {
		stats = YamlConfiguration.loadConfiguration(sfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}