package com.rhetorical.cod.object;

import com.rhetorical.cod.*;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GameInstance implements Listener {

	private long id;

	private ArrayList<Player> players = new ArrayList<>();
	private CodMap currentMap;
	private int gameTime;
	private int lobbyTime;

	private GameState state;

	private ArrayList<Player> blueTeam = new ArrayList<>();
	private ArrayList<Player> redTeam = new ArrayList<>();

	private int BlueTeamScore;
	private int RedTeamScore;

	private boolean forceStarted = false;

	private final int maxScore_TDM,
			maxScore_RSB,
			maxScore_FFA,
			maxScore_DOM,
			maxScore_CTF,
			maxScore_KC,
			maxScore_GUN,
			maxScore_OITC,
			maxScore_DESTROY,
			maxScore_RESCUE;

	private Item redFlag;
	private Item blueFlag;

	private ArmorStand aFlag, bFlag, cFlag;

	private int aFlagCapture, bFlagCapture, cFlagCapture; // Range: -10 to 10. Lower is red, higher is blue.

	// Score management and game information system for FFA (Free for all)
	private HashMap<Player, Integer> ffaPlayerScores = new HashMap<>();
	private HashMap<Player, BossBar> freeForAllBar = new HashMap<>();

	private BossBar scoreBar = Bukkit.createBossBar(ChatColor.GRAY + "«" + ChatColor.WHITE + getFancyTime(Main.getPlugin().getConfig().getInt("lobbyTime")) + ChatColor.RESET + "" + ChatColor.GRAY + "»", BarColor.PINK, BarStyle.SOLID);

	public HealthManager health;

	private HashMap<Player, CodScore> playerScores = new HashMap<>();

	private CodGun oneShotGun;

	private boolean oneShotReady;

	public GameInstance(ArrayList<Player> pls, CodMap map) {

		id = System.currentTimeMillis();

		players = pls;
		currentMap = map;

		Main.getPlugin().reloadConfig();

		if (getGamemode() != Gamemode.INFECT) {
			gameTime = Main.getPlugin().getConfig().getInt("gameTime." + getGamemode().toString());
		} else {
			if (ComVersion.getPurchased())
				gameTime = Main.getPlugin().getConfig().getInt("maxScore.INFECT");
			else
				gameTime = 120;
		}
		lobbyTime = Main.getPlugin().getConfig().getInt("lobbyTime");

		if (ComVersion.getPurchased()) {
			maxScore_TDM = Main.getPlugin().getConfig().getInt("maxScore.TDM");
			maxScore_CTF = Main.getPlugin().getConfig().getInt("maxScore.CTF");
			maxScore_DOM = Main.getPlugin().getConfig().getInt("maxScore.DOM");
			maxScore_FFA = Main.getPlugin().getConfig().getInt("maxScore.FFA");
			maxScore_RSB = Main.getPlugin().getConfig().getInt("maxScore.RSB");
			maxScore_KC = Main.getPlugin().getConfig().getInt("maxScore.KC");
			maxScore_GUN = Main.getPlugin().getConfig().getInt("maxScore.GUN");
			maxScore_OITC = Main.getPlugin().getConfig().getInt("maxScore.OITC");
			maxScore_DESTROY = Main.getPlugin().getConfig().getInt("maxScore.DESTROY");
			maxScore_RESCUE = Main.getPlugin().getConfig().getInt("maxScore.RESCUE");
		} else {
			maxScore_TDM = 75;
			maxScore_RSB = 75;
			maxScore_FFA = 30;
			maxScore_KC = 50;
			maxScore_DOM = 200;
			maxScore_CTF = 3;
			maxScore_OITC = 3;
			maxScore_DESTROY = 4;
			maxScore_RESCUE = 4;
			maxScore_GUN = 20;
		}

		setState(GameState.WAITING);

		health = new HealthManager(pls, Main.defaultHealth);

		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());

		for (Player p : pls) {
			health.update(p);
		}

		String oneShotGunName = Main.getPlugin().getConfig().getString("OITC_Gun");

		for (CodGun gun : Main.shopManager.getSecondaryGuns()) {
			if (gun.getName().equalsIgnoreCase(oneShotGunName)) {
				oneShotGun = gun;
				break;
			}
		}

		if (oneShotGun == null)
			for (CodGun gun : Main.shopManager.getPrimaryGuns()) {
				if (gun.getName().equals(oneShotGunName)) {
					oneShotGun = gun;
					break;
				}
			}

		oneShotReady = oneShotGun != null;

		System.gc();

		Main.cs.sendMessage(Main.codPrefix + ChatColor.GRAY + "Game lobby with id " + getId() + " created with map " + getMap().getName() + " with gamemode " + getGamemode() + ".");
	}

	private void reset() {

		RedTeamScore = 0;
		BlueTeamScore = 0;
		ffaPlayerScores.clear();

		setState(GameState.WAITING);

		changeMap(GameManager.pickRandomMap());

		health = new HealthManager(players, Main.defaultHealth);

		for (Player p : players) {
			health.update(p);
			p.getInventory().clear();
			p.teleport(Main.lobbyLoc);
		}

		playerScores.clear();

		if (players.size() >= Main.minPlayers) {
			startLobbyTimer(lobbyTime);
		}
	}

	public long getId() {
		return id;
	}

	private void changeMap(CodMap map) {
		if (map == null)
			return;

		map.changeGamemode();
		Gamemode gameMode = getGamemode();
		gameTime = Main.getPlugin().getConfig().getInt("gameTime." + gameMode.toString());
	}

	public void addPlayer(Player p) {

		if (players.size() >= 12)
			return;

		if (players.contains(p))
			return;

		health.addPlayer(p);

		Main.progManager.update(p);

		p.getInventory().clear();

		Main.killstreakManager.loadStreaks(p);

		p.setGameMode(GameMode.SURVIVAL);
		p.setHealth(20D);
		p.setFoodLevel(20);
		Main.progManager.update(p);

		p.teleport(Main.lobbyLoc);

		players.add(p);

		playerScores.put(p, new CodScore(p));

		scoreBar.addPlayer(p);

		if (getState() == GameState.INGAME) {
			assignTeams();

			if (getGamemode() != Gamemode.DESTROY && getGamemode() != Gamemode.RESCUE) {

				if (isOnRedTeam(p)) {
					spawnCodPlayer(p, currentMap.getRedSpawn());
				} else if (isOnBlueTeam(p)) {
					spawnCodPlayer(p, currentMap.getBlueSpawn());
				} else {
					spawnCodPlayer(p, currentMap.getPinkSpawn());
				}
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
		}

		if ((players.size() >= Main.minPlayers) && getState() == GameState.WAITING) {
			startLobbyTimer(lobbyTime);
			setState(GameState.STARTING);
		}
	}

	private void addBluePoint() {
		BlueTeamScore++;
	}

	private void addRedPoint() {
		RedTeamScore++;
	}

	private void addPointForPlayer(Player p) {
		if (!ffaPlayerScores.containsKey(p)) {
			ffaPlayerScores.put(p, 0);
		}

		ffaPlayerScores.put(p, ffaPlayerScores.get(p) + 1);
	}

	public void removePlayer(Player p) {
		if (!players.contains(p))
			return;

		if (scoreBar.getPlayers().contains(p)) {
			scoreBar.removePlayer(p);
		}

		if (freeForAllBar.containsKey(p)) {
			if (freeForAllBar.get(p) == null) {
				freeForAllBar.remove(p);
			}

			BossBar bar = freeForAllBar.get(p);

			bar.removeAll();

			freeForAllBar.remove(p);
		}

		health.removePlayer(p);

		if (playerScores.containsKey(p)) {
			playerScores.remove(p);
		}

		players.remove(p);
		ffaPlayerScores.remove(p);

		if (players.size() == 0) {
			GameManager.removeInstance(this);
		}

		System.gc();
	}

	private void startGame() {

		if (forceStarted) {
			forceStarted = false;
		}

		assignTeams();
		playerScores.clear();

		for (Player p : players) {

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
				spawnCodPlayer(p, currentMap.getPinkSpawn());
			}
		}

		if (getGamemode() == Gamemode.DESTROY || getGamemode() == Gamemode.RESCUE) {
			for (Player p : players) {
				isAlive.put(p, true);
			}
		}

		startGameTimer(gameTime, false);
		setState(GameState.INGAME);
	}

	private void dropFlag(Item flag, Location location) {
		location.getWorld().dropItem(location, flag.getItemStack());
	}

	private void setupFlags(boolean red, boolean blue) {
		if (red) {
			Location spawn = currentMap.getRedFlagSpawn();
			ItemStack flag = new ItemStack(Material.RED_BANNER);
			redFlag = spawn.getWorld().dropItem(spawn, flag);
		}

		if (blue) {
			Location spawn = currentMap.getBlueFlagSpawn();
			ItemStack flag = new ItemStack(Material.BLUE_BANNER);
			blueFlag = spawn.getWorld().dropItem(spawn, flag);
		}
	}

	private void spawnCodPlayer(Player p, Location L) {
		p.teleport(L);
		p.getInventory().clear();
		p.setGameMode(GameMode.ADVENTURE);
		p.setHealth(20d);
		p.setFoodLevel(20);
		Loadout loadout = Main.loadManager.getActiveLoadout(p);
		Main.killstreakManager.streaksAfterDeath(p);

		if (blueTeam.contains(p)) {
			setTeamArmor(p, Color.BLUE);
		} else if (redTeam.contains(p)) {
			if (getGamemode() == Gamemode.INFECT) {
				setTeamArmor(p, Color.GREEN);
			} else {
				setTeamArmor(p, Color.RED);
			}
		} else {
			setTeamArmor(p, Color.PURPLE);
		}

		if (getGamemode() == Gamemode.RSB) {

			CodGun primary = Main.loadManager.getRandomPrimary();

			CodGun secondary = Main.loadManager.getRandomSecondary();

			CodWeapon lethal = Main.loadManager.getRandomLethal();

			CodWeapon tactical = Main.loadManager.getRandomTactical();

			ItemStack primaryAmmo = primary.getAmmo();
			primaryAmmo.setAmount(primary.getAmmoCount());

			ItemStack secondaryAmmo = secondary.getAmmo();
			secondaryAmmo.setAmount(secondary.getAmmoCount());


			p.getInventory().setItem(0, Main.loadManager.knife);
			if (!primary.equals(Main.loadManager.blankPrimary)) {
				p.getInventory().setItem(1, primary.getGun());
				p.getInventory().setItem(19, primaryAmmo);
			}

			if (!secondary.equals(Main.loadManager.blankSecondary)) {
				p.getInventory().setItem(2, secondary.getGun());
				p.getInventory().setItem(25, secondaryAmmo);
			}

			if (Math.random() > 0.5 && !lethal.equals(Main.loadManager.blankLethal)) {
				p.getInventory().setItem(3, lethal.getWeapon());
			}

			if (Math.random() > 0.5 && !lethal.equals(Main.loadManager.blankTactical)) {
				p.getInventory().setItem(4, tactical.getWeapon());
			}

		} else if (getGamemode() == Gamemode.DOM
				|| getGamemode() == Gamemode.CTF
				|| getGamemode() == Gamemode.KC
				|| getGamemode() == Gamemode.TDM
				|| getGamemode() == Gamemode.FFA
				|| getGamemode() == Gamemode.INFECT
				|| getGamemode() == Gamemode.DESTROY
				|| getGamemode() == Gamemode.RESCUE) {

			p.getInventory().setItem(0, Main.loadManager.knife);

			if (getGamemode() != Gamemode.INFECT || (getGamemode() == Gamemode.INFECT && blueTeam.contains(p))) {
				if (!loadout.getPrimary().equals(Main.loadManager.blankPrimary)) {
					p.getInventory().setItem(1, loadout.getPrimary().getGun());

					ItemStack primaryAmmo = loadout.getPrimary().getAmmo();
					primaryAmmo.setAmount(loadout.getPrimary().getAmmoCount());
					p.getInventory().setItem(19, primaryAmmo);
				}

				if (!loadout.getSecondary().equals(Main.loadManager.blankSecondary)) {
					p.getInventory().setItem(2, loadout.getSecondary().getGun());
					if (!loadout.hasPerk(Perk.ONE_MAN_ARMY)) {
						ItemStack secondaryAmmo = loadout.getSecondary().getAmmo();
						secondaryAmmo.setAmount(loadout.getSecondary().getAmmoCount());
						p.getInventory().setItem(25, secondaryAmmo);
					}
				}

				if (!loadout.getLethal().equals(Main.loadManager.blankLethal))
					p.getInventory().setItem(3, loadout.getLethal().getWeapon());

				if (!loadout.getTactical().equals(Main.loadManager.blankTactical))
					p.getInventory().setItem(4, loadout.getTactical().getWeapon());
			}

			if (getGamemode() == Gamemode.INFECT && redTeam.contains(p)) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * gameTime, 1));
			}

		}
	}

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

			if (blueTeam.contains(p))
				meta.setDisplayName(ChatColor.BLUE + p.getName() + "'s dogtag");
			else
				meta.setDisplayName(ChatColor.RED + p.getName() + "'s dogtag");

			List<String> lore = new ArrayList<>();

			if (blueTeam.contains(p))
				lore.add(p.getUniqueId().toString());
			else
				lore.add(p.getUniqueId().toString());

			meta.setLore(lore);
			dogtag.setItemMeta(meta);



			p.getWorld().dropItem(p.getLocation(), dogtag).setCustomNameVisible(true);
		}
	}

	private void assignTeams() {

		if (getGamemode() != Gamemode.FFA && getGamemode() != Gamemode.OITC && getGamemode() != Gamemode.GUN) {
			for (Player p : players) {
				if (blueTeam.contains(p) || redTeam.contains(p))
					continue;

				if (redTeam.size() >= blueTeam.size()) {
					blueTeam.add(p);
					Main.sendMessage(p, Main.codPrefix + ChatColor.BLUE + "You are on the blue team!", Main.lang);
				} else {
					redTeam.add(p);
					Main.sendMessage(p, Main.codPrefix + ChatColor.RED + "You are on the red team!", Main.lang);
				}
			}
		} else if (getGamemode() == Gamemode.INFECT) {
			for (Player p : players) {
				if (redTeam.size() != 0) {
					blueTeam.add(p);
					continue;
				}

				redTeam.add(p);
			}
		} else {
			for (Player p : players) {
				if (ffaPlayerScores.containsKey(p))
					continue;

				Main.sendMessage(p, Main.codPrefix + ChatColor.LIGHT_PURPLE + "You are on the pink team!", Main.lang);
			}
		}

	}

	private void stopGame() {

		for (Player p : players) {

			if (freeForAllBar.containsKey(p)) {
				freeForAllBar.get(p).removeAll();
			}

			if (!scoreBar.getPlayers().contains(p)) {
				scoreBar.addPlayer(p);
			}

			p.getInventory().clear();

			Main.progManager.saveData(p);

			StatHandler.saveStatData();
		}

		if (getGamemode() == Gamemode.DOM)
			despawnDomFlags();

		setState(GameState.STOPPING);

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = 10;

			public void run() {
				for (Player p : game.players) {
					for (int i = 0; i < 100; i++) {
						Main.sendMessage(p, "", Main.lang);
					}

					String teamFormat = "";

					if (currentMap.getGamemode() != Gamemode.FFA) {
						if (getWinningTeam().equalsIgnoreCase("red")) {
							teamFormat = ChatColor.RED + "RED";
						} else if (getWinningTeam().equalsIgnoreCase("blue")) {
							teamFormat = ChatColor.DARK_BLUE + "BLUE";
						} else if (getWinningTeam().equalsIgnoreCase("nobody") || getWinningTeam().equalsIgnoreCase("tie")) {
							Main.sendMessage(p, Main.codPrefix + ChatColor.GRAY + "Nobody won the match! It was a tie!", Main.lang);
							Main.sendMessage(p, Main.codPrefix + ChatColor.WHITE + "Returning to the lobby in " + Integer.toString(t) + " seconds!", Main.lang);
							playerScores.computeIfAbsent(p, k -> new CodScore(p));
							CodScore score = playerScores.get(p);

							float kd = ((float) score.getKills() / (float) score.getDeaths());

							if (Float.isNaN(kd) || Float.isInfinite(kd)) {
								kd = score.getKills();
							}

							Main.sendMessage(p, ChatColor.GREEN + "" + ChatColor.BOLD + "Kills: " + score.getKills() + " " + ChatColor.RED + "" + ChatColor.BOLD + "Deaths: " + score.getDeaths() + " " + ChatColor.WHITE + "" + ChatColor.BOLD + "KDR: " + kd, Main.lang);
							continue;
						}

						Main.sendMessage(p, Main.codPrefix + ChatColor.WHITE + "The " + teamFormat + ChatColor.RESET + "" + ChatColor.WHITE + " team won the match!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + ChatColor.WHITE +"Returning to the lobby in " + Integer.toString(t) + " seconds!", Main.lang);
						CodScore score = playerScores.get(p);

						float kd = ((float) score.getKills() / (float) score.getDeaths());

						if (Float.isNaN(kd) || Float.isInfinite(kd)) {
							kd = score.getKills();
						}

						Main.sendMessage(p, ChatColor.GREEN + "" + ChatColor.BOLD + "Kills: " + score.getKills() + " " + ChatColor.RED + ChatColor.BOLD + "Deaths: " + score.getDeaths() + " " + ChatColor.WHITE + "" + ChatColor.BOLD + "KDR: " + kd, Main.lang);
					} else {
						Main.sendMessage(p, Main.codPrefix + ChatColor.YELLOW + getWinningTeam() + " " + ChatColor.RESET + "" + ChatColor.WHITE + "won the match!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + ChatColor.WHITE + "Returning to the lobby in " + Integer.toString(t) + " seconds!", Main.lang);
						CodScore score = playerScores.get(p);
						float kd = ((float) score.getKills() / (float) score.getDeaths());

						if (Float.isNaN(kd) || Float.isInfinite(kd)) {
							kd = score.getKills();
						}

						Main.sendMessage(p, ChatColor.GREEN + "" + ChatColor.BOLD + "Kills: " + score.getKills() + " " + ChatColor.RED + "" + ChatColor.BOLD + "Deaths: " + score.getDeaths() + " " + ChatColor.WHITE + "" + ChatColor.BOLD + "KDR: " + kd, Main.lang);
					}
				}

				t--;

				if (t <= 0) {
					game.reset();
					cancel();
				}

			}
		};

		br.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}

	private void startLobbyTimer(int time) {

		if (forceStarted) {
			forceStarted = false;
		}

		setState(GameState.STARTING);

		scoreBar.removeAll();
		for (Player p : players) {
			scoreBar.addPlayer(p);
		}

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = time;

			int lobbyTime = time;

			@Override
			public void run() {

				String counter = getFancyTime(t);

				scoreBar.setTitle(ChatColor.GRAY + "«" + ChatColor.WHITE + counter + ChatColor.RESET + "" + ChatColor.GRAY + "»");

				Double progress = (((double) t) / ((double) lobbyTime));

				scoreBar.setProgress(progress);

				if (t % 30 == 0 || (t % 10 == 0 && t < 30) || (t % 5 == 0 && t < 15)) {
					for (Player p : game.players) {

						if (t == 0) {
							Main.sendMessage(p, Main.codPrefix + ChatColor.GRAY + "Game starting now!", Main.lang);
							continue;
						}

						Main.sendMessage(p, Main.codPrefix + ChatColor.GRAY + "Game starting in " + getFancyTime(t) + "!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + ChatColor.GRAY + "Map: " + ChatColor.GREEN + game.currentMap.getName() + ChatColor.RESET + ChatColor.GRAY + " Gamemode: " + ChatColor.RED + game.currentMap.getGamemode().toString(), Main.lang);
					}
				}

				if (t <= 0 || forceStarted) {

					startGame();

					cancel();
				}

				t--;
			}
		};

		br.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}

	private void startGameTimer(int time, boolean newRound) {

		if (!newRound) {
			setState(GameState.INGAME);

			scoreBar.removeAll();

			for (Player p : players) {
				if (!currentMap.getGamemode().equals(Gamemode.FFA)) {
					scoreBar.addPlayer(p);
				} else {
					freeForAllBar.put(p, Bukkit.createBossBar(ChatColor.GRAY + "«" + ChatColor.WHITE + getFancyTime(Main.getPlugin().getConfig().getInt("gameTime.FFA")) + ChatColor.RESET + ChatColor.WHITE + "»", BarColor.PINK, BarStyle.SOLID));
					freeForAllBar.get(p).addPlayer(p);
				}
			}

			if (getGamemode().equals(Gamemode.DOM)) {
				spawnDomFlags();
			}
		} else {
			for (Player p : players) {
				if (isOnBlueTeam(p)) {
					spawnCodPlayer(p, getMap().getBlueSpawn());
				} else if (isOnRedTeam(p)) {
					spawnCodPlayer(p, getMap().getRedSpawn());
				} else {
					assignTeams();
				}

			}
		}
		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = time;
			int gameTime = time;

			@Override
			public void run() {

				if (t == 0) {

					stopGame();

					cancel();
					return;
				}

				t--;

				String counter = getFancyTime(t);


				if (currentMap.getGamemode() != Gamemode.FFA) {
					scoreBar.setTitle(ChatColor.RED + "RED: " + RedTeamScore + ChatColor.GRAY + " «" + ChatColor.WHITE + counter + ChatColor.RESET + ChatColor.GRAY + "»" + ChatColor.DARK_BLUE + " BLU: " + BlueTeamScore);
				} else {

					if (currentMap.getGamemode() == Gamemode.DOM) {
						game.checkFlags();
					}

					Player highestScorer = Bukkit.getPlayer(getWinningTeam());

					for (Player p : players) {
						if (highestScorer == null) {
							highestScorer = p;
						}

						if (!ffaPlayerScores.containsKey(p)) {
							ffaPlayerScores.put(p, 0);
						}

						if (!ffaPlayerScores.containsKey(highestScorer)) {
							ffaPlayerScores.put(highestScorer, 0);
						}
						Double progress = (((double) t) / ((double) gameTime));
						freeForAllBar.get(p).setTitle(ChatColor.GREEN + p.getDisplayName() + ": " + ffaPlayerScores.get(p) + ChatColor.GRAY + " «" + ChatColor.WHITE + counter + ChatColor.RESET + ChatColor.GRAY + "»" + " " + ChatColor.GOLD + highestScorer.getDisplayName() + ": " + ffaPlayerScores.get(highestScorer));
						freeForAllBar.get(p).setProgress(progress);
					}
				}

				Double progress = (((double) t) / ((double) gameTime));

				scoreBar.setProgress(progress);

				game.updateTabList();

				if (currentMap.getGamemode() == Gamemode.TDM || currentMap.getGamemode() == Gamemode.RSB || currentMap.getGamemode() == Gamemode.DOM || currentMap.getGamemode() == Gamemode.CTF || currentMap.getGamemode() == Gamemode.KC) {
					if (BlueTeamScore >= maxScore_TDM || RedTeamScore >= maxScore_TDM && getGamemode().equals(Gamemode.TDM)) {
						endGameByScore(this);
						return;
					}

					if (BlueTeamScore >= maxScore_RSB || RedTeamScore >= maxScore_RSB && getGamemode().equals(Gamemode.RSB)) {
						endGameByScore(this);
						return;
					}

					if (BlueTeamScore >= maxScore_DOM || RedTeamScore >= maxScore_DOM && getGamemode().equals(Gamemode.DOM)) {
						endGameByScore(this);
						return;
					}

					if (BlueTeamScore >= maxScore_CTF || RedTeamScore >= maxScore_CTF && getGamemode().equals(Gamemode.CTF)) {
						endGameByScore(this);
						return;
					}

					if (BlueTeamScore >= maxScore_KC || RedTeamScore >= maxScore_KC && getGamemode().equals(Gamemode.KC)) {
						endGameByScore(this);
						return;
					}
				}

				if (getGamemode() == Gamemode.DESTROY || getGamemode() == Gamemode.RESCUE) {
					if (getAlivePlayers(redTeam) == 0) {
						addBluePoint();

						if (!(BlueTeamScore >= maxScore_DESTROY) && !(BlueTeamScore >= maxScore_RESCUE)) {
							startNewRound(7, blueTeam);
						}

						for (Player pp : players) {
							isAlive.put(pp, true);
						}
						cancel();
					} else if (getAlivePlayers(blueTeam) == 0) {
						addRedPoint();

						if (!(RedTeamScore >= maxScore_DESTROY) && !(RedTeamScore >= maxScore_RESCUE)) {
							startNewRound(7, redTeam);
						}

						for (Player pp : players) {
							isAlive.put(pp, true);
						}
						cancel();
					}

					if (BlueTeamScore >= maxScore_DESTROY || RedTeamScore >= maxScore_DESTROY && getGamemode().equals(Gamemode.DESTROY)) {
						endGameByScore(this);
						cancel();
						return;
					}else if (BlueTeamScore >= maxScore_RESCUE || RedTeamScore >= maxScore_RESCUE && getGamemode().equals(Gamemode.RESCUE)) {
						endGameByScore(this);
						cancel();
						return;
					}
				}

				if (currentMap.getGamemode().equals(Gamemode.FFA)) {
					for (Player p : players) {
						if (ffaPlayerScores.get(p) >= maxScore_FFA) {
							endGameByScore(this);
							return;
						}
					}
				}

				if(currentMap.getGamemode().equals(Gamemode.OITC)) {
					for (Player p : players) {
						if (ffaPlayerScores.get(p) >= maxScore_OITC) {
							endGameByScore(this);
							return;
						}
					}
				}

				if (currentMap.getGamemode().equals(Gamemode.GUN)) {
					for (Player p : players) {
						if (ffaPlayerScores.get(p) >= maxScore_GUN) {
							endGameByScore(this);
							return;
						}
					}
				}

			}

		};

		br.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}

	private void startNewRound(int delay, List<Player> prevRWT) {
		for(Player p : players) {
			if (prevRWT != null && !prevRWT.isEmpty()) {
				if (prevRWT.equals(blueTeam)) {
                    p.sendTitle("\u00A79The blue team won the round!", String.format("The next round will start in %s seconds!", delay), 10, 20, 10);
                } else if (prevRWT.equals(redTeam)) {
                    p.sendTitle("\u00A7cThe red team won the round!", String.format("The next round will start in %s seconds!", delay), 10, 20, 10);
                }
			}
		}

		BukkitRunnable br = new BukkitRunnable() {
			@Override
			public void run() {
				startGameTimer(gameTime, true);
			}
		};

		br.runTaskLater(Main.getPlugin(), 20L * (long) delay);
	}

	private void endGameByScore(BukkitRunnable runnable) {
		stopGame();
		runnable.cancel();
	}

	public void resetScoreBoard() {
		if (getGamemode() != Gamemode.FFA) {
			scoreBar = Bukkit.createBossBar(Color.RED + "RED: 0" + "     " + "«" + getFancyTime(gameTime) + "»" + "     " + Color.BLUE + "BLUE: 0", BarColor.WHITE, BarStyle.SEGMENTED_10);
		} else {
			scoreBar = Bukkit.createBossBar(Color.RED + "YOU: 0" + "     " + "«" + getFancyTime(gameTime) + "»" + "     " + Color.BLUE + "1ST: 0", BarColor.WHITE, BarStyle.SEGMENTED_10);
		}
	}

	private String getWinningTeam() {

		if (getGamemode().equals(Gamemode.FFA) || getGamemode().equals(Gamemode.OITC) || getGamemode().equals(Gamemode.GUN)) {
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

		if (RedTeamScore > BlueTeamScore) {
			return "red";
		} else if (BlueTeamScore > RedTeamScore) {
			return "blue";
		}

		return "tie";
	}

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

		if (getGamemode() != Gamemode.DESTROY && getGamemode() != Gamemode.RESCUE)
			return 1;

		for (Player p : team) {
			if (isAlive.get(p)) {
				count++;
			}
		}

		return count;
	}


	public void kill(Player p, Player killer) {

		Main.killstreakManager.kill(p, killer);

		if (getGamemode() == Gamemode.DESTROY || getGamemode() == Gamemode.RESCUE) {
			p.setGameMode(GameMode.SPECTATOR);
			p.getInventory().clear();
			isAlive.put(p, false);

			if (getGamemode() == Gamemode.RESCUE) {
				dropDogTag(p);
				if (isOnBlueTeam(p) && getAlivePlayers(blueTeam) > 0) {
					Main.sendTitle(p, Main.codPrefix + ChatColor.RED + "You will respawn if your teammate picks up your dog tag!", "");
				} else if (isOnRedTeam(p) && getAlivePlayers(redTeam) > 0) {
					Main.sendTitle(p, Main.codPrefix + ChatColor.RED + "You will respawn if your teammate picks up your dog tag!", "");
				}
			} else {
				Main.sendTitle(p, Main.codPrefix + ChatColor.RED + "You will respawn next round!", "");
			}

			return;
		}


		if (getGamemode() == Gamemode.KC) {
			dropDogTag(p);
		}

		if (getGamemode() == Gamemode.INFECT && redTeam.contains(killer)) {
			if (blueTeam.contains(p)) {
				blueTeam.remove(p);
			}

			redTeam.add(p);
		}

		BukkitRunnable br = new BukkitRunnable() {
			int t = 3;

			public void run() {

				p.getInventory().clear();
				p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 1));
				p.removePotionEffect(PotionEffectType.SPEED);

				if (t > 0) {

					p.getInventory().clear();
					p.setGameMode(GameMode.SPECTATOR);
					p.setSpectatorTarget(killer);

					if (t == 3)
						Main.sendTitle(p, Main.codPrefix + ChatColor.RED + "You will respawn in " + t + " seconds!", "");
				} else if (t <= 1) {
					if (getState() == GameState.INGAME) {
						if (getGamemode() != Gamemode.FFA) {
							if (blueTeam.contains(p)) {
								spawnCodPlayer(p, getMap().getBlueSpawn());
							} else if (redTeam.contains(p)) {
								spawnCodPlayer(p, getMap().getRedSpawn());
							} else {
								assignTeams();
							}

							cancel();
						} else {
							spawnCodPlayer(p, getMap().getPinkSpawn());
							cancel();
						}
					} else {
						p.setGameMode(GameMode.ADVENTURE);
						p.teleport(Main.lobbyLoc);
						p.setHealth(20D);
						p.setFoodLevel(20);
						cancel();
					}
				} else {
					cancel();
				}

				t--;
			}
		};

		br.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}

	private void updateTabList() {

		String teamColor;

		for (Player p : players) {

			if (isOnRedTeam(p)) {
				teamColor = ChatColor.RED + "";
			} else if (isOnBlueTeam(p)) {
				teamColor = ChatColor.DARK_BLUE + "";
			} else {
				teamColor = ChatColor.LIGHT_PURPLE + "";
			}

			CodScore score = playerScores.get(p);

			p.setPlayerListName(teamColor + "[" + Main.progManager.getLevel(p) + "]" + p.getDisplayName() + " K " + score.getKills() + " / D " + score.getDeaths() + " / S " + score.getKillstreak());

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
			changeMap(GameManager.pickRandomMap());
		}

		return currentMap;
	}

	public boolean forceStart(boolean forceStarted) {
		this.forceStarted = forceStarted;
		return forceStarted;
	}

	private GameState getState() {
		return state;
	}

	private void setState(GameState state) {
		this.state = state;
	}

	private Gamemode getGamemode() {
		return getMap().getGamemode();
	}

	private void handleDeath(Player killer, Player victim) {

		RankPerks rank = Main.getRank(killer);

		Main.killstreakManager.kill(victim, killer);

		if (getGamemode().equals(Gamemode.TDM) || getGamemode().equals(Gamemode.KC) || getGamemode().equals(Gamemode.RSB) || getGamemode().equals(Gamemode.DOM) || getGamemode().equals(Gamemode.RESCUE) || getGamemode().equals(Gamemode.DESTROY)) {
			if (isOnRedTeam(killer)) {

				double xp = rank.getKillExperience();

				if (getGamemode().equals(Gamemode.KC)) {
					xp /= 2d;
				}

				Main.sendMessage(killer, "" + ChatColor.RED + ChatColor.BOLD + "YOU " + ChatColor.RESET + "" + ChatColor.WHITE + "[killed] " + ChatColor.RESET + ChatColor.DARK_BLUE + ChatColor.BOLD + victim.getDisplayName(), Main.lang);
				Main.sendTitle(killer, "", ChatColor.YELLOW + "+" + xp + "xp");

				Main.progManager.addExperience(killer, xp);
				CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
				kill(victim, killer);
				if (getGamemode() != Gamemode.RESCUE && getGamemode() != Gamemode.DESTROY && getGamemode() != Gamemode.KC) {
					addRedPoint();
				}
				updateScores(victim, killer, rank);
			} else if (isOnBlueTeam(killer)) {

				double xp = rank.getKillExperience();

				if (getGamemode().equals(Gamemode.KC)) {
					xp /= 2d;
				}

				Main.sendMessage(killer,  "" + ChatColor.DARK_BLUE + ChatColor.BOLD + "YOU " + ChatColor.RESET + ChatColor.WHITE + "[killed] " + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + victim.getDisplayName(), Main.lang);
				Main.sendTitle(killer, "", ChatColor.YELLOW + "+" + xp + "xp");
				Main.progManager.addExperience(killer, xp);
				kill(victim, killer);
				if (getGamemode() != Gamemode.RESCUE && getGamemode() != Gamemode.DESTROY && getGamemode() != Gamemode.KC) {
					addBluePoint();
				}
				updateScores(victim, killer, rank);
			}

			if (victim == redFlagHolder) {
				dropFlag(blueFlag, victim.getLocation());
				for (Player p : players) {
					Main.sendMessage(p, ChatColor.GREEN + "The " + ChatColor.DARK_BLUE + "blue " + ChatColor.GREEN + "flag has been dropped!", Main.lang);
				}
			}else if (victim == blueFlagHolder) {
				dropFlag(redFlag, victim.getLocation());
				for (Player p : players) {
					Main.sendMessage(p, ChatColor.GREEN + "The " + ChatColor.RED + "red " + ChatColor.GREEN + "flag has been dropped!", Main.lang);
				}
			}

		} else if (getGamemode().equals(Gamemode.CTF) || getGamemode().equals(Gamemode.INFECT)) {
			if (redTeam.contains(killer)) {
				Main.sendMessage(killer, "" + ChatColor.RED + ChatColor.BOLD + "YOU " + ChatColor.RESET + "" + ChatColor.WHITE + "[killed] " + ChatColor.RESET + ChatColor.DARK_BLUE + ChatColor.BOLD + victim.getDisplayName(), Main.lang);
				Main.sendTitle(killer, "", ChatColor.YELLOW + "+" + rank.getKillExperience() + "xp");

				Main.progManager.addExperience(killer, rank.getKillExperience());
				CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
				kill(victim, killer);
				updateScores(victim, killer, rank);
			} else if (blueTeam.contains(killer)) {
				Main.sendMessage(killer,  "" + ChatColor.DARK_BLUE + ChatColor.BOLD + "YOU " + ChatColor.RESET + ChatColor.WHITE + "[killed] " + ChatColor.RESET + ChatColor.RED + ChatColor.BOLD + victim.getDisplayName(), Main.lang);
				Main.sendTitle(killer, "", ChatColor.YELLOW + "+" + rank.getKillExperience() + "xp");
				Main.progManager.addExperience(killer, rank.getKillExperience());
				kill(victim, killer);
				updateScores(victim, killer, rank);
			}

		} else if (getGamemode().equals(Gamemode.FFA) || getGamemode().equals(Gamemode.GUN)) {
			Main.sendMessage(killer, "" + ChatColor.GREEN + ChatColor.BOLD + "YOU " + ChatColor.RESET + ChatColor.WHITE + "[killed] " + ChatColor.RESET	 + ChatColor.GOLD + ChatColor.BOLD + victim.getDisplayName(), Main.lang);
			Main.sendTitle(killer, "", ChatColor.YELLOW + "+" + rank.getKillExperience() + "xp");
			Main.progManager.addExperience(killer, rank.getKillExperience());
			kill(victim, killer);
			addPointForPlayer(killer);
			updateScores(victim, killer, rank);
		}
	}

	private void updateScores(Player victim, Player killer, RankPerks rank) {

		playerScores.computeIfAbsent(killer, k -> new CodScore(killer));

		CodScore killerScore = playerScores.get(killer);

		killerScore.addScore(rank.getKillExperience());

		killerScore.addKillstreak();

		killerScore.addKill();

		playerScores.put(killer, killerScore);

		if (playerScores.get(victim) == null) {
			playerScores.put(killer, new CodScore(victim));
		}

		CodScore victimScore = playerScores.get(victim);

		victimScore.setDeaths(victimScore.getDeaths() + 1);
		StatHandler.addDeath(victim);

		victimScore.resetKillstreak();

		playerScores.put(victim, victimScore);
	}

	/* Gamemode Listeners */

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerHit(EntityDamageByEntityEvent e) {

		if (!(e.getEntity() instanceof Player && e.getDamager() instanceof Player))
			return;

		Player victim = (Player) e.getEntity();
		Player attacker = (Player) e.getDamager();

		if (GameManager.isInMatch(victim) || GameManager.isInMatch(attacker)) {
			e.setCancelled(true);
		} else {
			return;
		}

		if (!players.contains(victim) && !players.contains(attacker))
			return;

		if (getState() != GameState.INGAME) {
			return;
		}

		if (!areEnemies(attacker, victim)) {
			e.setDamage(0);
			return;
		}

		double damage;

		ItemStack heldWeapon = attacker.getInventory().getItemInMainHand();

		if (heldWeapon.getType() == Material.DIAMOND_SWORD || heldWeapon.getType() == Material.GOLDEN_SWORD || heldWeapon.getType() == Material.IRON_SWORD || heldWeapon.getType() == Material.STONE_SWORD || heldWeapon.getType() == Material.WOODEN_SWORD) {
			damage = Main.defaultHealth;
		} else {
			damage = Math.round(Main.defaultHealth / 4);
		}

		health.damage(victim, damage);

		//TODO:
		// - Send kill notification messages to players above action bar
		// - Add GunGame support
		// - Add one in the chamber support

		if (health.isDead(victim)) {
			if (!Main.loadManager.getCurrentLoadout(victim).hasPerk(Perk.LAST_STAND)) {
				handleDeath(attacker, victim);
			} else {
				Main.perkListener.lastStand(victim, this);
			}
		}

	}

	@EventHandler
	public void preventInventoryMovement(InventoryClickEvent e) {

		Player p = (Player) e.getWhoClicked();

		if (getPlayers().contains(p)) {
			e.setCancelled(true);
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerHitByWeapon(EntityDamageByEntityEvent e) {

		Projectile bullet;

		if (e.getDamager() instanceof Projectile) {
			bullet = (Projectile) e.getDamager();
			if (!(bullet.getShooter() instanceof Player)) {
				return;
			}
		} else {
			return;
		}

		if (!(bullet.getShooter() instanceof Player))
			return;

		Player victim = (Player) e.getEntity();
		Player shooter = (Player) bullet.getShooter();

		Double damage = e.getDamage();

		if (!players.contains(victim) && !players.contains(shooter))
			return;

		if (getState() != GameState.INGAME) {
			return;
		}

		if (players.contains(victim) && players.contains(shooter)) {
			e.setCancelled(true);
		} else {
			return;
		}

		if (!areEnemies(shooter, victim)) {
			return;
		}
		if (!health.isDead(victim)) {
			health.damage(victim, damage);

			if (health.isDead(victim)) {
				if (!Main.loadManager.getCurrentLoadout(victim).hasPerk(Perk.LAST_STAND)) {
					handleDeath(shooter, victim);
				} else {
					Main.perkListener.lastStand(victim, this);
				}
			}
		}
	}

	private Player blueFlagHolder;
	private Player redFlagHolder;

	@EventHandler
	public void playerPutFlag(PlayerMoveEvent e) {

		if (!players.contains(e.getPlayer()) && (blueFlagHolder == e.getPlayer() || redFlagHolder == e.getPlayer()))
			return;

		Player p = e.getPlayer();

		if (p.getLocation() != getMap().getRedFlagSpawn() && p.getLocation() != getMap().getBlueFlagSpawn())
			return;

		if (p == redFlagHolder) {

			Location l = p.getLocation();

			if (l.distance(getMap().getRedFlagSpawn()) < 1) {
				setTeamArmor(p, Color.RED);
				addRedPoint();
				setupFlags(false, true);
				redFlagHolder = null;
				for (Player player : players) {
					Main.sendTitle(player, ChatColor.RED + "The red team scored!", "");
				}
			}

		} else {
			Location l = p.getLocation();

			if (l.distance(getMap().getBlueFlagSpawn()) < 1) {
				setTeamArmor(p, Color.BLUE);
				addBluePoint();
				setupFlags(true, false);
				blueFlagHolder = null;
				for (Player player : players) {
					Main.sendTitle(player,  ChatColor.DARK_BLUE + "The blue team scored!", "");
				}
			}
		}

	}

	@EventHandler
	public void onPlayerPickupFlag(EntityPickupItemEvent e) {
		if (!(e.getItem().equals(redFlag) || e.getItem().equals(blueFlag)))
			return;

		e.setCancelled(true);

		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player p = (Player) e.getEntity();
		if (!players.contains(p)) {
			return;
		}

		Item flag = e.getItem();

		if (isOnRedTeam(p)) {
			if (flag.equals(redFlag)) {
				setupFlags(true, false);
				flag.remove();
				return;
			}

			redFlagHolder = p;
		} else {
			if (flag.equals(blueFlag)) {
				setupFlags(false, true);
				flag.remove();
				return;
			}

			blueFlagHolder = p;
		}


		updateFlagHolder();

	}

	@EventHandler
	public void onPlayerPickupDogtag(EntityPickupItemEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;

		Player p = (Player) e.getEntity();

		if (!GameManager.isInMatch(p))
			return;

		if (!players.contains(p))
			return;

		ItemStack stack = e.getItem().getItemStack();

		e.setCancelled(true);
		e.getItem().remove();

		if (stack.getItemMeta().getLore().size() == 0) {
			return;
		}

		Player tagOwner = Bukkit.getPlayer(UUID.fromString(stack.getItemMeta().getLore().get(0)));

		if (!areEnemies(p, tagOwner)) {
			if (getGamemode() == Gamemode.RESCUE) {
				if (isOnBlueTeam(tagOwner)) {
					spawnCodPlayer(tagOwner, getMap().getBlueSpawn());
				} else if (isOnRedTeam(tagOwner)) {
					spawnCodPlayer(tagOwner, getMap().getRedSpawn());
				}
			} else if (getGamemode() == Gamemode.KC) {
				p.sendMessage(ChatColor.GRAY + "Kill Denied!");
				Main.sendTitle(p, "", ChatColor.YELLOW + "+" + (Main.getRank(p).getKillExperience() / 2) + "xp!");
				Main.progManager.addExperience(p, Main.getRank(p).getKillExperience() / 2);
			}
		} else {
			if (getGamemode() == Gamemode.RESCUE) {
				p.sendMessage(Main.codPrefix + ChatColor.RED + "You just denied " + tagOwner.getName() + " a respawn!");
			} else if (getGamemode() == Gamemode.KC) {
				p.sendMessage(ChatColor.GRAY + "Kill Confirmed!");
				Main.sendTitle(p, "", ChatColor.YELLOW + "+" + Main.getRank(p).getKillExperience() + "xp!");
				Main.progManager.addExperience(p, Main.getRank(p).getKillExperience());
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

	private void updateFlagHolder() {
		if (blueFlagHolder != null) {


			ItemStack flag = new ItemStack(Material.RED_BANNER);

			blueFlagHolder.getInventory().setHelmet(flag);
		}

		if(redFlagHolder != null) {

			ItemStack flag = new ItemStack(Material.BLUE_BANNER);

			redFlagHolder.getInventory().setHelmet(flag);
		}
	}

	private void spawnDomFlags() {
		if(!getGamemode().equals(Gamemode.DOM))
			return;

		Location aLoc = getMap().getAFlagSpawn();
		Location bLoc = getMap().getBFlagSpawn();
		Location cLoc = getMap().getCFlagSpawn();

		if(aLoc == null || bLoc == null || cLoc == null) {
			Main.sendMessage(Main.cs, Main.codPrefix + ChatColor.RED + "The Alpha, Beta, or Charlie flag spawns have not been set for the current map in arena id " + getId() + ". The game will likely not work properly.", Main.lang);
			return;
		}

		Main.sendMessage(Main.cs, "Spawning flags", Main.lang	);

		aFlag = (ArmorStand) aLoc.getWorld().spawnEntity(aLoc, EntityType.ARMOR_STAND);

		aFlag.setCustomName("Flag A");
		aFlag.setCustomNameVisible(true);
		aFlag.setVisible(true);
		aFlag.setGravity(true);

		bFlag = (ArmorStand) bLoc.getWorld().spawnEntity(bLoc, EntityType.ARMOR_STAND);

		bFlag.setCustomName("Flag B");
		bFlag.setCustomNameVisible(true);
		bFlag.setVisible(true);
		bFlag.setGravity(true);

		cFlag = (ArmorStand) cLoc.getWorld().spawnEntity(cLoc, EntityType.ARMOR_STAND);

		cFlag.setCustomName("Flag C");
		cFlag.setCustomNameVisible(true);
		cFlag.setVisible(true);
		cFlag.setGravity(true);

		Main.sendMessage(Main.cs, "Spawned flags", Main.lang);
	}

	private void despawnDomFlags() {
		if (aFlag != null)
			aFlag.remove();

		if (bFlag != null)
			bFlag.remove();

		if (cFlag != null)
			cFlag.remove();

		aFlagCapture = 0;
		bFlagCapture = 0;
		cFlagCapture = 0;
	}

	private void checkFlags() {
		if (!getGamemode().equals(Gamemode.DOM))
			return;

		List<Player> aPlayers = new ArrayList<>();
		List<Player> bPlayers = new ArrayList<>();
		List<Player> cPlayers = new ArrayList<>();

		for(Entity e : aFlag.getNearbyEntities(10, 10, 10)) {
			if (e instanceof Player) {
				aPlayers.add(((Player) e));
			}
		}

		for(Entity e : bFlag.getNearbyEntities(10, 10, 10)) {
			if (e instanceof Player) {
				bPlayers.add(((Player) e));
			}
		}

		for(Entity e : cFlag.getNearbyEntities(10, 10, 10)) {
			if (e instanceof Player) {
				cPlayers.add(((Player) e));
			}
		}

		for(int i = 0; i <= 2; i++) {
			if(i == 0 && aPlayers.isEmpty())
				continue;


			int blue = 0;
			int red = 0;

			List<Player> check = new ArrayList<>();

			if(i == 0)
				check = aPlayers;
			else if (i == 1)
				check = bPlayers;
			else if (i == 2)
				check = cPlayers;

			for(Player p : check) {
				if (isOnBlueTeam(p))
					blue++;
				else if(isOnRedTeam(p))
					red++;
			}

			if (i == 0) {
				if (aFlagCapture == 10 && blue >= red) {
					BlueTeamScore++;
				} else if (aFlagCapture == -10 && red >= blue) {
					RedTeamScore++;
				} else {
					aFlagCapture += blue - red;

					if (aFlagCapture > 10)
						aFlagCapture = 10;
					else if (aFlagCapture < -10)
						aFlagCapture = -10;

					if (aFlagCapture == 10) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "The " + ChatColor.DARK_BLUE + "BLUE " + ChatColor.YELLOW + "team has captured flag A!");
						}
					} else if (aFlagCapture == -10) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "The " + ChatColor.RED + "RED " + ChatColor.YELLOW + "team has captured flag A!");
						}
					} else if (aFlagCapture == 0) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "Flag A has been Neutralized!");
						}
					}
				}
			} else if (i == 1) {
				if (bFlagCapture == 10 && blue >= red) {
					BlueTeamScore++;
				} else if (bFlagCapture == -10 && red >= blue) {
					RedTeamScore++;
				} else {
					bFlagCapture += blue - red;

					if (bFlagCapture > 10)
						bFlagCapture = 10;
					else if (bFlagCapture < -10)
						bFlagCapture = -10;

					if (bFlagCapture == 10) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "The " + ChatColor.DARK_BLUE + "BLUE " + ChatColor.YELLOW + "team has captured flag B!");
						}
					} else if (bFlagCapture == -10) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "The " + ChatColor.RED + "RED " + ChatColor.YELLOW + "team has captured flag B!");
						}
					} else if (bFlagCapture == 0) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "Flag B has been Neutralized!");
						}
					}
				}
			} else if (i == 2) {
				if (cFlagCapture == 10 && blue >= red) {
					BlueTeamScore++;
				} else if (cFlagCapture == -10 && red >= blue) {
					RedTeamScore++;
				} else {
					cFlagCapture += blue - red;

					if (cFlagCapture > 10)
						cFlagCapture = 10;
					else if (cFlagCapture < -10)
						cFlagCapture = -10;

					if (cFlagCapture == 10) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "The " + ChatColor.DARK_BLUE + "BLUE " + ChatColor.YELLOW + "team has captured flag C!");
						}
					} else if (cFlagCapture == -10) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "The " + ChatColor.RED + "RED " + ChatColor.YELLOW + "team has captured flag C!");
						}
					} else if (cFlagCapture == 0) {
						for (Player p : getPlayers()) {
							p.sendMessage(ChatColor.YELLOW + "Flag C has been Neutralized!");
						}
					}
				}
			}
		}

	}

	private void setTeamArmor(Player p, Color color) {
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
		ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

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
}
