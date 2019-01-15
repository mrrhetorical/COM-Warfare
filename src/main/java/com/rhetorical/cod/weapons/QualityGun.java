package com.rhetorical.cod.weapons;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class QualityGun {

    private static Class<?> apiClass;

    public static void setup() {
        try {
            apiClass = Class.forName("me.zombie_striker.qg.api.QualityArmory");
        } catch(ClassNotFoundException ignored) {} catch(Exception ignored) {}
    }

    public static ItemStack getGunForName(String name) {
        ItemStack gun = null;
//        gun = me.zombie_striker.qg.api.QualityArmory.getGunItemStack(name);
        try {
            gun = (ItemStack) apiClass
					.getMethod("getGunItemStack", String.class)
					.invoke(null, name);

        } catch(NoSuchMethodException ignored) {
//        	ignored.printStackTrace();
		} catch(Exception ignored) {
//        	ignored.printStackTrace();
		}

        return gun != null ? gun : new ItemStack(Material.AIR);
    }

    public static ItemStack getAmmoForName(String name) {
        ItemStack ammo = null;

        try {
            ammo = (ItemStack) apiClass
					.getMethod("getAmmoItemStack", String.class)
					.invoke(null, name);
        } catch(NoSuchMethodException ignored) {
//        	ignored.printStackTrace();
		} catch(Exception ignored) {
//        	ignored.printStackTrace();
		}

//		ammo = me.zombie_striker.qg.api.QualityArmory.getAmmoItemStack(name);

        return ammo != null ? ammo : new ItemStack(Material.AIR);
    }

}
