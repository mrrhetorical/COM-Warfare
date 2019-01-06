package com.rhetorical.cod.object;

import com.rhetorical.cod.lang.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum KillStreak {
	
	UAV(Lang.UAV_NAME.getMessage(), 3, new ItemStack(Material.SHEARS)),
	COUNTER_UAV(Lang.COUNTER_UAV_NAME.getMessage(), 4, new ItemStack(Material.REDSTONE)),
	DOGS(Lang.DOGS_NAME.getMessage(), 9, new ItemStack(Material.PAPER)),
	NUKE(Lang.NUKE_NAME.getMessage(), 25, new ItemStack(Material.TNT));

	private String displayName;
	private int requiredKills;
	private ItemStack item;
	
	KillStreak(String _displayName, int _requiredKills, ItemStack _item) {
		requiredKills = _requiredKills;
		displayName = _displayName;

		ItemMeta meta = _item.getItemMeta();
		meta.setDisplayName(displayName);
		_item.setItemMeta(meta);

		item = _item;
	}
	
	public int getRequiredKills() {
		return this.requiredKills;
	}
	
	public ItemStack getKillStreakItem() {
		return this.item;
	}
}
