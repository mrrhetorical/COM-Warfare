package com.rhetorical.cod.progression;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.StatsFile;
import com.rhetorical.cod.sql.SQLDriver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * This class handles things for storing data used in leaderboards and the combat record.
 * */
public class StatHandler {

	public static void addKill(Player p) {
		if (ComWarfare.MySQL) {
			int i = SQLDriver.getInstance().getKills(p.getUniqueId()) + 1;
			SQLDriver.getInstance().setKills(p.getUniqueId(), i);
		} else {
			String playerName = ComWarfare.setName(p);
			if (!StatsFile.getData().contains(playerName + ".kills")) {
				StatsFile.getData().set(playerName + ".kills", 0);
			}

			int kills = StatsFile.getData().getInt(playerName + ".kills") + 1;

			StatsFile.getData().set(playerName + ".kills", kills);
			StatHandler.addPlayerToLeaderboardList(playerName);
		}
	}

	public static void addDeath(Player p) {
		if (ComWarfare.MySQL) {
			int i = SQLDriver.getInstance().getDeaths(p.getUniqueId()) + 1;
			SQLDriver.getInstance().setDeaths(p.getUniqueId(), i);
		} else {
			String playerName = ComWarfare.setName(p);
			if (!StatsFile.getData().contains(playerName + ".deaths")) {
				StatsFile.getData().set(playerName + ".deaths", 0);
			}

			int deaths = StatsFile.getData().getInt(playerName + ".deaths");

			deaths++;
			StatsFile.getData().set(playerName + ".deaths", deaths);
			StatHandler.addPlayerToLeaderboardList(playerName);
		}
	}

	static void addExperience(Player p, double experience) {
		if (ComWarfare.MySQL) {
			double d = SQLDriver.getInstance().getExperience(p.getUniqueId()) + experience;
			SQLDriver.getInstance().setExperience(p.getUniqueId(), d);
		} else {
			String playerName = ComWarfare.setName(p);
			if (!StatsFile.getData().contains(playerName + ".experience")) {
				StatsFile.getData().set(playerName + ".experience", 0D);
			}

			double totalExperience = StatsFile.getData().getDouble(playerName + ".experience");

			totalExperience += experience;

			StatsFile.getData().set(playerName + ".experience", totalExperience);
			StatHandler.addPlayerToLeaderboardList(playerName);
		}
	}

	public static void removeExperience(Player p, double experience) {
		if (ComWarfare.MySQL) {
			double d = SQLDriver.getInstance().getExperience(p.getUniqueId()) - experience;
			SQLDriver.getInstance().setExperience(p.getUniqueId(), d);
		} else {
			String playerName = ComWarfare.setName(p);
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
			String playerName = ComWarfare.setName(p);
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
			String playerName = ComWarfare.setName(p);
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
		String playerName = ComWarfare.setName(Bukkit.getPlayer(pName));
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

		ArrayList<String> leaderboard = new ArrayList<String>();

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
		int kills;
		if (ComWarfare.MySQL) {
			kills = SQLDriver.getInstance().getKills(Bukkit.getPlayer(playerName).getUniqueId());
		} else {
			if (ComWarfare.useUuidForYml) playerName = Bukkit.getPlayer(playerName).getUniqueId().toString();
			if (!StatsFile.getData().contains(playerName + ".kills"))
				return 0;
			kills = StatsFile.getData().getInt(playerName + ".kills");
		}
		return kills;
	}

	public static double getExperience(String playerName) {
		double experience;
		if (ComWarfare.MySQL) {
			experience = SQLDriver.getInstance().getExperience(Bukkit.getPlayer(playerName).getUniqueId());
		} else {
			if (ComWarfare.useUuidForYml) playerName = Bukkit.getPlayer(playerName).getUniqueId().toString();
			if (!StatsFile.getData().contains(playerName + ".experience"))
				return 0D;
			experience = StatsFile.getData().getDouble(playerName + ".experience");
		}

		return experience;
	}

	public static int getDeaths(String playerName) {
		int deaths;
		if (ComWarfare.MySQL) {
			deaths = SQLDriver.getInstance().getDeaths(Bukkit.getPlayer(playerName).getUniqueId());
		} else {
			if (ComWarfare.useUuidForYml) playerName = Bukkit.getPlayer(playerName).getUniqueId().toString();
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