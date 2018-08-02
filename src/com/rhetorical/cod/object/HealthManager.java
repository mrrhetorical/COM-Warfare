package com.rhetorical.cod.object;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class HealthManager {
	public HashMap<Player, Double> healthMap = new HashMap<Player, Double>();
	
	public double defaultHealth;
	
	public HealthManager(ArrayList<Player> pls, double health) {
		
		this.defaultHealth = health;
		
		for (Player p : pls) {
			healthMap.put(p, health);
		}
		
		return;
	}
	
	public void addPlayer(Player p) {
		getHealth(p);
		update(p);
		return;
	}
	
	public void removePlayer(Player p) {
		if (healthMap.containsKey(p)) {
			healthMap.remove(p);
			return;
		}
		return;
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
		
		update(p);
		return;
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
		return;
	}
	
	public void reset(Player p) {
		healthMap.put(p, this.defaultHealth);
		update(p);
		return;
	}
}
