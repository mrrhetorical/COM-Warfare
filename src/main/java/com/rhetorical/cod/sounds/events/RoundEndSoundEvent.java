package com.rhetorical.cod.sounds.events;

import org.bukkit.entity.Player;

import java.util.Set;

public class RoundEndSoundEvent extends SoundEvent {

	private Set<Player> players;
	private boolean victory;

	public RoundEndSoundEvent(Set<Player> players, boolean victory) {
		this.players = players;
		this.victory = victory;
	}

	public Set<Player> getPlayers() {
		return players;
	}

	public boolean isVictory() {
		return victory;
	}
}
