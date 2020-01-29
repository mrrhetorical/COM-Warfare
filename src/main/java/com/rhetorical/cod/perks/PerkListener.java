package com.rhetorical.cod.perks;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
import com.rhetorical.cod.game.GameState;
import com.rhetorical.cod.game.Gamemode;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.loadouts.Loadout;
import com.rhetorical.cod.loadouts.LoadoutManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class adds functionality for perks that aren't damage based.
 * */
public class PerkListener implements Listener {

	private static PerkListener instance;

	private List<Player> isInLastStand = new ArrayList<>();

	private PerkListener() {
		if (instance == null)
			instance = this;
		Bukkit.getServer().getPluginManager().registerEvents(this, ComWarfare.getPlugin());
	}

	public static PerkListener getInstance() {
		return instance != null ? instance : new PerkListener();
	}

	///// PERK ONE /////
	/**
	 * Prevents food level changing for marathon users
	 * */
	@EventHandler
	public void marathon(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();

		if (LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.MARATHON)) {
			GameInstance i = GameManager.getMatchWhichContains(p);
			if (i == null)
				return;
			if (i.getState() != GameState.IN_GAME)
				return;
			if(i.getGamemode() != Gamemode.GUN && i.getGamemode() != Gamemode.OITC && i.getGamemode() != Gamemode.RSB && (i.getGamemode() != Gamemode.INFECT || i.isOnBlueTeam(p))) {
				e.setCancelled(true);
			}
		}
	}
	/**
	 * Switches the class for the given player with a 10 second cooldown
	 * */
	public void oneManArmy(Player p) {

		Location l = p.getLocation();

		BukkitRunnable br = new BukkitRunnable() {

			private int time = 10;

			public void run() {
				if (time > 0) {
					time--;
				} else {
					if (p.getLocation().distance(l) < 1D) {
						Loadout loadout = LoadoutManager.getInstance().getActiveLoadout(p);
						p.getInventory().clear();
						p.getInventory().setItem(0, LoadoutManager.getInstance().knife);
						p.getInventory().setItem(1, loadout.getPrimary().getMenuItem());
						p.getInventory().setItem(2, loadout.getSecondary().getMenuItem());
						p.getInventory().setItem(3, loadout.getLethal().getMenuItem());
						p.getInventory().setItem(4, loadout.getTactical().getMenuItem());

						ItemStack primaryAmmo = loadout.getPrimary().getAmmo();
						primaryAmmo.setAmount(loadout.getPrimary().getAmmoCount());

						if (!loadout.hasPerk(Perk.ONE_MAN_ARMY)) {
							ItemStack secondaryAmmo = loadout.getSecondary().getAmmo();
							secondaryAmmo.setAmount(loadout.getSecondary().getAmmoCount());
							p.getInventory().setItem(25, secondaryAmmo);
						}

						p.getInventory().setItem(19, primaryAmmo);
					} else {
						ComWarfare.sendMessage(p, ComWarfare.getPrefix() + Lang.PERK_ONE_MAN_ARMY_FAILED.getMessage(), ComWarfare.getLang());
					}
					this.cancel();
				}
			}
		};

		br.runTaskLater(ComWarfare.getPlugin(), 200L);
	}

	/**
	 * Drops a scavenger pack at the victim's body if the killer has scavenger equipped.
	 * @return Returns the scavenger pack dropped.
	 * */
	public Entity scavengerDeath(Player victim, Player killer) {
		if (LoadoutManager.getInstance().getActiveLoadout(killer).hasPerk(Perk.SCAVENGER)) {

			Item i = victim.getWorld().dropItem(victim.getLocation(), new ItemStack(Material.LAPIS_BLOCK));

			BukkitRunnable br = new BukkitRunnable() {
				public void run() {
					i.remove();
				}
			};

			br.runTaskLater(ComWarfare.getPlugin(), 600L);
			return i;
		}

		return null;
	}

	public List<Player> getIsInLastStand() {
		return isInLastStand;
	}

	/**
	 * Handles picking up of the scavenger pack.
	 * */
	@EventHandler(priority = EventPriority.HIGH)
	public void scavengerPickup(PlayerPickupItemEvent e) {

		Player p = e.getPlayer();
		ItemStack i = e.getItem().getItemStack();

		if (GameManager.isInMatch(p) && LoadoutManager.getInstance().getActiveLoadout(p).hasPerk(Perk.SCAVENGER) && i.getType().equals(Material.LAPIS_BLOCK)) {
			e.getItem().remove();

			{
				ItemStack ammoToAdd = LoadoutManager.getInstance().getActiveLoadout(p).getPrimary().getAmmo();
				ammoToAdd.setAmount(LoadoutManager.getInstance().getActiveLoadout(p).getPrimary().getAmmoCount() / 8);

				ItemStack currentAmmo = p.getInventory().getItem(28);

				if (currentAmmo != null)
					currentAmmo.setAmount(currentAmmo.getAmount() + ammoToAdd.getAmount());
				else
					currentAmmo = ammoToAdd;

				p.getInventory().setItem(28, currentAmmo);
			}

			{
				ItemStack ammoToAdd = LoadoutManager.getInstance().getActiveLoadout(p).getSecondary().getAmmo();
				ammoToAdd.setAmount(LoadoutManager.getInstance().getActiveLoadout(p).getSecondary().getAmmoCount() / 8);

				ItemStack currentAmmo = p.getInventory().getItem(29);

				if (currentAmmo != null)
					currentAmmo.setAmount(currentAmmo.getAmount() + ammoToAdd.getAmount());
				else
					currentAmmo = ammoToAdd;

				p.getInventory().setItem(29, currentAmmo);
			}
		}
	}

	///// PERK TWO /////
