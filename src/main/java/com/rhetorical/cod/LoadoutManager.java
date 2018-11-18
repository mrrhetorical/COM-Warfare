package com.rhetorical.cod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.files.LoadoutsFile;
import com.rhetorical.cod.object.CodGun;
import com.rhetorical.cod.object.CodPerk;
import com.rhetorical.cod.object.CodWeapon;
import com.rhetorical.cod.object.GunType;
import com.rhetorical.cod.object.Loadout;
import com.rhetorical.cod.object.PerkSlot;
import com.rhetorical.cod.object.UnlockType;
import com.rhetorical.cod.object.WeaponType;

public class LoadoutManager {

	private HashMap<Player, Integer> allowedClasses = new HashMap<Player, Integer>();
	private HashMap<Player, ArrayList<Loadout>> playerLoadouts = new HashMap<Player, ArrayList<Loadout>>();
	private HashMap<Player, Loadout> activeLoadouts = new HashMap<Player, Loadout>();

	public ItemStack knife;

	LoadoutManager(HashMap<Player, ArrayList<Loadout>> pL) {
		this.playerLoadouts = pL;
		for (Player p : Bukkit.getOnlinePlayers()) {
			allowedClasses.put(p, getAllowedClasses(p));
		}

		knife = new ItemStack(Material.IRON_SWORD);
		ItemMeta knifeMeta = knife.getItemMeta();
		knifeMeta.setDisplayName("\u00A7eKnife");
		ArrayList<String> knifeLore = new ArrayList<String>();
		knifeLore.add("\u00A76A knife that can kill players in one hit.");
		knifeMeta.setLore(knifeLore);
		knife.setItemMeta(knifeMeta);
	}

