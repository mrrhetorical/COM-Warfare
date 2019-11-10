package com.rhetorical.cod;

import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.game.GameState;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.lang.LevelNames;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.progression.ProgressionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class Listeners implements Listener {

	/*
	 * GLOBAL LISTENERS -------------------- This file contains global listeners
	 * for anything having to do with COD-Warfare, but that is not necessarily
	 * tied to a specific game mode, and has to do with base function of the
	 * plugin outside of managing the game.
	 * 
	 * 
	 */

	@EventHandler
	public void playerLeaveGame(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		// remove from cod
		for (GameInstance i : GameManager.getRunningGames()) {
			if (i.getPlayers().contains(p)) {
				p.teleport(Main.getLobbyLocation());
				i.removePlayer(p);
				break;
			}
		}
	}

	@EventHandler
	public void playerJoinGame(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		CreditManager.loadCredits(p);

		if (Main.isServerMode()) {
			GameManager.findMatch(p);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerDropItem(PlayerDropItemEvent e) {
		Player p = e.getPlayer();

		if (GameManager.isInMatch(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void playerTalkInChat(AsyncPlayerChatEvent e) {
		Player sender = e.getPlayer();
		String message = e.getMessage();

		if (GameManager.isInMatch(sender))
			e.setCancelled(true);

		if (sender.hasPermission("com.chat"))
			return;

		GameInstance match = GameManager.getMatchWhichContains(sender);
		if (match == null)
			return;
		for (Player receiver : match.getPlayers()) {

				ChatColor tColor = ChatColor.GRAY;

				if (receiver.equals(sender)) {
					tColor = ChatColor.GREEN;
				} else if (match.isOnBlueTeam(sender)) {
					tColor = ChatColor.BLUE;
				} else if (match.isOnRedTeam(sender)) {
					tColor = ChatColor.RED;
				} else if (match.isOnPinkTeam(sender)) {
					tColor = ChatColor.LIGHT_PURPLE;
				}

				int level = ProgressionManager.getInstance().getLevel(sender);
				int pLevel = ProgressionManager.getInstance().getPrestigeLevel(sender);
				String prestige = pLevel > 0 ? ChatColor.WHITE + "[" + ChatColor.GREEN + pLevel + ChatColor.WHITE + "]-" : "";
				String levelName = LevelNames.getInstance().getLevelName(level);
				levelName = !levelName.equals("") ? "[" + levelName + "] " : "";

				String name = ChatColor.WHITE + levelName + prestige + "[" + level + "] "
						+ tColor + sender.getDisplayName();

				String msg = Lang.CHAT_FORMAT.getMessage();
				msg = msg.replace("{team-color}", tColor + "");
				msg = msg.replace("{player}", name);
				msg = msg.replace("{message}", message);

				receiver.sendMessage(msg);
		}

	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (GameManager.isInMatch(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerHitByWolf(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Wolf && e.getEntity() instanceof Player))
			return;

		Player p = (Player) e.getEntity();

		if (!GameManager.isInMatch(p))
			return;


		if (Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getState() != GameState.IN_GAME) {
			e.setCancelled(true);
			return;
		}

		double damage = Main.getDefaultHealth();
		for (Player pp : Objects.requireNonNull(GameManager.getMatchWhichContains(p)).dogsScoreStreak.keySet()) {
			for (Wolf w : Objects.requireNonNull(GameManager.getMatchWhichContains(p)).dogsScoreStreak.get(pp)) {
				if (w.getCustomName().equals(e.getDamager().getCustomName())) {
					Objects.requireNonNull(GameManager.getMatchWhichContains(p)).damagePlayer(p, damage, pp);
				}
			}
		}
		e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerHit(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) return;

		if (e.isCancelled())
			return;

		Player p = (Player) e.getEntity();
		
		DamageCause cause = e.getCause();
		
		double damage;
		double scalar;
		
		if (GameManager.isInMatch(p)) {

			if(Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getState() != GameState.IN_GAME) {
				e.setCancelled(true);
				return;
			}

			scalar = (Main.getDefaultHealth() / 20D);
			
			damage = e.getDamage() * scalar;
			
		} else {
			return;
		}

		if ((cause == DamageCause.DROWNING || cause == DamageCause.SUICIDE) && Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getState() == GameState.IN_GAME) {
			e.setCancelled(true);
			Objects.requireNonNull(GameManager.getMatchWhichContains(p)).kill(p, p);
			return;
		}

		if (cause != DamageCause.ENTITY_ATTACK && cause != DamageCause.ENTITY_EXPLOSION && cause != DamageCause.PROJECTILE) {
			boolean exists = true;
			DamageCause damageCause = null;
			try {
				damageCause = DamageCause.valueOf("ENTITY_SWEEP_ATTACK");
			} catch (Exception ex) {
				exists = false;
			}

			if (exists && cause != damageCause) {
				if (!Objects.requireNonNull(GameManager.getMatchWhichContains(p)).health.isDead(p) && Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getState() == GameState.IN_GAME) {
					e.setCancelled(true);
					Objects.requireNonNull(GameManager.getMatchWhichContains(p)).damagePlayer(p, damage);
				}
			}
		}
		

	}
}
