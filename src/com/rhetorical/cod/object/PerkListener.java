package com.rhetorical.cod.object;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.rhetorical.cod.GameManager;
import com.rhetorical.cod.Main;

@SuppressWarnings("deprecation")
public class PerkListener implements Listener {

	public PerkListener() {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());
	}

	///// PERK ONE /////
	@EventHandler
	public void marathon(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();

		if (GameManager.isInMatch(p) || Main.loadManager.getCurrentLoadout(p).hasPerk(Perk.MARATHON)) {
			e.setCancelled(true);
		}
	}

	public void oneManArmy(Player p) {

		Location l = p.getLocation();

		BukkitRunnable br = new BukkitRunnable() {

			private int time = 10;

			public void run() {
				if (time > 0) {
					time--;
				} else {
					if (p.getLocation().distance(l) < 1D) {
						Loadout loadout = Main.loadManager.getCurrentLoadout(p);
						p.getInventory().clear();
						p.getInventory().setItem(0, Main.loadManager.knife);
						p.getInventory().setItem(1, loadout.getPrimary().getGun());
						p.getInventory().setItem(2, loadout.getSecondary().getGun());
						p.getInventory().setItem(3, loadout.getLethal().getWeapon());
						p.getInventory().setItem(4, loadout.getTactical().getWeapon());

						ItemStack primaryAmmo = loadout.getPrimary().getAmmo();
						primaryAmmo.setAmount(loadout.getPrimary().getAmmoCount());

						if (!loadout.hasPerk(Perk.ONE_MAN_ARMY)) {
							ItemStack secondaryAmmo = loadout.getSecondary().getAmmo();
							secondaryAmmo.setAmount(loadout.getSecondary().getAmmoCount());
							p.getInventory().setItem(25, secondaryAmmo);
						}

						p.getInventory().setItem(19, primaryAmmo);
					} else {
						p.sendMessage(Main.codPrefix + "§cYou moved and couldn't finish switching your class!");
					}
					this.cancel();
				}
			}
		};

		br.runTaskLater(Main.getPlugin(), 200L);
	}

	public void scavengerDeath(Player victim, Player killer) {
		if (Main.loadManager.getCurrentLoadout(killer).hasPerk(Perk.SCAVENGER)) {

			Item i = victim.getWorld().dropItem(victim.getLocation(), new ItemStack(Material.LAPIS_BLOCK));

			BukkitRunnable br = new BukkitRunnable() {
				public void run() {
					i.remove();
				}
			};

			br.runTaskLaterAsynchronously(Main.getPlugin(), 600L);
		}
	}

	@EventHandler
	public void scavengerPickup(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		ItemStack i = e.getItem().getItemStack();

		if (GameManager.isInMatch(p) && Main.loadManager.getCurrentLoadout(p).hasPerk(Perk.SCAVENGER) && i.getType().equals(Material.LAPIS_BLOCK)) {
			e.getItem().remove();

			ItemStack ammoToAdd = Main.loadManager.getCurrentLoadout(p).getPrimary().getAmmo();
			ammoToAdd.setAmount(Main.loadManager.getCurrentLoadout(p).getPrimary().getAmmoCount() / 8);

			ItemStack currentAmmo = p.getInventory().getItem(19);

			currentAmmo.setAmount(currentAmmo.getAmount() + ammoToAdd.getAmount());

			p.getInventory().setItem(19, currentAmmo);
		}
	}

	///// PERK TWO /////
	@EventHandler(priority = EventPriority.HIGH)
	public void stoppingPower(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (!(GameManager.isInMatch((Player) e.getDamager()) && GameManager.isInMatch((Player) e.getEntity())))
				return;
			if (Main.loadManager.getCurrentLoadout((Player) e.getDamager()).hasPerk(Perk.STOPPING_POWER)) {
				e.setDamage(e.getDamage() * 1.2D);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void juggernaut(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (!(GameManager.isInMatch((Player) e.getDamager()) && GameManager.isInMatch((Player) e.getEntity())))
				return;
			if (Main.loadManager.getCurrentLoadout((Player) e.getEntity()).hasPerk(Perk.JUGGERNAUT)) {
				e.setDamage(e.getDamage() / 1.2D);
				return;
			}
		}
	}
	
	///// PERK THREE /////
	@EventHandler(priority = EventPriority.HIGH)
	public void commando(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (!(GameManager.isInMatch((Player) e.getEntity()) && GameManager.isInMatch((Player) e.getDamager())))
				return;
			
			if (Main.loadManager.getCurrentLoadout((Player) e.getDamager()).hasPerk(Perk.COMMANDO)) {
				e.setDamage(200D);
				return;
			}	
			
		}
	}
	
	public void lastStand(Player p, GameInstance i) {
		
		i.health.reset(p);
		i.health.damage(p, i.health.defaultHealth * 0.8D);
		p.setWalkSpeed(0.1F);
		p.setSneaking(true);
		
		p.sendMessage("§fYou are in final stand! Wait 20 seconds to get back up!");
		
		BukkitRunnable br = new BukkitRunnable() {
			
			private int time = 40;
			
			public void run() {
				if (time > 0)
					time--;
				else {
					p.setWalkSpeed(1F);
					i.health.reset(p);
					p.sendMessage("§fYou are out of final stand!");
					this.cancel();
				}
				
				p.setSneaking(true);
			}
		};
		
		
		br.runTaskTimerAsynchronously(Main.getPlugin(), 0l, 10L);
	}
}
