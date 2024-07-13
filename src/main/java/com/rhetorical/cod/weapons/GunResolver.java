package com.rhetorical.cod.weapons;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.weapons.support.ItemBridgeUtil;
import com.rhetorical.cod.weapons.support.CrackShotGun;
import com.rhetorical.cod.weapons.support.QualityGun;
import com.rhetorical.cod.weapons.support.WeaponMechanicsItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class GunResolver {

	private static GunResolver instance;

	private GunResolver() {}

	public static GunResolver getInstance() {
		if (instance == null) {
			instance = new GunResolver();
		}
		return instance;
	}

	public ItemStack getGunItem(String itemName) {
		ItemStack item = null;

		if (ComWarfare.hasItemBridge()) {
			item = ItemBridgeUtil.getItemBridgeItem(itemName, null);
		}

		if (ComWarfare.hasWeaponMechanics()) {
			try {
				item = WeaponMechanicsItem.getWeapon(itemName);
			} catch (NullPointerException e) {
				if (itemName != null && !itemName.isEmpty() && !itemName.equalsIgnoreCase("AIR")) {
					ComWarfare.getInstance().getLogger().log(Level.SEVERE, String.format("Could not get weapon %s from weapon mechanics. It probably hasn't loaded yet.", itemName));
				}
			}
		}

		if (ComWarfare.hasQualityArms() && item == null) {
			item = QualityGun.getGunForName(itemName);
		}

		if (ComWarfare.hasCrackShot() && item == null) {
			item = CrackShotGun.generateWeapon(itemName);
			if (item != null) {
				item = CrackShotGun.updateItem(itemName, item, null);
			}
		}


		return item != null ? item : new ItemStack(Material.BOW, 1);
	}

}
