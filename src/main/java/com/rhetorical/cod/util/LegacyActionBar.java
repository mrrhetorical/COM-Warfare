package com.rhetorical.cod.util;

import com.rhetorical.cod.Main;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

///This class is used to send action bars to users running Minecraft 1.8.8
public class LegacyActionBar {
	private static final Map<Player, BukkitTask> PENDING_MESSAGES = new HashMap<>();

	public static void sendActionBarMessage(Player bukkitPlayer, String message) {
		if (!Main.hasProtocolLib())
			return;
		sendRawActionBarMessage(bukkitPlayer, "{\"text\": \"" + message + "\"}");
	}

	private static void sendRawActionBarMessage(Player player, String rawMessage) {
		if(rawMessage == null || rawMessage.isEmpty()) {
			return;
		}

		com.comphenix.protocol.events.PacketContainer chat = new com.comphenix.protocol.events.PacketContainer(com.comphenix.protocol.PacketType.Play.Server.CHAT);
		chat.getBytes().write(0, (byte)2);
		chat.getChatComponents().write(0, com.comphenix.protocol.wrappers.WrappedChatComponent.fromJson(rawMessage));

		try {
			com.comphenix.protocol.ProtocolLibrary.getProtocolManager().sendServerPacket(player, chat);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

}