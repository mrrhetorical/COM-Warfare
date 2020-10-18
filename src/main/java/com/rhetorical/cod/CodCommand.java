package com.rhetorical.cod;

import com.rhetorical.cod.files.ArenasFile;
import com.rhetorical.cod.game.*;
import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.inventories.MatchBrowser;
import com.rhetorical.cod.inventories.ShopManager;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.progression.ProgressionManager;
import com.rhetorical.cod.weapons.CodGun;
import com.rhetorical.cod.weapons.CodWeapon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CodCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cColor = "" + ChatColor.YELLOW;
		String dColor = "" + ChatColor.WHITE;

		if (args.length == 0) {

			if (!ComWarfare.hasPerm(sender, "com.help"))
				return true;

			onCommand(sender, command, label, new String[]{"help", "1"});
			return true;
		} else {
			if (args[0].equalsIgnoreCase("help")) {
				if (!ComWarfare.hasPerm(sender, "com.help", true))
					return true;

				if (args.length != 2) {
					args = new String[]{"help", "1"};
				}

				int page;
				try {
					page = Integer.parseInt(args[1]);
				} catch (Exception e) {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.NOT_PROPER_PAGE.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!(page > 0 && page <= 5)) {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.NOT_PROPER_PAGE.getMessage(), ComWarfare.getLang());
					return true;
				}

				ComWarfare.sendMessage(sender, "-===" + ChatColor.GOLD + "COM-Warfare Help" + ChatColor.RESET + "===-", ComWarfare.getLang());
				ComWarfare.sendMessage(sender, ChatColor.WHITE + "[Page " + ChatColor.GREEN	+ page + ChatColor.WHITE + " of 5]", ComWarfare.getLang());

				switch (page) {
					case 1:
						ComWarfare.sendMessage(sender, cColor + "/cod help [page number] | " + dColor + "Opens a help page.");
						ComWarfare.sendMessage(sender, cColor + "/cod menu | " + dColor + "Opens the cod menu.");
						ComWarfare.sendMessage(sender, cColor + "/cod join | " + dColor + "Joins a match via matchmaker.");
						ComWarfare.sendMessage(sender, cColor + "/cod browser | " + dColor + "Opens the match browser.");
						ComWarfare.sendMessage(sender, cColor + "/cod leave | " + dColor + "Leaves the current game.");
						ComWarfare.sendMessage(sender, cColor + "/cod lobby | " + dColor + "Teleports you to the lobby.");
						break;
					case 2:
						ComWarfare.sendMessage(sender, cColor + "/cod shop | " + dColor + "Opens the shop.");
						ComWarfare.sendMessage(sender, cColor + "/cod balance | " + dColor + "Shows player's credit balance.");
						ComWarfare.sendMessage(sender, cColor + "/cod class | " + dColor + "Opens class selection menu.");
						ComWarfare.sendMessage(sender, cColor + "/cod listMaps | " + dColor + "Lists all available maps.");
						ComWarfare.sendMessage(sender, cColor + "/cod start | " + dColor + "Auto-starts the match if the lobby timer has started.");
						break;
					case 3:
						ComWarfare.sendMessage(sender, cColor + "/cod boot | " + dColor + "Forces all players in all matches to leave.");
						ComWarfare.sendMessage(sender, cColor + "/cod changeMap/changeMode [map name/game mode] | " + dColor + "Changes the current map/mode.");
						ComWarfare.sendMessage(sender, cColor + "/cod setLevel [player] (level) | " + dColor + "Sets the player's level.");
						ComWarfare.sendMessage(sender, cColor + "/cod credits [give/set] [player] (amt) | " + dColor + "Gives credits.");
						ComWarfare.sendMessage(sender, cColor + "/cod createGun (args) | " + dColor + "Creates a gun. Type command to see a full list of arguments.");
						break;
					case 4:
						ComWarfare.sendMessage(sender, cColor + "/cod createWeapon (args) | " + dColor + "Creates a grenade. Type to see a full list of arguments.");
						ComWarfare.sendMessage(sender, cColor + "/cod createMap [name] | " + dColor + "Creates a map.");
						ComWarfare.sendMessage(sender, cColor + "/cod removeMap [name] | " + dColor + "Removes a map.");
						ComWarfare.sendMessage(sender, cColor + "/cod set [lobby/spawn/flag] | " + dColor + "Set spawns, flags, and lobby location.");
						ComWarfare.sendMessage(sender, cColor + "/cod add [oitc/gun] (gun name) | " + dColor + "Sets gun for OITC or adds gun to Gun Game.");
						break;
					case 5:
						ComWarfare.sendMessage(sender, cColor + "/cod blacklist (map) (mode) | " + dColor + "Prevents a mode from being played on the map.");
						ComWarfare.sendMessage(sender, cColor + "/cod version | " + dColor + "Displays the running version of COM-Warfare.");
						ComWarfare.sendMessage(sender, cColor + "/cod removeSpawns (map) | " + dColor + "Shows spawn points so they may be removed.");
						ComWarfare.sendMessage(sender, cColor + "/cod reload (map) | " + dColor + "Reloads a map's data and enables it if able to.");
						break;
					default:
						break;
				}

			} else if (args[0].equalsIgnoreCase("join")) {

				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.join"))
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
					ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.leave", true))
					return true;

				Player p = (Player) sender;
				GameManager.leaveMatch(p);

				return true;
			} else if (args[0].equalsIgnoreCase("version")) {
				if (!ComWarfare.hasPerm(sender, "com.version", true))
					return true;

				ComWarfare.sendMessage(sender, String.format("%sYou are running COM-Warfare version: %s%s", ChatColor.GREEN, ChatColor.YELLOW, ComWarfare.getInstance().getDescription().getVersion()));

			} else if (args[0].equalsIgnoreCase("browser")) {
				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.join", true))
					return true;

				Player p = (Player) sender;

				p.openInventory(MatchBrowser.getInstance().getBrowser());

			} else if (args[0].equalsIgnoreCase("menu")) {
				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.join", false))
					return true;

				Player p = (Player) sender;
				p.openInventory(InventoryManager.getInstance().mainInventory);
				p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
			} else if (args[0].equalsIgnoreCase("listMaps")) {

				if (!ComWarfare.hasPerm(sender, "com.map.list", true))
					return true;

				ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.MAP_LIST_HEADER.getMessage(), ComWarfare.getLang());
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

					ComWarfare.sendMessage(sender, entry, ComWarfare.getLang());
				}
				return true;
			} else if (args[0].equalsIgnoreCase("createMap")) {


				if (!ComWarfare.hasPerm(sender, "com.map.create"))
					return true;

				if (args.length >= 2) {
					CodMap newMap;
					String mapName = args[1];

					for (CodMap m : GameManager.getAddedMaps()) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.CREATE_MAP_ALREADY_EXISTS.getMessage(), ComWarfare.getLang());
							return true;
						}
					}

					newMap = new CodMap(mapName);

					GameManager.getAddedMaps().add(newMap);
					String msg = Lang.CREATE_MAP_SUCCESS.getMessage();
					msg = msg.replace("{map-name}", mapName);
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + msg, ComWarfare.getLang());
					newMap.setEnable();
					return true;
				} else {
					String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createMap (name)");
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + msg);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("removeMap")) {

				if (!ComWarfare.hasPerm(sender, "com.map.remove"))
					return true;

				if (args.length >= 2) {
					GameManager.loadMaps();

					String mapName = args[1];

					for (CodMap m : GameManager.getAddedMaps()) {
						if (m.getName().equalsIgnoreCase(mapName)) {
							GameManager.getAddedMaps().remove(m);

							File aFile = new File(ComWarfare.getInstance().getDataFolder(), "arenas.yml");

							if (aFile.exists()) {
								boolean success = aFile.delete();
							}

							ArenasFile.setup(ComWarfare.getInstance());

							for (CodMap notChanged : GameManager.getAddedMaps()) {
								notChanged.save();
							}

							ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.REMOVE_MAP_SUCCESS.getMessage(), ComWarfare.getLang());
							return true;
						}
					}

					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage(), ComWarfare.getLang());
					return true;

				} else {
					String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod removeMap (name)");
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + msg);
					return true;
				}

			} else if (args[0].equalsIgnoreCase("set")) {

				if (!ComWarfare.hasPerm(sender, "com.map.modify"))
					return true;

				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}
				Player p = (Player) sender;

				if (!(args.length > 1)) {
					String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set (lobby/spawn/flag) [args]");
					ComWarfare.sendMessage(p, ComWarfare.getPrefix() + msg);
					return true;
				}

				if (args[1].equalsIgnoreCase("lobby")) {

					Location lobby = p.getLocation();
					ComWarfare.getInstance().getConfig().set("com.lobby", lobby);
					ComWarfare.getInstance().lobbyLoc = (Location) ComWarfare.getInstance().getConfig().get("com.lobby");
					ComWarfare.getInstance().saveConfig();
					ComWarfare.getInstance().reloadConfig();
					ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.SET_LOBBY_SUCCESS.getMessage(), ComWarfare.getLang());
					return true;
				} else if (args[1].equalsIgnoreCase("spawn")) {

					if (!ComWarfare.hasPerm(p, "com.map.addSpawn"))
						return true;

					if (args.length < 4) {
						String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set spawn (map name) (team)");
						ComWarfare.sendMessage(p, ComWarfare.getPrefix() + msg);
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
						ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.MAP_NOT_EXISTS_WITH_NAME, ComWarfare.getLang());
						return true;
					}

					Block block = p.getLocation().getBlock();
					List<Location> blocks = new ArrayList<>(map.getRedSpawns());
					blocks.addAll(map.getBlueSpawns());
					blocks.addAll(map.getPinkSpawns());

					for (Location loc : blocks) {
						if (loc.getBlock().equals(block)) {
							ComWarfare.sendMessage(p, Lang.SPAWN_ALREADY_EXISTS.getMessage());
							return true;
						}
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
							ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.TEAM_NOT_EXISTS_WITH_NAME.getMessage(), ComWarfare.getLang());
							return true;
					}

					String msg = Lang.SET_SPAWN_SUCCESS.getMessage().replace("{team}", team).replace("{map-name}", map.getName());
					ComWarfare.sendMessage(p, ComWarfare.getPrefix() + msg);
				} else if (args[1].equalsIgnoreCase("flag")) {

					if (!ComWarfare.hasPerm(p, "com.map.modify"))
						return true;

					if (args.length < 4) {
						String msg = Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod set flag (map name) (hardpoint/red/blue/a/b/c)");
						ComWarfare.sendMessage(p, ComWarfare.getPrefix() + msg);
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
						ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage(), ComWarfare.getLang());
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
							ComWarfare.sendMessage(p, ComWarfare.getPrefix() + msg);
							return true;
					}

					if (team == null) {
						ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.SET_FLAG_DOM_SUCCESS.getMessage().replace("{flag}" + ChatColor.RESET, flag));
					} else {
						ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.SET_FLAG_CTF_SUCCESS.getMessage().replace("{team}" + ChatColor.RESET, team));
					}

					return true;
				}

			} else if (args[0].equalsIgnoreCase("lobby")) {

				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.RED + Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}


				if (!ComWarfare.hasPerm(sender, "com.lobby"))
					return true;

				Player p = (Player) sender;

				if (GameManager.isInMatch(p)) {
					ComWarfare.sendMessage(p, Lang.MUST_NOT_BE_IN_GAME.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (ComWarfare.getInstance().lobbyLoc != null) {
					p.teleport(ComWarfare.getInstance().lobbyLoc);
				} else {
					ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.LOBBY_NOT_EXISTS.getMessage(), ComWarfare.getLang());
				}
			} else if (args[0].equalsIgnoreCase("balance")) {
				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.RED + Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}


				if (!ComWarfare.hasPerm(sender, "com.join", true))
					return true;

				Player p = (Player) sender;
				int credits = CreditManager.getCredits(p);
				ComWarfare.sendMessage(p, Lang.BALANCE_COMMAND.getMessage().replace("{credits}", credits + ""), ComWarfare.getLang());
			} else if (args[0].equalsIgnoreCase("credits")) {
				if (args.length < 3) {
					if (ComWarfare.hasPerm(sender, "com.credits.give"))
						ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits [give/set] {player} (amount)"));
					return true;
				}

				if (args[1].equalsIgnoreCase("give")) {

					if (!ComWarfare.hasPerm(sender, "com.credits.give"))
						return true;

					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits give {player} (amount)"));
						return true;

					}

					CreditManager.setCredits(playerName, CreditManager.getCredits(playerName) + amount);
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.GIVE_BALANCE_COMMAND.getMessage().replace("{player}", playerName).replace("{amount}", amount + "").replace("{total}", CreditManager.getCredits(playerName) + ""), ComWarfare.getLang());
					return true;
				} else if (args[1].equalsIgnoreCase("set")) {

					if (!ComWarfare.hasPerm(sender, "com.credits.set"))
						return true;

					String playerName = args[2];
					int amount;
					try {
						amount = Integer.parseInt(args[3]);
					} catch (Exception e) {
						ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod credits set {name} [amount]"));
						return true;
					}

					CreditManager.setCredits(playerName, amount);
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.SET_BALANCE_COMMAND.getMessage().replace("{player}", playerName).replace("{amount}", amount + ""), ComWarfare.getLang());
					return true;
				}
			} else if (args[0].equalsIgnoreCase("createGun")) {

				if (!ComWarfare.hasPerm(sender, "com.createGun"))
					return true;

				if (args.length >= 9) {
					ComWarfare.getInstance().createGun(sender, args);
					return true;
				} else {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createGun (Gun name) (Primary/Secondary) (Unlock type: level/credits/both) (Ammo Amount) (Gun Material) (Ammo Material) (Level Unlock) (Cost)"));
					return true;
				}
			} else if ((args[0].equalsIgnoreCase("createWeapon") || args[0].equalsIgnoreCase("createGrenade"))) {

				if (!ComWarfare.hasPerm(sender, "com.createWeapon"))
					return true;

				if (args.length >= 7) {
					ComWarfare.getInstance().createWeapon(sender, args);
					return true;
				} else {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod createWeapon (name) (Lethal/Tactical) (Unlock Type: level/credits/both) (Grenade Material) (Level Unlock) (Cost)"));
					return true;
				}
			} else if (args[0].equalsIgnoreCase("start")) {
				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), ChatColor.RED + Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.forceStart", true))
					return true;

				Player p = (Player) sender;
				if (GameManager.isInMatch(p)) {
					try {
						if (GameManager.getMatchWhichContains(p) != null) {
							GameInstance game = GameManager.getMatchWhichContains(p);
							if (game != null) {
								game.forceStart(true);
							} else {
								p.sendMessage(Lang.FORCE_START_FAIL.getMessage());
							}
						}
					} catch(Exception e) {
						ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + Lang.COULD_NOT_FIND_GAME_PLAYER_IN, ComWarfare.getLang());
					}
					return true;
				} else {
					ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.MUST_BE_IN_GAME.getMessage(), ComWarfare.getLang());
				}

				return true;
			} else if (args[0].equalsIgnoreCase("class")) {
				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.selectClass", true))
					return true;

				Player p = (Player) sender;
				InventoryManager.getInstance().openSelectClassInventory(p);
				return true;
			} else if (args[0].equalsIgnoreCase("shop")) {
				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.openShop", true))
					return true;

				Player p = (Player) sender;
				p.closeInventory();
				p.openInventory(InventoryManager.getInstance().mainShopInventory);
				return true;
			} else if (args[0].equalsIgnoreCase("boot")) {

				if (!ComWarfare.hasPerm(sender, "com.bootAll", true))
					return true;

				boolean result = ComWarfare.getInstance().bootPlayers();
				if (result) {
					sender.sendMessage(ComWarfare.getPrefix() + Lang.PLAYERS_BOOTED_SUCCESS.getMessage());
				} else {
					sender.sendMessage(ComWarfare.getPrefix() + Lang.PLAYER_BOOTED_FAILURE.getMessage());
				}
			} else if (args[0].equalsIgnoreCase("add")) {

				if (!ComWarfare.hasPerm(sender, "com.add"))
					return true;

				if (args.length	< 3) {
					ComWarfare.sendMessage(sender, Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod add [oitc/gun] (gun name)"));
					return true;
				}

				String type = args[1];
				String gunName = args[2];
				CodWeapon weapon = ShopManager.getInstance().getWeaponForName(gunName);

				if (!(weapon instanceof CodGun)) {
					ComWarfare.sendMessage(sender, Lang.WEAPON_NOT_FOUND_WITH_NAME.getMessage().replace("{gun-name}", gunName));
					return true;
				}

				if (type.equalsIgnoreCase("oitc")) {
					ComWarfare.getInstance().getConfig().set("OITC_Gun", weapon.getName());
					ComWarfare.getInstance().saveConfig();
					ComWarfare.getInstance().reloadConfig();
					GameManager.setupOITC();
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.OITC_GUN_SET_SUCCESS.getMessage().replace("{gun-name}", gunName));
					return true;
				} else if (type.equalsIgnoreCase("gun")) {
					GameManager.gunGameGuns.add((CodGun) weapon);
					List<String> gunList = new ArrayList<>();
					for(CodGun g : GameManager.gunGameGuns) {
						gunList.add(g.getName());
					}
					ComWarfare.getInstance().getConfig().set("GunProgression", gunList);
					ComWarfare.getInstance().saveConfig();
					ComWarfare.getInstance().reloadConfig();
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.GUN_PROGRESSION_ADDED_SUCCESS.getMessage());
					return true;
				}
				ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod add [oitc/gun] (gun name)"));
				return true;
			} else if (args[0].equalsIgnoreCase("changeMap")) {
				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.changeMap", true))
					return true;

				Player p = (Player) sender;

				if (args.length < 2) {
					ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod changeMap (name)"));
					return true;
				}

				if (!GameManager.isInMatch(p)) {
					ComWarfare.sendMessage(p, Lang.MUST_BE_IN_GAME.getMessage());
					return true;
				}

				GameInstance game = GameManager.getMatchWhichContains(p);
				if (game == null)
					return true;

				if (game.getState() != GameState.WAITING && game.getState() != GameState.STARTING) {
					ComWarfare.sendMessage(p, Lang.MUST_NOT_BE_IN_GAME.getMessage());
					return true;
				}


				CodMap map = GameManager.getMapForName(args[1]);

				if (map == null) {
					ComWarfare.sendMessage(p, Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				GameManager.changeMap(game, map);
				ComWarfare.sendMessage(p, Lang.MAP_CHANGE_SUCCESS.getMessage().replace("{map-name}", map.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("changeMode")) {
				if (!(sender instanceof Player)) {
					ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + Lang.MUST_BE_PLAYER.getMessage(), ComWarfare.getLang());
					return true;
				}

				if (!ComWarfare.hasPerm(sender, "com.changeMode", true))
					return true;

				Player p = (Player) sender;
				if (args.length < 2) {
					ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod changeMode (name)"));
					return true;
				}

				if (!GameManager.isInMatch(p)) {
					ComWarfare.sendMessage(p, Lang.MUST_BE_IN_GAME.getMessage());
					return true;
				}

				GameInstance game = GameManager.getMatchWhichContains(p);
				if (game == null)
					return true;

				if (game.getState() != GameState.WAITING && game.getState() != GameState.STARTING) {
					ComWarfare.sendMessage(p, Lang.MUST_NOT_BE_IN_GAME.getMessage());
					return true;
				}

				Gamemode mode;

				try {
					mode = Gamemode.valueOf(args[1].toUpperCase());
				} catch(Exception e) {
					ComWarfare.sendMessage(p, Lang.GAME_MODE_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				if (!game.getMap().getAvailableGamemodes().contains(mode)) {
					ComWarfare.sendMessage(p, Lang.GAME_MODE_NOT_SET_UP_ON_MAP.getMessage());
					return true;
				}

				Objects.requireNonNull(GameManager.getMatchWhichContains(p)).changeGamemode(mode);
				ComWarfare.sendMessage(p, Lang.GAME_MODE_CHANGE_SUCCESS.getMessage().replace("{game-mode}", mode.toString()));
				return true;
			} else if (args[0].equalsIgnoreCase("blacklist")) {

				if (!ComWarfare.hasPerm(sender, "com.blacklist"))
					return true;

				if (args.length	< 3) {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod blacklist (map) (mode)"));
					return true;
				}

				Gamemode mode;

				try {
					mode = Gamemode.valueOf(args[2].toUpperCase());
				} catch(Exception e) {
					ComWarfare.sendMessage(sender, Lang.GAME_MODE_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				CodMap map = GameManager.getMapForName(args[1]);

				if (map == null) {
					ComWarfare.sendMessage(sender, Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				map.addToBlacklist(mode);

				ComWarfare.sendMessage(sender, Lang.BLACKLIST_SUCCESS.getMessage().replace("{mode}", mode.toString()).replace("{map-name}", map.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("setLevel")) {

				if (!ComWarfare.hasPerm(sender, "com.modifyLevel"))
					return true;

				if (args.length < 3) {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod setLevel (player) (level)"));
					return true;
				}

				int level;
				Player player = Bukkit.getPlayer(args[1]);

				if(player == null) {
					ComWarfare.sendMessage(sender, Lang.ERROR_PLAYER_NOT_EXISTS.getMessage());
					return true;
				}

				try {
					level = Integer.parseInt(args[2]);
					if (level > ProgressionManager.getInstance().maxLevel)
						throw new NumberFormatException();
				} catch(NumberFormatException e) {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod setLevel (player) (level)"));
					return true;
				}

				ProgressionManager.getInstance().setLevel(player, level, true);
				ProgressionManager.getInstance().saveData(player);
				ComWarfare.sendMessage(sender, Lang.SET_LEVEL_SUCCESS.getMessage().replace("{player}", player.getDisplayName()).replace("{level}", level + ""));
				return true;
			} else if (args[0].equalsIgnoreCase("removeSpawns")) {
				if (!ComWarfare.hasPerm(sender, "com.removeSpawns"))
					return true;

				if (args.length < 2) {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod removeSpawns (map)"));
					return true;
				}

				String mapName = args[1];
				CodMap map = GameManager.getMapForName(mapName);

				if (map == null) {
					ComWarfare.sendMessage(sender, Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				if (SpawnRemover.isShowingSpawns(map)) {
					SpawnRemover.clearSpawns(map);
					ComWarfare.sendMessage(sender, Lang.SPAWN_REMOVER_DEACTIVATED.getMessage());
				} else {
					if (SpawnRemover.showSpawns(map))
						ComWarfare.sendMessage(sender, Lang.SPAWN_REMOVER_ACTIVATED.getMessage());
					else
						ComWarfare.sendMessage(sender, Lang.MAP_IN_USE.getMessage());
				}

				return true;
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!ComWarfare.hasPerm(sender, "com.map.modify"))
					return true;

				if (args.length < 2) {
					ComWarfare.sendMessage(sender, ComWarfare.getPrefix() + Lang.INCORRECT_USAGE.getMessage().replace("{command}", "/cod reload (map)"));
					return true;
				}

				CodMap map = GameManager.getMapForName(args[1]);

				if (map == null) {
					ComWarfare.sendMessage(sender, Lang.MAP_NOT_EXISTS_WITH_NAME.getMessage());
					return true;
				}

				if (GameManager.usedMaps.contains(map)) {
					ComWarfare.sendMessage(sender, Lang.MAP_IN_USE.getMessage());
					return true;
				}

				map.setEnable();

				ComWarfare.sendMessage(sender, Lang.MAP_RELOADED.getMessage());

				return true;
			} else {
				sender.sendMessage(ComWarfare.getPrefix() + Lang.UNKNOWN_COMMAND.getMessage());
				return true;
			}
		}

		return true;
	}
}
