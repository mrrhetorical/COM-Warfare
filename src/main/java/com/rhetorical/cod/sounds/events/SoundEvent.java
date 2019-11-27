package com.rhetorical.cod.sounds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SoundEvent extends Event {

	private static final HandlerList handlerList = new HandlerList();

	private boolean cancelled = false;

	SoundEvent() {}

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean value) {
		cancelled = value;
	}
}
