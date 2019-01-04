package com.rhetorical.cod;

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

				if (p.isSprinting()) {
					if (p.getFoodLevel() > 6) {
						p.setFoodLevel(p.getFoodLevel() - 2);
						System.out.println("Health modified");
					}
				} else {
					if (p.getFoodLevel() < 20) {
						p.setFoodLevel(p.getFoodLevel() + 2);
						System.out.println("Health modified");
					}
				}
			}
		};

		br.runTaskTimer(Main.getPlugin(), 20L, 20L);
	}


}