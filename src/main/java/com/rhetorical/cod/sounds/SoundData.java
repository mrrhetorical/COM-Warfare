package com.rhetorical.cod.sounds;

import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.SoundFile;
import com.rhetorical.cod.game.Gamemode;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SoundData {

	class SoundStruct {
		String sound;
		float volume;
		float pitch;
	}

	private Map<String, SoundStruct> soundStructMap = new HashMap<>();

	SoundData() {}

	void load(String event) {
		if (event.equalsIgnoreCase("GameEndSoundEvent") || event.equalsIgnoreCase("RoundEndSoundEvent")) {
			if (!SoundFile.getData().contains(event)) {
				SoundFile.getData().set(event + ".victory", "VICTORY::1.0::1.0");
				SoundFile.getData().set(event + ".loss", "LOSS::1.0::1.0");
				SoundFile.saveData();
				SoundFile.reloadData();
			} else {
				String v = SoundFile.getData().getString(event + ".victory"),
						l = SoundFile.getData().getString(event + ".loss");
				SoundStruct victory = deserialize(v),
						loss = deserialize(l);
				if (victory != null)
					soundStructMap.put("victory", victory);
				if (loss != null)
					soundStructMap.put("loss", loss);
			}
			return;
		} else if (event.equalsIgnoreCase("GameStartSoundEvent")) {
			if (!SoundFile.getData().contains(event)) {
				for (int i = 0; i < Gamemode.values().length - 1; i++) {
					Gamemode mode = Gamemode.values()[i];
					SoundFile.getData().set(event + "." + mode.toString().toLowerCase(), mode.toString() + "::1.0::1.0");
				}
				SoundFile.saveData();
				SoundFile.reloadData();
			} else {
				for (int i = 0; i < Gamemode.values().length - 1; i++) {
					Gamemode mode = Gamemode.values()[i];
					String s = SoundFile.getData().getString(event + "." + mode.toString().toLowerCase(), mode.toString() + "::1.0::1.0");
					SoundStruct ss = deserialize(s);
					if (ss != null)
						soundStructMap.put(mode.toString().toLowerCase(), ss);
				}
			}
			return;
		} else {
			if (!SoundFile.getData().contains(event)) {
				SoundFile.getData().set(event, "SOUND_NAME::1.0::1.0");
				SoundFile.saveData();
				SoundFile.reloadData();
			} else {
				String s = SoundFile.getData().getString(event);
				SoundStruct ss = deserialize(s);
				if (ss != null)
					soundStructMap.put("single", ss);
			}
			return;
		}
	}

	public Map<String, SoundStruct> getSoundStructMap() {
		return soundStructMap;
	}

	private String serialize(SoundStruct input) {
		StringBuilder sb = new StringBuilder();
		sb.append(input.sound.toLowerCase());
		sb.append("::");
		sb.append(input.volume);
		sb.append("::");
		sb.append(input.pitch);
		return sb.toString();
	}

	private SoundStruct deserialize(String input) {
		if (input == null)
			return null;
		String[] contents = input.split("::");
		if (contents.length != 3) {
			Main.sendMessage(Main.getConsole(), ChatColor.RED + "Error deserializing sound!", Main.getLang());
			return null;
		}

		String s = contents[0],
				v = contents[1],
				p = contents[2];

		float volume, pitch;
		try {
			volume = Float.parseFloat(v);
			pitch = Float.parseFloat(p);
		} catch (Exception e) {
			Main.sendMessage(Main.getConsole(), ChatColor.RED + "Error deserializing sound!", Main.getLang());
			return null;
		}

		SoundStruct soundStruct = new SoundStruct();
		soundStruct.sound = s;
		soundStruct.volume = volume;
		soundStruct.pitch = pitch;

		return soundStruct;
	}
}
