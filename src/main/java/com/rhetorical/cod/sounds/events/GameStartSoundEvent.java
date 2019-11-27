package com.rhetorical.cod.sounds.events;

import com.rhetorical.cod.game.Gamemode;
import org.bukkit.entity.Player;

import java.util.Set;

public class GameStartSoundEvent extends SoundEvent {

	private Set<Player> players;
	private Gamemode gamemode;

	public GameStartSoundEvent(Set<Player> players, Gamemode gamemode) {
		this.players = players;
		this.gamemode = gamemode;
	}

	public Set<Player> getPlayers() {
		return players;
	}

	public Gamemode getGamemode() {
		return gamemode;
	}
}
