package com.rhetorical.cod.object;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Location;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.ArenasFile;

public class CodMap {
	
	private String name;
	private Gamemode gm;
	private boolean enabled = false;
	
	
	private ArrayList<Location> blueSpawns = new ArrayList<>();
	private ArrayList<Location> redSpawns = new ArrayList<>();
	private ArrayList<Location> pinkSpawns = new ArrayList<>();
	private Location redFlagSpawn;
	private Location blueFlagSpawn;
	private Location Flag_A;
	private Location Flag_B;
	private Location Flag_C;
	
	
	public CodMap(String name, Gamemode gm) {
		this.name = name;
		this.setGamemode(gm);
	}
	
	public void save() {
		
		int k;
		for (k = 0; ArenasFile.getData().contains("Maps." + k); k++) {
			if (ArenasFile.getData().getString("Maps." + k + ".name").equalsIgnoreCase(this.name)) break;
		}
		
		ArenasFile.getData().set("Maps." + k + ".AFlag", getAFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".BFlag", getBFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".CFlag", getCFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".blueFlagSpawn", getBlueFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".blueSpawns", getBlueSpawns());
		ArenasFile.getData().set("Maps." + k + ".enabled", isEnabled());
		ArenasFile.getData().set("Maps." + k + ".gm", getGamemode().toString());
		ArenasFile.getData().set("Maps." + k + ".name", this.name);
		ArenasFile.getData().set("Maps." + k + ".pinkSpawns", getPinkSpawns());
		ArenasFile.getData().set("Maps." + k + ".redFlagSpawn", getRedFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".redSpawns", getRedSpawns());
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
	
	//Check if enabled
	public boolean setEnable() {
		
		if (getGamemode().equals(Gamemode.TDM) || getGamemode().equals(Gamemode.RSB) || getGamemode().equals(Gamemode.INFECT) || getGamemode().equals(Gamemode.KC) || getGamemode().equals(Gamemode.RESCUE)) {
			if (getBlueSpawns() != null && getRedSpawns() != null) {
				if (getBlueSpawns().size() >= 1 && getRedSpawns().size() >= 1) {
					this.setEnabled(true);
					this.save();
					return true;
				}
				this.setEnabled(false);
				this.save();
				return false;
			}
			this.setEnabled(false);
			this.save();
			return false;
		} else if (getGamemode().equals(Gamemode.FFA) || getGamemode().equals(Gamemode.OITC) || getGamemode().equals(Gamemode.GUN)) {
			if (getPinkSpawns() != null && getPinkSpawns().size() >= Main.maxPlayers) {
				this.setEnabled(true);
				this.save();
				return true;
			}
			
			this.setEnabled(false);
			this.save();
			return false;
		} else if (getGamemode().equals(Gamemode.CTF)) {
			if (getBlueSpawns() != null && getRedSpawns() != null && getBlueFlagSpawn() != null && getRedFlagSpawn() != null) {
				if (getBlueSpawns().size() >= 1 && getRedSpawns().size() >= 1) {
					this.setEnabled(true);
					this.save();
					return true;
				}
				this.setEnabled(false);
				this.save();
				return false;
			}

			this.setEnabled(false);
			this.save();
			return false;
		} else if (getGamemode().equals(Gamemode.DOM)) {
			if (getBlueSpawns() != null && getRedSpawns() != null && getAFlagSpawn() != null && getBFlagSpawn() != null && getCFlagSpawn() != null) {
				if (getBlueSpawns().size() >= 1 && getRedSpawns().size() >= 1) {
					this.setEnabled(true);
					this.save();
					return true;
				}

				this.setEnabled(false);
				this.save();
				return true;
			}
		}
		
		this.setEnabled(false);
		this.save();
		return false;
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
	
	Location getBlueSpawn() {
		Collections.shuffle(this.getBlueSpawns());
		return this.getBlueSpawns().get(0);
		
	}
	
	Location getRedSpawn() {
		Collections.shuffle(this.getRedSpawns());
		return this.getRedSpawns().get(0);
	}
	
	Location getPinkSpawn() {
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

	private ArrayList<Location> getBlueSpawns() {
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

	private ArrayList<Location> getPinkSpawns() {
		return pinkSpawns;
	}

	public void setPinkSpawns(ArrayList<Location> pinkSpawns) {
		this.pinkSpawns = pinkSpawns;
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


	private ArrayList<Location> getRedSpawns() {
		return redSpawns;
	}

	public void setRedSpawns(ArrayList<Location> redSpawns) {
		this.redSpawns = redSpawns;
	}

	public Gamemode getGamemode() {
		return gm;
	}

	private void setGamemode(Gamemode gm) {
		this.gm = gm;
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
}
