package com.rhetorical.cod.lang;

import org.bukkit.ChatColor;

public enum Lang {

	UNKNOWN_COMMAND("&cUnknown Command! Try using '/cod help' for a list of commands."),
	NO_PERMISSION("&cYou don't have permission to do that!"),
	INCORRECT_USAGE("&cIncorrect usage! Correct usage: '{command}'"),
	MUST_BE_PLAYER("&cYou must be a player to execute this command."),
	NOT_PROPER_PAGE("&cYou didn't specify a proper page."),
	MAP_LIST_HEADER("&f=====&6&flMap List&r&f====="),
	MAP_LIST_ENTRY( "{map-id} - &6&lName: &r&a {map-name} &r&6&lGamemode: &r&c {game-mode} &r&6&lStatus: {map-status}"),
	CREATE_MAP_ALREADY_EXISTS("&cThere already exists a map with that name!"),
	CREATE_MAP_SUCCESS("&aSuccessfully created map {map-name}!"),
	REMOVE_MAP_SUCCESS("&aSuccessfully removed map!"),
	MAP_NOT_EXISTS_WITH_NAME("&cNo map exists with that name!"),
	GAME_MODE_NOT_EXISTS_WITH_NAME("&cNo game mode exists with that name!"),
	GAME_MODE_NOT_SET_UP_ON_MAP("&cThat game mode is not set up on that map!"),
	SET_LOBBY_SUCCESS("&aSuccessfully set lobby to your location! (You might want to restart the server for it to take effect)"),
	SET_SPAWN_SUCCESS("&aSuccessfully created {team} spawn for map {map-name}!"),
	TEAM_NOT_EXISTS_WITH_NAME("&cNo team exists with that name!"),
	SET_FLAG_CTF_SUCCESS("&aSuccessfully set {team} flag spawn!"),
	SET_FLAG_DOM_SUCCESS("&aSuccessfully set {flag} DOM flag spawn!"),
	LOBBY_NOT_EXISTS("&cThere is no lobby to send you to!"),
	BALANCE_COMMAND("&aYou have {credits} credits!"),
	GIVE_BALANCE_COMMAND("&aSuccessfully gave {player} {amount} credits! They now have {total} credits!"),
	SET_BALANCE_COMMAND("&aSuccessfully set {player}'s balance to {amount} credits!"),
	FORCE_START_FAIL("&cCould not force start arena!"),
	COULD_NOT_FIND_GAME_PLAYER_IN("&cCould not find the game that the player is in!"),
	MUST_BE_IN_GAME("&cYou must be in game to use that command!"),
	PLAYERS_BOOTED_SUCCESS("&aAll players removed from all games!"),
	PLAYER_BOOTED_FAILURE("&cCouldn't boot all players successfully!"),
	WEAPON_NOT_FOUND_WITH_NAME("&cCould not find weapon with the name: {gun-name}!"),
	OITC_GUN_SET_SUCCESS("&aSuccessfully set OITC gun to {gun-name}!"),
	GUN_PROGRESSION_ADDED_SUCCESS("&aSuccessfully added gun to the Gun Game progression!"),
	MAP_CHANGE_SUCCESS("&aSuccessfully changed map to &6&l{map-name}&r&a!"),
	GAME_MODE_CHANGE_SUCCESS("&aSuccessfully changed game mode to &6&l{game-mode}&r&a!"),
	BLACKLIST_SUCCESS("&aSuccessfully blacklisted mode {mode} from {map-name}!"),
	WEAPON_TYPE_NOT_EXISTS("&cThat weapon type does not exist! Valid types include 'tactical' and 'lethal'!"),
	GUN_TYPE_NOT_EXISTS("&cThat gun type does not exist! Valid types include 'primary' and 'secondary'!"),
	UNLOCK_TYPE_NOT_EXISTS("&cThat unlock type does not exist! Valid types include 'level', 'credits', and 'both'!"),
	MATERIAL_NOT_EXISTS("&cThat material does not exist!"),
	WEAPON_CREATED_SUCCESS("&aSuccessfully created weapon {weapon-name} as a {weapon-type} grenade!"),
	GUN_CREATED_SUCCESS("&aSuccessfully created gun {gun-name} as a {gun-type}!"),
	WEAPON_UNLOCKED("&aYou just unlocked the &6{gun-name}! \n &aEquip it after the match!"),
	WEAPON_PURCHASE_UNLOCKED("&The &6{gun-name} is now available for purchase!"),
	RANK_UP_MESSAGE("&7Congratulations! You just ranked up to level &e{level}&r&7!"),
	RANK_UP_PRESTIGE_MESSAGE("&7Congratulations! You just ranked up to prestige level &e{level}&r&7!"),
	RANK_UP_READY_TO_PRESTIGE("&7Congratulations! You have just reached the highest rank! Visit the prestige menu to get your reward!"),
	RANK_RESET_MESSAGE("&7Your rank has been reset!"),
	ERROR_SETTING_PLAYER_EXPERIENCE_LEVEL("&cThere was an error setting the player's experience level.");


	private String message;

	Lang(String msg) {
		message = msg;
	}

	public static void load() {
		NO_PERMISSION.message = "";
	}

	public String getMessage() {
		// \u00A7 is section symbol.
		return message.replace("&", "\u00A7");
	}

}