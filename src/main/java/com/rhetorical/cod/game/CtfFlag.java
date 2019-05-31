package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.lang.Lang;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

enum Team {
	RED(0, ChatColor.RED), BLUE(1, ChatColor.BLUE);

	private final ChatColor color;
	private final int code;

	Team(int code, ChatColor color) {
		this.code = code;
		this.color = color;
	}

	ChatColor getColor() {
		return color;
	}

	int getCode() {
		return code;
	}
}

class CtfFlag {

	private final GameInstance owner;

	private ArmorStand flag, name;

	private final Lang flagName;
	private final Location flagLoc;

	private final Team team;

	private Player flagHolder;

	private boolean inFlagHolder;
	private boolean pickedUp;

	private CtfFlag otherFlag;

	CtfFlag(GameInstance owner, Team team, Lang flagName, Location flagLoc) {
		this.flagName = flagName;
		this.flagLoc = flagLoc.clone();
		this.team = team;
		this.owner = owner;
	}

	void spawn() {
		spawn(getLocation());
	}

	void spawn(Location location) {
		name = (ArmorStand) getLocation().getWorld().spawnEntity(location.clone().add(0, 2, 0), EntityType.ARMOR_STAND);

		name.setCustomName(getFlagName().getMessage());
		name.setCustomNameVisible(true);
		name.setVisible(false);
		name.setGravity(false);
		name.setMarker(true);

		flag = (ArmorStand) getLocation().getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		flag.setVisible(false);
		flag.setGravity(false);
		flag.setSmall(true);
		flag.setMarker(true);
		flag.setHelmet(FlagUtil.getBannerForTeam(getTeam().getCode()));

		inFlagHolder = true;
		pickedUp = false;
	}

	void pickup(Player p) {

		pickedUp = true;

		despawn();

		p.getInventory().setHelmet(FlagUtil.getBannerForTeam(getTeam().getCode()));

		flagHolder = p;

		for (Player player : getOwner().getPlayers()) {
			Main.sendMessage(player, Lang.PLAYER_PICKED_UP_FLAG.getMessage().replace("{player}", p.getName()).replace("{team}", getTeam().toString().toLowerCase()).replace("{team-color}", getTeam().getColor() + ""), Main.getLang());
		}
	}

	void drop(Player p) {

		pickedUp = false;

		flagHolder = null;

		spawn(p.getLocation());

		for (Player player : getOwner().getPlayers()) {
			Main.sendMessage(player, Lang.FLAG_DROPPED.getMessage().replace("{team}", getTeam().toString().toLowerCase()).replace("{team-color}", getTeam().getColor() + ""), Main.getLang());
		}
	}

	void despawn() {
		if (flag != null)
			flag.remove();

		if (name != null)
			name.remove();

		name = null;
		flag = null;
	}

	void resetFlag() {
		despawn();
		spawn();
	}

	void scorePoint(Player p) {

		resetFlag();

		flagHolder = null;
		pickedUp = false;

		getOwner().setTeamArmor(p);

		for (Player player : getOwner().getPlayers()) {
			Main.sendMessage(player, Lang.TEAM_SCORED.getMessage().replace("{player}", p.getName()).replace("{team}", getTeam().toString().toLowerCase()).replace("{team-color}", getTeam().getColor() + ""), Main.getLang());
		}
	}


	Lang getFlagName() {
		return flagName;
	}

	Location getLocation() {
		return flagLoc.clone();
	}


	Player getFlagHolder() {
		return flagHolder;
	}

	private GameInstance getOwner() {
		return owner;
	}

	boolean isInFlagHolder() {
		return inFlagHolder;
	}

	boolean isPickedUp() {
		return pickedUp;
	}

	Player getNearestPlayer() {

		if (pickedUp)
			return null;

		for (Entity e : name.getNearbyEntities(0.75, 0.75, 0.75)) {
			if (e instanceof Player)
				return (Player) e;
		}

		return null;
	}

	Team getTeam() {
		return team;
	}

	void setOtherFlag(CtfFlag other) {
		otherFlag = other;
	}

	CtfFlag getOtherFlag() {
		return otherFlag;
	}

	void checkNearbyPlayers() {

		if (isPickedUp())
			return;

		if (getOwner().getMap().getGamemode() != Gamemode.CTF)
			return;

		Player nearest = getNearestPlayer();
		if (nearest == null)
			return;

		if ( (getTeam() == Team.BLUE && getOwner().isOnBlueTeam(nearest)) || (getTeam() == Team.RED	&& getOwner().isOnRedTeam(nearest)) ) {
			if (nearest.equals(getOtherFlag().getFlagHolder())) {
				getOtherFlag().scorePoint(nearest);
				getOwner().incrementScore(nearest);
			} else if (!isInFlagHolder())
				resetFlag();
		} else {
			pickup(nearest);
		}

	}

}
