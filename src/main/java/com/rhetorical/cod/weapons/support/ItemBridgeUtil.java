package com.rhetorical.cod.weapons.support;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.util.ItemBridgePrefix;
import org.bukkit.inventory.ItemStack;

public class ItemBridgeUtil {

	public static ItemStack getItem(String namespace, String key) {
		return com.jojodmo.itembridge.ItemBridge.getItemStack(namespace, key);
	}

	public static ItemStack getItemBridgeItem(String name, ItemStack itemStack) {
		if (!ComWarfare.hasItemBridge())
			return itemStack;

		for (ItemBridgePrefix prefix : ComWarfare.getItemBridgePrefixes()) {
			if (prefix.getWeapons().contains(name))
				return getItem(prefix.getPrefix(), name);
		}
		return itemStack;
	}

}
