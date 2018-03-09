package com.rhetorical.cod.inventories;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.rhetorical.cod.CreditManager;
import com.rhetorical.cod.GameManager;
import com.rhetorical.cod.Main;
import com.rhetorical.cod.object.CodGun;
import com.rhetorical.cod.object.CodPerk;
import com.rhetorical.cod.object.CodWeapon;
import com.rhetorical.cod.object.GunType;
import com.rhetorical.cod.object.Loadout;
import com.rhetorical.cod.object.PerkSlot;
import com.rhetorical.cod.object.UnlockType;
import com.rhetorical.cod.object.WeaponType;

public class InventoryManager implements Listener {

	public ItemStack closeInv = new ItemStack(Material.BARRIER);

	public boolean shouldCancelClick(Inventory i, Player p) {
		if (i.equals(mainInventory)) {
			return true;
		}
		if (i.equals(p.getInventory()) && GameManager.isInMatch(p)) {
			return true;
		}
		if (i.equals(createClassInventory.get(p))) {
			return true;
		}

		for (Loadout loadout : Main.loadManager.getLoadouts(p)) {
			if (loadout.getPrimaryInventory().equals(i) || loadout.getSecondaryInventory().equals(i)
					|| loadout.getLethalInventory().equals(i) || loadout.getTacticalInventory().equals(i)
					|| loadout.getPerk1Inventory().equals(i) || loadout.getPerk2Inventory().equals(i)
					|| loadout.getPerk3Inventory().equals(i)) {
				return true;
			}
		}

		if (i.equals(this.selectClassInventory.get(p))) {
			return true;
		}
		
		if (i.equals(mainShopInventory)) {
			return true;
		}

		if (i.equals(Main.shopManager.gunShop.get(p)) || i.equals(Main.shopManager.weaponShop.get(p))
				|| i.equals(Main.shopManager.perkShop.get(p))) {
			return true;
		}
		return false;
	}

	/*
	 * TODO: - Create shop inventory - Create item for primary guns - Create
	 * item for secondary guns - Create item for weapons (Lethal & Tactical) -
	 * Create item for killstreaks & scorestreaks - Create item for credit &
	 * experience boosts
	 * 
	 * - Create loadout inventory - Create item for each loadout with lore that
	 * displays - Loadout name ("Class 1" by default with appropriate numbers
	 * for names) - Primary weapon name - Secondary weapon name - Lethal &
	 * Tactical weapon name - Perk names
	 * 
	 * - Within loadout screen (When clicked item shows list of available
	 * weapons) - Display primary - Display secondary - Display lethal - Display
	 * tactical - Display Perk 1 - Display Perk 2 - Display Perk 3 - Display
	 * back button - Create Killstreak & Scorestreak inventory - Create item for
	 * each killstreak or scorestreak that isn't currently bought by the user
	 * 
	 */

