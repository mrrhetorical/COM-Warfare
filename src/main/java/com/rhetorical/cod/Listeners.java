package com.rhetorical.cod;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.rhetorical.cod.object.GameInstance;

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
		for (GameInstance i : GameManager.RunningGames) {
			if (i.getPlayers().contains(p)) {
				p.teleport(Main.lobbyLoc);
				i.removePlayer(p);
				break;
			}
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

		for (Player reciever : Bukkit.getOnlinePlayers()) {
			if (GameManager.isInMatch(sender) && GameManager.isInMatch(reciever)) {
				if (GameManager.getMatchWhichContains(sender) == GameManager.getMatchWhichContains(reciever)) {
					if (reciever == sender) {
						reciever.sendMessage("§a" + sender.getDisplayName() + " §r§f»§r §7" + message);
						continue;
					}

					GameInstance i = GameManager.getMatchWhichContains(sender);

					if (i.isOnBlueTeam(sender)) {
						reciever.sendMessage("§9" + sender.getDisplayName() + " §r§f»§r §7" + message);
					} else if (i.isOnRedTeam(sender)) {
						reciever.sendMessage("§c" + sender.getDisplayName() + " §r§f»§r §7" + message);
					} else if (i.isOnPinkTeam(sender)) {
						reciever.sendMessage("§d" + sender.getDisplayName() + " §r§f»§r §7" + message);
					} else {
						reciever.sendMessage("§7" + sender.getDisplayName() + " §r§f»§r §7" + message);
					}
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
			e.setCancelled(true);
			
			scalar = (Main.defaultHealth / 20D);
			
			damage = e.getDamage() * scalar;
			
		} else {
			return;
		}
		
		if (cause == DamageCause.FALL) {
			GameManager.getMatchWhichContains(p).health.damage(p, damage);
		}
		
		if (cause == DamageCause.DROWNING || cause == DamageCause.SUICIDE) {
			GameManager.getMatchWhichContains(p).kill(p, p);
			return;
		}
	}
}
