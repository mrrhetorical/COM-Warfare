package com.rhetorical.cod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UUIDFetcher {
    public static String getUUID(String name) throws IOException {
        StringBuilder uuid = new StringBuilder();
        InputStream is;
        is = new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream();
        JsonElement root = new JsonParser().parse(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
        for (int i = 0; i <= 31; i++) {
            uuid.append(root.getAsJsonObject().get("id").getAsString().charAt(i));
            if (i == 7 || i == 11 || i == 15 || i == 19) uuid.append("-");
        }
        is.close();
        return uuid.toString();
    }
}