	// Method should return true if loadout creation is allowed and is
	// successful, returns false if the creation of the loadout is
	// either unsuccessful or is not allowed
	public boolean create(Player p, String loadoutName, CodGun primaryGun, CodGun secondaryGun, CodWeapon LethalWeapon,
			CodWeapon tacticalWeapon, CodPerk perkOne, CodPerk perkTwo, CodPerk perkThree) {

		Loadout loadout = new Loadout(p, loadoutName, primaryGun, secondaryGun, LethalWeapon, tacticalWeapon, perkOne,
				perkTwo, perkThree);

		ArrayList<Loadout> currentLoadouts = this.getLoadouts(p);

		if (getAllowedClasses(p) < currentLoadouts.size()) {
			currentLoadouts.add(loadout);
			this.playerLoadouts.put(p, currentLoadouts);
		}

		while (getAllowedClasses(p) < currentLoadouts.size()) {
			Loadout defaultLoadout = new Loadout(p, "Class " + Integer.toString(currentLoadouts.size() + 1),
					getDefaultPrimary(), getDefaultSecondary(), getDefaultLethal(), getDefaultTactical(),
					Main.perkManager.getDefaultPerk(PerkSlot.ONE), Main.perkManager.getDefaultPerk(PerkSlot.TWO),
					Main.perkManager.getDefaultPerk(PerkSlot.THREE));
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
		p.getInventory().clear();

		CodGun primary = loadout.getPrimary();
		CodGun secondary = loadout.getSecondary();
		CodWeapon LETHAL = loadout.getLethal();
		CodWeapon tactical = loadout.getTactical();
		// CodPerk perkOne = loadout.getPerk1();
		// CodPerk perkTwo = loadout.getPerk2();
		// CodPerk perkThree = loadout.getPerk3();

		// Knife
		p.getInventory().setItem(0, this.knife);

		// Guns
		p.getInventory().setItem(1, primary.getGun());
		p.getInventory().setItem(2, secondary.getGun());

		// Ammo
		ItemStack primaryAmmo = primary.getAmmo();
		ItemStack secondaryAmmo = secondary.getAmmo();

		primaryAmmo.setAmount(primary.getAmmoCount());
		secondaryAmmo.setAmount(secondary.getAmmoCount());

		p.getInventory().setItem(19, primaryAmmo);
		p.getInventory().setItem(25, secondaryAmmo);

		// Grenades
		p.getInventory().setItem(3, LETHAL.getWeapon());
		p.getInventory().setItem(4, tactical.getWeapon());

		/*
		 * TODO: - Set inventory of player - Slot 0: Knife - Slot 1: Primary -
		 * Slot 2: Secondary - Slot 3: LETHAL - Slot 4: Tactical - Slot 5: -
		 * Slot 6: - Slot 7: - Slot 8: Current Scorestreak
		 * 
		 */

	}

	public int getAllowedClasses(Player p) {

		int classes = 5;

		for (int i = 0; i < Main.progManager.getPrestigeLevel(p); i++) {
			switch (i) {
			case 1:
				classes++;
				break;
			case 3:
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

//			Main.sendMessage(Main.cs,  Main.codPrefix + "\u00A7cCan't start COM-Warfare without a default PRIMARY weapon!", Main.lang);
			return Main.shopManager.blankPrimary;
		}

		String gunName = GunsFile.getData().getString("Guns.Primary.default.name");
		int ammoAmount = GunsFile.getData().getInt("Guns.Primary.default.ammoCount");
		ItemStack ammoItem = (ItemStack) GunsFile.getData().get("Guns.Primary.default.ammoItem");
		ItemStack gunItem = (ItemStack) GunsFile.getData().get("Guns.Primary.default.gunItem");

		CodGun gun = new CodGun(gunName, GunType.Primary, null, ammoAmount, ammoItem, gunItem, 0);

		gun.setCreditUnlock(0);

		return gun;
	}

	public CodGun getDefaultSecondary() {

		if (!GunsFile.getData().contains("Guns.Secondary.default")) {

//			Main.sendMessage(Main.cs,  Main.codPrefix + "\u00A7cCan't start COM-Warfare without a default SECONDARY weapon!", Main.lang);

			return Main.shopManager.blankSecondary;
		}

		String gunName = GunsFile.getData().getString("Guns.Secondary.default.name");
		int ammoAmount = GunsFile.getData().getInt("Guns.Secondary.default.ammoCount");
		ItemStack ammoItem = GunsFile.getData().getItemStack("Guns.Secondary.default.ammoItem");
		ItemStack gunItem = GunsFile.getData().getItemStack("Guns.Secondary.default.gunItem");

		CodGun gun = new CodGun(gunName, GunType.Secondary, null, ammoAmount, ammoItem, gunItem, 0);

		gun.setCreditUnlock(0);

		return gun;
	}

	public CodWeapon getDefaultLethal() {

		if (!GunsFile.getData().contains("Weapons.LETHAL.default")) {

//			Main.sendMessage(Main.cs,  Main.codPrefix + "\u00A7cCan't start COM-Warfare without a default LETHAL weapon!", Main.lang);
			return Main.shopManager.blankLethal;
		}

		String weaponName = GunsFile.getData().getString("Weapons.LETHAL.default.name");
		UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.LETHAL.default.unlockType"));
		ItemStack weapon = GunsFile.getData().getItemStack("Weapons.LETHAL.default.item");

		return new CodWeapon(weaponName, WeaponType.LETHAL, type, weapon, 0);
	}

	public CodWeapon getDefaultTactical() {

		if (!GunsFile.getData().contains("Weapons.TACTICAL.default")) {

//			Main.sendMessage(Main.cs,  Main.codPrefix + "\u00A7cCan't start COM-Warfare without a default TACTICAL weapon!", Main.lang);
			return Main.shopManager.blankTactical;
		}

		String weaponName = GunsFile.getData().getString("Weapons.TACTICAL.default.name");
		UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.TACTICAL.default.unlockType"));
		ItemStack weapon = GunsFile.getData().getItemStack("Weapons.TACTICAL.default.item");

		return new CodWeapon(weaponName, WeaponType.TACTICAL, type, weapon, 0);
	}

	public boolean load(Player p) {

		ArrayList<Loadout> l = new ArrayList<>();

		int k = 0;
		while (LoadoutsFile.getData().contains("Loadouts." + p.getName() + "." + k)) {

			Loadout loadout;

			String name = LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Name");
			CodGun primary = null;

			for (CodGun gun : Main.shopManager.getPrimaryGuns()) {
				if (gun.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Primary"))) {
					if (gun.getName().equals(
							LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Primary"))) {
						primary = gun;
					}
				}
			}

			CodGun secondary = null;

			for (CodGun gun : Main.shopManager.getSecondaryGuns()) {
				if (gun.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Secondary"))) {
					if (gun.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Secondary"))) {
						secondary = gun;
					}
				}
			}

			CodWeapon lethal = null;

			for (CodWeapon grenade : Main.shopManager.getLethalWeapons()) {
				if (grenade.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Lethal"))) {
					if (grenade.getName().equals(
							LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".Lethal"))) {
						lethal = grenade;
					}
				}
			}

			CodWeapon tactical = null;

			for (CodWeapon grenade : Main.shopManager.getTacticalWeapons()) {
				if (grenade.getName().equals(LoadoutsFile.getData().getString("Loadouts." + p.getName() + "." + k + ".tactical"))) {
					tactical = grenade;
				}
			}

			CodPerk perkOne = null;
			CodPerk perkTwo = null;
			CodPerk perkThree = null;

			if (primary == null) {
				primary = Main.loadManager.getDefaultPrimary();
			}

			if (secondary == null) {
				secondary = Main.loadManager.getDefaultSecondary();
			}

			if (lethal == null) {
				lethal = Main.loadManager.getDefaultLethal();
			}

			if (tactical == null) {
				tactical = Main.loadManager.getDefaultTactical();
			}

			if (perkOne == null) {
				perkOne = Main.perkManager.getDefaultPerk(PerkSlot.ONE);
			}

			if (perkTwo == null) {
				perkTwo = Main.perkManager.getDefaultPerk(PerkSlot.TWO);
			}

			if (perkThree == null) {
				perkThree = Main.perkManager.getDefaultPerk(PerkSlot.THREE);
			}

			try {

				loadout = new Loadout(p, name, primary, secondary, lethal, tactical, perkOne, perkTwo, perkThree);
				l.add(loadout);
			} catch (Exception e) {
				Main.sendMessage(Main.cs,  Main.codPrefix + "\u00A7cError loading player loadout from the config.", Main.lang);
			}

			k++;
		}

		if (l.isEmpty()) {
			for (int i = 0; i < 5; i++) {
				Loadout loadout = new Loadout(p, "Class " + Integer.toString(i + 1), this.getDefaultPrimary(),
						this.getDefaultSecondary(), this.getDefaultLethal(), this.getDefaultTactical(),
						Main.perkManager.getDefaultPerk(PerkSlot.ONE), Main.perkManager.getDefaultPerk(PerkSlot.TWO),
						Main.perkManager.getDefaultPerk(PerkSlot.THREE));
				l.add(loadout);
			}
		}

		playerLoadouts.put(p, l);
		return true;
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
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Perk1",
					l.getPerk1().getPerk().getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Perk2",
					l.getPerk2().getPerk().getName());
			LoadoutsFile.getData().set("Loadouts." + p.getName() + "." + i + ".Perk3",
					l.getPerk3().getPerk().getName());
			LoadoutsFile.saveData();
			LoadoutsFile.reloadData();
			i++;
		}
	}

