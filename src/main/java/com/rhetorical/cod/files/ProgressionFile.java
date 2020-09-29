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

public class ProgressionFile {
    private static final ProgressionFile instance = new ProgressionFile();
    private static final int nameToUuid = 0;
    private static final int uuidToName = 1;
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

        upgradeFormat();

        if (ComWarfare.useUuidForYml)
            replaceNamesAndUUIDs(nameToUuid);
        else
            replaceNamesAndUUIDs(uuidToName);

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

    public static void upgradeFormat() {
        if (getData().getConfigurationSection("Players") != null) {
            for (String key : getData().getConfigurationSection("Players").getKeys(false)) {
                if (getData().getString("Players." + key + ".name") != null) {
                    String name = getData().getString("Players." + key + ".name");
                    int level = getData().getInt("Players." + key + ".level");
                    int prestigeLevel = getData().getInt("Players." + key + ".prestigeLevel");
                    double experience = getData().getDouble("Players." + key + ".experience");
                    getData().set("Players." + name + ".Level", level);
                    getData().set("Players." + name + ".PrestigeLevel", prestigeLevel);
                    getData().set("Players." + name + ".Experience", experience);
                    getData().set("Players." + key, null);
                }
            }
            saveData();
            reloadData();
            ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Upgraded progression.yml format!");
        }
    }

    public static void replaceNamesAndUUIDs(int type) {
        if (getData().getConfigurationSection("Players") == null) return;
        for (String key : getData().getConfigurationSection("Players").getKeys(false)) {
            String replacement;
            if (type == nameToUuid) {
                if (key.length() < 36) {
                    try {
                        replacement = UUIDFetcher.getUUID(key);
                        getData().set("Players." + replacement, getData().get("Players." + key));
                        getData().set("Players." + key, null);
                        ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player name " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with UUID " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in progression.yml.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (type == uuidToName) {
                if (key.length() > 16) {
                    try {
                        replacement = NameFetcher.getName(key);
                        getData().set("Players." + replacement, getData().get("Players." + key));
                        getData().set("Players." + key, null);
                        ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player UUID " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with name " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in progression.yml.");
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