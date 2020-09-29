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

public class StatsFile {
    private static final StatsFile instance = new StatsFile();
    private static final int nameToUuid = 0;
    private static final int uuidToName = 1;
    private static Plugin p;
    private static FileConfiguration stats;
    private static File sfile;

    public static StatsFile getInstance() {
        return instance;
    }

    public static void setup(Plugin p) {
        if (!p.getDataFolder().exists()) {
            p.getDataFolder().mkdir();
        }
        sfile = new File(p.getDataFolder(), "stats.yml");
        if (!sfile.exists()) {
            try {
                sfile.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create stats.yml!");
            }
        }

        reloadData();

        if (ComWarfare.useUuidForYml)
            replaceNamesAndUUIDs(nameToUuid);
        else
            replaceNamesAndUUIDs(uuidToName);
    }

    public static FileConfiguration getData() {
        return stats;
    }

    public static void saveData() {
        try {
            stats.save(sfile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save stats.yml!");
        }
    }

    public static void reloadData() {
        stats = YamlConfiguration.loadConfiguration(sfile);
    }

    public static PluginDescriptionFile getDesc() {
        return p.getDescription();
    }

    public static void replaceNamesAndUUIDs(int type) {
        if (getData().getConfigurationSection("") == null) return;
        for (String key : getData().getConfigurationSection("").getKeys(false)) {
            String replacement = "";
            if (type == nameToUuid) {
                if (key.length() < 36) {
                    try {
                        if (key.equals("Leaderboard")) {
                            for (String lkey : getData().getConfigurationSection("Leaderboard").getKeys(false)) {
                                if (getData().getString(key + "." + lkey + ".name").length() < 36) {
                                    replacement = UUIDFetcher.getUUID(getData().getString(key + "." + lkey + ".name"));
                                    getData().set(key + "." + lkey + ".name", replacement);
                                }
                            }
                        } else {
                            replacement = UUIDFetcher.getUUID(key);
                            getData().set(replacement, getData().get(key));
                            getData().set(key, null);
                            ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player name " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with UUID " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in stats.yml.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (type == uuidToName) {
                if (key.length() > 16) {
                    try {
                        replacement = NameFetcher.getName(key);
                        getData().set(replacement, getData().get(key));
                        getData().set(key, null);
                        ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player UUID " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with name " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in stats.yml.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        if (key.equals("Leaderboard")) {
                            for (String lkey : getData().getConfigurationSection("Leaderboard").getKeys(false)) {
                                if (getData().getString(key + "." + lkey + ".name").length() > 16) {
                                    replacement = NameFetcher.getName(getData().getString(key + "." + lkey + ".name"));
                                    getData().set(key + "." + lkey + ".name", replacement);
                                }
                            }
                        }
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