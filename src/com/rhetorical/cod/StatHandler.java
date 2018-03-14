package com.rhetorical.cod;

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
		return;
	}
	
	public static void addDeath(Player p) {
		if (!StatsFile.getData().contains(p.getName() + ".deaths")) {
			StatsFile.getData().set(p.getName() + ".deaths", 0);
		}
		
		int deaths = StatsFile.getData().getInt(p.getName() + ".deaths");
		
		deaths++;
		StatsFile.getData().set(p.getName() + ".deaths", deaths);
		return;
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
		return;
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
	}
	
	public static void saveStatData() {
		StatsFile.saveData();
		StatsFile.reloadData();
		return;
	}
}
