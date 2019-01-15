package com.rhetorical.cod;

import org.bukkit.Bukkit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

class DependencyManager {

	boolean checkDependencies() {
		return !(Bukkit.getServer().getPluginManager().getPlugin("McTranslatePlusPlus") == null /*|| Bukkit.getServer().getPluginManager().getPlugin("mySQL-API") == null*/);
	}

	void downloadDependencies() throws IOException {
		if (Bukkit.getServer().getPluginManager().getPlugin("McTranslatePlusPlus") == null) {
			Main.cs.sendMessage(Main.codPrefix + "\u00A7aStarting download of \"McTranslate++\" jar.");
			URL download = new URL("https://www.dropbox.com/s/hnktbtfpsb27tug/mctranslate.jar?dl=1");
			ReadableByteChannel rbc = Channels.newChannel(download.openStream());
			FileOutputStream fos = new FileOutputStream("plugins/mctranslate.jar");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			Main.cs.sendMessage(Main.codPrefix + "\u00A7aSuccessfully downloaded \"McTranslate++\" jar.");
			fos.close();
			rbc.close();
		}

//		if (Bukkit.getServer().getPluginManager().getPlugin("mySQL-API") == null) {
//			Main.cs.sendMessage(Main.codPrefix + "\u00A7aStarting download of \"Easy MySQL-API\" jar.");
//			URL download = new URL("https://www.dropbox.com/s/be429bx6nj5ggo1/mysqlapi.jar?dl=1");
//			ReadableByteChannel rbc = Channels.newChannel(download.openStream());
//			FileOutputStream fos = new FileOutputStream("plugins/mysqlapi.jar");
//			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//			Main.cs.sendMessage(Main.codPrefix + "\u00A7aSuccessfully downloaded \"Easy MySQl-API\".");
//			fos.close();
//			rbc.close();
//		}

		Main.cs.sendMessage(Main.codPrefix + "\u00A7aYou've got all dependencies installed! The next time you restart the server, the plugins will be registered!");
	}
}