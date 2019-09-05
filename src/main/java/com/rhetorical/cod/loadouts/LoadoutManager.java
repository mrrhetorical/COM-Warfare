package com.rhetorical.cod.loadouts;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.files.LoadoutsFile;
import com.rhetorical.cod.inventories.ShopManager;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.perks.CodPerk;
import com.rhetorical.cod.perks.Perk;
import com.rhetorical.cod.perks.PerkManager;
import com.rhetorical.cod.perks.PerkSlot;
import com.rhetorical.cod.progression.ProgressionManager;
import com.rhetorical.cod.weapons.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class LoadoutManager {

	private static LoadoutManager instance;

	private Map<Player, Integer> allowedClasses = new HashMap<>();
	private Map<Player, List<Loadout>> playerLoadouts = new HashMap<>();
	private Map<Player, Loadout> activeLoadouts = new HashMap<>();

	public ItemStack knife;

	public CodGun blankPrimary,
			blankSecondary;

	public CodWeapon blankLethal,
			blankTactical;

	public CodGun defaultPrimary,
			defaultSecondary;

	public CodWeapon defaultLethal,
			defaultTactical;

	private ItemStack emptyPrimary = new ItemStack(Material.BARRIER);
	private ItemStack emptySecondary = new ItemStack(Material.BARRIER);
	private ItemStack emptyLethal = new ItemStack(Material.BARRIER);
	private ItemStack emptyTactical = new ItemStack(Material.BARRIER);

	public LoadoutManager(HashMap<Player, List<Loadout>> pL) {

		if (instance != null)
			return;

		instance = this;

		ItemMeta primaryMeta = emptyPrimary.getItemMeta(),
				secondaryMeta = emptySecondary.getItemMeta(),
				lethalMeta = emptyLethal.getItemMeta(),
				tacticalMeta = emptyTactical.getItemMeta();

		primaryMeta.setDisplayName(Lang.NO_PRIMARY.getMessage());
		secondaryMeta.setDisplayName(Lang.NO_SECONDARY.getMessage());
		lethalMeta.setDisplayName(Lang.NO_LETHAL.getMessage());
		tacticalMeta.setDisplayName(Lang.NO_TACTICAL.getMessage());

		blankPrimary = new CodGun("No Primary", GunType.Primary, UnlockType.LEVEL, 0, new ItemStack(Material.AIR), emptyPrimary, 0, true);
		blankSecondary = new CodGun("No Secondary", GunType.Secondary, UnlockType.LEVEL, 0, new ItemStack(Material.AIR), emptySecondary, 0, true);
		blankLethal = new CodWeapon("No Lethal", WeaponType.LETHAL, UnlockType.LEVEL, emptyLethal, 0, true);
		blankTactical = new CodWeapon("No Tactical", WeaponType.TACTICAL, UnlockType.LEVEL, emptyTactical, 0, true);

		emptyPrimary.setItemMeta(primaryMeta);
		emptySecondary.setItemMeta(secondaryMeta);
		emptyLethal.setItemMeta(lethalMeta);
		emptyTactical.setItemMeta(tacticalMeta);

		this.playerLoadouts = pL;
		for (Player p : Bukkit.getOnlinePlayers()) {
			allowedClasses.put(p, getAllowedClasses(p));
		}

		knife = new ItemStack(Material.IRON_SWORD);
		ItemMeta knifeMeta = knife.getItemMeta();
		if (knifeMeta != null) {
			knifeMeta.setDisplayName(ChatColor.YELLOW + "Knife");
			ArrayList<String> knifeLore = new ArrayList<>();
			knifeLore.add(Lang.KNIFE_LORE.getMessage());
			knifeMeta.setLore(knifeLore);
			knifeMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		knife.setItemMeta(knifeMeta);
	}

	public static LoadoutManager getInstance() {
		return instance != null ? instance : new LoadoutManager(new HashMap<>());
	}

	// Method should return true if loadout creation is allowed and is
	// successful, returns false if the creation of the loadout is
	// either unsuccessful or is not allowed
	public boolean create(Player p, String loadoutName, CodGun primaryGun, CodGun secondaryGun, CodWeapon LethalWeapon,
						  CodWeapon tacticalWeapon, CodPerk perkOne, CodPerk perkTwo, CodPerk perkThree) {

		Loadout loadout = new Loadout(p, loadoutName, primaryGun, secondaryGun, LethalWeapon, tacticalWeapon, perkOne,
				perkTwo, perkThree);

		List<Loadout> currentLoadouts = this.getLoadouts(p);

		if (getAllowedClasses(p) < currentLoadouts.size()) {
			currentLoadouts.add(loadout);
			this.playerLoadouts.put(p, currentLoadouts);
		}

		while (getAllowedClasses(p) < currentLoadouts.size()) {
			PerkManager pm = PerkManager.getInstance();
			Loadout defaultLoadout = new Loadout(p, Lang.CLASS_PREFIX + " " + Integer.toString(currentLoadouts.size() + 1),
					getDefaultPrimary(), getDefaultSecondary(), getDefaultLethal(), getDefaultTactical(),
					pm.getDefaultPerk(PerkSlot.ONE), pm.getDefaultPerk(PerkSlot.TWO),
					pm.getDefaultPerk(PerkSlot.THREE));
			currentLoadouts.add(defaultLoadout);
			int next = 0;
			while (LoadoutsFile.getData().contains("Loadouts." + p.getUniqueId() + "." + next)) {
				next++;
			}
			LoadoutsFile.getData().set("Loadouts." + p.getUniqueId() + "." + next, defaultLoadout);
			LoadoutsFile.saveData();
			LoadoutsFile.reloadData();
		}

		this.playerLoadouts.put(p, currentLoadouts);

		return true;
	}

	public void giveLoadout(Player p, Loadout loadout) {
		CodGun primary = loadout.getPrimary();
		CodGun secondary = loadout.getSecondary();
		CodWeapon lethal = loadout.getLethal();
		CodWeapon tactical = loadout.getTactical();

		// Knife
		p.getInventory().setItem(0, knife);

		// Primary & Ammo

		if (!primary.equals(LoadoutManager.getInstance().blankPrimary)) {
			p.getInventory().setItem(1, primary.getGunItem());

			ItemStack primaryAmmo = primary.getAmmo();
			primaryAmmo.setAmount(primary.getAmmoCount());
			p.getInventory().setItem(28, primaryAmmo);
		}

		// Secondary & Ammo

		if (!secondary.equals(LoadoutManager.getInstance().blankSecondary)) {
			p.getInventory().setItem(2, secondary.getGunItem());
			if (!loadout.hasPerk(Perk.ONE_MAN_ARMY)) {
				ItemStack secondaryAmmo = secondary.getAmmo();
				secondaryAmmo.setAmount(secondary.getAmmoCount());
				p.getInventory().setItem(29, secondaryAmmo);
			}
		}

		// Grenades

		if (!lethal.equals(LoadoutManager.getInstance().blankLethal)) {
			p.getInventory().setItem(3, lethal.getWeaponItem());
		}

		if (!tactical.equals(LoadoutManager.getInstance().blankTactical)) {
			p.getInventory().setItem(4, tactical.getWeaponItem());
		}
	}

	public int getAllowedClasses(Player p) {

		int classes = 5;

		for (int i = 1; i <= ProgressionManager.getInstance().getPrestigeLevel(p); i++) {
			switch (i) {
				case 1:
					classes++;
					break;
				case 5:
					classes++;
					break;
				case 7:
					classes++;
					break;
				case 9:
					classes++;
					break;
				default:
					break;
			}
		}

		return classes;
	}

	public CodGun getDefaultPrimary() {

		if (!GunsFile.getData().contains("Guns.Primary.default")) {

			return blankPrimary;
		}

		if (defaultPrimary == null) {

			String gunName = GunsFile.getData().getString("Guns.Primary.default.name");
			int ammoAmount = GunsFile.getData().getInt("Guns.Primary.default.ammoCount");
			Material ammoMat = Material.valueOf(GunsFile.getData().getString("Guns.Primary.default.ammoItem"));
			short ammoData = (short) GunsFile.getData().getInt("Guns.Primary.default.ammoData");
			ItemStack ammoItem = new ItemStack(ammoMat, 1, ammoData);

			Material gunMat = Material.valueOf(GunsFile.getData().getString("Guns.Primary.default.gunItem"));
			short gunData = (short) GunsFile.getData().getInt("Guns.Primary.default.gunData");
			ItemStack gunItem = new ItemStack(gunMat, 1, gunData);

			String ammoName = GunsFile.getData().getString("Guns.Primary.default.ammoName");
			CodGun gun;
			if (ammoName == null) {
				gun = new CodGun(gunName, GunType.Primary, null, ammoAmount, ammoItem, gunItem, 0);
			} else {
				gun = new CodGun(gunName, ammoName, GunType.Primary, null, ammoAmount, ammoItem, gunItem, 0);
			}
			gun.setCreditUnlock(0);

			defaultPrimary = gun;

		}

		return defaultPrimary;
	}

	public CodGun getDefaultSecondary() {

		if (!GunsFile.getData().contains("Guns.Secondary.default")) {

			return blankSecondary;
		}

		if (defaultSecondary == null) {

			String gunName = GunsFile.getData().getString("Guns.Secondary.default.name");
			int ammoAmount = GunsFile.getData().getInt("Guns.Secondary.default.ammoCount");
			Material ammoMat = Material.valueOf(GunsFile.getData().getString("Guns.Secondary.default.ammoItem"));
			short ammoData = (short) GunsFile.getData().getInt("Guns.Secondary.default.ammoData");
			ItemStack ammoItem = new ItemStack(ammoMat, 1, ammoData);

			Material gunMat = Material.valueOf(GunsFile.getData().getString("Guns.Secondary.default.gunItem"));
			short gunData = (short) GunsFile.getData().getInt("Guns.Secondary.default.gunData");
			ItemStack gunItem = new ItemStack(gunMat, 1, gunData);

			String ammoName = GunsFile.getData().getString("Guns.Secondary.default.ammoName");
			CodGun gun;
			if (ammoName == null) {
				gun = new CodGun(gunName, GunType.Secondary, null, ammoAmount, ammoItem, gunItem, 0);
			} else {
				gun = new CodGun(gunName, ammoName, GunType.Secondary, null, ammoAmount, ammoItem, gunItem, 0);
			}

			gun.setCreditUnlock(0);

			defaultSecondary = gun;

		}

		return defaultSecondary;
	}

	public CodWeapon getDefaultLethal() {

		if (!GunsFile.getData().contains("Weapons.LETHAL.default")) {

			return blankLethal;
		}

		if (defaultLethal == null) {

			String weaponName = GunsFile.getData().getString("Weapons.LETHAL.default.name");
			UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.LETHAL.default.unlockType"));
			ItemStack weapon = GunsFile.getData().getItemStack("Weapons.LETHAL.default.item");
			short data = (short) GunsFile.getData().getInt("Weapons.LETHAL.default.data");

			defaultLethal = new CodWeapon(weaponName, WeaponType.LETHAL, type, weapon, 0);
		}

		return defaultLethal;
	}

	public CodWeapon getDefaultTactical() {

		if (!GunsFile.getData().contains("Weapons.TACTICAL.default")) {

			return blankTactical;
		}

		if (defaultTactical == null) {

			String weaponName = GunsFile.getData().getString("Weapons.TACTICAL.default.name");
			UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.TACTICAL.default.unlockType"));
			ItemStack weapon = GunsFile.getData().getItemStack("Weapons.TACTICAL.default.item");
			short data = (short) GunsFile.getData().getInt("Weapons.TACTICAL.default.data");

			defaultTactical = new CodWeapon(weaponName, WeaponType.TACTICAL, type, weapon, 0);
		}

		return defaultTactical;
	}

	public void prestigePlayer(Player p) {
		ArrayList<Loadout> loadouts = new ArrayList<>();
		playerLoadouts.put(p, loadouts);
		save(p);
		load(p);
	}

	public boolean load(Player p) {

		ArrayList<Loadout> l = new ArrayList<>();

		int k = 0;
		while (LoadoutsFile.getData().contains("Loadouts." + p.getName() + "." + k)) {

			Loadout loadout;

			String name = LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Name");
			CodGun primary = null;

			for (CodGun gun : ShopManager.getInstance().getPrimaryGuns()) {
				if (gun.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Primary"))) {
					primary = gun;
				}
			}

			CodGun secondary = null;

			for (CodGun gun : ShopManager.getInstance().getSecondaryGuns()) {
				if (gun.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Secondary"))) {
					secondary = gun;
				}
			}

			CodWeapon lethal = null;

			for (CodWeapon grenade : ShopManager.getInstance().getLethalWeapons()) {
				if (grenade.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Lethal"))) {
					lethal = grenade;
				}
			}

			CodWeapon tactical = null;

			for (CodWeapon grenade : ShopManager.getInstance().getTacticalWeapons()) {
				if (grenade.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Tactical"))) {
					tactical = grenade;
				}
			}

			CodPerk perkOne = null;
			CodPerk perkTwo = null;
			CodPerk perkThree = null;

			for (CodPerk perk : PerkManager.getInstance().getAvailablePerks()) {
				if (perk.getSlot() == PerkSlot.ONE) {
					if (perk.getPerk().getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Perk1"))) {
						perkOne = perk;
					}
				} else if (perk.getSlot() == PerkSlot.TWO) {
					if (perk.getPerk().getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Perk2"))) {
						perkTwo = perk;
					}
				} else if (perk.getSlot() == PerkSlot.THREE) {
					if (perk.getPerk().getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Perk3"))) {
						perkThree = perk;
					}
				}
			}


			if (primary == null) {
				primary = LoadoutManager.getInstance().getDefaultPrimary();
			}

			if (secondary == null) {
				secondary = LoadoutManager.getInstance().getDefaultSecondary();
			}

			if (lethal == null) {
				lethal = LoadoutManager.getInstance().getDefaultLethal();
			}

			if (tactical == null) {
				tactical = LoadoutManager.getInstance().getDefaultTactical();
			}

			if (perkOne == null) {
				perkOne = PerkManager.getInstance().getDefaultPerk(PerkSlot.ONE);
			}

			if (perkTwo == null) {
				perkTwo = PerkManager.getInstance().getDefaultPerk(PerkSlot.TWO);
			}

			if (perkThree == null) {
				perkThree = PerkManager.getInstance().getDefaultPerk(PerkSlot.THREE);
			}

			try {

				loadout = new Loadout(p, name, primary, secondary, lethal, tactical, perkOne, perkTwo, perkThree);
				l.add(loadout);
			} catch (Exception e) {
				Main.sendMessage(Main.getConsole(),  Main.getPrefix() + Lang.ERROR_READING_PLAYER_LOADOUT.getMessage(), Main.getLang());
			}

			k++;
		}

		if (k < getAllowedClasses(p)) {
			for (int i = k; i < getAllowedClasses(p); i++) {
				Loadout loadout = getDefaultLoadout(p, i);
				l.add(loadout);
			}
		}

		if (l.isEmpty()) {
			for (int i = 0; i < getAllowedClasses(p); i++) {
				Loadout loadout = getDefaultLoadout(p, i);
				l.add(loadout);
			}
		}

		playerLoadouts.put(p, l);
		return true;
	}

	private Loadout getDefaultLoadout(Player p, int i) {
		Loadout loadout = new Loadout(p, Lang.CLASS_PREFIX.getMessage() + " " + (i + 1), this.getDefaultPrimary(),
				this.getDefaultSecondary(), this.getDefaultLethal(), this.getDefaultTactical(),
				PerkManager.getInstance().getDefaultPerk(PerkSlot.ONE), PerkManager.getInstance().getDefaultPerk(PerkSlot.TWO),
				PerkManager.getInstance().getDefaultPerk(PerkSlot.THREE));

		return loadout;
	}

	public void save(Player p) {

		if (getLoadouts(p) == null) {
			return;
		}

		int i = 0;
		for (Loadout l : getLoadouts(p)) {
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Name", l.getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Primary", l.getPrimary().getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Secondary", l.getSecondary().getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Lethal", l.getLethal().getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Tactical", l.getTactical().getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Perk1", l.getPerk1().getPerk().getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Perk2", l.getPerk2().getPerk().getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Perk3", l.getPerk3().getPerk().getName());
			LoadoutsFile.saveData();
			LoadoutsFile.reloadData();
			i++;
		}
	}

	public Loadout getCurrentLoadout(Player p) {

		List<Loadout> loadouts = getLoadouts(p);

		return loadouts.get(0);
	}

	public List<Loadout> getLoadouts(Player p) {
		if (!playerLoadouts.containsKey(p)) {
			this.load(p);
		}

		return playerLoadouts.get(p);
	}

	public Loadout getActiveLoadout(Player p) {
		this.activeLoadouts.computeIfAbsent(p, k -> this.getLoadouts(p).get(0));

		return this.activeLoadouts.get(p);
	}

	public Map<Player, Loadout> getActiveLoadouts() {
		return activeLoadouts;
	}

	public void setActiveLoadout(Player p, Loadout loadout) {
		this.activeLoadouts.put(p, loadout);
	}

	public CodGun getRandomPrimary() {
		int size = ShopManager.getInstance().getPrimaryGuns().size() - 1;
		int position;

		if (ShopManager.getInstance().getPrimaryGuns().size() > 0) {
			position = (int) Math.round(Math.random() * size);
		} else {
			return blankPrimary;
		}

		return ShopManager.getInstance().getPrimaryGuns().get(position);
	}

	public CodGun getRandomSecondary() {
		int size = ShopManager.getInstance().getSecondaryGuns().size() - 1;
		int position;

		if (ShopManager.getInstance().getSecondaryGuns().size() > 0) {
			position = (int) Math.round(Math.random() * size);
		} else {
			return blankSecondary;
		}

		return ShopManager.getInstance().getSecondaryGuns().get(position);
	}

	public CodWeapon getRandomLethal() {
		int size = ShopManager.getInstance().getLethalWeapons().size() - 1;
		int position;

		if (ShopManager.getInstance().getLethalWeapons().size() > 0) {
			position = (int) Math.round(Math.random() * size);
		} else {
			return blankLethal;
		}

		return ShopManager.getInstance().getLethalWeapons().get(position);
	}

	public CodWeapon getRandomTactical() {
		int size = ShopManager.getInstance().getTacticalWeapons().size() - 1;
		int position;

		if (ShopManager.getInstance().getTacticalWeapons().size() > 0) {
			position = (int) Math.round(Math.random() * size);
		} else {
			return blankTactical;
		}

		return ShopManager.getInstance().getTacticalWeapons().get(position);
	}
}
