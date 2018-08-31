package com.rhetorical.cod.analytics;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.rhetorical.cod.Main;

public class CollectAnalytics {
	
	private static int joinedPlayers = 0;
	
	public static void collectPlayerStats() {
		BukkitRunnable br = new BukkitRunnable() {
			public void run() {
				CollectAnalytics.joinedPlayers = Bukkit.getOnlinePlayers().size();
//				Main.cs.sendMessage(Main.codPrefix + "\u00A7fThere's " + joinedPlayers + " players playing on this server");
			}
		};
		
		br.runTaskTimerAsynchronously(Main.getPlugin(), 20L, 1200L);
	}
	
	
	
}
