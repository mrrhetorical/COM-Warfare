package com.rhetorical.cod.progression;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.CreditsFile;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.sql.SQLDriver;
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
		if (ComWarfare.MySQL) {
			getInstance().creditMap.put(p, SQLDriver.getInstance().getCredits(p.getUniqueId()));
		} else {
			String playerName = ComWarfare.setName(p);

			int credits = CreditsFile.getData().getInt("Credits.Players." + playerName + ".Amount");
			getInstance().creditMap.put(p, credits);
		}
	}

	private static void saveCredits(Player p) {
		if (ComWarfare.MySQL) {
			SQLDriver.getInstance().setCredits(p.getUniqueId(), getCredits(p));
		} else {
			// Use name or uuid depending on settings
			String playerName = ComWarfare.setName(p);

			// Loop through all names/uuids until a match is found
			for (String name : CreditsFile.getData().getConfigurationSection("Credits.Players").getKeys(false)) {
				if (!name.equals(playerName)) return;
				// Set credits when a match is founds
				CreditsFile.getData().set(name + ".Amount", getCredits(p));
			}
			// Save & reload
			CreditsFile.saveData();
			CreditsFile.reloadData();
		}

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
		Player player = Bukkit.getPlayer(name);
		if (player == null) {
			return;
		}

		getInstance().creditMap.put(player, amt);
		saveCredits(player);
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
			ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.PURCHASE_SUCCESSFUL.getMessage(), ComWarfare.getLang());
			return true;
		} else {
			ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.INSUFFICIENT_FUNDS.getMessage(), ComWarfare.getLang());
			return false;
		}
	}
}
