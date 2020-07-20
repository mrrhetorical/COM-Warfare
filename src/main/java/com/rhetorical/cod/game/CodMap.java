package com.rhetorical.cod.game;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.ArenasFile;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Map data class to be used by GameInstance.
 * @see GameInstance
 * */

public class CodMap {
	
	private String name;
	private boolean enabled = false;
	
	
	private ArrayList<Location> blueSpawns = new ArrayList<>();
	private ArrayList<Location> redSpawns = new ArrayList<>();
	private ArrayList<Location> pinkSpawns = new ArrayList<>();
	private Location redFlagSpawn;
	private Location blueFlagSpawn;
	private Location Flag_A;
	private Location Flag_B;
	private Location Flag_C;
	private List<Location> hardpointFlags = new ArrayList<>();
	private List<Gamemode> blacklistedModes = new ArrayList<>();

	private List<Gamemode> availableGamemodes = new ArrayList<>();

	private Gamemode currentGamemode;
	
	
	public CodMap(String name) {
		this.name = name;
	}
	
	public void save() {
		
		int k;
		for (k = 0; ArenasFile.getData().contains("Maps." + k); k++) {
			if (ArenasFile.getData().getString("Maps." + k + ".name").equalsIgnoreCase(this.name)) break;
		}

		List<String> blm = new ArrayList<>();
		for (Gamemode gm : blacklistedModes) {
			blm.add(gm.toString());
		}
		
		ArenasFile.getData().set("Maps." + k + ".AFlag", getAFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".BFlag", getBFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".CFlag", getCFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".hardpointFlags", getHardpointFlags());
		ArenasFile.getData().set("Maps." + k + ".blueFlagSpawn", getBlueFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".blueSpawns", getBlueSpawns());
		ArenasFile.getData().set("Maps." + k + ".enabled", isEnabled());
		ArenasFile.getData().set("Maps." + k + ".name", this.name);
		ArenasFile.getData().set("Maps." + k + ".pinkSpawns", getPinkSpawns());
		ArenasFile.getData().set("Maps." + k + ".redFlagSpawn", getRedFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".redSpawns", getRedSpawns());
		ArenasFile.getData().set("Maps." + k + ".blacklist", blm);
		ArenasFile.saveData();
		ArenasFile.reloadData();
	}

	public void addblueSpawn(Location l) {
		if (l == null) return;
		
		getBlueSpawns().add(l);
		setEnable();
	}
	
	public void addRedSpawn(Location l) {
		if (l == null) return;
		
		getRedSpawns().add(l);
		setEnable();
	}
	
	public void addPinkSpawn(Location l) {
		if (l == null) return;
		
		getPinkSpawns().add(l);
		setEnable();
	}

	public void addHardPointFlag(Location l) {
		if (l == null) return;

		getHardpointFlags().add(l);
		setEnable();
	}

	public boolean setEnable() {
		if (getBlueSpawns() != null && getRedSpawns() != null) {
			if (getBlueSpawns().size() >= 1 && getRedSpawns().size() >= 1) {
				if (!availableGamemodes.contains(Gamemode.TDM) && !blacklistedModes.contains(Gamemode.TDM))
					availableGamemodes.add(Gamemode.TDM);
				if (!availableGamemodes.contains(Gamemode.GUNFIGHT) && !blacklistedModes.contains(Gamemode.GUNFIGHT))
					availableGamemodes.add(Gamemode.GUNFIGHT);
				if (!availableGamemodes.contains(Gamemode.RSB) && !blacklistedModes.contains(Gamemode.RSB))
					availableGamemodes.add(Gamemode.RSB);
				if (!availableGamemodes.contains(Gamemode.INFECT) && !blacklistedModes.contains(Gamemode.INFECT))
					availableGamemodes.add(Gamemode.INFECT);
				if (!availableGamemodes.contains(Gamemode.KC) && !blacklistedModes.contains(Gamemode.KC))
					availableGamemodes.add(Gamemode.KC);
				if (!availableGamemodes.contains(Gamemode.RESCUE) && !blacklistedModes.contains(Gamemode.RESCUE))
					availableGamemodes.add(Gamemode.RESCUE);

				if (getBlueFlagSpawn() != null && getRedFlagSpawn() != null) {
					if (!availableGamemodes.contains(Gamemode.CTF) && !blacklistedModes.contains(Gamemode.CTF))
						availableGamemodes.add(Gamemode.CTF);
				}

				if (getAFlagSpawn() != null && getBFlagSpawn() != null && getCFlagSpawn() != null) {
					if (!availableGamemodes.contains(Gamemode.DOM) && !blacklistedModes.contains(Gamemode.DOM))
						availableGamemodes.add(Gamemode.DOM);
				}

				if (getHardpointFlags().size() >= 1) {
					if (!getAvailableGamemodes().contains(Gamemode.HARDPOINT) && !blacklistedModes.contains(Gamemode.HARDPOINT))
						getAvailableGamemodes().add(Gamemode.HARDPOINT);
				}
			}
		}

		if (getPinkSpawns() != null) {
			if (getPinkSpawns().size() >= ComWarfare.getMaxPlayers()) {
				if (!availableGamemodes.contains(Gamemode.FFA) && !blacklistedModes.contains(Gamemode.FFA))
					availableGamemodes.add(Gamemode.FFA);
				if(!availableGamemodes.contains(Gamemode.OITC) && !blacklistedModes.contains(Gamemode.OITC)) {
					if (GameManager.oitcGun != null)
						availableGamemodes.add(Gamemode.OITC);
				}
				if (!availableGamemodes.contains(Gamemode.GUN) && !blacklistedModes.contains(Gamemode.GUN)) {
					if (GameManager.gunGameGuns != null && !GameManager.gunGameGuns.isEmpty()) {
						availableGamemodes.add(Gamemode.GUN);
					}
				}
			}
		}

		this.save();
		boolean shouldEnable = getAvailableGamemodes().size() > 0;
		this.setEnabled(shouldEnable);
		return shouldEnable;
	}

