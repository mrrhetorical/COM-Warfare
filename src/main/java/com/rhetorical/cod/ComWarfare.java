package com.rhetorical.cod;

import com.rhetorical.cod.assignments.AssignmentManager;
import com.rhetorical.cod.files.*;
import com.rhetorical.cod.game.*;
import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.inventories.MatchBrowser;
import com.rhetorical.cod.inventories.ShopManager;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.perks.PerkListener;
import com.rhetorical.cod.perks.PerkManager;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.progression.ProgressionManager;
import com.rhetorical.cod.progression.RankPerks;
import com.rhetorical.cod.sounds.SoundManager;
import com.rhetorical.cod.streaks.KillStreakManager;
import com.rhetorical.cod.util.LegacyActionBar;
import com.rhetorical.cod.util.LegacyTitle;
import com.rhetorical.cod.util.UpdateChecker;
import com.rhetorical.cod.weapons.*;
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
import java.util.List;
import java.util.Objects;

/**
 *  COM-Warfare is a plugin that completely changes Minecraft servers to give its players an experience similar to that of Call of Duty!
 *
 * @author Caleb Brock
 * @version 2.12.15
 * */

public class ComWarfare extends JavaPlugin {

	private static ComWarfare instance;

	/**
	 * @return Returns an instance of the ComWarfare class (assuming it is created).
	 * */
	public static Plugin getPlugin() {
		return getInstance();
	}

	/**
	 * @return Returns an instance of the ComWarfare class as a Plugin (assuming it is created).
	 * @see Plugin
	 * */
	public static ComWarfare getInstance() {
		return instance;
	}

	private String codPrefix = "[COM] ";
	private ConsoleCommandSender cs = Bukkit.getConsoleSender();

	private static String translate_api_key;

	private Object lang;
	private Object translate;
	
	private int minPlayers = 6;
	private int maxPlayers = 12;

	private boolean serverMode = false;

	private double defaultHealth = 20D;

	private ArrayList<RankPerks> serverRanks = new ArrayList<>();

	private Location lobbyLoc;

	private String header = "[COM-Warfare]";

	private boolean hasQA = false;
	private boolean hasCS = false;
	private boolean hasProtocol = false;

	private String reward_highestKD;
	private String reward_highestScore;
	public String reward_maxLevel;
	public String reward_maxPrestige;
	public String reward_maxPrestigeMaxLevel;

	private String lobbyServer;

	public double knifeDamage = 100d;

	private static boolean disabling = false;

	private static boolean legacy = false;

	private Metrics bMetrics;

	final String uid = "%%__USER__%%";
	final String rid = "%%__RESOURCE__%%";
	final String nonce = "%%__NONCE__%%";

	@Override
	public void onLoad() {
		hasQA = Bukkit.getServer().getPluginManager().getPlugin("QualityArmory") != null;
		hasCS = Bukkit.getServer().getPluginManager().getPlugin("CrackShot") != null;
		hasProtocol = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null;
	}

	/**
	 * Sets up the plugin and loads various information handlers such as the killstreak manager, loadout manager, etc.
	 * */
	@Override
	public void onEnable() {

		if (instance != null)
			return;

		instance = this;

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
			v = Integer.parseInt(bukkitVersion.split("\\.")[1]);
		} catch(Exception ignored) {}

