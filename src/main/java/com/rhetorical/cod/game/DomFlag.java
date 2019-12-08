package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.lang.Lang;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for domination and hardpoint flags
 * */
class DomFlag {
	private ArmorStand flag, name;

	private final Lang flagName;
	private final Location flagLoc;

	private int capture; // Range: -10 to 10. Lower is red, higher is blue.

	private List<Player> lastCapping = new ArrayList<>();

	private int secondsSinceLastNeutralized = 0;

	private final int secondsToNextNeutralizedMessage = 5;

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

		name = null;
		flag = null;

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
			}
		}

		return pls;
	}

	/**
	 * @return The "score" of the flag (how many players are on the flag) based on the team of the players nearby.
	 * */
	int checkFlag(GameInstance game) {

		int prevCaptureProgress = getCaptureProgress();

		int blue = 0;
		int red = 0;

		final List<Player> playersOnPoint = new ArrayList<>(getNearbyPlayers());

		for (Player p : lastCapping) {
			if (!playersOnPoint.contains(p))
				Main.sendActionBar(p, " "); //clear out the "you are in the cap zone" messages
		}

		for (Player p : playersOnPoint) {
			if (Math.abs(p.getLocation().getY() - getLocation().getY()) > 2f)
				continue;
			if (game.isOnBlueTeam(p))
				blue++;
			else if (game.isOnRedTeam(p))
				red++;
		}

		lastCapping = new ArrayList<>(playersOnPoint);

		int flagOwner = -1;

		if (getCaptureProgress() == 10 && blue >= red) {
			flagOwner = 0; // blue
		} else if (getCaptureProgress() == -10 && red >= blue) {
			flagOwner = 1; // red
		} else {
			int progress = blue - red;
			if (getFlagName().equals(Lang.FLAG_HARDPOINT))
				progress *= 2;

			setCaptureProgress(getCaptureProgress() + progress);

			if (getCaptureProgress() > 10) {
				setCaptureProgress(10);
				flagOwner = 0;
			} else if (getCaptureProgress() < -10) {
				setCaptureProgress(-10);
				flagOwner = 1;
			}

			String msg = Lang.FLAG_CAPTURED.getMessage();
			String flag = getFlagName().getMessage();

			String team = null;
			ChatColor color = null;


			if (getCaptureProgress() == 10) {
				team = "blue";
				color = ChatColor.BLUE;
				updateName(ChatColor.BLUE + getFlagName().getMessage());
				updateFlag(1);
			} else if (getCaptureProgress() == -10) {
				team = "red";
				color = ChatColor.RED;
				updateName(ChatColor.RED + getFlagName().getMessage());
				updateFlag(0);
			} else if (getCaptureProgress() == 0 && (blue > 0 || red > 0)) {
				updateName(ChatColor.WHITE + getFlagName().getMessage());
				updateFlag(-1);
				for (Player p : game.getPlayers()) {
					if (secondsSinceLastNeutralized >= getSecondsToNextNeutralizedMessage()) {
						p.sendMessage(Lang.FLAG_NEUTRALIZED.getMessage().replace("{flag}", flag));
						secondsSinceLastNeutralized = 0;
					}
				}
			}

			if (team != null) {
				for (Player p : game.getPlayers()) {
					p.sendMessage(msg.replace("{team}", team).replace("{team-color}", "" + color).replace("{flag}", flag));
				}
			}
		}

		for (Player p : playersOnPoint)
			Main.sendActionBar(p, getProgressMessage());

		secondsSinceLastNeutralized++;

		return flagOwner;
	}

	private String getProgressMessage() {
		StringBuilder builder = new StringBuilder();
		ChatColor color = getCaptureProgress() > 0 ? ChatColor.BLUE : ChatColor.RED;
		int points = Math.abs(getCaptureProgress());
		for (int i = 0; i < 10; i++) {
			builder.append(i > points ? ChatColor.GRAY : color);
			builder.append("■");
		}

		return builder.toString();
	}

	void updateFlag(int team) {
		flag.setHelmet(FlagUtil.getBannerForTeam(team));
	}

	void updateName(String value) {
		name.setCustomName(value);
	}

	private int getSecondsToNextNeutralizedMessage() {
		return secondsToNextNeutralizedMessage;
	}
}
