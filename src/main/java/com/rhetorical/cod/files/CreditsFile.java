package com.rhetorical.cod.files;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.util.NameFetcher;
import com.rhetorical.cod.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;

public class CreditsFile {
    private static final CreditsFile instance = new CreditsFile();
    private static final int nameToUuid = 0;
    private static final int uuidToName = 1;
    private static Plugin p;
    private static FileConfiguration credits;
    private static File cfile;

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

        reloadData();

        upgradeFormat();

        if (ComWarfare.useUuidForYml)
            replaceNamesAndUUIDs(nameToUuid);
        else
            replaceNamesAndUUIDs(uuidToName);

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

    public static void upgradeFormat() {
        if (getData().getConfigurationSection("Credits.players") != null) {
            for (String key : getData().getConfigurationSection("Credits.players").getKeys(false)) {
                String name = getData().getString("Credits.players." + key + ".player");
                int amount = getData().getInt("Credits.players." + key + ".amount");
                getData().set("Credits.Players." + name + ".Amount", amount);
            }
            getData().set("Credits.players", null);
            saveData();
            reloadData();
            ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Upgraded credits.yml format!");
        }
    }

    public static void replaceNamesAndUUIDs(int type) {
        if (getData().getConfigurationSection("Credits.Players") == null) return;
        for (String key : getData().getConfigurationSection("Credits.Players").getKeys(false)) {
            String replacement;
            if (type == nameToUuid) {
                if (key.length() < 36) {
                    try {
                        replacement = UUIDFetcher.getUUID(key);
                        getData().set("Credits.Players." + replacement, getData().get("Credits.Players." + key));
                        getData().set("Credits.Players." + key, null);
                        ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player name " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with UUID " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in credits.yml.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (type == uuidToName) {
                if (key.length() > 16) {
                    try {
                        replacement = NameFetcher.getName(key);
                        getData().set("Credits.Players." + replacement, getData().get("Credits.Players." + key));
                        getData().set("Credits.Players." + key, null);
                        ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player UUID " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with name " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in credits.yml.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        saveData();
        reloadData();
    }
}