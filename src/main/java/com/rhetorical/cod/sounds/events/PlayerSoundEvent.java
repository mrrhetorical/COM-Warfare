package com.rhetorical.cod.sounds.events;

import org.bukkit.entity.Player;

public class PlayerSoundEvent extends SoundEvent {

	private Player player;

	public PlayerSoundEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

}
