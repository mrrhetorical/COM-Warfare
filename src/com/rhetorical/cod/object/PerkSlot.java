package com.rhetorical.cod.object;

import java.util.ArrayList;
import java.util.Collections;

public enum PerkSlot {
	ONE(1), TWO(2), THREE(3);
	
	private int Id;
	
	
	private PerkSlot(int id) {
		this.Id = id;
	}
	
	public int getId() {
		return this.Id;
	}

	public static PerkSlot random() {
		
		ArrayList<PerkSlot> slots = new ArrayList<PerkSlot>();
		slots.add(ONE);
		slots.add(TWO);
		slots.add(THREE);
		
		Collections.shuffle(slots);
		
		return slots.get(0);
	}
	
	
}
