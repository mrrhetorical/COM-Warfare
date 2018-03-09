package com.rhetorical.cod;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.rhetorical.cod.files.CreditsFile;

public class CreditManager {
	public static HashMap<Player, Integer> creditMap = new HashMap<Player, Integer>();

	public static void loadCredits(Player p) {
		for (int k = 0; CreditsFile.getData().contains("Credits.players." + k); k++) {
			if (p.getName().equals(CreditsFile.getData().get("Credits.players." + k + ".player"))) {

				int credits = CreditsFile.getData().getInt("Credits.players." + k + ".amount");

				creditMap.put(p, credits);
				return;
			}
		}
	}

	public static void saveCredits(Player p) {
		int k = 0;
		for (k = 0; CreditsFile.getData().contains("Credits.players." + k); k++) {
			if (p.getName().equals(CreditsFile.getData().get("Credits.players." + k + ".player"))) {
				CreditsFile.getData().set("Credits.players." + k + ".amount", getCredits(p));
				return;
			}
		}

		CreditsFile.getData().set("Credits.players." + k + ".player", p.getName());
		CreditsFile.getData().set("Credits.players." + k + ".amount", getCredits(p));
		CreditsFile.saveData();
		CreditsFile.reloadData();

	}

	public static int getCredits(Player p) {
		if (!creditMap.containsKey(p)) {
			creditMap.put(p, 0);
			return 0;
		} else {
			return creditMap.get(p);
		}
	}
	
	public static int getCredits(String name) {
		
		if (Bukkit.getPlayer(name) instanceof OfflinePlayer) {
			return -1;
		}
		
		Player p = Bukkit.getPlayer(name);
		
		if (!creditMap.containsKey(p)) {
			creditMap.put(p, 0);
			return 0;
		} else {
			return creditMap.get(p);
		}
	}

	public static void setCredits(Player p, int amt) {
		creditMap.put(p, amt);
		saveCredits(p);
		return;
	}
	
	public static void setCredits(String name, int amt) {
		if (Bukkit.getPlayer(name) instanceof OfflinePlayer) {
			return;
		}
		
		creditMap.put(Bukkit.getPlayer(name), amt);
		saveCredits(Bukkit.getPlayer(name));
		return;
	}
	
	
	public static boolean purchase(Player p, int cost) {

		if (getCredits(p) >= cost) {
			if (cost > 0) {
				setCredits(p, getCredits(p) - cost);
			}
			saveCredits(p);
			p.sendMessage(Main.codPrefix + "§aPurchase successful!");
			return true;
		} else {
			p.sendMessage(Main.codPrefix + "§cInsufficient funds!");
			return false;
		}
	}
}
