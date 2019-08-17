package com.rhetorical.cod.streaks;

import com.rhetorical.cod.lang.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public enum KillStreak {
	
	UAV(Lang.UAV_NAME.getMessage(), 3, new ItemStack(Material.SHEARS)),
	COUNTER_UAV(Lang.COUNTER_UAV_NAME.getMessage(), 4, new ItemStack(Material.REDSTONE)),
	DOGS(Lang.DOGS_NAME.getMessage(), 9, new ItemStack(Material.PAPER)),
	NUKE(Lang.NUKE_NAME.getMessage(), 25, new ItemStack(Material.TNT)),
	JUGGERNAUT(Lang.JUGGERNAUT_NAME.getMessage(), 12, new ItemStack(Material.BREAD));

	private int requiredKills;
	private ItemStack item;
	
	KillStreak(String _displayName, int _requiredKills, ItemStack _item) {
		requiredKills = _requiredKills;

		ItemMeta meta = _item.getItemMeta();
		meta.setDisplayName(_displayName);

		List<String> lore = new ArrayList<>();
		lore.add(Lang.KILL_STREAK_REQUIRED_KILLS.getMessage().replace("{kills}", requiredKills + ""));

		meta.setLore(lore);

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
