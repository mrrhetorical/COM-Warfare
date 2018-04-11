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

	public static ArrayList<GameInstance> RunningGames = new ArrayList<GameInstance>();
	public static ArrayList<CodMap> AddedMaps = new ArrayList<CodMap>();
	public static ArrayList<CodMap> UsedMaps = new ArrayList<CodMap>();
	
	@SuppressWarnings("unchecked")
	public static void loadMaps() {
		AddedMaps.clear();
		int k = 0;
		while (ArenasFile.getData().contains("Maps." + k)) {
			CodMap m;
			String name = ArenasFile.getData().getString("Maps." + k + ".name");
			String gm1 = ArenasFile.getData().getString("Maps." + k + ".gm");

			Gamemode gm = Gamemode.valueOf(gm1);

			m = new CodMap(name, gm);

			m.setBlueAFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".blueAFlagSpawn"));
			m.setBlueBFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".blueBFlagSpawn"));
			m.setBlueCFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".blueCFlagSpawn"));
			m.setBlueFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".blueFlagSpawn"));
			m.setBlueSpawns((ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".blueSpawns"));
			m.setEnabled(ArenasFile.getData().getBoolean("Maps." + k + ".enabled"));
			m.setPinkSpawns((ArrayList<Location>) ArenasFile.getData().get("Maps." + k + ".pinkSpawns"));
			m.setRedAFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".redAFlagSpawn"));
			m.setRedBFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".redBFlagSpawn"));
			m.setRedCFlagSpawn((Location) ArenasFile.getData().get("Maps." + k + ".redCFlagSpawn"));
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
			Main.sendMessage(Main.cs, Main.codPrefix + "§cCouldn't load loadouts from " + p.getDisplayName() + "!", Main.lang);
		}
		
		Main.progManager.loadData(p);
		Main.progManager.saveData(p);
		
		if (Main.lobbyLoc == null) {
			
			Main.sendMessage(Main.cs, Main.codPrefix + "§cNo lobby set! You cannot start a game without a lobby location set!", Main.lang);
			Main.sendMessage(Main.cs, Main.codPrefix + "§7Could not create a match because there is no lobby location!", Main.lang);
			return false;
		}
		

		for (GameInstance i : RunningGames) {
			if (i.getPlayers().contains(p)) {
				Main.sendMessage(Main.cs, Main.codPrefix + "§7You are already in a game!", Main.lang);
				return false;
			}
		}

		TreeMap<Integer, GameInstance> possibleMatches = new TreeMap<Integer, GameInstance>();

		GameInstance newGame = null;

		Main.sendMessage(Main.cs, Main.codPrefix + "§7Searching for match. . .", Main.lang);
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

			Main.sendMessage(Main.cs, Main.codPrefix + "§7Could not find a match. . .", Main.lang);
			Main.sendMessage(Main.cs, Main.codPrefix + "§7Creating match. . .", Main.lang);

			CodMap map = pickRandomMap();
			
			if (map == null) {
				Main.sendMessage(Main.cs, Main.codPrefix + "§7Could not create a match because there are not enough maps!", Main.lang);
				return false;
			}

			newGame = new GameInstance(new ArrayList<Player>(), map);

			RunningGames.add(newGame);

			newGame.addPlayer(p);

			Main.sendMessage(Main.cs, Main.codPrefix + "§7Created Lobby!", Main.lang);
			return true;

		}

		possibleMatches.lastEntry().getValue().addPlayer(p);
		Main.sendMessage(Main.cs, Main.codPrefix + "§7Found Match!", Main.lang);
		for (Player inGame : possibleMatches.lastEntry().getValue().getPlayers()) {
			Main.sendMessage(inGame, Main.codPrefix + "§7" + p.getName() + "§7 has joined your lobby!", Main.lang);
		}

		return true;

		// Found match!
	}

	public static void leaveMatch(Player p) {
		for (GameInstance i : RunningGames) {
			if (i.getPlayers().contains(p)) {
				i.removePlayer(p);
				p.getInventory().clear();
				p.setHealth(20D);
				p.setFoodLevel(20);
				Main.sendMessage(Main.cs, Main.codPrefix + "§7You left the lobby!", Main.lang);
				return;
			}
		}

		Main.sendMessage(Main.cs, Main.codPrefix + "§7You aren't in a lobby!", Main.lang);
	}

	public static boolean isInMatch(Player p) {
		for (GameInstance game : RunningGames) {
			if (game.getPlayers().contains(p)) {
				return true;
			}
		}

		return false;

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

		Main.sendMessage(Main.cs, "§cCOM-Warfare ran out of maps! (Maybe consider adding more maps?)", Main.lang);

		return null;
	}

	public static void removeInstance(GameInstance i) {

		for (Player p : i.getPlayers()) {
			Main.sendMessage(p, "§cThe current game instance has been removed!", Main.lang);
		}
		
		Main.sendMessage(Main.cs, Main.codPrefix + "§7Game instance id " + i.getId() + " has been removed!", Main.lang);
		
		if (UsedMaps.contains(i.getMap())) {
			UsedMaps.remove(i.getMap());
		}
		
		i = null;
		System.gc();
		return;
	}

}
