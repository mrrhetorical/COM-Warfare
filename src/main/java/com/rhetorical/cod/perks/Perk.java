package com.rhetorical.cod.perks;

/**
 * Contains basic information about each perk.
 * */

public enum Perk {

	//completed 1: marathon, scavenger, one man army, hardline

	//completed 2: stopping power, juggernaut

	//completed 3: commando, last stand

	//todo: implement cold blooded, danger close, and ninja/ghost

	//cold blooded stops ai streaks (dogs and heli) from attacking player
	//ghost/ninja stops uav from affecting them
	//danger close allows players who would've died in 1 hit to stay alive at 1hp

	///// PERK SLOT ONE /////
	MARATHON("Marathon", PerkSlot.ONE), SCAVENGER("Scavenger", PerkSlot.ONE), ONE_MAN_ARMY("One Man Army", PerkSlot.ONE), HARDLINE("Hardline", PerkSlot.ONE),
	
	///// PERK SLOT TWO /////
	STOPPING_POWER("Stopping Power", PerkSlot.TWO), COLD_BLOODED("Cold Blooded", PerkSlot.TWO), DANGER_CLOSE("Danger Close", PerkSlot.TWO), JUGGERNAUT("Juggernaut", PerkSlot.TWO),
	
	///// PERK SLOT THREE /////
	COMMANDO("Commando", PerkSlot.THREE), NINJA("Ghost", PerkSlot.THREE), LAST_STAND("Last Stand", PerkSlot.THREE);
	
	private String name;
	private PerkSlot slot;
	
	Perk(String name, PerkSlot slot) {
		this.setName(name);
		this.setSlot(slot);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setSlot(PerkSlot slot) {
		this.slot = slot;
	}

	public String getName() {
		return this.name;
	}
}