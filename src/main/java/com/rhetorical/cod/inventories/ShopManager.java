package com.rhetorical.cod.inventories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.files.ShopFile;
import com.rhetorical.cod.weapons.CodGun;
import com.rhetorical.cod.perks.CodPerk;
import com.rhetorical.cod.weapons.CodWeapon;
import com.rhetorical.cod.weapons.GunType;
import com.rhetorical.cod.weapons.UnlockType;
import com.rhetorical.cod.weapons.WeaponType;

public class ShopManager {

	private ArrayList<CodGun> primaryGuns = new ArrayList<>();
	private ArrayList<CodGun> secondaryGuns = new ArrayList<>();
	private ArrayList<CodWeapon> lethalWeapons = new ArrayList<>();
	private ArrayList<CodWeapon> tacticalWeapons = new ArrayList<>();

	public HashMap<Player, ArrayList<CodGun>> purchasedGuns = new HashMap<>();
	public HashMap<Player, ArrayList<CodWeapon>> purchasedWeapons = new HashMap<>();
	public HashMap<Player, ArrayList<CodPerk>> purchasedPerks = new HashMap<>();

	public HashMap<Player, Inventory> gunShop = new HashMap<>();
	public HashMap<Player, Inventory> weaponShop = new HashMap<>();
	public HashMap<Player, Inventory> perkShop = new HashMap<>();

	public ShopManager() {

		loadGuns();
		loadWeapons();
		for (Player p : Bukkit.getOnlinePlayers()) {
			this.loadPurchaseData(p);
		}
	}

	public void prestigePlayer(Player p) {
		ArrayList<CodGun> guns = new ArrayList<>();
		ArrayList<CodWeapon> grenades = new ArrayList<>();
		ArrayList<CodPerk> perks = new ArrayList<>();

		purchasedGuns.put(p, guns);
		purchasedWeapons.put(p, grenades);
		purchasedPerks.put(p, perks);

		savePurchaseData(p);
		loadPurchaseData(p);
	}

	private void loadGuns() {

		if (Main.loadManager.getDefaultPrimary() == null) {
			return;
		}

		primaryGuns.add(Main.loadManager.getDefaultPrimary());
		for (int i = 0; GunsFile.getData().contains("Guns.Primary." + i); i++) {
			String name = GunsFile.getData().getString("Guns.Primary." + i + ".name");

			int ammoCount = GunsFile.getData().getInt("Guns.Primary." + i + ".ammoCount");
			Material ammoMat = Material.valueOf(GunsFile.getData().getString("Guns.Primary." + i + ".ammoItem"));
			short ammoData = (short) GunsFile.getData().getInt("Guns.Primary." + i + ".ammoData");
			ItemStack ammoItem = new ItemStack (ammoMat, 1, ammoData);

			Material gunMat = Material.valueOf(GunsFile.getData().getString("Guns.Primary." + i + ".gunItem"));
			short gunData = (short) GunsFile.getData().getInt("Guns.Primary." + i + ".gunData");
			ItemStack gunItem = new ItemStack(gunMat, 1, gunData);

			UnlockType unlockType = UnlockType.valueOf(GunsFile.getData().getString("Guns.Primary." + i + ".unlockType"));
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
			Material ammoMat = Material.valueOf(GunsFile.getData().getString("Guns.Secondary." + i + ".ammoItem"));
			short ammoData = (short) GunsFile.getData().getInt("Guns.Secondary." + i + ".ammoData");
			ItemStack ammoItem = new ItemStack(ammoMat, 1, ammoData);

			Material gunMat = Material.valueOf(GunsFile.getData().getString("Guns.Secondary." + i + ".gunItem"));
			short gunData = (short) GunsFile.getData().getInt("Guns.Secondary." + i + ".gunData");
			ItemStack gunItem = new ItemStack(gunMat, 1, gunData);

			UnlockType unlockType = UnlockType.valueOf(GunsFile.getData().getString("Guns.Secondary." + i + ".unlockType"));
			int levelUnlock = GunsFile.getData().getInt("Guns.Secondary." + i + ".levelUnlock");
			int creditsUnlock = GunsFile.getData().getInt("Guns.Secondary." + i + ".creditUnlock");

			CodGun gun = new CodGun(name, GunType.Secondary, unlockType, ammoCount, ammoItem, gunItem, levelUnlock);
			gun.setCreditUnlock(creditsUnlock);
			secondaryGuns.add(gun);
		}
	}

