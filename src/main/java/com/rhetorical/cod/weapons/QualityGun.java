package com.rhetorical.cod.weapons;

import com.rhetorical.cod.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class QualityGun {

	public static void setup() {
		//does nothing
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

		} catch(Error|Exception e) {
			if (Main.hasQualityArms()) {
				Bukkit.getLogger().severe(String.format("Could not get QA item with name %s!", name));
				e.printStackTrace();
			}
		}

		return stack != null ? stack : new ItemStack(Material.AIR);
	}

}