		if (v <= 8 ) {
			ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "You are not on the most recent version of Spigot/Bukkit, so COM-Warfare will have some features limited. To ensure the plugin will work as intended, please use version 1.9+!");
			legacy = true;
		}

		DependencyManager dm = new DependencyManager();
		if (!dm.checkDependencies()) {
			if (getPlugin().getConfig().getBoolean("auto-download-dependency")) {
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "One or more dependencies were not found, will attempt to download them.");
				try {
					dm.downloadDependencies();
				} catch (Exception e) {
					ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Could not download dependencies! Make sure that the plugins folder can be written to!");
					ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Not all dependencies for COM-Warfare are installed! The plugin may not work as intended and may throw errors!");
				}
			} else {
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Could not download dependencies! You must set the value for \"auto-download-dependency\" to 'true' in the config to automatically download them!");
				ComWarfare.getConsole().sendMessage("Not all dependencies for COM-Warfare are installed! The plugin likely will not work as intended!");
			}
		} else {
			ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "All dependencies are installed!");
		}

		if (getPlugin().getConfig().getBoolean("check-for-updates")) {
			ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Check for updates is enabled, checking for updates...");
			UpdateChecker updateChecker = new UpdateChecker();
		}

		try {
			if (getPlugin().getConfig().getString("lang").equalsIgnoreCase("none")) {
				lang = com.rhetorical.tpp.McLang.EN;
			} else {
				try {
					lang = com.rhetorical.tpp.McLang.valueOf(getPlugin().getConfig().getString("lang"));
					connectToTranslationService();
				} catch (Exception e) {
					lang = com.rhetorical.tpp.McLang.EN;
					cs.sendMessage(codPrefix + ChatColor.RED + "Could not get the language from the config! Make sure you're using the right two letter abbreviation!");
				}

				if (lang != com.rhetorical.tpp.McLang.EN)
					lang = com.rhetorical.tpp.McLang.EN;
			}
		} catch(Exception|Error ignored) {
			//if mctranslate is not installed, it is no issue
//			ComWarfare.getConsole().sendMessage(codPrefix + ChatColor.RED + "McTranslate++ is not installed.");
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
		SoundFile.setup(getPlugin());

		QualityGun.setup();
		CrackShotGun.setup();

		ProgressionManager.getInstance();
		PerkManager.getInstance();
		LoadoutManager.getInstance();
		ShopManager.getInstance();
		PerkListener.getInstance();
		KillStreakManager.getInstance();
		InventoryManager.getInstance();
		AssignmentManager.getInstance();
		SoundManager.getInstance();

		GameManager.setupOITC();
		GameManager.setupGunGame();

		Bukkit.getServer().getPluginManager().registerEvents(new Listeners(), getPlugin());

		GameManager.loadMaps();

		for (Player p : Bukkit.getOnlinePlayers()) {
			LoadoutManager.getInstance().load(p);
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
			knifeDamage = getPlugin().getConfig().getDouble("knifeDamage");
			lobbyServer = getPlugin().getConfig().getString("lobbyServer");
			if (knifeDamage < 1)
				knifeDamage = 1;
			else if (knifeDamage > 100)
				knifeDamage = 100;
		}

		if (ComVersion.getPurchased()) {
			int i = 0;

			while (getPlugin().getConfig().contains("RankTiers." + i)) {
				String name = getPlugin().getConfig().getString("RankTiers." + i + ".name");
				int killCredits = getPlugin().getConfig().getInt("RankTiers." + i + ".kill.credits");
				double killExperience = getPlugin().getConfig().getDouble("RankTiers." + i + ".kill.xp");
				int levelCredits = getPlugin().getConfig().getInt("RankTiers." + i + ".levelCredits");

				RankPerks rank = new RankPerks(name, killCredits, killExperience, levelCredits);

				ComWarfare.getServerRanks().add(rank);

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
			ComWarfare.getServerRanks().add(rank);
		}

		ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + ChatColor.GREEN + ChatColor.BOLD + "COM-Warfare version " + ChatColor.RESET + ChatColor.WHITE + version + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + " is now up and running!");

		if (serverMode) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				GameManager.findMatch(p);
			}
		}
	}

	/**
	 * Attempts to cleanly shut down all games and save all map change progress.
	 * */
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

	/**
	 * @param s = The permission node to check
	 * @param inGame = Whether not the command can be used in game
	 * @return Returns true if the given command sender has the permission node, the permission node "com.*", or if they're a server operator.
	 * */
	static boolean hasPerm(CommandSender p, String s, boolean inGame) {

		if (p.hasPermission(s) || p.hasPermission("com.*") || p instanceof ConsoleCommandSender || p.isOp()) {
			if (p instanceof Player) {
				if (GameManager.isInMatch((Player) p)) {
					if (!inGame){
						sendMessage(p, ComWarfare.getPrefix() + Lang.NOT_ALLOWED_IN_GAME.getMessage(), getLang());
						return false;
					}
				}
			}
			return true;
		} else {
			sendMessage(p, ComWarfare.getPrefix() + Lang.NO_PERMISSION.getMessage(), getLang());
			return false;
		}
	}

	/**
	 * Interface for ComWarfare#hasPerm(CommandSender, String, boolean)
	 * @see ComWarfare#hasPerm(CommandSender, String, boolean)
	 * @return Returns true if the given command sender has the permission node, the permission node "com.*", or if they're a server operator and aren't in game.
	 * */
	static boolean hasPerm(CommandSender p, String s) {
		return hasPerm(p, s, false);
	}

	/**
	 * @return Returns if the server has QualityArmory loaded on it.
	 * */
	public static boolean isUsingQA() {
		return Bukkit.getServer().getPluginManager().getPlugin("QualityArmory") != null;
	}

	@SuppressWarnings("Duplicates")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!label.equalsIgnoreCase("cod") && !label.equalsIgnoreCase("comr") && !label.equalsIgnoreCase("war") && !label.equalsIgnoreCase("com"))
			return false;

		String cColor = "" + ChatColor.YELLOW;
		String dColor = "" + ChatColor.WHITE;

		if (args.length == 0) {

			if (!hasPerm(sender, "com.help"))
				return true;

			sendMessage(sender, "-===" + ChatColor.GOLD + "COM-Warfare Help" + ChatColor.RESET + "===-");
			sendMessage(sender, ChatColor.WHITE + "[Page " + ChatColor.GREEN	+ "1" + ChatColor.WHITE + " of 5]");

			sendMessage(sender, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
			sendMessage(sender, cColor + "/cod menu | " + dColor + "Opens the cod menu.");
			sendMessage(sender, cColor + "/cod join | " + dColor + "Joins a match via matchmaker.");
			sendMessage(sender, cColor + "/cod browser | " + dColor + "Opens the match browser.");
			sendMessage(sender, cColor + "/cod leave | " + dColor + "Leaves the current game.");
			sendMessage(sender, cColor + "/cod lobby | " + dColor + "Teleports you to the lobby.");
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
						sendMessage(sender, ComWarfare.getPrefix() + Lang.NOT_PROPER_PAGE.getMessage(), lang);
						return true;
					}

					if (!(page > 0 && page <= 5)) {
						sendMessage(sender, ComWarfare.getPrefix() + Lang.NOT_PROPER_PAGE.getMessage(), lang);
						return true;
					}

					//FIXME: Left off here converting to ChatColor!

					sendMessage(sender, "-===" + ChatColor.GOLD + "COM-Warfare Help" + ChatColor.RESET + "===-", lang);
					sendMessage(sender, ChatColor.WHITE + "[Page " + ChatColor.GREEN	+ page + ChatColor.WHITE + " of 5]", lang);

					switch (page) {
						case 1:
							sendMessage(sender, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
							sendMessage(sender, cColor + "/cod menu | " + dColor + "Opens the cod menu.");
							sendMessage(sender, cColor + "/cod join | " + dColor + "Joins a match via matchmaker.");
							sendMessage(sender, cColor + "/cod browser | " + dColor + "Opens the match browser.");
							sendMessage(sender, cColor + "/cod leave | " + dColor + "Leaves the current game.");
							sendMessage(sender, cColor + "/cod lobby | " + dColor + "Teleports you to the lobby.");
							break;
						case 2:
							sendMessage(sender, cColor + "/cod shop | " + dColor + "Opens the shop.");
							sendMessage(sender, cColor + "/cod balance | " + dColor + "Shows player's credit balance.");
							sendMessage(sender, cColor + "/cod class | " + dColor + "Opens class selection menu.");
							sendMessage(sender, cColor + "/cod listMaps | " + dColor + "Lists all available maps.");
							sendMessage(sender, cColor + "/cod start | " + dColor + "Auto-starts the match if the lobby timer has started.");
							break;
						case 3:
							sendMessage(sender, cColor + "/cod boot | " + dColor + "Forces all players in all matches to leave.");
							sendMessage(sender, cColor + "/cod changeMap/changeMode [map name/game mode] | " + dColor + "Changes the current map/mode.");
							sendMessage(sender, cColor + "/cod setLevel [player] (level) | " + dColor + "Sets the player's level.");
							sendMessage(sender, cColor + "/cod credits [give/set] [player] (amt) | " + dColor + "Gives credits.");
							sendMessage(sender, cColor + "/cod createGun (args) | " + dColor + "Creates a gun. Type command to see a full list of arguments.");
							break;
						case 4:
							sendMessage(sender, cColor + "/cod createWeapon (args) | " + dColor + "Creates a grenade. Type to see a full list of arguments.");
							sendMessage(sender, cColor + "/cod createMap [name] | " + dColor + "Creates a map.");
							sendMessage(sender, cColor + "/cod removeMap [name] | " + dColor + "Removes a map.");
							sendMessage(sender, cColor + "/cod set [lobby/spawn/flag] | " + dColor + "Set spawns, flags, and lobby location.");
							sendMessage(sender, cColor + "/cod add [oitc/gun] (gun name) | " + dColor + "Sets gun for OITC or adds gun to Gun Game.");
							break;
						case 5:
							sendMessage(sender, cColor + "/cod blacklist (map) (mode) | " + dColor + "Prevents a mode from being played on the map.");
							sendMessage(sender, cColor + "/cod version | " + dColor + "Displays the running version of COM-Warfare.");
							break;
						default:
							break;
					}
				} else {
					sendMessage(sender, "-===" + ChatColor.GOLD + "COM-Warfare Help" + ChatColor.RESET + "===-");
					sendMessage(sender, ChatColor.WHITE + "[Page " + ChatColor.GREEN	+ "1" + ChatColor.WHITE + " of 5]");

					sendMessage(sender, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
					sendMessage(sender, cColor + "/cod menu | " + dColor + "Opens the cod menu.");
					sendMessage(sender, cColor + "/cod join | " + dColor + "Joins a match via matchmaker.");
					sendMessage(sender, cColor + "/cod browser | " + dColor + "Opens the match browser.");
					sendMessage(sender, cColor + "/cod leave | " + dColor + "Leaves the current game.");
					sendMessage(sender, cColor + "/cod lobby | " + dColor + "Teleports you to the lobby.");

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
					LoadoutManager.getInstance().load(p);
					ProgressionManager.getInstance().update(p);
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

				return true;
			} else if (args[0].equalsIgnoreCase("version")) {
				if (!hasPerm(sender, "com.version", true))
					return true;

				sendMessage(sender, String.format("%sYou are running COM-Warfare version: %s%s", ChatColor.GREEN, ChatColor.YELLOW, getPlugin().getDescription().getVersion()));

			} else if (args[0].equalsIgnoreCase("browser")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.join", true))
					return true;

				Player p = (Player) sender;

				p.openInventory(MatchBrowser.getInstance().getBrowser());

			} else if (args[0].equalsIgnoreCase("menu")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, Lang.MUST_BE_PLAYER.getMessage(), lang);
					return true;
				}

				if (!hasPerm(sender, "com.join", false))
					return true;

				Player p = (Player) sender;
				p.openInventory(InventoryManager.getInstance().mainInventory);
				p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
			} else if (args[0].equalsIgnoreCase("listMaps")) {

				if (!hasPerm(sender, "com.map.list", true))
					return true;

				sendMessage(sender, ComWarfare.getPrefix() + Lang.MAP_LIST_HEADER.getMessage(), lang);
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
							entry = entry.replace("{map-status}", ChatColor.RED + "UNFINISHED");
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
							sendMessage(sender, ComWarfare.getPrefix() + Lang.CREATE_MAP_ALREADY_EXISTS.getMessage(), lang);
							return true;
						}
					}

					newMap = new CodMap(mapName);

					GameManager.getAddedMaps().add(newMap);
					String msg = Lang.CREATE_MAP_SUCCESS.getMessage();
					msg = msg.replace("{map-name}", mapName);
					sendMessage(sender, ComWarfare.getPrefix() + msg, lang);
					newMap.setEnable();
					return true;
				} else {
					String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createMap (name)");
					sendMessage(sender, ComWarfare.getPrefix() + msg);
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

							sendMessage(sender, ComWarfare.getPrefix() + Lang.REMOVE_MAP_SUCCESS.getMessage(), lang);
							return true;
						}
					}

					sendMessage(sender, ComWarfare.getPrefix() + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage(), lang);
					return true;

				} else {
					String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod removeMap (name)");
					sendMessage(sender, ComWarfare.getPrefix() + msg);
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
					sendMessage(p, ComWarfare.getPrefix() + msg);
					return true;
				}

				if (args[1].equalsIgnoreCase("lobby")) {

					Location lobby = p.getLocation();
					getPlugin().getConfig().set("com.lobby", lobby);
					lobbyLoc = (Location) getPlugin().getConfig().get("com.lobby");
					getPlugin().saveConfig();
					getPlugin().reloadConfig();
					sendMessage(p, ComWarfare.getPrefix() + Lang.SET_LOBBY_SUCCESS.getMessage(), lang);
					return true;
				} else if (args[1].equalsIgnoreCase("spawn")) {

					if (!hasPerm(p, "com.map.addSpawn"))
						return true;

					if (args.length < 4) {
						String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set spawn (map name) (team)");
						sendMessage(p, ComWarfare.getPrefix() + msg);
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
						sendMessage(p, ComWarfare.getPrefix() + Lang.MAP_NOT_EXISTS_WITH_NAME, lang);
						return true;
					}

					String spawnTeam = args[3];
					String team;
					switch (spawnTeam.toUpperCase()) {
					case "RED":
						map.addRedSpawn(p.getLocation());
						team = ChatColor.RED + "RED";
						break;
					case "BLUE":
						map.addblueSpawn(p.getLocation());
						team = ChatColor.BLUE + "BLUE";
						break;
					case "PINK":
						map.addPinkSpawn(p.getLocation());
						team = ChatColor.LIGHT_PURPLE + "PINK";
						break;
					default:
						sendMessage(p, ComWarfare.getPrefix() + Lang.TEAM_NOT_EXISTS_WITH_NAME.getMessage(), lang);
						return true;
					}

					String msg = Lang.SET_SPAWN_SUCCESS.getMessage().replace("{team}", team).replace("{map-name}", map.getName());
					sendMessage(p, ComWarfare.getPrefix() + msg);

				} else if (args[1].equalsIgnoreCase("flag")) {

					if (!hasPerm(p, "com.map.modify"))
						return true;

					if (args.length < 4) {
						String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set flag (map name) (hardpoint/red/blue/a/b/c)");
						sendMessage(p, ComWarfare.getPrefix() + msg);
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
						sendMessage(p, ComWarfare.getPrefix() + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage(), lang);
						return true;
					}

					String arg = args[3];

					String team = null;
					String flag = null;

					switch(arg.toLowerCase()) {
						case "hardpoint":
							map.addHardPointFlag(p.getLocation());
							flag = ChatColor.YELLOW + "Hardpoint";
							break;
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
							String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set flag (map name) (hardpoint/red/blue/a/b/c)");
							sendMessage(p, ComWarfare.getPrefix() + msg);
							return true;
					}

					if (team == null) {
						sendMessage(p, ComWarfare.getPrefix() + Lang.SET_FLAG_DOM_SUCCESS.getMessage().replace("{flag}" + ChatColor.RESET, flag));
					} else {
						sendMessage(p, ComWarfare.getPrefix() + Lang.SET_FLAG_CTF_SUCCESS.getMessage().replace("{team}" + ChatColor.RESET, team));
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
					sendMessage(p, ComWarfare.getPrefix() + Lang.LOBBY_NOT_EXISTS.getMessage(), lang);
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
				if (args.length < 3) {
					if (hasPerm(sender, "com.credits.give"))
						sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits [give/set] {player} (amount)"));
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
						sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits give {player} (amount)"));
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);
					sendMessage(sender, ComWarfare.getPrefix() + Lang.GIVE_BALANCE_COMMAND.getMessage().replace("{player}", playerName).replace("{amount}", amount + "").replace("{total}", CreditManager.getCredits(playerName) + ""), lang);
					return true;
				} else if (args[1].equalsIgnoreCase("set")) {

					if (!hasPerm(sender, "com.credits.set"))
						return true;

					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits set {name} [amount]"));
						return true;
					}

					CreditManager.setCredits(playerName, amount);
					sendMessage(sender, ComWarfare.getPrefix() + Lang.SET_BALANCE_COMMAND.getMessage().replace("{player}", playerName).replace("{amount}", amount + ""), lang);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("createGun")) {

				if (!hasPerm(sender, "com.createGun"))
					return true;

				if (args.length >= 9) {
					createGun(sender, args);
					return true;
				} else {
					sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credits/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)"));
					return true;
				}
			} else if ((args[0].equalsIgnoreCase("createWeapon") || args[0].equalsIgnoreCase("createGrenade"))) {

				if (!hasPerm(sender, "com.createWeapon"))
					return true;

				if (args.length >= 7) {
					createWeapon(sender, args);
					return true;
				} else {
					sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credits/both) (Grenade Material) (Level Unlock) (Cost)"));
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
						sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + Lang.COULD_NOT_FIND_GAME_PLAYER_IN, ComWarfare.getLang());
					}
					return true;
				} else {
					sendMessage(p, ComWarfare.getPrefix() + Lang.MUST_BE_IN_GAME.getMessage(), lang);
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
				InventoryManager.getInstance().openSelectClassInventory(p);
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
				p.openInventory(InventoryManager.getInstance().mainShopInventory);
				return true;
			} else if (args[0].equalsIgnoreCase("boot")) {

				if (!hasPerm(sender, "com.bootAll", true))
					return true;

				boolean result = bootPlayers();
				if (result) {
					sender.sendMessage(ComWarfare.getPrefix() + Lang.PLAYERS_BOOTED_SUCCESS.getMessage());
				} else {
					sender.sendMessage(ComWarfare.getPrefix() + Lang.PLAYER_BOOTED_FAILURE.getMessage());
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
				CodWeapon weapon = ShopManager.getInstance().getWeaponForName(gunName);

				if (!(weapon instanceof CodGun)) {
					sendMessage(sender, Lang.WEAPON_NOT_FOUND_WITH_NAME.getMessage().replace("{gun-name}", gunName));
					return true;
				}

				if (type.equalsIgnoreCase("oitc")) {
					getConfig().set("OITC_Gun", weapon.getName());
					saveConfig();
					reloadConfig();
					GameManager.setupOITC();
					sendMessage(sender, ComWarfare.getPrefix() + Lang.OITC_GUN_SET_SUCCESS.getMessage().replace("{gun-name}", gunName));
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
					sendMessage(sender, ComWarfare.getPrefix() + Lang.GUN_PROGRESSION_ADDED_SUCCESS.getMessage());
					return true;
				}
				sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod add [oitc/gun] (gun name)"));
				return true;
			} else if (args[0].equalsIgnoreCase("changeMap")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ComWarfare.getPrefix() + Lang.MUST_BE_PLAYER.getMessage(), lang);
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

				GameInstance game = GameManager.getMatchWhichContains(p);
				if (game == null)
					return true;

				if (game.getState() != GameState.WAITING && game.getState() != GameState.STARTING) {
					sendMessage(p, codPrefix + Lang.MUST_NOT_BE_IN_GAME.getMessage());
					return true;
				}


				CodMap map = GameManager.getMapForName(args[1]);

				if (map == null) {
					sendMessage(p, codPrefix + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				GameManager.changeMap(game, map);
				sendMessage(p, codPrefix + Lang.MAP_CHANGE_SUCCESS.getMessage().replace("{map-name}", map.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("changeMode")) {
				if (!(sender instanceof Player)) {
					sendMessage(cs, ComWarfare.getPrefix() + Lang.MUST_BE_PLAYER.getMessage(), lang);
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

				GameInstance game = GameManager.getMatchWhichContains(p);
				if (game == null)
					return true;

				if (game.getState() != GameState.WAITING && game.getState() != GameState.STARTING) {
					sendMessage(p, codPrefix + Lang.MUST_NOT_BE_IN_GAME.getMessage());
					return true;
				}

				Gamemode mode;

				try {
					mode = Gamemode.valueOf(args[1].toUpperCase());
				} catch(Exception e) {
					sendMessage(p, codPrefix + Lang.GAME_MODE_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				if (!game.getMap().getAvailableGamemodes().contains(mode)) {
					sendMessage(p, codPrefix + Lang.GAME_MODE_NOT_SET_UP_ON_MAP.getMessage());
					return true;
				}

				Objects.requireNonNull(GameManager.getMatchWhichContains(p)).changeGamemode(mode);
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

				sendMessage(sender, ComWarfare.getPrefix() + Lang.BLACKLIST_SUCCESS.getMessage().replace("{mode}", mode.toString()).replace("{map-name}", map.getName()));
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
					if (level > ProgressionManager.getInstance().maxLevel)
						throw new NumberFormatException();
				} catch(NumberFormatException e) {
					sendMessage(sender, codPrefix + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod setLevel (player) (level)"));
					return true;
				}

				ProgressionManager.getInstance().setLevel(player, level, true);
				ProgressionManager.getInstance().saveData(player);
				sendMessage(sender, Lang.SET_LEVEL_SUCCESS.getMessage().replace("{player}", player.getDisplayName()).replace("{level}", level + ""));
				return true;
			} else {
				sender.sendMessage(ComWarfare.getPrefix() + Lang.UNKNOWN_COMMAND.getMessage());
				return true;
			}
		}

		return true;
	}


	/**
	 * Attempts to load McTranslate++'s API.
	 * */
	private void connectToTranslationService() {
		try {
			translate = new McTranslate(ComWarfare.getPlugin(), ComWarfare.translate_api_key);
		} catch(Exception e) {
			ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "Could not start McTranslate++ API!");
		}
	}

	/**
	 * Removes all players from all games cleanly.
	 * @return Returns if the operation was successful.
	 * */
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
					ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.PLAYER_LEAVE_GAME.getMessage(), ComWarfare.getLang());
				}
			}
		}
		return true;
	}

	/**
	 * Creates the weapon from command given the command arguments.
	 * @param args = The arguments passed from the createWeapon command.
	 * */
	private void createWeapon(CommandSender p, String[] args) {

		String command = "/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credits/both) (Grenade Material) (Level Unlock) (amount) (Cost)";
		if (args.length == 8) {
			String name = args[1];
			WeaponType grenadeType;
			UnlockType unlockType;

			try {
				grenadeType = WeaponType.valueOf(args[2].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.WEAPON_TYPE_NOT_EXISTS.getMessage(), lang);
				return;
			}
			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.UNLOCK_TYPE_NOT_EXISTS.getMessage(), lang);
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
				sendMessage(p, ComWarfare.getPrefix() + Lang.MATERIAL_NOT_EXISTS.getMessage().replace("{name}", args[4].toUpperCase()), lang);
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[5]);
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command), lang);
				return;
			}

			int amount;

			try {
				amount = Integer.parseInt(args[6]);
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command), lang);
				return;
			}

			if (amount < 1) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command), lang);
				return;
			}

			grenade.setAmount(amount);

			int cost;

			try {
				cost = Integer.parseInt(args[7]);
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command), lang);
				return;
			}

			CodWeapon grenadeWeapon = new CodWeapon(name, grenadeType, unlockType, grenade, levelUnlock, true);

			grenadeWeapon.setCreditUnlock(cost);

			grenadeWeapon.save();

			sendMessage(p, codPrefix + Lang.WEAPON_CREATED_SUCCESS.getMessage().replace("{weapon-name}", name).replace("{weapon-type}", grenadeType.toString()), lang);

			ShopManager sm = ShopManager.getInstance();

			switch (grenadeType) {
				case LETHAL:
					ArrayList<CodWeapon> lethalList = sm.getLethalWeapons();
					lethalList.add(grenadeWeapon);
					sm.setLethalWeapons(lethalList);
					break;
				case TACTICAL:
					ArrayList<CodWeapon> tacList = sm.getTacticalWeapons();
					tacList.add(grenadeWeapon);
					sm.setTacticalWeapons(tacList);
					break;
				default:
					break;
			}

		} else {
			sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command), lang);
		}
	}

	/**
	 * Creates the weapon from command given the command arguments.
	 * @param args = The arguments passed from the createGun command.
	 * */
	private void createGun(CommandSender p, String[] args) {
		String command = "/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credits/both) (Ammo Amount) (Gun Material[:data]) (Ammo Material[:data]) (Level Unlock) (Cost)";
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
				sendMessage(p, ComWarfare.getPrefix() + Lang.GUN_TYPE_NOT_EXISTS.getMessage(), lang);
				return;
			}

			UnlockType unlockType;

			try {
				unlockType = UnlockType.valueOf(args[3].toUpperCase());
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.UNLOCK_TYPE_NOT_EXISTS.getMessage(), lang);
				return;
			}

			int ammoAmount;

			try {
				ammoAmount = Integer.parseInt(args[4]);
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command) , lang);
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
				sendMessage(p, ComWarfare.getPrefix() + Lang.MATERIAL_NOT_EXISTS.getMessage().replace("{name}", args[5].toUpperCase()), lang);
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
				sendMessage(p, ComWarfare.getPrefix() + Lang.MATERIAL_NOT_EXISTS.getMessage().replace("{name}", args[6].toUpperCase()), lang);
				return;
			}

			int levelUnlock;

			try {
				levelUnlock = Integer.parseInt(args[7]);
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command) , lang);
				return;
			}

			int cost;

			try {
				cost = Integer.parseInt(args[8]);
			} catch (Exception e) {
				sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command) , lang);
				return;
			}

			CodGun gun = new CodGun(name, gunType, unlockType, ammoAmount, ammoItem, gunItem, levelUnlock, true);

			gun.setCreditUnlock(cost);

			gun.save();

			sendMessage(p, codPrefix + Lang.GUN_CREATED_SUCCESS.getMessage().replace("{gun-name}", name).replace("{gun-type}", gunType.toString()), lang);

			ShopManager sm = ShopManager.getInstance();

			switch (gunType) {
				case Primary:
					ArrayList<CodGun> pList = sm.getPrimaryGuns();
					pList.add(gun);
					sm.setPrimaryGuns(pList);
					break;
				case Secondary:
					ArrayList<CodGun> sList = sm.getSecondaryGuns();
					sList.add(gun);
					sm.setSecondaryGuns(sList);
					break;
				default:
					break;
			}
		} else {
			sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", command) , lang);
		}

	}

	/**
	 * @return Gets the RankPerk the player belongs to.
	 * @see RankPerks
	 * */
	public static RankPerks getRank(Player p) {
		for (RankPerks perk : ComWarfare.getServerRanks()) {
			if (p.hasPermission("com." + perk.getName())) {
				return perk;
			}
		}

		for (RankPerks perk : ComWarfare.getServerRanks()) {
			if (perk.getName().equals("default")) {
				return perk;
			}
		}

		return new RankPerks("default", 1, 100D, 10);
	}

	/**
	 * Sends a message to the target without translation.
	 * */
	private static void sendMessage(CommandSender target, String message) {
		target.sendMessage(message);
	}

	/**
	 * Sends a message to the target and attempts to translate given the target lang with McTranslate++.
	 * @param targetLang = The target language to attempt to translate the message to.
	 * */
	public static void sendMessage(CommandSender target, String message, Object targetLang) {

		try {
			if (targetLang == com.rhetorical.tpp.McLang.EN || !ComVersion.getPurchased()) {
				sendMessage(target, message);
				return;
			}

			String translatedMessage;

			try {
				translatedMessage = ((McTranslate)getInstance().getTranslate()).translateRuntime(message, com.rhetorical.tpp.McLang.EN, (com.rhetorical.tpp.McLang) targetLang);
			} catch (Exception e) {
				sendMessage(target, message);
				return;
			}

			sendMessage(target, translatedMessage);
		} catch (Exception|Error classException) {
			target.sendMessage(message);
		}
	}

	/**
	 * Sends title to the player. (interface)
	 * @see ComWarfare#sendTitle(Player, String, String, ChatColor, int...)
	 * */
	public static void sendTitle(Player p, String title, String subtitle, int... timings) {
		sendTitle(p, title, subtitle, ChatColor.YELLOW, timings);
	}

	/**
	 * Sends a title to the player given the Color, fade in, out, and stay times.
	 * @param timings = Fade in, stay, and fade out times.
	 * */
	public static void sendTitle(Player p, String title, String subtitle, ChatColor legacyColor, int... timings) {

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
		} catch(NoSuchMethodError|Exception e) {
			LegacyTitle.sendTitle(p, title, start, stop, linger, legacyColor);
			LegacyTitle.sendSubTitle(p, title, start, stop, linger, legacyColor);
		}
