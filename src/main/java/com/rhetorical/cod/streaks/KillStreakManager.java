package com.rhetorical.cod.streaks;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.KillstreaksFile;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.sql.SQLDriver;
import org.bukkit.entity.Player;

import java.util.*;


public class KillStreakManager {

	///// END KILLSTREAK ITEMS /////

	private static KillStreakManager instance;

	private HashMap<Player, KillStreak[]> playerKillstreaks = new HashMap<>();
	private HashMap<Player, ArrayList<KillStreak>> availableKillstreaks = new HashMap<>();

	private KillStreakManager() {
		if (instance == null)
			instance = this;
	}

	public static KillStreakManager getInstance() {
		return instance != null ? instance : new KillStreakManager();
	}

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
		String playerName = ComWarfare.setName(p.getName());
		List<String> streaks;
		if (ComWarfare.MySQL) {
			if (SQLDriver.getInstance().getKillstreaks(p.getUniqueId()).isEmpty()) {
				KillStreak[] killStreaks = new KillStreak[3];
				killStreaks[0] = (KillStreak.UAV);
				killStreaks[1] = (KillStreak.COUNTER_UAV);
				killStreaks[2] = (KillStreak.AIRSTRIKE);
				playerKillstreaks.put(p, killStreaks);
				saveStreaks(p);
				return;
			}
			streaks = SQLDriver.getInstance().getKillstreaks(p.getUniqueId());
		} else {
			if (!KillstreaksFile.getData().contains("Killstreaks." + playerName)) {
				KillStreak[] killStreaks = new KillStreak[3];
				killStreaks[0] = (KillStreak.UAV);
				killStreaks[1] = (KillStreak.COUNTER_UAV);
				killStreaks[2] = (KillStreak.AIRSTRIKE);
				playerKillstreaks.put(p, killStreaks);
				saveStreaks(p);
				return;
			}
			streaks = KillstreaksFile.getData().getStringList("Killstreaks." + playerName + ".streaks");
		}

		KillStreak[] killStreaks = new KillStreak[3];
		int i = -1;
		for (String s : streaks) {
			i++;
			KillStreak ks;
			try {
				ks = KillStreak.valueOf(s);
			} catch (Exception e) {
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + Lang.ERROR_COULD_NOT_LOAD_KILL_STREAKS.getMessage().replace("{player}", p.getName()));
				continue;
			}

			killStreaks[i] = ks;
		}

		playerKillstreaks.put(p, killStreaks);
	}

	private void saveStreaks(Player p) {
		if (!playerKillstreaks.containsKey(p)) {
			String[] streaks = { "UAV", "COUNTER_UAV", "DOGS" };
			if (ComWarfare.MySQL) {
				SQLDriver.getInstance().setKillstreaks(p.getUniqueId(), Arrays.asList(streaks));
			} else {
				String playerName = ComWarfare.setName(p.getName());
				KillstreaksFile.getData().set("Killstreaks." + playerName + ".streaks", streaks);
				KillstreaksFile.saveData();
				KillstreaksFile.reloadData();
			}
			return;
		}

		List<String> killStreakStrings = new ArrayList<>();

		for (KillStreak k : getStreaks(p)) {
			String s = k.toString();
			killStreakStrings.add(s);
		}

		if (ComWarfare.MySQL) {
			SQLDriver.getInstance().setKillstreaks(p.getUniqueId(), killStreakStrings);
		} else {
			String playerName = ComWarfare.setName(p.getName());
			KillstreaksFile.getData().set("Killstreaks." + playerName + ".streaks", killStreakStrings);
			KillstreaksFile.saveData();
			KillstreaksFile.reloadData();
		}
	}
}