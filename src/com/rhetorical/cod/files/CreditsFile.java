package com.rhetorical.cod.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class CreditsFile {
	static CreditsFile instance = new CreditsFile();
	static Plugin p;
	static FileConfiguration credits;
	static File cfile;

	public static CreditsFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		cfile = new File(p.getDataFolder(), "credits.yml");
		if (!cfile.exists()) {
			try {
				cfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create credits.yml!");
			}
		}
		credits = YamlConfiguration.loadConfiguration(cfile);
	}

	public static FileConfiguration getData() {
		return credits;
	}

	public static void saveData() {
		try {
			credits.save(cfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save credits.yml!");
		}
	}

	public static void reloadData() {
		credits = YamlConfiguration.loadConfiguration(cfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}