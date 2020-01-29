package com.rhetorical.cod.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rhetorical.cod.ComWarfare;

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * This class checks for updates from the GitHub repo for COM-Warfare.
 * @see <a href=https://github.com/mrrhetorical/COM-Warfare/">GitHub repo</a>
 * */

public class UpdateChecker {

	public class UpdateResponse {
		boolean newVersion = false;
		String versionNumber = "";
	}

	private static final String updateURL = "https://api.github.com/repos/mrrhetorical/COM-Warfare/releases/latest";

	public UpdateChecker() {
		final UpdateResponse[] result = {null};
		new Thread(() -> {

			result[0] = checkForUpdates();

			try { Thread.sleep(10000); } catch (Exception ignored) {}

			if (result[0].newVersion) {
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Your version of COM-Warfare is not up to date! Please update this plugin for the intended experience!");
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + String.format("Your version of COM-Warfare: %s", ComWarfare.getPlugin().getDescription().getVersion()));
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + String.format("Most recent of COM-Warfare: %s", result[0].versionNumber));
			} else {
				ComWarfare.getConsole().sendMessage(ComWarfare.getPrefix() + "Your version of COM-Warfare is up to date!");
			}
		}).start();
	}

	public UpdateResponse checkForUpdates() {

		final UpdateResponse[] res = {new UpdateResponse()};

		new Thread(() -> {

			try {
				URLConnection connection = new URL(updateURL).openConnection();

				Scanner scanner = new Scanner(connection.getInputStream());

				String response = scanner.useDelimiter("\\A").next();
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

						if (r > c) {
							old = true;
							break;
						}
					}

					if (old) {
						res[0].newVersion = true;
						res[0].versionNumber = latest;
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();

		return res[0];
	}

}
