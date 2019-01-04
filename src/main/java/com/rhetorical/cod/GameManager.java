package com.rhetorical.cod;

import java.util.*;

import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.object.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.rhetorical.cod.files.ArenasFile;

public class GameManager {

	static ArrayList<GameInstance> RunningGames = new ArrayList<>();
	static ArrayList<CodMap> AddedMaps = new ArrayList<>();
	public static ArrayList<CodMap> UsedMaps = new ArrayList<>();

	public static CodGun oitcGun = null;
	public static List<CodGun> gunGameGuns = new ArrayList<>();

	static void setupOITC() {
		String oitcGunName = Main.getPlugin().getConfig().getString("OITC_Gun");
		if (oitcGunName == null)
			return;

		CodWeapon weapon = Main.shopManager.getWeaponForName(oitcGunName);

		if (!(weapon instanceof CodGun))
			return;


		//OITC can now be played
		oitcGun = (CodGun) weapon;
	}

	static void setupGunGame() {
		List<String> guns = Main.getPlugin().getConfig().getStringList("GunProgression");
		for(String g : guns) {
			if (Main.shopManager.getWeaponForName(g) instanceof CodGun) {
				CodGun gun = (CodGun) Main.shopManager.getWeaponForName(g);
				if (!gunGameGuns.contains(gun))
					gunGameGuns.add(gun);
			}
		}
	}

	@SuppressWarnings("unchecked")
	static void loadMaps() {
		int k = 0;
		while (ArenasFile.getData().contains("Maps." + k)) {
			CodMap m;
			String name = ArenasFile.getData().getString("Maps." + k + ".name");

			m = new CodMap(name);

			m.setAFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".AFlag"));
			m.setBFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".BFlag"));
			m.setCFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".CFlag"));
			m.setBlueFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".blueFlagSpawn"));
			m.setBlueSpawns((ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".blueSpawns"));
			m.setEnabled(ArenasFile.getData().getBoolean("Maps." + k + ".enabled"));
			m.setPinkSpawns((ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".pinkSpawns"));
			m.setRedFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".redFlagSpawn"));
			m.setRedSpawns((ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".redSpawns"));

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

			for (CodMap map : AddedMaps) {
				contains = map.getName().equalsIgnoreCase(m.getName());
				if(contains)
					break;
			}

			if (!contains) {
				AddedMaps.add(m);
			}
			k++;
		}
	}

	public static boolean findMatch(Player p) {
		
		try  {
			Main.loadManager.load(p);
			Main.invManager.setupPlayerSelectionInventories(p);
		} catch(Exception e) {
			Main.sendMessage(Main.cs, Main.codPrefix + Lang.ERROR_READING_PLAYER_LOADOUT.getMessage(), Main.lang);
		}
		
		Main.progressionManager.loadData(p);
		Main.progressionManager.saveData(p);
		
		if (Main.lobbyLoc == null) {
			Main.sendMessage(p, Main.codPrefix + Lang.NO_LOBBY_SET.getMessage(), Main.lang);
			Main.sendMessage(p, Main.codPrefix + Lang.COULD_NOT_CREATE_MATCH_BECAUSE_NO_LOBBY.getMessage(), Main.lang);
			return false;
		}
		

		for (GameInstance i : RunningGames) {
			if (i.getPlayers().contains(p)) {
				Main.sendMessage(p, Main.codPrefix + Lang.ALREADY_IN_GAME.getMessage(), Main.lang);
				return false;
			}
		}

		TreeMap<Integer, GameInstance> possibleMatches = new TreeMap<>();

		GameInstance newGame;

		Main.sendMessage(p, Main.codPrefix + Lang.SEARCHING_FOR_MATCH.getMessage(), Main.lang);
		for (GameInstance i : RunningGames) {
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

			Main.sendMessage(p, Main.codPrefix + Lang.COULD_NOT_FIND_MATCH.getMessage(), Main.lang);
			Main.sendMessage(p, Main.codPrefix + Lang.CREATING_MATCH.getMessage(), Main.lang);

			CodMap map = pickRandomMap();
			if (map == null) {
				Main.sendMessage(p, Main.codPrefix + Lang.COULD_NOT_CREATE_MATCH_BECAUSE_NO_MAPS.getMessage(), Main.lang);
				return false;
			}

			map.changeGamemode();

			newGame = new GameInstance(new ArrayList<>(), map);

			RunningGames.add(newGame);

			newGame.addPlayer(p);

			Main.sendMessage(p, Main.codPrefix + Lang.CREATED_LOBBY.getMessage(), Main.lang);
			return true;

		}

		possibleMatches.lastEntry().getValue().addPlayer(p);
		Main.sendMessage(p, Main.codPrefix + Lang.FOUND_MATCH.getMessage(), Main.lang);
		for (Player inGame : possibleMatches.lastEntry().getValue().getPlayers()) {
			Main.sendMessage(inGame, Main.codPrefix + Lang.PLAYER_JOINED_LOBBY.getMessage().replace("{player}", p.getDisplayName()), Main.lang);
		}

		return true;

		// Found match!
	}

	public static void leaveMatch(Player p) {
		if (!isInMatch(p) || getMatchWhichContains(p) == null) {
			Main.sendMessage(p, Main.codPrefix + Lang.PLAYER_NOT_IN_GAME.getMessage(), Main.lang);
			return;
		}

		Objects.requireNonNull(getMatchWhichContains(p)).removePlayer(p);
		Main.sendMessage(p, Main.codPrefix + Lang.PLAYER_LEAVE_GAME.getMessage(), Main.lang);
	}

	public static boolean isInMatch(Player p) {
		for (GameInstance game : RunningGames) {
			if (game.getPlayers().contains(p)) {
				return true;
			}
		}

		return false;

	}

	public static CodMap getMapForName(String name) {
		for(CodMap map : AddedMaps) {
			if (map.getName().equalsIgnoreCase(name))
				return map;
		}

		return null;
	}
	
	public static GameInstance getMatchWhichContains(Player p) {
		for (GameInstance game : RunningGames) {
			if(game.getPlayers().contains(p)) {
				return game;
			}
		}
		
		return null;
	}

	public static CodMap pickRandomMap() {
		loadMaps();
		
		Collections.shuffle(AddedMaps);
		
		for (CodMap m : AddedMaps) {
			if (!m.isEnabled()) {
				continue;
			}
			if (!UsedMaps.contains(m)) {
				UsedMaps.add(m);
				return m;
			}
		}

		Main.sendMessage(Main.cs, Lang.RAN_OUT_OF_MAPS.getMessage(), Main.lang);

		return null;
	}

	public static boolean changeMap(GameInstance game, CodMap map) {

		if (game.getMap() == map)
			return false;

		if (UsedMaps.contains(map))
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
			Main.sendMessage(p, Lang.CURRENT_GAME_REMOVED.getMessage(), Main.lang);
		}
		
		Main.sendMessage(Main.cs, Main.codPrefix + "\u00A77Game instance id " + i.getId() + " has been removed!", Main.lang);

		UsedMaps.remove(i.getMap());

		System.gc();
	}

}
