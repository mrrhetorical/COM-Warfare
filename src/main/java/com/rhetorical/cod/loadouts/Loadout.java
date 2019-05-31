package com.rhetorical.cod.loadouts;

import com.rhetorical.cod.inventories.InventoryManager;
import com.rhetorical.cod.inventories.ShopManager;
import com.rhetorical.cod.perks.CodPerk;
import com.rhetorical.cod.perks.Perk;
import com.rhetorical.cod.perks.PerkSlot;
import com.rhetorical.cod.weapons.CodGun;
import com.rhetorical.cod.weapons.CodWeapon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class Loadout implements Listener {

	private Player owner;
	private String name;

	private CodGun primary;
	private CodGun secondary;
	private CodWeapon lethal;
	private CodWeapon tactical;
	private CodPerk perk1;
	private CodPerk perk2;
	private CodPerk perk3;

	private Inventory primaryInventory;
	private Inventory secondaryInventory;
	private Inventory lethalInventory;
	private Inventory tacticalInventory;

	private Inventory perk1Inventory;
	private Inventory perk2Inventory;
	private Inventory perk3Inventory;

	public Loadout(Player o, String n, CodGun p, CodGun s, CodWeapon l, CodWeapon t, CodPerk p1, CodPerk p2, CodPerk p3) {
		this.owner = o;
		this.name = n;
		this.primary = p;
		this.secondary = s;
		this.lethal = l;
		this.tactical = t;
		this.perk1 = p1;
		this.perk2 = p2;
		this.perk3 = p3;
	}

	public void setPrimary(CodGun gun) {
		this.primary = gun;
		LoadoutManager.getInstance().save(this.getOwner());
		LoadoutManager.getInstance().load(this.getOwner());
		InventoryManager.getInstance().setupCreateClassInventory(this.getOwner());
	}

	public void setSecondary(CodGun gun) {
		this.secondary = gun;
		LoadoutManager.getInstance().save(this.getOwner());
		LoadoutManager.getInstance().load(this.getOwner());
		InventoryManager.getInstance().setupCreateClassInventory(this.getOwner());
	}

	public void setLethal(CodWeapon grenade) {
		this.lethal = grenade;
		LoadoutManager.getInstance().save(this.getOwner());
		LoadoutManager.getInstance().load(this.getOwner());
		InventoryManager.getInstance().setupCreateClassInventory(this.getOwner());
	}

	public void setTactical(CodWeapon grenade) {
		this.tactical = grenade;
		LoadoutManager.getInstance().save(this.getOwner());
		LoadoutManager.getInstance().load(this.getOwner());
		InventoryManager.getInstance().setupCreateClassInventory(this.getOwner());
	}

	public void setPerk(PerkSlot slot, CodPerk perk) {
		switch (slot) {
			case ONE:
				this.perk1 = perk;
				LoadoutManager.getInstance().save(this.getOwner());
				LoadoutManager.getInstance().load(this.getOwner());
				InventoryManager.getInstance().setupCreateClassInventory(this.getOwner());
				break;
			case TWO:
				this.perk2 = perk;
				LoadoutManager.getInstance().save(this.getOwner());
				LoadoutManager.getInstance().load(this.getOwner());
				InventoryManager.getInstance().setupCreateClassInventory(this.getOwner());
				break;
			case THREE:
				this.perk3 = perk;
				LoadoutManager.getInstance().save(this.getOwner());
				LoadoutManager.getInstance().load(this.getOwner());
				InventoryManager.getInstance().setupCreateClassInventory(this.getOwner());
				break;
			default:
				break;
		}
	}

	private Player getOwner() {
		return this.owner;
	}

	public CodGun getPrimary() {
		return this.primary;
	}

	public CodGun getSecondary() {
		return this.secondary;
	}

	public CodWeapon getLethal() {
		return this.lethal;
	}

	public CodWeapon getTactical() {
		return this.tactical;
	}

	public String getName() {
		return this.name;
	}

	public CodPerk getPerk1() {
		return this.perk1;
	}

	public CodPerk getPerk2() {
		return this.perk2;
	}

	public CodPerk getPerk3() {
		return this.perk3;
	}

	public boolean hasPerk(Perk perk) {
		return getPerk1().getPerk().equals(perk) || getPerk2().getPerk().equals(perk) || getPerk3().getPerk().equals(perk);

	}

	public Inventory getPrimaryInventory() {

		if (this.primaryInventory == null) {
			InventoryManager.getInstance().setupPlayerSelectionInventories(this.getOwner());
		}

		return this.primaryInventory;
	}

	public Inventory getSecondaryInventory() {

		if (this.secondaryInventory == null) {
			InventoryManager.getInstance().setupPlayerSelectionInventories(this.getOwner());
		}

		return this.secondaryInventory;
	}

	public Inventory getLethalInventory() {

		if (this.lethalInventory == null) {
			InventoryManager.getInstance().setupPlayerSelectionInventories(this.getOwner());
		}
		return this.lethalInventory;
	}

	public Inventory getTacticalInventory() {

		if (this.tacticalInventory == null) {
			InventoryManager.getInstance().setupPlayerSelectionInventories(this.getOwner());
		}

		return this.tacticalInventory;
	}

	public Inventory getPerk1Inventory() {

		if (this.perk1Inventory == null) {
			InventoryManager.getInstance().setupPlayerSelectionInventories(this.getOwner());
		}

		return this.perk1Inventory;
	}

	public Inventory getPerk2Inventory() {

		if (this.perk2Inventory == null) {
			InventoryManager.getInstance().setupPlayerSelectionInventories(this.getOwner());
		}

		return this.perk2Inventory;
	}

	public Inventory getPerk3Inventory() {

		if (this.perk3Inventory == null) {
			InventoryManager.getInstance().setupPlayerSelectionInventories(this.getOwner());
		}

		return this.perk3Inventory;
	}

	public void setPerkInventory(int number, Inventory inventory) {
		switch (number) {
		case 0:
			this.perk1Inventory = inventory;
			break;
		case 1:
			this.perk2Inventory = inventory;
			break;
		case 2:
			this.perk3Inventory = inventory;
			break;
		default:
			return;
		}
	}

	public void setPrimaryInventory(Inventory inventory) {
		this.primaryInventory = inventory;
	}

	public void setSecondaryInventory(Inventory inventory) {
		this.secondaryInventory = inventory;
	}

	public void setLethalInventory(Inventory inventory) {
		this.lethalInventory = inventory;
	}

	public void setTacticalInventory(Inventory inventory) {
		this.tacticalInventory = inventory;
	}

	@EventHandler
	public void inventoryClickListener(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;

		Player p = (Player) e.getWhoClicked();

		if (InventoryManager.getInstance().shouldCancelClick(e.getInventory(), p)) {
			e.setCancelled(true);
		} else {
			return;
		}

		if (e.getCurrentItem() == null)
			return;

		if (!(e.getInventory().equals(this.getPrimaryInventory()) || e.getInventory().equals(this.getSecondaryInventory()) || e.getInventory().equals(this.getLethalInventory()) || e.getInventory().equals(this.getTacticalInventory()))) {
			return;
		}

		if (e.getCurrentItem().equals(InventoryManager.getInstance().backInv)) {
			p.closeInventory();
			return;
		}

		if (e.getInventory().equals(this.getPrimaryInventory())) {
			for (CodGun gun : ShopManager.getInstance().getPrimaryGuns()) {
				if (gun.getMenuItem().equals(e.getCurrentItem())) {
					this.setPrimary(gun);
					p.closeInventory();
					p.openInventory(InventoryManager.getInstance().createClassInventory.get(p));
				}
			}
		}
	}

}
