package com.rhetorical.cod.game;

import com.rhetorical.cod.ComVersion;
import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.assignments.AssignmentManager;
import com.rhetorical.cod.game.events.KillFeedEvent;
import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.lang.LevelNames;
import com.rhetorical.cod.loadouts.Loadout;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.perks.Perk;
import com.rhetorical.cod.perks.PerkListener;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.progression.ProgressionManager;
import com.rhetorical.cod.progression.RankPerks;
import com.rhetorical.cod.progression.StatHandler;
import com.rhetorical.cod.sounds.events.*;
import com.rhetorical.cod.streaks.KillStreak;
import com.rhetorical.cod.streaks.KillStreakManager;
import com.rhetorical.cod.weapons.CodGun;
import com.rhetorical.cod.weapons.CodWeapon;
import com.rhetorical.cod.weapons.CrackShotGun;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This handles like 60% of everything needed for a game of COM-Warfare. This is the representation of the actual Game Lobby.
 * */

public class GameInstance implements Listener {

	private long id;

	private ArrayList<Player> players;
	private CodMap currentMap;
	private int gameTime;
	private int lobbyTime;

	private GameState state;

	private ArrayList<Player> blueTeam = new ArrayList<>();
	private ArrayList<Player> redTeam = new ArrayList<>();

	private int blueTeamScore;
	private int redTeamScore;

	private int hardpointController;

	private boolean forceStarted = false;

	private final int maxScore_TDM,
			maxScore_RSB,
			maxScore_FFA,
			maxScore_DOM,
			maxScore_CTF,
			maxScore_KC,
			maxScore_GUN,
			maxScore_OITC,
			maxScore_RESCUE,
			maxScore_HARDPOINT,
			maxScore_GUNFIGHT;

	private CtfFlag redFlag, blueFlag;

	private DomFlag aFlag, bFlag, cFlag, hardpointFlag;

	// Score management and game information system for FFA (Free for all)
	private HashMap<Player, Integer> ffaPlayerScores = new HashMap<>();
	private HashMap<Player, Object> freeForAllBar = new HashMap<>();

	private Object scoreBar;

	private ScoreboardManager scoreboardManager;

	public HealthManager health;
	private HungerManager hungerManager;

	private HashMap<Player, CodScore> playerScores = new HashMap<>();

	private CodMap[] nextMaps = new CodMap[2];
	private Gamemode[] nextModes = new Gamemode[2];
	private ArrayList[] mapVotes = new ArrayList[2];

	private final EntityManager entityManager = new EntityManager();

	private boolean blueUavActive;
	private boolean redUavActive;

	private boolean blueVSATActive;
	private boolean redVSATActive;

	private boolean blueCounterUavActive;
	private boolean redCounterUavActive;
	private boolean pinkCounterUavActive;

	private boolean blueNukeActive;
	private boolean redNukeActive;
	private Player pinkNukeActive;

	private boolean pastClassChange = true;
	private boolean canVote = true;

	//todo: keep tracck of all runnables and cancel out when staritng
	private List<BukkitRunnable> runnables = new ArrayList<>();


	GameInstance(ArrayList<Player> pls, CodMap map) {

		try {
			scoreBar = Bukkit.createBossBar(ChatColor.GRAY + "«" + ChatColor.WHITE + getFancyTime(ComWarfare.getPlugin().getConfig().getInt("lobbyTime")) + ChatColor.RESET + "" + ChatColor.GRAY + "»", org.bukkit.boss.BarColor.GREEN, org.bukkit.boss.BarStyle.SEGMENTED_10);
		} catch(NoClassDefFoundError e) {
			System.out.println();
		} catch(Exception ignored) {}

		id = System.currentTimeMillis();

		players = pls;
		currentMap = map;
		ComWarfare.getPlugin().reloadConfig();

		updateTimeLeft();

		lobbyTime = ComWarfare.getPlugin().getConfig().getInt("lobbyTime");

		if (ComVersion.getPurchased()) {
			maxScore_TDM = ComWarfare.getPlugin().getConfig().getInt("maxScore.TDM");
			maxScore_CTF = ComWarfare.getPlugin().getConfig().getInt("maxScore.CTF");
			maxScore_DOM = ComWarfare.getPlugin().getConfig().getInt("maxScore.DOM");
			maxScore_FFA = ComWarfare.getPlugin().getConfig().getInt("maxScore.FFA");
			maxScore_RSB = ComWarfare.getPlugin().getConfig().getInt("maxScore.RSB");
			maxScore_KC = ComWarfare.getPlugin().getConfig().getInt("maxScore.KC");
			maxScore_GUN = GameManager.gunGameGuns.size();
			maxScore_OITC = ComWarfare.getPlugin().getConfig().getInt("maxScore.OITC");
			maxScore_RESCUE = ComWarfare.getPlugin().getConfig().getInt("maxScore.RESCUE");
			maxScore_HARDPOINT = ComWarfare.getPlugin().getConfig().getInt("maxScore.HARDPOINT");
			maxScore_GUNFIGHT = ComWarfare.getPlugin().getConfig().getInt("maxScore.GUNFIGHT");
		} else {
			maxScore_TDM = 75;
			maxScore_RSB = 75;
			maxScore_FFA = 30;
			maxScore_KC = 50;
			maxScore_DOM = 200;
			maxScore_CTF = 3;
			maxScore_OITC = 3;
			maxScore_RESCUE = 4;
			maxScore_GUN = GameManager.gunGameGuns.size();
			maxScore_HARDPOINT = 75;
			maxScore_GUNFIGHT = 6;
		}

		setState(GameState.WAITING);

		health = new HealthManager(pls, ComWarfare.getDefaultHealth());
		hungerManager = new HungerManager();

		Bukkit.getServer().getPluginManager().registerEvents(this, ComWarfare.getPlugin());

		for (Player p : pls) {
			health.update(p);
		}

		scoreboardManager = new ScoreboardManager(this);

		ComWarfare.getConsole().sendMessage(ChatColor.GRAY + "Game lobby with id " + getId() + " created with map " + getMap().getName() + " with gamemode " + getGamemode() + ".");

		startLobbyTimer(lobbyTime);
	}

	/**
	 * Sets up the next maps for map voting.
	 * */
	private void setupNextMaps() {
		clearNextMaps();
		CodMap m1 = GameManager.pickRandomMap();
		CodMap m2 = GameManager.pickRandomMap();

		nextMaps[0] = m1 == null ? currentMap : m1;
		nextMaps[1] = m2 == null ? currentMap : m2;
		mapVotes[0] = new ArrayList<>();
		mapVotes[1] = new ArrayList<>();

		if (nextMaps[0] != null)
			nextModes[0] = nextMaps[0].getRandomGameMode();
		if (nextMaps[1] != null)
			nextModes[1] = nextMaps[1].getRandomGameMode();
	}

	/**
	 * Cleans up residue from map voting.
	 * */
	private void clearNextMaps() {
		if (nextMaps[0] != null && nextMaps[0] != currentMap) {
			GameManager.usedMaps.remove(nextMaps[0]);
		}

		if (nextMaps[1] != null && nextMaps[1] != currentMap) {
			GameManager.usedMaps.remove(nextMaps[1]);
		}
	}

	/**
	 * Adds a vote for the given map from the given player
	 * */
	public void addVote(int map, Player p) throws Exception {
		if (map != 1 && map != 0)
			throw new Exception(ComWarfare.getPrefix() + "Improper map selected!");

		mapVotes[0].remove(p);
		mapVotes[1].remove(p);

		if (!mapVotes[map].contains(p))
			mapVotes[map].add(p);
	}

	/**
	 * Resets the game instance to prepare for the next game. DO NOT CALL FROM OUTSIDE OF GAME LOOP WITHOUT FIRST STOPPING GAME.
	 * */
	private void reset() {

		redTeamScore = 0;
		blueTeamScore = 0;
		ffaPlayerScores.clear();
		blueTeam.clear();
		redTeam.clear();

		setState(GameState.WAITING);

		changeMap(GameManager.pickRandomMap());

		health = new HealthManager(players, ComWarfare.getDefaultHealth());

		for (Player p : players) {
			p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			health.update(p);
			p.getInventory().clear();
			p.teleport(ComWarfare.getLobbyLocation());

			setTeamArmor(p);

			InventoryManager inv = InventoryManager.getInstance();
			p.getInventory().setItem(0, inv.codItem);
			p.getInventory().setItem(8, inv.leaveItem);

			try {
				scoreBar.getClass().getMethod("setTitle", String.class).invoke(scoreBar, ChatColor.GOLD + getMap().getName() + " " + ChatColor.GRAY + "«" + ChatColor.WHITE + getFancyTime(lobbyTime) + ChatColor.RESET + "" + ChatColor.GRAY + "» " + ChatColor.GOLD + getMap().getGamemode().toString());
			}catch(NoClassDefFoundError e) {
				System.out.println();
			} catch(Exception ignored) {}

			getScoreboardManager().clearScoreboards(p);
		}

		playerScores.clear();

		startLobbyTimer(lobbyTime);
	}

	public long getId() {
		return id;
	}

	void changeMap(CodMap map) {
		GameManager.usedMaps.remove(getMap());
		clearNextMaps();
		canVote = false;

		if (map != null) {
			currentMap = map;
			map.changeGamemode();
		}

		updateTimeLeft();
	}

	void changeMap(CodMap map, Gamemode mode) {
		if (map == null)
			return;

		GameManager.usedMaps.remove(getMap());
		clearNextMaps();

		currentMap = map;
		map.setGamemode(mode);
		updateTimeLeft();
	}

	public void changeGamemode(Gamemode gm) {
		if (getState() != GameState.WAITING && getState() != GameState.STARTING)
			return;

		if (!getMap().getAvailableGamemodes().contains(gm))
			return;

		clearNextMaps();
		canVote = false;

		currentMap.changeGamemode(gm);

		updateTimeLeft();
	}

	/**
	 * Adds the given player to the game lobby.
	 * @return Returns if the player may join the match.
	 * */
	boolean addPlayer(Player p) {

		if (p == null)
			return false;

		if (players.size() >= ComWarfare.getMaxPlayers())
			return false;

		if (players.contains(p))
			return false;

		if (getState() == GameState.STOPPING)
			return false;

		players.add(p);

		new PlayerSnapshot(p);
		int level = ProgressionManager.getInstance().getLevel(p);
		String prestige = ProgressionManager.getInstance().getPrestigeLevel(p) > 0 ? ChatColor.WHITE + "[" + ChatColor.GREEN + ProgressionManager.getInstance().getPrestigeLevel(p) + ChatColor.WHITE + "]-" : "";
		String levelName = LevelNames.getInstance().getLevelName(level);
		levelName = !levelName.equals("") ? "[" + levelName + "] " : "";
		p.setPlayerListName(ChatColor.WHITE + levelName + prestige + "[" +
				level + "] " + ChatColor.YELLOW + p.getDisplayName());

		health.addPlayer(p);
		hungerManager.addPlayer(p);

		ProgressionManager.getInstance().update(p);

		p.getInventory().clear();

		KillStreakManager.getInstance().loadStreaks(p);

		p.setGameMode(GameMode.SURVIVAL);
		p.setHealth(20D);
		p.setFoodLevel(20);
		ProgressionManager.getInstance().update(p);

		List<PotionEffect> peTypes = new ArrayList<>(p.getActivePotionEffects());
		for (PotionEffect pe : peTypes) {
			p.removePotionEffect(pe.getType());
		}

		p.teleport(ComWarfare.getLobbyLocation());

		playerScores.put(p, new CodScore(p));

		try {
			scoreBar.getClass().getMethod("addPlayer", Player.class).invoke(scoreBar, p);

		} catch(NoClassDefFoundError e) {
			System.out.println();
		}catch(Exception ignored) {}

		if (getState() == GameState.IN_GAME) {

			getScoreboardManager().setupGameBoard(p, getFancyTime(gameTime));

			assignTeams();

			if (getGamemode() == Gamemode.OITC) {
				ffaPlayerScores.put(p, maxScore_OITC);
			}

			if (getGamemode() == Gamemode.FFA || getGamemode() == Gamemode.OITC || getGamemode() == Gamemode.GUN) {
				try {
					Object bar = Bukkit.createBossBar(ChatColor.GRAY + "«" + ChatColor.WHITE + getFancyTime(gameTime) + ChatColor.RESET + ChatColor.WHITE + "»", org.bukkit.boss.BarColor.GREEN, org.bukkit.boss.BarStyle.SEGMENTED_10);
					freeForAllBar.put(p, bar);
					freeForAllBar.get(p).getClass().getMethod("addPlayer", Player.class).invoke(freeForAllBar.get(p), p);
				} catch(NoClassDefFoundError e) {
					System.out.println();
				} catch(Exception ignored) {}
			}

			if (getGamemode() != Gamemode.RESCUE && getGamemode() != Gamemode.GUNFIGHT) {

				Location spawn;
				if (isOnRedTeam(p)) {
					spawn = currentMap.getRedSpawn();
				} else if (isOnBlueTeam(p)) {
					spawn = currentMap.getBlueSpawn();
				} else {
					spawn = currentMap.getPinkSpawn();
				}

				spawnCodPlayer(p, spawn);
			} else {
				p.setGameMode(GameMode.SPECTATOR);
				isAlive.put(p, false);
				if (isOnRedTeam(p)) {
					if(redTeam.size() > 1) {
						p.setSpectatorTarget(redTeam.get(0));
					}
				} else if (isOnBlueTeam(p)) {
					if(blueTeam.size() > 1) {
						p.setSpectatorTarget(redTeam.get(0));
					}
				}
			}
		} else {
			setTeamArmor(p);
			p.getInventory().setItem(0, InventoryManager.getInstance().codItem);
			p.getInventory().setItem(8, InventoryManager.getInstance().leaveItem);

			getScoreboardManager().setupLobbyBoard(p, getFancyTime(lobbyTime));
		}

		for (Player pp : players) {
			ComWarfare.sendMessage(pp, Lang.PLAYER_JOINED_LOBBY.getMessage().replace("{player}", p.getDisplayName()), ComWarfare.getLang());
		}

		return true;
	}

	private void addBluePoint() {
		blueTeamScore++;
	}

	private void addRedPoint() {
		redTeamScore++;
	}

	private void addPointForPlayer(Player p) {
		if (!ffaPlayerScores.containsKey(p)) {
			ffaPlayerScores.put(p, 0);
		}

		ffaPlayerScores.put(p, ffaPlayerScores.get(p) + 1);
	}

