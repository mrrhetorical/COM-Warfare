package com.rhetorical.cod.perks;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.lang.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * Manages perks for shops and stuff
 * */

public class PerkManager {

	private static PerkManager instance;

	private ArrayList<CodPerk> availablePerks = new ArrayList<CodPerk>();
	private CodPerk defaultOne;
	private CodPerk defaultTwo;
	private CodPerk defaultThree;

	public PerkManager() {
		if (instance == null)
			instance = this;

		this.loadPerks();
	}

	public static PerkManager getInstance() {
		return instance != null ? instance : new PerkManager();
	}

	private void loadPerks() {
		for (int i = 0; ComWarfare.getPlugin().getConfig().contains("Perks." + i); i++) {
			Perk perk;
			try {
				perk = Perk.valueOf(ComWarfare.getPlugin().getConfig().getString("Perks." + i + ".name"));
			} catch (Exception e) {
				ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + Lang.ERROR_READING_PERK_DATA.getMessage(), ComWarfare.getLang());
				continue;
			}

			int cost = ComWarfare.getPlugin().getConfig().getInt("Perks." + i + ".cost");

			String slot = ComWarfare.getPlugin().getConfig().getString("Perks." + i + ".slot");

			int customModelData = ComWarfare.getPlugin().getConfig().getInt("Perks." + i + ".customModelData", 0);


			PerkSlot perkSlot;

			switch (slot) {
			case "ONE":
				perkSlot = PerkSlot.ONE;
				break;
			case "TWO":
				perkSlot = PerkSlot.TWO;
				break;
			case "THREE":
				perkSlot = PerkSlot.THREE;
				break;
			default:
				ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + Lang.ERROR_READING_PERK_DATA.getMessage(), ComWarfare.getLang());
				continue;
			}

			ItemStack item = new ItemStack(Material.valueOf(ComWarfare.getPlugin().getConfig().getString("Perks." + i + ".material")));

			if (ComWarfare.canUseCustomModelData()) {
				ItemMeta meta = item.getItemMeta();
				meta.setCustomModelData(customModelData);
				item.setItemMeta(meta);
			}

			ArrayList<String> lore = (ArrayList<String>) ComWarfare.getPlugin().getConfig().getStringList("Perks." + i + ".lore");

			CodPerk codPerk = new CodPerk(perk, item, perkSlot, lore, cost);

			availablePerks.add(codPerk);

		}

		this.setDefaultOne(this.getDefaultPerk(PerkSlot.ONE));
		this.setDefaultTwo(this.getDefaultPerk(PerkSlot.TWO));
		this.setDefaultThree(this.getDefaultPerk(PerkSlot.THREE));

		for (int i = 0, k = 3; k < Perk.values().length; i++, k++) {
			if (!ComWarfare.getPlugin().getConfig().contains("Perks." + i)) {
				Perk perk = Perk.values()[k];
				PerkSlot slot = PerkSlot.random();
				ComWarfare.getPlugin().getConfig().set("Perks." + i + ".name", perk.toString());
				ComWarfare.getPlugin().getConfig().set("Perks." + i + ".material", Material.APPLE.toString());
				ComWarfare.getPlugin().getConfig().set("Perks." + i + ".cost", 0);
				ComWarfare.getPlugin().getConfig().set("Perks." + i + ".slot", slot.toString());
				ComWarfare.getPlugin().getConfig().set("Perks." + i + ".lore", perk.getLore());

				availablePerks.add(new CodPerk(perk, new ItemStack(Material.APPLE), slot, new ArrayList<>(), 0));

				ComWarfare.getPlugin().saveConfig();
				ComWarfare.getPlugin().reloadConfig();
			}
		}

		availablePerks.add(this.defaultOne);
		availablePerks.add(this.defaultTwo);
		availablePerks.add(this.defaultThree);

		System.out.println("Loaded " + availablePerks.size() + " perks!");
	}

	public CodPerk getDefaultTwo() {
		return defaultTwo;
	}

	private void setDefaultTwo(CodPerk defaultTwo) {
		this.defaultTwo = defaultTwo;
	}

	public CodPerk getDefaultPerk(PerkSlot slot) {

		int s;

		switch (slot) {
			case ONE:
				s = 1;
				break;
			case TWO:
				s = 2;
				break;
			case THREE:
				s = 3;
				break;
			default:
				ComWarfare.sendMessage(ComWarfare.getConsole(), ComWarfare.getPrefix() + Lang.ERROR_READING_PERK_DATA.getMessage(), ComWarfare.getLang());
				return null;
		}

		if (ComWarfare.getPlugin().getConfig().contains("Perks.default." + s)) {

			Perk name = Perk.valueOf(ComWarfare.getPlugin().getConfig().getString("Perks.default." + s + ".name"));
			ItemStack perkItem = new ItemStack(Material.valueOf(ComWarfare.getPlugin().getConfig().getString("Perks.default." + s + ".material")));
			int cost = ComWarfare.getPlugin().getConfig().getInt("Perks.default." + s + ".cost");
			ArrayList<String> lore = (ArrayList<String>) ComWarfare.getPlugin().getConfig().getStringList("Perks.default." + s + ".lore");

			return new CodPerk(name, perkItem, slot, lore, cost);
		} else {
			ComWarfare.getPlugin().getConfig().set("Perks.default." + s + ".name", Perk.values()[s - 1].toString());
			ComWarfare.getPlugin().getConfig().set("Perks.default." + s + ".material", Material.APPLE.toString());
			ComWarfare.getPlugin().getConfig().set("Perks.default." + s + ".slot", slot.toString());
			ComWarfare.getPlugin().getConfig().set("Perks.default." + s + ".lore", Perk.values()[s - 1].getLore());

			ComWarfare.getPlugin().saveConfig();
			ComWarfare.getPlugin().reloadConfig();

			return getDefaultPerk(slot);
		}

		// CodPerk perk = GunsFile.getData().getString("Perks." + s + ".default.name");
	}

	public ArrayList<CodPerk> getAvailablePerks() {
		return availablePerks;
	}

	public void setAvailablePerks(ArrayList<CodPerk> availablePerks) {
		this.availablePerks = availablePerks;
	}

	public CodPerk getDefaultThree() {
		return defaultThree;
	}

	private void setDefaultThree(CodPerk defaultThree) {
		this.defaultThree = defaultThree;
	}

	public CodPerk getDefaultOne() {
		return defaultOne;
	}

	private void setDefaultOne(CodPerk defaultOne) {
		this.defaultOne = defaultOne;
	}
}
