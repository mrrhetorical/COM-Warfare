package com.rhetorical.cod.weapons;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class CrackShotGun implements Listener {

	private static Object instance;

	public static void setup() {
		boolean installed = true;
		try {
			instance = new com.shampaggon.crackshot.CSUtility();
		} catch(Error|Exception ignored) {
			installed = false;
		}

		if (installed)
			Bukkit.getServer().getPluginManager().registerEvents(new CrackShotGun(), Main.getPlugin());
	}

	public static ItemStack generateWeapon(String name) {
		ItemStack item = null;
		try {
			item = ((com.shampaggon.crackshot.CSUtility) instance).generateWeapon(name);
		} catch(Error|Exception ignored) {}

		return item == null ? new ItemStack(Material.AIR) : item;
	}

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

	//ba-dum-tss
	@EventHandler(priority = EventPriority.HIGH)
	public void onCrackShot(com.shampaggon.crackshot.events.WeaponDamageEntityEvent e) {

		Player victim;
		if (!(e.getVictim() instanceof Player))
			return;

		victim = (Player) e.getVictim();

		if (!GameManager.isInMatch(victim) && !GameManager.isInMatch(e.getPlayer()))
			return;

		e.setCancelled(true);

		double damage = e.getDamage();

		GameInstance match = GameManager.getMatchWhichContains(victim);
		if (match != null)
			match.damagePlayer(victim, damage, e.getPlayer());
	}

}