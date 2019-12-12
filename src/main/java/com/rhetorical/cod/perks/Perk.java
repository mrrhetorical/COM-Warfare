package com.rhetorical.cod.perks;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains basic information about each perk.
 * */

public enum Perk {

	//cold blooded stops ai streaks (dogs and heli) from attacking player
	//ghost/ninja stops uav from affecting them
	//danger close allows players who would've died in 1 hit to stay alive at 1hp

	///// PERK SLOT ONE /////
	MARATHON("Marathon", "Never run out of stamina!"), SCAVENGER("Scavenger", "Pick up ammo from players you kill!"), ONE_MAN_ARMY("One Man Army", "Switch between loadouts without dying."), HARDLINE("Hardline", "Killstreaks take 1 less kill."),
	
	///// PERK SLOT TWO /////
	STOPPING_POWER("Stopping Power", "Deal more damage!"), COLD_BLOODED("Cold Blooded", "Can't be targeted by AI killstreaks."), DANGER_CLOSE("Danger Close", "When you'd normally be instantly killed, you instead have 1 hp."), JUGGERNAUT("Juggernaut", "Take less incoming damage."),
	
	///// PERK SLOT THREE /////
	COMMANDO("Commando", "Knife damage is always a one hit kill!"), GHOST("Ghost", "Can't be seen by UAV or VSAT"), LAST_STAND("Last Stand", "Go into a final stand before dying.");
	
	private String name;
	private ArrayList<String> lore;
	
	Perk(String name, String lore) {
		this.setName(name);
		this.getLore().add(lore);
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getLore() {
		if (lore == null)
			lore = new ArrayList<>();
		return lore;
	}

	void setLore(ArrayList<String> lore) {
		this.lore = lore;
	}

	public String getName() {
		return this.name;
	}
}