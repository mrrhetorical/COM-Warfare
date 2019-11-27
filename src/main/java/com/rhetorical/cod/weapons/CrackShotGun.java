package com.rhetorical.cod.weapons;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.inventories.ShopManager;
import me.zombie_striker.qg.config.CrackshotLoader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class CrackShotGun implements Listener {

	private static Object instance;

	private Set<String> weapons = new HashSet<>();

	public static void setup() {
		boolean installed = true;
		try {
			instance = new com.shampaggon.crackshot.CSUtility();
		} catch(Error|Exception ignored) {
			installed = false;
		}

		if (installed) {
			CrackShotGun instance = new CrackShotGun();
			for (CodWeapon w : ShopManager.getInstance().getLethalWeapons()) {
				instance.weapons.add(w.getName());
			}
			for (CodWeapon w : ShopManager.getInstance().getTacticalWeapons()) {
				instance.weapons.add(w.getName());
			}
			Bukkit.getServer().getPluginManager().registerEvents(instance, Main.getPlugin());
		}
	}

	/**
	 * @return The CrackShot representation of the weapon in question or nothing if none exists.
	 * */
	public static ItemStack generateWeapon(String name) {
		ItemStack item = null;
		try {
			item = ((com.shampaggon.crackshot.CSUtility) instance).generateWeapon(name);
		} catch(Error|Exception ignored) {}

		return item == null ? new ItemStack(Material.AIR) : item;
	}

	/**
	 * Updates the item as a gun from CrackShot given the owning player's features such as skins, attachments, etc. stored in CrackShot.
	 * */
	public static ItemStack updateItem(String name, ItemStack item, Player player) {
		try {
			if(Bukkit.getPluginManager().getPlugin("CrackShotPlus") != null) {
				if (player == null)
					return me.DeeCaaD.CrackShotPlus.CSPapi.updateItemStackFeaturesNonPlayer(name, item);
				else
					return me.DeeCaaD.CrackShotPlus.CSPapi.updateItemStackFeatures(name, item, player);
			}
		} catch (Error|Exception ignored) {}
		return item;
	}

	/**
	 * Ba-dum-tss....
	 * Damage handler for CrackShot weapons.
	 * */
	@EventHandler(priority = EventPriority.HIGH)
	public void onCrackShot(com.shampaggon.crackshot.events.WeaponDamageEntityEvent e) {

		Player victim;
		if (!(e.getVictim() instanceof Player))
			return;

		victim = (Player) e.getVictim();

		double damage = e.getDamage();

		if (!weapons.contains(e.getWeaponTitle()))
			e.setCancelled(true);
		else
			e.setDamage(0);

		GameInstance match = GameManager.getMatchWhichContains(victim);
		if (match != null) {
			if (match.canDamage(e.getPlayer(), victim) || e.getPlayer().equals(victim))
				match.damagePlayer(victim, damage, e.getPlayer());
		}
	}

}