	public InventoryManager() {

		setupCloseInvButton();
		setupMainInventories();

		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());
	}

	public void setupCloseInvButton() {
		ItemMeta closeInvMeta = closeInv.getItemMeta();
		closeInvMeta.setDisplayName("§c§lClose");
		closeInv.setItemMeta(closeInvMeta);
	}

	// Main Inventory

	public static Inventory mainInventory = Bukkit.createInventory(null, 18, "COM-Warfare");
	public static Inventory mainShopInventory = Bukkit.createInventory(null, 9, "Shop Menu");
	public HashMap<Player, Inventory> createClassInventory = new HashMap<Player, Inventory>();
	public HashMap<Player, Inventory> selectClassInventory = new HashMap<Player, Inventory>();

	public static ItemStack joinGame = new ItemStack(Material.EMERALD);
	public static ItemStack createClass = new ItemStack(Material.CHEST);
	public static ItemStack scoreStreaks = new ItemStack(Material.DIAMOND);
	public static ItemStack prestige = new ItemStack(Material.ANVIL);
	public static ItemStack assignments = new ItemStack(Material.GOLD_INGOT);
	public static ItemStack clanTag = new ItemStack(Material.SHEARS);
	public static ItemStack combatRecord = new ItemStack(Material.PAPER);
	public static ItemStack leaderboard = new ItemStack(Material.PAPER);

	public static ItemStack shopItem = new ItemStack(Material.EMERALD);
	public static ItemStack gunShopItem = new ItemStack(Material.CHEST);
	public static ItemStack grenadeShopItem = new ItemStack(Material.CHEST);
	public static ItemStack perkShopItem = new ItemStack(Material.CHEST);

	public void setupMainInventories() {
		joinGame = new ItemStack(Material.EMERALD);
		ItemMeta joinGameMeta = joinGame.getItemMeta();
		joinGameMeta.setDisplayName("§a§lFind Match");
		ArrayList<String> joinGameLore = new ArrayList<String>();
		joinGameLore.add("§6Utilize the matchmaker to find a match ");
		joinGameLore.add("§6with the best match for you!");
		joinGameMeta.setLore(joinGameLore);
		joinGame.setItemMeta(joinGameMeta);

		mainInventory.setItem(0, joinGame);

		createClass = new ItemStack(Material.CHEST);
		ItemMeta createClassMeta = createClass.getItemMeta();
		createClassMeta.setDisplayName("§4§lCreate-a-Class");
		ArrayList<String> createClassLore = new ArrayList<String>();
		createClassLore.add("§6Create custom loadouts for you to ");
		createClassLore.add("§6use in game!");
		createClassMeta.setLore(createClassLore);
		createClass.setItemMeta(createClassMeta);

		mainInventory.setItem(1, createClass);

		scoreStreaks = new ItemStack(Material.DIAMOND);
		ItemMeta scoreStreakMeta = scoreStreaks.getItemMeta();
		scoreStreakMeta.setDisplayName("§b§lScorestreaks");
		ArrayList<String> scoreStreakLore = new ArrayList<String>();
		scoreStreakLore.add("§6Choose which scorestreaks you want");
		scoreStreakLore.add("§6to use during a match!");
		scoreStreakMeta.setLore(scoreStreakLore);
		scoreStreaks.setItemMeta(scoreStreakMeta);

		mainInventory.setItem(2, scoreStreaks);

		prestige = new ItemStack(Material.ANVIL);
		ItemMeta prestigeMeta = prestige.getItemMeta();
		prestigeMeta.setDisplayName("§6§lPrestige Options");
		ArrayList<String> prestigeLore = new ArrayList<String>();
		prestigeLore.add("§6Prestige your account and unlock ");
		prestigeLore.add("§6special awards!");
		prestigeMeta.setLore(prestigeLore);
		prestige.setItemMeta(prestigeMeta);

		mainInventory.setItem(3, prestige);

		assignments = new ItemStack(Material.GOLD_INGOT);
		ItemMeta assignmentMeta = assignments.getItemMeta();
		assignmentMeta.setDisplayName("§3§lAssignments & Contracts");
		ArrayList<String> assignmentLore = new ArrayList<String>();
		assignmentLore.add("§6Complete assignments and buy ");
		assignmentLore.add("§6contracts to get extra rewards ");
		assignmentLore.add("§6for playing the game and completing ");
		assignmentLore.add("§6challenges!");
		assignmentMeta.setLore(assignmentLore);
		assignments.setItemMeta(assignmentMeta);

		mainInventory.setItem(4, assignments);

		clanTag = new ItemStack(Material.SHEARS);
		ItemMeta clanTagMeta = clanTag.getItemMeta();
		clanTagMeta.setDisplayName("§5§lClan Tag");
		ArrayList<String> clanTagLore = new ArrayList<String>();
		clanTagLore.add("§6Change your clan tag in the chat!");
		clanTagMeta.setLore(clanTagLore);
		clanTag.setItemMeta(clanTagMeta);

		mainInventory.setItem(5, clanTag);

		combatRecord = new ItemStack(Material.PAPER);
		ItemMeta combatRecordMeta = combatRecord.getItemMeta();
		combatRecordMeta.setDisplayName("§9§lCombat Record");
		ArrayList<String> combatRecordLore = new ArrayList<String>();
		combatRecordLore.add("§6Check your combat record and see what ");
		combatRecordLore.add("§6weapons and killstreaks you've gotten  ");
		combatRecordLore.add("§6the most kills with!");
		combatRecordMeta.setLore(combatRecordLore);
		combatRecord.setItemMeta(combatRecordMeta);

		mainInventory.setItem(6, combatRecord);

		leaderboard = new ItemStack(Material.PAPER);
		ItemMeta leaderboardMeta = leaderboard.getItemMeta();
		leaderboardMeta.setDisplayName("§2§lLeaderboard");
		ArrayList<String> leaderboardLore = new ArrayList<String>();
		leaderboardLore.add("§6Compare where your stats are compared ");
		leaderboardLore.add("§6to everyone else's!");
		leaderboardMeta.setLore(leaderboardLore);
		leaderboard.setItemMeta(leaderboardMeta);

		mainInventory.setItem(7, leaderboard);

		mainInventory.setItem(8, this.closeInv);

		ItemStack shop = shopItem;
		ItemMeta shopMeta = shop.getItemMeta();
		shopMeta.setDisplayName("§a§lShop");
		ArrayList<String> shopLore = new ArrayList<String>();
		shopLore.add("§6Buy items here at the shop!");
		shopMeta.setLore(shopLore);
		shop.setItemMeta(shopMeta);

		shopItem = shop;

		mainInventory.setItem(9, shopItem);

		ItemStack gunItem = new ItemStack(Main.loadManager.getDefaultPrimary().getGun().getType());
		ItemMeta gunMeta = gunItem.getItemMeta();
		gunMeta.setDisplayName("§9Gun Shop");
		ArrayList<String> gunLore = new ArrayList<String>();
		gunLore.add("§6Buy guns that you have unlocked here!");
		gunMeta.setLore(gunLore);
		gunItem.setItemMeta(gunMeta);

		gunShopItem = gunItem;

		ItemStack grenadeItem = new ItemStack(Main.loadManager.getDefaultLethal().getWeapon().getType());
		ItemMeta grenadeMeta = grenadeItem.getItemMeta();
		grenadeMeta.setDisplayName("§cGrenade Shop");
		ArrayList<String> grenadeLore = new ArrayList<String>();
		grenadeLore.add("§6Buy grenades that you have unlocked here!");
		gunMeta.setLore(grenadeLore);
		grenadeItem.setItemMeta(grenadeMeta);

		grenadeShopItem = grenadeItem;

		ItemStack perkItem = new ItemStack(Main.perkManager.getDefaultOne().getItem());
		ItemMeta perkMeta = perkItem.getItemMeta();
		perkMeta.setDisplayName("§aPerk Shop");
		ArrayList<String> perkLore = new ArrayList<String>();
		perkLore.add("§6Buy perks that you have unlocked here!");
		perkMeta.setLore(perkLore);
		perkItem.setItemMeta(perkMeta);

		perkShopItem = perkItem;

		mainShopInventory.setItem(0, gunShopItem);
		mainShopInventory.setItem(1, grenadeShopItem);
		mainShopInventory.setItem(2, perkShopItem);
		mainShopInventory.setItem(8, closeInv);
	}

	public void setupCreateClassInventory(Player p) {

		if (Main.loadManager.getLoadouts(p) == null) {
			Main.loadManager.load(p);
		}

		if (Main.loadManager.getLoadouts(p).size() < 5) {
			Main.loadManager.load(p);
		}

		Main.shopManager.loadPurchaseData(p);

		Inventory customClassInventory = Bukkit.createInventory(p, 9 * Main.loadManager.getAllowedClasses(p),
				"Create-a-Class");

		int line;

		for (int i = 0; i < Main.loadManager.getAllowedClasses(p); i++) {
			line = i * 9;

			Loadout loadout = Main.loadManager.getLoadouts(p).get(i);

			ItemStack header = new ItemStack(Material.WORKBENCH);
			ItemMeta headerMeta = header.getItemMeta();
			headerMeta.setDisplayName(loadout.getName());
			ArrayList<String> headerLore = new ArrayList<String>();
			headerLore.add("§6Edit the class to the right.");
			headerMeta.setLore(headerLore);
			header.setItemMeta(headerMeta);

			ItemStack primary = loadout.getPrimary().getGun();
			ItemMeta primaryMeta = primary.getItemMeta();
			primaryMeta.setDisplayName("§6Primary Weapon§f: §r§f" + loadout.getPrimary().getName());
			ArrayList<String> primaryLore = new ArrayList<String>();
			primaryLore.add("§6This is your primary weapon. During games,");
			primaryLore.add("§6this should be your go-to gun.");
			primaryMeta.setLore(primaryLore);
			primary.setItemMeta(primaryMeta);

			ItemStack secondary = loadout.getSecondary().getGun();
			ItemMeta secondaryMeta = secondary.getItemMeta();
			secondaryMeta.setDisplayName("§6Secondary Weapon§f: §r§f" + loadout.getSecondary().getName());
			ArrayList<String> secondaryLore = new ArrayList<String>();
			secondaryLore.add("§6This is your secondary weapon. During games,");
			secondaryLore.add("§6this should be used as a backup weapon.");
			secondaryMeta.setLore(secondaryLore);
			secondary.setItemMeta(secondaryMeta);

			ItemStack lethal = loadout.getLethal().getWeapon();
			ItemMeta lethalMeta = lethal.getItemMeta();
			lethalMeta.setDisplayName("§6Lethal Grenade§f: §r§f" + loadout.getLethal().getName());
			ArrayList<String> lethalLore = new ArrayList<String>();
			lethalLore.add("§6This is your lethal grenade. Use it");
			lethalLore.add("§6during games to kill players.");
			lethalMeta.setLore(lethalLore);
			lethal.setItemMeta(lethalMeta);

			ItemStack tactical = loadout.getTactical().getWeapon();
			ItemMeta tacticalMeta = tactical.getItemMeta();
			tacticalMeta.setDisplayName("§6Tactical Grenade§f: §r§f" + loadout.getTactical().getName());
			ArrayList<String> tacticalLore = new ArrayList<String>();
			tacticalLore.add("§6This is your tactical grenade. Use it");
			tacticalLore.add("§6to disorient other players and use");
			tacticalLore.add("§6it to gain a tactical advantage over players.");
			tacticalMeta.setLore(tacticalLore);
			tactical.setItemMeta(tacticalMeta);

			ItemStack perkOne = loadout.getPerk1().getItem();
			ItemMeta perkOneMeta = perkOne.getItemMeta();
			perkOneMeta.setDisplayName("§6Perk 1§f: §r§f" + loadout.getPerk1().getPerk().getName());
			perkOneMeta.setLore(loadout.getPerk1().getLore());
			perkOne.setItemMeta(perkOneMeta);

			ItemStack perkTwo = loadout.getPerk2().getItem();
			ItemMeta perkTwoMeta = perkTwo.getItemMeta();
			perkTwoMeta.setDisplayName("§6Perk 2§f: §r§f" + loadout.getPerk2().getPerk().getName());
			perkTwoMeta.setLore(loadout.getPerk2().getLore());
			perkTwo.setItemMeta(perkTwoMeta);

			ItemStack perkThree = loadout.getPerk3().getItem();
			ItemMeta perkThreeMeta = perkThree.getItemMeta();
			perkThreeMeta.setDisplayName("§6Perk 3§f: §r§f" + loadout.getPerk3().getPerk().getName());
			perkThreeMeta.setLore(loadout.getPerk3().getLore());
			perkThree.setItemMeta(perkThreeMeta);

			customClassInventory.setItem(line + 0, header);
			customClassInventory.setItem(line + 1, primary);
			customClassInventory.setItem(line + 2, secondary);
			customClassInventory.setItem(line + 3, lethal);
			customClassInventory.setItem(line + 4, tactical);
			customClassInventory.setItem(line + 5, perkOne);
			customClassInventory.setItem(line + 6, perkTwo);
			customClassInventory.setItem(line + 7, perkThree);
			customClassInventory.setItem(line + 8, this.closeInv);

			createClassInventory.put(p, customClassInventory);

		}

	}

	public void setupPlayerSelectionInventories(Player p) {
		for (Loadout loadout : Main.loadManager.getLoadouts(p)) {

			Main.shopManager.loadPurchaseData(p);

			Inventory primary = Bukkit.createInventory(p, 36, "Primary Weapons");
			Inventory secondary = Bukkit.createInventory(p, 36, "Secondary Weapons");
			Inventory lethal = Bukkit.createInventory(p, 27, "Lethal Grenades");
			Inventory tactical = Bukkit.createInventory(p, 27, "Tactical Grenades");
			Inventory perk1 = Bukkit.createInventory(p, 27, "Perk One");
			Inventory perk2 = Bukkit.createInventory(p, 27, "Perk Two");
			Inventory perk3 = Bukkit.createInventory(p, 27, "Perk Three");

//			primary.addItem(Main.loadManager.getDefaultPrimary().getGun());
//			secondary.addItem(Main.loadManager.getDefaultSecondary().getGun());
//			lethal.addItem(Main.loadManager.getDefaultLethal().getWeapon());
//			tactical.addItem(Main.loadManager.getDefaultTactical().getWeapon());

			for (CodGun gun : Main.shopManager.getPurchasedGuns().get(p)) {
				if (gun.getGunType() == GunType.Primary) {
					primary.addItem(gun.getGun());
				} else {
					secondary.addItem(gun.getGun());
				}
			}

			for (CodWeapon weapon : Main.shopManager.getPurchasedWeapons().get(p)) {
				if (weapon.getWeaponType() == WeaponType.LETHAL) {
					lethal.addItem(weapon.getWeapon());
				} else {
					tactical.addItem(weapon.getWeapon());
				}
			}

			for (CodPerk perk : Main.shopManager.getPerks(p)) {
				if (perk.getSlot().equals(PerkSlot.ONE)) {
					perk1.addItem(perk.getItem());
				} else if (perk.getSlot().equals(PerkSlot.TWO)) {
					perk2.addItem(perk.getItem());
				} else if (perk.getSlot().equals(PerkSlot.THREE)) {
					perk3.addItem(perk.getItem());
				}
			}

			primary.setItem(35, closeInv);
			secondary.setItem(35, closeInv);
			lethal.setItem(26, closeInv);
			tactical.setItem(26, closeInv);
			perk1.setItem(26, closeInv);
			perk2.setItem(26, closeInv);
			perk3.setItem(26, closeInv);

			loadout.setPrimaryInventory(primary);
			loadout.setSecondaryInventory(secondary);
			loadout.setLethalInventory(lethal);
			loadout.setTacticalInventory(tactical);
			loadout.setPerkInventory(0, perk1);
			loadout.setPerkInventory(1, perk2);
			loadout.setPerkInventory(2, perk3);
		}
	}

	public void setupShopInventories(Player p) {
		Inventory gunShop = Bukkit.createInventory(p, 36, "Gun Shop");
		Inventory weaponShop = Bukkit.createInventory(p, 36, "Grenade Shop");
		Inventory perkShop = Bukkit.createInventory(p, 36, "Perk Shop");

		ArrayList<CodGun> guns = Main.shopManager.getPrimaryGuns();
		guns.addAll(Main.shopManager.getSecondaryGuns());

		for (CodGun gun : guns) {
			if (gun.getType() == UnlockType.BOTH) {
				if (Main.progManager.getLevel(p) >= gun.getLevelUnlock()) {

					ItemStack item = gun.getGun();

					ItemMeta gunMeta = item.getItemMeta();

					ArrayList<String> lore = new ArrayList<String>();

					lore.add("§6Cost: " + gun.getCreditUnlock());

					gunMeta.setLore(lore);

					item.setItemMeta(gunMeta);

					gunShop.addItem(item);

				}

			} else if (gun.getType() == UnlockType.CREDITS) {
				ItemStack item = gun.getGun();

				ItemMeta gunMeta = item.getItemMeta();

				ArrayList<String> lore = new ArrayList<String>();

				lore.add("§6Cost: " + gun.getCreditUnlock());

				gunMeta.setLore(lore);

				item.setItemMeta(gunMeta);

				gunShop.addItem(item);
			}

		}

		ArrayList<CodWeapon> grenades = Main.shopManager.getLethalWeapons();
		grenades.addAll(Main.shopManager.getTacticalWeapons());

		for (CodWeapon grenade : grenades) {
			if (grenade.getType() == UnlockType.BOTH) {
				if (Main.progManager.getLevel(p) >= grenade.getLevelUnlock()) {

					ItemStack item = grenade.getWeapon();

					ItemMeta gunMeta = item.getItemMeta();

					ArrayList<String> lore = new ArrayList<String>();

					lore.add("§6Cost: " + grenade.getCreditUnlock());

					gunMeta.setLore(lore);

					item.setItemMeta(gunMeta);

					weaponShop.addItem(item);

				}

			} else if (grenade.getType() == UnlockType.CREDITS) {
				ItemStack item = grenade.getWeapon();

				ItemMeta gunMeta = item.getItemMeta();

				ArrayList<String> lore = new ArrayList<String>();

				lore.add("§6Cost: " + grenade.getCreditUnlock());

				gunMeta.setLore(lore);

				item.setItemMeta(gunMeta);

				weaponShop.addItem(item);
			}

		}

		ArrayList<CodPerk> perks = Main.perkManager.getAvailablePerks();
		for (CodPerk perk : perks) {

			if (perk.getPerk().getName().equals(Main.perkManager.getDefaultOne().getPerk().getName())
					|| perk.getPerk().getName().equals(Main.perkManager.getDefaultTwo().getPerk().getName())
					|| perk.getPerk().getName().equals(Main.perkManager.getDefaultThree().getPerk().getName())) {
				continue;
			}

			if (!Main.shopManager.purchasedPerks.get(p).contains(perk) && !perk.equals(Main.perkManager.getDefaultOne())
					&& !perk.equals(Main.perkManager.getDefaultTwo())
					&& !perk.equals(Main.perkManager.getDefaultThree())) {
				ItemStack item = perk.getItem();
				ItemMeta perkMeta = item.getItemMeta();
				if (perkMeta.getLore() == null) {
					perkMeta.setLore(new ArrayList<String>());
				}
				ArrayList<String> lore = (ArrayList<String>) perkMeta.getLore();
				lore.add("§6Cost: " + perk.getCost());
				perkMeta.setLore(lore);
				item.setItemMeta(perkMeta);

				perkShop.addItem(item);
			}

		}

		gunShop.setItem(35, closeInv);
		weaponShop.setItem(35, closeInv);
		perkShop.setItem(35, closeInv);

		Main.shopManager.gunShop.put(p, gunShop);
		Main.shopManager.weaponShop.put(p, weaponShop);
		Main.shopManager.perkShop.put(p, perkShop);
	}

	public void setupSelectClassInventory(Player p) {

		Inventory inventory = Bukkit.createInventory(p, 9, "Select Class");

		for (int i = 0; i < Main.loadManager.getAllowedClasses(p); i++) {

			Loadout loadout = Main.loadManager.getLoadouts(p).get(i);

			ItemStack item = loadout.getPrimary().getGun();

			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(loadout.getName());

			ArrayList<String> lore = new ArrayList<String>();
			lore.add("§6Primary: " + loadout.getPrimary().getName());
			lore.add("§6Secondary: " + loadout.getSecondary().getName());
			lore.add("§6Lethal: " + loadout.getLethal().getName());
			lore.add("§6Tactical: " + loadout.getTactical().getName());
			lore.add("§6Perk 1: " + loadout.getPerk1().getPerk().toString());
			lore.add("§6Perk 2: " + loadout.getPerk2().getPerk().toString());
			lore.add("§6Perk 3: " + loadout.getPerk3().getPerk().toString());

			meta.setLore(lore);
			item.setItemMeta(meta);

			inventory.setItem(i, item);
		}

		this.selectClassInventory.put(p, inventory);
	}

	public boolean openSelectClassInventory(Player p) {
		if (!GameManager.isInMatch(p)) {
			p.sendMessage(Main.codPrefix + "§cYou can't select a class while not in a game!");
			return false;
		}

		if (this.selectClassInventory.get(p) == null) {
			this.setupSelectClassInventory(p);
		}

		p.closeInventory();
		p.openInventory(this.selectClassInventory.get(p));
		return true;
	}

	@EventHandler
	public void inventoryClickListener(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;

		Player p = (Player) e.getWhoClicked();

		if (e.getInventory() == null)
			return;

		if (shouldCancelClick(e.getInventory(), p)) {
			e.setCancelled(true);
		} else {
			return;
		}

		if (e.getCurrentItem() == null)
			return;

		if (e.getCurrentItem().equals(closeInv)) {
			p.closeInventory();
		}

		if (e.getClickedInventory().equals(mainInventory)) {

			if (e.getCurrentItem().equals(joinGame)) {
				p.sendMessage(Main.codPrefix + "§7Put in matchmaker queue. . .");
				GameManager.findMatch(p);
				p.closeInventory();
				return;
			} else if (e.getCurrentItem().equals(createClass)) {
				p.closeInventory();

				this.setupCreateClassInventory(p);

				p.openInventory(createClassInventory.get(p));
			} else if (e.getCurrentItem().equals(shopItem)) {
				p.closeInventory();
				p.openInventory(mainShopInventory);
				return;
			} else if (e.getCurrentItem().equals(closeInv)) {
				p.closeInventory();
				return;
			}
		} else if (e.getInventory().equals(mainShopInventory)) {
			if (e.getCurrentItem().equals(gunShopItem)) {
				p.closeInventory();
				if (!Main.shopManager.gunShop.containsKey(p)) {
					this.setupShopInventories(p);
				}
				p.openInventory(Main.shopManager.gunShop.get(p));
				return;
			} else if (e.getCurrentItem().equals(grenadeShopItem)) {
				p.closeInventory();
				if (!Main.shopManager.weaponShop.containsKey(p)) {
					this.setupShopInventories(p);
				}
				p.openInventory(Main.shopManager.weaponShop.get(p));
				return;
			} else if (e.getCurrentItem().equals(perkShopItem)) {
				p.closeInventory();
				if (!Main.shopManager.weaponShop.containsKey(p)) {
					this.setupShopInventories(p);
				}
				p.openInventory(Main.shopManager.perkShop.get(p));
				return;
			} else if (e.getCurrentItem().equals(shopItem)) {
				p.closeInventory();
				p.openInventory(mainShopInventory);
				return;
			}
		} else if (e.getInventory().equals(createClassInventory.get(p))) {

			/*
			 * TODO: - Get perk inventories from loadout - Change class name
			 * when clicking class icon on left
			 */

			int slot = e.getSlot();

			for (int i = 0; i < Main.loadManager.getAllowedClasses(p); i++) {
				if (slot == 1 + (9 * i)) {
					p.closeInventory();
					p.openInventory(Main.loadManager.getLoadouts(p).get(i).getPrimaryInventory());
				} else if (slot == 2 + (9 * i)) {
					p.closeInventory();
					p.openInventory(Main.loadManager.getLoadouts(p).get(i).getSecondaryInventory());
				} else if (slot == 3 + (9 * i)) {
					p.closeInventory();
					p.openInventory(Main.loadManager.getLoadouts(p).get(i).getLethalInventory());
				} else if (slot == 4 + (9 * i)) {
					p.closeInventory();
					p.openInventory(Main.loadManager.getLoadouts(p).get(i).getTacticalInventory());
				} else if (slot == 5 + (9 * i)) {
					p.closeInventory();
					p.openInventory(Main.loadManager.getLoadouts(p).get(i).getPerk1Inventory());
				} else if (slot == 6 + (9 * i)) {
					p.closeInventory();
					p.openInventory(Main.loadManager.getLoadouts(p).get(i).getPerk2Inventory());
				} else if (slot == 7 + (9 * i)) {
					p.closeInventory();
					p.openInventory(Main.loadManager.getLoadouts(p).get(i).getPerk3Inventory());
				}
			}

		} else if (e.getInventory().equals(Main.shopManager.gunShop.get(p))) {

			Main.shopManager.loadPurchaseData(p);

			ArrayList<CodGun> guns = Main.shopManager.getPrimaryGuns();
			guns.addAll(Main.shopManager.getSecondaryGuns());

			for (CodGun gun : guns) {
				if (e.getCurrentItem().getType().equals(gun.getGun().getType())) {
					int cost = gun.getCreditUnlock();
					if (CreditManager.purchase(p, cost)) {
						ArrayList<CodGun> purchasedGuns = Main.shopManager.purchasedGuns.get(p);
						purchasedGuns.add(gun);
						Main.shopManager.purchasedGuns.put(p, purchasedGuns);
						Main.shopManager.savePurchaseData(p);
						Main.shopManager.loadPurchaseData(p);
						p.closeInventory();
						Main.invManager.setupShopInventories(p);
						Main.invManager.setupCreateClassInventory(p);
						Main.invManager.setupPlayerSelectionInventories(p);
						return;
					} else {
						p.closeInventory();
						return;
					}
				}
			}

		} else if (e.getInventory().equals(Main.shopManager.weaponShop.get(p))) {

			Main.shopManager.loadPurchaseData(p);

			ArrayList<CodWeapon> grenades = Main.shopManager.getLethalWeapons();
			grenades.addAll(Main.shopManager.getTacticalWeapons());

			for (CodWeapon grenade : grenades) {
				if (e.getCurrentItem().getType().equals(grenade.getWeapon().getType())) {
					int cost = grenade.getCreditUnlock();
					if (CreditManager.purchase(p, cost)) {
						ArrayList<CodWeapon> purchasedGrenades = Main.shopManager.purchasedWeapons.get(p);
						purchasedGrenades.add(grenade);
						Main.shopManager.purchasedWeapons.put(p, purchasedGrenades);
						Main.shopManager.savePurchaseData(p);
						Main.shopManager.loadPurchaseData(p);
						p.closeInventory();
						Main.invManager.setupShopInventories(p);
						Main.invManager.setupCreateClassInventory(p);
						Main.invManager.setupPlayerSelectionInventories(p);
						return;
					} else {
						p.closeInventory();
						return;
					}
				}
			}
		} else if (e.getInventory().equals(Main.shopManager.perkShop.get(p))) {

			Main.shopManager.loadPurchaseData(p);

			ArrayList<CodPerk> perks = Main.perkManager.getAvailablePerks();

			for (CodPerk perk : perks) {
				if (e.getCurrentItem().getType().equals(perk.getItem().getType())
						&& e.getCurrentItem().getItemMeta().getDisplayName().equals(perk.getPerk().getName())) {
					int cost = perk.getCost();
					if (CreditManager.purchase(p, cost)) {
						ArrayList<CodPerk> purchasedPerks = Main.shopManager.purchasedPerks.get(p);
						purchasedPerks.add(perk);
						Main.shopManager.purchasedPerks.put(p, purchasedPerks);
						Main.shopManager.savePurchaseData(p);
						Main.shopManager.loadPurchaseData(p);
						p.closeInventory();
						Main.invManager.setupShopInventories(p);
						Main.invManager.setupCreateClassInventory(p);
						Main.invManager.setupPlayerSelectionInventories(p);
						return;
					} else {
						p.closeInventory();
						return;
					}
				}
			}
		} else if(e.getInventory().equals(this.selectClassInventory.get(p))) {
			int slot = 0;
			try {
				slot = e.getSlot();
			} catch(NullPointerException exception) {
				Main.cs.sendMessage("Could not select the proper class");
				return;
			}
			Loadout current = Main.loadManager.getLoadouts(p).get(slot);
			Main.loadManager.setActiveLoadout(p, current);
			p.closeInventory();
			p.sendMessage(Main.codPrefix + "§fYou changed your class to " + current.getName() + ". It will change when you next spawn.");

			return;
		} else {

			ItemStack item = e.getCurrentItem();
			for (Loadout loadout : Main.loadManager.getLoadouts(p)) {

				if (e.getInventory().equals(loadout.getPrimaryInventory())) {

					for (CodGun gun : Main.shopManager.getPurchasedGuns().get(p)) {
						if (gun.getGun().getType().equals(item.getType())) {
							loadout.setPrimary(gun);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

					if (Main.loadManager.getDefaultPrimary().getGun().getType().equals(item.getType())) {
						loadout.setPrimary(Main.loadManager.getDefaultPrimary());
						Main.invManager.setupCreateClassInventory(p);
						p.closeInventory();
						p.openInventory(Main.invManager.createClassInventory.get(p));
						return;
					}

					break;

				} else if (e.getInventory().equals(loadout.getSecondaryInventory())) {

					for (CodGun gun : Main.shopManager.getPurchasedGuns().get(p)) {
						if (gun.getGun().getType().equals(item.getType())) {
							loadout.setSecondary(gun);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

					if (Main.loadManager.getDefaultSecondary().getGun().getType().equals(item.getType())) {
						loadout.setSecondary(Main.loadManager.getDefaultSecondary());
						Main.invManager.setupCreateClassInventory(p);
						p.closeInventory();
						p.openInventory(Main.invManager.createClassInventory.get(p));
						return;
					}

					break;

				} else if (e.getInventory().equals(loadout.getLethalInventory())) {

					for (CodWeapon grenade : Main.shopManager.getPurchasedWeapons().get(p)) {
						if (grenade.getWeapon().getType().equals(item.getType())) {
							loadout.setLethal(grenade);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

					if (Main.loadManager.getDefaultLethal().getWeapon().getType().equals(item.getType())) {
						loadout.setLethal(Main.loadManager.getDefaultLethal());
						Main.invManager.setupCreateClassInventory(p);
						p.closeInventory();
						p.openInventory(Main.invManager.createClassInventory.get(p));
						return;
					}

					break;

				} else if (e.getInventory().equals(loadout.getTacticalInventory())) {

					for (CodWeapon grenade : Main.shopManager.getPurchasedWeapons().get(p)) {
						if (grenade.getWeapon().getType().equals(item.getType())) {
							loadout.setTactical(grenade);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

					if (Main.loadManager.getDefaultTactical().getWeapon().getType().equals(item.getType())) {
						loadout.setTactical(Main.loadManager.getDefaultTactical());
						Main.invManager.setupCreateClassInventory(p);
						p.closeInventory();
						p.openInventory(Main.invManager.createClassInventory.get(p));
						return;
					}

					break;

				} else if (e.getInventory().equals(loadout.getPerk1Inventory())) {
					for (CodPerk perk : Main.shopManager.getPerks(p)) {
						if (perk.getItem().getType().equals(item.getType())
								&& perk.getPerk().getName().equals(item.getItemMeta().getDisplayName())) {
							loadout.setPerk(PerkSlot.ONE, perk);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

				} else if (e.getInventory().equals(loadout.getPerk2Inventory())) {
					for (CodPerk perk : Main.shopManager.getPerks(p)) {
						if (perk.getItem().getType().equals(item.getType())
								&& perk.getPerk().getName().equals(item.getItemMeta().getDisplayName())) {
							loadout.setPerk(PerkSlot.TWO, perk);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

				} else if (e.getInventory().equals(loadout.getPerk3Inventory())) {
					for (CodPerk perk : Main.shopManager.getPerks(p)) {
						if (perk.getItem().getType().equals(item.getType())
								&& perk.getPerk().getName().equals(item.getItemMeta().getDisplayName())) {
							loadout.setPerk(PerkSlot.THREE, perk);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

				}
			}

			if (e.getCurrentItem().equals(Main.invManager.closeInv)) {
				p.closeInventory();
				return;
			}

		}

	}

	/*
	 * 1. [Find match] 2. [Create-a-Class] 3. [Scorestreaks] 4. [Prestige] 5.
	 * [Assignments & Contracts] 6. [Clan Tag] 7. [Combat Record] 8.
	 * [Leaderboard] 9. [Close]
	 */

	////////////////////////////////////////////////////////////////////////////////////////////////////
}
