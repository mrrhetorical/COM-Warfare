package com.rhetorical.cod.sounds.events;

import com.rhetorical.cod.game.Gamemode;
import org.bukkit.entity.Player;

public class GameStartSoundEvent extends SoundEvent {

	private Player player;
	private Gamemode gamemode;

	public GameStartSoundEvent(Player player, Gamemode gamemode) {
		this.player = player;
		this.gamemode = gamemode;
	}

	public Player getPlayer() {
		return player;
	}

	public Gamemode getGamemode() {
		return gamemode;
	}
}
