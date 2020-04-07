package com.rhetorical.cod.inventories;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * This class has some data stored about inventories associated with it.
 * This class acts as a sort of doubly linked list with a link to another previous and/or next inventory (which in turn stores an inventory holder of this type)
 * */

public class COMInventoryHolder implements InventoryHolder {
	private boolean cancelClick;

	private GUIGroup group;

	private int loadout;

	private Inventory prev;
	private Inventory next;

	public COMInventoryHolder(boolean cancelClick) {
		setCancelClick(cancelClick);
		setGroup(GUIGroup.SINGLE);
	}

	public COMInventoryHolder(boolean cancelClick, GUIGroup group) {
		setCancelClick(cancelClick);
		setGroup(group);
	}

	public COMInventoryHolder(boolean cancelClick, GUIGroup group, Inventory prev, Inventory next) {
		setCancelClick(cancelClick);
		setGroup(group);
		setPrev(prev);
		setNext(next);
	}

	public COMInventoryHolder(boolean cancelClick, GUIGroup group, Inventory prev, Inventory next, int loadout) {
		setCancelClick(cancelClick);
		setGroup(group);
		setPrev(prev);
		setNext(next);
		setLoadout(loadout);
	}

	public boolean isCancelClick() {
		return cancelClick;
	}

	private void setCancelClick(boolean cancelClick) {
		this.cancelClick = cancelClick;
	}

	public GUIGroup getGroup() {
		return group;
	}

	private void setGroup(GUIGroup group) {
		this.group = group;
	}

	public Inventory getPrev() {
		return prev;
	}

	public void setPrev(Inventory prev) {
		this.prev = prev;
	}

	public Inventory getNext() {
		return next;
	}

	public void setNext(Inventory next) {
		this.next = next;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

	public int getLoadout() {
		return loadout;
	}

	public void setLoadout(int loadout) {
		this.loadout = loadout;
	}
}