	private void removePointForPlayer(Player p) {
		if (!ffaPlayerScores.containsKey(p)) {
			ffaPlayerScores.put(p, 0);
			return;
		}

		if (ffaPlayerScores.get(p) <= 0)
			return;


		ffaPlayerScores.put(p, ffaPlayerScores.get(p) - 1);
	}

	/**
	 * Removes the given player from the game lobby.
	 * */
	public void removePlayer(Player p) {
		if (!players.contains(p))
			return;

//		if (isLegacy)
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());


		try {
			List pls = (List) scoreBar.getClass().getMethod("getPlayers").invoke(scoreBar);
			if (pls.contains(p)) {
				scoreBar.getClass().getMethod("removePlayer", Player.class).invoke(scoreBar, p);
			}
		} catch(NoClassDefFoundError ignored) {
		}catch(Exception ignored) {
		}

		if (freeForAllBar.containsKey(p)) {
			if (freeForAllBar.get(p) == null) {
				freeForAllBar.remove(p);
			}

			try {
				Object bar = freeForAllBar.get(p);

				bar.getClass().getMethod("removeAll").invoke(bar);

				freeForAllBar.remove(p);
			} catch(NoClassDefFoundError e) {
				System.out.println();
			}catch(Exception ignored) {}
		}

		health.removePlayer(p);

		playerScores.remove(p);

		players.remove(p);
		hungerManager.removePlayer(p);
		ffaPlayerScores.remove(p);

		ProgressionManager.getInstance().saveData(p);

		AssignmentManager.getInstance().save(p);

		if (PlayerSnapshot.hasSnapshot(p)) {
			PlayerSnapshot.apply(p);
		} else {
			p.setPlayerListName(p.getDisplayName());
			p.setFoodLevel(20);
			p.setLevel(0);
			p.setExp(0f);
			p.setHealth(20d);
		}

		if (players.size() == 0) {
			despawnCtfFlags();
			despawnDomFlags();
			despawnHardpointFlag();
			GameManager.removeInstance(this);
			return;
		} else if ((redTeam.size() > 0 && blueTeam.size() == 0)
				|| (blueTeam.size() > 0 && redTeam.size() == 0)
				|| getPlayers().size() == 1) {
			if (getState() == GameState.IN_GAME) {
				if (!ComWarfare.isDisabling())
					stopGame();
			}
		}

		getScoreboardManager().clearScoreboards(p);

