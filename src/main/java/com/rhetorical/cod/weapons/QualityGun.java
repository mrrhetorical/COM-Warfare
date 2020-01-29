package com.rhetorical.cod.weapons;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class QualityGun implements Listener {

	public static void setup() {
		//does nothing

		if (ComWarfare.hasQualityArms()) {
			Bukkit.getServer().getPluginManager().registerEvents(new QualityGun(), ComWarfare.getPlugin());
		}
	}

    static ItemStack getGunForName(String name) {
        return getCustomItemForName(name);
    }

    static ItemStack getAmmoForName(String name) {
        return getCustomItemForName(name);
    }

    private static ItemStack getCustomItemForName(String name) {
		ItemStack stack = null;

		try {
			stack = me.zombie_striker.qg.api.QualityArmory.getCustomItem(me.zombie_striker.qg.api.QualityArmory.getCustomItemByName(name));

		} catch(Error|Exception ignored) {
		}

		return stack != null ? stack : new ItemStack(Material.AIR);
	}

	/**
	 * Handles damage from QualityArmory's weapons separately.
	 * */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQualityWeaponDamage(me.zombie_striker.qg.api.QAWeaponDamageEntityEvent e) {
		Player victim;
		if (!(e.getDamaged() instanceof Player))
			return;

		victim = (Player) e.getDamaged();

		if (!(GameManager.isInMatch(victim) && GameManager.isInMatch(e.getPlayer())))
			return;

		e.setCanceled(true);

		GameInstance match = GameManager.getMatchWhichContains(victim);
		if (match != null)
			match.damagePlayer(victim, e.getDamage(), e.getPlayer());
	}

}
