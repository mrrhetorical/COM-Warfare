package com.rhetorical.cod.weapons;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CrackShotGun implements Listener {

	private static Object instance;

	public static void setup() {
		boolean installed = true;
		try {
			instance = new com.shampaggon.crackshot.CSUtility();
		} catch(NoClassDefFoundError|Exception ignored) {
			installed = false;
		}

		if (installed)
			Bukkit.getServer().getPluginManager().registerEvents(new CrackShotGun(), Main.getPlugin());
	}

	public static ItemStack generateWeapon(String name) {
		ItemStack item = null;
		try {
			item = ((com.shampaggon.crackshot.CSUtility) instance).generateWeapon(name);
		} catch(NoClassDefFoundError|Exception ignored) {}

		return item == null ? new ItemStack(Material.AIR) : item;
	}

	@EventHandler
	public void onCrackShotExplode(com.shampaggon.crackshot.events.WeaponDamageEntityEvent e) {
//		if (!(e.getDamager() instanceof TNTPrimed))
//			return;

		Player victim;
		if (!(e.getVictim() instanceof Player))
			return;

		victim = (Player) e.getVictim();

		if (GameManager.isInMatch(victim) || GameManager.isInMatch(e.getPlayer())) {
			e.setCancelled(true);
		}

		if (!(GameManager.isInMatch(victim) && GameManager.isInMatch(e.getPlayer())))
			return;

		GameInstance match = GameManager.getMatchWhichContains(victim);
		if (match != null)
			match.damagePlayer(victim, e.getDamage(), e.getPlayer());
	}


}