package com.rhetorical.cod.progression;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.CreditsFile;
import com.rhetorical.cod.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class CreditManager {

	private static CreditManager instance;

	private HashMap<Player, Integer> creditMap = new HashMap<>();

	private static CreditManager getInstance() {
		if (instance == null)
			instance = new CreditManager();
		return instance;
	}

	public static void loadCredits(Player p) {
		for (int k = 0; CreditsFile.getData().contains("Credits.players." + k); k++) {
			if (p.getName().equals(CreditsFile.getData().get("Credits.players." + k + ".player"))) {

				int credits = CreditsFile.getData().getInt("Credits.players." + k + ".amount");

				getInstance().creditMap.put(p, credits);
				return;
			}
		}
	}

	private static void saveCredits(Player p) {
		int k;
		for (k = 0; CreditsFile.getData().contains("Credits.players." + k); k++) {
			if (p.getName().equals(CreditsFile.getData().get("Credits.players." + k + ".player"))) {
				CreditsFile.getData().set("Credits.players." + k + ".amount", getCredits(p));
				CreditsFile.saveData();
				CreditsFile.reloadData();
				return;
			}
		}

		CreditsFile.getData().set("Credits.players." + k + ".player", p.getName());
		CreditsFile.getData().set("Credits.players." + k + ".amount", getCredits(p));
		CreditsFile.saveData();
		CreditsFile.reloadData();

	}

	public static int getCredits(Player p) {
		if (!getInstance().creditMap.containsKey(p)) {
			getInstance().creditMap.put(p, 0);
			return 0;
		} else {
			return getInstance().creditMap.get(p);
		}
	}
	
	public static int getCredits(String name) {
		
		if (Bukkit.getPlayer(name) == null) {
			return -1;
		}
		
		Player p = Bukkit.getPlayer(name);

		if (!getInstance().creditMap.containsKey(p)) {
			getInstance().creditMap.put(p, 0);
			saveCredits(p);
			return 0;
		} else {
			return getInstance().creditMap.get(p);
		}
	}

	public static void setCredits(Player p, int amt) {
		getInstance().creditMap.put(p, amt);
		saveCredits(p);
	}
	
	public static void setCredits(String name, int amt) {
		if (Bukkit.getPlayer(name) == null) {
			return;
		}
		
		getInstance().creditMap.put(Bukkit.getPlayer(name), amt);
		saveCredits(Bukkit.getPlayer(name));
	}
	

	/**
	 * @return Returns if the target player has enough money (credits) to cover the cost of the purchase.
	 * */
	public static boolean purchase(Player p, int cost) {

		if (getCredits(p) >= cost) {
			if (cost > 0) {
				setCredits(p, getCredits(p) - cost);
			}
			saveCredits(p);
			Main.sendMessage(p, Main.getPrefix() + Lang.PURCHASE_SUCCESSFUL.getMessage(), Main.getLang());
			return true;
		} else {
			Main.sendMessage(p, Main.getPrefix() + Lang.INSUFFICIENT_FUNDS.getMessage(), Main.getLang());
			return false;
		}
	}
}
