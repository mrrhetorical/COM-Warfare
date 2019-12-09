package com.rhetorical.cod.perks;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * More robust information about the Perk.
 * @see Perk
 * */
public class CodPerk {
	private Perk perk;
	private PerkSlot slot;
	private ItemStack item;
	private int cost;
	
	public CodPerk(Perk perk, ItemStack item, PerkSlot slot, ArrayList<String> lore, int cost) {
		setPerk(perk);
		setItem(item);
		setSlot(slot);
		setCost(cost);
		for (int i = 0; i < lore.size(); i++) {
			String line = lore.get(i);
			lore.set(i, line.replace("&", "\u00A7"));
		}
		perk.setLore(lore);
	}

	public Perk getPerk() {
		return perk;
	}

	public void setPerk(Perk perk) {
		this.perk = perk;
	}

	public ItemStack getItem() {
		ItemStack i = item;
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(getPerk().getName());
		meta.setLore(getLore());
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
		return getPerk().getLore();
	}

	public int getCost() {
		return cost;
	}

	private void setCost(int cost) {
		this.cost = cost;
	}
}
