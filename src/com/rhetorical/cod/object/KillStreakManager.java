package com.rhetorical.cod.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	private HashMap<Player, KillStreak[]> playerKillstreaks = new HashMap<Player, KillStreak[]>();
	private HashMap<Player, Integer> killstreakMap = new HashMap<Player, Integer>();
	private HashMap<Player, ArrayList<KillStreak>> availableKillstreaks = new HashMap<Player, ArrayList<KillStreak>>();

	public KillStreakManager() {

	}
	
	public static void setup() {
		KillStreakManager.uavItem = new ItemStack(Material.SHEARS);
		ItemMeta uavMeta = uavItem.getItemMeta();
		uavMeta.setDisplayName("§6§lUAV");
		List<String> uavLore = uavMeta.getLore();
		uavLore.add("§f§lRequired kills: §a" + 3);
		uavMeta.setLore(uavLore);
		uavItem.setItemMeta(uavMeta);

		KillStreakManager.counterUavItem = new ItemStack(Material.SHEARS);
		ItemMeta counterUavMeta = counterUavItem.getItemMeta();
		counterUavMeta.setDisplayName("§6§lCounter UAV");
		List<String> counterUavLore = counterUavMeta.getLore();
		counterUavLore.add("§f§lRequred kills: §a" + 4);
		counterUavItem.setItemMeta(counterUavMeta);

		KillStreakManager.nukeItem = new ItemStack(Material.TNT);
		ItemMeta nukeMeta = nukeItem.getItemMeta();
		nukeMeta.setDisplayName("§6§lNuke");
		List<String> nukeLore = nukeMeta.getLore();
		nukeLore.add("§f§lRequired kills: §a" + 25);
		nukeMeta.setLore(nukeLore);
		nukeItem.setItemMeta(nukeMeta);
	}

	public void kill(Player p, Player killer) {
		if (!killstreakMap.containsKey(p))
			killstreakMap.put(p, 0);

		if (!killstreakMap.containsKey(killer))
			killstreakMap.put(killer, 0);

		killstreakMap.put(killer, killstreakMap.get(p) + 1);

		killstreakMap.put(p, 0);

		checkStreaks(killer);
	}

	public void checkStreaks(Player p) {
		KillStreak[] streaks = playerKillstreaks.get(p);

		for (KillStreak s : streaks) {
			if (killstreakMap.get(p) == s.getRequiredKills()) {
				if (!(availableKillstreaks.containsKey(p) || availableKillstreaks.get(p).contains(s))) {
					p.getInventory().addItem(s.getKillstreakItem());
					if (!availableKillstreaks.containsKey(p))
						availableKillstreaks.put(p, new ArrayList<KillStreak>());

					availableKillstreaks.get(p).add(s);
					availableKillstreaks.put(p, availableKillstreaks.get(p));
				}
			}
		}

	}

	/*
	 * SETTING KILLSTREAKS: - All killstreaks in menu - Killstreaks when clicked change number between 1, 2, and 3. - Save button saves streaks in said position. - When streaks have 1, they're in slot 1, and so on. Slot number is just for saving convenience. - When there are 2 streaks with the same slot, saving is cancelled. - When there are 2 streaks with the same required killcount, saving is cancelled. - If the inventory is closed before the killstreaks can be saved, the saving is cancelled.
	 * 
	 */

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

	public void streaksAfterDeath(Player p) {
		if (availableKillstreaks.containsKey(p)) {
			for (KillStreak k : availableKillstreaks.get(p)) {
				p.getInventory().addItem(k.getKillstreakItem());
			}
		}
	}

	public void loadStreaks(Player p) {
		if (!KillstreaksFile.getData().contains("Killstreaks." + p.getName())) {
			this.saveStreaks(p);
			KillStreak[] killStreaks = {};
			killStreaks[0] = (KillStreak.UAV);
			killStreaks[1] = (KillStreak.COUNTER_UAV);
			killStreaks[2] = (KillStreak.NUKE);
			playerKillstreaks.put(p, killStreaks);
			return;
		}

		List<String> streaks = KillstreaksFile.getData().getStringList("Killstreaks." + p.getName() + ".streaks");

		KillStreak[] killStreaks = {};
		int i = 0;
		for (String s : streaks) {
			i++;
			KillStreak ks;
			try {
				ks = KillStreak.valueOf(s);
			} catch (Exception e) {
				Main.cs.sendMessage(Main.codPrefix + "§cCould not load killstreak information for " + p.getName());
				continue;
			}

			killStreaks[i] = ks;
		}

		this.playerKillstreaks.put(p, killStreaks);
	}

	public void saveStreaks(Player p) {
		if (!this.playerKillstreaks.containsKey(p)) {
			String[] streaks = { "UAV", "COUNTER_UAV", "NUKE" };
			KillstreaksFile.getData().set("Killstreaks." + p.getName() + ".streaks", streaks);
			KillstreaksFile.saveData();
			KillstreaksFile.reloadData();
			return;
		}

		ArrayList<String> killStreakStrings = new ArrayList<String>();

		for (KillStreak k : this.playerKillstreaks.get(p)) {
			String s = k.toString();
			killStreakStrings.add(s);
		}

		KillstreaksFile.getData().set("Killstreaks." + p.getName() + ".streaks", killStreakStrings);
		KillstreaksFile.saveData();
		KillstreaksFile.reloadData();
	}
}