package com.rhetorical.cod.files;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;

public class ProgressionFile {
	private static ProgressionFile instance = new ProgressionFile();
	private static Plugin p;
	private static FileConfiguration progression;
	private static File pfile;

	public static ProgressionFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		pfile = new File(p.getDataFolder(), "progression.yml");
		if (!pfile.exists()) {
			try {
				pfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create progression.yml!");
			}
		}
		reloadData();
	}

	public static FileConfiguration getData() {
		return progression;
	}

	public static void saveData() {
		try {
			progression.save(pfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save progression.yml!");
		}
	}

	public static void reloadData() {
		progression = YamlConfiguration.loadConfiguration(pfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}