package com.rhetorical.cod.files;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;

public class SoundFile {
	private static SoundFile instance = new SoundFile();
	private static Plugin p;
	private static FileConfiguration sounds;
	private static File sfile;

	public static SoundFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		sfile = new File(p.getDataFolder(), "sounds.yml");
		if (!sfile.exists()) {
			try {
				sfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create sounds.yml!");
			}
		}
		sounds = YamlConfiguration.loadConfiguration(sfile);
	}

	public static FileConfiguration getData() {
		return sounds;
	}

	public static void saveData() {
		try {
			sounds.save(sfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save sounds.yml!");
		}
	}

	public static void reloadData() {
		sounds = YamlConfiguration.loadConfiguration(sfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}