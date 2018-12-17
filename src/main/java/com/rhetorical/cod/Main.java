package com.rhetorical.cod;

import com.rhetorical.cod.files.*;
import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.object.*;
import com.rhetorical.tpp.McLang;
import com.rhetorical.tpp.api.McTranslate;
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

class PatchNotes {
	private static String[] notes = {
			"Added:\n" + ChatColor.GREEN +
					"- '/cod notes' to check patch notes. Permission: 'com.patchNotes'\n" +
					"- Message, experience, and credits when downing a player using final stand.",
			ChatColor.RESET + "Changes & Fixes:\n" + ChatColor.GRAY + 
					"* Fixed an issue where the player list wasn't updated after leaving a game or returning to the lobby.\n" +
					"* Fixed an issue where the player list didn't update names correctly. \n" +
					"* Fixed an issue where players spawned in the ground if the map was located in a different world from the lobby.\n" +
					"* Fixed an issue where when players used final stand it would produce an error in the console when they got back up.\n" +
					"* Fixed an issue with scavenger not working properly and producing an error.\n" +
					"* Fixed an issue with leveling up producing an error.\n" +
					"* When players level up they are now rewarded their guns when leaving the game or returning to the lobby.\n" +
					"* Players states are saved before joining cod, and is reset upon leaving. \n" +
					"* Fixed a bug when picking up items from within a game where it would produce an error to the console.\n" +
					"* Fixed a bug where players in last stand would become invincible.\n" +
					"* Fixed a bug where players would be able to use final stand more than once at a time.\n" +
					"* Fixed a bug where players would have super speed when they got out of final stand.\n" +
					"* Players in final stand now have their speed set to 0."
	};
	static void getPatchNotes(CommandSender s) {
		s.sendMessage("=====[" + ChatColor.BOLD + ChatColor.RED + " COM-Warfare " + ChatColor.GOLD	+ Main.getPlugin().getDescription().getVersion() + ChatColor.RESET + "]=====");
		for (String note : notes) {
			s.sendMessage(note);
		}
	}
}

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

	public static Object lang;
	private static Object translate;
	
	public static int minPlayers = 6;
	public static int maxPlayers = 12;

	static boolean serverMode = false;

	public static double defaultHealth = 20D;

	private static ArrayList<RankPerks> serverRanks = new ArrayList<>();

	public static Location lobbyLoc;
	public static HashMap<Player, Location> lastLoc = new HashMap<>();

	public static String header = "[COM-Warfare]";

	private Metrics bMetrics;

	@Override
	public void onEnable() {

		ComVersion.setup(true);

		getPlugin().saveDefaultConfig();
		getPlugin().reloadConfig();

		if (ComVersion.getPurchased()) {
			codPrefix = getPlugin().getConfig().getString("prefix") + " ";

			if (codPrefix.equalsIgnoreCase("")) {
				codPrefix = "[COD] ";
			}
		}

		bMetrics = new Metrics(this);

		String bukkitVersion = Bukkit.getServer().getBukkitVersion();

		if (bukkitVersion.startsWith("1.8")) {
			Main.cs.sendMessage(Main.codPrefix + ChatColor.RED + "You are not on the most recent version of Spigot/Bukkit, so COM-Warfare might not work as advertised. To ensure it will work properly, please use version " + ChatColor.WHITE + "1.9 - 1.13" + ChatColor.RED + "!");
		}

		Main.cs.sendMessage(Main.codPrefix + "Checking dependencies...");

		DependencyManager dm = new DependencyManager();
		if (!dm.checkDependencies()) {
			if (getPlugin().getConfig().getBoolean("auto-download-dependency")) {
				Main.cs.sendMessage(Main.codPrefix + ChatColor.RED + "One or more dependencies were not found, will attempt to download them.");
				try {
					dm.downloadDependencies();
				} catch (Exception e) {
					Main.cs.sendMessage(Main.codPrefix + ChatColor.RED +"Could not download dependencies! Make sure that the plugins folder can be written to!");
					Main.cs.sendMessage(Main.codPrefix + ChatColor.RED + "Not all dependencies for COM-Warfare are installed! The plugin likely will not work as intended!");
				}
			} else {
				Main.cs.sendMessage(Main.codPrefix + ChatColor.RED + "Could not download dependencies! You must set the value for \"auto-download-dependency\" to 'true' in the config to automatically download them!");
				Main.cs.sendMessage(ChatColor.RED + "Not all dependencies for COM-Warfare are installed! The plugin likely will not work as intended!");
			}
		} else {
			Main.cs.sendMessage(Main.codPrefix + ChatColor.GREEN + "All dependencies are installed!");
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
			Main.cs.sendMessage(Main.codPrefix + ChatColor.RED + "McTranslate++ Doesn't seem to be installed? If you have 'auto-download-dependencies' turned on, it will automatically install, and after installing, you should restart the server!");
		}

		String version = getPlugin().getDescription().getVersion();


		LangFile.setup(getPlugin());
		ProgressionFile.setup(getPlugin());
		ArenasFile.setup(getPlugin());
		CreditsFile.setup(getPlugin());
		GunsFile.setup(getPlugin());
		ShopFile.setup(getPlugin());
		LoadoutsFile.setup(getPlugin());
		StatsFile.setup(getPlugin());
		KillstreaksFile.setup(getPlugin());

		progressionManager = new ProgressionManager();
		perkManager = new PerkManager();
		loadManager = new LoadoutManager(new HashMap<>());
		shopManager = new ShopManager();
		perkListener = new PerkListener();
		killstreakManager = new KillStreakManager();
		invManager = new InventoryManager();

		KillStreakManager.setup();

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

		Main.cs.sendMessage(Main.codPrefix + ChatColor.GREEN + ChatColor.BOLD + "COM-Warfare version " + ChatColor.RESET + ChatColor.WHITE + version + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + " is now up and running!");
	}

	@Override
	public void onDisable() {
		if (GameManager.AddedMaps.size() != 0) {
			for (CodMap m : GameManager.AddedMaps) {
				m.save();
			}

			bootPlayers();
		}
	}

	static boolean hasPerm(CommandSender p, String s) {
		if (p.hasPermission(s) || p.hasPermission("com.*") || p instanceof ConsoleCommandSender || p.isOp()) {
			return true;
		} else {
			sendMessage(p, Main.codPrefix + ChatColor.RED + "You don't have permission to do that!", lang);
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
			if (!(sender instanceof Player)) {
				sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
				return true;
			}
			Player p = (Player) sender;
			openMainMenu(p);
			return true;
		} else {
			if (args[0].equalsIgnoreCase("help") && hasPerm(sender, "com.help")) {

				if (args.length == 2) {
					int page;
					try {
						page = Integer.parseInt(args[1]);
					} catch (Exception e) {
						sendMessage(sender, Main.codPrefix + ChatColor.RED + "You didn't specify a proper page.", lang);
						return true;
					}

					if (!(page > 0 && page <= 5)) {
						sendMessage(sender, Main.codPrefix + ChatColor.RED + "You didn't give a proper page number!", lang);
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

			} else if (args[0].equalsIgnoreCase("join") && hasPerm(sender, "com.join")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;
				boolean b = GameManager.findMatch(p);
				if (b) {
					loadManager.load(p);
					Location l = p.getLocation();
					Main.progressionManager.update(p);
					Main.lastLoc.put(p, l);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("leave") && hasPerm(sender, "com.leave")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
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
			} else if (args[0].equalsIgnoreCase("listMaps") && hasPerm(sender, "com.map.list")) {
				sendMessage(sender, Main.codPrefix + "\u00A7f=====\u00A76\u00A7lMap List\u00A7r\u00A7f=====", lang);
				int k = 0;
				for (CodMap m : GameManager.AddedMaps) {
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

					if (GameManager.UsedMaps.contains(m)) {
						sendMessage(sender, k + " - \u00A76\u00A7lName: \u00A7r\u00A7a" + m.getName() + " \u00A7r\u00A76\u00A7lGamemode: \u00A7r\u00A7c" + gmr.toString() + " \u00A7r\u00A76\u00A7lStatus: \u00A7r\u00A74IN USE", lang);
					} else {
						if (m.isEnabled()) {
							sendMessage(sender, k + " - \u00A76\u00A7lName: \u00A7r\u00A7a" + m.getName() + " \u00A7r\u00A76\u00A7lGamemode: \u00A7r\u00A7c" + gmr.toString() + " \u00A7r\u00A76\u00A7lStatus: \u00A7r\u00A7aAVAILABLE", lang);
							continue;
						}

						sendMessage(sender, k + " - \u00A76\u00A7lName: \u00A7r\u00A7a" + m.getName() + " \u00A7r\u00A76\u00A7lGamemode: \u00A7r\u00A7c" + gmr.toString() + " \u00A7r\u00A76\u00A7lStatus: \u00A7r\u00A7aUNFINISHED", lang);
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("createMap") && hasPerm(sender, "com.map.create")) {
				if (args.length >= 2) {
					CodMap newMap;
					String mapName = args[1];

					for (CodMap m : GameManager.AddedMaps) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							sendMessage(sender, Main.codPrefix + "\u00A7cThere already exists a map with that name!", lang);
							return true;
						}
					}

					newMap = new CodMap(mapName);

					GameManager.AddedMaps.add(newMap);
					sendMessage(sender, Main.codPrefix + "\u00A7aSuccessfully created map " + newMap.getName() + "!", lang);
					newMap.setEnable();
					return true;
				} else {
					sendMessage(sender, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: /cod createMap (name)");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("removeMap") && hasPerm(sender, "com.map.remove")) {

				if (args.length >= 2) {
					GameManager.loadMaps();

					String mapName = args[1];

					for (CodMap m : GameManager.AddedMaps) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							GameManager.AddedMaps.remove(m);

							File aFile = new File(getPlugin().getDataFolder(), "arenas.yml");

							if (aFile.exists()) {
								boolean success = aFile.delete();
							}

							ArenasFile.setup(getPlugin());

							for (CodMap notChanged : GameManager.AddedMaps) {
								notChanged.save();
							}

							sendMessage(sender, Main.codPrefix + "\u00A7aSuccessfully removed map!", lang);
							return true;
						}
					}

					sendMessage(sender, Main.codPrefix + "\u00A7cThere's no map with that name!", lang);
					return true;

				} else {
					sendMessage(sender, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: /cod removeMap (name)");
					return true;
				}

			} else if (args[0].equalsIgnoreCase("set") && hasPerm(sender, "com.map.modify")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;

				if (!(args.length > 1)) {
					sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: /cod set (lobby/spawn/flag) [args]");
					return true;
				}

				if (args[1].equalsIgnoreCase("lobby")) {

					Location lobby = p.getLocation();
					getPlugin().getConfig().set("com.lobby", lobby);
					Main.lobbyLoc = (Location) getPlugin().getConfig().get("com.lobby");
					getPlugin().saveConfig();
					getPlugin().reloadConfig();
					sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully set lobby to your location! (You might want to restart the server for it to take effect)", lang);
					return true;
				} else if (args[1].equalsIgnoreCase("spawn") && hasPerm(p, "com.map.addSpawn")) {

					if (args.length < 4) {
						sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: /cod set spawn (map name) (team)");
						return true;
					}
					CodMap map = null;
					String spawnMapName = args[2];
					for (CodMap m : GameManager.AddedMaps) {
						if (m.getName().equalsIgnoreCase(spawnMapName)) {
							map = m;
						}
					}

					if (map == null) {
						sendMessage(p, Main.codPrefix + "\u00A7cThat map doesn't exist! Map names are case sensitive!", lang);
						return true;
					}

					String spawnTeam = args[3];
					switch (spawnTeam.toUpperCase()) {
					case "RED":
						map.addRedSpawn(p.getLocation());
						sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully created \u00A7cRED \u00A7aspawn for map \u00A76" + map.getName(), lang);
						map.setEnable();
						return true;
					case "BLUE":
						map.addblueSpawn(p.getLocation());
						sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully created \u00A79BLUE \u00A7aspawn for map \u00A76" + map.getName(), lang);
						map.setEnable();
						return true;
					case "PINK":
						map.addPinkSpawn(p.getLocation());
						sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully created \u00A7dPINK \u00A7aspawn for map \u00A76" + map.getName(), lang);
						map.setEnable();
						return true;
					default:
						sendMessage(p, Main.codPrefix + "\u00A7cThat's not a valid team!", lang);
						return true;
					}
				} else if (args[1].equalsIgnoreCase("flag") && hasPerm(p, "com.map.modify")) {

					if (args.length < 4) {
						sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: /cod set flag (map name) (red/blue/a/b/c)");
						return true;
					}

					CodMap map = null;

					String mapName = args[2];
					for(CodMap m : GameManager.AddedMaps) {
						if (m.getName().equalsIgnoreCase(mapName)){
							map = m;
							break;
						}
					}

					if (map == null) {
						sendMessage(p, Main.codPrefix + "\u00A7cThat map doesn't exist! Map names are case sensitive!", lang);
						return true;
					}

					String arg = args[3];

					switch(arg.toLowerCase()) {
						case "red":
							map.addRedFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully set \u00A7cred \u00A7aCTF flag spawn!");
							return true;
						case "blue":
							map.addBlueFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully set \u00A79blue \u00A7aCTF flag spawn!");
							return true;
						case "a":
							map.addAFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully set \u00A7eA DOM \u00A7aflag spawn!");
							return true;
						case "b":
							map.addBFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully set \u00A7eB DOM \u00A7aflag spawn!");
							return true;
						case "c":
							map.addCFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully set \u00A7eC DOM \u00A7aflag spawn!");
							return true;
						default:
							sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Available flags are \"A\", \"B\", \"C\", \"Blue\", or \"Red\"!");
							return true;
					}

				}

			} else if (args[0].equalsIgnoreCase("lobby") && hasPerm(sender, "com.lobby")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;
				if (lobbyLoc != null) {
					p.teleport(lobbyLoc);
				} else {
					sendMessage(p, Main.codPrefix + "\u00A7cThere's no lobby to send you to!", lang);
				}
			} else if (args[0].equalsIgnoreCase("balance")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;
				int credits = CreditManager.getCredits(p);
				sendMessage(p, codPrefix + ChatColor.GREEN + "You have " + credits + " credits!", lang);
			} else if (args[0].equalsIgnoreCase("credits")) {
				if (!(args.length >= 3)) {
					sendMessage(sender, Main.codPrefix + "\u00A7cIncorrect usage! Proper usage: '/cod credits [give/set] {player} (amount)'");
					return true;
				}
				if (args[1].equalsIgnoreCase("give") && hasPerm(sender, "com.credits.give")) {
					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						sendMessage(sender, Main.codPrefix + "\u00A7cIncorrect usage! Proper usage: '/cod credits give {player} (amount)'");
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);
					sendMessage(sender, Main.codPrefix + "\u00A7aSuccessfully gave " + playerName + " " + Integer.toString(amount) + " credits! They now have " + CreditManager.getCredits(playerName) + " credits!", lang);
					return true;
				} else if (args[1].equalsIgnoreCase("set") && hasPerm(sender, "com.credits.set")) {
					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						sendMessage(sender, Main.codPrefix + ChatColor.RED + "Incorrect usage! Proper usage: '/cod credits set {name} [amount]'");
						return true;
					}

					CreditManager.setCredits(playerName, amount);
					sendMessage(sender, Main.codPrefix + ChatColor.GREEN + "Successfully set " + playerName + "'s credit count to " + Integer.toString(amount) + "!", lang);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("createGun") && hasPerm(sender, "com.createGun")) {

				if (args.length >= 9) {
					createGun(sender, args);
					return true;
				} else {
					sendMessage(sender, Main.codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credit/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)'");
					return true;
				}
			} else if ((args[0].equalsIgnoreCase("createWeapon") || args[0].equalsIgnoreCase("createGrenade")) && hasPerm(sender, "com.createWeapon")) {
				if (args.length >= 7) {
					createWeapon(sender, args);
					return true;
				} else {
					sendMessage(sender, Main.codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credit/both) (Grenade Material) (Level Unlock) (Cost)'");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("start") && hasPerm(sender, "com.forceStart")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;
				if (GameManager.isInMatch(p)) {
					try {
						if (GameManager.getMatchWhichContains(p) != null) {
							GameInstance game = GameManager.getMatchWhichContains(p);
							if (game != null) {
								game.forceStart(true);
							} else {
								p.sendMessage(codPrefix + ChatColor.RED + "Could not force start arena!");
							}
						}
					} catch(Exception e) {
						sendMessage(Main.cs, Main.codPrefix + ChatColor.RED + "Could not find the game that the player is in!", Main.lang	);
					}
					return true;
				} else {
					sendMessage(p, Main.codPrefix + ChatColor.RED + "You must be in a game to use that command!", lang);
				}

				return true;
			} else if (args[0].equalsIgnoreCase("class") && hasPerm(sender, "com.selectClass")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;
				Main.invManager.openSelectClassInventory(p);
				return true;
			} else if (args[0].equalsIgnoreCase("shop") && hasPerm(sender, "com.openShop")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;
				p.closeInventory();
				p.openInventory(invManager.mainShopInventory);
				return true;
			} else if (args[0].equalsIgnoreCase("boot") && hasPerm(sender, "com.bootAll")) {
				boolean result = bootPlayers();
				if (result) {
					sender.sendMessage(Main.codPrefix + ChatColor.GREEN + "All players booted!");
				} else {
					sender.sendMessage(Main.codPrefix + ChatColor.RED + "Couldn't boot all players!");
				}
			} else if (args[0].equalsIgnoreCase("add") && hasPerm(sender, "com.add")) {
				if (args.length	< 3) {
					sendMessage(sender, ChatColor.RED + "Incorrect usage! Correct usage: '/cod add [oitc/gun] (gun name)'");
					return true;
				}

				String type = args[1];
				String gunName = args[2];
				CodWeapon weapon = shopManager.getWeaponForName(gunName);

				if (!(weapon instanceof CodGun)) {
					sendMessage(sender, ChatColor.RED + "Could not find a weapon with the name: \"" + gunName + "\"!");
					return true;
				}

				if (type.equalsIgnoreCase("oitc")) {
					getConfig().set("OITC_Gun", weapon.getName());
					saveConfig();
					reloadConfig();
					GameManager.setupOITC();
					sendMessage(sender, Main.codPrefix + ChatColor.GREEN + "Successfully set OITC gun to \"" + weapon.getName() + "\"!");
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
					sendMessage(sender, Main.codPrefix + ChatColor.GREEN + "Successfully added gun to the Gun Game progression!");
					return true;
				}
				sendMessage(sender, Main.codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod add [oitc/gun] (gun name)'");
				return true;
			} else if (args[0].equalsIgnoreCase("changeMap") && hasPerm(sender, "com.changeMap")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;

				if (args.length < 2) {
					sendMessage(p, codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod changeMap (name)'");
					return true;
				}

				if (!GameManager.isInMatch(p)) {
					sendMessage(p, codPrefix + ChatColor.RED + "You aren't in a game!");
					return true;
				}

				CodMap map = GameManager.getMapForName(args[1]);

				if (map == null) {
					sendMessage(p, codPrefix + ChatColor.RED + "No map with that name exists!");
					return true;
				}

				GameManager.changeMap(Objects.requireNonNull(GameManager.getMatchWhichContains(p)), map);
				sendMessage(p, codPrefix + ChatColor.GREEN + "Successfully changed map to " + ChatColor.GOLD + ChatColor.BOLD + map.getName() + ChatColor.RESET + ChatColor.GREEN + "!");
				return true;
			} else if (args[0].equalsIgnoreCase("changeMode") && hasPerm(sender, "com.changeMode")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ChatColor.RED + "You must be a player to execute this command", lang);
					return true;
				}
				Player p = (Player) sender;
				if (args.length < 2) {
					sendMessage(p, codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod changeMode (name)'");
					return true;
				}

				if (!GameManager.isInMatch(p)) {
					sendMessage(p, codPrefix + ChatColor.RED + "You aren't in a game!");
					return true;
				}

				Gamemode mode;

				try {
					mode = Gamemode.valueOf(args[1]);
				} catch(Exception e) {
					sendMessage(p, codPrefix + ChatColor.RED + "No gamemode with that name exists!");
					return true;
				}

				if (!Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getMap().getAvailableGamemodes().contains(mode)) {
					sendMessage(p, codPrefix + ChatColor.RED + "That gamemode is not set up on that map!");
					return true;
				}

				Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getMap().changeGamemode(mode);
				sendMessage(p, codPrefix + ChatColor.GREEN + "Successfully changed game mode to " + ChatColor.GOLD + ChatColor.BOLD + mode.toString() + ChatColor.RESET + ChatColor.GREEN + "!");
				return true;
			} else if (args[0].equalsIgnoreCase("blacklist") && hasPerm(sender, "com.blacklist")) {
				if (args.length	< 3) {
					sendMessage(sender, codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod blacklist (map) (mode)'");
					return true;
				}

				Gamemode mode;

				try {
					mode = Gamemode.valueOf(args[2].toUpperCase());
				} catch(Exception e) {
					sendMessage(sender, codPrefix + ChatColor.RED + "No gamemode exists with that name!");
					return true;
				}

				CodMap map = GameManager.getMapForName(args[1]);

				if (map == null) {
					sendMessage(sender, codPrefix + ChatColor.RED + "No map exists with that name!");
					return true;
				}

				map.addToBlacklist(mode);

				sendMessage(sender, Main.codPrefix + ChatColor.GREEN + "Successfully blacklisted " + mode.toString() + " from " + map.getName() + "!");
				return true;
			} else if (args[0].equalsIgnoreCase("notes") && hasPerm(sender, "com.patchNotes")) {
				PatchNotes.getPatchNotes(sender);
				return true;
			} else {
				sender.sendMessage(Main.codPrefix + ChatColor.RED + "Unknown command! Try using '/cod help' for a list of commands!");
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
		for (GameInstance i : GameManager.RunningGames) {
			if (i != null) {
				try {
					for (Player p : i.getPlayers()) {
						GameManager.leaveMatch(p);
					}
				} catch(Exception e) {
					sendMessage(cs, Main.codPrefix + ChatColor.RED + "Couldn't successfully boot all players.");
					return false;
				}
			}
		}
		return true;
	}

	private void createWeapon(CommandSender p, String[] args) {
		if (args.length == 7) {
			String name = args[1];
			WeaponType grenadeType;
			UnlockType unlockType;

			try {
				grenadeType = WeaponType.valueOf(args[2].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cThat weapon type does not exist! 'tactical' and 'lethal' are the two available weapon types.", lang);
				return;
			}
			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cThat unlock type doesn't exist! 'level', 'credits', and 'both' are the only available options.", lang);
				return;
			}
			ItemStack grenade;

			try {
				grenade = new ItemStack(Material.valueOf(args[4].toUpperCase()));
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cThat material does not exist!", lang);
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[5]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cYou didn't provide a number for the level unlock!", lang);
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[6]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cYou didn't provide a number for the cost!", lang);
				return;
			}

			CodWeapon grenadeWeapon = new CodWeapon(name, grenadeType, unlockType, grenade, levelUnlock);

			grenadeWeapon.setCreditUnlock(cost);

			grenadeWeapon.save();

			sendMessage(p, codPrefix + "\u00A7aSuccessfully created weapon " + name + " as a " + grenadeType.toString() + " grenade!", lang);

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
			sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: '/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credit/both) (Grenade Material) (Level Unlock) (Cost)'");
		}
	}

	private void createGun(CommandSender p, String[] args) {
		if (args.length == 9) {
			String name = args[1];

			GunType gunType;

			try {
				gunType = GunType.valueOf(args[2]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cThat gun type doesn't exist! 'Primary' & 'Secondary' are the only two options.", lang);
				return;
			}

			UnlockType unlockType;

			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cThat unlock type doesn't exist! 'level', 'credits', and 'both' are the only available options.", lang);
				return;
			}

			int ammoAmount;

			try {
				ammoAmount = Integer.parseInt(args[4]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cYou didn't provide a number for the ammo type!", lang);
				return;
			}

			ItemStack gunItem;
			ItemStack ammoItem;

			try {
				gunItem = new ItemStack(Material.valueOf(args[5].toUpperCase()));
				ammoItem = new ItemStack(Material.valueOf(args[6].toUpperCase()));
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cEither the, primary, secondary, or both of the gun material do not exist!", lang);
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[7]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cYou didn't provide a number for the level unlock!", lang);
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[8]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "\u00A7cYou didn't provide a number for the cost!", lang);
				return;
			}

			CodGun gun = new CodGun(name, gunType, unlockType, ammoAmount, ammoItem, gunItem, levelUnlock);

			gun.setCreditUnlock(cost);

			gun.save();

			sendMessage(p, codPrefix + "\u00A7aSuccessfully created gun " + name + " as a " + gunType.toString() + " weapon!", lang);

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
			sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: '/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credit/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)'");
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

	public static void sendTitle(Player p, String title, String subtitle) {
		try {
			Object[] args = new Object[5];
			args[0] = title;
			args[1] = subtitle;
			args[2] = 10;
			args[3] = 20;
			args[4] = 10;
			p.getClass().getMethod("sendTitle", String.class, String.class, Integer.class, Integer.class, Integer.class).invoke(p, args);
		} catch(Exception e) {
			p.sendMessage(title);
			p.sendMessage(subtitle);
		}
//		p.sendTitle(title, subtitle, 10, 0, 10);
	}

	public static void sendActionBar(Player p, String message) {
		//TODO: Implement
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

	public static String getTranslatorApiKey() {
		return Main.translate_api_key;
	}
}
