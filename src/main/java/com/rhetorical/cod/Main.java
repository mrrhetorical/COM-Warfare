package com.rhetorical.cod;

import com.rhetorical.cod.assignments.AssignmentManager;
import com.rhetorical.cod.files.*;
import com.rhetorical.cod.game.CodMap;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.game.Gamemode;
import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.inventories.ShopManager;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.perks.PerkListener;
import com.rhetorical.cod.perks.PerkManager;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.progression.ProgressionManager;
import com.rhetorical.cod.progression.RankPerks;
import com.rhetorical.cod.streaks.KillStreakManager;
import com.rhetorical.cod.weapons.*;
import com.rhetorical.tpp.McLang;
import com.rhetorical.tpp.api.McTranslate;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {

	public static Plugin getPlugin() {
		return Bukkit.getServer().getPluginManager().getPlugin("COM-Warfare");
	}

	public static String codPrefix = "[COM] ";
	public static ConsoleCommandSender cs = Bukkit.getConsoleSender();

	private static String translate_api_key;

	public static ProgressionManager progressionManager;
	public static LoadoutManager loadManager;
	public static PerkManager perkManager;
	public static InventoryManager invManager;
	public static ShopManager shopManager;
	public static PerkListener perkListener;
	public static KillStreakManager killstreakManager;
	public static AssignmentManager assignmentManager;

	public static Object lang;
	private static Object translate;
	
	public static int minPlayers = 6;
	public static int maxPlayers = 12;

	public static boolean serverMode = false;

	public static double defaultHealth = 20D;

	private static ArrayList<RankPerks> serverRanks = new ArrayList<>();

	public static Location lobbyLoc;
	public static HashMap<Player, Location> lastLoc = new HashMap<>();

	public static String header = "[COM-Warfare]";

	public static boolean hasQA = false;
	public static boolean hasCS = false;

	public static String reward_highestKD;
	public static String reward_highestScore;
	public static String reward_maxLevel;
	public static String reward_maxPrestige;
	public static String reward_maxPrestigeMaxLevel;

	private static boolean disabling = false;

	private Metrics bMetrics;

	@Override
	public void onEnable() {

		ComVersion.setup(true);

		getPlugin().saveDefaultConfig();
		getPlugin().reloadConfig();

		if (ComVersion.getPurchased()) {
			codPrefix = getPlugin().getConfig().getString("prefix").replace("&", "\u00A7") + " ";

			if (codPrefix.equalsIgnoreCase("")) {
				codPrefix = "[COD] ";
			}
		}

		bMetrics = new Metrics(this);

		String bukkitVersion = Bukkit.getServer().getBukkitVersion();

		int v = 8;

		try {
			v = Integer.parseInt(bukkitVersion.split(".")[1].charAt(0) + "");
		} catch(Exception ignored) {}

		if (bukkitVersion.startsWith("1.8") || v < 8 ) {
			Main.cs.sendMessage(Main.codPrefix + "You are not on the most recent version of Spigot/Bukkit, so COM-Warfare might not work as advertised. To ensure it will work properly, please use version 1.9 - 1.14!");
		}

		Main.cs.sendMessage(Main.codPrefix + "Checking dependencies...");

		DependencyManager dm = new DependencyManager();
		if (!dm.checkDependencies()) {
			if (getPlugin().getConfig().getBoolean("auto-download-dependency")) {
				Main.cs.sendMessage(Main.codPrefix + "One or more dependencies were not found, will attempt to download them.");
				try {
					dm.downloadDependencies();
				} catch (Exception e) {
					Main.cs.sendMessage(Main.codPrefix + "Could not download dependencies! Make sure that the plugins folder can be written to!");
					Main.cs.sendMessage(Main.codPrefix + "Not all dependencies for COM-Warfare are installed! The plugin may not work as intended and may throw errors!");
				}
			} else {
				Main.cs.sendMessage(Main.codPrefix + "Could not download dependencies! You must set the value for \"auto-download-dependency\" to 'true' in the config to automatically download them!");
				Main.cs.sendMessage("Not all dependencies for COM-Warfare are installed! The plugin likely will not work as intended!");
			}
		} else {
			Main.cs.sendMessage(Main.codPrefix + "All dependencies are installed!");
		}

		try {
			if (getPlugin().getConfig().getString("lang").equalsIgnoreCase("none")) {
				lang = McLang.EN;
			} else {
				try {
					lang = McLang.valueOf(getPlugin().getConfig().getString("lang"));
					connectToTranslationService();
				} catch (Exception e) {
					lang = McLang.EN;
					cs.sendMessage(codPrefix + ChatColor.RED + "Could not get the language from the config! Make sure you're using the right two letter abbreviation!");
				}

				if (lang != McLang.EN)
					lang = McLang.EN;
			}
		} catch(Exception classException) {
			Main.cs.sendMessage(codPrefix + ChatColor.RED + "McTranslate++ Doesn't seem to be installed? If you have 'auto-download-dependencies' turned on, it will automatically install, and after installing, you should restart the server!");
		}

		String version = getPlugin().getDescription().getVersion();


		LangFile.setup(getPlugin());
		Lang.load();

		ProgressionFile.setup(getPlugin());
		ArenasFile.setup(getPlugin());
		CreditsFile.setup(getPlugin());
		GunsFile.setup(getPlugin());
		ShopFile.setup(getPlugin());
		LoadoutsFile.setup(getPlugin());
		StatsFile.setup(getPlugin());
		KillstreaksFile.setup(getPlugin());
		AssignmentFile.setup(getPlugin());

		progressionManager = new ProgressionManager();
		perkManager = new PerkManager();
		loadManager = new LoadoutManager(new HashMap<>());
		shopManager = new ShopManager();
		perkListener = new PerkListener();
		killstreakManager = new KillStreakManager();
		invManager = new InventoryManager();
		assignmentManager = new AssignmentManager();

		QualityGun.setup();
		CrackShotGun.setup();
		GameManager.setupOITC();
		GameManager.setupGunGame();

		Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), getPlugin());

		GameManager.loadMaps();

		for (Player p : Bukkit.getOnlinePlayers()) {
			loadManager.load(p);
			CreditManager.loadCredits(p);
		}

		lobbyLoc = (Location) getPlugin().getConfig().get("com.lobby");

		if (ComVersion.getPurchased()) {
			header = getPlugin().getConfig().getString("Scoreboard.Header");
			minPlayers = getPlugin().getConfig().getInt("players.min");
			maxPlayers = getPlugin().getConfig().getInt("players.max");
			serverMode = getPlugin().getConfig().getBoolean("serverMode");
			defaultHealth = getPlugin().getConfig().getDouble("defaultHealth");
			translate_api_key = getPlugin().getConfig().getString("translate.api_key");
			reward_highestKD = getPlugin().getConfig().getString("Rewards.Highest_KD");
			reward_highestScore = getPlugin().getConfig().getString("Rewards.Highest_Score");
			reward_maxLevel = getPlugin().getConfig().getString("Rewards.Max_Level");
			reward_maxPrestige = getPlugin().getConfig().getString("Rewards.Max_Prestige");
			reward_maxPrestigeMaxLevel = getPlugin().getConfig().getString("Rewards.Max_Prestige_Max_Level");
		}

		if (ComVersion.getPurchased()) {
			int i = 0;

			while (getPlugin().getConfig().contains("RankTiers." + i)) {
				String name = getPlugin().getConfig().getString("RankTiers." + i + ".name");
				int killCredits = getPlugin().getConfig().getInt("RankTiers." + i + ".kill.credits");
				double killExperience = getPlugin().getConfig().getDouble("RankTiers." + i + ".kill.xp");
				int levelCredits = getPlugin().getConfig().getInt("RankTiers." + i + ".levelCredits");

				RankPerks rank = new RankPerks(name, killCredits, killExperience, levelCredits);

				Main.serverRanks.add(rank);

				i++;
			}

			if (i == 0) {
				getPlugin().getConfig().set("RankTiers.0.name", "default");
				getPlugin().getConfig().set("RankTiers.0.kill.credits", 1);
				getPlugin().getConfig().set("RankTiers.0.kill.xp", 100);
				getPlugin().getConfig().set("RankTiers.0.levelCredits", 10);
				getPlugin().saveConfig();
				getPlugin().reloadConfig();
			}
		} else {
			RankPerks rank = new RankPerks("default", 1, 100, 0);
			Main.serverRanks.add(rank);
		}

		hasQA = Bukkit.getServer().getPluginManager().getPlugin("QualityArmory") != null;
		hasCS = Bukkit.getServer().getPluginManager().getPlugin("CrackShot") != null;

		Main.cs.sendMessage(Main.codPrefix + ChatColor.GREEN + ChatColor.BOLD + "COM-Warfare version " + ChatColor.RESET + ChatColor.WHITE + version + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + " is now up and running!");

		if (serverMode) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				GameManager.findMatch(p);
			}
		}
	}

	@Override
	public void onDisable() {

		disabling = true;

		if (GameManager.getAddedMaps().size() != 0) {
			for (CodMap m : GameManager.getAddedMaps()) {
				m.save();
			}

			bootPlayers();
		}
	}

	static boolean hasPerm(CommandSender p, String s, boolean... inGame) {

		boolean canUseInGame = false;

		if (inGame.length > 0) {
			canUseInGame = inGame[0];
		}
		if (p.hasPermission(s) || p.hasPermission("com.*") || p instanceof ConsoleCommandSender || p.isOp()) {
			if (p instanceof Player) {
				if (GameManager.isInMatch((Player) p)) {
					if (!canUseInGame){
						sendMessage(p, Main.codPrefix + Lang.NOT_ALLOWED_IN_GAME.getMessage(), lang);
						return false;
					}
				}
			}
			return true;
		} else {
			sendMessage(p, Main.codPrefix + Lang.NO_PERMISSION.getMessage(), lang);
			return false;
		}
	}

	public static boolean isLegacy() {
		//returns true if server is using 1.8
		return Bukkit.getBukkitVersion().toUpperCase().startsWith("1.8");
	}

	public static boolean isUsingQA() {
		return Bukkit.getServer().getPluginManager().getPlugin("QualityArmory") != null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!label.equalsIgnoreCase("cod") && !label.equalsIgnoreCase("comr") && !label.equalsIgnoreCase("war") && !label.equalsIgnoreCase("com"))
			return false;

		String cColor = "" + ChatColor.GREEN + ChatColor.BOLD;
		String dColor = "" + ChatColor.WHITE + ChatColor.BOLD;

		if (args.length == 0) {

			if (!hasPerm(sender, "com.help"))
				return true;

			sendMessage(sender, "-===\u00A76\u00A7lCOM-Warfare Help\u00A7r===-");
			sendMessage(sender, "\u00A7f[\u00A7lPage 1 of 5\u00A7r\u00A7l]");

			sendMessage(sender, "\u00A7f\u00A7lType the command to see specifics.", lang);
			sendMessage(sender, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
			sendMessage(sender, cColor + "/cod | " + dColor + "Opens the main menu.");
			sendMessage(sender, cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
			sendMessage(sender, cColor + "/cod leave | " + dColor + "Leaves the current game.");
			sendMessage(sender, cColor + "/cod shop | " + dColor + "Opens the shop.");
			return true;
		} else {
			if (args[0].equalsIgnoreCase("help")) {
				if (!hasPerm(sender, "com.help", true))
					return true;

				if (args.length == 2) {
					int page;
					try {
						page = Integer.parseInt(args[1]);
					} catch (Exception e) {
						sendMessage(sender, Main.codPrefix + Lang.NOT_PROPER_PAGE.getMessage(), lang);
						return true;
					}

					if (!(page > 0 && page <= 5)) {
						sendMessage(sender, Main.codPrefix + Lang.NOT_PROPER_PAGE.getMessage(), lang);
						return true;
					}

					//FIXME: Left off here converting to ChatColor!

					sendMessage(sender, "-===\u00A76\u00A7lCOM-Warfare Help\u00A7r===-", lang);
					sendMessage(sender, "\u00A7f[\u00A7lPage " + page + " of 5\u00A7r\u00A7l]", lang);

					switch (page) {
						case 1:
							sendMessage(sender, "\u00A7f\u00A7lType the command to see specifics.", lang);
							sendMessage(sender, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
							sendMessage(sender, cColor + "/cod | " + dColor + "Opens the main menu.");
							sendMessage(sender, cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
							sendMessage(sender, cColor + "/cod leave | " + dColor + "Leaves the current game.");
							sendMessage(sender, cColor + "/cod shop | " + dColor + "Opens the shop.");
							break;
						case 2:
							sendMessage(sender, cColor + "/cod createMap [name] | " + dColor + "Create a map.");
							sendMessage(sender, cColor + "/cod removeMap [name] | " + dColor + "Command to remove a map.");
							sendMessage(sender, cColor + "/cod listMaps | " + dColor + "Lists the available maps.");
							sendMessage(sender, cColor + "/cod set [lobby/spawn/flag] | " + dColor + "Command to set spawns, flags, and lobby location.");
							sendMessage(sender, cColor + "/cod lobby | " + dColor + "Teleports you to the lobby.");
							break;
						case 3:
							sendMessage(sender, cColor + "/cod shop | " + dColor + "Opens the shop.");
							sendMessage(sender, cColor + "/cod createGun | " + dColor + "Creates a gun. Type command to see a full list of arguments.");
							sendMessage(sender, cColor + "/cod credits give [player] (amt) | " + dColor + "Gives credits to a person.");
							sendMessage(sender, cColor + "/cod credits set [player] (amt) | " + dColor + "Sets amount of credits for a player.");
							sendMessage(sender, cColor + "/cod balance | " + dColor + "Shows your credit balance.");
							break;
						case 4:
							sendMessage(sender, cColor + "/cod add [oitc/gun] (gun name) | " + dColor + "Sets the gun for OITC or adds a gun to Gun Game.");
							sendMessage(sender, cColor + "/cod changeMap/changeMode [map name/gamemode] | " + dColor + "Changes the current map/mode.");
							sendMessage(sender, cColor + "/cod class | " + dColor + "Opens the class selection menu.");
							sendMessage(sender, cColor + "/cod start | " + dColor + "Auto-starts the match if the lobby timer is started.");
							sendMessage(sender, cColor + "/cod boot | " + dColor + "Forces all players in all matches to leave.");
							break;
						case 5:
							sendMessage(sender, cColor + "/cod blacklist (map) (mode) | " + dColor + "Prevents a mode from being played on the map.");
							sendMessage(sender, cColor + "/cod notes | " + dColor + "Lists the patch notes for the current version of the plugin.");
							break;
						default:
							break;
					}
				} else {
					sendMessage(sender, "-===\u00A76\u00A7lCOM-Warfare Help\u00A7r===-");
					sendMessage(sender, "\u00A7f[\u00A7lPage 1 of 5\u00A7r\u00A7l]");

					sendMessage(sender, "\u00A7f\u00A7lType the command to see specifics.", lang);
					sendMessage(sender, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
					sendMessage(sender, cColor + "/cod | " + dColor + "Opens the main menu.");
					sendMessage(sender, cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
					sendMessage(sender, cColor + "/cod leave | " + dColor + "Leaves the current game.");
					sendMessage(sender, cColor + "/cod shop | " + dColor + "Opens the shop.");

				}

			} else if (args[0].equalsIgnoreCase("join")) {

				if (!(sender instanceof Player)) {
					sendMessage(cs, Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.join"))
					return true;

				Player p = (Player) sender;
				boolean b = GameManager.findMatch(p);
				if (b) {
					loadManager.load(p);
					Location l = p.getLocation();
					Main.progressionManager.update(p);
					Main.lastLoc.put(p, l);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("leave")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.leave", true))
					return true;

				Player p = (Player) sender;
				GameManager.leaveMatch(p);
				if (lastLoc.containsKey(p)) {
					p.teleport(lastLoc.get(p));
					lastLoc.remove(p);
				} else {
					if (lobbyLoc != null) {
						p.teleport(lobbyLoc);
					}
				}

				return true;
			} else if (args[0].equalsIgnoreCase("listMaps")) {

				if (!hasPerm(sender, "com.map.list", true))
					return true;

				sendMessage(sender, Main.codPrefix + Lang.MAP_LIST_HEADER.getMessage(), lang);
				int k = 0;
				for (CodMap m : GameManager.getAddedMaps()) {
					k++;
					StringBuilder gmr = new StringBuilder();
					for(Gamemode gm : m.getAvailableGamemodes()) {
						gmr.append(gm.toString());
						if (!m.getAvailableGamemodes().get(m.getAvailableGamemodes().size() - 1).equals(gm)) {
							gmr.append(", ");
						}
					}

					if (m.getAvailableGamemodes().size() == 0) {
						gmr.append("NONE");
					}

					String entry = Lang.MAP_LIST_ENTRY.getMessage();

					entry = entry.replace("{map-id}", k + "");
					entry = entry.replace("{map-name}", m.getName());
					entry = entry.replace("{game-mode}", gmr.toString());

					if (GameManager.usedMaps.contains(m)) {
						entry = entry.replace("{map-status}", ChatColor.RED + "IN-USE");
					} else {
						if (m.isEnabled()) {
							entry = entry.replace("{map-status}", ChatColor.GREEN + "WAITING");
						} else {
							entry = entry.replace("{map-status]", ChatColor.RED + "UNFINISHED");
						}
					}

					sendMessage(sender, entry, lang);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("createMap")) {


				if (!hasPerm(sender, "com.map.create"))
					return true;

				if (args.length >= 2) {
					CodMap newMap;
					String mapName = args[1];

					for (CodMap m : GameManager.getAddedMaps()) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							sendMessage(sender, Main.codPrefix + Lang.CREATE_MAP_ALREADY_EXISTS.getMessage(), lang);
							return true;
						}
					}

					newMap = new CodMap(mapName);

					GameManager.getAddedMaps().add(newMap);
					String msg = Lang.CREATE_MAP_SUCCESS.getMessage();
					msg = msg.replace("{map-name}", mapName);
					sendMessage(sender, Main.codPrefix + msg, lang);
					newMap.setEnable();
					return true;
				} else {
					String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createMap (name)");
					sendMessage(sender, Main.codPrefix + msg);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("removeMap")) {

				if (!hasPerm(sender, "com.map.remove"))
					return true;

				if (args.length >= 2) {
					GameManager.loadMaps();

					String mapName = args[1];

					for (CodMap m : GameManager.getAddedMaps()) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							GameManager.getAddedMaps().remove(m);

							File aFile = new File(getPlugin().getDataFolder(), "arenas.yml");

							if (aFile.exists()) {
								boolean success = aFile.delete();
							}

							ArenasFile.setup(getPlugin());

							for (CodMap notChanged : GameManager.getAddedMaps()) {
								notChanged.save();
							}

							sendMessage(sender, Main.codPrefix + Lang.REMOVE_MAP_SUCCESS.getMessage(), lang);
							return true;
						}
					}

					sendMessage(sender, Main.codPrefix + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage(), lang);
					return true;

				} else {
					String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod removeMap (name)");
					sendMessage(sender, Main.codPrefix + msg);
					return true;
				}

			} else if (args[0].equalsIgnoreCase("set")) {

				if (!hasPerm(sender, "com.map.modify"))
					return true;

				if (!(sender instanceof Player)) {
					sendMessage(cs, Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}
				Player p = (Player) sender;

				if (!(args.length > 1)) {
					String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set (lobby/spawn/flag) [args]");
					sendMessage(p, Main.codPrefix + msg);
					return true;
				}

				if (args[1].equalsIgnoreCase("lobby")) {

					Location lobby = p.getLocation();
					getPlugin().getConfig().set("com.lobby", lobby);
					Main.lobbyLoc = (Location) getPlugin().getConfig().get("com.lobby");
					getPlugin().saveConfig();
					getPlugin().reloadConfig();
					sendMessage(p, Main.codPrefix + Lang.SET_LOBBY_SUCCESS.getMessage(), lang);
					return true;
				} else if (args[1].equalsIgnoreCase("spawn")) {

					if (!hasPerm(p, "com.map.addSpawn"))
						return true;

					if (args.length < 4) {
						String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set spawn (map name) (team)");
						sendMessage(p, Main.codPrefix + msg);
						return true;
					}
					CodMap map = null;
					String spawnMapName = args[2];
					for (CodMap m : GameManager.getAddedMaps()) {
						if (m.getName().equalsIgnoreCase(spawnMapName)) {
							map = m;
						}
					}

					if (map == null) {
						sendMessage(p, Main.codPrefix + Lang.MAP_NOT_EXISTS_WITH_NAME, lang);
						return true;
					}

					String spawnTeam = args[3];
					String team = "";
					switch (spawnTeam.toUpperCase()) {
					case "RED":
						map.addRedSpawn(p.getLocation());
						team = ChatColor.RED + "RED";
						map.setEnable();
						break;
					case "BLUE":
						map.addblueSpawn(p.getLocation());
						team = ChatColor.BLUE + "BLUE";
						map.setEnable();
						break;
					case "PINK":
						map.addPinkSpawn(p.getLocation());
						team = ChatColor.LIGHT_PURPLE + "PINK";
						map.setEnable();
						break;
					default:
						sendMessage(p, Main.codPrefix + Lang.TEAM_NOT_EXISTS_WITH_NAME.getMessage(), lang);
						return true;
					}

					String msg = Lang.SET_SPAWN_SUCCESS.getMessage().replace("{team}", team).replace("{map-name}", map.getName());
					sendMessage(p, Main.codPrefix + msg);

				} else if (args[1].equalsIgnoreCase("flag")) {

					if (!hasPerm(p, "com.map.modify"))
						return true;

					if (args.length < 4) {
						String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set flag (map name) (red/blue/a/b/c)");
						sendMessage(p, Main.codPrefix + msg);
						return true;
					}

					CodMap map = null;

					String mapName = args[2];
					for(CodMap m : GameManager.getAddedMaps()) {
						if (m.getName().equalsIgnoreCase(mapName)){
							map = m;
							break;
						}
					}

					if (map == null) {
						sendMessage(p, Main.codPrefix + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage(), lang);
						return true;
					}

					String arg = args[3];

					String team = null;
					String flag = null;

					switch(arg.toLowerCase()) {
						case "red":
							map.addRedFlagSpawn(p.getLocation());
							team = ChatColor.RED + "RED";
							break;
						case "blue":
							map.addBlueFlagSpawn(p.getLocation());
							team = ChatColor.BLUE + "BLUE";
							break;
						case "a":
							map.addAFlagSpawn(p.getLocation());
							flag = ChatColor.YELLOW + "A";
							break;
						case "b":
							map.addBFlagSpawn(p.getLocation());
							flag = ChatColor.YELLOW + "B";
							break;
						case "c":
							map.addCFlagSpawn(p.getLocation());
							flag = ChatColor.YELLOW + "C";
							break;
						default:
							String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set flag (map name) (red/blue/a/b/c)");
							sendMessage(p, Main.codPrefix + msg);
							return true;
					}

					if (team == null) {
						sendMessage(p, Main.codPrefix + Lang.SET_FLAG_DOM_SUCCESS.getMessage().replace("{flag}", flag));
					} else {
						sendMessage(p, Main.codPrefix + Lang.SET_FLAG_CTF_SUCCESS.getMessage().replace("{team}", team));
					}

					return true;
				}

			} else if (args[0].equalsIgnoreCase("lobby")) {

				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}


				if (!hasPerm(sender, "com.lobby"))
					return true;

				Player p = (Player) sender;

				if (GameManager.isInMatch(p)) {
					sendMessage(p, Lang.MUST_NOT_BE_IN_GAME.getMessage(), lang);
					return true;
				}

				if (lobbyLoc != null) {
					p.teleport(lobbyLoc);
				} else {
					sendMessage(p, Main.codPrefix + Lang.LOBBY_NOT_EXISTS.getMessage(), lang);
				}
			} else if (args[0].equalsIgnoreCase("balance")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}


				if (!hasPerm(sender, "com.join", true))
					return true;

				Player p = (Player) sender;
				int credits = CreditManager.getCredits(p);
				sendMessage(p, codPrefix + Lang.BALANCE_COMMAND.getMessage().replace("{credits}", credits + ""), lang);
			} else if (args[0].equalsIgnoreCase("credits")) {
				if (!(args.length >= 3) && (hasPerm(sender, "com.credits.give") || hasPerm(sender	, "com.credits.set"))) {
					sendMessage(sender, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits [give/set] {player} (amount)"));
					return true;
				}

				if (args[1].equalsIgnoreCase("give")) {

					if (!hasPerm(sender, "com.credits.give"))
						return true;

					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						sendMessage(sender, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits give {player} (amount)"));
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);
					sendMessage(sender, Main.codPrefix + Lang.GIVE_BALANCE_COMMAND.getMessage().replace("{player}", playerName).replace("{amount}", amount + "").replace("{total}", CreditManager.getCredits(playerName) + ""), lang);
					return true;
				} else if (args[1].equalsIgnoreCase("set")) {

					if (!hasPerm(sender, "com.credits.set"))
						return true;

					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						sendMessage(sender, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits set {name} [amount]"));
						return true;
					}

					CreditManager.setCredits(playerName, amount);
					sendMessage(sender, Main.codPrefix + Lang.SET_BALANCE_COMMAND.getMessage().replace("{player}", playerName).replace("{amount}", amount + ""), lang);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("createGun")) {

				if (!hasPerm(sender, "com.createGun"))
					return true;

				if (args.length >= 9) {
					createGun(sender, args);
					return true;
				} else {
					sendMessage(sender, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credits/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)"));
					return true;
				}
			} else if ((args[0].equalsIgnoreCase("createWeapon") || args[0].equalsIgnoreCase("createGrenade"))) {

				if (!hasPerm(sender, "com.createWeapon"))
					return true;

				if (args.length >= 7) {
					createWeapon(sender, args);
					return true;
				} else {
					sendMessage(sender, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credits/both) (Grenade Material) (Level Unlock) (Cost)"));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("start")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.forceStart", true))
					return true;

				Player p = (Player) sender;
				if (GameManager.isInMatch(p)) {
					try {
						if (GameManager.getMatchWhichContains(p) != null) {
							GameInstance game = GameManager.getMatchWhichContains(p);
							if (game != null) {
								game.forceStart(true);
							} else {
								p.sendMessage(codPrefix + Lang.FORCE_START_FAIL.getMessage());
							}
						}
					} catch(Exception e) {
						sendMessage(Main.cs, Main.codPrefix + Lang.COULD_NOT_FIND_GAME_PLAYER_IN, Main.lang	);
					}
					return true;
				} else {
					sendMessage(p, Main.codPrefix + Lang.MUST_BE_IN_GAME.getMessage(), lang);
				}

				return true;
			} else if (args[0].equalsIgnoreCase("class")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.selectClass", true))
					return true;

				Player p = (Player) sender;
				Main.invManager.openSelectClassInventory(p);
				return true;
			} else if (args[0].equalsIgnoreCase("shop")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.openShop", true))
					return true;

				Player p = (Player) sender;
				p.closeInventory();
				p.openInventory(invManager.mainShopInventory);
				return true;
			} else if (args[0].equalsIgnoreCase("boot")) {

				if (!hasPerm(sender, "com.bootAll", true))
					return true;

				boolean result = bootPlayers();
				if (result) {
					sender.sendMessage(Main.codPrefix + Lang.PLAYERS_BOOTED_SUCCESS.getMessage());
				} else {
					sender.sendMessage(Main.codPrefix + Lang.PLAYER_BOOTED_FAILURE.getMessage());
				}
			} else if (args[0].equalsIgnoreCase("add")) {

				if (!hasPerm(sender, "com.add"))
					return true;

				if (args.length	< 3) {
					sendMessage(sender, Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod add [oitc/gun] (gun name)"));
					return true;
				}

				String type = args[1];
				String gunName = args[2];
				CodWeapon weapon = shopManager.getWeaponForName(gunName);

				if (!(weapon instanceof CodGun)) {
					sendMessage(sender, Lang.WEAPON_NOT_FOUND_WITH_NAME.getMessage().replace("{gun-name}", gunName));
					return true;
				}

				if (type.equalsIgnoreCase("oitc")) {
					getConfig().set("OITC_Gun", weapon.getName());
					saveConfig();
					reloadConfig();
					GameManager.setupOITC();
					sendMessage(sender, Main.codPrefix + Lang.OITC_GUN_SET_SUCCESS.getMessage().replace("{gun-name}", gunName));
					return true;
				} else if (type.equalsIgnoreCase("gun")) {
					GameManager.gunGameGuns.add((CodGun) weapon);
					List<String> gunList = new ArrayList<>();
					for(CodGun g : GameManager.gunGameGuns) {
						gunList.add(g.getName());
					}
					getConfig().set("GunProgression", gunList);
					saveConfig();
					reloadConfig();
					sendMessage(sender, Main.codPrefix + Lang.GUN_PROGRESSION_ADDED_SUCCESS.getMessage());
					return true;
				}
				sendMessage(sender, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod add [oitc/gun] (gun name)"));
				return true;
			} else if (args[0].equalsIgnoreCase("changeMap")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, Main.codPrefix + Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.changeMap", true))
					return true;

				Player p = (Player) sender;

				if (args.length < 2) {
					sendMessage(p, codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod changeMap (name)"));
					return true;
				}

				if (!GameManager.isInMatch(p)) {
					sendMessage(p, codPrefix + Lang.MUST_BE_IN_GAME.getMessage());
					return true;
				}

				CodMap map = GameManager.getMapForName(args[1]);

				if (map == null) {
					sendMessage(p, codPrefix + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				GameManager.changeMap(Objects.requireNonNull(GameManager.getMatchWhichContains(p)), map);
				sendMessage(p, codPrefix + Lang.MAP_CHANGE_SUCCESS.getMessage().replace("{map-name}", map.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("changeMode")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, Main.codPrefix + Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.changeMode", true))
					return true;

				Player p = (Player) sender;
				if (args.length < 2) {
					sendMessage(p, codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod changeMode (name)"));
					return true;
				}

				if (!GameManager.isInMatch(p)) {
					sendMessage(p, codPrefix + Lang.MUST_BE_IN_GAME.getMessage());
					return true;
				}

				Gamemode mode;

				try {
					mode = Gamemode.valueOf(args[1]);
				} catch(Exception e) {
					sendMessage(p, codPrefix + Lang.GAME_MODE_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				if (!Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getMap().getAvailableGamemodes().contains(mode)) {
					sendMessage(p, codPrefix + Lang.GAME_MODE_NOT_SET_UP_ON_MAP.getMessage());
					return true;
				}

				Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getMap().changeGamemode(mode);
				sendMessage(p, codPrefix + Lang.GAME_MODE_CHANGE_SUCCESS.getMessage().replace("{game-mode}", mode.toString()));
				return true;
			} else if (args[0].equalsIgnoreCase("blacklist")) {

				if (!hasPerm(sender, "com.blacklist"))
					return true;

				if (args.length	< 3) {
					sendMessage(sender, codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod blacklist (map) (mode)"));
					return true;
				}

				Gamemode mode;

				try {
					mode = Gamemode.valueOf(args[2].toUpperCase());
				} catch(Exception e) {
					sendMessage(sender, codPrefix + Lang.GAME_MODE_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				CodMap map = GameManager.getMapForName(args[1]);

				if (map == null) {
					sendMessage(sender, codPrefix + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				map.addToBlacklist(mode);

				sendMessage(sender, Main.codPrefix + Lang.BLACKLIST_SUCCESS.getMessage().replace("{mode}", mode.toString()).replace("{map-name}", map.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("setLevel")) {

				if (!hasPerm(sender, "com.modifyLevel"))
					return true;

				if (args.length < 3) {
					sendMessage(sender, codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod setLevel (player) (level)"));
					return true;
				}

				int level;
				Player player = Bukkit.getPlayer(args[1]);

				if(player == null) {
					sendMessage(sender, codPrefix + Lang.ERROR_PLAYER_NOT_EXISTS.getMessage());
					return true;
				}

				try {
					level = Integer.parseInt(args[2]);
					if (level > progressionManager.maxLevel)
						throw new NumberFormatException();
				} catch(NumberFormatException e) {
					sendMessage(sender, codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod setLevel (player) (level)"));
					return true;
				}

				Main.progressionManager.setLevel(player, level, true);
				Main.progressionManager.saveData(player);
				sendMessage(sender, Lang.SET_LEVEL_SUCCESS.getMessage().replace("{player}", player.getDisplayName()).replace("{level}", level + ""));
				return true;
			} else {
				sender.sendMessage(Main.codPrefix + Lang.UNKNOWN_COMMAND.getMessage());
				return true;
			}
		}

		return true;
	}

	private void connectToTranslationService() {
		try {
			translate = new McTranslate(Main.getPlugin(), Main.translate_api_key);
		} catch(Exception e) {
			Main.sendMessage(Main.cs, Main.codPrefix + ChatColor.RED + "Could not start McTranslate++ API!");
		}
	}

	private boolean bootPlayers() {
		GameInstance[] runningGames = new GameInstance[GameManager.getRunningGames().size()];

		for (int k = 0; k < runningGames.length; k++) {
			runningGames[k] = GameManager.getRunningGames().get(k);
		}

		for (GameInstance i : runningGames) {
			if (i != null) {
				Player[] pls = new Player[i.getPlayers().size()];
				for (int k = 0; k < pls.length; k ++) {
					pls[k] = i.getPlayers().get(k);
				}

				for (Player p : pls) {
					i.removePlayer(p);
					Main.sendMessage(p, Main.codPrefix + Lang.PLAYER_LEAVE_GAME.getMessage(), Main.lang);
				}
			}
		}
		return true;
	}

	private void createWeapon(CommandSender p, String[] args) {

		String command = "/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credits/both) (Grenade Material) (Level Unlock) (Cost)";
		if (args.length == 7) {
			String name = args[1];
			WeaponType grenadeType;
			UnlockType unlockType;

			try {
				grenadeType = WeaponType.valueOf(args[2].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.WEAPON_TYPE_NOT_EXISTS.getMessage(), lang);
				return;
			}
			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.UNLOCK_TYPE_NOT_EXISTS.getMessage(), lang);
				return;
			}
			ItemStack grenade;

			try {
				String[] wa = args[4].toUpperCase().split(":");

				if (wa.length == 1) {
					grenade = new ItemStack(Material.valueOf(args[4].toUpperCase()));
				} else {
					byte data = Byte.parseByte(wa[1]);
					grenade = new ItemStack(Material.valueOf(wa[4]), 1, data);
				}
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.MATERIAL_NOT_EXISTS.getMessage().replace("{name}", args[4].toUpperCase()), lang);
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[5]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command), lang);
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[6]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command), lang);
				return;
			}

			CodWeapon grenadeWeapon = new CodWeapon(name, grenadeType, unlockType, grenade, levelUnlock);

			grenadeWeapon.setCreditUnlock(cost);

			grenadeWeapon.save();

			sendMessage(p, codPrefix + Lang.WEAPON_CREATED_SUCCESS.getMessage().replace("{weapon-name}", name).replace("{weapon-type}", grenadeType.toString()), lang);

			switch (grenadeType) {
			case LETHAL:
				ArrayList<CodWeapon> lethalList = Main.shopManager.getLethalWeapons();
				lethalList.add(grenadeWeapon);
				Main.shopManager.setLethalWeapons(lethalList);
				break;
			case TACTICAL:
				ArrayList<CodWeapon> tacList = Main.shopManager.getTacticalWeapons();
				tacList.add(grenadeWeapon);
				Main.shopManager.setTacticalWeapons(tacList);
				break;
			default:
				break;
			}

		} else {
			sendMessage(p, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command), lang);
		}
	}

	private void createGun(CommandSender p, String[] args) {
		String command = "/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credits/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)";
		if (args.length == 9) {
			String name = args[1];

			GunType gunType;

			try {
				String gt = args[2];
				String first = gt.charAt(0) + "";
				gt = gt.substring(1);
				gt = first.toUpperCase() + gt.toLowerCase();
				gunType = GunType.valueOf(gt);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.GUN_TYPE_NOT_EXISTS.getMessage(), lang);
				return;
			}

			UnlockType unlockType;

			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.UNLOCK_TYPE_NOT_EXISTS.getMessage(), lang);
				return;
			}

			int ammoAmount;

			try {
				ammoAmount = Integer.parseInt(args[4]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command) , lang);
				return;
			}

			ItemStack gunItem;
			ItemStack ammoItem;
			try {
				String[] ga = args[5].toUpperCase().split(":");

				if (ga.length <= 1) {
					gunItem = new ItemStack(Material.valueOf(args[5].toUpperCase()));
				} else {
					byte data = Byte.parseByte(ga[1]);
					gunItem = new ItemStack(Material.valueOf(ga[0]), 1, data);
				}
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.MATERIAL_NOT_EXISTS.getMessage().replace("{name}", args[5].toUpperCase()), lang);
				return;
			}

			try {
				String[] aa = args[6].toUpperCase().split(":");

				if (aa.length <= 1) {
					ammoItem = new ItemStack(Material.valueOf(args[6].toUpperCase()));
				} else {
					byte data = Byte.parseByte(aa[1]);
					ammoItem = new ItemStack(Material.valueOf(aa[0]), 1, data);
				}
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.MATERIAL_NOT_EXISTS.getMessage().replace("{name}", args[6].toUpperCase()), lang);
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[7]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command) , lang);
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[8]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command) , lang);
				return;
			}

			CodGun gun = new CodGun(name, gunType, unlockType, ammoAmount, ammoItem, gunItem, levelUnlock);

			gun.setCreditUnlock(cost);

			gun.save();

			sendMessage(p, codPrefix + Lang.GUN_CREATED_SUCCESS.getMessage().replace("{gun-name}", name).replace("{gun-type}", gunType.toString()), lang);

			switch (gunType) {
			case Primary:
				ArrayList<CodGun> pList = Main.shopManager.getPrimaryGuns();
				pList.add(gun);
				Main.shopManager.setPrimaryGuns(pList);
				break;
			case Secondary:
				ArrayList<CodGun> sList = Main.shopManager.getSecondaryGuns();
				sList.add(gun);
				Main.shopManager.setSecondaryGuns(sList);
				break;
			default:
				break;
			}
		} else {
			sendMessage(p, Main.codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command) , lang);
		}

	}

	public static RankPerks getRank(Player p) {
		for (RankPerks perk : Main.serverRanks) {
			if (p.hasPermission("com." + perk.getName())) {
				return perk;
			}
		}

		for (RankPerks perk : Main.serverRanks) {
			if (perk.getName().equals("default")) {
				return perk;
			}
		}

		return new RankPerks("default", 1, 100D, 10);
	}

	private static void sendMessage(CommandSender target, String message) {
		target.sendMessage(message);
	}

	public static void sendMessage(CommandSender target, String message, Object targetLang) {

		if (targetLang == McLang.EN || !ComVersion.getPurchased()) {
			sendMessage(target, message);
			return;
		}

		String translatedMessage;

		try {
			translatedMessage = ((McTranslate)translate).translateRuntime(message, McLang.EN, (McLang) targetLang);
		} catch (Exception e) {
			sendMessage(target, message);
			return;
		}

		sendMessage(target, translatedMessage);
	}

	public static void sendTitle(Player p, String title, String subtitle, int... timings) {

		int start;
		int linger;
		int stop;

		if (timings.length < 3) {
			start = 10;
			linger = 20;
			stop = 10;
		} else {
			start = timings[0];
			linger = timings[1];
			stop = timings[2];
		}
		try {
			Class.forName("org.bukkit.entity.Player").getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class).invoke(p, title, subtitle, start, linger, stop);
		} catch(Exception e) {
			e.printStackTrace();
//			p.sendMessage(title);
//			p.sendMessage(subtitle);
		}
//		p.sendTitle(title, subtitle, 10, 0, 10);
	}

	public static void sendActionBar(Player p, String message) {
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
	}

	public static void openMainMenu(Player p) {
		p.openInventory(invManager.mainInventory);
		try {
			p.playSound(p.getLocation(), Sound.valueOf("BLOCK_CHEST_OPEN"), 4f, 1f);
		}catch(Exception e) {
			//fail silently and play legacy sound
			p.playSound(p.getLocation(), Sound.valueOf("CHEST_OPEN"), 4f, 1f);
		}
	}

	public static boolean hasQualityArms() {
		return hasQA;
	}

	public static boolean hasCrackShot() {
		return hasCS;
	}

	public static boolean isDisabling() {
		return disabling;
	}

}
