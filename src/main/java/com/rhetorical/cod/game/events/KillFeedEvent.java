package com.rhetorical.cod.game.events;

import com.rhetorical.cod.game.GameInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KillFeedEvent extends Event implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private GameInstance instance;
	private Player victim,
			killer;

	private boolean cancelled;

	public KillFeedEvent(GameInstance instance, Player victim, Player killer) {
		this.instance = instance;
		this.victim = victim;
		this.killer = killer;
	}

	public GameInstance getInstance() {
		return instance;
	}

	public Player getVictim() {
		return victim;
	}

	public Player getKiller() {
		return killer;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
