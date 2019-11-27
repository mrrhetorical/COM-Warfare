package com.rhetorical.cod.sounds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SoundEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private boolean cancelled = false;

	SoundEvent() {}

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean value) {
		cancelled = value;
	}
}
