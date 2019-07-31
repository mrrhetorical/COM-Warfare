package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.lang.Lang;
import org.bukkit.ChatColor;
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

//Used for domination as well as hardpoint
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
		flag.setHelmet(FlagUtil.getBannerForTeam(-1));
	}

	void remove() {
		if (flag != null)
			flag.remove();

		if (name != null)
			name.remove();
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
			if (e instanceof Player) {
				pls.add((Player) e);
				Main.sendActionBar((Player) e, Lang.CAPTURING_FLAG.getMessage());
			}
		}

		return pls;
	}

	int checkFlag(GameInstance game) {

		int blue = 0;
		int red = 0;

		final List<Player> check = new ArrayList<>(getNearbyPlayers());

		for (Player p : check) {
			if (game.isOnBlueTeam(p))
				blue++;
			else if (game.isOnRedTeam(p))
				red++;
		}

		if (getCaptureProgress() == 10 && blue >= red) {
			return 0; // blue
		} else if (getCaptureProgress() == -10 && red >= blue) {
			return 1; // red
		} else {
			int progress = blue - red;
			if (getFlagName().equals(Lang.FLAG_HARDPOINT))
				progress *= 2;

			setCaptureProgress(getCaptureProgress() + progress);

			if (getCaptureProgress() > 10) {
				setCaptureProgress(10);
			}
			else if (getCaptureProgress() < -10) {
				setCaptureProgress(-10);
			}

			String msg = Lang.FLAG_CAPTURED.getMessage();
			String flag = getFlagName().getMessage();

			String team = null;
			ChatColor color = null;


			if (getCaptureProgress() == 10) {
				team = "blue";
				color = ChatColor.BLUE;
				updateName(ChatColor.BLUE + Lang.FLAG_A.getMessage());
				updateFlag(1);
			} else if (getCaptureProgress() == -10) {
				team = "red";
				color = ChatColor.RED;
				updateName(ChatColor.RED + Lang.FLAG_A.getMessage());
				updateFlag(0);
			} else if (getCaptureProgress() == 0 && (blue > 0 || red > 0)) {
				updateName(ChatColor.WHITE + Lang.FLAG_A.getMessage());
				updateFlag(-1);
				for (Player p : game.getPlayers()) {
					p.sendMessage(Lang.FLAG_NEUTRALIZED.getMessage().replace("{flag}", flag));
				}
			}

			if (team != null) {
				for (Player p : game.getPlayers()) {
					p.sendMessage(msg.replace("{team}", team).replace("{team-color}", "" + color).replace("{flag}", flag));
				}
			}
		}

		return getCaptureProgress() == 10 ? 0 : getCaptureProgress() == -10 ? 1 : -1;
	}

	void updateFlag(int team) {
		flag.setHelmet(FlagUtil.getBannerForTeam(team));
	}

	void updateName(String value) {
		name.setCustomName(value);
	}
}
