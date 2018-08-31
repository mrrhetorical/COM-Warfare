package com.rhetorical.cod;

import com.rhetorical.cod.analytics.CollectAnalytics;
import com.rhetorical.cod.files.*;
import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.object.*;
import com.rhetorical.tpp.McLang;
import com.rhetorical.tpp.api.McTranslate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends JavaPlugin {

	public static Plugin getPlugin() {
		return Bukkit.getServer().getPluginManager().getPlugin("COM-Warfare");
	}

	public static String codPrefix = "§f§l[§r§6COM§f§l]§r ";
	public static ConsoleCommandSender cs = Bukkit.getConsoleSender();

	private static String sql_api_key;
	private static String translate_api_key;

	public static ProgressionManager progManager;
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

	public static double defaultHealth = 100D;

	private static ArrayList<RankPerks> serverRanks = new ArrayList<>();

	public static Location lobbyLoc;
	private static HashMap<Player, Location> lastLoc = new HashMap<>();

	@Override
	public void onEnable() {

		String bukkitVersion = Bukkit.getServer().getBukkitVersion();

		if (!bukkitVersion.startsWith("1.13")) {
			Main.cs.sendMessage(Main.codPrefix + "§cYou are not on the right version of Spigot/Bukkit, COM-Warfare might not work as intended. To ensure it will work properly, please use version §f1.13§c!");
		}

		Main.cs.sendMessage(Main.codPrefix + "§fChecking dependencies...");

		DependencyManager dm = new DependencyManager();
		if (!dm.checkDependencies()) {
			if (getPlugin().getConfig().getBoolean("auto-download-dependency")) {
				Main.cs.sendMessage(Main.codPrefix + "§cOne or more dependencies were not found, will attempt to download them.");
				try {
					dm.downloadDependencies();
				} catch (Exception e) {
					Main.cs.sendMessage(Main.codPrefix + "§cCould not download dependencies! Make sure that the plugins folder can be written to!");
					Main.cs.sendMessage("§l§f[§6§lCAUTION§r§f] §r§cNot all dependencies for COM-Warfare are installed! The plugin likely will not work as intended!");
				}
			} else {
				Main.cs.sendMessage(Main.codPrefix + "§cCould not download dependencies! You must set the value for \"auto-download-dependency\" to 'true' in the config to automatically download them!");
				Main.cs.sendMessage("§l§f[§6§lCAUTION§r§f] §r§cNot all dependencies for COM-Warfare are installed! The plugin likely will not work as intended!");
			}
		} else {
			Main.cs.sendMessage(Main.codPrefix + "§aAll dependencies are installed!");
		}

		try {
			if (getPlugin().getConfig().getString("lang").equalsIgnoreCase("none")) {
				lang = McLang.EN;
			} else {
				try {
					lang = McLang.valueOf(getPlugin().getConfig().getString("lang"));
				} catch (Exception e) {
					lang = McLang.EN;
					cs.sendMessage(codPrefix + "§cCould not get the language from the config! Make sure you're using the right two letter abbreviation!");
				}

				if (lang != McLang.EN)
					lang = McLang.EN;
			}
		} catch(Exception classException) {
			Main.cs.sendMessage(Main.codPrefix + "§cMcTranslate++ Doesn't seem to be installed? If you have 'auto-download-dependencies' turned on, it will automatically install, and after installing, you should restart the server!");
		}

		String version = getPlugin().getDescription().getVersion();

		CollectAnalytics.collectPlayerStats();

		ProgressionFile.setup(getPlugin());
		ArenasFile.setup(getPlugin());
		CreditsFile.setup(getPlugin());
		GunsFile.setup(getPlugin());
		ShopFile.setup(getPlugin());
		LoadoutsFile.setup(getPlugin());
		StatsFile.setup(getPlugin());
		KillstreaksFile.setup(getPlugin());

		getPlugin().saveDefaultConfig();
		getPlugin().reloadConfig();

		progManager = new ProgressionManager();
		loadManager = new LoadoutManager(new HashMap<>());
		perkManager = new PerkManager();
		invManager = new InventoryManager();
		shopManager = new ShopManager();
		perkListener = new PerkListener();
		killstreakManager = new KillStreakManager();

		KillStreakManager.setup();

		Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), getPlugin());

		// Set up all plugin functions loaded from the config are below.

		GameManager.loadMaps();

		for (Player p : Bukkit.getOnlinePlayers()) {
			loadManager.load(p);
			CreditManager.loadCredits(p);
		}

		minPlayers = getPlugin().getConfig().getInt("minPlayers");
		lobbyLoc = (Location) getPlugin().getConfig().get("com.lobby");
		defaultHealth = getPlugin().getConfig().getDouble("defaultHealth");
		sql_api_key = getPlugin().getConfig().getString("sql.api_key");
		translate_api_key = getPlugin().getConfig().getString("translate.api_key");

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
			i++;
		}

		Main.cs.sendMessage(Main.codPrefix + "There are " + i + " ranks registered!");
		for (RankPerks r : Main.serverRanks) {
			sendMessage(cs, Main.codPrefix + "Rank registered: " + r.getName(), lang);
		}
		
		try {
			translate = new McTranslate(Main.getPlugin(), Main.translate_api_key);
		} catch(Exception e) {
			Main.sendMessage(cs, Main.codPrefix + "§fAttempting to reconnect to McTranslate++ API...");
			BukkitRunnable tryTranslateAgain = new BukkitRunnable() {
				public void run() {
					try {
						translate = new McTranslate(Main.getPlugin(), Main.translate_api_key);
					} catch(Exception e) {
						Main.sendMessage(Main.cs, Main.codPrefix + "§cCould not start McTranslate++ API!");
						return;
					}
					
					Main.sendMessage(Main.cs, Main.codPrefix + "§aSuccessfully started McTranslate++ API!");
					this.cancel();
					
				}
			};
			
			tryTranslateAgain.runTaskTimer(getPlugin(), 200L, 200L);
		}

		Main.cs.sendMessage(Main.codPrefix + "§a§lCOM-Warfare version §r§f" + version + "§r§a§l is now up and running!");
	}

	@Override
	public void onDisable() {
		if (GameManager.AddedMaps.size() != 0) {
			for (CodMap m : GameManager.AddedMaps) {
				m.save();
			}

			for (GameInstance i : GameManager.RunningGames) {
				if (i != null) {
					for (Player p : i.getPlayers()) {
						GameManager.leaveMatch(p);
					}
				}
			}
		}
	}

	public static boolean hasPerm(Player p, String s) {
		if (p.hasPermission(s) || p.hasPermission("com.*")) {
			return true;
		} else {
			sendMessage(p, Main.codPrefix + "§cYou don't have permission to do that!", lang);
			return false;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!label.equalsIgnoreCase("cod") && !label.equalsIgnoreCase("comr") && !label.equalsIgnoreCase("war") && !label.equalsIgnoreCase("com"))
			return false;

		Player p = null;

		String cColor = "§a§l";
		String dColor = "§f§l";

		if (!(sender instanceof Player)) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("help")) {
					sendMessage(cs, "§6§lCOM-Warfare Help §f[§lPage 1 of 1§r§l]", lang);

					sendMessage(cs, "§f§lType the command to see the specifics on how to use it.", lang);
					sendMessage(cs, cColor + "/cod giveCredits {name} [amount] | " + dColor + "Gives an amount of credits to a player.");
					sendMessage(cs, cColor + "/cod setCredits {name} [amount] | " + dColor + "Sets the credits amount for a player.");
					return true;
				}
			} else if (args.length >= 3) {
				if (args[0].equalsIgnoreCase("giveCredits")) {
					String playerName = args[1];
					int amount;
					try {
						amount = Integer.parseInt(args[1]);
					} catch (Exception e) {
						amount = 0;
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
						amount = 0;
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
			sendMessage(cs, "§cYou must be a player to execute these commands for COM-Warfare!", lang);
			return true;
		}

		p = (Player) sender;

		if (args.length == 0) {
			p.openInventory(invManager.mainInventory);
			return true;
		} else if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("help") && hasPerm(p, "com.help")) {

				if (args.length == 2) {
					int page = 0;
					try {
						page = Integer.parseInt(args[1]);
					} catch (Exception e) {
						sendMessage(p, Main.codPrefix + "§cYou didn't specify a proper page.", lang);
						return true;
					}

					if (!(page > 0 && page <= 3)) {
						sendMessage(p, Main.codPrefix + "§cYou didn't give a proper page number!", lang);
						return true;
					}

					sendMessage(p, "-===§6§lCOM-Warfare Help§r===-", lang);
					sendMessage(p, "§f[§lPage " + page + " of 3§r§l]", lang);

					switch (page) {
					case 1:
						sendMessage(p, "§f§lType the command to see specifics.", lang);
						sendMessage(p, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
						sendMessage(p, cColor + "/cod | " + dColor + "Opens the main menu.");
						sendMessage(p, cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
						sendMessage(p, cColor + "/cod leave | " + dColor + "Leaves the current game.");
						sendMessage(p, cColor + "/cod listMaps | " + dColor + "Lists the avaiable maps.");
						sendMessage(p, cColor + "/cod createMap [args] | " + dColor + "Create a map.");
						sendMessage(p, cColor + "/cod removeMap [name] | " + dColor + "Command to remove a map.");
						break;
					case 2:
						sendMessage(p, cColor + "/cod set [lobby/spawn/flag] | " + dColor + "Opens a page in the help menu.");
						sendMessage(p, cColor + "/cod credits give | " + dColor + "Gives credits to a person.");
						sendMessage(p, cColor + "/cod credits set | " + dColor + "Sets amount of credits for a player.");
						sendMessage(p, cColor + "/cod lobby | " + dColor + "Teleports you to the lobby.");
						sendMessage(p, cColor + "/cod createGun [args] | " + dColor + "Creats a gun.");
						sendMessage(p, cColor + "/cod shop | " + dColor + "Opens the shop.");

						break;
					case 3:
						sendMessage(p, cColor + "/cod class | " + dColor + "Opens the class selection menu.");
						sendMessage(p, cColor + "/cod start | " + dColor + "Auto-starts the match if the lobby timer is started.");
						break;
					default:
						break;
					}
				} else {
					sendMessage(p, "-===§6§lCOM-Warfare Help§r===-");
					sendMessage(p, "§f[§lPage 1 of 3§r§l]");

					sendMessage(p, "§f§lType the command to see specifics.");
					sendMessage(p, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
					sendMessage(p, cColor + "/cod | " + dColor + "Opens the main menu.");
					sendMessage(p, cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
					sendMessage(p, cColor + "/cod leave | " + dColor + "Leaves the current game.");
					sendMessage(p, cColor + "/cod listMaps | " + dColor + "Lists the avaiable maps.");
					sendMessage(p, cColor + "/cod createMap [args] | " + dColor + "Create a map.");
					sendMessage(p, cColor + "/cod removeMap [name] | " + dColor + "Command to remove a map.");

				}

			} else if (args[0].equalsIgnoreCase("join") && hasPerm(p, "com.join")) {
				boolean b = GameManager.findMatch(p);
				if (b) {
					loadManager.load(p);
					Location l = p.getLocation();
					Main.progManager.update(p);
					Main.lastLoc.put(p, l);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("leave") && hasPerm(p, "com.leave")) {
				GameManager.leaveMatch(p);
				if (lastLoc.containsKey(p)) {
					p.teleport(lastLoc.get(p));
					lastLoc.remove(p);
				} else {
					if (!(lobbyLoc == null || lobbyLoc.equals(null))) {
						p.teleport(lobbyLoc);
					}
				}

				return true;
			} else if (args[0].equalsIgnoreCase("listMaps") && hasPerm(p, "com.map.list")) {
				sendMessage(p, Main.codPrefix + "§f=====§6§lMap List§r§f=====", lang);
				int k = 0;
				for (CodMap m : GameManager.AddedMaps) {
					k++;
					if (GameManager.UsedMaps.contains(m)) {
						sendMessage(p, Integer.toString(k) + " - §6§lName: §r§a" + m.getName() + " §r§6§lGamemode: §r§c" + m.getGamemode().toString() + " §r§6§lStatus: §r§4IN USE", lang);
					} else {
						if (m.isEnabled()) {
							sendMessage(p, Integer.toString(k) + " - §6§lName: §r§a" + m.getName() + " §r§6§lGamemode: §r§c" + m.getGamemode().toString() + " §r§6§lStatus: §r§aAVAILABLE", lang);
							continue;
						}

						sendMessage(p, Integer.toString(k) + " - §6§lName: §r§a" + m.getName() + " ea§r§6§lGamemode: §r§c" + m.getGamemode().toString() + " §r§6§lStatus: §r§aUNFINISHED", lang);
					}
				}
				return true;
			} else if (args[0].equalsIgnoreCase("createMap") && hasPerm(p, "com.map.create")) {
				if (args.length >= 3) {
					CodMap newMap;
					String mapName = args[1];
					String mapGm = args[2];
					Gamemode mapGameMode;
					try {
						mapGameMode = Gamemode.valueOf(mapGm);
					} catch (Exception e) {
						sendMessage(p, Main.codPrefix + "§cThat gamemode doesn't exist!", lang);
						return true;
					}

					for (CodMap m : GameManager.AddedMaps) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							sendMessage(p, Main.codPrefix + "§cThere already exists a map with that name!", lang);
							return true;
						}
					}

					newMap = new CodMap(mapName, mapGameMode);

					GameManager.AddedMaps.add(newMap);
					sendMessage(p, Main.codPrefix + "§aSuccessfully created map " + newMap.getName() + " with gamemode " + newMap.getGamemode().toString(), lang);
					newMap.setEnable();
					return true;
				} else {
					sendMessage(p, Main.codPrefix + "§cIncorrect usage! Correct usage: /cod createMap (name) (gamemode)");
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
								aFile.delete();
							}

							ArenasFile.setup(getPlugin());

							for (CodMap notChanged : GameManager.AddedMaps) {
								notChanged.save();
							}

							sendMessage(p, Main.codPrefix + "§aSuccessfully removed map!", lang);
							return true;
						}
					}

					sendMessage(p, Main.codPrefix + "§cThere's no map with that name!", lang);
					return true;

				} else {
					sendMessage(p, Main.codPrefix + "§cIncorrect useage! Correct usage: /cod removeMap (name)");
					return true;
				}

			} else if (args[0].equalsIgnoreCase("set") && hasPerm(p, "com.map.modify")) {

				if (!(args.length > 1)) {
					sendMessage(p, Main.codPrefix + "§cIncorrect usage! Correct usage: /cod set (lobby/spawn/flag) [args]");
					return true;
				}

				if (args[1].equalsIgnoreCase("lobby")) {

					Location lobby = p.getLocation();
					getPlugin().getConfig().set("com.lobby", lobby);
					Main.lobbyLoc = (Location) getPlugin().getConfig().get("com.lobby");
					getPlugin().saveConfig();
					getPlugin().reloadConfig();
					sendMessage(p, Main.codPrefix + "§aSuccessfully set lobby to your location! (You might want to restart the server for it to take effect)", lang);
					return true;
				} else if (args[1].equalsIgnoreCase("spawn") && hasPerm(p, "com.map.addSpawn")) {

					if (args.length < 4) {
						sendMessage(p, Main.codPrefix + "§cIncorrect usage! Correct usage: /cod set spawn (map name) (team)");
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
						sendMessage(p, Main.codPrefix + "§cThat map doesn't exist! Map names are case sensitive!", lang);
						return true;
					}

					String spawnTeam = args[3];
					switch (spawnTeam.toUpperCase()) {
					case "RED":
						map.addRedSpawn(p.getLocation());
						sendMessage(p, Main.codPrefix + "§aSuccessfully created §cRED §aspawn for map §6" + map.getName(), lang);
						map.setEnable();
						return true;
					case "BLUE":
						map.addblueSpawn(p.getLocation());
						sendMessage(p, Main.codPrefix + "§aSuccessfully created §9BLUE §aspawn for map §6" + map.getName(), lang);
						map.setEnable();
						return true;
					case "PINK":
						map.addPinkSpawn(p.getLocation());
						sendMessage(p, Main.codPrefix + "§aSuccessfully created §dPINK §aspawn for map §6" + map.getName(), lang);
						map.setEnable();
						return true;
					default:
						sendMessage(p, Main.codPrefix + "§cThat's not a valid team!", lang);
						return true;
					}
				} else if (args[1].equalsIgnoreCase("flag") && hasPerm(p, "com.map.modify")) {

					if (args.length < 4) {
						sendMessage(p, Main.codPrefix + "§cIncorrect usage! Correct usage: /cod set flag (map name) (red/blue/a/b/c)");
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
						sendMessage(p, Main.codPrefix + "§cThat map doesn't exist! Map names are case sensitive!", lang);
						return true;
					}

					String arg = args[3];

					switch(arg.toLowerCase()) {
						case "red":
							map.addRedFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "§aSuccessfully set §cred §aCTF flag spawn!");
							return true;
						case "blue":
							map.addBlueFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "§aSuccessfully set §9blue §aCTF flag spawn!");
							return true;
						case "a":
							map.addAFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "§aSuccessfully set §eA DOM §aflag spawn!");
							return true;
						case "b":
							map.addBFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "§aSuccessfully set §eB DOM §aflag spawn!");
							return true;
						case "c":
							map.addCFlagSpawn(p.getLocation());
							sendMessage(p, Main.codPrefix + "§aSuccessfully set §eC DOM §aflag spawn!");
							return true;
						default:
							sendMessage(p, Main.codPrefix + "§cIncorrect usage! Available flags are \"A\", \"B\", \"C\", \"Blue\", or \"Red\"!");
							return true;
					}

				}

			} else if (args[0].equalsIgnoreCase("lobby") && hasPerm(p, "com.lobby")) {
				if (lobbyLoc != null) {
					p.teleport(lobbyLoc);
				} else {
					sendMessage(p, Main.codPrefix + "§cThere's no lobby to send you to!", lang);
				}
			} else if (args[0].equalsIgnoreCase("credits")) {
				if (!(args.length >= 3)) {
					sendMessage(p, Main.codPrefix + "§cIncorrect usage! Proper usage: '/cod credits [give/set] {player} (amount)'");
					return true;
				}
				if (args[1].equalsIgnoreCase("give") && hasPerm(p, "com.credits.give")) {
					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						amount = 0;
						sendMessage(p, Main.codPrefix + "§cIncorrect usage! Proper usage: '/cod credits give {player} (amount)'");
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);
					sendMessage(p, Main.codPrefix + "§aSuccessfully gave " + playerName + " " + Integer.toString(amount) + " credits! They now have " + CreditManager.getCredits(playerName) + " credits!", lang);
					return true;
				} else if (args[1].equalsIgnoreCase("set") && hasPerm(p, "com.credits.set")) {
					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						amount = 0;
						sendMessage(p, Main.codPrefix + "§cIncorrect usage! Proper usage: '/cod credits set {name} [amount]'");
						return true;
					}

					CreditManager.setCredits(playerName, amount);
					sendMessage(p, Main.codPrefix + "§aSuccessfully set " + playerName + "'s credit count to " + Integer.toString(amount) + "!", lang);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("createGun") && hasPerm(p, "com.createGun")) {

				if (args.length >= 9) {
					createGun(p, args);
					return true;
				} else {
					sendMessage(p, Main.codPrefix + "§cIncorrect usage! Correct usage: '/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credit/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)'");
					return true;
				}
			} else if ((args[0].equalsIgnoreCase("createWeapon") || args[0].equalsIgnoreCase("createGrenade")) && hasPerm(p, "com.createWeapon")) {
				if (args.length >= 7) {
					createWeapon(p, args);
					return true;
				} else {
					sendMessage(p, Main.codPrefix + "§cIncorrect usage! Correct usage: '/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credit/both) (Grenade Material) (Level Unlock) (Cost)'");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("start") && hasPerm(p, "com.forceStart")) {
				if (GameManager.isInMatch(p)) {
					try {
						GameManager.getMatchWhichContains(p).forceStart(true);
					} catch(Exception e) {
						sendMessage(Main.cs, Main.codPrefix + "§cCould not find the game that the player is in!", Main.lang	);
					}
					return true;
				} else {
					sendMessage(p, Main.codPrefix + "§cYou must be in a game to use that command!", lang);
				}

				return true;
			} else if (args[0].equalsIgnoreCase("class") && hasPerm(p, "com.selectClass")) {
				Main.invManager.openSelectClassInventory(p);
				return true;
			} else if (args[0].equalsIgnoreCase("shop") && hasPerm(p, "com.openShop")) {
				p.closeInventory();
				p.openInventory(invManager.mainShopInventory);
				return true;
			}
			return true;
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
				sendMessage(p, Main.codPrefix + "§cThat weapon type does not exist! 'tactical' and 'lethal' are the two available weapon types.", lang);
				return;
			}
			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cThat unlock type doesn't exist! 'level', 'credits', and 'both' are the only available options.", lang);
				return;
			}
			ItemStack grenade;

			try {
				grenade = new ItemStack(Material.valueOf(args[4].toUpperCase()));
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cThat material does not exist!", lang);
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[5]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cYou didn't provide a number for the level unlock!", lang);
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[6]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cYou didn't provide a number for the cost!", lang);
				return;
			}

			CodWeapon grenadeWeapon = new CodWeapon(name, grenadeType, unlockType, grenade, levelUnlock);

			grenadeWeapon.setCreditUnlock(cost);

			grenadeWeapon.save();

			sendMessage(p, codPrefix + "§aSuccessfully created weapon " + name + " as a " + grenadeType.toString() + " grenade!", lang);

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
			sendMessage(p, Main.codPrefix + "§cIncorrect usage! Correct usage: '/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credit/both) (Grenade Material) (Level Unlock) (Cost)'");
		}
	}

	private void createGun(Player p, String[] args) {
		if (args.length == 9) {
			String name = args[1];

			GunType gunType;

			try {
				gunType = GunType.valueOf(args[2]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cThat gun type doesn't exist! 'Primary' & 'Secondary' are the only two options.", lang);
				return;
			}

			UnlockType unlockType;

			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cThat unlock type doesn't exist! 'level', 'credits', and 'both' are the only available options.", lang);
				return;
			}

			int ammoAmount;

			try {
				ammoAmount = Integer.parseInt(args[4]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cYou didn't provide a number for the ammo type!", lang);
				return;
			}

			ItemStack gunItem;
			ItemStack ammoItem;

			try {
				gunItem = new ItemStack(Material.valueOf(args[5].toUpperCase()));
				ammoItem = new ItemStack(Material.valueOf(args[6].toUpperCase()));
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cEither the, primary, secondary, or both of the gun material do not exist!", lang);
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[7]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cYou didn't provide a number for the level unlock!", lang);
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[8]);
			} catch (Exception e) {
				sendMessage(p, Main.codPrefix + "§cYou didn't provide a number for the cost!", lang);
				return;
			}

			CodGun gun = new CodGun(name, gunType, unlockType, ammoAmount, ammoItem, gunItem, levelUnlock);

			gun.setCreditUnlock(cost);

			gun.save();

			sendMessage(p, codPrefix + "§aSuccessfully created gun " + name + " as a " + gunType.toString() + " weapon!", lang);

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
				return;
			}
		} else {
			sendMessage(p, Main.codPrefix + "§cIncorrect usage! Correct usage: '/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credit/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)'");
			return;
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

		if (targetLang.equals(McLang.EN)) {
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
		//TODO: Add translation capability.

		p.sendTitle(title, subtitle, 10, 0, 10);
	}

	public static void sendActionBar(Player p, String message) {
		//TODO: Implement
		throw new NotImplementedException();
	}

	public static String getSQLApiKey() {
		return Main.sql_api_key;
	}

	public static String getTranslatorApiKey() {
		return Main.translate_api_key;
	}
}
