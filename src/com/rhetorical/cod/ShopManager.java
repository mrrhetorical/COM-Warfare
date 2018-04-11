package com.rhetorical.cod;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.files.ShopFile;
import com.rhetorical.cod.object.CodGun;
import com.rhetorical.cod.object.CodPerk;
import com.rhetorical.cod.object.CodWeapon;
import com.rhetorical.cod.object.GunType;
import com.rhetorical.cod.object.UnlockType;
import com.rhetorical.cod.object.WeaponType;

public class ShopManager {

	private ArrayList<CodGun> primaryGuns = new ArrayList<CodGun>();
	private ArrayList<CodGun> secondaryGuns = new ArrayList<CodGun>();
	private ArrayList<CodWeapon> lethalWeapons = new ArrayList<CodWeapon>();
	private ArrayList<CodWeapon> tacticalWeapons = new ArrayList<CodWeapon>();

	public HashMap<Player, ArrayList<CodGun>> purchasedGuns = new HashMap<Player, ArrayList<CodGun>>();
	public HashMap<Player, ArrayList<CodWeapon>> purchasedWeapons = new HashMap<Player, ArrayList<CodWeapon>>();
	public HashMap<Player, ArrayList<CodPerk>> purchasedPerks = new HashMap<Player, ArrayList<CodPerk>>();

	public HashMap<Player, Inventory> gunShop = new HashMap<Player, Inventory>();
	public HashMap<Player, Inventory> weaponShop = new HashMap<Player, Inventory>();
	public HashMap<Player, Inventory> perkShop = new HashMap<Player, Inventory>();

	public ShopManager() {
		this.loadGuns();
		this.loadWeapons();
		for (Player p : Bukkit.getOnlinePlayers()) {
			this.loadPurchaseData(p);
		}
	}

	public void loadGuns() {

		if (Main.loadManager.getDefaultPrimary() == null) {
			return;
		}

		primaryGuns.add(Main.loadManager.getDefaultPrimary());
		for (int i = 0; GunsFile.getData().contains("Guns.Primary." + i); i++) {
			String name = GunsFile.getData().getString("Guns.Primary." + i + ".name");
			int ammoCount = GunsFile.getData().getInt("Guns.Primary." + i + ".ammoCount");
			ItemStack ammoItem = GunsFile.getData().getItemStack("Guns.Primary." + i + ".ammoItem");
			ItemStack gunItem = GunsFile.getData().getItemStack("Guns.Primary." + i + ".gunItem");
			UnlockType unlockType = UnlockType
					.valueOf(GunsFile.getData().getString("Guns.Primary." + i + ".unlockType"));
			int levelUnlock = GunsFile.getData().getInt("Guns.Primary." + i + ".levelUnlock");
			int creditsUnlock = GunsFile.getData().getInt("Guns.Primary." + i + ".creditUnlock");

			CodGun gun = new CodGun(name, GunType.Primary, unlockType, ammoCount, ammoItem, gunItem, levelUnlock);
			gun.setCreditUnlock(creditsUnlock);
			primaryGuns.add(gun);
		}

		secondaryGuns.add(Main.loadManager.getDefaultSecondary());
		for (int i = 0; GunsFile.getData().contains("Guns.Secondary." + i); i++) {

			if (Main.loadManager.getDefaultPrimary() == null) {
				return;
			}
			String name = GunsFile.getData().getString("Guns.Secondary." + i + ".name");
			int ammoCount = GunsFile.getData().getInt("Guns.Secondary." + i + ".ammoCount");
			ItemStack ammoItem = GunsFile.getData().getItemStack("Guns.Secondary." + i + ".ammoItem");
			ItemStack gunItem = GunsFile.getData().getItemStack("Guns.Secondary." + i + ".gunItem");
			UnlockType unlockType = UnlockType
					.valueOf(GunsFile.getData().getString("Guns.Secondary." + i + ".unlockType"));
			int levelUnlock = GunsFile.getData().getInt("Guns.Secondary." + i + ".levelUnlock");
			int creditsUnlock = GunsFile.getData().getInt("Guns.Secondary." + i + ".creditUnlock");

			CodGun gun = new CodGun(name, GunType.Secondary, unlockType, ammoCount, ammoItem, gunItem, levelUnlock);
			gun.setCreditUnlock(creditsUnlock);
			secondaryGuns.add(gun);
		}
	}

