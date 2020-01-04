package com.rhetorical.cod.inventories;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.files.ShopFile;
import com.rhetorical.cod.lang.Lang;
import com.rhetorical.cod.loadouts.Loadout;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.perks.CodPerk;
import com.rhetorical.cod.perks.PerkManager;
import com.rhetorical.cod.progression.ProgressionManager;
import com.rhetorical.cod.weapons.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handles purchase data, list of available guns and whatnot.
 * */

public class ShopManager {

	private static ShopManager instance;

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
		if (instance != null)
			return;

		instance = this;

		loadGuns();
		loadWeapons();
		for (Player p : Bukkit.getOnlinePlayers()) {
			this.loadPurchaseData(p);
		}
	}

	public static ShopManager getInstance() {
		return instance != null ? instance : new ShopManager();
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

		if (LoadoutManager.getInstance().getDefaultPrimary() == null) {
			return;
		}

		primaryGuns.add(LoadoutManager.getInstance().getDefaultPrimary());
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

			String ammoName = GunsFile.getData().getString("Guns.Primary." + i + ".ammoName");

			boolean shop = GunsFile.getData().getBoolean("Guns.Primary." + i + ".showInShop");

			CodGun gun;
			if (ammoName == null) {
				gun = new CodGun(name, GunType.Primary, unlockType, ammoCount, ammoItem, gunItem,  levelUnlock, shop);
			} else {
				gun = new CodGun(name, ammoName, GunType.Primary, unlockType, ammoCount, ammoItem, gunItem, levelUnlock, shop);
			}

			gun.setCreditUnlock(creditsUnlock);
			primaryGuns.add(gun);
		}

		if (LoadoutManager.getInstance().getDefaultSecondary() != null)
			secondaryGuns.add(LoadoutManager.getInstance().getDefaultSecondary());
		for (int i = 0; GunsFile.getData().contains("Guns.Secondary." + i); i++) {

			if (LoadoutManager.getInstance().getDefaultPrimary() == null) {
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

			String ammoName = GunsFile.getData().getString("Guns.Secondary." + i + ".ammoName");

			boolean shop = GunsFile.getData().getBoolean("Guns.Secondary." + i + ".showInShop");

			CodGun gun;
			if (ammoName == null) {
				gun = new CodGun(name, GunType.Secondary, unlockType, ammoCount, ammoItem, gunItem, levelUnlock, shop);
			} else {
				gun = new CodGun(name, ammoName, GunType.Secondary, unlockType, ammoCount, ammoItem, gunItem, levelUnlock, shop);
			}
			gun.setCreditUnlock(creditsUnlock);
			secondaryGuns.add(gun);
		}
	}

	private void loadWeapons() {
		if (LoadoutManager.getInstance().getDefaultLethal() != null)
			lethalWeapons.add(LoadoutManager.getInstance().getDefaultLethal());
		if (LoadoutManager.getInstance().getDefaultTactical() != null)
			tacticalWeapons.add(LoadoutManager.getInstance().getDefaultTactical());

		for (int i = 0; GunsFile.getData().contains("Weapons.LETHAL." + i); i++) {
			String weaponName = GunsFile.getData().getString("Weapons.LETHAL." + i + ".name");
			UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.LETHAL." + i + ".unlockType"));
			int amount = GunsFile.getData().getInt("Weapons.LETHAL." + i + ".amount");
			Material weaponMaterial;
			String weaponMat = GunsFile.getData().getString("Weapons.LETHAL." + i + ".item");
			try {
				weaponMaterial = Material.valueOf(weaponMat);
			} catch (Exception e) {
				Main.sendMessage(Main.getConsole(), Main.getPrefix() + ChatColor.RED + "Could not load lethal " + weaponName + " because no material exits with name " + weaponMat + "!", Main.getLang());
				continue;
			}
			int levelUnlock = GunsFile.getData().getInt("Weapons.LETHAL." + i + ".levelUnlock");
			int creditUnlock = GunsFile.getData().getInt("Weapons.LETHAL." + i + ".creditUnlock");
			short weaponData = (short) GunsFile.getData().getInt("Weapons.LETHAL." + i + ".data");

			ItemStack weapon = new ItemStack(weaponMaterial, amount, weaponData);

			boolean shop = GunsFile.getData().getBoolean("Weapons.LETHAL." + i + ".showInShop");

			CodWeapon grenade = new CodWeapon(weaponName, WeaponType.LETHAL, type, weapon, levelUnlock, shop);
			grenade.setCreditUnlock(creditUnlock);
			lethalWeapons.add(grenade);
		}

		for (int i = 0; GunsFile.getData().contains("Weapons.TACTICAL." + i); i++) {
			String weaponName = GunsFile.getData().getString("Weapons.TACTICAL." + i + ".name");
			UnlockType type = UnlockType.valueOf(GunsFile.getData().getString("Weapons.TACTICAL." + i + ".unlockType"));
			int amount = GunsFile.getData().getInt("Weapons.TACTICAL." + i + ".amount");
			Material weaponMaterial;
			String weaponMat = GunsFile.getData().getString("Weapons.TACTICAL." + i + ".item");
			try {
				weaponMaterial = Material.valueOf(weaponMat);
			} catch (Exception e) {
				Main.sendMessage(Main.getConsole(), Main.getPrefix() + ChatColor.RED + "Could not load tactical " + weaponName + " because no material exits with name " + weaponMat + "!", Main.getLang());
				continue;
			}
			int levelUnlock = GunsFile.getData().getInt("Weapons.TACTICAL." + i + ".levelUnlock");
			int creditUnlock = GunsFile.getData().getInt("Weapons.TACTICAL." + i + ".creditUnlock");
			short weaponData = (short) GunsFile.getData().getInt("Weapons.TACTICAL." + i + ".data");

			ItemStack weapon = new ItemStack(weaponMaterial, amount, weaponData);

			boolean shop = GunsFile.getData().getBoolean("Weapons.TACTICAL." + i + ".showInShop");

			CodWeapon grenade = new CodWeapon(weaponName, WeaponType.TACTICAL, type, weapon, levelUnlock, shop);
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
					if (gun != LoadoutManager.getInstance().blankPrimary && gun != LoadoutManager.getInstance().blankSecondary)
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
					if (grenade != LoadoutManager.getInstance().blankLethal && grenade != LoadoutManager.getInstance().blankTactical)
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
			perks.add(PerkManager.getInstance().getDefaultOne().getPerk().getName());
			perks.add(PerkManager.getInstance().getDefaultTwo().getPerk().getName());
			perks.add(PerkManager.getInstance().getDefaultThree().getPerk().getName());
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
				List<CodGun> allGuns = new ArrayList<>(getPrimaryGuns());
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
					guns.add(LoadoutManager.getInstance().getDefaultPrimary());
					guns.add(LoadoutManager.getInstance().getDefaultSecondary());
				}
			}
		} else {
			guns.add(LoadoutManager.getInstance().getDefaultPrimary());
			guns.add(LoadoutManager.getInstance().getDefaultSecondary());
		}

		ArrayList<String> weaponList = (ArrayList<String>) ShopFile.getData().get("Purchased.Weapons." + p.getName());

		if (weaponList != null) {
			for (String s : weaponList) {
				boolean found = false;
				List<CodWeapon> allWeapons = new ArrayList<>(getLethalWeapons());
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
					grenades.add(LoadoutManager.getInstance().getDefaultLethal());
					grenades.add(LoadoutManager.getInstance().getDefaultTactical());
				}
			}
		} else {
			grenades.add(LoadoutManager.getInstance().getDefaultLethal());
			grenades.add(LoadoutManager.getInstance().getDefaultTactical());
		}

		ArrayList<String> perkList = (ArrayList<String>) ShopFile.getData().get("Purchased.Perks." + p.getName());
		if (perkList != null) {
			outsidePerks: for (String s : perkList) {
				for (CodPerk perk : PerkManager.getInstance().getAvailablePerks()) {
					if (perk.getPerk().getName().equals(s)) {
						perks.add(perk);
						continue outsidePerks;
					}
				}
				perks.add(PerkManager.getInstance().getDefaultOne());
				perks.add(PerkManager.getInstance().getDefaultTwo());
				perks.add(PerkManager.getInstance().getDefaultThree());
			}
		} else {
			perks.add(PerkManager.getInstance().getDefaultOne());
			perks.add(PerkManager.getInstance().getDefaultTwo());
			perks.add(PerkManager.getInstance().getDefaultThree());
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

	@Deprecated
	public boolean isAvailableForPurchase(Player p, CodGun gun) {

		if (gun.getType() == UnlockType.LEVEL || gun.getType() == UnlockType.BOTH) {

			if (ProgressionManager.getInstance().getLevel(p) >= gun.getLevelUnlock()) {
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

		ShopManager.getInstance().setPurchasedGuns(purchased);

		Main.sendMessage(p, Main.getPrefix() + Lang.WEAPON_UNLOCKED.getMessage().replace("{gun-name}", gun.getName()), Main.getLang());
	}

	private void unlockGrenade(Player p, CodWeapon grenade) {
		if (grenade.getType() == UnlockType.LEVEL) {

			HashMap<Player, ArrayList<CodWeapon>> purchased = ShopManager.getInstance().getPurchasedWeapons();

			if (!purchased.get(p).contains(grenade) && !grenade.equals(LoadoutManager.getInstance().blankLethal) && !grenade.equals(LoadoutManager.getInstance().blankTactical)) {

				if (ProgressionManager.getInstance().getLevel(p) >= grenade.getLevelUnlock()) {

					ArrayList<CodWeapon> grenades = purchased.get(p);

					grenades.add(grenade);

					purchased.put(p, grenades);

					ShopManager.getInstance().setPurchasedWeapons(purchased);

					Main.sendMessage(p, Main.getPrefix() + Lang.WEAPON_UNLOCKED.getMessage().replace("{gun-name}", grenade.getName()), Main.getLang());

				}
			}
		} else if (grenade.getType() == UnlockType.BOTH) {
			if (ProgressionManager.getInstance().getLevel(p) == grenade.getLevelUnlock() && grenade.isShowInShop()) {
				Main.sendMessage(p,
						Main.getPrefix() + Lang.WEAPON_PURCHASE_UNLOCKED.getMessage().replace("{gun-name}", grenade.getName()), Main.getLang());
			}
		}
	}

	public void checkForNewGuns(Player p) {

		ShopManager.getInstance().loadPurchaseData(p);

		List<CodGun> primaryGuns = new ArrayList<>(getPrimaryGuns());
		for (CodGun gun : primaryGuns) {
			if (gun.getType() == UnlockType.LEVEL) {

				HashMap<Player, ArrayList<CodGun>> purchased = ShopManager.getInstance().getPurchasedGuns();

				if (!purchased.get(p).contains(gun)) {

					if (ProgressionManager.getInstance().getLevel(p) >= gun.getLevelUnlock()) {

						unlockGun(purchased, p, gun);
					}
				}
			} else if (gun.getType() == UnlockType.BOTH) {
				if (ProgressionManager.getInstance().getLevel(p) == gun.getLevelUnlock() && gun.isShowInShop()) {
					Main.sendMessage(p, Main.getPrefix() + Lang.WEAPON_PURCHASE_UNLOCKED.getMessage().replace("{gun-name}", gun.getName()), Main.getLang());
				}
			}

		}

		List<CodGun> secondaryGuns = new ArrayList<>(getSecondaryGuns());
		for (CodGun gun : secondaryGuns) {
			if (gun.getType() == UnlockType.LEVEL) {

				HashMap<Player, ArrayList<CodGun>> purchased = ShopManager.getInstance().getPurchasedGuns();

				if (!purchased.get(p).contains(gun)) {

					if (ProgressionManager.getInstance().getLevel(p) >= gun.getLevelUnlock()) {
						unlockGun(purchased, p, gun);
					}
				}
			} else if (gun.getType() == UnlockType.BOTH) {
				if (ProgressionManager.getInstance().getLevel(p) >= gun.getLevelUnlock() && gun.isShowInShop()) {
					Main.sendMessage(p, Main.getPrefix() + Lang.WEAPON_PURCHASE_UNLOCKED.getMessage().replace("{gun-name}", gun.getName()), Main.getLang());
				}
			}

		}

		List<CodWeapon> lethalGrenades = new ArrayList<>(getLethalWeapons());
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
