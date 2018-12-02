package com.rhetorical.cod.inventories;

import java.util.*;

import com.rhetorical.cod.object.*;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.rhetorical.cod.CreditManager;
import com.rhetorical.cod.GameManager;
import com.rhetorical.cod.Main;
import com.rhetorical.cod.StatHandler;

import static com.rhetorical.cod.Main.lastLoc;
import static com.rhetorical.cod.Main.lobbyLoc;

public class InventoryManager implements Listener {

	public ItemStack closeInv = new ItemStack(Material.BARRIER);
	private ItemStack backInv = new ItemStack(Material.REDSTONE);

	public Inventory mainInventory;
	public Inventory mainShopInventory;
	private Inventory leaderboardInventory;
	public HashMap<Player, Inventory> createClassInventory = new HashMap<>();
	private HashMap<Player, Inventory> selectClassInventory = new HashMap<>();
	private HashMap<Player, Inventory> personalStatistics = new HashMap<>();
	private HashMap<Player, Inventory> killStreakInventory = new HashMap<>();

	private ItemStack joinGame = new ItemStack(Material.EMERALD);
	private ItemStack createClass = new ItemStack(Material.CHEST);
	private ItemStack scoreStreaks = new ItemStack(Material.DIAMOND);
	private ItemStack combatRecord = new ItemStack(Material.PAPER);
	private ItemStack leaderboard = new ItemStack(Material.PAPER);

	private ItemStack shopItem = new ItemStack(Material.EMERALD);
	private ItemStack gunShopItem = new ItemStack(Material.CHEST);
	private ItemStack grenadeShopItem = new ItemStack(Material.CHEST);
	private ItemStack perkShopItem = new ItemStack(Material.CHEST);

