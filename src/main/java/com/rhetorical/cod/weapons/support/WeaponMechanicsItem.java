package com.rhetorical.cod.weapons.support;

import org.bukkit.inventory.ItemStack;

public class WeaponMechanicsItem {

	public static ItemStack getWeapon(String itemName) {
		return me.deecaad.weaponmechanics.WeaponMechanicsAPI.generateWeapon(itemName);
	}

}
