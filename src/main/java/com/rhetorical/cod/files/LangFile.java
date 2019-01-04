package com.rhetorical.cod.files;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class LangFile {
	private static LangFile instance = new LangFile();
	private static Plugin p;
	private static FileConfiguration lang;
	private static File lfile;

	public static LangFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		lfile = new File(p.getDataFolder(), "lang.yml");
		if (!lfile.exists()) {
			try {
				lfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create lang.yml!");
			}
		}
		reloadData();
	}

	public static FileConfiguration getData() {
		return lang;
	}

	public static void saveData() {
		try {
			lang.save(lfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save lang.yml!");
		}

		reloadData();
	}

	public static void reloadData() {
		lang = YamlConfiguration.loadConfiguration(lfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}