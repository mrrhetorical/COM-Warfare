package com.rhetorical.cod.game;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.assignments.AssignmentManager;
import com.rhetorical.cod.files.ArenasFile;
import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.inventories.ShopManager;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.progression.ProgressionManager;
import com.rhetorical.cod.weapons.CodGun;
import com.rhetorical.cod.weapons.CodWeapon;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.*;

public class GameManager {

	static ArrayList<GameInstance> runningGames = new ArrayList<>();
	static ArrayList<CodMap> addedMaps = new ArrayList<>();
	public static ArrayList<CodMap> usedMaps = new ArrayList<>();

	public static CodGun oitcGun = null;
	public static List<CodGun> gunGameGuns = new ArrayList<>();

	public static void setupOITC() {
		String oitcGunName = Main.getPlugin().getConfig().getString("OITC_Gun");
		if (oitcGunName == null)
			return;

		CodWeapon weapon = ShopManager.getInstance().getWeaponForName(oitcGunName);

		if (!(weapon instanceof CodGun))
			return;


		//OITC can now be played
		oitcGun = (CodGun) weapon;
	}

	public static void setupGunGame() {
		List<String> guns = Main.getPlugin().getConfig().getStringList("GunProgression");
		for(String g : guns) {
			if (ShopManager.getInstance().getWeaponForName(g) instanceof CodGun) {
				CodGun gun = (CodGun) ShopManager.getInstance().getWeaponForName(g);
				if (!gunGameGuns.contains(gun))
					gunGameGuns.add(gun);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void loadMaps() {
		int k = 0;
		while (ArenasFile.getData().contains("Maps." + k)) {
			CodMap m;
			String name = ArenasFile.getData().getString("Maps." + k + ".name");

			m = new CodMap(name);


			Location aFlagSpawn = (Location) ArenasFile.getData().get("Maps." + k + ".AFlag");
			if (aFlagSpawn != null)
				m.setAFlagSpawn(aFlagSpawn);

			Location bFlagSpawn = (Location) ArenasFile.getData().get("Maps." + k + ".BFlag");
			if (bFlagSpawn != null)
				m.setBFlagSpawn(bFlagSpawn);

			Location cFlagSpawn = (Location) ArenasFile.getData().get("Maps." + k + ".CFlag");
			if (cFlagSpawn != null)
				m.setCFlagSpawn(cFlagSpawn);

			List<Location> hardpointSpawns = (List<Location>) ArenasFile.getData().get("Maps." + k + ".hardpointFlags");
			if (hardpointSpawns != null)
				m.setHardpointFlags(hardpointSpawns);

			Location blueFlagSpawn = (Location) ArenasFile.getData().get("Maps." + k + ".blueFlagSpawn");
			if (blueFlagSpawn != null)
				m.setBlueFlagSpawn(blueFlagSpawn);

			ArrayList<Location> blueSpawns = (ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".blueSpawns");
			if (blueSpawns != null)
				m.setBlueSpawns(blueSpawns);

			boolean enabled = ArenasFile.getData().getBoolean("Maps." + k + ".enabled");
			m.setEnabled(enabled);

			ArrayList<Location> pinkSpawns = (ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".pinkSpawns");
			if (pinkSpawns != null)
				m.setPinkSpawns(pinkSpawns);

			Location redFlagSpawn = (Location) ArenasFile.getData().get("Maps." + k + ".redFlagSpawn");
			if (redFlagSpawn != null)
				m.setRedFlagSpawn(redFlagSpawn);

			ArrayList<Location> redSpawns = (ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".redSpawns");
			if (redSpawns != null)
				m.setRedSpawns(redSpawns);

			List<String> gamemodes = ArenasFile.getData().getStringList("Maps." + k + ".blacklist");

			for (String str : gamemodes) {
				Gamemode mode;
				try {
					mode = Gamemode.valueOf(str);
				} catch(Exception e) {
					continue;
				}
				m.addToBlacklist(mode);
			}

			m.setEnable();

			boolean contains = false;

			for (CodMap map : addedMaps) {
				contains = map.getName().equalsIgnoreCase(m.getName());
				if(contains)
					break;
			}

			if (!contains) {
				addedMaps.add(m);
			}
			k++;
		}
	}

	public static boolean findMatch(Player p) {

		loadPlayerData(p);
		
		if (Main.getLobbyLocation() == null) {
			Main.sendMessage(p, Main.getPrefix() + Lang.NO_LOBBY_SET.getMessage(), Main.getLang());
			Main.sendMessage(p, Main.getPrefix() + Lang.COULD_NOT_CREATE_MATCH_BECAUSE_NO_LOBBY.getMessage(), Main.getLang());
			return false;
		}
		

		for (GameInstance i : runningGames) {
			if (i.getPlayers().contains(p)) {
				Main.sendMessage(p, Main.getPrefix() + Lang.ALREADY_IN_GAME.getMessage(), Main.getLang());
				return false;
			}
		}

		TreeMap<Integer, GameInstance> possibleMatches = new TreeMap<>();

		GameInstance newGame;

		Main.sendMessage(p, Main.getPrefix() + Lang.SEARCHING_FOR_MATCH.getMessage(), Main.getLang());
		for (GameInstance i : runningGames) {
			if (i.getPlayers().size() < 12) {
				if (i.getPlayers().size() == 0) {
					removeInstance(i);
					continue;
				} else {
					possibleMatches.put(i.getPlayers().size(), i);
				}
				break;
			}
		}

		if (possibleMatches.size() == 0) {

			Main.sendMessage(p, Main.getPrefix() + Lang.COULD_NOT_FIND_MATCH.getMessage(), Main.getLang());
			Main.sendMessage(p, Main.getPrefix() + Lang.CREATING_MATCH.getMessage(), Main.getLang());

			CodMap map = pickRandomMap();
			if (map == null) {
				Main.sendMessage(p, Main.getPrefix() + Lang.COULD_NOT_CREATE_MATCH_BECAUSE_NO_MAPS.getMessage(), Main.getLang());
				return false;
			}

			map.changeGamemode();

			newGame = new GameInstance(new ArrayList<>(), map);

			runningGames.add(newGame);

			newGame.addPlayer(p);

			Main.sendMessage(p, Main.getPrefix() + Lang.CREATED_LOBBY.getMessage(), Main.getLang());
			return true;

		}

		Main.sendMessage(p, Main.getPrefix() + Lang.FOUND_MATCH.getMessage(), Main.getLang());
		Main.sendMessage(p, Main.getPrefix() + Lang.JOINING_GAME.getMessage(), Main.getLang());

		if (!possibleMatches.lastEntry().getValue().addPlayer(p)) {
			Main.sendMessage(p, Main.getPrefix() + Lang.COULD_NOT_JOIN_GAME.getMessage(), Main.getLang());
		}


		return true;

		// Found match!
	}

	public static boolean joinGame(Player p, GameInstance match) {

		loadPlayerData(p);

		Main.sendMessage(p, Main.getPrefix() + Lang.JOINING_GAME.getMessage(), Main.getLang());

		boolean success = match.addPlayer(p);

		if (!success)
			Main.sendMessage(p, Main.getPrefix() + Lang.COULD_NOT_JOIN_GAME.getMessage(), Main.getLang());

		return success;
	}

	private static void loadPlayerData(Player p) {
		ShopManager.getInstance().checkForNewGuns(p);

		try  {
			LoadoutManager.getInstance().load(p);
			InventoryManager.getInstance().setupPlayerSelectionInventories(p);
		} catch(Exception e) {
			Main.sendMessage(Main.getConsole(), Main.getPrefix() + Lang.ERROR_READING_PLAYER_LOADOUT.getMessage(), Main.getLang());
		}

		AssignmentManager.getInstance().load(p);

		ProgressionManager.getInstance().loadData(p);
		ProgressionManager.getInstance().saveData(p);

	}

	public static void leaveMatch(Player p) {
		if (!isInMatch(p) || getMatchWhichContains(p) == null) {
			Main.sendMessage(p, Main.getPrefix() + Lang.PLAYER_NOT_IN_GAME.getMessage(), Main.getLang());
			return;
		}

		Objects.requireNonNull(getMatchWhichContains(p)).removePlayer(p);

		Main.sendMessage(p, Main.getPrefix() + Lang.PLAYER_LEAVE_GAME.getMessage(), Main.getLang());
		if (Main.isServerMode()) {
			p.kickPlayer("");
		}
	}

	public static boolean isInMatch(Player p) {
		for (GameInstance game : runningGames) {
			if (game.getPlayers().contains(p)) {
				return true;
			}
		}

		return false;

	}

	public static CodMap getMapForName(String name) {
		for(CodMap map : addedMaps) {
			if (map.getName().equalsIgnoreCase(name))
				return map;
		}

		return null;
	}
	
	public static GameInstance getMatchWhichContains(Player p) {
		for (GameInstance game : runningGames) {
			if(game.getPlayers().contains(p)) {
				return game;
			}
		}
		
		return null;
	}

	public static CodMap pickRandomMap() {
		loadMaps();
		
		Collections.shuffle(addedMaps);
		
		for (CodMap m : addedMaps) {
			if (!m.isEnabled()) {
				continue;
			}
			if (!usedMaps.contains(m)) {
				usedMaps.add(m);
				return m;
			}
		}

		Main.sendMessage(Main.getConsole(), Lang.RAN_OUT_OF_MAPS.getMessage(), Main.getLang());

		return null;
	}

	public static boolean changeMap(GameInstance game, CodMap map) {

		if (game.getMap() == map)
			return false;

		if (usedMaps.contains(map))
			return false;

		if (!map.isEnabled())
			return false;

		if (game.getState() != GameState.WAITING && game.getState() != GameState.STARTING)
			return false;

		game.changeMap(map);
		return true;
	}

	public static void removeInstance(GameInstance i) {

		for (Player p : i.getPlayers()) {
			Main.sendMessage(p, Lang.CURRENT_GAME_REMOVED.getMessage(), Main.getLang());
		}
		
		Main.sendMessage(Main.getConsole(), Main.getPrefix() + ChatColor.GRAY + "Game instance id " + i.getId() + " has been removed!", Main.getLang());

		usedMaps.remove(i.getMap());

		runningGames.remove(i);

		System.gc();
	}

	public static ArrayList<CodMap> getAddedMaps() {
		return addedMaps;
	}

	public static ArrayList<GameInstance> getRunningGames() {
		return runningGames;
	}
}
