package com.rhetorical.cod.sounds;


import com.rhetorical.cod.Main;
import com.rhetorical.cod.files.SoundFile;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

class SoundData {

	class SoundStruct {
		String sound;
		float volume;
		float pitch;
	}

	private List<SoundStruct> sounds = new ArrayList<>();

	SoundData() {}

	void load(String event) {
		if (!SoundFile.getData().contains(event)) {
			SoundFile.getData().set(event, sounds);
			SoundFile.saveData();
			SoundFile.reloadData();
			return;
		}
		List<String> unparsed = SoundFile.getData().getStringList(event);
		for (String s : unparsed) {
			SoundStruct struct = deserialize(s);
			if (struct == null)
				continue;
			sounds.add(struct);
		}
	}

	public List<SoundStruct> getSounds() {
		return sounds;
	}

	private String serialize(SoundStruct input) {
		StringBuilder sb = new StringBuilder();
		sb.append(input.sound.toString().toLowerCase());
		sb.append("::");
		sb.append(input.volume);
		sb.append("::");
		sb.append(input.pitch);
		return sb.toString();
	}

	private SoundStruct deserialize(String input) {
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
