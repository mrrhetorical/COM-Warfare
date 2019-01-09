package com.rhetorical.cod.object;

import com.rhetorical.cod.Main;
import org.bukkit.Material;
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
		weaponType = wt;
		
		unlockType = t;
	
		name = n;
		weaponItem = weaponI;
	}
	
	public void save() {
		
		GunsFile.reloadData();
		
		if (levelUnlock <= 1 & creditUnlock <= 0 && !GunsFile.getData().contains("Weapons." + weaponType.toString() + ".default.name")) {
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.name", name);
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.item", weaponItem.getType().toString());
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.data", weaponItem.getDurability());
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.unlockType", unlockType.toString());
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.levelUnlock", levelUnlock);
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.creditUnlock", creditUnlock);
			GunsFile.saveData();
			GunsFile.reloadData();
			return;
		}
		
		int k = 0;
		while (GunsFile.getData().contains("Weapons." + weaponType.toString() + "." + k)) {
			if (GunsFile.getData().getString("Weapons." + weaponType.toString() + "." + k + ".name").equals(name)) {
				break;
			}
			
			k++;
		}
		
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".name", name);
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".item", weaponItem.getType().toString());
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".data", weaponItem.getDurability());
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".unlockType", unlockType.toString());
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".levelUnlock", levelUnlock);
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".creditUnlock", creditUnlock);
		GunsFile.saveData();
		GunsFile.reloadData();
	}
	
	public String getName() {
		return name;
	}
	
	public UnlockType getType() {
		return unlockType;
	}
	
	public ItemStack getWeapon() {
		if (Main.hasQualityArms()) {
			ItemStack gun = QualityGun.getGunForName(getName());

			if (gun.getType() != Material.AIR) {
				return gun;
			}
		}

		if (Main.hasCrackShot()) {
			ItemStack gun = CrackShotGun.generateWeapon(getName());

			if (gun != null)
				return gun;
		}
		ItemMeta meta = weaponItem.getItemMeta();
		
		meta.setDisplayName(getName());
		
		weaponItem.setItemMeta(meta);
		
		return weaponItem;
	}
	
	public int getLevelUnlock() {
		return levelUnlock;
	}
	
	public int getCreditUnlock() {
		return creditUnlock;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(UnlockType type) {
		unlockType = type;
	}
	
	public void setWeaponItem(ItemStack weapon) {
		weaponItem = weapon;
	}
	
	public boolean setLevelUnlock(int i) {
		if (!(i > 0) && i != -1) {
			return false;
		}
		
		levelUnlock = i;
		
		if (getType() == UnlockType.CREDITS && i != -1) {
			setType(UnlockType.BOTH);
			return true;
		}
		
		return true;
	}
	
	public boolean setCreditUnlock(int newPrice) {
		if (newPrice > 0) {
			creditUnlock = newPrice;
			return true;
		} else if (newPrice == 0) {
			creditUnlock = 0;
			setType(UnlockType.LEVEL);
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
