package com.rhetorical.cod;

import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.object.GameState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.rhetorical.cod.object.GameInstance;

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
		for (GameInstance i : GameManager.runningGames) {
			if (i.getPlayers().contains(p)) {
				p.teleport(Main.lobbyLoc);
				i.removePlayer(p);
				break;
			}
		}
	}

	@EventHandler
	public void playerJoinGame(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		CreditManager.loadCredits(p);

		if (Main.serverMode) {
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

		if (GameManager.isInMatch(sender)) {
			e.setCancelled(true);
		}

		if (!Main.hasPerm(sender, "com.chat"))
			return;

		for (Player receiver : Bukkit.getOnlinePlayers()) {
			if (GameManager.isInMatch(receiver)) {
				if (GameManager.getMatchWhichContains(sender) == GameManager.getMatchWhichContains(receiver)) {
					if (receiver == sender) {
						receiver.sendMessage("\u00A7a" + sender.getDisplayName() + " \u00A7r\u00A7f»\u00A7r \u00A77" + message);
						continue;
					}

					GameInstance i = GameManager.getMatchWhichContains(sender);

					ChatColor tColor = ChatColor.GRAY;

					if (Objects.requireNonNull(i).isOnBlueTeam(sender)) {
						tColor = ChatColor.BLUE;
					} else if (i.isOnRedTeam(sender)) {
						tColor = ChatColor.RED;
					} else if (i.isOnPinkTeam(sender)) {
						tColor = ChatColor.LIGHT_PURPLE;
					}

					String msg = Lang.CHAT_FORMAT.getMessage();
					msg = msg.replace("{team-color}", tColor + "");
					msg = msg.replace("{player}", sender.getDisplayName());
					msg = msg.replace("{message}", message);

					receiver.sendMessage(msg);
				}
			} else {
				return;
			}
		}

	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (GameManager.isInMatch(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerHit(EntityDamageEvent e) {
		
		Player p;
		
		if (!(e.getEntity() instanceof Player)) return;
		
		p = (Player) e.getEntity();
		
		DamageCause cause = e.getCause();
		
		double damage;
		double scalar;
		
		if (GameManager.isInMatch(p)) {
			scalar = (Main.defaultHealth / 20D);
			
			damage = e.getDamage() * scalar;
			
		} else {
			return;
		}
		
		if ((cause == DamageCause.DROWNING || cause == DamageCause.SUICIDE) && Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getState() == GameState.IN_GAME) {
			e.setCancelled(true);
			Objects.requireNonNull(GameManager.getMatchWhichContains(p)).kill(p, p);
			return;
		}

		if (cause != DamageCause.ENTITY_ATTACK && cause != DamageCause.ENTITY_EXPLOSION && cause != DamageCause.ENTITY_SWEEP_ATTACK && cause != DamageCause.PROJECTILE) {
			if (!Objects.requireNonNull(GameManager.getMatchWhichContains(p)).health.isDead(p) && Objects.requireNonNull(GameManager.getMatchWhichContains(p)).getState() == GameState.IN_GAME) {
				e.setCancelled(true);
				Objects.requireNonNull(GameManager.getMatchWhichContains(p)).damagePlayer(p, damage);
			}
		}
		

	}
}
