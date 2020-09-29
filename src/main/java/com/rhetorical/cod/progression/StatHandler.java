package com.rhetorical.cod.progression;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.StatsFile;
import com.rhetorical.cod.sql.SQLDriver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles things for storing data used in leaderboards and the combat record.
 * */
public class StatHandler {

	private static Map<String, Integer> cachedKills = new HashMap<>();
	private static Map<String, Integer> cachedDeaths = new HashMap<>();
	private static Map<String, Double> cachedExperience = new HashMap<>();

	public static void flushCachedData() {
		cachedKills.clear();
		cachedDeaths.clear();
		cachedExperience.clear();
	}

	public static void addKill(Player p) {
		String name = ComWarfare.setName(p.getName());
		if (ComWarfare.MySQL) {
			int i = cachedKills.containsKey(name) ? cachedKills.get(name) : SQLDriver.getInstance().getKills(p.getUniqueId()) + 1;
			cachedKills.put(name, i);
			SQLDriver.getInstance().setKills(p.getUniqueId(), i);
		} else {
			if (!StatsFile.getData().contains(name + ".kills")) {
				StatsFile.getData().set(name + ".kills", 1);
				cachedKills.put(name, 1);
				return;
			}

			int kills = cachedKills.containsKey(name) ? StatsFile.getData().getInt(name + ".kills") + 1 : cachedKills.get(name) + 1;
			cachedKills.put(name, kills);

			StatsFile.getData().set(name + ".kills", kills);
			StatHandler.addPlayerToLeaderboardList(name);
		}
	}

	public static void addDeath(Player p) {
		String name = ComWarfare.setName(p.getName());
		if (ComWarfare.MySQL) {
			int i = cachedDeaths.containsKey(name) ? cachedDeaths.get(name) + 1 : SQLDriver.getInstance().getDeaths(p.getUniqueId()) + 1;
			cachedDeaths.put(name, i);
			SQLDriver.getInstance().setDeaths(p.getUniqueId(), i);
		} else {
			if (!StatsFile.getData().contains(name + ".deaths")) {
				StatsFile.getData().set(name + ".deaths", 1);
				cachedDeaths.put(name, 1);
				return;
			}

			int deaths = cachedDeaths.containsKey(name) ? cachedDeaths.get(name) + 1 : StatsFile.getData().getInt(name + ".deaths") + 1;
			cachedDeaths.put(name, deaths);

			StatsFile.getData().set(name + ".deaths", deaths);
			StatHandler.addPlayerToLeaderboardList(name);
		}
	}

	static void addExperience(Player p, double experience) {
		String name = ComWarfare.setName(p.getName());
		if (ComWarfare.MySQL) {
			double d = cachedExperience.containsKey(name) ? cachedExperience.get(name) + experience : SQLDriver.getInstance().getExperience(p.getUniqueId()) + experience;
			cachedExperience.put(name, d);
			SQLDriver.getInstance().setExperience(p.getUniqueId(), d);
		} else {
			if (!StatsFile.getData().contains(name + ".experience")) {
				StatsFile.getData().set(name + ".experience", experience);
				cachedExperience.put(name, experience);
				return;
			}

			double totalExperience = cachedExperience.containsKey(name) ? cachedExperience.get(name) + experience : StatsFile.getData().getDouble(name + ".experience") + experience;
			cachedExperience.put(name, totalExperience);

			StatsFile.getData().set(name + ".experience", totalExperience);
			StatHandler.addPlayerToLeaderboardList(name);
		}
	}

	public static void removeExperience(Player p, double experience) {
		if (ComWarfare.MySQL) {
			double d = SQLDriver.getInstance().getExperience(p.getUniqueId()) - experience;
			SQLDriver.getInstance().setExperience(p.getUniqueId(), d);
		} else {
			String playerName = ComWarfare.setName(p.getName());
			if (!StatsFile.getData().contains(playerName + ".experience")) {
				StatsFile.getData().set(playerName + ".experience", 0D);
				return;
			}
			double totalExperience = StatsFile.getData().getDouble(p.getName() + ".experience");

			totalExperience -= experience;
			StatsFile.getData().set(playerName + ".experience", totalExperience);
			StatHandler.addPlayerToLeaderboardList(playerName);
		}
	}

