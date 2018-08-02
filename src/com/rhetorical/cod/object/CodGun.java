package com.rhetorical.cod.object;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.rhetorical.cod.files.GunsFile;

public class CodGun extends CodWeapon {

	private String name;

	private UnlockType unlockType;
	private int ammo;
	private ItemStack gunItem;
	private ItemStack ammoItem;
	
	private GunType gunType;

	private int levelUnlock;
	private int creditUnlock;

	public CodGun(String name, GunType gunT, UnlockType t, int a, ItemStack ammoI, ItemStack gunI, int levelUnlock) {

		super(name, null, t, gunI, levelUnlock);

		this.name = name;
		this.setGunType(gunT);
		this.levelUnlock = levelUnlock;
		this.creditUnlock = 0;
		this.unlockType = UnlockType.LEVEL;
		this.ammo = a;
		this.ammoItem = ammoI;
		this.gunItem = gunI;
	}
	
	public void save() {
		
		GunsFile.reloadData();
		
		if (this.levelUnlock <= 1 & creditUnlock <= 0 && !GunsFile.getData().contains("Guns." + gunType.toString() + ".default.name")) {
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.name", this.name);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.ammoCount", this.ammo);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.ammoItem", this.ammoItem);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.gunItem", this.gunItem);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.unlockType", this.unlockType.toString());
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.levelUnlock", this.levelUnlock);
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.creditUnlock", this.creditUnlock);
			GunsFile.saveData();
			GunsFile.reloadData();
			return;
		}
		
		int k = 0;
		while (GunsFile.getData().contains("Guns." + gunType.toString() + "." + k)) {
			if (GunsFile.getData().getString("Guns." + gunType.toString() + "." + k + ".name") == this.name) {
				GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".name", this.name);
				GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".ammoCount", this.ammo);
				GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".ammoItem", this.ammoItem);
				GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".gunItem", this.gunItem);
				GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".unlockType", this.unlockType.toString());
				GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".levelUnlock", this.levelUnlock);
				GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".creditUnlock", this.creditUnlock);
				GunsFile.saveData();
				GunsFile.reloadData();
				return;
			}
			
			k++;
		}
		
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".name", this.name);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".ammoCount", this.ammo);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".ammoItem", this.ammoItem);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".gunItem", this.gunItem);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".unlockType", this.unlockType.toString());
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".levelUnlock", this.levelUnlock);
		GunsFile.getData().set("Guns." + gunType.toString() + "." + k + ".creditUnlock", this.creditUnlock);
		GunsFile.saveData();
		GunsFile.reloadData();
	}

	public String getName() {
		return this.name;
	}

	public UnlockType getType() {
		return this.unlockType;
	}

	public ItemStack getGun() {
		ItemMeta meta = this.gunItem.getItemMeta();
		
		meta.setDisplayName(this.getName());
		
		this.gunItem.setItemMeta(meta);
		
		return this.gunItem;
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

	public void setGunItem(ItemStack gun) {
		this.gunItem = gun;
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
