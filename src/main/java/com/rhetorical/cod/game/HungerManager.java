package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.loadouts.Loadout;
import com.rhetorical.cod.perks.Perk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class HungerManager {


	private List<Player> pls = new ArrayList<>();

	public HungerManager() {
	}

	public void addPlayer(Player p) {
		if (!pls.contains(p)) {
			pls.add(p);
			startHungerTask(p);
		}
	}

	public void removePlayer(Player p) {
		pls.remove(p);
	}

	private void startHungerTask(Player p) {
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {

				if (!pls.contains(p)) {
					this.cancel();
					return;
				}

				Loadout loadout = Main.loadManager.getActiveLoadout(p);

				if (!(loadout.getPerk1().getPerk() == Perk.MARATHON
						|| loadout.getPerk2().getPerk() == Perk.MARATHON
						|| loadout.getPerk3().getPerk() == Perk.MARATHON)){

					if (p.isSprinting()) {
						if (p.getFoodLevel() > 6) {
							p.setFoodLevel(p.getFoodLevel() - 2);
						}
					} else {
						if (!p.isSprinting() && p.getFoodLevel() < 20) {
							p.setFoodLevel(p.getFoodLevel() + 2);
						}
					}
				} else {
					if (p.getFoodLevel() != 20) {
						p.setFoodLevel(20);
					}
				}
			}
		};

		br.runTaskTimer(Main.getPlugin(), 20L, 20L);
	}


}