package com.rhetorical.cod.weapons;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.weapons.support.QualityGun;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CodGun extends CodWeapon {

	private String name;
	private String ammoName; //for the purposes of qualityarmory

	private int ammo;
	private ItemStack ammoItem;

	private int levelUnlock;
	private int creditUnlock;

	/**
	 * Creates a gun with the given parameters.
	 *
	 * */
	public CodGun(String name, WeaponType gunT, UnlockType unlockType, int ammoCount, String gunCode, String ammoCode, int levelUnlock, boolean shop) {

		super(name, gunT, unlockType, gunCode, 1, levelUnlock, shop);

		this.name = name;
		this.ammoName = ammoCode;
		this.levelUnlock = levelUnlock;
		this.creditUnlock = 0;
		this.ammo = ammoCount;

		ammoItem = getSetupItem(getAmmoName());
	}

	/**
	 * Creates a gun with the given parameters including the name of the ammo (for QualityArmory)
	 * */
	public CodGun(String name, String ammoName, WeaponType gunT, UnlockType unlockType, int ammoCount, String gunCode, String ammoCode, int levelUnlock, boolean shop) {

		super(name, gunT, unlockType, gunCode, 1, levelUnlock, shop);

		this.name = name;
		this.ammoName = ammoName;
		this.levelUnlock = levelUnlock;
		this.creditUnlock = 0;
		this.ammo = ammoCount;

		ammoItem = getSetupItem(ammoCode);
	}

	/**
	 * Intended to be used for generating blank guns.
	 * */
	public CodGun(String name, WeaponType gunT, UnlockType unlockType, int ammoCount, String gunCode, String ammoCode, int levelUnlock, boolean isBlank, boolean shop) {

		super(name, gunT, unlockType, gunCode, 1, levelUnlock, isBlank, shop);

		this.name = name;
		this.levelUnlock = levelUnlock;
		this.creditUnlock = 0;
		this.ammo = ammoCount;

		ammoItem = getSetupItem(ammoCode);
	}

	public void save() {
		super.save();

		GunsFile.reloadData();
		
		if (this.levelUnlock <= 1 & creditUnlock <= 0 && !GunsFile.getData().contains("Weapons." + getWeaponType().toString() + ".default.name")) {
			GunsFile.getData().set("Weapons." + getWeaponType().toString() + ".default.ammoCount", ammo);
			GunsFile.getData().set("Weapons." + getWeaponType().toString() + ".default.ammoName", ammoName);
			GunsFile.saveData();
			GunsFile.reloadData();
			return;
		}
		
		int k = 0;
		while (GunsFile.getData().contains("Weapons." + getWeaponType().toString() + "." + k)) {
			if (GunsFile.getData().getString("Weapons." + getWeaponType().toString() + "." + k + ".name").equals(name)) {
				break;
			}
			
			k++;
		}
		
		GunsFile.getData().set("Weapons." + getWeaponType().toString() + "." + k + ".ammoCount", ammo);
		GunsFile.getData().set("Weapons." + getWeaponType().toString() + "." + k + ".ammoName", ammoName);
		GunsFile.saveData();
		GunsFile.reloadData();
	}

	public String getName() {
		return this.name;
	}

	public String getAmmoName() {
		return this.ammoName;
	}

	public ItemStack getGunItem() {
		return getWeaponItem().clone();
	}

	public int getLevelUnlock() {
		return this.levelUnlock;
	}

	public int getCreditUnlock() {
		return this.creditUnlock;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean setLevelUnlock(int i) {
		if (!(i > 0) && i != -1) {
			return false;
		}

		this.levelUnlock = i;

		if (this.getType() == UnlockType.CREDITS && i != -1) {
			this.setType(UnlockType.BOTH);
			return true;
		}

		return true;
	}

	public boolean setCreditUnlock(int newPrice) {
		if (newPrice > 0) {
			this.creditUnlock = newPrice;
			if (this.getLevelUnlock() != 0) {
				this.setType(UnlockType.BOTH);
			} else {
				this.setType(UnlockType.CREDITS);
			}
			return true;
		} else if (newPrice == 0) {
			this.creditUnlock = 0;
			this.setType(UnlockType.LEVEL);
			return true;
		}

		return false;
	}

	public int getAmmoCount() {
		return this.ammo;
	}

	public ItemStack getAmmo() {
		if (ComWarfare.hasQualityArms()) {
			ItemStack ammo = QualityGun.getAmmoForName(getAmmoName());

			if (ammo.getType() != Material.AIR) {
				return ammo;
			}
		}
		return this.ammoItem.clone();
	}

	public void setAmmoCount(int amount) {
		this.ammo = amount;
	}

	public void setAmmoItem(ItemStack ammo) {
		this.ammoItem = ammo;
	}

}
