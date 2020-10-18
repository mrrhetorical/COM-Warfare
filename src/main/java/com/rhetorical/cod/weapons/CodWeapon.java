package com.rhetorical.cod.weapons;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.loadouts.LoadoutManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CodWeapon {

	private String name;
	
	private UnlockType unlockType;
	protected ItemStack weaponItem;
	protected ItemStack menuItem;
	
	private WeaponType weaponType;
	
	private int levelUnlock;
	private int creditUnlock;

	private boolean showInShop;

	public CodWeapon(String n, WeaponType wt, UnlockType t, ItemStack weaponI, int levelUnlock, boolean shop) {

		setShowInShop(shop);

		setLevelUnlock(levelUnlock);
		setWeaponType(wt);

		setType(t);
	
		setName(n);

		weaponItem = setupWeaponItem(weaponI);
		menuItem = setupMenuItem();
	}

	public CodWeapon(String n, WeaponType wt, UnlockType t, ItemStack weaponI, int levelUnlock, boolean isBlank, boolean shop) {
		setLevelUnlock(levelUnlock);

		setShowInShop(shop);

		setWeaponType(wt);
		setType(t);

		setName(n);
		if (!isBlank) {
			weaponItem = setupWeaponItem(weaponI);
			menuItem = setupMenuItem();
		} else {
			weaponItem = weaponI;
			menuItem = setupMenuItem();
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
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.showInShop", isShowInShop());
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
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".showInShop", isShowInShop());
		GunsFile.saveData();
		GunsFile.reloadData();
	}
	
	public String getName() {
		return name;
	}
	
	public UnlockType getType() {
		return unlockType;
	}

	/**
	 * @return The weapon item as set up by gun plugins. If the weapon doesn't exist in either QA or CrackShot, it is given a basic treatment by this plugin.
	 * */
	protected ItemStack setupWeaponItem(ItemStack weaponItem) {
		if (ComWarfare.hasQualityArms()) {
			if (!this.equals(LoadoutManager.getInstance().blankLethal) && !this.equals(LoadoutManager.getInstance().blankTactical)) {
				ItemStack gun = QualityGun.getGunForName(getName());

				if (gun != null && gun.getType() != Material.AIR) {
					return gun;
				}
			}
		}

		if (ComWarfare.hasCrackShot()) {
			if (!this.equals(LoadoutManager.getInstance().blankLethal) && !this.equals(LoadoutManager.getInstance().blankTactical)) {
				ItemStack gun = CrackShotGun.generateWeapon(getName());

				if (gun != null && gun.getType() != Material.AIR) {
					gun = CrackShotGun.updateItem(getName(), gun, null);
					return gun;
				}
			}
		}

		weaponItem = clearAttributesAndSetName(weaponItem);

		return weaponItem;
	}

	/**
	 * Gets the menu item for the gun. Slightly different from Weapon item.
	 * */
	protected ItemStack setupMenuItem() {
		ItemStack gun = getWeaponItem();

		gun = clearAttributesAndSetName(gun);

		return gun;
	}

	public ItemStack getMenuItem() {
		return getMenuItem(null);
	}

	public ItemStack getMenuItem(Player p) {
		return CrackShotGun.updateItem(getName(), menuItem.clone(), p);
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

	public boolean isShowInShop() {
		return showInShop;
	}

	public void setShowInShop(boolean value) {
		showInShop = value;
	}

	private ItemStack clearAttributesAndSetName(ItemStack item) {
		item = item.clone();
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(getName());
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		}

		item.setItemMeta(meta);
		return item;
	}

}
