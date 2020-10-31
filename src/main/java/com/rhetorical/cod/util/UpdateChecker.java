package com.rhetorical.cod.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rhetorical.cod.ComWarfare;

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * This class checks for updates from the GitHub repo for COM-Warfare.
 * @see <a href="https://github.com/mrrhetorical/COM-Warfare">GitHub repo</a>
 * */

public class UpdateChecker {

	public class UpdateResponse {
		boolean newVersion = false;
		String versionNumber = "";
	}

	private static final String updateURL = "https://api.github.com/repos/mrrhetorical/COM-Warfare/releases/latest";

	private volatile UpdateResponse result = null;

	public UpdateChecker() {
		new Thread(() -> {

			Thread.currentThread().setName("COM:W Updater");

			result = checkForUpdates();

			int tries = 0;

			while(result == null && tries < 10) {
				tries++;
				try { Thread.sleep(3000); } catch (Exception ignored) {}
			}

			if (result == null) {
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Could not check most recent version of COM-Warfare!");
			} else if (result.newVersion) {
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Your version of COM-Warfare is not up to date! Please update this plugin for the intended experience!");
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + String.format("Your version of COM-Warfare: %s", ComWarfare.getPlugin().getDescription().getVersion()));
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + String.format("Most recent of COM-Warfare: %s", result.versionNumber));
			} else {
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Your version of COM-Warfare is up to date!");
			}
		}).start();
	}

	public UpdateResponse checkForUpdates() {

		final UpdateResponse res = new UpdateResponse();

		new Thread(() -> {

			try {
				URLConnection connection = new URL(updateURL).openConnection();

				Scanner scanner = new Scanner(connection.getInputStream());

				String response = scanner.useDelimiter("\\A").next();
				scanner.close();
				if (response != null) {
					Gson g = new Gson();
					JsonObject object = g.fromJson(response, JsonObject.class);
					String latest = object.get("tag_name").getAsString();

					String[] current = ComWarfare.getPlugin().getDescription().getVersion().split("\\.");
					String[] remote = latest.split("\\.");

					boolean old = false;

					for (int i = 0; i < 3; i++) {
						int r, c;
						try {
							r = Integer.parseInt(remote[i]);
							c = Integer.parseInt(current[i]);
						} catch (Exception ignored) {
							ignored.printStackTrace();
							return;
						}

						// If remote is newer than current, then the current version is old.
						// Otherwise, if the current is newer than the remote, this is up to date as an indev version.
						if (r > c) {
							old = true;
							break;
						} else if (c > r) {
							break;
						}
					}

					if (old) {
						res.newVersion = true;
						res.versionNumber = latest;
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();

		return res;
	}

}
