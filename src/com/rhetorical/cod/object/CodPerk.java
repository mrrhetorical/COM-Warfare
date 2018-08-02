package com.rhetorical.cod.object;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CodPerk {
	private Perk perk;
	private PerkSlot slot;
	private ItemStack item;
	private ArrayList<String> lore = new ArrayList<String>();
	private int cost;
	
	public CodPerk(Perk perk, ItemStack item, PerkSlot slot, ArrayList<String> lore, int cost) {
		this.setPerk(perk);
		this.setItem(item);
		this.setSlot(slot);
		this.setLore(lore);
		this.setCost(cost);
	}

	public Perk getPerk() {
		return perk;
	}

	public void setPerk(Perk perk) {
		this.perk = perk;
	}

	public ItemStack getItem() {
		ItemStack i = this.item;
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(this.getPerk().getName());
		meta.setLore(this.getLore());
		i.setItemMeta(meta);
		return i;
	}

	private void setItem(ItemStack item) {
		this.item = item;
	}

	public PerkSlot getSlot() {
		return slot;
	}

	private void setSlot(PerkSlot slot) {
		this.slot = slot;
	}

	public ArrayList<String> getLore() {
		return lore;
	}

	private void setLore(ArrayList<String> lore) {
		this.lore = lore;
	}

	public int getCost() {
		return cost;
	}

	private void setCost(int cost) {
		this.cost = cost;
	}
}
