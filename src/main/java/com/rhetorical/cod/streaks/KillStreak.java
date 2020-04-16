package com.rhetorical.cod.streaks;

import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.util.MenuReplacementUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public enum KillStreak {
	
	UAV(Lang.UAV_NAME.getMessage(), 3, new ItemStack(MenuReplacementUtil.getInstance().getUAV())),
	COUNTER_UAV(Lang.COUNTER_UAV_NAME.getMessage(), 4, new ItemStack(MenuReplacementUtil.getInstance().getCounterUAV())),
	DOGS(Lang.DOGS_NAME.getMessage(), 9, new ItemStack(MenuReplacementUtil.getInstance().getDogs())),
	NUKE(Lang.NUKE_NAME.getMessage(), 25, new ItemStack(MenuReplacementUtil.getInstance().getNuke())),
	JUGGERNAUT(Lang.JUGGERNAUT_NAME.getMessage(), 12, new ItemStack(MenuReplacementUtil.getInstance().getJuggernautSuit())),
	AIRSTRIKE(Lang.AIRSTRIKE_NAME.getMessage(), 7, new ItemStack(MenuReplacementUtil.getInstance().getAirstrike())),
	VSAT(Lang.VSAT_NAME.getMessage(), 14, new ItemStack(MenuReplacementUtil.getInstance().getVSAT()));

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
