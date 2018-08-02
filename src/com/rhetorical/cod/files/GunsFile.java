package com.rhetorical.cod.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class GunsFile {
	static GunsFile instance = new GunsFile();
	static Plugin p;
	static FileConfiguration guns;
	static File gfile;

	public static GunsFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		gfile = new File(p.getDataFolder(), "guns.yml");
		if (!gfile.exists()) {
			try {
				gfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create guns.yml!");
			}
		}
		guns = YamlConfiguration.loadConfiguration(gfile);
	}

	public static FileConfiguration getData() {
		return guns;
	}

	public static void saveData() {
		try {
			guns.save(gfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save guns.yml!");
		}
	}

	public static void reloadData() {
		guns = YamlConfiguration.loadConfiguration(gfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}