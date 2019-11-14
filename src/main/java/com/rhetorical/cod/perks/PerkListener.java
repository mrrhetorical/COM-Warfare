package com.rhetorical.cod.perks;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.game.GameInstance;
import com.rhetorical.cod.game.GameManager;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PerkListener implements Listener {

	private static PerkListener instance;

	private List<Player> isInLastStand = new ArrayList<>();

	private PerkListener() {
		if (instance == null)
			instance = this;
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());
	}

	public static PerkListener getInstance() {
		return instance != null ? instance : new PerkListener();
	}

	///// PERK ONE /////
	@EventHandler
	public void marathon(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();

		if (GameManager.isInMatch(p) && LoadoutManager.getInstance().getCurrentLoadout(p).hasPerk(Perk.MARATHON)) {
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
						Loadout loadout = LoadoutManager.getInstance().getCurrentLoadout(p);
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
						Main.sendMessage(p, Main.getPrefix() + Lang.PERK_ONE_MAN_ARMY_FAILED.getMessage(), Main.getLang());
					}
					this.cancel();
				}
			}
		};

		br.runTaskLater(Main.getPlugin(), 200L);
	}

	public Entity scavengerDeath(Player victim, Player killer) {
		if (LoadoutManager.getInstance().getCurrentLoadout(killer).hasPerk(Perk.SCAVENGER)) {

			Item i = victim.getWorld().dropItem(victim.getLocation(), new ItemStack(Material.LAPIS_BLOCK));

			BukkitRunnable br = new BukkitRunnable() {
				public void run() {
					i.remove();
				}
			};

			br.runTaskLater(Main.getPlugin(), 600L);
			return i;
		}

		return null;
	}

	public List<Player> getIsInLastStand() {
		return isInLastStand;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void scavengerPickup(PlayerPickupItemEvent e) {

		Player p = e.getPlayer();
		ItemStack i = e.getItem().getItemStack();

		if (GameManager.isInMatch(p) && LoadoutManager.getInstance().getCurrentLoadout(p).hasPerk(Perk.SCAVENGER) && i.getType().equals(Material.LAPIS_BLOCK)) {
			e.getItem().remove();

			{
				ItemStack ammoToAdd = LoadoutManager.getInstance().getCurrentLoadout(p).getPrimary().getAmmo();
				ammoToAdd.setAmount(LoadoutManager.getInstance().getCurrentLoadout(p).getPrimary().getAmmoCount() / 8);

				ItemStack currentAmmo = p.getInventory().getItem(28);

				if (currentAmmo != null)
					currentAmmo.setAmount(currentAmmo.getAmount() + ammoToAdd.getAmount());
				else
					currentAmmo = ammoToAdd;

				p.getInventory().setItem(28, currentAmmo);
			}

			{
				ItemStack ammoToAdd = LoadoutManager.getInstance().getCurrentLoadout(p).getSecondary().getAmmo();
				ammoToAdd.setAmount(LoadoutManager.getInstance().getCurrentLoadout(p).getSecondary().getAmmoCount() / 8);

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
	@EventHandler(priority = EventPriority.HIGH)
	public void stoppingPower(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (!(GameManager.isInMatch((Player) e.getDamager()) && GameManager.isInMatch((Player) e.getEntity())))
				return;
			if (LoadoutManager.getInstance().getCurrentLoadout((Player) e.getDamager()).hasPerk(Perk.STOPPING_POWER)) {
				e.setDamage(e.getDamage() * 1.2D);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void juggernaut(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (!(GameManager.isInMatch((Player) e.getDamager()) && GameManager.isInMatch((Player) e.getEntity())))
				return;
			if (LoadoutManager.getInstance().getCurrentLoadout((Player) e.getEntity()).hasPerk(Perk.JUGGERNAUT)) {
				e.setDamage(e.getDamage() / 1.2D);
			}
		}
	}
	
	///// PERK THREE /////
	@EventHandler(priority = EventPriority.HIGH)
	public void commando(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			if (!(GameManager.isInMatch((Player) e.getEntity()) && GameManager.isInMatch((Player) e.getDamager())))
				return;
			
			if (LoadoutManager.getInstance().getCurrentLoadout((Player) e.getDamager()).hasPerk(Perk.COMMANDO)) {
				e.setDamage(200D);
			}
			
		}
	}
	
	private HashMap<Player, BukkitRunnable> lastStandRunnables = new HashMap<>();
	
	public void lastStand(Player p, GameInstance i) {
		
		i.health.reset(p);
		i.health.damage(p, i.health.defaultHealth * 0.8D);
		p.setWalkSpeed(0f);
		p.setSneaking(true);
		
		Main.sendMessage(p, Lang.PERK_FINAL_STAND_NOTIFICATION.getMessage(), Main.getLang());
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
				Main.sendMessage(p, Lang.PERK_FINAL_STAND_FINISHED.getMessage(), Main.getLang());
					cancel();
					cancelLastStand(p);
					isInLastStand.remove(p);
					p.setSneaking(false);
				}
				
				p.setSneaking(true);
			}
		};
		
		this.lastStandRunnables.put(p, br);
		br.runTaskTimer(Main.getPlugin(), 0L, 10L);
	}
	
	private void cancelLastStand(Player p) {
		if (lastStandRunnables.keySet().contains(p)) {
			lastStandRunnables.remove(p);
			lastStandRunnables.get(p);
		}
	}
}
