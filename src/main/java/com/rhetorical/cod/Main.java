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

		ProgressionFile.setup(getPlugin());
		ArenasFile.setup(getPlugin());
		CreditsFile.setup(getPlugin());
		GunsFile.setup(getPlugin());
		ShopFile.setup(getPlugin());
		LoadoutsFile.setup(getPlugin());
		StatsFile.setup(getPlugin());
		KillstreaksFile.setup(getPlugin());

		System.out.println("Loading managers...");

		progressionManager = new ProgressionManager();
		System.out.println("Progression Manager loaded");
		perkManager = new PerkManager();
		System.out.println("Perk Manager loaded");
		loadManager = new LoadoutManager(new HashMap<>());
		System.out.println("Loadout Manager loaded");
		shopManager = new ShopManager();
		System.out.println("Shop Manager loaded");
		perkListener = new PerkListener();
		System.out.println("Perk Listener loaded");
		killstreakManager = new KillStreakManager();
		System.out.println("Killstreak Manager loaded");
		invManager = new InventoryManager();
		System.out.println("Inventory Manager loaded");

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

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!label.equalsIgnoreCase("cod") && !label.equalsIgnoreCase("comr") && !label.equalsIgnoreCase("war") && !label.equalsIgnoreCase("com"))
			return false;

		Player p;

		String cColor = "" + ChatColor.GREEN + ChatColor.BOLD;
		String dColor = "" + ChatColor.WHITE + ChatColor.BOLD;

		if (!(sender instanceof Player)) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("help")) {
					sendMessage(cs, "" + ChatColor.GOLD + ChatColor.BOLD + "COM-Warfare Help " + ChatColor.WHITE + "[" + ChatColor.BOLD + "Page 1 of 1" + ChatColor.RESET + ChatColor.WHITE + "]", lang);

					sendMessage(cs, "" + ChatColor.WHITE + ChatColor.BOLD + "Type the command to see the specifics on how to use it.", lang);
					sendMessage(cs, cColor + "/cod giveCredits {name} [amount] | " + dColor + "Gives an amount of credits to a player.");
					sendMessage(cs, cColor + "/cod setCredits {name} [amount] | " + dColor + "Sets the credits amount for a player.");
					sendMessage(cs, cColor + "/cod version | " + dColor + "Shows you the current version COM-Warfare is running on.");
					sendMessage(cs, cColor + "/cod connectToTranslate | " + dColor + "Attempts to manually connect to the translate service.");
					return true;
				} else if (args[0].equalsIgnoreCase("version") && hasPerm(sender, "com.version")) {
					String version = getPlugin().getDescription().getVersion();
					sender.sendMessage(Main.codPrefix + ChatColor.GREEN + ChatColor.BOLD + "COM-Warfare version " + ChatColor.RESET + ChatColor.WHITE + version + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + " is currently installed on the server!");
					return true;
				} else if (args[0].equalsIgnoreCase("connectToTranslate") && hasPerm(sender, "com.connectToTranslate")) {
					sender.sendMessage(Main.codPrefix + ChatColor.GRAY + "Attempting to connect to translate service again...");
					connectToTranslationService();
					return true;
				}
			} else if (args.length >= 3) {
				if (args[0].equalsIgnoreCase("giveCredits")) {
					String playerName = args[1];
					int amount;
					try {
						amount = Integer.parseInt(args[1]);
					} catch (Exception e) {
						sendMessage(cs, Main.codPrefix + "Incorrect usage! Proper usage: '/cod giveCredits {name} [amount]");
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);
					return true;
				} else if (args[0].equalsIgnoreCase("setCredits")) {
					String playerName = args[1];
					int amount;
					try {
						amount = Integer.parseInt(args[1]);
					} catch (Exception e) {
						sendMessage(cs, Main.codPrefix + "Incorrect usage! Proper usage: '/cod setCredits {name} [amount]'");
						return true;
					}

					CreditManager.setCredits(playerName, amount);
					return true;
				}
			}
		}

		// Console commands ^^ | Player commands vv

		if (!(sender instanceof Player)) {
			sendMessage(cs, ChatColor.RED + "You must be a player to execute these commands for COM-Warfare!", lang);
			return true;
		}

		p = (Player) sender;

		if (args.length == 0) {
			openMainMenu(p);
			return true;
		} else {
			if (args[0].equalsIgnoreCase("help") && hasPerm(p, "com.help")) {

				if (args.length == 2) {
					int page;
					try {
						page = Integer.parseInt(args[1]);
					} catch (Exception e) {
						sendMessage(p, Main.codPrefix + ChatColor.RED + "You didn't specify a proper page.", lang);
						return true;
					}

					if (!(page > 0 && page <= 4)) {
						sendMessage(p, Main.codPrefix + ChatColor.RED + "You didn't give a proper page number!", lang);
						return true;
					}

					//FIXME: Left off here converting to ChatColor!

					sendMessage(p, "-===\u00A76\u00A7lCOM-Warfare Help\u00A7r===-", lang);
					sendMessage(p, "\u00A7f[\u00A7lPage " + page + " of 4\u00A7r\u00A7l]", lang);

					switch (page) {
						case 1:
							sendMessage(p, "\u00A7f\u00A7lType the command to see specifics.", lang);
							sendMessage(p, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
							sendMessage(p, cColor + "/cod | " + dColor + "Opens the main menu.");
							sendMessage(p, cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
							sendMessage(p, cColor + "/cod leave | " + dColor + "Leaves the current game.");
							sendMessage(p, cColor + "/cod shop | " + dColor + "Opens the shop.");
							break;
						case 2:
							sendMessage(p, cColor + "/cod createMap [name] | " + dColor + "Create a map.");
							sendMessage(p, cColor + "/cod removeMap [name] | " + dColor + "Command to remove a map.");
							sendMessage(p, cColor + "/cod listMaps | " + dColor + "Lists the available maps.");
							sendMessage(p, cColor + "/cod set [lobby/spawn/flag] | " + dColor + "Command to set spawns, flags, and lobby location.");
							sendMessage(p, cColor + "/cod lobby | " + dColor + "Teleports you to the lobby.");
							break;
						case 3:
							sendMessage(p, cColor + "/cod shop | " + dColor + "Opens the shop.");
							sendMessage(p, cColor + "/cod createGun | " + dColor + "Creates a gun. Type command to see a full list of arguments.");
							sendMessage(p, cColor + "/cod credits give [player] (amt) | " + dColor + "Gives credits to a person.");
							sendMessage(p, cColor + "/cod credits set [player] (amt) | " + dColor + "Sets amount of credits for a player.");
							sendMessage(p, cColor + "/cod balance | " + dColor + "Shows your credit balance.");
							break;
						case 4:
							sendMessage(p, cColor + "/cod add [oitc/gun] (gun name) | " + dColor + "Sets the gun for OITC or adds a gun to Gun Game.");
							sendMessage(p, cColor + "/cod changeMap/changeMode [map name/gamemode] | " + dColor + "Changes the current map/mode.");
							sendMessage(p, cColor + "/cod class | " + dColor + "Opens the class selection menu.");
							sendMessage(p, cColor + "/cod start | " + dColor + "Auto-starts the match if the lobby timer is started.");
							sendMessage(p, cColor + "/cod boot | " + dColor + "Forces all players in all matches to leave.");
							break;
						default:
							break;
					}
				} else {
					sendMessage(p, "-===\u00A76\u00A7lCOM-Warfare Help\u00A7r===-");
					sendMessage(p, "\u00A7f[\u00A7lPage 1 of 4\u00A7r\u00A7l]");

					sendMessage(p, "\u00A7f\u00A7lType the command to see specifics.", lang);
					sendMessage(p, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
					sendMessage(p, cColor + "/cod | " + dColor + "Opens the main menu.");
					sendMessage(p, cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
					sendMessage(p, cColor + "/cod leave | " + dColor + "Leaves the current game.");
					sendMessage(p, cColor + "/cod shop | " + dColor + "Opens the shop.");

				}

			} else if (args[0].equalsIgnoreCase("join") && hasPerm(p, "com.join")) {
				boolean b = GameManager.findMatch(p);
				if (b) {
					loadManager.load(p);
					Location l = p.getLocation();
					Main.progressionManager.update(p);
					Main.lastLoc.put(p, l);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("leave") && hasPerm(p, "com.leave")) {
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
			} else if (args[0].equalsIgnoreCase("listMaps") && hasPerm(p, "com.map.list")) {
				sendMessage(p, Main.codPrefix + "\u00A7f=====\u00A76\u00A7lMap List\u00A7r\u00A7f=====", lang);
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
						sendMessage(p, k + " - \u00A76\u00A7lName: \u00A7r\u00A7a" + m.getName() + " \u00A7r\u00A76\u00A7lGamemode: \u00A7r\u00A7c" + gmr.toString() + " \u00A7r\u00A76\u00A7lStatus: \u00A7r\u00A74IN USE", lang);
					} else {
						if (m.isEnabled()) {
							sendMessage(p, k + " - \u00A76\u00A7lName: \u00A7r\u00A7a" + m.getName() + " \u00A7r\u00A76\u00A7lGamemode: \u00A7r\u00A7c" + gmr.toString() + " \u00A7r\u00A76\u00A7lStatus: \u00A7r\u00A7aAVAILABLE", lang);
							continue;
						}

						sendMessage(p, k + " - \u00A76\u00A7lName: \u00A7r\u00A7a" + m.getName() + " \u00A7r\u00A76\u00A7lGamemode: \u00A7r\u00A7c" + gmr.toString() + " \u00A7r\u00A76\u00A7lStatus: \u00A7r\u00A7aUNFINISHED", lang);
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("createMap") && hasPerm(p, "com.map.create")) {
				if (args.length >= 2) {
					CodMap newMap;
					String mapName = args[1];

					for (CodMap m : GameManager.AddedMaps) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							sendMessage(p, Main.codPrefix + "\u00A7cThere already exists a map with that name!", lang);
							return true;
						}
					}

					newMap = new CodMap(mapName);

					GameManager.AddedMaps.add(newMap);
					sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully created map " + newMap.getName() + "!", lang);
					newMap.setEnable();
					return true;
				} else {
					sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: /cod createMap (name)");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("removeMap") && hasPerm(p, "com.map.remove")) {

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

							sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully removed map!", lang);
							return true;
						}
					}

					sendMessage(p, Main.codPrefix + "\u00A7cThere's no map with that name!", lang);
					return true;

				} else {
					sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Correct usage: /cod removeMap (name)");
					return true;
				}

			} else if (args[0].equalsIgnoreCase("set") && hasPerm(p, "com.map.modify")) {

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

			} else if (args[0].equalsIgnoreCase("lobby") && hasPerm(p, "com.lobby")) {
				if (lobbyLoc != null) {
					p.teleport(lobbyLoc);
				} else {
					sendMessage(p, Main.codPrefix + "\u00A7cThere's no lobby to send you to!", lang);
				}
			} else if (args[0].equalsIgnoreCase("balance")) {
				int credits = CreditManager.getCredits(p);
				sendMessage(p, codPrefix + ChatColor.GREEN + "You have " + credits + " credits!", lang);
			} else if (args[0].equalsIgnoreCase("credits")) {
				if (!(args.length >= 3)) {
					sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Proper usage: '/cod credits [give/set] {player} (amount)'");
					return true;
				}
				if (args[1].equalsIgnoreCase("give") && hasPerm(p, "com.credits.give")) {
					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						sendMessage(p, Main.codPrefix + "\u00A7cIncorrect usage! Proper usage: '/cod credits give {player} (amount)'");
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);
					sendMessage(p, Main.codPrefix + "\u00A7aSuccessfully gave " + playerName + " " + Integer.toString(amount) + " credits! They now have " + CreditManager.getCredits(playerName) + " credits!", lang);
					return true;
				} else if (args[1].equalsIgnoreCase("set") && hasPerm(p, "com.credits.set")) {
					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						sendMessage(p, Main.codPrefix + ChatColor.RED + "Incorrect usage! Proper usage: '/cod credits set {name} [amount]'");
						return true;
					}

					CreditManager.setCredits(playerName, amount);
					sendMessage(p, Main.codPrefix + ChatColor.GREEN + "Successfully set " + playerName + "'s credit count to " + Integer.toString(amount) + "!", lang);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("createGun") && hasPerm(p, "com.createGun")) {

				if (args.length >= 9) {
					createGun(p, args);
					return true;
				} else {
					sendMessage(p, Main.codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credit/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)'");
					return true;
				}
			} else if ((args[0].equalsIgnoreCase("createWeapon") || args[0].equalsIgnoreCase("createGrenade")) && hasPerm(p, "com.createWeapon")) {
				if (args.length >= 7) {
					createWeapon(p, args);
					return true;
				} else {
					sendMessage(p, Main.codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credit/both) (Grenade Material) (Level Unlock) (Cost)'");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("start") && hasPerm(p, "com.forceStart")) {
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
			} else if (args[0].equalsIgnoreCase("class") && hasPerm(p, "com.selectClass")) {
				Main.invManager.openSelectClassInventory(p);
				return true;
			} else if (args[0].equalsIgnoreCase("shop") && hasPerm(p, "com.openShop")) {
				p.closeInventory();
				p.openInventory(invManager.mainShopInventory);
				return true;
			} else if (args[0].equalsIgnoreCase("boot") && hasPerm(p, "com.bootAll")) {
				boolean result = bootPlayers();
				if (result) {
					p.sendMessage(Main.codPrefix + ChatColor.GREEN + "All players booted!");
				} else {
					p.sendMessage(Main.codPrefix + ChatColor.RED + "Couldn't boot all players!");
				}
			} else if (args[0].equalsIgnoreCase("add") && hasPerm(p, "com.add")) {
				if (args.length	< 3) {
					sendMessage(p, ChatColor.RED + "Incorrect usage! Correct usage: '/cod add [oitc/gun] (gun name)'");
					return true;
				}

				String type = args[1];
				String gunName = args[2];
				CodWeapon weapon = shopManager.getWeaponForName(gunName);

				if (!(weapon instanceof CodGun)) {
					sendMessage(p, ChatColor.RED + "Could not find a weapon with the name: \"" + gunName + "\"!");
					return true;
				}

				if (type.equalsIgnoreCase("oitc")) {
					getConfig().set("OITC_Gun", weapon.getName());
					saveConfig();
					reloadConfig();
					GameManager.setupOITC();
					sendMessage(p, Main.codPrefix + ChatColor.GREEN + "Successfully set OITC gun to \"" + weapon.getName() + "\"!");
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
					sendMessage(p, Main.codPrefix + ChatColor.GREEN + "Successfully added gun to the Gun Game progression!");
					return true;
				}
				sendMessage(p, Main.codPrefix + ChatColor.RED + "Incorrect usage! Correct usage: '/cod add [oitc/gun] (gun name)'");
				return true;
			} else if (args[0].equalsIgnoreCase("changeMap") && hasPerm(p, "com.changeMap")) {

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
			} else if (args[0].equalsIgnoreCase("changeMode") && hasPerm(p, "com.changeMode")) {
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
			} else {
				p.sendMessage(Main.codPrefix + ChatColor.RED + "Unknown command! Try using '/cod help' for a list of commands!");
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

	private void createWeapon(Player p, String[] args) {
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

	private void createGun(Player p, String[] args) {
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
