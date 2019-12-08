package com.rhetorical.cod.game;

import org.bukkit.ChatColor;

public enum GameState {
	STARTING(ChatColor.GREEN), IN_GAME(ChatColor.YELLOW), STOPPING(ChatColor.RED), WAITING(ChatColor.GREEN);

	private ChatColor color;

	public ChatColor getColor() {
		return color;
	}

	GameState(ChatColor c) {
		color = c;
	}
}
