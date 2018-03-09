package com.rhetorical.cod.object;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.rhetorical.cod.files.GunsFile;

public class CodWeapon {

	private String name;
	
	private UnlockType unlockType;
	private ItemStack weaponItem;
	
	private WeaponType weaponType;
	
	
	private int levelUnlock;
	private int creditUnlock;
	
	public CodWeapon(String n, WeaponType wt, UnlockType t, ItemStack weaponI, int levelUnlock) {
		this.levelUnlock = levelUnlock;
		this.weaponType = wt;
		
		this.unlockType = UnlockType.LEVEL;
	
		this.name = n;
		this.weaponItem = weaponI;
	}
	
	public void save() {
		
		GunsFile.reloadData();
		
		if (this.levelUnlock <= 1 & creditUnlock <= 0 && !GunsFile.getData().contains("Weapons." + weaponType.toString() + ".default.name")) {
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.name", this.name);
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.item", this.weaponItem);
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.unlockType", this.unlockType.toString());
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.levelUnlock", this.levelUnlock);
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.creditUnlock", this.creditUnlock);
			GunsFile.saveData();
			GunsFile.reloadData();
			return;
		}
		
		int k = 0;
		while (GunsFile.getData().contains("Weapons." + weaponType.toString() + "." + k)) {
			if (GunsFile.getData().getString("Weapons." + weaponType.toString() + "." + k + ".name") == this.name) {
				GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".name", this.name);
				GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".item", this.weaponItem);
				GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".unlockType", this.unlockType.toString());
				GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".levelUnlock", this.levelUnlock);
				GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".creditUnlock", this.creditUnlock);
				GunsFile.saveData();
				GunsFile.reloadData();
				return;
			}
			
			k++;
		}
		
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".name", this.name);
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".item", this.weaponItem);
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".unlockType", this.unlockType.toString());
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".levelUnlock", this.levelUnlock);
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".creditUnlock", this.creditUnlock);
		GunsFile.saveData();
		GunsFile.reloadData();
		return;
	}
	
	public String getName() {
		return this.name;
	}
	
	public UnlockType getType() {
		return this.unlockType;
	}
	
	public ItemStack getWeapon() {
		ItemMeta meta = this.weaponItem.getItemMeta();
		
		meta.setDisplayName(this.getName());
		
		this.weaponItem.setItemMeta(meta);
		
		return this.weaponItem;
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
	
	public void setWeaponItem(ItemStack weapon) {
		this.weaponItem = weapon;
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
			return true;
		} else if (newPrice == 0) {
			this.creditUnlock = 0;
			this.setType(UnlockType.LEVEL);
			return true;
		} 
		
		return false;
	}

	public WeaponType getWeaponType() {
		return weaponType;
	}

	public void setWeaponType(WeaponType weaponType) {
		this.weaponType = weaponType;
	}
}
