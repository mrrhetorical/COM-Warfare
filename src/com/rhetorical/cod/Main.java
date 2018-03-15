package com.rhetorical.cod;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

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

import com.rhetorical.cod.analytics.CollectAnalytics;
import com.rhetorical.cod.files.ArenasFile;
import com.rhetorical.cod.files.CreditsFile;
import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.files.LoadoutsFile;
import com.rhetorical.cod.files.ProgressionFile;
import com.rhetorical.cod.files.ShopFile;
import com.rhetorical.cod.files.StatsFile;
import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.object.CodGun;
import com.rhetorical.cod.object.CodMap;
import com.rhetorical.cod.object.CodWeapon;
import com.rhetorical.cod.object.GameInstance;
import com.rhetorical.cod.object.Gamemode;
import com.rhetorical.cod.object.GunType;
import com.rhetorical.cod.object.Loadout;
import com.rhetorical.cod.object.RankPerks;
import com.rhetorical.cod.object.UnlockType;
import com.rhetorical.cod.object.WeaponType;

public class Main extends JavaPlugin {

	public static Plugin getPlugin() {
		return Bukkit.getServer().getPluginManager().getPlugin("CallofMinecraftRemastered");
	}

	public static String codPrefix = "§f§l[§r§6COM§f§l]§r ";
	public static ConsoleCommandSender cs = Bukkit.getServer().getConsoleSender();

	private static String sql_api_key;
	private static String translate_api_key;

	public static String bVersion;
	private static String version;

	public static ProgressionManager progManager;
	public static LoadoutManager loadManager;
	public static PerkManager perkManager;
	public static InventoryManager invManager;
	public static ShopManager shopManager;

	public static int minPlayers = 6;
	public static int maxPlayers = 12;

	public static double defaultHealth = 100D;

	private static ArrayList<RankPerks> serverRanks = new ArrayList<RankPerks>();

	public static Location lobbyLoc;
	private static HashMap<Player, Location> lastLoc = new HashMap<Player, Location>();

	private String getServerVersionOfBukkit() {
		return Bukkit.getServer().getBukkitVersion();
	}

