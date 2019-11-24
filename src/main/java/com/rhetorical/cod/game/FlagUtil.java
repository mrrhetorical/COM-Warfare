package com.rhetorical.cod.game;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;

/**
 * Simple util class for flags.
 * */

class FlagUtil {

	/**
	 * @return Returns a banner for the target team given it's id.
	 * */
	static ItemStack getBannerForTeam(int team) {

		ItemStack stack;

		try {
			stack = new ItemStack(Material.WHITE_BANNER);
		} catch(NoSuchFieldError|Exception e) {
			stack = new ItemStack(Material.valueOf("BANNER"));
		}
		BannerMeta banner = (BannerMeta) stack.getItemMeta();

		Pattern pattern;

		switch (team) {
			case 0:
				pattern = new Pattern(DyeColor.RED, PatternType.BASE);
				banner.setPatterns(new ArrayList<>());
				banner.addPattern(pattern);
				break;
			case 1:
				pattern = new Pattern(DyeColor.BLUE, PatternType.BASE);
				banner.setPatterns(new ArrayList<>());
				banner.addPattern(pattern);
				break;
			default:
				pattern = new Pattern(DyeColor.WHITE, PatternType.BASE);
				banner.setPatterns(new ArrayList<>());
				banner.addPattern(pattern);
				break;
		}

		stack.setItemMeta(banner);

		return stack;
	}
}
