package com.rhetorical.cod.files;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;

public class ShopFile {
	private static ShopFile instance = new ShopFile();
	private static Plugin p;
	private static FileConfiguration shop;
	private static File sfile;

	public static ShopFile getInstance() {
		return instance;
	}

	public static void setup(Plugin p) {
		if (!p.getDataFolder().exists()) {
			p.getDataFolder().mkdir();
		}
		sfile = new File(p.getDataFolder(), "shop.yml");
		if (!sfile.exists()) {
			try {
				sfile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create shop.yml!");
			}
		}
		shop = YamlConfiguration.loadConfiguration(sfile);
	}

	public static FileConfiguration getData() {
		return shop;
	}

	public static void saveData() {
		try {
			shop.save(sfile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save shop.yml!");
		}
	}

	public static void reloadData() {
		shop = YamlConfiguration.loadConfiguration(sfile);
	}

	public static PluginDescriptionFile getDesc() {
		return p.getDescription();
	}
}