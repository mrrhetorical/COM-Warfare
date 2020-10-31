package com.rhetorical.cod.util;

public class InventoryPositions {
	//in game
	public static int primary = 1,
			secondary = 2,
			knife = 0,
			lethal = 3,
			tactical = 4,
			primaryAmmo = 28,
			secondaryAmmo = 29,
			compass = 8,
			selectClass = 32,
			leaveGame = 35,
			gunGameAmmo = 8;

	//lobby
	public static int menu = 0,
			leaveLobby = 8;

	public static boolean isValid(int i) {
		return i >= 0 && i <= 35;
	}
}