	public void loadWeapons() {
		lethalWeapons.add(Main.loadManager.getDefaultLethal());
		tacticalWeapons.add(Main.loadManager.getDefaultTactical());

		for (int i = 0; GunsFile.getData().contains("Weapons.LETHAL." + i + ".name"); i++) {
			String weaponName = GunsFile.getData().getString("Weapons.LETHAL." + i + ".name");
			UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.LETHAL." + i + ".unlockType"));
			ItemStack weapon = GunsFile.getData().getItemStack("Weapons.LETHAL." + i + ".item");
			int levelUnlock = GunsFile.getData().getInt("Weapons.LETHAL." + i + ".levelUnlock");
			int creditUnlock = GunsFile.getData().getInt("Weapons.LETHAL." + i + ".creditUnlock");

			CodWeapon grenade = new CodWeapon(weaponName, WeaponType.LETHAL, type, weapon, levelUnlock);
			grenade.setCreditUnlock(creditUnlock);
			lethalWeapons.add(grenade);
		}

		for (int i = 0; GunsFile.getData().contains("Weapons.TACTICAL." + i + ".name"); i++) {
			String weaponName = GunsFile.getData().getString("Weapons.TACTICAL." + i + ".name");
			UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.TACTICAL." + i + ".unlockType"));
			ItemStack weapon = GunsFile.getData().getItemStack("Weapons.TACTICAL." + i + ".item");
			int levelUnlock = GunsFile.getData().getInt("Weapons.TACTICAL." + i + ".levelUnlock");
			int creditUnlock = GunsFile.getData().getInt("Weapons.TACTICAL." + i + ".creditUnlock");

			CodWeapon grenade = new CodWeapon(weaponName, WeaponType.TACTICAL, type, weapon, levelUnlock);
			grenade.setCreditUnlock(creditUnlock);
			lethalWeapons.add(grenade);
		}
	}

	public void savePurchaseData(Player p) {

		ArrayList<String> guns = new ArrayList<String>();
		for (CodGun gun : this.purchasedGuns.get(p)) {
			guns.add(gun.getName());
		}

		ShopFile.getData().set("Purchased.Guns." + p.getName(), guns);

		ArrayList<String> weapons = new ArrayList<String>();
		for (CodWeapon grenade : this.purchasedWeapons.get(p)) {
			weapons.add(grenade.getName());
		}

		ShopFile.getData().set("Purchased.Weapons." + p.getName(), weapons);

		ArrayList<String> perks = new ArrayList<String>();
		for (CodPerk perk : this.purchasedPerks.get(p)) {
			perks.add(perk.getPerk().getName());
		}
		
		if (perks.size() == 0) {
			perks.add(Main.perkManager.getDefaultOne().getPerk().getName());
			perks.add(Main.perkManager.getDefaultTwo().getPerk().getName());
			perks.add(Main.perkManager.getDefaultThree().getPerk().getName());
		}
		
		ShopFile.getData().set("Purchased.Perks." + p.getName(), perks);

		ShopFile.saveData();
		ShopFile.reloadData();
	}

