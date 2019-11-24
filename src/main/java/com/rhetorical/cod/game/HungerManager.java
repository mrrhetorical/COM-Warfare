package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.loadouts.Loadout;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.perks.Perk;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles sprinting & hunger for each player in COM-Warfare.
 * */

public class HungerManager {


	private List<Player> hungerPlayers = new ArrayList<>();
	private List<Player> healthPlayers = new ArrayList<>();

	public HungerManager() {
	}

	public void addPlayer(Player p) {
		if (!hungerPlayers.contains(p)) {
			hungerPlayers.add(p);
			startHungerTask(p);
		}

		if (!healthPlayers.contains(p)) {
			healthPlayers.add(p);
			startHealthTask(p);
		}
	}

	void removePlayer(Player p) {
		hungerPlayers.remove(p);
		healthPlayers.remove(p);
	}

	private void startHungerTask(Player p) {
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {

				if (!hungerPlayers.contains(p)) {
					this.cancel();
					return;
				}

				Loadout loadout = LoadoutManager.getInstance().getActiveLoadout(p);

				if (!(loadout.getPerk1().getPerk() == Perk.MARATHON
						|| loadout.getPerk2().getPerk() == Perk.MARATHON
						|| loadout.getPerk3().getPerk() == Perk.MARATHON)){

					if (p.isSprinting()) {
						if (p.getFoodLevel() > 6) {
							p.setFoodLevel(p.getFoodLevel() - 1);
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

	private void startHealthTask(Player p) {
		BukkitRunnable br = new BukkitRunnable() {

			private int timeSinceLastDamage = 0;
			private GameInstance game = GameManager.getMatchWhichContains(p);
			private double lastHealth = game.health.defaultHealth;


			@Override
			public void run() {
				if (!healthPlayers.contains(p) || game == null) {
					this.cancel();
					return;
				}

				timeSinceLastDamage += 5L;

				if (p.isSprinting())
					timeSinceLastDamage = 0;

				if (game.health.getHealth(p) < lastHealth) {
					lastHealth = game.health.getHealth(p);
					timeSinceLastDamage = 0;
					return;
				}

				lastHealth = game.health.getHealth(p);

				if (timeSinceLastDamage >= 100L) {
					game.health.heal(p, 5d);
				}

			}
		};

		br.runTaskTimer(Main.getPlugin(), 0L, 5L);
	}


}