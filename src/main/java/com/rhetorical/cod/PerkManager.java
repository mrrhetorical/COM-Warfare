package com.rhetorical.cod;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.rhetorical.cod.object.CodPerk;
import com.rhetorical.cod.object.Perk;
import com.rhetorical.cod.object.PerkSlot;

public class PerkManager {

	// private HashMap<Perk, Integer> perkCost = new HashMap<Perk, Integer>();
	// private HashMap<Perk, PerkSlot> perkSlot = new HashMap<Perk, PerkSlot>();
	// private HashMap<Perk, ItemStack> perkItem = new HashMap<Perk, ItemStack>();
	// private HashMap<Perk, ArrayList<String>> perkLore = new HashMap<Perk, ArrayList<String>>();
	private ArrayList<CodPerk> availablePerks = new ArrayList<CodPerk>();
	private CodPerk defaultOne;
	private CodPerk defaultTwo;
	private CodPerk defaultThree;

	PerkManager() {
		this.loadPerks();
	}

	private void loadPerks() {
		for (int i = 0; Main.getPlugin().getConfig().contains("Perks." + i); i++) {
			Perk perk;
			try {
				perk = Perk.valueOf(Main.getPlugin().getConfig().getString("Perks." + i + ".name"));
			} catch (Exception e) {
				Main.sendMessage(Main.cs, Main.codPrefix + "§cThere was an error reading the perk data from the config. The file may be corrupted!", Main.lang);
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
				Main.sendMessage(Main.cs, Main.codPrefix + "§cThere was an error reading the perk slot data from the config. The file may be corrupted!", Main.lang);
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

		int i = 0;
		while (availablePerks.size() < Perk.values().length) {
			if (!Main.getPlugin().getConfig().contains("Perks." + i) && i < 17) {
				Main.getPlugin().getConfig().set("Perks." + i + ".name", Perk.values()[i].toString());
				Main.getPlugin().getConfig().set("Perks." + i + ".material", Material.APPLE.toString());
				Main.getPlugin().getConfig().set("Perks." + i + ".cost", 0);
				Main.getPlugin().getConfig().set("Perks." + i + ".slot", PerkSlot.random().toString());
				Main.getPlugin().getConfig().set("Perks." + i + ".lore", new ArrayList<String>());

				Main.getPlugin().saveConfig();
				Main.getPlugin().reloadConfig();

				i--;
			}

			i++;
		}
	}

	public CodPerk getDefaultTwo() {
		return defaultTwo;
	}

	private void setDefaultTwo(CodPerk defaultTwo) {
		this.defaultTwo = defaultTwo;
	}

	CodPerk getDefaultPerk(PerkSlot slot) {

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
			Main.sendMessage(Main.cs, Main.codPrefix + "§cThere was an error loading the perk slot data from the config.yml! The file may be corrupted!", Main.lang);
			return null;
		}

		if (Main.getPlugin().getConfig().contains("Perks.default." + s)) {

			Perk name = Perk.valueOf(Main.getPlugin().getConfig().getString("Perks.default." + s + ".name"));
			ItemStack perkItem = new ItemStack(Material.valueOf(Main.getPlugin().getConfig().getString("Perks.default." + s + ".material")));
			int cost = Main.getPlugin().getConfig().getInt("Perks.default." + s + ".cost");
			ArrayList<String> lore = (ArrayList<String>) Main.getPlugin().getConfig().getStringList("Perks.default." + s + ".lore");

			return new CodPerk(name, perkItem, slot, lore, cost);
		} else {
			Main.getPlugin().getConfig().set("Perks.default." + s + ".name", Perk.values()[s].toString());
			Main.getPlugin().getConfig().set("Perks.default." + s + ".material", Material.APPLE.toString());
			Main.getPlugin().getConfig().set("Perks.default." + s + ".slot", slot.toString());
			Main.getPlugin().getConfig().set("Perks.default." + s + ".lore", new ArrayList<String>());

			Main.getPlugin().saveConfig();
			Main.getPlugin().reloadConfig();
		}

		return null;

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
