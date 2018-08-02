package com.rhetorical.cod.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class LoadoutsFile {
	static LoadoutsFile instance = new LoadoutsFile();
	static Plugin p;
	static FileConfiguration loadouts;
	static File lfile;

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
		loadouts = YamlConfiguration.loadConfiguration(lfile);
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
}