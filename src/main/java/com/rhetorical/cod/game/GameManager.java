package com.rhetorical.cod.game;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.rhetorical.cod.ComWarfare;
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

import java.util.*;

public class GameManager {

	static ArrayList<GameInstance> runningGames = new ArrayList<>();
	static ArrayList<CodMap> addedMaps = new ArrayList<>();
	public static ArrayList<CodMap> usedMaps = new ArrayList<>();

	public static CodGun oitcGun = null;
	public static List<CodGun> gunGameGuns = new ArrayList<>();

	public static void setupOITC() {
		String oitcGunName = ComWarfare.getPlugin().getConfig().getString("OITC_Gun");
		if (oitcGunName == null)
			return;

		CodWeapon weapon = ShopManager.getInstance().getWeaponForName(oitcGunName);

		if (!(weapon instanceof CodGun))
			return;


		//OITC can now be played
		oitcGun = (CodGun) weapon;
	}

	public static void setupGunGame() {
		List<String> guns = ComWarfare.getPlugin().getConfig().getStringList("GunProgression");
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

	/**
	 * Attempts to find a match for the given player.
	 * @return Returns if the player was able to join a match.
	 * */
	public static boolean findMatch(Player p) {

		loadPlayerData(p);
		
		if (ComWarfare.getLobbyLocation() == null) {
			ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.NO_LOBBY_SET.getMessage(), ComWarfare.getLang());
			ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.COULD_NOT_CREATE_MATCH_BECAUSE_NO_LOBBY.getMessage(), ComWarfare.getLang());
			return false;
		}
		

		for (GameInstance i : runningGames) {
			if (i.getPlayers().contains(p)) {
				ComWarfare.sendMessage(p, Lang.ALREADY_IN_GAME.getMessage(), ComWarfare.getLang());
				return false;
			}
		}

		TreeMap<Integer, GameInstance> possibleMatches = new TreeMap<>();

		GameInstance newGame;

		ComWarfare.sendMessage(p, Lang.SEARCHING_FOR_MATCH.getMessage(), ComWarfare.getLang());
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

			ComWarfare.sendMessage(p, Lang.COULD_NOT_FIND_MATCH.getMessage(), ComWarfare.getLang());
			ComWarfare.sendMessage(p, Lang.CREATING_MATCH.getMessage(), ComWarfare.getLang());

			CodMap map = pickRandomMap();
			if (map == null) {
				ComWarfare.sendMessage(p, Lang.COULD_NOT_CREATE_MATCH_BECAUSE_NO_MAPS.getMessage(), ComWarfare.getLang());
				return false;
			}

			map.changeGamemode();

			newGame = new GameInstance(new ArrayList<>(), map);

			runningGames.add(newGame);

			newGame.addPlayer(p);

			ComWarfare.sendMessage(p, Lang.CREATED_LOBBY.getMessage(), ComWarfare.getLang());
			return true;

		}

		ComWarfare.sendMessage(p, Lang.FOUND_MATCH.getMessage(), ComWarfare.getLang());
		ComWarfare.sendMessage(p, Lang.JOINING_GAME.getMessage(), ComWarfare.getLang());

		if (!possibleMatches.lastEntry().getValue().addPlayer(p)) {
			ComWarfare.sendMessage(p, Lang.COULD_NOT_JOIN_GAME.getMessage(), ComWarfare.getLang());
		}


		return true;

		// Found match!
	}

	/**
	 * Tries to join a specific game instance.
	 * @return Returns if the player was able to successfully join the game.
	 * */
	public static boolean joinGame(Player p, GameInstance match) {

		loadPlayerData(p);

		ComWarfare.sendMessage(p,  Lang.JOINING_GAME.getMessage(), ComWarfare.getLang());

		boolean success = match.addPlayer(p);

		if (!success)
			ComWarfare.sendMessage(p, Lang.COULD_NOT_JOIN_GAME.getMessage(), ComWarfare.getLang());

		return success;
	}


	/**
	 * Loads all data associated with the player relevant to playing a match of COM-Warfare.
	 * */
	private static void loadPlayerData(Player p) {
		ShopManager.getInstance().checkForNewGuns(p, false);

		try  {
			LoadoutManager.getInstance().load(p);
			InventoryManager.getInstance().setupPlayerSelectionInventories(p);
		} catch(Exception e) {
			ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + Lang.ERROR_READING_PLAYER_LOADOUT.getMessage(), ComWarfare.getLang());
		}

		AssignmentManager.getInstance().load(p);

		ProgressionManager.getInstance().loadData(p);
		ProgressionManager.getInstance().saveData(p);

	}

	@SuppressWarnings("UnstableApiUsage")
	public static void leaveMatch(Player p) {
		if (!isInMatch(p) || getMatchWhichContains(p) == null) {
			ComWarfare.sendMessage(p, Lang.PLAYER_NOT_IN_GAME.getMessage(), ComWarfare.getLang());
			return;
		}

		Objects.requireNonNull(getMatchWhichContains(p)).removePlayer(p);

		ComWarfare.sendMessage(p, Lang.PLAYER_LEAVE_GAME.getMessage(), ComWarfare.getLang());
		if (ComWarfare.isServerMode() && !ComWarfare.getInstance().getLobbyServer().equalsIgnoreCase("none")) {
			try {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Connect");
				out.writeUTF(ComWarfare.getInstance().getLobbyServer());
				p.sendPluginMessage(ComWarfare.getInstance(), "BungeeCord", out.toByteArray());
			} catch (Exception e) {
				p.kickPlayer("");
			}
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

		ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.RAN_OUT_OF_MAPS.getMessage(), ComWarfare.getLang());

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
			ComWarfare.sendMessage(p, Lang.CURRENT_GAME_REMOVED.getMessage(), ComWarfare.getLang());
		}

		i.destroy();
		
		ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + ChatColor.GRAY + "Game instance id " + i.getId() + " has been removed!", ComWarfare.getLang());

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