	private void loadWeapons() {
		lethalWeapons.add(Main.loadManager.getDefaultLethal());
		tacticalWeapons.add(Main.loadManager.getDefaultTactical());

		for (int i = 0; GunsFile.getData().contains("Weapons.LETHAL." + i); i++) {
			String weaponName = GunsFile.getData().getString("Weapons.LETHAL." + i + ".name");
			UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.LETHAL." + i + ".unlockType"));
			ItemStack weapon = GunsFile.getData().getItemStack("Weapons.LETHAL." + i + ".item");
			int levelUnlock = GunsFile.getData().getInt("Weapons.LETHAL." + i + ".levelUnlock");
			int creditUnlock = GunsFile.getData().getInt("Weapons.LETHAL." + i + ".creditUnlock");

			CodWeapon grenade = new CodWeapon(weaponName, WeaponType.LETHAL, type, weapon, levelUnlock);
			grenade.setCreditUnlock(creditUnlock);
			lethalWeapons.add(grenade);
		}

		for (int i = 0; GunsFile.getData().contains("Weapons.TACTICAL." + i); i++) {
			String weaponName = GunsFile.getData().getString("Weapons.TACTICAL." + i + ".name");
			UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.TACTICAL." + i + ".unlockType"));
			ItemStack weapon = GunsFile.getData().getItemStack("Weapons.TACTICAL." + i + ".item");
			int levelUnlock = GunsFile.getData().getInt("Weapons.TACTICAL." + i + ".levelUnlock");
			int creditUnlock = GunsFile.getData().getInt("Weapons.TACTICAL." + i + ".creditUnlock");

			CodWeapon grenade = new CodWeapon(weaponName, WeaponType.TACTICAL, type, weapon, levelUnlock);
			grenade.setCreditUnlock(creditUnlock);
			tacticalWeapons.add(grenade);
		}
	}

