package com.rhetorical.cod.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CrackShotGun {

	private static Class<?> apiClass;

	public static void setup() {
		try {
			apiClass = Class.forName("com.shampaggon.crackshot.CSUtility");
		} catch(ClassNotFoundException ignored) {
			ignored.printStackTrace();
		}
		catch(Exception ignored) {
			ignored.printStackTrace();
		}
	}

	public static ItemStack generateWeapon(String name) {
		ItemStack item = null;
		try {
			item = (ItemStack) apiClass.getMethod("generateWeapon", String.class).invoke(apiClass.newInstance(), name);
		} catch(NoSuchMethodException ignored) {
			ignored.printStackTrace();
		}
		catch(Exception ignored) {
			ignored.printStackTrace();
		}

		return item == null ? new ItemStack(Material.AIR) : item;
	}


}