package com.rhetorical.cod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.bukkit.Bukkit;

class DependencyManager {

	boolean checkDependencies() {
		return !(Bukkit.getServer().getPluginManager().getPlugin("McTranslatePlusPlus") == null /*|| Bukkit.getServer().getPluginManager().getPlugin("mySQL-API") == null*/);
	}

	void downloadDependencies() throws IOException {
		if (Bukkit.getServer().getPluginManager().getPlugin("McTranslatePlusPlus") == null) {
			Main.cs.sendMessage(Main.codPrefix + "§aStarting download of \"McTranslate++\" jar.");
			URL download = new URL("https://www.dropbox.com/s/hnktbtfpsb27tug/mctranslate.jar?dl=1");
			ReadableByteChannel rbc = Channels.newChannel(download.openStream());
			FileOutputStream fos = new FileOutputStream("plugins/mctranslate.jar");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			Main.cs.sendMessage(Main.codPrefix + "§aSuccessfully downloaded \"McTranslate++\" jar.");
			fos.close();
			rbc.close();
		}

//		if (Bukkit.getServer().getPluginManager().getPlugin("mySQL-API") == null) {
//			Main.cs.sendMessage(Main.codPrefix + "§aStarting download of \"Easy MySQL-API\" jar.");
//			URL download = new URL("https://www.dropbox.com/s/be429bx6nj5ggo1/mysqlapi.jar?dl=1");
//			ReadableByteChannel rbc = Channels.newChannel(download.openStream());
//			FileOutputStream fos = new FileOutputStream("plugins/mysqlapi.jar");
//			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//			Main.cs.sendMessage(Main.codPrefix + "§aSuccessfully downloaded \"Easy MySQl-API\".");
//			fos.close();
//			rbc.close();
//		}

		Main.cs.sendMessage(Main.codPrefix + "§aYou've got all dependencies installed! The next time you restart the server, the plugins will be registered!");
	}
}