//
//  Moved to GameInstance#damagePlayer(...)
//
//	@EventHandler(priority = EventPriority.HIGH)
//	public void stoppingPower(EntityDamageByEntityEvent e) {
//		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
//			if (!(GameManager.isInMatch((Player) e.getDamager()) && GameManager.isInMatch((Player) e.getEntity())))
//				return;
//			if (LoadoutManager.getInstance().getActiveLoadout((Player) e.getDamager()).hasPerk(Perk.STOPPING_POWER)) {
//				e.setDamage(e.getDamage() * 1.2D);
//			}
//		}
//	}
//
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void juggernaut(EntityDamageByEntityEvent e) {
//		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
//			if (!(GameManager.isInMatch((Player) e.getDamager()) && GameManager.isInMatch((Player) e.getEntity())))
//				return;
//			if (LoadoutManager.getInstance().getActiveLoadout((Player) e.getEntity()).hasPerk(Perk.JUGGERNAUT)) {
//				e.setDamage(e.getDamage() / 1.2D);
//			}
//		}
//	}
	
	///// PERK THREE /////
//  Moved to GameInstance#onPlayerHit(...)
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public void commando(EntityDamageByEntityEvent e) {
//		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
//			if (!(GameManager.isInMatch((Player) e.getEntity()) && GameManager.isInMatch((Player) e.getDamager())))
//				return;
//
//			if (LoadoutManager.getInstance().getActiveLoadout((Player) e.getDamager()).hasPerk(Perk.COMMANDO)) {
//				e.setDamage(200D);
//			}
//
//		}
//	}
	
	private HashMap<Player, BukkitRunnable> lastStandRunnables = new HashMap<>();

	/**
	 * Puts the target player in last stand mode
	 * */
	public void lastStand(Player p, GameInstance i) {
		
		i.health.reset(p);
		i.health.damage(p, i.health.defaultHealth * 0.8D);
		p.setWalkSpeed(0f);
		p.setSneaking(true);
		
		ComWarfare.sendMessage(p, Lang.PERK_FINAL_STAND_NOTIFICATION.getMessage(), ComWarfare.getLang());
		BukkitRunnable br = new BukkitRunnable() {
			
			private int time = 40;

			public void run() {

				if (!isInLastStand.contains(p)) {
					cancelLastStand(p);
					cancel();
				}

				if (time > 0)
					time--;
				else {
					p.setWalkSpeed(0.2f);
					i.health.reset(p);
				ComWarfare.sendMessage(p, Lang.PERK_FINAL_STAND_FINISHED.getMessage(), ComWarfare.getLang());
					cancel();
					cancelLastStand(p);
					isInLastStand.remove(p);
					p.setSneaking(false);
				}
				
				p.setSneaking(true);
			}
		};
		
		this.lastStandRunnables.put(p, br);
		br.runTaskTimer(ComWarfare.getPlugin(), 0L, 10L);
	}
	
	private void cancelLastStand(Player p) {
		if (lastStandRunnables.keySet().contains(p)) {
			lastStandRunnables.remove(p);
			lastStandRunnables.get(p);
		}
	}
}
