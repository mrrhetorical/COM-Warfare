package com.rhetorical.cod.util;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormat {

    private static final Pattern pattern = Pattern.compile("\\{#[a-fA-F0-9]{6}}");

    public static String format(String msg) {
        if (Bukkit.getVersion().contains("1.16")) {
            Matcher match = pattern.matcher(msg);
            while (match.find()) {
                String hex = msg.substring(match.start(), match.end());
                msg = StringUtils.replace(msg, hex, "" + ChatColor.of(hex.replace("{", "").replace("}", "")));
                match = pattern.matcher(msg);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}