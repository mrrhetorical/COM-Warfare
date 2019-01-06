package com.rhetorical.cod.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rhetorical.cod.lang.Lang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.KillstreaksFile;


public class KillStreakManager {

	///// KILLSTREAK ITEMS /////

	public static ItemStack uavItem;
	public static ItemStack counterUavItem;
	public static ItemStack nukeItem;

	///// END KILLSTREAK ITEMS /////

	private HashMap<Player, KillStreak[]> playerKillstreaks = new HashMap<>();
	private HashMap<Player, Integer> killstreakMap = new HashMap<>();
	private HashMap<Player, ArrayList<KillStreak>> availableKillstreaks = new HashMap<>();

	public KillStreakManager() {

	}
	
	public static void setup() {
		KillStreakManager.uavItem = new ItemStack(Material.SHEARS);
		ItemMeta uavMeta = uavItem.getItemMeta();
		uavMeta.setDisplayName(Lang.UAV_NAME.getMessage());
		List<String> uavLore = uavMeta.getLore();
		if (uavLore == null)
			uavLore = new ArrayList<>();
		uavLore.add(Lang.KILL_STREAK_REQUIRED_KILLS.getMessage().replace("{kills}", "3"));
		uavMeta.setLore(uavLore);
		uavItem.setItemMeta(uavMeta);

		KillStreakManager.counterUavItem = new ItemStack(Material.SHEARS);
		ItemMeta counterUavMeta = counterUavItem.getItemMeta();
		counterUavMeta.setDisplayName(Lang.COUNTER_UAV_NAME.getMessage());
		List<String> counterUavLore = counterUavMeta.getLore();
		if (counterUavLore == null)
			counterUavLore = new ArrayList<>();
		counterUavLore.add(Lang.KILL_STREAK_REQUIRED_KILLS.getMessage().replace("{kills}", "4"));
		counterUavMeta.setLore(counterUavLore);
		counterUavItem.setItemMeta(counterUavMeta);

		KillStreakManager.nukeItem = new ItemStack(Material.TNT);
		ItemMeta nukeMeta = nukeItem.getItemMeta();
		nukeMeta.setDisplayName(Lang.NUKE_NAME.getMessage());
		List<String> nukeLore = nukeMeta.getLore();
		if (nukeLore == null)
			nukeLore = new ArrayList<>();
		nukeLore.add(Lang.KILL_STREAK_REQUIRED_KILLS.getMessage().replace("{kills}", "25"));
		nukeMeta.setLore(nukeLore);
		nukeItem.setItemMeta(nukeMeta);
	}

	void kill(Player p, Player killer) {
		if (!killstreakMap.containsKey(p))
			killstreakMap.put(p, 0);

		if (!killstreakMap.containsKey(killer))
			killstreakMap.put(killer, 0);

		killstreakMap.put(killer, killstreakMap.get(p) + 1);

		killstreakMap.put(p, 0);

		checkStreaks(killer);
	}

	private void checkStreaks(Player p) {
		KillStreak[] streaks = playerKillstreaks.get(p);

		for (KillStreak s : streaks) {
			if (killstreakMap.get(p) == s.getRequiredKills()) {
				if (!(availableKillstreaks.containsKey(p) || availableKillstreaks.get(p).contains(s))) {
					p.getInventory().addItem(s.getKillStreakItem());
					if (!availableKillstreaks.containsKey(p))
						availableKillstreaks.put(p, new ArrayList<>());

					ArrayList<KillStreak> st = availableKillstreaks.get(p);
					st.add(s);
					availableKillstreaks.put(p, st);
				}
			}
		}

	}

	/*
	 * SETTING KILLSTREAKS: - All killstreaks in menu - Killstreaks when clicked change number between 1, 2, and 3. - Save button saves streaks in said position. - When streaks have 1, they're in slot 1, and so on. Slot number is just for saving convenience. - When there are 2 streaks with the same slot, saving is cancelled. - When there are 2 streaks with the same required killcount, saving is cancelled. - If the inventory is closed before the killstreaks can be saved, the saving is cancelled.
	 * 
	 */

	public KillStreak[] getStreaks(Player p) {
		if (!playerKillstreaks.containsKey(p)) {
			setStreaks(p, KillStreak.UAV, KillStreak.COUNTER_UAV, KillStreak.NUKE);
		}

		return playerKillstreaks.get(p);
	}

	public void setStreak(Player p, KillStreak streak, int slot) {
		KillStreak[] streaks = getStreaks(p);
		streaks[slot] = streak;
		saveStreaks(p);
	}

	public boolean setStreaks(Player p, KillStreak first, KillStreak second, KillStreak third) {

		KillStreak[] streaks = { first, second, third };
		this.playerKillstreaks.put(p, streaks);
		this.saveStreaks(p);
		return true;
	}

	public boolean hasStreakActive(Player p, KillStreak ks) {

		if (this.playerKillstreaks.containsKey(p))
			for (KillStreak s : this.playerKillstreaks.get(p)) {
				if (s.equals(ks)) {
					return true;
				}
			}

		return false;
	}

	void streaksAfterDeath(Player p) {
		if (availableKillstreaks.containsKey(p)) {
			for (KillStreak k : availableKillstreaks.get(p)) {
				p.getInventory().addItem(k.getKillStreakItem());
			}
		}
	}

	public void loadStreaks(Player p) {
		if (!KillstreaksFile.getData().contains("Killstreaks." + p.getName())) {
			this.saveStreaks(p);
			KillStreak[] killStreaks = new KillStreak[3];
			killStreaks[0] = (KillStreak.UAV);
			killStreaks[1] = (KillStreak.COUNTER_UAV);
			killStreaks[2] = (KillStreak.NUKE);
			playerKillstreaks.put(p, killStreaks);
			return;
		}

		List<String> streaks = KillstreaksFile.getData().getStringList("Killstreaks." + p.getName() + ".streaks");

		KillStreak[] killStreaks = new KillStreak[streaks.size()];
		int i = -1;
		for (String s : streaks) {
			i++;
			KillStreak ks;
			try {
				ks = KillStreak.valueOf(s);
			} catch (Exception e) {
				Main.cs.sendMessage(Main.codPrefix + Lang.ERROR_COULD_NOT_LOAD_KILL_STREAKS.getMessage().replace("{player}", p.getName()));
				continue;
			}

			killStreaks[i] = ks;
		}

		this.playerKillstreaks.put(p, killStreaks);
	}

	private void saveStreaks(Player p) {
		if (!this.playerKillstreaks.containsKey(p)) {
			String[] streaks = { "UAV", "COUNTER_UAV", "NUKE" };
			KillstreaksFile.getData().set("Killstreaks." + p.getName() + ".streaks", streaks);
			KillstreaksFile.saveData();
			KillstreaksFile.reloadData();
			return;
		}

		List<String> killStreakStrings = new ArrayList<>();

		for (KillStreak k : this.playerKillstreaks.get(p)) {
			String s = k.toString();
			killStreakStrings.add(s);
		}

		KillstreaksFile.getData().set("Killstreaks." + p.getName() + ".streaks", killStreakStrings);
		KillstreaksFile.saveData();
		KillstreaksFile.reloadData();
	}
}