	public void savePurchaseData(Player p) {

		ArrayList<String> guns = new ArrayList<>();
		if (this.purchasedGuns == null) {
			this.purchasedGuns = new HashMap<>();
		}
		this.purchasedGuns.computeIfAbsent(p, k -> new ArrayList<>());
		if (purchasedGuns.get(p) != null) {
			for (CodGun gun : this.purchasedGuns.get(p)) {
				if (!guns.contains(gun.getName())) {
					guns.add(gun.getName());
				}
			}
		}

		ShopFile.getData().set("Purchased.Guns." + p.getName(), guns);

		ArrayList<String> weapons = new ArrayList<>();
		if (this.purchasedWeapons == null) {
			this.purchasedWeapons = new HashMap<>();
		}

		this.purchasedWeapons.computeIfAbsent(p, k -> new ArrayList<>());

		if (purchasedWeapons.get(p) != null) {
			for (CodWeapon grenade : this.purchasedWeapons.get(p)) {
				if (!weapons.contains(grenade.getName())) {
					weapons.add(grenade.getName());
				}
			}
		}

		ShopFile.getData().set("Purchased.Weapons." + p.getName(), weapons);

		ArrayList<String> perks = new ArrayList<>();
		this.purchasedPerks.computeIfAbsent(p, k -> new ArrayList<>());

		if (purchasedPerks.get(p) != null) {
			for (CodPerk perk : this.purchasedPerks.get(p)) {
				if (!perks.contains(perk.getPerk().getName())) {
					perks.add(perk.getPerk().getName());
				}
			}
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

		ArrayList<CodGun> guns = new ArrayList<>();
		ArrayList<CodWeapon> grenades = new ArrayList<>();
		ArrayList<CodPerk> perks = new ArrayList<>();

		ArrayList<String> gunList = (ArrayList<String>) ShopFile.getData().get("Purchased.Guns." + p.getName());

		if (gunList != null) {

			for (String s : gunList) {
				boolean found = false;
				List<CodGun> allGuns = this.getPrimaryGuns();
				allGuns.addAll(this.getSecondaryGuns());
				for (CodGun gun : allGuns) {
					if (gun.getName().equals(s)) {
						if (!guns.contains(gun)) {
							guns.add(gun);
							found = true;
							break;
						}
						found = true;
						break;
					}
				}

				if (!found) {
					guns.add(Main.loadManager.getDefaultPrimary());
					guns.add(Main.loadManager.getDefaultSecondary());
				}
			}
		} else {
			guns.add(Main.loadManager.getDefaultPrimary());
			guns.add(Main.loadManager.getDefaultSecondary());
		}

		ArrayList<String> weaponList = (ArrayList<String>) ShopFile.getData().get("Purchased.Weapons." + p.getName());

		if (weaponList != null) {
			for (String s : weaponList) {
				boolean found = false;
				List<CodWeapon> allWeapons = this.getLethalWeapons();
				allWeapons.addAll(this.getTacticalWeapons());
				for (CodWeapon weapon : allWeapons) {
					if (weapon.getName().equals(s)) {
						if (!grenades.contains(weapon)) {
							grenades.add(weapon);
						}
						found = true;
						break;
					}

				}

				if (!found) {
					grenades.add(Main.loadManager.getDefaultLethal());
					grenades.add(Main.loadManager.getDefaultTactical());
				}
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

	private void setPurchasedGuns(HashMap<Player, ArrayList<CodGun>> purchasedGuns) {
		this.purchasedGuns = purchasedGuns;
	}

	public HashMap<Player, ArrayList<CodWeapon>> getPurchasedWeapons() {
		return purchasedWeapons;
	}

	private void setPurchasedWeapons(HashMap<Player, ArrayList<CodWeapon>> purchasedWeapons) {
		this.purchasedWeapons = purchasedWeapons;
	}

	public boolean hasGun(Player p, CodGun gun) {
		return purchasedGuns.get(p).contains(gun);

	}

	public boolean hasWeapon(Player p, CodWeapon grenade) {
		return purchasedWeapons.get(p).contains(grenade);

	}

	public boolean isAvailableForPurchase(Player p, CodGun gun) {

		if (gun.getType() == UnlockType.LEVEL || gun.getType() == UnlockType.BOTH) {

			if (Main.progressionManager.getLevel(p) >= gun.getLevelUnlock()) {
				return true;
			}

		} else if (gun.getType() == UnlockType.CREDITS) {
			return true;
		}

		return false;
	}

	private void unlockGun(HashMap<Player, ArrayList<CodGun>> purchased, Player p, CodGun gun) {
		ArrayList<CodGun> guns = purchased.get(p);

		guns.add(gun);

		purchased.put(p, guns);

		Main.shopManager.setPurchasedGuns(purchased);

		Main.sendMessage(p, Main.codPrefix + Lang.WEAPON_UNLOCKED.getMessage().replace("{gun-name}", gun.getName()), Main.lang);
	}

	private void unlockGrenade(Player p, CodWeapon grenade) {
		if (grenade.getType() == UnlockType.LEVEL) {

			HashMap<Player, ArrayList<CodWeapon>> purchased = Main.shopManager.getPurchasedWeapons();

			if (!purchased.get(p).contains(grenade)) {

				if (Main.progressionManager.getLevel(p) >= grenade.getLevelUnlock()) {

					ArrayList<CodWeapon> grenades = purchased.get(p);

					grenades.add(grenade);

					purchased.put(p, grenades);

					Main.shopManager.setPurchasedWeapons(purchased);

					Main.sendMessage(p, Main.codPrefix + Lang.WEAPON_UNLOCKED.getMessage().replace("{gun-name}", grenade.getName()), Main.lang);

				}
			}
		} else if (grenade.getType() == UnlockType.BOTH) {
			if (Main.progressionManager.getLevel(p) == grenade.getLevelUnlock()) {
				Main.sendMessage(p,
						Main.codPrefix + Lang.WEAPON_PURCHASE_UNLOCKED.getMessage().replace("{gun-name}", grenade.getName()), Main.lang);
			}
		}
	}

	public void checkForNewGuns(Player p) {

		Main.shopManager.loadPurchaseData(p);

		List<CodGun> primaryGuns = getPrimaryGuns();
		for (CodGun gun : primaryGuns) {
			if (gun.getType() == UnlockType.LEVEL) {

				HashMap<Player, ArrayList<CodGun>> purchased = Main.shopManager.getPurchasedGuns();

				if (!purchased.get(p).contains(gun)) {

					if (Main.progressionManager.getLevel(p) >= gun.getLevelUnlock()) {

						unlockGun(purchased, p, gun);
					}
				}
			} else if (gun.getType() == UnlockType.BOTH) {
				if (Main.progressionManager.getLevel(p) == gun.getLevelUnlock()) {
					Main.sendMessage(p, Main.codPrefix + Lang.WEAPON_PURCHASE_UNLOCKED.getMessage().replace("{gun-name}", gun.getName()), Main.lang);
				}
			}

		}

		List<CodGun> secondaryGuns = getSecondaryGuns();
		for (CodGun gun : secondaryGuns) {
			if (gun.getType() == UnlockType.LEVEL) {

				HashMap<Player, ArrayList<CodGun>> purchased = Main.shopManager.getPurchasedGuns();

				if (!purchased.get(p).contains(gun)) {

					if (Main.progressionManager.getLevel(p) >= gun.getLevelUnlock()) {
						unlockGun(purchased, p, gun);
					}
				}
			} else if (gun.getType() == UnlockType.BOTH) {
				if (Main.progressionManager.getLevel(p) >= gun.getLevelUnlock()) {
					Main.sendMessage(p, Main.codPrefix + Lang.WEAPON_PURCHASE_UNLOCKED.getMessage().replace("{gun-name}", gun.getName()), Main.lang);
				}
			}

		}

		List<CodWeapon> lethalGrenades = getLethalWeapons();
		for (CodWeapon grenade : lethalGrenades) {
			unlockGrenade(p, grenade);
		}

		List<CodWeapon> tacticalGrenades = getTacticalWeapons();
		for (CodWeapon grenade : tacticalGrenades) {
			unlockGrenade(p, grenade);
		}

		this.savePurchaseData(p);

	}

	public ArrayList<CodPerk> getPerks(Player p) {
		return purchasedPerks.get(p);
	}

	public void setPerks(Player p, ArrayList<CodPerk> perks) {
		this.purchasedPerks.put(p, perks);
	}

	public CodWeapon getWeaponForName(String name) {
		for (CodGun g : getPrimaryGuns()) {
			if (g.getName().equalsIgnoreCase(name))
				return g;
		}

		for (CodGun g : getSecondaryGuns()) {
			if (g.getName().equalsIgnoreCase(name))
				return g;
		}

		for (CodWeapon w : getLethalWeapons()) {
			if (w.getName().equalsIgnoreCase(name))
				return w;
		}

		for (CodWeapon w : getTacticalWeapons()) {
			if (w.getName().equalsIgnoreCase(name))
				return w;
		}

		//No weapon by the name was found.
		return null;
	}

}
