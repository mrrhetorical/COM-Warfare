package com.rhetorical.cod.perks;

import java.util.ArrayList;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.lang.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.rhetorical.cod.perks.CodPerk;
import com.rhetorical.cod.perks.Perk;
import com.rhetorical.cod.perks.PerkSlot;

public class PerkManager {

	private ArrayList<CodPerk> availablePerks = new ArrayList<CodPerk>();
	private CodPerk defaultOne;
	private CodPerk defaultTwo;
	private CodPerk defaultThree;

	public PerkManager() {
		this.loadPerks();
	}

	private void loadPerks() {
		for (int i = 0; Main.getPlugin().getConfig().contains("Perks." + i); i++) {
			Perk perk;
			try {
				perk = Perk.valueOf(Main.getPlugin().getConfig().getString("Perks." + i + ".name"));
			} catch (Exception e) {
				Main.sendMessage(Main.cs, Main.codPrefix + Lang.ERROR_READING_PERK_DATA.getMessage(), Main.lang);
				continue;
			}

			int cost = Main.getPlugin().getConfig().getInt("Perks." + i + ".cost");

			String slot = Main.getPlugin().getConfig().getString("Perks." + i + ".slot");

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
				Main.sendMessage(Main.cs, Main.codPrefix + Lang.ERROR_READING_PERK_DATA.getMessage(), Main.lang);
				continue;
			}

			ItemStack item = new ItemStack(Material.valueOf(Main.getPlugin().getConfig().getString("Perks." + i + ".material")));

			ArrayList<String> lore = (ArrayList<String>) Main.getPlugin().getConfig().getStringList("Perks." + i + ".lore");

			CodPerk codPerk = new CodPerk(perk, item, perkSlot, lore, cost);

			availablePerks.add(codPerk);

		}

		this.setDefaultOne(this.getDefaultPerk(PerkSlot.ONE));
		this.setDefaultTwo(this.getDefaultPerk(PerkSlot.TWO));
		this.setDefaultThree(this.getDefaultPerk(PerkSlot.THREE));

		for (int i = 0, k = 3; k < Perk.values().length; i++, k++) {
			if (!Main.getPlugin().getConfig().contains("Perks." + i)) {
				Perk perk = Perk.values()[k];
				PerkSlot slot = PerkSlot.random();
				Main.getPlugin().getConfig().set("Perks." + i + ".name", perk.toString());
				Main.getPlugin().getConfig().set("Perks." + i + ".material", Material.APPLE.toString());
				Main.getPlugin().getConfig().set("Perks." + i + ".cost", 0);
				Main.getPlugin().getConfig().set("Perks." + i + ".slot", slot.toString());
				Main.getPlugin().getConfig().set("Perks." + i + ".lore", new ArrayList<String>());

				availablePerks.add(new CodPerk(perk, new ItemStack(Material.APPLE), slot, new ArrayList<>(), 0));

				Main.getPlugin().saveConfig();
				Main.getPlugin().reloadConfig();
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
				Main.sendMessage(Main.cs, Main.codPrefix + Lang.ERROR_READING_PERK_DATA.getMessage(), Main.lang);
				return null;
		}

		if (Main.getPlugin().getConfig().contains("Perks.default." + s)) {

			Perk name = Perk.valueOf(Main.getPlugin().getConfig().getString("Perks.default." + s + ".name"));
			ItemStack perkItem = new ItemStack(Material.valueOf(Main.getPlugin().getConfig().getString("Perks.default." + s + ".material")));
			int cost = Main.getPlugin().getConfig().getInt("Perks.default." + s + ".cost");
			ArrayList<String> lore = (ArrayList<String>) Main.getPlugin().getConfig().getStringList("Perks.default." + s + ".lore");

			return new CodPerk(name, perkItem, slot, lore, cost);
		} else {
			Main.getPlugin().getConfig().set("Perks.default." + s + ".name", Perk.values()[s - 1].toString());
			Main.getPlugin().getConfig().set("Perks.default." + s + ".material", Material.APPLE.toString());
			Main.getPlugin().getConfig().set("Perks.default." + s + ".slot", slot.toString());
			Main.getPlugin().getConfig().set("Perks.default." + s + ".lore", new ArrayList<String>());

			Main.getPlugin().saveConfig();
			Main.getPlugin().reloadConfig();

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
