package com.rhetorical.cod.object;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum KillStreak {
	
	UAV(3, new ItemStack(Material.SHEARS)), COUNTER_UAV(4, new ItemStack(Material.REDSTONE)), NUKE(25, new ItemStack(Material.TNT));

	private int requiredKills;
	private ItemStack item;
	
	KillStreak(int _requiredKills, ItemStack _item) {
		this.requiredKills = _requiredKills;
		this.item = _item;
	}
	
	public int getRequiredKills() {
		return this.requiredKills;
	}
	
	public ItemStack getKillStreakItem() {
		return this.item;
	}
}
