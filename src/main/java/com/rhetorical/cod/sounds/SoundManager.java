package com.rhetorical.cod.sounds;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.sounds.events.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.HashMap;


public class SoundManager implements Listener {

	private static SoundManager instance;

	private Map<String, SoundData> soundDataMap = new HashMap<>();

	private SoundManager() {
		Bukkit.getServer().getPluginManager().registerEvents(this, Main.getPlugin());

		soundDataMap.put("GameEndSoundEvent", new SoundData());
		soundDataMap.put("PlayerDieSoundEvent", new SoundData());
		soundDataMap.put("GameStartSoundEvent", new SoundData());
		soundDataMap.put("PlayerLevelUpSoundEvent", new SoundData());
		soundDataMap.put("PlayerSpawnSoundEvent", new SoundData());
		soundDataMap.put("PlayerPrestigeSoundEvent", new SoundData());

		for(String c : soundDataMap.keySet()) {
			soundDataMap.get(c).load(c);
		}
	}

	public static SoundManager getInstance() {
		if (instance == null)
			instance = new SoundManager();

		return instance;
	}

	private void playSound(Player p, SoundData data) {
		for (SoundData.SoundStruct sound : data.getSounds()) {
			p.playSound(p.getLocation(), sound.sound, sound.volume, sound.pitch);
		}
	}


	@EventHandler
	public void onPlayerLevelUp(PlayerLevelUpSoundEvent e) {

		SoundData data = soundDataMap.get("PlayerLevelUpSoundEvent");
		if (data != null) {
			playSound(e.getPlayer(), data);
		}
	}
}
