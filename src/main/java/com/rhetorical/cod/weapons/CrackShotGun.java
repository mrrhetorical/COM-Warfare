package com.rhetorical.cod.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CrackShotGun {

	private static Object instance;

	public static void setup() {
		try {
			instance = new com.shampaggon.crackshot.CSUtility();
		} catch(NoClassDefFoundError|Exception ignored) {
			ignored.printStackTrace();
		}
	}

	public static ItemStack generateWeapon(String name) {
		ItemStack item = null;
		try {
			item = ((com.shampaggon.crackshot.CSUtility) instance).generateWeapon(name);
		} catch(NoClassDefFoundError|Exception ignored) {
			ignored.printStackTrace();
		}

		return item == null ? new ItemStack(Material.AIR) : item;
	}


}