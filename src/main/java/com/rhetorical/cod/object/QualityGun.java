package com.rhetorical.cod.object;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class QualityGun {

    private static Class apiClass;

    public static void setup() {
        try {
            apiClass = Class.forName("me.zombie_striker.qa.QualityArmory");
        } catch(ClassNotFoundException ignored) {}
    }

    public static ItemStack getGunForName(String name) {
        ItemStack gun = null;
        try {
            gun = (ItemStack) apiClass.getMethod("getGunItemStack", String.class).invoke(null, name);

        } catch(NoSuchMethodException ignored) {} catch(Exception ignored) { }

        return gun != null ? gun : new ItemStack(Material.AIR);
    }

    public static ItemStack getAmmoForName(String name) {
        ItemStack ammo = null;

        try {
            ammo = (ItemStack) apiClass.getMethod("getAmmoItemStack", String.class).invoke(null, name);
        } catch(NoSuchMethodException ignored) {} catch(Exception ignored) { }

        return ammo != null ? ammo : new ItemStack(Material.AIR);
    }

}
