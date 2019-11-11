package com.rhetorical.cod.weapons;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.loadouts.LoadoutManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CodWeapon {

	private String name;
	
	private UnlockType unlockType;
	private final ItemStack weaponItem;
	private final ItemStack menuItem;
	
	private WeaponType weaponType;
	
	private int levelUnlock;
	private int creditUnlock;

	public CodWeapon(String n, WeaponType wt, UnlockType t, ItemStack weaponI, int levelUnlock) {
		this.levelUnlock = levelUnlock;
		weaponType = wt;
		
		unlockType = t;
	
		name = n;
		weaponItem = setupWeaponItem(weaponI);
		menuItem = setupMenuItem(weaponI);
	}

	public CodWeapon(String n, WeaponType wt, UnlockType t, ItemStack weaponI, int levelUnlock, boolean isBlank) {
		this.levelUnlock = levelUnlock;
		weaponType = wt;

		unlockType = t;

		name = n;
		if (!isBlank) {
			weaponItem = setupWeaponItem(weaponI);
			menuItem = setupMenuItem(weaponI);
		} else {
			weaponItem = weaponI;
			menuItem = weaponI;
		}
	}
	public void save() {
		
		GunsFile.reloadData();
		
		if (levelUnlock <= 1 & creditUnlock <= 0 && !GunsFile.getData().contains("Weapons." + weaponType.toString() + ".default.name")) {
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.name", name);
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.item", weaponItem.getType().toString());
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.amount", weaponItem.getAmount());
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
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".amount", weaponItem.getAmount());
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
	
	protected ItemStack setupWeaponItem(ItemStack weaponItem) {
		if (Main.hasQualityArms()) {
			if (!this.equals(LoadoutManager.getInstance().blankLethal) && !this.equals(LoadoutManager.getInstance().blankTactical)) {
				ItemStack gun = QualityGun.getGunForName(getName());

				if (gun != null && gun.getType() != Material.AIR) {
					return gun;
				}
			}
		}

		if (Main.hasCrackShot()) {
			if (!this.equals(LoadoutManager.getInstance().blankLethal) && !this.equals(LoadoutManager.getInstance().blankTactical)) {
				ItemStack gun = CrackShotGun.generateWeapon(getName());

				if (gun != null && gun.getType() != Material.AIR)
					return gun;
			}
		}
		ItemMeta meta = weaponItem.getItemMeta();

		if (meta != null) {
			meta.setDisplayName(getName());
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}
		
		weaponItem.setItemMeta(meta);

		weaponItem = CrackShotGun.updateItem(getName(), weaponItem, null);

		return weaponItem;
	}

	protected ItemStack setupMenuItem(ItemStack gunItem) {
		ItemStack gun = getWeaponItem();

		ItemMeta meta = gun.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(getName());
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}

		gun.setItemMeta(meta);

		return gun;
	}

	public ItemStack getMenuItem() {
		return menuItem.clone();
	}

	public ItemStack getWeaponItem() {
		return weaponItem.clone();
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
