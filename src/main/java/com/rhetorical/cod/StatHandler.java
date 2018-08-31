package com.rhetorical.cod;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.rhetorical.cod.files.StatsFile;

public class StatHandler {

	public static void addKill(Player p) {
		if (!StatsFile.getData().contains(p.getName() + ".kills")) {
			StatsFile.getData().set(p.getName() + ".kills", 0);
		}
		int kills = StatsFile.getData().getInt(p.getName() + ".kills");

		kills++;

		StatsFile.getData().set(p.getName() + ".kills", kills);
		StatHandler.addPlayerToLeaderboardList(p.getName());
	}

	public static void addDeath(Player p) {
		if (!StatsFile.getData().contains(p.getName() + ".deaths")) {
			StatsFile.getData().set(p.getName() + ".deaths", 0);
		}

		int deaths = StatsFile.getData().getInt(p.getName() + ".deaths");

		deaths++;
		StatsFile.getData().set(p.getName() + ".deaths", deaths);
		StatHandler.addPlayerToLeaderboardList(p.getName());
	}

	static void addExperience(Player p, double experience) {
		if (!StatsFile.getData().contains(p.getName() + ".experience")) {
			StatsFile.getData().set(p.getName() + ".experience", 0D);
		}

		double totalExperience = StatsFile.getData().getDouble(p.getName() + ".experience");

		totalExperience += experience;

		StatsFile.getData().set(p.getName() + ".experience", totalExperience);
		StatHandler.addPlayerToLeaderboardList(p.getName());
	}

	public static void removeExperience(Player p, double experience) {
		if (!StatsFile.getData().contains(p.getName() + ".experience")) {
			StatsFile.getData().set(p.getName() + ".experience", 0D);
			return;
		}
		double totalExperience = StatsFile.getData().getDouble(p.getName() + ".experience");

		totalExperience -= experience;
		StatsFile.getData().set(p.getName() + ".experience", totalExperience);
		StatHandler.addPlayerToLeaderboardList(p.getName());
	}

	public static void removeKill(Player p) {
		if (!StatsFile.getData().contains(p.getName() + ".kills")) {
			StatsFile.getData().set(p.getName() + ".kills", 0);
			return;
		}

		int kills = StatsFile.getData().getInt(p.getName() + ".kills");

		if (kills == 0)
			return;

		kills--;

		StatsFile.getData().set(p.getName() + ".kills", kills);
		StatHandler.addPlayerToLeaderboardList(p.getName());
	}

	public static void removeDeath(Player p) {
		if (!StatsFile.getData().contains(p.getName() + ".deaths")) {
			StatsFile.getData().set(p.getName() + ".deaths", 0);
			return;
		}

		int deaths = StatsFile.getData().getInt(p.getName() + ".deaths");

		if (deaths == 0)
			return;

		deaths--;

		StatsFile.getData().set(p.getName() + ".deaths", deaths);
		StatHandler.addPlayerToLeaderboardList(p.getName());
	}

	private static void addPlayerToLeaderboardList(String pName) {

		int k = 0;

		while (StatsFile.getData().contains("Leaderboard." + k)) {
			if (StatsFile.getData().getString("Leaderboard." + k + ".name").equals(pName))
				return;

			k++;
		}

		StatsFile.getData().set("Leaderboard." + k + ".name", pName);
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

	public static int getKills(String pName) {
		if (!StatsFile.getData().contains(pName + ".kills"))
			return 0;

		return StatsFile.getData().getInt(pName + ".kills");
	}

	public static double getExperience(String pName) {
		if (!StatsFile.getData().contains(pName + ".experience"))
			return 0D;

		return StatsFile.getData().getDouble(pName + ".experience");
	}

	public static int getDeaths(String pName) {
		if (!StatsFile.getData().contains(pName + ".deaths"))
			return 0;

		return StatsFile.getData().getInt(pName + ".deaths");
	}

	public static void saveStatData() {
		StatsFile.saveData();
		StatsFile.reloadData();
	}
}
