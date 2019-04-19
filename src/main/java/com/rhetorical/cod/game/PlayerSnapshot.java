package com.rhetorical.cod.game;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

class PlayerSnapshot {

    private static List<PlayerSnapshot> snapshots = new ArrayList<>();

    private Player owner;
    private String listName;
    private double experience;
    private int level;
    private double health;
    private int hunger;
    private HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
    private List<PotionEffect> potionEffects = new ArrayList<>();

    PlayerSnapshot(Player p) {
        owner = p;

        if (hasSnapshot(p))
            return;

        listName = p.getPlayerListName();
        experience = p.getExp();
        level = p.getLevel();
        health = p.getHealth();
        hunger = p.getFoodLevel();
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack item = p.getInventory().getItem(i);
            if (item != null) {
                inventoryItems.put(i, item);
            }
        }

        potionEffects.addAll(p.getActivePotionEffects());

        snapshots.add(this);
    }

    static boolean hasSnapshot(Player p) {

        for (PlayerSnapshot snapshot : snapshots) {
            if(snapshot.owner.equals(p))
                return true;
        }
        return false;
    }

    static void apply(Player p) {

        if (!hasSnapshot(p))
            return;

        PlayerSnapshot snapshot = null;

        for (PlayerSnapshot ps : snapshots) {
            if (ps.owner.equals(p)) {
                snapshot = ps;
                break;
            }
        }

        if (snapshot == null)
            return;

		final Collection<PotionEffect> potionEffects = p.getActivePotionEffects();
		for (PotionEffect effect : potionEffects) {
			p.removePotionEffect(effect.getType());
		}

        p.setPlayerListName(snapshot.listName);
        p.setExp((float) snapshot.experience);
        p.setLevel(snapshot.level);
        p.setHealth(snapshot.health);
        p.setFoodLevel(snapshot.hunger);
        p.getInventory().clear();
        for (int slot : snapshot.inventoryItems.keySet()) {
            p.getInventory().setItem(slot, snapshot.inventoryItems.get(slot));
        }

        for (PotionEffect pe : snapshot.potionEffects) {
            p.addPotionEffect(pe);
        }

        snapshot.owner.updateInventory();

        snapshots.remove(snapshot);
    }
}