	public Loadout getCurrentLoadout(Player p) {

		ArrayList<Loadout> loadouts = getLoadouts(p);
		if (!loadouts.equals(null)) {
			return loadouts.get(0);
		}

		return null;
	}

	public ArrayList<Loadout> getLoadouts(Player p) {
		if (!playerLoadouts.containsKey(p)) {
			this.load(p);
		}

		return playerLoadouts.get(p);
	}

	public Loadout getActiveLoadout(Player p) {
		this.activeLoadouts.computeIfAbsent(p, k -> this.getLoadouts(p).get(0));

		return this.activeLoadouts.get(p);
	}

	public void setActiveLoadout(Player p, Loadout loadout) {
		this.activeLoadouts.put(p, loadout);
	}

	public CodGun getRandomPrimary() {
		int size = Main.shopManager.getPrimaryGuns().size();
		int position;

		if (size != 0) {
			position = (int) Math.round(Math.random() * size);
		} else {
			return Main.shopManager.blankPrimary;
		}

		return Main.shopManager.getPrimaryGuns().get(position);
	}

	public CodGun getRandomSecondary() {
		int size = Main.shopManager.getSecondaryGuns().size();
		int position;

		if (size != 0) {
			position = (int) Math.round(Math.random() * size);
		} else {
			return Main.shopManager.blankSecondary;
		}

		return Main.shopManager.getSecondaryGuns().get(position);
	}

	public CodWeapon getRandomLethal() {
		int size = Main.shopManager.getLethalWeapons().size();
		int position;

		if (size != 0) {
			position = (int) Math.round(Math.random() * size);
		} else {
			return Main.shopManager.blankLethal;
		}

		return Main.shopManager.getLethalWeapons().get(position);
	}

	public CodWeapon getRandomTactical() {
		int size = Main.shopManager.getTacticalWeapons().size();
		int position;

		if (size != 0) {
			position = (int) Math.round(Math.random() * size);
		} else {
			return Main.shopManager.blankTactical;
		}

		return Main.shopManager.getTacticalWeapons().get(position);
	}
}
