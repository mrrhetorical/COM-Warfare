package com.rhetorical.cod.weapons;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.GunsFile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CodGun extends CodWeapon {

	private String name;
	private String ammoName; //for the purposes of qualityarmory

	private UnlockType unlockType;
	private int ammo;
	private final ItemStack gunItem;
	private ItemStack menuItem;
	private ItemStack ammoItem;
	
	private GunType gunType;

	private int levelUnlock;
	private int creditUnlock;

	public CodGun(String name, GunType gunT, UnlockType t, int a, ItemStack ammoI, ItemStack gunI, int levelUnlock) {

		super(name, null, t, gunI, levelUnlock);

		this.name = name;
		this.ammoName = "";
		this.setGunType(gunT);
		this.levelUnlock = levelUnlock;
		this.creditUnlock = 0;
		this.unlockType = UnlockType.LEVEL;
		this.ammo = a;
		this.ammoItem = ammoI;

		gunItem = setupWeaponItem(gunI);
		menuItem = setupMenuItem(gunI);
	}

	public CodGun(String name, String ammoName, GunType gunT, UnlockType t, int a, ItemStack ammoI, ItemStack gunI, int levelUnlock) {

		super(name, null, t, gunI, levelUnlock);

		this.name = name;
		this.ammoName = ammoName;
		this.setGunType(gunT);
		this.levelUnlock = levelUnlock;
		this.creditUnlock = 0;
		this.unlockType = UnlockType.LEVEL;
		this.ammo = a;
		this.ammoItem = ammoI;

		gunItem = setupWeaponItem(gunI);
		menuItem = setupMenuItem(gunI);
	}

	public CodGun(String name, GunType gunT, UnlockType t, int a, ItemStack ammoI, ItemStack gunI, int levelUnlock, boolean isBlank) {

		super(name, null, t, gunI, levelUnlock, isBlank);

		this.name = name;
		this.setGunType(gunT);
		this.levelUnlock = levelUnlock;
		this.creditUnlock = 0;
		this.unlockType = UnlockType.LEVEL;
		this.ammo = a;
		this.ammoItem = ammoI;

		if (!isBlank) {
			gunItem = setupWeaponItem(gunI);
			menuItem = setupMenuItem(gunI);
		} else {
			gunItem = gunI;
			menuItem = gunI;
		}
	}
	
	public void save() {
		
		GunsFile.reloadData();
		
		if (this.levelUnlock <= 1 & creditUnlock <= 0 && !GunsFile.getData().contains("Guns." + gunType.toString() + ".default.name")) {
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.name", name);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.ammoName", ammoName);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.ammoCount", ammo);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.ammoItem", ammoItem.getType().toString());
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.ammoData", ammoItem.getDurability());
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.gunItem", gunItem.getType().toString());
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.gunData", gunItem.getDurability());
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.unlockType", unlockType.toString());
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.levelUnlock", levelUnlock);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.creditUnlock", creditUnlock);
			GunsFile.saveData();
			GunsFile.reloadData();
			return;
		}
		
		int k = 0;
		while (GunsFile.getData().contains("Guns." + gunType.toString() + "." + k)) {
			if (GunsFile.getData().getString("Guns." + gunType.toString() + "." + k + ".name").equals(name)) {
				break;
			}
			
			k++;
		}
		
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".name", name);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".ammoCount", ammo);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".ammoName", ammoName);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".ammoItem", ammoItem.getType().toString());
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".ammoData", ammoItem.getDurability());
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".gunItem", gunItem.getType().toString());
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".gunData", gunItem.getDurability());
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".unlockType", unlockType.toString());
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".levelUnlock", levelUnlock);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".creditUnlock", creditUnlock);
		GunsFile.saveData();
		GunsFile.reloadData();
	}

	public String getName() {
		return this.name;
	}

	public String getAmmoName() {
		return this.ammoName;
	}

	public UnlockType getType() {
		return this.unlockType;
	}

	public ItemStack getGunItem() {
		return gunItem.clone();
	}

	public ItemStack getMenuItem() {
		return menuItem.clone();
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

	public void setType(UnlockType type) {
		this.unlockType = type;
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
		if (Main.hasQualityArms()) {
			ItemStack ammo = QualityGun.getAmmoForName(getAmmoName());

			if (ammo.getType() != Material.AIR) {
				return ammo;
			}
		}
		return this.ammoItem;
	}

	public void setAmmoCount(int amount) {
		this.ammo = amount;
	}

	public void setAmmoItem(ItemStack ammo) {
		this.ammoItem = ammo;
	}

	public GunType getGunType() {
		return gunType;
	}

	private void setGunType(GunType gunType) {
		this.gunType = gunType;
	}

}
