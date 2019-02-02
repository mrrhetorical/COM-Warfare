package com.rhetorical.cod.game;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;

public class HealthManager {
	private HashMap<Player, Double> healthMap = new HashMap<Player, Double>();
	
	public double defaultHealth;

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
		
		double health = getHealth(p) - damage;
		
		healthMap.put(p, health);
		p.playEffect(EntityEffect.HURT);
		p.playEffect(p.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);

		update(p);
	}

	public void heal(Player p, double healing) {
		if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) return;

		double health = getHealth(p) + healing;

		if (health > defaultHealth) {
			health = defaultHealth;
		}

		healthMap.put(p, health);
		update(p);
	}
	
	public boolean isDead(Player p) {
		
		if (getHealth(p) <= 0) {
			reset(p);
			return true;
		}
		
		return false;
	}
	
	public void update(Player p) {
		p.setLevel((int) Math.round(getHealth(p)));
	}
	
	public void reset(Player p) {
		healthMap.put(p, this.defaultHealth);
		update(p);
	}
}
