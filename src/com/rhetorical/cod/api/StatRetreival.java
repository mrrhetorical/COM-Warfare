package com.rhetorical.cod.api;

import com.rhetorical.cod.files.StatsFile;

public class StatRetreival<Data> {
	
	private	Data information;
	private int responseCode;
	
	private StatRetreival(Data info, int rCode) {
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
	
	public static StatRetreival<Double> getTotalExperience(String playerName) {
		
		if (!StatsFile.getData().contains(playerName + ".experience"))
			return new StatRetreival<Double>(0d, 2);
		
		return new StatRetreival<Double>(StatsFile.getData().getDouble(playerName + ".experience"), 0);
	}
	
	public static StatRetreival<Integer> getTotalDeaths(String playerName) {
		if (!StatsFile.getData().contains(playerName + ".deaths"))
			return new StatRetreival<Integer>(0, 2);
		
		return new StatRetreival<Integer>(StatsFile.getData().getInt(playerName + ".deaths"), 0);
	}
	
	public static StatRetreival<Integer> getTotalKills(String playerName) {
		if (!StatsFile.getData().contains(playerName + ".kills"))
			return new StatRetreival<Integer>(0, 2); 
		
		return new StatRetreival<Integer>(StatsFile.getData().getInt(playerName + ".kills"), 0);
		
	}
	
	
	
	
	
	
	
}
