package com.rhetorical.cod.streaks;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.KillstreaksFile;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.lang.Lang;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class KillStreakManager {

	///// END KILLSTREAK ITEMS /////

	private HashMap<Player, KillStreak[]> playerKillstreaks = new HashMap<>();
	private HashMap<Player, ArrayList<KillStreak>> availableKillstreaks = new HashMap<>();

	public void checkStreaks(Player p) {
		KillStreak[] streaks = playerKillstreaks.get(p);

		for (KillStreak s : streaks) {
			if (Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getScore(p).getKillstreak() == s.getRequiredKills()) {
				p.getInventory().addItem(s.getKillStreakItem());
				if (!availableKillstreaks.containsKey(p))
					availableKillstreaks.put(p, new ArrayList<>());

				ArrayList<KillStreak> st = availableKillstreaks.get(p);
				st.add(s);
				availableKillstreaks.put(p, st);
			}
		}

	}

	public void reset(Player p) {
		availableKillstreaks.put(p, new ArrayList<>());
	}

	public void useStreak(Player p, KillStreak streak) {
		if (!availableKillstreaks.containsKey(p))
			availableKillstreaks.put(p, new ArrayList<>());

		ArrayList<KillStreak> active = availableKillstreaks.get(p);
		active.remove(streak);

		availableKillstreaks.put(p, active);
	}

	public KillStreak[] getStreaks(Player p) {
		if (!playerKillstreaks.containsKey(p)) {
			loadStreaks(p);
		}

		return playerKillstreaks.get(p);
	}

	public void setStreak(Player p, KillStreak streak, int slot) {
		KillStreak[] streaks = getStreaks(p);
		streaks[slot] = streak;
		playerKillstreaks.put(p, streaks);
		saveStreaks(p);
	}

	public void streaksAfterDeath(Player p) {
		if (availableKillstreaks.containsKey(p)) {
			for (KillStreak k : availableKillstreaks.get(p)) {
				p.getInventory().addItem(k.getKillStreakItem());
			}
		}
	}

	public void loadStreaks(Player p) {
		if (!KillstreaksFile.getData().contains("Killstreaks." + p.getName())) {
			KillStreak[] killStreaks = new KillStreak[3];
			killStreaks[0] = (KillStreak.UAV);
			killStreaks[1] = (KillStreak.COUNTER_UAV);
			killStreaks[2] = (KillStreak.NUKE);
			playerKillstreaks.put(p, killStreaks);
			saveStreaks(p);
			return;
		}

		List<String> streaks = KillstreaksFile.getData().getStringList("Killstreaks." + p.getName() + ".streaks");

		KillStreak[] killStreaks = new KillStreak[3];
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

		playerKillstreaks.put(p, killStreaks);
	}

	private void saveStreaks(Player p) {
		if (!playerKillstreaks.containsKey(p)) {
			String[] streaks = { "UAV", "COUNTER_UAV", "NUKE" };
			KillstreaksFile.getData().set("Killstreaks." + p.getName() + ".streaks", streaks);
			KillstreaksFile.saveData();
			KillstreaksFile.reloadData();
			return;
		}

		List<String> killStreakStrings = new ArrayList<>();

		for (KillStreak k : getStreaks(p)) {
			String s = k.toString();
			killStreakStrings.add(s);
		}

		KillstreaksFile.getData().set("Killstreaks." + p.getName() + ".streaks", killStreakStrings);
		KillstreaksFile.saveData();
		KillstreaksFile.reloadData();
	}
}