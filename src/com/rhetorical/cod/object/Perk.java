package com.rhetorical.cod.object;

public enum Perk {
	MARATHON("Marathon", PerkSlot.ONE), SLEIGHT_OF_HAND("Sleight of Hand", PerkSlot.ONE), SCAVENGER("Scavenger", PerkSlot.ONE), BLING("Bling", PerkSlot.ONE), ONE_MAN_ARMY("One Man Army", PerkSlot.ONE), /* perk 2*/ STOPPING_POWER("Stopping Power", PerkSlot.TWO), LIGHTWEIGHT("Lightweight", PerkSlot.TWO), HARDLINE("Hardline", PerkSlot.TWO), COLD_BLOODED("Cold Blooded", PerkSlot.TWO), DANGER_CLOSE("Danger Close", PerkSlot.TWO), /* perk 3*/ COMMANDO("Commando", PerkSlot.THREE), STEADY_AIM("Steady Aim", PerkSlot.THREE), SCRAMBLER("Scrambler", PerkSlot.THREE), NINJA("Ninja", PerkSlot.THREE), SITREP("Sitrep", PerkSlot.THREE), LAST_STAND("Last Stand", PerkSlot.THREE);
	
	private String name;
	private PerkSlot slot;
	
	private Perk(String name, PerkSlot slot) {
		this.setName(name);
		this.setSlot(slot);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setSlot(PerkSlot slot) {
		this.slot = slot;
	}
	
	public PerkSlot getSlot() {
		return this.slot;
	}
	
	public String getName() {
		return this.name;
	}
}