	@SuppressWarnings("unchecked")
	public void loadPurchaseData(Player p) {

		ArrayList<CodGun> guns = new ArrayList<CodGun>();
		ArrayList<CodWeapon> grenades = new ArrayList<CodWeapon>();
		ArrayList<CodPerk> perks = new ArrayList<CodPerk>();

		ArrayList<String> gunList = (ArrayList<String>) ShopFile.getData().get("Purchased.Guns." + p.getName());

		if (gunList != null) {

			outsideGuns: for (String s : gunList) {
				for (CodGun gun : this.getPrimaryGuns()) {
					if (gun.getName().equals(s)) {
						guns.add(gun);
						continue outsideGuns;
					}
				}

				for (CodGun gun : this.getSecondaryGuns()) {
					if (gun.getName().equals(s)) {
						guns.add(gun);
						continue outsideGuns;
					}
				}
				guns.add(Main.loadManager.getDefaultPrimary());
				guns.add(Main.loadManager.getDefaultSecondary());
			}
		} else {
			guns.add(Main.loadManager.getDefaultPrimary());
			guns.add(Main.loadManager.getDefaultSecondary());
		}

		ArrayList<String> weaponList = (ArrayList<String>) ShopFile.getData().get("Purchased.Weapons." + p.getName());

		if (weaponList != null) {
			outsideWeapons: for (String s : weaponList) {
				for (CodWeapon weapon : this.getLethalWeapons()) {
					if (weapon.getName().equals(s)) {
						grenades.add(weapon);
						continue outsideWeapons;
					}
				}

				for (CodWeapon weapon : this.getTacticalWeapons()) {
					if (weapon.getName().equals(s)) {
						grenades.add(weapon);
						continue outsideWeapons;
					}
				}
				grenades.add(Main.loadManager.getDefaultLethal());
				grenades.add(Main.loadManager.getDefaultTactical());
			}
		} else {
			grenades.add(Main.loadManager.getDefaultLethal());
			grenades.add(Main.loadManager.getDefaultTactical());
		}

		ArrayList<String> perkList = (ArrayList<String>) ShopFile.getData().get("Purchased.Perks." + p.getName());
		if (perkList != null) {
			outsidePerks: for (String s : perkList) {
				for (CodPerk perk : Main.perkManager.getAvailablePerks()) {
					if (perk.getPerk().getName().equals(s)) {
						perks.add(perk);
						continue outsidePerks;
					}
				}
				perks.add(Main.perkManager.getDefaultOne());
				perks.add(Main.perkManager.getDefaultTwo());
				perks.add(Main.perkManager.getDefaultThree());
			}
		} else {
			perks.add(Main.perkManager.getDefaultOne());
			perks.add(Main.perkManager.getDefaultTwo());
			perks.add(Main.perkManager.getDefaultThree());
		}


		this.purchasedGuns.put(p, guns);
		this.purchasedWeapons.put(p, grenades);
		this.purchasedPerks.put(p, perks);

		this.savePurchaseData(p);
	}

	public ArrayList<CodGun> getPrimaryGuns() {
		return primaryGuns;
	}

	public void setPrimaryGuns(ArrayList<CodGun> primaryGuns) {
		this.primaryGuns = primaryGuns;
	}

	public ArrayList<CodGun> getSecondaryGuns() {
		return secondaryGuns;
	}

	public void setSecondaryGuns(ArrayList<CodGun> secondaryGuns) {
		this.secondaryGuns = secondaryGuns;
	}

	public ArrayList<CodWeapon> getLethalWeapons() {
		return lethalWeapons;
	}

	public void setLethalWeapons(ArrayList<CodWeapon> lethalWeapons) {
		this.lethalWeapons = lethalWeapons;
	}

	public ArrayList<CodWeapon> getTacticalWeapons() {
		return tacticalWeapons;
	}

	public void setTacticalWeapons(ArrayList<CodWeapon> tacticalWeapons) {
		this.tacticalWeapons = tacticalWeapons;
	}

	public HashMap<Player, ArrayList<CodGun>> getPurchasedGuns() {
		return purchasedGuns;
	}

	public void setPurchasedGuns(HashMap<Player, ArrayList<CodGun>> purchasedGuns) {
		this.purchasedGuns = purchasedGuns;
	}

	public HashMap<Player, ArrayList<CodWeapon>> getPurchasedWeapons() {
		return purchasedWeapons;
	}

	public void setPurchasedWeapons(HashMap<Player, ArrayList<CodWeapon>> purchasedWeapons) {
		this.purchasedWeapons = purchasedWeapons;
	}

	public boolean hasGun(Player p, CodGun gun) {
		if (purchasedGuns.get(p).contains(gun)) {
			return true;
		}

		return false;
	}

	public boolean hasWeapon(Player p, CodWeapon grenade) {
		if (purchasedWeapons.get(p).contains(grenade)) {
			return true;
		}

		return false;
	}

	public boolean isAvailableForPurchase(Player p, CodGun gun) {

		if (gun.getType() == UnlockType.LEVEL || gun.getType() == UnlockType.BOTH) {

			if (Main.progManager.getLevel(p) >= gun.getLevelUnlock()) {
				return true;
			}

		} else if (gun.getType() == UnlockType.CREDITS) {
			return true;
		}

		return false;
	}