	public void addToBlacklist(Gamemode mode) {
		blacklistedModes.add(mode);
		save();
	}
	
	//Only removes most recent spawn
	public void removeSpawn(String team) {
		switch (team.toUpperCase()) {
		case "RED":
			getRedSpawns().remove(getRedSpawns().size() - 1);
			break;
		case "BLUE":
			getBlueSpawns().remove(getBlueSpawns().size() - 1);
			break;
		case "PINK":
			getPinkSpawns().remove(getPinkSpawns().size() - 1);
			break;
		default:
			break;
		}
		setEnable();
	}
	
	public Location getBlueSpawn() {
		Collections.shuffle(this.getBlueSpawns());
		return this.getBlueSpawns().get(0);
		
	}
	
	public Location getRedSpawn() {
		Collections.shuffle(this.getRedSpawns());
		return this.getRedSpawns().get(0);
	}
	
	public Location getPinkSpawn() {
		Collections.shuffle(this.getPinkSpawns());
		return this.getPinkSpawns().get(0);
	}
	
	public String getName() {
		return this.name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ArrayList<Location> getBlueSpawns() {
		return blueSpawns;
	}

	public void setBlueSpawns(ArrayList<Location> blueSpawns) {
		this.blueSpawns = blueSpawns;
	}

	public Location getBlueFlagSpawn() {
		return blueFlagSpawn;
	}

	public void addBlueFlagSpawn(Location blueFlagSpawn) {
		this.blueFlagSpawn = blueFlagSpawn;
		setEnable();
	}

	public void setBlueFlagSpawn(Location blueFlagSpawn) {
		this.blueFlagSpawn = blueFlagSpawn;
	}

	public ArrayList<Location> getPinkSpawns() {
		return pinkSpawns;
	}

	public void setPinkSpawns(ArrayList<Location> pinkSpawns) {
		this.pinkSpawns = pinkSpawns;
	}

	public List<Location> getHardpointFlags() {
		return hardpointFlags;
	}

	public Location getRedFlagSpawn() {
		return redFlagSpawn;
	}

	public void addRedFlagSpawn(Location redFlagSpawn) {
		this.redFlagSpawn = redFlagSpawn;
		setEnable();
	}

	public void setRedFlagSpawn(Location redFlagSpawn) {
		this.redFlagSpawn = redFlagSpawn;
	}


	public ArrayList<Location> getRedSpawns() {
		return redSpawns;
	}

	public void setRedSpawns(ArrayList<Location> redSpawns) {
		this.redSpawns = redSpawns;
	}

	public Gamemode getGamemode() {
		return currentGamemode;
	}

	// Gets a random gamemode from the list of available game modes
	public Gamemode changeGamemode() {
		int index = (int) Math.round(Math.random() * (availableGamemodes.size() - 1)) ;

		currentGamemode = availableGamemodes.get(index);
		return getGamemode();
	}

	public void setGamemode(Gamemode mode) {
		currentGamemode = mode;
	}

	//Changes the gamemode (if possible) to the given gamemode.
	public boolean changeGamemode(Gamemode toChange) {
		if (!getAvailableGamemodes().contains(toChange))
			return false;

		currentGamemode = toChange;
		return true;
	}

	public Location getAFlagSpawn() {
		return this.Flag_A;
	}

	public void addAFlagSpawn(Location loc) {
		this.Flag_A = loc;
		setEnable();
	}

	public void setAFlagSpawn(Location loc) {
		this.Flag_A = loc;
	}

	public Location getBFlagSpawn() {
		return this.Flag_B;

	}


	public void addBFlagSpawn(Location loc) {
		this.Flag_B = loc;
		setEnable();
	}

	public void setBFlagSpawn(Location loc) {
		this.Flag_B = loc;
	}

	public Location getCFlagSpawn() {
		return this.Flag_C;
	}

	public void addCFlagSpawn(Location loc) {
		this.Flag_C = loc;
		setEnable();
	}

	public void setCFlagSpawn(Location loc) {
		this.Flag_C =  loc;
	}

	public void setHardpointFlags(List<Location> locations) {
		this.hardpointFlags = locations;
	}

	public List<Gamemode> getAvailableGamemodes() {
		return this.availableGamemodes;
	}

	public Gamemode getRandomGameMode() {
		List<Gamemode> modes = getAvailableGamemodes();
		if (modes.size() == 0)
			return null;
		Collections.shuffle(modes);
		return modes.get(0);
	}
}
