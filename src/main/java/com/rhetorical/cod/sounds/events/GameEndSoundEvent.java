package com.rhetorical.cod.sounds.events;

import org.bukkit.entity.Player;

public class GameEndSoundEvent extends SoundEvent {

	private Player player;
	private boolean victory;

	public GameEndSoundEvent(Player players, boolean victory) {
		this.player = players;
		this.victory = victory;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isVictory() {
		return victory;
	}
}
