package com.rhetorical.cod.sounds;

import com.rhetorical.cod.ComWarfare;
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
		Bukkit.getServer().getPluginManager().registerEvents(this, ComWarfare.getPlugin());

		soundDataMap.put("GameEndSoundEvent", new SoundData());
		soundDataMap.put("PlayerDieSoundEvent", new SoundData());
		soundDataMap.put("GameStartSoundEvent", new SoundData());
		soundDataMap.put("PlayerLevelUpSoundEvent", new SoundData());
		soundDataMap.put("PlayerSpawnSoundEvent", new SoundData());
		soundDataMap.put("PlayerPrestigeSoundEvent", new SoundData());
		soundDataMap.put("RoundEndSoundEvent", new SoundData());
		soundDataMap.put("AirstrikeExplodeSoundEvent", new SoundData());
		soundDataMap.put("PlayerHitmarkerSoundEvent", new SoundData());

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
		for (String s : data.getSoundStructMap().keySet()) {
			SoundData.SoundStruct sound = data.getSoundStructMap().get(s);
			if (!sound.sound.equalsIgnoreCase("none") && !sound.sound.equals("") && !sound.sound.isEmpty())
				p.playSound(p.getLocation(), sound.sound, sound.volume, sound.pitch);
		}
	}

	@EventHandler
	public void onPlayerSoundEvent(PlayerSoundEvent e) {
		SoundData data = soundDataMap.get(e.getEventName());
		if (data != null) {
			playSound(e.getPlayer(), data);
		}
	}

	@EventHandler
	public void onGameEnd(GameEndSoundEvent e) {
		SoundData data = soundDataMap.get(e.getEventName());
		if (data != null)
			playSound(e.getPlayer(), data);
	}

	@EventHandler
	public void onGameStart(GameStartSoundEvent e) {
		SoundData data = soundDataMap.get(e.getEventName());
		if (data != null) {
			playSound(e.getPlayer(), data);
		}
	}

	@EventHandler
	public void onRoundEnd(RoundEndSoundEvent e) {
		SoundData data = soundDataMap.get(e.getEventName());
		if (data != null) {
			playSound(e.getPlayer(), data);
		}
	}


}
