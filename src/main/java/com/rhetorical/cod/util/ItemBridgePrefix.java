package com.rhetorical.cod.util;

import com.rhetorical.cod.ComWarfare;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public class ItemBridgePrefix {
	private String prefix;
	private Set<String> weapons = new HashSet<>();

	public ItemBridgePrefix(String prefix) {
		setPrefix(prefix);


		FileConfiguration config = ComWarfare.getInstance().getConfig();

		ConfigurationSection s = config.getConfigurationSection(String.format("itemBridge.prefix.%s", prefix));
		if (s != null)
			weapons.addAll(s.getKeys(false));
	}

	private void setPrefix(String value) {
		prefix = value;
	}

	public String getPrefix() {
		return prefix;
	}

	public Set<String> getWeapons() {
		return weapons;
	}
}
