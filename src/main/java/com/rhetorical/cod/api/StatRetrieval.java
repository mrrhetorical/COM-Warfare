package com.rhetorical.cod.api;

import com.rhetorical.cod.files.StatsFile;

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
		
		if (!StatsFile.getData().contains(playerName + ".experience"))
			return new StatRetrieval<>(0d, 2);
		
		return new StatRetrieval<>(StatsFile.getData().getDouble(playerName + ".experience"), 0);
	}
	
	public static StatRetrieval<Integer> getTotalDeaths(String playerName) {
		if (!StatsFile.getData().contains(playerName + ".deaths"))
			return new StatRetrieval<>(0, 2);
		
		return new StatRetrieval<>(StatsFile.getData().getInt(playerName + ".deaths"), 0);
	}
	
	public static StatRetrieval<Integer> getTotalKills(String playerName) {
		if (!StatsFile.getData().contains(playerName + ".kills"))
			return new StatRetrieval<>(0, 2);
		
		return new StatRetrieval<>(StatsFile.getData().getInt(playerName + ".kills"), 0);

	}
	
	
	
	
	
	
	
}