	public void checkForNewGuns(Player p) {
		for (CodGun gun : Main.shopManager.primaryGuns) {
			if (gun.getType() == UnlockType.LEVEL) {

				Main.shopManager.loadPurchaseData(p);

				HashMap<Player, ArrayList<CodGun>> purchased = Main.shopManager.getPurchasedGuns();

				if (!purchased.get(p).contains(gun)) {

					if (Main.progManager.getLevel(p) == gun.getLevelUnlock()) {

						ArrayList<CodGun> guns = purchased.get(p);

						guns.add(gun);

						purchased.put(p, guns);

						Main.shopManager.setPurchasedGuns(purchased);

						Main.sendMessage(p, Main.codPrefix + "§aYou just unlocked the §6" + gun.getName() + "§a!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + "§aEquip it after the match!", Main.lang);
					}
				}
			} else if (gun.getType() == UnlockType.BOTH) {
				if (Main.progManager.getLevel(p) == gun.getLevelUnlock()) {
					Main.sendMessage(p, Main.codPrefix + "§aThe §6" + gun.getName() + "§a is now available for purchase!", Main.lang);
				}
			}

			this.savePurchaseData(p);
		}

		for (CodGun gun : Main.shopManager.secondaryGuns) {
			if (gun.getType() == UnlockType.LEVEL) {

				HashMap<Player, ArrayList<CodGun>> purchased = Main.shopManager.getPurchasedGuns();

				if (!purchased.get(p).contains(gun)) {

					if (Main.progManager.getLevel(p) == gun.getLevelUnlock()) {
						ArrayList<CodGun> guns = purchased.get(p);

						guns.add(gun);

						purchased.put(p, guns);

						Main.shopManager.setPurchasedGuns(purchased);

						Main.sendMessage(p, Main.codPrefix + "§aYou just unlocked the §6" + gun.getName() + "§a!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + "§aEquip it after the match!", Main.lang);
					}
				}
			} else if (gun.getType() == UnlockType.BOTH) {
				if (Main.progManager.getLevel(p) == gun.getLevelUnlock()) {
					Main.sendMessage(p, Main.codPrefix + "§aThe §6" + gun.getName() + "§a is now available for purchase!", Main.lang);
				}
			}
		}

		for (CodWeapon grenade : Main.shopManager.lethalWeapons) {
			if (grenade.getType() == UnlockType.LEVEL) {

				HashMap<Player, ArrayList<CodWeapon>> purchased = Main.shopManager.getPurchasedWeapons();

				if (!purchased.get(p).contains(grenade)) {

					if (Main.progManager.getLevel(p) == grenade.getLevelUnlock()) {

						ArrayList<CodWeapon> grenades = purchased.get(p);

						grenades.add(grenade);

						purchased.put(p, grenades);

						Main.shopManager.setPurchasedWeapons(purchased);

						Main.sendMessage(p, Main.codPrefix + "§aYou just unlocked the §6" + grenade.getName() + "§a!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + "§aEquip it after the match!", Main.lang);
					}
				}
			} else if (grenade.getType() == UnlockType.BOTH) {
				if (Main.progManager.getLevel(p) == grenade.getLevelUnlock()) {
					Main.sendMessage(p, 
							Main.codPrefix + "§aThe §6" + grenade.getName() + "§a is now available for purchase!", Main.lang);
				}
			}
		}

		for (CodWeapon grenade : Main.shopManager.tacticalWeapons) {
			if (grenade.getType() == UnlockType.LEVEL) {

				HashMap<Player, ArrayList<CodWeapon>> purchased = Main.shopManager.getPurchasedWeapons();

				if (!purchased.get(p).contains(grenade)) {

					if (Main.progManager.getLevel(p) == grenade.getLevelUnlock()) {

						ArrayList<CodWeapon> grenades = purchased.get(p);

						grenades.add(grenade);

						purchased.put(p, grenades);

						Main.shopManager.setPurchasedWeapons(purchased);

						Main.sendMessage(p, Main.codPrefix + "§aYou just unlocked the §6" + grenade.getName() + "§a!", Main.lang);
						Main.sendMessage(p, Main.codPrefix + "§aEquip it after the match!", Main.lang);
					}
				}
			} else if (grenade.getType() == UnlockType.BOTH) {
				if (Main.progManager.getLevel(p) == grenade.getLevelUnlock()) {
					Main.sendMessage(p, 
							Main.codPrefix + "§aThe §6" + grenade.getName() + "§a is now available for purchase!", Main.lang);
				}
			}
		}

	}

	public ArrayList<CodPerk> getPerks(Player p) {
		return purchasedPerks.get(p);
	}

	public void setPerks(Player p, ArrayList<CodPerk> perks) {
		this.purchasedPerks.put(p, perks);
	}

}
