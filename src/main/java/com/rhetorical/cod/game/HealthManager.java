package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HealthManager {
	private HashMap<Player, Double> healthMap = new HashMap<Player, Double>();
	
	public double defaultHealth;
	List<Player> inJuggernaut = new ArrayList<>();

	public HealthManager(ArrayList<Player> pls, double health) {
		
		this.defaultHealth = health;

		for (Player p : pls) {
			healthMap.put(p, health);
		}
		
	}
	
	public void addPlayer(Player p) {
		getHealth(p);
		update(p);
	}
	
	public void removePlayer(Player p) {
		if (healthMap.containsKey(p)) {
			healthMap.remove(p);
		}
	}
	
	public double getHealth(Player p) {
		if (!healthMap.containsKey(p)) {
			healthMap.put(p, this.defaultHealth);
		}
		
		return healthMap.get(p);
	}
	
	public void damage(Player p, double damage) {
		
		if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) return;
		
		if (p.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) return;

		if (inJuggernaut.contains(p))
			damage /= 3;

		double health = getHealth(p) - damage;
		
		healthMap.put(p, health);
		p.playEffect(EntityEffect.HURT);
		p.playEffect(p.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

		update(p);
	}

	void setHealth(Player p, double health) {
		if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) return;

		if (health > defaultHealth) {
			if (inJuggernaut.contains(p)) {
				if (health > defaultHealth * 5) {
					health = defaultHealth * 5;
				}
			} else
				health = defaultHealth;
		}

		healthMap.put(p, health);
		update(p);
	}

	void heal(Player p, double healing) {
		if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) return;

		if (inJuggernaut.contains(p))
			healing *= 2;

		double health = getHealth(p) + healing;

		setHealth(p, health);
	}
	
	public boolean isDead(Player p) {
		return getHealth(p) <= 0;
	}
	
	void update(Player p) {
		double health = getHealth(p);

		int desired = (int) Math.ceil((20d * health) / Main.getDefaultHealth());
		if (desired < 1)
			desired = 1;
		else if (desired > 20)
			desired = 20;

		p.setLevel((int) health);
		p.setHealth((double) desired);
	}
	
	public void reset(Player p) {
		inJuggernaut.remove(p);
		healthMap.put(p, this.defaultHealth);
		update(p);
	}
}
