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
	
	
	private ArrayList<Location> blueSpawns = new ArrayList<Location>();
	private ArrayList<Location> redSpawns = new ArrayList<Location>();
	private ArrayList<Location> pinkSpawns = new ArrayList<Location>();
	private Location redFlagSpawn;
	private Location blueFlagSpawn;
	private Location redAFlagSpawn;
	private Location redBFlagSpawn;
	private Location redCFlagSpawn;
	private Location blueAFlagSpawn;
	private Location blueBFlagSpawn;
	private Location blueCFlagSpawn;
	
	
	public CodMap(String name, Gamemode gm) {
		this.name = name;
		this.setGamemode(gm);
	}
	
	public void save() {
		
		int k = 0;
		for (k = 0; ArenasFile.getData().contains("Maps." + k); k++) {
			if (ArenasFile.getData().getString("Maps." + k + ".name").equalsIgnoreCase(this.name)) break;
		}
		
		ArenasFile.getData().set("Maps." + k + ".blueAFlagSpawn", getBlueAFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".blueBFlagSpawn", getBlueBFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".blueCFlagSpawn", getBlueCFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".blueFlagSpawn", getBlueFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".blueSpawns", getBlueSpawns());
		ArenasFile.getData().set("Maps." + k + ".enabled", isEnabled());
		ArenasFile.getData().set("Maps." + k + ".gm", getGamemode().toString());
		ArenasFile.getData().set("Maps." + k + ".name", this.name);
		ArenasFile.getData().set("Maps." + k + ".pinkSpawns", getPinkSpawns());
		ArenasFile.getData().set("Maps." + k + ".redAFlagSpawn", getRedAFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".redBFlagSpawn", getRedBFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".redCFlagSpawn", getRedCFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".redFlagSpawn", getRedFlagSpawn());
		ArenasFile.getData().set("Maps." + k + ".redSpawns", getRedSpawns());
		ArenasFile.saveData();
		ArenasFile.reloadData();
		return;
	}
	
	public boolean setCTFflag(String team, Location l) {
		if (team.equalsIgnoreCase("red")) {
			setRedFlagSpawn(l);
			setEnable();
			return true;
		} else if (team.equalsIgnoreCase("blue")) {
			setBlueFlagSpawn(l);
			setEnable();
			return true;
		}
		setEnable();
		return false;
	}

	public void addblueSpawn(Location l) {
		if (l == null) return;
		
		getBlueSpawns().add(l);
		setEnable();
		return;
	}
	
	public void addRedSpawn(Location l) {
		if (l == null) return;
		
		getRedSpawns().add(l);
		setEnable();
		return;
	}
	
	public void addPinkSpawn(Location l) {
		if (l == null) return;
		
		getPinkSpawns().add(l);
		setEnable();
		return;
	}
	
	//Check if enabled
	public boolean setEnable() {
		
		if (getGamemode().equals(Gamemode.TDM)) {
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
		} else if (getGamemode().equals(Gamemode.FFA)) {
			if (getPinkSpawns() != null && getPinkSpawns().size() >= Main.maxPlayers) {
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

	public Location getBlueAFlagSpawn() {
		return blueAFlagSpawn;
	}

	public void setBlueAFlagSpawn(Location blueAFlagSpawn) {
		this.blueAFlagSpawn = blueAFlagSpawn;
	}

	public Location getBlueBFlagSpawn() {
		return blueBFlagSpawn;
	}

	public void setBlueBFlagSpawn(Location blueBFlagSpawn) {
		this.blueBFlagSpawn = blueBFlagSpawn;
	}

	public Location getBlueCFlagSpawn() {
		return blueCFlagSpawn;
	}

	public void setBlueCFlagSpawn(Location blueCFlagSpawn) {
		this.blueCFlagSpawn = blueCFlagSpawn;
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

	public void setBlueFlagSpawn(Location blueFlagSpawn) {
		this.blueFlagSpawn = blueFlagSpawn;
	}

	public ArrayList<Location> getPinkSpawns() {
		return pinkSpawns;
	}

	public void setPinkSpawns(ArrayList<Location> pinkSpawns) {
		this.pinkSpawns = pinkSpawns;
	}

	public Location getRedBFlagSpawn() {
		return redBFlagSpawn;
	}

	public void setRedBFlagSpawn(Location redBFlagSpawn) {
		this.redBFlagSpawn = redBFlagSpawn;
	}

	public Location getRedAFlagSpawn() {
		return redAFlagSpawn;
	}

	public void setRedAFlagSpawn(Location redAFlagSpawn) {
		this.redAFlagSpawn = redAFlagSpawn;
	}

	public Location getRedCFlagSpawn() {
		return redCFlagSpawn;
	}

	public void setRedCFlagSpawn(Location redCFlagSpawn) {
		this.redCFlagSpawn = redCFlagSpawn;
	}

	public Location getRedFlagSpawn() {
		return redFlagSpawn;
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
		return gm;
	}

	public void setGamemode(Gamemode gm) {
		this.gm = gm;
	}






























}
