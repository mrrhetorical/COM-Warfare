package com.rhetorical.cod.util;

import com.rhetorical.cod.ComWarfare;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class MenuReplacementUtil {

	private static MenuReplacementUtil instance;

	private Material createAClass,shop, combatRecord, challenges, killstreaks, UAV, counterUAV, VSAT, dogs, nuke,
			juggernautSuit, matchBrowser, leaderboard, prestige, primaryShop, secondaryShop, perkShop, voteA, voteB,
			airstrike, openMenu, matchInfo, assignmentKills, assignmentGames, assignmentWinGames, assignmentWinGameModes,
			statKills, statDeaths, statKDR;

	private MenuReplacementUtil() {
		FileConfiguration config = ComWarfare.getPlugin().getConfig();

		createAClass = getMaterial(config.getString("menu.createAClass"));
		shop = getMaterial(config.getString("menu.shopMain"));
		combatRecord = getMaterial(config.getString("menu.combatRecord"));
		challenges = getMaterial(config.getString("menu.challengges"));
		killstreaks = getMaterial(config.getString("menu.killstreaks"));
		UAV = getMaterial(config.getString("menu.UAV"));
		counterUAV = getMaterial(config.getString("menu.counterUAV"));
		VSAT = getMaterial(config.getString("menu.VSAT"));
		dogs = getMaterial(config.getString("menu.dogs"));
		nuke = getMaterial(config.getString("menu.nuke"));
		juggernautSuit = getMaterial(config.getString("menu.juggernautSuit"));
		matchBrowser = getMaterial(config.getString("menu.matchBrowser"));
		leaderboard = getMaterial(config.getString("menu.leaderboard"));
		prestige = getMaterial(config.getString("menu.prestige"));
		primaryShop = getMaterial(config.getString("menu.primaryShop"));
		secondaryShop = getMaterial(config.getString("menu.grenadeShop"));
		perkShop = getMaterial(config.getString("menu.perkShop"));
		voteA = getMaterial(config.getString("menu.voteA"));
		voteB = getMaterial(config.getString("menu.voteB"));
		airstrike = getMaterial(config.getString("menu.airstrike"));
		openMenu = getMaterial(config.getString("menu.openMenu"));
		matchInfo = getMaterial(config.getString("menu.matchInfo"));
		assignmentKills = getMaterial(config.getString("menu.assignmentKills"));
		assignmentGames = getMaterial(config.getString("menu.assignmentPlayMode"));
		assignmentWinGames = getMaterial(config.getString("menu.assignmentWin"));
		assignmentWinGameModes = getMaterial(config.getString("menu.assignmentWinGameMode"));
		statKills = getMaterial(config.getString("menu.statKills"));
		statDeaths = getMaterial(config.getString("menu.statDeaths"));
		statKDR = getMaterial(config.getString("menu.statKDR"));
	}

	public static MenuReplacementUtil getInstance() {
		if (instance == null)
			instance = new MenuReplacementUtil();

		return instance;
	}


	public static Material getMaterial(String str) {
		Material mat = null;
		try {
			mat = Material.valueOf(str);
		} catch (Exception ignored) {}

		return mat;
	}

	public Material getCreateAClass() {
		return createAClass;
	}

	public Material getShop() {
		return shop != null ? shop : Material.EMERALD;
	}

	public Material getCombatRecord() {
		return combatRecord != null ? combatRecord : Material.PAPER;
	}

	public Material getChallenges() {
		return challenges != null ? challenges : Material.GOLD_INGOT;
	}

	public Material getKillstreaks() {
		return killstreaks != null ? killstreaks : Material.DIAMOND;
	}

	public Material getUAV() {
		return UAV != null ? UAV : Material.SHEARS;
	}

	public Material getCounterUAV() {
		return counterUAV != null ? counterUAV : Material.REDSTONE;
	}

	public Material getVSAT() {
		return VSAT != null ? VSAT : Material.GOLD_NUGGET;
	}

	public Material getDogs() {
		return dogs != null ? dogs : Material.BONE;
	}

	public Material getNuke() {
		return nuke != null ? nuke : Material.TNT;
	}

	public Material getJuggernautSuit() {
		return juggernautSuit != null ? juggernautSuit : Material.BREAD;
	}

	public Material getMatchBrowser() {
		return matchBrowser != null ? matchBrowser : Material.EMERALD;
	}

	public Material getLeaderboard() {
		return leaderboard != null ? leaderboard : Material.PAPER;
	}

	public Material getPrestige() {
		return prestige != null ? prestige : Material.ANVIL;
	}

	public Material getPrimaryShop() {
		return primaryShop != null ? primaryShop : Material.IRON_HOE;
	}

	public Material getSecondaryShop() {
		return secondaryShop != null ? secondaryShop : Material.SLIME_BALL;
	}

	public Material getPerkShop() {
		return perkShop != null ? perkShop : Material.APPLE;
	}

	public Material getVoteA() {
		return voteA != null ? voteA : Material.PAPER;
	}

	public Material getVoteB() {
		return voteB != null ? voteB : Material.PAPER;
	}

	public Material getOpenMenu() {
		return openMenu != null ? openMenu : Material.ENDER_PEARL;
	}

	public Material getAirstrike() {
		return airstrike != null ? airstrike : Material.GLASS_BOTTLE;
	}

	public Material getMatchInfo() {
		return matchInfo != null ? matchInfo : Material.PAPER;
	}

	public Material getAssignmentKills() {
		return assignmentKills != null ? assignmentKills : Material.GOLD_INGOT;
	}

	public Material getAssignmentGames() {
		return assignmentGames != null ? assignmentGames : Material.PAPER;
	}

	public Material getAssignmentWinGames() {
		return assignmentWinGames != null ? assignmentWinGames : Material.ARROW;
	}

	public Material getAssignmentWinGameModes() {
		return assignmentWinGameModes != null ? assignmentWinGameModes : Material.GOLDEN_APPLE;
	}

	public Material getStatKills() {
		return statKills != null ? statKills : Material.ARROW;
	}

	public Material getStatDeaths() {
		return statDeaths != null ? statDeaths : Material.REDSTONE;
	}

	public Material getStatKDR() {
		return statKDR != null ? statKDR : Material.GLASS_BOTTLE;
	}
}
