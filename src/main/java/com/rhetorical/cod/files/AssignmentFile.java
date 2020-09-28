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

public class AssignmentFile {
    private static final AssignmentFile instance = new AssignmentFile();
    private static final int nameToUuid = 0;
    private static final int uuidToName = 1;
    private static Plugin p;
    private static FileConfiguration assignments;
    private static File aFile;

    public static AssignmentFile getInstance() {
        return instance;
    }

    public static void setup(Plugin p) {
        if (!p.getDataFolder().exists()) {
            p.getDataFolder().mkdir();
        }
        aFile = new File(p.getDataFolder(), "assignments.yml");
        if (!aFile.exists()) {
            try {
                aFile.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create assignments.yml!");
            }
        }

        reloadData();

        if (ComWarfare.useUuidForYml)
            replaceNamesAndUUIDs(nameToUuid);
        else
            replaceNamesAndUUIDs(uuidToName);

    }

    public static FileConfiguration getData() {
        return assignments;
    }

    public static void saveData() {
        try {
            assignments.save(aFile);
        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save assignments.yml!");
        }

        reloadData();
    }

    public static void reloadData() {
        assignments = YamlConfiguration.loadConfiguration(aFile);
    }

    public static PluginDescriptionFile getDesc() {
        return p.getDescription();
    }

    public static void replaceNamesAndUUIDs(int type) {
        if (getData().getConfigurationSection("Players") == null) return;
        for (String key : getData().getConfigurationSection("Players").getKeys(false)) {
            String replacement = "";
            if (type == nameToUuid) {
                if (key.length() < 36) {
                    try {
                        replacement = UUIDFetcher.getUUID(key);
                        getData().set("Players." + replacement, getData().get("Players." + key));
                        getData().set("Players." + key, null);
                        ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player name " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with UUID " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in assignments.yml.");
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
                        ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player UUID " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with name " + ChatColor.DARK_GREEN + replacement + ChatColor.GREEN + " in assignments.yml.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        saveData();
    }
}

/*Assignment:
  Type:
    KILLS:
      baseReward: 1
    PLAY_MODE:
      baseReward: 20
    WIN_GAME:
      baseReward: 50
    WIN_GAME_MODE:
      baseReward: 75
Players:
  b06598a2-b958-4d0c-a82c-7469904f3070:
    Assignments:
      '0':
        assignmentType: PLAY_MODE
        requiredMode: GUNFIGHT
        amount: 1
        progress: 0
      '1':
        assignmentType: KILLS
        requiredMode: ANY
        amount: 10
        progress: 0
      '2':
        assignmentType: PLAY_MODE
        requiredMode: TDM
        amount: 2
        progress: 0
  9f06b172-4db7-4a0e-be53-a246097bc94a:
    Assignments:
      '0':
        assignmentType: WIN_GAME
        requiredMode: ANY
        amount: 3
        progress: 0
      '1':
        assignmentType: KILLS
        requiredMode: ANY
        amount: 25
        progress: 9
      '2':
        assignmentType: WIN_GAME
        requiredMode: ANY
        amount: 1
        progress: 0
  Insprill:
    Assignments:
      '0':
        assignmentType: PLAY_MODE
        requiredMode: GUNFIGHT
        amount: 1
        progress: 0
      '1':
        assignmentType: PLAY_MODE
        requiredMode: RSB
        amount: 3
        progress: 0
      '2':
        assignmentType: KILLS
        requiredMode: ANY
        amount: 25
        progress: 0
  SkibbityBop:
    Assignments:
      '0':
        assignmentType: WIN_GAME
        requiredMode: ANY
        amount: 3
        progress: 0
      '1':
        assignmentType: PLAY_MODE
        requiredMode: TDM
        amount: 1
        progress: 0
      '2':
        assignmentType: KILLS
        requiredMode: ANY
        amount: 45
        progress: 5
*/





    /*public static void replaceNamesWithUUIDs() {
        if (getData().getConfigurationSection("Players") == null) return;
        for (String key : getData().getConfigurationSection("Players").getKeys(false)) {
            if (key.length() < 36) {
                String uuid = "";
                try {
                    uuid = UUIDFetcher.getUUID(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getData().set("Players." + uuid, getData().get("Players." + key));
                getData().set("Players." + key, null);
                ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player name " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with UUID " + ChatColor.DARK_GREEN + uuid + ChatColor.GREEN + " in assignments.yml.");
            }
        }
        saveData();
    }

    public static void replaceUUIDsWithNames() {
        if (getData().getConfigurationSection("Players") == null) return;
        for (String key : getData().getConfigurationSection("Players").getKeys(false)) {
            if (key.length() > 16) {
                String name = "";
                try {
                    name = NameFetcher.getName(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getData().set("Players." + name, getData().get("Players." + key));
                getData().set("Players." + key, null);
                ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.GREEN + "Replaced player UUID " + ChatColor.DARK_GREEN + key + ChatColor.GREEN + " with name " + ChatColor.DARK_GREEN + name + ChatColor.GREEN + " in assignments.yml.");
            }
        }
        saveData();
    }*/