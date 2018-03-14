package com.rhetorical.cod.object;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.rhetorical.cod.CreditManager;
import com.rhetorical.cod.GameManager;
import com.rhetorical.cod.Main;
import com.rhetorical.cod.StatHandler;

public class GameInstance implements Listener {

	private long id;

	private ArrayList<Player> players = new ArrayList<Player>();
	private CodMap currentMap;
	public GameInstance gm = this;
	private int gameTime;
	private int lobbyTime;
	public String fancyTime;

	private GameState state;

	private ArrayList<Player> blueTeam = new ArrayList<Player>();
	private ArrayList<Player> redTeam = new ArrayList<Player>();

	private int BlueTeamScore;
	private int RedTeamScore;

	private boolean forceStarted = false;

	public final int maxScore_TDM;
	public final int maxScore_RSB;
	public final int maxScore_FFA;
	public final int maxScore_DOM;
	public final int maxScore_CTF;
	public final int maxScore_KC;
	public final int maxScore_GUN;
	public final int maxScore_OITC;

	// Score management and game information system for FFA (Free for all)
	public HashMap<Player, Integer> ffaPlayerScores = new HashMap<Player, Integer>();
	public HashMap<Player, BossBar> freeForAllBar = new HashMap<Player, BossBar>();

	public BossBar scoreBar = Bukkit.createBossBar(
			"§7«§f" + getFancyTime(Main.getPlugin().getConfig().getInt("lobbyTime")) + "§r§7»", BarColor.PINK,
			BarStyle.SOLID);

	public HealthManager health;

	public HashMap<Player, CodScore> playerScores = new HashMap<Player, CodScore>();

	public GameInstance(ArrayList<Player> pls, CodMap map) {

		this.id = Calendar.getInstance().getTimeInMillis();

		this.players = pls;
		this.currentMap = map;
		this.gameTime = Main.getPlugin().getConfig().getInt("gameTime." + this.getGameMode().toString());
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

		Main.cs.sendMessage(Main.codPrefix + "§7Game lobby with id " + this.getId() + " created with map "
				+ this.getMap().getName() + " with gamemode " + this.getGameMode() + "!");
	}