	@Override
	public void onEnable() {

		version = getPlugin().getDescription().getVersion();

		CollectAnalytics.collectPlayerStats();

		ProgressionFile.setup(getPlugin());
		ArenasFile.setup(getPlugin());
		CreditsFile.setup(getPlugin());
		GunsFile.setup(getPlugin());
		ShopFile.setup(getPlugin());
		LoadoutsFile.setup(getPlugin());
		StatsFile.setup(getPlugin());

		getPlugin().saveDefaultConfig();
		getPlugin().reloadConfig();

		progManager = new ProgressionManager();
		loadManager = new LoadoutManager(new HashMap<Player, ArrayList<Loadout>>());
		perkManager = new PerkManager();
		invManager = new InventoryManager();
		shopManager = new ShopManager();

		Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), getPlugin());

		// Set up all plugin functions loaded from the config are below.

		GameManager.loadMaps();

		for (Player p : Bukkit.getOnlinePlayers()) {
			loadManager.load(p);
			CreditManager.loadCredits(p);
		}

		bVersion = getServerVersionOfBukkit();

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

		Main.cs.sendMessage(
				Main.codPrefix + "§a§lCOM-Warfare version §r§f" + version + "§r§a§l is now up and running!");

		Main.cs.sendMessage(Main.codPrefix + "There are " + i + "ranks registered!");
		for (RankPerks r : Main.serverRanks) {
			Main.cs.sendMessage("Rank registered: " + r.getName());
		}

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
			p.sendMessage(Main.codPrefix + "§cYou don't have permission to do that!");
			return false;
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!label.equalsIgnoreCase("cod"))
			return false;

		Player p = null;

		String cColor = "§a§l";
		String dColor = "§f§l";

		if (!(sender instanceof Player)) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("help")) {
					cs.sendMessage("§6§lCOM-Warfare Help §f[§lPage 1 of 2§r§l]");

					cs.sendMessage("§f§lType the command to see the specifics on how to use it.");
					cs.sendMessage(cColor + "/cod giveCredits {name} [amount] | " + dColor
							+ "Gives an amount of credits to a player.");
					cs.sendMessage(cColor + "/cod setCredits {name} [amount] | " + dColor
							+ "Sets the credits amount for a player.");

				}
			} else if (args.length >= 3) {
				if (args[0].equalsIgnoreCase("giveCredits")) {
					String playerName = args[1];
					int amount;
					try {
						amount = Integer.parseInt(args[1]);
					} catch (Exception e) {
						amount = 0;
						cs.sendMessage(
								Main.codPrefix + "Incorrect usage! Proper usage: '/cod giveCredits {name} [amount]");
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);

				} else if (args[0].equalsIgnoreCase("setCredits")) {
					String playerName = args[1];
					int amount;
					try {
						amount = Integer.parseInt(args[1]);
					} catch (Exception e) {
						amount = 0;
						cs.sendMessage(
								Main.codPrefix + "Incorrect usage! Proper usage: '/cod setCredits {name} [amount]'");
						return true;
					}

					CreditManager.setCredits(playerName, amount);
				}
			}
		}

		// Console commands ^^ | Player commands vv

		if (!(sender instanceof Player)) {
			cs.sendMessage("§cYou must be a player to execute these commands for COM-Warfare!");
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
						p.sendMessage(Main.codPrefix + "§cYou didn't specify a proper page.");
						return true;
					}

					if (!(page > 0 && page <= 3)) {
						p.sendMessage(Main.codPrefix + "§cYou didn't give a proper page number!");
						return true;
					}

					// TODO: Create the help command
					p.sendMessage("-===§6§lCOM-Warfare Help§r===-");
					p.sendMessage("§f[§lPage " + page + " of 3§r§l]");

					switch (page) {
					case 1:
						p.sendMessage("§f§lType the command to see specifics.");
						p.sendMessage(
								cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
						p.sendMessage(cColor + "/cod | " + dColor + "Opens the main menu.");
						p.sendMessage(cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
						p.sendMessage(cColor + "/cod leave | " + dColor + "Leaves the current game.");
						p.sendMessage(cColor + "/cod listMaps | " + dColor + "Lists the avaiable maps.");
						p.sendMessage(
								cColor + "/cod createMap [args] | " + dColor + "CCreate a map.");
						p.sendMessage(cColor + "/cod removeMap [name] | " + dColor + "Command to remove a map.");
						break;
					case 2:
						p.sendMessage(cColor + "/cod set [lobby/spawn] | " + dColor + "Opens a page in the help menu.");
						p.sendMessage(
								cColor + "/cod credits give | " + dColor + "Gives credits to a person.");
						p.sendMessage(cColor + "/cod credits set | " + dColor
								+ "Sets amount of credits for a player.");
						p.sendMessage(cColor + "/cod lobby | " + dColor + "Teleports you to the lobby.");
						p.sendMessage(cColor + "/cod createGun [args] | " + dColor
								+ "Creats a gun.");
						p.sendMessage(cColor + "/cod shop | " + dColor + "Opens the shop.");

						break;
					case 3:
						p.sendMessage(cColor + "/cod class | " + dColor + "Opens the class selection menu.");
						p.sendMessage(cColor + "/cod start | " + dColor
								+ "Auto-starts the match if the lobby timer is started.");
						break;
					default:
						break;
					}
				} else {
					p.sendMessage("-===§6§lCOM-Warfare Help§r===-");
					p.sendMessage("§f[§lPage 1 of 3§r§l]");

					
					p.sendMessage("§f§lType the command to see specifics.");
					p.sendMessage(
							cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
					p.sendMessage(cColor + "/cod | " + dColor + "Opens the main menu.");
					p.sendMessage(cColor + "/cod join | " + dColor + "Joins a game through the matchmaker.");
					p.sendMessage(cColor + "/cod leave | " + dColor + "Leaves the current game.");
					p.sendMessage(cColor + "/cod listMaps | " + dColor + "Lists the avaiable maps.");
					p.sendMessage(
							cColor + "/cod createMap [args] | " + dColor + "CCreate a map.");
					p.sendMessage(cColor + "/cod removeMap [name] | " + dColor + "Command to remove a map.");

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
					p.teleport(lobbyLoc);
				}

				return true;
			} else if (args[0].equalsIgnoreCase("listMaps") && hasPerm(p, "com.map.list")) {
				p.sendMessage(Main.codPrefix + "§f=====§6§lMap List§r§f=====");
				int k = 0;
				for (CodMap m : GameManager.AddedMaps) {
					k++;
					if (GameManager.UsedMaps.contains(m)) {
						p.sendMessage(Integer.toString(k) + " - §6§lName: §r§a" + m.getName() + " §r§6§lGamemode: §r§c"
								+ m.getGamemode().toString() + " §r§6§lStatus: §r§4IN USE");
						continue;
					} else {
						if (m.isEnabled()) {
							p.sendMessage(
									Integer.toString(k) + " - §6§lName: §r§a" + m.getName() + " §r§6§lGamemode: §r§c"
											+ m.getGamemode().toString() + " §r§6§lStatus: §r§aAVAILABLE");
							continue;
						}

						p.sendMessage(Integer.toString(k) + " - §6§lName: §r§a" + m.getName() + " §r§6§lGamemode: §r§c"
								+ m.getGamemode().toString() + " §r§6§lStatus: §r§aUNFINISHED");

						continue;
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
						p.sendMessage(Main.codPrefix + "§cThat gamemode doesn't exist!");
						return true;
					}

					for (CodMap m : GameManager.AddedMaps) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							p.sendMessage(Main.codPrefix + "§cThere already exists a map with that name!");
							return true;
						}
					}

					newMap = new CodMap(mapName, mapGameMode);

					GameManager.AddedMaps.add(newMap);
					p.sendMessage(Main.codPrefix + "§aSuccessfully created map " + newMap.getName() + " with gamemode "
							+ newMap.getGamemode().toString());
					newMap.setEnable();
					return true;
				} else {
					p.sendMessage(
							Main.codPrefix + "§cIncorrect usage! Correct usage: /cod createMap (name) (gamemode)");
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

							p.sendMessage(Main.codPrefix + "§aSuccessfully removed map!");
							return true;
						}
					}

					p.sendMessage(Main.codPrefix + "§cThere's no map with that name!");
					return true;

				} else {
					p.sendMessage(Main.codPrefix + "§cIncorrect useage! Correct usage: /cod removeMap (name)");
					return true;
				}

			} else if (args[0].equalsIgnoreCase("set") && hasPerm(p, "com.map.modify")) {

				if (!(args.length > 1)) {
					p.sendMessage(Main.codPrefix + "§cIncorrect usage! Correct usage: /cod set (lobby/spawn) [args]");
					return true;
				}

				if (args[1].equalsIgnoreCase("lobby")) {

					Location lobby = p.getLocation();
					getPlugin().getConfig().set("com.lobby", lobby);
					Main.lobbyLoc = (Location) getPlugin().getConfig().get("com.lobby");
					getPlugin().saveConfig();
					getPlugin().reloadConfig();
					p.sendMessage(Main.codPrefix
							+ "§aSuccessfully set lobby to your location! (You might want to restart the server for it to take effect)");
					return true;
				} else if (args[1].equalsIgnoreCase("spawn") && hasPerm(p, "com.map.addSpawn")) {

					if (!(args.length >= 4)) {
						p.sendMessage(
								Main.codPrefix + "§cIncorrect usage! Correct usage: /cod set spawn (map name) (team)");
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
						p.sendMessage(Main.codPrefix + "§cThat map doesn't exist! Map names are case sensitive!");
						return true;
					}

					String spawnTeam = args[3];
					switch (spawnTeam.toUpperCase()) {
					case "RED":
						map.addRedSpawn(p.getLocation());
						p.sendMessage(
								Main.codPrefix + "§aSuccessfully created §cRED §aspawn for map §6" + map.getName());
						map.setEnable();
						return true;
					case "BLUE":
						map.addblueSpawn(p.getLocation());
						p.sendMessage(
								Main.codPrefix + "§aSuccessfully created §9BLUE §aspawn for map §6" + map.getName());
						map.setEnable();
						return true;
					case "PINK":
						map.addPinkSpawn(p.getLocation());
						p.sendMessage(
								Main.codPrefix + "§aSuccessfully created §dPINK §aspawn for map §6" + map.getName());
						map.setEnable();
						return true;
					default:
						p.sendMessage(Main.codPrefix + "§cThat's not a valid team!");
						return true;
					}
				}

			} else if (args[0].equalsIgnoreCase("lobby") && hasPerm(p, "com.lobby")) {
				if (lobbyLoc != null) {
					p.teleport(lobbyLoc);
				} else {
					p.sendMessage(Main.codPrefix + "§cThere's no lobby to send you to!");
				}
			} else if (args[0].equalsIgnoreCase("credits")) {
				if (!(args.length >= 3)) {
					p.sendMessage(Main.codPrefix
							+ "§cIncorrect usage! Proper usage: '/cod credits [give/set] {player} (amount)'");
					return true;
				}
				if (args[1].equalsIgnoreCase("give") && hasPerm(p, "com.credits.give")) {
					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						amount = 0;
						p.sendMessage(Main.codPrefix
								+ "§cIncorrect usage! Proper usage: '/cod credits give {player} (amount)'");
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);
					p.sendMessage(Main.codPrefix + "§aSuccessfully gave " + playerName + " " + Integer.toString(amount)
							+ " credits! They now have " + CreditManager.getCredits(playerName) + " credits!");
					return true;
				} else if (args[1].equalsIgnoreCase("set") && hasPerm(p, "com.credits.set")) {
					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						amount = 0;
						p.sendMessage(
								Main.codPrefix + "§cIncorrect usage! Proper usage: '/cod credits set {name} [amount]'");
						return true;
					}

					CreditManager.setCredits(playerName, amount);
					p.sendMessage(Main.codPrefix + "§aSuccessfully set " + playerName + "'s credit count to "
							+ Integer.toString(amount) + "!");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("createGun") && hasPerm(p, "com.createGun")) {

				if (args.length >= 9) {
					createGun(p, args);
					return true;
				} else {
					p.sendMessage(Main.codPrefix
							+ "§cIncorrect usage! Correct usage: '/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credit/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)'");
					return true;
				}
			} else if ((args[0].equalsIgnoreCase("createWeapon") || args[0].equalsIgnoreCase("createGrenade"))
					&& hasPerm(p, "com.createWeapon")) {
				if (args.length >= 7) {
					createWeapon(p, args);
					return true;
				} else {
					p.sendMessage(Main.codPrefix
							+ "§cIncorrect usage! Correct usage: '/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credit/both) (Grenade Material) (Level Unlock) (Cost)'");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("start") && hasPerm(p, "com.forceStart")) {
				if (GameManager.isInMatch(p)) {
					GameManager.getMatchWhichContains(p).forceStart(true);
					return true;
				} else {
					p.sendMessage(Main.codPrefix + "§cYou must be in a game to use that command!");
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

	public void createWeapon(Player p, String[] args) {
		if (args.length == 7) {
			String name = args[1];
			WeaponType grenadeType;
			UnlockType unlockType;

			try {
				grenadeType = WeaponType.valueOf(args[2].toUpperCase());
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix
						+ "§cThat weapon type does not exist! 'tactical' and 'lethal' are the two available weapon types.");
				return;
			}
			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix
						+ "§cThat unlock type doesn't exist! 'level', 'credits', and 'both' are the only available options.");
				return;
			}
			ItemStack grenade;

			try {
				grenade = new ItemStack(Material.valueOf(args[4].toUpperCase()));
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix + "§cThat material does not exist!");
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[5]);
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix + "§cYou didn't provide a number for the level unlock!");
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[6]);
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix + "§cYou didn't provide a number for the cost!");
				return;
			}

			CodWeapon grenadeWeapon = new CodWeapon(name, grenadeType, unlockType, grenade, levelUnlock);

			grenadeWeapon.setCreditUnlock(cost);

			grenadeWeapon.save();

			p.sendMessage(codPrefix + "§aSuccessfully created weapon " + name + " as a " + grenadeType.toString()
					+ " grenade!");

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
				return;
			}

		} else {
			p.sendMessage(Main.codPrefix
					+ "§cIncorrect usage! Correct usage: '/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credit/both) (Grenade Material) (Level Unlock) (Cost)'");
			return;
		}
	}

	public void createGun(Player p, String[] args) {
		if (args.length == 9) {
			String name = args[1];

			GunType gunType;

			try {
				gunType = GunType.valueOf(args[2]);
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix
						+ "§cThat gun type doesn't exist! 'Primary' & 'Secondary' are the only two options.");
				return;
			}

			UnlockType unlockType;

			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix
						+ "§cThat unlock type doesn't exist! 'level', 'credits', and 'both' are the only available options.");
				return;
			}

			int ammoAmount;

			try {
				ammoAmount = Integer.parseInt(args[4]);
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix + "§cYou didn't provide a number for the ammo type!");
				return;
			}

			ItemStack gunItem;
			ItemStack ammoItem;

			try {
				gunItem = new ItemStack(Material.valueOf(args[5].toUpperCase()));
				ammoItem = new ItemStack(Material.valueOf(args[6].toUpperCase()));
			} catch (Exception e) {
				p.sendMessage(
						Main.codPrefix + "§cEither the, primary, secondary, or both of the gun material do not exist!");
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[7]);
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix + "§cYou didn't provide a number for the level unlock!");
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[8]);
			} catch (Exception e) {
				p.sendMessage(Main.codPrefix + "§cYou didn't provide a number for the cost!");
				return;
			}

			CodGun gun = new CodGun(name, gunType, unlockType, ammoAmount, ammoItem, gunItem, levelUnlock);

			gun.setCreditUnlock(cost);

			gun.save();

			p.sendMessage(
					codPrefix + "§aSuccessfully created gun " + name + " as a " + gunType.toString() + " weapon!");

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
			p.sendMessage(Main.codPrefix
					+ "§cIncorrect usage! Correct usage: '/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credit/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)'");
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

	public static String getSQLApiKey() {
		return Main.sql_api_key;
	}

	public static String getTranslatorApiKey() {
		return Main.translate_api_key;
	}
}
