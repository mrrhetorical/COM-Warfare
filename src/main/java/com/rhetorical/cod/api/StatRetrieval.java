package com.rhetorical.cod.api;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.StatsFile;
import com.rhetorical.cod.sql.SQLDriver;
import org.bukkit.Bukkit;

public class StatRetrieval<Data> {

	private	Data information;
	private int responseCode;

	private StatRetrieval(Data info, int rCode) {
		this.information = info;
		this.responseCode = rCode;
	}

	public Data getData() {
		return this.information;
	}

	public int ResponseCode() {
		return this.responseCode;
	}

	public static String parseResponseCode(int code) {
		switch(code) {
		case 0:
			return "SUCCESS";
		case 1:
			return "NULL";
		case 2:
			return "FAIL";
		default:
			return null;
		}
	}

	public static StatRetrieval<Double> getTotalExperience(String playerName) {
		double kills;
		if (ComWarfare.MySQL) {
			kills = SQLDriver.getInstance().getExperience(Bukkit.getPlayer(playerName).getUniqueId());
		} else {
			if (ComWarfare.useUuidForYml) playerName = Bukkit.getPlayer(playerName).getUniqueId().toString();
			if (!StatsFile.getData().contains(playerName + ".experience"))
				return new StatRetrieval<>(0d, 2);
			kills = StatsFile.getData().getDouble(playerName + ".experience");
		}
		return new StatRetrieval<>(kills, 0);
	}

	public static StatRetrieval<Integer> getTotalDeaths(String playerName) {
		int kills;
		if (ComWarfare.MySQL) {
			kills = SQLDriver.getInstance().getDeaths(Bukkit.getPlayer(playerName).getUniqueId());
		} else {
			if (ComWarfare.useUuidForYml) playerName = Bukkit.getPlayer(playerName).getUniqueId().toString();
			if (!StatsFile.getData().contains(playerName + ".deaths"))
				return new StatRetrieval<>(0, 0);
			kills = StatsFile.getData().getInt(playerName + ".deaths");
		}
		return new StatRetrieval<>(kills, 0);
	}

	public static StatRetrieval<Integer> getTotalKills(String playerName) {
		int kills;
		if (ComWarfare.MySQL) {
			kills = SQLDriver.getInstance().getKills(Bukkit.getPlayer(playerName).getUniqueId());
		} else {
			if (ComWarfare.useUuidForYml) playerName = Bukkit.getPlayer(playerName).getUniqueId().toString();
			if (!StatsFile.getData().contains(playerName + ".kills"))
				return new StatRetrieval<>(0, 0);
			kills = StatsFile.getData().getInt(playerName + ".kills");
		}
		return new StatRetrieval<>(kills, 0);
	}
}