	public ItemStack codItem = new ItemStack(Material.ENDER_PEARL);
	public ItemStack leaveItem = new ItemStack(Material.BARRIER);

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
			if (loadout.getPrimaryInventory().equals(i) || loadout.getSecondaryInventory().equals(i) || loadout.getLethalInventory().equals(i) || loadout.getTacticalInventory().equals(i) || loadout.getPerk1Inventory().equals(i) || loadout.getPerk2Inventory().equals(i) || loadout.getPerk3Inventory().equals(i)) {
				return true;
			}
		}

		return i.equals(this.selectClassInventory.get(p)) || i.equals(leaderboardInventory) || i.equals(personalStatistics.get(p)) || i.equals(mainShopInventory) || i.equals(Main.shopManager.gunShop.get(p)) || i.equals(Main.shopManager.weaponShop.get(p)) || i.equals(Main.shopManager.perkShop.get(p)) || i.equals(this.killStreakInventory.get(p));

	}

	public InventoryManager() {

		mainInventory = Bukkit.createInventory(null, 18, "COM-Warfare");
		mainShopInventory = Bukkit.createInventory(null, 9, "Shop Menu");
		leaderboardInventory = Bukkit.createInventory(null, 36, "Leaderboard");

		setupStaticItems();
		setupMainInventories();

		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());
	}

	private void setupStaticItems() {
		ItemMeta closeInvMeta = closeInv.getItemMeta();
		closeInvMeta.setDisplayName(ChatColor.RED + "" +  ChatColor.BOLD + "Close");
		closeInv.setItemMeta(closeInvMeta);

		ItemMeta leaveMeta = leaveItem.getItemMeta();
		leaveMeta.setDisplayName("" + ChatColor.RED + ChatColor.BOLD + "Leave Game");
		List<String> leaveLore = new ArrayList<>();
		leaveLore.add(ChatColor.GOLD + "Right or left click this");
		leaveLore.add(ChatColor.GOLD + "item to leave the lobby.");
		leaveMeta.setLore(leaveLore);
		leaveItem.setItemMeta(leaveMeta);

		ItemMeta codMeta = codItem.getItemMeta();
		codMeta.setDisplayName("" + ChatColor.GOLD + ChatColor.BOLD + "Open Menu");
		List<String> codLore = new ArrayList<>();
		codLore.add(ChatColor.WHITE + "Right or left click this");
		codLore.add(ChatColor.WHITE + "item to open the cod menu.");
		codMeta.setLore(codLore);
		codItem.setItemMeta(codMeta);
	}

	public void setupBackInvButton() {
		ItemMeta backInvMeta = backInv.getItemMeta();
		backInvMeta.setDisplayName("\u00A7c\u00A7lBack");
		backInv.setItemMeta(backInvMeta);
	}

	// Main Inventory

	private void setupMainInventories() {
		joinGame = new ItemStack(Material.EMERALD);
		ItemMeta joinGameMeta = joinGame.getItemMeta();
		joinGameMeta.setDisplayName("\u00A7a\u00A7lFind Match");
		ArrayList<String> joinGameLore = new ArrayList<>();
		joinGameLore.add("\u00A76Utilize the matchmaker to find a match ");
		joinGameLore.add("\u00A76with the best match for you!");
		joinGameMeta.setLore(joinGameLore);
		joinGame.setItemMeta(joinGameMeta);

		mainInventory.setItem(0, joinGame);

		createClass = new ItemStack(Material.CHEST);
		ItemMeta createClassMeta = createClass.getItemMeta();
		createClassMeta.setDisplayName("\u00A74\u00A7lCreate-a-Class");
		ArrayList<String> createClassLore = new ArrayList<>();
		createClassLore.add("\u00A76Create custom loadouts for you to ");
		createClassLore.add("\u00A76use in game!");
		createClassMeta.setLore(createClassLore);
		createClass.setItemMeta(createClassMeta);

		mainInventory.setItem(1, createClass);

		scoreStreaks = new ItemStack(Material.DIAMOND);
		ItemMeta scoreStreakMeta = scoreStreaks.getItemMeta();
		scoreStreakMeta.setDisplayName("\u00A7b\u00A7lScorestreaks");
		ArrayList<String> scoreStreakLore = new ArrayList<>();
		scoreStreakLore.add("\u00A76Choose which scorestreaks you want");
		scoreStreakLore.add("\u00A76to use during a match!");
		scoreStreakMeta.setLore(scoreStreakLore);
		scoreStreaks.setItemMeta(scoreStreakMeta);

		mainInventory.setItem(2, scoreStreaks);

		ItemStack prestige = new ItemStack(Material.ANVIL);
		ItemMeta prestigeMeta = prestige.getItemMeta();
		prestigeMeta.setDisplayName("\u00A76\u00A7lPrestige Options");
		ArrayList<String> prestigeLore = new ArrayList<>();
		prestigeLore.add("\u00A76Prestige your account and unlock ");
		prestigeLore.add("\u00A76special awards!");
		prestigeMeta.setLore(prestigeLore);
		prestige.setItemMeta(prestigeMeta);

		mainInventory.setItem(3, prestige);

		ItemStack assignments = new ItemStack(Material.GOLD_INGOT);
		ItemMeta assignmentMeta = assignments.getItemMeta();
		assignmentMeta.setDisplayName("\u00A73\u00A7lAssignments & Contracts");
		ArrayList<String> assignmentLore = new ArrayList<>();
		assignmentLore.add("\u00A76Complete assignments and buy ");
		assignmentLore.add("\u00A76contracts to get extra rewards ");
		assignmentLore.add("\u00A76for playing the game and completing ");
		assignmentLore.add("\u00A76challenges!");
		assignmentMeta.setLore(assignmentLore);
		assignments.setItemMeta(assignmentMeta);

		mainInventory.setItem(4, assignments);

		ItemStack clanTag = new ItemStack(Material.SHEARS);
		ItemMeta clanTagMeta = clanTag.getItemMeta();
		clanTagMeta.setDisplayName("\u00A75\u00A7lClan Tag");
		ArrayList<String> clanTagLore = new ArrayList<>();
		clanTagLore.add("\u00A76Change your clan tag in the chat!");
		clanTagMeta.setLore(clanTagLore);
		clanTag.setItemMeta(clanTagMeta);

		mainInventory.setItem(5, clanTag);

		combatRecord = new ItemStack(Material.PAPER);
		ItemMeta combatRecordMeta = combatRecord.getItemMeta();
		combatRecordMeta.setDisplayName("\u00A79\u00A7lCombat Record");
		ArrayList<String> combatRecordLore = new ArrayList<>();
		combatRecordLore.add("\u00A76Check your combat record and see what ");
		combatRecordLore.add("\u00A76weapons and killstreaks you've gotten  ");
		combatRecordLore.add("\u00A76the most kills with!");
		combatRecordMeta.setLore(combatRecordLore);
		combatRecord.setItemMeta(combatRecordMeta);

		mainInventory.setItem(6, combatRecord);

		leaderboard = new ItemStack(Material.PAPER);
		ItemMeta leaderboardMeta = leaderboard.getItemMeta();
		leaderboardMeta.setDisplayName("\u00A72\u00A7lLeaderboard");
		ArrayList<String> leaderboardLore = new ArrayList<>();
		leaderboardLore.add("\u00A76Compare where your stats are compared ");
		leaderboardLore.add("\u00A76to everyone else's!");
		leaderboardMeta.setLore(leaderboardLore);
		leaderboard.setItemMeta(leaderboardMeta);

		mainInventory.setItem(7, leaderboard);

		mainInventory.setItem(8, this.closeInv);

		ItemStack shop = shopItem;
		ItemMeta shopMeta = shop.getItemMeta();
		shopMeta.setDisplayName("\u00A7a\u00A7lShop");
		ArrayList<String> shopLore = new ArrayList<>();
		shopLore.add("\u00A76Buy items here at the shop!");
		shopMeta.setLore(shopLore);
		shop.setItemMeta(shopMeta);

		shopItem = shop;

		mainInventory.setItem(9, shopItem);

		ItemStack gunItem;
		try {
			gunItem = new ItemStack(Material.WOODEN_HOE);
		} catch(Exception e) {
			gunItem = new ItemStack(Material.valueOf("WOOD_HOE"));
		}
		ItemMeta gunMeta = gunItem.getItemMeta();
		gunMeta.setDisplayName("\u00A79Gun Shop");
		ArrayList<String> gunLore = new ArrayList<>();
		gunLore.add("\u00A76Buy guns that you have unlocked here!");
		gunMeta.setLore(gunLore);
		gunItem.setItemMeta(gunMeta);

		gunShopItem = gunItem;

		ItemStack grenadeItem = new ItemStack(Material.SLIME_BALL);
		ItemMeta grenadeMeta = grenadeItem.getItemMeta();
		grenadeMeta.setDisplayName("\u00A7cGrenade Shop");
		ArrayList<String> grenadeLore = new ArrayList<>();
		grenadeLore.add("\u00A76Buy grenades that you have unlocked here!");
		gunMeta.setLore(grenadeLore);
		grenadeItem.setItemMeta(grenadeMeta);

		grenadeShopItem = grenadeItem;

		ItemStack perkItem = new ItemStack(Material.APPLE);
		ItemMeta perkMeta = perkItem.getItemMeta();
		perkMeta.setDisplayName("\u00A7aPerk Shop");
		ArrayList<String> perkLore = new ArrayList<>();
		perkLore.add("\u00A76Buy perks that you have unlocked here!");
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

		Inventory customClassInventory = Bukkit.createInventory(p, 9 * Main.loadManager.getAllowedClasses(p), "Create-a-Class");

		int line;

		for (int i = 0; i < Main.loadManager.getAllowedClasses(p); i++) {
			line = i * 9;

			Loadout loadout = Main.loadManager.getLoadouts(p).get(i);

			ItemStack header;
			try {
				header = new ItemStack(Material.valueOf("CRAFTING_TABLE"));
			} catch(Exception e) {
				header = new ItemStack(Material.valueOf("WORKBENCH"));
			}
			ItemMeta headerMeta = header.getItemMeta();
			headerMeta.setDisplayName(loadout.getName());
			ArrayList<String> headerLore = new ArrayList<>();
			headerLore.add("\u00A76Edit the class to the right.");
			headerMeta.setLore(headerLore);
			header.setItemMeta(headerMeta);

			ItemStack primary = loadout.getPrimary().getGun();
			ItemMeta primaryMeta = primary.getItemMeta();
			primaryMeta.setDisplayName("\u00A76Primary Weapon\u00A7f: \u00A7r\u00A7f" + loadout.getPrimary().getName());
			ArrayList<String> primaryLore = new ArrayList<>();
			primaryLore.add("\u00A76This is your primary weapon. During games,");
			primaryLore.add("\u00A76this should be your go-to gun.");
			primaryMeta.setLore(primaryLore);
			primary.setItemMeta(primaryMeta);

			ItemStack secondary = loadout.getSecondary().getGun();
			ItemMeta secondaryMeta = secondary.getItemMeta();
			secondaryMeta.setDisplayName("\u00A76Secondary Weapon\u00A7f: \u00A7r\u00A7f" + loadout.getSecondary().getName());
			ArrayList<String> secondaryLore = new ArrayList<>();
			secondaryLore.add("\u00A76This is your secondary weapon. During games,");
			secondaryLore.add("\u00A76this should be used as a backup weapon.");
			secondaryMeta.setLore(secondaryLore);
			secondary.setItemMeta(secondaryMeta);

			ItemStack lethal = loadout.getLethal().getWeapon();
			ItemMeta lethalMeta = lethal.getItemMeta();
			lethalMeta.setDisplayName("\u00A76Lethal Grenade\u00A7f: \u00A7r\u00A7f" + loadout.getLethal().getName());
			ArrayList<String> lethalLore = new ArrayList<>();
			lethalLore.add("\u00A76This is your lethal grenade. Use it");
			lethalLore.add("\u00A76during games to kill players.");
			lethalMeta.setLore(lethalLore);
			lethal.setItemMeta(lethalMeta);

			ItemStack tactical = loadout.getTactical().getWeapon();
			ItemMeta tacticalMeta = tactical.getItemMeta();
			tacticalMeta.setDisplayName("\u00A76Tactical Grenade\u00A7f: \u00A7r\u00A7f" + loadout.getTactical().getName());
			ArrayList<String> tacticalLore = new ArrayList<>();
			tacticalLore.add("\u00A76This is your tactical grenade. Use it");
			tacticalLore.add("\u00A76to disorient other players and use");
			tacticalLore.add("\u00A76it to gain a tactical advantage over players.");
			tacticalMeta.setLore(tacticalLore);
			tactical.setItemMeta(tacticalMeta);

			ItemStack perkOne = loadout.getPerk1().getItem();
			ItemMeta perkOneMeta = perkOne.getItemMeta();
			perkOneMeta.setDisplayName("\u00A76Perk 1\u00A7f: \u00A7r\u00A7f" + loadout.getPerk1().getPerk().getName());
			perkOneMeta.setLore(loadout.getPerk1().getLore());
			perkOne.setItemMeta(perkOneMeta);

			ItemStack perkTwo = loadout.getPerk2().getItem();
			ItemMeta perkTwoMeta = perkTwo.getItemMeta();
			perkTwoMeta.setDisplayName("\u00A76Perk 2\u00A7f: \u00A7r\u00A7f" + loadout.getPerk2().getPerk().getName());
			perkTwoMeta.setLore(loadout.getPerk2().getLore());
			perkTwo.setItemMeta(perkTwoMeta);

			ItemStack perkThree = loadout.getPerk3().getItem();
			ItemMeta perkThreeMeta = perkThree.getItemMeta();
			perkThreeMeta.setDisplayName("\u00A76Perk 3\u00A7f: \u00A7r\u00A7f" + loadout.getPerk3().getPerk().getName());
			perkThreeMeta.setLore(loadout.getPerk3().getLore());
			perkThree.setItemMeta(perkThreeMeta);

			customClassInventory.setItem(line, header);
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

			// primary.addItem(Main.loadManager.getDefaultPrimary().getGun());
			// secondary.addItem(Main.loadManager.getDefaultSecondary().getGun());
			// lethal.addItem(Main.loadManager.getDefaultLethal().getWeapon());
			// tactical.addItem(Main.loadManager.getDefaultTactical().getWeapon());

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

	private void setupShopInventories(Player p) {
		Inventory gunShop = Bukkit.createInventory(p, 36, "Gun Shop");
		Inventory weaponShop = Bukkit.createInventory(p, 36, "Grenade Shop");
		Inventory perkShop = Bukkit.createInventory(p, 36, "Perk Shop");

		ArrayList<CodGun> guns = Main.shopManager.getPrimaryGuns();
		guns.addAll(Main.shopManager.getSecondaryGuns());

		for (CodGun gun : guns) {
			if (gun.getType() == UnlockType.BOTH || gun.getType() == UnlockType.CREDITS) {
				if (Main.progressionManager.getLevel(p) >= gun.getLevelUnlock()) {

					ItemStack item = gun.getGun();

					ItemMeta gunMeta = item.getItemMeta();

					ArrayList<String> lore = new ArrayList<>();

					lore.add("\u00A76Cost: " + gun.getCreditUnlock());

					gunMeta.setLore(lore);

					item.setItemMeta(gunMeta);

					gunShop.addItem(item);

				}

			}

		}

		ArrayList<CodWeapon> grenades = Main.shopManager.getLethalWeapons();
		grenades.addAll(Main.shopManager.getTacticalWeapons());

		for (CodWeapon grenade : grenades) {
			if (grenade == null)
				continue;

			if (grenade.getType() == UnlockType.BOTH || grenade.getType() == UnlockType.CREDITS) {
				if (Main.progressionManager.getLevel(p) >= grenade.getLevelUnlock()) {

					ItemStack item = grenade.getWeapon();

					ItemMeta gunMeta = item.getItemMeta();

					ArrayList<String> lore = new ArrayList<>();

					lore.add("\u00A76Cost: " + grenade.getCreditUnlock());

					gunMeta.setLore(lore);

					item.setItemMeta(gunMeta);

					weaponShop.addItem(item);

				}

			}

		}

		ArrayList<CodPerk> perks = Main.perkManager.getAvailablePerks();
		for (CodPerk perk : perks) {

			if (perk.getPerk().getName().equals(Main.perkManager.getDefaultOne().getPerk().getName()) || perk.getPerk().getName().equals(Main.perkManager.getDefaultTwo().getPerk().getName()) || perk.getPerk().getName().equals(Main.perkManager.getDefaultThree().getPerk().getName())) {
				continue;
			}

			Main.shopManager.purchasedPerks.computeIfAbsent(p, k -> new ArrayList<>());

			if (!Main.shopManager.purchasedPerks.get(p).contains(perk) && !perk.equals(Main.perkManager.getDefaultOne()) && !perk.equals(Main.perkManager.getDefaultTwo()) && !perk.equals(Main.perkManager.getDefaultThree())) {
				ItemStack item = perk.getItem();
				ItemMeta perkMeta = item.getItemMeta();
				if (perkMeta.getLore() == null) {
					perkMeta.setLore(new ArrayList<>());
				}
				ArrayList<String> lore = (ArrayList<String>) perkMeta.getLore();
				lore.add("\u00A76Cost: " + perk.getCost());
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

	private void setupSelectClassInventory(Player p) {

		Inventory inventory = Bukkit.createInventory(p, 9, "Select Class");

		for (int i = 0; i < Main.loadManager.getAllowedClasses(p); i++) {

			Loadout loadout = Main.loadManager.getLoadouts(p).get(i);

			ItemStack item = loadout.getPrimary().getGun();

			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(loadout.getName());

			ArrayList<String> lore = new ArrayList<>();
			lore.add("\u00A76Primary: " + loadout.getPrimary().getName());
			lore.add("\u00A76Secondary: " + loadout.getSecondary().getName());
			lore.add("\u00A76Lethal: " + loadout.getLethal().getName());
			lore.add("\u00A76Tactical: " + loadout.getTactical().getName());
			lore.add("\u00A76Perk 1: " + loadout.getPerk1().getPerk().toString());
			lore.add("\u00A76Perk 2: " + loadout.getPerk2().getPerk().toString());
			lore.add("\u00A76Perk 3: " + loadout.getPerk3().getPerk().toString());

			meta.setLore(lore);
			item.setItemMeta(meta);

			inventory.setItem(i, item);
		}

		selectClassInventory.put(p, inventory);
	}

	public boolean openSelectClassInventory(Player p) {
		if (!GameManager.isInMatch(p)) {
			Main.sendMessage(p,Main.codPrefix + "\u00A7cYou can't select a class while not in a game!", Main.lang);
			return false;
		}

		if (this.selectClassInventory.get(p) == null) {
			this.setupSelectClassInventory(p);
		}

		p.closeInventory();
		p.openInventory(this.selectClassInventory.get(p));
		return true;
	}

	private void setupLeaderBoard() {
		leaderboardInventory.setItem(35, closeInv);
		ArrayList<String> pls = StatHandler.getLeaderboardList();

		TreeMap<Double, String> expMap = new TreeMap<>();

		HashMap<String, ItemStack> leaderboardOrder = new HashMap<>();

		for (String name : pls) {
			ItemStack player;
			try {
				player = new ItemStack(Material.SKELETON_SKULL);
			} catch(Exception e) {
				player = new ItemStack(Material.valueOf("SKULL"));
			}
			ItemMeta playerMeta = player.getItemMeta();
			playerMeta.setDisplayName("\u00A7f\u00A7lPlayer: " + name);

			double experience = StatHandler.getExperience(name);

			player.setItemMeta(playerMeta);

			leaderboardOrder.put(name, player);

			expMap.put(experience, name);
		}

		for (int i = 0, pos = 1; i < expMap.size(); i++, pos++) {
			String id = expMap.get(expMap.descendingKeySet().toArray()[i]);
			ItemStack item = leaderboardOrder.get(id);
			ItemMeta itemMeta = item.getItemMeta();
			float kills = (float) StatHandler.getKills(id);
			float deaths = (float) StatHandler.getDeaths(id);

			double experience = StatHandler.getExperience(id);

			float kdr = kills / deaths;

			ArrayList<String> lore = new ArrayList<>();

			lore.add("\u00A7f\u00A7lPosition: " + pos);
			lore.add("\u00A76\u00A7lScore: " + experience);
			lore.add("\u00A7a\u00A7lKills: " + (int) kills);
			lore.add("\u00A7c\u00A7lDeaths: " + (int) deaths);
			if (!Float.isNaN(kdr)) {
				lore.add("\u00A75\u00A7lKDR: " + kdr);
			} else {
				lore.add("\u00A75\u00A7lKDR: " + (int) kills);
			}

			Main.sendMessage(Main.cs, item.toString() + " pos : " + pos, Main.lang);

			itemMeta.setLore(lore);

			item.setItemMeta(itemMeta);

			leaderboardInventory.setItem(pos - 1, item);

		}

		System.gc();
	}

	private void setupPersonalStatsBoardMenu(Player p) {
		if (!personalStatistics.containsKey(p)) {
			personalStatistics.put(p, Bukkit.createInventory(null, 9, "Combat Record"));
		}

		Inventory inv = personalStatistics.get(p);

		inv.clear();

		int totalKills = StatHandler.getKills(p.getName());
		int totalDeaths = StatHandler.getDeaths(p.getName());

		float kdr = ((float) totalKills) / ((float) totalDeaths);

		ItemStack kills = new ItemStack(Material.ARROW);
		ItemMeta killsMeta = kills.getItemMeta();
		killsMeta.setDisplayName("\u00A7a\u00A7lKills: \u00A7r\u00A7f" + totalKills);
		kills.setItemMeta(killsMeta);

		ItemStack deaths;
		try {
			deaths = new ItemStack(Material.SKELETON_SKULL);
		} catch(Exception e) {
			deaths = new ItemStack(Material.valueOf("SKULL"));
		}
		ItemMeta deathsMeta = deaths.getItemMeta();
		deathsMeta.setDisplayName("\u00A7c\u00A7lDeaths: \u00A7r\u00A7f" + totalDeaths);
		deaths.setItemMeta(deathsMeta);

		ItemStack killDeathRatio = new ItemStack(Material.GLASS_BOTTLE);
		ItemMeta killDeathRatioMeta = killDeathRatio.getItemMeta();
		if (!Float.isNaN(kdr)) {
			killDeathRatioMeta.setDisplayName("\u00A76\u00A7lKDR: \u00A7r\u00A7f" + kdr);
		} else {
			killDeathRatioMeta.setDisplayName("\u00A76\u00A7lKDR: \u00A7r\u00A7f" + totalKills);
		}

		killDeathRatio.setItemMeta(killDeathRatioMeta);

		inv.setItem(0, kills);
		inv.setItem(1, deaths);
		inv.setItem(2, killDeathRatio);
		inv.setItem(8, closeInv);

		personalStatistics.put(p, inv);
	}

	private boolean openPersonalStatsMenu(Player p) {

		setupPersonalStatsBoardMenu(p);

		Inventory inv = personalStatistics.get(p);

		p.openInventory(inv);

		return true;
	}

	private void setupKillStreaksInventory(Player p) {

		Inventory inv = Bukkit.createInventory(null, 36, "Killstreaks");

		inv.setItem(0, KillStreakManager.uavItem);
		inv.setItem(2, KillStreakManager.counterUavItem);
		inv.setItem(4, KillStreakManager.nukeItem);

		inv.setItem(35, closeInv);

		this.killStreakInventory.put(p, inv);

	}

	private boolean openKillStreaksInventory(Player p) {

		setupKillStreaksInventory(p);
		Inventory inv = killStreakInventory.get(p);
		p.openInventory(inv);
		return true;
	}

	@EventHandler
	public void inventoryClickListener(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;

		Player p = (Player) e.getWhoClicked();

		if (e.getInventory() == null)
			return;

		try {
			if (shouldCancelClick(e.getInventory(), p)) {
				e.setCancelled(true);
			} else {
				return;
			}
		} catch(Exception exception) {
			Main.sendMessage(Main.cs, "\u00A7c Make sure that you have the default weapons and guns set!", Main.lang);
		}

		if (e.getCurrentItem() == null)
			return;

		if (e.getCurrentItem().equals(closeInv)) {
			p.closeInventory();
		}

		if (e.getClickedInventory().equals(mainInventory)) {

			if (e.getCurrentItem().equals(joinGame)) {
				Main.sendMessage(p,Main.codPrefix + "\u00A77Put in matchmaker queue. . .", Main.lang);
				GameManager.findMatch(p);
				p.closeInventory();
			} else if (e.getCurrentItem().equals(createClass)) {
				p.closeInventory();
				this.setupCreateClassInventory(p);
				p.openInventory(createClassInventory.get(p));
			} else if (e.getCurrentItem().equals(shopItem)) {
				p.closeInventory();
				p.openInventory(mainShopInventory);
				setupShopInventories(p);
			} else if (e.getCurrentItem().equals(combatRecord)) {
				p.closeInventory();
				openPersonalStatsMenu(p);
			} else if (e.getCurrentItem().equals(leaderboard)) {
				p.closeInventory();
				setupLeaderBoard();
				p.openInventory(leaderboardInventory);
			} else if (e.getCurrentItem().equals(scoreStreaks)) {
				p.closeInventory();
				this.openKillStreaksInventory(p);
			} else if (e.getCurrentItem().equals(closeInv)) {
				p.closeInventory();
			}
		} else if (e.getInventory().equals(mainShopInventory)) {
			if (e.getCurrentItem().equals(gunShopItem)) {
				p.closeInventory();
				if (!Main.shopManager.gunShop.containsKey(p)) {
					this.setupShopInventories(p);
				}
				p.openInventory(Main.shopManager.gunShop.get(p));
			} else if (e.getCurrentItem().equals(grenadeShopItem)) {
				p.closeInventory();
				if (!Main.shopManager.weaponShop.containsKey(p)) {
					this.setupShopInventories(p);
				}
				p.openInventory(Main.shopManager.weaponShop.get(p));
			} else if (e.getCurrentItem().equals(perkShopItem)) {
				p.closeInventory();
				if (!Main.shopManager.weaponShop.containsKey(p)) {
					this.setupShopInventories(p);
				}
				p.openInventory(Main.shopManager.perkShop.get(p));
			} else if (e.getCurrentItem().equals(shopItem)) {
				p.closeInventory();
				p.openInventory(mainShopInventory);
			}
		} else if (e.getInventory().equals(killStreakInventory.get(p))) {
			if (e.getCurrentItem() == null || e.getCurrentItem().getType().equals(Material.AIR)) {
				return;
			}

			if (e.getCurrentItem().getItemMeta().equals(KillStreakManager.uavItem.getItemMeta()) || e.getCurrentItem().getItemMeta().equals(KillStreakManager.counterUavItem.getItemMeta()) || e.getCurrentItem().getItemMeta().equals(KillStreakManager.nukeItem.getItemMeta())) {
				ItemStack i = e.getCurrentItem();
				switch (i.getAmount()) {
					case 1:
						i.setAmount(2);
						break;
					case 2:
						i.setAmount(3);
						break;
					case 3:
						i.setAmount(9);
						break;
					case 9:
						i.setAmount(1);
						break;
					default:
						i.setAmount(9);
						break;
				}
			} else if (e.getCurrentItem().getType().equals(Material.CHEST)) {

				ItemStack uavStack = this.killStreakInventory.get(p).getItem(0);
				ItemStack counterUavStack = this.killStreakInventory.get(p).getItem(2);
				ItemStack nukeStack = this.killStreakInventory.get(p).getItem(4);

				Integer[] usedNumbers = new Integer[3];

				HashMap<KillStreak, Integer> numMap = new HashMap<>();

				if (uavStack.getAmount() != 9) {
					usedNumbers[0] = uavStack.getAmount();
					numMap.put(KillStreak.UAV, uavStack.getAmount());
				}

				if (counterUavStack.getAmount() != 9) {
					for (int i : usedNumbers) {
						if (i == counterUavStack.getAmount()) {
							Main.sendMessage(p,Main.codPrefix + "\u00A7cCan't save killstreaks! More than one item has the same killstreak slot number!", Main.lang);
							return;
						}
					}

					usedNumbers[usedNumbers.length - 1] = counterUavStack.getAmount();
					numMap.put(KillStreak.COUNTER_UAV, counterUavStack.getAmount());
				}

				if (nukeStack.getAmount() != 9) {
					for (int i : usedNumbers) {
						if (i == nukeStack.getAmount()) {
							Main.sendMessage(p,Main.codPrefix + "\u00A7cCan't save killstreaks! More than one item has the same killstreak slot number!",Main.lang);
							return;
						}
					}

					usedNumbers[usedNumbers.length - 1] = nukeStack.getAmount();
					numMap.put(KillStreak.NUKE, nukeStack.getAmount());
				}
				///// FOR ANY NEW KILLSTREAKS, ADD THE ABOVE CODE FOR NEW STREAKS /////

				if (!(usedNumbers[2] != null && usedNumbers[2] != 0 && usedNumbers[2] != 9)) {
					Main.sendMessage(p,Main.codPrefix + "\u00A7cThere were not enough killstreaks set to save!", Main.lang);
					return;
				}

				KillStreak[] toSet = new KillStreak[3];

				int i = 0;
				for (KillStreak k : numMap.keySet()) {
					toSet[i] = k;
					i++;
				}

				Main.killstreakManager.setStreaks(p, toSet[0], toSet[1], toSet[2]);

			}

		} else if (e.getInventory().equals(createClassInventory.get(p))) {

			// TODO: - Change class name when clicking class icon on left

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

		} else if (Main.shopManager.gunShop.get(p) != null && e.getInventory().equals(Main.shopManager.gunShop.get(p))) {

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
				if (e.getCurrentItem().getType().equals(perk.getItem().getType()) && e.getCurrentItem().getItemMeta().getDisplayName().equals(perk.getPerk().getName())) {
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
		} else if (e.getInventory().equals(this.selectClassInventory.get(p))) {
			int slot;
			try {
				slot = e.getSlot();
			} catch (NullPointerException exception) {
				Main.sendMessage(Main.cs, "Could not select the proper class", Main.lang);
				return;
			}

			boolean hasOneManArmy = false;

			if (Main.loadManager.getCurrentLoadout(p).hasPerk(Perk.ONE_MAN_ARMY)) {
				hasOneManArmy = true;
			}

			Loadout current = Main.loadManager.getLoadouts(p).get(slot);
			Main.loadManager.setActiveLoadout(p, current);
			p.closeInventory();

			if (!hasOneManArmy) {
				Main.sendMessage(p, Main.codPrefix + "\u00A7fYou changed your class to " + current.getName() + ". It will change when you next spawn.", Main.lang);
				Main.perkListener.oneManArmy(p);
			} else {
				Main.sendMessage(p, Main.codPrefix + "\u00A7fyou changed your class to " + current.getName() + ". It will change in 10 seconds if you don't move.", Main.lang);
			}

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
						if (perk.getItem().getType().equals(item.getType()) && perk.getPerk().getName().equals(item.getItemMeta().getDisplayName())) {
							loadout.setPerk(PerkSlot.ONE, perk);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

				} else if (e.getInventory().equals(loadout.getPerk2Inventory())) {
					for (CodPerk perk : Main.shopManager.getPerks(p)) {
						if (perk.getItem().getType().equals(item.getType()) && perk.getPerk().getName().equals(item.getItemMeta().getDisplayName())) {
							loadout.setPerk(PerkSlot.TWO, perk);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

				} else if (e.getInventory().equals(loadout.getPerk3Inventory())) {
					for (CodPerk perk : Main.shopManager.getPerks(p)) {
						if (perk.getItem().getType().equals(item.getType()) && perk.getPerk().getName().equals(item.getItemMeta().getDisplayName())) {
							loadout.setPerk(PerkSlot.THREE, perk);
							Main.invManager.setupCreateClassInventory(p);
							p.closeInventory();
							p.openInventory(Main.invManager.createClassInventory.get(p));
							return;
						}
					}

				}
			}

			if (e.getCurrentItem().equals(closeInv)) {
				p.closeInventory();
			}

		}

	}

	@EventHandler
	public void itemUseListener(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {

			if (e.getPlayer().getInventory().getItemInMainHand().equals(leaveItem) || e.getPlayer().getInventory().getItemInOffHand().equals(leaveItem)) {
				if (!GameManager.isInMatch(e.getPlayer())) return;
				GameManager.leaveMatch(e.getPlayer());
				if (Main.lastLoc.containsKey(e.getPlayer())) {
					e.getPlayer().teleport(lastLoc.get(e.getPlayer()));
					lastLoc.remove(e.getPlayer());
				} else {
					if (lobbyLoc != null) {
						e.getPlayer().teleport(lobbyLoc);
					}
				}
				e.setCancelled(true);
				return;
			}
			if (e.getPlayer().getInventory().getItemInMainHand().equals(codItem) || e.getPlayer().getInventory().getItemInOffHand().equals(codItem)) {
				Main.openMainMenu(e.getPlayer());
				e.setCancelled(true);
//				return;
			}
		}
	}

}
