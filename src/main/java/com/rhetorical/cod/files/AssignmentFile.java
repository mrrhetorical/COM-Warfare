package com.rhetorical.cod.files;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;

public class AssignmentFile {
	private static AssignmentFile instance = new AssignmentFile();
	private static Plugin p;
	private static FileConfiguration assignments;
	private static File aFile;

	public static AssignmentFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		aFile = new File(p.getDataFolder(), "assignments.yml");
		if (!aFile.exists()) {
			try {
				aFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create assignments.yml!");
			}
		}
		assignments = YamlConfiguration.loadConfiguration(aFile);
	}

	public static FileConfiguration getData() {
		return assignments;
	}

	public static void saveData() {
		try {
			assignments.save(aFile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save assignments.yml!");
		}

		reloadData();
	}

	public static void reloadData() {
		assignments = YamlConfiguration.loadConfiguration(aFile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}
