package com.rhetorical.cod.weapons;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.GunsFile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

		gunItem = setupGunItem();
	}
	
	public void save() {
		
		GunsFile.reloadData();
		
		if (this.levelUnlock <= 1 & creditUnlock <= 0 && !GunsFile.getData().contains("Guns." + gunType.toString() + ".default.name")) {
			GunsFile.getData().set("Guns." + gunType.toString() + ".default.name", name);
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

	public UnlockType getType() {
		return this.unlockType;
	}

	private ItemStack setupGunItem() {
		if (Main.hasQualityArms()) {
			if (!this.equals(Main.loadManager.blankPrimary) && !this.equals(Main.loadManager.blankSecondary)) {
				ItemStack gun = QualityGun.getGunForName(getName());

				if (gun.getType() != Material.AIR) {
					return gun;
				}
			}
		}

		if (Main.hasCrackShot()) {
			if (!this.equals(Main.loadManager.blankPrimary) && !this.equals(Main.loadManager.blankSecondary)) {
				ItemStack gun = CrackShotGun.generateWeapon(getName());

				if (gun != null)
					return gun;
			}
		}

		ItemMeta meta = gunItem.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(this.getName());
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}

		this.gunItem.setItemMeta(meta);
		return this.gunItem;
	}

	public ItemStack getGun() {
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
		if (Main.hasQualityArms()) {
			ItemStack ammo = QualityGun.getAmmoForName(getName());

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
