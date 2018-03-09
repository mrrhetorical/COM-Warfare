package com.rhetorical.cod.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class ArenasFile {
	static ArenasFile instance = new ArenasFile();
	static Plugin p;
	static FileConfiguration arenas;
	static File afile;

	public static ArenasFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		afile = new File(p.getDataFolder(), "arenas.yml");
		if (!afile.exists()) {
			try {
				afile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create arenas.yml!");
			}
		}
		arenas = YamlConfiguration.loadConfiguration(afile);
	}

	public static FileConfiguration getData() {
		return arenas;
	}

	public static void saveData() {
		try {
			arenas.save(afile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save arenas.yml!");
		}
	}

	public static void reloadData() {
		arenas = YamlConfiguration.loadConfiguration(afile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}