		try {
			p.getClass().getMethod("setPlayerListHeader", String.class).invoke(p, "");
			p.getClass().getMethod("setPlayerListFooter", String.class).invoke(p, "");
		} catch(NoSuchMethodException ignored) {} catch(Exception ignored) {}

	}

	/**
	 * Starts the game
	 * */
	private void startGame() {

		forceStarted = false;

		blueTeam.clear();
		redTeam.clear();
		ffaPlayerScores.clear();

		assignTeams();
		playerScores.clear();

		despawnDomFlags();
		despawnHardpointFlag();
		despawnCtfFlags();

		for (Player p : players) {

			KillStreakManager.getInstance().reset(p);

			playerScores.put(p, new CodScore(p));

			if (getGamemode() != Gamemode.FFA && getGamemode() != Gamemode.OITC && getGamemode() != Gamemode.GUN) {
				if (blueTeam.contains(p)) {
					spawnCodPlayer(p, currentMap.getBlueSpawn());
				} else if (redTeam.contains(p)) {
					spawnCodPlayer(p, currentMap.getRedSpawn());
				} else {
					assignTeams();
				}
			} else {
				if (getGamemode() != Gamemode.OITC) {
					ffaPlayerScores.put(p, 0);
				} else {
					ffaPlayerScores.put(p, maxScore_OITC);
				}
				spawnCodPlayer(p, currentMap.getPinkSpawn());
			}

			Bukkit.getPluginManager().callEvent(new GameStartSoundEvent(p, getGamemode()));

		}

		if (getGamemode() == Gamemode.RESCUE || getGamemode() == Gamemode.GUNFIGHT) {
			for (Player p : players) {
				isAlive.put(p, true);
			}
		}

		startGameTimer(gameTime, false);
		setState(GameState.IN_GAME);
	}

	private void spawnCtfFlags() {
		despawnCtfFlags();

		if (getGamemode() != Gamemode.CTF)
			return;

		redFlag = new CtfFlag(this, Team.RED, Lang.FLAG_RED, getMap().getRedFlagSpawn());
		blueFlag = new CtfFlag(this, Team.BLUE, Lang.FLAG_BLUE, getMap().getBlueFlagSpawn());

		redFlag.setOtherFlag(blueFlag);
		blueFlag.setOtherFlag(redFlag);

		redFlag.spawn();
		blueFlag.spawn();
	}

	/**
	 * Spawns the player within the current map at the given Location with the given loadout.
	 * */
	private void spawnCodPlayer(Player p, Location L, Loadout loadout) {
		p.teleport(L);
		p.getInventory().clear();
		p.setGameMode(GameMode.ADVENTURE);
		p.setHealth(20d);
		p.setFoodLevel(20);
		health.reset(p);

		setTeamArmor(p);

		Bukkit.getPluginManager().callEvent(new PlayerSpawnSoundEvent(p));

		if (getGamemode() == Gamemode.RSB) {

			CodGun primary = LoadoutManager.getInstance().getRandomPrimary();
			CodGun secondary = LoadoutManager.getInstance().getRandomSecondary();
			CodWeapon lethal = LoadoutManager.getInstance().getRandomLethal();
			CodWeapon tactical = LoadoutManager.getInstance().getRandomTactical();

			ItemStack primaryAmmo = primary.getAmmo();
			primaryAmmo.setAmount(primary.getAmmoCount());

			ItemStack secondaryAmmo = secondary.getAmmo();
			secondaryAmmo.setAmount(secondary.getAmmoCount());


			p.getInventory().setItem(0, LoadoutManager.getInstance().knife);
			if (!primary.equals(LoadoutManager.getInstance().blankPrimary)) {
				p.getInventory().setItem(1, CrackShotGun.updateItem(primary.getName(), primary.getGunItem(), p));
				p.getInventory().setItem(28, primaryAmmo);
			}

			if (!secondary.equals(LoadoutManager.getInstance().blankSecondary)) {
				p.getInventory().setItem(2, CrackShotGun.updateItem(secondary.getName(), secondary.getGunItem(), p));
				p.getInventory().setItem(29, secondaryAmmo);
			}

			if (Math.random() > 0.5 && !lethal.equals(LoadoutManager.getInstance().blankLethal)) {
				p.getInventory().setItem(3, lethal.getWeaponItem());
			}

			if (Math.random() > 0.5 && !tactical.equals(LoadoutManager.getInstance().blankTactical)) {
				p.getInventory().setItem(4, tactical.getWeaponItem());
			}

		} else if (getGamemode() == Gamemode.DOM
				|| getGamemode() == Gamemode.CTF
				|| getGamemode() == Gamemode.KC
				|| getGamemode() == Gamemode.TDM
				|| getGamemode() == Gamemode.FFA
				|| getGamemode() == Gamemode.INFECT
				|| getGamemode() == Gamemode.RESCUE
				|| getGamemode() == Gamemode.HARDPOINT
				|| getGamemode() == Gamemode.GUNFIGHT) {

			p.getInventory().setItem(0, LoadoutManager.getInstance().knife);

			if (getGamemode() != Gamemode.INFECT || (getGamemode() == Gamemode.INFECT && blueTeam.contains(p))) {
				LoadoutManager.getInstance().giveLoadout(p, loadout);
			}

			if (getGamemode() == Gamemode.INFECT && redTeam.contains(p)) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * gameTime, 1));
			}

			if (getGamemode() == Gamemode.RESCUE || getGamemode() == Gamemode.GUNFIGHT)
				isAlive.put(p, true);

		} else if (getGamemode() == Gamemode.OITC) {
			p.getInventory().setItem(0, LoadoutManager.getInstance().knife);
			p.getInventory().setItem(1, GameManager.oitcGun.getGunItem());
			ItemStack ammo = GameManager.oitcGun.getAmmo();
			ammo.setAmount(1);
			p.getInventory().setItem(8, ammo);
		} else if (getGamemode() == Gamemode.GUN) {
			if(!ffaPlayerScores.containsKey(p)) {
				ffaPlayerScores.put(p, 0);
			}
			p.getInventory().setItem(0, LoadoutManager.getInstance().knife);
			if (getState() != GameState.STOPPING) {
				CodGun gun = GameManager.gunGameGuns.get(ffaPlayerScores.get(p));

				ItemStack ammo = gun.getAmmo();
				ammo.setAmount(gun.getAmmoCount());

				p.getInventory().setItem(1, CrackShotGun.updateItem(gun.getName(), gun.getGunItem(), p));
				p.getInventory().setItem(28, ammo);
			}
		}

		if (ComWarfare.isSpawnProtection())
			p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, ComWarfare.getSpawnProtectionDuration() * 20, 1));

		p.getInventory().setItem(32, InventoryManager.getInstance().selectClass);
		p.getInventory().setItem(35, InventoryManager.getInstance().leaveItem);

		p.updateInventory();

		KillStreakManager.getInstance().streaksAfterDeath(p);
	}

	/**
	 * Spawns the player within the current map at the given Location with their selected loadout.
	 * */
	private void spawnCodPlayer(Player p, Location loc) {
		spawnCodPlayer(p, loc, LoadoutManager.getInstance().getActiveLoadout(p));
	}

	/**
	 * Drops a dog tag belonging to the target player.
	 * */
	private void dropDogTag(Player p) {
		if (!GameManager.isInMatch(p))
			return;

		if (!players.contains(p))
			return;

		if (!isOnRedTeam(p) && !isOnBlueTeam(p)) {
			assignTeams();
			return;
		}

		if (getGamemode() == Gamemode.RESCUE || getGamemode() == Gamemode.KC) {

			ItemStack dogtag = new ItemStack(Material.NAME_TAG);

			ItemMeta meta = dogtag.getItemMeta();

			ChatColor teamColor;

			if (blueTeam.contains(p))
				teamColor = ChatColor.BLUE;
			else
				teamColor = ChatColor.RED;


			meta.setDisplayName(Lang.PLAYER_DOG_TAG_NAME.getMessage().replace("{team-color}", teamColor + "").replace("{player}", p.getName()));

			List<String> lore = new ArrayList<>();

			if (blueTeam.contains(p))
				lore.add(p.getUniqueId().toString());
			else
				lore.add(p.getUniqueId().toString());

			meta.setLore(lore);
			dogtag.setItemMeta(meta);


			Entity e = p.getWorld().dropItem(p.getLocation(), dogtag);
			e.setCustomNameVisible(true);
			entityManager.registerEntity(e);
		}
	}

	/**
	 * Assigns player to teams randomly.
	 * */
	private void assignTeams() {

		if (getGamemode() != Gamemode.FFA && getGamemode() != Gamemode.OITC && getGamemode() != Gamemode.GUN && getGamemode() != Gamemode.INFECT) {
			for (Player p : players) {
				if (blueTeam.contains(p) || redTeam.contains(p))
					continue;

				ChatColor tColor;
				String team;

				if (redTeam.size() >= blueTeam.size()) {
					blueTeam.add(p);
					tColor = ChatColor.BLUE;
					team = "blue";
				} else {
					redTeam.add(p);
					tColor = ChatColor.RED;
					team = "red";
				}

				ComWarfare.sendMessage(p, Lang.ASSIGNED_TO_TEAM.getMessage().replace("{team-color}", tColor + "").replace("{team}", team), ComWarfare.getLang());
			}
		} else if (getGamemode() == Gamemode.INFECT) {
			List<Player> pls = new ArrayList<>(getPlayers());
			Collections.shuffle(pls);
			for (Player p : pls) {
				ChatColor tColor;
				String team;
				if (redTeam.isEmpty()) {
					redTeam.add(p);
					tColor = ChatColor.RED;
					team = "red";
				} else {
					blueTeam.add(p);
					tColor = ChatColor.BLUE;
					team = "blue";
				}


				ComWarfare.sendMessage(p, Lang.ASSIGNED_TO_TEAM.getMessage().replace("{team-color}", tColor + "").replace("{team}", team), ComWarfare.getLang());
			}
		} else {
			for (Player p : players) {
				if (ffaPlayerScores.containsKey(p))
					continue;

				ComWarfare.sendMessage(p, Lang.ASSIGNED_TO_TEAM.getMessage().replace("{team-color}", ChatColor.LIGHT_PURPLE + "").replace("{team}", "pink"), ComWarfare.getLang());
			}
		}

	}

	/**
	 * Stops the game with a timer.
	 * */
	private void stopGame() {

		setState(GameState.STOPPING);

		despawnDomFlags();
		despawnCtfFlags();
		despawnHardpointFlag();

		resetKillstreakData();

		entityManager.clearEntities();

		CodScore highestScore = null;
		CodScore highestKD = null;

		for (Player p : getPlayers()) {
			if (p.getGameMode().equals(GameMode.SPECTATOR)) {
				Location spawnPoint = isOnPinkTeam(p) ? currentMap.getPinkSpawn() : isOnBlueTeam(p) ? currentMap.getBlueSpawn() : currentMap.getRedSpawn();
				spawnCodPlayer(p, spawnPoint);
			}
			p.setGameMode(GameMode.ADVENTURE);
		}

		for (CodScore score : playerScores.values()) {
			if (highestScore == null || score.getScore() > highestScore.getScore()) {
				highestScore = score;
			}

			if (highestKD == null || score.getRatio() > highestKD.getRatio()) {
				highestKD = score;
			}
		}

		if (!ComWarfare.getRewardHighestScore().equalsIgnoreCase("none") && highestScore != null) {
			String cmd = ComWarfare.getRewardHighestScore().replace("{PLAYER}", highestScore.getOwner().getName());
			Bukkit.getServer().dispatchCommand(ComWarfare.getConsole(), cmd);
		}

		if (!ComWarfare.getRewardHighestKD().equalsIgnoreCase("none") && highestKD != null) {
			String cmd = ComWarfare.getRewardHighestKD().replace("{PLAYER}", highestScore.getOwner().getName());
			Bukkit.getServer().dispatchCommand(ComWarfare.getConsole(), cmd);
		}

		for (Player p : getPlayers()) {

			boolean won = false;

			p.removePotionEffect(PotionEffectType.SPEED);

			if (getWinningTeam().equalsIgnoreCase("red") && redTeam.contains(p)) {
				won = true;
			} else if (getWinningTeam().equalsIgnoreCase("blue") && blueTeam.contains(p)) {
				won = true;
			} else if (getWinningTeam().equals(p.getDisplayName())) {
				won = true;
			}

			Bukkit.getPluginManager().callEvent(new GameEndSoundEvent(p, won));

			AssignmentManager.getInstance().updateAssignments(p, 0, getGamemode(), won);

			AssignmentManager.getInstance().save(p);

			if (freeForAllBar.containsKey(p)) {
				try {
					freeForAllBar.get(p).getClass().getMethod("removeAll").invoke(freeForAllBar.get(p));
				}catch(NoClassDefFoundError e) {
					System.out.println();
				} catch(Exception ignored) {}
			}

			try {
				List players = (List) scoreBar.getClass().getMethod("getPlayers").invoke(scoreBar);
				if (!players.contains(p)) {
					scoreBar.getClass().getMethod("addPlayer", Player.class).invoke(scoreBar, p);
				}
			} catch(NoClassDefFoundError e) {
				System.out.println();
			}catch (Exception ignored) {}

			p.getInventory().clear();

			ProgressionManager.getInstance().saveData(p);

			StatHandler.saveStatData();
		}

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = 10;

			public void run() {

				if (cancelIfNotActive(this))
					return;

				if (t <= 0) {
					if (!getPlayers().isEmpty() && !ComWarfare.isKickAfterMatch()) {
						game.reset();
						cancel();
						getRunnables().remove(this);
					} else {
						getRunnables().remove(this);
						cancel();
						GameManager.removeInstance(GameInstance.this);
					}
					return;
				}


				if (t == 10) {
					for (Player p : game.players) {
						String teamFormat = "";

						if (currentMap.getGamemode() != Gamemode.FFA && currentMap.getGamemode() != Gamemode.GUN && currentMap.getGamemode() != Gamemode.OITC) {
							if (getWinningTeam().equalsIgnoreCase("red")) {
								teamFormat = ChatColor.RED + "RED";
							} else if (getWinningTeam().equalsIgnoreCase("blue")) {
								teamFormat = ChatColor.BLUE + "BLUE";
							} else if (getWinningTeam().equalsIgnoreCase("nobody") || getWinningTeam().equalsIgnoreCase("tie")) {
								ComWarfare.sendMessage(p,  ChatColor.GRAY + Lang.NOBODY_WON_GAME.getMessage(), ComWarfare.getLang());
								playerScores.computeIfAbsent(p, k -> new CodScore(p));
								CodScore score = playerScores.get(p);

								float kd = ((float) score.getKills() / (float) score.getDeaths());

								if (Float.isNaN(kd) || Float.isInfinite(kd)) {
									kd = score.getKills();
								}

								String msg = Lang.END_GAME_KILLS_DEATHS.getMessage();
								msg = msg.replace("{kills}", score.getKills() + "");
								msg = msg.replace("{deaths}", score.getDeaths() + "");
								msg = msg.replace("{kd}", kd + "");
								ComWarfare.sendMessage(p, msg, ComWarfare.getLang());
								continue;
							}

							ComWarfare.sendMessage(p, Lang.SOMEBODY_WON_GAME.getMessage().replace("{team}", teamFormat), ComWarfare.getLang());
							CodScore score = playerScores.get(p);

							float kd = ((float) score.getKills() / (float) score.getDeaths());

							if (Float.isNaN(kd) || Float.isInfinite(kd)) {
								kd = score.getKills();
							}

							String msg = Lang.END_GAME_KILLS_DEATHS.getMessage();
							msg = msg.replace("{kills}", score.getKills() + "");
							msg = msg.replace("{deaths}", score.getDeaths() + "");
							msg = msg.replace("{kd}", kd + "");
							ComWarfare.sendMessage(p, msg, ComWarfare.getLang());
						} else {
							ComWarfare.sendMessage(p, Lang.SOMEONE_WON_GAME.getMessage().replace("{player}", getWinningTeam()), ComWarfare.getLang());
							CodScore score = playerScores.get(p);
							float kd = ((float) score.getKills() / (float) score.getDeaths());

							if (Float.isNaN(kd) || Float.isInfinite(kd)) {
								kd = score.getKills();
							}

							String msg = Lang.END_GAME_KILLS_DEATHS.getMessage();
							msg = msg.replace("{kills}", score.getKills() + "");
							msg = msg.replace("{deaths}", score.getDeaths() + "");
							msg = msg.replace("{kd}", kd + "");
							ComWarfare.sendMessage(p, msg, ComWarfare.getLang());
						}
					}
				}

				t--;

				for (Player p : getPlayers()) {
					ComWarfare.sendActionBar(p, Lang.RETURNING_TO_LOBBY.getMessage().replace("{time}", t + ""));
				}

			}
		};

		getRunnables().add(br);
		br.runTaskTimer(ComWarfare.getPlugin(), 0L, 20L);
	}

	/**
	 * Starts the game loop. Only needs to be called once from opening a game instance.
	 * */
	private void startLobbyTimer(int time) {

		setState(GameState.STARTING);

		forceStarted = false;
		canVote = true;


		try {
			scoreBar.getClass().getMethod("removeAll").invoke(scoreBar);
		} catch(NoClassDefFoundError e) {
			System.out.println();
		}catch(Exception ignored) {}
		for (Player p : players) {
			try {
				scoreBar.getClass().getMethod("addPlayer", Player.class).invoke(scoreBar, p);
			} catch(Exception ignored) {}
			p.getInventory().setItem(0, InventoryManager.getInstance().codItem);
			p.getInventory().setItem(8, InventoryManager.getInstance().leaveItem);

			getScoreboardManager().clearScoreboards(p);
			getScoreboardManager().setupLobbyBoard(p, getFancyTime(lobbyTime));
		}

		GameInstance game = this;

		setupNextMaps();

		for (Player p : players) {
			setTeamArmor(p);
		}

		changeMap(nextMaps[0], nextModes[0]);


		BukkitRunnable br = new BukkitRunnable() {

			int t = time;

			int lobbyTime = time;

			@Override
			public void run() {
				if (cancelIfNotActive(this))
					return;

				if (t == 0 || forceStarted || getState() == GameState.IN_GAME || getState() == GameState.STOPPING) {

					for (Player p : getPlayers()) {
						if (t == 0) {
							ComWarfare.sendMessage(p, Lang.GAME_STARTING.getMessage(), ComWarfare.getLang());
						}
					}

					clearNextMaps();
					startGame();
					cancel();
					getRunnables().remove(this);
					return;
				}

				String counter = getFancyTime(t);

				if (getPlayers().size() == 1) {
					t = lobbyTime;
				} else
					t--;

				try {
					scoreBar.getClass().getMethod("setTitle", String.class).invoke(scoreBar, ChatColor.GOLD + getMap().getName() + " " + ChatColor.GRAY + "«" + ChatColor.WHITE + counter + ChatColor.RESET + "" + ChatColor.GRAY + "» " + ChatColor.GOLD + getMap().getGamemode().toString());
				} catch(Exception|NoClassDefFoundError ignored) {}

				double progress = (((double) t) / ((double) lobbyTime));

				try {
					scoreBar.getClass().getMethod("setProgress", Double.class).invoke(scoreBar, progress);
				} catch(Exception|NoClassDefFoundError ignored) {}

				if (canVote && t == 20) {
					CodMap[] maps = nextMaps;
					if (mapVotes[0].size() > mapVotes[1].size()) {
						changeMap(maps[0], nextModes[0]);
					} else if (mapVotes[1].size() > mapVotes[0].size()) {
						changeMap(maps[1], nextModes[1]);
					} else {
						int index = (new Random()).nextInt(2);
						changeMap(maps[index], nextModes[index]);
					}
					clearNextMaps();

					for (Player p : game.players) {
						ComWarfare.sendMessage(p, Lang.MAP_VOTING_NEXT_MAP.getMessage().replace("{map}", game.currentMap.getName()), ComWarfare.getLang());
					}
				}

				for (Player p : game.getPlayers()) {
					getScoreboardManager().updateLobbyBoard(p, getFancyTime(t));
				}

				if (t % 30 == 0 || (t % 10 == 0 && t < 30) || (t % 5 == 0 && t < 15)) {
					for (Player p : game.players) {
						sendNextMap(p, t);
						if (t > 20 && canVote) {
							ComWarfare.sendMessage(p, Lang.MAP_VOTING_HEADER.getMessage(), ComWarfare.getLang());
							ComWarfare.sendMessage(p, ChatColor.GRAY + "===============", ComWarfare.getLang());
							ComWarfare.sendMessage(p, Lang.MAP_VOTING_NAMES.getMessage().replace("{1}", nextMaps[0].getName() + " - " + nextModes[0].toString()).replace("{2}", nextMaps[1].getName() + " - " + nextModes	[1].toString()), ComWarfare.getLang());
							ComWarfare.sendMessage(p, Lang.MAP_VOTING_VOTES.getMessage().replace("{1}", mapVotes[0].size() + "").replace("{2}", mapVotes[1].size() + ""), ComWarfare.getLang());
						}
					}
				}

				for (Player p : game.players) {
					int level = ProgressionManager.getInstance().getLevel(p);
					String prestige = ProgressionManager.getInstance().getPrestigeLevel(p) > 0 ? ChatColor.WHITE + "[" + ChatColor.GREEN + ProgressionManager.getInstance().getPrestigeLevel(p) + ChatColor.WHITE + "]-" : "";
					String levelName = LevelNames.getInstance().getLevelName(level);
					levelName = !levelName.equals("") ? "[" + levelName + "] " : "";
					p.setPlayerListName(ChatColor.WHITE + levelName + prestige + "[" +
							level + "] " + ChatColor.YELLOW + p.getDisplayName());
					try {
						p.getClass().getMethod("setPlayerListHeader", String.class).invoke(p, Lang.LOBBY_HEADER.getMessage());
						p.getClass().getMethod("setPlayerListFooter", String.class).invoke(p, Lang.LOBBY_FOOTER.getMessage().replace("{time}", getFancyTime(t)));
					} catch(Exception ignored) {}

					if (canVote && t > 20) {
						if (p.getInventory().getItem(3) == null || !p.getInventory().getItem(3).getType().equals(InventoryManager.getInstance().voteItemA.getType())) {
							ItemStack voteItem = InventoryManager.getInstance().voteItemA;
							ItemMeta voteMeta = voteItem.getItemMeta();
							List<String> lore = new ArrayList<>();
							lore.add(Lang.VOTE_MAP_NAME.getMessage().replace("{map}", nextMaps[0].getName()));
							lore.add(Lang.VOTE_MAP_MODE.getMessage().replace("{mode}", nextModes[0].toString()));
							voteMeta.setLore(lore);
							voteItem.setItemMeta(voteMeta);
							p.getInventory().setItem(3, voteItem);
						}

						if (p.getInventory().getItem(4) == null || !p.getInventory().getItem(4).getType().equals(InventoryManager.getInstance().voteItemB.getType())) {
							ItemStack voteItem = InventoryManager.getInstance().voteItemB;
							ItemMeta voteMeta = voteItem.getItemMeta();
							List<String> lore = new ArrayList<>();
							lore.add(Lang.VOTE_MAP_NAME.getMessage().replace("{map}", nextMaps[1].getName()));
							lore.add(Lang.VOTE_MAP_MODE.getMessage().replace("{mode}", nextModes[1].toString()));
							voteMeta.setLore(lore);
							voteItem.setItemMeta(voteMeta);
							p.getInventory().setItem(4, voteItem);
						}
					} else {
						p.getInventory().setItem(3, new ItemStack(Material.AIR));
						p.getInventory().setItem(4, new ItemStack(Material.AIR));
					}
				}
			}
		};

		getRunnables().add(br);
		br.runTaskTimer(ComWarfare.getPlugin(), 0L, 20L);
	}


	/**
	 * Starts things that should run in the game loop but should run at a faster tick rate.
	 * */
	private void startPriorityGameTimer() {
		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				if (cancelIfNotActive(this))
					return;

				if (getState() != GameState.IN_GAME) {
					cancel();
					getRunnables().remove(this);
					return;
				}

				if (getGamemode() == Gamemode.CTF || getGamemode() == Gamemode.DOM || getGamemode() == Gamemode.HARDPOINT) {
					for (Player p : getPlayers()) {
						Location closestObjective = getClosestObjective(p);
						if (closestObjective != null) {
							p.setCompassTarget(closestObjective);
							int distance = (int) p.getLocation().distance(closestObjective);
							ItemStack stack = p.getInventory().getItem(8);
							boolean exists = true;
							if (stack == null || stack.getType() == Material.AIR) {
								stack = new ItemStack(Material.COMPASS, 1);
								exists = false;
							}
							ItemMeta meta = stack.getItemMeta();
							if (meta != null)
								meta.setDisplayName(Lang.CLOSEST_OBJECTIVE.getMessage().replace("{distance}", distance <= 100 ? Integer.toString(distance) : ">100"));
							stack.setItemMeta(meta);
							if (!exists)
								p.getInventory().setItem(8, stack);
						}
					}
				}
			}
		};

		getRunnables().add(br);
		br.runTaskTimer(ComWarfare.getPlugin(), 0L, 5L);
	}

	private void resetKillstreakData() {
		redUavActive = false;
		blueUavActive = false;

		redVSATActive = false;
		blueVSATActive = false;

		pinkCounterUavActive = false;
		redCounterUavActive = false;
		blueCounterUavActive = false;

		redNukeActive = false;
		blueNukeActive = false;
		pinkNukeActive = null;
	}

	private void startGameTimer(int time, boolean newRound) {

		pastClassChange = false;

		entityManager.clearEntities();

		resetKillstreakData();

		if (!newRound) {
			setState(GameState.IN_GAME);

			try {
				scoreBar.getClass().getMethod("removeAll").invoke(scoreBar);
			} catch(NoClassDefFoundError e) {
				System.out.println();
			}catch(Exception ignored) {}

			for (Player p : players) {
				if (!currentMap.getGamemode().equals(Gamemode.FFA) && !currentMap.getGamemode().equals(Gamemode.OITC) && !currentMap.getGamemode().equals(Gamemode.GUN)) {
					try {
						scoreBar.getClass().getMethod("addPlayer", Player.class).invoke(scoreBar, p);
					}catch(NoClassDefFoundError e) {
						System.out.println();
					} catch(Exception ignored) {}
				} else {

					try {
						Object bar = Bukkit.createBossBar(ChatColor.GRAY + "«" + ChatColor.WHITE + getFancyTime(gameTime) + ChatColor.RESET + ChatColor.WHITE + "»", org.bukkit.boss.BarColor.GREEN, org.bukkit.boss.BarStyle.SEGMENTED_10);
						freeForAllBar.put(p, bar);
						freeForAllBar.get(p).getClass().getMethod("addPlayer", Player.class).invoke(freeForAllBar.get(p), p);

					} catch(NoClassDefFoundError e) {
						System.out.println();
					} catch(Exception ignored) {}

					if (getGamemode() == Gamemode.OITC) {
						ffaPlayerScores.put(p, maxScore_OITC);
					} else {
						ffaPlayerScores.put(p, 0);
					}
				}

				getScoreboardManager().setupGameBoard(p, getFancyTime(gameTime));
			}

			startPriorityGameTimer();
		} else {


			if (getGamemode() == Gamemode.GUNFIGHT) {
				CodGun primary = LoadoutManager.getInstance().getRandomPrimary();
				CodGun secondary = LoadoutManager.getInstance().getRandomSecondary();
				CodWeapon lethal = LoadoutManager.getInstance().getRandomLethal();
				CodWeapon tactical = LoadoutManager.getInstance().getRandomTactical();

				Loadout loadout = new Loadout(null, "GUNFIGHT LOAOUT", primary, secondary, Math.random() > 0.5 ? lethal : LoadoutManager.getInstance().blankLethal, Math.random() > 0.5 ? tactical : LoadoutManager.getInstance().blankTactical, null, null, null, false);
				for (Player p : getPlayers())
					if (isOnBlueTeam(p))
						spawnCodPlayer(p, getMap().getBlueSpawn(), loadout);
					else if (isOnRedTeam(p))
						spawnCodPlayer(p, getMap().getRedSpawn(), loadout);
					else
						assignTeams();
			} else if (getGamemode() == Gamemode.RESCUE)
				for (Player p : getPlayers())
					if (isOnBlueTeam(p))
						spawnCodPlayer(p, getMap().getBlueSpawn());
					else if (isOnRedTeam(p))
						spawnCodPlayer(p, getMap().getRedSpawn());
					else
						assignTeams();
		}

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = time;
			int timeSinceLastHardpoint = 0;
			@Override
			public void run() {
				if (cancelIfNotActive(this))
					return;
				if (t == 0) {

					if (getGamemode() == Gamemode.RESCUE || getGamemode() == Gamemode.GUNFIGHT) {
						if (getAlivePlayers(redTeam) > getAlivePlayers(blueTeam)) {
							addBluePoint();

							if (getGamemode() == Gamemode.RESCUE) {
								if (!(blueTeamScore >= maxScore_RESCUE))
									startNewRound(7, blueTeam);
							}
							else if (getGamemode() == Gamemode.GUNFIGHT) {
								if (!(blueTeamScore >= maxScore_GUNFIGHT))
									startNewRound(7, blueTeam);
							}

							for (Player pp : players) {
								isAlive.put(pp, true);
							}

							getRunnables().remove(this);
							cancel();
							return;
						} else if (getAlivePlayers(redTeam) > getAlivePlayers(blueTeam)) {
							addRedPoint();

							if (getGamemode() == Gamemode.RESCUE) {
								if (!(redTeamScore >= maxScore_RESCUE))
									startNewRound(7, redTeam);
							} else if (getGamemode() == Gamemode.GUNFIGHT) {
								if (!(redTeamScore >= maxScore_GUNFIGHT))
									startNewRound(7, redTeam);
							}

							for (Player pp : players) {
								isAlive.put(pp, true);
							}

							getRunnables().remove(this);
							cancel();
							return;
						} else {
							startNewRound(7, null);
						}
						getRunnables().remove(this);
						cancel();
						return;
					}

					stopGame();

					getRunnables().remove(this);
					cancel();
					return;
				}

				if (t == time - 5) {
					if (getGamemode() == Gamemode.DOM)
						spawnDomFlags();

					if (getGamemode() == Gamemode.CTF)
						spawnCtfFlags();
				}

				if ((t == time || timeSinceLastHardpoint == 60) && getGamemode() == Gamemode.HARDPOINT) {
					updateHardpointFlagLocation();
					timeSinceLastHardpoint = 0;
				}

				if (getState() != GameState.IN_GAME) {
					this.cancel();
					return;
				}

				if (time - t == 10) {
					pastClassChange = true;
				}

				timeSinceLastHardpoint++;
				if (getGamemode() != Gamemode.HARDPOINT)
					t--;
				else {
					if (hardpointFlag == null || Math.abs(hardpointFlag.getCaptureProgress()) != 10)
						t--;
					else
						if (hardpointFlag.getCaptureProgress() == -10)
							addRedPoint();
						else
							addBluePoint();
				}

				String counter = getFancyTime(t);

				if (getGamemode() == Gamemode.DOM) {
					game.checkDomFlags(t);
				}

				if (getGamemode() == Gamemode.HARDPOINT) {
					game.checkHardpointFlag();
				}

				if (getGamemode() == Gamemode.CTF) {
					if (redFlag != null)
						redFlag.checkNearbyPlayers();
					if (blueFlag != null)
						blueFlag.checkNearbyPlayers();
				}

				if (getGamemode() == Gamemode.INFECT) {
					blueTeamScore = blueTeam.size();
					redTeamScore = redTeam.size();
				}

				if (currentMap.getGamemode() != Gamemode.FFA && currentMap.getGamemode() != Gamemode.OITC && currentMap.getGamemode() != Gamemode.GUN) {
					try {
						//scoreBar.setTitle(ChatColor.RED + "RED: " + redTeamScore + ChatColor.GRAY + " «" + ChatColor.WHITE + counter + ChatColor.RESET + ChatColor.GRAY + "»" + ChatColor.BLUE + " BLU: " + blueTeamScore);
						scoreBar.getClass().getMethod("setTitle", String.class).invoke(scoreBar, ChatColor.RED + "RED: " + redTeamScore + ChatColor.GRAY + " «" + ChatColor.WHITE + counter + ChatColor.RESET + ChatColor.GRAY + "»" + ChatColor.BLUE + " BLU: " + blueTeamScore);
					} catch(NoClassDefFoundError e) {
						System.out.println();
					} catch(Exception ignored) {}
				} else {

					Player highestScorer = Bukkit.getPlayer(getWinningTeam());

					for (Player p : players) {
						if (highestScorer == null) {
							highestScorer = p;
						}

						if (!ffaPlayerScores.containsKey(p)) {
							if (getGamemode() != Gamemode.OITC) {
								ffaPlayerScores.put(p, 0);
							} else {
								ffaPlayerScores.put(p, maxScore_OITC);
							}
						}

						if (!ffaPlayerScores.containsKey(highestScorer)) {
							ffaPlayerScores.put(highestScorer, 0);
						}

						if (highestScorer == p) {
							if (getPlayers().size() > 1) {
								TreeMap<Integer, Player> scores = new TreeMap<>();
								for (Player pl : ffaPlayerScores.keySet()) {
									if (pl == highestScorer)
										continue;
									scores.put(ffaPlayerScores.get(pl), pl);
								}

								highestScorer = scores.lastEntry().getValue();
							}
						}


						double progress = (((double) t) / ((double) gameTime));
						try {
							freeForAllBar.get(p).getClass().getMethod("setTitle", String.class).invoke(freeForAllBar.get(p), ChatColor.GREEN + p.getDisplayName() + ": " + ffaPlayerScores.get(p) + ChatColor.GRAY + " «" + ChatColor.WHITE + counter + ChatColor.RESET + ChatColor.GRAY + "»" + " " + ChatColor.GOLD + highestScorer.getDisplayName() + ": " + ffaPlayerScores.get(highestScorer));
							freeForAllBar.get(p).getClass().getMethod("setProgress", Double.class).invoke(freeForAllBar.get(p), progress);
						} catch(NoClassDefFoundError e) {
							System.out.println();
						}catch(Exception ignored) {}
					}
				}

				double progress = (((double) t) / ((double) gameTime));

				try {
					scoreBar.getClass().getMethod("setProgress", Double.class).invoke(scoreBar, progress);
				} catch(Exception ignored) {}
				game.updateTabList();

				for (Player p : getPlayers()) {
					getScoreboardManager().updateGameScoreBoard(p, getFancyTime(t));
				}

				if (currentMap.getGamemode() == Gamemode.TDM || currentMap.getGamemode() == Gamemode.RSB || currentMap.getGamemode() == Gamemode.DOM || currentMap.getGamemode() == Gamemode.CTF || currentMap.getGamemode() == Gamemode.KC || currentMap.getGamemode() == Gamemode.HARDPOINT) {
					if ((blueTeamScore >= maxScore_TDM || redTeamScore >= maxScore_TDM) && getGamemode().equals(Gamemode.TDM)) {
						endGameByScore(this);
						return;
					} else if ((blueTeamScore >= maxScore_RSB || redTeamScore >= maxScore_RSB) && getGamemode().equals(Gamemode.RSB)) {
						endGameByScore(this);
						return;
					} else if ((blueTeamScore >= maxScore_DOM || redTeamScore >= maxScore_DOM) && getGamemode().equals(Gamemode.DOM)) {
						endGameByScore(this);
						return;
					} else if ((blueTeamScore >= maxScore_CTF || redTeamScore >= maxScore_CTF) && getGamemode().equals(Gamemode.CTF)) {
						endGameByScore(this);
						return;
					} else if ((blueTeamScore >= maxScore_KC || redTeamScore >= maxScore_KC) && getGamemode().equals(Gamemode.KC)) {
						endGameByScore(this);
						return;
					} else if ((blueTeamScore >= maxScore_HARDPOINT || redTeamScore >= maxScore_HARDPOINT) && getGamemode().equals(Gamemode.HARDPOINT)) {
						endGameByScore(this);
						return;
					}
				}

				if (getGamemode() == Gamemode.RESCUE || getGamemode() == Gamemode.GUNFIGHT) {
					if (getAlivePlayers(redTeam) == 0) {
						addBluePoint();

							if (getGamemode() == Gamemode.RESCUE) {
								if (!(blueTeamScore >= maxScore_RESCUE))
									startNewRound(7, blueTeam);
							}
							else if (getGamemode() == Gamemode.GUNFIGHT) {
								if (!(blueTeamScore >= maxScore_GUNFIGHT))
									startNewRound(7, blueTeam);
							}

						for (Player pp : players) {
							isAlive.put(pp, true);
						}
						cancel();
					} else if (getAlivePlayers(blueTeam) == 0) {
						addRedPoint();

						if (getGamemode() == Gamemode.RESCUE) {
							if (!(redTeamScore >= maxScore_RESCUE))
								startNewRound(7, redTeam);
						} else if (getGamemode() == Gamemode.GUNFIGHT) {
							if (!(redTeamScore >= maxScore_GUNFIGHT))
								startNewRound(7, redTeam);
						}

						for (Player pp : players) {
							isAlive.put(pp, true);
						}
						cancel();
					}

					if (getGamemode() == Gamemode.RESCUE) {
						if (blueTeamScore >= maxScore_RESCUE || redTeamScore >= maxScore_RESCUE && getGamemode() == Gamemode.RESCUE) {
							endGameByScore(this);
							return;
						}
					} else if (getGamemode() == Gamemode.GUNFIGHT) {
						if (blueTeamScore >= maxScore_GUNFIGHT || redTeamScore >= maxScore_GUNFIGHT && getGamemode() == Gamemode.GUNFIGHT) {
							endGameByScore(this);
							return;
						}
					}
				}

				if (getGamemode() == Gamemode.INFECT) {
					if (blueTeamScore == 0 && t < time - 5) {
						endGameByScore(this);
						return;
					}
				}

				if (getGamemode().equals(Gamemode.FFA)) {
					for (Player p : players) {
						if (ffaPlayerScores.get(p) >= maxScore_FFA) {
							endGameByScore(this);
							return;
						}
					}
				}

				if(getGamemode().equals(Gamemode.OITC)) {
					for(Player p : getPlayers()) {
						boolean lastManStanding = true;
						for(Player other : getPlayers()) {
							if (other.equals(p))
								continue;

							if (ffaPlayerScores.get(other) > 0)
								lastManStanding = false;
						}

						if(lastManStanding) {
							endGameByScore(this);
							return;
						}
					}
				}

				if (getGamemode().equals(Gamemode.GUN)) {
					for (Player p : players) {
						if (ffaPlayerScores.get(p) >= maxScore_GUN) {
							endGameByScore(this);
							return;
						}
					}
				}

			}

		};
		getRunnables().add(br);
		br.runTaskTimer(ComWarfare.getPlugin(), 0L, 20L);
	}

	private void startNewRound(int delay, List<Player> prevRWT) {
		for(Player p : players) {

			ChatColor tColor = ChatColor.GRAY;
			String team = "Nobody";

			if (prevRWT != null && !prevRWT.isEmpty()) {
				if (prevRWT.equals(blueTeam)) {
					tColor = ChatColor.BLUE;
					team = "BLUE";
				} else {
					tColor = ChatColor.RED;
					team = "RED";
				}
			}

			if (prevRWT != null) {
				if (prevRWT.equals(blueTeam) && isOnBlueTeam(p))
					Bukkit.getPluginManager().callEvent(new RoundEndSoundEvent(p, true));
				else if (prevRWT.equals(redTeam) && isOnRedTeam(p))
					Bukkit.getPluginManager().callEvent(new RoundEndSoundEvent(p, true));
				else
					Bukkit.getPluginManager().callEvent(new RoundEndSoundEvent(p, false));
			}

			ComWarfare.sendTitle(p, Lang.TEAM_WON_ROUND.getMessage().replace("{team-color}", tColor + "").replace("{team}", team), Lang.NEXT_ROUND_STARTING.getMessage().replace("{time}", delay + ""), tColor);

		}

		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				if (cancelIfNotActive(this))
					return;
				startGameTimer(gameTime, true);
			}
		};

		getRunnables().add(br);
		br.runTaskLater(ComWarfare.getPlugin(), 20L * (long) delay);
	}

	private void endGameByScore(BukkitRunnable runnable) {
		stopGame();
		getRunnables().remove(runnable);
		runnable.cancel();
	}

	@Deprecated
	public void resetScoreBoard() {
		if (getGamemode() != Gamemode.FFA && getGamemode() != Gamemode.GUN && getGamemode() != Gamemode.OITC) {
			try {
				scoreBar = Bukkit.createBossBar(Color.RED + "RED: 0" + "     " + "«" + getFancyTime(gameTime) + "»" + "     " + Color.BLUE + "BLUE: 0", org.bukkit.boss.BarColor.GREEN, org.bukkit.boss.BarStyle.SEGMENTED_10);
			} catch(NoClassDefFoundError e) {
				System.out.println();
			}catch(Exception ignored) {}
		} else {
			try {
				scoreBar = Bukkit.createBossBar(Color.RED + "YOU: 0" + "     " + "«" + getFancyTime(gameTime) + "»" + "     " + Color.BLUE + "1ST: 0", org.bukkit.boss.BarColor.GREEN, org.bukkit.boss.BarStyle.SEGMENTED_10);
			} catch(NoClassDefFoundError e) {
				System.out.println();
			}catch(Exception ignored) {}
		}
	}

	/**
	 * @return Gets the winning team as a string.
	 * */
	private String getWinningTeam() {

		if (getGamemode().equals(Gamemode.FFA) || getGamemode().equals(Gamemode.OITC) || getGamemode().equals(Gamemode.GUN)) {

			if (pinkNukeActive != null)
				return pinkNukeActive.getDisplayName();

			int highestScore = 0;
			Player highestScoringPlayer = null;
			for (Player p : ffaPlayerScores.keySet()) {
				if (ffaPlayerScores.get(p) > highestScore) {
					highestScore = ffaPlayerScores.get(p);
					highestScoringPlayer = p;
				}
			}

			if (highestScoringPlayer == null) {
				return "nobody";
			}

			return highestScoringPlayer.getDisplayName();
		}

		if (getGamemode() == Gamemode.INFECT) {
			return blueTeamScore > 0 ? "blue" : "red";
		}

		if (redNukeActive)
			return "red";
		else if (blueNukeActive)
			return "blue";

		if (redTeamScore > blueTeamScore) {
			return "red";
		} else if (blueTeamScore > redTeamScore) {
			return "blue";
		}

		return "tie";
	}

	/**
	 * @param time Time in seconds.
	 * @return Formatted time in minutes and seconds.
	 * */
	private String getFancyTime(int time) {

		String seconds = Integer.toString(time % 60);

		if (seconds.length() == 1) {
			seconds = "0" + seconds;
		}

		String minutes = Integer.toString(time / 60);

		if (minutes.length() == 1) {
			minutes = "0" + minutes;
		}

		return (minutes + ":" + seconds);
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}


	private boolean areEnemies(Player a, Player b) {

		if (a == null || b == null) {
			return true;
		}

		if (redTeam.contains(a) && redTeam.contains(b)) {
			return false;
		} else if (blueTeam.contains(a) && blueTeam.contains(b)) {
			return false;
		}

		return true;
	}

	private HashMap<Player, Boolean> isAlive = new HashMap<>();
	private int getAlivePlayers(ArrayList<Player> team) {
		int count = 0;

		if (getGamemode() != Gamemode.RESCUE && getGamemode() != Gamemode.GUNFIGHT)
			return 1;

		for (Player p : team) {
			if (isAlive.get(p)) {
				count++;
			}
		}

		return count;
	}


	/**
	 * Kills the target player within the modified health system.
	 *
	 * @param p The player to kill
	 * @param killer The player who killed the player
	 * */
	public void kill(Player p, Player killer) {

		Bukkit.getPluginManager().callEvent(new PlayerDieSoundEvent(p));

		AssignmentManager.getInstance().updateAssignments(p, 1, getGamemode());

		if (getGamemode() == Gamemode.RESCUE || getGamemode() == Gamemode.GUNFIGHT) {
			p.setGameMode(GameMode.SPECTATOR);
			p.getInventory().clear();
			isAlive.put(p, false);

			if (getGamemode() == Gamemode.RESCUE) {
				if (isOnBlueTeam(p) && getAlivePlayers(blueTeam) > 0) {
					ComWarfare.sendTitle(p, Lang.RESPAWN_IF_DOG_TAG_PICKED_UP.getMessage(), "");
					dropDogTag(p);
				} else if (isOnRedTeam(p) && getAlivePlayers(redTeam) > 0) {
					ComWarfare.sendTitle(p, Lang.RESPAWN_IF_DOG_TAG_PICKED_UP.getMessage(), "");
					dropDogTag(p);
				}
			} else {
				ComWarfare.sendTitle(p, Lang.RESPAWN_NEXT_ROUND.getMessage(), "");
			}

			return;
		}


		if (getGamemode() == Gamemode.KC) {
			dropDogTag(p);
		}

		if (getGamemode() == Gamemode.INFECT && redTeam.contains(killer)) {
			blueTeam.remove(p);

			redTeam.add(p);

			if (getGamemode().equals(Gamemode.INFECT)) {
				blueTeamScore = blueTeam.size();
				redTeamScore = redTeam.size();
			}
		}

		if (getGamemode() == Gamemode.OITC) {
			if (ffaPlayerScores.get(p) == 0) {
				ComWarfare.sendMessage(p, Lang.OITC_RAN_OUT_OF_LIVES.getMessage());
				p.setGameMode(GameMode.SPECTATOR);
				p.getInventory().clear();
				return;
			}
		}

		BukkitRunnable br = new BukkitRunnable() {
			int t = 3;

			public void run() {
				if (cancelIfNotActive(this))
					return;

				p.getInventory().clear();
				p.removePotionEffect(PotionEffectType.SPEED);

				if (t > 0) {

					p.getInventory().clear();
					p.setGameMode(GameMode.SPECTATOR);
					p.setSpectatorTarget(killer);

					if (t == 3)
						ComWarfare.sendTitle(p, Lang.YOU_WILL_RESPAWN.getMessage().replace("{time}", t + ""), "");
				} else {
					if (getState() == GameState.IN_GAME) {
						if (getGamemode() != Gamemode.FFA && getGamemode() != Gamemode.OITC && getGamemode() != Gamemode.GUN) {
							if (blueTeam.contains(p)) {
								spawnCodPlayer(p, getMap().getBlueSpawn());
							} else if (redTeam.contains(p)) {
								spawnCodPlayer(p, getMap().getRedSpawn());
							} else {
								assignTeams();
							}

							getRunnables().remove(this);
							cancel();
							return;
						} else {
							spawnCodPlayer(p, getMap().getPinkSpawn());
							getRunnables().remove(this);
							cancel();
							return;
						}
					} else {
						p.setGameMode(GameMode.ADVENTURE);
						p.teleport(ComWarfare.getLobbyLocation());
						p.setHealth(20D);
						p.setFoodLevel(20);
						getRunnables().remove(this);
						cancel();
						return;
					}
				}

				t--;
			}
		};

		getRunnables().add(br);
		br.runTaskTimer(ComWarfare.getPlugin(), 0L, 20L);
	}

	private void updateTabList() {

		String teamColor;

		for (Player p : players) {

			if (isOnRedTeam(p)) {
				teamColor = ChatColor.RED + "";
			} else if (isOnBlueTeam(p)) {
				teamColor = ChatColor.BLUE + "";
			} else {
				teamColor = ChatColor.LIGHT_PURPLE + "";
			}

			CodScore score = playerScores.get(p);

			try {
				p.getClass().getMethod("setPlayerListHeader", String.class).invoke(p, ComWarfare.getHeader());
				p.getClass().getMethod("setPlayerListFooter", String.class).invoke(p, ChatColor.WHITE + "Playing " + ChatColor.GOLD + getMap().getGamemode().toString() + ChatColor.WHITE + " on " + ChatColor.GOLD + getMap().getName() + ChatColor.WHITE + "!");
			} catch(NoSuchMethodException ig) {} catch(Exception ignored) {}

			int level = ProgressionManager.getInstance().getLevel(p);
			String prestige = ProgressionManager.getInstance().getPrestigeLevel(p) > 0 ? ChatColor.WHITE + "[" + ChatColor.GREEN + ProgressionManager.getInstance().getPrestigeLevel(p) + ChatColor.WHITE + "]-" : "";
			String levelName = LevelNames.getInstance().getLevelName(level);
			levelName = !levelName.equals("") ? "[" + levelName + "] " : "";
			p.setPlayerListName(ChatColor.WHITE + levelName + prestige + "[" +
					level + "] " + teamColor + p.getDisplayName() + ChatColor.WHITE + " [K] " +
					ChatColor.GREEN + score.getKills() + ChatColor.WHITE + " [D] " + ChatColor.GREEN + score.getDeaths() +
					ChatColor.WHITE + " [S] " + ChatColor.GREEN + score.getKillstreak());
		}
	}

	public boolean isOnRedTeam(Player p) {
		return redTeam.contains(p);
	}

	public boolean isOnBlueTeam(Player p) {
		return blueTeam.contains(p);
	}

	public boolean isOnPinkTeam(Player p) {
		return ffaPlayerScores.containsKey(p);

	}

	public CodMap getMap() {

		if (currentMap == null) {
			CodMap map = GameManager.pickRandomMap();
			GameManager.usedMaps.add(map);
			changeMap(map);
		}

		return currentMap;
	}

	public boolean forceStart(boolean forceStarted) {
		this.forceStarted = forceStarted;
		return forceStarted;
	}

	public CodScore getScore(Player p) {
		if (!playerScores.containsKey(p)) {
			playerScores.put(p, new CodScore(p));
		}

		return playerScores.get(p);
	}

	public GameState getState() {
		return state;
	}

	private void setState(GameState state) {
		this.state = state;
	}

	public Gamemode getGamemode() {
		return getMap().getGamemode();
	}

	public boolean isPastClassChange() {
		return pastClassChange;
	}

	public void changeClass(Player p) {
		if (!isPastClassChange()) {
			if (isOnBlueTeam(p))
				spawnCodPlayer(p, getMap().getBlueSpawn());
			else if (isOnRedTeam(p))
				spawnCodPlayer(p, getMap().getRedSpawn());
			else
				spawnCodPlayer(p, getMap().getPinkSpawn());
		}
	}

	/**
	 * Gets the closest objective for the player.
	 * */
	@Nullable
	private Location getClosestObjective(Player p) {
		if (getGamemode() == Gamemode.DOM) {
			if (aFlag == null || bFlag == null || cFlag == null)
				return null;

			DomFlag closest = aFlag;
			if (p.getLocation().distanceSquared(bFlag.getLocation()) < p.getLocation().distanceSquared(closest.getLocation()))
				closest = bFlag;
			if (p.getLocation().distanceSquared(cFlag.getLocation()) < p.getLocation().distanceSquared(closest.getLocation()))
				closest = cFlag;

			return closest.getLocation();
		}

		if (getGamemode() == Gamemode.CTF)
		{
			if (redFlag == null || blueFlag == null)
				return null;

			CtfFlag a, b;
			if (isOnBlueTeam(p)) {
				a = redFlag;
				b = blueFlag;
			} else {
				a = blueFlag;
				b = redFlag;
			}

			Location closest = a.getPosition();
			if (!b.isInFlagHolder())
				if (b.getPosition().distanceSquared(p.getLocation()) < closest.distanceSquared(p.getLocation()))
					closest = b.getPosition();

			return closest;
		}

		else if (getGamemode() == Gamemode.HARDPOINT) {
			if (hardpointFlag == null)
				return null;

			return hardpointFlag.getLocation();
		}

		return null;
	}

	/**
	 * Handles things that should happen on death for the given player and victim.
	 * */
	private void handleDeath(Player killer, Player victim) {

		RankPerks rank = ComWarfare.getRank(killer);

		KillFeedEvent kfe = new KillFeedEvent(this, victim, killer);
		Bukkit.getServer().getPluginManager().callEvent(kfe);

		if (getGamemode().equals(Gamemode.TDM) || getGamemode().equals(Gamemode.KC) || getGamemode().equals(Gamemode.RSB) || getGamemode().equals(Gamemode.DOM) || getGamemode().equals(Gamemode.RESCUE) || getGamemode().equals(Gamemode.GUNFIGHT) || getGamemode().equals(Gamemode.HARDPOINT)) {
			double xp = rank.getKillExperience();

			if (getGamemode().equals(Gamemode.KC)) {
				xp /= 2d;
			}

//			ComWarfare.sendMessage(killer,  "" + ChatColor.BLUE + ChatColor.BOLD + "YOU " + ChatColor.RESET + ChatColor.WHITE + "[" + Lang.KILLED_TEXT.getMessage() + "] " + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + victim.getDisplayName(), ComWarfare.getLang());

			ComWarfare.sendActionBar(killer, ChatColor.YELLOW + "+" + xp + "xp");
			ProgressionManager.getInstance().addExperience(killer, xp);
			CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
			kill(victim, killer);

			if (isOnRedTeam(killer)) {
				if (getGamemode() != Gamemode.RESCUE && getGamemode() != Gamemode.KC && getGamemode() != Gamemode.GUNFIGHT) {
					if (getGamemode() != Gamemode.HARDPOINT) {
						addRedPoint();
					} else if (hardpointController == 1) {
						addRedPoint();
					}
				}
				updateScores(victim, killer, rank);
			} else if (isOnBlueTeam(killer)) {
				if (getGamemode() != Gamemode.RESCUE && getGamemode() != Gamemode.KC && getGamemode() != Gamemode.GUNFIGHT) {
					if (getGamemode() != Gamemode.HARDPOINT) {
						addBluePoint();
					} else if (hardpointController == 0) {
						addBluePoint();
					}
				}
				updateScores(victim, killer, rank);
			}

			if(getGamemode() != Gamemode.GUN && getGamemode() != Gamemode.OITC && getGamemode() != Gamemode.RSB && (getGamemode() != Gamemode.INFECT || isOnBlueTeam(killer))) {
				Entity bag = PerkListener.getInstance().scavengerDeath(victim, killer);
				if (bag != null)
					entityManager.registerEntity(bag);
			}

		} else if (getGamemode().equals(Gamemode.CTF) || getGamemode().equals(Gamemode.INFECT)) {
			if (redTeam.contains(killer)) {
//				ComWarfare.sendMessage(killer, "" + ChatColor.RED + ChatColor.BOLD + "YOU " + ChatColor.RESET + "" + ChatColor.WHITE + "[" + Lang.KILLED_TEXT.getMessage() + "] " + ChatColor.RESET + ChatColor.BLUE + ChatColor.BOLD + victim.getDisplayName(), ComWarfare.getLang());
				ComWarfare.sendActionBar(killer, ChatColor.YELLOW + "+" + rank.getKillExperience() + "xp");

				ProgressionManager.getInstance().addExperience(killer, rank.getKillExperience());
				CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
				kill(victim, killer);
				updateScores(victim, killer, rank);
			} else if (blueTeam.contains(killer)) {
//				ComWarfare.sendMessage(killer,  "" + ChatColor.BLUE + ChatColor.BOLD + "YOU " + ChatColor.RESET + ChatColor.WHITE + "[" + Lang.KILLED_TEXT.getMessage() + "] " + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + victim.getDisplayName(), ComWarfare.getLang());
				ComWarfare.sendActionBar(killer,  ChatColor.YELLOW + "+" + rank.getKillExperience() + "xp");
				CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
				ProgressionManager.getInstance().addExperience(killer, rank.getKillExperience());
				kill(victim, killer);
				updateScores(victim, killer, rank);
			}

			if (getGamemode() == Gamemode.CTF) {
				if (victim.equals(redFlag.getFlagHolder())) {
					redFlag.drop(victim);
				} else if (victim.equals(blueFlag.getFlagHolder())) {
					blueFlag.drop(victim);
				}
			}

		} else if (getGamemode().equals(Gamemode.FFA) || getGamemode().equals(Gamemode.GUN) || getGamemode().equals(Gamemode.OITC)) {
//			ComWarfare.sendMessage(killer, "" + ChatColor.GREEN + ChatColor.BOLD + "YOU " + ChatColor.RESET + ChatColor.WHITE + "[" + Lang.KILLED_TEXT.getMessage() + "] " + ChatColor.RESET	 + ChatColor.GOLD + ChatColor.BOLD + victim.getDisplayName(), ComWarfare.getLang());
			ComWarfare.sendActionBar(killer, ChatColor.YELLOW + "+" + rank.getKillExperience() + "xp");
			ProgressionManager.getInstance().addExperience(killer, rank.getKillExperience());
			CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
			kill(victim, killer);
			if (getGamemode() == Gamemode.OITC) {
				removePointForPlayer(victim);
				ItemStack ammo = GameManager.oitcGun.getAmmo();
				ammo.setAmount(1);
				if (killer.getInventory().getItem(8) != null && killer.getInventory().getItem(8).getType() == ammo.getType()) {
					killer.getInventory().addItem(ammo);
				} else {
					killer.getInventory().setItem(8, ammo);
				}
			} else {
				addPointForPlayer(killer);
			}



			if (getGamemode() == Gamemode.GUN) {

				ItemStack held;

				try {
					held = (ItemStack) killer.getInventory().getClass().getMethod("getItemInMainHand").invoke(killer.getInventory());
				} catch(NoSuchMethodException e) {
					held = killer.getInventory().getItemInHand();
				} catch(Exception e) {
					held = killer.getInventory().getItemInHand();
				}

				if (held.equals(LoadoutManager.getInstance().knife)) {
					removePointForPlayer(victim);
				}

				killer.getInventory().clear();
				setTeamArmor(killer);
				killer.getInventory().setItem(32, InventoryManager.getInstance().selectClass);
				killer.getInventory().setItem(35, InventoryManager.getInstance().leaveItem);

				KillStreakManager.getInstance().streaksAfterDeath(killer);
				killer.getInventory().setItem(0, LoadoutManager.getInstance().knife);
				CodGun gun;
				try {
					gun = GameManager.gunGameGuns.get(ffaPlayerScores.get(killer));
					ItemStack gunItem = gun.getGunItem();
					ItemStack ammo = gun.getAmmo();
					ammo.setAmount(gun.getAmmoCount());

					killer.getInventory().setItem(1, gunItem);
					killer.getInventory().setItem(19, ammo);
					killer.updateInventory();
				} catch(Exception ignored) {
					killer.getInventory().clear();
				}
			}

			updateScores(victim, killer, rank);
		}
	}

	private void updateScores(Player victim, Player killer, RankPerks rank) {

		playerScores.computeIfAbsent(killer, k -> new CodScore(killer));

		CodScore killerScore = playerScores.get(killer);

		if (!killer.equals(victim)) {
			killerScore.addScore(rank.getKillExperience());
			killerScore.addKillstreak();
			if (getGamemode() != Gamemode.OITC && getGamemode() != Gamemode.GUN)
				KillStreakManager.getInstance().checkStreaks(killer);

			killerScore.addKill();

			playerScores.put(killer, killerScore);

			if (playerScores.get(victim) == null) {
				playerScores.put(killer, new CodScore(victim));
			}
		}

		CodScore victimScore = playerScores.get(victim);

		victimScore.setDeaths(victimScore.getDeaths() + 1);
		StatHandler.addDeath(victim);

		victimScore.resetKillstreak();

		playerScores.put(victim, victimScore);
	}

	/* Gamemode Listeners */

	/**
	 * Melee hit listener.
	 * */
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerHit(EntityDamageByEntityEvent e) {
		if (e.isCancelled())
			return;

		if (!(e.getEntity() instanceof Player && e.getDamager() instanceof Player))
			return;

		Player victim = (Player) e.getEntity();
		Player attacker = (Player) e.getDamager();

		if (victim.equals(attacker)) {
			e.setCancelled(true);
			return;
		}

		if (!canDamage(attacker, victim)) {
			if (!areEnemies(attacker, victim) && getPlayers().contains(attacker) && getPlayers().contains(victim))
				e.setCancelled(true);

			return;
		}

		e.setCancelled(true);

		if (isInvulnerable(victim))
			return;

		double damage;

		ItemStack heldWeapon;

		try {
			heldWeapon = (ItemStack) attacker.getInventory().getClass().getMethod("getItemInMainHand").invoke(attacker.getInventory());
		} catch(Exception|Error e1) {
			heldWeapon = attacker.getInventory().getItemInHand();
		}

		Material gSwordMat;
		Material wSwordMat;

		try {
			gSwordMat = Material.valueOf("GOLDEN_SWORD");
		} catch(Exception silent) {
			gSwordMat = Material.valueOf("GOLD_SWORD");
		}

		try {
			wSwordMat = Material.valueOf("WOODEN_SWORD");
		} catch(Exception silent) {
			wSwordMat = Material.valueOf("WOOD_SWORD");
		}

		if (heldWeapon.getType() == Material.DIAMOND_SWORD || heldWeapon.getType() == gSwordMat || heldWeapon.getType() == Material.IRON_SWORD || heldWeapon.getType() == Material.STONE_SWORD || heldWeapon.getType() == wSwordMat)
			damage = ComWarfare.getInstance().knifeDamage;
		else
			return;


		if (getGamemode() != Gamemode.GUN && getGamemode() != Gamemode.OITC && getGamemode() != Gamemode.RSB && getGamemode() != Gamemode.GUNFIGHT && (getGamemode() != Gamemode.INFECT || isOnBlueTeam(attacker))) {
			if (LoadoutManager.getInstance().getActiveLoadout(attacker).hasPerk(Perk.COMMANDO))
				damage = 10 * ComWarfare.getDefaultHealth();
		}

		if (damage != 0)
			damagePlayer(victim, damage, attacker);
	}

	public void onPlayerInteractWithWolf(PlayerInteractEntityEvent e) {
		if (!(e.getRightClicked() instanceof Wolf))
			return;

		if (!getPlayers().contains(e.getPlayer()))
			return;

		e.setCancelled(true);
	}

	@EventHandler
	public void preventInventoryMovement(InventoryClickEvent e) {

		Player p = (Player) e.getWhoClicked();

		if (getPlayers().contains(p)) {
			if (e.getCurrentItem() != null && e.getCurrentItem().equals(InventoryManager.getInstance().selectClass)) {
				InventoryManager.getInstance().openSelectClassInventory(p);
			} else if (e.getCurrentItem() != null && e.getCurrentItem().equals(InventoryManager.getInstance().leaveItem)) {
				GameManager.leaveMatch(p);
			}
			e.setCancelled(true);
		}

	}

	/**
	 * Player hits dog [ :( ] listener.
	 * */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerHitWolf(EntityDamageByEntityEvent e) {

		if (e.isCancelled())
			return;

		if (!(e.getDamager() instanceof Player || e.getDamager() instanceof Projectile))
			return;

		if (!(e.getEntity() instanceof Wolf))
			return;

		Player damager;

		if (e.getDamager() instanceof Player) {
			damager = (Player) e.getDamager();
		} else {
			if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
				damager = (Player) ((Projectile)e.getDamager()).getShooter();
			} else {
				return;
			}
		}

		if (!players.contains(damager))
			return;

		e.setCancelled(true);

		double scalar = (20d / ComWarfare.getDefaultHealth()) * 0.4d;
		double damage = e.getDamage() * scalar;
		damage /= 2;

		for (Player p : dogsScoreStreak.keySet()) {
			if (p.equals(damager)) {
				continue;
			}
			for (Wolf w : dogsScoreStreak.get(p)) {
				if (w.equals(e.getEntity())) {
					if (w.getHealth() - damage <= 0d) {
						e.getEntity().remove();
						e.setCancelled(true);
					} else {
						w.setHealth(w.getHealth() - damage);
						e.setCancelled(true);
					}
				}
			}

		}
	}

	/**
	 * Listener for when a player hits another player using a ranged weapon.
	 * */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerHitByWeapon(EntityDamageByEntityEvent e) {

		if (e.isCancelled())
			return;

		Projectile bullet;

		if (e.getDamager() instanceof Projectile) {
			bullet = (Projectile) e.getDamager();
			if (!(bullet.getShooter() instanceof Player)) {
				return;
			}
		} else {
			return;
		}

		if (!(e.getEntity() instanceof Player))
			return;

		Player victim = (Player) e.getEntity();
		Player shooter = (Player) bullet.getShooter();

		if (!canDamage(shooter, victim))
			return;

		e.setCancelled(true);

		if (isInvulnerable(victim))
			return;

		double damage = e.getDamage();

		damagePlayer(victim, damage, shooter);
	}

	/**
	 * @param a The damager
	 * @param b The victim
	 * @return Returns if person a can damage person b.
	 * */
	public boolean canDamage(Player a, Player b) {
		if (!players.contains(b) && !players.contains(a))
			return false;

		if (getState() != GameState.IN_GAME)
			return false;

		if (!areEnemies(a, b))
			return false;

		if (health.isDead(b))
			return false;
		return true;
	}

	public boolean isInvulnerable(Player p) {
		return p.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
	}

	/**
	 * Damages the victim for the given damage.
	 * */
	public void damagePlayer(Player victim, double damage, Player... damagers) {
		if (health.isDead(victim))
			return;


		if (getState() != GameState.IN_GAME) {
			return;
		}

		if (getGamemode() == Gamemode.OITC) {
			damage = health.defaultHealth * 2;
		} else if (getGamemode() != Gamemode.GUN && getGamemode() != Gamemode.RSB && getGamemode() != Gamemode.INFECT && getGamemode() != Gamemode.GUNFIGHT) {
			if (damagers.length != 0) {
				Player shooter = damagers[0];
				if (LoadoutManager.getInstance().getActiveLoadout(shooter).hasPerk(Perk.STOPPING_POWER)) {
					damage *= 1.2d;
				}

				if (LoadoutManager.getInstance().getActiveLoadout(victim).hasPerk(Perk.JUGGERNAUT)) {
					damage /= 1.2d;
				}
			}
		}

		if (LoadoutManager.getInstance().getActiveLoadout(victim).hasPerk(Perk.DANGER_CLOSE)) {
			double cur = health.getHealth(victim);
			if (cur == ComWarfare.getDefaultHealth() && cur - damage < 0) {
				health.setHealth(victim, 1);
				return;
			}
		}

		if (damagers.length < 1) {
			health.damage(victim, damage);
		} else {
			if (areEnemies(victim, damagers[0])) {
				if (getGamemode() == Gamemode.OITC) {
					damage = health.defaultHealth * 2;
				}

				health.damage(victim, damage);
				Bukkit.getPluginManager().callEvent(new PlayerHitmarkerSoundEvent(damagers[0]));
			}
		}

		if (health.isDead(victim)) {
			if (!LoadoutManager.getInstance().getActiveLoadout(victim).hasPerk(Perk.LAST_STAND) || !(getGamemode() != Gamemode.GUN && getGamemode() != Gamemode.GUNFIGHT && getGamemode() != Gamemode.OITC && getGamemode() != Gamemode.RSB && (getGamemode() != Gamemode.INFECT || isOnBlueTeam(victim)))) {
				if (damagers.length < 1) {
//					ComWarfare.sendMessage(victim, "" + ChatColor.GREEN + ChatColor.BOLD + "YOU " + ChatColor.RESET + "" + ChatColor.WHITE + "[" + Lang.KILLED_TEXT.getMessage() + "] " + ChatColor.RESET + ChatColor.GREEN + ChatColor.BOLD + "YOURSELF", ComWarfare.getLang());
					kill(victim, victim);
				} else {
					handleDeath(damagers[0], victim);
				}
			} else {
				if (!PerkListener.getInstance().getIsInLastStand().contains(victim)) {
					PerkListener.getInstance().getIsInLastStand().add(victim);
					PerkListener.getInstance().lastStand(victim, this);
					if (damagers.length > 0) {
						Player attacker = damagers[0];
						double xp = ComWarfare.getRank(attacker).getKillExperience() / 2f;
//						ChatColor t1 = redTeam.contains(attacker) ? ChatColor.RED : blueTeam.contains(attacker) ? ChatColor.BLUE : ChatColor.LIGHT_PURPLE;
//						ChatColor t2 = t1 == ChatColor.RED ? ChatColor.BLUE : t1 == ChatColor.BLUE ? ChatColor.RED : ChatColor.LIGHT_PURPLE;
//						ComWarfare.sendMessage(attacker, "" + t1 + ChatColor.BOLD + "YOU " + ChatColor.RESET + ChatColor.WHITE + "[" + Lang.DOWNED_TEXT.getMessage() + "] " + ChatColor.RESET + t2 + ChatColor.BOLD + victim.getDisplayName(), ComWarfare.getLang());
						ComWarfare.sendActionBar(attacker, ChatColor.YELLOW + "+" + xp + "xp");
						ProgressionManager.getInstance().addExperience(attacker, xp);
						CreditManager.setCredits(attacker, CreditManager.getCredits(attacker) + ComWarfare.getRank(attacker).getKillCredits());
					}
				} else {
					PerkListener.getInstance().getIsInLastStand().remove(victim);
					victim.setSneaking(false);
					victim.setWalkSpeed(0.2f);
					handleDeath(damagers[0], victim);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerPickupDogtag(PlayerPickupItemEvent e) {

		Player p = e.getPlayer();

		if (!GameManager.isInMatch(p))
			return;

		if (!players.contains(p))
			return;

		ItemStack stack = e.getItem().getItemStack();

		e.setCancelled(true);
		e.getItem().remove();

		if (stack.getItemMeta() == null || stack.getItemMeta().getLore() == null || stack.getItemMeta().getLore().size() == 0) {
			return;
		}

		Player tagOwner;

		try {
			tagOwner = Bukkit.getPlayer(UUID.fromString(stack.getItemMeta().getLore().get(0)));
		} catch(Exception ex) {
			return;
		}
		if (!areEnemies(p, tagOwner)) {
			if (getGamemode() == Gamemode.RESCUE) {
				if (isOnBlueTeam(tagOwner)) {
					spawnCodPlayer(tagOwner, getMap().getBlueSpawn());
				} else if (isOnRedTeam(tagOwner)) {
					spawnCodPlayer(tagOwner, getMap().getRedSpawn());
				}
			} else if (getGamemode() == Gamemode.KC) {
				ComWarfare.sendMessage(p, Lang.KILL_DENIED.getMessage());
				ComWarfare.sendActionBar(p, ChatColor.YELLOW + "+" + (ComWarfare.getRank(p).getKillExperience() / 2) + "xp!");
				ProgressionManager.getInstance().addExperience(p, ComWarfare.getRank(p).getKillExperience() / 2);
			}
		} else {
			if (getGamemode() == Gamemode.RESCUE) {
				ComWarfare.sendMessage(p, Lang.SPAWN_DENIED.getMessage().replace("{player}", tagOwner.getName()));
			} else if (getGamemode() == Gamemode.KC) {
				ComWarfare.sendMessage(p, Lang.KILL_CONFIRMED.getMessage());
				ComWarfare.sendActionBar(p,ChatColor.YELLOW + "+" + ComWarfare.getRank(p).getKillExperience() + "xp!");
				ProgressionManager.getInstance().addExperience(p, ComWarfare.getRank(p).getKillExperience());
				if (isOnRedTeam(p)) {
					addRedPoint();
				} else if (isOnBlueTeam(p)) {
					addBluePoint();
				} else {
					assignTeams();
				}
			}
			e.setCancelled(true);
			e.getItem().remove();
		}

	}

	private void spawnDomFlags() {
		if(!getGamemode().equals(Gamemode.DOM))
			return;

		despawnDomFlags();

		Location aLoc = getMap().getAFlagSpawn();
		Location bLoc = getMap().getBFlagSpawn();
		Location cLoc = getMap().getCFlagSpawn();

		if(aLoc == null || bLoc == null || cLoc == null) {
			ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "The Alpdha, Beta, or Charlie flag spawns have not been set for the current map in arena id " + getId() + ". The game will likely not work properly.", ComWarfare.getLang());
			return;
		}

		aFlag = new DomFlag(Lang.FLAG_A, aLoc);
		bFlag = new DomFlag(Lang.FLAG_B, bLoc);
		cFlag = new DomFlag(Lang.FLAG_C, cLoc);

		aFlag.spawn();
		bFlag.spawn();
		cFlag.spawn();
	}

	private void despawnDomFlags() {
		if (aFlag != null)
			aFlag.remove();

		if (bFlag != null)
			bFlag.remove();

		if (cFlag != null)
			cFlag.remove();

		aFlag = null;
		bFlag = null;
		cFlag = null;
	}

	private void despawnHardpointFlag() {
		if (hardpointFlag != null)
			hardpointFlag.remove();

		hardpointFlag = null;
	}

	private void despawnCtfFlags() {
		if (blueFlag != null)
			blueFlag.despawn();

		if (redFlag != null)
			redFlag.despawn();

		blueFlag = null;
		redFlag = null;
	}

	private void checkDomFlags(int t) {
		if (!getGamemode().equals(Gamemode.DOM))
			return;

		if (aFlag == null || bFlag == null || cFlag == null)
			return;


		int[] flags = {aFlag.checkFlag(this), bFlag.checkFlag(this), cFlag.checkFlag(this)};

		int blueFlags = 0,
				redFlags = 0;

		for (int flag : flags) {
			switch (flag) {
				case 0:
					blueFlags++;
					break;
				case 1:
					redFlags++;
					break;
			}
		}

		if (t % 4 == 0) {
			blueTeamScore += blueFlags;
			redTeamScore += redFlags;
		}
	}

	private void updateHardpointFlagLocation() {

		Location lastLoc = null;
		if (hardpointFlag != null) {
			lastLoc = hardpointFlag.getLocation();
			despawnHardpointFlag();
		}

		List<Location> locs = new ArrayList<>(getMap().getHardpointFlags());

		Collections.shuffle(locs);

		Location spawnLocation = null;

		if (locs.size() == 0) {
			ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.RED + "No hardpoint locations set up, could not move hardpoint location!", ComWarfare.getLang());
			for (Player p : getPlayers()) {
				removePlayer(p);
			}
			return;
		} else if (locs.size() == 1) {
			spawnLocation = locs.get(0);
		} else {
			if (lastLoc != null) {
				for (Location possibleLoc : locs) {
					if (possibleLoc.equals(lastLoc))
						continue;
					spawnLocation = possibleLoc;
					break;
				}
			} else {
				spawnLocation = locs.get(0);
			}
		}

		for (Player p : getPlayers()) {
			ComWarfare.sendMessage(p, Lang.HARDPOINT_FLAG_SPAWNED.getMessage());
		}

		hardpointController = -1;

		hardpointFlag = new DomFlag(Lang.FLAG_HARDPOINT, spawnLocation);

		hardpointFlag.spawn();
	}

	private void checkHardpointFlag() {
		if(hardpointFlag == null)
			hardpointController = 0;
		else
			hardpointController = hardpointFlag.checkFlag(this);
	}

	void setTeamArmor(Player p) {

		Color color;

		if (isOnBlueTeam(p)) {
			color = Color.BLUE;
		} else if (isOnRedTeam(p)) {
			if (getGamemode() == Gamemode.INFECT)
				color = Color.GREEN;
			else
				color = Color.RED;
		} else {
			color = Color.PURPLE;
		}

		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
		ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS, 1);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS, 1);

		LeatherArmorMeta hMeta = (LeatherArmorMeta) helmet.getItemMeta();
		hMeta.setColor(color);
		helmet.setItemMeta(hMeta);

		LeatherArmorMeta cMeta = (LeatherArmorMeta) chest.getItemMeta();
		cMeta.setColor(color);
		chest.setItemMeta(cMeta);

		LeatherArmorMeta lMeta = (LeatherArmorMeta) legs.getItemMeta();
		lMeta.setColor(color);
		legs.setItemMeta(lMeta);

		LeatherArmorMeta bMeta = (LeatherArmorMeta) boots.getItemMeta();
		bMeta.setColor(color);
		boots.setItemMeta(bMeta);

		p.getInventory().setHelmet(helmet);
		p.getInventory().setChestplate(chest);
		p.getInventory().setLeggings(legs);
		p.getInventory().setBoots(boots);
		p.updateInventory();
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {

		if (!players.contains(e.getPlayer()))
			return;

		Player p = e.getPlayer();

		if (p.getItemInHand().equals(KillStreak.UAV.getKillStreakItem())) {
			if (isOnBlueTeam(p)) {
				if (!blueUavActive) {
					startUav(p);
				} else {
					ComWarfare.sendMessage(p, Lang.KILLSTREAK_AIRSPACE_OCCUPIED.getMessage(), ComWarfare.getLang());
				}
			} else if (isOnRedTeam(p)) {
				if (!redUavActive) {
					startUav(p);
				} else {
					ComWarfare.sendMessage(p, Lang.KILLSTREAK_AIRSPACE_OCCUPIED.getMessage(), ComWarfare.getLang());
				}

			} else {
				startUav(p);
			}
		} else if (p.getItemInHand().equals(KillStreak.VSAT.getKillStreakItem())) {
			if (isOnBlueTeam(p)) {
				if (!blueVSATActive) {
					startVSAT(p);
				} else {
					ComWarfare.sendMessage(p, Lang.KILLSTREAK_AIRSPACE_OCCUPIED.getMessage(), ComWarfare.getLang());
				}
			} else if (isOnRedTeam(p)) {
				if (!redVSATActive) {
					startVSAT(p);
				} else {
					ComWarfare.sendMessage(p, Lang.KILLSTREAK_AIRSPACE_OCCUPIED.getMessage(), ComWarfare.getLang());
				}

			} else {
				startVSAT(p);
			}
		} else if (p.getItemInHand().equals(KillStreak.AIRSTRIKE.getKillStreakItem())) {
			callAirstrike(p);
		} else if (p.getItemInHand().equals(KillStreak.COUNTER_UAV.getKillStreakItem())) {
			startCounterUav(p);
			if (!isOnBlueTeam(p) && !isOnRedTeam(p)) {
				if (!pinkCounterUavActive) {
					startCounterUav(p);
				} else {
					ComWarfare.sendMessage(p, Lang.KILLSTREAK_AIRSPACE_OCCUPIED.getMessage(), ComWarfare.getLang());
				}
			}
		} else if (p.getItemInHand().equals(KillStreak.DOGS.getKillStreakItem())) {
			startDogs(p);
		} else if (p.getItemInHand().equals(KillStreak.NUKE.getKillStreakItem())) {
			startNuke(p);
		} else if (p.getItemInHand().equals(KillStreak.JUGGERNAUT.getKillStreakItem())) {
			startJuggernaut(p);
		}

	}

	private void startUav(Player owner) {

		if (!players.contains(owner))
			return;

		if (isOnRedTeam(owner))
			redUavActive = true;
		else if (isOnBlueTeam(owner))
			blueUavActive = true;

		owner.getInventory().remove(KillStreak.UAV.getKillStreakItem());
		KillStreakManager.getInstance().useStreak(owner, KillStreak.UAV);

		BukkitRunnable br = new BukkitRunnable() {

			int t = 10;

			@Override
			public void run() {
				if (cancelIfNotActive(this))
					return;

				t--;

				if (t < 0) {
					if (isOnRedTeam(owner))
						redUavActive = false;
					else if (isOnBlueTeam(owner))
						blueUavActive = false;
					getRunnables().remove(this);
					cancel();
					return;
				}

				if(isOnBlueTeam(owner)) {
					if (redCounterUavActive)
						return;

					//blue launched
					for (Player p : redTeam) {
						if (health.isDead(p))
							continue;

						if (ComWarfare.isLegacy()) {
							Firework fw = p.getLocation().getWorld().spawn(p.getLocation(), Firework.class);
							FireworkMeta fwm = fw.getFireworkMeta();
							fwm.addEffect(FireworkEffect.builder()
									.flicker(false)
									.trail(true)
									.with(FireworkEffect.Type.BALL)
									.withColor(Color.RED)
									.build());

							fwm.setPower(3);

							fw.setFireworkMeta(fwm);
						} else {
							if (!LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.GHOST))
								p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1));
						}
					}
				} else if(isOnRedTeam(owner)) {
					//red launched
					if (blueCounterUavActive)
						return;

					for (Player p : blueTeam) {
						if (health.isDead(p))
							continue;
						if (ComWarfare.isLegacy()) {
							Firework fw = p.getLocation().getWorld().spawn(p.getLocation(), Firework.class);
							FireworkMeta fwm = fw.getFireworkMeta();
							fwm.addEffect(FireworkEffect.builder()
									.flicker(false)
									.trail(true)
									.with(FireworkEffect.Type.BALL)
									.withColor(Color.BLUE)
									.build());

							fwm.setPower(3);

							fw.setFireworkMeta(fwm);
						} else {
							if(!LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.GHOST))
								p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1));
						}
					}
				} else {
					//pink
					if (pinkCounterUavActive)
						return;

					for (Player p : players) {
						if (p == owner)
							continue;

						if (health.isDead(p))
							continue;

						if (ComWarfare.isLegacy()) {
							Firework fw = p.getLocation().getWorld().spawn(p.getLocation(), Firework.class);
							FireworkMeta fwm = fw.getFireworkMeta();
							fwm.addEffect(FireworkEffect.builder()
									.flicker(false)
									.trail(true)
									.with(FireworkEffect.Type.BALL)
									.withColor(Color.PURPLE)
									.build());

							fwm.setPower(3);

							fw.setFireworkMeta(fwm);
						} else {
							if (!LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.GHOST))
								p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20, 1));
						}
					}
				}
			}
		};

		getRunnables().add(br);
		br.runTaskTimer(ComWarfare.getPlugin(), 3L, 60L);
	}

	private void startVSAT(Player owner) {

		if (!players.contains(owner))
			return;

		if (isOnRedTeam(owner))
			redVSATActive = true;
		else if (isOnBlueTeam(owner))
			blueVSATActive = true;

		owner.getInventory().remove(KillStreak.VSAT.getKillStreakItem());
		KillStreakManager.getInstance().useStreak(owner, KillStreak.VSAT);

		BukkitRunnable br = new BukkitRunnable() {

			int t = 15;

			@Override
			public void run() {
				if (cancelIfNotActive(this))
					return;

				t--;

				if (t < 0) {
					if (isOnRedTeam(owner))
						redVSATActive = false;
					else if (isOnBlueTeam(owner))
						blueVSATActive = false;
					getRunnables().remove(this);
					cancel();
					return;
				}

				if(isOnBlueTeam(owner)) {
					if (redCounterUavActive)
						return;

					//blue launched
					for (Player p : redTeam) {
						if (health.isDead(p))
							continue;

						if (ComWarfare.isLegacy()) {
							Firework fw = p.getLocation().getWorld().spawn(p.getLocation(), Firework.class);
							FireworkMeta fwm = fw.getFireworkMeta();
							fwm.addEffect(FireworkEffect.builder()
									.flicker(false)
									.trail(true)
									.with(FireworkEffect.Type.BALL)
									.withColor(Color.RED)
									.build());

							fwm.setPower(3);

							fw.setFireworkMeta(fwm);
						} else {
							if (!LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.GHOST))
								p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 65, 1));
						}
					}
				} else if(isOnRedTeam(owner)) {
					//red launched
					if (blueCounterUavActive)
						return;

					for (Player p : blueTeam) {
						if (health.isDead(p))
							continue;
						if (ComWarfare.isLegacy()) {
							Firework fw = p.getLocation().getWorld().spawn(p.getLocation(), Firework.class);
							FireworkMeta fwm = fw.getFireworkMeta();
							fwm.addEffect(FireworkEffect.builder()
									.flicker(false)
									.trail(true)
									.with(FireworkEffect.Type.BALL)
									.withColor(Color.BLUE)
									.build());

							fwm.setPower(3);

							fw.setFireworkMeta(fwm);
						} else {
							if(!LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.GHOST))
								p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 65, 1));
						}
					}
				} else {
					//pink
					if (pinkCounterUavActive)
						return;

					for (Player p : players) {
						if (p == owner)
							continue;

						if (health.isDead(p))
							continue;

						if (ComWarfare.isLegacy()) {
							Firework fw = p.getLocation().getWorld().spawn(p.getLocation(), Firework.class);
							FireworkMeta fwm = fw.getFireworkMeta();
							fwm.addEffect(FireworkEffect.builder()
									.flicker(false)
									.trail(true)
									.with(FireworkEffect.Type.BALL)
									.withColor(Color.PURPLE)
									.build());

							fwm.setPower(3);

							fw.setFireworkMeta(fwm);
						} else {
							if (!LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.GHOST))
								p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 65, 1));
						}
					}
				}
			}
		};

		getRunnables().add(br);
		br.runTaskTimer(ComWarfare.getPlugin(), 3L, 60L);
	}

	private void startCounterUav(Player owner) {

		if (!players.contains(owner))
			return;

		KillStreakManager.getInstance().useStreak(owner, KillStreak.COUNTER_UAV);

		owner.getInventory().remove(KillStreak.COUNTER_UAV.getKillStreakItem());

		if (isOnBlueTeam(owner)) {
			blueCounterUavActive = true;
		} else if (isOnRedTeam(owner)) {
			redCounterUavActive = true;
		} else {
			pinkCounterUavActive = true;
		}

		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				if (cancelIfNotActive(this))
					return;
				if (isOnBlueTeam(owner)) {
					blueCounterUavActive = false;
				} else if (isOnRedTeam(owner)) {
					redCounterUavActive = false;
				} else {
					pinkCounterUavActive = false;
				}
			}
		};
		getRunnables().add(br);
		br.runTaskLater(ComWarfare.getPlugin(), 20L * 20L);

	}

	public HashMap<Player, Wolf[]> dogsScoreStreak = new HashMap<>();

	private void callAirstrike(Player owner) {
		if (!players.contains(owner))
			return;

		owner.getInventory().remove(KillStreak.AIRSTRIKE.getKillStreakItem());
		KillStreakManager.getInstance().useStreak(owner, KillStreak.AIRSTRIKE);

		for (Player p : getPlayers())
			ComWarfare.sendMessage(p, Lang.AIRSTRIKE_INCOMING.getMessage());

		ArrayList<Player> targets;
		ArrayList<Player> team;
		if (isOnRedTeam(owner)) {
			targets = new ArrayList<>(blueTeam);
			team = blueTeam;
		} else if (isOnBlueTeam(owner)) {
			targets = new ArrayList<>(redTeam);
			team = redTeam;
		} else {
			targets = new ArrayList<>(getPlayers());
			team = getPlayers();
		}

		for (Player p : team) {
			if (p.getUniqueId().equals(owner.getUniqueId()) || health.isDead(p) || LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.COLD_BLOODED))
				targets.remove(p);
		}

		int targeted = (int) Math.round(Math.random() * 5);

		for (int i = 0; i < targeted; i++) {
			if (!targets.isEmpty()) {
				int index = (int) Math.round(Math.random() * (targets.size() - 1));
				Bukkit.getPluginManager().callEvent(new AirstrikeExplodeSoundEvent(targets.get(index)));
				if (!isUnderRoof(targets.get(index)))
					damagePlayer(targets.get(index), ComWarfare.getDefaultHealth() * 100, owner);
				targets.remove(index);
			}
		}
	}

	private boolean isUnderRoof(Player p) {
		for (int i = 0; i < 180; i++) {
			if (p.getEyeLocation().getBlockY() >= 180)
				return false;

			Block b = p.getEyeLocation().add(0, i, 0).getBlock();
			if (b.getType() != Material.AIR)
				return true;
		}
		return false;
	}

	private void startDogs(Player owner) {
		if (!players.contains(owner))
			return;

		KillStreakManager.getInstance().useStreak(owner, KillStreak.DOGS);

		owner.getInventory().remove(KillStreak.DOGS.getKillStreakItem());

		Wolf[] wolves = new Wolf[8];

		for (int i = 0; i < 8; i++) {
			updateDogs(owner, wolves, i);
		}

		for (int i = 0; i < wolves.length; i++) {
			Wolf wolf = wolves[i];
			setNewDogsTarget(wolf, owner);
		}

		if (dogsScoreStreak.containsKey(owner)) {
			for (Wolf w : dogsScoreStreak.get(owner)) {
				if (w != null)
					w.remove();
			}
		}

		dogsScoreStreak.put(owner, wolves);

		BukkitRunnable br = new BukkitRunnable() {
			int t = 30;

			@Override
			public void run() {
				if (cancelIfNotActive(this))
					return;
				t--;

				if (t < 0) {

					Wolf[] currentWolves = dogsScoreStreak.get(owner);
					if (Arrays.equals(currentWolves, wolves))
						dogsScoreStreak.remove(owner);

					for (Wolf w : wolves) {
						Objects.requireNonNull(w).remove();
					}
					getRunnables().remove(this);
					cancel();
					return;
				}
				for (int i = 0; i < wolves.length; i++) {
					Wolf w = wolves[i];
					if (w == null) {
						updateDogs(owner, wolves, i);
						w = wolves[i];
					}

					w.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2));

					if (w.getTarget() == null || !(w.getTarget() instanceof Player) || (w.getTarget() instanceof Player && health.isDead(((Player) w.getTarget())))) {
						setNewDogsTarget(w, owner);
					}
				}
			}
		};

		getRunnables().add(br);
		br.runTaskTimer(ComWarfare.getPlugin(), 0L, 20L);
	}

	private void setNewDogsTarget(Wolf w, Player owner) {
		List<Player> targets;
		if (isOnBlueTeam(owner)) {
			targets = redTeam;
		} else if (isOnRedTeam(owner)) {
			targets = blueTeam;
		} else {
			targets = getPlayers();
		}

		Player target;
		List<Player> team = new ArrayList<>(targets);
		for (Player p : targets)
			if (p.getUniqueId().equals(owner.getUniqueId()) || LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.COLD_BLOODED))
				team.remove(p);
		int index = (new Random()).nextInt(team.size());
		target = team.get(index);

		w.setTarget(target);
	}

	private void updateDogs(Player owner, Wolf[] wolves, int i) {
		Wolf wolf = owner.getLocation().getWorld().spawn(owner.getLocation(), Wolf.class);
		wolf.setOwner(owner);
		wolf.setAngry(true);
		DyeColor collarColor;

		if (isOnBlueTeam(owner))
			collarColor = DyeColor.BLUE;
		else if (isOnRedTeam(owner))
			collarColor = DyeColor.RED;
		else
			collarColor = DyeColor.PINK;

		wolf.setCollarColor(collarColor);
		wolf.setCanPickupItems(false);
		wolf.setCustomName(owner.getDisplayName() + "'s Dog");
		wolf.setCustomNameVisible(true);
		wolf.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 2));

		wolves[i] = wolf;
	}

	private void startNuke(Player owner) {
		if (!players.contains(owner))
			return;

		KillStreakManager.getInstance().useStreak(owner, KillStreak.NUKE);
		owner.getInventory().remove(KillStreak.NUKE.getKillStreakItem());

		if (!redNukeActive && !blueNukeActive && pinkNukeActive == null) {

			ChatColor tColor;
			String launcher = owner.getDisplayName();

			if (isOnRedTeam(owner)) {
				redNukeActive = true;
				tColor = ChatColor.RED;
			} else if (isOnBlueTeam(owner)) {
				blueNukeActive = true;
				tColor = ChatColor.BLUE;
			} else {
				tColor = ChatColor.LIGHT_PURPLE;
				pinkNukeActive = owner;
			}

			BukkitRunnable br = new BukkitRunnable() {

				int t = 10;

				@Override
				public void run() {
					if (cancelIfNotActive(this))
						return;
					t--;

					if (t < 1 || getState() != GameState.IN_GAME) {
						if (getState() == GameState.IN_GAME) {
							stopGame();
						}
						blueNukeActive = false;
						redNukeActive = false;
						pinkNukeActive = null;
						getRunnables().remove(this);
						cancel();
						return;
					}

					for (Player p : players) {
						String title = Lang.NUKE_LAUNCHED_TITLE.getMessage().replace("{team-color}", tColor + "").replace("{team}", launcher),
								subtitle = Lang.NUKE_LAUNCHED_SUBTITLE.getMessage().replace("{time}", Integer.toString(t));

						ComWarfare.sendTitle(p, title, subtitle, tColor, 1, 20, 1);
					}
				}
			};

			getRunnables().add(br);
			br.runTaskTimer(ComWarfare.getPlugin(), 0L, 20L);
		}
	}

	private void startJuggernaut(Player owner) {
		if (!players.contains(owner))
			return;

		KillStreakManager.getInstance().useStreak(owner, KillStreak.JUGGERNAUT);
		owner.getInventory().remove(KillStreak.JUGGERNAUT.getKillStreakItem());

		health.inJuggernaut.add(owner);

		health.setHealth(owner, health.defaultHealth * 5);

		ComWarfare.sendTitle(owner, Lang.JUGGERNAUT_STARTED.getMessage(), "");
	}

	private void updateTimeLeft() {
		if (getGamemode() != Gamemode.INFECT) {
			gameTime = ComWarfare.getPlugin().getConfig().getInt("gameTime." + getGamemode().toString());
		} else {
			if (ComVersion.getPurchased())
				gameTime = ComWarfare.getPlugin().getConfig().getInt("maxScore.INFECT");
			else
				gameTime = 120;
		}
	}

	private ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	void incrementScore(Player p) {
		if (isOnRedTeam(p))
			redTeamScore++;
		else if (isOnBlueTeam(p))
			blueTeamScore++;
		else
			try {
				throw new Exception("Unexpected game logic when incrementing score!");
			} catch(Exception e) {
				e.printStackTrace();
			}
	}

	void sendNextMap(Player p, int t) {
		ComWarfare.sendMessage(p, Lang.GAME_STARTING_MESSAGE.getMessage().replace("{time}", getFancyTime(t)), ComWarfare.getLang());
		ComWarfare.sendMessage(p, Lang.GAME_STARTING_MAP_MESSAGE.getMessage().replace("{map}", getMap().getName()).replace("{mode}", getMap().getGamemode().toString()), ComWarfare.getLang());
	}

	public List<BukkitRunnable> getRunnables() {
		return runnables;
	}

	public void destroy() {
		List<BukkitRunnable> r = new ArrayList<>(getRunnables());
		for (int i = r.size() - 1; i >= 0; i--)
			getRunnables().remove(i).cancel();
	}

	@EventHandler
	public void preventItemHandSwap(PlayerSwapHandItemsEvent e) {
		if (getPlayers().contains(e.getPlayer()))
			e.setCancelled(true);
	}

	@EventHandler
	public void killFeedEvent(KillFeedEvent e) {
		if (e.isCancelled())
			return;

		if (e.getInstance() != this)
			return;

		showKillFeed(e);
	}

	private void showKillFeed(KillFeedEvent e) {
		Player victim = e.getVictim(),
				killer = e.getKiller();

		ChatColor vTeam = isOnBlueTeam(victim) ? ChatColor.BLUE : isOnRedTeam(victim) ? ChatColor.RED : ChatColor.LIGHT_PURPLE,
				kTeam = isOnBlueTeam(killer) ? ChatColor.BLUE : isOnRedTeam(killer) ? ChatColor.RED : ChatColor.LIGHT_PURPLE;

		if (ComWarfare.isLegacy() || !ComWarfare.isKillFeedUseBossBar()) {
			victim.sendMessage("" + kTeam + ChatColor.BOLD + killer.getName() + ChatColor.RESET + ChatColor.WHITE + Lang.KILLED_TEXT.getMessage() + ChatColor.RESET + ChatColor.YELLOW + ChatColor.BOLD + victim.getName());
			killer.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + killer.getName() + ChatColor.RESET + ChatColor.WHITE + Lang.KILLED_TEXT.getMessage() + ChatColor.RESET + vTeam + ChatColor.BOLD + victim.getName());

			if (ComWarfare.isKillFeedShowAll()) {
				for (Player p : getPlayers()) {
					if (p.equals(victim) || p.equals(killer))
						continue;
					ComWarfare.sendMessage(p, "" + kTeam + ChatColor.BOLD + killer.getName() + ChatColor.RESET + ChatColor.WHITE + Lang.KILLED_TEXT.getMessage() + ChatColor.RESET + vTeam + ChatColor.BOLD + victim.getName());
				}
			}
		} else {
			String title = "" + kTeam + ChatColor.BOLD + killer.getName() + ChatColor.RESET + ChatColor.WHITE + Lang.KILLED_TEXT.getMessage() + ChatColor.RESET + vTeam + ChatColor.BOLD + victim.getName();
			if (title.length() > 64) {
				ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.RED + "The \"KILLED_TEXT\" value in the lang.yml is too long!");
				title = title.replaceAll(Lang.KILLED_TEXT.getMessage(), " [killed] ");
			}

			int len = 64 - title.length();

			StringBuilder titleBuilder = new StringBuilder(title);
			for (int i = 0; i < len; i++) {
				titleBuilder.insert(0, " ");
			}
			title = titleBuilder.toString();
			BossBar bar = Bukkit.createBossBar(title, BarColor.WHITE, BarStyle.SEGMENTED_20);
			for (Player p : getPlayers()) {
				if (ComWarfare.isKillFeedShowAll() || victim.equals(p) || killer.equals(p)) {
					bar.addPlayer(p);
				}
			}
			removeKillFeed(bar, 80L);
		}
	}

	private void removeKillFeed(BossBar bar, long delay) {
		if (delay == 0) {
			bar.removeAll();
			return;
		}

		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				if (cancelIfNotActive(this)) {
					bar.removeAll();
					getRunnables().remove(this);
					cancel();
					return;
				}

				bar.removeAll();
				getRunnables().remove(this);
				cancel();
			}
		};

		br.runTaskLater(ComWarfare.getPlugin(), delay); //4 seconds to go away
		getRunnables().add(br);
	}

	private boolean cancelIfNotActive(BukkitRunnable runnable) {
		if (!GameManager.getRunningGames().contains(this)) {
			getRunnables().remove(runnable);
			runnable.cancel();
			return true;
		}
		return false;
	}
}