//		p.sendTitle(title, subtitle, 10, 0, 10);
	}

	/**
	 * Sends an action bar with the given message to the target player.
	 * */
	public static void sendActionBar(Player p, String message) {
		try {
			p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
		} catch (NoSuchMethodError|Exception e) {
			if (e instanceof NoSuchMethodError) {
				LegacyActionBar.sendActionBarMessage(p, message);
//				p.sendMessage(message);
			} else {
				Bukkit.getLogger().severe("Error when attempting to send action bar in COM-Warfare:");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Opens the main menu in COM-Warfare for the target player.
	 * */
	public static void openMainMenu(Player p) {
		p.openInventory(InventoryManager.getInstance().mainInventory);
		try {
			p.playSound(p.getLocation(), Sound.valueOf("BLOCK_CHEST_OPEN"), 4f, 1f);
		}catch(Exception e) {
			//fail silently and play legacy sound
			p.playSound(p.getLocation(), Sound.valueOf("CHEST_OPEN"), 4f, 1f);
		}
	}

	/**
	 * @return If the server is running on Bukkit 1.8.X or earlier builds.
	 * */
	public static boolean isLegacy() { return legacy; }

	/**
	 * @return Returns if the server has QualityArmory installed.
	 * */
	public static boolean hasQualityArms() {
		return getInstance().hasQA;
	}

	/**
	 * @return Returns if the server has CrackShot installed.
	 * */
	public static boolean hasCrackShot() {
		return getInstance().hasCS;
	}

	/**
	 * @return Returns if the server has ProtocolLib installed.
	 * */
	public static boolean hasProtocolLib() {
		return getInstance().hasProtocol;
	}

	public static boolean isDisabling() {
		return disabling;
	}

	/**
	 * @return Returns the lobby server in a bungee configuration as a String.
	 * */
	public String getLobbyServer() {
		return lobbyServer;
	}

	public static String getPrefix() {
		return getInstance().codPrefix;
	}

	/**
	 * @return The McTranslate language of choice for translation.
	 * */
	public static Object getLang() {
		return getInstance().lang;
	}

	public static ConsoleCommandSender getConsole() {
		return getInstance().cs;
	}

	private Object getTranslate() {
		return getInstance().translate;
	}

	private static List<RankPerks> getServerRanks() {
		return getInstance().serverRanks;
	}

	public static double getDefaultHealth() {
		return getInstance().defaultHealth;
	}

	public static Location getLobbyLocation() {
		return getInstance().lobbyLoc;
	}

	public static int getMinPlayers() {
		return getInstance().minPlayers;
	}

	public static int getMaxPlayers() {
		return getInstance().maxPlayers;
	}

	public static String getRewardHighestScore() {
		return getInstance().reward_highestScore;
	}

	public static String getRewardHighestKD() {
		return getInstance().reward_highestKD;
	}

	public static String getRewardMaxLevel() {
		return getInstance().reward_maxLevel;
	}

	public static String getRewardMaxPrestige() {
		return getInstance().reward_maxPrestige;
	}

	public static String getRewardMaxLevelMaxPrestige() {
		return getInstance().reward_maxPrestigeMaxLevel;
	}

	public static String getHeader() {
		return getInstance().header;
	}

	public static boolean isServerMode() {
		return getInstance().serverMode;
	}
}
