package com.rhetorical.cod.game;

import com.rhetorical.cod.lang.Lang;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.List;

class DomFlag {
	private ArmorStand flag, name;

	private final Lang flagName;
	private final Location flagLoc;

	private int capture; // Range: -10 to 10. Lower is red, higher is blue.

	DomFlag(Lang flagName, Location flagLoc) {
		this.flagName = flagName;
		this.flagLoc = flagLoc.clone();
	}

	void spawn() {
		name = (ArmorStand) getLocation().getWorld().spawnEntity(getLocation().add(0, 2, 0), EntityType.ARMOR_STAND);

		name.setCustomName(getFlagName().getMessage());
		name.setCustomNameVisible(true);
		name.setVisible(false);
		name.setGravity(false);
		name.setMarker(true);

		flag = (ArmorStand) getLocation().getWorld().spawnEntity(getLocation(), EntityType.ARMOR_STAND);
		flag.setVisible(false);
		flag.setGravity(false);
		flag.setSmall(true);
		flag.setMarker(true);
		flag.setHelmet(getBannerForTeam(-1));
	}

	void remove() {
		if (flag != null)
			flag.remove();

		if (name != null)
			name.remove();
	}

	private ItemStack getBannerForTeam(int team) {
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

	Lang getFlagName() {
		return flagName;
	}

	Location getLocation() {
		return flagLoc.clone();
	}

	int getCaptureProgress() {
		return capture;
	}

	void setCaptureProgress(int progress) {
		capture = progress;
	}

	List<Player> getNearbyPlayers() {
		List<Player> pls = new ArrayList<>();

		for (Entity e : name.getNearbyEntities(10, 5, 10)) {
			if (e instanceof Player)
				pls.add((Player) e);
		}

		return pls;
	}

	void updateFlag(int team) {
		flag.setHelmet(getBannerForTeam(team));
	}

	void updateName(String value) {
		name.setCustomName(value);
	}
}
