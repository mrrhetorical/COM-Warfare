package com.rhetorical.cod;

import com.rhetorical.cod.assignments.AssignmentManager;
import com.rhetorical.cod.files.*;
import com.rhetorical.cod.game.CodMap;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.inventories.InventoryManager;
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
import com.rhetorical.cod.util.*;
import com.rhetorical.cod.weapons.*;
import com.rhetorical.tpp.api.McTranslate;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
	private boolean kickAfterMatch = false;

	private boolean mapVoting = true;


	private double defaultHealth = 20D;

	private List<RankPerks> serverRanks = new ArrayList<>();
	private List<ItemBridgePrefix> itemBridgePrefixes = new ArrayList<>();

	Location lobbyLoc;

	private String header = "[COM-Warfare]";

	private boolean hasQA = false;
	private boolean hasCS = false;
	private boolean hasProtocol = false;
	private static boolean hasPAPI = false;
	private static boolean hasItemBridge = false;
	private static boolean hasTranslate = false;

	private String reward_highestKD =  "";
	private String reward_highestScore = "";
	public String reward_maxLevel = "";
	public String reward_maxPrestige = "";
	public String reward_maxPrestigeMaxLevel = "";

	private String lobbyServer = "";

	public double knifeDamage = 100d;

	private static boolean disabling = false;

	private static boolean legacy = false;
	private static boolean canUseCustomModelData = false;

	private static boolean debug = true;

	private static boolean spawnProtection = true;

	private static boolean killFeedShowAll = true;
	private static boolean killFeedUseBossBar = true;

	private static int spawnProtectionDuration = 3;

	private Metrics bMetrics;

	final String uid = "%%__USER__%%";
	final String rid = "%%__RESOURCE__%%";
	final String nonce = "%%__NONCE__%%";

	/**
	 * Sets up the plugin and loads various information handlers such as the killstreak manager, loadout manager, etc.
	 * */
	@Override
	public void onEnable() {

		if (instance != null)
			return;

		instance = this;
		
		getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		getCommand("cod").setExecutor(new CodCommand());
		getCommand("cod").setTabCompleter(new CodTabCompleter());

		hasQA = Bukkit.getServer().getPluginManager().getPlugin("QualityArmory") != null;
		hasCS = Bukkit.getServer().getPluginManager().getPlugin("CrackShot") != null;
		hasProtocol = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib") != null;
		hasPAPI = Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
		hasItemBridge = Bukkit.getServer().getPluginManager().getPlugin("ItemBridge") != null;
		hasTranslate = Bukkit.getServer().getPluginManager().getPlugin("McTranslate++") != null;

		ComVersion.setup(true);

		getPlugin().saveDefaultConfig();
		getPlugin().reloadConfig();

		if (ComVersion.getPurchased()) {
			codPrefix = getPlugin().getConfig().getString("prefix").replace("&", "\u00A7") + " ";

			if (ComWarfare.getPrefix().equalsIgnoreCase("")) {
				codPrefix = "[COD] ";
			}
		}

		bMetrics = new Metrics(this);

		String bukkitVersion = Bukkit.getServer().getBukkitVersion();

		int v = 8;

		try {
			v = Integer.parseInt(bukkitVersion.split("\\.")[1]);
		} catch(Exception ignored) {}

		if (v <= 8) {
			ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "You are not on the most recent version of Spigot/Bukkit, so COM-Warfare will have a limited set of features. To ensure the plugin will work as intended, please use the latest version of Spigot!");
			legacy = true;
		}

		if (v >= 14) {
			canUseCustomModelData = true;
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
			new UpdateChecker();
		}


		//check if McTranslate++ is installed and language is set
		if (hasTranslate) {
			try {
				if (getPlugin().getConfig().getString("lang", "none").equalsIgnoreCase("none")) {
					lang = com.rhetorical.tpp.McLang.EN;
				} else {
					try {
						lang = com.rhetorical.tpp.McLang.valueOf(getPlugin().getConfig().getString("lang"));
						connectToTranslationService();
					} catch (Exception e) {
						lang = com.rhetorical.tpp.McLang.EN;
						cs.sendMessage(ComWarfare.getPrefix() + ChatColor.RED + "Could not get the language from the config! Make sure you're using the right two letter abbreviation!");
					}

					if (lang != com.rhetorical.tpp.McLang.EN)
						lang = com.rhetorical.tpp.McLang.EN;
				}
			} catch (Exception | Error ignored) {}
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

		if (hasQualityArms())
			QualityGun.setup();

		if (hasCrackShot())
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
			header = getPlugin().getConfig().getString("Scoreboard.Header", "[COM-Warfare]");
			minPlayers = getPlugin().getConfig().getInt("players.min", 2);
			maxPlayers = getPlugin().getConfig().getInt("players.max", 12);
			serverMode = getPlugin().getConfig().getBoolean("serverMode", false);
			kickAfterMatch = getPlugin().getConfig().getBoolean("autoKickOnMatchEnd", false);
			mapVoting = getPlugin().getConfig().getBoolean("mapVoting", true);
			defaultHealth = getPlugin().getConfig().getDouble("defaultHealth", 20d);
			translate_api_key = getPlugin().getConfig().getString("translate.api_key", "none");
			reward_highestKD = getPlugin().getConfig().getString("Rewards.Highest_KD", "");
			reward_highestScore = getPlugin().getConfig().getString("Rewards.Highest_Score", "");
			reward_maxLevel = getPlugin().getConfig().getString("Rewards.Max_Level", "");
			reward_maxPrestige = getPlugin().getConfig().getString("Rewards.Max_Prestige", "");
			reward_maxPrestigeMaxLevel = getPlugin().getConfig().getString("Rewards.Max_Prestige_Max_Level", "");
			knifeDamage = getPlugin().getConfig().getDouble("knifeDamage", 100d);
			lobbyServer = getPlugin().getConfig().getString("lobbyServer", "none");
			spawnProtection = getPlugin().getConfig().getBoolean("spawnProtection.enabled",  false);
			spawnProtectionDuration = getPlugin().getConfig().getInt("spawnProtection.duration", 3);
			killFeedShowAll = getPlugin().getConfig().getBoolean("killfeed.showForAll", true);
			killFeedUseBossBar = getPlugin().getConfig().getBoolean("killfeed.useBossBar", true);
			if (knifeDamage < 1)
				knifeDamage = 1;
			else if (knifeDamage > 100)
				knifeDamage = 100;

			InventoryPositions.primary = getPlugin().getConfig().getInt("inventory.inGame.primary", 1);
			InventoryPositions.secondary = getPlugin().getConfig().getInt("inventory.inGame.secondary", 2);
			InventoryPositions.knife = getPlugin().getConfig().getInt("inventory.inGame.knife", 0);
			InventoryPositions.lethal = getPlugin().getConfig().getInt("inventory.inGame.lethal", 3);
			InventoryPositions.tactical = getPlugin().getConfig().getInt("inventory.inGame.tactical", 4);
			InventoryPositions.primaryAmmo = getPlugin().getConfig().getInt("inventory.inGame.primaryAmmo", 28);
			InventoryPositions.secondaryAmmo = getPlugin().getConfig().getInt("inventory.inGame.secondaryAmmo", 29);
			InventoryPositions.compass = getPlugin().getConfig().getInt("inventory.inGame.compass", 8);
			InventoryPositions.selectClass = getPlugin().getConfig().getInt("inventory.inGame.selectClass", 32);
			InventoryPositions.leaveGame = getPlugin().getConfig().getInt("inventory.inGame.leaveGame", 35);
			InventoryPositions.gunGameAmmo = getPlugin().getConfig().getInt("inventory.inGame.gunGameAmmo", 8);

			InventoryPositions.menu = getPlugin().getConfig().getInt("inventory.lobby.menu", 0);
			InventoryPositions.leaveLobby = getPlugin().getConfig().getInt("inventory.lobby.leaveLobby", 8);


			if (getPlugin().getConfig().getBoolean("itemBridge.enabled", false)) {
				ConfigurationSection prefixSection = getConfig().getConfigurationSection("itemBridge.prefix");
				if (prefixSection != null)
					for (String key : prefixSection.getKeys(false)) {
						ItemBridgePrefix prefix = new ItemBridgePrefix(key);
						getItemBridgePrefixes().add(prefix);
					}
			}
		}

		spawnProtectionDuration = spawnProtectionDuration >= 1 ? spawnProtectionDuration : 1;

		debug = getPlugin().getConfig().getBoolean("debug", false);


		RankPerks defaultRank = new RankPerks("default", 1, 100, 0);

		if (ComVersion.getPurchased()) {
			ConfigurationSection section = getPlugin().getConfig().getConfigurationSection("RankTiers");
			if (section != null) {
				Set<String> keySet = section.getKeys(false);

				for (String key : keySet) {
					int killCredits = getPlugin().getConfig().getInt("RankTiers." + key + ".kill.credits", 0);
					double killExperience = getPlugin().getConfig().getDouble("RankTiers." + key + ".kill.xp", 0);
					int levelCredits = getPlugin().getConfig().getInt("RankTiers." + key + ".levelCredits", 0);

					if (!key.equalsIgnoreCase("default")) {
						RankPerks rank = new RankPerks(key, killCredits, killExperience, levelCredits);
						ComWarfare.getServerRanks().add(rank);
					} else {
						defaultRank.setKillCredits(killCredits);
						defaultRank.setKillExperience(killExperience);
						defaultRank.setLevelCredits(levelCredits);
					}
				}
			}
		}

		ComWarfare.getServerRanks().add(defaultRank);

		ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + ChatColor.GREEN + ChatColor.BOLD + "COM-Warfare version " + ChatColor.RESET + ChatColor.WHITE + version + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + " is now up and running!");

		if (serverMode) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				GameManager.findMatch(p);
			}
		}
    
		if (hasPAPI) new PAPI(this).register();
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
	 * @param s The permission node to check
	 * @param inGame Whether not the command can be used in game
	 * @return Returns true if the given command sender has the permission node, the permission node "com.*", or if they're a server operator.
	 * */
	public static boolean hasPerm(CommandSender p, String s, boolean inGame) {

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
	public static boolean hasPerm(CommandSender p, String s) {
		return hasPerm(p, s, false);
	}

	/**
	 * @return Returns if the server has QualityArmory loaded on it.
	 * */
	public static boolean isUsingQA() {
		return Bukkit.getServer().getPluginManager().getPlugin("QualityArmory") != null;
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
	boolean bootPlayers() {
		GameInstance[] runningGames = new GameInstance[GameManager.getRunningGames().size()];

		for (int k = 0; k < runningGames.length; k++) {
			runningGames[k] = GameManager.getRunningGames().get(k);
		}

		for (GameInstance i : runningGames) {
			GameManager.removeInstance(i);
		}
		return true;
	}

	/**
	 * Creates the weapon from command given the command arguments.
	 * @param args = The arguments passed from the createWeapon command.
	 * */
	void createWeapon(CommandSender p, String[] args) {

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

			sendMessage(p, Lang.WEAPON_CREATED_SUCCESS.getMessage().replace("{weapon-name}", name).replace("{weapon-type}", grenadeType.toString()), lang);

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
	void createGun(CommandSender p, String[] args) {
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

			sendMessage(p, Lang.GUN_CREATED_SUCCESS.getMessage().replace("{gun-name}", name).replace("{gun-type}", gunType.toString()), lang);

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
	public static void sendMessage(CommandSender target, String message) {
		if (target instanceof Player && hasPAPI) {
			message = PlaceholderAPI.setPlaceholders((Player) target, message);
		}
		target.sendMessage(message);
	}

	/**
	 * Sends a message to the target and attempts to translate given the target lang with McTranslate++.
	 * @param targetLang = The target language to attempt to translate the message to.
	 *
	 * @deprecated Deprecated because lang.yml more or less removed this as a need.
	 * */
	@Deprecated
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
//				ComWarfare.sendMessage(p, message);
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
	public static boolean isLegacy() {
		return legacy;
	}

	/**
	 * @return If the server is running on Bukkit 1.14.X or later builds.
	 * */
	public static boolean canUseCustomModelData() {
		return canUseCustomModelData;
	}

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
	 * @return Returns if the server has ItemBridge installed.
	 * */
	public static boolean hasItemBridge() {
		return getInstance().hasItemBridge;
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

	public static boolean isKickAfterMatch() {
		return getInstance().kickAfterMatch;
	}

	public static boolean isMapVoting() {
		return getInstance().mapVoting;
	}

	public static boolean isDebug() {
		return debug;
	}

	public static boolean isSpawnProtection() {
		return spawnProtection;
	}

	public static boolean isKillFeedShowAll() {
		return killFeedShowAll;
	}

	public static boolean isKillFeedUseBossBar() {
		return killFeedUseBossBar;
	}

	public static int getSpawnProtectionDuration() {
		return spawnProtectionDuration;
	}

	public static List<ItemBridgePrefix> getItemBridgePrefixes() {
		return getInstance().itemBridgePrefixes;
	}

	public ItemBridgePrefix getItemBridgePrefix(CodWeapon weapon) {
		for (ItemBridgePrefix p : getItemBridgePrefixes())
			if (p.getWeapons().contains(weapon.getName()))
				return p;
		return null;
	}
}
