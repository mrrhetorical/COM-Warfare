package com.rhetorical.cod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.bukkit.Bukkit;

public class DependencyManager {

	public boolean checkDependencies() {
		boolean hasDependencies = true;
		if (Bukkit.getServer().getPluginManager().getPlugin("McTranslatePlusPlus") == null || Bukkit.getServer().getPluginManager().getPlugin("mySQL-API") == null) {
			hasDependencies = false;
		}
		return hasDependencies;
	}

	public void downloadDependencies() throws IOException {
		if (Bukkit.getServer().getPluginManager().getPlugin("McTranslatePlusPlus") == null) {
			Main.cs.sendMessage(Main.codPrefix + "§aStarting download of \"McTranslate++\" jar.");
			URL download = new URL("https://www.dropbox.com/s/oaaazjgkfdpq4eq/mctranslateplusplus.jar?dl=1");
			ReadableByteChannel rbc = Channels.newChannel(download.openStream());
			FileOutputStream fos = new FileOutputStream("plugins/mctranslateplusplus.jar");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			Main.cs.sendMessage(Main.codPrefix + "§aSuccessfully downloaded \"McTranslate++\" jar.");
			fos.close();
			rbc.close();
		}

		if (Bukkit.getServer().getPluginManager().getPlugin("mySQL-API") == null) {
			Main.cs.sendMessage(Main.codPrefix + "§aStarting download of \"Easy MySQL-API\" jar.");
			URL download = new URL("https://www.dropbox.com/s/hewpvkkzn5uqopc/mysqlapi.jar?dl=1");
			ReadableByteChannel rbc = Channels.newChannel(download.openStream());
			FileOutputStream fos = new FileOutputStream("plugins/easymysqlapi.jar");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			Main.cs.sendMessage(Main.codPrefix + "§aSuccessfully downloaded \"Easy MySQl-API\".");
			fos.close();
			rbc.close();
		}
	}
}