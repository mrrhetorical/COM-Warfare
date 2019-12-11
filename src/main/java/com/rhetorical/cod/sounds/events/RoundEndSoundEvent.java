package com.rhetorical.cod.sounds.events;

import org.bukkit.entity.Player;

import java.util.Set;

public class RoundEndSoundEvent extends SoundEvent {

	private Player player;
	private boolean victory;

	public RoundEndSoundEvent(Player player, boolean victory) {
		this.player = player;
		this.victory = victory;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isVictory() {
		return victory;
	}
}
