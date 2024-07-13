package com.rhetorical.cod.weapons;

import com.rhetorical.cod.files.GunsFile;
import com.rhetorical.cod.loadouts.LoadoutManager;
import com.rhetorical.cod.weapons.support.CrackShotGun;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CodWeapon {

	private String name;

	private String itemName;

	private int itemAmount;
	
	private UnlockType unlockType;
	private WeaponType weaponType;
	
	private int levelUnlock;
	private int creditUnlock;

	private boolean showInShop;

	public CodWeapon(String n, WeaponType wt, UnlockType unlockType, String itemName, int itemAmount, int levelUnlock, boolean shop) {

		setShowInShop(shop);

		setLevelUnlock(levelUnlock);
		setWeaponType(wt);

		setType(unlockType);
	
		setName(n);

		setItemName(itemName);
		setItemAmount(itemAmount);
	}

	public CodWeapon(String n, WeaponType wt, UnlockType unlockType, String itemName, int itemAmount, int levelUnlock, boolean isBlank, boolean shop) {
		setLevelUnlock(levelUnlock);

		setShowInShop(shop);

		setWeaponType(wt);
		setType(unlockType);

		setName(n);
		setItemName(itemName);
		setItemAmount(itemAmount);
	}
	public void save() {
		GunsFile.reloadData();
		
		if (levelUnlock <= 1 & creditUnlock <= 0 && !GunsFile.getData().contains("Weapons." + weaponType.toString() + ".default.name")) {
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.name", getName());
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.itemName", getItemName());
			GunsFile.getData().set("Weapons." + weaponType.toString() + ".default.amount", getItemAmount());
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
		
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".name", getName());
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".itemName", getItemName());
		GunsFile.getData().set("Weapons." + weaponType.toString() + "." + k + ".amount", getItemAmount());
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
	 * @param weaponItem The base ItemStack for the weapon item.
	 * @return The weapon item as set up by gun plugins. If the weapon doesn't exist in either QA or CrackShot, it is given a basic treatment by this plugin.
	 * */
	protected ItemStack getSetupItem(String weaponItem) {
		if (this.equals(LoadoutManager.getInstance().blankLethal)
				|| this.equals(LoadoutManager.getInstance().blankTactical)
				|| this.equals(LoadoutManager.getInstance().blankPrimary)
				|| this.equals(LoadoutManager.getInstance().blankSecondary)) {

			ItemStack itemStack = new ItemStack(Material.valueOf(weaponItem.toUpperCase()), 1);
			return clearAttributesAndSetName(itemStack);
		}

		GunResolver gunResolver = GunResolver.getInstance();

		return gunResolver.getGunItem(weaponItem);
	}

	/**
	 * Gets the menu item for the gun. Slightly different from Weapon item.
	 * @return The menu item
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
		return CrackShotGun.updateItem(getName(), setupMenuItem().clone(), p);
	}

	public ItemStack getWeaponItem() {
		return getSetupItem(getItemName());
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

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public int getItemAmount() {
		return itemAmount;
	}

	public void setItemAmount(int itemAmount) {
		this.itemAmount = itemAmount;
	}
}