	public void reset() {

		this.RedTeamScore = 0;
		this.BlueTeamScore = 0;
		ffaPlayerScores.clear();

		this.state = GameState.WAITING;

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

	public void changeMap(CodMap map) {
		if (map == null)
			return;

		Gamemode gameMode = this.getGameMode();
		this.gameTime = Main.getPlugin().getConfig().getInt("gameTime." + gameMode.toString());
		return;
	}

	public void addPlayer(Player p) {
		// if (players.contains(p)) {
		// return;
		// }

		if (players.size() >= 12)
			return;

		this.health.addPlayer(p);

		Main.progManager.update(p);

		p.getInventory().clear();

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

	public void addBluePoint() {
		this.BlueTeamScore++;
		return;
	}

	public void addRedPoint() {
		this.RedTeamScore++;
		return;
	}

	public void addPointForPlayer(Player p) {
		if (!this.ffaPlayerScores.containsKey(p)) {
			this.ffaPlayerScores.put(p, 0);
		}

		this.ffaPlayerScores.put(p, this.ffaPlayerScores.get(p) + 1);

		return;
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

	public void startGame() {

		if (this.isForceStarted()) {
			this.forceStart(false);
		}

		assignTeams();
		this.playerScores.clear();

		for (Player p : players) {

			this.playerScores.put(p, new CodScore(p));

			if (currentMap.getGamemode() != Gamemode.FFA) {
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

	public void spawnCodPlayer(Player p, Location L) {
		p.teleport(L);
		p.getInventory().clear();
		Loadout loadout = Main.loadManager.getActiveLoadout(p);

		/*
		 * TODO: - Gather randomized loadout for RSB - Gather loadouts for
		 * GunGame, Infected, and One in the Chamber
		 * 
		 */

		if (this.getGameMode() == Gamemode.RSB) {

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

		} else if (this.getGameMode() != Gamemode.RSB && this.getGameMode() != Gamemode.INFECT
				&& this.getGameMode() != Gamemode.OITC && this.getGameMode() != Gamemode.GUN) {

			p.getInventory().setItem(0, Main.loadManager.knife);
			p.getInventory().setItem(1, loadout.getPrimary().getGun());
			p.getInventory().setItem(2, loadout.getSecondary().getGun());
			p.getInventory().setItem(3, loadout.getLethal().getWeapon());
			p.getInventory().setItem(4, loadout.getTactical().getWeapon());

			// Ammo

			ItemStack primaryAmmo = loadout.getPrimary().getAmmo();
			primaryAmmo.setAmount(loadout.getPrimary().getAmmoCount());

			ItemStack secondaryAmmo = loadout.getSecondary().getAmmo();
			secondaryAmmo.setAmount(loadout.getSecondary().getAmmoCount());

			p.getInventory().setItem(19, primaryAmmo);
			p.getInventory().setItem(25, secondaryAmmo);

		}
	}

	public void assignTeams() {

		if (this.getGameMode() != Gamemode.FFA && this.getGameMode() != Gamemode.OITC
				&& this.getGameMode() != Gamemode.GUN) {
			for (Player p : players) {
				// check if any players are partied and put them on a team
				// together

				if (blueTeam.contains(p) || redTeam.contains(p))
					continue;

				if (redTeam.size() >= blueTeam.size()) {
					blueTeam.add(p);
					p.sendMessage(Main.codPrefix + "§9You are on the blue team!");
				} else {
					redTeam.add(p);
					p.sendMessage(Main.codPrefix + "§cYou are on the red team!");
				}
			}
		} else {
			for (Player p : players) {
				if (this.ffaPlayerScores.containsKey(p))
					continue;

				p.sendMessage(Main.codPrefix + "§dYou are on the pink team!");
			}
		}

	}

	public void stopGame() {

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

		this.setState(GameState.STOPPING);

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = 10;

			public void run() {
				for (Player p : game.players) {
					for (int i = 0; i < 100; i++) {
						p.sendMessage("");
					}

					String teamFormat = "";

					if (currentMap.getGamemode() != Gamemode.FFA) {
						if (getWinningTeam().equalsIgnoreCase("red")) {
							teamFormat = "§cRED";
						} else if (getWinningTeam().equalsIgnoreCase("blue")) {
							teamFormat = "§9BLUE";
						} else if (getWinningTeam().equalsIgnoreCase("nobody")
								|| getWinningTeam().equalsIgnoreCase("tie")) {
							p.sendMessage(Main.codPrefix + "§7Nobody won the match! It was a tie!");
							p.sendMessage(Main.codPrefix + "§fReturning to the lobby in " + Integer.toString(t)
									+ " seconds!");
							CodScore score = playerScores.get(p);

							float kd = ((float) score.getKills() / (float) score.getDeaths());

							if (Float.isNaN(kd) || Float.isInfinite(kd)) {
								kd = score.getKills();
							}

							p.sendMessage("§a§lKills: " + score.getKills() + " §c§lDeaths: " + score.getDeaths()
									+ " §f§lKDR: " + kd);
							continue;
						}

						p.sendMessage(Main.codPrefix + "§fThe " + teamFormat + " §r§fteam won the match!");
						p.sendMessage(
								Main.codPrefix + "§fReturning to the lobby in " + Integer.toString(t) + " seconds!");
						CodScore score = playerScores.get(p);

						float kd = ((float) score.getKills() / (float) score.getDeaths());

						if (Float.isNaN(kd) || Float.isInfinite(kd)) {
							kd = score.getKills();
						}

						p.sendMessage("§a§lKills: " + score.getKills() + " §c§lDeaths: " + score.getDeaths()
								+ " §f§lKDR: " + kd);
					} else {
						p.sendMessage(Main.codPrefix + "§e" + getWinningTeam() + " §r§fwon the match!");
						p.sendMessage(
								Main.codPrefix + "§fReturning to the lobby in " + Integer.toString(t) + " seconds!");
						CodScore score = playerScores.get(p);
						float kd = ((float) score.getKills() / (float) score.getDeaths());

						if (Float.isNaN(kd) || Float.isInfinite(kd)) {
							kd = score.getKills();
						}

						p.sendMessage("§a§lKills: " + score.getKills() + " §c§lDeaths: " + score.getDeaths()
								+ " §f§lKDR: " + kd);
					}
				}

				t--;

				if (t <= 0) {
					game.reset();
					this.cancel();
				}

			}
		};

		br.runTaskTimerAsynchronously(Main.getPlugin(), 0L, 20L);
	}

	public void startLobbyTimer(int time) {

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

				scoreBar.setTitle("§7«§f" + counter + "§r§7»");

				Double progress = (((double) t) / ((double) lobbyTime));

				scoreBar.setProgress(progress);

				if (t % 30 == 0 || (t % 10 == 0 && t < 30) || (t % 5 == 0 && t < 15)) {
					for (Player p : game.players) {

						if (t == 0) {
							p.sendMessage(Main.codPrefix + "§7Game starting now!");
							continue;
						}

						p.sendMessage(Main.codPrefix + "§7Game starting in " + getFancyTime(t) + "!");
						p.sendMessage(Main.codPrefix + "§7Map: §a" + game.currentMap.getName() + " §r§7Gamemode: §c"
								+ game.currentMap.getGamemode().toString());
					}
				}

				if (t <= 0 || game.isForceStarted()) {

					startGame();

					this.cancel();
				}

				t--;
			}
		};

		br.runTaskTimerAsynchronously(Main.getPlugin(), 0L, 20L);
	}

	public void startGameTimer(int time) {

		this.setState(GameState.INGAME);

		scoreBar.removeAll();

		for (Player p : players) {
			if (!currentMap.getGamemode().equals(Gamemode.FFA)) {
				scoreBar.addPlayer(p);
			} else {
				freeForAllBar.put(p,
						Bukkit.createBossBar(
								"§7«§f" + getFancyTime(Main.getPlugin().getConfig().getInt("gameTime.FFA")) + "§r§7»",
								BarColor.PINK, BarStyle.SOLID));
				freeForAllBar.get(p).addPlayer(p);
			}
		}

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {

			int t = time;
			int gameTime = time;

			@Override
			public void run() {

				String counter = getFancyTime(t);

				if (currentMap.getGamemode() != Gamemode.FFA) {
					scoreBar.setTitle(
							"§cRED: " + RedTeamScore + " §7«§f" + counter + "§r§7»" + " §9BLU: " + BlueTeamScore);
				} else {

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
						freeForAllBar.get(p)
								.setTitle("§a" + p.getDisplayName() + ": " + ffaPlayerScores.get(p) + " §7«§f" + counter
										+ "§r§7»" + " §6" + highestScorer.getDisplayName() + ": "
										+ ffaPlayerScores.get(highestScorer));
						freeForAllBar.get(p).setProgress(progress);
					}
				}

				Double progress = (((double) t) / ((double) gameTime));

				scoreBar.setProgress(progress);

				game.updateTabList();

				if (currentMap.getGamemode() == Gamemode.TDM || currentMap.getGamemode() == Gamemode.RSB
						|| currentMap.getGamemode() == Gamemode.DOM || currentMap.getGamemode() == Gamemode.CTF
						|| currentMap.getGamemode() == Gamemode.KC) {
					if (BlueTeamScore >= maxScore_TDM || RedTeamScore >= maxScore_TDM) {
						stopGame();
						this.cancel();
					}
				} else if (currentMap.getGamemode() == Gamemode.FFA) {
					for (Player p : players) {
						if (ffaPlayerScores.get(p) >= maxScore_FFA) {
							stopGame();
							this.cancel();
						}
					}
				}

				if (t <= 0) {

					stopGame();

					this.cancel();
				}

				t--;
			}

		};

		br.runTaskTimerAsynchronously(Main.getPlugin(), 0L, 20L);
	}

	public void resetScoreBoard() {
		if (this.getGameMode() != Gamemode.FFA) {
			this.scoreBar = Bukkit.createBossBar(Color.RED + "RED: 0" + "     " + "«" + this.getFancyTime(gameTime)
					+ "»" + "     " + Color.BLUE + "BLUE: 0", BarColor.WHITE, BarStyle.SEGMENTED_10);
		} else {
			this.scoreBar = Bukkit.createBossBar(Color.RED + "YOU: 0" + "     " + "«" + this.getFancyTime(gameTime)
					+ "»" + "     " + Color.BLUE + "1ST: 0", BarColor.WHITE, BarStyle.SEGMENTED_10);
		}
	}

	public String getWinningTeam() {

		if (this.getGameMode().equals(Gamemode.FFA)) {
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

	public String getFancyTime(int time) {

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

	public boolean areEnemies(Player a, Player b) {

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

		GameInstance game = this;

		BukkitRunnable br = new BukkitRunnable() {
			int t = 3;

			public void run() {

				p.getInventory().clear();
				p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 1));

				if (t > 0) {

					p.getInventory().clear();
					p.setGameMode(GameMode.SPECTATOR);
					p.setSpectatorTarget(killer);

					if (t == 3)
						p.sendMessage(Main.codPrefix + "§cYou will respawn in " + t + " seconds!");
				} else if (t <= 1) {
					if (game.state == GameState.INGAME) {
						if (currentMap.getGamemode() != Gamemode.FFA) {
							if (blueTeam.contains(p)) {
								// spawnCodPlayer(p,
								// Main.playerLoadouts.get(p).get(0),
								// this.currentMap.getBlueSpawn());
								spawnCodPlayer(p, game.currentMap.getBlueSpawn());
								p.setHealth(20D);
							} else if (redTeam.contains(p)) {
								// spawnCodPlayer(p,
								// Main.playerLoadouts.get(p).get(0),
								// this.currentMap.getRedSpawn());
								spawnCodPlayer(p, game.currentMap.getRedSpawn());
								p.setHealth(20D);
							} else {
								assignTeams();
							}

							p.setGameMode(GameMode.SURVIVAL);
							p.setHealth(20D);
							p.setFoodLevel(20);
							this.cancel();
						} else {
							spawnCodPlayer(p, game.currentMap.getPinkSpawn());
							p.setGameMode(GameMode.SURVIVAL);
							p.setHealth(20D);
							p.setFoodLevel(20);
							this.cancel();
						}
					} else {
						p.setGameMode(GameMode.SURVIVAL);
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

	public void updateTabList() {

		String teamColor;

		for (Player p : this.players) {

			if (this.isOnRedTeam(p)) {
				teamColor = "§c";
			} else if (this.isOnBlueTeam(p)) {
				teamColor = "§9";
			} else {
				teamColor = "§d";
			}

			CodScore score = this.playerScores.get(p);

			p.setPlayerListName(teamColor + "[" + Main.progManager.getLevel(p) + "]" + p.getDisplayName() + " K "
					+ score.getKills() + " / D " + score.getDeaths() + " / S " + score.getKillstreak());

		}
	}

	public boolean isOnRedTeam(Player p) {
		if (this.redTeam.contains(p))
			return true;

		return false;
	}

	public boolean isOnBlueTeam(Player p) {
		if (this.blueTeam.contains(p))
			return true;

		return false;
	}

	public boolean isOnPinkTeam(Player p) {
		if (this.ffaPlayerScores.containsKey(p))
			return true;

		return false;
	}

	public CodMap getMap() {

		if (this.currentMap == null) {
			this.changeMap(GameManager.pickRandomMap());
		}

		return this.currentMap;
	}

	public void setMap(CodMap codMap) {
		this.currentMap = codMap;
	}

	public boolean isForceStarted() {
		return forceStarted;
	}

	public void forceStart(boolean forceStarted) {
		this.forceStarted = forceStarted;
	}

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public Gamemode getGameMode() {
		return this.getMap().getGamemode();
	}

	public void handleDeath(Player killer, Player victim) {

		RankPerks rank = Main.getRank(killer);

		/*
		 * TODO: - Make kill messages show up above action bar - Create
		 * variables for how much xp and whether or not to give them donator xp,
		 * same for credits - Create config option to not give credits in game
		 * and only per level
		 * 
		 */
		if (this.getGameMode().equals(Gamemode.TDM) || this.getGameMode().equals(Gamemode.RSB)
				|| this.getGameMode().equals(Gamemode.DOM)) {
			if (redTeam.contains(killer)) {
				killer.sendMessage("§c§lYOU §r§f[killed] §r§9§l" + victim.getDisplayName());
				killer.sendMessage("§e+" + rank.getKillExperience() + "xp");

				Main.progManager.addExperience(killer, rank.getKillExperience());
				CreditManager.setCredits(killer, CreditManager.getCredits(killer) + rank.getKillCredits());
				this.kill(victim, killer);
				this.addRedPoint();
				this.updateScores(victim, killer, rank);
				return;
			} else if (blueTeam.contains(killer)) {
				killer.sendMessage("§9§lYOU §r§f[killed] §r§c§l" + victim.getDisplayName());
				killer.sendMessage("§e+" + rank.getKillExperience() + "xp");
				Main.progManager.addExperience(killer, rank.getKillExperience());
				this.kill(victim, killer);
				this.addBluePoint();
				this.updateScores(victim, killer, rank);
				return;
			}
		} else if (this.getGameMode().equals(Gamemode.FFA)) {
			killer.sendMessage("§a§lYOU §r§f[killed] §r§6§l" + victim.getDisplayName());
			killer.sendMessage("§e+" + rank.getKillExperience() + "xp");
			Main.progManager.addExperience(killer, rank.getKillExperience());
			this.kill(victim, killer);
			this.addPointForPlayer(killer);
			this.updateScores(victim, killer, rank);
			return;
		}
	}

	public void updateScores(Player victim, Player killer, RankPerks rank) {

		if (this.playerScores.get(killer) == null) {
			this.playerScores.put(killer, new CodScore(killer));
		}

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

	/*
	 * 
	 * GAMEMODE LISTENERS --------------------
	 * 
	 * Gamemode listeners contain all the listeners for each gamemode. What will
	 * be a part of this: Death listeners per gamemode (and kill), pick up
	 * object (for CTF and KC).
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	@EventHandler
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
			attacker.sendMessage(Main.codPrefix + "§7Try not to attack your teammates!");
			e.setDamage(0);
			return;
		}

		double damage = e.getDamage();

		ItemStack heldWeapon = attacker.getInventory().getItemInMainHand();

		if (heldWeapon.getType() == Material.DIAMOND_SWORD || heldWeapon.getType() == Material.GOLD_SWORD
				|| heldWeapon.getType() == Material.IRON_SWORD || heldWeapon.getType() == Material.STONE_SWORD
				|| heldWeapon.getType() == Material.WOOD_SWORD) {
			damage = Main.defaultHealth;
		} else {
			damage = Math.round(Main.defaultHealth / 4);
		}

		this.health.damage(victim, damage);

		/*
		 * Death handlers per gamemode are handled here
		 * 
		 * TODO: - Update statistics on death for players - Send kill
		 * notification messages to players above action bar - Add gungame
		 * support - Add one in the chamber support
		 */

		if (this.health.isDead(victim)) {
			this.handleDeath(attacker, victim);
		}

	}

	@EventHandler
	public void preventInventoryMovement(InventoryClickEvent e) {

		Player p = (Player) e.getWhoClicked();

		if (this.getPlayers().contains(p)) {
			e.setCancelled(true);
			return;
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

		if (GameManager.isInMatch(victim) || GameManager.isInMatch(shooter)) {
			e.setCancelled(true);
		} else {
			return;
		}

		if (!areEnemies(shooter, victim)) {
			shooter.sendMessage(Main.codPrefix + "§7Try not to attack your teammates!");
			return;
		}

		if (!this.health.isDead(victim)) {
			this.health.damage(victim, damage);

			// Gamemode settings
			// TODO: Update statistics on death for players

			if (this.health.isDead(victim)) {
				this.handleDeath(shooter, victim);
			}

			// TODO: Add catches for gungame and other
		} else {
			return;
		}

	}
}
