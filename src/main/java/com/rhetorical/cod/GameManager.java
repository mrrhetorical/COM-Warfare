package com.rhetorical.cod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.rhetorical.cod.files.ArenasFile;
import com.rhetorical.cod.object.CodMap;
import com.rhetorical.cod.object.GameInstance;
import com.rhetorical.cod.object.Gamemode;

public class GameManager {

	static ArrayList<GameInstance> RunningGames = new ArrayList<>();
	static ArrayList<CodMap> AddedMaps = new ArrayList<>();
	static ArrayList<CodMap> UsedMaps = new ArrayList<>();

	@SuppressWarnings("unchecked")
	static void loadMaps() {
		AddedMaps.clear();
		int k = 0;
		while (ArenasFile.getData().contains("Maps." + k)) {
			CodMap m;
			String name = ArenasFile.getData().getString("Maps." + k + ".name");
			String gm1 = ArenasFile.getData().getString("Maps." + k + ".gm");

			Gamemode gm = Gamemode.valueOf(gm1);

			m = new CodMap(name, gm);

			m.setAFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".AFlag"));
			m.setBFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".BFlag"));
			m.setCFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".CFlag"));
			m.setBlueFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".blueFlagSpawn"));
			m.setBlueSpawns((ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".blueSpawns"));
			m.setEnabled(ArenasFile.getData().getBoolean("Maps." + k + ".enabled"));
			m.setPinkSpawns((ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".pinkSpawns"));
			m.setRedFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".redFlagSpawn"));
			m.setRedSpawns((ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".redSpawns"));

			m.setEnable();

			AddedMaps.add(m);
			k++;
		}
	}

	public static boolean findMatch(Player p) {
		
		try  {
			Main.loadManager.load(p);
			Main.invManager.setupPlayerSelectionInventories(p);
		} catch(Exception e) {
			Main.sendMessage(Main.cs, Main.codPrefix + "\u00A7cCouldn't load loadouts from " + p.getDisplayName() + "!", Main.lang);
		}
		
		Main.progManager.loadData(p);
		Main.progManager.saveData(p);
		
		if (Main.lobbyLoc == null) {
			Main.sendMessage(p, Main.codPrefix + "\u00A7cNo lobby set! You cannot start a game without a lobby location set!", Main.lang);
			Main.sendMessage(p, Main.codPrefix + "\u00A77Could not create a match because there is no lobby location!", Main.lang);
			return false;
		}
		

		for (GameInstance i : RunningGames) {
			if (i.getPlayers().contains(p)) {
				Main.sendMessage(p, Main.codPrefix + "\u00A77You are already in a game!", Main.lang);
				return false;
			}
		}

		TreeMap<Integer, GameInstance> possibleMatches = new TreeMap<>();

		GameInstance newGame;

		Main.sendMessage(p, Main.codPrefix + "\u00A77Searching for match. . .", Main.lang);
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

			Main.sendMessage(p, Main.codPrefix + "\u00A77Could not find a match. . .", Main.lang);
			Main.sendMessage(p, Main.codPrefix + "\u00A77Creating match. . .", Main.lang);

			CodMap map = pickRandomMap();
			
			if (map == null) {
				Main.sendMessage(p, Main.codPrefix + "\u00A77Could not create a match because there are not enough maps!", Main.lang);
				return false;
			}

			newGame = new GameInstance(new ArrayList<>(), map);

			RunningGames.add(newGame);

			newGame.addPlayer(p);

			Main.sendMessage(p, Main.codPrefix + "\u00A77Created Lobby!", Main.lang);
			return true;

		}

		possibleMatches.lastEntry().getValue().addPlayer(p);
		Main.sendMessage(p, Main.codPrefix + "\u00A77Found Match!", Main.lang);
		for (Player inGame : possibleMatches.lastEntry().getValue().getPlayers()) {
			Main.sendMessage(inGame, Main.codPrefix + "\u00A77" + p.getName() + "\u00A77 has joined your lobby!", Main.lang);
		}

		return true;

		// Found match!
	}

	static void leaveMatch(Player p) {
		for (GameInstance i : RunningGames) {
			if (i.getPlayers().contains(p)) {
				i.removePlayer(p);
				p.getInventory().clear();
				p.setHealth(20D);
				p.setFoodLevel(20);
				Main.sendMessage(p, Main.codPrefix + "\u00A77You left the lobby!", Main.lang);
				return;
			}
		}

		Main.sendMessage(p, Main.codPrefix + "\u00A77You aren't in a lobby!", Main.lang);
	}

	public static boolean isInMatch(Player p) {
		for (GameInstance game : RunningGames) {
			if (game.getPlayers().contains(p)) {
				return true;
			}
		}

		return false;

	}
	
	static GameInstance getMatchWhichContains(Player p) {
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

		Main.sendMessage(Main.cs, "\u00A7cCOM-Warfare ran out of maps! (Maybe consider adding more maps?)", Main.lang);

		return null;
	}

	public static void removeInstance(GameInstance i) {

		for (Player p : i.getPlayers()) {
			Main.sendMessage(p, "\u00A7cThe current game instance has been removed!", Main.lang);
		}
		
		Main.sendMessage(Main.cs, Main.codPrefix + "\u00A77Game instance id " + i.getId() + " has been removed!", Main.lang);
		
		if (UsedMaps.contains(i.getMap())) {
			UsedMaps.remove(i.getMap());
		}

		System.gc();
	}

}
