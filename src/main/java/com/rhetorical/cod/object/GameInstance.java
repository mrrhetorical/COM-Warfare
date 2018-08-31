package com.rhetorical.cod.object;

import com.rhetorical.cod.CreditManager;
import com.rhetorical.cod.GameManager;
import com.rhetorical.cod.Main;
import com.rhetorical.cod.StatHandler;
import org.bukkit.*;
import org.bukkit.block.Banner;
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
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

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

	private final int maxScore_TDM, maxScore_RSB, maxScore_FFA, maxScore_DOM, maxScore_CTF, maxScore_KC, maxScore_GUN,  maxScore_OITC;

	private Item redFlag;
	private Item blueFlag;

	private ArmorStand aFlag, bFlag, cFlag;

	private int aFlagCapture, bFlagCapture, cFlagCapture; // Range: -10 to 10. Lower is red, higher is blue.

	// Score management and game information system for FFA (Free for all)
	private HashMap<Player, Integer> ffaPlayerScores = new HashMap<>();
	private HashMap<Player, BossBar> freeForAllBar = new HashMap<>();

	private BossBar scoreBar = Bukkit.createBossBar("\u00A77«\u00A7f" + getFancyTime(Main.getPlugin().getConfig().getInt("lobbyTime")) + "\u00A7r\u00A77»", BarColor.PINK, BarStyle.SOLID);

	public HealthManager health;

	private HashMap<Player, CodScore> playerScores = new HashMap<Player, CodScore>();

	public GameInstance(ArrayList<Player> pls, CodMap map) {

		this.id = System.currentTimeMillis();

		this.players = pls;
		this.currentMap = map;

		Main.getPlugin().reloadConfig();

		this.gameTime = Main.getPlugin().getConfig().getInt("gameTime." + this.getGamemode().toString());
		this.lobbyTime = Main.getPlugin().getConfig().getInt("lobbyTime");
		this.maxScore_TDM = Main.getPlugin().getConfig().getInt("maxScore.TDM");
		this.maxScore_CTF = Main.getPlugin().getConfig().getInt("maxScore.CTF");
		this.maxScore_DOM = Main.getPlugin().getConfig().getInt("maxScore.DOM");
		this.maxScore_FFA = Main.getPlugin().getConfig().getInt("maxScore.FFA");
		this.maxScore_RSB = Main.getPlugin().getConfig().getInt("maxScore.RSB");
		this.maxScore_KC = Main.getPlugin().getConfig().getInt("maxScore.KC");
		this.maxScore_GUN = Main.getPlugin().getConfig().getInt("maxScore.GUN");
		this.maxScore_OITC = Main.getPlugin().getConfig().getInt("maxScore.OITC");

		this.setState(GameState.WAITING);

		health = new HealthManager(pls, Main.defaultHealth);

		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());

		for (Player p : pls) {
			health.update(p);
		}

		System.gc();

		Main.cs.sendMessage(Main.codPrefix + "\u00A77Game lobby with id " + this.getId() + " created with map " + this.getMap().getName() + " with gamemode " + this.getGamemode() + ".");
	}

	private void reset() {

		this.RedTeamScore = 0;
		this.BlueTeamScore = 0;
		ffaPlayerScores.clear();

		this.setState(GameState.WAITING);

		changeMap(GameManager.pickRandomMap());

		this.health = new HealthManager(players, Main.defaultHealth);

		for (Player p : this.players) {
			health.update(p);
			p.getInventory().clear();
			p.teleport(Main.lobbyLoc);
		}

		playerScores.clear();

		if (this.players.size() >= Main.minPlayers) {
			this.startLobbyTimer(this.lobbyTime);
		}
	}

	public long getId() {
		return this.id;
	}

	private void changeMap(CodMap map) {
		if (map == null)
			return;

		Gamemode gameMode = this.getGamemode();
		this.gameTime = Main.getPlugin().getConfig().getInt("gameTime." + gameMode.toString());
	}

	public void addPlayer(Player p) {

		if (players.size() >= 12)
			return;

		if (players.contains(p))
			return;

		this.health.addPlayer(p);

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

		if (this.getState() == GameState.INGAME) {
			this.assignTeams();
			if (this.isOnRedTeam(p)) {
				this.spawnCodPlayer(p, this.currentMap.getRedSpawn());
			} else if (this.isOnBlueTeam(p)) {
				this.spawnCodPlayer(p, this.currentMap.getBlueSpawn());
			} else {
				this.spawnCodPlayer(p, this.currentMap.getPinkSpawn());
			}
		}

		if ((players.size() >= Main.minPlayers) && this.getState() == GameState.WAITING) {
			startLobbyTimer(lobbyTime);
			this.setState(GameState.STARTING);
		}
	}

	private void addBluePoint() {
		this.BlueTeamScore++;
	}

	private void addRedPoint() {
		this.RedTeamScore++;
	}

	private void addPointForPlayer(Player p) {
		if (!this.ffaPlayerScores.containsKey(p)) {
			this.ffaPlayerScores.put(p, 0);
		}

		this.ffaPlayerScores.put(p, this.ffaPlayerScores.get(p) + 1);
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

		if (this.playerScores.containsKey(p)) {
			this.playerScores.remove(p);
		}

		players.remove(p);
		this.ffaPlayerScores.remove(p);

		if (this.players.size() == 0) {
			GameManager.removeInstance(this);
		}

		System.gc();
	}

	private void startGame() {

		if (this.isForceStarted()) {
			this.forceStart(false);
		}

		assignTeams();
		this.playerScores.clear();

		for (Player p : players) {

			this.playerScores.put(p, new CodScore(p));

			if (this.getGamemode() != Gamemode.FFA && this.getGamemode() != Gamemode.OITC && this.getGamemode() != Gamemode.GUN) {
				if (blueTeam.contains(p)) {
					spawnCodPlayer(p, this.currentMap.getBlueSpawn());
				} else if (redTeam.contains(p)) {
					spawnCodPlayer(p, this.currentMap.getRedSpawn());
				} else {
					assignTeams();
				}
			} else {
				spawnCodPlayer(p, this.currentMap.getPinkSpawn());
			}
		}

		startGameTimer(gameTime);
		this.setState(GameState.INGAME);
	}

	private void dropFlag(Item flag, Location location) {
		location.getWorld().dropItem(location, flag.getItemStack());
	}

	private void setupFlags(boolean red, boolean blue) {
		if (red) {
			Location spawn = this.currentMap.getRedFlagSpawn();
			ItemStack flag = new ItemStack(Material.RED_BANNER);
			redFlag = spawn.getWorld().dropItem(spawn, flag);
		}

		if (blue) {
			Location spawn = this.currentMap.getBlueFlagSpawn();
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

		if (this.getGamemode() == Gamemode.RSB) {

			CodGun primary = Main.loadManager.getRandomPrimary();
			CodGun secondary = Main.loadManager.getRandomSecondary();

			CodWeapon lethal = Main.loadManager.getRandomLethal();

			CodWeapon tactical = Main.loadManager.getRandomTactical();

			p.getInventory().setItem(0, Main.loadManager.knife);
			p.getInventory().setItem(1, primary.getGun());
			p.getInventory().setItem(2, secondary.getGun());

			if (Math.random() > 0.5) {
				p.getInventory().setItem(3, lethal.getWeapon());
			}

			if (Math.random() > 0.5) {
				p.getInventory().setItem(4, tactical.getWeapon());
			}

			// Ammo

			ItemStack primaryAmmo = primary.getAmmo();
			primaryAmmo.setAmount(primary.getAmmoCount());

			ItemStack secondaryAmmo = secondary.getAmmo();
			secondaryAmmo.setAmount(secondary.getAmmoCount());

			p.getInventory().setItem(19, primaryAmmo);
			p.getInventory().setItem(25, secondaryAmmo);

		} else if (getGamemode() == Gamemode.DOM
				|| getGamemode() == Gamemode.CTF
				|| getGamemode() == Gamemode.KC
				|| getGamemode() == Gamemode.TDM
				|| getGamemode() == Gamemode.FFA
				|| getGamemode() == Gamemode.INFECT) {

			p.getInventory().setItem(0, Main.loadManager.knife);

			if (getGamemode() != Gamemode.INFECT || (getGamemode() == Gamemode.INFECT && blueTeam.contains(p))) {
				p.getInventory().setItem(1, loadout.getPrimary().getGun());
				p.getInventory().setItem(2, loadout.getSecondary().getGun());
				p.getInventory().setItem(3, loadout.getLethal().getWeapon());
				p.getInventory().setItem(4, loadout.getTactical().getWeapon());
			}
			// Ammo

			ItemStack primaryAmmo = loadout.getPrimary().getAmmo();
			primaryAmmo.setAmount(loadout.getPrimary().getAmmoCount());

			if (!loadout.hasPerk(Perk.ONE_MAN_ARMY)) {
				ItemStack secondaryAmmo = loadout.getSecondary().getAmmo();
				secondaryAmmo.setAmount(loadout.getSecondary().getAmmoCount());
				p.getInventory().setItem(25, secondaryAmmo);
			}

			p.getInventory().setItem(19, primaryAmmo);

			if (getGamemode() == Gamemode.INFECT && redTeam.contains(p)) {
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * gameTime, 1));
			}

		}
	}

	private void assignTeams() {

		if (this.getGamemode() != Gamemode.FFA && this.getGamemode() != Gamemode.OITC && this.getGamemode() != Gamemode.GUN) {
			for (Player p : players) {
				if (blueTeam.contains(p) || redTeam.contains(p))
					continue;

				if (redTeam.size() >= blueTeam.size()) {
					blueTeam.add(p);
					Main.sendMessage(p, Main.codPrefix + "\u00A79You are on the blue team!", Main.lang);
				} else {
					redTeam.add(p);
					Main.sendMessage(p, Main.codPrefix + "\u00A7cYou are on the red team!", Main.lang);
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
				if (this.ffaPlayerScores.containsKey(p))
					continue;

				Main.sendMessage(p, Main.codPrefix + "\u00A7dYou are on the pink team!", Main.lang);
			}
		}

	}

	private void stopGame() {

		for (Player p : this.players) {

			if (this.freeForAllBar.containsKey(p)) {
				this.freeForAllBar.get(p).removeAll();
			}

			if (!this.scoreBar.getPlayers().contains(p)) {
				this.scoreBar.addPlayer(p);
			}

			p.getInventory().clear();

			Main.progManager.saveData(p);

			StatHandler.saveStatData();
		}

		if (this.getGamemode() == Gamemode.DOM)
			this.despawnDomFlags();

		this.setState(GameState.STOPPING);

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
							teamFormat = "\u00A7cRED";
						} else if (getWinningTeam().equalsIgnoreCase("blue")) {
							teamFormat = "\u00A79BLUE";
						} else if (getWinningTeam().equalsIgnoreCase("nobody") || getWinningTeam().equalsIgnoreCase("tie")) {
							Main.sendMessage(p, Main.codPrefix + "\u00A77Nobody won the match! It was a tie!", Main.lang);
							Main.sendMessage(p, Main.codPrefix + "\u00A7fReturning to the lobby in " + Integer.toString(t) + " seconds!", Main.lang);
							CodScore score = playerScores.get(p);

							float kd = ((float) score.getKills() / (float) score.getDeaths());

							if (Float.isNaN(kd) || Float.isInfinite(kd)) {
								kd = score.getKills();
							}

							Main.sendMessage(p, "\u00A7a\u00A7lKills: " + score.getKills() + " \u00A7c\u00A7lDeaths: " + score.getDeaths() + " \u00A7f\u00A7lKDR: " + kd, Main.lang);
							continue;
						}

						Main.sendMessage(p, Main.codPrefix + "\u00A7fThe " + teamFormat + " \u00A7r\u00A7fteam won the match!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + "\u00A7fReturning to the lobby in " + Integer.toString(t) + " seconds!", Main.lang);
						CodScore score = playerScores.get(p);

						float kd = ((float) score.getKills() / (float) score.getDeaths());

						if (Float.isNaN(kd) || Float.isInfinite(kd)) {
							kd = score.getKills();
						}

						Main.sendMessage(p, "\u00A7a\u00A7lKills: " + score.getKills() + " \u00A7c\u00A7lDeaths: " + score.getDeaths() + " \u00A7f\u00A7lKDR: " + kd, Main.lang);
					} else {
						Main.sendMessage(p, Main.codPrefix + "\u00A7e" + getWinningTeam() + " \u00A7r\u00A7fwon the match!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + "\u00A7fReturning to the lobby in " + Integer.toString(t) + " seconds!", Main.lang);
						CodScore score = playerScores.get(p);
						float kd = ((float) score.getKills() / (float) score.getDeaths());

						if (Float.isNaN(kd) || Float.isInfinite(kd)) {
							kd = score.getKills();
						}

						Main.sendMessage(p, "\u00A7a\u00A7lKills: " + score.getKills() + " \u00A7c\u00A7lDeaths: " + score.getDeaths() + " \u00A7f\u00A7lKDR: " + kd, Main.lang);
					}
				}

				t--;

				if (t <= 0) {
					game.reset();
					this.cancel();
				}

			}
		};

		br.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}

	private void startLobbyTimer(int time) {

		if (this.isForceStarted()) {
			this.forceStart(false);
		}

		this.setState(GameState.STARTING);

		scoreBar.removeAll();
		for (Player p : this.players) {
			scoreBar.addPlayer(p);
		}

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = time;

			int lobbyTime = time;

			@Override
			public void run() {

				String counter = getFancyTime(t);

				scoreBar.setTitle("\u00A77«\u00A7f" + counter + "\u00A7r\u00A77»");

				Double progress = (((double) t) / ((double) lobbyTime));

				scoreBar.setProgress(progress);

				if (t % 30 == 0 || (t % 10 == 0 && t < 30) || (t % 5 == 0 && t < 15)) {
					for (Player p : game.players) {

						if (t == 0) {
							Main.sendMessage(p, Main.codPrefix + "\u00A77Game starting now!", Main.lang);
							continue;
						}

						Main.sendMessage(p, Main.codPrefix + "\u00A77Game starting in " + getFancyTime(t) + "!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + "\u00A77Map: \u00A7a" + game.currentMap.getName() + " \u00A7r\u00A77Gamemode: \u00A7c" + game.currentMap.getGamemode().toString(), Main.lang);
					}
				}

				if (t <= 0 || game.isForceStarted()) {

					startGame();

					this.cancel();
				}
			}
		};

		br.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}

	private void startGameTimer(int time) {

		this.setState(GameState.INGAME);

		scoreBar.removeAll();

		for (Player p : players) {
			if (!currentMap.getGamemode().equals(Gamemode.FFA)) {
				scoreBar.addPlayer(p);
			} else {
				freeForAllBar.put(p, Bukkit.createBossBar("\u00A77«\u00A7f" + getFancyTime(Main.getPlugin().getConfig().getInt("gameTime.FFA")) + "\u00A7r\u00A77»", BarColor.PINK, BarStyle.SOLID));
				freeForAllBar.get(p).addPlayer(p);
			}
		}

		if (this.getGamemode().equals(Gamemode.DOM)) {
			this.spawnDomFlags();
		}

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = time;
			int gameTime = time;

			@Override
			public void run() {

				if (t <= 0) {

					stopGame();

					this.cancel();
					return;
				}

				t--;

				String counter = getFancyTime(t);


				if (currentMap.getGamemode() != Gamemode.FFA) {
					scoreBar.setTitle("\u00A7cRED: " + RedTeamScore + " \u00A77«\u00A7f" + counter + "\u00A7r\u00A77»" + " \u00A79BLU: " + BlueTeamScore);
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
						freeForAllBar.get(p).setTitle("\u00A7a" + p.getDisplayName() + ": " + ffaPlayerScores.get(p) + " \u00A77«\u00A7f" + counter + "\u00A7r\u00A77»" + " \u00A76" + highestScorer.getDisplayName() + ": " + ffaPlayerScores.get(highestScorer));
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

	private void endGameByScore(BukkitRunnable runnable) {
		stopGame();
		runnable.cancel();
	}

	public void resetScoreBoard() {
		if (this.getGamemode() != Gamemode.FFA) {
			this.scoreBar = Bukkit.createBossBar(Color.RED + "RED: 0" + "     " + "«" + this.getFancyTime(gameTime) + "»" + "     " + Color.BLUE + "BLUE: 0", BarColor.WHITE, BarStyle.SEGMENTED_10);
		} else {
			this.scoreBar = Bukkit.createBossBar(Color.RED + "YOU: 0" + "     " + "«" + this.getFancyTime(gameTime) + "»" + "     " + Color.BLUE + "1ST: 0", BarColor.WHITE, BarStyle.SEGMENTED_10);
		}
	}

	private String getWinningTeam() {

		if (this.getGamemode().equals(Gamemode.FFA)) {
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

		if (this.RedTeamScore > this.BlueTeamScore) {
			return "red";
		} else if (this.BlueTeamScore > this.RedTeamScore) {
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
		return this.players;
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

	public void kill(Player p, Player killer) {

		Main.killstreakManager.kill(p, killer);

		GameInstance game = this;

		if (getGamemode() == Gamemode.INFECT && redTeam.contains(killer))
		{
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
						Main.sendTitle(p, Main.codPrefix + "\u00A7cYou will respawn in " + t + " seconds!", "");
				} else if (t <= 1) {
					if (game.state == GameState.INGAME) {
						if (currentMap.getGamemode() != Gamemode.FFA) {
							if (blueTeam.contains(p) || redTeam.contains(p)) {
								spawnCodPlayer(p, game.currentMap.getBlueSpawn());
							} else {
								assignTeams();
							}

							this.cancel();
						} else {
							spawnCodPlayer(p, game.currentMap.getPinkSpawn());
							this.cancel();
						}
					} else {
						p.setGameMode(GameMode.ADVENTURE);
						p.teleport(Main.lobbyLoc);
						p.setHealth(20D);
						p.setFoodLevel(20);
						this.cancel();
					}
				} else {
					this.cancel();
				}

				t--;
			}
		};

		br.runTaskTimer(Main.getPlugin(), 0L, 20L);
	}

	private void updateTabList() {

		String teamColor;

		for (Player p : this.players) {

			if (this.isOnRedTeam(p)) {
				teamColor = "\u00A7c";
			} else if (this.isOnBlueTeam(p)) {
				teamColor = "\u00A79";
			} else {
				teamColor = "\u00A7d";
			}

			CodScore score = this.playerScores.get(p);

			p.setPlayerListName(teamColor + "[" + Main.progManager.getLevel(p) + "]" + p.getDisplayName() + " K " + score.getKills() + " / D " + score.getDeaths() + " / S " + score.getKillstreak());

		}
	}

	public boolean isOnRedTeam(Player p) {
		return this.redTeam.contains(p);
	}

	public boolean isOnBlueTeam(Player p) {
		return this.blueTeam.contains(p);

	}

	public boolean isOnPinkTeam(Player p) {
		return this.ffaPlayerScores.containsKey(p);

	}

	public CodMap getMap() {

		if (this.currentMap == null) {
			this.changeMap(GameManager.pickRandomMap());
		}

		return this.currentMap;
	}

	private boolean isForceStarted() {
		return forceStarted;
	}

	public void forceStart(boolean forceStarted) {
		this.forceStarted = forceStarted;
	}

	private GameState getState() {
		return state;
	}

	private void setState(GameState state) {
		this.state = state;
	}

	private Gamemode getGamemode() {
		return this.getMap().getGamemode();
	}

	private void handleDeath(Player killer, Player victim) {

		RankPerks rank = Main.getRank(killer);

		Main.killstreakManager.kill(victim, killer);

		if (this.getGamemode().equals(Gamemode.TDM) || this.getGamemode().equals(Gamemode.RSB) || this.getGamemode().equals(Gamemode.DOM)) {
			if (redTeam.contains(killer)) {
				Main.sendMessage(killer, "\u00A7c\u00A7lYOU \u00A7r\u00A7f[killed] \u00A7r\u00A79\u00A7l" + victim.getDisplayName(), Main.lang);
				Main.sendTitle(killer, "", "\u00A7e+" + rank.getKillExperience() + "xp");

				Main.progManager.addExperience(killer, rank.getKillExperience());
				CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
				this.kill(victim, killer);
				this.addRedPoint();
				this.updateScores(victim, killer, rank);
			} else if (blueTeam.contains(killer)) {
				Main.sendMessage(killer, "\u00A79\u00A7lYOU \u00A7r\u00A7f[killed] \u00A7r\u00A7c\u00A7l" + victim.getDisplayName(), Main.lang);
				Main.sendTitle(killer, "", "\u00A7e+" + rank.getKillExperience() + "xp");
				Main.progManager.addExperience(killer, rank.getKillExperience());
				this.kill(victim, killer);
				this.addBluePoint();
				this.updateScores(victim, killer, rank);
			}

			if (victim == redFlagHolder) {
				dropFlag(blueFlag, victim.getLocation());
				for (Player p : players) {
					Main.sendMessage(p, "\u00A7a The \u00A7b blue \u00A7a flag has been dropped!", Main.lang);
				}
				return;
			}

			if (victim == blueFlagHolder) {
				dropFlag(redFlag, victim.getLocation());
				for (Player p : players) {
					Main.sendMessage(p, "\u00A7a The \u00A7c red \u00A7a flag has been dropped!", Main.lang);
				}
				return;
			}

			return;
		}

		if (this.getGamemode().equals(Gamemode.CTF) || this.getGamemode().equals(Gamemode.INFECT)) {
			if (redTeam.contains(killer)) {
				Main.sendMessage(killer, "\u00A7c\u00A7lYOU \u00A7r\u00A7f[killed] \u00A7r\u00A79\u00A7l" + victim.getDisplayName(), Main.lang);
				Main.sendTitle(killer, "", "\u00A7e+" + rank.getKillExperience() + "xp");

				Main.progManager.addExperience(killer, rank.getKillExperience());
				CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
				this.kill(victim, killer);
				this.updateScores(victim, killer, rank);
			} else if (blueTeam.contains(killer)) {
				Main.sendMessage(killer, "\u00A79\u00A7lYOU \u00A7r\u00A7f[killed] \u00A7r\u00A7c\u00A7l" + victim.getDisplayName(), Main.lang);
				Main.sendTitle(killer, "", "\u00A7e+" + rank.getKillExperience() + "xp");
				Main.progManager.addExperience(killer, rank.getKillExperience());
				this.kill(victim, killer);
				this.updateScores(victim, killer, rank);
			}

			return;
		}

		if (this.getGamemode().equals(Gamemode.FFA) || this.getGamemode().equals(Gamemode.GUN)) {
			Main.sendMessage(killer, "\u00A7a\u00A7lYOU \u00A7r\u00A7f[killed] \u00A7r\u00A76\u00A7l" + victim.getDisplayName(), Main.lang);
			Main.sendTitle(killer, "", "\u00A7e+" + rank.getKillExperience() + "xp");
			Main.progManager.addExperience(killer, rank.getKillExperience());
			this.kill(victim, killer);
			this.addPointForPlayer(killer);
			this.updateScores(victim, killer, rank);
//			return;
		}
	}

	private void updateScores(Player victim, Player killer, RankPerks rank) {

		this.playerScores.computeIfAbsent(killer, k -> new CodScore(killer));

		CodScore killerScore = this.playerScores.get(killer);

		killerScore.addScore(rank.getKillExperience());

		killerScore.addKillstreak();

		killerScore.addKill();

		playerScores.put(killer, killerScore);

		if (this.playerScores.get(victim) == null) {
			this.playerScores.put(killer, new CodScore(victim));
		}

		CodScore victimScore = this.playerScores.get(victim);

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

		if (!this.players.contains(victim) && !this.players.contains(attacker))
			return;

		if (this.getState() != GameState.INGAME) {
			return;
		}

		if (!areEnemies(attacker, victim)) {
			e.setDamage(0);
			return;
		}

		double damage = e.getDamage();

		ItemStack heldWeapon = attacker.getInventory().getItemInMainHand();

		if (heldWeapon.getType() == Material.DIAMOND_SWORD || heldWeapon.getType() == Material.GOLDEN_SWORD || heldWeapon.getType() == Material.IRON_SWORD || heldWeapon.getType() == Material.STONE_SWORD || heldWeapon.getType() == Material.WOODEN_SWORD) {
			damage = Main.defaultHealth;
		} else {
			damage = Math.round(Main.defaultHealth / 4);
		}

		this.health.damage(victim, damage);

		//TODO:
		 //- Send kill notification messages to players above action bar
		 //- Add gungame support
		 //- Add one in the chamber support

		if (this.health.isDead(victim)) {
			if (!Main.loadManager.getCurrentLoadout(victim).hasPerk(Perk.LAST_STAND)) {
				this.handleDeath(attacker, victim);
			} else {
				Main.perkListener.lastStand(victim, this);
			}
		}

	}

	@EventHandler
	public void preventInventoryMovement(InventoryClickEvent e) {

		Player p = (Player) e.getWhoClicked();

		if (this.getPlayers().contains(p)) {
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

		if (!this.players.contains(victim) && !this.players.contains(shooter))
			return;

		if (this.getState() != GameState.INGAME) {
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
		if (!this.health.isDead(victim)) {
			this.health.damage(victim, damage);

			if (this.health.isDead(victim)) {
				if (!Main.loadManager.getCurrentLoadout(victim).hasPerk(Perk.LAST_STAND)) {
					this.handleDeath(shooter, victim);
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
					Main.sendTitle(player, "\u00A7cThe red team scored!", "");
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
					Main.sendTitle(player,  "\u00A79The blue team scored!", "");
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

		Location aLoc = this.getMap().getAFlagSpawn();
		Location bLoc = this.getMap().getBFlagSpawn();
		Location cLoc = this.getMap().getCFlagSpawn();

		if(aLoc == null || bLoc == null || cLoc == null) {
			Main.sendMessage(Main.cs, Main.codPrefix + "\u00A7The Alpha, Beta, or Charlie flag spawns have not been set for the current map in arena id " + this.getId() + ". The game will likely not work properly.", Main.lang);
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

		Main.sendMessage(Main.cs, "Spawned flags", Main.lang	);
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
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eThe \u00A79BLUE \u00A7eteam has captured flag A!");
						}
					} else if (aFlagCapture == -10) {
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eThe \u00A7cRED \u00A7eteam has captured flag A!");
						}
					} else if (aFlagCapture == 0) {
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eFlag A has been Neutralized!");
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
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eThe \u00A79BLUE \u00A7eteam has captured flag B!");
						}
					} else if (bFlagCapture == -10) {
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eThe \u00A7cRED \u00A7eteam has captured flag B!");
						}
					} else if (bFlagCapture == 0) {
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eFlag B has been Neutralized!");
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
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eThe \u00A79BLUE \u00A7eteam has captured flag C!");
						}
					} else if (cFlagCapture == -10) {
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eThe \u00A7cRED \u00A7eteam has captured flag C!");
						}
					} else if (cFlagCapture == 0) {
						for (Player p : this.getPlayers()) {
							p.sendMessage("\u00A7eFlag C has been Neutralized!");
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
