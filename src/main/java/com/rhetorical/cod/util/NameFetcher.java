package com.rhetorical.cod.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NameFetcher {
    public static String getName(String uuid) throws IOException {
        try (InputStream is = new URL("https://mcapi.ca/player/profile/" + uuid).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(rd);
            JsonObject rootobj = root.getAsJsonObject();
            return rootobj.get("name").getAsString();
        }
    }
}