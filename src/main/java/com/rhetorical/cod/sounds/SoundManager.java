package com.rhetorical.cod.sounds;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.game.Gamemode;
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
		for (SoundData.SoundStruct sound : data.getSounds()) {
			p.playSound(p.getLocation(), sound.sound, sound.volume, sound.pitch);
		}
	}

	private void playSound(Player p, SoundData data, int index) {
		try {
			SoundData.SoundStruct sound = data.getSounds().get(index);
			p.playSound(p.getLocation(), sound.sound, sound.volume, sound.pitch);
		} catch (Exception|Error ignored) {}
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
		int index = e.isVictory() ? 0 : 1;
		playSound(e.getPlayer(), data, index);
	}

	@EventHandler
	public void onGameStart(GameStartSoundEvent e) {
		Gamemode gm = e.getGamemode();
		int i;
		for (i = 0; i < Gamemode.values().length - 1; i++) {
			if (Gamemode.values()[i] == gm)
				break;
		}

		playSound(e.getPlayer(), soundDataMap.get(e.getEventName()), i);
	}

	@EventHandler
	public void onRoundEnd(RoundEndSoundEvent e) {
		SoundData data = soundDataMap.get(e.getEventName());
		int index = e.isVictory() ? 0 : 1;
		playSound(e.getPlayer(), data, index);
	}


}
