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
			Main.cs.sendMessage(Main.codPrefix + "§cCouldn't load loadouts from " + p.getDisplayName() + "!");
		}
		
		Main.progManager.loadData(p);
		Main.progManager.saveData(p);
		
		if (Main.lobbyLoc == null) {
			
			Main.cs.sendMessage(Main.codPrefix + "§cNo lobby set! You cannot start a game without a lobby location set!");
			p.sendMessage(Main.codPrefix + "§7Could not create a match because there is no lobby location!");
			return false;
		}
		

		for (GameInstance i : RunningGames) {
			if (i.getPlayers().contains(p)) {
				p.sendMessage(Main.codPrefix + "§7You are already in a game!");
				return false;
			}
		}

		TreeMap<Integer, GameInstance> possibleMatches = new TreeMap<Integer, GameInstance>();

		GameInstance newGame = null;

		p.sendMessage(Main.codPrefix + "§7Searching for match. . .");
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

			p.sendMessage(Main.codPrefix + "§7Could not find a match. . .");
			p.sendMessage(Main.codPrefix + "§7Creating match. . .");

			CodMap map = pickRandomMap();
			
			if (map == null) {
				p.sendMessage(Main.codPrefix + "§7Could not create a match because there are not enough maps!");
				return false;
			}

			newGame = new GameInstance(new ArrayList<Player>(), map);

			RunningGames.add(newGame);

			newGame.addPlayer(p);

			p.sendMessage(Main.codPrefix + "§7Created Lobby!");
			return true;

		}

		possibleMatches.lastEntry().getValue().addPlayer(p);
		p.sendMessage(Main.codPrefix + "§7Found Match!");
		for (Player inGame : possibleMatches.lastEntry().getValue().getPlayers()) {
			inGame.sendMessage(Main.codPrefix + "§7" + p.getName() + "§7 has joined your lobby!");
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
				p.sendMessage(Main.codPrefix + "§7You left the lobby!");
				return;
			}
		}

		p.sendMessage(Main.codPrefix + "§7You aren't in a lobby!");
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

		Main.cs.sendMessage("§cCOM-Warfare ran out of maps! (Maybe consider adding more maps?)");

		return null;
	}

	public static void removeInstance(GameInstance i) {

		for (Player p : i.getPlayers()) {
			p.sendMessage("§cThe current game instance has been removed!");
		}
		
		Main.cs.sendMessage(Main.codPrefix + "§7Game instance id " + i.getId() + " has been removed!");
		
		if (UsedMaps.contains(i.getMap())) {
			UsedMaps.remove(i.getMap());
		}
		
		i = null;
		System.gc();
		return;
	}

}