	public static void removeKill(Player p) {
		if (ComWarfare.MySQL) {
			int i = SQLDriver.getInstance().getKills(p.getUniqueId()) - 1;
			SQLDriver.getInstance().setKills(p.getUniqueId(), i);
		} else {
			String playerName = ComWarfare.setName(p.getName());
			if (!StatsFile.getData().contains(playerName + ".kills")) {
				StatsFile.getData().set(playerName + ".kills", 0);
				return;
			}

			int kills = StatsFile.getData().getInt(playerName + ".kills");

			if (kills == 0)
				return;

			kills--;

			StatsFile.getData().set(playerName + ".kills", kills);
			StatHandler.addPlayerToLeaderboardList(playerName);
		}

	}

	public static void removeDeath(Player p) {
		if (ComWarfare.MySQL) {
			int i = SQLDriver.getInstance().getDeaths(p.getUniqueId()) - 1;
			SQLDriver.getInstance().setDeaths(p.getUniqueId(), i);
		} else {
			String playerName = ComWarfare.setName(p.getName());
			if (!StatsFile.getData().contains(playerName + ".deaths")) {
				StatsFile.getData().set(playerName + ".deaths", 0);
				return;
			}

			int deaths = StatsFile.getData().getInt(playerName + ".deaths");

			if (deaths == 0)
				return;

			deaths--;

			StatsFile.getData().set(playerName + ".deaths", deaths);
			StatHandler.addPlayerToLeaderboardList(playerName);
		}
	}

	private static void addPlayerToLeaderboardList(String pName) {
		String playerName = ComWarfare.setName(pName);
		int k = 0;
		while (StatsFile.getData().contains("Leaderboard." + k)) {
			if (StatsFile.getData().getString("Leaderboard." + k + ".name").equals(playerName))
				return;

			k++;
		}

		StatsFile.getData().set("Leaderboard." + k + ".name", playerName);
		StatsFile.saveData();
		StatsFile.reloadData();
	}

	public static ArrayList<String> getLeaderboardList() {

		ArrayList<String> leaderboard = new ArrayList<>();

		for (int i = 0; StatsFile.getData().contains("Leaderboard." + i); i++) {
			leaderboard.add(StatsFile.getData().getString("Leaderboard." + i + ".name"));
		}
		
		if (leaderboard.isEmpty()) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				StatHandler.addPlayerToLeaderboardList(p.getName());
			}
			
			for (int i = 0; StatsFile.getData().contains("Leaderboard." + i); i++) {
				leaderboard.add(StatsFile.getData().getString("Leaderboard." + i + ".name"));
			}
		}
		return leaderboard;
	}

	public static int getKills(String playerName) {
		playerName = ComWarfare.setName(playerName);

		if (cachedKills.containsKey(playerName))
			return cachedKills.get(playerName);

		int kills;
		if (ComWarfare.MySQL) {
			kills = SQLDriver.getInstance().getKills(Bukkit.getOfflinePlayer(playerName).getUniqueId());
		} else {
			if (!StatsFile.getData().contains(playerName + ".kills"))
				return 0;
			kills = StatsFile.getData().getInt(playerName + ".kills");
		}
		return kills;
	}

	public static double getExperience(String playerName) {
		playerName = ComWarfare.setName(playerName);

		if (cachedExperience.containsKey(playerName))
			return cachedExperience.get(playerName);

		double experience;
		if (ComWarfare.MySQL) {
			experience = SQLDriver.getInstance().getExperience(Bukkit.getOfflinePlayer(playerName).getUniqueId());
		} else {
			if (!StatsFile.getData().contains(playerName + ".experience"))
				return 0D;
			experience = StatsFile.getData().getDouble(playerName + ".experience");
		}

		return experience;
	}

	public static int getDeaths(String playerName) {
		playerName = ComWarfare.setName(playerName);

		if (cachedDeaths.containsKey(playerName))
			return cachedDeaths.get(playerName);

		int deaths;
		if (ComWarfare.MySQL) {
			deaths = SQLDriver.getInstance().getDeaths(Bukkit.getOfflinePlayer(playerName).getUniqueId());
		} else {
			if (!StatsFile.getData().contains(playerName + ".deaths"))
				return 0;
			deaths = StatsFile.getData().getInt(playerName + ".deaths");
		}
		return deaths;
	}

	public synchronized static void saveStatData() {
		if (!ComWarfare.MySQL) {
			StatsFile.saveData();
			StatsFile.reloadData();
		}
	}
}