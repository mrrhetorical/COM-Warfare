package com.rhetorical.cod.util;

import com.rhetorical.cod.ComWarfare;
import org.bukkit.inventory.ItemStack;

public class ItemBridgeUtil {

	public static ItemStack getItem(String namespace, String key) {
		return com.jojodmo.itembridge.ItemBridge.getItemStack(namespace, key);
	}

	public static ItemStack getItemBridgeItem(String name) {
		if (!ComWarfare.hasItemBridge())
			return null;
		
		for (ItemBridgePrefix prefix : ComWarfare.getItemBridgePrefixes()) {
			if (prefix.getWeapons().contains(name))
				return getItem(prefix.getPrefix(), name);
		}
		return null;
	}

}
